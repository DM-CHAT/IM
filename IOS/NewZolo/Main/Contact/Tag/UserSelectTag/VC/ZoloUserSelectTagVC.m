//
//  ZoloUserSelectTagVC.m
//  NewZolo
//
//  Created by JTalking on 2022/10/28.
//

#import "ZoloUserSelectTagVC.h"

#import "ZoloSettingContactCell.h"
#import "ZoloUserSelectTagHead.h"
#import "ZoloContactGroupVC.h"
#import "ZoloNewFriendVC.h"
#import "ZoloSingleChatVC.h"
#import "ZoloContactDetailVC.h"
#import "ZoloSmallProgramVC.h"
#import "ZoloTagVC.h"
#import "HCSortString.h"
#import "EmptyVC.h"
#import "ZoloContactHead.h"

@interface ZoloUserSelectTagVC ()

@property (nonatomic, strong) EmptyVC *emptyVC;
@property (strong, nonatomic) NSDictionary *allDataSource;/**<排序后的整个数据源*/
@property (strong, nonatomic) NSArray *indexDataSource;/**<索引数据源*/
@property (nonatomic, strong) UIButton *setBtn;
@property (nonatomic, strong) ZoloTagModel *info;
@property (nonatomic, assign) NSInteger counts;
@property (nonatomic, assign) NSInteger groupCounts;

@end

@implementation ZoloUserSelectTagVC

- (instancetype)initWithTagInfo:(ZoloTagModel *)info {
    if (self = [super init]) {
        self.info = info;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.tableView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
    self.tableView.separatorColor = [FontManage MHLineSeparatorColor];
    self.tableView.backgroundColor = [FontManage MHGrayColor];
    self.tableView.contentInset = UIEdgeInsetsMake(0, 0, 50, 0);
    MJRefreshNormalHeader *header = [MJRefreshNormalHeader headerWithRefreshingTarget:self refreshingAction:@selector(userFresh)];
    header.lastUpdatedTimeLabel.hidden = YES;
    header.stateLabel.hidden = YES;
    self.tableView.mj_header = header;
    self.counts = 0;
    self.groupCounts = 0;
    [self registerCellWithNibName:NSStringFromClass([ZoloSettingContactCell class]) isTableview:YES];
    [self registerHeaderFooterWithNibName:NSStringFromClass([ZoloContactHead class])];
    [self getData:NO];
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:self.setBtn];
}

- (void)userFresh {
    [self.dataSource removeAllObjects];
    [self getData:NO];
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
    NSMutableArray *userArray = [[NSMutableArray alloc] initWithCapacity:0];
    NSMutableArray *groupArray = [[NSMutableArray alloc] initWithCapacity:0];
    for (NSString *str in _indexDataSource) {
        for (id info in _allDataSource[str]) {
            if ([info isKindOfClass:[DMCCUserInfo class]]) {
                DMCCUserInfo *user = (DMCCUserInfo *)info;
                if (user.isSelect) {
                    [userArray addObject:user.userId];
                }
            } else {
                DMCCGroupInfo *group = (DMCCGroupInfo *)info;
                if (group.isSelect) {
                    [groupArray addObject:group.target];
                }
            }
        }
    }
    [self addTagWithUser:userArray withGroup:groupArray];
}

- (void)addTagWithUser:(NSArray *)userIDs withGroup:(NSArray *)groupIDs {
    if (userIDs.count == 0) {
        [self addTagWithGroup:groupIDs];
    } else {
        WS(ws);
        [[ZoloAPIManager instanceManager] addTagNameWithName:self.info.group_name type:2 osnId:[userIDs componentsJoinedByString:@","] tagId:self.info.id WithCompleteBlock:^(BOOL isSuccess) {
            if (isSuccess) {
                [ws addTagWithGroup:groupIDs];
            }
        }];
    }
}

- (void)addTagWithGroup:(NSArray *)groupIDs {
    if (groupIDs.count == 0) {
        [MHAlert showMessage:LocalizedString(@"AlertSuccess")];
        [self.navigationController popToRootViewControllerAnimated:YES];
    } else {
        [[ZoloAPIManager instanceManager] addTagNameWithName:self.info.group_name type:3 osnId:[groupIDs componentsJoinedByString:@","] tagId:self.info.id WithCompleteBlock:^(BOOL isSuccess) {
            if (isSuccess) {
                [MHAlert showMessage:LocalizedString(@"AlertSuccess")];
                [self.navigationController popToRootViewControllerAnimated:YES];
            }
        }];
    }
}

- (void)getData:(BOOL)isFresh {
    [self.dataSource removeAllObjects];

    NSMutableArray *friendArray = [NSMutableArray arrayWithArray:[[DMCCIMService sharedDMCIMService] getMyFriendList:isFresh]];
    for (NSString *string in friendArray) {
        [self getUserInfo:string array:friendArray.count];
    }
    if (friendArray.count == 0) {
        [self.tableView.mj_header endRefreshing];
    }
}

