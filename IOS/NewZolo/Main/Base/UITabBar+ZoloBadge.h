//
//  UITabBar+ZoloBadge.h
//  NewZolo
//
//  Created by JTalking on 2022/8/3.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface UITabBar (ZoloBadge)

- (void)showBadgeOnItemIndex:(int)index;
- (void)hideBadgeOnItemIndex:(int)index;

@end

NS_ASSUME_NONNULL_END
