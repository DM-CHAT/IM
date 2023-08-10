//
//  ZoloMyCell.h
//  NewZolo
//
//  Created by JTalking on 2022/6/29.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface ZoloWalletCell : UITableViewCell

@property (weak, nonatomic) IBOutlet UILabel *title;
@property (weak, nonatomic) IBOutlet UIImageView *icon;
@property (weak, nonatomic) IBOutlet UILabel *redStatus;

@end

NS_ASSUME_NONNULL_END
