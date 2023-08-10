//
//  DMCUConfigManager.m
//  DMChatUIKit
//
//  Created by heavyrain lee on 2019/9/22.
//  Copyright © 2019 WF Chat. All rights reserved.
//

#import "DMCUConfigManager.h"
#import <DMChatClient/DMCChatClient.h>

static DMCUConfigManager *sharedSingleton = nil;
@implementation DMCUConfigManager

+ (DMCUConfigManager *)globalManager {
    if (sharedSingleton == nil) {
        @synchronized (self) {
            if (sharedSingleton == nil) {
                sharedSingleton = [[DMCUConfigManager alloc] init];
                sharedSingleton.conversationFilesDir = @"ConversationResource";
            }
        }
    }
    return sharedSingleton;
}

- (instancetype)init {
    self = [super init];
    if (self) {
        _selectedTheme = [[NSUserDefaults standardUserDefaults] integerForKey:@"DMC_THEME_TYPE"];
    }
    return self;
}

-(void)setSelectedTheme:(DMCUThemeType)themeType {
    _selectedTheme = themeType;
    
    [[NSUserDefaults standardUserDefaults] setInteger:themeType forKey:@"DMC_THEME_TYPE"];
    [[NSUserDefaults standardUserDefaults] synchronize];
    
    [self setupNavBar];
}

- (void)setupNavBar {
    [UIApplication sharedApplication].statusBarStyle = UIStatusBarStyleLightContent;
    
    UINavigationBar *bar = [UINavigationBar appearance];
    bar.barTintColor = [DMCUConfigManager globalManager].naviBackgroudColor;
    bar.tintColor = [DMCUConfigManager globalManager].naviTextColor;
    bar.titleTextAttributes = @{NSForegroundColorAttributeName : [DMCUConfigManager globalManager].naviTextColor};
    bar.barStyle = UIBarStyleDefault;
    
    if (@available(iOS 13, *)) {
        UINavigationBarAppearance *navBarAppearance = [[UINavigationBarAppearance alloc] init];
        bar.standardAppearance = navBarAppearance;
        bar.scrollEdgeAppearance = navBarAppearance;
        navBarAppearance.backgroundColor = [DMCUConfigManager globalManager].naviBackgroudColor;
        navBarAppearance.titleTextAttributes = @{NSForegroundColorAttributeName:[DMCUConfigManager globalManager].naviTextColor};
    }
    
    [[UITabBar appearance] setBarTintColor:[DMCUConfigManager globalManager].frameBackgroudColor];
    [UITabBar appearance].translucent = YES;
}

- (UIColor *)backgroudColor {
    if (_backgroudColor) {
        return _backgroudColor;
    }
    BOOL darkModel = NO;
    if (@available(iOS 13.0, *)) {
        if(UITraitCollection.currentTraitCollection.userInterfaceStyle == UIUserInterfaceStyleDark) {
            darkModel = YES;
        }
    }
    
    if (darkModel) {
        return [UIColor colorWithRed:33/255.f green:33/255.f blue:33/255.f alpha:1.0f];
    } else {
        if (self.selectedTheme == ThemeType_DMChat) {
            return [UIColor colorWithRed:243/255.f green:243/255.f blue:243/255.f alpha:1.0f];
        } else if (self.selectedTheme == ThemeType_White) {
            return MHColorFromHex(0xededed);
        }
        return [UIColor whiteColor];
    }
}

- (UIColor *)frameBackgroudColor {
    if (_frameBackgroudColor) {
        return _frameBackgroudColor;
    }
    BOOL darkModel = NO;
    if (@available(iOS 13.0, *)) {
        if(UITraitCollection.currentTraitCollection.userInterfaceStyle == UIUserInterfaceStyleDark) {
            darkModel = YES;
        }
    }
    
    if (darkModel) {
        return [UIColor colorWithRed:39/255.f green:39/255.f blue:39/255.f alpha:1.0f];
    } else {
        return [UIColor colorWithRed:239/255.f green:239/255.f blue:239/255.f alpha:1.0f];
    }
}

