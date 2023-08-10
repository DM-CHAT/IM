//
//  ZoloChatPushView.m
//  NewZolo
//
//  Created by MHHY on 2023/4/10.
//

#import "ZoloChatPushView.h"

@implementation ZoloChatPushView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self = [[[NSBundle mainBundle] loadNibNamed:@"ZoloChatPushView" owner:self options:nil] firstObject];
        [self setChatPushView];
    }
    return self;
}

- (void)setChatPushView {
    self.noticeTitle.text = LocalizedString(@"ChatNoticeTitle");
    [self.noticeBtn setTitle:LocalizedString(@"ChatNoticeOk") forState:UIControlStateNormal];
}

- (IBAction)noticeBtnClick:(id)sender {
    if (_noticeBtnBlock) {
        _noticeBtnBlock();
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
