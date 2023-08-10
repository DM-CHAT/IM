//
//  ZoloMyHeadView.h
//  NewZolo
//
//  Created by JTalking on 2022/6/29.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^PersonEditBlock)(void);
typedef void(^LongGesClickBlock)(void);

@interface ZoloPersonHeadView : UITableViewHeaderFooterView

@property (weak, nonatomic) IBOutlet UIImageView *icon;
@property (weak, nonatomic) IBOutlet UILabel *nickName;
@property (weak, nonatomic) IBOutlet UILabel *remark;
@property (nonatomic, copy) PersonEditBlock personEditBlock;
@property (nonatomic, copy) LongGesClickBlock longGesClickBlock;

@end

NS_ASSUME_NONNULL_END
