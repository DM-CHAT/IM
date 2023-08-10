//
//  ZoloSettingFoot.m
//  NewZolo
//
//  Created by JTalking on 2022/9/15.
//

#import "ZoloSettingFoot.h"

@implementation ZoloSettingFoot

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    
    self.quitBtn.backgroundColor = [FontManage MHAlertColor];
    [self.quitBtn setTitleColor:[FontManage MHTitleSubColor] forState:UIControlStateNormal];
}

- (IBAction)quiteBtnBlock:(id)sender {
    if (_quitBlock) {
        _quitBlock();
    }
}


- (IBAction)deleteBtnClick:(id)sender {
    if (_deleteBlock) {
        _deleteBlock();
    }
}


@end
