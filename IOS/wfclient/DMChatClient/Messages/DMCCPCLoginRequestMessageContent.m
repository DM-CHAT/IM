//
//  DMCCPCLoginRequestMessageContent.m
//  DMChatClient
//
//  Created by heavyrain on 2017/9/19.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCPCLoginRequestMessageContent.h"

#import "DMCCNetworkService.h"
#import "Common.h"

@implementation DMCCPCLoginRequestMessageContent
- (DMCCMessagePayload *)encode {
    DMCCMessagePayload *payload = [super encode];
    payload.contentType = [self.class getContentType];
    
    NSMutableDictionary *dataDict = [NSMutableDictionary dictionary];
    if (self.sessionId) {
        [dataDict setObject:self.sessionId forKey:@"t"];
    }
    if (self.platform) {
        [dataDict setObject:@(self.platform) forKey:@"p"];
    }
    
    
    payload.binaryContent = [NSJSONSerialization dataWithJSONObject:dataDict
                                                                           options:kNilOptions
                                                                             error:nil];
    
    return payload;
}

- (void)decode:(DMCCMessagePayload *)payload {
    [super decode:payload];
    NSError *__error = nil;
    NSDictionary *dictionary = [NSJSONSerialization JSONObjectWithData:payload.binaryContent
                                                               options:kNilOptions
                                                                 error:&__error];
    if (!__error) {
        self.sessionId = dictionary[@"t"];
        self.platform = [dictionary[@"p"] intValue];
    }
}

+ (int)getContentType {
    return MESSAGE_PC_LOGIN_REQUSET;
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
