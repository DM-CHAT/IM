//
//  ZoloSendGroupFootView.m
//  NewZolo
//
//  Created by JTalking on 2022/8/23.
//

#import "ZoloSendGroupFootView.h"
#import "ZoloSendGroupFootCell.h"

@interface ZoloSendGroupFootView () <UICollectionViewDelegate, UICollectionViewDataSource>

@property (weak, nonatomic) IBOutlet UICollectionView *collectionView;
@property (weak, nonatomic) IBOutlet UICollectionViewFlowLayout *flowlayout;
@property (weak, nonatomic) IBOutlet UIButton *finishBtn;

@end

@implementation ZoloSendGroupFootView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self = [[[NSBundle mainBundle] loadNibNamed:@"ZoloSendGroupFootView" owner:self options:nil] firstObject];
        self.frame = CGRectMake(0, KScreenheight - 60 - 40, KScreenWidth, 60);
        self.collectionView.showsVerticalScrollIndicator = NO;
        self.collectionView.showsHorizontalScrollIndicator = NO;
        self.flowlayout.minimumLineSpacing = 10;
        self.collectionView.dataSource = self;
        self.collectionView.delegate = self;
        self.flowlayout.scrollDirection = UICollectionViewScrollDirectionHorizontal;
        [self.collectionView registerNib:[UINib nibWithNibName:NSStringFromClass([ZoloSendGroupFootCell class]) bundle:nil] forCellWithReuseIdentifier:@"ZoloSendGroupFootCell"];
    }
    return self;
}

- (void)setData:(NSArray *)data {
    _data = data;
    [self.finishBtn setTitle:[NSString stringWithFormat:@"完成(%ld)", data.count] forState:UIControlStateNormal];
    [self.collectionView reloadData];
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return self.data.count;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    ZoloSendGroupFootCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:NSStringFromClass([ZoloSendGroupFootCell class]) forIndexPath:indexPath];
    DMCCUserInfo *info = self.data[indexPath.item];
    [cell.img sd_setImageWithURL:[NSURL URLWithString:info.portrait] placeholderImage:SDImageDefault];
    return cell;
}

- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath {
    return CGSizeMake(30, 30);
}

- (IBAction)finishBtnClick:(id)sender {
    if (_finishBlock) {
        _finishBlock();
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
