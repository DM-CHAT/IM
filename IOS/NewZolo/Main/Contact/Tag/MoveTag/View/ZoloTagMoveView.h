//
//  ZoloTagMoveView.h
//  NewZolo
//
//  Created by JTalking on 2022/10/31.
//

#import <UIKit/UIKit.h>

typedef void(^TagDelBtnBlock)(void);
typedef void(^TagMoveBtnBlock)(void);
typedef void(^TagAllBtnBlock)(BOOL isAll);

NS_ASSUME_NONNULL_BEGIN

@interface ZoloTagMoveView : UIView

@property (nonatomic, copy) TagDelBtnBlock tagDelBtnBlock;
@property (nonatomic, copy) TagMoveBtnBlock tagMoveBtnBlock;
@property (nonatomic, copy) TagAllBtnBlock tagAllBtnBlock;

@end

NS_ASSUME_NONNULL_END
