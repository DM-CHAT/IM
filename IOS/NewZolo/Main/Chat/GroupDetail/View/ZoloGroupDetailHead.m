//
//  ZoloGroupDetailHead.m
//  NewZolo
//
//  Created by JTalking on 2022/8/12.
//

#import "ZoloGroupDetailHead.h"
#import "ZoloGroupDetailHeadCell.h"

@interface ZoloGroupDetailHead () <UICollectionViewDelegate, UICollectionViewDataSource>

@property (weak, nonatomic) IBOutlet UICollectionView *collectionView;
@property (weak, nonatomic) IBOutlet UICollectionViewFlowLayout *flowLayout;
@property (nonatomic, assign) BOOL isManager;
@property (nonatomic, assign) BOOL isShowMore;
@property (weak, nonatomic) IBOutlet UIButton *moreBtn;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *moreHeight;
@property (nonatomic, strong) DMCCGroupInfo *info;

@end

@implementation ZoloGroupDetailHead

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    
    [self.moreBtn setTitle:LocalizedString(@"SeeAllMemeber") forState:UIControlStateNormal];
    self.moreBtn.backgroundColor = [FontManage MHLineSeparatorColor];
    [self.moreBtn setTitleColor:[FontManage MHBlockColor] forState:UIControlStateNormal];
    [self.collectionView registerNib:[UINib nibWithNibName:NSStringFromClass([ZoloGroupDetailHeadCell class]) bundle:nil] forCellWithReuseIdentifier:@"ZoloGroupDetailHeadCell"];
    self.collectionView.showsVerticalScrollIndicator = NO;
    self.collectionView.showsHorizontalScrollIndicator = NO;
    self.flowLayout.minimumLineSpacing = 10;
    self.flowLayout.minimumInteritemSpacing = 10;
    self.collectionView.dataSource = self;
    self.collectionView.delegate = self;
    self.flowLayout.scrollDirection = UICollectionViewScrollDirectionVertical;
}

- (void)setMemberList:(NSArray *)memberList withIsGroupManager:(BOOL)isManager withIsShowMore:(BOOL)isShowMore group:(DMCCGroupInfo *)info {
    _memberData = memberList;
    self.isShowMore = isShowMore;
    self.isManager = isManager;
    self.info = info;
    [self.collectionView reloadData];
}

- (IBAction)moreBtnClick:(id)sender {
    if (_friendSeeMoreBlock) {
        _friendSeeMoreBlock();
    }
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    if (self.isManager) {
        if (self.isShowMore) {
            self.moreBtn.hidden = NO;
            self.moreHeight.constant = 50;
        } else {
            self.moreBtn.hidden = YES;
            self.moreHeight.constant = 0;
        }
        return self.memberData.count + 2;
    } else {
        if (self.isShowMore) {
            self.moreBtn.hidden = NO;
            self.moreHeight.constant = 50;
        } else {
            self.moreBtn.hidden = YES;
            self.moreHeight.constant = 0;
        }
        return self.memberData.count + 1;
    }
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    ZoloGroupDetailHeadCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:NSStringFromClass([ZoloGroupDetailHeadCell class]) forIndexPath:indexPath];
  
        if (indexPath.row == self.memberData.count) {
            if ([self isManagerStr]) {
                if (!self.isManager) {
                    // no show
                } else {
                    cell.img.image = [UIImage imageNamed:@"f_add"];
                }
                
            } else {
                cell.img.image = [UIImage imageNamed:@"f_add"];
                
            }
            cell.titleLab.hidden = YES;
            cell.nftImg.hidden = YES;
        } else if (indexPath.row == self.memberData.count + 1 && self.isManager) {
            cell.img.image = [UIImage imageNamed:@"f_less"];
            cell.titleLab.hidden = YES;
            cell.nftImg.hidden = YES;
        } else {
            cell.titleLab.hidden = NO;
            DMCCGroupMember *member = self.memberData[indexPath.row];
            DMCCUserInfo *user = [[DMCCIMService sharedDMCIMService] getUserInfo:member.memberId inGroup:member.groupId refresh:NO];
            DMCCGroupMember *gm = [[DMCCIMService sharedDMCIMService] getGroupMember:member.groupId memberId:user.userId];
            if (![gm.alias isEqualToString:@"(null)"] && gm.alias.length > 0) {
                cell.titleLab.text = gm.alias;
            } else {
                cell.titleLab.text = user.displayName;
            }
            [cell.img sd_setImageWithURL:[NSURL URLWithString:user.portrait] placeholderImage:SDImageDefault];
            
            NSString *nftStr = [DMCCUserInfo getNft:user.describes];
            if (nftStr.length > 0) {
                cell.nftImg.hidden = NO;
            } else {
                cell.nftImg.hidden = YES;
            }
            
        }
    
    return cell;
}

- (BOOL)isManagerStr {
    NSString *str = [[DMCCIMService sharedDMCIMService] getGroupJoinTypeWithGroupId:self.info.target];
    return [str isEqualToString:@"admin"];
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {

        if (indexPath.row == self.memberData.count) {
            if ([self isManagerStr]) {
                if (!self.isManager) {
                    // no show
                    return;
                }
            }
            if (_friendAddBlock) {
                _friendAddBlock();
            }
        } else if (indexPath.row == self.memberData.count + 1 && self.isManager) {
            if (_friendLessBlock) {
                _friendLessBlock();
            }
        } else {
            if (_friendSeeBlock) {
                _friendSeeBlock(indexPath.row);
            }
        }
    
}

- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath {
    if (iphone6_4_7) {
        return CGSizeMake(56, 56);
    } else {
        return CGSizeMake(60, 60);
    }
}

- (UIEdgeInsets)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout insetForSectionAtIndex:(NSInteger)section {
    return UIEdgeInsetsMake(20, 20, 20, 20);
}

@end
