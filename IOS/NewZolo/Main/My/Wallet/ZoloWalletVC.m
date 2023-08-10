//
//  ViewController.m
//  Calendar
//
//  Created by Calendar on 2022/7/14.
//

#import "ZoloWalletVC.h"
#import <WebKit/WebKit.h>
#import "ZoloContactDetailVC.h"
#import "ZoloAddFriendInfoVC.h"
#import "ZoloAddGroupInfoVC.h"
#import "ZoloSingleChatVC.h"
#import "ZoloDappViewController.h"
#import "ZoloShareDappVC.h"
#import "ZoloAddWalletVC.h"

@interface ZoloWalletVC () <WKNavigationDelegate, WKScriptMessageHandler>

@property (strong, nonatomic) WKWebView *webView;
@property (weak, nonatomic) IBOutlet UIView *subView;
@property (nonatomic, strong) UIButton *closeBtn;
@property (nonatomic, strong) DMCCLitappInfo *appInfo;
@property (nonatomic, assign) bool islogin;
@property (nonatomic, copy) NSString *decPassword;
@property (nonatomic, copy) NSString *payPassword;
@property (nonatomic, strong) NSDictionary *dataDic;
@property (nonatomic, strong) NSDictionary *argsDic;
@property (nonatomic, assign) BOOL isShowPass;
@property (nonatomic, copy) NSString *webUrl;
@property (nonatomic, strong) DMCCConversation *conversation;

@end

@implementation ZoloWalletVC

- (instancetype)initWithLitappInfo:(DMCCLitappInfo *)appInfo withUrl:(NSString *)url withConversation:(DMCCConversation *)conversation {
    if (self = [super init]) {
        self.appInfo = appInfo;
        self.webUrl = url;
        self.conversation = conversation;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.navigationItem.title = self.appInfo.name;
    [self setUpWebView];
}

- (UIButton *)closeBtn {
    if (!_closeBtn) {
        _closeBtn = [[UIButton alloc] initWithFrame:CGRectZero];
        [_closeBtn setImage:[UIImage imageNamed:@"close_off"] forState:UIControlStateNormal];
        [_closeBtn setTitleColor:[FontManage MHMainColor] forState:UIControlStateNormal];
        [_closeBtn addTarget:self action:@selector(closeBtnClick) forControlEvents:UIControlEventTouchUpInside];
    }
    return _closeBtn;
}

- (void)closeBtnClick {
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (void)setUpWebView {
    WKUserContentController *userContentController = [[WKUserContentController alloc] init];
    NSString *javaScriptSource = @"var osnsdk = new Object();"
        @"osnsdk.run = function(args){"
        @"return window.webkit.messageHandlers.osnsdk.postMessage(args);}";
    WKUserScript *userScript = [[WKUserScript alloc] initWithSource:javaScriptSource injectionTime:WKUserScriptInjectionTimeAtDocumentStart forMainFrameOnly:YES];
    [userContentController addUserScript:userScript];
    [userContentController addScriptMessageHandler:self name:@"osnsdk"];
    
    NSString *logger = @"console.log = (function(oriLogFunc){"
        @"return function(str){"
        @"oriLogFunc.call(console,str);"
        @"window.webkit.messageHandlers.log.postMessage(str);}"
        @"})(console.log);";
    userScript = [[WKUserScript alloc] initWithSource:logger injectionTime:WKUserScriptInjectionTimeAtDocumentStart forMainFrameOnly:YES];
    [userContentController addUserScript:userScript];
    [userContentController addScriptMessageHandler:self name:@"log"];

    [MHAlert showLoadingStr:LocalizedString(@"loadLoading")];
    
    WKWebViewConfiguration *configuration = [[WKWebViewConfiguration alloc] init];
    configuration.userContentController = userContentController;
            
    _webView = [[WKWebView alloc] initWithFrame:CGRectMake(0, 0, KScreenWidth, KScreenHeight - iPhoneX_bottomH - 64) configuration:configuration];
    
    [_webView loadRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:self.webUrl]]];
    [_webView addObserver:self forKeyPath:@"estimatedProgress" options:NSKeyValueObservingOptionNew context:nil];
    [self.subView addSubview:_webView];
}
 
