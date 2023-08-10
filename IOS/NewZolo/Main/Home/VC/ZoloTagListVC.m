//
//  ZoloTagListVC.m
//  NewZolo
//
//  Created by JTalking on 2022/10/28.
//

#import "ZoloTagListVC.h"
#import "ZoloGroupCell.h"
#import "ZoloSingleCell.h"
#import "ZoloSingleChatVC.h"
#import "LSTPopView.h"
#import "ZoloForwardView.h"
#import "EmptyVC.h"

@interface ZoloTagListVC (){
    dispatch_queue_t msgQueue;
}

@property (nonatomic, strong) DMCCTagInfo *tagInfo;

@property(nonatomic, strong)NSMutableArray *conversations;
@property (nonatomic, strong) EmptyVC *emptyVC;
@property (nonatomic, strong) NSObject *luck;

@end

@implementation ZoloTagListVC

- (instancetype)initWithTagInfo:(DMCCTagInfo *)tagInfo {
    if (self = [super init]) {
        self.tagInfo = tagInfo;
    }
    return self;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    dispatch_async(msgQueue, ^{[self getReloadData];});
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    msgQueue = dispatch_queue_create("com.ospn.chatTag", DISPATCH_QUEUE_CONCURRENT);
    _luck = [NSObject new];

    
    [self getData];
    
    self.tableView.contentInset = UIEdgeInsetsMake(0.05, 0, 180 + iPhoneX_bottomH, 0);
    self.tableView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
    self.tableView.separatorColor = [FontManage MHLineSeparatorColor];
    self.tableView.backgroundColor = [FontManage MHGrayColor];
    
    [self registerCellWithNibName:NSStringFromClass([ZoloSingleCell class]) isTableview:YES];
    [self registerCellWithNibName:NSStringFromClass([ZoloGroupCell class]) isTableview:YES];
    MJRefreshNormalHeader *header = [MJRefreshNormalHeader headerWithRefreshingTarget:self refreshingAction:@selector(conversationListMessages)];
    header.lastUpdatedTimeLabel.hidden = YES;
    header.stateLabel.hidden = YES;
    self.tableView.mj_header = header;

    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onReceiveMessages:) name:kReceiveMessages object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(conversationListMessages) name:FZ_EVENT_CONVERSATIONLISTCHANG_STATE object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(conversationListMessages) name:kRecallMsgInfoUpdated object:nil];
}

- (EmptyVC *)emptyVC {
    if (!_emptyVC) {
        _emptyVC = [[EmptyVC alloc] init];
        _emptyVC.view.frame = CGRectMake(0, 0, KScreenWidth, KScreenHeight);
    }
    return _emptyVC;
}

// 收到消息，判断消息是否相同类型，否则丢弃
- (void)onReceiveMessages:(NSNotification *)notification {
    NSArray<DMCCMessage *> *messages = notification.object;
    DMCCMessage *msg = [messages firstObject];
    if (msg.conversation.type == Single_Type || msg.conversation.type == Group_Type) {
        [self reloadOldData:msg];
    }
}

- (void)reloadOldData:(DMCCMessage *)message {
    
    DMCCConversationInfo *info = [[DMCCIMService sharedDMCIMService] getMsgConversationInfo:message];
    NSInteger index = [self conversationAtIndex:info.conversation.target];
    @synchronized (_luck) {
        dispatch_async(dispatch_get_main_queue(), ^{
            if (index != -1) {
                [self.conversations replaceObjectAtIndex:index withObject:info];
                NSIndexPath *indexPath=[NSIndexPath indexPathForRow:index inSection:0];
                [self.tableView reloadRowsAtIndexPaths:[NSArray arrayWithObjects:indexPath,nil] withRowAnimation:UITableViewRowAnimationNone];
                
                dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.2 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                    [self insertTopConversation:info];
                });

            }
        });
    }
}

