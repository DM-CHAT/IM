//
//  ZoloMyVC.m
//  NewZolo
//
//  Created by JTalking on 2022/6/29.
//

#import "ZoloMyVC.h"
#import "ZoloMyHeadView.h"
#import "ZoloMyCell.h"
#import "ZoloSettingVC.h"
#import "ZoloMyQrCodeVC.h"
#import "ZoloPersonVC.h"
#import "ZoloWalletViewController.h"
#import "ZoloCustomViewController.h"
#import "ZoloNoticeVC.h"
#import "ZoloFeedbackVC.h"
#import "YBImageBrowser.h"
#import "FZAnimationVC.h"
#import "ZoloShareViewController.h"
#import "ZoloSystemNoticeVC.h"
#import "ZoloWalletListVC.h"
#import "ZoloCollectVC.h"

@interface ZoloMyVC () <UITableViewDelegate, UITableViewDataSource>

@property (nonatomic, strong) NSArray *titleArray;
@property (nonatomic, strong) DMCCUserInfo *userInfo;
@property (nonatomic, assign) BOOL isHidenNav;

@end

@implementation ZoloMyVC

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self.navigationController setNavigationBarHidden:YES animated:animated];
    [self getData];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    if (self.isHidenNav) {
        [self.navigationController setNavigationBarHidden:YES animated:animated];
    } else {
        [self.navigationController setNavigationBarHidden:NO animated:animated];
    }
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.extendedLayoutIncludesOpaqueBars = YES;
    if (@available(iOS 11.0, *)) {
          self.tableView.contentInsetAdjustmentBehavior = UIScrollViewContentInsetAdjustmentNever;
    } else {
          self.automaticallyAdjustsScrollViewInsets = NO;
    }
    self.tableView.backgroundColor = [FontManage MHGrayColor];
    self.tableView.separatorColor = [FontManage MHLineSeparatorColor];
    self.titleArray = @[LocalizedString(@"WalletTitle"),LocalizedString(@"Assets"), LocalizedString(@"CustomerService"), LocalizedString(@"ChatNotice"), LocalizedString(@"MsgPush"), LocalizedString(@"UserFeedback"), LocalizedString(@"appShare"), LocalizedString(@"CollectData"), LocalizedString(@"String")];
    [self registerHeaderFooterWithNibName:NSStringFromClass([ZoloMyHeadView class])];
    [self registerCellWithNibName:NSStringFromClass([ZoloMyCell class]) isTableview:YES];
}

- (void)getData {
    WS(ws);
    [[DMCCIMService sharedDMCIMService] getUserInfo:[[OsnSDK getInstance] getUserID] refresh:NO success:^(DMCCUserInfo *userInfo) {
        if (userInfo) {
            ws.userInfo = userInfo;
            dispatch_async(dispatch_get_main_queue(), ^{
                [ws.tableView.mj_header endRefreshing];
                [ws.tableView reloadData];
            });
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
    ZoloMyCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloMyCell class])];
    cell.icon.image = [UIImage imageNamed:[NSString stringWithFormat:@"my%ld.png", indexPath.row]];
    cell.title.text = self.titleArray[indexPath.row];
    if (indexPath.row == 4) {
        cell.redStatus.hidden = [self redStatusHidden];
    } else {
        cell.redStatus.hidden = YES;
    }
    return cell;
}

