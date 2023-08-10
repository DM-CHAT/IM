//
//  DMCCTextMessageContent.m
//  DMChatClient
//
//  Created by heavyrain on 2017/8/16.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCCallStartMessageContent.h"
#import "DMCCIMService.h"
#import "Common.h"
#import <config/config.h>

@implementation DMCCCallStartMessageContent
- (DMCCMessagePayload *)encode {
    
    DMCCMessagePayload *payload = [super encode];
    payload.contentType = [self.class getContentType];
    payload.content = self.callId;
    
    NSMutableDictionary *dataDict = [NSMutableDictionary dictionary];
    if (self.connectTime) {
        [dataDict setObject:@(self.connectTime) forKey:@"c"];
    }
    if (self.endTime) {
        [dataDict setObject:@(self.endTime) forKey:@"e"];
    }
    if (self.status) {
        [dataDict setObject:@(self.status) forKey:@"s"];
    }
    if (self.pin) {
        [dataDict setObject:self.pin forKey:@"p"];
    }
    
    [dataDict setObject:self.targetIds forKey:@"ts"];
    //多人音视频与单人音视频兼容
    [dataDict setObject:self.targetIds[0] forKey:@"t"];
    [dataDict setValue:@(self.audioOnly?1:0) forKey:@"a"];
    
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
        self.connectTime = dictionary[@"c"] ? [dictionary[@"c"] longLongValue] : 0;
        self.endTime = dictionary[@"e"] ? [dictionary[@"e"] longLongValue] : 0;
        self.status = dictionary[@"s"] ? [dictionary[@"s"] intValue] : 0;
        self.audioOnly = [dictionary[@"a"] intValue] ? YES : NO;
        self.targetIds = dictionary[@"ts"];
        self.pin = dictionary[@"p"];
        if (self.targetIds.count == 0) {
            NSString *target = dictionary[@"t"];
            NSMutableArray *arr = [[NSMutableArray alloc] init];
            [arr addObject:target];
            self.targetIds = arr;
        }
    }
}

+ (int)getContentType {
    return VOIP_CONTENT_TYPE_START;
}

+ (int)getContentFlags {
    return DMCCPersistFlag_PERSIST_AND_COUNT;
}

+ (void)load {
    [[DMCCIMService sharedDMCIMService] registerMessageContent:self];
}

- (NSString *)digest:(DMCCMessage *)message {
    if (_audioOnly) {
        return LocalizedString(@"audio_digest");
    } else {
        return LocalizedString(@"video_digest");
    }
}
@end
