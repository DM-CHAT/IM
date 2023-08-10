//
//  ZoloHomeViewController.m
//  NewZolo
//
//  Created by 陈泽萱 on 2022/6/11.
//

#import "ZoloHomeViewController.h"
#import "ZoloSingleVC.h"
#import "ZoloGroupVC.h"
#import "SGPagingView.h"
#import "ZoloHomeAddView.h"
#import "ZoloAddFriendVC.h"
#import "ScanQRcode.h"
#import "WBQRCodeVC.h"
#import "ZoloSendGroupVC.h"
#import "ZoloAddFriendInfoVC.h"
#import "ZoloContactDetailVC.h"
#import "ZoloSearchViewController.h"
#import "ZoloSingleChatVC.h"
#import "ZoloAddGroupInfoVC.h"
#import "ZoloPersonVC.h"
#import "ZoloTagListVC.h"
#import "ZoloSingleCallVC.h"
#import "ZoloChatPushView.h"
#import "LSTPopView.h"
#import "ZoloNotice.h"
#import "ZoloBindAcountView.h"
#import "AppDelegate.h"

@interface ZoloHomeViewController () <SGPageTitleViewDelegate, SGPageContentScrollViewDelegate> {
    dispatch_queue_t msgQueue;
}

@property (nonatomic, strong) SGPageTitleView *pageTitleView;
@property (nonatomic, strong) SGPageContentScrollView *pageContentView;
@property (nonatomic, strong) NSArray *titleArr;
@property (nonatomic, strong) NSArray *childArr;
@property (nonatomic, strong) UIView *btnView;
@property (nonatomic, strong) UIButton *addBtn;
@property (nonatomic, strong) UIButton *searchBtn;
@property (nonatomic, strong) ZoloHomeAddView *addView;
@property (nonatomic, strong) UILabel *nameLab;
@property (nonatomic, assign) NSInteger currentTargetIndex;
@property (nonatomic, assign) NSInteger currentTags;
@property (nonatomic, assign) bool isCalling;

@end

@implementation ZoloHomeViewController

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    dispatch_async(msgQueue, ^{[self updateBadgeNumber];});
}

- (void)getFriendList {
    dispatch_async(msgQueue, ^{
        [[DMCCIMService sharedDMCIMService] getFriendList:YES];
    });
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    msgQueue = dispatch_queue_create("com.ospn.chatHome", DISPATCH_QUEUE_CONCURRENT);
    dispatch_async(msgQueue, ^{[self updateBadgeNumber];});
    
    self.currentTargetIndex = 0;
    [self.btnView addSubview:self.addBtn];
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:self.btnView];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onReceiveMessages:) name:kReceiveMessages object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(updateBadgeNumber) name:FZ_EVENT_MSGSTATUS_STATE object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(tagStatus) name:FZ_EVENT_TAGSTATUS_STATE object:nil];
    self.view.backgroundColor = [FontManage MHWhiteColor];
    [self setupPageView];
    BOOL isShow = [[NSUserDefaults standardUserDefaults] boolForKey:@"isShowNick"];
    if (!isShow) {
        [self setUserNickName];
    }
    WS(ws);
    NSString *language = [OSNLanguage getBaseLanguage];
    [[ZoloAPIManager instanceManager] getChatPushNotice:language.intValue WithCompleteBlock:^(ZoloNotice * _Nonnull data) {
        if (data) {
            [ws showNoticeView:data];
        }
    }];
    
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [self getFriendList];
    });
}

