//
//  ZoloSendRedPackVC.h
//  NewZolo
//
//  Created by JTalking on 2022/9/8.
//

#import "MHParentViewController.h"

NS_ASSUME_NONNULL_BEGIN

@interface ZoloSendRedPackVC : MHParentViewController

- (instancetype)initWithRedPackType:(NSInteger)type withConversation:(DMCCConversation *)conversation;

@end

NS_ASSUME_NONNULL_END
