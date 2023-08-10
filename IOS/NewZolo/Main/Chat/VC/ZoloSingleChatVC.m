//
//  ZoloSingleChatVC.m
//  NewZolo
//
//  Created by JTalking on 2022/6/30.
//

#import "ZoloSingleChatVC.h"
#import "ZoloBaseChatCell.h"
#import "ZoloChatTestMsgCell.h"
#import "ZoloChatImageMsgCell.h"
#import "ZoloChatVedioCell.h"
#import "ZoloChatCardCell.h"
#import "ZoloChatVoiceCell.h"
#import "ZoloRedPacketCell.h"
#import "ZoloInformationCell.h"
#import "ZoloChatCallCell.h"
#import "ZoloChatFileCell.h"
#import "ZoloChatSendView.h"
#import "ZoloEmoOrImgView.h"
#import "ZoloGroupDetailVC.h"
#import "ZoloChatMoreView.h"
#import "TZImagePickerController.h"
#import "DMCUConfigManager.h"
#import "HXCustomCameraViewController.h"
#import "HXPhotoManager.h"
#import "ZoloCardVC.h"
#import "LSTPopView.h"
#import "ZoloCardView.h"
#import "ZoloPlayViewController.h"
#import "ZoloContactDetailVC.h"
#import "YBImageBrowser.h"
#import "YBIBVideoData.h"
#import "ZoloAudioView.h"
#import "LGAudioPlayer.h"
#import "ZoloChatDetailVC.h"
#import "ZoloForwardView.h"
#import "ZoloDappViewController.h"
#import "ZoloAddFriendInfoVC.h"
#import "ZoloDappView.h"
#import "ZoloDappWebView.h"
#import "ZoloTransferVC.h"
#import "ZoloCardContactVC.h"
#import "ZoloSendRedPackVC.h"
#import "ZoloTransferRecordVC.h"
#import "ZoloRedPackView.h"
#import "ZoloRedPackOpenView.h"
#import "ZoloRedPackRecordVC.h"
#import "ZoloAddGroupInfoVC.h"
#import "ZoloNoticeHeadView.h"
#import "ZoloGroupSeeNoticeVC.h"
#import <IQKeyboardManager.h>
#import "WordFilterHelper.h"
#import "ZoloKeywordModel.h"
#import "ZoloSettingContactVC.h"
#import "ZoloReplyView.h"
#import "ZoloTopHeadView.h"
#import "ZoloTopMsgVC.h"
#import "ZoloSingleCallVC.h"
#import "AppDelegate.h"
#import "ZoloWalletViewController.h"
#import "ZoloFileOpenController.h"
#import "ZoloWalletVC.h"
#import "ZoloMulBottomView.h"
#import "ZoloMulTopView.h"
#import "ZoloCardMulVC.h"
#import "ZoloQuoteMsgVC.h"

@interface ZoloSingleChatVC () <UITableViewDelegate,UITableViewDataSource,HXCustomCameraViewControllerDelegate, UIDocumentPickerDelegate, UIDocumentInteractionControllerDelegate,LGAudioPlayerDelegate,UIActionSheetDelegate>

@property (nonatomic, strong) ZoloChatSendView *sendView;
@property (nonatomic, strong) DMCCUserInfo *userInfo; // 聊天对方信息
@property (nonatomic, strong) DMCCUserInfo *myInfo;
@property (nonatomic, strong) DMCCConversationInfo *conversation;
@property (nonatomic, strong) ZoloEmoOrImgView *emoOrImgView;
@property (nonatomic, strong) ZoloChatMoreView *moreView;
@property (nonatomic, assign) NSInteger isLoadMore; // 加载更多数据
@property (nonatomic, assign) NSInteger isOneceLoad; // 加载一次数据
@property (nonatomic, assign) DMCCConversationType chatType;
@property (nonatomic, strong) UIButton *moreBtn; // 群聊 详情
@property (nonatomic, strong) UIImageView *iconImg; // 单聊 头像
@property (nonatomic, strong) UIButton *serveBtn; // 服务 详情
@property (nonatomic, strong) UILabel *titleLab;
@property (nonatomic, strong) UIView *btnView;
@property (nonatomic, strong)NSMutableDictionary<NSNumber *, Class> *cellContentDict; // 计算cell高度
@property (nonatomic, strong) ZoloAudioView *audioView;
@property (nonatomic, strong) ZoloForwardView *forwardView;
@property (nonatomic, strong) HXPhotoManager *manager;
@property (nonatomic, strong)UIDocumentPickerViewController *documentPickerVC;
@property (nonatomic, strong) ZoloDappView *dappView;
@property (nonatomic, strong) ZoloDappWebView *dappWebView;
@property (nonatomic, strong) DMCCLitappInfo *litappInfo;
@property (nonatomic, strong) ZoloNoticeHeadView *noticeView;
@property (nonatomic, strong) NSString *timeInterval;
@property (nonatomic, strong) WordFilterHelper *wordfilter;
@property (nonatomic, strong)UIDocumentInteractionController *doVC;
@property (nonatomic, strong) NSArray *sendNoticeArray;
@property (nonatomic, strong) ZoloReplyView *replyView;
@property (nonatomic, strong) ZoloTopHeadView *topHeadView;
@property (nonatomic, strong) DMCCMessage *replyMsg;
@property (nonatomic, strong) AFHTTPSessionManager *sessionManager;
@property (nonatomic, assign) BOOL isShowBom; // 是否显示扫雷红包
@property (nonatomic, strong) NSMutableArray *upLongArray;
@property (nonatomic, copy) NSString *shareID;
@property (nonatomic, assign) BOOL isShowFooter;
@property (nonatomic, assign) BOOL isRefrash;
@property (nonatomic, strong) NSObject *lock;
@property (nonatomic, assign) BOOL isMulSelect;
@property (nonatomic, strong) ZoloMulTopView *mulTopView;
@property (nonatomic, strong) ZoloMulBottomView *mulBottomView;

@end

@implementation ZoloSingleChatVC

- (instancetype)initWithConversationInfo:(DMCCConversationInfo *)conversation {
    if (self = [super init]) {
        self.conversation = conversation;
        self.lock = [NSObject new];
    }
    return self;
}

- (instancetype)initWithConversationInfo:(DMCCConversationInfo *)conversation withMessage:(DMCCMessage *)message {
    if (self = [super init]) {
        self.conversation = conversation;
        self.dataSource = [NSMutableArray arrayWithObject:message];
        self.isShowFooter = YES;
        self.lock = [NSObject new];
    }
    return self;
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [self mulTopViewCancel];
    [[DMCCIMService sharedDMCIMService] setConversation:self.conversation.conversation draft:self.sendView.textView.text];
    [[IQKeyboardManager sharedManager] setEnable:YES];
    [self.wordfilter freeFilter];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self.navigationController setNavigationBarHidden:NO animated:animated];
    [[IQKeyboardManager sharedManager] setEnable:NO];
    [self getMyUser];
    self.chatType = self.conversation.conversation.type;

    self.wordfilter = [[WordFilterHelper alloc] initWithFilter];

    [self.btnView addSubview:self.titleLab];

    if (self.conversation.draft.length > 0) {
        self.sendView.textView.text = self.conversation.draft;
    }

    if (self.chatType == Single_Type) {

        DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:self.conversation.conversation.target refresh:NO];
        NSString *alias = [[DMCCIMService sharedDMCIMService] getFriendAlias:userInfo.userId];
        dispatch_async(dispatch_get_main_queue(), ^{
            self.titleLab.text = userInfo.displayName;
            if (alias.length > 0) {
                self.titleLab.text = alias;
            }
            self.timeInterval = @"0";
            self.userInfo = userInfo;
            [self.btnView addSubview:self.iconImg];
            self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:self.btnView];
        });

    }
    if (self.chatType == Group_Type) {

        NSArray<DMCCGroupMember *> *groupMembers = [[DMCCIMService sharedDMCIMService] getGroupMembers:self.conversation.conversation.target forceUpdate:NO];
        NSMutableArray *memberIds = [[NSMutableArray alloc] init];
        for (DMCCGroupMember *member in groupMembers) {
            [memberIds addObject:member.memberId];
        }
        [[DMCCIMService sharedDMCIMService] getUserInfos:memberIds inGroup:self.conversation.conversation.target];
        DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:self.conversation.conversation.target refresh:NO];
        dispatch_async(dispatch_get_main_queue(), ^{
            self.titleLab.text = [NSString stringWithFormat:@"%@(%ld)", group.name, group.memberCount];
            [self.btnView addSubview:self.moreBtn];
            self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:self.btnView];
        });

        [self groupUpdata];
        [self getGroupNotice];
        [self getKeyword];
        [self getShowRedPack];
        // 这里是否能开个线程来执行，不要影响主界面
        dispatch_async(dispatch_get_global_queue(0, DISPATCH_QUEUE_PRIORITY_DEFAULT), ^{
            self.timeInterval = [[DMCCIMService sharedDMCIMService] getTimeIntervalGroupId:self.conversation.conversation.target];
            if ([[DMCCIMService sharedDMCIMService] getGroupZoneMembersTop:self.conversation.conversation.target begin:0 forceUpdate:NO].count <= 1) {
                [[DMCCIMService sharedDMCIMService] getGroupZoneMembers:self.conversation.conversation.target begin:0 forceUpdate:YES];
            }
        });
    } else if (self.chatType == Service_Type) {

        if ([self.conversation.conversation.target containsString:@"OSNS"]) {
            DMCCLitappInfo *appInfo = [[DMCCIMService sharedDMCIMService] getLitapp:self.conversation.conversation.target];
            if (appInfo) {
                [self.btnView addSubview:self.serveBtn];
                self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:self.btnView];
            }
        }

    }
    [self recoverMoreView];
    [self recoverEmjView];
}

- (void)getKeyword {
    WS(ws);
    dispatch_async(dispatch_get_global_queue(0, DISPATCH_QUEUE_PRIORITY_DEFAULT), ^{
        [[ZoloAPIManager instanceManager] getKeywordListAndGroupMuteTimeWithGroupId:self.conversation.conversation.target WithCompleteBlock:^(ZoloKeywordModel * _Nonnull data) {
            if (data) {
                for (ZoloKeywdModel *model in data.keywordList) {
                    [ws.wordfilter insertWords:model.content];
                }
            }
        }];
    });
}

- (void)getShowRedPack {
    WS(ws);
    dispatch_async(dispatch_get_global_queue(0, DISPATCH_QUEUE_PRIORITY_DEFAULT), ^{
        [[ZoloAPIManager instanceManager] bombHiddenWithGroupId:self.conversation.conversation.target WithCompleteBlock:^(BOOL isSuccess) {
            dispatch_async(dispatch_get_main_queue(), ^{
                ws.moreView.isShowBom = isSuccess;
                ws.isShowBom = isSuccess;
            });
        }];
    });
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.cellContentDict = [[NSMutableDictionary alloc] init];
    self.chatType = self.conversation.conversation.type;
    UIImage *image = [UIImage imageNamed:@"chatBg"];
    self.tableView.layer.contents = (id) image.CGImage;
    self.tableView.layer.backgroundColor = [UIColor clearColor].CGColor;

    MJRefreshNormalHeader *header = [MJRefreshNormalHeader headerWithRefreshingTarget:self refreshingAction:@selector(loadMoreData)];
    header.lastUpdatedTimeLabel.hidden = YES;
    header.stateLabel.hidden = YES;
    self.tableView.mj_header = header;

    if (self.isShowFooter) {
        MJRefreshAutoFooter *footer = [MJRefreshAutoFooter footerWithRefreshingTarget:self refreshingAction:@selector(loadMoreFooterData)];
        self.tableView.mj_footer = footer;
    }
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onMessageListChanged:) name:FZ_EVENT_MESSAGELISTCHANG_STATE object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onGroupRefreshChanged) name:FZ_EVENT_GROUPREFRESH_STATE object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onReceiveMessages:) name:kReceiveMessages object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(reloadMessage) name:kRecallMsgInfoUpdated object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(reloadMessage) name:kDeleteMsgInfoUpdated object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(groupUpdata) name:kGroupMemberUpdated object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(insertMessage:) name:FZ_EVENT_MSGINSERTSTATUS_STATE object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillAppearance:) name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillDismiss:) name:UIKeyboardWillHideNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(groupInfoUpdated:) name:kGroupInfoUpdated object:nil];

    [self registerCellWithNibName:NSStringFromClass([ZoloChatTestMsgCell class]) isTableview:YES];
    [self registerCellWithNibName:@"ZoloChatTestMsgMeCell" isTableview:YES];
    [self registerCell:[ZoloChatTestMsgCell class] forContent:[DMCCTextMessageContent class]];

    [self registerCellWithNibName:NSStringFromClass([ZoloChatImageMsgCell class]) isTableview:YES];
    [self registerCellWithNibName:@"ZoloChatImageMsgMeCell" isTableview:YES];
    [self registerCell:[ZoloChatImageMsgCell class] forContent:[DMCCImageMessageContent class]];
    [self registerCell:[ZoloChatImageMsgCell class] forContent:[DMCCStickerMessageContent class]];

    [self registerCellWithNibName:NSStringFromClass([ZoloChatCardCell class]) isTableview:YES];
    [self registerCellWithNibName:@"ZoloChatCardMeCell" isTableview:YES];
    [self registerCell:[ZoloChatCardCell class] forContent:[DMCCCardMessageContent class]];

    [self registerCellWithNibName:NSStringFromClass([ZoloChatVoiceCell class]) isTableview:YES];
    [self registerCellWithNibName:@"ZoloChatVoiceMeCell" isTableview:YES];
    [self registerCell:[ZoloChatVoiceCell class] forContent:[DMCCSoundMessageContent class]];

    [self registerCellWithNibName:NSStringFromClass([ZoloChatVedioCell class]) isTableview:YES];
    [self registerCellWithNibName:@"ZoloChatVedioMeCell" isTableview:YES];
    [self registerCell:[ZoloChatVedioCell class] forContent:[DMCCVideoMessageContent class]];

    [self registerCellWithNibName:NSStringFromClass([ZoloRedPacketCell class]) isTableview:YES];
    [self registerCellWithNibName:@"ZoloRedPacketMeCell" isTableview:YES];
    [self registerCell:[ZoloRedPacketCell class] forContent:[DMCCRedPacketMessageContent class]];

    [self registerCellWithNibName:NSStringFromClass([ZoloChatFileCell class]) isTableview:YES];
    [self registerCellWithNibName:@"ZoloChatFileMeCell" isTableview:YES];
    [self registerCell:[ZoloChatFileCell class] forContent:[DMCCFileMessageContent class]];

    [self registerCellWithNibName:NSStringFromClass([ZoloChatCallCell class]) isTableview:YES];
    [self registerCellWithNibName:@"ZoloChatCallMeCell" isTableview:YES];
    [self registerCell:[ZoloChatCallCell class] forContent:[DMCCCallMessageContent class]];

    [self registerCellWithNibName:NSStringFromClass([ZoloInformationCell class]) isTableview:YES];

    [self registerCell:[ZoloInformationCell class] forContent:[DMCCNotificationMessageContent class]];
    
    [self registerHeaderFooterWithNibName:NSStringFromClass([ZoloNoticeHeadView class])];
    self.tableView.contentInset = UIEdgeInsetsMake(0, 0, 55 + iPhoneX_bottomH, 0);
    self.tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
    [self.tableView reloadData];
    if (self.conversation.conversation.type == Service_Type) {
        // 服务消息不处理
    } else {
        [self.view addSubview:self.sendView];
    }
}

- (void)checkShowDapp {
    dispatch_async(dispatch_get_global_queue(0, DISPATCH_QUEUE_PRIORITY_DEFAULT), ^{
        DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:self.conversation.conversation.target refresh:NO];
        if (group == nil) {
            return;
        }
        WS(ws);
        [[ZoloAPIManager instanceManager] checkBindDappWithGroupId:group.target ownerId:group.owner withCompleteBlock:^(NSDictionary * _Nonnull data) {
            if (data) {
                NSData *dataString = [data[@"dappUrlFront"] dataUsingEncoding:NSUTF8StringEncoding];
                NSDictionary *dataDic = [NSJSONSerialization JSONObjectWithData:dataString options:NSJSONReadingMutableContainers error:nil];
                DMCCLitappInfo *litapp = [DMCCLitappInfo new];
                litapp.name = dataDic[@"name"];
                litapp.target = dataDic[@"target"];
                litapp.url = dataDic[@"url"];
                litapp.portrait = dataDic[@"portrait"];
                litapp.info = dataDic[@"info"];
                litapp.shareId = dataDic[@"shareId"];
                ws.litappInfo = litapp;
                dispatch_async(dispatch_get_main_queue(), ^{
                    ws.dappView.hidden = NO;
                });
            }
        }];
    });
}

