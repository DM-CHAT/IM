//
//  ZoloContactVC.m
//  NewZolo
//
//  Created by JTalking on 2022/6/30.
//

#import "ZoloContactVC.h"
#import "ZoloContactCell.h"
#import "ZoloContactHeadView.h"
#import "ZoloContactGroupVC.h"
#import "ZoloNewFriendVC.h"
#import "ZoloSingleChatVC.h"
#import "ZoloContactDetailVC.h"
#import "ZoloSmallProgramVC.h"
#import "ZoloTagVC.h"
#import "HCSortString.h"
#import "EmptyVC.h"
#import "ZoloContactHead.h"
#import "ZoloSearchViewController.h"
#import "ZoloContactNomalCell.h"

@interface ZoloContactVC () <UITableViewDelegate, UITableViewDataSource>

@property (nonatomic, strong) ZoloContactHeadView *contactHeadView;
@property (nonatomic, strong) EmptyVC *emptyVC;

@end

@implementation ZoloContactVC

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self updateBadgeNumber];
    [self getData:NO];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.tableView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
    self.tableView.separatorColor = [FontManage MHLineSeparatorColor];
    self.tableView.backgroundColor = [FontManage MHGrayColor];
    self.tableView.contentInset = UIEdgeInsetsMake(0, 0, 50 + iPhoneX_bottomH, 0);
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(deleteUserMessages) name:FZ_EVENT_DELETEUSER_STATE object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onFriendListUpdatedMessages:) name:kFriendListUpdated object:nil];
    self.tableView.tableHeaderView = self.contactHeadView;
    MJRefreshNormalHeader *header = [MJRefreshNormalHeader headerWithRefreshingTarget:self refreshingAction:@selector(userFresh)];
    header.lastUpdatedTimeLabel.hidden = YES;
    header.stateLabel.hidden = YES;
    self.tableView.mj_header = header;
    [self registerCellWithNibName:NSStringFromClass([ZoloContactCell class]) isTableview:YES];
    [self registerCellWithNibName:NSStringFromClass([ZoloContactNomalCell class]) isTableview:YES];
    [self registerHeaderFooterWithNibName:NSStringFromClass([ZoloContactHead class])];
    [self getData:NO];
    
    int count = [[DMCCIMService sharedDMCIMService] getUnreadFriendRequestStatus];
    if (count > 0) {
        MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
        if (login.json.MainDapp.length > 0) {
            [self.tabBarController.tabBar showBadgeOnItemIndex:2];
        } else {
            [self.tabBarController.tabBar showBadgeOnItemIndex:1];
        }
    }
}

- (void)onFriendListUpdatedMessages:(NSNotification *)notification  {
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(3 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [self getData:NO];
    });
}

- (void)userFresh {
    [self.dataSource removeAllObjects];
    [self getData:YES];
    [[DMCCIMService sharedDMCIMService] getFriendList:YES];
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [self.tableView.mj_header endRefreshing];
    });
}

- (void)deleteUserMessages {
    [self getData:NO];
}

