//
//  ZoloOssManager.h
//  NewZolo
//
//  Created by JTalking on 2022/8/8.
//

#import <Foundation/Foundation.h>
#import "OSSHttpdns.h"
#import "OSSModel.h"
#import "OSSTask.h"
#import "OSSClient.h"

/** oss 配置 */
#define OSS_ACCESSKEY_ID                @"LTAI5tLKaJ9GyGCbAwrmUsnu"                                // 子账号id
#define OSS_SECRETKEY_ID                @"j6wqfCVeswhxelFdqBwI0XcXCs2Xbd"                          // 子账号secret
#define OSS_ENDPOINT                    @"https://oss-ap-southeast-1.aliyuncs.com"                 // 访问的阿里云endpoint

#define OSS_BUCKET_IMAGE                @"zolo-image1"                                             // image bucket名称

#define OSS_NAME_USERPOR                @"userPortrait/"                                           // 用户头像
#define OSS_NAME_GROUPPOR               @"groupPortrait/"                                          // 群组头像
#define OSS_NAME_OTHER                  @"temp/"                                                   // 其他图片

typedef NS_ENUM(NSInteger, OSSUploadType) {
    OSSUploadType_Image,                                                                                        // 图片
    OSSUploadType_Video,                                                                                        // 视频
    OSSUploadType_CloudFile,                                                                                    // cloud文件
    OSSUploadType_File,                                                                                         // 文件
    OSSUploadType_Voice,                                                                                        // 音频
    OSSUploadType_Gif,                                                                                          // 动图
};

typedef NS_ENUM(NSInteger, OSSUploadImageType) {
    OSSUploadImageType_User,                                                                                    // 用户头像
    OSSUploadImageType_Group,                                                                                   // 群组头像
    OSSUploadImageType_Other,                                                                                   // 其他文件
};

extern NSString *ossDataExceed;

NS_ASSUME_NONNULL_BEGIN

@interface ZoloOssManager : NSObject

@property (nonatomic, strong) OSSClient *client;

@property (nonatomic, copy) NSString *OSS_ACCESSKEY_ID_Str;
@property (nonatomic, copy) NSString *OSS_SECRETKEY_ID_Str;
@property (nonatomic, copy) NSString *OSS_ENDPOINT_Str;
@property (nonatomic, copy) NSString *OSS_BUCKET_IMAGE_Str;
@property (nonatomic, copy) NSString *OSS_NAME_USERPOR_Str;
@property (nonatomic, copy) NSString *OSS_NAME_GROUPPOR_Str;
@property (nonatomic, copy) NSString *OSS_NAME_OTHER_Str;

+ (instancetype)instanceManager;

- (void)setupClientWithOSS_ACCESSKEY_ID:(NSString *)OSS_ACCESSKEY_ID_String
                   withOSS_SECRETKEY_ID:(NSString *)OSS_SECRETKEY_ID_String
                       withOSS_ENDPOINT:(NSString *)OSS_ENDPOINT_String
               withOSS_BUCKET_IMAGE_Str:(NSString *)OSS_BUCKET_IMAGE_String
               withOSS_NAME_USERPOR_Str:(NSString *)OSS_NAME_USERPOR_String
              withOSS_NAME_GROUPPOR_Str:(NSString *)OSS_NAME_GROUPPOR_String
                 withOSS_NAME_OTHER_Str:(NSString *)OSS_NAME_OTHER_String;

- (void)uploadFileWithOssType:(OSSUploadType)type WithName:(NSString *)localPath CompleteBlock:(void (^)(NSString *data, NSInteger size, NSString *pwd))completeBlock;

- (void)uploadDeskeyFileWithOssType:(OSSUploadType)type WithName:(NSString *)localPath  password:(NSString *)password CompleteBlock:(void (^)(NSString *data, NSInteger size, NSString *pwd))completeBlock;

- (void)uploadImageWithType:(OSSUploadImageType)type WithName:(NSData *)imgData CompleteBlock:(void (^)(NSString *str))completeBlock;

@end

NS_ASSUME_NONNULL_END
