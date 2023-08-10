//
//  DMCCThingsLostEventContent.m
//  DMChatClient
//
//  Created by heavyrain on 2017/8/16.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCThingsLostEventContent.h"
#import "DMCCIMService.h"
#import "Common.h"


@implementation DMCCThingsLostEventContent
- (DMCCMessagePayload *)encode {
    DMCCMessagePayload *payload = [super encode];
    payload.contentType = [self.class getContentType];
    return payload;
}

- (void)decode:(DMCCMessagePayload *)payload {
    [super decode:payload];
}

+ (int)getContentType {
    return THINGS_CONTENT_TYPE_LOST_EVENT;
}

+ (int)getContentFlags {
    return DMCCPersistFlag_TRANSPARENT;
}


+ (void)load {
    [[DMCCIMService sharedDMCIMService] registerMessageContent:self];
}

- (NSString *)digest:(DMCCMessage *)message {
  return nil;
}
@end
