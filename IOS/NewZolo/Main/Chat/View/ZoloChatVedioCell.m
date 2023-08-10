//
//  ZoloChatVedioCell.m
//  NewZolo
//
//  Created by JTalking on 2022/7/12.
//

#import "ZoloChatVedioCell.h"
#import "DMCUUtilities.h"
#import "DMCUMediaMessageDownloader.h"

@implementation ZoloChatVedioCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    self.iconImg.layer.masksToBounds = YES;
    self.iconImg.layer.cornerRadius = 2;
    
    self.vedioView.layer.masksToBounds = YES;
    self.vedioView.layer.cornerRadius = 5;
    [self.vedioView addGestureRecognizer:self.tapGes];
    self.iconImg.userInteractionEnabled = YES;
    [self.iconImg addGestureRecognizer:self.iconTapGes];
    [self.iconImg addGestureRecognizer:self.longIconGes];
    [self.vedioView addGestureRecognizer:self.longGes];
    
    self.iconLeftMargin.constant = 15;
    self.mulView.hidden = YES;
    [self.mulView addGestureRecognizer:self.mulTapGes];
}

+ (void)calculateCellHeight:(DMCCMessage *)message chatType:(BOOL)isGroup mulSelect:(BOOL)isMulSelect byMsg:(BOOL)isMySend {
    message.msgCellHeight = 160 + 34;
    message.msgCellWidth = 130;
}

- (void)setMessage:(DMCCMessage *)message {
    [super setMessage:message];
    NSString *homeStr = NSHomeDirectory();
    DMCCVideoMessageContent *videoContent = (DMCCVideoMessageContent *)message.content;
    if (videoContent.thumbnail == nil && videoContent.localPath.length > 0) {
        videoContent.thumbnail = [MHHelperUtils getFileThumbnailImage:videoContent.localPath];
    }
    if (videoContent.localPath.length > 0) {
        if ([videoContent.localPath containsString:homeStr]) {
            self.thumBg.image = videoContent.thumbnail;
        } else {
            self.thumBg.image = [UIImage imageNamed:@"video_bg"];
        }
    } else {
        self.thumBg.image = [UIImage imageNamed:@"video_bg"];
        if ([videoContent.remoteUrl hasSuffix:@".zip"] && videoContent.decKey.length > 0) {
            self.thumBg.image = [UIImage imageNamed:@"deckey"];
        } else if ([videoContent.remoteUrl hasSuffix:@".zip"] && videoContent.decKey.length == 0) {
            self.thumBg.image = [UIImage imageNamed:@"file_damage"];
        }
    }
  
    if ([message.fromUser isEqualToString:self.myInfo.userId]) {
        self.msgTimeLabel.textAlignment = NSTextAlignmentRight;
        if (self.chatType == Single_Type) {
            self.iconImg.hidden = YES;
            self.iconBgImg.hidden = YES;
            self.rightMargin.constant = -30;
            self.msgTimeLabel.frame = CGRectMake(-24, message.msgCellHeight + 4, kScreenwidth, 20);
        } else {
            self.iconImg.hidden = NO;
            self.iconBgImg.hidden = NO;
            self.rightMargin.constant = 4;
            [self.iconImg sd_setImageWithURL:[NSURL URLWithString:self.myInfo.portrait] placeholderImage:SDImageDefault];
            self.msgTimeLabel.frame = CGRectMake(-62, message.msgCellHeight + 4, kScreenwidth, 20);
            DMCCGroupMember *gm = [[DMCCIMService sharedDMCIMService] getGroupMember:self.conversation.target memberId:self.myInfo.userId];
            if (gm.type == Member_Type_Owner) {
                self.iconBgImg.image = [UIImage imageNamed:@"member_main"];
            } else if (gm.type == Member_Type_Manager) {
                self.iconBgImg.image = [UIImage imageNamed:@"member_manager"];
            } else {
                self.iconBgImg.image = [UIImage imageNamed:@"member_nomal"];
            }
            NSString *nftStr = [DMCCUserInfo getNft:self.myInfo.describes];
            if (nftStr.length > 0) {
                self.nftImg.hidden = NO;
            } else {
                self.nftImg.hidden = YES;
            }
        }
   
    } else {
        self.msgTimeLabel.textAlignment = NSTextAlignmentLeft;
        if (self.chatType == Group_Type) {
            self.iconImg.hidden = NO;
            self.iconBgImg.hidden = NO;
            self.leftMargin.constant = 4;
            DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:message.fromUser refresh:NO];
            [self.iconImg sd_setImageWithURL:[NSURL URLWithString:userInfo.portrait] placeholderImage:SDImageDefault];
            self.msgTimeLabel.frame = CGRectMake(62, message.msgCellHeight + 4, kScreenwidth, 20);
            DMCCGroupMember *gm = [[DMCCIMService sharedDMCIMService] getGroupMember:self.conversation.target memberId:userInfo.userId];
            if (gm.type == Member_Type_Owner) {
                self.iconBgImg.image = [UIImage imageNamed:@"member_main"];
            } else if (gm.type == Member_Type_Manager) {
                self.iconBgImg.image = [UIImage imageNamed:@"member_manager"];
            } else {
                self.iconBgImg.image = [UIImage imageNamed:@"member_nomal"];
            }
            NSString *nftStr = [DMCCUserInfo getNft:userInfo.describes];
            if (nftStr.length > 0) {
                self.nftImg.hidden = NO;
            } else {
                self.nftImg.hidden = YES;
            }
        } else {
            self.iconImg.hidden = YES;
            self.iconBgImg.hidden = YES;
            self.leftMargin.constant = -30;
            [self.iconImg sd_setImageWithURL:[NSURL URLWithString:self.userInfo.portrait] placeholderImage:SDImageDefault];
            self.msgTimeLabel.frame = CGRectMake(24, message.msgCellHeight + 4, kScreenwidth, 20);
        }
    }
    self.nickNameLabel.frame = CGRectMake(62, message.msgCellHeight + 4, kScreenwidth, 20);
    self.errorImg.frame = CGRectMake(kScreenwidth - message.msgCellWidth - 20 - 60, message.msgCellHeight / 2 - 10 , 20, 20);
    
    if (self.isMulSelect) {
        self.iconLeftMargin.constant = 50;
        self.mulView.hidden = NO;
        self.mulBtn.selected = message.isMulSelect;
    } else {
        self.iconLeftMargin.constant = 15;
        self.mulView.hidden = YES;
    }
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end
