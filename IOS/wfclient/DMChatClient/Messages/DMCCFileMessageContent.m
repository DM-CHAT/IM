//
//  DMCCSoundMessageContent.m
//  DMChatClient
//
//  Created by heavyrain on 2017/9/9.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCFileMessageContent.h"
#import "DMCCUtilities.h"
#import "DMCCIMService.h"
#import "Common.h"
#import <config/config.h>

@implementation DMCCFileMessageContent
+ (instancetype)fileMessageContentFromPath:(NSString *)filePath {
    DMCCFileMessageContent *fileMsg = [[DMCCFileMessageContent alloc] init];
    fileMsg.localPath = filePath;
    fileMsg.name = [filePath lastPathComponent];
    NSDictionary *fileAttributes = [[NSFileManager defaultManager] attributesOfItemAtPath:filePath error:nil];
    fileMsg.size = [fileAttributes fileSize];

    return fileMsg;
}

- (DMCCMessagePayload *)encode {
    DMCCMediaMessagePayload *payload = [[DMCCMediaMessagePayload alloc] init];
    payload.extra = self.extra;
    payload.contentType = [self.class getContentType];
    payload.searchableContent = self.name;
    payload.content = [NSString stringWithFormat:@"%ld", (long)self.size];
    payload.mediaType = Media_Type_FILE;
    
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
        self.name = mediaPayload.searchableContent;
        self.size = [mediaPayload.content integerValue];
    }
}


+ (int)getContentType {
    return MESSAGE_CONTENT_TYPE_FILE;
}

+ (int)getContentFlags {
    return DMCCPersistFlag_PERSIST_AND_COUNT;
}


+ (void)load {
    [[DMCCIMService sharedDMCIMService] registerMessageContent:self];
}

- (NSString *)digest:(DMCCMessage *)message {
    return [NSString stringWithFormat:@"%@:%@", LocalizedString(@"file_digest"),self.name];
}
@end
