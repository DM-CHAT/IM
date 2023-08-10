//
//  DMCCImageMessageContent.m
//  DMChatClient
//
//  Created by heavyrain on 2017/9/2.
//  Copyright © 2017年 wildfire chat. All rights reserved.
//

#import "DMCCStickerMessageContent.h"
#import "DMCCNetworkService.h"
#import "DMCCIMService.h"
#import "DMCCUtilities.h"
#import "Common.h"
#import <config/config.h>

@implementation DMCCStickerMessageContent
+ (instancetype)contentFrom:(NSString *)stickerPath {
    DMCCStickerMessageContent *content = [[DMCCStickerMessageContent alloc] init];
    content.localPath = stickerPath;
    content.size = [UIImage imageWithContentsOfFile:stickerPath].size;
    return content;
}

- (DMCCMessagePayload *)encode {
    DMCCMediaMessagePayload *payload = [[DMCCMediaMessagePayload alloc] init];
    payload.extra = self.extra;
    payload.contentType = [self.class getContentType];
    payload.searchableContent = LocalizedString(@"sticker_digest");
    payload.mediaType = Media_Type_STICKER;
    payload.remoteMediaUrl = self.remoteUrl;
    payload.localMediaPath = self.localPath;
    
    NSMutableDictionary *dataDict = [NSMutableDictionary dictionary];
    [dataDict setObject:@(self.size.width) forKey:@"x"];
    [dataDict setObject:@(self.size.height) forKey:@"y"];
    
    payload.binaryContent = [NSJSONSerialization dataWithJSONObject:dataDict
                                                            options:kNilOptions
                                                              error:nil];
    
    return payload;
}

- (void)decode:(DMCCMessagePayload *)payload {
    [super decode:payload];
    if ([payload isKindOfClass:[DMCCMediaMessagePayload class]]) {
        DMCCMediaMessagePayload *mediaPayload = (DMCCMediaMessagePayload *)payload;
        self.remoteUrl = mediaPayload.remoteMediaUrl;
        self.localPath = mediaPayload.localMediaPath;
    }
    
    NSError *__error = nil;
    NSDictionary *dictionary = [NSJSONSerialization JSONObjectWithData:payload.binaryContent
                                                               options:kNilOptions
                                                                 error:&__error];
    if (!__error) {
        self.size = CGSizeMake([dictionary[@"x"] floatValue], [dictionary[@"y"] floatValue]);
    }
}

+ (int)getContentType {
    return MESSAGE_CONTENT_TYPE_STICKER;
}

+ (int)getContentFlags {
    return DMCCPersistFlag_PERSIST_AND_COUNT;
}




+ (void)load {
    [[DMCCIMService sharedDMCIMService] registerMessageContent:self];
}

- (NSString *)digest:(DMCCMessage *)message {
    return LocalizedString(@"sticker_digest");
}
@end
