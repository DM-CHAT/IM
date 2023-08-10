//
//  FontManage.m
//  IMChatNew
//
//  Created by mac-a on 2016/12/26.
//  Copyright © 2016年 mac-a. All rights reserved.
//

#import "FontManage.h"
#import "Config.h"
#import "CoreArchive.h"

#define FontSize       @"FontSize"

@implementation FontManage

+ (NSInteger)currentFontAdd {
    if (iphone6_4_7 || iphone6Plus_5_5) {
        return 2;
    }else if(iphone5x_4_0){
        return 1;
    }else {
        return 0;
    }
}

+ (CGFloat)currentHeightScal {
    return (([self getCurrentFontSize] - FONT_MIN) * 0.05 + 1);
}

+ (CGFloat)currentFontScal {
    return (([self getCurrentFontSize] - FONT_MIN) * 0.1 + 1);
}

+ (UIFont *)systemFontSizeNumber:(CGFloat)number {
    if (iphone5x_4_0) {
        number = (number - 2) * [self currentFontScal];
    } else {
        number = number * [self currentFontScal];
    }
    UIFont *fonts = [UIFont fontWithName:@"PingFangSC-Regular" size:number];
    if (!fonts) {
        fonts = [UIFont systemFontOfSize:number];
    }
    return fonts;
}

+ (UIFont *)systemFontSize {
    CGFloat fontSize = [self currentFontNumber];
    UIFont *fonts = [UIFont fontWithName:@"PingFangSC-Regular" size:fontSize];
    if (!fonts) {
        fonts = [UIFont systemFontOfSize:fontSize];
    }
    return fonts;
    
}

+ (CGFloat)currentFontNumber {
    FONT_SIZE font = [CoreArchive intForKey:FontSize];
    if (!font) {
        font = FONT_STANDARD;
        [CoreArchive setInt:font key:FontSize];
    }
    CGFloat fontSize = 0;
    switch (font) {
        case FONT_MIN:
            fontSize = 13.5;
            break;
        case FONT_STANDARD:
            fontSize = 15.0;
            break;
        case FONT_MAX:
            fontSize = 17.0;
            break;
        case FONT_VERY_MAX:
            fontSize = 19.0;
            break;
        default:
            fontSize = 15.0;
            break;
    }
    font += [self currentFontAdd];
    return fontSize;
}

+ (FONT_SIZE)getCurrentFontSize {
    FONT_SIZE font = [CoreArchive intForKey:FontSize];
    if (!font) {
        [CoreArchive setInt:FONT_STANDARD key:FontSize];
    }
    font = [CoreArchive intForKey:FontSize];
    return font;
}

+ (void)setCurrentFontSize:(FONT_SIZE)font {
    [CoreArchive setInt:font key:FontSize];
}

+ (UIFont *)boldSystemFontOfSize:(CGFloat)number {
    number = number * [self currentFontScal];
    UIFont *fonts = [UIFont fontWithName:@"PingFangSC-Regular" size:number];
    if (!fonts) {
        fonts = [UIFont boldSystemFontOfSize:number];
    }
    return fonts;
}

+ (CGFloat)currentSizeForNumber:(CGFloat)number {
    NSInteger count = [self getCurrentFontSize] - FONT_MIN;
    number += (count * 2);
    number += [self currentFontAdd];
    return number;
}

+ (CGFloat)currentFontWithScale:(CGFloat)number {
    return number;
}

+ (CGFloat)fontManagerAutoFontnum:(CGFloat)num {
    return num * [self currentFontScal];
}

//10
+ (UIFont *)MHFont10 {
    return [self systemFontSizeNumber:10];
}
//12
+ (UIFont *)MHFont12 {
    return [self systemFontSizeNumber:12];
}
//13
+ (UIFont *)MHFont13 {
    return [self systemFontSizeNumber:13];
}
//14
+ (UIFont *)MHFont14 {
    return [self systemFontSizeNumber:14];
}
//15
+ (UIFont *)MHFont15 {
    return [self systemFontSizeNumber:15];
}
//16
+ (UIFont *)MHFont16 {
    return [self systemFontSizeNumber:16];
}
//19
+ (UIFont *)MHFont19 {
    return [self systemFontSizeNumber:19];
}
//20
+ (UIFont *)MHFont20 {
    return [self systemFontSizeNumber:20];
}
//21
+ (UIFont *)MHFont21 {
    return [self systemFontSizeNumber:21];
}
//24
+ (UIFont *)MHFont24 {
    return [self systemFontSizeNumber:24];
}
//36
+ (UIFont *)MHFont36 {
    return [self systemFontSizeNumber:36];
}

+ (UIColor *)MHWhiteColor {
    if (@available(iOS 13.0, *)) {
        UIColor *dyColor = [UIColor colorWithDynamicProvider:^UIColor * _Nonnull(UITraitCollection * _Nonnull traitCollection) {
            if (traitCollection.userInterfaceStyle == UIUserInterfaceStyleLight) {
                return MHColor(255, 255, 255);
            }else {
                return MHColorFromHex(0x181A20);
            }
        }];
        return dyColor;
    }else{
        return MHColor(255, 255, 255);
    }
}

