//
//  NSObject+Address.h
//  网易彩票2014MJ版
//
//  Created by muxi on 14-9-23.
//  Copyright (c) 2014年 沐汐. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NSObject (Extend)

/**
 *  按照屏幕宽度适配  根据不同屏幕适配
 */
- (CGFloat(^)(NSInteger number))scaleWidth;
/**
 *  按照屏幕高度适配  根据不同字体大小适配
 */
- (CGFloat(^)(NSInteger number))scaleHeight;


@end
