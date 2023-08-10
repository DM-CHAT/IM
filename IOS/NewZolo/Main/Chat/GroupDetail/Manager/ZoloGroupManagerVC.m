//
//  ZoloGroupManagerVC.m
//  NewZolo
//
//  Created by JTalking on 2022/8/18.
//

#import "ZoloGroupManagerVC.h"
#import "ZoloGroupDetailCell.h"
#import "ZoloSettingManagerVC.h"
#import "ZoloSettingMuteVC.h"
#import "ZoloKeywordVC.h"
#import "ZoloGroupDappListVC.h"

@interface ZoloGroupManagerVC () <UITableViewDelegate, UITableViewDataSource, UIActionSheetDelegate>

@property (nonatomic, strong) NSArray *titleArray;

@property (nonatomic, strong) NSArray *typeArray;
@property (nonatomic, strong) NSArray *addArray;
@property (nonatomic, strong) NSArray *authArray;
@property (nonatomic, strong) NSArray *speedArray;
@property (nonatomic, strong) NSArray *timeArray;
@property (nonatomic, strong) DMCCLitappInfo *litapp;

@property (nonatomic, strong) DMCCGroupInfo *info;

@end

@implementation ZoloGroupManagerVC

- (instancetype)initWithGroupInfo:(DMCCGroupInfo *)info withDapp:(DMCCLitappInfo *)dapp {
    if (self = [super init]) {
        self.info = info;
        self.litapp = dapp;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.titleArray = @[@[LocalizedString(@"GroupManagerSetManger"), LocalizedString(@"GroupManagerSetMute"), LocalizedString(@"ContactAddUser"), LocalizedString(@"AllowFowrdMsg"), LocalizedString(@"AllowCopyMsg")], @[LocalizedString(@"GroupManagerType"), LocalizedString(@"GroupManagerAdd"),LocalizedString(@"GroupManagerVeriy"), LocalizedString(@"SettingSendSpeed"), LocalizedString(@"SetingKeyword"), LocalizedString(@"TimeClearMsg")], @[LocalizedString(@"ContactAddDapp")]];

    self.typeArray = @[LocalizedString(@"GroupManagerSetFriend"), LocalizedString(@"GroupManagerSetOpen"), LocalizedString(@"GroupManagerSetLimit")];
    self.addArray = @[LocalizedString(@"AddGroupAll"), LocalizedString(@"AddGroupMember"), LocalizedString(@"AddGroupAdmin"), LocalizedString(@"AddGroupVerity"), LocalizedString(@"AddGroupPwd")];
    self.authArray = @[LocalizedString(@"GroupManagerVeriyNo"), LocalizedString(@"GroupManagerVeriyYes")];
    self.speedArray = @[@"0", @"3", @"5", @"15", @"30", @"60"];
    self.timeArray = @[LocalizedString(@"TimeClearChatOne"), LocalizedString(@"TimeClearChatTwo"), LocalizedString(@"TimeClearChatThree"), LocalizedString(@"TimeClearChatFour"), LocalizedString(@"TimeClearChatFive"), LocalizedString(@"TimeClearChatNo")];
    
    [self registerCellWithNibName:NSStringFromClass([ZoloGroupDetailCell class]) isTableview:YES];
}

- (BOOL)isGroupOwner {
    return [self.info.owner isEqualToString:[DMCCNetworkService sharedInstance].userId];
}

- (BOOL)isGroupManger {
    DMCCGroupMember *gm = [[DMCCIMService sharedDMCIMService] getGroupMember:self.info.target memberId:[DMCCNetworkService sharedInstance].userId];
    return gm.type == Member_Type_Manager;
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login.json.MainDapp.length > 0) {
        return 3;
    } else {
        return 2;
    }
}
 
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    NSArray *array = self.titleArray[section];
    return array.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ZoloGroupDetailCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloGroupDetailCell class])];
    NSArray *array = self.titleArray[indexPath.section];
    cell.nameLabel.text = array[indexPath.row];
    cell.switchBtn.hidden = YES;
    if (indexPath.section == 0) {
        cell.remarkLabel.hidden = YES;
        cell.switchBtn.hidden = YES;
        cell.arrowImg.hidden = NO;
        if (indexPath.row == 2) {
           // 允许互加好友
           cell.remarkLabel.hidden = YES;
           cell.switchBtn.hidden = NO;
           cell.arrowImg.hidden = YES;
           [cell.switchBtn setOn:[[DMCCIMService sharedDMCIMService] isAllowAddFriendGroupMemberName:self.info.target]];
           cell.topSwitchBlock = ^(BOOL isTop) {
               [[DMCCIMService sharedDMCIMService] allowAddFriend:self.info.target data:isTop cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
                  
               }];
           };
       } else if (indexPath.row == 3) {
           // 允许转发消息
           cell.remarkLabel.hidden = YES;
           cell.switchBtn.hidden = NO;
           cell.arrowImg.hidden = YES;
           [cell.switchBtn setOn:[[DMCCIMService sharedDMCIMService] isForwardGroupMemberName:self.info.target]];
           cell.topSwitchBlock = ^(BOOL isTop) {
               [[DMCCIMService sharedDMCIMService] allowForwardWithGroupId:self.info.target data:isTop?@"yes":@"no" cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
                 
               }];
           };
       } else if (indexPath.row == 4) {
           // 允许复制消息
           cell.remarkLabel.hidden = YES;
           cell.switchBtn.hidden = NO;
           cell.arrowImg.hidden = YES;
           [cell.switchBtn setOn:[[DMCCIMService sharedDMCIMService] isCopyGroupMemberName:self.info.target]];
           cell.topSwitchBlock = ^(BOOL isTop) {
               [[DMCCIMService sharedDMCIMService] allowCopyWithGroupId:self.info.target data:isTop?@"yes":@"no" cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
                  
               }];
           };
       }
    } else if (indexPath.section == 1) {
        cell.switchBtn.hidden = YES;
        cell.arrowImg.hidden = NO;
        cell.remarkLabel.hidden = NO;
        if (indexPath.row == 0) {
            if(self.info.type == 0)
                cell.remarkLabel.text = LocalizedString(@"GroupManagerSetFriend");
            else if(self.info.type == 1)
                cell.remarkLabel.text = LocalizedString(@"GroupManagerSetOpen");
            else if(self.info.type == 2)
                cell.remarkLabel.text = LocalizedString(@"GroupManagerSetLimit");
        } else if (indexPath.row == 1) {
            NSString *str = [[DMCCIMService sharedDMCIMService] getGroupJoinTypeWithGroupId:self.info.target];
            if ([str isEqualToString:@"free"]) {
                cell.remarkLabel.text = LocalizedString(@"AddGroupAll");
            } else if([str isEqualToString:@"member"]) {
                cell.remarkLabel.text = LocalizedString(@"AddGroupMember");
            } else if([str isEqualToString:@"admin"]) {
                cell.remarkLabel.text = LocalizedString(@"AddGroupAdmin");
            } else if([str isEqualToString:@"verify"]) {
                cell.remarkLabel.text = LocalizedString(@"AddGroupVerity");
            } else if([str isEqualToString:@"password"]) {
                cell.remarkLabel.text = LocalizedString(@"AddGroupPwd");
            } else {
                cell.remarkLabel.text = LocalizedString(@"AddGroupAll");
            }
        } else if (indexPath.row == 2) {
            if(self.info.passType == 0)
                cell.remarkLabel.text = LocalizedString(@"GroupManagerVeriyNo");
            else if(self.info.passType == 1)
                cell.remarkLabel.text = LocalizedString(@"GroupManagerVeriyYes");
        } else if (indexPath.row == 3) {
           NSString *str = [[DMCCIMService sharedDMCIMService] getTimeIntervalGroupId:self.info.target];
            cell.remarkLabel.text = [NSString stringWithFormat:@"%@s", str];
        }  else if (indexPath.row == 4) {
            cell.remarkLabel.hidden = YES;
        } else if (indexPath.row == 5) {
            NSString *str = [[DMCCIMService sharedDMCIMService] getClearChatsGroupMemberName:self.info.target];
            if ([str longLongValue] > 0) {
               long time = [str longLongValue] / 3600 / 1000;
                NSString *showTime = @"";
                if (time == 24) {
                    showTime = LocalizedString(@"TimeClearChatOne");
                } else if (time == 48) {
                    showTime = LocalizedString(@"TimeClearChatTwo");
                } else if (time == 168) {
                    showTime = LocalizedString(@"TimeClearChatThree");
                } else if (time == 360) {
                    showTime = LocalizedString(@"TimeClearChatFour");
                } else if (time == 720) {
                    showTime = LocalizedString(@"TimeClearChatFive");
                }
                cell.remarkLabel.text = [NSString stringWithFormat:@"%@", showTime];
            } else {
                cell.remarkLabel.text = [NSString stringWithFormat:@"%@", str];
            }
        }
    } else if (indexPath.section == 2) {
        cell.remarkLabel.hidden = YES;
        cell.arrowImg.hidden = NO;
        if (self.litapp) {
            cell.nameLabel.textColor = [FontManage MHHomeNColor];
            cell.nameLabel.text = [NSString stringWithFormat:@"%@（%@）", LocalizedString(@"ContactAddDapp"), self.litapp.name];
        } else {
            cell.nameLabel.textColor = [FontManage MHMainColor];
            cell.nameLabel.text = LocalizedString(@"ContactAddDapp");
        }
    }
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    if (indexPath.section == 0 && indexPath.row == 0) {
        [self.navigationController pushViewController:[[ZoloSettingManagerVC alloc] initWithGroupInfo:self.info] animated:YES];
    } else if (indexPath.section == 0 && indexPath.row == 1) {
        [self.navigationController pushViewController:[[ZoloSettingMuteVC alloc] initWithGroupInfo:self.info] animated:YES];
    } else if (indexPath.section == 1 && indexPath.row == 0) {
        [self selectedActionClick:indexPath.row];
    } else if (indexPath.section == 1 && indexPath.row == 1) {
        [self selectedActionClick:indexPath.row];
    } else if (indexPath.section == 1 && indexPath.row == 2) {
        [self selectedActionClick:indexPath.row];
    } else if (indexPath.section == 1 && indexPath.row == 3) {
        [self selectedActionClick:indexPath.row];
    } else if (indexPath.section == 1 && indexPath.row == 4) {
        [self.navigationController pushViewController:[[ZoloKeywordVC alloc] initWithGroupInfo:self.info] animated:YES];
    } else if (indexPath.section == 1 && indexPath.row == 5) {
        [self selectedActionClick:indexPath.row];
    } else if (indexPath.section == 2 && indexPath.row == 0) {
        if (self.litapp) {
            [MHAlert showMessage:LocalizedString(@"AlertBindedDapp")];
            return;
        }
        DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:self.info.target refresh:NO];
        [self.navigationController pushViewController:[[ZoloGroupDappListVC alloc] initWithGroupInfo:group] animated:YES];
    }
}

