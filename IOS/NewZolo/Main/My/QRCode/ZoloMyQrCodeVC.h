//
//  ZoloMyQrCodeVC.h
//  NewZolo
//
//  Created by JTalking on 2022/7/6.
//

#import "MHParentViewController.h"

NS_ASSUME_NONNULL_BEGIN

@interface ZoloMyQrCodeVC : MHParentViewController

- (instancetype)initWithGroupQr:(DMCCConversation *)conversation withIsGroup:(BOOL)isGroup IsUser:(BOOL)isUser;

@end

NS_ASSUME_NONNULL_END
