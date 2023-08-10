//
//  ZoloChatPushView.h
//  NewZolo
//
//  Created by MHHY on 2023/4/10.
//

#import <UIKit/UIKit.h>

typedef void(^BindEmailBtnBlock)(NSString *str);
typedef void(^BindPhoneBtnBlock)(NSString *zone, NSString *phone);

NS_ASSUME_NONNULL_BEGIN

@interface ZoloBindAcountView : UIView


@property (weak, nonatomic) IBOutlet UILabel *noticeTitle;
@property (weak, nonatomic) IBOutlet UILabel *contentLab;
@property (weak, nonatomic) IBOutlet UIButton *emailBtn;
@property (weak, nonatomic) IBOutlet UIButton *phoneBtn;
@property (weak, nonatomic) IBOutlet UIButton *noticeBtn;

@property (weak, nonatomic) IBOutlet UITextField *phoneTextField;
@property (weak, nonatomic) IBOutlet UITextField *phoneZoneTextField;
@property (weak, nonatomic) IBOutlet UITextField *emailTextField;

@property (nonatomic, copy) BindEmailBtnBlock bindEmailBtnBlock;
@property (nonatomic, copy) BindPhoneBtnBlock bindPhoneBtnBlock;

@end

NS_ASSUME_NONNULL_END
