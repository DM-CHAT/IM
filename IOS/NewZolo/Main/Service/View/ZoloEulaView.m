//
//  ZoloChatPushView.m
//  NewZolo
//
//  Created by MHHY on 2023/4/10.
//

#import "ZoloEulaView.h"

@implementation ZoloEulaView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self = [[[NSBundle mainBundle] loadNibNamed:@"ZoloEulaView" owner:self options:nil] firstObject];
        [self setChatPushView];
    }
    return self;
}

- (void)setChatPushView {
    
    self.noBtn.layer.cornerRadius = 8;
    self.noBtn.layer.masksToBounds = YES;
    self.noticeBtn.layer.cornerRadius = 8;
    self.noticeBtn.layer.masksToBounds = YES;
    
    self.noticeTitle.text = LocalizedString(@"Alert");
    self.noticeContent.text = LocalizedString(@"EULAAgreement");
    [self.noticeBtn setTitle:LocalizedString(@"EULAAgreementYes") forState:UIControlStateNormal];
    [self.noBtn setTitle:LocalizedString(@"EULAAgreementNo") forState:UIControlStateNormal];
}

- (IBAction)noticeBtnClick:(id)sender {
    if (_noticeBtnBlock) {
        _noticeBtnBlock(YES);
    }
}

- (IBAction)noBtnClick:(id)sender {
    if (_noticeBtnBlock) {
        _noticeBtnBlock(NO);
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
