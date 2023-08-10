//
//  ZoloGroupVC.m
//  NewZolo
//
//  Created by JTalking on 2022/6/30.
//

#import "ZoloContactGroupVC.h"
#import "ZoloContactGroupCell.h"
#import "ZoloSingleChatVC.h"
#import "EmptyVC.h"

@interface ZoloContactGroupVC ()

@property (nonatomic, strong) EmptyVC *emptyVC;

@end

@implementation ZoloContactGroupVC

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.tableView.contentInset = UIEdgeInsetsMake(0, 0, 50 + iPhoneX_bottomH, 0);
    [self registerCellWithNibName:NSStringFromClass([ZoloContactGroupCell class]) isTableview:YES];
    MJRefreshNormalHeader *header = [MJRefreshNormalHeader headerWithRefreshingTarget:self refreshingAction:@selector(userFresh)];
    header.lastUpdatedTimeLabel.hidden = YES;
    header.stateLabel.hidden = YES;
    self.tableView.mj_header = header;
    [self getData];
}

- (void)userFresh {
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [self.tableView.mj_header endRefreshing];
    });
    NSArray *ids = [[DMCCIMService sharedDMCIMService] getFavGroups:YES];
    [self.dataSource removeAllObjects];
    for (NSString *groupId in ids) {
        [self getGroupInfo:groupId];
    }
    [self.tableView reloadData];
    if (self.dataSource == nil || self.dataSource.count == 0) {
        //  没有数据
        [self.view insertSubview:self.emptyVC.view aboveSubview:self.tableView];
    }else {
        [self.emptyVC.view removeFromSuperview];
    }
}

- (void)getData {
    NSArray *ids = [[DMCCIMService sharedDMCIMService] getFavGroups];
    [self.dataSource removeAllObjects];
    for (NSString *groupId in ids) {
        [self getGroupInfo:groupId];
    }
    [self.tableView reloadData];
    if (self.dataSource == nil || self.dataSource.count == 0) {
        //  没有数据
        [self userFresh];
        [self.view insertSubview:self.emptyVC.view aboveSubview:self.tableView];
    }else {
        [self.emptyVC.view removeFromSuperview];
    }
}

- (EmptyVC *)emptyVC {
    if (!_emptyVC) {
        _emptyVC = [[EmptyVC alloc] init];
        _emptyVC.view.frame = CGRectMake(0, 0, KScreenWidth, KScreenHeight);
    }
    return _emptyVC;
}

- (void)getGroupInfo:(NSString *)groupId {
    DMCCGroupInfo *groupInfo = [[DMCCIMService sharedDMCIMService] getGroupInfo:groupId refresh:NO];
    if (groupInfo) {
        groupInfo.target = groupId;
        [self.dataSource addObject:groupInfo];
    }
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.dataSource.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ZoloContactGroupCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloContactGroupCell class])];
    DMCCGroupInfo *info = self.dataSource[indexPath.row];
    [cell.icon sd_setImageWithURL:[NSURL URLWithString:info.portrait] placeholderImage:SDImageDefault];
    cell.titleName.text = info.name;
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    DMCCGroupInfo *groupInfo = self.dataSource[indexPath.row];
    DMCCConversation *conversation = [DMCCConversation conversationWithType:Group_Type target:groupInfo.target line:0];
    DMCCConversationInfo *info = [DMCCConversationInfo new];
    info.conversation = conversation;
    [self.navigationController pushViewController:[[ZoloSingleChatVC alloc] initWithConversationInfo:info] animated:YES];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 0.001;
}


- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 0.001;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return self.scaleHeight(80);
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
