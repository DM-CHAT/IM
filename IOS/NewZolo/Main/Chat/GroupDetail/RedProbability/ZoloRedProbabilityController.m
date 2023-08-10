//
//  ZoloWalletViewController.m
//  NewZolo
//
//  Created by JTalking on 2022/9/8.
//

#import "ZoloRedProbabilityController.h"
#import <WebKit/WebKit.h>
#import "ZoloRedPackRecordVC.h"
#import "ZoloAddFriendInfoVC.h"
#import "ZoloContactDetailVC.h"
#import "ZoloDappViewController.h"

@interface ZoloRedProbabilityController () <WKScriptMessageHandler>

@property (strong, nonatomic) WKWebView* webView;
@property (nonatomic, copy) NSString *url;
@property (nonatomic, copy) NSString *groupId;
@property (nonatomic, assign) BOOL isHideNav;

@end

@implementation ZoloRedProbabilityController

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    if (self.isHideNav) {
        [self.navigationController setNavigationBarHidden:NO animated:animated];
    } else {
        [self.navigationController setNavigationBarHidden:YES animated:animated];
    }
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [self.navigationController setNavigationBarHidden:NO animated:animated];
}

- (instancetype)initWithUrlInfo:(NSString *)url groupId:(NSString *)groupId isHidden:(BOOL)isHideNav {
    if (self = [super init]) {
        self.url = url;
        self.isHideNav = isHideNav;
        self.groupId = groupId;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    WKUserContentController *userContentController = [[WKUserContentController alloc] init];
    [userContentController addScriptMessageHandler:self name:@"osnsdk"];
    
    NSString *javaScriptSource = @"var osnsdk = new Object();"
        @"osnsdk.run = function(args){"
        @"return window.webkit.messageHandlers.osnsdk.postMessage(args);}";
    WKUserScript *userScript = [[WKUserScript alloc] initWithSource:javaScriptSource injectionTime:WKUserScriptInjectionTimeAtDocumentEnd forMainFrameOnly:YES];
    [userContentController addUserScript:userScript];

    WKWebViewConfiguration *configuration = [[WKWebViewConfiguration alloc] init];
    configuration.userContentController = userContentController;

    _webView = [[WKWebView alloc] initWithFrame:CGRectMake(0, 20 + iPhoneX_topH, KScreenWidth, KScreenHeight - iPhoneX_bottomH) configuration:configuration];
    [self.view addSubview:_webView];
    
    [MHAlert showLoadingStr:LocalizedString(@"loadLoading")];
    
    NSString *token = [[NSUserDefaults standardUserDefaults]objectForKey:@"ospn_token"];
    NSString *language = [[NSUserDefaults standardUserDefaults]objectForKey:@"ospn_language"];
    
    if(strstr(_url.UTF8String, "?")){
        _url = [_url stringByAppendingString:@"&"];
    }else{
        _url = [_url stringByAppendingString:@"?"];
    }
    _url = [_url stringByAppendingFormat:@"language=%@&token=%@&groupId=%@",language, token, self.groupId];
    [_webView addObserver:self forKeyPath:@"estimatedProgress" options:NSKeyValueObservingOptionNew context:nil];
    [_webView loadRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:self.url]]];
}

- (void)userContentController:(WKUserContentController *)userContentController didReceiveScriptMessage:(WKScriptMessage *)message {
    NSLog(@"args: %@",message.body);
    NSDictionary* args = [OsnUtils json2Dics:message.body];
    NSString* command = args[@"command"];
    if(command == nil)
        return;
    if([command isEqualToString:@"GoBack"])
        [self.navigationController popViewControllerAnimated:YES];
    else if ([command isEqualToString:@"ShowRedPacket"]) {
        DMCCRedPacketInfo *info = [DMCCRedPacketInfo new];
        info.packetID = args[@"txid"];
        info.unpackID = args[@"txid"];
        info.urlQuery = args[@"urlQuery"];
        info.luckNum = args[@"luckNum"];
        info.text = args[@"text"];
        info.count = args[@"count"];
        info.price = args[@"price"];
        info.type = args[@"type"];
        if(info.type.intValue == 2)
            info.type = @"loot";
        else
            info.type = @"bomb";
        [self.navigationController pushViewController:[[ZoloRedPackRecordVC alloc] initWithRedPacket:info] animated:YES];
    } else if ([command isEqualToString:@"AddFriend"]) {
        [self addFriend:args[@"userID"]];
    } else if ([command isEqualToString:@"OpenDapp"]) {
        id info = args[@"DappInfo"];
        NSDictionary *dic = nil;
        if ([info isKindOfClass:[NSString class]] ) {
            dic = [OsnUtils json2Dics:args[@"DappInfo"]];
        } else if ([info isKindOfClass:[NSDictionary class]] ) {
            dic = args[@"DappInfo"];
        }
        
        DMCCLitappInfo *litapp = [DMCCLitappInfo new];
        litapp.target = dic[@"target"];
        litapp.url = dic[@"url"];
        litapp.info = dic[@"info"];
        litapp.name = dic[@"name"];
        litapp.portrait = dic[@"portrait"];
        
        ZoloDappViewController *vc = [[ZoloDappViewController alloc] initWithLitappInfo:litapp];
        MHNavigationVC *nav = [[MHNavigationVC alloc] initWithRootViewController:vc];
        nav.modalPresentationStyle = UIModalPresentationFullScreen;
        [self presentViewController:nav animated:YES completion:nil];
    }
}

- (void)addFriend:(NSString *)userId {
    // 判断好友是否已经存在
    DMCCUserInfo *userInfo = [DMCCUserInfo new];
    userInfo.userId = userId;
    if ([[DMCCIMService sharedDMCIMService] isMyFriend:userInfo.userId]) {
        [self.navigationController pushViewController:[[ZoloContactDetailVC alloc] initWithUserInfo:userInfo] animated:YES];
    } else {
        ZoloAddFriendInfoVC *vc = [[ZoloAddFriendInfoVC alloc] initWithUserInfo:userInfo];
        [self.navigationController pushViewController:vc animated:YES];
    }
}


//- (void)addFriend:(NSString *)userId {
//    WS(ws);
//    [[DMCCIMService sharedDMCIMService] getUserInfo:userId refresh:true success:^(DMCCUserInfo *userInfo) {
//        dispatch_async(dispatch_get_main_queue(), ^{
//            // 判断好友是否已经存在
//            if ([[DMCCIMService sharedDMCIMService] isMyFriend:userInfo.userId]) {
//                [ws.navigationController pushViewController:[[ZoloContactDetailVC alloc] initWithUserInfo:userInfo] animated:YES];
//            } else {
//                ZoloAddFriendInfoVC *vc = [[ZoloAddFriendInfoVC alloc] initWithUserInfo:userInfo];
//                [ws.navigationController pushViewController:vc animated:YES];
//            }
//        });
//    } error:^(int errorCode) {
//        dispatch_async(dispatch_get_main_queue(), ^{
//            [MHAlert showMessage:LocalizedString(@"AlertAddFail")];
//        });
//    }];
//}

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary<NSString *,id> *)change context:(void *)context {
    if ([keyPath isEqualToString:@"estimatedProgress"]) {
        if (self.webView.estimatedProgress == 1) {
            [MHAlert dismiss];
        }
    }else{
        [super observeValueForKeyPath:keyPath ofObject:object change:change context:context];
    }
}

- (void)dealloc {
    NSLog(@"==ZoloWalletViewController===dealloc==");
    [self.webView removeObserver:self forKeyPath:@"estimatedProgress"];
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
