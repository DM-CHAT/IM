//
//  MHHelperUtils.h
//  JTalking
//
//  Created by JTalking on 2022/6/24.
//

#import <Foundation/Foundation.h>
@class DMCCMessage;
NS_ASSUME_NONNULL_BEGIN

@interface MHHelperUtils : NSObject

/**
 手机号码验证
 */
+ (BOOL)isValidateMobile:(NSString *)mobile;

/**
 检验邮箱格式
 */
+ (BOOL)isValidateUserEmail:(NSString *)str;

/**
 检验身份证格式
 */
+ (BOOL)isValidateUserIDCard:(NSString *)str;

/**
 检验身份证判断性别
 */
+ (NSInteger)genderOfIDNumber:(NSString *)IDNumber;
/**
 转账格式
 */
+ (BOOL)validateMoney:(NSString *)money;
/**
  号码中间替换xxxx
*/
+ (NSString *)phoneReplaceStrWithPhoneNum:(NSString *)phone;

/**
 获取加密字符串
 */
+ (NSString *)getBase64Str;

/**
 获取网络当前状态
 */
+ (void)getNetworkStatesWithCompleteBlock:(void(^)(NSString *status))completeBlock;


/**
 php接口统一参数配置
 */
+ (NSDictionary *)getInterfaceConfig;

/**
 获取文本高度
 */
+ (CGFloat)cellTextHeight:(NSString *)str font:(CGFloat)fontSice textWith:(CGFloat)width;
+ (CGFloat)cellTextWidth:(NSString *)str font:(CGFloat)fontSice textHeight:(CGFloat)height;

/**
 * 数子转16进制字符串
 */
+ (NSString *)stringWithHexNumber:(NSInteger)hexNumber;

/**
 * 用于富士二维码开门
 * 取当前时间戳
 */
+ (NSString *)getCurrentTimeHexStr;

/**
 * 用于富士二维码开门
 * 随机生成4字节 十六进制数据
 */
+ (NSString *)getRandomHexStr;

/**
 * 是否有访问相册权限
 */
+ (BOOL)havaALAuthorizationStatusPhoto;

/**
 * /是否有访问相机权限
*/

+ (BOOL)havaAVAuthorizationVideoStatusCode;

/**
 * 是否有访问麦克风权限
*/
+ (BOOL)havaAVAuthorizationStatusCode;

+ (UIImage *)thumbnailWithImage:(UIImage *)originalImage maxSize:(CGSize)size;

+ (CGSize)sizeForClientArea:(DMCCMessage *)msgModel withViewWidth:(CGFloat)width;
    
+ (CGSize)sizeForStickerClientArea:(DMCCMessage *)msgModel withViewWidth:(CGFloat)width;

+ (UIImage*)getFileThumbnailImage:(NSString*)videoPath;

// 是否加载引导页
+ (BOOL)isLoadingLaunghView;

// 是否清空聊天记录
+ (BOOL)isClearChatWithTime:(long long)time;

//获取设备唯一KEY
+ (NSString *)getServerKey;

@end

NS_ASSUME_NONNULL_END
