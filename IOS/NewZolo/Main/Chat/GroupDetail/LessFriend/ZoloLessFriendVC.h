//
//  ZoloLessFriendVC.h
//  NewZolo
//
//  Created by JTalking on 2022/9/20.
//

#import "MHParentViewController.h"

NS_ASSUME_NONNULL_BEGIN
typedef void(^ConfirmBlock)(NSArray *contactArray);

@interface ZoloLessFriendVC : MHParentViewController

@property (nonatomic, copy) ConfirmBlock confirmBlock;

- (instancetype)initWithMemeberArray:(NSArray *)memberArray groupId:(NSString *)groupId;

@end

NS_ASSUME_NONNULL_END
