//
//  ZoloContactDetailHead.m
//  NewZolo
//
//  Created by JTalking on 2022/7/6.
//

#import "ZoloContactDetailFoot.h"

@implementation ZoloContactDetailFoot

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    self.contentView.backgroundColor = [FontManage MHGrayColor];
    
    self.deleteUserBtn.layer.cornerRadius = 4;
    self.deleteUserBtn.layer.masksToBounds = YES;
    self.sendMsgBtn.layer.cornerRadius = 4;
    self.sendMsgBtn.layer.masksToBounds = YES;
}

- (IBAction)sendMessageClick:(id)sender {
    if (_sendMessageBlock) {
        _sendMessageBlock();
    }
}

- (IBAction)deleteUserClick:(id)sender {
    if (_deleteUserBlock) {
        _deleteUserBlock();
    }
}


@end
