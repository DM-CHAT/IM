//
//  ZoloCardSmallProgramVC.h
//  NewZolo
//
//  Created by JTalking on 2022/9/2.
//

#import "ZoloSmallProgramVC.h"

NS_ASSUME_NONNULL_BEGIN

typedef void(^LitAppSelectBlock)(DMCCLitappInfo *litAppInfo);

@interface ZoloCardSmallProgramVC : ZoloSmallProgramVC

@property (nonatomic, copy) LitAppSelectBlock litAppSelectBlock;

@end

NS_ASSUME_NONNULL_END
