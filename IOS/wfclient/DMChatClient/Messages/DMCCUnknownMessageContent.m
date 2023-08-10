//
//  DMCCUnknownMessageContent.m
//  DMChatClient
//
//  Created by heavyrain on 2017/8/16.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCUnknownMessageContent.h"
#import "DMCCIMService.h"
#import "Common.h"
#import <config/config.h>

@implementation DMCCUnknownMessageContent
- (DMCCMessagePayload *)encode {
    return self.orignalPayload;
}

- (void)decode:(DMCCMessagePayload *)payload {
    self.orignalType = payload.contentType;
    self.orignalPayload = payload;
}

+ (int)getContentType {
    return MESSAGE_CONTENT_TYPE_UNKNOWN;
}

+ (int)getContentFlags {
    return DMCCPersistFlag_PERSIST;
}

- (NSString *)digest:(DMCCMessage *)message {
  return [NSString stringWithFormat:@"%@(%zd)", LocalizedString(@"unknown_message"),self.orignalType];
}
@end
