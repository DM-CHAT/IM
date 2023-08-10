//
//  ZoloContactHeadCell.m
//  NewZolo
//
//  Created by JTalking on 2022/6/30.
//

#import "ZoloContactHeadCell.h"

@implementation ZoloContactHeadCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    
    self.unRedCount.layer.cornerRadius = 5;
    self.unRedCount.layer.masksToBounds = YES;
    
}

@end