- (void)userContentController:(WKUserContentController *)userContentController didReceiveScriptMessage:(WKScriptMessage *)message {
    NSLog(@"====args: %@",message.body);
    if([message.name isEqualToString:@"log"]) {
        NSLog(@"WKWebView console log: %@", message.body);
        return;
    }
    NSLog(@"args: %@",message.body);
    NSDictionary* args = [OsnUtils json2Dics:message.body];
    self.argsDic = args;
    NSString* command = args[@"command"];
    if(command == nil)
        return;
    if([command isEqualToString:@"GoBack"])
        [self dismissViewControllerAnimated:YES completion:nil];
    else if([command isEqualToString:@"Login"]) {
        WS(ws);
        [[DMCCIMService sharedDMCIMService] loginLitappWithInfo:self.appInfo url:args[@"url"] success:^(NSMutableDictionary *json) {
            self->_islogin = true;
            NSString *sessionKey = json[@"sessionKey"];
            NSDictionary *token = [OsnUtils json2Dics:sessionKey];
            NSString *callback = [NSString stringWithFormat:@"%@(%@)", args[@"callback"], [OsnUtils dic2Json:token]];
            NSLog(@"callback: %@", callback);
            dispatch_async(dispatch_get_main_queue(), ^{
                [ws.webView evaluateJavaScript:callback completionHandler:^(id _Nullable result, NSError * _Nullable error) {
                    NSLog(@"done");
                }];
            });
        } error:^(int error_code) {
            self->_islogin = false;
        }];
    } else if ([command isEqualToString:@"GetUserInfo"]){
        WS(ws);
        if(!_islogin){
            NSMutableDictionary *result = [NSMutableDictionary new];
            result[@"errCode"] = @"-1:need login";
            NSString *callback = [NSString stringWithFormat:@"%@(%@)", args[@"callback"], [OsnUtils dic2Json:result]];
            NSLog(@"callback: %@", callback);
            dispatch_async(dispatch_get_main_queue(), ^{
                [ws.webView evaluateJavaScript:callback completionHandler:^(id _Nullable result, NSError * _Nullable error) {
                    NSLog(@"done");
                }];
            });
        }else{
            NSString *userId = [[DMCCIMService sharedDMCIMService] getUserID];
            DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:userId refresh:false];
            NSMutableDictionary *result = [NSMutableDictionary new];
            result[@"userID"] = userInfo.userId;
            result[@"userName"] = userInfo.name;
            result[@"nickName"] = userInfo.displayName;
            result[@"portrait"] = userInfo.portrait;
            NSString *callback = [NSString stringWithFormat:@"%@(%@)", args[@"callback"], [OsnUtils dic2Json:result]];
            NSLog(@"callback: %@", callback);
            dispatch_async(dispatch_get_main_queue(), ^{
                [ws.webView evaluateJavaScript:callback completionHandler:^(id _Nullable result, NSError * _Nullable error) {
                    NSLog(@"done");
                }];
            });
        }
    } else if ([command isEqualToString:@"AddFriend"]) {
        [self addFriend:args[@"userID"]];
    } else if ([command isEqualToString:@"GroupInfo"]) {
        [self addGroup:args[@"osnID"]];
    } else if ([command isEqualToString:@"SetNft"]) {
        [self setNftImg:args[@"portrait"] withUrl:args[@"url"]];
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
    } else if ([command isEqualToString:@"Share"]) {
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
        
        ZoloShareDappVC *vc = [[ZoloShareDappVC alloc] initWithLitappInfo:litapp];
        MHNavigationVC *nav = [[MHNavigationVC alloc] initWithRootViewController:vc];
        nav.modalPresentationStyle = UIModalPresentationFullScreen;
        [self presentViewController:nav animated:YES completion:nil];
    } else if ([command isEqualToString:@"PaySign"]) {
        NSString *needShadowSign = args[@"needShadowSign"];
        NSString *needPayPassword = args[@"needPayPassword"];
        if (needShadowSign == nil && needPayPassword == nil) {
            return;
        }
        
        // 弹框两次
        if (needShadowSign != nil && needPayPassword != nil) {
            [self paySign:args[@"data"]];
            self.isShowPass = YES;
            [self inputEncPasswordWithNameALert:LocalizedString(@"inputEncocPassword") withAlerttype:MHAlertTypeRemind];
        } else {
            self.isShowPass = NO;
            // 弹框一次
            if (needShadowSign != nil && [needShadowSign isEqualToString:@"yes"]) {
                [self paySign:args[@"data"]];
                [self inputEncPasswordWithNameALert:LocalizedString(@"inputEncocPassword") withAlerttype:MHAlertTypeRemind];
            }
            
            // 弹框一次
            if (needPayPassword != nil && [needPayPassword isEqualToString:@"yes"]) {
                [self paySign:args[@"data"]];
                [self inputPayPasswordWithNameALert:LocalizedString(@"inputPayPassword") withAlerttype:MHAlertTypeRemind];
            }
        }
    } else if ([command isEqualToString:@"AddWallet"]) {
        ZoloAddWalletVC *vc = [[ZoloAddWalletVC alloc] initWithLitappInfo:self.appInfo];
        vc.modalPresentationStyle = UIModalPresentationFullScreen;
        [self presentViewController:vc animated:YES completion:nil];
    } else if ([command isEqualToString:@"TransactionResult"]) {
        NSMutableDictionary *data = [[NSMutableDictionary alloc] init];
        NSDictionary *json = args[@"Data"];
        NSString* txid = json[@"txid"];
        NSString* urlQuery = json[@"queryUrl"];
        NSString* urlFetch = json[@"urlFetch"];
        NSString* wallet = json[@"wallet"];
        NSString* coinType = json[@"coinType"];
        
        if(txid == nil){
            return;
        }
        if(urlQuery == nil)
            urlQuery = @"";
        if(urlFetch == nil)
            urlFetch = @"";
        if (wallet == nil)
            wallet = @"";
        if (coinType == nil)
            coinType = @"";
        
        data[@"txid"] = txid;
        data[@"text"] = json[@"memo"];
        data[@"packetID"] = txid;
        data[@"unpackID"] = txid;
        data[@"urlQuery"] = urlQuery;
        data[@"urlFetch"] = urlFetch;
        data[@"wallet"] = wallet;
        data[@"coinType"] = coinType;
        data[@"timestamp"] = json[@"timestamp"];
        data[@"to"] = json[@"to"];
        data[@"from"] = json[@"from"];
        data[@"luckNum"] = json[@"luckNum"];
        data[@"unpackID"] = json[@"unpackID"];
        data[@"serial"] = json[@"serial"];
        data[@"balance"] = json[@"balance"];
        data[@"type"] = json[@"type"];
        data[@"greetings"] = json[@"greetings"];
        data[@"count"] = json[@"count"];
        
        DMCCRedPacketMessageContent *content = [DMCCRedPacketMessageContent new];
        content.ids = txid;
        content.info = [OsnUtils dic2Json:data];
        content.text = json[@"memo"];
        content.state = 0;
        [self sendMessage:content];
    }
}

