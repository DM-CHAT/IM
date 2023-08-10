//
//  ZoloAddFriendInfoFoot.h
//  NewZolo
//
//  Created by JTalking on 2022/8/24.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^AddBtnBLock)(void);

@interface ZoloAddFriendInfoFoot : UITableViewHeaderFooterView

@property (weak, nonatomic) IBOutlet UIButton *addBtn;


@property (nonatomic, copy) AddBtnBLock addBtnBLock;

@end

NS_ASSUME_NONNULL_END
