//
//  ZZZKeyboard.h
//  keyboard
//
//  Created by 货大大 on 16/3/28.
//  Copyright © 2016年 货大大. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ZZZKeyboard : UIInputView<UITextInputDelegate>
@property (nonatomic,weak) id <UIKeyInput> keyInput;

@property (nonatomic,assign) BOOL isAutoScroll;
@end