#pragma mark - 消息发送
- (void)sendMessage:(DMCCMessageContent *)content {
    [[DMCCIMService sharedDMCIMService] send:self.conversation content:content pwd:nil success:^(long long messageUid, long long timestamp) {
        [[NSNotificationCenter defaultCenter] postNotificationName:FZ_EVENT_MSGINSERTSTATUS_STATE object:@(messageUid)];
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.navigationController popViewControllerAnimated:YES];
        });
    } error:^(int error_code) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.navigationController popViewControllerAnimated:YES];
        });
    }];
}

- (void)paySign:(id)str {
    NSDictionary *dic = nil;
    
    if ([str isKindOfClass:[NSString class]] ) {
        dic = [OsnUtils json2Dics:str];
    } else if ([str isKindOfClass:[NSDictionary class]] ) {
        dic = str;
    }
    
    NSNumber *serial = dic[@"serial"];
    NSDictionary *data = @{
        @"type" : dic[@"type"],
        @"from" : dic[@"from"],
        @"to" : dic[@"to"],
        @"walletId" : dic[@"walletId"],
        @"balance" : dic[@"balance"],
        @"timestamp" : @([OsnUtils getTimeStamp]),
        @"serial" : [NSString stringWithFormat:@"%ld", [serial longValue] + 1],
        @"remark" : dic[@"remark"],
        @"coin" : dic[@"coin"]
    };
    self.dataDic = data;
}

