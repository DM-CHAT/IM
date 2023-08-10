//
//  FontManage.h
//  IMChatNew
//
//  Created by mac-a on 2016/12/26.
//  Copyright © 2016年 mac-a. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

typedef enum : NSUInteger {
    FONT_MIN = 1,//最小號字體
    FONT_STANDARD = 2,//标准字体
    FONT_MAX = 3,//大号字體
    FONT_VERY_MAX = 4, //大號字體
} FONT_SIZE;

@interface FontManage : NSObject

+ (CGFloat)currentHeightScal;
//獲取適配完成後的字體
+ (UIFont *)systemFontSizeNumber:(CGFloat)number;
//默認的字體
+ (UIFont *)systemFontSize;
//加粗完成後的字體
+ (UIFont *)boldSystemFontOfSize:(CGFloat)number;
//當前字體的大小
+ (CGFloat)currentFontNumber;
//當前字體的標註
+ (FONT_SIZE)getCurrentFontSize;
//設置當前字體的標識
+ (void)setCurrentFontSize:(FONT_SIZE)font;
//根據傳過來的值返回適配完成後的大小
+ (CGFloat)currentSizeForNumber:(CGFloat)number;
//获取当前字体改变后的屏幕上宽度
+ (CGFloat)currentFontWithScale:(CGFloat)number;
//當前放大比例
+ (CGFloat)currentFontScal;

+ (CGFloat)fontManagerAutoFontnum:(CGFloat)num;
//10
+ (UIFont *)MHFont10;
//12
+ (UIFont *)MHFont12;
//13
+ (UIFont *)MHFont13;
//14
+ (UIFont *)MHFont14;
//15
+ (UIFont *)MHFont15;
//16
+ (UIFont *)MHFont16;
//19
+ (UIFont *)MHFont19;
//20
+ (UIFont *)MHFont20;
//21
+ (UIFont *)MHFont21;
//24
+ (UIFont *)MHFont24;
//36
+ (UIFont *)MHFont36;

+ (UIColor *)MHWhiteColor;
+ (UIColor *)MHGrayColor;
+ (UIColor *)MHBlockColor;
+ (UIColor *)MHButtonColor;
+ (UIColor *)MHTextColor;
+ (UIColor *)MHInputBgColor;
+ (UIColor *)MHViewBgColor;
+ (UIColor *)MHMainColor;
+ (UIColor *)MHKeyColor;
+ (UIColor *)MHKeyBGColor;
+ (UIColor *)MHHomeNColor;
+ (UIColor *)MHHomeSColor;
+ (UIColor *)MHMsgTimeColor;
+ (UIColor *)MHMsgRecColor;
+ (UIColor *)MHLineSeparatorColor;
+ (UIColor *)MHTitleSubColor;
+ (UIColor *)MHAlertColor;
+ (UIColor *)MHTopMsgColor;
+ (UIColor *)MHTextViewTintColor;

@end
