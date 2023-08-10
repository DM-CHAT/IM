//
//  ZoloMyHeadView.m
//  NewZolo
//
//  Created by JTalking on 2022/6/29.
//

#import "ZoloConDetailHeadView.h"

@interface ZoloConDetailHeadView () <UIGestureRecognizerDelegate>


@end

@implementation ZoloConDetailHeadView

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    self.contentView.backgroundColor = [FontManage MHWhiteColor];
    self.icon.layer.cornerRadius = 5;
    self.icon.layer.masksToBounds = YES;
    
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(iconBtnClick)];
    self.icon.userInteractionEnabled = YES;
    [self.icon addGestureRecognizer:tap];
    self.nickName.textColor = [FontManage MHBlockColor];
    self.remark.userInteractionEnabled = YES;
    UILongPressGestureRecognizer *longPress = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(respondsToLongPress:)];
    longPress.delegate = self;
    [self.remark addGestureRecognizer:longPress];
}

- (void)iconBtnClick {
    if (_iconClickBlock) {
        _iconClickBlock();
    }
}

- (void)respondsToLongPress:(UILongPressGestureRecognizer *)sender {
    if (sender.state == UIGestureRecognizerStateBegan) {
        if (_longGesClickBlock) {
            _longGesClickBlock();
        }
    }
}


@end