- (void)getData:(BOOL)isFresh {
    [self.dataSource removeAllObjects];
    NSMutableArray *friendArray = [NSMutableArray arrayWithArray:[[DMCCIMService sharedDMCIMService] getMyFriendList:isFresh]];
    for (NSString *string in friendArray) {
        [self getUserInfo:string array:friendArray.count];
    }
    if (friendArray.count == 0) {
        [self.tableView.mj_header endRefreshing];
        [self.tableView reloadData];
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

// 编辑删除操作
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath {
    return YES;
}

- (NSArray*)tableView:(UITableView *)tableView editActionsForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewRowAction *action = [UITableViewRowAction rowActionWithStyle:UITableViewRowActionStyleDefault title:LocalizedString(@"MsgDel") handler:^(UITableViewRowAction * _Nonnull action, NSIndexPath * _Nonnull indexPath) {
        WS(ws);
        NSString *str = LocalizedString(@"AlertSureDelConcat");
        [MHAlert showCustomeAlert:str withAlerttype:MHAlertTypeWarn withOkBlock:^(UIAlertAction *action) {
            if ([action.title isEqualToString:LocalizedString(@"Sure")]) {
                [ws removeUserItem:indexPath];
            }
        }];
    }];
    action.backgroundColor = [FontManage MHMsgRecColor];
    return @[action];
}

- (void)removeUserItem:(NSIndexPath *)indexPath {
    NSArray *value = [_allDataSource objectForKey:_indexDataSource[indexPath.section]];
    DMCCUserInfo *user = value[indexPath.row];
    WS(ws);
    [MHAlert showLoadingStr:LocalizedString(@"AlertNowOpeating")];
    [[DMCCIMService sharedDMCIMService] deleteFriend:user.userId success:^{
        [MHAlert showLoadingStr:LocalizedString(@"AlertSuccess")];
        dispatch_async(dispatch_get_main_queue(), ^{
            [ws deleteChat:user];
            NSMutableArray *value = [ws.allDataSource objectForKey:ws.indexDataSource[indexPath.section]];
            [value removeObjectAtIndex:indexPath.row];
            [ws.tableView reloadData];
        });
    } error:^(int error_code) {
        [MHAlert showMessage:LocalizedString(@"NetworkError")];
    }];
}

// 删除会话
- (void)deleteChat:(DMCCUserInfo *)user {
    DMCCConversation *con = [DMCCConversation conversationWithType:Single_Type target:user.userId line:0];
    DMCCConversationInfo *info = [[DMCCIMService sharedDMCIMService] getConversationInfo:con];
    [self delTagUser:user];
    [[DMCCIMService sharedDMCIMService] clearUnreadStatus:info.conversation];
    [[DMCCIMService sharedDMCIMService] clearMessages:info.conversation];
    [[DMCCIMService sharedDMCIMService] removeConversation:info.conversation clearMessage:YES];
    [[DMCCIMService sharedDMCIMService] deleteFriendRequestWithTarget:user.userId];
    [[NSNotificationCenter defaultCenter] postNotificationName:FZ_EVENT_MSGSTATUS_STATE object:nil];
}

- (void)delTagUser:(DMCCUserInfo *)user {
    if (user.ugID == -100) {
        return;
    }
    NSMutableArray *array = [[NSMutableArray alloc] initWithCapacity:0];
    NSMutableArray *uArray = [[NSMutableArray alloc] initWithCapacity:0];
    
    [array addObject:@(user.ugID)];
    [uArray addObject:user.userId];
    
    [[ZoloAPIManager instanceManager] delTagUserList:array WithCompleteBlock:^(BOOL isSuccess) {
        if (isSuccess) {
            [[DMCCIMService sharedDMCIMService] updateUserWithUsers:uArray withTagWithkey:@[@"tagID"]];
        }
    }];
}

- (ZoloContactHeadView *)contactHeadView {
    if (!_contactHeadView) {
        _contactHeadView = [[ZoloContactHeadView alloc] initWithFrame:CGRectZero];
        WS(ws);
        _contactHeadView.selectedBlock = ^(NSInteger index) {
            [ws pushToVCWithIndex:index];
        };
        
        _contactHeadView.searchBlock = ^{
            ZoloSearchViewController *vc = [ZoloSearchViewController new];
            [ws.navigationController pushViewController:vc animated:YES];
        };
    }
    return _contactHeadView;
}

- (void)pushToVCWithIndex:(NSInteger)index {
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login.json.MainDapp.length > 0) {
        switch (index) {
            case 0: // 新朋友
            {
                [self.navigationController pushViewController:[ZoloNewFriendVC new] animated:YES];
            }
                break;
            case 1:  // 群聊
            {
                [self.navigationController pushViewController:[ZoloContactGroupVC new] animated:YES];
            }
                break;
            case 2:  // 小程序
            {
                [self.navigationController pushViewController:[ZoloSmallProgramVC new] animated:YES];
            }
                break;
            case 3:  // tag
            {
                [self.navigationController pushViewController:[ZoloTagVC new] animated:YES];
            }
                break;
                
            default:
                break;
        }
    } else {
        switch (index) {
            case 0: // 新朋友
            {
                [self.navigationController pushViewController:[ZoloNewFriendVC new] animated:YES];
            }
                break;
            case 1:  // 群聊
            {
                [self.navigationController pushViewController:[ZoloContactGroupVC new] animated:YES];
            }
                break;
            case 2:  // tag
            {
                [self.navigationController pushViewController:[ZoloTagVC new] animated:YES];
            }
                break;
                
            default:
                break;
        }
    }
 
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
    if (_indexDataSource.count == 0) {
        return 1;
    }
    return _indexDataSource.count;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    if (_allDataSource.count == 0) {
        return 1;
    }
    NSArray *value = [_allDataSource objectForKey:_indexDataSource[section]];
    return value.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    if (_allDataSource.count == 0) {
        ZoloContactNomalCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloContactNomalCell class])];
        return cell;
    }
    ZoloContactCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloContactCell class])];
    NSArray *value = [_allDataSource objectForKey:_indexDataSource[indexPath.section]];
    cell.userInfo = value[indexPath.row];
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    
    if (_indexDataSource.count == 0) {
        return;
    }
    
    NSArray *value = [_allDataSource objectForKey:_indexDataSource[indexPath.section]];
    DMCCUserInfo *user = value[indexPath.row];
    [self.navigationController pushViewController:[[ZoloContactDetailVC alloc] initWithUserInfo:user] animated:YES];
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section {
    ZoloContactHead *head = [tableView dequeueReusableHeaderFooterViewWithIdentifier:NSStringFromClass([ZoloContactHead class])];
    head.titleLab.text = _indexDataSource[section];
    head.backgroundColor = [FontManage MHWhiteColor];
    return head;
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 30;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 0.001;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    if (_allDataSource.count == 0) {
        return 400;
    }
    return self.scaleHeight(54);
}

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

// 未读计数
- (void)updateBadgeNumber {
    int count = [[DMCCIMService sharedDMCIMService] getUnreadFriendRequestStatus];
    if (count > 0) {
        MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
        if (login.json.MainDapp.length > 0) {
            [self.tabBarController.tabBar showBadgeOnItemIndex:2];
        } else {
            [self.tabBarController.tabBar showBadgeOnItemIndex:1];
        }
    }
    [self.contactHeadView refreshData];
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
