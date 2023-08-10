//
//  ZoloContactDetailVC.m
//  NewZolo
//
//  Created by JTalking on 2022/7/6.
//

#import "ZoloContactDetailVC.h"
#import "ZoloGroupDetailCell.h"
#import "ZoloConDetailHeadView.h"
#import "ZoloContactDetailFoot.h"
#import "ZoloSingleChatVC.h"
#import "ZoloNickNameVC.h"
#import "ZoloWalletViewController.h"
#import "YBImageBrowser.h"
#import "ZoloMyQrCodeVC.h"
#import "ZoloShareCardVC.h"
#import "ZoloTagVC.h"
#import "ZoloTagOperateVC.h"
#import "ZoloComplaintVC.h"

@interface ZoloContactDetailVC () <UITableViewDelegate, UITableViewDataSource>

@property (nonatomic, strong) NSArray *titleArray;
@property (nonatomic, strong) DMCCUserInfo *userInfo;

@end

@implementation ZoloContactDetailVC

- (instancetype)initWithUserInfo:(DMCCUserInfo *)userInfo {
    if (self = [super init]) {
        self.userInfo = userInfo;
    }
    return self;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self getLocalData];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [self getData];
    self.titleArray = @[@[LocalizedString(@"UserRemark"), LocalizedString(@"ShareCard"), LocalizedString(@"TagOperation")], @[LocalizedString(@"FriendQRCode"), LocalizedString(@"AddBlackList")], @[LocalizedString(@"ComplaintTitle")]];
    [self registerCellWithNibName:NSStringFromClass([ZoloGroupDetailCell class]) isTableview:YES];
    [self registerHeaderFooterWithNibName:NSStringFromClass([ZoloContactDetailFoot class])];
    [self registerHeaderFooterWithNibName:NSStringFromClass([ZoloConDetailHeadView class])];
    self.tableView.backgroundColor = [FontManage MHGrayColor];
    self.tableView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
    self.tableView.separatorColor = [FontManage MHLineSeparatorColor];
}

- (void)getLocalData {
    WS(ws);
    if (self.userInfo) {
        [[DMCCIMService sharedDMCIMService] getUserInfo:self.userInfo.userId refresh:NO success:^(DMCCUserInfo *userInfo) {
            if (userInfo) {
                ws.userInfo = userInfo;
                dispatch_async(dispatch_get_main_queue(), ^{
                    [ws.tableView reloadData];
                });
            }
        } error:^(int errorCode) {
            
        }];
    }
}

- (void)getData {
    WS(ws);
    [[DMCCIMService sharedDMCIMService] getUserInfo:self.userInfo.userId refresh:YES success:^(DMCCUserInfo *userInfo) {
        if (userInfo) {
            ws.userInfo = userInfo;
            dispatch_async(dispatch_get_main_queue(), ^{
                [ws.tableView reloadData];
            });
        }
    } error:^(int errorCode) {
        
    }];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return self.titleArray.count;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    NSArray *array = self.titleArray[section];
    return array.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ZoloGroupDetailCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloGroupDetailCell class])];
    NSArray *array = self.titleArray[indexPath.section];
    cell.nameLabel.text = array[indexPath.row];
    WS(ws);
    if (indexPath.row == 1 && indexPath.section == 1) {
        cell.switchBtn.hidden = NO;
        cell.arrowImg.hidden = YES;
        [cell.switchBtn setOn:[[DMCCIMService sharedDMCIMService] isBlackListed:self.userInfo.userId]];
        cell.topSwitchBlock = ^(BOOL isTop) {
            [ws addBlockList:isTop];
        };
    } else {
        cell.switchBtn.hidden = YES;
        cell.arrowImg.hidden = NO;
    }
    return cell;
}

