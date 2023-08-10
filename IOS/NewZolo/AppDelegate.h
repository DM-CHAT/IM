//
//  AppDelegate.h
//  NewZolo
//
//  Created by 陈泽萱 on 2022/6/3.
//

#import <UIKit/UIKit.h>
#import "ZoloServiceModel.h"
#import "FloatingWindow.h"
@interface AppDelegate : UIResponder <UIApplicationDelegate> {
    UIBackgroundTaskIdentifier bgTask;
    BOOL startedInBackground;
}

@property (strong, nonatomic) UIWindow *window;
@property(strong, nonatomic)FloatingWindow *floatWindow;
- (void)setRootView;

@end

