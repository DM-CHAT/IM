//
//  ZoloVeriryPayView.m
//  NewZolo
//
//  Created by JTalking on 2022/9/8.
//

#import "ZoloVeriryPayView.h"
#import <CRBoxInputView/CRBoxInputView.h>
#import <CRBoxInputView/CRLineView.h>
#import <CRBoxInputView/CRSecrectImageView.h>
#import "IQKeyboardManager.h"

@interface ZoloVeriryPayView ()

@property (weak, nonatomic) IBOutlet UIView *codeView;
@property(nonatomic, strong) CRBoxInputView *boxInputView;
@property (weak, nonatomic) IBOutlet UILabel *payLab;

@end

@implementation ZoloVeriryPayView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self = [[[NSBundle mainBundle] loadNibNamed:@"ZoloVeriryPayView" owner:self options:nil] firstObject];
        self.frame = CGRectMake(0, 0, KScreenWidth, kScreenwidth/1.2);
        _boxInputView = [self generateBoxInputView_customBox];
        _boxInputView.frame = CGRectMake(0, 0, kScreenwidth - 60, 60);
        _boxInputView.ifNeedSecurity = YES;
        _boxInputView.securityDelay = 0.01;
        self.payLab.text = LocalizedString(@"win_MoneyPayLab");
        [self.codeView addSubview:_boxInputView];
        if (!_boxInputView.textDidChangeblock) {
            _boxInputView.ifNeedSecurity = YES;
            _boxInputView.textDidChangeblock = ^(NSString *text, BOOL isFinished) {
                if (isFinished) {
                    if (_veriryPayBlock) {
                        _veriryPayBlock(text);
                    }
                }
            };
        }
    }
    return self;
}

#pragma mark - CustomBox
- (CRBoxInputView *)generateBoxInputView_customBox
{
    CRBoxInputCellProperty *cellProperty = [CRBoxInputCellProperty new];
    cellProperty.cellBgColorNormal = MHColorFromHex(0xEDEDED);
    cellProperty.cellBgColorSelected = [UIColor whiteColor];
    cellProperty.cellCursorColor = MHColorFromHex(0xEDEDED);
    cellProperty.cellCursorWidth = 2;
    cellProperty.cellCursorHeight = 27;
    cellProperty.cornerRadius = 4;
    cellProperty.borderWidth = 0;
    cellProperty.ifShowSecurity = YES;
    cellProperty.cellFont = [UIFont boldSystemFontOfSize:16];
    cellProperty.cellTextColor = [UIColor blackColor];
    cellProperty.configCellShadowBlock = ^(CALayer * _Nonnull layer) {
        layer.shadowColor = MHColorFromHex(0xEDEDED).CGColor;
        layer.shadowOpacity = 1;
        layer.shadowOffset = CGSizeMake(0, 2);
        layer.shadowRadius = 4;
    };

    CRBoxInputView *_boxInputView = [[CRBoxInputView alloc] initWithCodeLength:6];
    _boxInputView.mainCollectionView.contentInset = UIEdgeInsetsMake(0, 10, 0, 10);
    _boxInputView.boxFlowLayout.itemSize = CGSizeMake(40, 40);
    _boxInputView.customCellProperty = cellProperty;
    [_boxInputView loadAndPrepareViewWithBeginEdit:YES];
    return _boxInputView;
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

@end
