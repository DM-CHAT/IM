//
//  DMCCCreateGroupNotificationContent.m
//  DMChatClient
//
//  Created by heavyrain on 2017/9/19.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCGroupSetManagerNotificationContent.h"
#import "DMCCIMService.h"
#import "DMCCNetworkService.h"
#import "Common.h"

@implementation DMCCGroupSetManagerNotificationContent
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
    
    if (self.memberIds) {
        [dataDict setObject:self.memberIds forKey:@"ms"];
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
        self.memberIds = dictionary[@"ms"];
    }
}

+ (int)getContentType {
    return MESSAGE_CONTENT_TYPE_SET_MANAGER;
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
    NSString *from;
    NSString *targets = @"";
    if ([[DMCCNetworkService sharedInstance].userId isEqualToString:self.operatorId]) {
        from = @"你";
    } else {
        DMCCUserInfo *fromUserInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:self.operatorId inGroup:self.groupId refresh:NO];
        if (fromUserInfo.friendAlias.length > 0) {
            from = fromUserInfo.friendAlias;
        } else if(fromUserInfo.groupAlias.length > 0) {
            from = fromUserInfo.groupAlias;
        } else if (fromUserInfo.displayName.length > 0) {
            from = fromUserInfo.displayName;
        } else {
            from = [NSString stringWithFormat:@"用户<%@>", self.operatorId];
        }
    }
    
    int count = 0;
    if([self.memberIds containsObject:[DMCCNetworkService sharedInstance].userId]) {
        targets = [targets stringByAppendingString:@" 你"];
        count++;
    }
    
    for (NSString *memberId in self.memberIds) {
        NSString *target;
        if ([[DMCCNetworkService sharedInstance].userId isEqualToString:memberId]) {
            continue;
        } else {
            DMCCUserInfo *memberUserInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:memberId inGroup:self.groupId refresh:NO];
            if (memberUserInfo.friendAlias.length > 0) {
                target = memberUserInfo.friendAlias;
            } else if(memberUserInfo.groupAlias.length > 0) {
                target = memberUserInfo.groupAlias;
            } else if (memberUserInfo.displayName.length > 0) {
                target = memberUserInfo.displayName;
            } else {
                target = [NSString stringWithFormat:@"用户<%@>", memberId];
            }
        }
        if (!targets) {
            targets = target;
        } else {
            targets = [NSString stringWithFormat:@"%@,%@", targets, target];
        }
        count++;
        if(count >= 4) {
            break;
        }
    }
    
    if(self.memberIds.count > count) {
        targets = [targets stringByAppendingFormat:@" 等%ld名成员", self.memberIds.count];
    }
    
    if ([self.type isEqualToString:@"1"]) {
        return [NSString stringWithFormat:@"%@ 设置 %@ 为管理员", from, targets];
    } else {
        return [NSString stringWithFormat:@"%@ 取消 %@ 管理员权限", from, targets];
    }
}
@end
