//
//  ZoloOssManager.m
//  NewZolo
//
//  Created by JTalking on 2022/8/8.
//

#import "ZoloOssManager.h"
#import "SSZipArchive/SSZipArchive.h"

#define WS(weakSelf)  __weak __typeof(&*self)weakSelf = self;

NSString *ossDataExceed = @"DataExceed";

@implementation ZoloOssManager


static ZoloOssManager *_ossManager;

+ (instancetype)allocWithZone:(struct _NSZone *)zone {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _ossManager = [super allocWithZone:zone];
    });
    return _ossManager;
}

+ (instancetype)instanceManager {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _ossManager = [[self alloc] init];
    });
    return _ossManager;
}

- (id)copyWithZone:(NSZone *)zone {
    return _ossManager;
}

- (void)setupClientWithOSS_ACCESSKEY_ID:(NSString *)OSS_ACCESSKEY_ID_String
                   withOSS_SECRETKEY_ID:(NSString *)OSS_SECRETKEY_ID_String
                       withOSS_ENDPOINT:(NSString *)OSS_ENDPOINT_String
               withOSS_BUCKET_IMAGE_Str:(NSString *)OSS_BUCKET_IMAGE_String
               withOSS_NAME_USERPOR_Str:(NSString *)OSS_NAME_USERPOR_String
              withOSS_NAME_GROUPPOR_Str:(NSString *)OSS_NAME_GROUPPOR_String
                 withOSS_NAME_OTHER_Str:(NSString *)OSS_NAME_OTHER_String {
    self.OSS_ACCESSKEY_ID_Str = OSS_ACCESSKEY_ID_String;
    self.OSS_SECRETKEY_ID_Str = OSS_SECRETKEY_ID_String;
    self.OSS_ENDPOINT_Str = OSS_ENDPOINT_String;
    self.OSS_BUCKET_IMAGE_Str = OSS_BUCKET_IMAGE_String;
    self.OSS_NAME_USERPOR_Str = OSS_NAME_USERPOR_String;
    self.OSS_NAME_GROUPPOR_Str = OSS_NAME_GROUPPOR_String;
    self.OSS_NAME_OTHER_Str = OSS_NAME_OTHER_String;
    
    if (self.OSS_ACCESSKEY_ID_Str.length > 0) {
        id<OSSCredentialProvider> credential = [[OSSPlainTextAKSKPairCredentialProvider alloc] initWithPlainTextAccessKey:self.OSS_ACCESSKEY_ID_Str secretKey:self.OSS_SECRETKEY_ID_Str];
        _client = [[OSSClient alloc] initWithEndpoint:self.OSS_ENDPOINT_Str credentialProvider:credential];
        NSLog(@"==setupClientWithOSS_ACCESSKEY_ID=2222=");
    } else {
        id<OSSCredentialProvider> credential = [[OSSPlainTextAKSKPairCredentialProvider alloc] initWithPlainTextAccessKey:OSS_ACCESSKEY_ID secretKey:OSS_SECRETKEY_ID];
        _client = [[OSSClient alloc] initWithEndpoint:OSS_ENDPOINT credentialProvider:credential];
        NSLog(@"==setupClientWithOSS_ACCESSKEY_ID=1111=");
    }
}

- (NSString *)tempZipPath {
    NSString *path = [NSString stringWithFormat:@"%@/%@.zip",
                      NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES)[0],
                      [NSUUID UUID].UUIDString];
    return path;
}

//产生length个长度随机字符串
- (NSString *)getRandStringWithLength:(int)length {
    NSString *sourceStr = @"abcdefghijklmnopqrstuvwxyz";
    NSMutableString *resultStr = [[NSMutableString alloc] init];
    for (int i = 0; i < length; i++) {
        unsigned index = arc4random() % [sourceStr length];
        NSString *oneStr = [sourceStr substringWithRange:NSMakeRange(index, 1)];
        [resultStr appendString:oneStr];
    }
    return resultStr;
}

