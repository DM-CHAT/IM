//
//  DMCCMessageContent.m
//  DMChatClient
//
//  Created by heavyrain on 2017/8/15.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCMessageContent.h"
#import "Common.h"

@implementation DMCCMessagePayload
@end

@implementation DMCCMediaMessagePayload
@end

@implementation DMCCMessageContent
+ (void)load {
    
}
- (DMCCMessagePayload *)encode {
    DMCCMessagePayload *payload = [[DMCCMessagePayload alloc] init];
    payload.extra = self.extra;
    return payload;
}
- (void)decode:(DMCCMessagePayload *)payload {
    self.extra = payload.extra;
}
+ (int)getContentType {
    return 0;
}
+ (int)getContentFlags {
    return 0;
}
- (NSString *)digest:(DMCCMessage *)message {
  return @"Unimplement digest function";
}
@end