- (void)registerCell:(Class)cellCls forContent:(Class)msgContentCls {
    [self.cellContentDict setObject:cellCls forKey:@([msgContentCls getContentType])];
}

// 群组更多
- (void)moreBtnClick {
    if (self.chatType == Group_Type) {
        [self.navigationController pushViewController:[[ZoloGroupDetailVC alloc] initWithConversation:self.conversation withDapp:self.litappInfo isShowBom:self.isShowBom] animated:YES];
    }
}

// 单聊更多
- (void)iconBtnClick {
    [self.navigationController pushViewController:[[ZoloChatDetailVC alloc] initWithUserInfo:self.userInfo withConversition:self.conversation] animated:YES];
}

- (void)getMyUser {
    WS(ws);
    
    [[DMCCIMService sharedDMCIMService] getUserInfo:[[OsnSDK getInstance] getUserID] refresh:NO success:^(DMCCUserInfo *userInfo) {
        if (userInfo) {
            ws.myInfo = userInfo;
            ws.isOneceLoad = 0;
            [ws loadDBMessage];
        }
    } error:^(int errorCode) {
        
    }];
    
}

#pragma mark - 收到消息
// 收到消息，判断消息是否在同意sation，否则丢弃
- (void)onReceiveMessages:(NSNotification *)notification {
    
    @try {
        WS(ws);

        [[DMCCIMService sharedDMCIMService] clearUnreadStatus:self.conversation.conversation];
        NSArray<DMCCMessage *> *messages = notification.object;
        DMCCMessage *msg = [messages firstObject];
        NSLog(@"===11=====onReceiveMessages=======");
        if ([msg.conversation.target isEqualToString:self.conversation.conversation.target]) {
            if (messages.count > 0) {
                @synchronized (ws.lock) {

                    for(int i = 0; i < messages.count; ++i){
                        msg = messages[i];
                        if([msg.content isMemberOfClass:[DMCCCallMessageContent class]]){
                            DMCCCallMessageContent *call = (DMCCCallMessageContent*)msg.content;
                            if(call.duration != -1){
                                NSLog(@"===tableView===addObject===111===");
                                [self.dataSource addObject:msg];
                            }
                        }else{
                            NSLog(@"===tableView===addObject===222===");
                            [self.dataSource addObject:msg];
                        }
                    }
                }
                
                dispatch_async(dispatch_get_main_queue(), ^{
                    if (self.dataSource.count == 0) {
                        return;
                    }
                    @synchronized (ws.lock) {
                        [self.tableView reloadData];
                        if (self.isRefrash) {
                            NSLog(@"===tableView2===%ld===%@==", self.dataSource.count, [NSThread currentThread]);
                            NSIndexPath *index = [NSIndexPath indexPathForRow:self.dataSource.count-1 inSection:0];
                            [self.tableView scrollToRowAtIndexPath:index atScrollPosition:UITableViewScrollPositionBottom animated:false];
                        }
                    }

                });
                
            }
        }
        
    }
    @catch (NSException *exception){
        
    }
}

- (void)scrollViewDidScroll:(UIScrollView *)scrollView {
    NSArray * array = [self.tableView visibleCells];
    ZoloBaseChatCell *cell =  array.lastObject;
    NSInteger msgId = cell.message.messageId;
    
    DMCCMessage *msg = self.dataSource.lastObject;
    
    NSLog(@"===messageId=%ld===msgId==%ld=", msg.messageId, msgId);
    
    if (msg.messageId <= msgId) {
        self.isRefrash = YES;
    } else {
        self.isRefrash = NO;
    }
}

// 清空消息
- (void)onMessageListChanged:(NSNotification *)notification {
    if([notification.object isEqual:self.conversation.conversation]) {
        [self.dataSource removeAllObjects];
        [self.tableView reloadData];
    }
}

// 获取群更新消息
- (void)groupInfoUpdated:(NSNotification *)notification {
    if([notification.object isEqual:self.conversation.conversation]) {
        [self getTopMessage];
    }
}

- (void)getTopMessage {
    dispatch_async(dispatch_get_global_queue(0, DISPATCH_QUEUE_PRIORITY_DEFAULT), ^{
        [self removeTopMsg:self.conversation.conversation.target];
        NSArray *array = [[DMCCIMService sharedDMCIMService] getGroupTopMessageWithGroupId:self.conversation.conversation.target];
        dispatch_async(dispatch_get_main_queue(), ^{
            BOOL isNotice = self.noticeView.hidden;
            if (array.count > 0) {
                self.topHeadView.hidden = NO;
                self.topHeadView.dataSource = array;
                self.tableView.contentInset = UIEdgeInsetsMake(isNotice ? 34 : 32 + 34, 0, 55 + iPhoneX_bottomH, 0);
            } else {
                self.topHeadView.hidden = YES;
                self.tableView.contentInset = UIEdgeInsetsMake(isNotice ? 0 : 22, 0, 55 + iPhoneX_bottomH, 0);
            }
            [self checkShowDapp];
        });
    });
}

// 判断是否禁言
- (void)groupUpdata {
    dispatch_async(dispatch_get_global_queue(0, DISPATCH_QUEUE_PRIORITY_DEFAULT), ^{
        DMCCGroupMember *gm = [[DMCCIMService sharedDMCIMService] getGroupMember:self.conversation.conversation.target memberId:[DMCCNetworkService sharedInstance].userId];
        DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:self.conversation.conversation.target refresh:NO];
        dispatch_async(dispatch_get_main_queue(), ^{
            if (group!=nil) {
                if (group != nil) {
                    if (group.singleMute) {
                        [self.sendView muteViewHidden:NO];
                        return;
                    }
                }
            }
            if (gm.mute && gm.type == Member_Type_Normal) {
                [self.sendView muteViewHidden:NO];
            } else if (group.mute && gm.type == Member_Type_Normal) {
                [self.sendView muteViewHidden:NO];
            } else {
                [self.sendView muteViewHidden:YES];
            }
        });
    });
}

- (void)onGroupRefreshChanged {
    [self.tableView reloadData];
}

- (void)loadMoreData {
    self.isOneceLoad = 1;
    [self loadDBMessage];
}

- (void)reloadMessage {
    [self.dataSource removeAllObjects];
    self.isOneceLoad = 0;
    self.isLoadMore = 0;
    [self loadDBMessage];
}

#pragma mark - 加载本地数据库消息
- (void)loadDBMessage {
    /**
     * 问题1：viewWillAppear 加载第一次，无需重复加载， 需要底部属性  isOneceLoad  是否第一次加载
     * 问题2：上拉加载更多数据，需要顶部刷新； isLoadMore 用来控制加载更多
     */
    @try {
        
            if (self.isOneceLoad == 0 && self.isLoadMore > 0) {
                return;
            }
            
            long lastIndex = 0;
            if (self.dataSource.count) {
                DMCCMessage *msg = [self.dataSource firstObject];
                lastIndex = msg.messageId;
            }
            WS(ws);
            
            NSArray *messageList = [[DMCCIMService sharedDMCIMService] getMessages:ws.conversation.conversation contentTypes:nil from:lastIndex count:50 withUser:nil];
            NSInteger index = self.dataSource.count;
            if (index == 0) {
                index = 1;
            }
            @synchronized (ws.lock) {
                self.dataSource = [NSMutableArray arrayWithArray:[[[messageList reverseObjectEnumerator] allObjects] arrayByAddingObjectsFromArray:self.dataSource]];
                dispatch_async(dispatch_get_main_queue(), ^{
                    [self.tableView.mj_header endRefreshing];
                    [self.tableView reloadData];
                    if (self.dataSource.count == 0) {
                        return;
                    }
                    
                    if (self.isLoadMore == 0) {
                        [self.tableView scrollToRowAtIndexPath:[NSIndexPath indexPathForRow:self.dataSource.count-1 inSection:0] atScrollPosition:UITableViewScrollPositionBottom animated:false];
                    } else {
                        [self.tableView scrollToRowAtIndexPath:[NSIndexPath indexPathForRow:self.dataSource.count-index inSection:0] atScrollPosition:UITableViewScrollPositionTop animated:false];
                    }
                    
                    self.isLoadMore ++;
                    self.isOneceLoad ++;
                });
            }
        
    }
    @catch (NSException *exception){
        
    }
}

- (void)loadMoreFooterData {
    @try {
        
            long lastIndex = 0;
            if (self.dataSource.count) {
                DMCCMessage *msg = [self.dataSource lastObject];
                lastIndex = msg.messageId;
            }
            WS(ws);
            
            NSArray *messageList = [[DMCCIMService sharedDMCIMService] getMessagesBefore:ws.conversation.conversation contentTypes:nil from:lastIndex count:50 withUser:nil];
            if (messageList.count == 0) {
                [self.tableView.mj_footer endRefreshing];
                return;
            }
            NSLog(@"===tableView===addObject===333===");
            @synchronized (ws.lock) {
                [self.dataSource addObjectsFromArray:messageList];
            }
            
            NSInteger index = messageList.count;
            if (index == 0) {
                index = 1;
            }
            dispatch_async(dispatch_get_main_queue(), ^{
                [self.tableView.mj_footer endRefreshing];
                @synchronized (ws.lock) {
                    [self.tableView reloadData];
                
                    if (self.dataSource.count == 0) {
                        return;
                    }
                    NSLog(@"===tableView3===%ld===%@==", self.dataSource.count, [NSThread currentThread]);
                    if (self.isLoadMore == 0) {
                        [self.tableView scrollToRowAtIndexPath:[NSIndexPath indexPathForRow:self.dataSource.count-1 inSection:0] atScrollPosition:UITableViewScrollPositionBottom animated:false];
                    } else {
                        [self.tableView scrollToRowAtIndexPath:[NSIndexPath indexPathForRow:self.dataSource.count-index inSection:0] atScrollPosition:UITableViewScrollPositionTop animated:false];
                    }
                }
            });
        
    }
    @catch (NSException *exception){
        
    }
}

#pragma mark - 视图初始化
- (ZoloReplyView *)replyView {
    if (!_replyView) {
        _replyView = [[ZoloReplyView alloc] initWithFrame:CGRectZero];
        _replyView.hidden = YES;
        WS(ws);
        _replyView.replyViewBlock = ^{
            ws.replyMsg = nil;
            ws.replyView.hidden = YES;
        };
        [self.view addSubview:_replyView];
    }
    return _replyView;
}

- (ZoloTopHeadView *)topHeadView {
    if (!_topHeadView) {
        _topHeadView = [[ZoloTopHeadView alloc] initWithShowNotice:!self.noticeView.hidden];
        _topHeadView.hidden = YES;
        WS(ws);
        _topHeadView.delectBtnBlock = ^{
            [ws topDelectBtnBlock];
        };
        [self.view addSubview:_topHeadView];
    }
    return _topHeadView;
}

- (void)topDelectBtnBlock {
    [self.navigationController pushViewController:[[ZoloTopMsgVC alloc] initWithGroupId:self.conversation.conversation.target] animated:YES];
}

- (ZoloDappView *)dappView {
    if (!_dappView) {
        _dappView = [[ZoloDappView alloc] initWithLitappInfo:self.litappInfo showNotice:!self.noticeView.hidden];
        _dappView.hidden = YES;
        WS(ws);
        _dappView.dappBlock = ^{
            [ws dappClick];
        };
        [self.view addSubview:_dappView];
    }
    return _dappView;
}

- (ZoloNoticeHeadView *)noticeView {
    if (!_noticeView) {
        _noticeView = [[ZoloNoticeHeadView alloc] initWithFrame:CGRectZero];
        _noticeView.hidden = YES;
        WS(ws);
        _noticeView.noticeViewBlock = ^{
            [ws noticeViewBlcok];
        };
        [self.view addSubview:_noticeView];
    }
    return _noticeView;
}

- (void)noticeViewBlcok {
    DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:self.conversation.conversation.target refresh:NO];
    ZoloGroupSeeNoticeVC *vc = [[ZoloGroupSeeNoticeVC alloc] initWithGroupInfo:group];
    [self.navigationController pushViewController:vc animated:YES];
}

- (void)dappClick {
    if (!self.topHeadView.hidden) {
        self.tableView.y = KScreenheight / 2 - 20 - 64;
    } else {
        self.tableView.y = KScreenheight / 2 - 20;
    }
    self.dappWebView.hidden = NO;
}

- (ZoloDappWebView *)dappWebView {
    if (!_dappWebView) {
        _dappWebView = [[ZoloDappWebView alloc] initWithLitappInfo:self.litappInfo];
        _dappWebView.hidden = YES;
        WS(ws);
        _dappWebView.closeBtnBlock = ^{
            [ws dappWebCloseBtnClick];
        };
        _dappWebView.fullBtnBlock = ^{
            [ws dappWebFullBtnClick];
        };
        [self.view addSubview:_dappWebView];
    }
    return _dappWebView;
}

- (void)dappWebCloseBtnClick {
    [self.dappWebView removeFromSuperview];
    self.dappWebView = nil;
    self.tableView.y = 0;
}

- (void)dappWebFullBtnClick {
    ZoloDappViewController *vc = [[ZoloDappViewController alloc] initWithLitappInfo:self.litappInfo];
    MHNavigationVC *nav = [[MHNavigationVC alloc] initWithRootViewController:vc];
    nav.modalPresentationStyle = UIModalPresentationFullScreen;
    [self presentViewController:nav animated:YES completion:nil];
}

- (UIView *)btnView {
    if (!_btnView) {
        _btnView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 44, 44)];
        UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(iconBtnClick)];
        [_btnView addGestureRecognizer:tap];
    }
    return _btnView;
}

- (UILabel *)titleLab {
    if (!_titleLab) {
        _titleLab = [[UILabel alloc] initWithFrame:CGRectMake(-kScreenwidth + 150, 0, kScreenwidth - 154, 44)];
        _titleLab.font =[UIFont systemFontOfSize:20];
        _titleLab.textColor = [FontManage MHBlockColor];
        _titleLab.textAlignment = NSTextAlignmentCenter;
    }
    return _titleLab;
}

- (UIButton *)moreBtn {
    if (!_moreBtn) {
        _moreBtn = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, 44, 44)];
        [_moreBtn setImage:[UIImage imageNamed:@"nav_more"] forState:UIControlStateNormal];
        [_moreBtn addTarget:self action:@selector(moreBtnClick) forControlEvents:UIControlEventTouchUpInside];
    }
    return _moreBtn;
}

- (UIButton *)serveBtn {
    if (!_serveBtn) {
        _serveBtn = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, 44, 44)];
        [_serveBtn setImage:[UIImage imageNamed:@"my_off"] forState:UIControlStateNormal];
        [_serveBtn addTarget:self action:@selector(serveBtnClick) forControlEvents:UIControlEventTouchUpInside];
    }
    return _serveBtn;
}

- (void)serveBtnClick {
    if ([self.conversation.conversation.target containsString:@"OSNS"]) {
        DMCCLitappInfo *appInfo = [[DMCCIMService sharedDMCIMService] getLitapp:self.conversation.conversation.target];
        ZoloDappViewController *vc = [[ZoloDappViewController alloc] initWithLitappInfo:appInfo];
        MHNavigationVC *nav = [[MHNavigationVC alloc] initWithRootViewController:vc];
        nav.modalPresentationStyle = UIModalPresentationFullScreen;
        [self presentViewController:nav animated:YES completion:nil];
    }
}

- (UIImageView *)iconImg {
    if (!_iconImg) {
        _iconImg = [[UIImageView alloc] initWithFrame:CGRectMake(10, 5, 30, 30)];
        _iconImg.contentMode = UIViewContentModeScaleAspectFit;
        _iconImg.layer.masksToBounds = YES;
        _iconImg.layer.cornerRadius = 15;
        [_iconImg.widthAnchor constraintEqualToConstant:30].active = YES;
        [_iconImg.heightAnchor constraintEqualToConstant:30].active = YES;
        [_iconImg sd_setImageWithURL:[NSURL URLWithString:self.userInfo.portrait] placeholderImage:SDImageDefault];
    }
    return _iconImg;
}

