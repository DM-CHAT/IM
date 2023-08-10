//
//  ZoloChatPushView.m
//  NewZolo
//
//  Created by MHHY on 2023/4/10.
//

#import "ZoloBindAcountView.h"

@interface ZoloBindAcountView ()

@property (nonatomic, assign) NSInteger type;

@end

@implementation ZoloBindAcountView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self = [[[NSBundle mainBundle] loadNibNamed:@"ZoloBindAcountView" owner:self options:nil] firstObject];
        [self setChatPushView];
    }
    return self;
}

- (void)setChatPushView {
    self.type = 1;
    self.phoneBtn.layer.cornerRadius = 4;
    self.phoneBtn.layer.masksToBounds = YES;
    self.emailBtn.layer.cornerRadius = 4;
    self.emailBtn.layer.masksToBounds = YES;
}

- (IBAction)phoneBtnClick:(id)sender {
    // 0x303239 灰色   0x05944F 绿色
    self.phoneBtn.backgroundColor = MHColorFromHex(0x05944F);
    self.emailBtn.backgroundColor = MHColorFromHex(0x303239);
    self.type = 1;
    self.phoneTextField.hidden = NO;
    self.phoneZoneTextField.hidden = NO;
    self.emailTextField.hidden = YES;
    
}

- (IBAction)emailBtnClick:(id)sender {
    self.emailBtn.backgroundColor = MHColorFromHex(0x05944F);
    self.phoneBtn.backgroundColor = MHColorFromHex(0x303239);
    self.type = 2;
    self.phoneTextField.hidden = YES;
    self.phoneZoneTextField.hidden = YES;
    self.emailTextField.hidden = NO;
    
}

- (IBAction)noticeBtnClick:(id)sender {
    if (self.type == 1) {
        if (self.phoneZoneTextField.text.length == 0) {
            [MHAlert showMessage:LocalizedString(@"ErrorInput")];
            return;
        }
        if (self.phoneTextField.text.length == 0) {
            [MHAlert showMessage:LocalizedString(@"ErrorInput")];
            return;
        }
        if (_bindPhoneBtnBlock) {
            _bindPhoneBtnBlock(self.phoneZoneTextField.text, self.phoneTextField.text);
        }
    } else {
        if (self.emailTextField.text.length == 0) {
            [MHAlert showMessage:LocalizedString(@"ErrorInput")];
            return;
        }
        if (_bindEmailBtnBlock) {
            _bindEmailBtnBlock(self.emailTextField.text);
        }
    }
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

@end
