//
//  ZoloGroupAddFriendVC.h
//  NewZolo
//
//  Created by JTalking on 2022/9/12.
//

#import "MHParentViewController.h"

NS_ASSUME_NONNULL_BEGIN
typedef void(^ConfirmBlock)(NSArray *contactArray);

@interface ZoloGroupAddFriendVC : MHParentViewController

@property (nonatomic, copy) ConfirmBlock confirmBlock;

- (instancetype)initWithGroupInfo:(DMCCGroupInfo *)info;

@end

NS_ASSUME_NONNULL_END
