//
//  ZoloGroupAddFriendVC.m
//  NewZolo
//
//  Created by JTalking on 2022/9/12.
//

#import "ZoloGroupAddFriendVC.h"
#import "ZoloSettingContactCell.h"
#import "HCSortString.h"

@interface ZoloGroupAddFriendVC ()

@property (nonatomic, strong) DMCCGroupInfo *info;
@property (nonatomic, strong) UIButton *setBtn;
@property (strong, nonatomic) NSDictionary *allDataSource;/**<排序后的整个数据源*/
@property (strong, nonatomic) NSArray *indexDataSource;/**<索引数据源*/

@end

@implementation ZoloGroupAddFriendVC

- (instancetype)initWithGroupInfo:(DMCCGroupInfo *)info {
    if (self = [super init]) {
        self.info = info;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.navigationItem.title = LocalizedString(@"ChatSelectFriend");
    MJRefreshNormalHeader *header = [MJRefreshNormalHeader headerWithRefreshingTarget:self refreshingAction:@selector(userFresh)];
    header.lastUpdatedTimeLabel.hidden = YES;
    header.stateLabel.hidden = YES;
    self.tableView.mj_header = header;
    [self getData];
    [self registerCellWithNibName:NSStringFromClass([ZoloSettingContactCell class]) isTableview:YES];
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:self.setBtn];
}

- (void)userFresh {
    [self.dataSource removeAllObjects];
    [self getData];
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [self.tableView.mj_header endRefreshing];
    });
}

- (UIButton *)setBtn {
    if (!_setBtn) {
        _setBtn = [[UIButton alloc] initWithFrame:CGRectZero];
        [_setBtn setTitle:LocalizedString(@"Sure") forState:UIControlStateNormal];
        [_setBtn setTitleColor:[FontManage MHBlockColor] forState:UIControlStateNormal];
        [_setBtn addTarget:self action:@selector(setBtnClick) forControlEvents:UIControlEventTouchUpInside];
    }
    return _setBtn;
}

- (void)setBtnClick {
    NSMutableArray *array = [[NSMutableArray alloc] initWithCapacity:0];
    for (NSString *str in _indexDataSource) {
        for (DMCCUserInfo *user in _allDataSource[str]) {
            if (!user.isNotSelect && user.isSelect) {
                [array addObject:user.userId];
            }
        }
    }
    if (_confirmBlock) {
        _confirmBlock(array);
    }
    [self.navigationController popViewControllerAnimated:YES];
}

- (void)getData {
    [self.dataSource removeAllObjects];
    NSMutableArray *friendArray = [NSMutableArray arrayWithArray:[[DMCCIMService sharedDMCIMService] getMyFriendList:YES]];
    for (NSString *string in friendArray) {
        [self getUserInfo:string array:friendArray.count];
    }
}

- (void)getUserInfo:(NSString *)friendId array:(NSInteger)arrayCount {
    WS(ws);
    [[DMCCIMService sharedDMCIMService] getUserInfo:friendId refresh:NO success:^(DMCCUserInfo *userInfo) {
        if (userInfo) {
            [ws.dataSource addObject:userInfo];
            if (ws.dataSource.count == arrayCount) {
                [ws sortData];
            }
        }
    } error:^(int errorCode) {
        DMCCUserInfo *user = [DMCCUserInfo new];
        user.displayName = @"@Unknown";
        user.userId = friendId;
        [ws.dataSource addObject:user];
        if (ws.dataSource.count == arrayCount) {
            [ws sortData];
        }
    }];
}

- (void)sortData {
    _allDataSource = [HCSortString sortAndGroupForArray:self.dataSource PropertyName:@"displayName"];
    _indexDataSource = [HCSortString sortForStringAry:[_allDataSource allKeys]];
    WS(ws);
    dispatch_async(dispatch_get_main_queue(), ^{
        [ws filterData];
    });
}

- (void)filterData {
    NSArray *memberList = [[DMCCIMService sharedDMCIMService] getGroupMembers:self.info.target forceUpdate:NO];
    for (DMCCGroupMember *member in memberList) {
        DMCCUserInfo *user = [[DMCCIMService sharedDMCIMService] getUserInfo:member.memberId inGroup:member.groupId refresh:NO];
        for (NSString *str in _indexDataSource) {
            for (DMCCUserInfo *userN in _allDataSource[str]) {
                if ([user.userId isEqualToString:userN.userId]) {
                    userN.isSelect = YES;
                    userN.isNotSelect = YES;
                    continue;
                }
            }
        }
    }
    [self.tableView reloadData];
}

- (NSArray<NSString *> *)sectionIndexTitlesForTableView:(UITableView *)tableView {
    return _indexDataSource;
}

//索引点击事件
- (NSInteger)tableView:(UITableView *)tableView sectionForSectionIndexTitle:(NSString *)title atIndex:(NSInteger)index {
    [tableView scrollToRowAtIndexPath:[NSIndexPath indexPathForRow:0 inSection:index] atScrollPosition:UITableViewScrollPositionTop animated:YES];
    return index;
}
 
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return _indexDataSource.count;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    NSArray *value = [_allDataSource objectForKey:_indexDataSource[section]];
    return value.count;
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ZoloSettingContactCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloSettingContactCell class])];
    NSArray *value = [_allDataSource objectForKey:_indexDataSource[indexPath.section]];
    DMCCUserInfo *user = value[indexPath.row];
    [cell.icon sd_setImageWithURL:[NSURL URLWithString:user.portrait] placeholderImage:SDImageDefault];
    cell.nickName.text = user.displayName;
    
    if (user.isNotSelect) {
        cell.backgroundColor = [FontManage MHGrayColor];
        cell.selectImg.image = [UIImage imageNamed:@"noselect"];
    } else {
        cell.backgroundColor = [FontManage MHWhiteColor];
        cell.selectImg.image = user.isSelect ? [UIImage imageNamed:@"select_s"] : [UIImage imageNamed:@"select_n"];
    }
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    NSArray *value = [_allDataSource objectForKey:_indexDataSource[indexPath.section]];
    DMCCUserInfo *user = value[indexPath.row];
    if (!user.isNotSelect) {
        user.isSelect = !user.isSelect;
    }
    [self.tableView reloadData];
}

//头部索引标题
- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
    return _indexDataSource[section];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 50;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 0.001;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return self.scaleHeight(84);
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
