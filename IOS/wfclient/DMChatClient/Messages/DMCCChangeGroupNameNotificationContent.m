//
//  DMCCChangeGroupNameNotificationContent.m
//  DMChatClient
//
//  Created by heavyrain on 2017/9/20.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCChangeGroupNameNotificationContent.h"
#import "DMCCIMService.h"
#import "DMCCNetworkService.h"
#import "Common.h"
#import <config/config.h>

@implementation DMCCChangeGroupNameNotificationContent
- (DMCCMessagePayload *)encode {
    DMCCMessagePayload *payload = [super encode];
    payload.contentType = [self.class getContentType];
    
    NSMutableDictionary *dataDict = [NSMutableDictionary dictionary];
    if (self.operateUser) {
        [dataDict setObject:self.operateUser forKey:@"o"];
    }
    if (self.name) {
        [dataDict setObject:self.name forKey:@"n"];
    }
    
    if (self.groupId) {
        [dataDict setObject:self.groupId forKey:@"g"];
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
        self.name = dictionary[@"n"];
        if (self.name == nil) {
            self.name = @"";
        }
        self.groupId = dictionary[@"g"];
    }
}

+ (int)getContentType {
    return MESSAGE_CONTENT_TYPE_CHANGE_GROUP_NAME;
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
        formatMsg = [NSString stringWithFormat:@"%@%@：%@", LocalizedString(@"you"),LocalizedString(@"ModifyGroupName"),self.name];
    } else {
        DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:self.operateUser inGroup:self.groupId refresh:NO];
        if (userInfo.friendAlias.length > 0) {
            formatMsg = [NSString stringWithFormat:@"%@%@：", userInfo.friendAlias, LocalizedString(@"ModifyGroupName")];
        } else if(userInfo.groupAlias.length > 0) {
            formatMsg = [NSString stringWithFormat:@"%@%@：", userInfo.groupAlias, LocalizedString(@"ModifyGroupName")];
        } else if (userInfo.displayName.length > 0) {
            formatMsg = [NSString stringWithFormat:@"%@%@：", userInfo.displayName, LocalizedString(@"ModifyGroupName")];
        } else {
            formatMsg = [NSString stringWithFormat:@"%@%@：", self.operateUser, LocalizedString(@"ModifyGroupName")];
        }
        
        formatMsg = [formatMsg stringByAppendingString:self.name];
    }
    
    return formatMsg;
}
@end
