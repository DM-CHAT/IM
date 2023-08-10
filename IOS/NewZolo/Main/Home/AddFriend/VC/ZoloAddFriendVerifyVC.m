//
//  ZoloAddFriendVerifyVC.m
//  NewZolo
//
//  Created by JTalking on 2022/8/25.
//

#import "ZoloAddFriendVerifyVC.h"

@interface ZoloAddFriendVerifyVC ()

@property (weak, nonatomic) IBOutlet UITextView *remarkTextView;
@property (weak, nonatomic) IBOutlet UITextField *remarkTextField;
@property (nonatomic, strong) DMCCUserInfo *userInfo;
@property (weak, nonatomic) IBOutlet UILabel *sendRemarkLab;
@property (weak, nonatomic) IBOutlet UILabel *sendLab;
@property (weak, nonatomic) IBOutlet UIButton *sendBtn;

@end

@implementation ZoloAddFriendVerifyVC

- (instancetype)initWithUserInfo:(DMCCUserInfo *)userInfo {
    if (self = [super init]) {
        self.userInfo = userInfo;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    self.navigationItem.title = LocalizedString(@"ChatApplyAddFriend");
    [self.sendBtn setTitle:LocalizedString(@"ChatSendButtom") forState:UIControlStateNormal];
    self.sendLab.text = LocalizedString(@"ChatSendApplyAddFriend");
    self.sendRemarkLab.text = LocalizedString(@"ChatFriendRemark");
    self.remarkTextView.text = LocalizedString(@"ChatSendHello");
    self.remarkTextField.placeholder = LocalizedString(@"ChatInputRemark");
    self.view.backgroundColor = [FontManage MHGrayColor];
}

- (IBAction)sendBtnClick:(id)sender {
    WS(ws);
    [MHAlert showLoadingStr:LocalizedString(@"AlertNowOpeating")];
    if (self.userInfo == nil) {
        return;
    }
    [[DMCCIMService sharedDMCIMService] sendFriendRequest:self.userInfo.userId reason:self.remarkTextView.text extra:nil success:^{
        [MHAlert dismiss];
        dispatch_async(dispatch_get_main_queue(), ^{
            [ws.navigationController popToRootViewControllerAnimated:YES];
        });
    } error:^(int error_code) {
        [MHAlert dismiss];
        dispatch_async(dispatch_get_main_queue(), ^{
        });
    }];
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
