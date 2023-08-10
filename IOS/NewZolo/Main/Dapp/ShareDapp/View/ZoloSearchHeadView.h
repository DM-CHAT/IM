//
//  ZoloSearchHeadView.h
//  NewZolo
//
//  Created by MHHY on 2023/3/9.
//

#import <UIKit/UIKit.h>

typedef void(^SearchBlock)(NSString *str);

NS_ASSUME_NONNULL_BEGIN

@interface ZoloSearchHeadView : UIView

@property (nonatomic, copy) SearchBlock searchBlock;

@end

NS_ASSUME_NONNULL_END
