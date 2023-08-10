//
//  DMCCCreateGroupNotificationContent.m
//  DMChatClient
//
//  Created by heavyrain on 2017/9/19.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCGroupMemberMuteNotificationContent.h"
#import "DMCCIMService.h"
#import "DMCCNetworkService.h"
#import "Common.h"

@implementation DMCCGroupMemberMuteNotificationContent
- (DMCCMessagePayload *)encode {
    DMCCMessagePayload *payload = [super encode];
    payload.contentType = [self.class getContentType];
    
    NSMutableDictionary *dataDict = [NSMutableDictionary dictionary];
    if (self.creator) {
        [dataDict setObject:self.creator forKey:@"o"];
    }
    if (self.type) {
        [dataDict setObject:self.type forKey:@"n"];
    }
    
    if (self.groupId) {
        [dataDict setObject:self.groupId forKey:@"g"];
    }
    
    if (self.targetIds) {
        [dataDict setObject:self.targetIds forKey:@"ms"];
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
        self.creator = dictionary[@"o"];
        self.type = dictionary[@"n"];
        self.groupId = dictionary[@"g"];
        self.targetIds = dictionary[@"ms"];
    }
}

+ (int)getContentType {
    return MESSAGE_CONTENT_TYPE_MUTE_MEMBER;
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

    if ([[DMCCNetworkService sharedInstance].userId isEqualToString:self.creator]) {
        formatMsg = @"你";
    } else {
        DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:self.creator refresh:NO];
        if (userInfo.displayName.length > 0) {
            formatMsg = [NSString stringWithFormat:@"%@", userInfo.displayName];
        } else {
            formatMsg = [NSString stringWithFormat:@"%@", self.creator];
        }
    }
    
    if ([self.type isEqualToString:@"1"]) {
        formatMsg = [NSString stringWithFormat:@"%@ 禁言了", formatMsg];
    } else {
        formatMsg = [NSString stringWithFormat:@"%@ 取消禁言了", formatMsg];
    }
    
    int count = 0;
    if([self.targetIds containsObject:[DMCCNetworkService sharedInstance].userId]) {
        formatMsg = [formatMsg stringByAppendingString:@" 你"];
        count++;
    }
    
    for (NSString *member in self.targetIds) {
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
    
    if(self.targetIds.count > count) {
        formatMsg = [formatMsg stringByAppendingFormat:@" 等%ld名成员", self.targetIds.count];
    }

    return formatMsg;
}
@end