- (BOOL)redStatusHidden {
    NSArray *array = [NSMutableArray arrayWithArray:[[DMCCIMService sharedDMCIMService] getConversationInfos:@[@(Service_Type)] lines:@[@(0)]]];
    NSInteger count = 0;
    for (DMCCConversationInfo *info in array) {
        count += info.unreadCount.unread;
    }
    return count == 0;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    self.isHidenNav = NO;
    switch (indexPath.row) {
        case 0:
        {
            [self.navigationController pushViewController:[[ZoloWalletListVC alloc] init] animated:YES];
        }
            break;
        case 1:
        {
            MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
            self.isHidenNav = YES;
            [self.navigationController pushViewController:[[ZoloWalletViewController alloc] initWithUrlInfo:login.json.WALLET_URL isHidden:NO] animated:YES];
        }
            break;
        case 2:
        {
            [self.navigationController pushViewController:[ZoloCustomViewController new] animated:YES];
        }
            break;
        case 3:
        {
            [self.navigationController pushViewController:[ZoloSystemNoticeVC new] animated:YES];
        }
            break;
        case 4:
        {
            [self.navigationController pushViewController:[ZoloNoticeVC new] animated:YES];
        }
            break;
        case 5:
        {
            self.isHidenNav = YES;
            [self.navigationController pushViewController:[ZoloFeedbackVC new] animated:YES];
        }
            break;
        case 6:
        {
            [self getShareContent];
        }
            break;
        case 7:
        {
            [self.navigationController pushViewController:[ZoloCollectVC new] animated:YES];
        }
            break;
        case 8:
        {
            [self.navigationController pushViewController:[ZoloSettingVC new] animated:YES];
        }
            break;
        default:
        {
            [self.navigationController pushViewController:[FZAnimationVC new] animated:YES];
        }
            break;
    }
}

- (void)getShareContent {
    NSDictionary *data = @{
        @"isActive" : @(0),
        @"url_ios" : @"https://luckmoney8888.com/im_img/ios_share/share",
        @"url_ios_share" : @"https://luckmoney8888.com/dmChat/index.html"
    };
    
    [self.navigationController pushViewController:[[ZoloShareViewController alloc] initWithActive:[data[@"isActive"] boolValue] withUrl:data[@"url_ios"] shareUrl:data[@"url_ios_share"]] animated:YES];
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section {
    
        ZoloMyHeadView *headView = [tableView dequeueReusableHeaderFooterViewWithIdentifier:NSStringFromClass([ZoloMyHeadView class])];
        [headView.icon sd_setImageWithURL:[NSURL URLWithString:self.userInfo.portrait] placeholderImage:SDImageDefault];
        headView.nickName.text = self.userInfo.displayName;
        headView.remark.text = [NSString stringWithFormat:@"ID:%@", self.userInfo.userId == nil ? @"" : self.userInfo.userId];
        WS(ws);
    
        NSString *nftStr = [DMCCUserInfo getNft:self.userInfo.describes];
        if (nftStr.length > 0) {
            headView.nftImg.hidden = NO;
        } else {
            headView.nftImg.hidden = YES;
        }
    
        headView.longGesClickBlock = ^{
            [ws longPressClick];
        };
        headView.headBtnBlock = ^(NSInteger index) {
            [ws headBtnClick:index];
        };
        return headView;
    
}

- (UIView *)tableView:(UITableView *)tableView viewForFooterInSection:(NSInteger)section {
    UIView *view = [UIView new];
    view.backgroundColor = [FontManage MHGrayColor];
    return view;
}

- (void)longPressClick {
    self.isHidenNav = YES;
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

- (void)headBtnClick:(NSInteger)index {
    self.isHidenNav = NO;
    if (index == 10) {
        self.isHidenNav = YES;
        [self.navigationController pushViewController:[ZoloMyQrCodeVC new] animated:YES];
    } else if (index == 11) {
        [self.navigationController pushViewController:[[ZoloPersonVC alloc] initWithUserInfo:self.userInfo] animated:YES];
    }
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    if (section == 0) {
        return 150 + iPhoneX_bottomH;
    } else {
        return 0.001;
    }
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 0.001;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    if (indexPath.row == 0) {
        BOOL isShow = [self getWalletHidden];
        if (isShow) {
            return self.scaleHeight(54);
        } else {
            return 0.001;
        }
    } else if (indexPath.row == 1) {
        return 0.001;
    } else if (indexPath.row == 7) {
        BOOL isShow = [self getCollectHidden];
        if (isShow) {
            return self.scaleHeight(54);
        } else {
            return 0.001;
        }
    } else {
        return self.scaleHeight(54);
    }
}

- (BOOL)getWalletHidden {
    NSArray *array = [[DMCCIMService sharedDMCIMService] getWalletInfoList];
    BOOL isShow = array.count > 0;
    return isShow;
}

- (BOOL)getCollectHidden {
    NSArray *array = [[DMCCIMService sharedDMCIMService] getColletInfoList];
    BOOL isShow = array.count > 0;
    return isShow;
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