- (void)paySign2:(NSMutableDictionary *)dic {
    NSString *userId = [[DMCCIMService sharedDMCIMService] getUserID];
    NSString* calc = [NSString stringWithFormat:@"%@%@%@%@%@%@%@%@%@",dic[@"type"],dic[@"from"],dic[@"to"],dic[@"walletId"],dic[@"balance"],dic[@"timestamp"],dic[@"serial"],dic[@"greetings"],dic[@"coin"]];

    NSString* hash = [[DMCCIMService sharedDMCIMService] hashData:[calc dataUsingEncoding:NSUTF8StringEncoding]];
    NSString* sign = [[DMCCIMService sharedDMCIMService] signData:[hash dataUsingEncoding:NSUTF8StringEncoding]];
    
    [dic setObject:sign forKey:@"sign"];
    
    if (self.decPassword.length != 0) {
        NSString *shdkey = [[DMCCIMService sharedDMCIMService] getShadowKey];
        if (shdkey == nil) {
            self.isShowPass = NO;
            [MHAlert showMessage:LocalizedString(@"AlertOpearFail")];
            return;
        }
        NSData *decData = [OsnUtils b64Decode: shdkey];
        NSData* decKey = [OsnUtils sha256:[self.decPassword dataUsingEncoding: NSASCIIStringEncoding]];
        NSData* shadowKeyData = [OsnUtils aesDecrypt:decData keyData:decKey];
        NSString* shadowAddress = [ECUtils genAddress:shadowKeyData];
        NSString* shadowPrivateKeyStr = [ECUtils genPrivateKeyStr:shadowKeyData];
        NSString *pubSign = [ECUtils osnSign:shadowPrivateKeyStr data:[hash dataUsingEncoding:NSUTF8StringEncoding]];
        NSString *pubKey = shadowAddress;
        [dic setObject:pubKey forKey:@"shadowKey"];
        if (pubSign != nil) {
            [dic setObject:pubSign forKey:@"shadowSign"];
        }
    }
    
    if (self.payPassword.length != 0) {
        NSMutableDictionary* json = [NSMutableDictionary new];
        json[@"command"] = @"transfer";
        json[@"from"] = userId;
        NSString* encrypted = [OsnUtils aesEncrypt:[OsnUtils dic2Json:dic] keyStr:self.payPassword];
        json[@"body"] = encrypted;
        NSString* calc = [NSString stringWithFormat:@"%@%@%@",json[@"command"],json[@"from"],json[@"body"]];
        NSString* hash = [[DMCCIMService sharedDMCIMService] hashData:[calc dataUsingEncoding:NSUTF8StringEncoding]];
        NSString* sign = [[DMCCIMService sharedDMCIMService] signData:[hash dataUsingEncoding:NSUTF8StringEncoding]];
        json[@"hash"] = hash;
        json[@"sign"] = sign;
        json[@"language"] = [OSNLanguage getBaseLanguage];
        
        NSString *callback = [NSString stringWithFormat:@"%@(%@)", self.argsDic[@"callback"], [OsnUtils dic2Json:json]];
        NSLog(@"callback: %@", callback);
        WS(ws);
        dispatch_async(dispatch_get_main_queue(), ^{
            [ws.webView evaluateJavaScript:callback completionHandler:^(id _Nullable result, NSError * _Nullable error) {
                NSLog(@"done");
            }];
        });
    } else {
        NSString *callback = [NSString stringWithFormat:@"%@(%@)", self.argsDic[@"callback"], [OsnUtils dic2Json:dic]];
        NSLog(@"callback: %@", callback);
        WS(ws);
        dispatch_async(dispatch_get_main_queue(), ^{
            [ws.webView evaluateJavaScript:callback completionHandler:^(id _Nullable result, NSError * _Nullable error) {
                NSLog(@"done");
            }];
        });
    }
}