- (void)selectedActionClick:(NSInteger)row {
    UIActionSheet *actionSheet = [[UIActionSheet alloc]initWithTitle:LocalizedString(@"PlaseSelect") delegate:self cancelButtonTitle:nil destructiveButtonTitle:nil otherButtonTitles:nil];
    NSMutableArray *titleArray = [NSMutableArray arrayWithCapacity:0];
    if (row == 0) {
        titleArray = [NSMutableArray arrayWithArray:self.typeArray];
    } else if (row == 1) {
        titleArray = [NSMutableArray arrayWithArray:self.addArray];
    } else if (row == 2)  {
        titleArray = [NSMutableArray arrayWithArray:self.authArray];
    } else if (row == 3)  {
        titleArray = [NSMutableArray arrayWithArray:self.speedArray];
    } else {
        titleArray = [NSMutableArray arrayWithArray:self.timeArray];
    }
    for (int i = 0; i < titleArray.count; i++) {
        NSString *title = titleArray[i];
        [actionSheet addButtonWithTitle:title];
    }
    [actionSheet addButtonWithTitle:LocalizedString(@"Cancel")];
    actionSheet.tag = 40 + row;
    actionSheet.cancelButtonIndex = actionSheet.numberOfButtons -1;
    actionSheet.actionSheetStyle = UIActionSheetStyleBlackOpaque;
    [actionSheet showInView:self.view];
}

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    WS(ws);
    if (actionSheet.tag == 40) {
        if (buttonIndex != (self.typeArray.count)) {
            if (buttonIndex == 0) {
                [MHAlert showLoadingStr:LocalizedString(@"AlertNowOpeating")];
                [[DMCCIMService sharedDMCIMService] modifyGroupInfo:self.info.target type:Modify_Group_Type newValue:@"0" notifyLines:@[@(0)] notifyContent:nil success:^{
                    [MHAlert dismiss];
                    ws.info.type = GroupType_Normal;
                    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                        [ws.tableView reloadRowsAtIndexPaths:@[[NSIndexPath indexPathForRow:0 inSection:1]] withRowAnimation:UITableViewRowAnimationFade];
                    });
                } error:^(int error_code) {
                    [MHAlert showMessage:LocalizedString(@"AlertSettingFail")];
                }];
            } else if (buttonIndex == 1) {
                [MHAlert showLoadingStr:LocalizedString(@"AlertNowOpeating")];
                [[DMCCIMService sharedDMCIMService] modifyGroupInfo:self.info.target type:Modify_Group_Type newValue:@"1" notifyLines:@[@(0)] notifyContent:nil success:^{
                    [MHAlert dismiss];
                    ws.info.type = GroupType_Free;
                    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                        [ws.tableView reloadRowsAtIndexPaths:@[[NSIndexPath indexPathForRow:0 inSection:1]] withRowAnimation:UITableViewRowAnimationFade];
                    });
                } error:^(int error_code) {
                    [MHAlert showMessage:LocalizedString(@"AlertSettingFail")];
                }];
            } else {
                [MHAlert showLoadingStr:LocalizedString(@"AlertNowOpeating")];
                [[DMCCIMService sharedDMCIMService] modifyGroupInfo:self.info.target type:Modify_Group_Type newValue:@"2" notifyLines:@[@(0)] notifyContent:nil success:^{
                    [MHAlert dismiss];
                    ws.info.type = GroupType_Restricted;
                    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                        [ws.tableView reloadRowsAtIndexPaths:@[[NSIndexPath indexPathForRow:0 inSection:1]] withRowAnimation:UITableViewRowAnimationFade];
                    });
                } error:^(int error_code) {
                    [MHAlert showMessage:LocalizedString(@"AlertSettingFail")];
                }];
            }
        }
    } else if (actionSheet.tag == 41) {
        if (buttonIndex != (self.addArray.count)) {
            if (buttonIndex == 4) {
                if (![self isGroupOwner]) {
                    [MHAlert showMessage:LocalizedString(@"AlertNoOurthSee")];
                    return;
                }
            }
            [MHAlert showLoadingStr:LocalizedString(@"AlertNowOpeating")];
            [[DMCCIMService sharedDMCIMService] addGroupJsonTypeWithGroupId:self.info.target data:[self getJoinTypeWithIndex:buttonIndex] cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
                if (isSuccess) {
                    if (buttonIndex == 4) {
                        dispatch_sync(dispatch_get_main_queue(), ^{
                            [ws settingJoinPassword:LocalizedString(@"AddGroupInputPwd") withAlerttype:MHAlertTypeRemind];
                        });
                    }
                    [[DMCCIMService sharedDMCIMService] getGroupInfo:self.info.target refresh:YES];
                    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(3 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                        [MHAlert dismiss];
                        [ws.tableView reloadRowsAtIndexPaths:@[[NSIndexPath indexPathForRow:1 inSection:1]] withRowAnimation:UITableViewRowAnimationFade];
                    });
                } else {
                    [MHAlert showMessage:LocalizedString(@"AlertSettingFail")];
                }
            }];
        }
    } else if (actionSheet.tag == 42) {
        if (buttonIndex != (self.authArray.count)) {
            if (buttonIndex == 0) {
                [MHAlert showLoadingStr:LocalizedString(@"AlertNowOpeating")];
                [[DMCCIMService sharedDMCIMService] modifyGroupInfo:self.info.target type:Modify_Group_PassType newValue:@"0" notifyLines:@[@(0)] notifyContent:nil success:^{
                    [MHAlert dismiss];
                    ws.info.passType = 0;
                    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                        [ws.tableView reloadRowsAtIndexPaths:@[[NSIndexPath indexPathForRow:2 inSection:1]] withRowAnimation:UITableViewRowAnimationFade];
                    });
                } error:^(int error_code) {
                    [MHAlert showMessage:LocalizedString(@"AlertSettingFail")];
                }];
            } else {
                [MHAlert showLoadingStr:LocalizedString(@"AlertNowOpeating")];
                [[DMCCIMService sharedDMCIMService] modifyGroupInfo:self.info.target type:Modify_Group_PassType newValue:@"1" notifyLines:@[@(0)] notifyContent:nil success:^{
                    [MHAlert dismiss];
                    ws.info.passType = 1;
                    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                        [ws.tableView reloadRowsAtIndexPaths:@[[NSIndexPath indexPathForRow:2 inSection:1]] withRowAnimation:UITableViewRowAnimationFade];
                    });
                } error:^(int error_code) {
                    [MHAlert showMessage:LocalizedString(@"AlertSettingFail")];
                }];
            }
        }
    } else if (actionSheet.tag == 43) {
        if (buttonIndex != (self.speedArray.count)) {
            [MHAlert showLoadingStr:LocalizedString(@"AlertNowOpeating")];
            [[DMCCIMService sharedDMCIMService] timeIntervalWithGroupId:self.info.target data:self.speedArray[buttonIndex] cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
                [MHAlert dismiss];
                if (isSuccess) {
                    dispatch_sync(dispatch_get_main_queue(), ^{
                        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                            [ws.tableView reloadRowsAtIndexPaths:@[[NSIndexPath indexPathForRow:3 inSection:1]] withRowAnimation:UITableViewRowAnimationFade];
                        });
                    });
                } else {
                    [MHAlert showMessage:LocalizedString(@"AlertSettingFail")];
                }
            }];
        }
    } else {
        if (buttonIndex != (self.timeArray.count)) {
            long time = 0;
            if (buttonIndex == 0) {
                time = 24L * 3600 * 1000;
            } else if (buttonIndex == 1) {
                time = 48L * 3600 * 1000;
            } else if (buttonIndex == 2) {
                time = 168L * 3600 * 1000;
            } else if (buttonIndex == 3) {
                time = 360L * 3600 * 1000;
            } else if (buttonIndex == 4) {
                time = 720L * 3600 * 1000;
            } else {
                time = 0;
            }
            [MHAlert showLoadingStr:LocalizedString(@"AlertNowOpeating")];
            [[DMCCIMService sharedDMCIMService] allowClearChatsWithGroupId:self.info.target data:[NSString stringWithFormat:@"%ld", time] cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
                [MHAlert dismiss];
                if (isSuccess) {
                    dispatch_sync(dispatch_get_main_queue(), ^{
                        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                            [ws.tableView reloadRowsAtIndexPaths:@[[NSIndexPath indexPathForRow:5 inSection:1]] withRowAnimation:UITableViewRowAnimationFade];
                        });
                    });
                } else {
                    [MHAlert showMessage:LocalizedString(@"AlertSettingFail")];
                }
            }];
        }
    }
}

