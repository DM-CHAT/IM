//
//  ZoloMyHeadView.h
//  NewZolo
//
//  Created by JTalking on 2022/6/29.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
typedef void(^LongGesClickBlock)(void);
typedef void(^IconClickBlock)(void);

@interface ZoloConDetailHeadView : UITableViewHeaderFooterView

@property (weak, nonatomic) IBOutlet UIImageView *icon;
@property (weak, nonatomic) IBOutlet UILabel *nickName;
@property (weak, nonatomic) IBOutlet UILabel *remark;
@property (weak, nonatomic) IBOutlet UIImageView *nftImg;

@property (nonatomic, copy) LongGesClickBlock longGesClickBlock;
@property (nonatomic, copy) IconClickBlock iconClickBlock;


@end

NS_ASSUME_NONNULL_END
