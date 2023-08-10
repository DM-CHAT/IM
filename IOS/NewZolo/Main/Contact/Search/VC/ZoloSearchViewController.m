//
//  ZoloSearchViewController.m
//  NewZolo
//
//  Created by JTalking on 2022/8/29.
//

#import "ZoloSearchViewController.h"
#import "EmptyVC.h"
#import "ZoloContactCell.h"
#import "ZoloContactGroupCell.h"
#import "ZoloContactHead.h"
#import "ZoloSingleChatVC.h"
#import "ZoloContactDetailVC.h"

@interface ZoloSearchViewController () <UISearchBarDelegate>

@property (weak, nonatomic) IBOutlet UISearchBar *searchBar;
@property (nonatomic, strong) EmptyVC *emptyVC;

@property (nonatomic, strong) NSArray *users;
@property (nonatomic, strong) NSArray *groups;

@end

@implementation ZoloSearchViewController

-(void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self.navigationController setNavigationBarHidden:YES animated:animated];
    [self.searchBar becomeFirstResponder];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [self.navigationController setNavigationBarHidden:NO animated:animated];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    self.searchBar.placeholder = LocalizedString(@"PleaseEnterSearch");
    self.searchBar.barTintColor = [FontManage MHWhiteColor];
    self.searchBar.searchBarStyle = UISearchBarStyleMinimal;
    self.tableView.y = 80 + iPhoneX_topH;
    self.tableView.height = KScreenHeight - (80 + iPhoneX_topH);
    [self registerCellWithNibName:NSStringFromClass([ZoloContactCell class]) isTableview:YES];
    [self registerCellWithNibName:NSStringFromClass([ZoloContactGroupCell class]) isTableview:YES];
    [self registerHeaderFooterWithNibName:NSStringFromClass([ZoloContactHead class])];
    if (@available(iOS 9, *)) {
        [[UIBarButtonItem appearanceWhenContainedInInstancesOfClasses:@[[UISearchBar class]]] setTitleTextAttributes:[NSDictionary dictionaryWithObjectsAndKeys:[FontManage MHMainColor],NSForegroundColorAttributeName,nil] forState:UIControlStateNormal];
    } else {
        [[UIBarButtonItem appearanceWhenContainedIn:[UISearchBar class], nil] setTitleTextAttributes:[NSDictionary dictionaryWithObjectsAndKeys:[FontManage MHMainColor],NSForegroundColorAttributeName,nil] forState:UIControlStateNormal];
    }
    
    self.searchBar.delegate = self;
    
    [self getData];
}

- (void)getData {
    
    if ((self.users == nil || self.users.count == 0) && (self.groups == nil || self.groups.count == 0)) {
        //  没有数据
        [self.view insertSubview:self.emptyVC.view aboveSubview:self.tableView];
    }else {
        [self.emptyVC.view removeFromSuperview];
    }
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 2;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    if (section == 0) {
        return self.users.count;
    } else {
        return self.groups.count;
    }
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    if (indexPath.section == 0) {
        ZoloContactCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloContactCell class])];
        cell.userInfo = self.users[indexPath.row];
        return cell;
    } else {
        ZoloContactGroupCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloContactGroupCell class])];
        DMCCGroupInfo *info = self.groups[indexPath.row];
        [cell.icon sd_setImageWithURL:[NSURL URLWithString:info.portrait] placeholderImage:SDImageDefault];
        cell.titleName.text = info.name;
        return cell;
    }
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    if (indexPath.section == 0) {
        DMCCUserInfo *user = self.users[indexPath.row];
        [self.navigationController pushViewController:[[ZoloContactDetailVC alloc] initWithUserInfo:user] animated:YES];
    } else {
        DMCCGroupInfo *groupInfo = self.groups[indexPath.row];
        DMCCConversation *conversation = [DMCCConversation conversationWithType:Group_Type target:groupInfo.target line:0];
        DMCCConversationInfo *info = [DMCCConversationInfo new];
        info.conversation = conversation;
        [self.navigationController pushViewController:[[ZoloSingleChatVC alloc] initWithConversationInfo:info] animated:YES];
    }
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    if (section == 0) {
        return self.users.count > 0 ? 20 : 0.001;
    } else {
        return self.groups.count > 0 ? 20 : 0.001;
    }
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section {
    ZoloContactHead *head = [tableView dequeueReusableHeaderFooterViewWithIdentifier:NSStringFromClass([ZoloContactHead class])];
    if (section == 0) {
        head.titleLab.text = LocalizedString(@"user");
    } else {
        head.titleLab.text = LocalizedString(@"Group");
    }
    head.titleLab.font = [UIFont systemFontOfSize:12];
    head.contentView.backgroundColor = [FontManage MHLineSeparatorColor];
    return head;
}

- (UIView *)tableView:(UITableView *)tableView viewForFooterInSection:(NSInteger)section {
    UIView *view = [UIView new];
    view.backgroundColor = [FontManage MHLineSeparatorColor];
    return view;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 0.001;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return self.scaleHeight(54);
}

- (EmptyVC *)emptyVC {
    if (!_emptyVC) {
        _emptyVC = [[EmptyVC alloc] init];
        _emptyVC.view.frame = CGRectMake(0, 80 + iPhoneX_topH, KScreenWidth, KScreenHeight);
    }
    return _emptyVC;
}

// UISearchBarDelegate
- (void)searchBarTextDidBeginEditing:(UISearchBar *)searchBar {
    [searchBar setShowsCancelButton:YES animated:YES];
}

// 搜索
- (void)searchBarSearchButtonClicked:(UISearchBar *)searchBar {
    [self serarchWithStr:searchBar.text];
}

- (void)serarchWithStr:(NSString *)str {
    NSString *searchStr = [str stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
    if (searchStr.length == 0) {
        [MHAlert showMessage:LocalizedString(@"PleaseEnterSearch")];
        return;
    }
    WS(ws);
    [[DMCCIMService sharedDMCIMService] searchUser:str searchType:0 page:0 success:^(NSArray<DMCCUserInfo *> *machedUsers) {
        ws.users = machedUsers;
        [ws getData];
        [ws.tableView reloadData];
    } error:^(int errorCode) {
        
    }];
    
    [[DMCCIMService sharedDMCIMService] searchGroup:str page:0 success:^(NSArray<DMCCGroupInfo *> *machedGroups) {
        ws.groups = machedGroups;
        [ws getData];
        [ws.tableView reloadData];
    } error:^(int errorCode) {
        
    }];
}

// 取消
- (void)searchBarCancelButtonClicked:(UISearchBar *)searchBar {
    [self dismissViewControllerAnimated:NO completion:nil];
    [self.navigationController popViewControllerAnimated:YES];
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
