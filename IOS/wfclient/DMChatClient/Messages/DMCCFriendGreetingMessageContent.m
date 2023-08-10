//
//  DMCCFriendGreetingMessageContent.m
//  DMChatClient
//
//  Created by heavyrain on 2017/9/19.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCFriendGreetingMessageContent.h"
#import "DMCCIMService.h"
#import "DMCCNetworkService.h"
#import "Common.h"
#import <config/config.h>

@implementation DMCCFriendGreetingMessageContent
- (DMCCMessagePayload *)encode {
    DMCCMessagePayload *payload = [super encode];
    payload.contentType = [self.class getContentType];
    return payload;
}

- (void)decode:(DMCCMessagePayload *)payload {
    [super decode:payload];
}

+ (int)getContentType {
    return MESSAGE_FRIEND_GREETING;
}

+ (int)getContentFlags {
    return DMCCPersistFlag_PERSIST;
}

+ (void)load {
    [[DMCCIMService sharedDMCIMService] registerMessageContent:self];
}

- (NSString *)formatNotification:(DMCCMessage *)message {
    return LocalizedString(@"hello_context");
}

- (NSString *)digest:(DMCCMessage *)message {
    return [self formatNotification:message];
}
@end
