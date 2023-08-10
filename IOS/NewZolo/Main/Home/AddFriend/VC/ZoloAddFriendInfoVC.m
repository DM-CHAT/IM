//
//  ZoloAddFriendInfoVC.m
//  NewZolo
//
//  Created by JTalking on 2022/8/24.
//

#import "ZoloAddFriendInfoVC.h"
#import "ZoloConDetailHeadView.h"
#import "ZoloAddFriendInfoFoot.h"
#import "ZoloAddFriendVerifyVC.h"
#import "YBImageBrowser.h"
#import "ZoloWalletViewController.h"

@interface ZoloAddFriendInfoVC () <UITableViewDelegate, UITableViewDataSource>

@property (nonatomic, strong) DMCCUserInfo *userInfo;

@end

@implementation ZoloAddFriendInfoVC

- (instancetype)initWithUserInfo:(DMCCUserInfo *)userInfo {
    if (self = [super init]) {
        self.userInfo = userInfo;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    [self registerHeaderFooterWithNibName:NSStringFromClass([ZoloAddFriendInfoFoot class])];
    [self registerHeaderFooterWithNibName:NSStringFromClass([ZoloConDetailHeadView class])];
    self.tableView.backgroundColor = [FontManage MHGrayColor];
    self.view.backgroundColor = [FontManage MHGrayColor];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return 1;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [UITableViewCell new];
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 150;
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section {
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
    headView.iconClickBlock = ^{
        [ws iconBtnClickBlock];
    };
    return  headView;
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

- (UIView *)tableView:(UITableView *)tableView viewForFooterInSection:(NSInteger)section {
    ZoloAddFriendInfoFoot *foot = [tableView dequeueReusableHeaderFooterViewWithIdentifier:NSStringFromClass([ZoloAddFriendInfoFoot class])];
    WS(ws);
    foot.addBtnBLock = ^{
        [ws addUserBtnCLick];
    };
    return foot;
}

- (void)addUserBtnCLick {
    [self.navigationController pushViewController:[[ZoloAddFriendVerifyVC alloc] initWithUserInfo:self.userInfo] animated:YES];
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    if ([self.userInfo.userId isEqualToString:[[DMCCIMService sharedDMCIMService] getUserID]]) {
        return 0.001;
    }
    return 48;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 0.001;
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
