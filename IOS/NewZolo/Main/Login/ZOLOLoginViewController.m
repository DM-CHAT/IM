//
//  ZOLOLoginViewController.m
//  NewZolo
//
//  Created by 陈泽萱 on 2022/6/6.
//

#import "ZOLOLoginViewController.h"
#import <WebKit/WebKit.h>
#import "MHTabBarVC.h"
#import <config/config.h>
#import <osnsdk/osnsdk.h>
#import "MHLoginModel.h"
#import <JPUSHService.h>
#import <YYModel.h>
#import "ZoloInfoManager.h"
#import "ZoloCreateWalletVC.h"

@interface ZOLOLoginViewController ()<WKScriptMessageHandler>

@property (strong, nonatomic) WKWebView* webView;
@property (nonatomic, strong) NSDictionary *logDic;

@property (nonatomic, copy) NSString *url;

@end

@implementation ZOLOLoginViewController

- (instancetype)initWithUrlInfo:(NSString *)url {
    if (self = [super init]) {
        self.url = url;
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
    [MHAlert showLoadingStr:LocalizedString(@"loginLoading")];
    _webView = [[WKWebView alloc] initWithFrame:CGRectMake(0, 20 + iPhoneX_topH, kScreenwidth, KScreenheight) configuration:configuration];
    [self.view addSubview:_webView];
    [_webView addObserver:self forKeyPath:@"estimatedProgress" options:NSKeyValueObservingOptionNew context:nil];
    
    NSString *language = [[NSUserDefaults standardUserDefaults]objectForKey:@"ospn_language"];
    
    if(strstr(_url.UTF8String, "?")){
        _url = [_url stringByAppendingString:@"&"];
    }else{
        _url = [_url stringByAppendingString:@"?"];
    }
    _url = [_url stringByAppendingFormat:@"language=%@&device=%@",language, [MHHelperUtils getServerKey]];
    
    [_webView loadRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:self.url]]];
}

- (void)userContentController:(WKUserContentController *)userContentController didReceiveScriptMessage:(WKScriptMessage *)message {
    NSLog(@"args: %@",message.body);
    NSDictionary* args = [OsnUtils json2Dics:message.body];
    NSString* command = args[@"command"];
    if(command == nil)
        return;
    if([command isEqualToString:@"Login"])
        [self doLogin:args];
    if([command isEqualToString:@"MnemonicMode"]) {
        [self.navigationController pushViewController:[[ZoloCreateWalletVC alloc] initWithLoginUrl:args[@"url"]] animated:NO];
    }
}

- (void)doLogin:(NSDictionary *)data {
    NSString* username = data[@"username"];
    NSString* password = data[@"password"];
    NSString* ip = data[@"ip"];
    NSString* token = data[@"token"];
    id language = data[@"language"];
    if ([language isKindOfClass:[NSString class]]) {
        NSString * lang = (NSString *)language;
        if (lang.length == 0) {
            language = @"0";
        } else {
            language = lang;
        }
    } else {
        language = ((NSNumber*)data[@"language"]).stringValue;
    }

    NSString *oip = [[NSUserDefaults standardUserDefaults] objectForKey:@"ospn_hostip"];
    [[NSUserDefaults standardUserDefaults] setValue:ip forKey:@"ospn_hostip"];
    
    
    
    
    // 这里加一个弹出输入密码框，让用户输入解密密码
    NSArray *temp=[password componentsSeparatedByString:@"-"];
    if (temp != nil && temp.count == 3) {
        if (!strncmp(username.UTF8String, "OSN", 3)) {
            self.logDic = data;
            [self showPasswordALert:oip ip:ip username:username password:password token:token language:language data:data];
            return;
        }
    }
    
    
    
    
    
    
    
    

    __block bool loginResult = false;
    dispatch_semaphore_t lock = dispatch_semaphore_create(0);
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        if(![oip isEqualToString:ip]){
            [[DMCCNetworkService sharedInstance] setHost:ip];
            NSLog(@"reset hostIp: %@",ip);
        }
        long timestamp = [OsnUtils getTimeStamp]+5000;
        do{
            [NSThread sleepForTimeInterval:0.1];
        }while([[DMCCNetworkService sharedInstance] getConnectionStatus] != kConnectionStatusConnected
               && timestamp > [OsnUtils getTimeStamp]);
        loginResult = [[DMCCNetworkService sharedInstance] connect:username token:password password:nil];
        dispatch_semaphore_signal(lock);
    });

    dispatch_semaphore_wait(lock, dispatch_time(DISPATCH_TIME_NOW,NSEC_PER_SEC*15));
    if (loginResult) {
        NSString* userID = [[DMCCIMService sharedDMCIMService] getUserID];
        [[NSUserDefaults standardUserDefaults] setValue:userID forKey:@"ospn_id"];
        [[NSUserDefaults standardUserDefaults] setValue:token forKey:@"ospn_token"];
        [[NSUserDefaults standardUserDefaults] setValue:language forKey:@"ospn_language"];
        MHLoginModel *model = [MHLoginModel yy_modelWithJSON:data];
        [[ZoloInfoManager sharedUserManager] setCurrentInfo:model];
        [OSNLanguage setLanguage:language];
        NSLog(@"===push===%@", [JPUSHService registrationID]);
        MHTabBarVC * mainVc = [[MHTabBarVC alloc]init];
        [UIApplication sharedApplication].keyWindow.rootViewController = mainVc;
        [self getTagData];
    } else {
        
        NSString* hostIp = [[NSUserDefaults standardUserDefaults] objectForKey:@"ospn_hostip"];
        if(hostIp == nil){
            hostIp = @"8.219.11.57";
            [[NSUserDefaults standardUserDefaults] setValue:hostIp forKey:@"ospn_hostip"];
        }

        [[DMCCNetworkService sharedInstance] setServerAddress:hostIp];
        [[DMCCNetworkService sharedInstance] connect:nil token:nil password:nil];
        
    }
    
}