- (void)insertTopConversation:(DMCCConversationInfo *)info {
    NSInteger index = [self conversationAtIndex:info.conversation.target];
    @synchronized (_luck) {
        dispatch_async(dispatch_get_main_queue(), ^{
            if (index != -1) {
                [self.conversations removeObjectAtIndex:index];
                [self.conversations insertObject:info atIndex:[self getTopDataIndex]];
                [self.tableView reloadData];
            }
        });
    }
}

// 获取消息下标，用来局部刷新
- (NSInteger)conversationAtIndex:(NSString *)target {
    NSInteger count = self.conversations.count;
    if (count == 0) {
        [self getData];
        return -1;
    }
    for(int i = 0; i < count; i++){
        DMCCConversationInfo *info = self.conversations[i];
        if ([info.conversation.target isEqualToString:target]) {
            return i;
        }
    }
    return -1;
}

- (void)conversationListMessages {
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(3 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [self getData];
    });
}

- (void)getReloadData {
    if (self.conversations.count == 0) {
        return;
    }
    @synchronized (_luck) {
        NSMutableArray *oldArray = [self.conversations mutableCopy];
        for (DMCCConversationInfo *oldInfo in oldArray) {
            DMCCConversationInfo *info = [[DMCCIMService sharedDMCIMService] getMsgConversationInfo:oldInfo.lastMessage];
            NSInteger index = [self conversationAtIndex:info.conversation.target];
            dispatch_async(dispatch_get_main_queue(), ^{
                if (index != -1) {
                    if (oldArray.count == self.conversations.count) {
                        [self.conversations replaceObjectAtIndex:index withObject:info];
                        NSIndexPath *indexPath=[NSIndexPath indexPathForRow:index inSection:0];
                        [self.tableView reloadRowsAtIndexPaths:[NSArray arrayWithObjects:indexPath,nil] withRowAnimation:UITableViewRowAnimationNone];
                    }
                }
            });
        }
    }
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.5 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [self sortData];
    });
}

- (void)sortData {
    dispatch_async(msgQueue, ^{
        [self topDataSourse];
        dispatch_async(dispatch_get_main_queue(), ^{
            @synchronized (self->_luck) {
                [self.tableView reloadData];
            }
        });
    });    
}

- (void)getData {
    @synchronized (_luck) {
        self.conversations = [NSMutableArray arrayWithArray:[[DMCCIMService sharedDMCIMService] getConversationInfoTags:@[@(Single_Type),@(Group_Type)] lines:@[@(0)] tags:self.tagInfo.id]];
    }
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.tableView.mj_header endRefreshing];
        [self sortData];
        if (self.conversations == nil || self.conversations.count == 0) {
            //  没有数据
            [self.view insertSubview:self.emptyVC.view aboveSubview:self.tableView];
        }else {
            [self.emptyVC.view removeFromSuperview];
        }
    });
}

- (id)getUserInfo:(DMCCConversationInfo *)conv {
    if (conv.conversation.type == Single_Type) {
        DMCCUserInfo *user = [[DMCCIMService sharedDMCIMService] getUserInfo:conv.conversation.target refresh:NO];
        return user;
    } else {
        DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:conv.conversation.target refresh:NO];
        return group;
    }
}

- (void)topDataSourse {
    int i = 0;
    NSMutableArray *array = [[NSMutableArray alloc] init];
    for (DMCCConversationInfo *model in self.conversations) {
        if (model.isTop) {
            i++;
            [array addObject:model];
        }
    }
    [self.conversations removeObjectsInArray:array];
    NSIndexSet *indexSet = [NSIndexSet indexSetWithIndexesInRange:NSMakeRange(0, array.count)];
    [self.conversations insertObjects:array atIndexes:indexSet];
}

