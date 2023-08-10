//
//  ZoloGroupDetailHeadCell.m
//  NewZolo
//
//  Created by JTalking on 2022/8/12.
//

#import "ZoloGroupDetailHeadCell.h"

@interface ZoloGroupDetailHeadCell ()

@property (weak, nonatomic) IBOutlet UIImageView *iconImg;

@end

@implementation ZoloGroupDetailHeadCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code

    self.iconImg.layer.cornerRadius = 5;
    self.iconImg.layer.masksToBounds = YES;
}

@end
