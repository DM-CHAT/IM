//
//  MHHelperUtils.m
//  JTalking
//
//  Created by JTalking on 2022/6/24.
//

#import "MHHelperUtils.h"
#import <AssetsLibrary/AssetsLibrary.h>
#import <CoreLocation/CoreLocation.h>
#import <AVFoundation/AVFoundation.h>
#import "DateTools/DateTools.h"
#import "YYKeychain.h"

@implementation MHHelperUtils


/**
 手机号码验证
 */
+ (BOOL)isValidateMobile:(NSString *)mobile {
    NSString *phoneRegex = @"^1(3[0-9]|4[57]|5[0-35-9]|7[0135678]|8[0-9]|9[0-9])\\d{8}$";
    NSPredicate *phoneTest = [NSPredicate predicateWithFormat:@"SELF MATCHES %@",phoneRegex];
    return [phoneTest evaluateWithObject:mobile];
}

/**
 检验邮箱格式
 */
+ (BOOL)isValidateUserEmail:(NSString *)str {
    NSString *emailRegex = @"[A-Z0-9a-z\\._%+-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,4}+$";
    NSPredicate *emailTest = [NSPredicate predicateWithFormat:@"SELF MATCHES %@", emailRegex];
    return [emailTest evaluateWithObject:str];
}

/**
 检验身份证格式
 */
+ (BOOL)isValidateUserIDCard:(NSString *)str {
    NSString *cardRegex = @"^(^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$)|(^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])((\\d{4})|\\d{3}[Xx])$)$";
    NSPredicate *cardTest = [NSPredicate predicateWithFormat:@"SELF MATCHES %@", cardRegex];
    return [cardTest evaluateWithObject:str];
}

/**
 转账格式
 */
+ (BOOL)validateMoney:(NSString *)money {
    NSString *phoneRegex = @"^[0-9]+(.[0-9]{1,6})?$";
    NSPredicate *phoneTest = [NSPredicate predicateWithFormat:@"SELF MATCHES %@",phoneRegex];
    return [phoneTest evaluateWithObject:money];
}

/**
 检验身份证判断性别
 */
+ (NSInteger)genderOfIDNumber:(NSString *)IDNumber {
      //  记录校验结果：0未知，1男，2女
    NSInteger result = 0;
    NSString *fontNumer = nil;
    
    if (IDNumber.length == 15)
    { // 15位身份证号码：第15位代表性别，奇数为男，偶数为女。
        fontNumer = [IDNumber substringWithRange:NSMakeRange(14, 1)];
 
    }else if (IDNumber.length == 18)
    { // 18位身份证号码：第17位代表性别，奇数为男，偶数为女。
        fontNumer = [IDNumber substringWithRange:NSMakeRange(16, 1)];
    }else
    { //  不是15位也不是18位，则不是正常的身份证号码，直接返回
        return result;
    }
    
    NSInteger genderNumber = [fontNumer integerValue];
    
    if(genderNumber % 2 == 1)
        result = 1;
    
    else if (genderNumber % 2 == 0)
        result = 2;
    return result;
}

/**
  号码中间替换xxxx
*/
+ (NSString *)phoneReplaceStrWithPhoneNum:(NSString *)phone {
    NSString *withStr = @"****";
    NSInteger fromIndex = 3;
    NSRange range = NSMakeRange(fromIndex,  withStr.length);
    return [phone stringByReplacingCharactersInRange:range  withString:withStr];
}

/**
 获取网络当前状态
 */
+ (void)getNetworkStatesWithCompleteBlock:(void(^)(NSString *status))completeBlock {
    AFNetworkReachabilityManager *reachability = [AFNetworkReachabilityManager sharedManager];
    [reachability startMonitoring];
    [reachability setReachabilityStatusChangeBlock:^(AFNetworkReachabilityStatus status) {
        switch (status) {
            case AFNetworkReachabilityStatusUnknown:
                completeBlock(@"none");
                break;
            case AFNetworkReachabilityStatusNotReachable:
                completeBlock(@"none");
                break;
            case AFNetworkReachabilityStatusReachableViaWiFi:
                completeBlock(@"wifi");
                break;
            case AFNetworkReachabilityStatusReachableViaWWAN:
                completeBlock(@"4g");
                break;
            default:
                break;
        }
    }];
}


