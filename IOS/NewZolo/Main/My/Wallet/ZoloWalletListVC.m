//
//  ZoloWalletListVC.m
//  NewZolo
//
//  Created by MHHY on 2023/4/24.
//

#import "ZoloWalletListVC.h"
#import "ZoloWalletCell.h"
#import "ZoloDappViewController.h"

@interface ZoloWalletListVC ()

@end

@implementation ZoloWalletListVC

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    self.navigationItem.title = LocalizedString(@"WalletTitle");
    [self getData];
    [self registerCellWithNibName:NSStringFromClass([ZoloWalletCell class]) isTableview:YES];
}

- (void)getData {
    self.dataSource = [NSMutableArray arrayWithArray:[[DMCCIMService sharedDMCIMService] getWalletInfoList]];
    if (self.dataSource.count == 0) {
        [self.navigationController popViewControllerAnimated:YES];
    }
    [self.tableView reloadData];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.dataSource.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ZoloWalletCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloWalletCell class])];
    DMCCWalletInfo *info = self.dataSource[indexPath.row];
    NSDictionary *dic = [OsnUtils json2Dics:info.wallect];
    [cell.icon sd_setImageWithURL:dic[@"portrait"] placeholderImage:nil];
    cell.title.text = info.name;
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    
    DMCCWalletInfo *walletInfo = self.dataSource[indexPath.row];
    NSDictionary *dic = [OsnUtils json2Dics:walletInfo.wallect];
    DMCCLitappInfo *litapp = [DMCCLitappInfo new];
    litapp.url = walletInfo.url;
    litapp.target = walletInfo.osnID;
    litapp.name = dic[@"name"];
    litapp.displayName = dic[@"displayName"];
    litapp.portrait = dic[@"portrait"];
    
    ZoloDappViewController *vc = [[ZoloDappViewController alloc] initWithLitappInfo:litapp];
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
    DMCCWalletInfo *wallet = self.dataSource[indexPath.row];
    [[DMCCIMService sharedDMCIMService] deleteWalletWithInfo:wallet];
    [self getData];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 0.001;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 8;
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
