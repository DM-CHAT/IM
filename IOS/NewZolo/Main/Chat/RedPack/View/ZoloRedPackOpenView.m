//
//  ZoloRedPackView.m
//  NewZolo
//
//  Created by JTalking on 2022/9/9.
//

#import "ZoloRedPackOpenView.h"

@implementation ZoloRedPackOpenView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self = [[[NSBundle mainBundle] loadNibNamed:@"ZoloRedPackOpenView" owner:self options:nil] firstObject];
        
        self.iconImg.layer.masksToBounds = YES;
        self.iconImg.layer.cornerRadius = 2;
        self.typeLab.text = LocalizedString(@"ConRedPack");
        self.seeDetailLab.text = LocalizedString(@"RedPackSeeDetail");
        self.redPackOverLab.text = LocalizedString(@"RedPackGetOver");
        UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tapClick)];
        [self addGestureRecognizer:tap];
    }
    return self;
}

- (void)tapClick {
    if (_redPackCancelBlcok) {
        _redPackCancelBlcok();
    }
}

- (IBAction)seeDetailBtnClick:(id)sender {
    if (_redPackDetailBlcok) {
        _redPackDetailBlcok();
    }
}

- (IBAction)btnCloseClick:(id)sender {
    if (_redPackCancelBlcok) {
        _redPackCancelBlcok();
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