/**
获取文本高度
*/
+ (CGFloat)cellTextHeight:(NSString *)str font:(CGFloat)fontSice textWith:(CGFloat)width {
    NSMutableAttributedString *attributeString = [[NSMutableAttributedString alloc] initWithString:str];
    NSMutableParagraphStyle *style = [[NSMutableParagraphStyle alloc] init];
    style.lineSpacing = 1;
    UIFont *font = [UIFont systemFontOfSize:fontSice];
    [attributeString addAttribute:NSParagraphStyleAttributeName value:style range:NSMakeRange(0, str.length)];
    [attributeString addAttribute:NSFontAttributeName value:font range:NSMakeRange(0, str.length)];
    NSStringDrawingOptions options = NSStringDrawingUsesLineFragmentOrigin | NSStringDrawingUsesFontLeading;
    CGRect rect = [attributeString boundingRectWithSize:CGSizeMake(width, CGFLOAT_MAX) options:options context:nil];
    return rect.size.height;
}

+ (CGFloat)cellTextWidth:(NSString *)str font:(CGFloat)fontSice textHeight:(CGFloat)height {
    NSMutableAttributedString *attributeString = [[NSMutableAttributedString alloc] initWithString:str];
    NSMutableParagraphStyle *style = [[NSMutableParagraphStyle alloc] init];
    style.lineSpacing = 1;
    UIFont *font = [UIFont systemFontOfSize:fontSice];
    [attributeString addAttribute:NSParagraphStyleAttributeName value:style range:NSMakeRange(0, str.length)];
    [attributeString addAttribute:NSFontAttributeName value:font range:NSMakeRange(0, str.length)];
    NSStringDrawingOptions options = NSStringDrawingUsesLineFragmentOrigin | NSStringDrawingUsesFontLeading;
    CGRect rect = [attributeString boundingRectWithSize:CGSizeMake(CGFLOAT_MAX, height) options:options context:nil];
    return rect.size.width;
}

/**
 * 用于富士二维码开门
 * 取手机号后9位，转16进制数据
 */
+ (NSString *)stringWithHexNumber:(NSInteger)hexNumber {
    char hexChar[9];
    sprintf(hexChar, "%x", (int)hexNumber);
    NSString *hexString = [NSString stringWithCString:hexChar encoding:NSUTF8StringEncoding];
    return hexString;
}

/**
 * 是否有访问相册权限
*/
+ (BOOL)havaALAuthorizationStatusPhoto {
    ALAuthorizationStatus author = [ALAssetsLibrary authorizationStatus];
    if (author == kCLAuthorizationStatusRestricted || author ==kCLAuthorizationStatusDenied) {
        [MHAlert showMessage:LocalizedString(@"AlertPhotoNoFind")];
        return NO;
    } else if (author == AVAuthorizationStatusNotDetermined) {
        ALAssetsLibrary *assetsLibrary = [[ALAssetsLibrary alloc] init];
        [assetsLibrary enumerateGroupsWithTypes:ALAssetsGroupAll usingBlock:^(ALAssetsGroup *group, BOOL *stop) {
            if (*stop) {
                return;
            }
            *stop = TRUE;
        } failureBlock:^(NSError *error) {
        }];
        return NO;
    }
    return YES;
}

/**
 * /是否有访问相机权限
*/

+ (BOOL)havaAVAuthorizationVideoStatusCode {
    AVAuthorizationStatus authStatus = [AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeVideo];
    if (authStatus == AVAuthorizationStatusRestricted || authStatus ==AVAuthorizationStatusDenied)
    {
        [MHAlert showMessage:LocalizedString(@"NSCameraUsageDescription")];
        return NO;
    }else if (authStatus == AVAuthorizationStatusNotDetermined){
        [AVCaptureDevice requestAccessForMediaType:AVMediaTypeVideo completionHandler:^(BOOL granted) {//相机权限
            if (granted) {
                NSLog(@"Authorized");
            }else{
                NSLog(@"Denied or Restricted");
            }
        }];
        return YES;
    }
    return YES;
}