- (void)uploadDeskeyFileWithOssType:(OSSUploadType)type WithName:(NSString *)localPath  password:(NSString *)password CompleteBlock:(void (^)(NSString *data, NSInteger size, NSString *pwd))completeBlock {

    NSDateFormatter *formatter = [[NSDateFormatter alloc]init];
    formatter.dateFormat = @"yyyyMMddHHmmss";
    NSString *str = [formatter stringFromDate:[NSDate date]];
    NSString *fileName = [NSString stringWithFormat:@"%@",str];
    NSLog(@"%@",fileName);
    
    NSString *bucketStr = OSS_BUCKET_IMAGE;
    if (self.OSS_BUCKET_IMAGE_Str.length > 0) {
        bucketStr = self.OSS_BUCKET_IMAGE_Str;
    }
    
    NSString *nameOther = OSS_NAME_OTHER;
    if (self.OSS_NAME_OTHER_Str.length > 0) {
        nameOther = self.OSS_NAME_OTHER_Str;
    }
    
    OSSPutObjectRequest * request = [OSSPutObjectRequest new];
    if (type == OSSUploadType_Image) {
        request.bucketName = bucketStr;
        NSString *imageKey = [NSString stringWithFormat:@"image%@.zip",fileName];
        request.objectKey = [NSString stringWithFormat:@"%@%@", nameOther, imageKey];
    } else if (type == OSSUploadType_Video) {
        NSString *videoKey = [NSString stringWithFormat:@"video%@.zip",fileName];
        request.objectKey = videoKey;
        request.bucketName = bucketStr;
    } else if (type == OSSUploadType_Voice) {
        NSString *videoKey = [NSString stringWithFormat:@"voice%@.zip",fileName];
        request.objectKey = videoKey;
        request.bucketName = bucketStr;
    } else {
        NSString *fileKey = [NSString stringWithFormat:@"file%@%@",fileName,[localPath lastPathComponent]];
        request.bucketName = bucketStr;
        request.objectKey = fileKey;
    }
    
    if (type == OSSUploadType_Image || type == OSSUploadType_Video || type == OSSUploadType_Voice) {
        NSString *zipPath = [self tempZipPath];
        BOOL success = [SSZipArchive createZipFileAtPath:zipPath withFilesAtPaths:@[localPath] withPassword:password];
        if (success) {
            NSData* fileData = nil;
            fileData = [NSData dataWithContentsOfFile:zipPath];
            // 不能超过50M
            NSLog(@"==fileData==%ld=", fileData.length);
            if (fileData.length > 52428800) {
                dispatch_async(dispatch_get_main_queue(), ^{
                  [[NSNotificationCenter defaultCenter] postNotificationName:ossDataExceed object:nil];
                });
                completeBlock(nil, -1, nil);
                return;
            }
            
            request.uploadingData = fileData;
            __block int64_t totalSend = 0;
            request.uploadProgress = ^(int64_t bytesSent, int64_t totalByteSent, int64_t totalBytesExpectedToSend) {
                NSLog(@"1 -------------------- %lld, %lld, %lld", bytesSent, totalByteSent, totalBytesExpectedToSend);
                totalSend = totalBytesExpectedToSend;
            };
            
            OSSTask * task = [_client putObject:request];
            WS(ws);
            [[task continueWithBlock:^id(OSSTask *task) {
                OSSPutObjectResult * result = task.result;
                
                task = [ws.client presignPublicURLWithBucketName:request.bucketName withObjectKey:request.objectKey];
                if (task.completed) {
                    completeBlock(task.result, totalSend, password);
                }
                
                if (task.error) {
        //            OSSLogError(@"%@", task.error);
                }
          
                NSLog(@"Result - requestId: %@, headerFields: %@, servercallback: %@",
                             result.requestId,
                             result.httpResponseHeaderFields,
                             result.serverReturnJsonString);
                return nil;
            }] waitUntilFinished];
        }
    } else {
        NSData* fileData = nil;
        if (type == OSSUploadType_CloudFile) {
            NSError *error = nil;
            fileData = [NSData dataWithContentsOfURL:[NSURL URLWithString:localPath] options:NSDataReadingMappedIfSafe error:&error];
        } else {
            fileData = [NSData dataWithContentsOfFile:localPath];
        }
        // 不能超过50M
        NSLog(@"==fileData==%ld=", fileData.length);
        if (fileData.length > 52428800) {
            dispatch_async(dispatch_get_main_queue(), ^{
              [[NSNotificationCenter defaultCenter] postNotificationName:ossDataExceed object:nil];
            });
            return;
        }
        request.uploadingData = fileData;
        __block int64_t totalSend = 0;
        request.uploadProgress = ^(int64_t bytesSent, int64_t totalByteSent, int64_t totalBytesExpectedToSend) {
            NSLog(@"1 -------------------- %lld, %lld, %lld", bytesSent, totalByteSent, totalBytesExpectedToSend);
            totalSend = totalBytesExpectedToSend;
        };
        
        OSSTask * task = [_client putObject:request];
        WS(ws);
        [[task continueWithBlock:^id(OSSTask *task) {
            OSSPutObjectResult * result = task.result;
            
            task = [ws.client presignPublicURLWithBucketName:request.bucketName withObjectKey:request.objectKey];
            if (task.completed) {
                completeBlock(task.result, totalSend, @"");
            }
            
            if (task.error) {
    //            OSSLogError(@"%@", task.error);
            }
      
            NSLog(@"Result - requestId: %@, headerFields: %@, servercallback: %@",
                         result.requestId,
                         result.httpResponseHeaderFields,
                         result.serverReturnJsonString);
            return nil;
        }] waitUntilFinished];
    }
}