- (void)showPasswordALert:(NSString*)oip
                       ip:(NSString *)ip
                 username:(NSString *)username
                 password:(NSString *)password
                    token:(NSString *)token
                 language:(id)language
                     data:(NSDictionary *)data
{
    NSString *title = @"Password";

    UIViewController *topVC = [UIApplication sharedApplication].keyWindow.rootViewController;
    if ((topVC.presentedViewController) != nil) {
        topVC = topVC.presentedViewController;
    }
    WS(ws);
    NSString * message = @"Input decrypt password";
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:title message:message preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *ok = [UIAlertAction actionWithTitle:LocalizedString(@"Sure") style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        NSString *pwd = alert.textFields[0].text;
        
        
        
        
        if (pwd.length > 20) {
            [MHAlert showMessage:LocalizedString(@"NotWords")];
            return;
        }
        
        
        __block bool loginResult = false;
        dispatch_semaphore_t lock = dispatch_semaphore_create(0);
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
            if(![oip isEqualToString:ip]){
                [[DMCCNetworkService sharedInstance] setHost:ip];
                NSLog(@"reset hostIp: %@",ip);
            }
            long timestamp = [OsnUtils getTimeStamp]+5000;
            do{
                [NSThread sleepForTimeInterval:0.1];
            }while([[DMCCNetworkService sharedInstance] getConnectionStatus] != kConnectionStatusConnected
                   && timestamp > [OsnUtils getTimeStamp]);
            loginResult = [[DMCCNetworkService sharedInstance] connect:username token:password password:pwd];
            dispatch_semaphore_signal(lock);
        });

        dispatch_semaphore_wait(lock, dispatch_time(DISPATCH_TIME_NOW,NSEC_PER_SEC*15));
        if (loginResult) {
            NSString* userID = [[DMCCIMService sharedDMCIMService] getUserID];
            [[NSUserDefaults standardUserDefaults] setValue:userID forKey:@"ospn_id"];
            [[NSUserDefaults standardUserDefaults] setValue:token forKey:@"ospn_token"];
            [[NSUserDefaults standardUserDefaults] setValue:language forKey:@"ospn_language"];
            MHLoginModel *model = [MHLoginModel yy_modelWithJSON:data];
            [[ZoloInfoManager sharedUserManager] setCurrentInfo:model];
            [OSNLanguage setLanguage:language];
            NSLog(@"===push===%@", [JPUSHService registrationID]);
            MHTabBarVC * mainVc = [[MHTabBarVC alloc]init];
            [UIApplication sharedApplication].keyWindow.rootViewController = mainVc;
            [self getTagData];
        } else {
        }
        
        self.logDic = data;
        
        
        
        
        
        
        /*
        DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:ws.conversation.target refresh:NO];
        [[DMCCIMService sharedDMCIMService] modifyGroupInfo:group.target type:Modify_Group_Name newValue:alert.textFields[0].text notifyLines:@[@(0)] notifyContent:nil success:^{
            [MHAlert showMessage:LocalizedString(@"AlertSuccess")];
            dispatch_async(dispatch_get_main_queue(), ^{
                [ws.navigationController popViewControllerAnimated:YES];
            });
        } error:^(int error_code) {
            [MHAlert showMessage:LocalizedString(@"AlertSettingFail")];
        }];*/
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
    NSLog(@"==ZOLOLoginViewController===dealloc==");
    [self.webView removeObserver:self forKeyPath:@"estimatedProgress"];
}

- (void)getTagData {
    [[ZoloAPIManager instanceManager] addTagNameListWithCompleteBlock:^(NSArray * _Nonnull data) {
        
    }];
}


@end
