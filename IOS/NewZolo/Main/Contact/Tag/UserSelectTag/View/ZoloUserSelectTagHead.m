//
//  ZoloUserSelectTagHead.m
//  NewZolo
//
//  Created by JTalking on 2022/10/28.
//

#import "ZoloUserSelectTagHead.h"

@interface ZoloUserSelectTagHead ()

@property (weak, nonatomic) IBOutlet UILabel *titleLab;

@end

@implementation ZoloUserSelectTagHead

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self = [[[NSBundle mainBundle] loadNibNamed:@"ZoloUserSelectTagHead" owner:self options:nil] firstObject];
        self.titleLab.text = LocalizedString(@"SelectGroup");
        self.frame = CGRectMake(0, 0, KScreenWidth, 50);
        UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(selectClick)];
        [self addGestureRecognizer:tap];
    }
    return self;
}

- (void)selectClick {
    if (_selectedBlock) {
        _selectedBlock();
    }
}

@end
