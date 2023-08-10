//
//  ZoloGroupDappCell.h
//  NewZolo
//
//  Created by JTalking on 2022/8/30.
//

#import <UIKit/UIKit.h>
#import "ZoloDappModel.h"
NS_ASSUME_NONNULL_BEGIN

@interface ZoloGroupDappCell : UITableViewCell

@property (weak, nonatomic) IBOutlet UIImageView *statusImg;
@property (weak, nonatomic) IBOutlet UIImageView *iconImg;
@property (weak, nonatomic) IBOutlet UILabel *titleLab;

@property (nonatomic, strong) ZoloDappModel *dappModel;

@end

NS_ASSUME_NONNULL_END
