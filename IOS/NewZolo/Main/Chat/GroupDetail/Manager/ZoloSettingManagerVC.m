//
//  ZoloSettingManagerVC.m
//  NewZolo
//
//  Created by JTalking on 2022/8/22.
//

#import "ZoloSettingManagerVC.h"
#import "ZoloContactGroupCell.h"
#import "ZoloGroupDetailCell.h"
#import "ZoloSettingContactVC.h"

@interface ZoloSettingManagerVC () <UITableViewDelegate, UITableViewDataSource>

@property (nonatomic, strong) NSArray *titleArray;
@property (nonatomic, strong) DMCCGroupInfo *info;
@property (nonatomic, strong) NSMutableArray *managerList;

@end

@implementation ZoloSettingManagerVC

- (instancetype)initWithGroupInfo:(DMCCGroupInfo *)info {
    if (self = [super init]) {
        self.info = info;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    self.titleArray = @[LocalizedString(@"GroupManagerGroupOwner"), LocalizedString(@"GroupManagerSetManger")];
    [self registerCellWithNibName:NSStringFromClass([ZoloContactGroupCell class]) isTableview:YES];
    [self registerCellWithNibName:NSStringFromClass([ZoloGroupDetailCell class]) isTableview:YES];
    [self loadManagerList];
}


- (void)loadManagerList {
    NSArray *memberList = [[DMCCIMService sharedDMCIMService] getGroupMembers:self.info.target forceUpdate:NO];
    self.managerList = [[NSMutableArray alloc] init];
    for (DMCCGroupMember *member in memberList) {
        if (member.type == Member_Type_Manager) {
            [self.managerList addObject:member];
        }
    }
    [self.tableView reloadData];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return self.titleArray.count + 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    if (section == 1) {
        return self.managerList.count;
    }
    return 1;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    if (indexPath.section == 2) {
        ZoloGroupDetailCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloGroupDetailCell class])];
        cell.nameLabel.text = LocalizedString(@"AddGroupManager");
        return cell;
    } else {
        ZoloContactGroupCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloContactGroupCell class])];
        if (indexPath.section == 0) {
            DMCCUserInfo *owner = [[DMCCIMService sharedDMCIMService] getUserInfo:self.info.owner refresh:NO];
            [cell.icon sd_setImageWithURL:[NSURL URLWithString:owner.portrait] placeholderImage:SDImageDefault];
            cell.titleName.text = owner.displayName;
        } else if (indexPath.section == 1) {
            DMCCGroupMember *member = self.managerList[indexPath.row];
            DMCCUserInfo *user = [[DMCCIMService sharedDMCIMService] getUserInfo:member.memberId inGroup:member.groupId refresh:NO];
            [cell.icon sd_setImageWithURL:[NSURL URLWithString:user.portrait] placeholderImage:SDImageDefault];
            cell.titleName.text = user.displayName;
        }
        return cell;
    }
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    if (indexPath.section == 2) {
        ZoloSettingContactVC *vc = [[ZoloSettingContactVC alloc] initWithGroupInfo:self.info withType:1];
        WS(ws);
        vc.confirmBlock = ^(NSArray * _Nonnull contactArray) {
            NSMutableArray *upArray = [NSMutableArray arrayWithCapacity:0];
            for (DMCCGroupMember *mem in contactArray) {
                [upArray addObject:mem.memberId];
            }
            [[DMCCIMService sharedDMCIMService] setGroupManager:self.info.target isSet:YES memberIds:upArray notifyLines:@[] notifyContent:nil success:^{
                dispatch_async(dispatch_get_main_queue(), ^{
                    [ws.managerList addObjectsFromArray:contactArray];
                    [ws.tableView reloadData];
                });
            } error:^(int error_code) {
                
            }];
        };
        [self.navigationController pushViewController:vc animated:YES];
    }
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return self.scaleHeight(30);
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section {
    if (section == 2) {
        UIView *view = [[UIView alloc] initWithFrame:CGRectMake(0, 0, kScreenwidth, 30)];
        view.backgroundColor = [FontManage MHLineSeparatorColor];
        return view;
    } else {
        UIView *view = [[UIView alloc] initWithFrame:CGRectMake(0, 0, kScreenwidth, 30)];
        view.backgroundColor = [FontManage MHLineSeparatorColor];
        UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(20, 0, kScreenwidth - 20, 30)];
        if (section == 0) {
            label.text = LocalizedString(@"GroupManagerGroupOwner");
        } else {
            label.text = LocalizedString(@"GroupManagerSetManger");
        }
        label.font = [UIFont systemFontOfSize:12];
        [view addSubview:label];
        return view;
    }
}

// 编辑删除操作
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath {
    if (indexPath.section == 1) {
        return ![self isGroupManger];
    }
    return NO;
}

- (BOOL)isGroupManger {
    DMCCGroupMember *gm = [[DMCCIMService sharedDMCIMService] getGroupMember:self.info.target memberId:[DMCCNetworkService sharedInstance].userId];
    return gm.type == Member_Type_Manager;
}

- (NSArray*)tableView:(UITableView *)tableView editActionsForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewRowAction *action = [UITableViewRowAction rowActionWithStyle:UITableViewRowActionStyleDefault title:LocalizedString(@"MsgDel") handler:^(UITableViewRowAction * _Nonnull action, NSIndexPath * _Nonnull indexPath) {
        WS(ws);
        NSString *str = LocalizedString(@"AlertSureDelManger");
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
    [[DMCCIMService sharedDMCIMService] delGroupManager:self.info.target isSet:YES memberIds:upArray notifyLines:@[] notifyContent:nil success:^{
        dispatch_async(dispatch_get_main_queue(), ^{
            [ws.managerList removeObject:member];
            [ws.tableView reloadData];
        });
    } error:^(int error_code) {
        
    }];
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 8;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    if (indexPath.section == 2) {
        if ([self isGroupOwner]) {
            return self.scaleHeight(64);
        } else {
            return 0.001;
        }
    }
    return self.scaleHeight(64);
}

- (BOOL)isGroupOwner {
    return [self.info.owner isEqualToString:[DMCCNetworkService sharedInstance].userId];
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
