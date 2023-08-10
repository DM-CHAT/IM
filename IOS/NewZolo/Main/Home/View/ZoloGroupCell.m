//
//  ZoloSingleCell.m
//  NewZolo
//
//  Created by JTalking on 2022/6/29.
//

#import "ZoloGroupCell.h"

@interface ZoloGroupCell ()

@property (weak, nonatomic) IBOutlet UIImageView *icon;
@property (weak, nonatomic) IBOutlet UILabel *nickName;
@property (nonatomic, strong) UILongPressGestureRecognizer *longGes;

@end

@implementation ZoloGroupCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    
    self.icon.layer.cornerRadius = 8;
    self.icon.layer.masksToBounds = YES;
    self.msgRead.layer.cornerRadius = 10;
    self.msgRead.layer.masksToBounds = YES;
    self.msgContent.textColor = [FontManage MHTitleSubColor];
    self.msgTime.textColor = [FontManage MHTitleSubColor];
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

- (void)setGroupInfo:(DMCCGroupInfo *)groupInfo {
    _groupInfo = groupInfo;
    self.nickName.text = groupInfo.name;
    [self.icon sd_setImageWithURL:[NSURL URLWithString:groupInfo.portrait] placeholderImage:SDImageDefault];
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end
