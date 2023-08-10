//
//  ZoloEmoOrImgView.h
//  NewZolo
//
//  Created by JTalking on 2022/7/5.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^EmojClickBlock)(NSString *emjStr);
typedef void(^SendEmojClickBlock)();

@interface ZoloEmoOrImgView : UIView

@property (nonatomic, copy) EmojClickBlock emojClickBlock;
@property (nonatomic, copy) SendEmojClickBlock sendEmojClickBlock;

@end

NS_ASSUME_NONNULL_END
