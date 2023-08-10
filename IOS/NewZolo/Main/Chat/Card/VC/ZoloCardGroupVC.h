//
//  ZoloCardGroupVC.h
//  NewZolo
//
//  Created by JTalking on 2022/9/2.
//

#import "ZoloContactGroupVC.h"

NS_ASSUME_NONNULL_BEGIN

typedef void(^GroupSelectBlock)(DMCCGroupInfo *groupInfo);

@interface ZoloCardGroupVC : ZoloContactGroupVC

@property (nonatomic, copy) GroupSelectBlock groupSelectBlock;

- (instancetype)initWithGroupInfo:(DMCCGroupInfo*)info;

@end

NS_ASSUME_NONNULL_END
