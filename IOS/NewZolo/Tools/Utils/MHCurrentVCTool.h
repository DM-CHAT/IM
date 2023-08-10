//
//  MHCurrentVCTool.h
//  JTalking
//
//  Created by JTalking on 2022/6/24.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface MHCurrentVCTool : NSObject

+ (UIViewController*)getCurrentVC;
+ (UIViewController *)getPresentedVC;
+ (UINavigationController *)getCurrentNav;
+ (UIViewController*)currentViewController;

@end

NS_ASSUME_NONNULL_END