- (ZoloChatSendView *)sendView {
    if (!_sendView) {
        _sendView = [[ZoloChatSendView alloc] initWithFrame:CGRectZero];
        _sendView.chatType = self.chatType;
        WS(ws);
        [_sendView muteViewHidden:YES];
        _sendView.returnBlock = ^(NSString * _Nonnull sendStrs) {
            
            NSString *sendStr = [sendStrs stringByReplacingOccurrencesOfString:@"'" withString:@"’"];
            
            if (IsEmptyStr(sendStr)) {
                [MHAlert showMessage:LocalizedString(@"AlertNoSendNullMsg")];
                return;
            }
            if (sendStr.length > 2000) {
                [MHAlert showMessage:LocalizedString(@"AlertSendMoreWordNum")];
                return;
            }
            DMCCTextMessageContent *msg = [DMCCTextMessageContent new];
            msg.text = [ws.wordfilter filter:sendStr];
            if (ws.sendNoticeArray.count > 0) {
                msg.mentionedType = 1;
                msg.mentionedTargets = [ws.sendNoticeArray copy];
            }
            if (ws.replyMsg) {
                DMCCQuoteInfo *quote = [DMCCQuoteInfo new];
                quote.messageUid = ws.replyMsg.messageUid;
                quote.userId = ws.replyMsg.fromUser;
                DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:ws.replyMsg.fromUser refresh:NO];
                quote.userDisplayName = userInfo.displayName;
                quote.messageDigest = ws.replyMsg.digest;
                quote.messageHasho = ws.replyMsg.messageHasho;
                msg.quoteInfo = quote;
            }
            ws.replyView.hidden = YES;
            [ws sendMessage:msg pwd:nil];
        };
        
        _sendView.moreBtnBlock = ^{
            [ws recoverEmjView];
            [ws.view endEditing:YES];
            [ws loadMoreView];
        };
        
        _sendView.emjBlock = ^(BOOL isSelect) {
            [ws recoverMoreView];
            if (isSelect) {
                [ws.view endEditing:YES];
                [ws loadEmjView];
            } else {
                [ws recoverEmjView];
            }
        };
        
        _sendView.voiceBtnSelectBlock = ^(BOOL isSelect) {
            [ws.view endEditing:YES];
            [ws recoverEmjView];
            [ws recoverMoreView];
        };
        
        _sendView.voiceBtnBlock = ^{
            [ws showAudioView];
        };
        
        _sendView.noticeToBlock = ^{
            [ws noticeToClick];
        };
    }
    return _sendView;
}

// @功能
- (void)noticeToClick {
    DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:self.conversation.conversation.target refresh:NO];
    ZoloSettingContactVC *vc = [[ZoloSettingContactVC alloc] initWithGroupInfo:group withType:3];
    WS(ws);
    vc.confirmBlock = ^(NSArray * _Nonnull contactArray) {
        NSMutableArray *upArray = [NSMutableArray arrayWithCapacity:0];
        NSMutableArray *nameArray = [NSMutableArray arrayWithCapacity:0];
        for (DMCCGroupMember *mem in contactArray) {
            [upArray addObject:mem.memberId];
            DMCCUserInfo *user = [[DMCCIMService sharedDMCIMService] getUserInfo:mem.memberId inGroup:mem.groupId refresh:NO];
            [nameArray addObject:user.displayName];
        }
        ws.sendNoticeArray = upArray;
        ws.sendView.textView.text = [NSString stringWithFormat:@"%@%@ ", ws.sendView.getTextFieldContent,[nameArray componentsJoinedByString:@"@"]];
    };
    [self.navigationController pushViewController:vc animated:YES];
}

- (void)longIconNoticeToClick:(NSInteger)index {
    DMCCMessage *msg = self.dataSource[index];
    DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:msg.fromUser refresh:NO];
    [self.upLongArray addObject:userInfo.userId];
    self.sendNoticeArray = self.upLongArray;
    self.sendView.textView.text = [NSString stringWithFormat:@"%@%@ ", self.sendView.getTextFieldContent, [NSString stringWithFormat:@"@%@", userInfo.displayName]];
    [self.sendView.textView becomeFirstResponder];
}

- (NSMutableArray *)upLongArray {
    if (!_upLongArray) {
        _upLongArray = [NSMutableArray arrayWithCapacity:0];
    }
    return _upLongArray;
}

// 弹出语音视图
- (void)showAudioView {
    if ([MHHelperUtils havaAVAuthorizationStatusCode]) {
        self.audioView.hidden = NO;
    }
}

// 控制弹出视图高度
- (void)loadEmjView {
    [self cellHeight:300 duration:0.1];
    self.emoOrImgView.hidden = NO;
}

// 恢复弹出视图高度
- (void)recoverEmjView {
    if (self.isMulSelect) {
        self.tableView.y = 64 + iPhoneX_topH;
    } else {
        self.tableView.y = 0;
    }
    self.sendView.y = KScreenheight - 55 - iPhoneX_topH;
    self.emoOrImgView.hidden = YES;
    [self.sendView emojViewNomal];
    self.replyView.y = KScreenheight - 55 - 65 - iPhoneX_topH;
}

// 控制more视图高度
- (void)loadMoreView {
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    BOOL isShow = [self getWalletHidden];
    if (login.json.HIDE_ENABLE.intValue || isShow) {
        [self cellHeight:300 duration:0.1];
    } else {
        if (_chatType == Single_Type) {
            [self cellHeight:300 duration:0.1];
        } else {
            [self cellHeight:150 duration:0.1];
        }
    }
    self.moreView.hidden = NO;
    self.moreView.chatType = self.chatType;
}

// 恢复more视图高度
- (void)recoverMoreView {
    if (self.isMulSelect) {
        self.tableView.y = 64 + iPhoneX_topH;
    } else {
        self.tableView.y = 0;
    }
    self.sendView.y = KScreenheight - 55 - iPhoneX_topH;
    self.moreView.hidden = YES;
    self.replyView.y = KScreenheight - 55 - 65 - iPhoneX_topH;
}

- (void)keyboardWillAppearance:(NSNotification *)notification {
    NSInteger duration = [notification.userInfo[UIKeyboardAnimationDurationUserInfoKey] integerValue];
    CGRect keyboardFrame = [notification.userInfo[UIKeyboardFrameEndUserInfoKey] CGRectValue];
    CGFloat height = keyboardFrame.size.height;
    if (height == 0) {
        return;
    }
    [self cellHeight:height duration:duration];
}

- (void)cellHeight:(CGFloat)height duration:(NSInteger)duration {
    // 控制键盘弹出时，聊天消息弹出的问题
    NSArray *array = self.tableView.visibleCells;
    long mHeight = 0;
    for (ZoloBaseChatCell *cell in array) {
        if ([cell isKindOfClass:[ZoloInformationCell class]]) {
            mHeight += 80;
        } else {
            mHeight += cell.message.msgCellHeight;
        }
    }
    int tableH = 0;
    if (mHeight > (KScreenheight - height)) {
        tableH = height;
    } else if (mHeight > height) {
        tableH = height;
    } else {
        tableH = 0;
    }
    self.tableView.y = -tableH;
    self.sendView.y = KScreenheight - height -  55 - iPhoneX_topH;
    self.replyView.y = KScreenheight - height -  55 - iPhoneX_topH - 65;
}

- (void)keyboardWillDismiss:(NSNotification *)notification {
    self.tableView.y = 0;
    self.sendView.y = KScreenheight - 55 - iPhoneX_topH;
    self.replyView.y = KScreenheight - 55 - 65 - iPhoneX_topH;
}

- (void)scrollViewWillBeginDragging:(UIScrollView *)scrollView {
    [self.view endEditing:YES];
    [self recoverEmjView];
    [self recoverMoreView];
}

// 更多功能
- (ZoloChatMoreView *)moreView {
    if (!_moreView) {
        _moreView = [[ZoloChatMoreView alloc] initWithFrame:CGRectZero];
        _moreView.hidden = YES;
        _moreView.chatType = self.chatType;
        WS(ws);
        _moreView.moreClickBlock = ^(NSInteger index) {
            [ws moreViewBtnClick:index];
        };
        [self.view addSubview:_moreView];
    }
    return _moreView;
}


// 表情图片
- (ZoloEmoOrImgView *)emoOrImgView {
    if (!_emoOrImgView) {
        _emoOrImgView = [[ZoloEmoOrImgView alloc] initWithFrame:CGRectZero];
        _emoOrImgView.hidden = YES;
        WS(ws);
        _emoOrImgView.emojClickBlock = ^(NSString * _Nonnull emjStr) {
            [ws.sendView setEmojText:emjStr];
        };
        _emoOrImgView.sendEmojClickBlock = ^{
            NSString *sendStr = [ws.sendView getTextFieldContent];
            if (IsEmptyStr(sendStr)) {
                [MHAlert showMessage:LocalizedString(@"AlertNoSendNullMsg")];
                return;
            }
            if (sendStr.length > 2000) {
                [MHAlert showMessage:LocalizedString(@"AlertSendMoreWordNum")];
                return;
            }
            DMCCTextMessageContent *msg = [DMCCTextMessageContent new];
            msg.text = sendStr;
            [ws sendMessage:msg pwd:nil];
        };
        [self.view addSubview:_emoOrImgView];
    }
    return _emoOrImgView;
}

- (ZoloAudioView *)audioView {
    if (!_audioView) {
        _audioView = [[ZoloAudioView alloc] initWithFrame:CGRectZero];
        _audioView.hidden = YES;
        WS(ws);
        _audioView.audioViewBlock = ^{
            [ws removeAudioView];
        };
        _audioView.audioCompletionBlock = ^(NSString * _Nonnull audioFileString) {
            [ws sendAudioMessage:audioFileString];
            [ws removeAudioView];
        };
        [self.view addSubview:_audioView];
    }
    return _audioView;
}

- (void)sendAudioMessage:(NSString *)amrPath {
    NSError *error = nil;
    AVAudioPlayer* avAudioPlayer = [[AVAudioPlayer alloc]initWithContentsOfURL:[NSURL URLWithString:amrPath] error:&error];
    double duration = avAudioPlayer.duration;
    avAudioPlayer = nil;
    if (duration < 1) {
        [MHAlert showMessage:LocalizedString(@"AlertSendVoiceTime")];
        return;
    }
    [self sendMessage:[DMCCSoundMessageContent soundMessageContentForAmr:amrPath duration:duration] pwd:[self getRandStringWithLength:8]];
}

- (void)removeAudioView {
    [self.audioView removeFromSuperview];
    self.audioView = nil;
}

- (HXPhotoManager *)manager {
    if (!_manager) {
        _manager = [[HXPhotoManager alloc] initWithType:HXPhotoManagerSelectedTypePhotoAndVideo];
        _manager.configuration.type = HXConfigurationTypeWXChat;
        _manager.configuration.languageType = HXPhotoLanguageTypeEn;
    }
    return _manager;
}

//产生length个长度随机字符串
- (NSString *)getRandStringWithLength:(int)length {
    NSString *sourceStr = @"abcdefghijklmnopqrstuvwxyz";
    NSMutableString *resultStr = [[NSMutableString alloc] init];
    for (int i = 0; i < length; i++) {
        unsigned index = arc4random() % [sourceStr length];
        NSString *oneStr = [sourceStr substringWithRange:NSMakeRange(index, 1)];
        [resultStr appendString:oneStr];
    }
    return resultStr;
}

#pragma mark - 更多功能实现
- (void)moreViewBtnClick:(NSInteger)index {
    if (index == 0) { // 发图片
        TZImagePickerController *imagePickerVc = [[TZImagePickerController alloc] initWithMaxImagesCount:9 delegate:nil];
        imagePickerVc.modalPresentationStyle = UIModalPresentationFullScreen;
        imagePickerVc.preferredLanguage = [OSNLanguage getLanguage];
        WS(ws);
        [imagePickerVc setDidFinishPickingPhotosHandle:^(NSArray<UIImage *> *photos, NSArray *assets, BOOL isSelectOriginalPhoto) {
            [MHAlert showLoadingDelayStr:LocalizedString(@"AlertNowOpeating")];
            NSString *pwd = [self getRandStringWithLength:8];
            for (UIImage *capturedImage in photos) {
                UInt64 recordTime = [[NSDate date] timeIntervalSince1970]*1000;
                NSString *cacheDir = [[DMCUConfigManager globalManager] cachePathOf:ws.conversation.conversation mediaType:Media_Type_IMAGE];
                NSString *path = [cacheDir stringByAppendingPathComponent:[NSString stringWithFormat:@"img%lld.jpg", recordTime++]];
                DMCCImageMessageContent *imgContent = [DMCCImageMessageContent contentFrom:capturedImage cachePath:path];
                [MHAlert showMessage:LocalizedString(@"AlertSendLoading")];
                [ws sendMessage:imgContent pwd:pwd];
            }
        }];
        [imagePickerVc setDidFinishPickingVideoHandle:^(UIImage *coverImage, PHAsset *asset) {
            [MHAlert showLoadingDelayStr:LocalizedString(@"AlertNowOpeating")];
            [[TZImageManager manager] getVideoOutputPathWithAsset:asset success:^(NSString *outputPath) {
                DMCCVideoMessageContent *videoContent = [DMCCVideoMessageContent contentPath:outputPath thumbnail:coverImage];
                videoContent.duration = asset.duration;
                [MHAlert showMessage:LocalizedString(@"AlertSendLoading")];
                [ws sendMessage:videoContent pwd:[self getRandStringWithLength:8]];
            } failure:^(NSString *errorMessage, NSError *error) {
                
            }];
        }];
        [self presentViewController:imagePickerVc animated:YES completion:nil];
    } else if (index == 1) { // 拍摄
        HXCustomCameraViewController *vc = [HXCustomCameraViewController new];
        vc.manager = self.manager;
        vc.modalPresentationStyle = UIModalPresentationFullScreen;
        vc.delegate = self;
        [self presentViewController:vc animated:YES completion:nil];
    } else if (index == 2) { // 文件
        [self presentViewController:self.documentPickerVC animated:YES completion:nil];
    } else if (index == 3) { // 名片
        if (![[DMCCIMService sharedDMCIMService] isAllowAddFriendGroupMemberName:self.conversation.conversation.target] && self.chatType == Group_Type) {
            [MHAlert showMessage:LocalizedString(@"StopSendCard")];
            return;
        }
        ZoloCardContactVC *vc = nil;
        if (self.chatType == Group_Type) {
            DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:self.conversation.conversation.target refresh:NO];
            vc = [[ZoloCardContactVC alloc] initWithUserInfo:group];
        } else {
            vc = [[ZoloCardContactVC alloc] initWithUserInfo:self.userInfo];
        }
        WS(ws);
        vc.cardReturenBlock = ^(DMCCUserInfo * _Nonnull info) {
            [ws cardReturnBlock:info];
        };
        vc.cardGroupReturnBlock = ^(DMCCGroupInfo * _Nonnull groupInfo) {
            [ws cardReturnBlock:groupInfo];
        };
        vc.cardLitAppReturnBlock = ^(DMCCLitappInfo * _Nonnull litAppInfo) {
            [ws cardReturnBlock:litAppInfo];
        };
        MHNavigationVC *nav = [[MHNavigationVC alloc] initWithRootViewController:vc];
        nav.modalPresentationStyle = UIModalPresentationFullScreen;
        [self presentViewController:nav animated:YES completion:nil];
    } else {
        if (self.chatType == Group_Type) {
            DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:self.conversation.conversation.target refresh:NO];
            //4 手气红包  5 扫雷红包
            if (index == 4) {
                BOOL isShow = [self getWalletHidden];
                if (isShow) {
                    [self selectWallet];
                } else {
                    [self.navigationController pushViewController:[[ZoloSendRedPackVC alloc] initWithRedPackType:index withConversation:self.conversation.conversation] animated:YES];
                }
            } else {
                if (group.memberCount >= 4) {
                    [self.navigationController pushViewController:[[ZoloSendRedPackVC alloc] initWithRedPackType:index withConversation:self.conversation.conversation] animated:YES];
                } else {
                    [MHAlert showMessage:LocalizedString(@"AlertOnRedPack")];
                }
            }
        } else {
            if (index == 6) {
                if ([MHHelperUtils havaAVAuthorizationStatusCode]) {
                    AppDelegate *deleage = (AppDelegate *)[UIApplication sharedApplication].delegate;
                    if (deleage.floatWindow.isShow) {
                        return;
                    }
                    ZoloSingleCallVC *vc = [ZoloSingleCallVC new];
                    vc.conversation = self.conversation.conversation;
                    vc.status = @"launch";
                    vc.isAudioOnly = true;
                    vc.modalPresentationStyle = UIModalPresentationFullScreen;
                    [self presentViewController:vc animated:NO completion:nil];
                }
            } else if (index == 5) {
                if ([MHHelperUtils havaAVAuthorizationStatusCode]) {
                    AppDelegate *deleage = (AppDelegate *)[UIApplication sharedApplication].delegate;
                    if (deleage.floatWindow.isShow) {
                        return;
                    }
                    ZoloSingleCallVC *vc = [ZoloSingleCallVC new];
                    vc.conversation = self.conversation.conversation;
                    vc.status = @"launch";
                    vc.isAudioOnly = false;
                    vc.modalPresentationStyle = UIModalPresentationFullScreen;
                    [self presentViewController:vc animated:NO completion:nil];
                }
            } else {
                //转账
                BOOL isShow = [self getWalletHidden];
                if (isShow) {
                    [self selectWallet];
                } else {
                    [self.navigationController pushViewController:[[ZoloTransferVC alloc] initWithUserInfo:self.userInfo withConversation:self.conversation.conversation] animated:YES];
                }
            }
        }
    }
}