- (void)addBlockList:(BOOL)isAdd {
    [[DMCCIMService sharedDMCIMService] setBlackList:self.userInfo.userId isBlackListed:isAdd success:^{
        [MHAlert showMessage:LocalizedString(@"AlertSuccess")];
    } error:^(int error_code) {
        [MHAlert showMessage:LocalizedString(@"AlertOpearFail")];
    }];
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    if (indexPath.row == 0 && indexPath.section == 0) {
        [tableView deselectRowAtIndexPath:indexPath animated:YES];
        [self.navigationController pushViewController:[[ZoloNickNameVC alloc] initWithSettingRemarkName:MHNickNameType_User WithSId:self.userInfo.userId] animated:YES];
    } else if (indexPath.row == 0 && indexPath.section == 1) {
        DMCCConversation *conversation = [DMCCConversation conversationWithType:Single_Type target:self.userInfo.userId line:0];
        [self.navigationController pushViewController:[[ZoloMyQrCodeVC alloc] initWithGroupQr:conversation withIsGroup:NO IsUser:YES] animated:YES];
    } else if (indexPath.row == 1 && indexPath.section == 0) {
        [self.navigationController pushViewController:[[ZoloShareCardVC alloc] initWithUserInfo:self.userInfo] animated:YES];
    } else if (indexPath.section == 0 && indexPath.row == 2) {
        [self.navigationController pushViewController:[[ZoloTagOperateVC alloc] initWithTagAddInfo:self.userInfo] animated:YES];
    } else if (indexPath.section == 2) {
        [self.navigationController pushViewController:[[ZoloComplaintVC alloc] initWithIsGroup:NO] animated:YES];
    }
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section {
    if (section == 0) {
        ZoloConDetailHeadView *headView = [tableView dequeueReusableHeaderFooterViewWithIdentifier:NSStringFromClass([ZoloConDetailHeadView class])];
        [headView.icon sd_setImageWithURL:[NSURL URLWithString:self.userInfo.portrait] placeholderImage:SDImageDefault];
        headView.nickName.text = self.userInfo.displayName;
        headView.remark.text = [NSString stringWithFormat:@"ID:%@", self.userInfo.userId];
        NSString *alias = [[DMCCIMService sharedDMCIMService] getFriendAlias:self.userInfo.userId];
        if (alias.length > 0) {
            headView.nickName.text = alias;
        }
        NSString *nftStr = [DMCCUserInfo getNft:self.userInfo.describes];
        if (nftStr.length > 0) {
            headView.nftImg.hidden = NO;
        } else {
            headView.nftImg.hidden = YES;
        }
        WS(ws);
        headView.longGesClickBlock = ^{
            [ws longPressClick];
        };
        
        headView.iconClickBlock = ^{
            [ws iconBtnClickBlock];
        };
        
        return headView;
    } else {
        UIView *view = [UIView new];
        view.backgroundColor = [FontManage MHLineSeparatorColor];
        return view;
    }
}

- (void)iconBtnClickBlock {
    NSString *nftStr = [DMCCUserInfo getNft:self.userInfo.describes];
    if (nftStr.length > 0) {
        [self.navigationController pushViewController:[[ZoloWalletViewController alloc] initWithUrlInfo:nftStr isHidden:NO] animated:YES];
    } else {
        NSMutableArray *datas = [NSMutableArray arrayWithCapacity:0];
        YBIBImageData *data = [YBIBImageData new];
        if (self.userInfo.portrait.length > 0) {
            data.imageURL = [NSURL URLWithString:self.userInfo.portrait];
        } else {
            data.imageURL = [NSURL URLWithString:@"null"];
        }
        data.thumbImage = SDImageDefault;
        [datas addObject:data];
        YBImageBrowser *browser = [YBImageBrowser new];
        browser.dataSourceArray = datas;
        browser.currentPage = 0;
        browser.defaultToolViewHandler.topView.operationType = YBIBTopViewOperationTypeSave;
        [browser show];
    }
}

- (void)longPressClick {
    [[UIPasteboard generalPasteboard] setString:self.userInfo.userId];
    [MHAlert showMessage:LocalizedString(@"AlertCopySucess")];
}

- (UIView *)tableView:(UITableView *)tableView viewForFooterInSection:(NSInteger)section {
    ZoloContactDetailFoot *footView = [tableView dequeueReusableHeaderFooterViewWithIdentifier:NSStringFromClass([ZoloContactDetailFoot class])];
    [footView.sendMsgBtn setTitle:LocalizedString(@"ContactSendMsg") forState:UIControlStateNormal];
    [footView.deleteUserBtn setTitle:LocalizedString(@"ContactDelUser") forState:UIControlStateNormal];
    WS(ws);
    footView.sendMessageBlock = ^{
        [ws sendMessage];
    };
    footView.deleteUserBlock = ^{
        [ws deleteUser];
    };
    return footView;
}

- (void)deleteUser {
    WS(ws);
    NSString *str = LocalizedString(@"AlertSureDelConcat");
    [MHAlert showCustomeAlert:str withAlerttype:MHAlertTypeWarn withOkBlock:^(UIAlertAction *action) {
        if ([action.title isEqualToString:LocalizedString(@"Sure")]) {
            [ws deleUserClick];
        }
    }];
}

- (void)deleUserClick {
    WS(ws);
    [MHAlert showLoadingStr:LocalizedString(@"AlertNowOpeating")];
    [[DMCCIMService sharedDMCIMService] deleteFriend:self.userInfo.userId success:^{
        [MHAlert showLoadingStr:LocalizedString(@"AlertSuccess")];
        dispatch_async(dispatch_get_main_queue(), ^{
            [ws deleteChat];
            [[NSNotificationCenter defaultCenter] postNotificationName:FZ_EVENT_DELETEUSER_STATE object:nil];
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                [ws.navigationController popToRootViewControllerAnimated:YES];
            });
        });
    } error:^(int error_code) {
        
    }];
}

