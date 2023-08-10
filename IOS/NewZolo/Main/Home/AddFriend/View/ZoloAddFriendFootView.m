//
//  ZoloAddFriendFootView.m
//  NewZolo
//
//  Created by JTalking on 2022/8/24.
//

#import "ZoloAddFriendFootView.h"

@implementation ZoloAddFriendFootView

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    [self.addContactBtn setTitle:LocalizedString(@"SearchTitle") forState:UIControlStateNormal];
    self.addContactBtn.backgroundColor = [FontManage MHLineSeparatorColor];
}

- (IBAction)addBtnClick:(id)sender {
    
    if (_adddFriendBlock) {
        _adddFriendBlock();
    }
    
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    
    if (_adddFriendBlock) {
        _adddFriendBlock();
    }
    
}

@end
