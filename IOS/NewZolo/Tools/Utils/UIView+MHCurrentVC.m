//
//  UIView+MHCurrentVC.m
//  JTalking
//
//  Created by JTalking on 2022/6/24.
//

#import "UIView+MHCurrentVC.h"

@implementation UIView (MHCurrentVC)

#pragma mark - 获取当前的控制器
-(UIViewController *)getCurrentViewController{
    UIResponder *next = [self nextResponder];
    do {
        if ([next isKindOfClass:[UIViewController class]]) {
            return (UIViewController *)next;
        }
        next = [next nextResponder];
    } while (next != nil);
    
    UIViewController *topVC = [UIApplication sharedApplication].keyWindow.rootViewController;
    if ((topVC.presentedViewController) != nil) {
        topVC = topVC.presentedViewController;
    }
    return topVC;
}

#pragma mark - 获取当前的present出来的控制器
- (UIViewController *)getPresentedVC {
    UIViewController *topVC = [UIApplication sharedApplication].keyWindow.rootViewController;
    if ((topVC.presentedViewController) != nil) {
        topVC = topVC.presentedViewController;
    }
    return topVC;
}

@end
