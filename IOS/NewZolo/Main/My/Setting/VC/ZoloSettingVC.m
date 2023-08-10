//
//  ZoloSettingVC.m
//  NewZolo
//
//  Created by JTalking on 2022/6/30.
//

#import "ZoloSettingVC.h"
#import "ZoloSettingCell.h"
#import "ZoloSettingFoot.h"
#import "AppDelegate.h"
#import "ZoloThemeVC.h"
#import "ZoloAccoutSecityVC.h"
#import "ZoloPrivacyVC.h"
#import "ZoloWalletViewController.h"
#import "ZoloVersionViewController.h"
#import "ZoloBipAccoutSecityVC.h"

@interface ZoloSettingVC () <UITableViewDelegate, UITableViewDataSource>

@property (nonatomic, strong) NSArray *titleArray;

@end

@implementation ZoloSettingVC

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    self.navigationItem.title = LocalizedString(@"String");
    self.titleArray = @[LocalizedString(@"PrivacySetting"), LocalizedString(@"AccountSecurity"), LocalizedString(@"SettingLaunguage"), LocalizedString(@"AppPrivacyPolicy"), LocalizedString(@"BundleNumber")];
    [self registerCellWithNibName:NSStringFromClass([ZoloSettingCell class]) isTableview:YES];
    [self registerHeaderFooterWithNibName:NSStringFromClass([ZoloSettingFoot class])];
    self.tableView.backgroundColor = [FontManage MHGrayColor];
    self.tableView.separatorColor = [FontManage MHLineSeparatorColor];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.titleArray.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ZoloSettingCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloSettingCell class])];
    cell.titleName.text = self.titleArray[indexPath.row];
    cell.arrowImg.hidden = NO;
    cell.remarkLab.hidden = YES;
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    if (indexPath.row == 2) {
        [self switchLanguage];
    } else if (indexPath.row == 1) {
        NSString *current_account = [[NSUserDefaults standardUserDefaults]objectForKey:@"mne_account_current"];
        if (current_account) {
            [self.navigationController pushViewController:[ZoloBipAccoutSecityVC new] animated:YES];
        } else {
            [self.navigationController pushViewController:[ZoloAccoutSecityVC new] animated:YES];
        }

    } else if (indexPath.row == 0) {
        [self.navigationController pushViewController:[ZoloPrivacyVC new] animated:YES];
    } else if (indexPath.row == 3) {
        [self.navigationController pushViewController:[[ZoloWalletViewController alloc] initWithUrlInfo:LocalizedString(@"AppPrivacyPolicyWeb") isHidden:YES] animated:YES];
    } else if (indexPath.row == 4) {
        [self.navigationController pushViewController:[ZoloVersionViewController new] animated:YES];
    }
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 0.001;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 280;
}

- (UIView *)tableView:(UITableView *)tableView viewForFooterInSection:(NSInteger)section {
    ZoloSettingFoot *foot = [tableView dequeueReusableHeaderFooterViewWithIdentifier:NSStringFromClass([ZoloSettingFoot class])];
    [foot.quitBtn setTitle:LocalizedString(@"QuiltLogin") forState:UIControlStateNormal];
    
    WS(ws);
    foot.quitBlock = ^{
        NSString *str = LocalizedString(@"AlertQuiteLogin");
        [MHAlert showCustomeAlert:str withAlerttype:MHAlertTypeWarn withOkBlock:^(UIAlertAction *action) {
            if ([action.title isEqualToString:LocalizedString(@"Sure")]) {
                [[NSUserDefaults standardUserDefaults] removeObjectForKey:@"ospn_token"];
                [[NSUserDefaults standardUserDefaults] removeObjectForKey:@"ospn_id"];
                [[NSUserDefaults standardUserDefaults] removeObjectForKey:@"mne_account_current"];
                [[ZoloInfoManager sharedUserManager] loginOut];
                [[DMCCNetworkService sharedInstance] disconnect:YES clearSession:YES];
                AppDelegate *appDelegate = (AppDelegate *)[UIApplication sharedApplication].delegate;
                [appDelegate setRootView];
            }
        }];
    };
    
    [foot.deleteBtn setTitle:LocalizedString(@"DeleteAcount") forState:UIControlStateNormal];
    foot.deleteBlock = ^{
        NSString *str = LocalizedString(@"AlertSureDelContent");
        [MHAlert showCustomeAlert:str withAlerttype:MHAlertTypeWarn withOkBlock:^(UIAlertAction *action) {
            if ([action.title isEqualToString:LocalizedString(@"Sure")]) {
                [ws deleteUser];
            }
        }];
    };
    
    return foot;
}

