//
//  ZoloAccoutSecityVC.m
//  NewZolo
//
//  Created by JTalking on 2022/9/15.
//

#import "ZoloAccoutSecityVC.h"
#import "AppDelegate.h"
#import "ZoloDeleteVC.h"
#import "ZoloSetPasswordVC.h"
#import "ZoloSetPwdVC.h"

@interface ZoloAccoutSecityVC ()

@property (weak, nonatomic) IBOutlet UIView *acountView;
@property (weak, nonatomic) IBOutlet UIView *passView;

@property (weak, nonatomic) IBOutlet UILabel *acountLab;
@property (weak, nonatomic) IBOutlet UILabel *passwordLab;

@end

@implementation ZoloAccoutSecityVC

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    
    self.navigationItem.title = LocalizedString(@"AccountSecurity");
    self.view.backgroundColor = [FontManage MHGrayColor];

    self.acountView.backgroundColor = [FontManage MHWhiteColor];
    self.passView.backgroundColor = [FontManage MHWhiteColor];
    
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(settingPassword)];
    [self.passView addGestureRecognizer:tap];
    
    UITapGestureRecognizer *tap1 = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(deleteAcount)];
    [self.acountView addGestureRecognizer:tap1];
    
    self.passwordLab.text = LocalizedString(@"SettingPassword");
    self.acountLab.text = LocalizedString(@"DeleteAcount");
    
}

- (void)deleteAcount {
    [self.navigationController pushViewController:[ZoloDeleteVC new] animated:YES];
}

- (void)settingPassword {
    
    NSString *current_account = [[NSUserDefaults standardUserDefaults]objectForKey:@"mne_account_current"];
    
    if (current_account) {
        [self.navigationController pushViewController:[[ZoloSetPwdVC alloc] initWithUserAcount:current_account] animated:YES];
    } else {
        WS(ws);
        [MHAlert showLoadingStr:LocalizedString(@"loadLoading")];
        [[ZoloAPIManager instanceManager] getUserInfoWithCompleteBlock:^(NSString * _Nonnull data) {
            [MHAlert dismiss];
            if (data) {
                [ws.navigationController pushViewController:[[ZoloSetPasswordVC alloc] initWithUserAcount:data] animated:YES];
            }
        }];
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
