//
//  ZoloSendGroupVC.h
//  NewZolo
//
//  Created by JTalking on 2022/7/7.
//

#import "MHParentViewController.h"

NS_ASSUME_NONNULL_BEGIN
typedef void(^SendGroupBlock)(NSString * groupId);

@interface ZoloSendGroupVC : MHParentViewController

@property (nonatomic, copy) SendGroupBlock sendGroupBlock;

@end

NS_ASSUME_NONNULL_END