- (void)showBindView {
    ZoloBindAcountView *view = [[ZoloBindAcountView alloc] initWithFrame:CGRectZero];
    view.frame = CGRectMake(0, 0, kScreenwidth*0.8, KScreenheight/2);
    view.layer.cornerRadius = 16;
    view.layer.masksToBounds = YES;
    LSTPopView *popView = [LSTPopView initWithCustomView:view
                                                popStyle:LSTPopStyleSmoothFromTop
                                            dismissStyle:LSTDismissStyleSmoothToBottom];
    popView.popStyle = LSTPopStyleNO;
    popView.dismissStyle = LSTDismissStyleNO;
    popView.popDuration = 1.0;
    popView.dismissDuration = 1.0;
    LSTPopViewWK(popView)
    WS(ws);
    view.bindEmailBtnBlock = ^(NSString *str) {
        [[ZoloAPIManager instanceManager] bindAccount:@"" account:str type:2 WithCompleteBlock:^(BOOL isSuccess) {
            if (isSuccess) {
                [wk_popView dismiss];
                [ws quitelogin];
            }
        }];
    };
    
    view.bindPhoneBtnBlock = ^(NSString *zone, NSString *phone) {
        [[ZoloAPIManager instanceManager] bindAccount:zone account:phone type:1 WithCompleteBlock:^(BOOL isSuccess) {
            if (isSuccess) {
                [wk_popView dismiss];
                [ws quitelogin];
            }
        }];
    };
    
    [popView pop];
}

- (void)quitelogin {
    [[NSUserDefaults standardUserDefaults] removeObjectForKey:@"ospn_token"];
    [[NSUserDefaults standardUserDefaults] removeObjectForKey:@"ospn_id"];
    [[NSUserDefaults standardUserDefaults] removeObjectForKey:@"mne_account_current"];
    [[ZoloInfoManager sharedUserManager] loginOut];
    [[DMCCNetworkService sharedInstance] disconnect:YES clearSession:YES];
    AppDelegate *appDelegate = (AppDelegate *)[UIApplication sharedApplication].delegate;
    [appDelegate setRootView];
}

- (void)showNoticeView:(ZoloNotice *)notice {
    ZoloChatPushView *view = [[ZoloChatPushView alloc] initWithFrame:CGRectZero];
    view.frame = CGRectMake(0, 0, kScreenwidth*0.8, KScreenheight/2);
    view.layer.cornerRadius = 16;
    view.layer.masksToBounds = YES;
    view.noticeContent.text = notice.remark;
    LSTPopView *popView = [LSTPopView initWithCustomView:view
                                                popStyle:LSTPopStyleSmoothFromTop
                                            dismissStyle:LSTDismissStyleSmoothToBottom];
    popView.popStyle = LSTPopStyleNO;
    popView.dismissStyle = LSTDismissStyleNO;
    popView.popDuration = 1.0;
    popView.dismissDuration = 1.0;
    LSTPopViewWK(popView)
    WS(ws);
    view.noticeBtnBlock = ^{
        [ws noticeViewConfirm:notice.id];
        [wk_popView dismiss];
    };
    [popView pop];
}

- (void)noticeViewConfirm:(NSInteger)index {
    [[ZoloAPIManager instanceManager] getChatConfirmNoticeWithIndex:index WithCompleteBlock:^(BOOL isSuccess) {

    }];
}

- (void)setUserNickName {
    [[NSUserDefaults standardUserDefaults] setBool:YES forKey:@"isShowNick"];
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login.json.Alias.length > 0) {
        return;
    }
    NSString *str = LocalizedString(@"UserSetNickName");
    WS(ws);
    [MHAlert showCustomeAlert:str withAlerttype:MHAlertTypeRemind withOkBlock:^(UIAlertAction *action) {
        if ([action.title isEqualToString:LocalizedString(@"Sure")]) {
            [[DMCCIMService sharedDMCIMService] getUserInfo:[[OsnSDK getInstance] getUserID] refresh:NO success:^(DMCCUserInfo *userInfo) {
                if (userInfo) {
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [ws.navigationController pushViewController:[[ZoloPersonVC alloc] initWithUserInfo:userInfo] animated:YES];
                    });
                }
            } error:^(int errorCode) {
                
            }];
        }
    }];
}

- (UIView *)btnView {
    if (!_btnView) {
        _btnView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 44, 44)];
    }
    return _btnView;
}