- (NSInteger)getTopDataIndex {
    int i = 0;
    NSMutableArray *array = [[NSMutableArray alloc] init];
    for (DMCCConversationInfo *model in self.conversations) {
        if (model.isTop) {
            i++;
        }
    }
    return i;
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.conversations.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    DMCCConversationInfo *info = self.conversations[indexPath.row];
    id dataInfo = [self getUserInfo:info];
    if ([dataInfo isKindOfClass:[DMCCUserInfo class]]) {
        ZoloSingleCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloSingleCell class])];
        cell.userInfo = dataInfo;
        if (info.isTop) {
            cell.contentView.backgroundColor = [FontManage MHGrayColor];
        } else {
            cell.contentView.backgroundColor = [UIColor clearColor];
        }
        cell.msgTime.text = [MHDateTools formatTimeLabel:info.timestamp];
        DMCCMessage *msg = [[DMCCIMService sharedDMCIMService] getMessage:info.lastMessage.messageId];
        cell.msgContent.text = msg.digest;
        cell.msgContent.textColor = [FontManage MHTitleSubColor];
        
        if (info.draft.length > 0) {
            NSString * text = [NSString stringWithFormat:LocalizedString(@"ChatMessageDraft"), info.draft];
            NSMutableAttributedString *attrStr = [[NSMutableAttributedString alloc] initWithString:text];
            [attrStr addAttribute:NSForegroundColorAttributeName
                            value:[FontManage MHMsgRecColor]
                            range:NSMakeRange(0, LocalizedString(@"ChatMessageDraft").length - 2)];
            cell.msgContent.attributedText = attrStr;
        }
  
        if (info.isSilent) {
            cell.silentImg.hidden = NO;
            cell.redView.hidden = !(info.unreadCount.unread > 0);
            cell.msgRead.hidden = YES;
        } else {
            cell.redView.hidden = YES;
            cell.silentImg.hidden = YES;
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
        }
        
        WS(ws);
        cell.cellLongBlock = ^{
            NSLog(@"===cellLongBlock====");
            [ws cellLongBtnClick:indexPath.row];
        };
       
        return cell;
    } else {
        ZoloGroupCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloGroupCell class])];
        DMCCConversationInfo *info = self.conversations[indexPath.row];
        cell.groupInfo = dataInfo;
        if (info.isTop) {
            cell.contentView.backgroundColor = [FontManage MHGrayColor];
        } else {
            cell.contentView.backgroundColor = [UIColor whiteColor];
        }
        cell.msgTime.text = [MHDateTools formatTimeLabel:info.timestamp];
        DMCCMessage *msg = [[DMCCIMService sharedDMCIMService] getMessage:info.lastMessage.messageId];
        cell.msgContent.text = msg.digest;
                
        cell.msgContent.textColor = [FontManage MHTitleSubColor];
        
        if (info.unreadCount.unreadMention > 0) {
            NSString * text = [NSString stringWithFormat:@"%@%@", LocalizedString(@"session_mention"), msg.digest];
            NSMutableAttributedString *attrStr = [[NSMutableAttributedString alloc] initWithString:text];
            [attrStr addAttribute:NSForegroundColorAttributeName
                            value:[FontManage MHMsgRecColor]
                            range:NSMakeRange(0, LocalizedString(@"session_mention").length)];
            cell.msgContent.attributedText = attrStr;
        }
        
        if (info.draft.length > 0) {
            NSString * text = [NSString stringWithFormat:LocalizedString(@"ChatMessageDraft"), info.draft];
            NSMutableAttributedString *attrStr = [[NSMutableAttributedString alloc] initWithString:text];
            [attrStr addAttribute:NSForegroundColorAttributeName
                            value:[FontManage MHMsgRecColor]
                            range:NSMakeRange(0, LocalizedString(@"ChatMessageDraft").length - 2)];
            cell.msgContent.attributedText = attrStr;
        }
        
        if (info.isSilent) {
            cell.silentImg.hidden = NO;
            cell.redView.hidden = !(info.unreadCount.unread > 0);
            cell.msgRead.hidden = YES;
        } else {
            cell.redView.hidden = YES;
            cell.silentImg.hidden = YES;
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
        }
        WS(ws);
        cell.cellLongBlock = ^{
            [ws cellLongBtnClick:indexPath.row];
        };
        return cell;
    }
}

