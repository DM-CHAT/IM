//
//  ZoloAccoutSecityVC.m
//  NewZolo
//
//  Created by JTalking on 2022/9/15.
//

#import "ZoloBipAccoutSecityVC.h"
#import "AppDelegate.h"
#import "ZoloDeleteVC.h"
#import "ZoloSetPasswordVC.h"
#import "ZoloShowBipWordVC.h"
#import "ZoloSetPwdVC.h"

@interface ZoloBipAccoutSecityVC ()

@property (weak, nonatomic) IBOutlet UIView *acountView;
@property (weak, nonatomic) IBOutlet UIView *passView;
@property (weak, nonatomic) IBOutlet UIView *showView;

@property (weak, nonatomic) IBOutlet UILabel *acountLab;
@property (weak, nonatomic) IBOutlet UILabel *passwordLab;
@property (weak, nonatomic) IBOutlet UILabel *showLab;

@end

@implementation ZoloBipAccoutSecityVC

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    
    self.navigationItem.title = LocalizedString(@"AccountSecurity");
    self.view.backgroundColor = [FontManage MHGrayColor];

    self.acountView.backgroundColor = [FontManage MHWhiteColor];
    self.passView.backgroundColor = [FontManage MHWhiteColor];
    self.showView.backgroundColor = [FontManage MHWhiteColor];
    
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(settingPassword)];
    [self.passView addGestureRecognizer:tap];
    
    UITapGestureRecognizer *tap1 = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(deleteAcount)];
    [self.acountView addGestureRecognizer:tap1];
    
    UITapGestureRecognizer *tap2 = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(showAcount)];
    [self.showView addGestureRecognizer:tap2];
    
    self.passwordLab.text = LocalizedString(@"SettingPassword");
    self.acountLab.text = LocalizedString(@"DeleteAcount");
    self.showLab.text = LocalizedString(@"ShowBipWord");
    
}

- (void)showAcount {
    NSString *current_account = [[NSUserDefaults standardUserDefaults]objectForKey:@"mne_account_current"];
    NSDictionary *mne_account = [[NSUserDefaults standardUserDefaults] objectForKey:@"mne_account"];

    NSString *memnemonicString = mne_account[current_account];
    
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
                [self.navigationController pushViewController:[[ZoloShowBipWordVC alloc] initWithWords:decrypt] animated:YES];
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
                

- (void)deleteAcount {
    [self.navigationController pushViewController:[ZoloDeleteVC new] animated:YES];
}

- (void)settingPassword {
    NSString *current_account = [[NSUserDefaults standardUserDefaults]objectForKey:@"mne_account_current"];
    [self.navigationController pushViewController:[[ZoloSetPwdVC alloc] initWithUserAcount:current_account] animated:YES];
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