- (BOOL)getWalletHidden {
    NSArray *array = [[DMCCIMService sharedDMCIMService] getWalletInfoList];
    BOOL isShow = array.count > 0;
    return isShow;
}

- (void)selectWallet {
    UIActionSheet *actionSheet = [[UIActionSheet alloc]initWithTitle:LocalizedString(@"PlaseSelect") delegate:self cancelButtonTitle:nil destructiveButtonTitle:nil otherButtonTitles:nil];
    
    NSArray *array = [NSMutableArray arrayWithArray:[[DMCCIMService sharedDMCIMService] getWalletInfoList]];
    for (int i = 0; i < array.count; i++) {
        DMCCWalletInfo *wallet = array[i];
        [actionSheet addButtonWithTitle:wallet.name];
    }
    [actionSheet addButtonWithTitle:LocalizedString(@"Cancel")];
    actionSheet.cancelButtonIndex = actionSheet.numberOfButtons -1;
    actionSheet.actionSheetStyle = UIActionSheetStyleBlackOpaque;
    [actionSheet showInView:self.view];
}

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    NSArray *array = [NSMutableArray arrayWithArray:[[DMCCIMService sharedDMCIMService] getWalletInfoList]];
    if (buttonIndex != (array.count)) {
        DMCCWalletInfo *info = array[buttonIndex];
        NSDictionary *dic = [OsnUtils json2Dics:info.wallect];
        NSString *language = [[NSUserDefaults standardUserDefaults]objectForKey:@"ospn_language"];
        NSString *url = [NSString stringWithFormat:@"%@&loginType=osn&payTo=%@&language=%@", dic[@"transactionUrl"], self.conversation.conversation.target, language];
        DMCCLitappInfo *litInfo = [[DMCCIMService sharedDMCIMService] getLitapp:info.osnID];
        ZoloWalletVC *vc = [[ZoloWalletVC alloc] initWithLitappInfo:litInfo withUrl:url withConversation:self.conversation.conversation];
        [self.navigationController pushViewController:vc animated:YES];
    }
}

// 名片处理
- (void)cardReturnBlock:(id)info {
    ZoloCardView *view = [[ZoloCardView alloc] initWithFrame:CGRectZero];
    
    if (self.chatType == Single_Type) {
        [view.iconImg sd_setImageWithURL:[NSURL URLWithString:self.userInfo.portrait] placeholderImage:SDImageDefault];
        view.nameLab.text = self.userInfo.displayName;
    } else {
        DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:self.conversation.conversation.target refresh:NO];
        [view.iconImg sd_setImageWithURL:[NSURL URLWithString:group.portrait] placeholderImage:SDImageDefault];
        view.nameLab.text = group.name;
    }
    
    if ([info isKindOfClass:[DMCCUserInfo class]]) {
        DMCCUserInfo *user = (DMCCUserInfo *)info;
        view.cardNameLab.text = [NSString stringWithFormat:@"%@%@", LocalizedString(@"CardTypePerson"), user.displayName];
    } else if ([info isKindOfClass:[DMCCGroupInfo class]]) {
        DMCCGroupInfo *group = (DMCCGroupInfo *)info;
        view.cardNameLab.text = [NSString stringWithFormat:@"%@%@", LocalizedString(@"CardTypeGroup") , group.name];
    } else if ([info isKindOfClass:[DMCCLitappInfo class]]) {
        DMCCLitappInfo *litapp = (DMCCLitappInfo *)info;
        view.cardNameLab.text = [NSString stringWithFormat:@"%@%@", LocalizedString(@"CardTypeDapp") , litapp.name];
    }

    view.layer.cornerRadius = 10;
    view.layer.masksToBounds = YES;

    LSTPopView *popView = [LSTPopView initWithCustomView:view
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

    view.cancelBtnClick = ^{
        [wk_popView dismiss];
    };
    WS(ws);
    view.okBtnClick = ^(NSString * _Nonnull str) {
        NSLog(@"====%@", str);
        [wk_popView dismiss];
        // 发送名片
        
        if ([info isKindOfClass:[DMCCUserInfo class]]) {
            DMCCUserInfo *user = (DMCCUserInfo *)info;
            DMCCCardMessageContent *content = [DMCCCardMessageContent cardWithTarget:user.userId type:CardType_User from:self.myInfo.userId];
            [ws sendMessage:content pwd:nil];
        } else if ([info isKindOfClass:[DMCCGroupInfo class]]) {
            DMCCGroupInfo *group = (DMCCGroupInfo *)info;
            DMCCCardMessageContent *content = [DMCCCardMessageContent cardWithTarget:group.target type:CardType_Group from:self.myInfo.userId];
            [ws sendMessage:content pwd:nil];
        } else if ([info isKindOfClass:[DMCCLitappInfo class]]) {
            DMCCLitappInfo *litapp = (DMCCLitappInfo *)info;
            DMCCCardMessageContent *content = [DMCCCardMessageContent cardWithTarget:litapp.target type:CardType_Litapp from:self.myInfo.userId];
            [ws sendMessage:content pwd:nil];
        }
        
        // 发送留言
        if (str.length > 0) {
            DMCCTextMessageContent *msg = [DMCCTextMessageContent new];
            msg.text = str;
            [ws sendMessage:msg pwd:nil];
        }
    };
}

// 文件处理
- (UIDocumentPickerViewController *)documentPickerVC {
    if (!_documentPickerVC) {
        NSArray*types = @[@"com.microsoft.powerpoint.ppt",
                          @"com.microsoft.word.doc",
                          @"org.openxmlformats.wordprocessingml.document",
                          @"org.openxmlformats.presentationml.presentation",
                          @"public.mpeg-4",
                          @"com.adobe.pdf",
                          @"public.mp3",
                          @"public.text",
                          @"public.image",
                          @"public.movie",
                          @"public.content",
                          @"public.text",
                          @"com.pkware.zip-archive"
                        ];
        self.documentPickerVC = [[UIDocumentPickerViewController alloc] initWithDocumentTypes:types inMode:UIDocumentPickerModeOpen];
        _documentPickerVC.delegate = self;
        _documentPickerVC.modalPresentationStyle = UIModalPresentationFormSheet;
    }
    return _documentPickerVC;
}

- (void)documentPicker:(UIDocumentPickerViewController *)controller didPickDocumentsAtURLs:(NSArray<NSURL *> *)urls {
    //获取授权
    BOOL fileUrlAuthozied = [urls.firstObject startAccessingSecurityScopedResource];
    if (fileUrlAuthozied) {
        //通过文件协调工具来得到新的文件地址，以此得到文件保护功能
        NSFileCoordinator *fileCoordinator = [[NSFileCoordinator alloc] init];
        NSError *error;
        WS(ws);
        [fileCoordinator coordinateReadingItemAtURL:urls.firstObject options:0 error:&error byAccessor:^(NSURL *newURL) {
            //读取文件
            NSString *filePath = [newURL absoluteString];
            NSString *fileName = [newURL lastPathComponent];
            DMCCFileMessageContent *fileContent = [DMCCFileMessageContent fileMessageContentFromPath:filePath];
            fileContent.name = fileName;
            [MHAlert showMessage:LocalizedString(@"AlertSendLoading")];
            [ws sendMessage:fileContent pwd:nil];
            [ws dismissViewControllerAnimated:YES completion:NULL];
        }];
        [urls.firstObject stopAccessingSecurityScopedResource];
    } else {
        //授权失败
    }
}

// HXCustomCameraViewController代理方法
- (void)customCameraViewController:(HXCustomCameraViewController *)viewController
                           didDone:(HXPhotoModel *)model {
    if (model.type == HXPhotoModelMediaTypeVideo) {
        WS(ws);
        [model getImageWithSuccess:^(UIImage * _Nullable image, HXPhotoModel * _Nullable model, NSDictionary * _Nullable info) {
            DMCCVideoMessageContent *videoContent = [DMCCVideoMessageContent contentPath:model.videoURL.absoluteString thumbnail:image];
            videoContent.duration = model.videoDuration;
            [MHAlert showMessage:LocalizedString(@"AlertSendLoading")];
            [ws sendMessage:videoContent pwd:[self getRandStringWithLength:8]];
        } failed:^(NSDictionary * _Nullable info, HXPhotoModel * _Nullable model) {
            
        }];
    } else if (model.type == HXPhotoModelMediaTypePhoto) {
        UInt64 recordTime = [[NSDate date] timeIntervalSince1970]*1000;
        NSString *cacheDir = [[DMCUConfigManager globalManager] cachePathOf:self.conversation.conversation mediaType:Media_Type_IMAGE];
        NSString *path = [cacheDir stringByAppendingPathComponent:[NSString stringWithFormat:@"img%lld.jpg", recordTime++]];
        DMCCImageMessageContent *imgContent = [DMCCImageMessageContent contentFrom:model.thumbPhoto cachePath:path];
        [MHAlert showMessage:LocalizedString(@"AlertSendLoading")];
        [self sendMessage:imgContent pwd:[self getRandStringWithLength:8]];
    }
}

#pragma mark - 消息发送
- (void)sendMessage:(DMCCMessageContent *)content pwd:(NSString *)pwd {
    WS(ws);
    
    if (content == nil || self.conversation.conversation == nil) {
        return;
    }
    
    // 判断发送消息速度
    long time = self.timeInterval.intValue * 1000;
    
    if (time != 0) {
        long long currentTime = [MHDateTools timeStampWithDate:[MHDateTools getCurrentTimes]];
        DMCCMessage *msg = [self.dataSource lastObject];
        if (msg) {
            if ((msg.serverTime + time) > currentTime) {
                [MHAlert showMessage:LocalizedString(@"SpeedMessageSend")];
                return;
            }
        }
    }
    
    [[DMCCIMService sharedDMCIMService] send:self.conversation.conversation content:content pwd:pwd success:^(long long messageUid, long long timestamp) {
        dispatch_async(dispatch_get_main_queue(), ^{
            
            if (messageUid == -1) {
                return;
            }
            
            @synchronized (ws.lock) {
                [MHAlert dismiss];
                // 清空引用消息
                if (ws.replyMsg) {
                    ws.replyMsg = nil;
                }
                
                if (ws.sendNoticeArray.count > 0) {
                    ws.sendNoticeArray = nil;
                    ws.upLongArray = nil;
                }
                NSLog(@"===tableView===addObject===4444===");
                [ws.dataSource addObject:[[DMCCIMService sharedDMCIMService] getMessageByUid:messageUid]];
                [ws.tableView reloadData];
                if (ws.dataSource.count == 0) {
                    return;
                }
                NSLog(@"===tableView4===%ld===%@==", self.dataSource.count, [NSThread currentThread]);
                NSIndexPath *index = [NSIndexPath indexPathForRow:ws.dataSource.count-1 inSection:0];
                [ws.tableView scrollToRowAtIndexPath:index atScrollPosition:UITableViewScrollPositionBottom animated:false];
            }
        });
    } error:^(int error_code) {
        [ws reloadMessage];
    }];
}

- (void)insertMessage:(NSNotification *)notification {
    @try {
        WS(ws);
            dispatch_async(dispatch_get_global_queue(0, DISPATCH_QUEUE_PRIORITY_DEFAULT), ^{
                NSLog(@"===tableView==addObject===555===");
                @synchronized (ws.lock) {
                    [self.dataSource addObject:[[DMCCIMService sharedDMCIMService] getMessageByUid:[notification.object longLongValue]]];
                }
                if (self.dataSource.count == 0) {
                    return;
                }
                dispatch_async(dispatch_get_main_queue(), ^{
                    @synchronized (ws.lock) {
                        [self.tableView reloadData];
                        NSLog(@"===tableView5===%ld===%@==", self.dataSource.count, [NSThread currentThread]);
                        [self.tableView scrollToRowAtIndexPath:[NSIndexPath indexPathForRow:self.dataSource.count-1 inSection:0] atScrollPosition:UITableViewScrollPositionBottom animated:false];
                    }
                });
            });
        
    }
    @catch (NSException *exception){
        
    }
}

#pragma mark - tableView 代理
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    NSLog(@"===tableView==%ld===%@=\n", self.dataSource.count, [NSThread currentThread]);
    return self.dataSource.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    DMCCMessage *msg = self.dataSource[indexPath.row];
    ZoloBaseChatCell *cell = nil;
    if ([msg.content isKindOfClass:[DMCCTextMessageContent class]]) {
        if ([msg.fromUser isEqualToString:self.myInfo.userId]) {
            cell = [tableView dequeueReusableCellWithIdentifier:@"ZoloChatTestMsgMeCell"];
        } else {
            cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloChatTestMsgCell class])];
        }
    } else if ([msg.content isKindOfClass:[DMCCImageMessageContent class]] || [msg.content isKindOfClass:[DMCCStickerMessageContent class]]) {
        if ([msg.fromUser isEqualToString:self.myInfo.userId]) {
            cell = [tableView dequeueReusableCellWithIdentifier:@"ZoloChatImageMsgMeCell"];
        } else {
            cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloChatImageMsgCell class])];
        }
    } else if ([msg.content isKindOfClass:[DMCCSoundMessageContent class]]) {
        if ([msg.fromUser isEqualToString:self.myInfo.userId]) {
            cell = [tableView dequeueReusableCellWithIdentifier:@"ZoloChatVoiceMeCell"];
        } else {
            cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloChatVoiceCell class])];
        }
    } else if ([msg.content isKindOfClass:[DMCCCardMessageContent class]]) {
        if ([msg.fromUser isEqualToString:self.myInfo.userId]) {
            cell = [tableView dequeueReusableCellWithIdentifier:@"ZoloChatCardMeCell"];
        } else {
            cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloChatCardCell class])];
        }
    } else if ([msg.content isKindOfClass:[DMCCVideoMessageContent class]]) {
        if ([msg.fromUser isEqualToString:self.myInfo.userId]) {
            cell = [tableView dequeueReusableCellWithIdentifier:@"ZoloChatVedioMeCell"];
        } else {
            cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloChatVedioCell class])];
        }
    } else if ([msg.content isKindOfClass:[DMCCRedPacketMessageContent class]]) {
        if ([msg.fromUser isEqualToString:self.myInfo.userId]) {
            cell = [tableView dequeueReusableCellWithIdentifier:@"ZoloRedPacketMeCell"];
        } else {
            cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloRedPacketCell class])];
        }
    } else if ([msg.content isKindOfClass:[DMCCFileMessageContent class]]) {
        if ([msg.fromUser isEqualToString:self.myInfo.userId]) {
            cell = [tableView dequeueReusableCellWithIdentifier:@"ZoloChatFileMeCell"];
        } else {
            cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloChatFileCell class])];
        }
    } else if ([msg.content isKindOfClass:[DMCCNotificationMessageContent class]]) {
        cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloInformationCell class])];
    } else if ([msg.content isKindOfClass:[DMCCCallMessageContent class]]) {
        if ([msg.fromUser isEqualToString:self.myInfo.userId]) {
            cell = [tableView dequeueReusableCellWithIdentifier:@"ZoloChatCallMeCell"];
        } else {
            cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloChatCallCell class])];
        }
    } else {
        if ([msg.fromUser isEqualToString:self.myInfo.userId]) {
            cell = [tableView dequeueReusableCellWithIdentifier:@"ZoloChatTestMsgMeCell"];
        } else {
            cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloChatTestMsgCell class])];
        }
    }
    cell.isMulSelect = self.isMulSelect;
    [cell setChatType:self.chatType myInfo:self.myInfo userInfo:self.userInfo withConversation:self.conversation.conversation];
    NSLog(@"==test== index:%zd",indexPath.row);
    cell.message = msg;
    WS(ws);
    cell.cellLongBlock = ^{
        if (ws.conversation.conversation.type == Service_Type) {
            // 服务消息不处理
        } else {
            [ws cellLongBtnClick:indexPath.row];
        }
    };
    cell.cellTapBlock = ^{
        [ws cellTapBtnClick:indexPath];
    };
    cell.cellIconTapBlock = ^{
        [ws cellIconTapBtnClick:indexPath];
    };
    cell.cellResendTapBlock = ^{
        [ws cellResendTapBtnClick:indexPath];
    };
    cell.cellLongIconBlock = ^{
        [ws longIconNoticeToClick:indexPath.row];
    };
    cell.cellTapUrlBlock = ^(NSString * _Nonnull url) {
        
        NSURL *URL = [NSURL URLWithString:url];
        [[UIApplication sharedApplication] openURL:URL options:@{} completionHandler:nil];
        
//        [ws.navigationController pushViewController:[[ZoloWalletViewController alloc] initWithUrlInfo:url isHidden:NO] animated:YES];
    };
    cell.cellTapPhoneBlock = ^(NSString * _Nonnull phone) {
        NSURL *url = [NSURL URLWithString:[NSString stringWithFormat:@"tel://%@", phone]];
        [[UIApplication sharedApplication] openURL:url options:@{} completionHandler:nil];
    };
    cell.cellMulSelectBlock = ^{
        msg.isMulSelect = !msg.isMulSelect;
        NSInteger index= [ws messageAtIndex:[NSString stringWithFormat:@"%llu",msg.messageUid]];
        if (index != -1) {
            [ws.dataSource replaceObjectAtIndex:index withObject:msg];
            NSIndexPath *indexPath=[NSIndexPath indexPathForRow:index inSection:0];
            [ws.tableView reloadRowsAtIndexPaths:[NSArray arrayWithObjects:indexPath,nil] withRowAnimation:UITableViewRowAnimationNone];
        }
    };
    cell.cellQuoteTapBlock = ^{
        [ws cellQuetoTapClick:indexPath.row];
    };
    return cell;
}

