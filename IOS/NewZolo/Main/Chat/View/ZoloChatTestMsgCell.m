//
//  ZoloChatTestMsgCell.m
//  NewZolo
//
//  Created by JTalking on 2022/7/1.
//

#import "ZoloChatTestMsgCell.h"
#import "AttributedLabel.h"

@interface ZoloChatTestMsgCell () <AttributedLabelDelegate>

@property (weak, nonatomic) IBOutlet UIView *quoteView;

@property (weak, nonatomic) IBOutlet UILabel *quoNameLab;
@property (weak, nonatomic) IBOutlet UILabel *quoContenLab;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *quoBottomMargn;
@property (weak, nonatomic) IBOutlet UIImageView *iconBgImg;

@property (weak, nonatomic) IBOutlet UIView *mulView;
@property (weak, nonatomic) IBOutlet UIButton *mulBtn;

@end

@implementation ZoloChatTestMsgCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    
    self.icon.layer.masksToBounds = YES;
    self.icon.layer.cornerRadius = 2;
    
    self.quoteView.layer.masksToBounds = YES;
    self.quoteView.layer.cornerRadius = 12;
    [self.quoteView addGestureRecognizer:self.quoteTapGes];
    
    self.icon.userInteractionEnabled = YES;
    [self.icon addGestureRecognizer:self.iconTapGes];
    [self.icon addGestureRecognizer:self.longIconGes];
    
    self.msg.userInteractionEnabled = YES;
    [self.msg addGestureRecognizer:self.longGes];
    
    ((AttributedLabel*)self.msg).attributedLabelDelegate = self;
    
    self.iconLeftMargin.constant = 15;
    self.mulView.hidden = YES;
    [self.mulView addGestureRecognizer:self.mulTapGes];
}

+ (void)calculateCellHeight:(DMCCMessage *)message chatType:(BOOL)isGroup mulSelect:(BOOL)isMulSelect byMsg:(BOOL)isMySend {
    if (message.msgCellHeight > 0 && message.msgCellHeight < 500 && isMulSelect == NO) {
        return;
    }
    int textMulWidth = 0;
    if (isMulSelect && !isMySend) {
        textMulWidth = 34;
    }
    int textWidth = 124 + textMulWidth;
    if (isGroup) {
        textWidth = 156 + textMulWidth;
    }
    CGFloat height = [MHHelperUtils cellTextHeight:message.digest font:15.2 textWith:KScreenWidth - textWidth];
    CGFloat width = [MHHelperUtils cellTextWidth:message.digest font:15.2 textHeight:height];
    height += 34;
    message.msgCellWidth = width;
    message.msgCellHeight = height;
    DMCCTextMessageContent *text = (DMCCTextMessageContent *)message.content;
    if (text.quoteInfo.userId.length > 0) {
        message.msgCellHeight = message.msgCellHeight + 80;
    }
}

- (void)setMessage:(DMCCMessage *)message {
    [super setMessage:message];
    if ([message.fromUser isEqualToString:self.myInfo.userId]) {
        self.msg.textColor = [UIColor whiteColor];
        self.msgTimeLabel.textAlignment = NSTextAlignmentRight;
        [self.msg setText:message.digest isRight:YES];
        if (self.chatType == Single_Type) {
            self.icon.hidden = YES;
            self.iconBgImg.hidden = YES;
            self.rightMargin.constant = -30;
            self.msgTimeLabel.frame = CGRectMake(-24, message.msgCellHeight + 4, kScreenwidth, 20);
        } else {
            self.icon.hidden = NO;
            self.iconBgImg.hidden = NO;
            self.rightMargin.constant = 4;
            [self.icon sd_setImageWithURL:[NSURL URLWithString:self.myInfo.portrait] placeholderImage:SDImageDefault];
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
        self.msg.textColor = [UIColor blackColor];
        self.msgTimeLabel.textAlignment = NSTextAlignmentLeft;
        [self.msg setText:message.digest isRight:NO];
        if (self.chatType == Group_Type) {
            self.icon.hidden = NO;
            self.iconBgImg.hidden = NO;
            self.leftMargin.constant = 4;
            DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:message.fromUser refresh:NO];
            [self.icon sd_setImageWithURL:[NSURL URLWithString:userInfo.portrait] placeholderImage:SDImageDefault];
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
            self.icon.hidden = YES;
            self.iconBgImg.hidden = YES;
            self.leftMargin.constant = -30;
            [self.icon sd_setImageWithURL:[NSURL URLWithString:self.userInfo.portrait] placeholderImage:SDImageDefault];
            self.msgTimeLabel.frame = CGRectMake(24, message.msgCellHeight + 4, kScreenwidth, 20);
        }
    }
    
    DMCCTextMessageContent *text = (DMCCTextMessageContent *)message.content;
    if (text.quoteInfo.userId.length > 0) {
        self.quoNameLab.text = text.quoteInfo.userDisplayName;
        self.quoContenLab.text = text.quoteInfo.messageDigest;
        self.quoBottomMargn.constant = 80;
        self.quoteView.hidden = NO;
    } else {
        self.quoteView.hidden = YES;
        self.quoBottomMargn.constant = 15;
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

#pragma mark - AttributedLabelDelegate
- (void)didSelectUrl:(NSString *)urlString {
    NSLog(@"===%@==", urlString);
    if (self.cellTapUrlBlock) {
        self.cellTapUrlBlock(urlString);
    }
}
- (void)didSelectPhoneNumber:(NSString *)phoneNumberString {
    NSLog(@"===%@==", phoneNumberString);
    if (self.cellTapPhoneBlock) {
        self.cellTapPhoneBlock(phoneNumberString);
    }
}

@end
