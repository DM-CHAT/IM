//
//  ZoloMulTopView.h
//  NewZolo
//
//  Created by MHHY on 2023/5/9.
//

#import <UIKit/UIKit.h>

typedef void(^MulCancelBlock)(void);

NS_ASSUME_NONNULL_BEGIN

@interface ZoloMulTopView : UIView

@property (nonatomic, copy) MulCancelBlock mulCancelBlock;

@end

NS_ASSUME_NONNULL_END