- (UIButton *)addBtn {
    if (!_addBtn) {
        _addBtn = [[UIButton alloc] initWithFrame:CGRectMake(10, 0, 44, 44)];
        [_addBtn setImage:[UIImage imageNamed:@"add"] forState:UIControlStateNormal];
        [_addBtn addTarget:self action:@selector(addBtnClick) forControlEvents:UIControlEventTouchUpInside];
    }
    return _addBtn;
}

- (UIButton *)searchBtn {
    if (!_searchBtn) {
        _searchBtn = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, 44, 44)];
        [_searchBtn setImage:[UIImage imageNamed:@"h_search"] forState:UIControlStateNormal];
        [_searchBtn addTarget:self action:@selector(searchBtnClick) forControlEvents:UIControlEventTouchUpInside];
    }
    return _searchBtn;
}

- (UILabel *)nameLab {
    if (!_nameLab) {
        _nameLab = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 44, 44)];
        _nameLab.text = LocalizedString(@"Home");
        _nameLab.font =[UIFont fontWithName:@"PingFangSC-Semibold" size:20];
        _nameLab.textColor = [FontManage MHBlockColor];
    }
    return _nameLab;
}

- (ZoloHomeAddView *)addView {
    if (!_addView) {
        _addView = [[ZoloHomeAddView alloc] initWithFrame:CGRectZero];
        _addView.hidden = YES;
        WS(ws);
        _addView.addViewBlock = ^(NSInteger index) {
            [ws addViewIndexClick:index];
        };
        [self.view addSubview:_addView];
    }
    return _addView;
}

- (void)searchBtnClick {
    ZoloSearchViewController *vc = [ZoloSearchViewController new];
    [self.navigationController pushViewController:vc animated:YES];
}

- (void)addViewIndexClick:(NSInteger)index {
    [self removeAddView];

    switch (index) {
        case 0:
        {
            ZoloSendGroupVC *vc = [ZoloSendGroupVC new];
            WS(ws);
            vc.sendGroupBlock = ^(NSString * _Nonnull groupId) {
                
                [MHAlert showLoadingStr:LocalizedString(@"AlertNowOpeating")];
                dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                    [MHAlert dismiss];
                    DMCCConversationInfo *info = [DMCCConversationInfo new];
                    DMCCConversation *conversation = [DMCCConversation conversationWithType:Group_Type target:groupId line:0];
                    info.conversation = conversation;
                    [ws.navigationController pushViewController:[[ZoloSingleChatVC alloc] initWithConversationInfo:info] animated:YES];
                });
                
            };
            [self presentViewController:vc animated:YES completion:nil];
        }
            break;
        case 1:
        {
            [self.navigationController pushViewController:[ZoloAddFriendVC new] animated:YES];
        }
            break;
        case 2:
        {
            
            if ( [MHHelperUtils havaAVAuthorizationVideoStatusCode]) {
                WBQRCodeVC *vc = [WBQRCodeVC new];
                WS(ws);
                vc.resultBlcok = ^(NSString *result) {
                   [ws resultBlockWithStr:result];
                };
                [self.navigationController pushViewController:vc animated:YES];
            }
        }
            break;
        default:
            break;
    }
}

