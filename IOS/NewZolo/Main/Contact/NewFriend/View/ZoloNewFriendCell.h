//
//  ZoloNewFriendCell.h
//  NewZolo
//
//  Created by JTalking on 2022/6/30.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void (^UserBtnBlock)(void);
typedef void (^IconBtnBlock)(void);

@interface ZoloNewFriendCell : UITableViewCell

@property (nonatomic, copy) UserBtnBlock userBtnBlock;
@property (nonatomic, copy) IconBtnBlock iconBtnBlock;
@property (nonatomic, strong) DMCCFriendRequest *info;

@end

NS_ASSUME_NONNULL_END
