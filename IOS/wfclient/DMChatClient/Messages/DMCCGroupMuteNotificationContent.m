//
//  DMCCCreateGroupNotificationContent.m
//  DMChatClient
//
//  Created by heavyrain on 2017/9/19.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCGroupMuteNotificationContent.h"
#import "DMCCIMService.h"
#import "DMCCNetworkService.h"
#import "Common.h"

@implementation DMCCGroupMuteNotificationContent
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
    }
}

+ (int)getContentType {
    return MESSAGE_CONTENT_TYPE_CHANGE_MUTE;
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
    if ([[DMCCNetworkService sharedInstance].userId isEqualToString:self.creator]) {
        return [self.type isEqualToString:@"1"] ? @"你开启了全员禁言" : @"你关闭了全员禁言";
    } else {
        DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:self.creator inGroup:self.groupId refresh:NO];
        if (userInfo.friendAlias.length > 0) {
            return [NSString stringWithFormat:[self.type isEqualToString:@"1"] ? @"%@开启了全员禁言" : @"%@关闭了全员禁言", userInfo.friendAlias];
        } else if(userInfo.groupAlias.length > 0) {
            return [NSString stringWithFormat:[self.type isEqualToString:@"1"] ? @"%@开启了全员禁言" : @"%@关闭了全员禁言", userInfo.groupAlias];
        } else if (userInfo.displayName.length > 0) {
            return [NSString stringWithFormat:[self.type isEqualToString:@"1"] ? @"%@开启了全员禁言" : @"%@关闭了全员禁言", userInfo.displayName];
        } else {
            return [NSString stringWithFormat:[self.type isEqualToString:@"1"] ? @"用户<%@>开启了全员禁言" : @"用户<%@>关闭了全员禁言", self.creator];
        }
    }
}
@end
