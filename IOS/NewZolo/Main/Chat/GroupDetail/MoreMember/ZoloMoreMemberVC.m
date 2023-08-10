//
//  ZoloMoreMemberVC.m
//  NewZolo
//
//  Created by JTalking on 2022/9/22.
//

#import "ZoloMoreMemberVC.h"
#import "ZoloGroupDetailHeadCell.h"
#import "ZoloContactDetailVC.h"
#import "ZoloAddFriendInfoVC.h"

@interface ZoloMoreMemberVC () <UICollectionViewDelegate, UICollectionViewDataSource>

@property (nonatomic, strong) DMCCGroupInfo *info;

@end

@implementation ZoloMoreMemberVC

- (instancetype)initWithGroupInfo:(DMCCGroupInfo *)info {
    if (self = [super init]) {
        self.info = info;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.layout.minimumLineSpacing = 10;
    self.layout.minimumInteritemSpacing = 10;
    self.layout.scrollDirection = UICollectionViewScrollDirectionVertical;
    self.collectionView.backgroundColor = [FontManage MHGrayColor];
    self.collectionView.showsVerticalScrollIndicator = NO;
    self.collectionView.showsHorizontalScrollIndicator = NO;
    [self registerCellWithNibName:NSStringFromClass([ZoloGroupDetailHeadCell class]) isTableview:NO];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(groupMemberUpdata:) name:kGroupMemberUpdated object:nil];
    
    self.dataSource = [NSMutableArray arrayWithArray:[[DMCCIMService sharedDMCIMService] getGroupZoneMembers:self.info.target begin:0 forceUpdate:NO]];
    [self.collectionView reloadData];
    
    
    
    int index = 0;
    while (index < self.info.memberCount) {
        NSLog(@"=test2= getGroupZoneMembers begin %d count %ld", index, self.info.memberCount);
        [[DMCCIMService sharedDMCIMService] getGroupZoneMembers:self.info.target begin:index forceUpdate:YES];
        index += 50;
    }
    
    
    
    index = self.info.memberCount - 1;
    NSLog(@"=test2= delOutGroupMember index %d count %ld", index, self.info.memberCount);
    [[DMCCIMService sharedDMCIMService] delOutGroupMember:self.info.target index:index];
    
}

- (void)groupMemberUpdata:(NSNotification *)notification {
    NSDictionary *dic = (NSDictionary*)notification.userInfo;
    NSArray *array = dic[@"members"];
    self.dataSource = [NSMutableArray arrayWithArray:array];
    [self.collectionView reloadData];
}


- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return self.dataSource.count;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    ZoloGroupDetailHeadCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:NSStringFromClass([ZoloGroupDetailHeadCell class]) forIndexPath:indexPath];
    DMCCGroupMember *member = self.dataSource[indexPath.row];
    DMCCUserInfo *user = [[DMCCIMService sharedDMCIMService] getUserInfo:member.memberId inGroup:member.groupId refresh:NO];
    cell.titleLab.text = user.displayName;
    [cell.img sd_setImageWithURL:[NSURL URLWithString:user.portrait] placeholderImage:SDImageDefault];
    
    NSString *nftStr = [DMCCUserInfo getNft:user.describes];
    if (nftStr.length > 0) {
        cell.nftImg.hidden = NO;
    } else {
        cell.nftImg.hidden = YES;
    }
    return cell;
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    if (![[DMCCIMService sharedDMCIMService] isAllowAddFriendGroupMemberName:self.info.target]) {
        [MHAlert showMessage:LocalizedString(@"StopMemberChat")];
        return;
    }
    // 判断好友是否已经存在
    DMCCGroupMember *member = self.dataSource[indexPath.item];
    DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:member.memberId inGroup:member.groupId refresh:NO];
    if ([[DMCCIMService sharedDMCIMService] isMyFriend:userInfo.userId]) {
        [self.navigationController pushViewController:[[ZoloContactDetailVC alloc] initWithUserInfo:userInfo] animated:YES];
    } else {
        ZoloAddFriendInfoVC *vc = [[ZoloAddFriendInfoVC alloc] initWithUserInfo:userInfo];
        [self.navigationController pushViewController:vc animated:YES];
    }
}

- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath {
    return CGSizeMake(60, 60);
}

- (UIEdgeInsets)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout insetForSectionAtIndex:(NSInteger)section {
    return UIEdgeInsetsMake(20, 20, 20, 20);
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