- (void)cellQuetoTapClick:(NSInteger)itemIndex {
    DMCCMessage *message = self.dataSource[itemIndex];
    DMCCTextMessageContent *text = (DMCCTextMessageContent *)message.content;
    DMCCMessage *msg = [[DMCCIMService sharedDMCIMService] getMessageByHashO:text.quoteInfo.messageHasho];
    if (msg == nil) {
        [MHAlert showMessage:LocalizedString(@"EmptyPageContent")];
    } else {
        [self queteTapBtnClick:msg];
    }
}

- (void)queteTapBtnClick:(DMCCMessage *)msg {
    if ([msg.content isKindOfClass:[DMCCTextMessageContent class]]) {
        
        DMCCQuoteInfo *info = [DMCCQuoteInfo new];
        info.messageDigest = msg.digest;
        ZoloQuoteMsgVC *vc = [[ZoloQuoteMsgVC alloc] initWithQuoteInfo:info];
        [self.navigationController pushViewController:vc animated:YES];
        
    } else if ([msg.content isKindOfClass:[DMCCVideoMessageContent class]] || [msg.content isKindOfClass:[DMCCImageMessageContent class]]) {
        
        [self seeImageOrVideo2:msg];
        
    } else if ([msg.content isKindOfClass:[DMCCCardMessageContent class]]) {
        
        DMCCCardMessageContent *cardContent = (DMCCCardMessageContent *)msg.content;
        if (cardContent.type == CardType_User) {
            
            DMCCUserInfo *userInfo = [DMCCUserInfo new];
            userInfo.userId = cardContent.targetId;
            if ([[DMCCIMService sharedDMCIMService] isMyFriend:userInfo.userId]) {
                [self.navigationController pushViewController:[[ZoloContactDetailVC alloc] initWithUserInfo:userInfo] animated:YES];
            } else {
                ZoloAddFriendInfoVC *vc = [[ZoloAddFriendInfoVC alloc] initWithUserInfo:userInfo];
                [self.navigationController pushViewController:vc animated:YES];
            }
        } else if (cardContent.type == CardType_Litapp) {
            DMCCLitappInfo *litapp = [DMCCLitappInfo new];
            litapp.name = cardContent.name;
            litapp.target = cardContent.targetId;
            litapp.url = cardContent.url;
            litapp.portrait = cardContent.portrait;
            litapp.info = cardContent.info;
            
            ZoloDappViewController *vc = [[ZoloDappViewController alloc] initWithLitappInfo:litapp];
            MHNavigationVC *nav = [[MHNavigationVC alloc] initWithRootViewController:vc];
            nav.modalPresentationStyle = UIModalPresentationFullScreen;
            [self presentViewController:nav animated:YES completion:nil];
            
        } else if (cardContent.type == CardType_Group) {
            
            DMCCCardMessageContent *cardContent = (DMCCCardMessageContent *)msg.content;
//             判断群组是否已经存在
            DMCCConversationInfo* convInfo = [self getConversation:cardContent.targetId];
            if (convInfo != nil) {
                DMCCConversationInfo *info = [DMCCConversationInfo new];
                DMCCConversation *conversation = [DMCCConversation conversationWithType:Group_Type target:cardContent.targetId line:0];
                info.conversation = conversation;
                [self.navigationController pushViewController:[[ZoloSingleChatVC alloc] initWithConversationInfo:info] animated:YES];
                return;
            }

            DMCCGroupInfo *groupInfo = [DMCCGroupInfo new];
            groupInfo.name = cardContent.targetId;
            groupInfo.target = cardContent.targetId;
            groupInfo.portrait = @"";
            ZoloAddGroupInfoVC *vc = [[ZoloAddGroupInfoVC alloc] initWithGroupInfo:groupInfo];
            [self.navigationController pushViewController:vc animated:YES];
            
        }
    } else if ([msg.content isKindOfClass:[DMCCSoundMessageContent class]]) {
        
        [self playSoundWithMsg:msg withIndex:0];
        
    } else if ([msg.content isKindOfClass:[DMCCRedPacketMessageContent class]]) {
        DMCCRedPacketMessageContent *content = (DMCCRedPacketMessageContent *)msg.content;
        DMCCRedPacketInfo *info = [[DMCCIMService sharedDMCIMService] getRedPacket:content.ids];
        if ([info.type isEqualToString:@"normal"]) { // 转账
            ZoloTransferRecordVC *vc = [[ZoloTransferRecordVC alloc] initWithRedPacketInfo:info];
            WS(ws);
            vc.receiveBlock = ^{
                [ws updateMessage:info withMsg:msg];
            };
            [self.navigationController pushViewController:vc animated:YES];
        } else if ([info.type isEqualToString:@"loot"]) { // 手气
            if(info.state == 0) {
                [self openRedPackWithDic:info withMsg:msg];
            } else {
                // 红包获取记录
                [self redpackRecordList:info];
            }
        } else if ([info.type isEqualToString:@"bomb"]) { // 扫雷
            if(info.state == 0) {
                [self openRedPackWithDic:info withMsg:msg];
            } else {
                // 红包获取记录
                [self redpackRecordList:info];
            }
        }
    } else if ([msg.content isKindOfClass:[DMCCFileMessageContent class]]) {
        
        [self downFileWithMsg:msg];
        
    }
}

#pragma mark - 转发  删除
- (void)cellLongBtnClick:(NSInteger)itemIndex {
    [self.view endEditing:YES];
    DMCCMessage *msg = self.dataSource[itemIndex];
    if ([msg.content isKindOfClass:[DMCCNotificationMessageContent class]]) {
        return;
    }

    NSString *topMSg = LocalizedString(@"ChatTop");
    if ([[DMCCIMService sharedDMCIMService] checkGroupTopMessageWithGroupId:self.conversation.conversation.target withHashO:msg.messageHasho]) {
        topMSg = LocalizedString(@"MsgCancelTop");
    }

    ZoloForwardView *forwardView = [[ZoloForwardView alloc] initWithFrame:CGRectZero];
    [forwardView setMyInfo:self.myInfo withMessage:msg type:self.chatType isOwner:[self isGroupOwner] || [self isGroupManger]];
    if (self.chatType == Single_Type) {
        if ([msg.content isKindOfClass:[DMCCTextMessageContent class]]) {
            forwardView.titleArray = @[LocalizedString(@"MsgTransfor"), LocalizedString(@"MsgDel"), LocalizedString(@"MsgRecall"), LocalizedString(@"MsgDelToMsg"), LocalizedString(@"MsgCopy"), LocalizedString(@"ChatReply"), LocalizedString(@"MsgSelect")];
            forwardView.imgArray = @[@"chat_transfer", @"chat_del", @"chat_recal", @"chat_delhim", @"chat_copy", @"chat_reply", @"chat_select"];
        } else {
            if([msg.content isKindOfClass:[DMCCRedPacketMessageContent class]] || [msg.content isKindOfClass:[DMCCCallMessageContent class]]){
                forwardView.titleArray = @[LocalizedString(@"MsgDel")];
                forwardView.imgArray = @[@"chat_del"];
            }else{
                forwardView.titleArray = @[LocalizedString(@"MsgTransfor"), LocalizedString(@"MsgDel"), LocalizedString(@"MsgRecall"), LocalizedString(@"MsgDelToMsg"),LocalizedString(@"ChatReply"), LocalizedString(@"MsgSelect")];
                forwardView.imgArray = @[@"chat_transfer", @"chat_del", @"chat_recal", @"chat_delhim",@"chat_reply", @"chat_select"];
            }
        }
    } else {
        if ([msg.content isKindOfClass:[DMCCTextMessageContent class]]) {
            forwardView.titleArray = @[LocalizedString(@"MsgTransfor"), LocalizedString(@"MsgDel"), LocalizedString(@"MsgRecall"), LocalizedString(@"MsgCopy"),LocalizedString(@"ChatReply"),topMSg, LocalizedString(@"MsgSelect")];
            forwardView.imgArray = @[@"chat_transfer", @"chat_del", @"chat_recal", @"chat_copy",@"chat_reply",@"chat_top", @"chat_select"];
        } else {
            if([msg.content isKindOfClass:[DMCCRedPacketMessageContent class]] || [msg.content isKindOfClass:[DMCCCallMessageContent class]]){
                forwardView.titleArray = @[LocalizedString(@"MsgDel")];
                forwardView.imgArray = @[@"chat_del"];
            }else{
                forwardView.titleArray = @[LocalizedString(@"MsgTransfor"), LocalizedString(@"MsgDel"), LocalizedString(@"MsgRecall"),LocalizedString(@"ChatReply"), LocalizedString(@"MsgSelect")];
                forwardView.imgArray = @[@"chat_transfer", @"chat_del", @"chat_recal",@"chat_reply", @"chat_select"];
            }
        }
    }
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
            DMCCMessage *msg = self.dataSource[itemIndex];
            if([msg.content isKindOfClass:[DMCCRedPacketMessageContent class]] || [msg.content isKindOfClass:[DMCCCallMessageContent class]]){
                [[DMCCIMService sharedDMCIMService] deleteMessage:msg.messageId];
                [self.dataSource removeObject:msg];
                [self.tableView reloadData];
                return;
            }
            
            if (self.chatType == Group_Type) {
                if ([[DMCCIMService sharedDMCIMService] isForwardGroupMemberName:self.conversation.conversation.target]) {
                    DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:self.conversation.conversation.target refresh:NO];
                    ZoloCardVC *vc = [[ZoloCardVC alloc] initWithGroupInfo:group];
                    vc.cardReturenBlock = ^(id  _Nonnull info) {
                        [ws forwardWithUserOrGroup:info withItem:itemIndex];
                    };
                    MHNavigationVC *nav = [[MHNavigationVC alloc] initWithRootViewController:vc];
                    nav.modalPresentationStyle = UIModalPresentationFullScreen;
                    [self presentViewController:nav animated:YES completion:nil];
                } else {
                    [MHAlert showMessage:LocalizedString(@"AlertNoOurthSee")];
                }
            } else {
                ZoloCardVC *vc = [[ZoloCardVC alloc] initWithUserInfo:self.userInfo];
                vc.cardReturenBlock = ^(id  _Nonnull info) {
                    [ws forwardWithUserOrGroup:info withItem:itemIndex];
                };
                MHNavigationVC *nav = [[MHNavigationVC alloc] initWithRootViewController:vc];
                nav.modalPresentationStyle = UIModalPresentationFullScreen;
                [self presentViewController:nav animated:YES completion:nil];
            }
        }
            break;
        case 1:
        {
            DMCCMessage *msg = self.dataSource[itemIndex];
            [[DMCCIMService sharedDMCIMService] deleteMessage:msg.messageId];
            [self.dataSource removeObject:msg];
            [self.tableView reloadData];
        }
            break;
        case 2:
        {
            // 撤销
            if (self.chatType == Group_Type) {
                DMCCMessage *msg = self.dataSource[itemIndex];
                DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:self.conversation.conversation.target refresh:NO];
                [[DMCCIMService sharedDMCIMService] recall:msg toUsers:group.target success:^{

                    DMCCMessage* message = [DMCCMessage new];
                    message.fromUser = self.myInfo.userId;
                    message.conversation = [DMCCConversation  conversationWithType:Single_Type target:self.userInfo.userId line:0];
                    message.direction = MessageDirection_Receive;
                    message.status = Message_Status_Readed;
                    message.serverTime = [OsnUtils getTimeStamp];

                    DMCCRecallMessageContent* textMessageContent = [DMCCRecallMessageContent new];
                    message.content = textMessageContent;
                    textMessageContent.operatorId = self.myInfo.userId;
                    [[DMCCIMService sharedDMCIMService] insertMessage:message];
                    [[DMCCIMService sharedDMCIMService] deleteMessage:msg.messageId];
                    [ws reloadMessage];
                } error:^(int error_code) {

                }];
            } else {
                DMCCMessage *msg = self.dataSource[itemIndex];
                [[DMCCIMService sharedDMCIMService] recall:msg toUsers:self.userInfo.userId success:^{

                    DMCCMessage* message = [DMCCMessage new];
                    message.fromUser = self.myInfo.userId;
                    message.conversation = [DMCCConversation  conversationWithType:Single_Type target:self.userInfo.userId line:0];
                    message.direction = MessageDirection_Receive;
                    message.status = Message_Status_Readed;
                    message.serverTime = [OsnUtils getTimeStamp];

                    DMCCRecallMessageContent* textMessageContent = [DMCCRecallMessageContent new];
                    message.content = textMessageContent;
                    textMessageContent.operatorId = self.myInfo.userId;
                    [[DMCCIMService sharedDMCIMService] insertMessage:message];

                    [[DMCCIMService sharedDMCIMService] deleteMessage:msg.messageId];
                    [ws reloadMessage];

                } error:^(int error_code) {

                }];
            }
        }
            break;
        case 3:
        {
            if (self.chatType == Group_Type) {
                DMCCMessage *msg = self.dataSource[itemIndex];
                if ([msg.content isKindOfClass:[DMCCTextMessageContent class]]) {
                    // 复制消息
                    if ([[DMCCIMService sharedDMCIMService] isCopyGroupMemberName:self.conversation.conversation.target]) {
                        [[UIPasteboard generalPasteboard] setString:msg.digest];
                        [MHAlert showMessage:LocalizedString(@"AlertCopySucess")];
                    } else {
                        [MHAlert showMessage:LocalizedString(@"AlertNoOurthSee")];
                    }
                } else {
                    self.replyMsg = msg;
                    [self showReplyView];
                }
            } else {
                DMCCMessage *msg = self.dataSource[itemIndex];
                [[DMCCIMService sharedDMCIMService] DeleteToFromMessage:msg toUsers:self.userInfo.userId success:^{
                    [[DMCCIMService sharedDMCIMService] deleteMessage:msg.messageId];
                    [ws.dataSource removeObject:msg];
                    [ws.tableView reloadData];
                } error:^(int error_code) {

                }];
            }
        }
            break;
        case 4:
        {
            if (self.chatType == Group_Type) {
                DMCCMessage *msg = self.dataSource[itemIndex];
                if ([msg.content isKindOfClass:[DMCCTextMessageContent class]]) {
                    self.replyMsg = msg;
                    [self showReplyView];
                } else {
                    // 多选
                    NSLog(@"=====多选===");
                    [self showMulView];
                }

            } else {
                DMCCMessage *msg = self.dataSource[itemIndex];
                if ([msg.content isKindOfClass:[DMCCTextMessageContent class]]) {
                    if (self.chatType == Group_Type) {
                        if ([[DMCCIMService sharedDMCIMService] isCopyGroupMemberName:self.conversation.conversation.target]) {
                            [[UIPasteboard generalPasteboard] setString:msg.digest];
                            [MHAlert showMessage:LocalizedString(@"AlertCopySucess")];
                        } else {
                            [MHAlert showMessage:LocalizedString(@"AlertNoOurthSee")];
                        }
                    } else {
                        [[UIPasteboard generalPasteboard] setString:msg.digest];
                        [MHAlert showMessage:LocalizedString(@"AlertCopySucess")];
                    }
                } else {
                    self.replyMsg = msg;
                    [self showReplyView];
                }
            }
        }
            break;
        case 5:
        {
            // 置顶
            if (self.chatType == Group_Type) {
                DMCCMessage *msg = self.dataSource[itemIndex];
                if ([[DMCCIMService sharedDMCIMService] checkGroupTopMessageWithGroupId:self.conversation.conversation.target withHashO:msg.messageHasho]) {
                    [MHAlert showLoadingStr:LocalizedString(@"AlertOpreating")];
                    WS(ws);
                    NSString *key = [NSString stringWithFormat:@"top_%lld", msg.serverTime];
                    [[DMCCIMService sharedDMCIMService] removeTopChatsWithGroupId:self.conversation.conversation.target keyData:key cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
                        [MHAlert dismiss];
                        if (isSuccess) {
                            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.5 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                                [ws getTopMessage];
                            });
                        }
                    }];
                } else {
                    NSMutableDictionary* json = [NSMutableDictionary new];

                    if ([msg.content isMemberOfClass:[DMCCTextMessageContent class]]) {
                        NSString *key = [NSString stringWithFormat:@"top_%lld", msg.serverTime];

                        DMCCTextMessageContent* textMessageContent = (DMCCTextMessageContent*)msg.content;
                        json[@"type"] = @"text";
                        json[@"data"] = textMessageContent.text;
                        json[@"hasho"] = msg.messageHasho;
                        json[@"fromUser"] = msg.fromUser;
                        json[@"key"] = key;

                        [[DMCCIMService sharedDMCIMService] topMessageWithGroupId:self.conversation.conversation.target key:key data:[OsnUtils dic2Json:json] cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
                            if (isSuccess) {
                                dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.5 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                                    [ws getTopMessage];
                                });
                            }
                        }];
                    }
                }
            } else {
                DMCCMessage *msg = self.dataSource[itemIndex];
                if ([msg.content isKindOfClass:[DMCCTextMessageContent class]]) {
                    self.replyMsg = msg;
                    [self showReplyView];
                } else {
                    // 多选
                    NSLog(@"=====多选===");
                    [self showMulView];
                }
            }
        }
            break;
        case 6: {
            // 多选
            NSLog(@"=====多选===");
            [self showMulView];
        }
        default:
            break;
    }
}

