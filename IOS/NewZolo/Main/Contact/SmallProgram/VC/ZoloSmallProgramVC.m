//
//  ZoloSmallProgramVC.m
//  NewZolo
//
//  Created by JTalking on 2022/7/7.
//

#import "ZoloSmallProgramVC.h"
#import "ZoloDappTableViewCell.h"
#import "ZoloDappViewController.h"
#import "EmptyVC.h"

@interface ZoloSmallProgramVC ()

@property (nonatomic, strong) EmptyVC *emptyVC;

@end

@implementation ZoloSmallProgramVC

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.navigationItem.title = LocalizedString(@"SmallProgram");
    [self registerCellWithNibName:NSStringFromClass([ZoloDappTableViewCell class]) isTableview:YES];
    MJRefreshNormalHeader *header = [MJRefreshNormalHeader headerWithRefreshingTarget:self refreshingAction:@selector(userFresh)];
    header.lastUpdatedTimeLabel.hidden = YES;
    header.stateLabel.hidden = YES;
    self.tableView.mj_header = header;
    [self userFresh];
}

- (void)userFresh {
    [self.dataSource removeAllObjects];
    WS(ws);
    [[ZoloAPIManager instanceManager] getUserDappWithCompleteBlock:^(BOOL isSuccess) {
        [ws getData];
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            [ws.tableView.mj_header endRefreshing];
        });
    }];
}

- (void)getData {
    self.dataSource = [NSMutableArray arrayWithArray:[[DMCCIMService sharedDMCIMService] getLitAppList]];
    [self.tableView reloadData];
    if (self.dataSource == nil || self.dataSource.count == 0) {
        //  没有数据
        [self.view insertSubview:self.emptyVC.view aboveSubview:self.tableView];
    }else {
        [self.emptyVC.view removeFromSuperview];
    }
}

- (EmptyVC *)emptyVC {
    if (!_emptyVC) {
        _emptyVC = [[EmptyVC alloc] init];
        _emptyVC.view.frame = CGRectMake(0, 0, KScreenWidth, KScreenHeight);
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
    ZoloDappTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloDappTableViewCell class])];
    DMCCLitappInfo *info = self.dataSource[indexPath.row];
    [cell.iconImg sd_setImageWithURL:[NSURL URLWithString:info.portrait] placeholderImage:SDImageDefault];
    cell.nickName.text = info.name;
    
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    DMCCLitappInfo *info = self.dataSource[indexPath.row];
    ZoloDappViewController *vc = [[ZoloDappViewController alloc] initWithLitappInfo:info];
    MHNavigationVC *nav = [[MHNavigationVC alloc] initWithRootViewController:vc];
    nav.modalPresentationStyle = UIModalPresentationFullScreen;
    [self presentViewController:nav animated:YES completion:nil];
}

// 编辑删除操作
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath {
    return YES;
}

- (NSArray*)tableView:(UITableView *)tableView editActionsForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewRowAction *action = [UITableViewRowAction rowActionWithStyle:UITableViewRowActionStyleDefault title:LocalizedString(@"MsgDel") handler:^(UITableViewRowAction * _Nonnull action, NSIndexPath * _Nonnull indexPath) {
        WS(ws);
        NSString *str = LocalizedString(@"AlertSureDelLitapp");
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
    DMCCLitappInfo *litapp = self.dataSource[indexPath.row];
    [[DMCCIMService sharedDMCIMService] deleteLitappWithInfo:litapp];
    WS(ws);
    [[ZoloAPIManager instanceManager] deleteDappWithId:litapp.sid WithCompleteBlock:^(BOOL isSuccess) {
        [ws getData];
    }];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 0.001;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 8;
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
