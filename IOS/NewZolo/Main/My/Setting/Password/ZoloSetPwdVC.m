//
//  ZoloSetPasswordVC.m
//  NewZolo
//
//  Created by MHHY on 2023/3/8.
//

#import "ZoloSetPwdVC.h"

@interface ZoloSetPwdVC (){
    int _applyCodeTimeLeft;
}

@property (weak, nonatomic) IBOutlet UITextField *oldPassword;
@property (weak, nonatomic) IBOutlet UITextField *nowPassword;
@property (weak, nonatomic) IBOutlet UITextField *confirmPassword;
@property (weak, nonatomic) IBOutlet UILabel *codeLab;
@property (weak, nonatomic) IBOutlet UILabel *acountLab;
@property (weak, nonatomic) IBOutlet UILabel *acountLable;
@property (weak, nonatomic) IBOutlet UILabel *nowLab;
@property (weak, nonatomic) IBOutlet UILabel *confirmLab;
@property (weak, nonatomic) IBOutlet UIButton *sumBtn;
@property (weak, nonatomic) IBOutlet UIButton *codeBtn;
@property (nonatomic, strong) NSTimer *countTimer;
@property (nonatomic, copy) NSString *account;

@end

@implementation ZoloSetPwdVC

- (instancetype)initWithUserAcount:(NSString *)account {
    if (self = [super init]) {
        self.account = account;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    self.view.backgroundColor = [FontManage MHGrayColor];
    self.navigationItem.title = LocalizedString(@"SettingPassword");
    self.codeLab.text = LocalizedString(@"OldPassword");
    self.nowLab.text = LocalizedString(@"NewPassword");
    self.confirmLab.text = LocalizedString(@"ConfirmPassword");
    self.acountLab.text = LocalizedString(@"PersonAcount");
    self.oldPassword.placeholder = [NSString stringWithFormat:@"%@%@", LocalizedString(@"SettingInput"), LocalizedString(@"OldPassword")];
    self.nowPassword.placeholder = [NSString stringWithFormat:@"%@%@", LocalizedString(@"SettingInput"), LocalizedString(@"NewPassword")];
    self.confirmPassword.placeholder = [NSString stringWithFormat:@"%@%@", LocalizedString(@"SettingInput"), LocalizedString(@"ConfirmPassword")];
    [self.codeBtn setTitle:LocalizedString(@"GetCode") forState:UIControlStateNormal];
    self.sumBtn.backgroundColor = [FontManage MHMainColor];
    self.sumBtn.layer.cornerRadius = 10;
    self.sumBtn.layer.masksToBounds = YES;
    [self.codeBtn setTintColor:[FontManage MHMainColor]];
    self.codeBtn.layer.borderColor = [FontManage MHMainColor].CGColor;
    self.codeBtn.layer.borderWidth = 0.2;
    self.codeBtn.layer.cornerRadius = 5;
    self.acountLable.text = self.account;
}

- (IBAction)sumBtnClick:(id)sender {

    if (self.oldPassword.text.length == 0) {
        [MHAlert showMessage:[NSString stringWithFormat:@"%@%@", LocalizedString(@"SettingInput"), LocalizedString(@"OldPassword")]];
        return;
    }

    NSString *current_account = [[NSUserDefaults standardUserDefaults]objectForKey:@"mne_account_current"];
    NSDictionary *mne_account = [[NSUserDefaults standardUserDefaults] objectForKey:@"mne_account"];
    NSString *memnemonicString = mne_account[current_account];
    NSString *decrypt = [OsnUtils aesDecrypt:memnemonicString keyStr:self.oldPassword.text];
    if (decrypt.length > 0) {
        
    } else {
        [MHAlert showMessage:[NSString stringWithFormat:@"%@%@", LocalizedString(@"OldPassword"), LocalizedString(@"ErrorInput")]];
        return;
    }
    
    if (self.nowPassword.text.length == 0) {
        [MHAlert showMessage:[NSString stringWithFormat:@"%@%@", LocalizedString(@"SettingInput"), LocalizedString(@"NewPassword")]];
        return;
    }
    
    if (self.confirmPassword.text.length == 0) {
        [MHAlert showMessage:[NSString stringWithFormat:@"%@%@", LocalizedString(@"SettingInput"), LocalizedString(@"ConfirmPassword")]];
        return;
    }
    
    if (![self.nowPassword.text isEqualToString:self.confirmPassword.text]) {
        [MHAlert showMessage:LocalizedString(@"PlearSuccessPassword")];
        return;
    }
    
    NSString *data = [OsnUtils aesEncrypt:decrypt keyStr:self.nowPassword.text];
   
   if (mne_account) {
       NSMutableDictionary *dic = [[NSMutableDictionary alloc] initWithDictionary:mne_account];
       [dic setValue:data forKey:current_account];
       [[NSUserDefaults standardUserDefaults] setValue:dic forKey:@"mne_account"];
   } else {
       NSMutableDictionary *dic = [NSMutableDictionary new];
       [dic setValue:data forKey:current_account];
       [[NSUserDefaults standardUserDefaults] setValue:dic forKey:@"mne_account"];
   }
    
    [MHAlert showMessage:LocalizedString(@"AlertSuccess")];
    [self.navigationController popViewControllerAnimated:YES];
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    [self.view endEditing:YES];
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
