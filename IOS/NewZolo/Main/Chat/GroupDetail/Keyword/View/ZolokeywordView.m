//
//  ZolokeywordView.m
//  NewZolo
//
//  Created by JTalking on 2022/10/29.
//

#import "ZolokeywordView.h"

@implementation ZolokeywordView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self = [[[NSBundle mainBundle] loadNibNamed:@"ZolokeywordView" owner:self options:nil] firstObject];
        self.frame = CGRectMake(0, 0, KScreenWidth, 130);
        
        self.titleOneLab.text = LocalizedString(@"GroupKeywordTitle");
        self.titleTwoLab.text = LocalizedString(@"GroupKeywordFiveTitle");
        self.titleThreeLab.text = LocalizedString(@"GroupKeywordShowTitle");
    }
    return self;
}

@end