- (NSString*)timestamp{
    long timestamp = [OsnUtils getTimeStamp];
    return [NSString stringWithFormat:@"%ld",timestamp];
}

// 输入解密密码
- (void)inputEncPasswordWithNameALert:(NSString *)message withAlerttype:(MHAlertType)alerttype {
    NSString *title;
    switch (alerttype) {
        case MHAlertTypeWarn:
            title = LocalizedString(@"AlertWarn");
            break;
        case MHAlertTypeRemind:
            title = LocalizedString(@"AlertRemind");
            break;
    }
    UIViewController *topVC = [UIApplication sharedApplication].keyWindow.rootViewController;
    if ((topVC.presentedViewController) != nil) {
        topVC = topVC.presentedViewController;
    }
    WS(ws);
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:title message:message preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *ok = [UIAlertAction actionWithTitle:LocalizedString(@"Sure") style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        NSString *inputText = alert.textFields[0].text;
        ws.decPassword = inputText;
        [ws paySign2:[NSMutableDictionary dictionaryWithDictionary:ws.dataDic]];
        if (ws.isShowPass) {
            [ws inputPayPasswordWithNameALert:LocalizedString(@"inputPayPassword") withAlerttype:MHAlertTypeRemind];
        }
    }];
    UIAlertAction *cancel = [UIAlertAction actionWithTitle:LocalizedString(@"Cancel") style:UIAlertActionStyleDefault handler:nil];
    [alert addTextFieldWithConfigurationHandler:^(UITextField * _Nonnull textField) {
        textField.placeholder = message;
        textField.secureTextEntry = YES;
    }];
    [alert addAction:cancel];
    [alert addAction:ok];
    [topVC presentViewController:alert animated:YES completion:nil];
}

// 输入支付密码
    - (void)inputPayPasswordWithNameALert:(NSString *)message withAlerttype:(MHAlertType)alerttype {
    NSString *title;
    switch (alerttype) {
        case MHAlertTypeWarn:
            title = LocalizedString(@"AlertWarn");
            break;
        case MHAlertTypeRemind:
            title = LocalizedString(@"AlertRemind");
            break;
    }
    UIViewController *topVC = [UIApplication sharedApplication].keyWindow.rootViewController;
    if ((topVC.presentedViewController) != nil) {
        topVC = topVC.presentedViewController;
    }
    NSString *amountStr = [NSString stringWithFormat:@"%@%@", LocalizedString(@"ChatRedSum"), self.dataDic[@"balance"]];
    WS(ws);
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:LocalizedString(@"win_MoneyPayLab") message:amountStr preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *ok = [UIAlertAction actionWithTitle:LocalizedString(@"Sure") style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        NSString *inputText = alert.textFields[0].text;
        ws.payPassword = inputText;
        [ws paySign2:[NSMutableDictionary dictionaryWithDictionary:ws.dataDic]];
    }];
    UIAlertAction *cancel = [UIAlertAction actionWithTitle:LocalizedString(@"Cancel") style:UIAlertActionStyleDefault handler:nil];
    [alert addTextFieldWithConfigurationHandler:^(UITextField * _Nonnull textField) {
        textField.placeholder = message;
        textField.secureTextEntry = YES;
    }];
    [alert addAction:cancel];
    [alert addAction:ok];
    [topVC presentViewController:alert animated:YES completion:nil];
}

- (void)setNftImg:(NSString *)img withUrl:(NSString *)url {
    WS(ws);
    [MHAlert showCustomeAlert:LocalizedString(@"SettingNFTImg") withAlerttype:MHAlertTypeWarn withOkBlock:^(UIAlertAction *action) {
        if ([action.title isEqualToString:LocalizedString(@"Sure")]) {
            [[DMCCIMService sharedDMCIMService] modifyMyInfo:@{@(Modify_Portrait) : img} success:^{
                [[DMCCIMService sharedDMCIMService] getUserInfo:[[OsnSDK getInstance] getUserID] refresh:YES success:^(DMCCUserInfo *userInfo) {
                    if (userInfo) {
                        dispatch_async(dispatch_get_main_queue(), ^{
                            [MHAlert showMessage:LocalizedString(@"AlertSettingSucess")];
                            [ws sendDescribesValue:url];
                        });
                    }
                } error:^(int errorCode) {
                    
                }];
            } error:^(int error_code) {
                
            }];
        }
    }];
}

