//
//  ZoloAcountLoginCell.m
//  NewZolo
//
//  Created by MHHY on 2023/5/20.
//

#import "ZoloAcountLoginCell.h"

@implementation ZoloAcountLoginCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    
    self.backgroundColor = [UIColor clearColor];
    self.nameLab.backgroundColor = MHColorFromHex(0x303239);
    self.layer.cornerRadius = 4;
    self.layer.masksToBounds = YES;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end