- (void)showMulView {
    self.tableView.y = 64 + iPhoneX_topH;
    self.isMulSelect = YES;
    self.mulTopView.hidden = NO;
    self.mulBottomView.hidden = NO;
    [self mulBottomView];
    [self mulTopView];
    self.navigationController.navigationBar.hidden = YES;
    [self.tableView reloadData];
}

- (ZoloMulBottomView *)mulBottomView {
    if (!_mulBottomView) {
        _mulBottomView = [[ZoloMulBottomView alloc] initWithFrame:CGRectZero];
        WS(ws);
        _mulBottomView.mulForwardBlock = ^{
            if (ws.chatType == Group_Type) {
                if ([[DMCCIMService sharedDMCIMService] isForwardGroupMemberName:ws.conversation.conversation.target]) {
                    DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:ws.conversation.conversation.target refresh:NO];
                    ZoloCardMulVC *vc = [[ZoloCardMulVC alloc] initWithGroupInfo:group];
                    vc.cardReturenBlock = ^(id  _Nonnull info) {
                        [ws forwardMulWithUserOrGroup:info];
                    };
                    MHNavigationVC *nav = [[MHNavigationVC alloc] initWithRootViewController:vc];
                    nav.modalPresentationStyle = UIModalPresentationFullScreen;
                    [ws presentViewController:nav animated:YES completion:nil];
                } else {
                    [MHAlert showMessage:LocalizedString(@"AlertNoOurthSee")];
                }
            } else {
                ZoloCardMulVC *vc = [[ZoloCardMulVC alloc] initWithUserInfo:ws.userInfo];
                vc.cardReturenBlock = ^(id  _Nonnull info) {
                    [ws forwardMulWithUserOrGroup:info];
                };
                MHNavigationVC *nav = [[MHNavigationVC alloc] initWithRootViewController:vc];
                nav.modalPresentationStyle = UIModalPresentationFullScreen;
                [ws presentViewController:nav animated:YES completion:nil];
            }
            
        };
        _mulBottomView.mulDelBlock = ^{
            NSString *str = LocalizedString(@"AlertSureDel");
            [MHAlert showCustomeAlert:str withAlerttype:MHAlertTypeWarn withOkBlock:^(UIAlertAction *action) {
                if ([action.title isEqualToString:LocalizedString(@"Sure")]) {
                    [ws mulDelClick];
                    [ws mulTopViewCancel];
                }
            }];
        };
        [self.view addSubview:_mulBottomView];
    }
    return _mulBottomView;
}

- (void)mulDelClick {
    NSArray *dataSourceCopy = [self.dataSource mutableCopy];
    for (DMCCMessage *msg in dataSourceCopy) {
        if (msg.isMulSelect) {
            [[DMCCIMService sharedDMCIMService] deleteMessage:msg.messageId];
            [self.dataSource removeObject:msg];
        }
    }
    [self.tableView reloadData];
}

- (void)forwardMulWithUserOrGroup:(id)info {
    
    DMCCConversation *conversation = [[DMCCConversation alloc] init];
    
    ZoloCardView *view = [[ZoloCardView alloc] initWithFrame:CGRectZero];
    
    if ([info isKindOfClass:[DMCCUserInfo class]]) {
        DMCCUserInfo *user = info;
        conversation.type = Single_Type;
        conversation.target = user.userId;
        conversation.line = 0;
        
        [view.iconImg sd_setImageWithURL:[NSURL URLWithString:user.portrait] placeholderImage:SDImageDefault];
        view.nameLab.text = user.displayName;
        view.cardNameLab.text = [NSString stringWithFormat:@"%@", user.displayName];
    } else {
        DMCCGroupInfo *group = info;
        conversation.type = Group_Type;
        conversation.target = group.target;
        conversation.line = 0;
        
        [view.iconImg sd_setImageWithURL:[NSURL URLWithString:group.portrait] placeholderImage:SDImageDefault];
        view.nameLab.text = group.name;
        view.cardNameLab.text = [NSString stringWithFormat:@"%@", group.name];
    }
    
    view.layer.cornerRadius = 10;
    view.layer.masksToBounds = YES;

    LSTPopView *popView = [LSTPopView initWithCustomView:view
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

    view.cancelBtnClick = ^{
        [wk_popView dismiss];
    };
    WS(ws);
    view.okBtnClick = ^(NSString * _Nonnull str) {
        NSLog(@"====%@", str);
        [wk_popView dismiss];
        for (DMCCMessage *msg in self.dataSource) {
            if (msg.isMulSelect) {
                if ([msg.content isKindOfClass:[DMCCRedPacketMessageContent class]]) {
                    DMCCRedPacketMessageContent *content = (DMCCRedPacketMessageContent *)msg.content;
                    DMCCRedPacketInfo *info = [[DMCCIMService sharedDMCIMService] getRedPacket:content.ids];
                    if ([info.type isEqualToString:@"normal"]) {
                        DMCCTextMessageContent *msg = [DMCCTextMessageContent new];
                        msg.text = LocalizedString(@"ChatTransfer");
                        [ws sendForwardMessage:msg withConversation:conversation];
                    } else {
                        DMCCTextMessageContent *msg = [DMCCTextMessageContent new];
                        msg.text = LocalizedString(@"red_packet_digest");
                        [ws sendForwardMessage:msg withConversation:conversation];
                    }
                } else if ([msg.content isKindOfClass:[DMCCSoundMessageContent class]]) {
                    DMCCTextMessageContent *msg = [DMCCTextMessageContent new];
                    msg.text = LocalizedString(@"audio_digest");
                    [ws sendForwardMessage:msg withConversation:conversation];
                } else if ([msg.content isKindOfClass:[DMCCCallMessageContent class]]) {
                    DMCCTextMessageContent *msg = [DMCCTextMessageContent new];
                    msg.text = LocalizedString(@"CallTransferTitle");
                    [ws sendForwardMessage:msg withConversation:conversation];
                } else if ([msg.content isKindOfClass:[DMCCTextMessageContent class]]) {
                    DMCCTextMessageContent *newMsg = [DMCCTextMessageContent new];
                    DMCCTextMessageContent *content = (DMCCTextMessageContent *)msg.content;
                    newMsg.text = content.text;
                    [ws sendForwardMessage:newMsg withConversation:conversation];
                } else {
                    [ws sendForwardMessage:msg.content withConversation:conversation];
                }
            }
        }
       
        // 发送留言
        if (str.length > 0) {
            DMCCTextMessageContent *msg = [DMCCTextMessageContent new];
            msg.text = str;
            [ws sendForwardMessage:msg withConversation:conversation];
        }
        
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(2 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            if ([ws.conversation.conversation.target isEqualToString:conversation.target]) {
                [ws reloadMessage];
            }
        });
    };
}

- (ZoloMulTopView *)mulTopView {
    if (!_mulTopView) {
        _mulTopView = [[ZoloMulTopView alloc] initWithFrame:CGRectZero];
        WS(ws);
        _mulTopView.mulCancelBlock = ^{
            [ws mulTopViewCancel];
        };
        [self.view addSubview:_mulTopView];
    }
    return _mulTopView;
}

- (void)mulTopViewCancel {
    self.tableView.y = 0;
    self.navigationController.navigationBar.hidden = NO;
    self.mulTopView.hidden = YES;
    self.mulBottomView.hidden = YES;
    self.isMulSelect = NO;
    [self.tableView reloadData];
}

- (void)showReplyView {
    self.replyView.hidden = NO;
    DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:self.replyMsg.fromUser refresh:NO];
    self.replyView.nameLab.text = userInfo.displayName;
    self.replyView.contentLab.text = self.replyMsg.digest;
}

// 获取群公告
- (void)getGroupNotice {
    if (self.chatType != Group_Type) {
        return;
    }
    dispatch_async(dispatch_get_global_queue(0, DISPATCH_QUEUE_PRIORITY_DEFAULT), ^{
        DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:self.conversation.conversation.target refresh:NO];
        dispatch_async(dispatch_get_main_queue(), ^{
            if (group.notice.length > 0) {
                self.noticeView.noticeLab.text = [NSString stringWithFormat:@"%@：%@", LocalizedString(@"ContactGroupNotice"), group.notice];
                self.noticeView.hidden = NO;
                self.tableView.contentInset = UIEdgeInsetsMake(22, 0, 55 + iPhoneX_bottomH, 0);
            } else {
                self.noticeView.hidden = YES;
                self.tableView.contentInset = UIEdgeInsetsMake(0, 0, 55 + iPhoneX_bottomH, 0);
            }
        });
    });
    [self getTopMessage];
}

- (void)forwardWithUserOrGroup:(id)info withItem:(NSInteger)item {
    NSLog(@"-----转发--%ld-", item);
    
    DMCCConversation *conversation = [[DMCCConversation alloc] init];
    
    ZoloCardView *view = [[ZoloCardView alloc] initWithFrame:CGRectZero];
    
    if ([info isKindOfClass:[DMCCUserInfo class]]) {
        DMCCUserInfo *user = info;
        conversation.type = Single_Type;
        conversation.target = user.userId;
        conversation.line = 0;
        
        [view.iconImg sd_setImageWithURL:[NSURL URLWithString:user.portrait] placeholderImage:SDImageDefault];
        view.nameLab.text = user.displayName;
        view.cardNameLab.text = [NSString stringWithFormat:@"%@", user.displayName];
    } else {
        DMCCGroupInfo *group = info;
        conversation.type = Group_Type;
        conversation.target = group.target;
        conversation.line = 0;
        
        [view.iconImg sd_setImageWithURL:[NSURL URLWithString:group.portrait] placeholderImage:SDImageDefault];
        view.nameLab.text = group.name;
        view.cardNameLab.text = [NSString stringWithFormat:@"%@", group.name];
    }
    
    view.layer.cornerRadius = 10;
    view.layer.masksToBounds = YES;

    LSTPopView *popView = [LSTPopView initWithCustomView:view
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

    view.cancelBtnClick = ^{
        [wk_popView dismiss];
    };
    WS(ws);
    view.okBtnClick = ^(NSString * _Nonnull str) {
        NSLog(@"====%@", str);
        [wk_popView dismiss];
        
        DMCCMessage *msg = self.dataSource[item];

        if ([msg.content isKindOfClass:[DMCCRedPacketMessageContent class]]) {
            DMCCRedPacketMessageContent *content = (DMCCRedPacketMessageContent *)msg.content;
            DMCCRedPacketInfo *info = [[DMCCIMService sharedDMCIMService] getRedPacket:content.ids];
            if ([info.type isEqualToString:@"normal"]) {
                DMCCTextMessageContent *msg = [DMCCTextMessageContent new];
                msg.text = LocalizedString(@"ChatTransfer");
                [ws sendForwardMessage:msg withConversation:conversation];
            } else {
                DMCCTextMessageContent *msg = [DMCCTextMessageContent new];
                msg.text = LocalizedString(@"red_packet_digest");
                [ws sendForwardMessage:msg withConversation:conversation];
            }
        } else if ([msg.content isKindOfClass:[DMCCSoundMessageContent class]]) {
            DMCCTextMessageContent *msg = [DMCCTextMessageContent new];
            msg.text = LocalizedString(@"audio_digest");
            [ws sendForwardMessage:msg withConversation:conversation];
        } else if ([msg.content isKindOfClass:[DMCCCallMessageContent class]]) {
            DMCCTextMessageContent *msg = [DMCCTextMessageContent new];
            msg.text = LocalizedString(@"CallTransferTitle");
            [ws sendForwardMessage:msg withConversation:conversation];
        } else if ([msg.content isKindOfClass:[DMCCTextMessageContent class]]) {
            DMCCTextMessageContent *newMsg = [DMCCTextMessageContent new];
            DMCCTextMessageContent *content = (DMCCTextMessageContent *)msg.content;
            newMsg.text = content.text;
            [ws sendForwardMessage:newMsg withConversation:conversation];
        } else {
            [ws sendForwardMessage:msg.content withConversation:conversation];
        }
            
        // 发送留言
        if (str.length > 0) {
            DMCCTextMessageContent *msg = [DMCCTextMessageContent new];
            msg.text = str;
            [ws sendForwardMessage:msg withConversation:conversation];
        }
        
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(2 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            if ([ws.conversation.conversation.target isEqualToString:conversation.target]) {
                [ws reloadMessage];
            }
        });
    };
}

- (void)sendForwardMessage:(DMCCMessageContent *)content withConversation:(DMCCConversation *)conversation {
    
    NSString *pwd = nil;
    if ([content isKindOfClass:[DMCCSoundMessageContent class]] || [content isKindOfClass:[DMCCImageMessageContent class]] || [content isKindOfClass:[DMCCVideoMessageContent class]]) {
        pwd = [self getRandStringWithLength:8];
    }
    
    [[DMCCIMService sharedDMCIMService] send:conversation content:content pwd:pwd success:^(long long messageUid, long long timestamp) {
        [MHAlert showMessage:LocalizedString(@"AlertSendSucess")];
    } error:^(int error_code) {
      
    }];
}

// cell点击事件
- (void)cellResendTapBtnClick:(NSIndexPath *)indexPath {
    ZoloForwardView *forwardView = [[ZoloForwardView alloc] initWithFrame:CGRectZero];
    forwardView.titleArray = @[LocalizedString(@"MsgResend")];
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
        [ws forwardResendViewClick:index withIndex:indexPath.row];
        [wk_popView dismiss];
    };
}

- (void)forwardResendViewClick:(NSInteger)type withIndex:(NSInteger)itemIndex {
    switch (type) {
        case 0:
        {
            DMCCMessage *msg = self.dataSource[itemIndex];
            [self sendMessage:msg.content pwd:nil];
            [[DMCCIMService sharedDMCIMService] deleteMessage:msg.messageId];
            [self.dataSource removeObject:msg];
            [self.tableView reloadData];
        }
            break;
        default:
            break;
    }
}

- (void)cellIconTapBtnClick:(NSIndexPath *)indexPath {
    WS(ws);
    if (![[DMCCIMService sharedDMCIMService] isAllowAddFriendGroupMemberName:self.conversation.conversation.target]) {
        [MHAlert showMessage:LocalizedString(@"StopMemberChat")];
        return;
    }
    // 判断好友是否已经存在
    DMCCMessage *message = self.dataSource[indexPath.row];
    DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:self.conversation.conversation.target refresh:NO];
    DMCCUserInfo *userInfo = nil;
    if ([message.fromUser isEqualToString:self.myInfo.userId]) {
         userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:self.myInfo.userId inGroup:group.target refresh:NO];
    } else {
        userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:message.fromUser inGroup:group.target refresh:NO];
    }
    if ([[DMCCIMService sharedDMCIMService] isMyFriend:userInfo.userId] || [message.fromUser isEqualToString:self.myInfo.userId]) {
        [self.navigationController pushViewController:[[ZoloContactDetailVC alloc] initWithUserInfo:userInfo] animated:YES];
    } else {
        ZoloAddFriendInfoVC *vc = [[ZoloAddFriendInfoVC alloc] initWithUserInfo:userInfo];
        [ws.navigationController pushViewController:vc animated:YES];
    }
}

