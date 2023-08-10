//
//  ZoloChatMoreView.h
//  NewZolo
//
//  Created by JTalking on 2022/7/13.
//

#import <UIKit/UIKit.h>

typedef void(^MoreClickBlock)(NSInteger index);

NS_ASSUME_NONNULL_BEGIN

@interface ZoloChatMoreView : UIView

@property (nonatomic, copy) MoreClickBlock moreClickBlock;
@property (nonatomic, assign) DMCCConversationType chatType;
@property (nonatomic, assign) BOOL isShowBom;

@end

NS_ASSUME_NONNULL_END
