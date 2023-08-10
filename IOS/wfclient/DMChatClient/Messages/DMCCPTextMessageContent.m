//
//  DMCCPTextMessageContent.m
//  DMChatClient
//
//  Created by heavyrain on 2017/8/16.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCPTextMessageContent.h"
#import "DMCCIMService.h"
#import "Common.h"


@implementation DMCCPTextMessageContent
- (DMCCMessagePayload *)encode {
    DMCCMessagePayload *payload = [super encode];
    payload.contentType = [self.class getContentType];
    payload.searchableContent = self.text;
    return payload;
}

- (void)decode:(DMCCMessagePayload *)payload {
    [super decode:payload];
    self.text = payload.searchableContent;
}

+ (int)getContentType {
    return MESSAGE_CONTENT_TYPE_P_TEXT;
}

+ (int)getContentFlags {
    return DMCCPersistFlag_PERSIST;
}


+ (instancetype)contentWith:(NSString *)text {
    DMCCPTextMessageContent *content = [[DMCCPTextMessageContent alloc] init];
    content.text = text;
    return content;
}

+ (void)load {
    [[DMCCIMService sharedDMCIMService] registerMessageContent:self];
}

- (NSString *)digest:(DMCCMessage *)message {
  return self.text;
}
@end
