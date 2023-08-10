//
//  ZoloGroupDetailFoot.m
//  NewZolo
//
//  Created by JTalking on 2022/7/11.
//

#import "ZoloGroupDetailFoot.h"

@implementation ZoloGroupDetailFoot

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    self.contentView.backgroundColor = [FontManage MHLineSeparatorColor];
}

- (IBAction)clearChatBtnClick:(id)sender {
    
    if (_clearChatBlock) {
        _clearChatBlock();
    }
    
}

- (IBAction)deleteBtnClick:(id)sender {
    
    if (_deleteChatBlock) {
        _deleteChatBlock();
    }
    
}

- (IBAction)jubaoBtnClick:(id)sender {
    if (_complaintChatBlock) {
        _complaintChatBlock();
    }
}


@end
