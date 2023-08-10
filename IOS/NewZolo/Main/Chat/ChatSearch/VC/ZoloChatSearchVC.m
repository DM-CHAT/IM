//
//  ZoloSearchViewController.m
//  NewZolo
//
//  Created by JTalking on 2022/8/29.
//

#import "ZoloChatSearchVC.h"
#import "EmptyVC.h"
#import "ZoloContactHead.h"
#import "ZoloSingleCell.h"
#import "ZoloSingleChatVC.h"

@interface ZoloChatSearchVC () <UISearchBarDelegate>

@property (weak, nonatomic) IBOutlet UISearchBar *searchBar;
@property (nonatomic, strong) EmptyVC *emptyVC;
@property (nonatomic, strong)DMCCConversationInfo *conversationInfo;

@end

@implementation ZoloChatSearchVC

- (instancetype)initWithConversition:(DMCCConversationInfo *)conversationInfo {
    if (self = [super init]) {
        self.conversationInfo = conversationInfo;
    }
    return self;
}

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
    [self registerCellWithNibName:NSStringFromClass([ZoloSingleCell class]) isTableview:YES];
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
    if (self.dataSource == nil || self.dataSource.count == 0) {
        //  没有数据
        [self.view insertSubview:self.emptyVC.view aboveSubview:self.tableView];
    }else {
        [self.emptyVC.view removeFromSuperview];
    }
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.dataSource.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ZoloSingleCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloSingleCell class])];
    DMCCMessage *message = self.dataSource[indexPath.row];
    DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:message.fromUser refresh:NO];
    cell.userInfo = userInfo;
    cell.msgRead.hidden = YES;
    cell.msgTime.text = [MHDateTools formatTimeLabel:message.serverTime];
    cell.msgContent.text = message.digest;
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    DMCCMessage *message = self.dataSource[indexPath.row];
    ZoloSingleChatVC *vc = [[ZoloSingleChatVC alloc] initWithConversationInfo:self.conversationInfo withMessage:message];
    [self.navigationController pushViewController:vc animated:YES];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 20;
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section {
    ZoloContactHead *head = [tableView dequeueReusableHeaderFooterViewWithIdentifier:NSStringFromClass([ZoloContactHead class])];
    head.titleLab.text = LocalizedString(@"chat_record_digest1");
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
    return self.scaleHeight(64);
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
    self.dataSource = [NSMutableArray arrayWithArray:[[DMCCIMService sharedDMCIMService] selectKeywordMessages:self.conversationInfo.conversation keyword:str page:0]];
    [self getData];
    [self.tableView reloadData];
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
