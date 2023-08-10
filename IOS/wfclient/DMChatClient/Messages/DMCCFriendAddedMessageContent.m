//
//  DMCCFriendAddedMessageContent.m
//  DMChatClient
//
//  Created by heavyrain on 2017/9/19.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCFriendAddedMessageContent.h"
#import "DMCCIMService.h"
#import "DMCCNetworkService.h"
#import "Common.h"
#import <config/config.h>

@implementation DMCCFriendAddedMessageContent
- (DMCCMessagePayload *)encode {
    DMCCMessagePayload *payload = [super encode];
    payload.contentType = [self.class getContentType];
    return payload;
}

- (void)decode:(DMCCMessagePayload *)payload {
    [super decode:payload];
}

+ (int)getContentType {
    return MESSAGE_FRIEND_ADDED_NOTIFICATION;
}

+ (int)getContentFlags {
    return DMCCPersistFlag_PERSIST;
}



+ (void)load {
    [[DMCCIMService sharedDMCIMService] registerMessageContent:self];
}

- (NSString *)formatNotification:(DMCCMessage *)message {
    return LocalizedString(@"you_are_friend_and_start_chat");
}

- (NSString *)digest:(DMCCMessage *)message {
    return [self formatNotification:message];
}
@end
