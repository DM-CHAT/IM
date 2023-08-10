//
//  ZoloSetPasswordVC.m
//  NewZolo
//
//  Created by MHHY on 2023/3/8.
//

#import "ZoloSetPasswordVC.h"

@interface ZoloSetPasswordVC (){
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

@implementation ZoloSetPasswordVC

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
    self.codeLab.text = LocalizedString(@"GetCode");
    self.nowLab.text = LocalizedString(@"NewPassword");
    self.confirmLab.text = LocalizedString(@"ConfirmPassword");
    self.acountLab.text = LocalizedString(@"PersonAcount");
    self.oldPassword.placeholder = [NSString stringWithFormat:@"%@%@", LocalizedString(@"SettingInput"), LocalizedString(@"GetCode")];
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
        [MHAlert showMessage:[NSString stringWithFormat:@"%@%@", LocalizedString(@"SettingInput"), LocalizedString(@"GetCode")]];
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
    [MHAlert showLoadingStr:LocalizedString(@"AlertNowOpeating")];
    WS(ws);
    [[ZoloAPIManager instanceManager] updataPasswordWithPhone:self.account code:self.oldPassword.text password:self.confirmPassword.text WithCompleteBlock:^(BOOL isSuccess) {
        if (isSuccess) {
            [MHAlert showMessage:LocalizedString(@"AlertSuccess")];
            [ws.navigationController popViewControllerAnimated:YES];
        }
    }];
}

- (IBAction)codeBtnClick:(id)sender {
    WS(ws);
    [MHAlert showLoadingStr:LocalizedString(@"AlertNowOpeating")];
    [[ZoloAPIManager instanceManager] updataPwdCodeWithPhone:self.account WithCompleteBlock:^(BOOL isSuccess) {
        [MHAlert dismiss];
        if (isSuccess) {
            [ws start];
        }
    }];
}

- (void)start {
    _applyCodeTimeLeft = 60;
    [self.countTimer fire];
}

- (NSTimer *)countTimer {
    if (!_countTimer) {
        _countTimer = [NSTimer scheduledTimerWithTimeInterval:1 target:self selector:@selector(countTime:) userInfo:nil repeats:YES];
        [[NSRunLoop mainRunLoop] addTimer:self.countTimer forMode:NSRunLoopCommonModes];
    }
    return _countTimer;
}

- (void)countTime:(NSTimer *)timer {
    _applyCodeTimeLeft = _applyCodeTimeLeft - 1;
    _codeBtn.enabled = NO;
    [_codeBtn setTitle:[NSString stringWithFormat:@"%d%@", _applyCodeTimeLeft, LocalizedString(@"Second")] forState:UIControlStateNormal];
    if (_applyCodeTimeLeft < 1) {
        [_codeBtn setTitle:LocalizedString(@"GetResetCode") forState:UIControlStateNormal];
        _codeBtn.enabled = YES;
        [_countTimer invalidate];
        _countTimer = nil;
    }
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
