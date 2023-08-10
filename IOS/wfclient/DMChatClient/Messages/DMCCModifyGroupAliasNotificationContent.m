//
//  DMCCModifyGroupAliasNotificationContent.m
//  DMChatClient
//
//  Created by heavyrain on 2017/9/20.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCModifyGroupAliasNotificationContent.h"
#import "DMCCIMService.h"
#import "DMCCNetworkService.h"
#import "Common.h"

@implementation DMCCModifyGroupAliasNotificationContent
- (DMCCMessagePayload *)encode {
    DMCCMessagePayload *payload = [super encode];
    payload.contentType = [self.class getContentType];
    
    NSMutableDictionary *dataDict = [NSMutableDictionary dictionary];
    if (self.operateUser) {
        [dataDict setObject:self.operateUser forKey:@"o"];
    }
    if (self.alias) {
        [dataDict setObject:self.alias forKey:@"n"];
    }
    
    if (self.groupId) {
        [dataDict setObject:self.groupId forKey:@"g"];
    }
    
    if (self.memberId) {
        [dataDict setObject:self.memberId forKey:@"m"];
    }
    
    
    payload.binaryContent = [NSJSONSerialization dataWithJSONObject:dataDict
                                                            options:kNilOptions
                                                              error:nil];
    
    return payload;
}

- (void)decode:(DMCCMessagePayload *)payload {
    [super decode:payload];
    NSError *__error = nil;
    NSDictionary *dictionary = [NSJSONSerialization JSONObjectWithData:payload.binaryContent
                                                               options:kNilOptions
                                                                 error:&__error];
    if (!__error) {
        self.operateUser = dictionary[@"o"];
        self.alias = dictionary[@"n"];
        self.groupId = dictionary[@"g"];
        self.memberId = dictionary[@"m"];
    }
}

+ (int)getContentType {
    return MESSAGE_CONTENT_TYPE_MODIFY_GROUP_ALIAS;
}

+ (int)getContentFlags {
    return DMCCPersistFlag_PERSIST;
}



+ (void)load {
    [[DMCCIMService sharedDMCIMService] registerMessageContent:self];
}

- (NSString *)digest:(DMCCMessage *)message {
    return [self formatNotification:message];
}

- (NSString *)formatNotification:(DMCCMessage *)message {
    NSString *formatMsg;
    if ([[DMCCNetworkService sharedInstance].userId isEqualToString:self.operateUser]) {
        formatMsg = @"你修改";
    } else {
        DMCCUserInfo *userInfo;
        if([self.operateUser isEqualToString:self.memberId]) {
            userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:self.operateUser refresh:NO];
        } else {
            userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:self.operateUser inGroup:self.groupId refresh:NO];
        }
        
        if (self.memberId.length && userInfo.groupAlias.length) {
            formatMsg = [NSString stringWithFormat:@"%@修改", userInfo.groupAlias];
        } else if (userInfo.friendAlias.length > 0) {
            formatMsg = [NSString stringWithFormat:@"%@修改", userInfo.friendAlias];
        } else if (userInfo.displayName.length > 0) {
            formatMsg = [NSString stringWithFormat:@"%@修改", userInfo.displayName];
        } else {
            formatMsg = [NSString stringWithFormat:@"%@修改", self.operateUser];
        }
    }
    
    if (self.memberId.length && ![self.memberId isEqualToString:self.operateUser]) {
        if ([[DMCCNetworkService sharedInstance].userId isEqualToString:self.memberId]) {
            formatMsg = [formatMsg stringByAppendingFormat:@"%@的", @"你"];
        } else {
            DMCCUserInfo *member = [[DMCCIMService sharedDMCIMService] getUserInfo:self.memberId refresh:NO];
            if (member.friendAlias.length > 0) {
                formatMsg = [formatMsg stringByAppendingFormat:@"%@的", member.friendAlias];
            } else if (member.displayName.length > 0) {
                formatMsg = [formatMsg stringByAppendingFormat:@"%@的", member.displayName];
            } else {
                formatMsg = [formatMsg stringByAppendingFormat:@"%@的", self.memberId];
            }
        }
    }
    
    formatMsg = [formatMsg stringByAppendingFormat:@"群昵称为%@", self.alias];
    return formatMsg;
}
@end
