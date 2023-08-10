//
//  DMCCKickoffGroupMemberNotificationContent.m
//  DMChatClient
//
//  Created by heavyrain on 2017/9/20.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCKickoffGroupMemberNotificationContent.h"
#import "DMCCIMService.h"
#import "DMCCNetworkService.h"
#import "Common.h"
#import <config/config.h>

@implementation DMCCKickoffGroupMemberNotificationContent
- (DMCCMessagePayload *)encode {
    DMCCMessagePayload *payload = [super encode];
    payload.contentType = [self.class getContentType];
    
    NSMutableDictionary *dataDict = [NSMutableDictionary dictionary];
    if (self.operateUser) {
        [dataDict setObject:self.operateUser forKey:@"o"];
    }
    if (self.kickedMembers) {
        [dataDict setObject:self.kickedMembers forKey:@"ms"];
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
        self.kickedMembers = dictionary[@"ms"];
        self.groupId = dictionary[@"g"];
    }
}

+ (int)getContentType {
    return MESSAGE_CONTENT_TYPE_KICKOF_GROUP_MEMBER;
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
        formatMsg = [NSString stringWithFormat:@"%@%@", LocalizedString(@"you"),LocalizedString(@"dose")];
    } else {
        DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:self.operateUser inGroup:self.groupId refresh:NO];
        if (userInfo.friendAlias.length > 0) {
            formatMsg = [NSString stringWithFormat:@"%@%@", userInfo.friendAlias, LocalizedString(@"dose")];
        } else if(userInfo.groupAlias.length > 0) {
            formatMsg = [NSString stringWithFormat:@"%@%@", userInfo.groupAlias, LocalizedString(@"dose")];
        } else if (userInfo.displayName.length > 0) {
            formatMsg = [NSString stringWithFormat:@"%@%@", userInfo.displayName, LocalizedString(@"dose")];
        } else {
            formatMsg = [NSString stringWithFormat:@"%@<%@>%@", LocalizedString(@"user"), self.operateUser, LocalizedString(@"dose")];
        }
    }
    
    int count = 0;
    if([self.kickedMembers containsObject:[DMCCNetworkService sharedInstance].userId]) {
        formatMsg = [formatMsg stringByAppendingString:LocalizedString(@"you")];
        count++;
    }
    for (NSString *member in self.kickedMembers) {
        if ([member isEqualToString:[DMCCNetworkService sharedInstance].userId]) {
            continue;
        } else {
            DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:member inGroup:self.groupId refresh:NO];
            if (userInfo.friendAlias.length > 0) {
                formatMsg = [formatMsg stringByAppendingFormat:@" %@", userInfo.friendAlias];
            } else if(userInfo.groupAlias.length > 0) {
                formatMsg = [formatMsg stringByAppendingFormat:@" %@", userInfo.groupAlias];
            } else if (userInfo.displayName.length > 0) {
                formatMsg = [formatMsg stringByAppendingFormat:@" %@", userInfo.displayName];
            } else {
                formatMsg = [formatMsg stringByAppendingFormat:@" %@<%@>", LocalizedString(@"user"),member];
            }
            count++;
            if(count >= 4) {
                break;
            }
        }
    }
    if(self.kickedMembers.count > count) {
        formatMsg = [formatMsg stringByAppendingFormat:LocalizedString(@"groupCountmember"), self.kickedMembers.count];
    }
    
    formatMsg = [formatMsg stringByAppendingString:LocalizedString(@"remove_member")];
    return formatMsg;
}
@end
