//
//  ZoloForwardCell.m
//  NewZolo
//
//  Created by JTalking on 2022/8/16.
//

#import "ZoloForwardCell.h"

@implementation ZoloForwardCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    self.contentView.backgroundColor = [FontManage MHAlertColor];
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end
