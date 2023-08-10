//
//  ZoloBaseChatCell.m
//  NewZolo
//
//  Created by JTalking on 2022/6/30.
//

#import "ZoloBaseChatCell.h"

@interface ZoloBaseChatCell ()

@end

@implementation ZoloBaseChatCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
}

- (UIImageView *)errorImg {
    if (!_errorImg) {
        _errorImg = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, 20, 20)];
        _errorImg.image = [UIImage imageNamed:@"warning"];
        _errorImg.userInteractionEnabled = YES;
        [_errorImg addGestureRecognizer:self.resendTapGes];
        [self addSubview:_errorImg];
    }
    return _errorImg;
}

- (UILabel *)nickNameLabel {
    if (!_nickNameLabel) {
        _nickNameLabel = [UILabel new];
        _nickNameLabel.backgroundColor = [UIColor clearColor];
        _nickNameLabel.textColor = MHColor(255, 255, 255);
        _nickNameLabel.font = [UIFont systemFontOfSize:12];
        [self addSubview:_nickNameLabel];
        
    }
    return _nickNameLabel;
}

- (UILabel *)msgTimeLabel {
    if (!_msgTimeLabel) {
        _msgTimeLabel = [UILabel new];
        _msgTimeLabel.backgroundColor = [UIColor clearColor];
        _msgTimeLabel.textColor = MHColor(255, 255, 255);
        _msgTimeLabel.font = [UIFont systemFontOfSize:12];
        [self addSubview:_msgTimeLabel];
        
    }
    return _msgTimeLabel;
}

- (UITapGestureRecognizer *)tapGes {
    if (!_tapGes) {
        _tapGes = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tapBtnClick)];
    }
    return _tapGes;
}

- (UITapGestureRecognizer *)iconTapGes {
    if (!_iconTapGes) {
        _iconTapGes = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(iconTapClick)];
    }
    return _iconTapGes;
}

- (UITapGestureRecognizer *)resendTapGes {
    if (!_resendTapGes) {
        _resendTapGes = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(resendTapClick)];
    }
    return _resendTapGes;
}

- (UITapGestureRecognizer *)mulTapGes {
    if (!_mulTapGes) {
        _mulTapGes = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(mulTapClick)];
    }
    return _mulTapGes;
}

- (UITapGestureRecognizer *)quoteTapGes {
    if (!_quoteTapGes) {
        _quoteTapGes = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(quetoTapClick)];
    }
    return _quoteTapGes;
}

- (void)quetoTapClick {
    if (_cellQuoteTapBlock) {
        _cellQuoteTapBlock();
    }
}

- (void)resendTapClick {
    if (_cellResendTapBlock) {
        _cellResendTapBlock();
    }
}

- (void)iconTapClick {
    if (_cellIconTapBlock) {
        _cellIconTapBlock();
    }
}

- (void)tapBtnClick {
    if (_cellTapBlock) {
        _cellTapBlock();
    }
}

- (void)mulTapClick {
    if (_cellMulSelectBlock) {
        _cellMulSelectBlock();
    }
}

- (void)setIsMulSelect:(BOOL)isMulSelect {
    _isMulSelect = isMulSelect;
}

- (UILongPressGestureRecognizer *)longGes {
    if (!_longGes) {
        _longGes = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(longPressAction:)];
        [self addGestureRecognizer:_longGes];
    }
    return _longGes;
}

- (UILongPressGestureRecognizer *)longIconGes {
    if (!_longIconGes) {
        _longIconGes = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(longIconPressAction:)];
        [self addGestureRecognizer:_longIconGes];
    }
    return _longIconGes;
}

- (void)longPressAction:(UILongPressGestureRecognizer *)longPress {
    if (longPress.state == UIGestureRecognizerStateBegan) {
        if (_cellLongBlock) {
            _cellLongBlock();
        }
    }
}

- (void)longIconPressAction:(UILongPressGestureRecognizer *)longPress {
    if (longPress.state == UIGestureRecognizerStateBegan) {
        if (_cellLongIconBlock) {
            _cellLongIconBlock();
        }
    }
}

- (void)setChatType:(DMCCConversationType)type myInfo:(DMCCUserInfo *)myInfo userInfo:(DMCCUserInfo *)userInfo withConversation:(DMCCConversation *)conversation {
    self.chatType = type;
    self.myInfo = myInfo;
    self.userInfo = userInfo;
    self.conversation = conversation;
}

+ (void)calculateCellHeight:(DMCCMessage *)message chatType:(BOOL)isGroup mulSelect:(BOOL)isMulSelect byMsg:(BOOL)isMySend {
    message.msgCellHeight = 0;
}

- (void)setMessage:(DMCCMessage *)message {
    _message = message;
    
    if (message.status == Message_Status_Send_Failure) {
        self.errorImg.hidden = NO;
    } else {
        self.errorImg.hidden = YES;
    }
    
    NSString *timeStr = [MHDateTools formatTimeDetailLabel:message.serverTime];
    
    if ([message.fromUser isEqualToString:self.myInfo.userId]) {
        self.nickNameLabel.hidden = YES;
        self.msgTimeLabel.text = timeStr;
    } else {
        self.nickNameLabel.hidden = [[DMCCIMService sharedDMCIMService] isHiddenGroupMemberName:self.conversation.target];
        DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:message.fromUser refresh:NO];
        self.nickNameLabel.text = [NSString stringWithFormat:@"%@ %@", userInfo.displayName, timeStr];
        if (self.chatType == Group_Type) {
            DMCCGroupMember *gm = [[DMCCIMService sharedDMCIMService] getGroupMember:self.conversation.target memberId:userInfo.userId];
            if (![gm.alias isEqualToString:@"(null)"] && gm.alias.length > 0) {
                self.nickNameLabel.text = [NSString stringWithFormat:@"%@ %@", gm.alias, timeStr];
            }
        } else {
            NSString *alias = [[DMCCIMService sharedDMCIMService] getFriendAlias:userInfo.userId];
            if (alias.length > 0) {
                self.nickNameLabel.text = [NSString stringWithFormat:@"%@ %@", alias, timeStr];
            }
        }
        BOOL isNickName = ![[DMCCIMService sharedDMCIMService] isHiddenGroupMemberName:self.conversation.target];
        if (!isNickName) {
            self.msgTimeLabel.text = timeStr;
            self.msgTimeLabel.hidden = NO;
        } else {
            self.msgTimeLabel.hidden = YES;
        }
    }
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end
