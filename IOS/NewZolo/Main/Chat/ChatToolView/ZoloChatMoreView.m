//
//  ZoloChatMoreView.m
//  NewZolo
//
//  Created by JTalking on 2022/7/13.
//

#import "ZoloChatMoreView.h"
#import "ZoloChatMoreCell.h"

@interface ZoloChatMoreView () <UICollectionViewDelegate, UICollectionViewDataSource>

@property (weak, nonatomic) IBOutlet UICollectionView *collectionView;
@property (weak, nonatomic) IBOutlet UICollectionViewFlowLayout *flowLayout;
@property (nonatomic, strong) NSArray *data;
@property (nonatomic, strong) NSArray *imgData;

@end

@implementation ZoloChatMoreView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self = [[[NSBundle mainBundle] loadNibNamed:@"ZoloChatMoreView" owner:self options:nil] firstObject];
        [self.collectionView registerNib:[UINib nibWithNibName:NSStringFromClass([ZoloChatMoreCell class]) bundle:nil] forCellWithReuseIdentifier:@"ZoloChatMoreCell"];
        self.collectionView.showsVerticalScrollIndicator = NO;
        self.collectionView.showsHorizontalScrollIndicator = NO;
        self.flowLayout.minimumLineSpacing = 30;
        self.flowLayout.minimumInteritemSpacing = 0;
        self.collectionView.dataSource = self;
        self.collectionView.delegate = self;
        self.flowLayout.scrollDirection = UICollectionViewScrollDirectionVertical;
        self.backgroundColor = [FontManage MHWhiteColor];
    }
    return self;
}

- (void)setChatType:(DMCCConversationType)chatType {
    _chatType = chatType;
    if (chatType == Single_Type) {
        self.data = @[LocalizedString(@"ConImage"), LocalizedString(@"ConVedio"), LocalizedString(@"ConFile"), LocalizedString(@"ConCard"), LocalizedString(@"ConTransfer"), LocalizedString(@"videoCall"), LocalizedString(@"audioCall")];
        self.imgData = @[@"c_phone", @"c_video", @"c_file", @"c_card", @"c_sendbill", @"c_vedioCall", @"c_audioCall"];
    } else {
        self.data = @[LocalizedString(@"ConImage"), LocalizedString(@"ConVedio"), LocalizedString(@"ConFile"), LocalizedString(@"ConCard"), LocalizedString(@"ConRedPack"), LocalizedString(@"ConBoomRedPack")];
        self.imgData = @[@"c_phone", @"c_video", @"c_file", @"c_card", @"c_redpack_p", @"c_redpack"];
    }
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    BOOL isShow = [self getWalletHidden];
    if (login.json.HIDE_ENABLE.intValue || isShow) {
        self.frame = CGRectMake(0, KScreenheight - 300, KScreenWidth, 300);
    } else {
        if (_chatType == Single_Type) {
            self.frame = CGRectMake(0, KScreenheight - 300, KScreenWidth, 300);
        } else {
            self.frame = CGRectMake(0, KScreenheight - 150, KScreenWidth, 150);
        }
    }
    [self.collectionView reloadData];
}

- (void)setIsShowBom:(BOOL)isShowBom {
    _isShowBom = isShowBom;
    [self.collectionView reloadData];
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return self.data.count;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    ZoloChatMoreCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:NSStringFromClass([ZoloChatMoreCell class]) forIndexPath:indexPath];
    cell.nameLabel.text = self.data[indexPath.item];
    cell.iconImg.image = [UIImage imageNamed:self.imgData[indexPath.item]];
    return cell;
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    if (_moreClickBlock) {
        _moreClickBlock(indexPath.row);
    }
}

- (BOOL)getWalletHidden {
    NSArray *array = [[DMCCIMService sharedDMCIMService] getWalletInfoList];
    BOOL isShow = array.count > 0;
    return isShow;
}

- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath {
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    BOOL isShow = [self getWalletHidden];
    if (self.chatType == Single_Type) {
        if (indexPath.item == 4) {
            if (login.json.HIDE_ENABLE.intValue || isShow) {
                return CGSizeMake(KScreenWidth / 4, 100);
            } else {
                return CGSizeMake(0, 0);
            }
        } else {
            return CGSizeMake(KScreenWidth / 4, 100);
        }
    } else {
        if (indexPath.item == 4) {
            if (login.json.HIDE_ENABLE.intValue || isShow) {
                return CGSizeMake(KScreenWidth / 4, 100);
            } else {
                return CGSizeMake(0, 0);
            }
        } else if (indexPath.item == 5) {
            if (self.isShowBom) {
                return CGSizeMake(KScreenWidth / 4, 100);
            } else {
                return CGSizeMake(0, 0);
            }
            
        } else {
            return CGSizeMake(KScreenWidth / 4, 100);
        }
    }
}

- (UIEdgeInsets)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout insetForSectionAtIndex:(NSInteger)section {
    return UIEdgeInsetsMake(0, 0, 0, 0);
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

@end
