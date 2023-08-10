//
//  ZoloMyCell.m
//  NewZolo
//
//  Created by JTalking on 2022/6/29.
//

#import "ZoloWalletCell.h"

@implementation ZoloWalletCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    
    self.redStatus.layer.cornerRadius = 5;
    self.redStatus.layer.masksToBounds = YES;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end
