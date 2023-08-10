//
//  ZoloChatDetailFootView.m
//  NewZolo
//
//  Created by JTalking on 2022/8/15.
//

#import "ZoloChatDetailFootView.h"

@implementation ZoloChatDetailFootView

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    self.contentView.backgroundColor = [FontManage MHGrayColor];
    [self.deleteBtn setTitle:LocalizedString(@"DeleteContant") forState:UIControlStateNormal];
    
    self.deleteBtn.layer.cornerRadius = 4;
    self.deleteBtn.layer.masksToBounds = YES;
}

- (IBAction)deleteBtnClick:(id)sender {
    if (_deleteChatBlock) {
        _deleteChatBlock();
    }
}


@end
