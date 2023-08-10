//
//  ZoloMyHeadView.m
//  NewZolo
//
//  Created by JTalking on 2022/6/29.
//

#import "ZoloPersonHeadView.h"

@interface ZoloPersonHeadView () <UIGestureRecognizerDelegate>

@property (weak, nonatomic) IBOutlet UIButton *editBtn;

@end

@implementation ZoloPersonHeadView

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    
    [self.editBtn setTitle:LocalizedString(@"ContactAlterEdit") forState:UIControlStateNormal];
    self.icon.layer.cornerRadius = 8;
    self.icon.layer.masksToBounds = YES;
    self.contentView.backgroundColor = [FontManage MHWhiteColor];
    self.nickName.textColor = [FontManage MHBlockColor];
    self.remark.userInteractionEnabled = YES;
    UILongPressGestureRecognizer *longPress = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(respondsToLongPress:)];
    longPress.delegate = self;
    [self.remark addGestureRecognizer:longPress];
}

- (void)respondsToLongPress:(UILongPressGestureRecognizer *)sender {
    if (sender.state == UIGestureRecognizerStateBegan) {
        if (_longGesClickBlock) {
            _longGesClickBlock();
        }
    }
}

- (IBAction)editBtnClick:(id)sender {
    
    if (_personEditBlock) {
        _personEditBlock();
    }
    
}


@end