- (void)cellTapBtnClick:(NSIndexPath *)indexPath {
    DMCCMessage *msg = self.dataSource[indexPath.row];
    if ([msg.content isKindOfClass:[DMCCVideoMessageContent class]] || [msg.content isKindOfClass:[DMCCImageMessageContent class]]) {
        
        [self seeImageOrVideo2:msg];
        
    } else if ([msg.content isKindOfClass:[DMCCCardMessageContent class]]) {
        
        DMCCCardMessageContent *cardContent = (DMCCCardMessageContent *)msg.content;
        if (cardContent.type == CardType_User) {
            
            DMCCUserInfo *userInfo = [DMCCUserInfo new];
            userInfo.userId = cardContent.targetId;
            if ([[DMCCIMService sharedDMCIMService] isMyFriend:userInfo.userId]) {
                [self.navigationController pushViewController:[[ZoloContactDetailVC alloc] initWithUserInfo:userInfo] animated:YES];
            } else {
                ZoloAddFriendInfoVC *vc = [[ZoloAddFriendInfoVC alloc] initWithUserInfo:userInfo];
                [self.navigationController pushViewController:vc animated:YES];
            }
        } else if (cardContent.type == CardType_Litapp) {
            DMCCLitappInfo *litapp = [DMCCLitappInfo new];
            litapp.name = cardContent.name;
            litapp.target = cardContent.targetId;
            litapp.url = cardContent.url;
            litapp.portrait = cardContent.portrait;
            litapp.info = cardContent.info;
            
            ZoloDappViewController *vc = [[ZoloDappViewController alloc] initWithLitappInfo:litapp];
            MHNavigationVC *nav = [[MHNavigationVC alloc] initWithRootViewController:vc];
            nav.modalPresentationStyle = UIModalPresentationFullScreen;
            [self presentViewController:nav animated:YES completion:nil];
            
        } else if (cardContent.type == CardType_Group) {
            
            DMCCCardMessageContent *cardContent = (DMCCCardMessageContent *)msg.content;
//             判断群组是否已经存在
            DMCCConversationInfo* convInfo = [self getConversation:cardContent.targetId];
            if (convInfo != nil) {
                DMCCConversationInfo *info = [DMCCConversationInfo new];
                DMCCConversation *conversation = [DMCCConversation conversationWithType:Group_Type target:cardContent.targetId line:0];
                info.conversation = conversation;
                [self.navigationController pushViewController:[[ZoloSingleChatVC alloc] initWithConversationInfo:info] animated:YES];
                return;
            }

            DMCCGroupInfo *groupInfo = [DMCCGroupInfo new];
            groupInfo.name = cardContent.targetId;
            groupInfo.target = cardContent.targetId;
            groupInfo.portrait = @"";
            ZoloAddGroupInfoVC *vc = [[ZoloAddGroupInfoVC alloc] initWithGroupInfo:groupInfo];
            [self.navigationController pushViewController:vc animated:YES];
            
        }
    } else if ([msg.content isKindOfClass:[DMCCSoundMessageContent class]]) {
        
        [self playSoundWithMsg:msg withIndex:indexPath.row];
        
    } else if ([msg.content isKindOfClass:[DMCCRedPacketMessageContent class]]) {
        DMCCRedPacketMessageContent *content = (DMCCRedPacketMessageContent *)msg.content;
        DMCCRedPacketInfo *info = [[DMCCIMService sharedDMCIMService] getRedPacket:content.ids];
        if ([info.type isEqualToString:@"normal"]) { // 转账
            ZoloTransferRecordVC *vc = [[ZoloTransferRecordVC alloc] initWithRedPacketInfo:info];
            WS(ws);
            vc.receiveBlock = ^{
                [ws updateMessage:info withMsg:msg];
            };
            [self.navigationController pushViewController:vc animated:YES];
        } else if ([info.type isEqualToString:@"loot"]) { // 手气
            if(info.state == 0) {
                [self openRedPackWithDic:info withMsg:msg];
            } else {
                // 红包获取记录
                [self redpackRecordList:info];
            }
        } else if ([info.type isEqualToString:@"bomb"]) { // 扫雷
            if(info.state == 0) {
                [self openRedPackWithDic:info withMsg:msg];
            } else {
                // 红包获取记录
                [self redpackRecordList:info];
            }
        }
    } else if ([msg.content isKindOfClass:[DMCCFileMessageContent class]]) {
        
        [self downFileWithMsg:msg];
        
    }
}

- (void)downFileWithMsg:(DMCCMessage *)msg {
    
    DMCCFileMessageContent *fileContent = (DMCCFileMessageContent *)msg.content;
    
    if ([fileContent.remoteUrl hasSuffix:@".zip"] && fileContent.decKey.length == 0) {
        [MHAlert showMessage:LocalizedString(@"FileError")];
        return;
    }
    
    if (fileContent.localPath.length > 0) {
        
        if ([msg.fromUser isEqualToString:[DMCCIMService sharedDMCIMService].getUserID]) {
            [self.navigationController pushViewController:[[ZoloFileOpenController alloc] initWithUrlInfo:fileContent.remoteUrl isHidden:YES] animated:YES];
        } else {
            [self previewFileWithURL:[NSURL fileURLWithPath:fileContent.localPath]];
        }
        
    } else  if ([fileContent.remoteUrl hasSuffix:@".zip"] && fileContent.decKey.length > 0) {
        [self downFile:msg];
        return;
    } else {
        [self.navigationController pushViewController:[[ZoloFileOpenController alloc] initWithUrlInfo:fileContent.remoteUrl isHidden:YES] animated:YES];
    }

}

- (void)downFile:(DMCCMessage *)obj {
    
    WS(ws);
    [MHAlert showLoadingStr:LocalizedString(@"loadLoading")];
    [[DMCCIMService sharedDMCIMService] downWithMessage:obj cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        if (isSuccess) {
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                NSInteger index= [ws messageAtIndex:[NSString stringWithFormat:@"%llu",obj.messageUid]];
                if (index != -1) {
                    [MHAlert dismiss];
                    [ws.dataSource replaceObjectAtIndex:index withObject:[[DMCCIMService sharedDMCIMService] getMessageByUid:obj.messageUid]];
                    NSIndexPath *indexPath=[NSIndexPath indexPathForRow:index inSection:0];
                    [ws.tableView reloadRowsAtIndexPaths:[NSArray arrayWithObjects:indexPath,nil] withRowAnimation:UITableViewRowAnimationAutomatic];
                    [ws downFileWithMsg:[[DMCCIMService sharedDMCIMService] getMessageByUid:obj.messageUid]];
                }
            });
        }
    } progress:^(long progress, long total) {
        
    }];
    
}

- (void)previewFileWithURL:(NSURL *)URL {
    [MHAlert showLoadingStr:LocalizedString(@"loadLoading")];
    if ([URL isFileURL]) {
        if ([[[NSFileManager alloc] init] fileExistsAtPath:URL.path]) {

            self.doVC = [UIDocumentInteractionController interactionControllerWithURL:URL];
            self.doVC.delegate = self;
            [self.doVC presentOpenInMenuFromRect:self.view.bounds inView:self.view animated:YES];
        }
    } else {
        if ([URL.scheme containsString:@"http"]) {
            NSString *filePathString = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES).firstObject stringByAppendingPathComponent:URL.absoluteString.lastPathComponent];
   
            NSURLSessionDownloadTask *downloadTask = [self.sessionManager downloadTaskWithRequest:[NSURLRequest requestWithURL:URL] progress:^(NSProgress * _Nonnull downloadProgress) {
                
            } destination:^NSURL * _Nonnull(NSURL * _Nonnull targetPath, NSURLResponse * _Nonnull response) {
                return [NSURL fileURLWithPath:filePathString];
            } completionHandler:^(NSURLResponse * _Nonnull response, NSURL * _Nullable filePath, NSError * _Nullable error) {
                
                self.doVC = [UIDocumentInteractionController interactionControllerWithURL:filePath];
                self.doVC.delegate = self;
                [self.doVC presentOpenInMenuFromRect:self.view.bounds inView:self.view animated:YES];
                
            }];
            //启动下载文件任务
            [downloadTask resume];
        }
    }
}

-(AFHTTPSessionManager *)sessionManager
{
    if (!_sessionManager) {
        _sessionManager = [AFHTTPSessionManager manager];
        _sessionManager.requestSerializer.timeoutInterval = 45.f;
        _sessionManager.responseSerializer = [AFJSONResponseSerializer serializer];
        _sessionManager.responseSerializer.acceptableContentTypes = [NSSet setWithObjects:@"application/json", @"text/html", @"text/json", @"text/plain", @"text/javascript", @"text/xml", @"image/*", nil];
    }
    return _sessionManager;
}

- (void)audioPlayerStateDidChanged:(LGAudioPlayerState)audioPlayerState forIndex:(NSUInteger)index {
    NSLog(@"audioPlayerStateDidChanged === %ld, %ld", audioPlayerState , index);
    if (audioPlayerState == 2 || audioPlayerState == 0) {
        DMCCMessage *msg = self.dataSource[index];
        NSArray *array = self.tableView.visibleCells;
        for (ZoloBaseChatCell *cell in array) {
            if ([cell isKindOfClass:[ZoloChatVoiceCell class]]) {
                if (cell.message.messageUid == msg.messageUid) {
                    ZoloChatVoiceCell *voiceCell = (ZoloChatVoiceCell *)cell;
                    if (audioPlayerState == 2) {
                        [voiceCell.voiceImg startAnimating];
                    } else if (audioPlayerState == 0 || audioPlayerState == 3) {
                        [voiceCell.voiceImg stopAnimating];
                    }
                }
            }
        }
    }
}

- (UIViewController *)documentInteractionControllerViewControllerForPreview:(UIDocumentInteractionController *)controller {
    return self;
    
}

- (UIView*)documentInteractionControllerViewForPreview:(UIDocumentInteractionController*)controller {
    return self.view;
}

- (CGRect)documentInteractionControllerRectForPreview:(UIDocumentInteractionController*)controller {
    
    return CGRectMake(0, self.view.frame.size.height - 300, self.view.frame.size.width, 300);
}

- (DMCCConversationInfo *)getConversation:(NSString *)target {
    DMCCConversation *conversation = [DMCCConversation conversationWithType:Group_Type target:target line:0];
    DMCCConversationInfo *info = [[DMCCIMService sharedDMCIMService] getConversationInfo:conversation];
    return info;
}

// 手气红包
- (void)openRedPackWithDic:(DMCCRedPacketInfo *)dic withMsg:(DMCCMessage *)msg {
    ZoloRedPackView *view = [[ZoloRedPackView alloc] initWithFrame:CGRectZero];
    view.frame = CGRectMake(0, 0, kScreenwidth, KScreenheight);
    view.titleLab.text = dic.text;
    [[DMCCIMService sharedDMCIMService] getUserInfo:dic.user refresh:YES success:^(DMCCUserInfo *userInfo) {
        if (userInfo) {
            dispatch_async(dispatch_get_main_queue(), ^{
                [view.iconImg sd_setImageWithURL:[NSURL URLWithString:userInfo.portrait] placeholderImage:SDImageDefault];
                view.nameLab.text = [NSString stringWithFormat:@"%@%@", userInfo.displayName, LocalizedString(@"win_RedSaveLab")];
            });
        }
    } error:^(int errorCode) {
        
    }];

    LSTPopView *popView = [LSTPopView initWithCustomView:view
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
    view.redPackCancelBlcok = ^{
        [wk_popView dismiss];
    };
    WS(ws);
    view.redPackOpenBlcok = ^{
        [wk_popView dismiss];
        [ws openRedPackWithInfo:dic withMsg:msg];
    };
    [popView pop];
}

// 开红包
- (void)openRedPackWithInfo:(DMCCRedPacketInfo *)info withMsg:(DMCCMessage *)msg {
    if(info.state == 0 && [info.type isEqualToString:@"loot"]) {
        NSLog(@"urlQuery: %@",info.urlQuery);
        NSLog(@"urlFetch: %@",info.urlFetch);
        NSMutableDictionary* data = [NSMutableDictionary new];
        data[@"get"] = @"txid";
        data[@"txid"] = info.unpackID;
        NSLog(@"price data: %@",[OsnUtils dic2Json:data]);
        data = [HttpUtils doPosts:info.urlQuery data:[OsnUtils dic2Json:data]];
        if(data == nil)
            return;
        NSLog(@"price result: %@",[OsnUtils dic2Json:data]);
        data = [self getRedData:data];
        if(data == nil)
            return;
        if((NSInteger)data[@"balance"] == 0){
            return;
        }
        long timestamp = [OsnUtils getTimeStamp];
        NSMutableDictionary *sign = [NSMutableDictionary new];
        sign[@"groupID"] = self.conversation.conversation.target;
        sign[@"from"] = [[DMCCIMService sharedDMCIMService] getUserID];
        sign[@"txid"] = info.unpackID;
        sign[@"timestamp"] = [NSString stringWithFormat:@"%ld",timestamp];
        NSLog(@"getGroupSign data: %@",[OsnUtils dic2Json:sign]);
        [MHAlert showLoadingDelayStr:LocalizedString(@"loadLoading")];
        [[DMCCIMService sharedDMCIMService]getGroupSign:self.conversation.conversation.target data:[OsnUtils dic2Json:sign] cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
            if(isSuccess){
                NSLog(@"getGroupSign result: %@", [OsnUtils dic2Json:json]);
                dispatch_async(dispatch_get_main_queue(), ^{
                    [self openGroupRedPacket:json withInfo:info withMsg:msg];
                });
            }else{
                [MHAlert showLoadingStr:LocalizedString(@"NetworkError")];
                NSLog(@"getGroupSign result: %@", error);
            }
        }];
    }else{
        // 红包获取记录
        [self redpackRecordList:info];
        [self updateMessage:info withMsg:msg];
    }
}

- (NSMutableDictionary*) getRedData:(NSDictionary*)data{
    if(data == nil)
        return nil;
    NSNumber *code = data[@"code"];
    if(code.longValue != 200){
        [MHAlert showCodeMessage:code.longValue];
        return nil;
    }
    return data[@"data"];
}

- (void) openGroupRedPacket:(NSMutableDictionary*)json withInfo:(DMCCRedPacketInfo *)info withMsg:(DMCCMessage *)msg {
    NSString *hash = [NSString stringWithFormat:@"%@%@%@%@",json[@"txid"],json[@"groupID"],json[@"from"],json[@"timestamp"]];
    NSLog(@"hash: %@", hash);
    json[@"hash"] = [[DMCCIMService sharedDMCIMService] hashData:[hash dataUsingEncoding:NSUTF8StringEncoding]];
    json[@"sign"] = [[DMCCIMService sharedDMCIMService] signData:[hash dataUsingEncoding:NSUTF8StringEncoding]];
    NSLog(@"openGroupRedPacket info: %@",[OsnUtils dic2Json:json]);
    json = [HttpUtils doPosts:info.urlFetch data:[OsnUtils dic2Json:json]];
    [MHAlert dismiss];
    if(json == nil)
        return;
    NSLog(@"openGroupRedPacket result: %@",[OsnUtils dic2Json:json]);
    [self openShowRedPackWithDic:info withDicInfo:json];
    [self updateMessage:info withMsg:msg];
}

- (void) updateMessage:(DMCCRedPacketInfo *)info withMsg:(DMCCMessage *)msg {
    [[DMCCIMService sharedDMCIMService] openRedPacket:info.packetID unpackID:info.unpackID messageID:msg.messageId cb:nil];
    NSInteger index= [self messageAtIndex:[NSString stringWithFormat:@"%llu",msg.messageUid]];
    if (index != -1) {
        [self.dataSource replaceObjectAtIndex:index withObject:[[DMCCIMService sharedDMCIMService] getMessageByUid:msg.messageUid]];
        NSIndexPath *indexPath=[NSIndexPath indexPathForRow:index inSection:0];
        [self.tableView reloadRowsAtIndexPaths:[NSArray arrayWithObjects:indexPath,nil] withRowAnimation:UITableViewRowAnimationAutomatic];
    }
}

// 获取消息下标，用来局部刷新
- (NSInteger)messageAtIndex:(NSString *)msgID {
    NSInteger count = self.dataSource.count;
    for(int i = 0; i < count; i++){
        DMCCMessage *msg = self.dataSource[i];
        if (msg.messageUid == msgID.longLongValue) {
            return i;
        }
    }
    return -1;
}

