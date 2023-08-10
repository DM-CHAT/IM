//
//  ZoloSingleChatVC.h
//  NewZolo
//
//  Created by JTalking on 2022/6/30.
//

#import "MHParentViewController.h"

NS_ASSUME_NONNULL_BEGIN

@interface ZoloSingleChatVC : MHParentViewController

- (instancetype)initWithConversationInfo:(DMCCConversationInfo *)conversation;

- (instancetype)initWithConversationInfo:(DMCCConversationInfo *)conversation withMessage:(DMCCMessage *)message;

@end

NS_ASSUME_NONNULL_END
