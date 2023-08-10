//
//  ZoloGroupDappListVC.m
//  NewZolo
//
//  Created by JTalking on 2022/8/30.
//

#import "ZoloGroupDappListVC.h"
#import "ZoloGroupDappCell.h"

@interface ZoloGroupDappListVC () <UITableViewDelegate, UITableViewDataSource>

@property (nonatomic, strong) UIButton *confirmBtn;
@property (nonatomic, strong) DMCCGroupInfo *info;

@property (nonatomic, assign) NSInteger index;

@end

@implementation ZoloGroupDappListVC

- (instancetype)initWithGroupInfo:(DMCCGroupInfo *)info {
    if (self = [super init]) {
        self.info = info;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    self.navigationItem.title = LocalizedString(@"SmallProgram");
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:self.confirmBtn];
    [self registerCellWithNibName:NSStringFromClass([ZoloGroupDappCell class]) isTableview:YES];
    [self getData];
}

- (void)getData {
    WS(ws);
    [[ZoloAPIManager instanceManager] getDappListWithGroupId:self.info.target withCompleteBlock:^(NSArray * _Nonnull data) {
        if (data) {
            ws.dataSource = [NSMutableArray arrayWithArray:data];
            [ws checkBingDapp];
        }
    }];
}

- (void)checkBingDapp {
    for (int i = 0; i < self.dataSource.count; i++) {
        ZoloDappModel *model = self.dataSource[i];
        if (model.ospnId.length > 0) {
            self.index = i;
            break;
        }
    }
    [self.tableView reloadData];
}

- (UIButton *)confirmBtn {
    if (!_confirmBtn) {
        _confirmBtn = [[UIButton alloc] initWithFrame:CGRectZero];
        [_confirmBtn setTitle:LocalizedString(@"Sure") forState:UIControlStateNormal];
        [_confirmBtn setTitleColor:[FontManage MHBlockColor] forState:UIControlStateNormal];
        [_confirmBtn addTarget:self action:@selector(confirmBtnClick) forControlEvents:UIControlEventTouchUpInside];
    }
    return _confirmBtn;
}

- (void)confirmBtnClick {
    ZoloDappModel *dappModel = self.dataSource[self.index];
    NSData *dataString = [dappModel.dapp_url_front dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *dataDic = [NSJSONSerialization JSONObjectWithData:dataString options:NSJSONReadingMutableContainers error:nil];
    NSString *savedLanguage = [[NSUserDefaults standardUserDefaults] stringForKey:@"ospn_language"];
    NSString *savedUserId = [[NSUserDefaults standardUserDefaults] stringForKey:@"ospn_id"];
    WS(ws);
    [[DMCCIMService sharedDMCIMService] getOwnerSign:self.info.target cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        if (isSuccess) {
            NSDictionary *dic = @{
                  @"command": @"AddDapp",
                  @"dappId": dataDic[@"target"],
                  @"grogramId": dappModel.id,
                  @"groupID": json[@"groupID"],
                  @"groupSign": json[@"groupSign"],
                  @"owner": json[@"owner"],
                  @"timestamp": json[@"timestamp"],
                  @"language": savedLanguage,
                  @"userId": savedUserId
            };
            NSString *calc = [NSString stringWithFormat:@"AddDapp%@%@%@", savedUserId, dataDic[@"target"], json[@"groupID"]];
            NSString *hash = [[DMCCIMService sharedDMCIMService] hashData:[calc dataUsingEncoding:NSUTF8StringEncoding]];
            NSString *sign = [[DMCCIMService sharedDMCIMService] signData:[hash dataUsingEncoding:NSUTF8StringEncoding]];
            NSMutableDictionary *mdic = [NSMutableDictionary dictionaryWithDictionary:dic];
            [mdic setObject:sign forKey:@"sign"];
            [[ZoloAPIManager instanceManager] addBindDappWithDic:mdic withCompleteBlock:^(BOOL isSuccess) {
                if (isSuccess) {
                    [ws.navigationController popToRootViewControllerAnimated:YES];
                }
            }];
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
    ZoloGroupDappCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloGroupDappCell class])];
    cell.dappModel = self.dataSource[indexPath.row];
    if (indexPath.row == self.index) {
        cell.statusImg.image = [UIImage imageNamed:@"select_s"];
    } else {
        cell.statusImg.image = [UIImage imageNamed:@"select_n"];
    }
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    self.index = indexPath.row;
    [self.tableView reloadData];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 0.001;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 8;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return self.scaleHeight(70);
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
