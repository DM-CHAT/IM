//
//  ZoloAcountLoginVC.m
//  NewZolo
//
//  Created by MHHY on 2023/5/20.
//

#import "ZoloAcountLoginVC.h"
#import "ZoloAcountLoginCell.h"
#import "MHTabBarVC.h"
#import <config/config.h>
#import <osnsdk/osnsdk.h>
#import "MHLoginModel.h"
#import <JPUSHService.h>
#import <YYModel.h>
#import "ZoloInfoManager.h"
#include <CommonCrypto/CommonKeyDerivation.h>

@interface ZoloAcountLoginVC ()

@property (nonatomic, strong) NSArray *keyArray;
@property (nonatomic, strong) NSDictionary *accountDic;
@property (nonatomic, strong) UIButton *addBtn;
@property (nonatomic, strong) NSDictionary *logDic;
@property (nonatomic, copy) NSString *currentAcount;
@property (nonatomic, copy) NSString *loginUrl;

@end

@implementation ZoloAcountLoginVC

- (instancetype)initWithLoginUrl:(NSString *)url {
    if (self = [super init]) {
        self.loginUrl = url;
    }
    return self;
}

- (UIButton *)addBtn {
    if (!_addBtn) {
        _addBtn = [[UIButton alloc] initWithFrame:CGRectMake(10, 20 + iPhoneX_topH, 50, 50)];
        [_addBtn setImage:[UIImage imageNamed:@"Back_b1"] forState:UIControlStateNormal];
        _addBtn.userInteractionEnabled = YES;
        [_addBtn addTarget:self action:@selector(addBtnClick) forControlEvents:UIControlEventTouchUpInside];
        [self.view addSubview:_addBtn];
    }
    return _addBtn;
}

- (void)addBtnClick {
    [self.navigationController popViewControllerAnimated:YES];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    self.view.backgroundColor = MHColorFromHex(0x23262B);
    [self addBtn];
    self.tableView.y = 70 + iPhoneX_topH;
    [self registerCellWithNibName:NSStringFromClass([ZoloAcountLoginCell class]) isTableview:YES];
    NSDictionary *mne_account = [[NSUserDefaults standardUserDefaults] objectForKey:@"mne_account"];
    self.accountDic = mne_account;
    self.keyArray = mne_account.allKeys;
    self.tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
    [self.tableView reloadData];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.keyArray.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ZoloAcountLoginCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloAcountLoginCell class])];
    NSString *str = self.keyArray[indexPath.row];
    cell.nameLab.text = str;
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    
    NSString *str = self.keyArray[indexPath.row];
    NSString *memnemonicString = self.accountDic[str];
    self.currentAcount = str;
    
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
        if (pwd.length > 0) {
            NSString *decrypt = [OsnUtils aesDecrypt:memnemonicString keyStr:pwd];
            if (decrypt.length > 0) {
                [ws userPassword:pwd mnemonicString:decrypt];
            } else {
                [MHAlert showMessage:LocalizedString(@"PlearSuccessPassword")];
            }
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

- (void)userPassword:(NSString *)pwd mnemonicString:(NSString *)mnemonicString {
    NSData* data = [self seedForWords:mnemonicString password:@""];
    NSArray *array = [ECUtils createOsnIdFromMnemonic:data password:pwd];
    WS(ws);
    [[ZoloAPIManager instanceManager] bip39LoginWithUserString:array.firstObject withUrl:self.loginUrl WithCompleteBlock:^(NSDictionary * _Nonnull data) {
        if (data) {
            NSString *username = array[0];
            NSString *password = [NSString stringWithFormat:@"VER2-%@-%@", array[1], array[2]];
            [ws doLogin:data username:username pwd:password inputPwd:pwd];
        }
    }];
}

- (void)doLogin:(NSDictionary *)data username:(NSString *)usrname pwd:(NSString *)pwd inputPwd:(NSString *)inputPwd {
    NSString* username = usrname;
    NSString* password = pwd;
    NSString* ip = data[@"osn_node"];
    NSString* token = data[@"token"];
    id language = @"0";
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
            [self showPasswordALert:oip ip:ip username:username password:password token:token language:language data:data inpoutPwd:inputPwd];
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
        [[NSUserDefaults standardUserDefaults] setValue:self.currentAcount forKey:@"mne_account_current"];
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

- (NSData*) seedForWords:(NSString*)words password:(NSString*)password {
    password = password ?: @"";

    NSData* mnemonic = [words dataUsingEncoding:NSUTF8StringEncoding];
    NSData* salt = [[@"mnemonic" stringByAppendingString:password] dataUsingEncoding:NSUTF8StringEncoding];

    const NSUInteger seedLength = 64;
    NSMutableData* seed = [NSMutableData dataWithLength:seedLength];

    CCKeyDerivationPBKDF(kCCPBKDF2,
                         mnemonic.bytes,
                         mnemonic.length,
                         salt.bytes,
                         salt.length,
                         kCCPRFHmacAlgSHA512,
                         2048,
                         seed.mutableBytes,
                         seedLength);

    return seed;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return self.scaleHeight(54);
}

- (void)showPasswordALert:(NSString*)oip
                       ip:(NSString *)ip
                 username:(NSString *)username
                 password:(NSString *)password
                    token:(NSString *)token
                 language:(id)language
                     data:(NSDictionary *)data
                inpoutPwd:(NSString *)pwd
{
    NSString *title = @"Password";

    UIViewController *topVC = [UIApplication sharedApplication].keyWindow.rootViewController;
    if ((topVC.presentedViewController) != nil) {
        topVC = topVC.presentedViewController;
    }
    WS(ws);

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
        [[NSUserDefaults standardUserDefaults] setValue:self.currentAcount forKey:@"mne_account_current"];
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
}

- (void)getTagData {
    [[ZoloAPIManager instanceManager] addTagNameListWithCompleteBlock:^(NSArray * _Nonnull data) {
        
    }];
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