/**
 * 是否有访问麦克风权限
*/
+ (BOOL)havaAVAuthorizationStatusCode {
    AVAuthorizationStatus authStatus = [AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeAudio];
    if (authStatus == AVAuthorizationStatusRestricted || authStatus ==AVAuthorizationStatusDenied)
    {
        [MHAlert showMessage:LocalizedString(@"AlertMicroNoFind")];
        return NO;
    }else if (authStatus == AVAuthorizationStatusNotDetermined){
        [AVCaptureDevice requestAccessForMediaType:AVMediaTypeAudio completionHandler:^(BOOL granted) {//相机权限
            if (granted) {
                NSLog(@"Authorized");
            }else{
                NSLog(@"Denied or Restricted");
            }
        }];
        return YES;
    }
    return YES;
}

+ (UIImage *)thumbnailWithImage:(UIImage *)originalImage maxSize:(CGSize)size {
    CGSize originalsize = [originalImage size];
    //原图长宽均小于标准长宽的，不作处理返回原图
    if (originalsize.width<size.width && originalsize.height<size.height){
        return originalImage;
    }
    //原图长宽均大于标准长宽的，按比例缩小至最大适应值
    else if(originalsize.width>size.width && originalsize.height>size.height){
        CGFloat rate = 1.0;
        CGFloat widthRate = originalsize.width/size.width;
        CGFloat heightRate = originalsize.height/size.height;
        rate = widthRate>heightRate?heightRate:widthRate;
        CGImageRef imageRef = nil;
        if (heightRate>widthRate){
            imageRef = CGImageCreateWithImageInRect([originalImage CGImage], CGRectMake(0, originalsize.height/2-size.height*rate/2, originalsize.width, size.height*rate));//获取图片整体部分
        }else{
            imageRef = CGImageCreateWithImageInRect([originalImage CGImage], CGRectMake(originalsize.width/2-size.width*rate/2, 0, size.width*rate, originalsize.height));//获取图片整体部分
        }
        UIGraphicsBeginImageContext(size);//指定要绘画图片的大小
        CGContextRef con = UIGraphicsGetCurrentContext();
        CGContextTranslateCTM(con, 0.0, size.height);
        CGContextScaleCTM(con, 1.0, -1.0);
        CGContextDrawImage(con, CGRectMake(0, 0, size.width, size.height), imageRef);
        UIImage *standardImage = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        CGImageRelease(imageRef);
        return standardImage;
    }
    //原图长宽有一项大于标准长宽的，对大于标准的那一项进行裁剪，另一项保持不变
    else if(originalsize.height>size.height || originalsize.width>size.width){
        CGImageRef imageRef = nil;
        if(originalsize.height>size.height){
            imageRef = CGImageCreateWithImageInRect([originalImage CGImage], CGRectMake(0, originalsize.height/2-originalsize.width/2, originalsize.width, originalsize.width));//获取图片整体部分
        }
        else if (originalsize.width>size.width){
            imageRef = CGImageCreateWithImageInRect([originalImage CGImage], CGRectMake(originalsize.width/2-originalsize.height/2, 0, originalsize.height, originalsize.height));//获取图片整体部分
        }
        UIGraphicsBeginImageContext(size);//指定要绘画图片的大小
        CGContextRef con = UIGraphicsGetCurrentContext();
        CGContextTranslateCTM(con, 0.0, size.height);
        CGContextScaleCTM(con, 1.0, -1.0);
        CGContextDrawImage(con, CGRectMake(0, 0, size.width, size.height), imageRef);
        UIImage *standardImage = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        CGImageRelease(imageRef);
        return standardImage;
    }
    //原图为标准长宽的，不做处理
    else{
        return originalImage;
    }
}

+ (CGSize)sizeForClientArea:(DMCCMessage *)msgModel withViewWidth:(CGFloat)width {
    DMCCImageMessageContent *imgContent = (DMCCImageMessageContent *)msgModel.content;
    CGSize size = CGSizeMake(120, 120);
    if(imgContent.thumbnail) {
        size = imgContent.thumbnail.size;
    } else {
        size = [DMCCUtilities imageScaleSize:imgContent.size targetSize:CGSizeMake(120, 120) thumbnailPoint:nil];
    }
    
    
    if (size.height > width || size.width > width) {
        float scale = MIN(width/size.height, width/size.width);
        size = CGSizeMake(size.width * scale, size.height * scale);
    }
    return size;
}

