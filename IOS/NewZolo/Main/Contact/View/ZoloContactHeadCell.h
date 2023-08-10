//
//  ZoloContactHeadCell.h
//  NewZolo
//
//  Created by JTalking on 2022/6/30.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface ZoloContactHeadCell : UICollectionViewCell

@property (weak, nonatomic) IBOutlet UIImageView *icon;
@property (weak, nonatomic) IBOutlet UILabel *nickName;

@property (weak, nonatomic) IBOutlet UIView *unRedCount;

@end

NS_ASSUME_NONNULL_END
