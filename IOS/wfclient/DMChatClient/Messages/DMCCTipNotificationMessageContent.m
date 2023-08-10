//
//  DMCCNotificationMessageContent.m
//  DMChatClient
//
//  Created by heavyrain on 2017/9/19.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCTipNotificationMessageContent.h"
#import "DMCCIMService.h"
#import "DMCCNetworkService.h"
#import "Common.h"

@implementation DMCCTipNotificationContent
- (DMCCMessagePayload *)encode {
    DMCCMessagePayload *payload = [super encode];
    payload.contentType = [self.class getContentType];
    
    payload.content = self.tip;
    return payload;
}

- (void)decode:(DMCCMessagePayload *)payload {
    [super decode:payload];
    self.tip = payload.content;
}

+ (int)getContentType {
    return MESSAGE_CONTENT_TYPE_TIP;
}

+ (int)getContentFlags {
    return DMCCPersistFlag_PERSIST;
}



+ (void)load {
    [[DMCCIMService sharedDMCIMService] registerMessageContent:self];
}

- (NSString *)formatNotification:(DMCCMessage *)message {
    return self.tip;
}

- (NSString *)digest:(DMCCMessage *)message {
    return self.tip;
}
@end
