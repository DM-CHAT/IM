//
//  ZoloRoleView.m
//  NewZolo
//
//  Created by JTalking on 2022/9/22.
//

#import "ZoloRoleView.h"

@implementation ZoloRoleView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self = [[[NSBundle mainBundle] loadNibNamed:@"ZoloRoleView" owner:self options:nil] firstObject];
    }
    return self;
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

@end