- (EmptyVC *)emptyVC {
    if (!_emptyVC) {
        _emptyVC = [[EmptyVC alloc] init];
        _emptyVC.view.frame = CGRectMake(0, 0, KScreenWidth, KScreenHeight);
    }
    return _emptyVC;
}

- (void)getUserInfo:(NSString *)friendId array:(NSInteger)arrayCount {
    WS(ws);
    [[DMCCIMService sharedDMCIMService] getUserInfo:friendId refresh:NO success:^(DMCCUserInfo *userInfo) {
        if (userInfo && userInfo.tagID == -1) {
            [ws.dataSource addObject:userInfo];
        } else {
            // 统计已被添加的人
            ws.counts ++;
        }
        // 未添加的人 ==  总个数 - 已添加的人
        if (ws.dataSource.count == (arrayCount - ws.counts)) {
            [ws getGroup];
        }
    } error:^(int errorCode) {
        DMCCUserInfo *user = [DMCCUserInfo new];
        user.displayName = @"@Unknown";
        user.userId = friendId;
        [ws.dataSource addObject:user];
        if (ws.dataSource.count == (arrayCount - ws.counts)) {
            [ws getGroup];
        }
    }];
}

- (void)getGroup {
    NSMutableArray *newGroups = [NSMutableArray arrayWithCapacity:0];
    NSMutableArray *cons = [NSMutableArray arrayWithArray:[[DMCCIMService sharedDMCIMService] getConversationInfoTags:@[@(Group_Type)] lines:@[@(0)] tags:-2]];
    for (DMCCConversationInfo *conv in cons) {
        [newGroups addObject:conv.conversation.target];
    }
    NSArray *ids = [[DMCCIMService sharedDMCIMService] getFavGroups];
    [newGroups addObjectsFromArray:ids];
    
    NSSet *sets = [NSSet setWithArray:newGroups];
    NSArray *groups = [sets allObjects];
    
    self.counts = self.dataSource.count;
    if (groups.count == 0) {
        [self sortData];
        return;
    }
    for (NSString *groupId in groups) {
        DMCCGroupInfo *groupInfo = [[DMCCIMService sharedDMCIMService] getGroupInfo:groupId refresh:NO];
        if (groupInfo && groupInfo.tagID == -2) {
            groupInfo.target = groupId;
            groupInfo.displayName = groupInfo.name;
            [self.dataSource addObject:groupInfo];
        } else {
            // 统计已被添加的群
            self.groupCounts ++;
        }
        // 未添加的群 ==  总个数 - 已添加的群
        if (self.dataSource.count == (groups.count - self.groupCounts + self.counts)) {
            [self sortData];
        }
    }
}

- (void)sortData {
    WS(ws);
    dispatch_async(dispatch_get_global_queue(0, 0), ^{
        ws.allDataSource = [HCSortString sortAndGroupForArray:ws.dataSource PropertyName:@"displayName"];
        ws.indexDataSource = [HCSortString sortForStringAry:[ws.allDataSource allKeys]];
        
        WS(ws);
        dispatch_async(dispatch_get_main_queue(), ^{
            [ws.tableView.mj_header endRefreshing];
            [ws.tableView reloadData];
        });
    });
}

- (void)pushToGroupVC {
   
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
    
    id info = value[indexPath.row];
    if ([info isKindOfClass:[DMCCUserInfo class]]) {
        DMCCUserInfo *user = value[indexPath.row];
        [cell.icon sd_setImageWithURL:[NSURL URLWithString:user.portrait] placeholderImage:SDImageDefault];
        cell.nickName.text = user.displayName;
        cell.selectImg.image = user.isSelect ? [UIImage imageNamed:@"select_s"] : [UIImage imageNamed:@"select_n"];
    } else {
        DMCCGroupInfo *group = value[indexPath.row];
        [cell.icon sd_setImageWithURL:[NSURL URLWithString:group.portrait] placeholderImage:SDImageDefault];
        cell.nickName.text = group.name;
        cell.selectImg.image = group.isSelect ? [UIImage imageNamed:@"select_s"] : [UIImage imageNamed:@"select_n"];
    }
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    NSArray *value = [_allDataSource objectForKey:_indexDataSource[indexPath.section]];
    id info = value[indexPath.row];
    if ([info isKindOfClass:[DMCCUserInfo class]]) {
        DMCCUserInfo *user = value[indexPath.row];
        user.isSelect = !user.isSelect;
    } else {
        DMCCGroupInfo *group = value[indexPath.row];
        group.isSelect = !group.isSelect;
    }
    [self.tableView reloadData];
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section {
    ZoloContactHead *head = [tableView dequeueReusableHeaderFooterViewWithIdentifier:NSStringFromClass([ZoloContactHead class])];
    head.titleLab.text = _indexDataSource[section];
    return head;
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 30;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 0.001;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return self.scaleHeight(54);
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
