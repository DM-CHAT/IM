//
//  DMCCSoundMessageContent.m
//  DMChatClient
//
//  Created by heavyrain on 2017/9/9.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCSoundMessageContent.h"
#import "DMCCUtilities.h"
#import "wav_amr.h"
#import "DMCCIMService.h"
#import "Common.h"
#import <config/config.h>

@implementation DMCCSoundMessageContent
+ (instancetype)soundMessageContentForWav:(NSString *)wavPath
                       destinationAmrPath:(NSString *)amrPath
                                 duration:(long)duration {
    DMCCSoundMessageContent *soundMsg = [[DMCCSoundMessageContent alloc] init];
    soundMsg.duration = duration;
    encode_amr([wavPath UTF8String], [amrPath UTF8String]);
    
    soundMsg.localPath = amrPath;
    
    return soundMsg;
}

+ (instancetype)soundMessageContentForAmr:(NSString *)amrPath
                                 duration:(long)duration {
    DMCCSoundMessageContent *soundMsg = [[DMCCSoundMessageContent alloc] init];
    soundMsg.duration = duration;
    soundMsg.localPath = amrPath;
    
    return soundMsg;
}

- (NSData *)getWavData {
    if (!self.localPath) {
        return nil;
    } else {
        return [[DMCCIMService sharedDMCIMService] getWavData:self.localPath];
    }
}

- (DMCCMessagePayload *)encode {
    DMCCMediaMessagePayload *payload = [[DMCCMediaMessagePayload alloc] init];
    payload.extra = self.extra;
    payload.contentType = [self.class getContentType];
    payload.searchableContent = LocalizedString(@"audio_digest");
    payload.mediaType = Media_Type_VOICE;
    NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
    [dict setObject:@(_duration) forKey:@"duration"];
    payload.content = [[NSString alloc] initWithData:[NSJSONSerialization dataWithJSONObject:dict options:kNilOptions error:nil] encoding:NSUTF8StringEncoding];
    
    payload.remoteMediaUrl = self.remoteUrl;
    payload.localMediaPath = self.localPath;
    return payload;
}

- (void)decode:(DMCCMessagePayload *)payload {
    [super decode:payload];
    if ([payload isKindOfClass:[DMCCMediaMessagePayload class]]) {
        DMCCMediaMessagePayload *mediaPayload = (DMCCMediaMessagePayload *)payload;
        self.remoteUrl = mediaPayload.remoteMediaUrl;
        self.localPath = mediaPayload.localMediaPath;
        
        NSError *__error = nil;
        NSDictionary *dictionary = [NSJSONSerialization JSONObjectWithData:[payload.content dataUsingEncoding:NSUTF8StringEncoding]
                                                                   options:kNilOptions
                                                                     error:&__error];
        if (!__error) {
            self.duration = [dictionary[@"duration"] longValue];
        }
    }
}


+ (int)getContentType {
    return MESSAGE_CONTENT_TYPE_SOUND;
}

+ (int)getContentFlags {
    return DMCCPersistFlag_PERSIST_AND_COUNT;
}


+ (void)load {
    [[DMCCIMService sharedDMCIMService] registerMessageContent:self];
}

- (NSString *)digest:(DMCCMessage *)message {
    return LocalizedString(@"audio_digest");
}
@end
