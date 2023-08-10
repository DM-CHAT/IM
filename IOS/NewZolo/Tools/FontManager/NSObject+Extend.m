//
//  NSObject+Address.m
//  网易彩票2014MJ版
//
//  Created by muxi on 14-9-23.
//  Copyright (c) 2014年 沐汐. All rights reserved.
//

#import "NSObject+Extend.h"
#import "FontManage.h"

@implementation NSObject (Extend)

/**
 *  按照屏幕宽度适配
 */
- (CGFloat(^)(NSInteger number))scaleWidth {
    return ^(NSInteger numer){
        return numer * KScreenWidth / KScreenHeight;
    };
}
/**
 *  按照屏幕高度适配
 */
- (CGFloat(^)(NSInteger number))scaleHeight {
    return ^(NSInteger numer){
        return numer * [FontManage currentHeightScal];
    };
}

@end
