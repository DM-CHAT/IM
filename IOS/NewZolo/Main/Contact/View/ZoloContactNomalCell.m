//
//  ZoloContactNomalCell.m
//  NewZolo
//
//  Created by MHHY on 2023/5/16.
//

#import "ZoloContactNomalCell.h"

@interface ZoloContactNomalCell ()

@property (weak, nonatomic) IBOutlet UILabel *nomalTitle;

@end

@implementation ZoloContactNomalCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    
    self.nomalTitle.text = LocalizedString(@"EmptyPageContent");
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end
