//
//  DMCCPTTInviteMessageContent.m
//  DMChatClient
//
//  Created by heavyrain on 2021/2/18.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCPTTInviteMessageContent.h"
#import "DMCCIMService.h"
#import "Common.h"


@implementation DMCCPTTInviteMessageContent
- (DMCCMessagePayload *)encode {
    
    DMCCMessagePayload *payload = [super encode];
    payload.contentType = [self.class getContentType];
    payload.content = self.callId;
    
    NSMutableDictionary *dataDict = [NSMutableDictionary dictionary];
    if (self.host) {
        [dataDict setObject:self.host forKey:@"h"];
    }

    if (self.title) {
        [dataDict setObject:self.title forKey:@"t"];
    }
    if (self.desc) {
        [dataDict setObject:self.desc forKey:@"d"];
    }
    if (self.pin) {
        [dataDict setObject:self.pin forKey:@"p"];
    }
    
    payload.binaryContent = [NSJSONSerialization dataWithJSONObject:dataDict
                                                            options:kNilOptions
                                                              error:nil];
    return payload;
}

- (void)decode:(DMCCMessagePayload *)payload {
    [super decode:payload];
    self.callId = payload.content;
    NSError *__error = nil;
    NSDictionary *dictionary = [NSJSONSerialization JSONObjectWithData:payload.binaryContent
                                                               options:kNilOptions
                                                                 error:&__error];
    if (!__error) {
        self.host = dictionary[@"h"];
        self.title = dictionary[@"t"];
        self.desc = dictionary[@"d"];
        self.pin = dictionary[@"p"];
    }
}

+ (int)getContentType {
    return VOIP_CONTENT_PTT_INVITE;
}

+ (int)getContentFlags {
    return DMCCPersistFlag_PERSIST_AND_COUNT;
}

+ (void)load {
    [[DMCCIMService sharedDMCIMService] registerMessageContent:self];
}

- (NSString *)digest:(DMCCMessage *)message {
    return @"[对讲邀请]";
}
@end