// 扫描结果处理
- (void)resultBlockWithStr:(NSString *)str {
    NSLog(@"==scanstr ===%@==", str);
    if ([str rangeOfString:@"ospn://user" options:NSCaseInsensitiveSearch].location == 0) {
        [MHAlert showLoadingStr:LocalizedString(@"AlertOpreating")];
        NSRange range = [str rangeOfString:@"ospn://user/"];
        NSString *scanStr = [str substringFromIndex:range.length];
        
        DMCCUserInfo *userInfo = [DMCCUserInfo new];
        userInfo.userId = scanStr;
        if ([[DMCCIMService sharedDMCIMService] isMyFriend:userInfo.userId]) {
            [self.navigationController pushViewController:[[ZoloContactDetailVC alloc] initWithUserInfo:userInfo] animated:YES];
        } else {
            ZoloAddFriendInfoVC *vc = [[ZoloAddFriendInfoVC alloc] initWithUserInfo:userInfo];
            [self.navigationController pushViewController:vc animated:YES];
        }
        
//        WS(ws);
//        [[DMCCIMService sharedDMCIMService] getUserInfo:scanStr refresh:NO success:^(DMCCUserInfo *userInfo) {
//            dispatch_async(dispatch_get_main_queue(), ^{
//                // 判断好友是否已经存在
//                if ([[DMCCIMService sharedDMCIMService] isMyFriend:userInfo.userId]) {
//                    [self.navigationController pushViewController:[[ZoloContactDetailVC alloc] initWithUserInfo:userInfo] animated:YES];
//                } else {
//                    ZoloAddFriendInfoVC *vc = [[ZoloAddFriendInfoVC alloc] initWithUserInfo:userInfo];
//                    [ws.navigationController pushViewController:vc animated:YES];
//                }
//            });
//        } error:^(int errorCode) {
//
//            [MHAlert dismiss];
//            dispatch_async(dispatch_get_main_queue(), ^{
//                // 判断好友是否已经存在
//                DMCCUserInfo *userInfo = [DMCCUserInfo new];
//                userInfo.userId = scanStr;
//                if ([[DMCCIMService sharedDMCIMService] isMyFriend:userInfo.userId]) {
//                    [ws.navigationController pushViewController:[[ZoloContactDetailVC alloc] initWithUserInfo:userInfo] animated:YES];
//                } else {
//                    ZoloAddFriendInfoVC *vc = [[ZoloAddFriendInfoVC alloc] initWithUserInfo:userInfo];
//                    [ws.navigationController pushViewController:vc animated:YES];
//                }
//            });
//
//        }];
        
    } else if ([str rangeOfString:@"ospn://group" options:NSCaseInsensitiveSearch].location == 0) {
        
        NSRange range = [str rangeOfString:@"ospn://group/"];
        NSString *scanStr = [str substringFromIndex:range.length];
        
        NSString * groupId = scanStr;
    
        DMCCConversationInfo * convInfo = [self getConversation:groupId];
        if (convInfo != nil) {
            DMCCConversationInfo *info = [DMCCConversationInfo new];
            DMCCConversation *conversation = [DMCCConversation conversationWithType:Group_Type target:groupId line:0];
            info.conversation = conversation;
            [self.navigationController pushViewController:[[ZoloSingleChatVC alloc] initWithConversationInfo:info] animated:YES];
            return;
        }
        
        DMCCGroupInfo *groupInfo = [DMCCGroupInfo new];
        groupInfo.name = groupId;
        groupInfo.target = groupId;
        groupInfo.portrait = @"";
        ZoloAddGroupInfoVC *vc = [[ZoloAddGroupInfoVC alloc] initWithGroupInfo:groupInfo];
        [self.navigationController pushViewController:vc animated:YES];
        
//        [MHAlert showLoadingStr:LocalizedString(@"AlertOpreating")];
//        // 在这里加个转圈
//        [[DMCCIMService sharedDMCIMService] getGroupInfoEx:groupId
//                                                   refresh:NO
//                                                        cb:^(bool isSuccess2, DMCCGroupInfo* groupInfo, NSString *error) {
//            [MHAlert dismiss];
//            if (isSuccess2) {
//                if (groupInfo.isMember !=nil) {
//                    if ([groupInfo.isMember isEqualToString:@"yes"]) {
//                        [[DMCCIMService sharedDMCIMService] insertConversation:groupId groupInfo:groupInfo];
//                        //[SqliteUtils insertConversation:Group_Type target:groupInfo.groupID line:0];
//                        DMCCConversation *conversation = [DMCCConversation conversationWithType:Group_Type target:groupId line:0];
//                        DMCCConversationInfo *info = [DMCCConversationInfo new];
//                        info.conversation = conversation;
//                        dispatch_sync(dispatch_get_main_queue(), ^{
//                            [self.navigationController pushViewController:[[ZoloSingleChatVC alloc] initWithConversationInfo:info] animated:YES];
//                        });
//
//                        return;
//                    }
//                }
//            } else {
//                groupInfo = [DMCCGroupInfo new];
//                groupInfo.name = @"";
//                groupInfo.target = groupId;
//                groupInfo.portrait = @"";
//            }
//
//            dispatch_sync(dispatch_get_main_queue(), ^{
//                ZoloAddGroupInfoVC *vc = [[ZoloAddGroupInfoVC alloc] initWithGroupInfo:groupInfo];
//                [self.navigationController pushViewController:vc animated:YES];
//            });
//
//
//        }];
        
    } else if ([str rangeOfString:@"ospn://pcsession" options:NSCaseInsensitiveSearch].location == 0) {
     
    } else if ([str rangeOfString:@"ospn://weblogin" options:NSCaseInsensitiveSearch].location == 0) {

        NSRange range = [str rangeOfString:@"ospn://weblogin/"];
        NSString *scanStr = [str substringFromIndex:range.length];
        NSDictionary *dic = [OsnUtils json2Dics:scanStr];
        NSMutableDictionary *mdic = [NSMutableDictionary dictionaryWithDictionary:dic];
        long timestamp = [OsnUtils getTimeStamp];
        [mdic setObject:@(timestamp) forKey:@"timestamp"];
        NSString* calc = [NSString stringWithFormat:@"%@%@%ld",dic[@"url"],dic[@"webId"],timestamp];
        NSString* hash = [[DMCCIMService sharedDMCIMService] hashData:[calc dataUsingEncoding:NSUTF8StringEncoding]];
        NSString* sign = [[DMCCIMService sharedDMCIMService] signData:[hash dataUsingEncoding:NSUTF8StringEncoding]];
        [mdic setObject:sign forKey:@"sign"];
        [mdic setObject:[DMCCIMService sharedDMCIMService].getUserID forKey:@"osnId"];
        
        NSString *str = [NSString stringWithFormat:LocalizedString(@"ScanloginTitle"), dic[@"url"]];
        [MHAlert showCustomeAlert:str withAlerttype:MHAlertTypeRemind withOkBlock:^(UIAlertAction *action) {
            if ([action.title isEqualToString:LocalizedString(@"Sure")]) {
                [MHAlert showLoadingStr:LocalizedString(@"AlertNowOpeating")];
                [[ZoloAPIManager instanceManager] scanLoginDappWithDic:mdic WithCompleteBlock:^(BOOL isSuccess) {
                    if (isSuccess) {
                        [MHAlert showMessage:LocalizedString(@"ScanloginSucess")];
                    } else {
                        [MHAlert showMessage:LocalizedString(@"AlertOpearFail")];
                    }
                }];
            }
        }];
        
    }
}

