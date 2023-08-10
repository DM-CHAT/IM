//
//  ZoloCustomViewController.m
//  NewZolo
//
//  Created by JTalking on 2022/9/14.
//

#import "ZoloCustomViewController.h"
#import "ZoloContactCell.h"
#import "ZoloSingleChatVC.h"
#import "ZoloAddFriendInfoVC.h"

@interface ZoloCustomViewController ()

@end

@implementation ZoloCustomViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [self registerCellWithNibName:NSStringFromClass([ZoloContactCell class]) isTableview:YES];
    MJRefreshNormalHeader *header = [MJRefreshNormalHeader headerWithRefreshingTarget:self refreshingAction:@selector(getData)];
    header.lastUpdatedTimeLabel.hidden = YES;
    header.stateLabel.hidden = YES;
    self.tableView.mj_header = header;
    [self getData];
}

- (void)getData {
    WS(ws);
    [[ZoloAPIManager instanceManager] getCustomListWithCompleteBlock:^(NSDictionary * _Nonnull data) {
        [ws.tableView.mj_header endRefreshing];
        if (data) {
            NSDictionary *dic = [OsnUtils json2Dics:data[@"data"]];
            ws.dataSource = [NSMutableArray arrayWithArray:dic[@"kf"]];
            [ws.tableView reloadData];
        }
    }];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.dataSource.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ZoloContactCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloContactCell class])];
    DMCCUserInfo *user = [[DMCCIMService sharedDMCIMService] getUserInfo:self.dataSource[indexPath.row] refresh:YES];
    cell.userInfo = user;
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    
    NSString *target = self.dataSource[indexPath.row];
    
    [MHAlert showLoadingStr:LocalizedString(@"AlertOpreating")];
    WS(ws);
    [[DMCCIMService sharedDMCIMService] getUserInfo:target refresh:true success:^(DMCCUserInfo *userInfo) {
        [MHAlert dismiss];
        
        dispatch_async(dispatch_get_main_queue(), ^{
            // 判断好友是否已经存在
            if ([[DMCCIMService sharedDMCIMService] isMyFriend:userInfo.userId]) {
                DMCCConversationInfo *info = [DMCCConversationInfo new];
                DMCCConversation *conversation = [DMCCConversation conversationWithType:Single_Type target:target line:0];
                info.conversation = conversation;
                [self.navigationController pushViewController:[[ZoloSingleChatVC alloc] initWithConversationInfo:info] animated:YES];
            } else {
                ZoloAddFriendInfoVC *vc = [[ZoloAddFriendInfoVC alloc] initWithUserInfo:userInfo];
                [ws.navigationController pushViewController:vc animated:YES];
            }
        });
    } error:^(int errorCode) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [MHAlert showMessage:LocalizedString(@"AlertAddFail")];
        });
    }];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 0.001;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 8;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return self.scaleHeight(64);
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