- (UIColor *)textColor {
    if (_textColor) {
        return _textColor;
    }
    BOOL darkModel = NO;
    if (@available(iOS 13.0, *)) {
        if(UITraitCollection.currentTraitCollection.userInterfaceStyle == UIUserInterfaceStyleDark) {
            darkModel = YES;
        }
    }
    
    if (darkModel) {
        return [UIColor whiteColor];
    } else {
        return MHColorFromHex(0x1d1d1d);
    }
}

- (UIColor *)naviBackgroudColor {
    if (_naviBackgroudColor) {
        return _naviBackgroudColor;
    }
    BOOL darkModel = NO;
    if (@available(iOS 13.0, *)) {
        if(UITraitCollection.currentTraitCollection.userInterfaceStyle == UIUserInterfaceStyleDark) {
            darkModel = YES;
        }
    }
    
    if (darkModel) {
        return [UIColor colorWithRed:39/255.f green:39/255.f blue:39/255.f alpha:1.0f];
    } else {
        if (self.selectedTheme == ThemeType_DMChat) {
            return [UIColor colorWithRed:0.1 green:0.27 blue:0.9 alpha:0.9];
        } else if(self.selectedTheme == ThemeType_White) {
            return MHColorFromHex(0xededed);
        }
        return [UIColor colorWithRed:239/255.f green:239/255.f blue:239/255.f alpha:1.0f];
    }
}

- (UIColor *)naviTextColor {
    if (_naviTextColor) {
        return _naviTextColor;
    }
    BOOL darkModel = NO;
    if (@available(iOS 13.0, *)) {
        if(UITraitCollection.currentTraitCollection.userInterfaceStyle == UIUserInterfaceStyleDark) {
            darkModel = YES;
        }
    }
    
    if (darkModel) {
        return [UIColor whiteColor];
    } else {
        if (self.selectedTheme == ThemeType_DMChat) {
            return [UIColor blackColor];
        } else if(self.selectedTheme == ThemeType_White) {
            MHColorFromHex(0x0c0c0c);
        }
        return [UIColor blackColor];
    }
}

- (UIColor *)separateColor {
    if (_separateColor) {
        return _separateColor;
    }
    BOOL darkModel = NO;
    if (@available(iOS 13.0, *)) {
        if(UITraitCollection.currentTraitCollection.userInterfaceStyle == UIUserInterfaceStyleDark) {
            darkModel = YES;
        }
    }
    
    if (darkModel) {
        return MHColorFromHex(0x3f3f3f);
    } else {
        return MHColorFromHex(0xe7e7e7);
    }
    
}

//file path document/conversationresource/conv_line/conv_type/conv_target/mediatype/
- (NSString *)cachePathOf:(DMCCConversation *)conversation mediaType:(DMCCMediaType)mediaType {
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory,
                                                         NSUserDomainMask, YES);
    NSString *documentDirectory = [paths objectAtIndex:0];
    NSString *path = [documentDirectory stringByAppendingPathComponent:self.conversationFilesDir];
    path = [path stringByAppendingPathComponent:[NSString stringWithFormat:@"%d/%d/%@", conversation.line, (int)conversation.type, conversation.target]];
    
    NSString *type = @"general";
    if(mediaType == Media_Type_IMAGE) type = @"image";
    else if(mediaType == Media_Type_VOICE) type = @"voice";
    else if(mediaType == Media_Type_VIDEO) type = @"video";
    else if(mediaType == Media_Type_PORTRAIT) type = @"portrait";
    else if(mediaType == Media_Type_FAVORITE) type = @"favorite";
    else if(mediaType == Media_Type_STICKER) type = @"sticker";
    else if(mediaType == Media_Type_MOMENTS) type = @"moments";
    
    path = [path stringByAppendingPathComponent:type];

    if (![[NSFileManager defaultManager] fileExistsAtPath:path]) {
        [[NSFileManager defaultManager] createDirectoryAtPath:path withIntermediateDirectories:YES attributes:nil error:nil];
    }
    return path;
}
@end