// 删除会话
- (void)deleteChat {
    DMCCConversation *con = [DMCCConversation conversationWithType:Single_Type target:self.userInfo.userId line:0];
    DMCCConversationInfo *info = [[DMCCIMService sharedDMCIMService] getConversationInfo:con];
    [self delTagUser:self.userInfo];
    [[DMCCIMService sharedDMCIMService] clearUnreadStatus:info.conversation];
    [[DMCCIMService sharedDMCIMService] clearMessages:info.conversation];
    [[DMCCIMService sharedDMCIMService] removeConversation:info.conversation clearMessage:YES];
    [[NSNotificationCenter defaultCenter] postNotificationName:FZ_EVENT_MSGSTATUS_STATE object:nil];
}

- (void)delTagUser:(DMCCUserInfo *)user {
    if (user.ugID == -100) {
        return;
    }
    NSMutableArray *array = [[NSMutableArray alloc] initWithCapacity:0];
    NSMutableArray *uArray = [[NSMutableArray alloc] initWithCapacity:0];
    
    [array addObject:@(user.ugID)];
    [uArray addObject:user.userId];
    
    [[ZoloAPIManager instanceManager] delTagUserList:array WithCompleteBlock:^(BOOL isSuccess) {
        if (isSuccess) {
            [[DMCCIMService sharedDMCIMService] updateUserWithUsers:uArray withTagWithkey:@[@"tagID"]];
        }
    }];
}

- (void)sendMessage {
    DMCCConversationInfo *info = [DMCCConversationInfo new];
    DMCCConversation *conversation = [DMCCConversation conversationWithType:Single_Type target:self.userInfo.userId line:0];
    info.conversation = conversation;
    [self.navigationController pushViewController:[[ZoloSingleChatVC alloc] initWithConversationInfo:info] animated:YES];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    if (section == 0) {
        return 150;
    } else {
        return 20;
    }
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    if (section == 2) {
        if ([[[DMCCIMService sharedDMCIMService] getUserID] isEqualToString:self.userInfo.userId]) {
            return 0.001;
        }
        return 200;
    } else {
        return 0.001;
    }
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
