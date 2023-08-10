//
//  AppDelegate.m
//  NewZolo
//
//  Created by 陈泽萱 on 2022/6/3.
//

#import "AppDelegate.h"
#import "MHLoginModel.h"
#import "MHTabBarVC.h"
#import <osnsdk/osnsdk.h>
#import <config/language.h>
#import <IQKeyboardManager.h>
#import <osnsdk/ZoloOssManager.h>
#import "JPUSHService.h"
#import <UserNotifications/UserNotifications.h>
#import "EAIntroView.h"
#import "ZoloServiceVC.h"
#import <StoreKit/StoreKit.h>
#import <Bugly/Bugly.h>

//NSString *IM_SERVER_HOST = @"47.92.123.66";
NSString *IM_SERVER_HOST = @"8.219.11.57";
NSDate  *g_LastKeepAliveDate = nil;
NSLock  *g_lock = nil;

/**
 * 极光推送设置
 */
static BOOL isProduction = true;   // YES 生产环境   NO 开发环境


@interface AppDelegate () <JPUSHRegisterDelegate>

@property (nonatomic,assign)NSInteger numberCount;

@end

@implementation AppDelegate


- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
        
    self.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    self.window.backgroundColor = [UIColor whiteColor];
    IQKeyboardManager *manager = [IQKeyboardManager sharedManager];
    manager.enableAutoToolbar = NO;
    
    [[ZoloAPIManager instanceManager] getLoginConfigWithCompleteBlock:^(BOOL isSuccess) {
        
    }];

    /**
    极光推送注册
    */

    [JPUSHService registerForRemoteNotificationTypes:(UIUserNotificationTypeBadge | UIUserNotificationTypeSound | UIUserNotificationTypeAlert)categories:nil];
    
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login.json.JpushAppKey.length > 0) {
        [JPUSHService setupWithOption:launchOptions appKey:login.json.JpushAppKey channel:JpushChannel apsForProduction:isProduction];
    }
    
    if (login.json.buglyKey.length > 0) {
        [Bugly startWithAppId:login.json.buglyKey];
    }
    
    [[ZoloOssManager instanceManager] setupClientWithOSS_ACCESSKEY_ID:login.json.AccessKeyId withOSS_SECRETKEY_ID:login.json.AccessKeySecret withOSS_ENDPOINT:login.json.ENDPOINT withOSS_BUCKET_IMAGE_Str:login.json.BUCKETNAME withOSS_NAME_USERPOR_Str:login.json.UserPortraitDirectory withOSS_NAME_GROUPPOR_Str:login.json.GroupPortraitDirectory withOSS_NAME_OTHER_Str:login.json.TempDirectory];
    
    NSString* hostIp = [[NSUserDefaults standardUserDefaults] objectForKey:@"ospn_hostip"];
    if(hostIp == nil){
        hostIp = IM_SERVER_HOST;
        [[NSUserDefaults standardUserDefaults] setValue:hostIp forKey:@"ospn_hostip"];
    }
    
    [[DMCCNetworkService sharedInstance] setServerAddress:hostIp];
    [[DMCCNetworkService sharedInstance] connect:nil token:nil password:nil];
    
    NSString *savedToken = [[NSUserDefaults standardUserDefaults] stringForKey:@"ospn_token"];
    NSString *savedUserId = [[NSUserDefaults standardUserDefaults] stringForKey:@"ospn_id"];

    if (savedToken.length > 0 && savedUserId.length > 0) {
        [DMCCNetworkService sharedInstance].userId = savedUserId;
        [DMCCNetworkService sharedInstance].passwd = savedToken;
        self.window.rootViewController = [[MHTabBarVC alloc]init];
    } else {
        
        MHNavigationVC *vc = [[MHNavigationVC alloc] initWithRootViewController:[ZoloServiceVC new]];
        self.window.rootViewController = vc;
    }
    
    [self.window makeKeyAndVisible];
    if (@available(iOS 13.0, *)) {
        [UIApplication sharedApplication].statusBarStyle = UIStatusBarStyleDefault;
        NSString *themeStr = [[NSUserDefaults standardUserDefaults] stringForKey:@"theme"];
        if ([themeStr isEqualToString:@"light"]) {
            self.window.overrideUserInterfaceStyle = UIUserInterfaceStyleLight;
        } else if ([themeStr isEqualToString:@"dark"]) {
            self.window.overrideUserInterfaceStyle = UIUserInterfaceStyleDark;
        } else {
            self.window.overrideUserInterfaceStyle = UIUserInterfaceStyleUnspecified;
        }
    }
    
    if ([MHHelperUtils isLoadingLaunghView]) {
        [self showIntroWithCrossDissolve];
    }
    
    if (Sys_Version >= 10.3) {
         [self appLaunchNumber];
    }
    
    NSString* userID = [[DMCCIMService sharedDMCIMService] getUserID];
    if (userID != nil) {
        [self settingPushWithOsnId:userID];
    }
    
    return YES;
}

- (void)settingPushWithOsnId:(NSString *)osnId {
    [[ZoloAPIManager instanceManager] pushWithName:[JPUSHService registrationID] osnID:osnId language:[OSNLanguage getBaseLanguage] WithCompleteBlock:^(BOOL isSuccess) {
        
    }];
}

