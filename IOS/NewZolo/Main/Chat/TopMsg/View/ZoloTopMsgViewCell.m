//
//  ZoloKeywordCell.m
//  NewZolo
//
//  Created by JTalking on 2022/10/29.
//

#import "ZoloTopMsgViewCell.h"

@implementation ZoloTopMsgViewCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    self.titleLab.text = LocalizedString(@"SetingKeyword");
    self.idLab.layer.cornerRadius = 10;
    self.idLab.layer.masksToBounds = YES;
    self.timeLab.textColor = [FontManage MHTitleSubColor];
    self.nameLab.textColor = [FontManage MHBlockColor];
}

- (IBAction)delBtnClick:(id)sender {
    if (_delBtnBlock) {
        _delBtnBlock();
    }
}


@end
