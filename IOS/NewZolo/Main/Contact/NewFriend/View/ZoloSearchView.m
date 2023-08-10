//
//  ZoloSearchView.m
//  NewZolo
//
//  Created by JTalking on 2022/10/25.
//

#import "ZoloSearchView.h"

@interface ZoloSearchView ()

@property (weak, nonatomic) IBOutlet UITextField *textField;
@property (weak, nonatomic) IBOutlet UIView *searchView;


@end

@implementation ZoloSearchView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self = [[[NSBundle mainBundle] loadNibNamed:@"ZoloSearchView" owner:self options:nil] firstObject];
        self.frame = CGRectMake(0, 0, KScreenWidth, 54);
        self.textField.placeholder = LocalizedString(@"SearchTitle");
        self.backgroundColor = [FontManage MHWhiteColor];
        self.searchView.layer.cornerRadius = 20;
        self.searchView.layer.masksToBounds = YES;
        self.searchView.backgroundColor = [FontManage MHLineSeparatorColor];
        UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tapClick)];
        [self addGestureRecognizer:tap];
    }
    return self;
}

- (void)tapClick {
    if (_searchBlock) {
        _searchBlock();
    }
}

@end