-(FloatingWindow *)floatWindow {
    if (!_floatWindow) {
        _floatWindow = [[FloatingWindow alloc] initWithFrame:CGRectMake(100, 100, 76, 76) imageName:@"av_call"];
        [_floatWindow makeKeyAndVisible];
        _floatWindow.hidden = YES;
    }
    return _floatWindow;
}

# pragma mark - 引导页加载
- (void)showIntroWithCrossDissolve {
    EAIntroPage *page1 = [EAIntroPage page];
    page1.bgImage = [UIImage imageNamed:@"bg1"];
    EAIntroPage *page2 = [EAIntroPage page];
    page2.bgImage = [UIImage imageNamed:@"bg2"];
    EAIntroPage *page3 = [EAIntroPage page];
    page3.bgImage = [UIImage imageNamed:@"bg3"];
    EAIntroView *intro = [[EAIntroView alloc] initWithFrame:[UIApplication sharedApplication].keyWindow.bounds andPages:@[page1,page2,page3]];
    intro.skipButtonAlignment = EAViewAlignmentRight;
    [intro.skipButton setTitle:LocalizedString(@"BGImgNext") forState:UIControlStateNormal];
    intro.pageControl.pageIndicatorTintColor = [UIColor clearColor];
    intro.pageControl.currentPageIndicatorTintColor = [UIColor clearColor];
    intro.bgViewContentMode = UIViewContentModeScaleToFill;
    intro.skipButtonY = 80.f;
    intro.pageControlY = 42.f;
    [intro showInView:[UIApplication sharedApplication].keyWindow animateDuration:0.3];
}

- (void)setRootView {
    NSString *savedToken = [[NSUserDefaults standardUserDefaults] stringForKey:@"ospn_token"];
    NSString *savedUserId = [[NSUserDefaults standardUserDefaults] stringForKey:@"ospn_id"];
    if (savedToken.length > 0 && savedUserId.length > 0) {
        [DMCCNetworkService sharedInstance].userId = savedUserId;
        [DMCCNetworkService sharedInstance].passwd = savedToken;
        self.window.rootViewController = [[MHTabBarVC alloc]init];
    } else {
        MHNavigationVC *vc = [[MHNavigationVC alloc] initWithRootViewController:[ZoloServiceVC new]];
        self.window.rootViewController = vc;
    }
}

/**app评分*/
- (void)appLaunchNumber {
    _numberCount = [[NSUserDefaults standardUserDefaults] integerForKey:NumberCount];
    if (_numberCount == NumberCountOne || _numberCount == NumberCountTwo || _numberCount == NumberCountThree) {
        if (@available(iOS 10.3, *)) {
            [SKStoreReviewController requestReview];
        }
    }
    _numberCount ++;
    [[NSUserDefaults standardUserDefaults] setInteger:_numberCount forKey:NumberCount];
    [[NSUserDefaults standardUserDefaults] synchronize];
}

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
    //sdk注册DeviceToken
    [JPUSHService registerDeviceToken:deviceToken];
}

- (void)applicationWillResignActive:(UIApplication *)application {
    MHLog(@"===applicationWillResignActive=====失去活动状态====");
    //极光推送清除所有本地通知
    [JPUSHService clearAllLocalNotifications];
    [self backgroundHandler];
}

- (void)applicationDidBecomeActive:(UIApplication *)application {
    MHLog(@"===applicationDidBecomeActive=====进入活动状态====");
    /** 设置角标 */
    [JPUSHService setBadge:0];
    if (startedInBackground) {
        startedInBackground = FALSE;
    }
    /** 清空角标 */
    [[UIApplication sharedApplication] setApplicationIconBadgeNumber:0];
}

- (void)applicationWillTerminate:(UIApplication *)application {
    MHLog(@"===applicationWillTerminate=====程序要死了====");
}

- (void)applicationWillEnterForeground:(UIApplication *)application {
    MHLog(@"===applicationWillEnterForeground=====返回前台====");
}

//当程序被推送到后台的时候
- (void)applicationDidEnterBackground:(UIApplication *)application {
    startedInBackground=true;
    [self backgroundHandler];
}

- (void)backgroundHandler {
    UIApplication* app = [UIApplication sharedApplication];
    bgTask = [app beginBackgroundTaskWithExpirationHandler:^{
        [app endBackgroundTask:self->bgTask];
        self->bgTask =UIBackgroundTaskInvalid;
    }];
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT,0), ^{
        while (0 < 200000){
            sleep(1);
            if (g_LastKeepAliveDate != Nil) {
                NSDate *current = [NSDate date];
                if ([current timeIntervalSinceDate:g_LastKeepAliveDate] > 700) {
                    [g_lock lock];
                    g_LastKeepAliveDate=[NSDate date];
                    [g_lock unlock];
                }
            }
        }
    });
}

/**
 iOS 9.0 之后
 三方唤起本程序后执行的方法
 return YES 表示允许唤起本程序  程序运行过程中调用
 */
- (BOOL)application:(UIApplication *)app openURL:(NSURL *)url options:(NSDictionary<UIApplicationOpenURLOptionsKey, id> *)options{
    return YES;
}


@end
