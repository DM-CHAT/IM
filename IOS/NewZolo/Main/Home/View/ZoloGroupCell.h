//
//  ZoloSingleCell.h
//  NewZolo
//
//  Created by JTalking on 2022/6/29.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
typedef void(^CellLongBlock)(void);

@interface ZoloGroupCell : UITableViewCell

@property (weak, nonatomic) IBOutlet UILabel *msgTime;
@property (weak, nonatomic) IBOutlet UILabel *msgRead;
@property (weak, nonatomic) IBOutlet UILabel *msgContent;
@property (nonatomic, strong) DMCCGroupInfo *groupInfo;
@property (weak, nonatomic) IBOutlet UIView *redView;
@property (weak, nonatomic) IBOutlet UIImageView *silentImg;

@property (nonatomic, copy) CellLongBlock cellLongBlock;


@end

NS_ASSUME_NONNULL_END
