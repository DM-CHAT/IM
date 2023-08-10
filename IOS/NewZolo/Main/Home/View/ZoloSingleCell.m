//
//  ZoloSingleCell.m
//  NewZolo
//
//  Created by JTalking on 2022/6/29.
//

#import "ZoloSingleCell.h"

@interface ZoloSingleCell ()

@property (nonatomic, strong) UILongPressGestureRecognizer *longGes;
@property (weak, nonatomic) IBOutlet UIImageView *nftImg;

@end

@implementation ZoloSingleCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    
    self.icon.layer.cornerRadius = 8;
    self.icon.layer.masksToBounds = YES;
    self.msgRead.layer.cornerRadius = 10;
    self.msgRead.layer.masksToBounds = YES;
    self.msgTime.textColor = [FontManage MHTitleSubColor];
    self.msgContent.textColor = [FontManage MHTitleSubColor];
    self.redView.layer.cornerRadius = 5;
    self.redView.layer.masksToBounds = YES;
    
    [self longGes];
}

- (UILongPressGestureRecognizer *)longGes {
    if (!_longGes) {
        _longGes = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(longPressAction:)];
        [self addGestureRecognizer:_longGes];
    }
    return _longGes;
}

- (void)longPressAction:(UILongPressGestureRecognizer *)longPress {
    if (longPress.state == UIGestureRecognizerStateBegan) {
        if (_cellLongBlock) {
            _cellLongBlock();
        }
    }
}

- (void)setUserInfo:(DMCCUserInfo *)userInfo {
    _userInfo = userInfo;
    self.nickName.text = userInfo.displayName;
    NSString *nftStr = [DMCCUserInfo getNft:userInfo.describes];
    if (nftStr.length > 0) {
        self.nftImg.hidden = NO;
    } else {
        self.nftImg.hidden = YES;
    }
    [self.icon sd_setImageWithURL:[NSURL URLWithString:userInfo.portrait] placeholderImage:SDImageDefault];
    NSString *alias = [[DMCCIMService sharedDMCIMService] getFriendAlias:userInfo.userId];
    if (alias.length > 0) {
        self.nickName.text = alias;
    }
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end
