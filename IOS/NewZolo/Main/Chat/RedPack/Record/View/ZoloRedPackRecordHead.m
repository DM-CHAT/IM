//
//  ZoloRedPackRecordHead.m
//  NewZolo
//
//  Created by JTalking on 2022/9/9.
//

#import "ZoloRedPackRecordHead.h"

@implementation ZoloRedPackRecordHead

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    self.transtagLab.text = LocalizedString(@"win_TransforLab");
    self.redLab.text = LocalizedString(@"win_RendSendLab");
    self.redName.text = LocalizedString(@"ChatRedSendLab");
    self.userIcon.layer.masksToBounds = YES;
    self.userIcon.layer.cornerRadius = 25;
    self.walletIcon.layer.cornerRadius = 4;
    self.walletIcon.layer.masksToBounds = YES;
    self.redView.backgroundColor = [FontManage MHWhiteColor];
    self.otherView.backgroundColor = [FontManage MHWhiteColor];
    self.grayView.backgroundColor = [FontManage MHLineSeparatorColor];
    
    self.redName.textColor = [FontManage MHTitleSubColor];
    self.winB.textColor = [FontManage MHTitleSubColor];
    self.winNum.textColor = [FontManage MHTitleSubColor];
    self.winMoney.textColor = [FontManage MHTitleSubColor];
}

- (IBAction)backBtnClick:(id)sender {
    if (_backBlock) {
        _backBlock();
    }
}

- (IBAction)walletBtnClick:(id)sender {
    if (_walletBtnBlock) {
        _walletBtnBlock();
    }
}


@end
