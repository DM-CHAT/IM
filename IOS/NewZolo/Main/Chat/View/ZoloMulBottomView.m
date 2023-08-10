//
//  ZoloMulBottomView.m
//  NewZolo
//
//  Created by MHHY on 2023/5/9.
//

#import "ZoloMulBottomView.h"

@implementation ZoloMulBottomView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:CGRectZero]) {
        self = [[[NSBundle mainBundle] loadNibNamed:@"ZoloMulBottomView" owner:self options:nil] firstObject];
        self.frame = CGRectMake(0, KScreenheight - 55 - iPhoneX_topH, kScreenwidth, 55);
    }
    return self;
}

- (IBAction)mulDelBtnClick:(id)sender {
    if (_mulDelBlock) {
        _mulDelBlock();
    }
}

- (IBAction)mulForwardBtnClick:(id)sender {
    if (_mulForwardBlock) {
        _mulForwardBlock();
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
