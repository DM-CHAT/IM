//
//  ZoloCardMulVC.h
//  NewZolo
//
//  Created by MHHY on 2023/5/11.
//

#import "MHParentViewController.h"

NS_ASSUME_NONNULL_BEGIN

typedef void(^CardReturnInfoBlock)(id info);

@interface ZoloCardMulVC : MHParentViewController

- (instancetype)initWithUserInfo:(DMCCUserInfo *)user;
- (instancetype)initWithGroupInfo:(DMCCGroupInfo *)group;

@property (nonatomic, copy) CardReturnInfoBlock cardReturenBlock;

@end

NS_ASSUME_NONNULL_END
