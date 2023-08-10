//
//  ZoloEmoOrImgView.m
//  NewZolo
//
//  Created by JTalking on 2022/7/5.
//

#import "ZoloEmoOrImgView.h"
#import "ZoloEmjCell.h"

@interface ZoloEmoOrImgView () <UICollectionViewDelegate, UICollectionViewDataSource>

@property (weak, nonatomic) IBOutlet UICollectionView *collectionView;
@property (weak, nonatomic) IBOutlet UICollectionViewFlowLayout *flowLayout;
@property (nonatomic, strong) NSArray *data;
@property (weak, nonatomic) IBOutlet UIButton *sendBtn;
@property (weak, nonatomic) IBOutlet UIView *sendBgView;

@end

@implementation ZoloEmoOrImgView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self = [[[NSBundle mainBundle] loadNibNamed:@"ZoloEmoOrImgView" owner:self options:nil] firstObject];
        self.frame = CGRectMake(0, KScreenheight - 300, KScreenWidth, 300);
        [self loadEmojData];
        [self.collectionView registerNib:[UINib nibWithNibName:NSStringFromClass([ZoloEmjCell class]) bundle:nil] forCellWithReuseIdentifier:@"ZoloEmjCell"];
        self.collectionView.showsVerticalScrollIndicator = NO;
        self.collectionView.showsHorizontalScrollIndicator = NO;
        self.flowLayout.minimumLineSpacing = 0;
        self.collectionView.dataSource = self;
        self.collectionView.delegate = self;
        self.flowLayout.scrollDirection = UICollectionViewScrollDirectionVertical;
        [self.collectionView reloadData];
        self.sendBtn.layer.cornerRadius = 2;
        self.sendBtn.layer.masksToBounds = YES;
        [self.sendBtn setTitle:LocalizedString(@"ChatSendButtom") forState:UIControlStateNormal];
        self.backgroundColor = [FontManage MHWhiteColor];
        self.sendBgView.backgroundColor = [FontManage MHWhiteColor];
    }
    return self;
}

- (void)loadEmojData {
    NSString *resourcePath = [[NSBundle mainBundle] resourcePath];
    NSString *bundlePath = [resourcePath stringByAppendingPathComponent:@"Emoj.plist"];
    self.data = [[NSArray alloc]initWithContentsOfFile:bundlePath];
    [self.collectionView reloadData];
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return self.data.count;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    ZoloEmjCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:NSStringFromClass([ZoloEmjCell class]) forIndexPath:indexPath];
    cell.emjLabel.text = self.data[indexPath.item];
    cell.emjLabel.font = [UIFont fontWithName:@"AppleColorEmoji" size:30.0];
    return cell;
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    if (_emojClickBlock) {
        _emojClickBlock(self.data[indexPath.row]);
    }
}

- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath {
    return CGSizeMake(KScreenWidth / 7, 44);
}

- (UIEdgeInsets)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout insetForSectionAtIndex:(NSInteger)section {
    return UIEdgeInsetsMake(0, 14, 0, 14);
}

- (IBAction)sendBtnClick:(id)sender {
    if (_sendEmojClickBlock) {
        _sendEmojClickBlock();
    }
}

@end
