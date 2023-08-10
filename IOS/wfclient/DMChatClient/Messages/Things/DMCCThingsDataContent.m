//
//  DMCCThingsDataContent.m
//  DMChatClient
//
//  Created by heavyrain on 2017/8/16.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCThingsDataContent.h"
#import "DMCCIMService.h"
#import "Common.h"


@implementation DMCCThingsDataContent
- (DMCCMessagePayload *)encode {
    DMCCMessagePayload *payload = [super encode];
    payload.binaryContent = self.data;
    return payload;
}

- (void)decode:(DMCCMessagePayload *)payload {
    [super decode:payload];
    self.data = payload.binaryContent;
}

+ (int)getContentType {
    return THINGS_CONTENT_TYPE_DATA;
}

+ (int)getContentFlags {
    return DMCCPersistFlag_NOT_PERSIST;
}



+ (void)load {
    [[DMCCIMService sharedDMCIMService] registerMessageContent:self];
}

- (NSString *)digest:(DMCCMessage *)message {
  return nil;
}
@end