- (DMCCConversationInfo *)getConversation:(NSString *)target {
    DMCCConversation *conversation = [DMCCConversation conversationWithType:Group_Type target:target line:0];
    DMCCConversationInfo *info = [[DMCCIMService sharedDMCIMService] getConversationInfo:conversation];
    return info;
}

- (void)addBtnClick {
    self.addView.hidden = NO;
    self.addView.alpha = 1;
}

- (void)removeAddView {
    [self.addView removeFromSuperview];
    self.addView = nil;
}

- (void)tagStatus {

    // 判断数据是否和网络数据一致
    if (self.currentTags == [[DMCCIMService sharedDMCIMService] getTagList].count) {
        return;
    }
    
    [self.pageTitleView removeFromSuperview];
    [self.pageContentView removeFromSuperview];
    
    [self setupPageView];
}

- (void)setupPageView {
    _titleArr = @[LocalizedString(@"Friends"), LocalizedString(@"Group")];
    
    NSArray *tagInfos = [[DMCCIMService sharedDMCIMService] getTagList];
    NSMutableArray *tags = [NSMutableArray arrayWithArray:_titleArr];
    NSMutableArray *tagsVC = [NSMutableArray arrayWithCapacity:0];
    
    for (DMCCTagInfo *tag in tagInfos) {
        [tags addObject:tag.group_name];
        [tagsVC addObject:[[ZoloTagListVC alloc] initWithTagInfo:tag]];
    }
    
    self.currentTags = tags.count -2;
    
    SGPageTitleViewConfigure *configure = [SGPageTitleViewConfigure pageTitleViewConfigure];
    
    configure.showBottomSeparator = NO;
    configure.indicatorStyle = SGIndicatorStyleCover;
    configure.indicatorCornerRadius = 15;
    configure.titleFont = [UIFont fontWithName:@"PingFangTC-Medium" size:16];
    configure.titleSelectedFont = [UIFont fontWithName:@"PingFangTC-Medium" size:16];
    configure.titleColor = [FontManage MHHomeNColor];
    configure.titleSelectedColor = MHColorFromHex(0x4FA075);
    configure.indicatorColor = [UIColor colorWithRed:79/255.0 green:160/255.0 blue:117/255.0 alpha:0.2000];
    configure.indicatorAdditionalWidth = 20;
  
    ZoloSingleVC *single = [[ZoloSingleVC alloc] init];
    ZoloGroupVC *group = [[ZoloGroupVC alloc] init];
    
    self.childArr = @[single, group];
    
    NSMutableArray *childsVC = [NSMutableArray arrayWithArray:self.childArr];
    for (ZoloTagListVC *tagVC in tagsVC) {
        [childsVC addObject:tagVC];
    }
    
    NSString *language = [[NSUserDefaults standardUserDefaults] stringForKey:@"ospn_language"];
    NSLog(@"==%@==", language);
    
    float width = (120 / 2) * tags.count;
    int margin = 5;
    if (![language isEqualToString:@"0"]) {
        width = (150 / 2) * tags.count;
        margin = 15;
    }
    if (width > kScreenwidth) {
        width = kScreenwidth - 40;
    }
    self.pageTitleView = [SGPageTitleView pageTitleViewWithFrame:CGRectMake(margin, 56 + iPhoneX_topH, width, 54 + iPhoneX_topH) delegate:self titleNames:tags configure:configure];
    self.pageTitleView.backgroundColor = [UIColor clearColor];
    [self.view addSubview: _pageTitleView];
    self.pageContentView = [[SGPageContentScrollView alloc] initWithFrame:CGRectMake(0, 110 + iPhoneX_topH , kScreenwidth, KScreenHeight) parentVC:self childVCs:childsVC];
    _pageContentView.delegatePageContentScrollView = self;
    [self.view addSubview:_pageContentView];
}

