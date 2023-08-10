//
//  ZoloRedPackView.h
//  NewZolo
//
//  Created by JTalking on 2022/9/9.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^RedPackCancelBlcok)(void);

typedef void(^RedPackDetailBlcok)(void);

@interface ZoloRedPackOpenView : UIView

@property (weak, nonatomic) IBOutlet UIImageView *iconImg;
@property (weak, nonatomic) IBOutlet UILabel *nameLab;
@property (weak, nonatomic) IBOutlet UILabel *titleLab;
@property (weak, nonatomic) IBOutlet UILabel *moneyLab;
@property (weak, nonatomic) IBOutlet UILabel *typeLab;
@property (weak, nonatomic) IBOutlet UIImageView *bgImg;

@property (weak, nonatomic) IBOutlet UILabel *seeDetailLab;

@property (weak, nonatomic) IBOutlet UILabel *redPackOverLab;

@property (nonatomic, copy) RedPackCancelBlcok redPackCancelBlcok;
@property (nonatomic, copy) RedPackDetailBlcok redPackDetailBlcok;

@end

NS_ASSUME_NONNULL_END
