//
//  ZoloNewFriendCell.m
//  NewZolo
//
//  Created by JTalking on 2022/6/30.
//

#import "ZoloNewFriendCell.h"

@interface ZoloNewFriendCell ()

@property (weak, nonatomic) IBOutlet UIImageView *icon;
@property (weak, nonatomic) IBOutlet UILabel *nickName;
@property (weak, nonatomic) IBOutlet UILabel *phoneTitle;
@property (weak, nonatomic) IBOutlet UIButton *addBtn;

@end

@implementation ZoloNewFriendCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
}

- (void)setInfo:(DMCCFriendRequest *)info {
    _info = info;
    if (info.type == 0) {
        DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:info.target refresh:NO];
        self.nickName.text = userInfo.displayName;
        [self.icon sd_setImageWithURL:[NSURL URLWithString:userInfo.portrait] placeholderImage:SDImageDefault];
        self.phoneTitle.text = info.reason;
        if (info.status == 1) {
            [self.addBtn setTitle:LocalizedString(@"NewFriendAdded") forState:UIControlStateNormal];
        } else {
            [self.addBtn setTitle:LocalizedString(@"NewFriendAdd") forState:UIControlStateNormal];
        }
        UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tapClick)];
        [self.icon addGestureRecognizer:tap];
    } else {
        DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:info.userID refresh:NO];
        DMCCGroupInfo *groupInfo = [[DMCCIMService sharedDMCIMService] getGroupInfo:info.target refresh:NO];
        self.nickName.text = userInfo.displayName;
        [self.icon sd_setImageWithURL:[NSURL URLWithString:userInfo.portrait] placeholderImage:SDImageDefault];
        self.phoneTitle.text = [NSString stringWithFormat:LocalizedString(@"ApplyAddGroup"), userInfo.displayName, groupInfo.name];
        if (info.status == 1) {
            [self.addBtn setTitle:LocalizedString(@"NewFriendAdded") forState:UIControlStateNormal];
        } else {
            [self.addBtn setTitle:LocalizedString(@"NewFriendAdd") forState:UIControlStateNormal];
        }
        UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tapClick)];
        [self.icon addGestureRecognizer:tap];
    }
   
}

- (void)tapClick {
    if (_iconBtnBlock) {
        _iconBtnBlock();
    }
}

- (IBAction)addUserBtnClick:(id)sender {
    // 已添加不用回调
    if (self.info.status == 1) {
        return;
    }
    if (_userBtnBlock) {
        _userBtnBlock();
    }
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end
