//
//  HttpUtils.m
//  DMChatClient
//
//  Created by abc on 2021/1/20.
//  Copyright © 2021 WildFireChat. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "HttpUtils.h"
#import "OsnUtils.h"
#import "SSZipArchive/SSZipArchive.h"

@implementation HttpUtils

+ (NSData*) doGet:(NSString*)url {
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:url] cachePolicy:0 timeoutInterval:5.0f];
    request.HTTPMethod = @"GET";
    [request setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    __block NSData *result = nil;
    NSURLSession *session = [NSURLSession sharedSession];
    dispatch_semaphore_t semaphore = dispatch_semaphore_create(0);
    [[session dataTaskWithRequest:request completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
        result = data;
        dispatch_semaphore_signal(semaphore);
    }]resume];
    dispatch_semaphore_wait(semaphore, dispatch_time(DISPATCH_TIME_NOW,NSEC_PER_SEC*10));
    return result;
}
+ (NSData*) doPost:(NSString*)url data:(NSString*)data{
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:url] cachePolicy:0 timeoutInterval:5.0f];
    request.HTTPMethod = @"POST";
    request.HTTPBody = [data dataUsingEncoding:NSUTF8StringEncoding];
    [request setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    __block NSData *result = nil;
    NSURLSession *session = [NSURLSession sharedSession];
    dispatch_semaphore_t semaphore = dispatch_semaphore_create(0);
    [[session dataTaskWithRequest:request completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
        result = data;
        dispatch_semaphore_signal(semaphore);
        if(error != nil)
            NSLog(@"doPost error: %@", error);
    }]resume];
    dispatch_semaphore_wait(semaphore, dispatch_time(DISPATCH_TIME_NOW,NSEC_PER_SEC*10));
    return result;
}
+ (NSMutableDictionary*) doPosts:(NSString*)url data:(NSString*)data{
    NSData *result = [HttpUtils doPost:url data:data];
    if(result == nil)
        return nil;
    return [OsnUtils json2Dic:result];
}
- (void) upload:(NSString*) sUrl type:(NSString*) type name:(NSString*) fileName data:(NSData*) data cb:(onResult)cb progress:(onProgress)progress{
    @try{
        NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:sUrl] cachePolicy:0 timeoutInterval:5.0f];
    
        NSString *boundary = [NSString stringWithFormat:@"----------%ld",[OsnUtils getTimeStamp]];
        [request setHTTPMethod:@"POST"];
        [request setValue:[NSString stringWithFormat:@"multipart/form-data; boundary=%@",boundary] forHTTPHeaderField:@"Content-Type"];
        
        NSMutableData *body = [NSMutableData data];
        NSString* prefix = [type isEqualToString:@"portrait"] ? @"P":@"C";
        [body appendData:[[NSString stringWithFormat:@"--%@\r\n",boundary]dataUsingEncoding:NSUTF8StringEncoding]];
        [body appendData:[[NSString stringWithFormat:@"Content-Disposition: form-data;name=\"%@%@\"\r\n\r\n\r\n",prefix,fileName]dataUsingEncoding:NSUTF8StringEncoding]];
        [body appendData:[[NSString stringWithFormat:@"--%@\r\n",boundary]dataUsingEncoding:NSUTF8StringEncoding]];
        [body appendData:[[NSString stringWithFormat:@"Content-Disposition: form-data;name=\"file\";filename=\"%@%@\"\r\n",prefix,fileName]dataUsingEncoding:NSUTF8StringEncoding]];
        [body appendData:[[NSString stringWithFormat:@"Content-Type:application/octet-stream\r\n\r\n"]dataUsingEncoding:NSUTF8StringEncoding]];
        [body appendData:data];
        [body appendData:[[NSString stringWithFormat:@"\r\n\r\n--%@--\r\n",boundary]dataUsingEncoding:NSUTF8StringEncoding]];
        
        NSURLSessionConfiguration *config = [NSURLSessionConfiguration defaultSessionConfiguration];
        NSURLSession *session = [NSURLSession sessionWithConfiguration:config delegate:self delegateQueue:[NSOperationQueue mainQueue]];
        NSURLSessionUploadTask *task = [session uploadTaskWithRequest:request fromData:body completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
            if (!error) {
                NSMutableDictionary *json = [OsnUtils json2Dic:data];
                cb(true,json,nil);
            }else{
                NSLog(@"error --- %@", error.localizedDescription);
                cb(false,nil,error.localizedDescription);
            }
        }];
        self.progress = progress;
        [task resume];
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
        if(cb != nil)
            cb(false,nil,e.reason);
    }
}

- (NSString *)tempZipPath {
    NSString *path = [NSString stringWithFormat:@"%@/%@",
                      NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES)[0],
                      [NSUUID UUID].UUIDString];
    return path;
}

