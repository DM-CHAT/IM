//
//  ViewController.m
//  Calendar
//
//  Created by Calendar on 2022/7/14.
//

#import "ZoloFindViewController.h"
#import <WebKit/WebKit.h>
#import "ZoloContactDetailVC.h"
#import "ZoloAddFriendInfoVC.h"
#import "ZoloAddGroupInfoVC.h"
#import "ZoloSingleChatVC.h"
#import "ZoloDappViewController.h"
#import "ZoloShareDappVC.h"
#import "ZoloAddWalletVC.h"
#import "ZoloCardContactVC.h"

@interface ZoloFindViewController () <WKNavigationDelegate, WKScriptMessageHandler>

@property (strong, nonatomic) WKWebView *webView;
@property (weak, nonatomic) IBOutlet UIView *subView;
@property (nonatomic, strong) DMCCLitappInfo *appInfo;
@property (nonatomic, copy) NSString *decPassword;
@property (nonatomic, copy) NSString *payPassword;
@property (nonatomic, strong) NSDictionary *dataDic;
@property (nonatomic, strong) NSDictionary *argsDic;
@property (nonatomic, assign) BOOL isShowPass;

@end

@implementation ZoloFindViewController

- (instancetype)initWithLitappInfo:(DMCCLitappInfo *)appInfo {
    if (self = [super init]) {
        self.appInfo = appInfo;
    }
    return self;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self.navigationController setNavigationBarHidden:YES animated:animated];
}


- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [self.navigationController setNavigationBarHidden:NO animated:animated];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.navigationItem.title = self.appInfo.name;
    [self setUpWebView];
    self.navigationItem.title = LocalizedString(@"Tabfind");
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

    WKWebViewConfiguration *configuration = [[WKWebViewConfiguration alloc] init];
    configuration.userContentController = userContentController;
            
    _webView = [[WKWebView alloc] initWithFrame:CGRectMake(0, 0, KScreenWidth, KScreenHeight - iPhoneX_bottomH - 64) configuration:configuration];
    
    NSString *url = self.appInfo.url;
    NSString *language = [[NSUserDefaults standardUserDefaults]objectForKey:@"ospn_language"];
    
    if(strstr(url.UTF8String, "?")){
        url = [url stringByAppendingString:@"&"];
    }else{
        url = [url stringByAppendingString:@"?"];
    }
    url = [url stringByAppendingFormat:@"loginType=%@&language=%@", @"osn", language];
    
        [_webView loadRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:url]]];
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
    NSString* command = args[@"command"];
    if(command == nil)
        return;
    if([command isEqualToString:@"GoBack"])
        [self dismissViewControllerAnimated:YES completion:nil];
    else if([command isEqualToString:@"Login"]) {
        WS(ws);
        [[DMCCIMService sharedDMCIMService] loginLitappWithInfo:self.appInfo url:args[@"url"] success:^(NSMutableDictionary *json) {
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

        }];
    } else if ([command isEqualToString:@"AddFriend"]) {
        [self addFriend:args[@"userID"]];
    }  else if ([command isEqualToString:@"GroupInfo"]) {
        [self addGroup:args[@"osnID"]];
    } else if ([command isEqualToString:@"SetNft"]) {
        [self setNftImg:args[@"portrait"] withUrl:args[@"url"]];
    }  else if ([command isEqualToString:@"OpenDapp"]) {
        
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
    } else if ([command isEqualToString:@"AddWallet"]) {
        NSString *str = args[@"DappInfo"];
        NSString *content = [str stringByReplacingOccurrencesOfString:@"\\\\" withString:@""];
        NSDictionary *dic = [OsnUtils json2Dics:content.length > 0 ? content : str];
        DMCCLitappInfo *litapp = [DMCCLitappInfo new];
        litapp.displayName = dic[@"displayName"];
        litapp.portrait = dic[@"portrait"];
        litapp.target = dic[@"target"];
        litapp.info = dic[@"info"];
        litapp.param = dic[@"param"];
        litapp.name = dic[@"name"];
        litapp.url = dic[@"url"];
        ZoloAddWalletVC *vc = [[ZoloAddWalletVC alloc] initWithLitappInfo:litapp];
        vc.modalPresentationStyle = UIModalPresentationFullScreen;
        [self presentViewController:vc animated:YES completion:nil];
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
    } else if ([command isEqualToString:@"AddObj"]) {
        ZoloCardContactVC *vc = [ZoloCardContactVC new];
        WS(ws);
        vc.cardReturenBlock = ^(DMCCUserInfo * _Nonnull info) {
            [ws cardReturnBlock:info args:args];
        };
        vc.cardGroupReturnBlock = ^(DMCCGroupInfo * _Nonnull groupInfo) {
            [ws cardReturnBlock:groupInfo args:args];
        };
        vc.cardLitAppReturnBlock = ^(DMCCLitappInfo * _Nonnull litAppInfo) {
            [ws cardReturnBlock:litAppInfo args:args];
        };
        MHNavigationVC *nav = [[MHNavigationVC alloc] initWithRootViewController:vc];
        nav.modalPresentationStyle = UIModalPresentationFullScreen;
        [self presentViewController:nav animated:YES completion:nil];
    }
}

