//
//  ZoloAddFriendInfoFoot.m
//  NewZolo
//
//  Created by JTalking on 2022/8/24.
//

#import "ZoloAddFriendInfoFoot.h"

@implementation ZoloAddFriendInfoFoot

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    
    [self.addBtn setTitle:LocalizedString(@"ChatAddContact") forState:UIControlStateNormal];
}

- (IBAction)addBtnClick:(id)sender {
    if (_addBtnBLock) {
        _addBtnBLock();
    }
}


@end