- (void) download:(NSString*) sUrl path:(NSString*) path decKey:(NSString *)decKey cb:(onResult)cb progress:(onProgress)progress{
    if (sUrl.length == 0 || sUrl == nil) {
        return;
    }
    if (![sUrl hasPrefix:@"http"]) {
        return;
    }
    @try {
        NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:sUrl] cachePolicy:0 timeoutInterval:5.0f];
        NSURLSessionConfiguration *config = [NSURLSessionConfiguration defaultSessionConfiguration];
        NSURLSession *session = [NSURLSession sessionWithConfiguration:config delegate:self delegateQueue:[NSOperationQueue mainQueue]];
        __block dispatch_semaphore_t semaphore = 0;
        if(cb == nil){
            semaphore = dispatch_semaphore_create(0);
        }
        NSURLSessionDownloadTask *task = [session downloadTaskWithRequest:request completionHandler:^(NSURL * _Nullable location, NSURLResponse * _Nullable response, NSError * _Nullable error) {
            if (location == nil) {
                return;
            }
           NSString *temPath = NSTemporaryDirectory();
           NSString *filePath = [temPath stringByAppendingPathComponent:response.suggestedFilename];
           NSLog(@"filePath = %@",filePath);
           NSURL *toURL = [NSURL fileURLWithPath:filePath];
           [[NSFileManager defaultManager] moveItemAtURL:location toURL:toURL error:nil];
            
            NSString *desPath = [self tempZipPath];
            
            [SSZipArchive unzipFileAtPath:filePath
                            toDestination:desPath
                                overwrite:NO
                                 password:decKey
                          progressHandler:^(NSString * _Nonnull entry, unz_file_info zipInfo, long entryNumber, long total) { }
                        completionHandler:^(NSString * _Nonnull path, BOOL succeeded, NSError * _Nullable error) {
                            if (succeeded) {
                                
                                if(!error){
                                    if(cb != nil){
                                        cb(true,nil,nil);
                                    }
                                }else{
                                    NSLog(@"error --- %@", error.localizedDescription);
                                    if(cb != nil){
                                        cb(false,nil,error.localizedDescription);
                                    }
                                }
                                if(semaphore){
                                    dispatch_semaphore_signal(semaphore);
                                }
         
                                NSError *error = nil;
                                NSMutableArray<NSString *> *items = [[[NSFileManager defaultManager]
                                                                      contentsOfDirectoryAtPath:desPath
                                                                      error:&error] mutableCopy];
                                if (error) {
                                    return;
                                }

                                [items enumerateObjectsUsingBlock:^(NSString * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
                                    NSURL *url = [NSURL fileURLWithPath:[NSString stringWithFormat:@"%@/%@",desPath,obj]];
                                    [[NSFileManager defaultManager] moveItemAtURL:url toURL:[NSURL fileURLWithPath:self.downPath] error:nil];
                                }];
                                
                }
            }];
        }];
        self.downPath = path;
        self.progress = progress;
        NSLog(@"path==== %@",path);
        [task resume];
        if(semaphore){
            dispatch_semaphore_wait(semaphore, DISPATCH_TIME_FOREVER);
        }
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
        cb(false,nil,e.reason);
    }
}

- (void) download:(NSString*) sUrl path:(NSString*) path cb:(onResult)cb progress:(onProgress)progress{
    if (sUrl.length == 0 || sUrl == nil) {
        return;
    }
    if (![sUrl hasPrefix:@"http"]) {
        return;
    }
    @try {
        NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:sUrl] cachePolicy:0 timeoutInterval:5.0f];
        NSURLSessionConfiguration *config = [NSURLSessionConfiguration defaultSessionConfiguration];
        NSURLSession *session = [NSURLSession sessionWithConfiguration:config delegate:self delegateQueue:[NSOperationQueue mainQueue]];
        __block dispatch_semaphore_t semaphore = 0;
        if(cb == nil){
            semaphore = dispatch_semaphore_create(0);
        }
        NSURLSessionDownloadTask *task = [session downloadTaskWithRequest:request completionHandler:^(NSURL * _Nullable location, NSURLResponse * _Nullable response, NSError * _Nullable error) {
            if (location == nil) {
                return;
            }
            [[NSFileManager defaultManager] moveItemAtURL:location toURL:[NSURL fileURLWithPath:self.downPath] error:nil];
            if(!error){
                if(cb != nil){
                    cb(true,nil,nil);
                }
            }else{
                NSLog(@"error --- %@", error.localizedDescription);
                if(cb != nil){
                    cb(false,nil,error.localizedDescription);
                }
            }
            if(semaphore){
                dispatch_semaphore_signal(semaphore);
            }
        }];
        self.downPath = path;
        self.progress = progress;
  
        [task resume];
        if(semaphore){
            dispatch_semaphore_wait(semaphore, DISPATCH_TIME_FOREVER);
        }
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
        cb(false,nil,e.reason);
    }
}

- (void)URLSession:(NSURLSession *)session downloadTask:(NSURLSessionDownloadTask *)downloadTask
didFinishDownloadingToURL:(NSURL *)location {
    [[NSFileManager defaultManager] moveItemAtPath:location.path toPath:self.downPath error:nil];
}
- (void)URLSession:(NSURLSession *)session downloadTask:(NSURLSessionDownloadTask *)downloadTask
      didWriteData:(int64_t)bytesWritten
 totalBytesWritten:(int64_t)totalBytesWritten
totalBytesExpectedToWrite:(int64_t)totalBytesExpectedToWrite {
    self.progress(bytesWritten,totalBytesWritten);
}
- (void)URLSession:(NSURLSession *)session task: (NSURLSessionTask *)task didCompleteWithError:(NSError *)error {
    NSLog(@"%@",error);
}
- (void)URLSession:(NSURLSession *)session task:(NSURLSessionTask *)task
   didSendBodyData:(int64_t)bytesSent
    totalBytesSent:(int64_t)totalBytesSent
totalBytesExpectedToSend:(int64_t)totalBytesExpectedToSend {
    if(self.progress != nil){
        self.progress(bytesSent,totalBytesSent);
    }
}

@end

    
