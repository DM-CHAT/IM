//
//  DMCCImageMessageContent.m
//  DMChatClient
//
//  Created by heavyrain on 2017/9/2.
//  Copyright © 2017年 wildfire chat. All rights reserved.
//

#import "DMCCImageMessageContent.h"
#import "DMCCNetworkService.h"
#import "DMCCIMService.h"
#import "DMCCUtilities.h"
#import "Common.h"
#import <config/config.h>

//@interface DMCCImageMessageContent ()
//@property (nonatomic, assign)CGSize size;
//@end

@implementation DMCCImageMessageContent
+ (instancetype)contentFrom:(UIImage *)image cachePath:(NSString *)path {
    DMCCImageMessageContent *content = [[DMCCImageMessageContent alloc] init];
    
//    image = [DMCCUtilities generateThumbnail:image withWidth:1024 withHeight:1024];
    image = image;
    NSData *imgData = UIImageJPEGRepresentation(image, 1);
    
    [imgData writeToFile:path atomically:YES];
    
    content.localPath = path;
    content.size = image.size;
    content.thumbnail = [DMCCUtilities generateThumbnail:image withWidth:120 withHeight:120];
    
    return content;
}

- (DMCCMessagePayload *)encode {
    DMCCMediaMessagePayload *payload = [[DMCCMediaMessagePayload alloc] init];
    payload.extra = self.extra;
    payload.contentType = [self.class getContentType];
    payload.searchableContent = LocalizedString(@"photo_digest");;
    
    NSMutableDictionary *dataDict = [NSMutableDictionary dictionary];
    if (self.thumbParameter.length && self.size.width > 0) {
        [dataDict setValue:self.thumbParameter forKey:@"tp"];
        [dataDict setValue:@(self.size.width) forKey:@"w"];
        [dataDict setValue:@(self.size.height) forKey:@"h"];
    } else if (![[DMCCIMService sharedDMCIMService] imageThumbPara]) {
        dataDict = nil;
        payload.binaryContent = UIImageJPEGRepresentation(self.thumbnail, 1);
    } else {
        UIImage *image = [UIImage imageWithContentsOfFile:self.localPath];
        if (image) {
            [dataDict setValue:[[DMCCIMService sharedDMCIMService] imageThumbPara] forKey:@"tp"];
            [dataDict setValue:@(image.size.width) forKey:@"w"];
            [dataDict setValue:@(image.size.height) forKey:@"h"];
        } else {
            payload.binaryContent = UIImageJPEGRepresentation(self.thumbnail, 1);
            dataDict = nil;
        }
    }
    
    if (dataDict) {
        NSData *data = [NSJSONSerialization dataWithJSONObject:dataDict
                                                       options:kNilOptions
                                                         error:nil];
        payload.content = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
    }
    
    payload.mediaType = Media_Type_IMAGE;
    payload.remoteMediaUrl = self.remoteUrl;
    payload.localMediaPath = self.localPath;
    return payload;
}

- (void)decode:(DMCCMessagePayload *)payload {
    [super decode:payload];
    if ([payload isKindOfClass:[DMCCMediaMessagePayload class]]) {
        DMCCMediaMessagePayload *mediaPayload = (DMCCMediaMessagePayload *)payload;
        if ([payload.binaryContent length]) {
            self.thumbnail = [UIImage imageWithData:payload.binaryContent];
        }
        self.remoteUrl = mediaPayload.remoteMediaUrl;
        self.localPath = mediaPayload.localMediaPath;
        if (mediaPayload.content.length) {
            NSError *__error = nil;
            NSDictionary *dictionary = [NSJSONSerialization JSONObjectWithData:[payload.content dataUsingEncoding:NSUTF8StringEncoding]
                                                                       options:kNilOptions
                                                                         error:&__error];
            if (!__error) {
                NSString *str1 = dictionary[@"w"];
                NSString *str2 = dictionary[@"h"];
                self.thumbParameter = dictionary[@"tp"];
                self.size = CGSizeMake([str1 intValue], [str2 intValue]);
            }
        }
    }
}

- (UIImage *)thumbnail {
    if (!_thumbnail && self.localPath.length && [[NSFileManager defaultManager] isExecutableFileAtPath:self.localPath]) {
        UIImage *image = [UIImage imageWithContentsOfFile:self.localPath];
        _thumbnail = [DMCCUtilities generateThumbnail:image withWidth:120 withHeight:120];
    }
    return _thumbnail;
}

+ (int)getContentType {
    return MESSAGE_CONTENT_TYPE_IMAGE;
}

+ (int)getContentFlags {
    return DMCCPersistFlag_PERSIST_AND_COUNT;
}




+ (void)load {
    [[DMCCIMService sharedDMCIMService] registerMessageContent:self];
}

- (NSString *)digest:(DMCCMessage *)message {
    return LocalizedString(@"photo_digest");
}
@end
