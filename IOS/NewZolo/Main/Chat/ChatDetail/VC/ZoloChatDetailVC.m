//
//  ZoloChatDetailVC.m
//  NewZolo
//
//  Created by JTalking on 2022/8/15.
//

#import "ZoloChatDetailVC.h"
#import "ZoloContactDetailCell.h"
#import "ZoloConDetailHeadView.h"
#import "ZoloChatDetailFootView.h"
#import "ZoloGroupDetailCell.h"
#import "ZoloWalletViewController.h"
#import "YBImageBrowser.h"
#import "ZoloShareCardVC.h"
#import "ZoloChatSearchVC.h"
#import "ZoloComplaintVC.h"

@interface ZoloChatDetailVC () <UITableViewDelegate, UITableViewDataSource>

@property (nonatomic, strong) NSArray *titleArray;
@property (nonatomic, strong) DMCCUserInfo *userInfo;
@property (nonatomic, strong)DMCCConversationInfo *conversationInfo;

@end

@implementation ZoloChatDetailVC

- (instancetype)initWithUserInfo:(DMCCUserInfo *)userInfo withConversition:(DMCCConversationInfo *)conversationInfo {
    if (self = [super init]) {
        self.userInfo = userInfo;
        self.conversationInfo = conversationInfo;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.titleArray = @[@[LocalizedString(@"ContactMsgNoNotice"), LocalizedString(@"ContactTopChat")], @[LocalizedString(@"ContactChatRecord"), LocalizedString(@"ShareCard"), LocalizedString(@"ContactClearChat")], @[LocalizedString(@"ComplaintTitle")]];
    [self registerCellWithNibName:NSStringFromClass([ZoloGroupDetailCell class]) isTableview:YES];
    [self registerHeaderFooterWithNibName:NSStringFromClass([ZoloChatDetailFootView class])];
    [self registerHeaderFooterWithNibName:NSStringFromClass([ZoloConDetailHeadView class])];
    self.tableView.backgroundColor = [FontManage MHGrayColor];
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
    if (indexPath.section == 0) {
        cell.switchBtn.hidden = NO;
        cell.arrowImg.hidden = YES;
        if (indexPath.row == 0) {
            [cell.switchBtn setOn:self.conversationInfo.isSilent];
            cell.topSwitchBlock = ^(BOOL isTop) {
                [ws silentConversation:isTop];
            };
        } else {
            [cell.switchBtn setOn:self.conversationInfo.isTop];
            cell.topSwitchBlock = ^(BOOL isTop) {
                [ws topConversation:isTop];
            };
        }
    } else {
        cell.switchBtn.hidden = YES;
        cell.arrowImg.hidden = NO;
    }
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    WS(ws);
    if (indexPath.section == 1 && indexPath.row == 2) {
        NSString *str = LocalizedString(@"AlertClear");
        [MHAlert showCustomeAlert:str withAlerttype:MHAlertTypeWarn withOkBlock:^(UIAlertAction *action) {
            if ([action.title isEqualToString:LocalizedString(@"Sure")]) {
                [[DMCCIMService sharedDMCIMService] clearMessages:ws.conversationInfo.conversation];
                [[NSNotificationCenter defaultCenter] postNotificationName:FZ_EVENT_MESSAGELISTCHANG_STATE object:ws.conversationInfo.conversation];
            }
        }];
    } else if (indexPath.section == 1 && indexPath.row == 1) {
        [self.navigationController pushViewController:[[ZoloShareCardVC alloc] initWithUserInfo:self.userInfo] animated:YES];
    } else if (indexPath.section == 1 && indexPath.row == 0) {
        [self.navigationController pushViewController:[[ZoloChatSearchVC alloc] initWithConversition:self.conversationInfo] animated:YES];
    }  else if (indexPath.section == 2 && indexPath.row == 0) {
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
        data.imageURL = [NSURL URLWithString:self.userInfo.portrait];
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
    ZoloChatDetailFootView *footView = [tableView dequeueReusableHeaderFooterViewWithIdentifier:NSStringFromClass([ZoloChatDetailFootView class])];
    WS(ws);
    footView.deleteChatBlock = ^{
        NSString *str = LocalizedString(@"AlertSureDel");
        [MHAlert showCustomeAlert:str withAlerttype:MHAlertTypeWarn withOkBlock:^(UIAlertAction *action) {
            if ([action.title isEqualToString:LocalizedString(@"Sure")]) {
                [ws deleteChat];
            }
        }];
    };
    return footView;
}

- (void)topConversation:(BOOL)isTop {
    [[DMCCIMService sharedDMCIMService] setConversation:self.conversationInfo.conversation top:isTop success:^{
        [[NSNotificationCenter defaultCenter] postNotificationName:FZ_EVENT_CONVERSATIONLISTCHANG_STATE object:nil];
    } error:^(int error_code) {
    }];
}

- (void)silentConversation:(BOOL)isSilent {
    [[DMCCIMService sharedDMCIMService] setConversation:self.conversationInfo.conversation silent:isSilent success:^{
        [[NSNotificationCenter defaultCenter] postNotificationName:FZ_EVENT_CONVERSATIONLISTCHANG_STATE object:nil];
    } error:^(int error_code) {
        
    }];
}

// 删除会话
- (void)deleteChat {
    [[DMCCIMService sharedDMCIMService] clearUnreadStatus:self.conversationInfo.conversation];
    [[DMCCIMService sharedDMCIMService] removeConversation:self.conversationInfo.conversation clearMessage:YES];
    [[NSNotificationCenter defaultCenter] postNotificationName:FZ_EVENT_CONVERSATIONLISTCHANG_STATE object:nil];
    [self.navigationController popToRootViewControllerAnimated:YES];
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
        return 120;
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
