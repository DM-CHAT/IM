//
//  DMCUConfigManager.h
//  DMChatUIKit
//
//  Created by heavyrain lee on 2019/9/22.
//  Copyright © 2019 WF Chat. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
//#import "DMCUAppServiceProvider.h"

NS_ASSUME_NONNULL_BEGIN

@class DMCCConversation;
typedef NS_ENUM(NSInteger, DMCCMediaType);
/**
 主题类型

 - ThemeType_DMChat: ZOLO风格
 - ThemeType_White: 白色风格
 */
typedef NS_ENUM(NSInteger, DMCUThemeType) {
    ThemeType_DMChat,
    ThemeType_White
};

@interface DMCUConfigManager : NSObject
+ (DMCUConfigManager *)globalManager;

- (void)setupNavBar;
@property(nonatomic, assign)DMCUThemeType selectedTheme;

@property(nonatomic, strong)UIColor *backgroudColor;
/*
 * 与backgroudColor的区别是，backgroudColor是内容区的背景颜色；frameBackgroudColor是内容区之外框架的颜色，也用在输入框的背景色。
 */
@property(nonatomic, strong)UIColor *frameBackgroudColor;
@property(nonatomic, strong)UIColor *textColor;

@property(nonatomic, strong)UIColor *naviBackgroudColor;
@property(nonatomic, strong)UIColor *naviTextColor;

@property(nonatomic, strong)UIColor *separateColor;

//@property(nonatomic, weak)id<DMCUAppServiceProvider> appServiceProvider;

@property(nonatomic, strong)NSString *fileTransferId;

@property(nonatomic, strong)NSString *conversationFilesDir;

- (NSString *)cachePathOf:(DMCCConversation *)conversation mediaType:(DMCCMediaType)mediaType;
@end

NS_ASSUME_NONNULL_END
