//
//  ZoloTagNameCell.h
//  NewZolo
//
//  Created by JTalking on 2022/10/27.
//

#import <UIKit/UIKit.h>
#import "ZoloTagModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface ZoloTagNameCell : UITableViewCell

@property (weak, nonatomic) IBOutlet UIView *bgView;
@property (weak, nonatomic) IBOutlet UILabel *tagName;
@property (weak, nonatomic) IBOutlet UILabel *userNames;
@property (nonatomic, strong) ZoloTagModel *tagModel;

@end

NS_ASSUME_NONNULL_END
