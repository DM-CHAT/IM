//
//  ZoloGroupCell.m
//  NewZolo
//
//  Created by JTalking on 2022/6/30.
//

#import "ZoloContactGroupCell.h"

@implementation ZoloContactGroupCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    self.icon.layer.cornerRadius = 8;
    self.icon.layer.masksToBounds = YES;
    self.titleName.textColor = [FontManage MHBlockColor];
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end
