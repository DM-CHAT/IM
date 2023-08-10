//
//  ZoloHomeAddCell.m
//  NewZolo
//
//  Created by JTalking on 2022/7/7.
//

#import "ZoloHomeAddCell.h"

@implementation ZoloHomeAddCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    
    self.nameLabel.textColor = [UIColor whiteColor];
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end