- (void)sendDescribesValue:(NSString *)value {
    [[DMCCIMService sharedDMCIMService] settingDescribesWithValue:value cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        
    }];
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

- (void)addGroup:(NSString *)groupId {
    // 判断群组是否已经存在
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
}

//- (void)addFriend:(NSString *)userId {
//    WS(ws);
//    [MHAlert showLoadingStr:LocalizedString(@"AlertOpreating")];
//    [[DMCCIMService sharedDMCIMService] getUserInfo:userId refresh:true success:^(DMCCUserInfo *userInfo) {
//        [MHAlert dismiss];
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
//
//        [MHAlert dismiss];
//        dispatch_async(dispatch_get_main_queue(), ^{
//            // 判断好友是否已经存在
//            DMCCUserInfo *userInfo = [DMCCUserInfo new];
//            userInfo.userId = userId;
//            if ([[DMCCIMService sharedDMCIMService] isMyFriend:userInfo.userId]) {
//                [ws.navigationController pushViewController:[[ZoloContactDetailVC alloc] initWithUserInfo:userInfo] animated:YES];
//            } else {
//                ZoloAddFriendInfoVC *vc = [[ZoloAddFriendInfoVC alloc] initWithUserInfo:userInfo];
//                [ws.navigationController pushViewController:vc animated:YES];
//            }
//        });
//    }];
//}

//- (void)addGroup:(NSString *)groupId {
//    // 判断群组是否已经存在
//    DMCCConversationInfo * convInfo = [self getConversation:groupId];
//    if (convInfo != nil) {
//        DMCCConversationInfo *info = [DMCCConversationInfo new];
//        DMCCConversation *conversation = [DMCCConversation conversationWithType:Group_Type target:groupId line:0];
//        info.conversation = conversation;
//        [self.navigationController pushViewController:[[ZoloSingleChatVC alloc] initWithConversationInfo:info] animated:YES];
//        return;
//    }
//    
//    // 在这里加个转圈
//    [MHAlert showLoadingStr:LocalizedString(@"AlertOpreating")];
//    [[DMCCIMService sharedDMCIMService] getGroupInfoEx:groupId
//                                               refresh:true
//                                                    cb:^(bool isSuccess, DMCCGroupInfo* groupInfo, NSString *error) {
//        [MHAlert dismiss];
//        if (isSuccess) {
//            if (groupInfo.isMember != nil) {
//                if ([groupInfo.isMember isEqualToString:@"yes"]) {
//                    // entry group
//                    [[DMCCIMService sharedDMCIMService] insertConversation:groupId groupInfo:groupInfo];
//                    DMCCConversation *conversation = [DMCCConversation conversationWithType:Group_Type target:groupId line:0];
//                    DMCCConversationInfo *info = [DMCCConversationInfo new];
//                    info.conversation = conversation;
//                    dispatch_sync(dispatch_get_main_queue(), ^{
//                        [self.navigationController pushViewController:[[ZoloSingleChatVC alloc] initWithConversationInfo:info] animated:YES];
//                    });
//
//                    return;
//                }
//            }
//        } else {
//            groupInfo = [DMCCGroupInfo new];
//            groupInfo.name = groupId;
//            groupInfo.target = groupId;
//            groupInfo.portrait = @"";
//        }
//        
//        dispatch_sync(dispatch_get_main_queue(), ^{
//            ZoloAddGroupInfoVC *vc = [[ZoloAddGroupInfoVC alloc] initWithGroupInfo:groupInfo];
//            [self.navigationController pushViewController:vc animated:YES];
//        });
//        
//    }];
//}

- (DMCCConversationInfo *)getConversation:(NSString *)target {
    DMCCConversation *conversation = [DMCCConversation conversationWithType:Group_Type target:target line:0];
    DMCCConversationInfo *info = [[DMCCIMService sharedDMCIMService] getConversationInfo:conversation];
    return info;
}

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
