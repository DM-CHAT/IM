//
//  DMCCCreateGroupNotificationContent.m
//  DMChatClient
//
//  Created by heavyrain on 2017/9/19.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCCreateGroupNotificationContent.h"
#import "DMCCIMService.h"
#import "DMCCNetworkService.h"
#import "Common.h"
#import <config/config.h>

@implementation DMCCCreateGroupNotificationContent
- (DMCCMessagePayload *)encode {
    DMCCMessagePayload *payload = [super encode];
    payload.contentType = [self.class getContentType];
    
    NSMutableDictionary *dataDict = [NSMutableDictionary dictionary];
    if (self.creator) {
        [dataDict setObject:self.creator forKey:@"o"];
    }
    if (self.groupName) {
        [dataDict setObject:self.groupName forKey:@"n"];
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
        self.groupName = dictionary[@"n"];
        self.groupId = dictionary[@"g"];
    }
}

+ (int)getContentType {
    return MESSAGE_CONTENT_TYPE_CREATE_GROUP;
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
        NSLog(@"==test== formatNotification  %@",[NSString stringWithFormat:LocalizedString(@"you_create_group"), self.groupName]);
        return [NSString stringWithFormat:LocalizedString(@"you_create_group"), self.groupName];
    } else {
        DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:self.creator inGroup:self.groupId refresh:NO];
        if (userInfo.friendAlias.length > 0) {
            return [NSString stringWithFormat:LocalizedString(@"create_group"), userInfo.friendAlias, self.groupName];
        } else if(userInfo.groupAlias.length > 0) {
            return [NSString stringWithFormat:LocalizedString(@"create_group"), userInfo.groupAlias, self.groupName];
        } else if (userInfo.displayName.length > 0) {
            return [NSString stringWithFormat:LocalizedString(@"create_group"), userInfo.displayName, self.groupName];
        } else {
            return [NSString stringWithFormat:LocalizedString(@"usercreate_group"), self.creator, self.groupName];
        }
    }
}
@end
