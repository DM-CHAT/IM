//
//  ZoloMulTopView.m
//  NewZolo
//
//  Created by MHHY on 2023/5/9.
//

#import "ZoloMulTopView.h"

@interface ZoloMulTopView ()

@property (weak, nonatomic) IBOutlet UIButton *cancelBtn;

@end

@implementation ZoloMulTopView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:CGRectZero]) {
        self = [[[NSBundle mainBundle] loadNibNamed:@"ZoloMulTopView" owner:self options:nil] firstObject];
        self.frame = CGRectMake(0, 0, kScreenwidth, 64 + iPhoneX_topH);
        [self.cancelBtn setTitle:LocalizedString(@"Cancel") forState:UIControlStateNormal];
    }
    return self;
}

- (IBAction)cancelBtnClick:(id)sender {
    if (_mulCancelBlock) {
        _mulCancelBlock();
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