// 名片处理
- (void)cardReturnBlock:(id)info args:(NSDictionary*) args {
    NSMutableDictionary *dic = [NSMutableDictionary new];
    NSString *type = @"";
    NSObject *jsonStr = @"";
    if ([info isKindOfClass:[DMCCGroupInfo class]]) {
        DMCCGroupInfo *group = (DMCCGroupInfo *)info;
        NSMutableDictionary *groupDic = [NSMutableDictionary new];
        groupDic[@"groupID"] = group.target;
        groupDic[@"name"] = group.name;
        groupDic[@"portrait"] = group.portrait;
        groupDic[@"owner"] = group.owner;
        groupDic[@"memberCount"] = @(group.memberCount);
        groupDic[@"attribute"] = group.attribute;
        
        type = @"group";
        jsonStr = [groupDic yy_modelToJSONObject];
        
    } else if ([info isKindOfClass:[DMCCLitappInfo class]]) {
        DMCCLitappInfo *litapp = (DMCCLitappInfo *)info;
        NSMutableDictionary *dappDic = [NSMutableDictionary new];
        dappDic[@"target"] = litapp.target;
        dappDic[@"name"] = litapp.name;
        dappDic[@"displayName"] = litapp.displayName;
        dappDic[@"portrait"] = litapp.portrait;
        dappDic[@"theme"] = litapp.theme;
        dappDic[@"url"] = litapp.url;
        dappDic[@"info"] = litapp.info;
        dappDic[@"param"] = litapp.param;
        dappDic[@"themeUrl"] = litapp.theme;
    
        type = @"dapp";
        jsonStr = [dappDic yy_modelToJSONObject];
    }

    dic[@"type"] = type;
    dic[@"data"] = jsonStr;
    
    NSString *jsonSting = [OsnUtils dic2Json:dic];
    NSString *callback = [NSString stringWithFormat:@"javascript:%@(%@)", args[@"callback"], jsonSting];
    NSLog(@"callback: %@", callback);
    WS(ws);
    dispatch_async(dispatch_get_main_queue(), ^{
        [ws.webView evaluateJavaScript:callback completionHandler:^(id _Nullable result, NSError * _Nullable error) {
            NSLog(@"done");
        }];
    });
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

//- (void)addGroup:(NSString *)groupId {
//    // 判断群组是否已经存在 添加判断，是否有群组成员
//    DMCCConversationInfo* convInfo = [self getConversation:groupId];
//    if (convInfo != nil) {
//        // 只要有conversation直接进入
//        DMCCConversationInfo *info = [DMCCConversationInfo new];
//        DMCCConversation *conversation = [DMCCConversation conversationWithType:Group_Type target:groupId line:0];
//        info.conversation = conversation;
//        [self.navigationController pushViewController:[[ZoloSingleChatVC alloc] initWithConversationInfo:info] animated:YES];
//        return;
//    }
//
//    [MHAlert showLoadingStr:LocalizedString(@"AlertOpreating")];
//    // 在这里加个转圈
//    [[DMCCIMService sharedDMCIMService] getGroupInfoEx:groupId
//                                               refresh:true
//                                                    cb:^(bool isSuccess, DMCCGroupInfo* groupInfo, NSString *error) {
//        [MHAlert dismiss];
//        if (groupInfo == nil) {
//            groupInfo = [DMCCGroupInfo new];
//            groupInfo.name = @"";
//            groupInfo.target = groupId;
//            groupInfo.portrait = @"";
//        }
//        if (groupInfo.isMember !=nil) {
//            if ([groupInfo.isMember isEqualToString:@"yes"]) {
//                [[DMCCIMService sharedDMCIMService] insertConversation:groupId groupInfo:groupInfo];
//                DMCCConversation *conversation = [DMCCConversation conversationWithType:Group_Type target:groupId line:0];
//                DMCCConversationInfo *info = [DMCCConversationInfo new];
//                info.conversation = conversation;
//
//                dispatch_sync(dispatch_get_main_queue(), ^{
//                    [self.navigationController pushViewController:[[ZoloSingleChatVC alloc] initWithConversationInfo:info] animated:YES];
//                });
//
//
//                return;
//            }
//        }
//
//        dispatch_sync(dispatch_get_main_queue(), ^{
//            ZoloAddGroupInfoVC *vc = [[ZoloAddGroupInfoVC alloc] initWithGroupInfo:groupInfo];
//            [self.navigationController pushViewController:vc animated:YES];
//        });
//
//
//
//
//    }];
//    //DMCCGroupInfo *groupInfo = [[DMCCIMService sharedDMCIMService] getGroupInfoEx:groupId refresh:true];
//    //[MHAlert dismiss];
//
//
//
//}

- (DMCCConversationInfo *)getConversation:(NSString *)target {
    DMCCConversation *conversation = [DMCCConversation conversationWithType:Group_Type target:target line:0];
    DMCCConversationInfo *info = [[DMCCIMService sharedDMCIMService] getConversationInfo:conversation];
    return info;
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

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
