//
//  ZoloCardContactVC.h
//  NewZolo
//
//  Created by JTalking on 2022/9/2.
//

#import "ZoloContactVC.h"

NS_ASSUME_NONNULL_BEGIN
typedef void(^CardReturnBlock)(DMCCUserInfo *info);
typedef void(^CardGroupReturnBlock)(DMCCGroupInfo *groupInfo);
typedef void(^CardLitAppReturnBlock)(DMCCLitappInfo *litAppInfo);

@interface ZoloCardContactVC : ZoloContactVC

- (instancetype)initWithUserInfo:(id)info;

@property (nonatomic, copy) CardReturnBlock cardReturenBlock;
@property (nonatomic, copy) CardGroupReturnBlock cardGroupReturnBlock;
@property (nonatomic, copy) CardLitAppReturnBlock cardLitAppReturnBlock;

@end

NS_ASSUME_NONNULL_END
