//
//  ZoloSendGroupFootView.h
//  NewZolo
//
//  Created by JTalking on 2022/8/23.
//

#import <UIKit/UIKit.h>

typedef void(^FinishBlock)(void);

NS_ASSUME_NONNULL_BEGIN

@interface ZoloSendGroupFootView : UIView

@property (nonatomic, strong) NSArray *data;

@property (nonatomic, copy) FinishBlock finishBlock;

@end

NS_ASSUME_NONNULL_END
