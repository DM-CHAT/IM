//
//  ZoloNoticeVC.m
//  NewZolo
//
//  Created by JTalking on 2022/9/15.
//

#import "ZoloNoticeVC.h"
#import "ZoloSingleCell.h"
#import "ZoloSingleChatVC.h"
#import "EmptyVC.h"

@interface ZoloNoticeVC ()

@property(nonatomic, strong)NSMutableArray *conversations;
@property (nonatomic, strong) EmptyVC *emptyVC;

@end

@implementation ZoloNoticeVC

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self getData];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [self registerCellWithNibName:NSStringFromClass([ZoloSingleCell class]) isTableview:YES];
}

- (void)getData {
    [self.dataSource removeAllObjects];
    [self.tableView reloadData];
    self.conversations = [NSMutableArray arrayWithArray:[[DMCCIMService sharedDMCIMService] getConversationInfos:@[@(Service_Type)] lines:@[@(0)]]];
    for (DMCCConversationInfo *conv in self.conversations) {
        [self getUserInfo:conv];
    }
    if (self.conversations == nil || self.conversations.count == 0) {
        //  没有数据
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

- (void)getUserInfo:(DMCCConversationInfo *)conv {
    DMCCUserInfo *user = [[DMCCIMService sharedDMCIMService] getUserInfo:conv.conversation.target refresh:NO];
    WS(ws);
    [self.dataSource addObject:user];
    dispatch_async(dispatch_get_main_queue(), ^{
        [ws.tableView reloadData];
    });
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.dataSource.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ZoloSingleCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloSingleCell class])];
    cell.userInfo = self.dataSource[indexPath.row];
    DMCCConversationInfo *info = self.conversations[indexPath.row];
    if (info.unreadCount.unread > 0) {
        if (info.unreadCount.unread > 99) {
            cell.msgRead.text = [NSString stringWithFormat:@"99+"];
        } else {
            cell.msgRead.text = [NSString stringWithFormat:@"%d", info.unreadCount.unread];
        }
        cell.msgRead.hidden = NO;
    } else {
        cell.msgRead.hidden = YES;
    }

    cell.msgTime.text = [MHDateTools formatTimeLabel:info.timestamp];
    DMCCMessage *msg = [[DMCCIMService sharedDMCIMService] getMessage:info.lastMessage.messageId];
    cell.msgContent.text = msg.digest;
    
    if ([info.conversation.target containsString:@"OSNS"]) {
        DMCCLitappInfo *litInfo = [[DMCCIMService sharedDMCIMService] getLitapp:info.conversation.target];
        [cell.icon sd_setImageWithURL:[NSURL URLWithString:litInfo.portrait] placeholderImage:SDImageDefault];
        cell.nickName.text = litInfo.name;
    }
    
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    DMCCConversationInfo *info = self.conversations[indexPath.row];
    [[DMCCIMService sharedDMCIMService] clearUnreadStatus:info.conversation];
    [self.navigationController pushViewController:[[ZoloSingleChatVC alloc] initWithConversationInfo:self.conversations[indexPath.row]] animated:YES];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 0.001;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 0.001;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return self.scaleHeight(100);
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
