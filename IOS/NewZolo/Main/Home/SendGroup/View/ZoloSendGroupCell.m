//
//  ZoloSendGroupCell.m
//  NewZolo
//
//  Created by JTalking on 2022/7/8.
//

#import "ZoloSendGroupCell.h"

@interface ZoloSendGroupCell ()

@property (weak, nonatomic) IBOutlet UIImageView *icon;
@property (weak, nonatomic) IBOutlet UILabel *nickName;
@property (weak, nonatomic) IBOutlet UILabel *subName;
@property (weak, nonatomic) IBOutlet UIImageView *selectImg;

@end

@implementation ZoloSendGroupCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
}

- (void)setUserInfo:(DMCCUserInfo *)userInfo {
    _userInfo = userInfo;
    self.nickName.text = userInfo.displayName;
    [self.icon sd_setImageWithURL:[NSURL URLWithString:userInfo.portrait] placeholderImage:SDImageDefault];
    self.selectImg.image = userInfo.isSelect ? [UIImage imageNamed:@"select_s"] : [UIImage imageNamed:@"select_n"];
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end
