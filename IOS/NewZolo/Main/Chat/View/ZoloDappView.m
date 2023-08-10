//
//  ZoloDappView.m
//  NewZolo
//
//  Created by JTalking on 2022/8/30.
//

#import "ZoloDappView.h"

@interface ZoloDappView ()

@property (weak, nonatomic) IBOutlet UIImageView *dappIcon;
@property (weak, nonatomic) IBOutlet UIImageView *offImg;
@property (weak, nonatomic) IBOutlet UIView *onView;

@property (weak, nonatomic) IBOutlet UIView *offView;

@property (nonatomic, strong) DMCCLitappInfo *appInfo;

@end

@implementation ZoloDappView

- (instancetype)initWithLitappInfo:(DMCCLitappInfo *)appInfo showNotice:(BOOL)isNotice {
    if (self = [super initWithFrame:CGRectZero]) {
        self = [[[NSBundle mainBundle] loadNibNamed:@"ZoloDappView" owner:self options:nil] firstObject];
        int height = 100;
        if (isNotice) {
            height += 64;
        }
        self.frame = CGRectMake(0, height, kScreenwidth, 68);
        [self setTapView];
        self.appInfo = appInfo;
        [self.dappIcon sd_setImageWithURL:[NSURL URLWithString:appInfo.portrait] placeholderImage:SDImageDefault];
    }
    return self;
}

- (void)setTapView {
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(offViewHideClick)];
    [self.offView addGestureRecognizer:tap];
    
    UITapGestureRecognizer *tap1 = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onViewHideClick)];
    self.offImg.userInteractionEnabled = YES;
    [self.offImg addGestureRecognizer:tap1];
    
    UITapGestureRecognizer *tap2 = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(dappIconClick)];
    self.dappIcon.userInteractionEnabled = YES;
    [self.dappIcon addGestureRecognizer:tap2];
}

- (void)offViewHideClick {
    self.offView.hidden = YES;
    self.onView.hidden = NO;
}

- (void)onViewHideClick {
    self.offView.hidden = NO;
    self.onView.hidden = YES;
}

- (void)dappIconClick {
    [self onViewHideClick];
    if (_dappBlock) {
        _dappBlock();
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