- (void)uploadFileWithOssType:(OSSUploadType)type WithName:(NSString *)localPath CompleteBlock:(void (^)(NSString *data, NSInteger size, NSString *pwd))completeBlock {
    NSDateFormatter *formatter = [[NSDateFormatter alloc]init];
    formatter.dateFormat = @"yyyyMMddHHmmss";
    NSString *str = [formatter stringFromDate:[NSDate date]];
    NSString *fileName = [NSString stringWithFormat:@"%@",str];
    NSLog(@"%@",fileName);
    
    NSString *bucketStr = OSS_BUCKET_IMAGE;
    if (self.OSS_BUCKET_IMAGE_Str.length > 0) {
        bucketStr = self.OSS_BUCKET_IMAGE_Str;
    }
    
    NSString *nameOther = OSS_NAME_OTHER;
    if (self.OSS_NAME_OTHER_Str.length > 0) {
        nameOther = self.OSS_NAME_OTHER_Str;
    }
    
    OSSPutObjectRequest * request = [OSSPutObjectRequest new];
    if (type == OSSUploadType_Image) {
        request.bucketName = bucketStr;
        NSString *imageKey = [NSString stringWithFormat:@"image%@.png",fileName];
        request.objectKey = [NSString stringWithFormat:@"%@%@", nameOther, imageKey];
    } else if (type == OSSUploadType_Video) {
        NSString *videoKey = [NSString stringWithFormat:@"video%@.mp4",fileName];
        request.objectKey = videoKey;
        request.bucketName = bucketStr;
    } else if (type == OSSUploadType_Voice) {
        NSString *videoKey = [NSString stringWithFormat:@"voice%@.mp3",fileName];
        request.objectKey = videoKey;
        request.bucketName = bucketStr;
    } else {
        NSString *fileKey = [NSString stringWithFormat:@"file%@%@",fileName,[localPath lastPathComponent]];
        request.bucketName = bucketStr;
        request.objectKey = fileKey;
    }
    
    NSData* fileData = nil;
    if (type == OSSUploadType_CloudFile) {
        NSError *error = nil;
        fileData = [NSData dataWithContentsOfURL:[NSURL URLWithString:localPath] options:NSDataReadingMappedIfSafe error:&error];
    } else {
        fileData = [NSData dataWithContentsOfFile:localPath];
    }
    // 不能超过50M
    if (fileData.length > 52428800) {
        dispatch_async(dispatch_get_main_queue(), ^{
          [[NSNotificationCenter defaultCenter] postNotificationName:ossDataExceed object:nil];
        });
        return;
    }
    request.uploadingData = fileData;
    __block int64_t totalSend = 0;
    request.uploadProgress = ^(int64_t bytesSent, int64_t totalByteSent, int64_t totalBytesExpectedToSend) {
        NSLog(@"1 -------------------- %lld, %lld, %lld", bytesSent, totalByteSent, totalBytesExpectedToSend);
        totalSend = totalBytesExpectedToSend;
    };
    
    OSSTask * task = [_client putObject:request];
    WS(ws);
    [[task continueWithBlock:^id(OSSTask *task) {
        OSSPutObjectResult * result = task.result;
        
        task = [ws.client presignPublicURLWithBucketName:request.bucketName withObjectKey:request.objectKey];
        if (task.completed) {
            completeBlock(task.result, totalSend, @"");
        }
        
        if (task.error) {
//            OSSLogError(@"%@", task.error);
        }
  
        NSLog(@"Result - requestId: %@, headerFields: %@, servercallback: %@",
                     result.requestId,
                     result.httpResponseHeaderFields,
                     result.serverReturnJsonString);
        return nil;
    }] waitUntilFinished];
}