- (void)pageTitleView:(SGPageTitleView *)pageTitleView selectedIndex:(NSInteger)selectedIndex {
    [self.pageContentView setPageContentScrollViewCurrentIndex:selectedIndex];
}

- (void)pageContentScrollView:(SGPageContentScrollView *)pageContentScrollView progress:(CGFloat)progress originalIndex:(NSInteger)originalIndex targetIndex:(NSInteger)targetIndex {
    self.currentTargetIndex = targetIndex;
    if (targetIndex == 1) {
        [self.pageTitleView removeBadgeForIndex:1];
        MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
        if (login.json.MainDapp.length > 0) {
            [self.tabBarController.tabBar hideBadgeOnItemIndex:1];
        } else {
            [self.tabBarController.tabBar hideBadgeOnItemIndex:0];
        }
    } else if (targetIndex == 0) {
        [self.pageTitleView removeBadgeForIndex:0];
        MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
        if (login.json.MainDapp.length > 0) {
            [self.tabBarController.tabBar hideBadgeOnItemIndex:1];
        } else {
            [self.tabBarController.tabBar hideBadgeOnItemIndex:0];
        }
    }
    [self.pageTitleView setPageTitleViewWithProgress:progress originalIndex:originalIndex targetIndex:targetIndex];
}

// 收到消息，判断消息是否相同类型，否则丢弃
- (void)onReceiveMessages:(NSNotification *)notification {
    
    NSArray<DMCCMessage *> *messages = notification.object;
    DMCCMessage *msg = [messages firstObject];
    [self updateBadgeNumber];
    if([msg.content.class getContentType] == MESSAGE_CONTENT_TYPE_CALL){
        DMCCCallMessageContent *callMessageContent = (DMCCCallMessageContent*)msg.content;
        if(callMessageContent.action == CallActionInvite && !_isCalling){
            if(callMessageContent.mode == CallModeSingle){
                dispatch_async(dispatch_get_main_queue(), ^{
                    ZoloSingleCallVC *vc = [ZoloSingleCallVC new];
                    vc.conversation = msg.conversation;
                    vc.status = @"invite";
                    vc.receiveCall = callMessageContent;
                    vc.isAudioOnly = callMessageContent.type == CallTypeAudio;
                    vc.modalPresentationStyle = UIModalPresentationFullScreen;
                    [self presentViewController:vc animated:YES completion:nil];
                });
            }
        }
    }
    
}

