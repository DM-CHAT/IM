//
//  DMCCTextMessageContent.m
//  DMChatClient
//
//  Created by heavyrain on 2017/8/16.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCLinkMessageContent.h"
#import "DMCCIMService.h"
#import "Common.h"


@implementation DMCCLinkMessageContent
- (DMCCMessagePayload *)encode {
    DMCCMessagePayload *payload = [super encode];
    payload.contentType = [self.class getContentType];
    payload.searchableContent = self.title;
    
    NSMutableDictionary *dataDict = [NSMutableDictionary dictionary];
    if (self.contentDigest) {
        [dataDict setObject:self.contentDigest forKey:@"d"];
    }
    if (self.url) {
        [dataDict setObject:self.url forKey:@"u"];
    }
    
    if (self.thumbnailUrl) {
        [dataDict setObject:self.thumbnailUrl forKey:@"t"];
    }
    
    payload.binaryContent = [NSJSONSerialization dataWithJSONObject:dataDict
                                                                           options:kNilOptions
                                                                             error:nil];
    
    return payload;
}

- (void)decode:(DMCCMessagePayload *)payload {
    [super decode:payload];
    self.title = payload.searchableContent;
    
    NSError *__error = nil;
    NSDictionary *dictionary = [NSJSONSerialization JSONObjectWithData:payload.binaryContent
                                                               options:kNilOptions
                                                                 error:&__error];
    if (!__error) {
        self.contentDigest = dictionary[@"d"];
        self.url = dictionary[@"u"];
        self.thumbnailUrl = dictionary[@"t"];
    }
}

+ (int)getContentType {
    return MESSAGE_CONTENT_TYPE_LINK;
}

+ (int)getContentFlags {
    return DMCCPersistFlag_PERSIST_AND_COUNT;
}

+ (void)load {
    [[DMCCIMService sharedDMCIMService] registerMessageContent:self];
}

- (NSString *)digest:(DMCCMessage *)message {
  return [NSString stringWithFormat:@"[链接]%@", self.title];
}
@end