- (void)uploadImageWithType:(OSSUploadImageType)type WithName:(NSData *)imgData CompleteBlock:(void (^)(NSString *str))completeBlock {
    NSDateFormatter *formatter = [[NSDateFormatter alloc]init];
    formatter.dateFormat = @"yyyyMMddHHmmss";
    NSString *str = [formatter stringFromDate:[NSDate date]];
    NSString *fileName = [NSString stringWithFormat:@"%@",str];
    NSLog(@"%@",fileName);
    
    NSString *imageKey = [NSString stringWithFormat:@"image%@.png",fileName];
    
    NSString *bucketStr = OSS_BUCKET_IMAGE;
    if (self.OSS_BUCKET_IMAGE_Str.length > 0) {
        bucketStr = self.OSS_BUCKET_IMAGE_Str;
    }
    
    NSString *userOther = OSS_NAME_USERPOR;
    if (self.OSS_NAME_USERPOR_Str.length > 0) {
        userOther = self.OSS_NAME_USERPOR_Str;
    }
    
    NSString *groupOther = OSS_NAME_GROUPPOR;
    if (self.OSS_NAME_GROUPPOR_Str.length > 0) {
        groupOther = self.OSS_NAME_GROUPPOR_Str;
    }
    
    NSString *nameOther = OSS_NAME_OTHER;
    if (self.OSS_NAME_OTHER_Str.length > 0) {
        nameOther = self.OSS_NAME_OTHER_Str;
    }
    
    OSSPutObjectRequest * request = [OSSPutObjectRequest new];
    request.bucketName = bucketStr;
    if (type == OSSUploadImageType_User) {
        request.objectKey = [NSString stringWithFormat:@"%@%@", userOther, imageKey];
    } else if (type == OSSUploadImageType_Group) {
        request.objectKey = [NSString stringWithFormat:@"%@%@", groupOther, imageKey];
    } else {
        request.objectKey = [NSString stringWithFormat:@"%@%@", nameOther, imageKey];
    }
    
    request.uploadingData = imgData;
    request.uploadProgress = ^(int64_t bytesSent, int64_t totalByteSent, int64_t totalBytesExpectedToSend) {
        NSLog(@"1 -------------------- %lld, %lld, %lld", bytesSent, totalByteSent, totalBytesExpectedToSend);
    };
    
    OSSTask * task = [_client putObject:request];
    WS(ws);
    [[task continueWithBlock:^id(OSSTask *task) {
        OSSPutObjectResult * result = task.result;
        
        task = [ws.client presignPublicURLWithBucketName:request.bucketName withObjectKey:request.objectKey];
        if (task.completed) {
            completeBlock(task.result);
        }
        
        if (task.error) {
//            OSSLogError(@"%@", task.error);
        }
  
        NSLog(@"Result - requestId: %@, headerFields: %@, servercallback: %@",
                     result.requestId,
                     result.httpResponseHeaderFields,
                     result.serverReturnJsonString);
        return nil;
    }] waitUntilFinished];
}

@end
