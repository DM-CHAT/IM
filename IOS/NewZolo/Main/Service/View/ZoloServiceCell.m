//
//  ZoloServiceCell.m
//  NewZolo
//
//  Created by JTalking on 2022/10/7.
//

#import "ZoloServiceCell.h"

@interface ZoloServiceCell ()


@end

@implementation ZoloServiceCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    
    self.layer.cornerRadius = 15;
    self.layer.masksToBounds = YES;
    self.iocn.layer.cornerRadius = 4;
    self.iocn.layer.masksToBounds = YES;
}

@end
