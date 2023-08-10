//
//  ZoloChatImageMsgCell.m
//  NewZolo
//
//  Created by JTalking on 2022/7/12.
//

#import "ZoloChatImageMsgCell.h"

@implementation ZoloChatImageMsgCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    self.iconImg.layer.masksToBounds = YES;
    self.iconImg.layer.cornerRadius = 2;
    self.showImg.layer.masksToBounds = YES;
    self.showImg.layer.cornerRadius = 5;
    self.showImg.userInteractionEnabled = YES;
    [self.showImg addGestureRecognizer:self.tapGes];
    self.iconImg.userInteractionEnabled = YES;
    [self.iconImg addGestureRecognizer:self.iconTapGes];
    [self.iconImg addGestureRecognizer:self.longIconGes];
    self.showImg.userInteractionEnabled = YES;
    [self.showImg addGestureRecognizer:self.longGes];
    
    self.iconLeftMargin.constant = 15;
    self.mulView.hidden = YES;
    [self.mulView addGestureRecognizer:self.mulTapGes];
}

+ (void)calculateCellHeight:(DMCCMessage *)message chatType:(BOOL)isGroup mulSelect:(BOOL)isMulSelect byMsg:(BOOL)isMySend {
    CGSize size = CGSizeMake(0, 0);
    if ([message.content isKindOfClass:[DMCCImageMessageContent class]]) {
        size = [MHHelperUtils sizeForClientArea:message withViewWidth:KScreenWidth - 120];
    } else {
        size = [MHHelperUtils sizeForStickerClientArea:message withViewWidth:KScreenWidth - 120];
    }
    message.msgCellWidth = size.width;
    message.msgCellHeight = size.height + 20;
}

- (void)setMessage:(DMCCMessage *)message {
    [super setMessage:message];
    NSString *homeStr = NSHomeDirectory();
    if ([message.content isKindOfClass:[DMCCImageMessageContent class]]) {
        DMCCImageMessageContent *imgContent = (DMCCImageMessageContent *)message.content;
        if ([message.fromUser isEqualToString:self.myInfo.userId]) {
            if (imgContent.localPath.length > 0) {
                self.showImg.image = [UIImage imageNamed:imgContent.localPath];
            } else {
                if ([imgContent.remoteUrl hasSuffix:@".zip"] && imgContent.decKey.length > 0) {
                    self.showImg.image = [UIImage imageNamed:@"deckey"];
                } else if ([imgContent.remoteUrl hasSuffix:@".zip"] && imgContent.decKey.length == 0) {
                    self.showImg.image = [UIImage imageNamed:@"file_damage"];
                } else {
                    self.showImg.image = [UIImage imageNamed:@"chatImgBg"];
                }
            }
        } else {
            if (imgContent.localPath.length > 0) {
                if ([imgContent.localPath containsString:homeStr]) {
                    self.showImg.image = [UIImage imageNamed:imgContent.localPath];
                } else {
                    [self.showImg sd_setImageWithURL:[NSURL URLWithString:imgContent.remoteUrl] placeholderImage:[UIImage imageNamed:@"chatImgBg"]];
                }
            } else {
                [self.showImg sd_setImageWithURL:[NSURL URLWithString:imgContent.remoteUrl] placeholderImage:[UIImage imageNamed:@"chatImgBg"]];
                if ([imgContent.remoteUrl hasSuffix:@".zip"] && imgContent.decKey.length > 0) {
                    self.showImg.image = [UIImage imageNamed:@"deckey"];
                } else if ([imgContent.remoteUrl hasSuffix:@".zip"] && imgContent.decKey.length == 0) {
                    self.showImg.image = [UIImage imageNamed:@"file_damage"];
                }
            }
        }
        CGSize size = [MHHelperUtils sizeForClientArea:message withViewWidth:KScreenWidth - 120];
        self.imgW.constant = size.width;
        self.imgH.constant = size.height;
    } else {
        DMCCStickerMessageContent *imgContent = (DMCCStickerMessageContent *)message.content;
        if ([message.fromUser isEqualToString:self.myInfo.userId]) {
            self.showImg.image = [UIImage imageNamed:imgContent.localPath];
        } else {
            if (imgContent.localPath.length > 0) {
                if ([imgContent.localPath containsString:homeStr]) {
                    self.showImg.image = [UIImage imageNamed:imgContent.localPath];
                } else {
                    [self.showImg sd_setImageWithURL:[NSURL URLWithString:imgContent.remoteUrl] placeholderImage:[UIImage imageNamed:@"chatImgBg"]];
                }
            } else {
                [self.showImg sd_setImageWithURL:[NSURL URLWithString:imgContent.remoteUrl] placeholderImage:[UIImage imageNamed:@"chatImgBg"]];
            }
        }
        CGSize size = [MHHelperUtils sizeForStickerClientArea:message withViewWidth:KScreenWidth - 120];
        self.imgW.constant = size.width;
        self.imgH.constant = size.height;
    }
    
    if ([message.fromUser isEqualToString:self.myInfo.userId]) {
        self.msgTimeLabel.textAlignment = NSTextAlignmentRight;
        if (self.chatType == Single_Type) {
            self.iconImg.hidden = YES;
            self.iconBgImg.hidden = YES;
            self.meRightMargin.constant = -30;
            self.msgTimeLabel.frame = CGRectMake(-24, message.msgCellHeight + 4, kScreenwidth, 20);
        } else {
            self.iconImg.hidden = NO;
            self.iconBgImg.hidden = NO;
            self.meRightMargin.constant = 4;
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
            self.leftMagin.constant = 4;
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
            self.leftMagin.constant = -30;
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
