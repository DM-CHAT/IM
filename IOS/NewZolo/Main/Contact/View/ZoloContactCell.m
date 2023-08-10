//
//  ZoloContactCell.m
//  NewZolo
//
//  Created by JTalking on 2022/6/30.
//

#import "ZoloContactCell.h"

@interface ZoloContactCell ()

@property (weak, nonatomic) IBOutlet UIImageView *icon;
@property (weak, nonatomic) IBOutlet UILabel *nickName;
@property (weak, nonatomic) IBOutlet UILabel *subName;
@property (weak, nonatomic) IBOutlet UIImageView *nftImg;

@end

@implementation ZoloContactCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    
    self.icon.layer.cornerRadius = 2;
    self.icon.layer.masksToBounds = YES;
}

- (void)setUserInfo:(DMCCUserInfo *)userInfo {
    _userInfo = userInfo;
    self.nickName.text = userInfo.displayName;
    [self.icon sd_setImageWithURL:[NSURL URLWithString:userInfo.portrait] placeholderImage:SDImageDefault];
    self.subName.text = userInfo.userId;
    NSString *alias = [[DMCCIMService sharedDMCIMService] getFriendAlias:userInfo.userId];
    if (alias.length > 0) {
        self.nickName.text = alias;
    }
    NSString *nftStr = [DMCCUserInfo getNft:userInfo.describes];
    if (nftStr.length > 0) {
        self.nftImg.hidden = NO;
    } else {
        self.nftImg.hidden = YES;
    }
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end
