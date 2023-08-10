//
//  DMCCCreateGroupNotificationContent.m
//  DMChatClient
//
//  Created by heavyrain on 2017/9/19.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCGroupPrivateChatNotificationContent.h"
#import "DMCCIMService.h"
#import "DMCCNetworkService.h"
#import "Common.h"

@implementation DMCCGroupPrivateChatNotificationContent
- (DMCCMessagePayload *)encode {
    DMCCMessagePayload *payload = [super encode];
    payload.contentType = [self.class getContentType];
    
    NSMutableDictionary *dataDict = [NSMutableDictionary dictionary];
    if (self.operatorId) {
        [dataDict setObject:self.operatorId forKey:@"o"];
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
        self.operatorId = dictionary[@"o"];
        self.type = dictionary[@"n"];
        self.groupId = dictionary[@"g"];
    }
}

+ (int)getContentType {
    return MESSAGE_CONTENT_TYPE_CHANGE_PRIVATECHAT;
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
    if ([[DMCCNetworkService sharedInstance].userId isEqualToString:self.operatorId]) {
        return [self.type isEqualToString:@"0"] ? @"你开启了成员私聊" : @"你关闭了成员私聊";
    } else {
        DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:self.operatorId inGroup:self.groupId refresh:NO];
        if (userInfo.friendAlias.length > 0) {
            return [NSString stringWithFormat:[self.type isEqualToString:@"0"] ? @"%@开启了成员私聊" : @"%@关闭了成员私聊", userInfo.friendAlias];
        } else if(userInfo.groupAlias.length > 0) {
            return [NSString stringWithFormat:[self.type isEqualToString:@"0"] ? @"%@开启了成员私聊" : @"%@关闭了成员私聊", userInfo.groupAlias];
        } else if (userInfo.displayName.length > 0) {
            return [NSString stringWithFormat:[self.type isEqualToString:@"0"] ? @"%@开启了成员私聊" : @"%@关闭了成员私聊", userInfo.displayName];
        } else {
            return [NSString stringWithFormat:[self.type isEqualToString:@"0"] ? @"用户<%@>开启了成员私聊" : @"用户<%@>关闭了成员私聊", self.operatorId];
        }
    }
}
@end
