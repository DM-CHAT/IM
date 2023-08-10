//
//  ZoloDeleteVC.m
//  NewZolo
//
//  Created by JTalking on 2022/9/15.
//

#import "ZoloDeleteVC.h"
#import "AppDelegate.h"
#import "ZoloAccoutSecityVC.h"

@interface ZoloDeleteVC ()

@property (weak, nonatomic) IBOutlet UIButton *deleteBtn;

@end

@implementation ZoloDeleteVC

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    
    self.navigationItem.title = LocalizedString(@"DeleteAcount");
    self.view.backgroundColor = [FontManage MHGrayColor];
    self.deleteBtn.backgroundColor = [FontManage MHWhiteColor];
    [self.deleteBtn setTitle:LocalizedString(@"DeleteAcount") forState:UIControlStateNormal];
  
}

- (IBAction)deleteBtnClick:(id)sender {
    WS(ws);
    NSString *str = LocalizedString(@"AccoutDeleteTag");
    [MHAlert showCustomeAlert:str withAlerttype:MHAlertTypeWarn withOkBlock:^(UIAlertAction *action) {
        if ([action.title isEqualToString:LocalizedString(@"Sure")]) {
            [ws deleteUser];
        }
    }];
}

- (void)deleteUser {
    [[ZoloAPIManager instanceManager] deleteUserWithCompleteBlock:^(BOOL isSuccess) {
        if (isSuccess) {
            [[NSUserDefaults standardUserDefaults] removeObjectForKey:@"ospn_token"];
            [[NSUserDefaults standardUserDefaults] removeObjectForKey:@"ospn_id"];
            [[NSUserDefaults standardUserDefaults] removeObjectForKey:@"mne_account_current"];
            [[ZoloInfoManager sharedUserManager] loginOut];
            [[DMCCNetworkService sharedInstance] disconnect:YES clearSession:YES];
            AppDelegate *appDelegate = (AppDelegate *)[UIApplication sharedApplication].delegate;
            [appDelegate setRootView];
        }
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
