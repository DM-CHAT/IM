//
//  ZoloMyHeadView.h
//  NewZolo
//
//  Created by JTalking on 2022/6/29.
//

#import <UIKit/UIKit.h>

typedef void(^HeadBtnClickBlock)(NSInteger index);
typedef void(^LongGesClickBlock)(void);
NS_ASSUME_NONNULL_BEGIN

@interface ZoloMyHeadView : UITableViewHeaderFooterView

@property (weak, nonatomic) IBOutlet UIImageView *icon;
@property (weak, nonatomic) IBOutlet UILabel *nickName;
@property (weak, nonatomic) IBOutlet UILabel *remark;
@property (nonatomic, copy) HeadBtnClickBlock headBtnBlock;
@property (nonatomic, copy) LongGesClickBlock longGesClickBlock;
@property (weak, nonatomic) IBOutlet UIImageView *nftImg;

@end

NS_ASSUME_NONNULL_END
