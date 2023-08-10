//
//  TypingMessageContent.m
//  DMChatClient
//
//  Created by heavyrain on 2017/8/16.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCTypingMessageContent.h"
#import "DMCCIMService.h"
#import "Common.h"


@implementation DMCCTypingMessageContent
- (DMCCMessagePayload *)encode {
    DMCCMessagePayload *payload = [super encode];
    payload.contentType = [self.class getContentType];
    payload.content = [NSString stringWithFormat:@"%d", (int)self.type];
    return payload;
}

- (void)decode:(DMCCMessagePayload *)payload {
    [super decode:payload];
    self.type = [payload.content intValue];
}

+ (int)getContentType {
    return MESSAGE_CONTENT_TYPE_TYPING;
}

+ (int)getContentFlags {
    return DMCCPersistFlag_TRANSPARENT;
}


+ (instancetype)contentType:(DMCCTypingType)type {
    DMCCTypingMessageContent *content = [[DMCCTypingMessageContent alloc] init];
    content.type = type;
    return content;
}

+ (void)load {
    [[DMCCIMService sharedDMCIMService] registerMessageContent:self];
}

- (NSString *)digest:(DMCCMessage *)message {
  return nil;
}
@end
