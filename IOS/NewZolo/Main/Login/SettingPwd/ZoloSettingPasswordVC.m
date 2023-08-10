//
//  ZoloSettingPasswordVC.m
//  NewZolo
//
//  Created by MHHY on 2023/5/19.
//

#import "ZoloSettingPasswordVC.h"
#include <CommonCrypto/CommonKeyDerivation.h>

#import "MHTabBarVC.h"
#import <config/config.h>
#import <osnsdk/osnsdk.h>
#import "MHLoginModel.h"
#import <JPUSHService.h>
#import <YYModel.h>
#import "ZoloInfoManager.h"

@interface ZoloSettingPasswordVC ()

@property (weak, nonatomic) IBOutlet UILabel *accountTitle;
@property (weak, nonatomic) IBOutlet UITextField *accountTextField;
@property (weak, nonatomic) IBOutlet UILabel *pwdTitle;
@property (weak, nonatomic) IBOutlet UITextField *passwordTextField;
@property (weak, nonatomic) IBOutlet UILabel *confirmTitle;
@property (weak, nonatomic) IBOutlet UITextField *confirmTextField;
@property (weak, nonatomic) IBOutlet UIButton *registerBtn;
@property (nonatomic, copy) NSString *mnemonicString;
@property (nonatomic, copy) NSString *url;
@property (nonatomic, strong) NSDictionary *logDic;

@end

@implementation ZoloSettingPasswordVC

- (instancetype)initWithWords:(NSString *)words loginUrl:(NSString *)url {
    if (self = [super init]) {
        self.mnemonicString = words;
        self.url = url;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    
    self.passwordTextField.layer.cornerRadius = 8;
    self.view.backgroundColor = MHColorFromHex(0x23262B);
    self.registerBtn.layer.cornerRadius = 15;
}

-(void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    [self.view endEditing:YES];
}

- (IBAction)registerBtnClick:(id)sender {
    
    if (self.accountTextField.text.length == 0) {
        [MHAlert showMessage:[NSString stringWithFormat:@"%@", LocalizedString(@"InputAccnout")]];
        return;
    }
    
    NSDictionary *mne_account = [[NSUserDefaults standardUserDefaults] objectForKey:@"mne_account"];
    if (mne_account) {
        NSArray *array = mne_account.allKeys;
        BOOL isAcount = NO;
        for (NSString *str in array) {
            if ([str isEqualToString:self.accountTextField.text]) {
                isAcount = YES;
                break;
            }
        }
        if (isAcount) {
            [MHAlert showMessage:[NSString stringWithFormat:@"%@", LocalizedString(@"AccountRepeat")]];
            return;
        }
    }
  
    if (self.passwordTextField.text.length == 0) {
        [MHAlert showMessage:[NSString stringWithFormat:@"%@", LocalizedString(@"InputSetingPwd")]];
        return;
    }
    
    if (self.confirmTextField.text.length == 0) {
        [MHAlert showMessage:[NSString stringWithFormat:@"%@", LocalizedString(@"InputConfirmPwd")]];
        return;
    }
    
    if (![self.passwordTextField.text isEqualToString:self.confirmTextField.text]) {
        [MHAlert showMessage:LocalizedString(@"PlearSuccessPassword")];
        return;
    }
    
    NSData* data = [self seedForWords:self.mnemonicString password:@""];
    NSArray *array = [ECUtils createOsnIdFromMnemonic:data password:self.passwordTextField.text];
    WS(ws);
    [[ZoloAPIManager instanceManager] bip39LoginWithUserString:array.firstObject withUrl:self.url WithCompleteBlock:^(NSDictionary * _Nonnull data) {
        if (data) {
            NSString *username = array[0];
            NSString *password = [NSString stringWithFormat:@"VER2-%@-%@", array[1], array[2]];
            [ws doLogin:data username:username pwd:password inputPwd:self.passwordTextField.text];
            [self saveWords:self.mnemonicString withAccount:self.accountTextField.text pwd:self.passwordTextField.text];
        }
    }];
}

- (void)saveWords:(NSString *)word withAccount:(NSString *)accont pwd:(NSString *)pwd {
     NSString *data = [OsnUtils aesEncrypt:word keyStr:pwd];
    
    NSDictionary *mne_account = [[NSUserDefaults standardUserDefaults] objectForKey:@"mne_account"];
    if (mne_account) {
        NSMutableDictionary *dic = [[NSMutableDictionary alloc] initWithDictionary:mne_account];
        [dic setValue:data forKey:accont];
        [[NSUserDefaults standardUserDefaults] setValue:dic forKey:@"mne_account"];
    } else {
        NSMutableDictionary *dic = [NSMutableDictionary new];
        [dic setValue:data forKey:accont];
        [[NSUserDefaults standardUserDefaults] setValue:dic forKey:@"mne_account"];
    }
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
       [[NSUserDefaults standardUserDefaults] setValue:language forKey:@"ospn_language"];
       [[NSUserDefaults standardUserDefaults] setValue:self.accountTextField.text forKey:@"mne_account_current"];
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

- (void)getTagData {
    [[ZoloAPIManager instanceManager] addTagNameListWithCompleteBlock:^(NSArray * _Nonnull data) {
        
    }];
}

- (IBAction)backBtnClick:(id)sender {
    
    [self.navigationController popViewControllerAnimated:YES];
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