+ (UIColor *)MHGrayColor {
    if (@available(iOS 13.0, *)) {
        UIColor *dyColor = [UIColor colorWithDynamicProvider:^UIColor * _Nonnull(UITraitCollection * _Nonnull traitCollection) {
            if (traitCollection.userInterfaceStyle == UIUserInterfaceStyleLight) {
                return MHColorFromHex(0xf9f9f9);
            }else {
                return MHColorFromHex(0x181A20);
            }
        }];
        return dyColor;
    }else{
        return MHColorFromHex(0xf9f9f9);
    }
}

+ (UIColor *)MHInputBgColor {
    if (@available(iOS 13.0, *)) {
        UIColor *dyColor = [UIColor colorWithDynamicProvider:^UIColor * _Nonnull(UITraitCollection * _Nonnull traitCollection) {
            if (traitCollection.userInterfaceStyle == UIUserInterfaceStyleLight) {
                return MHColorFromHex(0xf9f9f9);
            }else {
                return MHColorFromHex(0x585858);
            }
        }];
        return dyColor;
    }else{
        return MHColorFromHex(0xf9f9f9);
    }
}

+ (UIColor *)MHBlockColor {
    if (@available(iOS 13.0, *)) {
        UIColor *dyColor = [UIColor colorWithDynamicProvider:^UIColor * _Nonnull(UITraitCollection * _Nonnull traitCollection) {
            if (traitCollection.userInterfaceStyle == UIUserInterfaceStyleLight) {
                return MHColor(0, 0, 0);
            }else {
                return MHColor(255, 255, 255);
            }
        }];
        return dyColor;
    }else{
        return MHColor(0, 0, 0);
    }
}

+ (UIColor *)MHButtonColor {
    if (@available(iOS 13.0, *)) {
        UIColor *dyColor = [UIColor colorWithDynamicProvider:^UIColor * _Nonnull(UITraitCollection * _Nonnull traitCollection) {
            if (traitCollection.userInterfaceStyle == UIUserInterfaceStyleLight) {
                return MHColorFromHex(0x00c3b2);
            }else {
                return MHColor(255, 255, 255);
            }
        }];
        return dyColor;
    }else{
        return MHColorFromHex(0x00c3b2);
    }
}

+ (UIColor *)MHTextColor {
    if (@available(iOS 13.0, *)) {
        UIColor *dyColor = [UIColor colorWithDynamicProvider:^UIColor * _Nonnull(UITraitCollection * _Nonnull traitCollection) {
            if (traitCollection.userInterfaceStyle == UIUserInterfaceStyleLight) {
                return MHColorFromHex(0x333333);
            }else {
                return MHColor(255, 255, 255);
            }
        }];
        return dyColor;
    }else{
        return MHColorFromHex(0x333333);
    }
}

+ (UIColor *)MHViewBgColor {
    if (@available(iOS 13.0, *)) {
        UIColor *dyColor = [UIColor colorWithDynamicProvider:^UIColor * _Nonnull(UITraitCollection * _Nonnull traitCollection) {
            if (traitCollection.userInterfaceStyle == UIUserInterfaceStyleLight) {
                return MHColor(247, 247, 247);
            }else {
                return MHColor(0, 0, 0);
            }
        }];
        return dyColor;
    }else{
        return MHColor(247, 247, 247);
    }
}

+ (UIColor *)MHMainColor {
    if (@available(iOS 13.0, *)) {
        UIColor *dyColor = [UIColor colorWithDynamicProvider:^UIColor * _Nonnull(UITraitCollection * _Nonnull traitCollection) {
            if (traitCollection.userInterfaceStyle == UIUserInterfaceStyleLight) {
                return MHColorFromHex(0x22A45D);
            }else {
                return MHColorFromHex(0xffffff);
            }
        }];
        return dyColor;
    }else{
        return MHColorFromHex(0x22A45D);
    }
}

+ (UIColor *)MHTextViewTintColor {
    if (@available(iOS 13.0, *)) {
        UIColor *dyColor = [UIColor colorWithDynamicProvider:^UIColor * _Nonnull(UITraitCollection * _Nonnull traitCollection) {
            if (traitCollection.userInterfaceStyle == UIUserInterfaceStyleLight) {
                return MHColorFromHex(0x22A45D);
            }else {
                return MHColorFromHex(0x000000);
            }
        }];
        return dyColor;
    }else{
        return MHColorFromHex(0x22A45D);
    }
}

+ (UIColor *)MHKeyColor {
    if (@available(iOS 13.0, *)) {
        UIColor *dyColor = [UIColor colorWithDynamicProvider:^UIColor * _Nonnull(UITraitCollection * _Nonnull traitCollection) {
            if (traitCollection.userInterfaceStyle == UIUserInterfaceStyleLight) {
                return MHColorFromHex(0x3388B3);
            }else {
                return MHColor(255, 255, 255);
            }
        }];
        return dyColor;
    }else{
        return MHColorFromHex(0x3388B3);
    }
}

