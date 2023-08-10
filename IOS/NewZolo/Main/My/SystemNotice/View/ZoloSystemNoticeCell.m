//
//  ZoloSystemNoticeCell.m
//  NewZolo
//
//  Created by MHHY on 2023/4/10.
//

#import "ZoloSystemNoticeCell.h"

@interface ZoloSystemNoticeCell ()

@end

@implementation ZoloSystemNoticeCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    
    self.redRemark.layer.cornerRadius = 6;
    self.redRemark.layer.masksToBounds = YES;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end
