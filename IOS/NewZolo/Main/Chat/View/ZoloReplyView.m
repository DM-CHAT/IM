//
//  ZoloReplyView.m
//  NewZolo
//
//  Created by JTalking on 2022/11/16.
//

#import "ZoloReplyView.h"

@interface ZoloReplyView ()

@property (weak, nonatomic) IBOutlet UIView *replyBgView;

@end

@implementation ZoloReplyView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self = [[[NSBundle mainBundle] loadNibNamed:@"ZoloReplyView" owner:self options:nil] firstObject];
        self.replyBgView.layer.cornerRadius = 5;
        self.replyBgView.layer.masksToBounds = YES;
        self.frame = CGRectMake(0, KScreenheight, KScreenWidth, 65);
        self.replyBgView.backgroundColor = [FontManage MHGrayColor];
    }
    return self;
}


- (IBAction)closeBtnClick:(id)sender {
    if (_replyViewBlock) {
        _replyViewBlock();
    }
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

@end
