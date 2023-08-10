//
//  ZoloKeywordCell.m
//  NewZolo
//
//  Created by JTalking on 2022/10/29.
//

#import "ZoloKeywordCell.h"

@implementation ZoloKeywordCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    self.titleLab.text = LocalizedString(@"SetingKeyword");
    self.idLab.layer.cornerRadius = 10;
    self.idLab.layer.masksToBounds = YES;
}

- (IBAction)delBtnClick:(id)sender {
    if (_delBtnBlock) {
        _delBtnBlock();
    }
}


@end