- (void)openShowRedPackWithDic:(DMCCRedPacketInfo *)dic withDicInfo:(NSMutableDictionary *)info {
    ZoloRedPackOpenView *view = [[ZoloRedPackOpenView alloc] initWithFrame:CGRectZero];
    view.frame = CGRectMake(0, 0, kScreenwidth, KScreenheight);
    view.titleLab.text = dic.text;
    if (dic.coinType.length > 0) {
        view.moneyLab.text = [NSString stringWithFormat:@"%@ %.6f", dic.coinType, [info[@"data"][@"balance"] doubleValue] / 1000000];
    } else {
        view.moneyLab.text = [NSString stringWithFormat:@"USDT %.6f", [info[@"data"][@"balance"] doubleValue] / 1000000];
    }
    
    if ([info[@"data"][@"balance"] floatValue] > 0) {
        view.redPackOverLab.hidden = YES;
        view.moneyLab.hidden = NO;
        view.typeLab.hidden = NO;
    } else {
        view.moneyLab.hidden = YES;
        view.typeLab.hidden = YES;
        view.redPackOverLab.hidden = NO;
    }
    
    [[DMCCIMService sharedDMCIMService] getUserInfo:dic.user refresh:YES success:^(DMCCUserInfo *userInfo) {
        if (userInfo) {
            dispatch_async(dispatch_get_main_queue(), ^{
                [view.iconImg sd_setImageWithURL:[NSURL URLWithString:userInfo.portrait] placeholderImage:SDImageDefault];
                view.nameLab.text = [NSString stringWithFormat:@"%@%@", userInfo.displayName, LocalizedString(@"win_RedSaveLab")];
            });
        }
    } error:^(int errorCode) {
        
    }];

    LSTPopView *popView = [LSTPopView initWithCustomView:view
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
    WS(ws);
    view.redPackCancelBlcok = ^{
        [wk_popView dismiss];
    };
    view.redPackDetailBlcok = ^{
        [wk_popView dismiss];
        [ws redpackRecordList:dic];
    };
    [popView pop];
}

- (void)redpackRecordList:(DMCCRedPacketInfo*)info {
    [self.navigationController pushViewController:[[ZoloRedPackRecordVC alloc] initWithRedPacket:info] animated:YES];
}

- (void)downImageOrVideo:(DMCCMessage *)obj {
    WS(ws);
    [MHAlert showLoadingStr:LocalizedString(@"loadLoading")];
    [[DMCCIMService sharedDMCIMService] downWithMessage:obj cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        if (isSuccess) {
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                NSInteger index= [ws messageAtIndex:[NSString stringWithFormat:@"%llu",obj.messageUid]];
                if (index != -1) {
                    [MHAlert dismiss];
                    [ws.dataSource replaceObjectAtIndex:index withObject:[[DMCCIMService sharedDMCIMService] getMessageByUid:obj.messageUid]];
                    NSIndexPath *indexPath=[NSIndexPath indexPathForRow:index inSection:0];
                    [ws.tableView reloadRowsAtIndexPaths:[NSArray arrayWithObjects:indexPath,nil] withRowAnimation:UITableViewRowAnimationAutomatic];
                    [ws seeImageOrVideo2:[[DMCCIMService sharedDMCIMService] getMessageByUid:obj.messageUid]];
                }
            });
        }
    } progress:^(long progress, long total) {
        
    }];

}

- (void)downSound:(DMCCMessage *)obj {
    WS(ws);
    [MHAlert showLoadingStr:LocalizedString(@"loadLoading")];
    [[DMCCIMService sharedDMCIMService] downWithMessage:obj cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        if (isSuccess) {
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                NSInteger index= [ws messageAtIndex:[NSString stringWithFormat:@"%llu",obj.messageUid]];
                if (index != -1) {
                    [MHAlert dismiss];
                    [ws.dataSource replaceObjectAtIndex:index withObject:[[DMCCIMService sharedDMCIMService] getMessageByUid:obj.messageUid]];
                    NSIndexPath *indexPath=[NSIndexPath indexPathForRow:index inSection:0];
                    [ws.tableView reloadRowsAtIndexPaths:[NSArray arrayWithObjects:indexPath,nil] withRowAnimation:UITableViewRowAnimationAutomatic];
                    [ws playSoundWithMsg:[[DMCCIMService sharedDMCIMService] getMessageByUid:obj.messageUid] withIndex:index];
                }
            });
        }
    } progress:^(long progress, long total) {
        
    }];
}

// 播放语音
- (void)playSoundWithMsg:(DMCCMessage *)msg withIndex:(NSInteger)index {
    DMCCSoundMessageContent *soundContent = (DMCCSoundMessageContent *)msg.content;
    
    if ([soundContent.remoteUrl hasSuffix:@".zip"] && soundContent.decKey.length == 0) {
        [MHAlert showMessage:LocalizedString(@"FileError")];
        return;
    }
    
    if (soundContent.localPath.length > 0) {
        NSString *soundStr = @"";
        if ([soundContent.localPath hasSuffix:@".mp3"]) {
            soundStr = soundContent.localPath;
        } else {
            soundStr = [NSString stringWithFormat:@"%@", soundContent.localPath];
        }
        [[LGAudioPlayer sharePlayer] playAudioWithURLString:soundStr atIndex:index];
        [LGAudioPlayer sharePlayer].delegate = self;
    } else  if ([soundContent.remoteUrl hasSuffix:@".zip"] && soundContent.decKey.length > 0) {
        [self downSound:msg];
        return;
    } else {
        NSString *soundStr = @"";
        if ([soundContent.remoteUrl hasSuffix:@".mp3"]) {
            soundStr = soundContent.remoteUrl;
        } else {
            soundStr = [NSString stringWithFormat:@"%@", soundContent.remoteUrl];
        }
        [[LGAudioPlayer sharePlayer] playAudioOnlineWithContentsOfURL:[NSURL URLWithString:soundStr]];
        [LGAudioPlayer sharePlayer].delegate = self;
    }
    if (msg.status == Message_Status_Unread) {
        [[DMCCIMService sharedDMCIMService] updateMessage:msg.messageId status:Message_Status_Readed];
        NSInteger index= [self messageAtIndex:[NSString stringWithFormat:@"%llu",msg.messageUid]];
        if (index != -1) {
            [self.dataSource replaceObjectAtIndex:index withObject:[[DMCCIMService sharedDMCIMService] getMessageByUid:msg.messageUid]];
            NSIndexPath *indexPath=[NSIndexPath indexPathForRow:index inSection:0];
            [self.tableView reloadRowsAtIndexPaths:[NSArray arrayWithObjects:indexPath,nil] withRowAnimation:UITableViewRowAnimationAutomatic];
        }
    }
}

// 查看图片或视频
- (void)seeImageOrVideo2:(DMCCMessage *)obj {
    [self.view endEditing:YES];
    NSString *homeStr = NSHomeDirectory();
    NSMutableArray *datas = [NSMutableArray array];
    if ([obj.content isKindOfClass:[DMCCVideoMessageContent class]]) {
        DMCCVideoMessageContent *videoContent = (DMCCVideoMessageContent *)obj.content;
        
        if ([videoContent.remoteUrl hasSuffix:@".zip"] && videoContent.decKey.length == 0) {
            [MHAlert showMessage:LocalizedString(@"FileError")];
            return;
        }
        
        if (videoContent.localPath.length > 0) {
            YBIBVideoData *data = [YBIBVideoData new];
            NSString *videoStr = @"";
            if ([videoContent.localPath containsString:homeStr]) {
                if ([videoContent.localPath hasSuffix:@".mp4"]) {
                    videoStr = videoContent.localPath;
                } else {
                    videoStr = [NSString stringWithFormat:@"%@", videoContent.localPath];
                }
            } else {
                videoStr = videoContent.remoteUrl;
                [self downImageOrVideo:obj];
                return;
            }
            data.videoURL = [NSURL fileURLWithPath:videoStr];
            [datas addObject:data];
            
            
        } else {
                YBIBVideoData *data = [YBIBVideoData new];
                
                NSString *videoStr = @"";
                if ([videoContent.remoteUrl hasSuffix:@".mp4"]) {
                    videoStr = videoContent.remoteUrl;
                } else {
                    videoStr = [NSString stringWithFormat:@"%@", videoContent.remoteUrl];
                }
                
                data.videoURL = [NSURL URLWithString:videoStr];
                [datas addObject:data];
                [self downImageOrVideo:obj];
                return;
            }
        } else if ([obj.content isKindOfClass:[DMCCImageMessageContent class]]) {
            DMCCImageMessageContent *imgContent = (DMCCImageMessageContent *)obj.content;
            
            if ([imgContent.remoteUrl hasSuffix:@".zip"] && imgContent.decKey.length == 0) {
                [MHAlert showMessage:LocalizedString(@"FileError")];
                return;
            }
            
            if (imgContent.localPath.length > 0) {
                YBIBImageData *data = [YBIBImageData new];
                NSString *imgStr = @"";
                if ([imgContent.localPath containsString:homeStr]) {
                    if ([imgContent.localPath hasSuffix:@".jpg"]) {
                        imgStr = imgContent.localPath;
                    } else if ([imgContent.localPath hasSuffix:@".png"]) {
                        imgStr = imgContent.localPath;
                    } else {
                        imgStr = [NSString stringWithFormat:@"%@", imgContent.localPath];
                    }
                    data.imagePath = imgStr;
                    [datas addObject:data];
                } else {
                    imgStr = imgContent.remoteUrl;
                    data.imageURL = [NSURL URLWithString:imgStr];
                    [datas addObject:data];
                    [self downImageOrVideo:obj];
                    return;
                }
                
            } else {
                YBIBImageData *data = [YBIBImageData new];
                
                NSString *imgStr = @"";
                if ([imgContent.remoteUrl hasSuffix:@".jpg"]) {
                    imgStr = imgContent.remoteUrl;
                } else if ([imgContent.localPath hasSuffix:@".png"]) {
                    imgStr = imgContent.localPath;
                } else {
                    imgStr = [NSString stringWithFormat:@"%@", imgContent.remoteUrl];
                }
                
                data.imageURL = [NSURL URLWithString:imgStr];
                [datas addObject:data];
                [self downImageOrVideo:obj];
                return;
            }
        }
    
    YBImageBrowser *browser = [YBImageBrowser new];
    browser.dataSourceArray = datas;
    browser.currentPage = 0;
    browser.defaultToolViewHandler.topView.operationType = YBIBTopViewOperationTypeSave;
    [browser show];
}

// 查看图片或视频
- (void)seeImageOrVideo:(DMCCMessage *)msg {
    [self.view endEditing:YES];
    NSMutableArray *datas = [NSMutableArray array];
    [self.dataSource enumerateObjectsUsingBlock:^(DMCCMessage *_Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        if ([obj.content isKindOfClass:[DMCCVideoMessageContent class]]) {
            DMCCVideoMessageContent *videoContent = (DMCCVideoMessageContent *)obj.content;
            if (videoContent.localPath.length > 0) {
                YBIBVideoData *data = [YBIBVideoData new];
                
                NSString *videoStr = @"";
                if ([videoContent.localPath hasSuffix:@".mp4"]) {
                    videoStr = videoContent.localPath;
                } else {
                    videoStr = [NSString stringWithFormat:@"%@", videoContent.localPath];
                }
                
                data.videoURL = [NSURL fileURLWithPath:videoStr];
                [datas addObject:data];
                
                
            } else {
                YBIBVideoData *data = [YBIBVideoData new];
                
                NSString *videoStr = @"";
                if ([videoContent.remoteUrl hasSuffix:@".mp4"]) {
                    videoStr = videoContent.remoteUrl;
                } else {
                    videoStr = [NSString stringWithFormat:@"%@", videoContent.remoteUrl];
                }
                
                data.videoURL = [NSURL URLWithString:videoStr];
                [datas addObject:data];
            }
        } else if ([obj.content isKindOfClass:[DMCCImageMessageContent class]]) {
            DMCCImageMessageContent *imgContent = (DMCCImageMessageContent *)obj.content;
            if (imgContent.localPath.length > 0) {
                YBIBImageData *data = [YBIBImageData new];
                
                NSString *imgStr = @"";
                if ([imgContent.localPath hasSuffix:@".jpg"]) {
                    imgStr = imgContent.localPath;
                } else if ([imgContent.localPath hasSuffix:@".png"]) {
                    imgStr = imgContent.localPath;
                } else {
                    imgStr = [NSString stringWithFormat:@"%@.jpg", imgContent.localPath];
                }
                
                
                
                data.imagePath = imgStr;
                [datas addObject:data];
                
            } else {
                YBIBImageData *data = [YBIBImageData new];
                
                NSString *imgStr = @"";
                if ([imgContent.remoteUrl hasSuffix:@".jpg"]) {
                    imgStr = imgContent.remoteUrl;
                } else if ([imgContent.localPath hasSuffix:@".png"]) {
                    imgStr = imgContent.localPath;
                } else {
                    imgStr = [NSString stringWithFormat:@"%@.jpg", imgContent.remoteUrl];
                }
                
                data.imageURL = [NSURL URLWithString:imgStr];
                [datas addObject:data];
            }
        }
    }];
    
    NSInteger index = 0;
    for (int i = 0; i < datas.count; i++) {
        id obj = datas[i];
        NSString *fileName = @"";
        if ([obj isKindOfClass:[YBIBImageData class]]) {
            YBIBImageData *da = (YBIBImageData *)obj;
            if (da.imagePath.length > 0) {
                fileName = da.imagePath;
            } else {
                fileName = [da.imageURL absoluteString];
            }
        } else {
            YBIBVideoData *da = (YBIBVideoData *)obj;
            fileName = [da.videoURL absoluteString];
        }
        
        if ([msg.content isKindOfClass:[DMCCImageMessageContent class]]) {
            DMCCImageMessageContent *imgContent = (DMCCImageMessageContent *)msg.content;
            if (imgContent.localPath.length > 0) {
                if ([fileName containsString:imgContent.localPath]) {
                    index = i;
                    break;
                }
            } else {
                if ([fileName containsString:imgContent.remoteUrl]) {
                    index = i;
                    break;
                }
            }
        } else {
            DMCCVideoMessageContent *videoContent = (DMCCVideoMessageContent *)msg.content;
            if (videoContent.localPath.length > 0) {
                if ([fileName containsString:videoContent.localPath]) {
                    index = i;
                    break;
                }
            } else {
                if ([fileName containsString:videoContent.remoteUrl]) {
                    index = i;
                    break;
                }
            }
        }
    }
    
    YBImageBrowser *browser = [YBImageBrowser new];
    browser.dataSourceArray = datas;
    browser.currentPage = index;
    browser.defaultToolViewHandler.topView.operationType = YBIBTopViewOperationTypeSave;
    [browser show];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 0.001;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 0.001;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    // cell高度是先调用的
    DMCCMessage *msg = self.dataSource[indexPath.row];
    if ([msg.content isKindOfClass:[DMCCCallMessageContent class]]) {
        DMCCCallMessageContent *callContent = (DMCCCallMessageContent *)msg.content;
        if (callContent.status.length == 0) {
            return 0;
        }
    }
    Class cellCls = self.cellContentDict[@([[msg.content class] getContentType])];
    [cellCls calculateCellHeight:msg chatType:self.chatType == Group_Type mulSelect:self.isMulSelect byMsg:[msg.fromUser isEqualToString:self.myInfo.userId]];
    BOOL isNickName = ![[DMCCIMService sharedDMCIMService] isHiddenGroupMemberName:self.conversation.conversation.target];
    int nickNameHeight = 0;
    int msgTimeHeight = 20;
    if (isNickName) {
        nickNameHeight = 0;
    }
    if (msg.msgCellHeight == 0) {
        return 22 + nickNameHeight + msgTimeHeight;
    } else {
        return msg.msgCellHeight + nickNameHeight + msgTimeHeight;
    }
}

- (BOOL)isGroupOwner {
    DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:self.conversation.conversation.target refresh:NO];
    return [group.owner isEqualToString:[DMCCNetworkService sharedInstance].userId];
}

- (BOOL)isGroupManger {
    DMCCGroupMember *gm = [[DMCCIMService sharedDMCIMService] getGroupMember:self.conversation.conversation.target memberId:[DMCCNetworkService sharedInstance].userId];
    return gm.type == Member_Type_Manager;
}

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

// 删除超时的top消息
- (void)removeTopMsg:(NSString *)groupId {
    NSString *time = [[DMCCIMService sharedDMCIMService] getClearChatsGroupMemberName:groupId];
    if ([time longLongValue] > 0) {
        long long currentTime = [MHDateTools timeStampWithDate:[MHDateTools getCurrentTimes]];
        [[DMCCIMService sharedDMCIMService] delMessages:(currentTime - [time longLongValue]) target:groupId];
        
        if ([self isGroupOwner]) {
            NSArray *topMsgs = [NSMutableArray arrayWithArray:[[DMCCIMService sharedDMCIMService] getGroupTopMessageWithGroupId:groupId]];
            NSString *times = [[DMCCIMService sharedDMCIMService] getClearChatsGroupMemberName:groupId];
            if (topMsgs.count > 0) {
                NSMutableArray *keys = [NSMutableArray arrayWithCapacity:0];
                for (NSString *str in topMsgs) {
                    NSDictionary *dic = [OsnUtils json2Dics:str];
                    NSString *timeStr = dic[@"key"];
                    NSString *time = [timeStr componentsSeparatedByString:@"_"].lastObject;
                    long long currentTime = [MHDateTools timeStampWithDate:[MHDateTools getCurrentTimes]];
                    if ([time longLongValue] <= (currentTime - [times longLongValue])) {
                        [keys addObject:dic[@"key"]];
                    }
                }
                if (keys.count > 0) {
                    [[DMCCIMService sharedDMCIMService] removeTopChatsWithGroupId:groupId keysData:keys cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
                        
                    }];
                }
            }
        }
    }
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
