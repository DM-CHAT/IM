//
//  DMCCTransferGroupOwnerNotificationContent.m
//  DMChatClient
//
//  Created by heavyrain on 2017/9/20.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCTransferGroupOwnerNotificationContent.h"
#import "DMCCIMService.h"
#import "DMCCNetworkService.h"
#import "Common.h"
#import <config/config.h>

@implementation DMCCTransferGroupOwnerNotificationContent
- (DMCCMessagePayload *)encode {
    DMCCMessagePayload *payload = [super encode];
    payload.contentType = [self.class getContentType];
    
    NSMutableDictionary *dataDict = [NSMutableDictionary dictionary];
    if (self.operateUser) {
        [dataDict setObject:self.operateUser forKey:@"o"];
    }
    if (self.owner) {
        [dataDict setObject:self.owner forKey:@"m"];
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
        self.owner = dictionary[@"m"];
        self.groupId = dictionary[@"g"];
    }
}

+ (int)getContentType {
    return MESSAGE_CONTENT_TYPE_TRANSFER_GROUP_OWNER;
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
        DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:self.owner inGroup:self.groupId refresh:NO];
        if (userInfo.friendAlias.length > 0) {
            formatMsg = [NSString stringWithFormat:@"%@%@", LocalizedString(@"you_transfer_group_owner"),userInfo.friendAlias];
        } else if(userInfo.groupAlias.length > 0) {
            formatMsg = [NSString stringWithFormat:@"%@%@", LocalizedString(@"you_transfer_group_owner"),userInfo.groupAlias];
        } else if (userInfo.displayName.length > 0) {
            formatMsg = [NSString stringWithFormat:@"%@%@", LocalizedString(@"you_transfer_group_owner"),userInfo.displayName];
        } else {
            formatMsg = [NSString stringWithFormat:@"%@%@", LocalizedString(@"you_transfer_group_owner"),self.owner];
        }
    } else {
        DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:self.operateUser inGroup:self.groupId refresh:NO];
        if (userInfo.friendAlias.length > 0) {
            formatMsg = [NSString stringWithFormat:@"%@%@", userInfo.friendAlias, LocalizedString(@"transfer_group_owner")];
        } else if(userInfo.groupAlias.length > 0) {
            formatMsg = [NSString stringWithFormat:@"%@%@", userInfo.groupAlias, LocalizedString(@"transfer_group_owner")];
        } else if (userInfo.displayName.length > 0) {
            formatMsg = [NSString stringWithFormat:@"%@%@", userInfo.displayName, LocalizedString(@"transfer_group_owner")];
        } else {
            formatMsg = [NSString stringWithFormat:@"%@%@", self.operateUser, LocalizedString(@"transfer_group_owner")];
        }
        
        if ([[DMCCNetworkService sharedInstance].userId isEqualToString:self.owner]) {
            formatMsg = [formatMsg stringByAppendingString:@"你"];
        } else {
            userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:self.owner inGroup:self.groupId refresh:NO];
            if (userInfo.friendAlias.length > 0) {
                formatMsg = [formatMsg stringByAppendingString:userInfo.friendAlias];
            } else if(userInfo.groupAlias.length > 0) {
                formatMsg = [formatMsg stringByAppendingString:userInfo.groupAlias];
            } else if (userInfo.displayName.length > 0) {
                formatMsg = [formatMsg stringByAppendingString:userInfo.displayName];
            } else {
                formatMsg = [formatMsg stringByAppendingString:self.owner];
            }
        }
    }
    
    return formatMsg;
}
@end
