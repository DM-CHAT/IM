//
//  ZoloNoticeHeadView.m
//  NewZolo
//
//  Created by JTalking on 2022/9/22.
//

#import "ZoloNoticeHeadView.h"

@implementation ZoloNoticeHeadView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:CGRectZero]) {
        self = [[[NSBundle mainBundle] loadNibNamed:@"ZoloNoticeHeadView" owner:self options:nil] firstObject];
        self.frame = CGRectMake(0, 64 + iPhoneX_topH, kScreenwidth, 32);
        UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tapClick)];
        self.backgroundColor = [FontManage MHGrayColor];
        [self addGestureRecognizer:tap];
    }
    return self;
}

- (void)tapClick {
    if (_noticeViewBlock) {
        _noticeViewBlock();
    }
}

@end
