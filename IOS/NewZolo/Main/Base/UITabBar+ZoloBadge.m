//
//  UITabBar+ZoloBadge.m
//  NewZolo
//
//  Created by JTalking on 2022/8/3.
//

#import "UITabBar+ZoloBadge.h"

@implementation UITabBar (ZoloBadge)

//显示红点
- (void)showBadgeOnItemIndex:(int)index{
//    [self removeBadgeOnItemIndex:index];
//    //新建小红点
//    UIView *bview = [[UIView alloc]init];
//    bview.tag = 888 + index;
//    bview.layer.cornerRadius = 5;
//    bview.clipsToBounds = YES;
//    bview.backgroundColor = [UIColor redColor];
//    CGRect tabFram = self.frame;
//    
//    float value = 0.5;
//    
//    if (index == 0) {
//        value = 0.56;
//    } else if (index == 1) {
//        value = 0.55;
//    } else {
//        value = 0.52;
//    }
//    
//    int TabbarItemNums = 3.0;
//    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
//    if (login.json.MainDapp.length > 0) {
//        TabbarItemNums = 4.0;
//    }
//    float percentX = (index + value) / TabbarItemNums;
//    CGFloat x = ceilf(percentX*tabFram.size.width);
//    CGFloat y = ceilf(0.1*tabFram.size.height);
//    bview.frame = CGRectMake(x, y, 10, 10);
//    [self addSubview:bview];
//    [self bringSubviewToFront:bview];
}

//隐藏红点
-(void)hideBadgeOnItemIndex:(int)index {
//    [self removeBadgeOnItemIndex:index];
}

//移除控件
- (void)removeBadgeOnItemIndex:(int)index {
//    for (UIView*subView in self.subviews) {
//        if (subView.tag == 888+index) {
//            [subView removeFromSuperview];
//        }
//    }
}

@end
