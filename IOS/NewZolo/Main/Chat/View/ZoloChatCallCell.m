//
//  ZoloChatVoiceCell.m
//  NewZolo
//
//  Created by JTalking on 2022/7/12.
//

#import "ZoloChatCallCell.h"

@interface ZoloChatCallCell ()

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *voiceViewW;
@property (weak, nonatomic) IBOutlet UIImageView *iconBgImg;

@property (weak, nonatomic) IBOutlet UIView *mulView;
@property (weak, nonatomic) IBOutlet UIButton *mulBtn;

@end

@implementation ZoloChatCallCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    self.iconImg.layer.masksToBounds = YES;
    self.iconImg.layer.cornerRadius = 2;
    
    [self.voiceView addGestureRecognizer:self.tapGes];
    self.iconImg.userInteractionEnabled = YES;
    [self.iconImg addGestureRecognizer:self.iconTapGes];
    [self.iconImg addGestureRecognizer:self.longIconGes];
    
    [self.voiceView addGestureRecognizer:self.longGes];
    
    self.iconLeftMargin.constant = 15;
    self.mulView.hidden = YES;
    [self.mulView addGestureRecognizer:self.mulTapGes];
}

+ (void)calculateCellHeight:(DMCCMessage *)message chatType:(BOOL)isGroup mulSelect:(BOOL)isMulSelect byMsg:(BOOL)isMySend {
    message.msgCellHeight = 50;
    message.msgCellWidth = 160;
}

- (void)setMessage:(DMCCMessage *)message {
    [super setMessage:message];
    DMCCCallMessageContent *soundContent = (DMCCCallMessageContent *)message.content;
    NSString *actionString = @"";
    if (soundContent.status.length == 0) {
        self.hidden = YES;
    } else {
        self.hidden = NO;
    }
    if ([soundContent.status isEqualToString:@"finish"]) {
        NSString *timeStr = [self changeTimeFormater:soundContent.duration/1000];
        actionString = [NSString stringWithFormat:@"%@ %@", LocalizedString(@"CallActionDuration"), timeStr];
    } else if ([soundContent.status isEqualToString:@"reject"]) {
        actionString = [NSString stringWithFormat:@"%@", LocalizedString(@"CallOtherCancer")];
    } else if ([soundContent.status isEqualToString:@"cancel"]) {
        actionString = LocalizedString(@"CallActionCancel");
    }  else if ([soundContent.status isEqualToString:@"refuse"]) {
        actionString = [NSString stringWithFormat:@"%@%@", LocalizedString(@"CallActionTalk"), LocalizedString(@"CallActionReject")];
    }
    if (soundContent.type == 1) {
        self.timeLabel.text = [NSString stringWithFormat:@"%@", actionString];
        self.voiceImg.image = [UIImage imageNamed:@"msg_l_audio"];
    } else {
        self.timeLabel.text = [NSString stringWithFormat:@"%@", actionString];
    }
    self.voiceViewW.constant = 160;
    
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
        
        if (soundContent.type == 1) {
            self.voiceImg.image = [UIImage imageNamed:@"msg_r_audio"];
        } else {
            self.voiceImg.image = [UIImage imageNamed:@"msg_r_vedio"];
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
        
        if (soundContent.type == 1) {
            self.voiceImg.image = [UIImage imageNamed:@"msg_l_audio"];
        } else {
            self.voiceImg.image = [UIImage imageNamed:@"msg_l_vedio"];
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

- (NSString *)changeTimeFormater:(NSInteger)time{
    NSInteger minutecount = time / 60;
    
    NSInteger secondcount = time % 60;
    NSString *timeString;
    if (minutecount > 60) {
        NSInteger hour = minutecount / 60;
        minutecount = hour % 60;
        if (hour > 10) {
            if (minutecount < 10 && secondcount < 10) {
                timeString = [NSString stringWithFormat:@"%ld:0%ld:0%ld",hour,minutecount,secondcount];
                return timeString;
            }
            if (minutecount < 10) {
                timeString = [NSString stringWithFormat:@"%ld:%ld:%ld",hour,minutecount,secondcount];
                return timeString;
            }
            if (secondcount < 10) {
                timeString = [NSString stringWithFormat:@"%ld:%ld:0%ld",hour,minutecount,secondcount];
                return timeString;
                
            }
        } else {
            if (minutecount < 10 && secondcount < 10) {
                timeString = [NSString stringWithFormat:@"0%ld:0%ld:0%ld",hour,minutecount,secondcount];
                return timeString;
            }
            if (minutecount < 10) {
                timeString = [NSString stringWithFormat:@"0%ld:%ld:%ld",hour,minutecount,secondcount];
                return timeString;
            }
            if (secondcount < 10) {
                timeString = [NSString stringWithFormat:@"0%ld:%ld:0%ld",hour,minutecount,secondcount];
                return timeString;
                
            }
        }
        
    }
    if (minutecount < 10 && secondcount < 10) {
        timeString = [NSString stringWithFormat:@"0%ld:0%ld",minutecount,secondcount];
        return timeString;
    }
    if (minutecount < 10) {
        timeString = [NSString stringWithFormat:@"0%ld:%ld",minutecount,secondcount];
        return timeString;
    }
    if (secondcount < 10) {
        timeString = [NSString stringWithFormat:@"%ld:0%ld",minutecount,secondcount];
        return timeString;
        
    }
    return [NSString stringWithFormat:@"%ld:%ld",minutecount,secondcount];
}

@end
