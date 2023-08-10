//
//  ZoloBlackListVC.m
//  NewZolo
//
//  Created by JTalking on 2022/9/22.
//

#import "ZoloBlackListVC.h"
#import "ZoloContactCell.h"
#import "EmptyVC.h"

@interface ZoloBlackListVC ()
@property (nonatomic, strong) EmptyVC *emptyVC;
@end

@implementation ZoloBlackListVC

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [self getData];
    
    self.navigationItem.title = LocalizedString(@"ContactBlack");
    [self registerCellWithNibName:NSStringFromClass([ZoloContactCell class]) isTableview:YES];
}

- (EmptyVC *)emptyVC {
    if (!_emptyVC) {
        _emptyVC = [[EmptyVC alloc] init];
        _emptyVC.view.frame = CGRectMake(0, 0, KScreenWidth, KScreenHeight);
    }
    return _emptyVC;
}

- (void)getData {
    self.dataSource = [NSMutableArray arrayWithArray:[[DMCCIMService sharedDMCIMService] getBlackList:YES]];
    [self.tableView reloadData];
    if (self.dataSource == nil || self.dataSource.count == 0) {
        //  没有数据
        [self.tableView insertSubview:self.emptyVC.view aboveSubview:self.tableView];
    }else {
        [self.emptyVC.view removeFromSuperview];
    }
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.dataSource.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ZoloContactCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloContactCell class])];
    DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService]getUserInfo:self.dataSource[indexPath.row] refresh:false];
    cell.userInfo = userInfo;
    return cell;
}

// 编辑删除操作
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath {
    return YES;
}

- (NSArray*)tableView:(UITableView *)tableView editActionsForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewRowAction *action = [UITableViewRowAction rowActionWithStyle:UITableViewRowActionStyleDefault title:@"移出" handler:^(UITableViewRowAction * _Nonnull action, NSIndexPath * _Nonnull indexPath) {
        WS(ws);
        NSString *str = LocalizedString(@"AlertSureDelBlack");
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
    DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService]getUserInfo:self.dataSource[indexPath.row] refresh:false];
    WS(ws);
    [[DMCCIMService sharedDMCIMService] setBlackList:userInfo.userId isBlackListed:NO success:^{
        [MHAlert showMessage:LocalizedString(@"AlertSuccess")];
        [ws getData];
    } error:^(int error_code) {
        [MHAlert showMessage:LocalizedString(@"AlertOpearFail")];
    }];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
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
