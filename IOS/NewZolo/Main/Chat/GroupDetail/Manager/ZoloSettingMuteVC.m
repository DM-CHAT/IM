//
//  ZoloSettingMuteVC.m
//  NewZolo
//
//  Created by JTalking on 2022/8/22.
//

#import "ZoloSettingMuteVC.h"
#import "ZoloGroupDetailCell.h"
#import "ZoloSettingContactVC.h"
#import "ZoloContactGroupCell.h"

@interface ZoloSettingMuteVC () <UITableViewDelegate, UITableViewDataSource>

@property (nonatomic, strong) DMCCGroupInfo *info;

@property (nonatomic, strong) NSMutableArray *managerList;

@end

@implementation ZoloSettingMuteVC

- (instancetype)initWithGroupInfo:(DMCCGroupInfo *)info {
    if (self = [super init]) {
        self.info = info;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    [self loadManagerList];
    [self registerCellWithNibName:NSStringFromClass([ZoloContactGroupCell class]) isTableview:YES];
    [self registerCellWithNibName:NSStringFromClass([ZoloGroupDetailCell class]) isTableview:YES];
}

- (void)loadManagerList {
    [self.managerList removeAllObjects];
    NSArray *memberList = [[DMCCIMService sharedDMCIMService] getGroupMembers:self.info.target forceUpdate:NO];
    self.managerList = [[NSMutableArray alloc] init];
    for (DMCCGroupMember *member in memberList) {
        if (member.mute == 1) {
            [self.managerList addObject:member];
        }
    }
    [self.tableView reloadData];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 3;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    if (section == 2) {
        return self.managerList.count;
    } else {
        return 1;
    }
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    if (indexPath.section == 2) {
        ZoloContactGroupCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloContactGroupCell class])];
        DMCCGroupMember *member = self.managerList[indexPath.row];
        DMCCUserInfo *user = [[DMCCIMService sharedDMCIMService] getUserInfo:member.memberId inGroup:member.groupId refresh:NO];
        [cell.icon sd_setImageWithURL:[NSURL URLWithString:user.portrait] placeholderImage:SDImageDefault];
        cell.titleName.text = user.displayName;
        return cell;
    } else {
        ZoloGroupDetailCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloGroupDetailCell class])];
        if (indexPath.section == 0) {
            cell.switchBtn.hidden = NO;
            cell.arrowImg.hidden = YES;
            cell.nameLabel.text = LocalizedString(@"GroupManagerMuteAll");
            [cell.switchBtn setOn:[[DMCCIMService sharedDMCIMService] isMuteGroup:self.info.target]];
            cell.topSwitchBlock = ^(BOOL isTop) {
                [[DMCCIMService sharedDMCIMService] muteGroupMember:self.info.target isSet:isTop mode:1 memberIds:@[] notifyLines:@[] notifyContent:nil success:^{
                    
                } error:^(int error_code) {
                    
                }];
            };
        } else if (indexPath.section == 1) {
            cell.switchBtn.hidden = YES;
            cell.arrowImg.hidden = NO;
            cell.nameLabel.text = LocalizedString(@"GroupManagerMuteMember");
        }
        return cell;
    }
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    if (indexPath.section == 1) {
        ZoloSettingContactVC *vc = [[ZoloSettingContactVC alloc] initWithGroupInfo:self.info withType:2];
        WS(ws);
        vc.confirmBlock = ^(NSArray * _Nonnull contactArray) {
            NSMutableArray *upArray = [NSMutableArray arrayWithCapacity:0];
            for (DMCCGroupMember *mem in contactArray) {
                [upArray addObject:mem.memberId];
            }
            [[DMCCIMService sharedDMCIMService] muteGroupMember:ws.info.target isSet:1 mode:0 memberIds:upArray notifyLines:@[] notifyContent:nil success:^{
                dispatch_async(dispatch_get_main_queue(), ^{
                    [MHAlert showMessage:LocalizedString(@"AlertSettingSucess")];
                    [ws loadManagerList];
                });
            } error:^(int error_code) {
                [MHAlert showMessage:LocalizedString(@"AlertSettingFail")];
            }];
        };
        [self.navigationController pushViewController:vc animated:YES];
    }
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 20;
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section {
    UIView *view = [UIView new];
    view.backgroundColor = [FontManage MHLineSeparatorColor];
    return view;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 8;
}

// 编辑删除操作
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath {
    if (indexPath.section == 2) {
        return YES;
    }
    return NO;
}

- (NSArray*)tableView:(UITableView *)tableView editActionsForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewRowAction *action = [UITableViewRowAction rowActionWithStyle:UITableViewRowActionStyleDefault title:LocalizedString(@"MsgDel") handler:^(UITableViewRowAction * _Nonnull action, NSIndexPath * _Nonnull indexPath) {
        WS(ws);
        NSString *str = LocalizedString(@"AlertSureDelMute");
        [MHAlert showCustomeAlert:str withAlerttype:MHAlertTypeWarn withOkBlock:^(UIAlertAction *action) {
            if ([action.title isEqualToString:LocalizedString(@"Sure")]) {
                [ws removeUserItem:indexPath.row];
            }
        }];
    }];
    action.backgroundColor = [FontManage MHMsgRecColor];
    return @[action];
}

- (void)removeUserItem:(NSInteger)row {
    NSMutableArray *upArray = [NSMutableArray arrayWithCapacity:0];
    DMCCGroupMember *member = self.managerList[row];
    [upArray addObject:member.memberId];
    WS(ws);
    [[DMCCIMService sharedDMCIMService] muteGroupMember:self.info.target isSet:0 mode:0 memberIds:upArray notifyLines:@[] notifyContent:nil success:^{
        [MHAlert showMessage:LocalizedString(@"AlertSettingSucess")];
        dispatch_async(dispatch_get_main_queue(), ^{
            [ws.managerList removeObject:member];
            [ws.tableView reloadData];
        });
    } error:^(int error_code) {
        [MHAlert showMessage:LocalizedString(@"AlertSettingFail")];
    }];
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    if (indexPath.section == 2) {
        return self.scaleHeight(64);
    } else {
        return self.scaleHeight(44);
    }
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
