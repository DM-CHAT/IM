//
//  ZoloSoundManager.h
//  NewZolo
//
//  Created by JTalking on 2022/8/5.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface ZoloSoundManager : NSObject

@property (nonatomic,assign)BOOL isSounding;

+ (instancetype)sharedManager;

- (void)prePlayVideoNotifySound;
- (void)stopAlertSound;

@end

NS_ASSUME_NONNULL_END
