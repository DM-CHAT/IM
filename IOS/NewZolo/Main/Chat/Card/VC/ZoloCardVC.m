//
//  ZoloCardVC.m
//  NewZolo
//
//  Created by JTalking on 2022/8/5.
//

#import "ZoloCardVC.h"
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
#import "ZoloSearchHeadView.h"

@interface ZoloCardVC ()


@property (nonatomic, strong) DMCCLitappInfo *appInfo;
@property (nonatomic, strong) DMCCUserInfo *curentUser;
@property (nonatomic, strong) EmptyVC *emptyVC;
@property (strong, nonatomic) NSDictionary *allDataSource;/**<排序后的整个数据源*/
@property (strong, nonatomic) NSArray *indexDataSource;/**<索引数据源*/
@property (nonatomic, strong) UIButton *setBtn;
@property (nonatomic, strong) UIButton *cancelBtn;
@property (nonatomic, assign) NSInteger counts;
@property (nonatomic, assign) NSInteger groupCounts;
@property (nonatomic, strong) ZoloSearchHeadView *contactHeadView;
@property (nonatomic, strong) DMCCGroupInfo *curentGroup;
@property (nonatomic, assign) BOOL isSearch;
@property (nonatomic, strong) NSMutableArray *searchArray;
@property (nonatomic, assign) BOOL isGroup;
@property (nonatomic, strong) UIButton *closeBtn;

@end

@implementation ZoloCardVC

- (instancetype)initWithUserInfo:(DMCCUserInfo *)user {
    if (self = [super init]) {
        self.curentUser = user;
    }
    return self;
}

- (instancetype)initWithGroupInfo:(DMCCGroupInfo *)group {
    if (self = [super init]) {
        self.curentGroup = group;
        self.isGroup = YES;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.navigationItem.title = LocalizedString(@"ChatSelectFriend");
    self.tableView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
    self.tableView.separatorColor = [FontManage MHLineSeparatorColor];
    self.tableView.backgroundColor = [FontManage MHGrayColor];
    self.tableView.contentInset = UIEdgeInsetsMake(0, 0, 50, 0);
    self.tableView.tableHeaderView = self.contactHeadView;
    MJRefreshNormalHeader *header = [MJRefreshNormalHeader headerWithRefreshingTarget:self refreshingAction:@selector(userFresh)];
    header.lastUpdatedTimeLabel.hidden = YES;
    header.stateLabel.hidden = YES;
    self.tableView.mj_header = header;
    self.counts = 0;
    self.groupCounts = 0;
    [self registerCellWithNibName:NSStringFromClass([ZoloSettingContactCell class]) isTableview:YES];
    [self registerHeaderFooterWithNibName:NSStringFromClass([ZoloContactHead class])];
    self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:self.closeBtn];
    [self getData:NO];
}

- (void)userFresh {
    [self.dataSource removeAllObjects];
    [self getData:NO];
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [self.tableView.mj_header endRefreshing];
    });
}

- (UIButton *)closeBtn {
    if (!_closeBtn) {
        _closeBtn = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, 44, 44)];
        [_closeBtn setImage:[UIImage imageNamed:@"close_off"] forState:UIControlStateNormal];
        [_closeBtn addTarget:self action:@selector(closeBtnClick) forControlEvents:UIControlEventTouchUpInside];
    }
    return _closeBtn;
}

- (void)closeBtnClick {
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (ZoloSearchHeadView *)contactHeadView {
    if (!_contactHeadView) {
        _contactHeadView = [[ZoloSearchHeadView alloc] initWithFrame:CGRectZero];
        WS(ws);
        _contactHeadView.searchBlock = ^(NSString *str) {
            [ws searchClick:[str lowercaseString]];
        };
    }
    return _contactHeadView;
}

- (void)searchClick:(NSString *)str {
    if (str.length == 0) {
        self.isSearch = NO;
        [self.tableView reloadData];
        return;
    }
    self.isSearch = YES;
    for (id info in self.dataSource) {
        if ([info isKindOfClass:[DMCCUserInfo class]]) {
            DMCCUserInfo *user = info;
            if ([user.displayName containsString:str]) {
                [self.searchArray addObject:user];
            }
        } else {
            DMCCGroupInfo *group = info;
            if ([group.name containsString:str]) {
                [self.searchArray addObject:group];
            }
        }
    }
    
    NSArray *array = [self.searchArray mutableCopy];
    NSSet *set = [[NSSet alloc] initWithArray:array];
    self.searchArray = [NSMutableArray arrayWithArray:set.allObjects]; ;
    [self.tableView reloadData];
}

- (NSMutableArray *)searchArray {
    if (!_searchArray) {
        _searchArray = [NSMutableArray arrayWithCapacity:0];
    }
    return _searchArray;
}

#pragma mark - 消息发送
- (void)sendMessage:(DMCCMessageContent *)content userInfo:(DMCCUserInfo *)info {
    DMCCConversation *conversation = [[DMCCConversation alloc] init];
    conversation.type = Single_Type;
    conversation.target = info.userId;
    conversation.line = 0;
    
    [[DMCCIMService sharedDMCIMService] send:conversation content:content pwd:nil success:^(long long messageUid, long long timestamp) {
    } error:^(int error_code) {
        
    }];
}

- (void)sendMessage:(DMCCMessageContent *)content groupInfo:(DMCCGroupInfo *)info {
    DMCCConversation *conversation = [[DMCCConversation alloc] init];
    conversation.type = Group_Type;
    conversation.target = info.target;
    conversation.line = 0;
    
    [[DMCCIMService sharedDMCIMService] send:conversation content:content pwd:nil success:^(long long messageUid, long long timestamp) {
    } error:^(int error_code) {
        
    }];
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
    int equal = 0;
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
    }
    // 未添加的群 ==  总个数 - 已添加的群
    if (self.dataSource.count == (groups.count - equal - self.groupCounts + self.counts)) {
        [self sortData];
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
    if (_isSearch) {
        return 1;
    }
    return _indexDataSource.count;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    if (_isSearch) {
        return self.searchArray.count;
    }
    NSArray *value = [_allDataSource objectForKey:_indexDataSource[section]];
    return value.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    ZoloSettingContactCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloSettingContactCell class])];
    
    if (_isSearch) {
        
        id info = self.searchArray[indexPath.row];
        if ([info isKindOfClass:[DMCCUserInfo class]]) {
            DMCCUserInfo *user = info;
            [cell.icon sd_setImageWithURL:[NSURL URLWithString:user.portrait] placeholderImage:SDImageDefault];
            cell.nickName.text = user.displayName;
            cell.selectImg.image = user.isSelect ? [UIImage imageNamed:@"select_s"] : [UIImage imageNamed:@"select_n"];
        } else {
            DMCCGroupInfo *group = info;
            [cell.icon sd_setImageWithURL:[NSURL URLWithString:group.portrait] placeholderImage:SDImageDefault];
            cell.nickName.text = group.name;
            cell.selectImg.image = group.isSelect ? [UIImage imageNamed:@"select_s"] : [UIImage imageNamed:@"select_n"];
        }
        return cell;
    } else {
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

}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    if (_isSearch) {
        
        id info = self.searchArray[indexPath.row];
        if (_cardReturenBlock) {
            _cardReturenBlock(info);
        }
        [self dismissViewControllerAnimated:YES completion:nil];
        
    } else {
    
        NSArray *value = [_allDataSource objectForKey:_indexDataSource[indexPath.section]];
        id info = value[indexPath.row];

        if (_cardReturenBlock) {
            _cardReturenBlock(info);
        }
        [self dismissViewControllerAnimated:YES completion:nil];
        
    }
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
