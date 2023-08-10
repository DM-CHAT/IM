//
//  ZoloChatDetailVC.h
//  NewZolo
//
//  Created by JTalking on 2022/8/15.
//

#import "MHParentViewController.h"

NS_ASSUME_NONNULL_BEGIN

@interface ZoloChatDetailVC : MHParentViewController

- (instancetype)initWithUserInfo:(DMCCUserInfo *)userInfo withConversition:(DMCCConversationInfo *)conversationInfo;

@end

NS_ASSUME_NONNULL_END
