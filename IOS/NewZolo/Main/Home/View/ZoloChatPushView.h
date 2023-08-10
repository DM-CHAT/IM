//
//  ZoloChatPushView.h
//  NewZolo
//
//  Created by MHHY on 2023/4/10.
//

#import <UIKit/UIKit.h>

typedef void(^NoticeBtnBlock)();
NS_ASSUME_NONNULL_BEGIN

@interface ZoloChatPushView : UIView


@property (weak, nonatomic) IBOutlet UILabel *noticeTitle;
@property (weak, nonatomic) IBOutlet UILabel *noticeContent;
@property (weak, nonatomic) IBOutlet UIButton *noticeBtn;
@property (nonatomic, copy) NoticeBtnBlock noticeBtnBlock;

@end

NS_ASSUME_NONNULL_END
