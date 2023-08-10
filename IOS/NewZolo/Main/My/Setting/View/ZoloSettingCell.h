//
//  ZoloSettingCell.h
//  NewZolo
//
//  Created by JTalking on 2022/6/30.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface ZoloSettingCell : UITableViewCell

@property (weak, nonatomic) IBOutlet UILabel *titleName;
@property (weak, nonatomic) IBOutlet UIImageView *arrowImg;
@property (weak, nonatomic) IBOutlet UILabel *remarkLab;

@end

NS_ASSUME_NONNULL_END
