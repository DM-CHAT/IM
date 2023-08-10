//
//  ZoloSearchHeadView.m
//  NewZolo
//
//  Created by MHHY on 2023/3/9.
//

#import "ZoloSearchHeadView.h"

@interface ZoloSearchHeadView () <UITextFieldDelegate>

@property (weak, nonatomic) IBOutlet UITextField *textField;
@property (weak, nonatomic) IBOutlet UIView *searchView;

@end

@implementation ZoloSearchHeadView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self = [[[NSBundle mainBundle] loadNibNamed:@"ZoloSearchHeadView" owner:self options:nil] firstObject];
        self.frame = CGRectMake(-20, 0, KScreenWidth, 100);
        self.textField.placeholder = LocalizedString(@"SearchTitle");
        self.searchView.layer.cornerRadius = 20;
        self.searchView.layer.masksToBounds = YES;
        self.searchView.backgroundColor = [FontManage MHLineSeparatorColor];
        self.textField.delegate = self;
    }
    return self;
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    if (_searchBlock) {
        _searchBlock(textField.text);
    }
    return YES;
}


/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

@end
