//
//  ZoloMyHeadView.m
//  NewZolo
//
//  Created by JTalking on 2022/6/29.
//

#import "ZoloMyHeadView.h"

@interface ZoloMyHeadView () <UIGestureRecognizerDelegate>

@property (weak, nonatomic) IBOutlet UIView *grayView;


@end

@implementation ZoloMyHeadView

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    self.icon.layer.cornerRadius = 5;
    self.icon.layer.masksToBounds = YES;
    self.icon.userInteractionEnabled = YES;
    self.nickName.textColor = [FontManage MHBlockColor];
    self.contentView.backgroundColor = [FontManage MHWhiteColor];
    self.grayView.backgroundColor = [FontManage MHGrayColor];
    self.remark.userInteractionEnabled = YES;
    UITapGestureRecognizer *longPress = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tapClick)];
    [self.icon addGestureRecognizer:longPress];
}

- (void)tapClick {
    if (_longGesClickBlock) {
        _longGesClickBlock();
    }
}

- (IBAction)qrBtnClick:(id)sender {
    
    if (_headBtnBlock) {
        _headBtnBlock(10);
    }
    
}

- (IBAction)userInfoBtnClick:(id)sender {
    
    if (_headBtnBlock) {
        _headBtnBlock(11);
    }
    
}

@end
