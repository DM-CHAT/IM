//
//  ZoloCardVC.h
//  NewZolo
//
//  Created by JTalking on 2022/8/5.
//

#import "MHParentViewController.h"

NS_ASSUME_NONNULL_BEGIN

typedef void(^CardReturnInfoBlock)(id info);

@interface ZoloCardVC : MHParentViewController

- (instancetype)initWithUserInfo:(DMCCUserInfo *)user;
- (instancetype)initWithGroupInfo:(DMCCGroupInfo *)group;

@property (nonatomic, copy) CardReturnInfoBlock cardReturenBlock;

@end

NS_ASSUME_NONNULL_END
