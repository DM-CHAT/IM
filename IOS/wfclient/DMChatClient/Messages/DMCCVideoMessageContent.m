//
//  DMCCVideoMessageContent.m
//  DMChatClient
//
//  Created by heavyrain on 2017/9/2.
//  Copyright © 2017年 wildfire chat. All rights reserved.
//

#import "DMCCVideoMessageContent.h"
#import "DMCCNetworkService.h"
#import "DMCCIMService.h"
#import "DMCCUtilities.h"
#import "Common.h"
#import <AVFoundation/AVFoundation.h>
#import <config/config.h>

@implementation DMCCVideoMessageContent
+ (instancetype)contentPath:(NSString *)localPath thumbnail:(UIImage *)image {
    DMCCVideoMessageContent *content = [[DMCCVideoMessageContent alloc] init];
    content.localPath = localPath;
    content.thumbnail = [DMCCUtilities imageWithRightOrientation:image];
    
    NSURL *videoUrl = [NSURL URLWithString:localPath];
    AVURLAsset *avUrl = [AVURLAsset assetWithURL:videoUrl];
    CMTime time = [avUrl duration];
    content.duration = ceil(time.value/time.timescale);

    return content;
}
- (DMCCMessagePayload *)encode {
    DMCCMediaMessagePayload *payload = [[DMCCMediaMessagePayload alloc] init];
    payload.extra = self.extra;
    payload.contentType = [self.class getContentType];
    payload.searchableContent = LocalizedString(@"video_digest");
    payload.binaryContent = UIImageJPEGRepresentation(self.thumbnail, 1);
    payload.mediaType = Media_Type_VIDEO;
    payload.remoteMediaUrl = self.remoteUrl;
    payload.localMediaPath = self.localPath;
    
    NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
    [dict setObject:@(_duration) forKey:@"duration"];
    [dict setObject:@(_duration) forKey:@"d"];
    payload.content = [[NSString alloc] initWithData:[NSJSONSerialization dataWithJSONObject:dict options:kNilOptions error:nil] encoding:NSUTF8StringEncoding];
    
    return payload;
}

- (void)decode:(DMCCMessagePayload *)payload {
    [super decode:payload];
    if ([payload isKindOfClass:[DMCCMediaMessagePayload class]]) {
        DMCCMediaMessagePayload *mediaPayload = (DMCCMediaMessagePayload *)payload;
        self.thumbnail = [UIImage imageWithData:payload.binaryContent];
        self.remoteUrl = mediaPayload.remoteMediaUrl;
        self.localPath = mediaPayload.localMediaPath;
        
        NSError *__error = nil;
        NSDictionary *dictionary = [NSJSONSerialization JSONObjectWithData:[payload.content dataUsingEncoding:NSUTF8StringEncoding]
                                                                   options:kNilOptions
                                                                     error:&__error];
        if (!__error) {
            self.duration = [dictionary[@"duration"] longValue];
            if(self.duration == 0) {
                self.duration = [dictionary[@"d"] longValue];
            }
        }
    }
}

+ (int)getContentType {
    return MESSAGE_CONTENT_TYPE_VIDEO;
}

+ (int)getContentFlags {
    return DMCCPersistFlag_PERSIST_AND_COUNT;
}




+ (void)load {
    [[DMCCIMService sharedDMCIMService] registerMessageContent:self];
}

- (NSString *)digest:(DMCCMessage *)message {
    return LocalizedString(@"video_digest");
}
@end
