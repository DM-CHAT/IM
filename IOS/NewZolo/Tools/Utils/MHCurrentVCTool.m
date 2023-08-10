//
//  MHCurrentVCTool.m
//  JTalking
//
//  Created by JTalking on 2022/6/24.
//

#import "MHCurrentVCTool.h"

@implementation MHCurrentVCTool

#pragma mark - 获取当前的控制器
+ (UIViewController*)getCurrentVC
{
    UIViewController *result = nil;
    
    UIWindow * window = [[UIApplication sharedApplication] keyWindow];
    if (window.windowLevel != UIWindowLevelNormal)
    {
        NSArray *windows = [[UIApplication sharedApplication] windows];
        for(UIWindow * tmpWin in windows)
        {
            if (tmpWin.windowLevel == UIWindowLevelNormal)
            {
                window = tmpWin;
                break;
            }
        }
    }
    
    UIView *frontView = [[window subviews] objectAtIndex:0];
    id nextResponder = [frontView nextResponder];
    
    if ([nextResponder isKindOfClass:[UIViewController class]])
        result = nextResponder;
    else
        result = window.rootViewController;
    
    return result;
}

#pragma mark - 获取当前的present出来的控制器
+ (UIViewController *)getPresentedVC {
    UIViewController *topVC = [UIApplication sharedApplication].keyWindow.rootViewController;
    if ((topVC.presentedViewController) != nil) {
        topVC = topVC.presentedViewController;
    }
    return topVC;
}

+ (UINavigationController *)getCurrentNav {
    UITabBarController *tabBarVc = (UITabBarController *)[UIApplication sharedApplication].keyWindow.rootViewController;
    UINavigationController *nav = [tabBarVc selectedViewController];
    return nav;
}

//获取Window当前显示的ViewController
+ (UIViewController*)currentViewController {
    //获得当前活动窗口的根视图
    UIViewController* vc = [UIApplication sharedApplication].keyWindow.rootViewController;
    while (1)
    {
        //根据不同的页面切换方式，逐步取得最上层的viewController
        if ([vc isKindOfClass:[UITabBarController class]]) {
            vc = ((UITabBarController*)vc).selectedViewController;
        }
        if ([vc isKindOfClass:[UINavigationController class]]) {
            vc = ((UINavigationController*)vc).visibleViewController;
        }
        if (vc.presentedViewController) {
            vc = vc.presentedViewController;
        }else{
            break;
        }
    }
    return vc;
}

@end
