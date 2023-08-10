//
//  ZoloShareDappVC.h
//  NewZolo
//
//  Created by JTalking on 2023/1/12.
//

#import "MHParentViewController.h"

NS_ASSUME_NONNULL_BEGIN

@interface ZoloShareCardVC : MHParentViewController

- (instancetype)initWithUserInfo:(DMCCUserInfo *)user;
- (instancetype)initWithGroupInfo:(DMCCGroupInfo *)group;

@end

NS_ASSUME_NONNULL_END
