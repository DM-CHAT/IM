//
//  ZoloTagMoveView.m
//  NewZolo
//
//  Created by JTalking on 2022/10/31.
//

#import "ZoloTagMoveView.h"

@interface ZoloTagMoveView ()

@property (weak, nonatomic) IBOutlet UIButton *delBtn;
@property (weak, nonatomic) IBOutlet UIButton *moveBtn;
@property (weak, nonatomic) IBOutlet UIButton *allBtn;

@end

@implementation ZoloTagMoveView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self = [[[NSBundle mainBundle] loadNibNamed:@"ZoloTagMoveView" owner:self options:nil] firstObject];
        self.frame = CGRectMake(0, KScreenheight - 100, KScreenWidth, 100);
        
        self.delBtn.layer.cornerRadius = 4;
        self.delBtn.layer.masksToBounds = YES;
        self.moveBtn.layer.cornerRadius = 4;
        self.moveBtn.layer.masksToBounds = YES;
    }
    return self;
}

- (IBAction)delBtnClick:(id)sender {
    if (_tagDelBtnBlock) {
        _tagDelBtnBlock();
    }
}

- (IBAction)moveBtnClick:(id)sender {
    if (_tagMoveBtnBlock) {
        _tagMoveBtnBlock();
    }
}

- (IBAction)allBtnClick:(id)sender {
    self.allBtn.selected = !self.allBtn.selected;
    if (_tagAllBtnBlock) {
        _tagAllBtnBlock(self.allBtn.selected);
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