+ (CGSize)sizeForStickerClientArea:(DMCCMessage *)msgModel withViewWidth:(CGFloat)width {
    DMCCStickerMessageContent *imgContent = (DMCCStickerMessageContent *)msgModel.content;
    CGSize size = [DMCCUtilities imageScaleSize:imgContent.size targetSize:CGSizeMake(120, 120) thumbnailPoint:nil];
    if (size.height > width || size.width > width) {
        float scale = MIN(width/size.height, width/size.width);
        size = CGSizeMake(size.width * scale, size.height * scale);
    }
    return size;
}

+ (UIImage*)getFileThumbnailImage:(NSString*)videoPath {

    AVURLAsset *asset = [[AVURLAsset alloc] initWithURL:[NSURL fileURLWithPath:videoPath] options:nil];
    NSParameterAssert(asset);//断言
    AVAssetImageGenerator *assetImageGenerator = [[AVAssetImageGenerator alloc] initWithAsset:asset];
    assetImageGenerator.appliesPreferredTrackTransform = YES;
    assetImageGenerator.apertureMode = AVAssetImageGeneratorApertureModeEncodedPixels;
    NSTimeInterval time = 0.1;
    CGImageRef thumbnailImageRef =NULL;
    CFTimeInterval thumbnailImageTime = time;
    NSError*error =nil;
    thumbnailImageRef = [assetImageGenerator copyCGImageAtTime:CMTimeMake(thumbnailImageTime,60) actualTime:NULL error:&error];
    if( error ) {
        NSLog(@"%@", error );
    }
    if( thumbnailImageRef ) {
        return[[UIImage alloc]initWithCGImage:thumbnailImageRef];
    }
    return nil;
}

// 是否加载引导页
+ (BOOL)isLoadingLaunghView {
    NSString *key = @"CFBundleShortVersionString";
    NSString *lastVersion = [[NSUserDefaults standardUserDefaults] stringForKey:key];
    NSString *currentVersion = [NSBundle mainBundle].infoDictionary[key];
    if ([currentVersion isEqualToString:lastVersion]) {
        return NO;
    } else {
        [[NSUserDefaults standardUserDefaults] setObject:currentVersion forKey:key];
        [[NSUserDefaults standardUserDefaults] synchronize];
        return YES;
    }
}

// 是否清空聊天记录
+ (BOOL)isClearChatWithTime:(long long)time {
    if (time == 0) {
        return NO;
    }
    NSDate *startDate = [NSDate date];
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    formatter.dateFormat = @"yyyy-MM-dd HH:mm:ss";
    NSDate *endDate = [formatter dateFromString:[MHDateTools timeStampToString:time]];
    DTTimePeriod *timePeriod =[[DTTimePeriod alloc] initWithStartDate:startDate endDate:endDate];
    double  durationInDays    = [timePeriod durationInDays]; //相差日
    if (durationInDays > 1.0) {
        return YES;
    } else {
        return NO;
    }
}

//获取设备唯一KEY
+ (NSString *)getServerKey {
    NSString *strKey = @"";
    if (strKey.length == 0) {
        NSUserDefaults * defaults = [NSUserDefaults standardUserDefaults];
        strKey = [defaults objectForKey:KeychainSevers];
        if (!strKey) {
            strKey = [YYKeychain getPasswordForService:KeychainSevers account:KeychainAccount];
            if(!strKey){
                //钥匙串
                strKey = [OsnUtils createUUID];
                [YYKeychain setPassword:strKey forService:KeychainSevers account:KeychainAccount];
            }
            if (strKey) {
                NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
                [defaults setValue:strKey forKey:KeychainSevers];
                [defaults synchronize];
            }
        }
    }
    if (strKey.length == 0) {
        strKey = [NSString stringWithFormat:@"123321%d",arc4random()%100];
    }
    return strKey ;
}

@end
