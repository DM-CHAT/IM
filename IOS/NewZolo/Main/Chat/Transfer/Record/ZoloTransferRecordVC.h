//
//  ZoloTransferRecordVC.h
//  NewZolo
//
//  Created by JTalking on 2022/9/9.
//

#import "MHParentViewController.h"

NS_ASSUME_NONNULL_BEGIN

typedef void(^ReceiveBlock)(void);

@interface ZoloTransferRecordVC : MHParentViewController

- (instancetype)initWithRedPacketInfo:(DMCCRedPacketInfo *)info;

@property (nonatomic, copy) ReceiveBlock receiveBlock;

@end

NS_ASSUME_NONNULL_END
