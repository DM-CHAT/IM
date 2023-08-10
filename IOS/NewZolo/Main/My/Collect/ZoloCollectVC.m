//
//  ZoloCollectVC.m
//  NewZolo
//
//  Created by MHHY on 2023/5/17.
//

#import "ZoloCollectVC.h"
#import "ZoloWalletCell.h"
#import "ZoloDappViewController.h"

@interface ZoloCollectVC ()

@end

@implementation ZoloCollectVC

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    self.navigationItem.title = LocalizedString(@"CollectData");
    [self getData];
    [self registerCellWithNibName:NSStringFromClass([ZoloWalletCell class]) isTableview:YES];
}

- (void)getData {
    self.dataSource = [NSMutableArray arrayWithArray:[[DMCCIMService sharedDMCIMService] getColletInfoList]];
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
    DMCCCollectInfo *info = self.dataSource[indexPath.row];
    NSDictionary *dic = [OsnUtils json2Dics:info.content];
    [cell.icon sd_setImageWithURL:dic[@"portrait"] placeholderImage:nil];
    cell.title.text = info.name;
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    
    DMCCCollectInfo *info = self.dataSource[indexPath.row];
    DMCCLitappInfo *appInfo = [[DMCCIMService sharedDMCIMService] getLitapp:info.osnID];
    ZoloDappViewController *vc = [[ZoloDappViewController alloc] initWithLitappInfo:appInfo];
    MHNavigationVC *nav = [[MHNavigationVC alloc] initWithRootViewController:vc];
    nav.modalPresentationStyle = UIModalPresentationFullScreen;
    [self presentViewController:nav animated:YES completion:nil];
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
