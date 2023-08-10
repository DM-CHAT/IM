//
//  ZoloSingleCell.h
//  NewZolo
//
//  Created by JTalking on 2022/6/29.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
typedef void(^CellLongBlock)(void);

@interface ZoloSingleCell : UITableViewCell

@property (weak, nonatomic) IBOutlet UILabel *msgTime;
@property (weak, nonatomic) IBOutlet UILabel *msgRead;
@property (weak, nonatomic) IBOutlet UILabel *msgContent;
@property (weak, nonatomic) IBOutlet UIView *redView;
@property (weak, nonatomic) IBOutlet UIImageView *silentImg;
@property (weak, nonatomic) IBOutlet UIImageView *icon;
@property (weak, nonatomic) IBOutlet UILabel *nickName;

@property (nonatomic, strong) DMCCUserInfo *userInfo;

@property (nonatomic, copy) CellLongBlock cellLongBlock;

@end

NS_ASSUME_NONNULL_END
