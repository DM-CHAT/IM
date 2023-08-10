//
//  ZoloInfoManager.h
//  NewZolo
//
//  Created by JTalking on 2022/10/6.
//

#import <Foundation/Foundation.h>
#import "MHLoginModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface ZoloInfoManager : NSObject

@property (nonatomic, strong) MHLoginModel *currentInfo;

+ (instancetype)sharedUserManager;

/** 保存当前用户 */
- (void)savecurrentInfo:(MHLoginModel *)user;
- (void)loginOut;

@end

NS_ASSUME_NONNULL_END
