//
//  ZoloRedPackRecordCell.m
//  NewZolo
//
//  Created by JTalking on 2022/9/9.
//

#import "ZoloRedPackRecordCell.h"

@implementation ZoloRedPackRecordCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    self.imgIcon.layer.cornerRadius = 2;
    self.imgIcon.layer.masksToBounds = YES;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end
