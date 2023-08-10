//
//  ZoloHomeAddView.h
//  NewZolo
//
//  Created by JTalking on 2022/7/7.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^AddViewBlock)(NSInteger index);

@interface ZoloHomeAddView : UIView

@property (nonatomic, copy) AddViewBlock addViewBlock;

- (void)hiddleAddView;

@end

NS_ASSUME_NONNULL_END
