//
//  ZoloGroupDetailCell.m
//  NewZolo
//
//  Created by JTalking on 2022/7/11.
//

#import "ZoloGroupDetailCell.h"

@implementation ZoloGroupDetailCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
}

- (IBAction)switchBtnClick:(UISwitch *)sender {
    if (_topSwitchBlock) {
        _topSwitchBlock(sender.on);
    }
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end
