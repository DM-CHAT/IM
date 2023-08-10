//
//  ZoloNewFriendVC.m
//  NewZolo
//
//  Created by JTalking on 2022/6/30.
//

#import "ZoloNewFriendVC.h"
#import "ZoloNewFriendCell.h"
#import "EmptyVC.h"
#import "ZoloSearchView.h"
#import "ZoloAddFriendVC.h"
#import "ZoloContactDetailVC.h"
#import "ZoloAddFriendInfoVC.h"

@interface ZoloNewFriendVC ()

@property (nonatomic, strong) EmptyVC *emptyVC;
@property (nonatomic, strong) ZoloSearchView *searchView;

@end

@implementation ZoloNewFriendVC

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    self.navigationItem.title = LocalizedString(@"NewFriend");
    [self getData];
    [self registerCellWithNibName:NSStringFromClass([ZoloNewFriendCell class]) isTableview:YES];
    self.view.backgroundColor = [FontManage MHWhiteColor];
    self.tableView.tableHeaderView = self.searchView;
    MJRefreshNormalHeader *header = [MJRefreshNormalHeader headerWithRefreshingTarget:self refreshingAction:@selector(getData)];
    header.lastUpdatedTimeLabel.hidden = YES;
    header.stateLabel.hidden = YES;
    self.tableView.mj_header = header;
}

- (void)getData {
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [self.tableView.mj_header endRefreshing];
    });
    int count = [[DMCCIMService sharedDMCIMService] getUnreadFriendRequestStatus];
    if (count > 0) {
        MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
        if (login.json.MainDapp.length > 0) {
            [self.tabBarController.tabBar hideBadgeOnItemIndex:2];
        } else {
            [self.tabBarController.tabBar hideBadgeOnItemIndex:1];
        }
        [[DMCCIMService sharedDMCIMService] clearUnreadFriendRequestStatus];
    }

    NSArray *array = [self removeData:[NSMutableArray arrayWithArray:[[DMCCIMService sharedDMCIMService] getIncommingFriendRequest]]];
    self.dataSource = [NSMutableArray arrayWithArray:array];
    
    [self.tableView reloadData];
    
    if (self.dataSource == nil || self.dataSource.count == 0) {
        //  没有数据
        [self.view insertSubview:self.emptyVC.view aboveSubview:self.tableView];
    }else {
        [self.emptyVC.view removeFromSuperview];
    }
}

// 数据去重
- (NSArray *)removeData:(NSArray *)array {
    
    NSMutableArray *newList = [NSMutableArray arrayWithCapacity:0];
    for (DMCCFriendRequest *req in array) {
        if ([req.target containsString:@"OSNU"]) {
            if (![self isExistUser:newList withUser:req.target]) {
                [newList addObject:req];
            }
        } else if ([req.target containsString:@"OSNG"]) {
            if (![self isExistGroup:newList withUser:req.userID withGroup:req.target]) {
                [newList addObject:req];
            }
        }
    }
    
    return newList;
}

- (BOOL)isExistUser:(NSArray *)list withUser:(NSString *)user {
    for (DMCCFriendRequest *req in list) {
        if ([req.target isEqualToString:user]) {
            return YES;
        }
    }
    
    return NO;
}

- (BOOL)isExistGroup:(NSArray *)list withUser:(NSString *)user withGroup:(NSString *)group {
    for (DMCCFriendRequest *req in list) {
        if ([req.target isEqualToString:group] && [req.userID isEqualToString:user]) {
            return YES;
        }
    }
    return NO;
}

- (ZoloSearchView *)searchView {
    if (!_searchView) {
        _searchView = [[ZoloSearchView alloc] initWithFrame:CGRectZero];
        WS(ws);
        _searchView.searchBlock = ^{
            [ws.navigationController pushViewController:[ZoloAddFriendVC new] animated:YES];
        };
    }
    return _searchView;
}

- (EmptyVC *)emptyVC {
    if (!_emptyVC) {
        _emptyVC = [[EmptyVC alloc] init];
        _emptyVC.view.frame = CGRectMake(0, 150, KScreenWidth, KScreenHeight);
    }
    return _emptyVC;
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.dataSource.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ZoloNewFriendCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloNewFriendCell class])];
    cell.info = self.dataSource[indexPath.row];
    WS(ws);
    cell.userBtnBlock = ^{
        [ws addFriend:indexPath.row];
    };
    cell.iconBtnBlock = ^{
        [ws iconBtnBlockClick:indexPath.row];
    };
    return cell;
}

- (void)iconBtnBlockClick:(NSInteger)index {
    DMCCFriendRequest *info = self.dataSource[index];
    DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:info.target refresh:NO];
    if ([[DMCCIMService sharedDMCIMService] isMyFriend:userInfo.userId]) {
        [self.navigationController pushViewController:[[ZoloContactDetailVC alloc] initWithUserInfo:userInfo] animated:YES];
    } else {
        ZoloAddFriendInfoVC *vc = [[ZoloAddFriendInfoVC alloc] initWithUserInfo:userInfo];
        [self.navigationController pushViewController:vc animated:YES];
    }
}

- (void)addFriend:(NSInteger)index {
    DMCCFriendRequest *user = self.dataSource[index];
    
    if (user.type == 0) {
        [MHAlert showLoadingStr:LocalizedString(@"AlertNowOpeating")];
        WS(ws);
        [[DMCCIMService sharedDMCIMService] handleFriendRequest:user.target accept:YES extra:nil success:^{
            [MHAlert showMessage:LocalizedString(@"AlertAddSucess")];
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                [ws getData];
            });
        } error:^(int error_code) {
            [MHAlert showMessage:LocalizedString(@"AlertAddFail")];
        }];
    } else {
        [MHAlert showLoadingStr:LocalizedString(@"AlertNowOpeating")];
        WS(ws);
        [[DMCCIMService sharedDMCIMService] handleFriendAddGroupRequest:user.userID accept:YES tagart:user.target extra:nil success:^{
            [MHAlert showMessage:LocalizedString(@"AlertAddSucess")];
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                [ws getData];
            });
        } error:^(int error_code) {
            [MHAlert showMessage:LocalizedString(@"AlertAddFail")];
        }];
    }
  
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 0.001;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 0.001;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return self.scaleHeight(80);
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
