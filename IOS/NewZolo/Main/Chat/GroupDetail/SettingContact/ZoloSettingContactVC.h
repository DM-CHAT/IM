//
//  ZoloSettingContactVC.h
//  NewZolo
//
//  Created by JTalking on 2022/8/23.
//

#import "MHParentViewController.h"

NS_ASSUME_NONNULL_BEGIN

typedef void(^ConfirmBlock)(NSArray *contactArray);

@interface ZoloSettingContactVC : MHParentViewController

@property (nonatomic, copy) ConfirmBlock confirmBlock;

// type 1 管理员  2 群成员 3 成员
- (instancetype)initWithGroupInfo:(DMCCGroupInfo *)info withType:(NSInteger)type;

@end

NS_ASSUME_NONNULL_END
