//
//  ZoloMulBottomView.h
//  NewZolo
//
//  Created by MHHY on 2023/5/9.
//

#import <UIKit/UIKit.h>

typedef void(^MulDelBlock)(void);
typedef void(^MulForwardBlock)(void);

NS_ASSUME_NONNULL_BEGIN

@interface ZoloMulBottomView : UIView

@property (nonatomic, copy) MulDelBlock mulDelBlock;
@property (nonatomic, copy) MulForwardBlock mulForwardBlock;

@end

NS_ASSUME_NONNULL_END
