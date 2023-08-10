//
//  ZoloAddFriendFootView.h
//  NewZolo
//
//  Created by JTalking on 2022/8/24.
//

#import <UIKit/UIKit.h>

typedef void(^AddFriendBlock)(void);

NS_ASSUME_NONNULL_BEGIN

@interface ZoloAddFriendFootView : UITableViewHeaderFooterView

@property (weak, nonatomic) IBOutlet UIButton *addContactBtn;

@property (nonatomic, copy) AddFriendBlock adddFriendBlock;

@end

NS_ASSUME_NONNULL_END