- (void)deleteUser {
    [[ZoloAPIManager instanceManager] deleteUserWithCompleteBlock:^(BOOL isSuccess) {
        if (isSuccess) {
            [[NSUserDefaults standardUserDefaults] removeObjectForKey:@"ospn_token"];
            [[NSUserDefaults standardUserDefaults] removeObjectForKey:@"ospn_id"];
            [[NSUserDefaults standardUserDefaults] removeObjectForKey:@"mne_account_current"];
            [[ZoloInfoManager sharedUserManager] loginOut];
            [[DMCCNetworkService sharedInstance] disconnect:YES clearSession:YES];
            AppDelegate *appDelegate = (AppDelegate *)[UIApplication sharedApplication].delegate;
            [appDelegate setRootView];
        }
    }];
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    if (indexPath.row == 3) {
        return 0.001;
    }
    return self.scaleHeight(54);
}
    
// 设置语言
- (void)setLanguage:(NSString*)language {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:LocalizedString(@"Alert")
                                                                   message:LocalizedString(@"LaunguageSuccessAlert")
                                                            preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *sureAction = [UIAlertAction actionWithTitle:LocalizedString(@"Sure") style:UIAlertActionStyleDefault
       handler:^(UIAlertAction * action) {
        [OSNLanguage setLanguage:language];
        AppDelegate *appDelegate = (AppDelegate *)[UIApplication sharedApplication].delegate;
        [appDelegate setRootView];
    }];
    [alert addAction:sureAction];
    [self presentViewController:alert animated:NO completion:nil];
}
- (void)switchLanguage {
    UIAlertController * alertController = [UIAlertController alertControllerWithTitle:LocalizedString(@"Launguage")
                                                                              message:LocalizedString(@"LaunguageAlert")
                                                                       preferredStyle:UIAlertControllerStyleActionSheet];
    UIAlertAction * cancelAction = [UIAlertAction actionWithTitle:LocalizedString(@"Cancel") style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
    }];
    UIAlertAction * chinese = [UIAlertAction actionWithTitle:LocalizedString(@"LaunguageOne") style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [self setLanguage:@"0"];
    }];
    UIAlertAction * vitnam = [UIAlertAction actionWithTitle:LocalizedString(@"LaunguageThree") style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [self setLanguage:@"1"];
    }];
    UIAlertAction * english = [UIAlertAction actionWithTitle:LocalizedString(@"LaunguageTwo") style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [self setLanguage:@"2"];
    }];
    UIAlertAction * Japanese = [UIAlertAction actionWithTitle:LocalizedString(@"LaunguageFour") style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [self setLanguage:@"3"];
    }];
    UIAlertAction * Korean = [UIAlertAction actionWithTitle:LocalizedString(@"LaunguageFive") style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [self setLanguage:@"4"];
    }];
    UIAlertAction * German = [UIAlertAction actionWithTitle:LocalizedString(@"LaunguageSix") style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [self setLanguage:@"5"];
    }];
    UIAlertAction * chinese_tr = [UIAlertAction actionWithTitle:LocalizedString(@"LaunguageSaven") style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [self setLanguage:@"6"];
    }];
    UIAlertAction * Spanish = [UIAlertAction actionWithTitle:LocalizedString(@"LaunguageEight") style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [self setLanguage:@"7"];
    }];
    UIAlertAction * Turkish = [UIAlertAction actionWithTitle:LocalizedString(@"LaunguageNine") style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [self setLanguage:@"8"];
    }];
    UIAlertAction * Ids = [UIAlertAction actionWithTitle:LocalizedString(@"LaunguageTen") style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [self setLanguage:@"9"];
    }];
    [alertController addAction:chinese];
    [alertController addAction:english];
    [alertController addAction:vitnam];
    [alertController addAction:Japanese];
    [alertController addAction:Korean];
    [alertController addAction:German];
    [alertController addAction:chinese_tr];
    [alertController addAction:Spanish];
    [alertController addAction:Turkish];
    [alertController addAction:Ids];
    [alertController addAction:cancelAction];
        
    [self presentViewController:alertController animated:YES completion:nil];
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
