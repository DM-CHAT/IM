//
//  ZoloTopMsgVC.m
//  NewZolo
//
//  Created by JTalking on 2022/11/30.
//

#import "ZoloTopMsgVC.h"
#import "ZoloTopMsgViewCell.h"

@interface ZoloTopMsgVC ()

@property (nonatomic, copy) NSString *groupId;
@property (nonatomic, strong) UIButton *closeBtn;

@end

@implementation ZoloTopMsgVC

- (instancetype)initWithGroupId:(NSString *)groupId {
    if (self = [super init]) {
        self.groupId = groupId;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [self registerCellWithNibName:NSStringFromClass([ZoloTopMsgViewCell class]) isTableview:YES];
    [self getData];
    self.navigationItem.title = LocalizedString(@"TopMsgSeting");
    MJRefreshNormalHeader *header = [MJRefreshNormalHeader headerWithRefreshingTarget:self refreshingAction:@selector(getData)];
    header.lastUpdatedTimeLabel.hidden = YES;
    header.stateLabel.hidden = YES;
    self.tableView.mj_header = header;
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:self.closeBtn];
    
}

- (UIButton *)closeBtn {
    if (!_closeBtn) {
        _closeBtn = [[UIButton alloc] initWithFrame:CGRectZero];
        [_closeBtn setImage:[UIImage imageNamed:@"allDel"] forState:UIControlStateNormal];
        [_closeBtn setTitleColor:[FontManage MHMainColor] forState:UIControlStateNormal];
        [_closeBtn addTarget:self action:@selector(delAllBtnClick) forControlEvents:UIControlEventTouchUpInside];
    }
    return _closeBtn;
}

- (void)delAllBtnClick {
    NSString *topStr = LocalizedString(@"AlertClear");
    WS(ws);
    [MHAlert showCustomeAlert:topStr withAlerttype:MHAlertTypeWarn withOkBlock:^(UIAlertAction *action) {
        if ([action.title isEqualToString:LocalizedString(@"Sure")]) {
            [ws removeAllData];
        }
    }];
}

- (void)removeAllData {
    if ([self isGroupOwner] || [self isGroupManger]) {
        if (self.dataSource.count > 0) {
            [MHAlert showLoadingStr:LocalizedString(@"AlertNowOpeating")];
            NSMutableArray *keys = [NSMutableArray arrayWithCapacity:0];
            for (NSString *str in self.dataSource) {
                NSDictionary *dic = [OsnUtils json2Dics:str];
                [keys addObject:dic[@"key"]];
            }
            WS(ws);
            if (keys.count > 0) {
                [[DMCCIMService sharedDMCIMService] removeTopChatsWithGroupId:self.groupId keysData:keys cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
                    if (isSuccess) {
                        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                            [MHAlert dismiss];
                            [ws getData];
                        });
                    }
                }];
            }
        }
    } else {
        [MHAlert showMessage:LocalizedString(@"AlertNoOurthSee")];
    }
}

- (BOOL)isGroupOwner {
    DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:self.groupId refresh:NO];
    return [group.owner isEqualToString:[DMCCNetworkService sharedInstance].userId];
}

- (BOOL)isGroupManger {
    DMCCGroupMember *gm = [[DMCCIMService sharedDMCIMService] getGroupMember:self.groupId memberId:[DMCCNetworkService sharedInstance].userId];
    return gm.type == Member_Type_Manager;
}

- (void)getData {
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [self.tableView.mj_header endRefreshing];
    });
    self.dataSource = [NSMutableArray arrayWithArray:[[DMCCIMService sharedDMCIMService] getGroupTopMessageWithGroupId:self.groupId]];
    [self.tableView reloadData];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.dataSource.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ZoloTopMsgViewCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloTopMsgViewCell class])];
    cell.nameLab.textColor = [FontManage MHTopMsgColor];
    cell.titleLab.textColor = [FontManage MHTopMsgColor];
    NSString *str = self.dataSource[indexPath.row];
    NSDictionary *dic = [OsnUtils json2Dics:str];
    cell.nameLab.text = dic[@"data"];
    DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:dic[@"fromUser"] refresh:NO];
    cell.titleLab.text = userInfo.displayName;
    
    NSString *timeStr = dic[@"key"];
    NSString *time = [timeStr componentsSeparatedByString:@"_"].lastObject;
    cell.timeLab.text = [MHDateTools timeStampToString:time.longLongValue];
    WS(ws);
    cell.delBtnBlock = ^{
        [ws delBtnClick:indexPath.row];
    };
    return cell;
}

- (void)delBtnClick:(NSInteger)row {
    if ([self isGroupOwner] || [self isGroupManger]) {
        NSString *str = self.dataSource[row];
        NSDictionary *dic = [OsnUtils json2Dics:str];
        WS(ws);
        NSString *topStr = LocalizedString(@"CancelTopMsg");
        [MHAlert showCustomeAlert:topStr withAlerttype:MHAlertTypeWarn withOkBlock:^(UIAlertAction *action) {
            if ([action.title isEqualToString:LocalizedString(@"Sure")]) {
                [ws removeTopMsg:dic];
            }
        }];
    } else {
        [MHAlert showMessage:LocalizedString(@"AlertNoOurthSee")];
    }
}

- (void)removeTopMsg:(NSDictionary *)dic {
    [MHAlert showLoadingStr:LocalizedString(@"AlertOpreating")];
    WS(ws);
    [[DMCCIMService sharedDMCIMService] removeTopChatsWithGroupId:self.groupId keyData:dic[@"key"] cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        [MHAlert dismiss];
        if (isSuccess) {
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.5 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                [ws getData];
            });
        }
    }];
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 0.001;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 8;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    NSString *str = self.dataSource[indexPath.row];
    NSDictionary *dic = [OsnUtils json2Dics:str];
    CGFloat height = [MHHelperUtils cellTextHeight:dic[@"data"] font:17 textWith:KScreenWidth - 70];
    return height + 80;
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
