//
//  ZoloChatSendView.h
//  NewZolo
//
//  Created by JTalking on 2022/7/1.
//

#import <UIKit/UIKit.h>
#import "YYTextView.h"
NS_ASSUME_NONNULL_BEGIN

typedef void(^ReturnBlock)(NSString *sendStr);
typedef void(^EmjBlock)(BOOL isSelect);
typedef void(^VoiceBtnSelectBlock)(BOOL isSelect);
typedef void(^MoreBtnBlock)(void);
typedef void(^VoiceBtnBlock)(void);
typedef void(^NoticeToBlock)(void);

@interface ZoloChatSendView : UIView

@property (nonatomic, copy) ReturnBlock returnBlock;
@property (nonatomic, copy) EmjBlock emjBlock;
@property (nonatomic, copy) VoiceBtnSelectBlock voiceBtnSelectBlock;
@property (nonatomic, copy) MoreBtnBlock moreBtnBlock;
@property (nonatomic, copy) VoiceBtnBlock voiceBtnBlock;
@property (nonatomic, assign) DMCCConversationType chatType;
@property (nonatomic, copy) NoticeToBlock noticeToBlock;
@property (nonatomic, strong) YYTextView *textView;

- (void)setEmojText:(NSString *)emoJString;
- (void)muteViewHidden:(BOOL)isHidden;
- (void)emojViewNomal;
- (NSString *)getTextFieldContent;

@end

NS_ASSUME_NONNULL_END
