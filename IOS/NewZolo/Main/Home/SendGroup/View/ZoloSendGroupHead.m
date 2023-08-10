//
//  ZoloSendGroupHead.m
//  NewZolo
//
//  Created by JTalking on 2022/7/8.
//

#import "ZoloSendGroupHead.h"

@interface ZoloSendGroupHead ()

@property (weak, nonatomic) IBOutlet UILabel *headTitle;
@property (weak, nonatomic) IBOutlet UILabel *searchLabel;
@property (weak, nonatomic) IBOutlet UILabel *groupLabel;
@property (weak, nonatomic) IBOutlet UILabel *inviteLabel;

@end

@implementation ZoloSendGroupHead

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self = [[[NSBundle mainBundle] loadNibNamed:@"ZoloSendGroupHead" owner:self options:nil] firstObject];
        self.frame = CGRectMake(0, 0, KScreenWidth, 60);
        [self createView];
    }
    return self;
}

- (void)createView {
    self.headTitle.text = LocalizedString(@"CreateGoup");
    self.searchLabel.text = LocalizedString(@"SearchTitle");
    self.groupLabel.text = LocalizedString(@"HavaGoup");
    self.inviteLabel.text = LocalizedString(@"InviteTitle");
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

@end
