//
//  ZoloContactHeadView.m
//  NewZolo
//
//  Created by JTalking on 2022/6/30.
//

#import "ZoloContactHeadView.h"
#import "ZoloContactHeadCell.h"

@interface ZoloContactHeadView () <UICollectionViewDelegate, UICollectionViewDataSource>

@property (weak, nonatomic) IBOutlet UITextField *textField;
@property (weak, nonatomic) IBOutlet UIView *searchView;
@property (nonatomic, assign) NSInteger number;

@end

@implementation ZoloContactHeadView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self = [[[NSBundle mainBundle] loadNibNamed:@"ZoloContactHeadView" owner:self options:nil] firstObject];
        self.frame = CGRectMake(-20, 0, KScreenWidth, 160);
        
        self.textField.placeholder = LocalizedString(@"SearchTitle");
        self.searchView.layer.cornerRadius = 20;
        self.searchView.layer.masksToBounds = YES;
        self.searchView.backgroundColor = [FontManage MHLineSeparatorColor];
        
        UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tapClick)];
        [self.searchView addGestureRecognizer:tap];
        
        [self.collectionView registerNib:[UINib nibWithNibName:NSStringFromClass([ZoloContactHeadCell class]) bundle:nil] forCellWithReuseIdentifier:@"ZoloContactHeadCell"];
        self.collectionView.showsVerticalScrollIndicator = NO;
        self.collectionView.showsHorizontalScrollIndicator = NO;
        self.flowLayout.minimumLineSpacing = 0;
        self.collectionView.dataSource = self;
        self.collectionView.delegate = self;
        self.collectionView.backgroundColor = [FontManage MHWhiteColor];
        self.flowLayout.scrollDirection = UICollectionViewScrollDirectionHorizontal;
        
        MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
        if (login.json.MainDapp.length > 0) {
            self.data = @[LocalizedString(@"NewFriend"), LocalizedString(@"GroupChat"), LocalizedString(@"SmallProgram"), LocalizedString(@"Tag")];
            self.number = 4;
        } else {
            self.data = @[LocalizedString(@"NewFriend"), LocalizedString(@"GroupChat"), LocalizedString(@"Tag")];
            self.number = 3;
        }
        [self.collectionView reloadData];
    }
    return self;
}

- (void)tapClick {
    if (_searchBlock) {
        _searchBlock();
    }
}

- (void)refreshData {
    [self.collectionView reloadData];
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return self.data.count;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    ZoloContactHeadCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:NSStringFromClass([ZoloContactHeadCell class]) forIndexPath:indexPath];
    cell.icon.image = [UIImage imageNamed:[NSString stringWithFormat:@"contact0%ld", indexPath.row + 1]];
    cell.nickName.text = self.data[indexPath.row];
    int count = [[DMCCIMService sharedDMCIMService] getUnreadFriendRequestStatus];
    if (count > 0 && indexPath.item == 0) {
        cell.unRedCount.hidden = NO;
    } else {
        cell.unRedCount.hidden = YES;
    }
    return cell;
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    if (_selectedBlock) {
        _selectedBlock(indexPath.item);
    }
}

- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath {
    return CGSizeMake(KScreenWidth / self.number, 100);
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

@end
