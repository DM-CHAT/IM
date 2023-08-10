//
//  MediaMessageDownloader.m
//  WildFireChat
//
//  Created by heavyrain lee on 2018/8/29.
//  Copyright © 2018 WildFireChat. All rights reserved.
//

#import "DMCUMediaMessageDownloader.h"
#import "AFHTTPSessionManager.h"
#import <DMChatClient/DMCChatClient.h>
#import "DMCUConfigManager.h"
#import "DMCUUtilities.h"

static DMCUMediaMessageDownloader *sharedSingleton = nil;

@interface DMCUMediaMessageDownloader()
@property(nonatomic, strong)NSMutableDictionary<NSString*, NSNumber*> *downloadingMessages;
@end



@implementation DMCUMediaMessageDownloader
+ (instancetype)sharedDownloader {
    if (sharedSingleton == nil) {
        @synchronized (self) {
            if (sharedSingleton == nil) {
                sharedSingleton = [[DMCUMediaMessageDownloader alloc] init];
                sharedSingleton.downloadingMessages = [[NSMutableDictionary alloc] init];
            }
        }
    }
    
    return sharedSingleton;
}

- (void)downloadFileWithURL:(NSString*)requestURLString
                 parameters:(NSDictionary *)parameters
                  savedPath:(NSString*)savedPath
            downloadSuccess:(void (^)(NSURLResponse *response, NSURL *filePath))success
            downloadFailure:(void (^)(NSError *error))failure
           downloadProgress:(void (^)(NSProgress *downloadProgress))progress

