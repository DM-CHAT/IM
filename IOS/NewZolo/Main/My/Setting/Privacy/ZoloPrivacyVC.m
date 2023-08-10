//
//  ZoloPrivacyVC.m
//  NewZolo
//
//  Created by JTalking on 2022/9/15.
//

#import "ZoloPrivacyVC.h"
#import "ZoloSettingCell.h"
#import "ZoloBlackListVC.h"
#import "ZoloGroupDetailCell.h"

@interface ZoloPrivacyVC ()

@property (nonatomic, strong) NSArray *titleArray;
@property (nonatomic, strong) NSArray *allowTitleArray;
@property (nonatomic, copy) NSString *allowTitleStr;
@property (nonatomic, assign) NSInteger allowTitleIndex;

@end

@implementation ZoloPrivacyVC

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [self registerCellWithNibName:NSStringFromClass([ZoloGroupDetailCell class]) isTableview:YES];
    self.titleArray = @[LocalizedString(@"ContactBlack"),LocalizedString(@"AddFriend"),LocalizedString(@"AllowRecallMsg"),LocalizedString(@"AllowDelMsg"),LocalizedString(@"AllowStrangerChat")];
    self.allowTitleArray = @[LocalizedString(@"AllowAllAdd"),LocalizedString(@"AllowAddVerify"),LocalizedString(@"AllowAddNo")];
    self.allowTitleIndex = 0;
    self.tableView.backgroundColor = [FontManage MHGrayColor];
    [self getData];
}

- (void)getData {
    WS(ws);
    [[DMCCIMService sharedDMCIMService] getUserInfo:[DMCCIMService sharedDMCIMService].getUserID refresh:YES success:^(DMCCUserInfo *userInfo) {
        if (userInfo) {
            if (userInfo.role.length > 0) {
                NSDictionary *dic = [OsnUtils json2Dics:userInfo.role];
                self.allowTitleIndex = [dic[@"AddFriend"] intValue];
                dispatch_async(dispatch_get_main_queue(), ^{
                    [ws.tableView.mj_header endRefreshing];
                    [ws.tableView reloadData];
                });
            }
        }
    } error:^(int errorCode) {
        
    }];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.titleArray.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ZoloGroupDetailCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloGroupDetailCell class])];
    cell.nameLabel.text = self.titleArray[indexPath.row];
    cell.arrowImg.hidden = YES;
    cell.remarkLabel.hidden = YES;
    cell.switchBtn.hidden = YES;
    if (indexPath.row == 0) {
        cell.arrowImg.hidden = NO;
    }
    if (indexPath.row == 1) {
        cell.remarkLabel.text = self.allowTitleStr;
        cell.arrowImg.hidden = NO;
        cell.remarkLabel.hidden = NO;
    } else if (indexPath.row == 2) {
        cell.switchBtn.hidden = NO;
        BOOL recall = [[NSUserDefaults standardUserDefaults] boolForKey:@"msg_recall"];
        [cell.switchBtn setOn:!recall];
        cell.topSwitchBlock = ^(BOOL isTop) {
            [[NSUserDefaults standardUserDefaults] setValue:@(!isTop) forKey:@"msg_recall"];
        };
    } else if (indexPath.row == 3) {
        cell.switchBtn.hidden = NO;
        BOOL del = [[NSUserDefaults standardUserDefaults] boolForKey:@"msg_del"];
        [cell.switchBtn setOn:!del];
        cell.topSwitchBlock = ^(BOOL isTop) {
            [[NSUserDefaults standardUserDefaults] setValue:@(!isTop) forKey:@"msg_del"];
        };
    } else if (indexPath.row == 4) {
        cell.switchBtn.hidden = NO;
        [cell.switchBtn setOn:[[DMCCIMService sharedDMCIMService] isAllowTemporaryChat:[DMCCIMService sharedDMCIMService].getUserID]];
        cell.topSwitchBlock = ^(BOOL isTop) {
            [[DMCCIMService sharedDMCIMService] allowTemporaryChatWithDataValue:isTop ? @"yes" : @"no" cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
                
            }];
        };
    }
    
    self.allowTitleStr = self.allowTitleArray[self.allowTitleIndex];
    
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    if (indexPath.row == 0) {
        [self.navigationController pushViewController:[ZoloBlackListVC new] animated:YES];
    } else if (indexPath.row == 1) {
        [self switchAddFriend];
    }
}

- (void)switchAddFriend {
    UIAlertController * alertController = [UIAlertController alertControllerWithTitle:LocalizedString(@"AddFriend")
                                                                              message:@""
                                                                       preferredStyle:UIAlertControllerStyleActionSheet];
    UIAlertAction * cancelAction = [UIAlertAction actionWithTitle:LocalizedString(@"Cancel") style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
    }];
    UIAlertAction *one = [UIAlertAction actionWithTitle:LocalizedString(@"AllowAllAdd") style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        self.allowTitleIndex = 0;
        [self roleSetingType];
        [self.tableView reloadData];
    }];
    UIAlertAction *two = [UIAlertAction actionWithTitle:LocalizedString(@"AllowAddVerify") style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        self.allowTitleIndex = 1;
        [self roleSetingType];
        [self.tableView reloadData];
    }];
    UIAlertAction *three = [UIAlertAction actionWithTitle:LocalizedString(@"AllowAddNo") style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        self.allowTitleIndex = 2;
        [self roleSetingType];
        [self.tableView reloadData];
    }];
    
    [alertController addAction:one];
    [alertController addAction:two];
    [alertController addAction:three];
    [alertController addAction:cancelAction];
    [self presentViewController:alertController animated:YES completion:nil];
}

- (void)roleSetingType {
    [[DMCCIMService sharedDMCIMService] roleAddFriendWithType:[NSString stringWithFormat:@"%ld", self.allowTitleIndex] cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        
    }];
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
