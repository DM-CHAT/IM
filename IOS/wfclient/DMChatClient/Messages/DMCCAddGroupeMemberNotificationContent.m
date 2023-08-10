//
//  DMCCAddGroupeMemberNotificationContent.m
//  DMChatClient
//
//  Created by heavyrain on 2017/9/20.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCAddGroupeMemberNotificationContent.h"
#import "DMCCIMService.h"
#import "DMCCNetworkService.h"
#import "Common.h"
#import <config/config.h>

@implementation DMCCAddGroupeMemberNotificationContent
- (DMCCMessagePayload *)encode {
    DMCCMessagePayload *payload = [super encode];
    payload.contentType = [self.class getContentType];
    
    NSMutableDictionary *dataDict = [NSMutableDictionary dictionary];
    if (self.invitor) {
        [dataDict setObject:self.invitor forKey:@"o"];
    }
    
    if (self.invitees) {
        [dataDict setObject:self.invitees forKey:@"ms"];
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
        self.invitor = dictionary[@"o"];
        self.invitees = dictionary[@"ms"];
        self.groupId = dictionary[@"g"];
    }
}

+ (int)getContentType {
    return MESSAGE_CONTENT_TYPE_ADD_GROUP_MEMBER;
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
    NSString *formatMsg = @""; 
//    if ([self.invitees count] == 1 && [[self.invitees objectAtIndex:0] isEqualToString:self.invitor]) {
//        if ([[DMCCNetworkService sharedInstance].userId isEqualToString:self.invitor]) {
//            formatMsg = LocalizedString(@"you_joined_group");
//        } else {
//            DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:self.invitor inGroup:self.groupId refresh:NO];
//            NSString *joinGroupString = LocalizedString(@"joined_group");
//            if (userInfo.friendAlias.length > 0) {
//                formatMsg = [NSString stringWithFormat:@"%@%@", userInfo.friendAlias, joinGroupString];
//            } else if(userInfo.groupAlias.length > 0) {
//                formatMsg = [NSString stringWithFormat:@"%@%@", userInfo.groupAlias, joinGroupString];
//            } else if (userInfo.displayName.length > 0) {
//                formatMsg = [NSString stringWithFormat:@"%@%@", userInfo.displayName, joinGroupString];
//            } else {
//                formatMsg = [NSString stringWithFormat:@"%@%@", self.invitor, joinGroupString];
//            }
//        }
//        return formatMsg;
//    }
//
//    if ([[DMCCNetworkService sharedInstance].userId isEqualToString:self.invitor]) {
//        formatMsg = LocalizedString(@"you_invite");
//    } else {
//        DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:self.invitor refresh:NO];
//        if (userInfo.displayName.length > 0) {
//            formatMsg = [NSString stringWithFormat:@"%@%@", userInfo.displayName, LocalizedString(@"invite")];
//        } else {
//            formatMsg = [NSString stringWithFormat:@"%@%@", self.invitor, LocalizedString(@"invite")];
//        }
//    }
//
    
    int count = 0;
    if([self.invitees containsObject:[DMCCNetworkService sharedInstance].userId]) {
        formatMsg = [formatMsg stringByAppendingString:LocalizedString(@"you")];
        count++;
    }

    for (NSString *member in self.invitees) {
        if ([member isEqualToString:[DMCCNetworkService sharedInstance].userId]) {
            continue;
        } else {
            DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:member refresh:NO];
            if (userInfo.displayName.length > 0) {
                formatMsg = [formatMsg stringByAppendingFormat:@" %@", userInfo.displayName];
            } else {
                formatMsg = [formatMsg stringByAppendingFormat:@" %@", member];
            }
            
            count++;
            if(count >= 4) {
                break;
            }
        }
    }
    
    if(self.invitees.count > count) {
        formatMsg = [formatMsg stringByAppendingFormat:LocalizedString(@"groupCountmember"), self.invitees.count];
    }
    formatMsg = [formatMsg stringByAppendingString:LocalizedString(@"joined_group")];
    return formatMsg;
}
@end