// 未读计数
- (void)updateBadgeNumber {
    NSMutableArray *tagArray = [NSMutableArray arrayWithCapacity:0];
    
    DMCCTagInfo *user = [DMCCTagInfo new];
    user.group_name = @"好友";
    user.id = -1;
    
    [tagArray addObject:user];
    
    DMCCTagInfo *group = [DMCCTagInfo new];
    group.group_name = @"群组";
    group.id = -2;
    [tagArray addObject:group];
    
    
    NSArray *tagInfos = [[DMCCIMService sharedDMCIMService] getTagList];
    [tagArray addObjectsFromArray:tagInfos];
    
    NSInteger msgCount = 0;
    
    for (DMCCTagInfo *info in tagArray) {
        NSInteger count = [[DMCCIMService sharedDMCIMService] getUnreadCountWithTagId:info.id];
        info.count = count;
        msgCount += count;
    }
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if (msgCount > 0) {
            self.nameLab.text = [NSString stringWithFormat:@"%@(%ld)", LocalizedString(@"Home"), msgCount];
            self.tabBarItem.badgeValue = [NSString stringWithFormat:@"%ld", msgCount];
        } else if (msgCount > 99) {
            self.nameLab.text = [NSString stringWithFormat:@"%@(...)", LocalizedString(@"Home")];
            self.tabBarItem.badgeValue = @"99+";
        } else {
            self.nameLab.text = LocalizedString(@"Home");
            self.tabBarItem.badgeValue = nil;
        }
        
        if (msgCount > 0) {
            for (int i = 0; i < tagArray.count; i++) {
                DMCCTagInfo *info = tagArray[i];
                if (info.count > 0) {
                    NSLog(@"==ad %d==", i);
                    [self.pageTitleView addBadgeForIndex:i];
                } else {
                    [self.pageTitleView removeBadgeForIndex:i];
                }
            }
            MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
            if (login.json.MainDapp.length > 0) {
                [self.tabBarController.tabBar showBadgeOnItemIndex:1];
            } else {
                [self.tabBarController.tabBar showBadgeOnItemIndex:0];
            }
        }
        else {
            for (int i = 0; i < tagArray.count; i++) {
                [self.pageTitleView removeBadgeForIndex:i];
            }
            MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
            if (login.json.MainDapp.length > 0) {
                [self.tabBarController.tabBar hideBadgeOnItemIndex:1];
            } else {
                [self.tabBarController.tabBar hideBadgeOnItemIndex:0];
            }
        }
    });
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
