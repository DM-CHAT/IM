//
//  ZoloForwardView.h
//  NewZolo
//
//  Created by JTalking on 2022/8/16.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^ForwardBlock)(NSInteger index);

@interface ZoloForwardView : UIView

@property (nonatomic, strong) NSArray *titleArray;
@property (nonatomic, strong) NSArray *imgArray;
@property (nonatomic, copy) ForwardBlock forwardBlock;

- (void)setMyInfo:(DMCCUserInfo *)myInfo withMessage:(DMCCMessage *)message type:(DMCCConversationType)chatType isOwner:(BOOL)isOwner;
- (void)setSasstion:(BOOL)isSasstion;

@end

NS_ASSUME_NONNULL_END
