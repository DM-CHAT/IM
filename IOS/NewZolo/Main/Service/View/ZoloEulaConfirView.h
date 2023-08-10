//
//  ZoloChatPushView.h
//  NewZolo
//
//  Created by MHHY on 2023/4/10.
//

#import <UIKit/UIKit.h>

typedef void(^NoticeConfirmBtnBlock)(BOOL isAgree);
NS_ASSUME_NONNULL_BEGIN

@interface ZoloEulaConfirView : UIView


@property (weak, nonatomic) IBOutlet UILabel *noticeTitle;
@property (weak, nonatomic) IBOutlet UILabel *noticeContent;
@property (weak, nonatomic) IBOutlet UIButton *noticeBtn;
@property (weak, nonatomic) IBOutlet UIButton *noBtn;

@property (nonatomic, copy) NoticeConfirmBtnBlock noticeBtnBlock;

@end

NS_ASSUME_NONNULL_END
