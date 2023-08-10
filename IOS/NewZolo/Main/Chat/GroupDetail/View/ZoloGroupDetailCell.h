//
//  ZoloGroupDetailCell.h
//  NewZolo
//
//  Created by JTalking on 2022/7/11.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^TopSwitchBlock)(BOOL isTop);

@interface ZoloGroupDetailCell : UITableViewCell

@property (weak, nonatomic) IBOutlet UILabel *nameLabel;
@property (weak, nonatomic) IBOutlet UIImageView *arrowImg;
@property (weak, nonatomic) IBOutlet UILabel *remarkLabel;
@property (weak, nonatomic) IBOutlet UISwitch *switchBtn;

@property (nonatomic, copy) TopSwitchBlock topSwitchBlock;

@end

NS_ASSUME_NONNULL_END
