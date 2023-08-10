//
//  ZoloCardView.h
//  NewZolo
//
//  Created by JTalking on 2022/8/5.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^OkBtnClick)(NSString *str);
typedef void(^CancelBtnClick)(void);

@interface ZoloCardView : UIView

@property (weak, nonatomic) IBOutlet UIImageView *iconImg;
@property (weak, nonatomic) IBOutlet UILabel *nameLab;
@property (weak, nonatomic) IBOutlet UILabel *cardNameLab;

@property (nonatomic, copy) OkBtnClick okBtnClick;
@property (nonatomic, copy) CancelBtnClick cancelBtnClick;

@end

NS_ASSUME_NONNULL_END
