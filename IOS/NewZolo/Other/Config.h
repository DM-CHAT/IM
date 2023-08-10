//
//  Config.h
//  NewZolo
//
//  Created by JTalking on 2022/6/29.
//

#ifndef Config_h
#define Config_h

/** 服務器配  */

//#define LOCAL_DEBUG 1   //是否是本地調試 模式
#ifdef DEBUG
    #ifdef LOCAL_DEBUG
        #define      MHRequestURL            @"http://8.219.9.10:11020/"
        #define      MHLoginURL              @"https://luckmoney8888.com/#/login"
    #else
        #define      MHRequestURL            @"http://8.219.9.10:11020/"
        #define      MHLoginURL              @"https://luckmoney8888.com/#/login"
    #endif
#else
    #ifdef LOCAL_DEBUG
        #define      MHRequestURL            @"http://8.219.9.10:11020/"
        #define      MHLoginURL              @"https://luckmoney8888.com/#/login"
    #else
        #define      MHRequestURL            @"http://8.219.9.10:11020/"
        #define      MHLoginURL              @"https://luckmoney8888.com/#/login"
    #endif
#endif

/** 打印输出 */
#ifdef DEBUG
    #define MHLog( s, ... ) NSLog( @"< %@:(%d) > %@", [[NSString stringWithUTF8String:__FILE__] lastPathComponent], __LINE__, [NSString stringWithFormat:(s), ##__VA_ARGS__] )
#else
    #define MHLog( s, ... ) NSLog(@"")
#endif

/** 循环引用 */
#define WS(weakSelf)  __weak __typeof(&*self)weakSelf = self;

/** 屏幕适配 */
#define IS_IPHONE (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone)
#define KScreenWidth                [[UIScreen mainScreen]bounds].size.width
#define KScreenHeight               [[UIScreen mainScreen]bounds].size.height
#define iphone5x_4_0         (KScreenHeight==568.0f)
#define iphone6_4_7          (KScreenHeight==667.0f)
#define iphone6Plus_5_5      (KScreenHeight==736.0f || KScreenWidth==414.0f)
#define iPhoneX              [[UIScreen mainScreen] bounds].size.width >= 375.0f && [[UIScreen mainScreen] bounds].size.height >= 812.0f && IS_IPHONE
#define iPhoneX_bottomH      (iPhoneX ? 34 : 0)
#define iPhoneX_topH         (iPhoneX ? 23 : 0)

#define Sys_Version [[UIDevice currentDevice].systemVersion doubleValue]

/** RGB颜色 */
#define MHColor(r, g, b) [UIColor colorWithRed:(r)/255.0 green:(g)/255.0 blue:(b)/255.0 alpha:1]
#define MHColorFromHex(s) [UIColor colorWithRed:(((s & 0xFF0000) >> 16))/255.0 green:(((s & 0xFF00) >>8))/255.0 blue:((s & 0xFF))/255.0 alpha:1.0]

/** 判断是否为空字符串 */
#define IsEmptyStr(str)   (str == nil || [[str stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]] isEqualToString:@""])

/** 系统评分 */
#define NumberCount @"numberCount"
#define NumberCountOne 5
#define NumberCountTwo 50
#define NumberCountThree 100

#define SDImageDefault [UIImage imageNamed:@"herdImg"]

/** 多语言 */
#define MHLALI(__KEY__) NSLocalizedString(__KEY__, nil)

/** aes加密key */
#define AESKEY @"hJ1LiHfjCdiILldI"

/**加签*/
#define PHPTokenKEY @"acde213413dea411511d3b1866f06364"

/** 本地数据库设置 */
#define MHDBPath [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) lastObject]
#define MHDBName @"ubicell.sqlite"

#define MHZipPath [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) lastObject]

/** 微信通用链接 */
#define WXUniversalLink @"https://www.ubicell.cn/ishang/"
#define WXAppkey @"wxc765dac4dca89131"
#define WXAppSecret @"41f5a01894b04f1f260d02ab2955f13f"

/** QQ */
#define QQAppKey @"1105849243"
#define QQAppSecret @"5AghEcLeh0tVfJJr"

/** 萤石监控 */
#define EzvizAppKey @"9c5522208f8f4305a2bcbd811380983f"

/** bugly使用key */
#define BuglyAppKey @"4d92c4f6a5"

/** 友盟使用key */
#define UMAppKey @"586b0d8f82b6352def000804"

/** 极光推送使用key */
#define JpushChannel @"Publish channel"  //固定写法

/** 屏幕宽度 */
#define kScreenwidth ([UIScreen mainScreen].bounds.size.width)

/** 屏幕高度 */
#define KScreenheight ([UIScreen mainScreen].bounds.size.height)

/** 颜色宏定义*/
#define XHRGBColor(r,g,b) [UIColor colorWithRed:(r)/255.0 green:(g)/255.0 blue:(b)/255.0 alpha:1.0]

/** 屏幕亮度 */
#define screenLight 0.3

/** 上滑显示tabbar */
#define NAVBAR_COLORCHANGE_POINT (IMAGE_HEIGHT - NAV_HEIGHT*2)
#define IMAGE_HEIGHT 180 + 40
#define NAV_HEIGHT 64

/** 一定要先配置自己项目在商店的APPID,配置完最好在真机上运行才能看到完全效果哦! 对应泛在家园APP的ID */
#define STOREAPPID @"1159208421"

//KeyChain  Severs Account
#define KeychainSevers [NSString stringWithFormat:@"http://com.jtalking.dm_Severs"]
#define KeychainAccount [NSString stringWithFormat:@"http://com.jtalking.dm_Account"]

#endif /* Config_h */
