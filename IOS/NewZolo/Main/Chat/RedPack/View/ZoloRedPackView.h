//
//  ZoloRedPackView.h
//  NewZolo
//
//  Created by JTalking on 2022/9/9.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^RedPackCancelBlcok)(void);

typedef void(^RedPackOpenBlcok)(void);

@interface ZoloRedPackView : UIView

@property (weak, nonatomic) IBOutlet UIImageView *iconImg;
@property (weak, nonatomic) IBOutlet UILabel *nameLab;
@property (weak, nonatomic) IBOutlet UILabel *titleLab;
@property (weak, nonatomic) IBOutlet UIImageView *sanBgImg;
@property (weak, nonatomic) IBOutlet UILabel *openLable;

@property (nonatomic, copy) RedPackCancelBlcok redPackCancelBlcok;
@property (nonatomic, copy) RedPackOpenBlcok redPackOpenBlcok;

@end

NS_ASSUME_NONNULL_END
