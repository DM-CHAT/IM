//
//  ZoloCardView.m
//  NewZolo
//
//  Created by JTalking on 2022/8/5.
//

#import "ZoloCardView.h"

@interface ZoloCardView ()

@property (weak, nonatomic) IBOutlet UITextField *textField;

@end

@implementation ZoloCardView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self = [[[NSBundle mainBundle] loadNibNamed:@"ZoloCardView" owner:self options:nil] firstObject];
        self.frame = CGRectMake(40, 0, KScreenWidth - 80, 225);
    }
    return self;
}

- (IBAction)sendBtnClick:(id)sender {
    if (_okBtnClick) {
        _okBtnClick(self.textField.text);
    }
}

- (IBAction)cancelBtnClick:(id)sender {
    if (_cancelBtnClick) {
        _cancelBtnClick();
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
