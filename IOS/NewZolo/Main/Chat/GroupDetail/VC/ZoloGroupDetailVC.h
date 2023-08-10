//
//  ZoloGroupDetailVC.h
//  NewZolo
//
//  Created by JTalking on 2022/7/11.
//

#import "MHParentViewController.h"

NS_ASSUME_NONNULL_BEGIN

@interface ZoloGroupDetailVC : MHParentViewController

- (instancetype)initWithConversation:(DMCCConversationInfo *)conversationInfo withDapp:(DMCCLitappInfo *)dapp isShowBom:(BOOL)isShowBom;

@end

NS_ASSUME_NONNULL_END