{
    
    AFHTTPRequestSerializer *serializer = [AFHTTPRequestSerializer serializer];
    NSMutableURLRequest *request =[serializer requestWithMethod:@"GET" URLString:requestURLString parameters:parameters error:nil];
    NSURLSessionDownloadTask *task = [[AFHTTPSessionManager manager] downloadTaskWithRequest:request progress:^(NSProgress * _Nonnull downloadProgress) {
        progress(downloadProgress);
    } destination:^NSURL * _Nonnull(NSURL * _Nonnull targetPath, NSURLResponse * _Nonnull response) {
        return [NSURL fileURLWithPath:[savedPath stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    } completionHandler:^(NSURLResponse * _Nonnull response, NSURL * _Nullable filePath, NSError * _Nullable error) {
        if(error){
            failure(error);
        }else{
            success(response,filePath);
        }
    }];
    [task resume];
    
}

- (BOOL)tryDownload:(DMCCMessage *)msg
            success:(void(^)(long long messageUid, NSString *localPath))successBlock
              error:(void(^)(long long messageUid, int error_code))errorBlock {
    long long messageUid = msg.messageUid;
    
    if (!messageUid) {
        NSLog(@"Error, try download message have invalid uid");
        errorBlock(0, -2);
        return NO;
    }
    
    if (![msg.content isKindOfClass:[DMCCMediaMessageContent class]]) {
        NSLog(@"Error, try download message with id %lld, but it's not media message", messageUid);
        errorBlock(msg.messageUid, -1);
        return NO;
    }
    
    DMCCMediaMessageContent *mediaContent = (DMCCMediaMessageContent *)msg.content;
    if (mediaContent.localPath.length && [DMCUUtilities isFileExist:mediaContent.localPath]) {
        successBlock(msg.messageUid, mediaContent.localPath);
        return NO;
    }
    
    DMCCMediaMessagePayload *payload = (DMCCMediaMessagePayload *)[msg.content encode];
    NSString *cacheDir = [[DMCUConfigManager globalManager] cachePathOf:msg.conversation mediaType:payload.mediaType];
    
    if (self.downloadingMessages[mediaContent.remoteUrl] != nil) {
        return NO;
    }
    
    NSString *savedPath = [cacheDir stringByAppendingPathComponent:[NSString stringWithFormat:@"media_%lld", messageUid]];

    if ([mediaContent isKindOfClass:[DMCCSoundMessageContent class]]) {
        if ([mediaContent.remoteUrl pathExtension].length) {
            savedPath = [savedPath stringByAppendingFormat:@".%@", [mediaContent.remoteUrl pathExtension]];
        }
    } else if([mediaContent isKindOfClass:[DMCCVideoMessageContent class]]) {
        savedPath = [NSString stringWithFormat:@"%@.mp4", savedPath];
    } else if([mediaContent isKindOfClass:[DMCCImageMessageContent class]]) {
        savedPath = [NSString stringWithFormat:@"%@.jpg", savedPath];
    } else if([mediaContent isKindOfClass:[DMCCFileMessageContent class]]) {
        DMCCFileMessageContent *content = (DMCCFileMessageContent *)mediaContent;
        savedPath = [cacheDir stringByAppendingPathComponent:content.name];
    }
    
    savedPath = [DMCUUtilities getUnduplicatedPath:savedPath];
    NSFileManager *fileManager = [NSFileManager defaultManager];
    if ([fileManager fileExistsAtPath:savedPath]) {
        mediaContent.localPath = savedPath;
        [[DMCCIMService sharedDMCIMService] updateMessage:msg.messageId content:mediaContent];
        
        dispatch_async(dispatch_get_main_queue(), ^{
            successBlock(msg.messageUid, savedPath);
        });
        return NO;
    }
    
    
    [self.downloadingMessages setObject:@(messageUid) forKey:mediaContent.remoteUrl];
    
    //通知UI开始显示下载动画
    [[NSNotificationCenter defaultCenter] postNotificationName:kMediaMessageStartDownloading object:@(messageUid)];
    
    
    [self downloadFileWithURL:mediaContent.remoteUrl parameters:nil savedPath:savedPath downloadSuccess:^(NSURLResponse *response, NSURL *filePath) {
        NSLog(@"download message content of %lld success", messageUid);
        dispatch_async(dispatch_get_main_queue(), ^{
            [[DMCUMediaMessageDownloader sharedDownloader].downloadingMessages removeObjectForKey:mediaContent.remoteUrl];
            DMCCMessage *newMsg = msg;
            if (msg.conversation.type != Chatroom_Type) {
                newMsg = [[DMCCIMService sharedDMCIMService] getMessageByUid:messageUid];
            }
            
            if (!newMsg) {
                NSLog(@"Error, message %lld not exist", messageUid);
                errorBlock(msg.messageUid, -1);
                [[NSNotificationCenter defaultCenter] postNotificationName:kMediaMessageDownloadFinished object:@(messageUid) userInfo:@{@"result":@(NO)}];
                return;
            }
            
            if (![newMsg.content isKindOfClass:[DMCCMediaMessageContent class]]) {
                NSLog(@"Error, message %lld not media message", messageUid);
                errorBlock(msg.messageUid, -1);
                [[NSNotificationCenter defaultCenter] postNotificationName:kMediaMessageDownloadFinished object:@(messageUid) userInfo:@{@"result":@(NO)}];
                return;
            }
            
            DMCCMediaMessageContent *newContent = (DMCCMediaMessageContent *)newMsg.content;
            newContent.localPath = filePath.absoluteString;
            [[DMCCIMService sharedDMCIMService] updateMessage:newMsg.messageId content:newContent];
            
            mediaContent.localPath = filePath.absoluteString;
            successBlock(newMsg.messageUid, filePath.absoluteString);
            
            [[NSNotificationCenter defaultCenter] postNotificationName:kMediaMessageDownloadFinished object:@(messageUid) userInfo:@{@"result":@(YES), @"localPath":filePath.absoluteString}];
        });
    } downloadFailure:^(NSError *error) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [[DMCUMediaMessageDownloader sharedDownloader].downloadingMessages removeObjectForKey:mediaContent.remoteUrl];
            
            errorBlock(msg.messageUid, -1);
            
            [[NSNotificationCenter defaultCenter] postNotificationName:kMediaMessageDownloadFinished object:@(messageUid) userInfo:@{@"result":@(NO)}];
        });
        NSLog(@"download message content of %lld failure with error %@", messageUid, error);
    } downloadProgress:^(NSProgress *downloadProgress) {
        NSLog(@"总大小：%lld,当前大小:%lld",downloadProgress.totalUnitCount,downloadProgress.completedUnitCount);
    }];
    return YES;
}