#pragma mark - 转发  删除
- (void)cellLongBtnClick:(NSInteger)itemIndex {
    DMCCConversationInfo *info = self.conversations[itemIndex];
    ZoloForwardView *forwardView = [[ZoloForwardView alloc] initWithFrame:CGRectZero];
    [forwardView setSasstion:YES];
    forwardView.titleArray = @[LocalizedString(@"DeleteContant"),LocalizedString(@"ClearConcastion"),info.isTop ? LocalizedString(@"MsgCancelTop") : LocalizedString(@"MsgTop")];
    LSTPopView *popView = [LSTPopView initWithCustomView:forwardView
                                                popStyle:LSTPopStyleSmoothFromTop
                                            dismissStyle:LSTDismissStyleSmoothToBottom];
    popView.popStyle = LSTPopStyleNO;
    popView.dismissStyle = LSTDismissStyleNO;
    popView.popDuration = 1.0;
    popView.dismissDuration = 1.0;
    LSTPopViewWK(popView)
    popView.bgClickBlock = ^{
        [wk_popView dismiss];
    };
    popView.dragStyle = LSTDragStyleAll;
    popView.sweepStyle = LSTSweepStyleALL;
    popView.sweepDismissStyle = LSTSweepDismissStyleVelocity;
    [popView pop];
    WS(ws);
    forwardView.forwardBlock = ^(NSInteger index) {
        [ws forwardViewClick:index withIndex:itemIndex];
        [wk_popView dismiss];
    };
}

- (void)forwardViewClick:(NSInteger)type withIndex:(NSInteger)itemIndex {
    WS(ws);
    switch (type) {
        case 0:
        {
            NSString *str = LocalizedString(@"AlertSureDel");
            [MHAlert showCustomeAlert:str withAlerttype:MHAlertTypeWarn withOkBlock:^(UIAlertAction *action) {
                if ([action.title isEqualToString:LocalizedString(@"Sure")]) {
                    [ws deleteChat:itemIndex];
                }
            }];
        }
            break;
        case 1:
        {
            NSString *str = LocalizedString(@"AlertClear");
            [MHAlert showCustomeAlert:str withAlerttype:MHAlertTypeWarn withOkBlock:^(UIAlertAction *action) {
                if ([action.title isEqualToString:LocalizedString(@"Sure")]) {
                    [ws clearChat:itemIndex];
                }
            }];
        }
            break;
        case 2:
        {
            DMCCConversationInfo *info = self.conversations[itemIndex];
            [self topChat:itemIndex isTop:info.isTop ? NO : YES];
        }
            break;
        default:
            break;
    }
}

// 删除会话
- (void)deleteChat:(NSInteger)index {
    DMCCConversationInfo *info = self.conversations[index];
    [[DMCCIMService sharedDMCIMService] clearUnreadStatus:info.conversation];
    [[DMCCIMService sharedDMCIMService] clearMessages:info.conversation];
    [[DMCCIMService sharedDMCIMService] removeConversation:info.conversation clearMessage:YES];
    [self getData];
    [[NSNotificationCenter defaultCenter] postNotificationName:FZ_EVENT_MSGSTATUS_STATE object:nil];
}

// 清空会话
- (void)clearChat:(NSInteger)index {
    DMCCConversationInfo *info = self.conversations[index];
    [[DMCCIMService sharedDMCIMService] clearUnreadStatus:info.conversation];
    [[DMCCIMService sharedDMCIMService] clearMessages:info.conversation];
    [self getData];
    [[NSNotificationCenter defaultCenter] postNotificationName:FZ_EVENT_MSGSTATUS_STATE object:nil];
}

// 置顶消息
- (void)topChat:(NSInteger)index isTop:(BOOL)isTop {
    DMCCConversationInfo *info = self.conversations[index];
    WS(ws);
    [[DMCCIMService sharedDMCIMService] setConversation:info.conversation top:isTop success:^{
        [ws getData];
    } error:^(int error_code) {
    }];
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
    return self.scaleHeight(80);
}

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
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