- (NSString *)getJoinTypeWithIndex:(NSInteger)index {
    NSArray *str = @[@"free", @"member", @"admin", @"verify", @"password"];
    return str[index];
}

-(void)settingJoinPassword:(NSString *)message withAlerttype:(MHAlertType)alerttype {
    NSString *title;
    switch (alerttype) {
        case MHAlertTypeWarn:
            title = LocalizedString(@"AlertWarn");
            break;
        case MHAlertTypeRemind:
            title = LocalizedString(@"AlertRemind");
            break;
    }
    WS(ws);
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:title message:message preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *ok = [UIAlertAction actionWithTitle:LocalizedString(@"Sure") style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        NSString *inputText = alert.textFields[0].text;
        if (inputText.length == 0) {
            [MHAlert showMessage:LocalizedString(@"AddGroupInputPwd")];
            return;
        }
        DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:ws.info.target refresh:NO];
        [[DMCCIMService sharedDMCIMService] addGroupJsonPwdWithGroupId:self.info.target data:alert.textFields[0].text cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
            if (isSuccess) {
                [MHAlert showMessage:LocalizedString(@"AlertSuccess")];
            } else {
                [MHAlert showMessage:LocalizedString(@"AlertSettingFail")];
            }
        }];
    }];
    UIAlertAction *cancel = [UIAlertAction actionWithTitle:LocalizedString(@"Cancel") style:UIAlertActionStyleDefault handler:nil];
    [alert addTextFieldWithConfigurationHandler:^(UITextField * _Nonnull textField) {
        textField.placeholder = LocalizedString(@"AddGroupInputPwd");
        textField.clearButtonMode = UITextFieldViewModeAlways;
    }];
    [alert addAction:cancel];
    [alert addAction:ok];
    [self presentViewController:alert animated:YES completion:nil];
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section {
    UIView *view = [[UIView alloc] initWithFrame:CGRectMake(0, 0, kScreenwidth, 30)];
    view.backgroundColor = [FontManage MHLineSeparatorColor];
    UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(20, 0, kScreenwidth - 20, 30)];
    if (section == 0) {
        label.text = LocalizedString(@"GroupManagerMemberSet");
    } else if (section == 1) {
        label.text = LocalizedString(@"GroupManagerSeting");
    } else {
        label.text = LocalizedString(@"LitappManager");
    }
    label.font = [UIFont systemFontOfSize:12];
    [view addSubview:label];
    return view;
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    if (section == 1) {
        if ([self isGroupOwner] || [self isGroupManger]) {
            return 30;
        } else {
            return 0.001;
        }
    }
    return 30;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 8;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    if (indexPath.section == 0 && indexPath.row == 0) {
        if ([self isGroupOwner] || [self isGroupManger]) {
           return self.scaleHeight(54);
       } else {
           return 0.001;
       }
    } else if (indexPath.section == 1) {
        if (([self isGroupOwner] || [self isGroupManger]) && indexPath.row != 2 && indexPath.row != 0) {
            return self.scaleHeight(54);
        } else if (indexPath.row == 2) {
            return 0.001;
        } else if (indexPath.row == 0) {
            return 0.001;
        }
    }

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