- (BOOL)tryDownload:(NSString *)mediaPath
                uid:(long long)uid
          mediaType:(DownloadMediaType)mediaType
            success:(void(^)(long long uid, NSString *localPath))successBlock
              error:(void(^)(long long uid, int error_code))errorBlock {
    
    if (!uid) {
        NSLog(@"Error, try download message have invalid uid");
        errorBlock(0, -2);
        return NO;
    }
    
    if (!mediaPath.length) {
        NSLog(@"Error, try download message with id %lld, but it's not media message", uid);
        errorBlock(uid, -1);
        return NO;
    }
    
    DMCCMessage *msg = [[DMCCIMService sharedDMCIMService] getMessageByUid:uid];
    
    NSString *cacheDir;
    if(msg) {
        DMCCMediaMessagePayload *payload = (DMCCMediaMessagePayload *)[msg.content encode];
        cacheDir = [[DMCUConfigManager globalManager] cachePathOf:msg.conversation mediaType:payload.mediaType];
        
    } else {
        NSFileManager *fileManager = [NSFileManager defaultManager];
        BOOL isDir;
        NSError *error = nil;
        NSString *downloadDir = [NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES).firstObject stringByAppendingString:@"/download"];
        if (![fileManager fileExistsAtPath:downloadDir isDirectory:&isDir]) {
            if(![fileManager createDirectoryAtPath:downloadDir withIntermediateDirectories:YES attributes:nil error:&error]) {
                errorBlock(uid, -1);
                NSLog(@"Error, create download folder error");
                return NO;
            }
            if (error) {
                errorBlock(uid, -1);
                NSLog(@"Error, create download folder error:%@", error);
                return NO;
            }
        }
        if (!isDir) {
            errorBlock(uid, -1);
            NSLog(@"Error, create download folder error");
            return NO;
        }
        cacheDir = downloadDir;
    }
    
    
    //通知UI开始显示下载动画
    [[NSNotificationCenter defaultCenter] postNotificationName:kMediaMessageStartDownloading object:@(uid)];
    
    
    if (self.downloadingMessages[mediaPath] != nil) {
        return NO;
    }
    
    NSString *savedPath = [cacheDir stringByAppendingPathComponent:[NSString stringWithFormat:@"media_%lld", uid]];
    switch (mediaType) {
        case DownloadMediaType_Image:
            savedPath = [NSString stringWithFormat:@"%@.jpg", savedPath];
            break;
        
        case DownloadMediaType_Voice:
            savedPath = [NSString stringWithFormat:@"%@.wav", savedPath];
            break;
            
        case DownloadMediaType_Video:
            savedPath = [NSString stringWithFormat:@"%@.mp4", savedPath];
            break;
            
        case DownloadMediaType_File:
            if(msg && [msg.content isKindOfClass:[DMCCFileMessageContent class]]) {
                DMCCFileMessageContent *fileContent = (DMCCFileMessageContent *)msg.content;
                savedPath = [cacheDir stringByAppendingPathComponent:fileContent.name];
            } else {
                savedPath = [cacheDir stringByAppendingPathComponent:[NSString stringWithFormat:@"file_%lld", uid]];
            }
            break;
        default:
            break;
    }
    savedPath = [DMCUUtilities getUnduplicatedPath:savedPath];
    
    NSFileManager *fileManager = [NSFileManager defaultManager];
    if ([fileManager fileExistsAtPath:savedPath]) {
        dispatch_async(dispatch_get_main_queue(), ^{
            successBlock(uid, savedPath);
            [[NSNotificationCenter defaultCenter] postNotificationName:kMediaMessageDownloadFinished object:@(uid) userInfo:@{@"result":@(YES), @"localPath":savedPath}];
        });
        return NO;
    }
    
    [self.downloadingMessages setObject:@(uid) forKey:mediaPath];
    
    [self downloadFileWithURL:mediaPath parameters:nil savedPath:savedPath downloadSuccess:^(NSURLResponse *response, NSURL *filePath) {
        NSLog(@"download message content of %lld success", uid);
        dispatch_async(dispatch_get_main_queue(), ^{
            [[DMCUMediaMessageDownloader sharedDownloader].downloadingMessages removeObjectForKey:mediaPath];
            successBlock(uid, filePath.absoluteString);
            [[NSNotificationCenter defaultCenter] postNotificationName:kMediaMessageDownloadFinished object:@(uid) userInfo:@{@"result":@(YES), @"localPath":filePath.absoluteString}];
        });
    } downloadFailure:^(NSError *error) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [[DMCUMediaMessageDownloader sharedDownloader].downloadingMessages removeObjectForKey:mediaPath];
            errorBlock(uid, -1);
            [[NSNotificationCenter defaultCenter] postNotificationName:kMediaMessageDownloadFinished object:@(uid) userInfo:@{@"result":@(NO)}];
        });
        NSLog(@"download message content of %lld failure with error %@", uid, error);
    } downloadProgress:^(NSProgress *downloadProgress) {
        NSLog(@"总大小：%lld,当前大小:%lld",downloadProgress.totalUnitCount,downloadProgress.completedUnitCount);
    }];
    return YES;
}

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}
@end