+ (UIColor *)MHKeyBGColor {
    if (@available(iOS 13.0, *)) {
        UIColor *dyColor = [UIColor colorWithDynamicProvider:^UIColor * _Nonnull(UITraitCollection * _Nonnull traitCollection) {
            if (traitCollection.userInterfaceStyle == UIUserInterfaceStyleLight) {
                return MHColorFromHex(0x45CDAE);
            }else {
                return MHColor(255, 255, 255);
            }
        }];
        return dyColor;
    }else{
        return MHColorFromHex(0x45CDAE);
    }
}

+ (UIColor *)MHHomeNColor {
    if (@available(iOS 13.0, *)) {
        UIColor *dyColor = [UIColor colorWithDynamicProvider:^UIColor * _Nonnull(UITraitCollection * _Nonnull traitCollection) {
            if (traitCollection.userInterfaceStyle == UIUserInterfaceStyleLight) {
                return [UIColor colorWithRed:0 green:0 blue:0 alpha:0.5];
            }else {
                return MHColor(255, 255, 255);
            }
        }];
        return dyColor;
    }else{
        return [UIColor colorWithRed:0 green:0 blue:0 alpha:0.5];
    }
}

+ (UIColor *)MHHomeSColor {
    if (@available(iOS 13.0, *)) {
        UIColor *dyColor = [UIColor colorWithDynamicProvider:^UIColor * _Nonnull(UITraitCollection * _Nonnull traitCollection) {
            if (traitCollection.userInterfaceStyle == UIUserInterfaceStyleLight) {
                return [UIColor colorWithRed:0 green:0 blue:0 alpha:1];
            }else {
                return MHColor(255, 255, 255);
            }
        }];
        return dyColor;
    }else{
        return [UIColor colorWithRed:0 green:0 blue:0 alpha:1];
    }
}

+ (UIColor *)MHMsgTimeColor {
    if (@available(iOS 13.0, *)) {
        UIColor *dyColor = [UIColor colorWithDynamicProvider:^UIColor * _Nonnull(UITraitCollection * _Nonnull traitCollection) {
            if (traitCollection.userInterfaceStyle == UIUserInterfaceStyleLight) {
                return MHColorFromHex(0xC0C0C1);
            }else {
                return MHColor(255, 255, 255);
            }
        }];
        return dyColor;
    }else{
        return MHColorFromHex(0xC0C0C1);
    }
}

+ (UIColor *)MHMsgRecColor {
    if (@available(iOS 13.0, *)) {
        UIColor *dyColor = [UIColor colorWithDynamicProvider:^UIColor * _Nonnull(UITraitCollection * _Nonnull traitCollection) {
            if (traitCollection.userInterfaceStyle == UIUserInterfaceStyleLight) {
                return MHColorFromHex(0xFF0000);
            }else {
                return MHColorFromHex(0xFF0000);
            }
        }];
        return dyColor;
    }else{
        return MHColorFromHex(0xFF0000);
    }
}

+ (UIColor *)MHLineSeparatorColor {
    if (@available(iOS 13.0, *)) {
        UIColor *dyColor = [UIColor colorWithDynamicProvider:^UIColor * _Nonnull(UITraitCollection * _Nonnull traitCollection) {
            if (traitCollection.userInterfaceStyle == UIUserInterfaceStyleLight) {
                return MHColorFromHex(0xF7F7F9);
            }else {
                return MHColorFromHex(0x262626);
            }
        }];
        return dyColor;
    }else{
        return MHColorFromHex(0xF7F7F9);
    }
}

+ (UIColor *)MHTitleSubColor {
    if (@available(iOS 13.0, *)) {
        UIColor *dyColor = [UIColor colorWithDynamicProvider:^UIColor * _Nonnull(UITraitCollection * _Nonnull traitCollection) {
            if (traitCollection.userInterfaceStyle == UIUserInterfaceStyleLight) {
                return MHColorFromHex(0x75757C);
            }else {
                return MHColorFromHex(0xaaaaaa);
            }
        }];
        return dyColor;
    }else{
        return MHColorFromHex(0x75757C);
    }
}

+ (UIColor *)MHAlertColor {
    if (@available(iOS 13.0, *)) {
        UIColor *dyColor = [UIColor colorWithDynamicProvider:^UIColor * _Nonnull(UITraitCollection * _Nonnull traitCollection) {
            if (traitCollection.userInterfaceStyle == UIUserInterfaceStyleLight) {
                return MHColorFromHex(0xE5E6EB);
            }else {
                return MHColorFromHex(0x1F222A);
            }
        }];
        return dyColor;
    }else{
        return MHColorFromHex(0xE5E6EB);
    }
}

+ (UIColor *)MHTopMsgColor {
    if (@available(iOS 13.0, *)) {
        UIColor *dyColor = [UIColor colorWithDynamicProvider:^UIColor * _Nonnull(UITraitCollection * _Nonnull traitCollection) {
            if (traitCollection.userInterfaceStyle == UIUserInterfaceStyleLight) {
                return MHColorFromHex(0x808080);
            }else {
                return MHColorFromHex(0xffffff);
            }
        }];
        return dyColor;
    }else{
        return MHColorFromHex(0x808080);
    }
}

@end
