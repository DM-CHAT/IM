//
//  ZoloNickNameVC.m
//  NewZolo
//
//  Created by JTalking on 2022/7/8.
//

#import "ZoloNickNameVC.h"

@interface ZoloNickNameVC ()

@property (weak, nonatomic) IBOutlet UITextField *nickTextField;
@property (nonatomic, strong) UIButton *saveBtn;
@property (weak, nonatomic) IBOutlet UILabel *nameLab;
@property (nonatomic, assign) BOOL isRemark;
@property (nonatomic, copy) NSString *SId;

@property (nonatomic, assign) MHNickNameType type;

@end

@implementation ZoloNickNameVC

- (instancetype)initWithSettingRemarkName:(MHNickNameType)type WithSId:(NSString *)SId {
    if (self = [super init]) {
        self.type = type;
        self.SId = SId;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    
    self.nameLab.text = LocalizedString(@"SettingName");
    [self.saveBtn setTitle:LocalizedString(@"SettingSave") forState:UIControlStateNormal];
    self.nickTextField.placeholder = LocalizedString(@"SettingInput");
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:self.saveBtn];
    self.view.backgroundColor = [FontManage MHGrayColor];
    
    if (self.type == MHNickNameType_My || self.type == MHNickNameType_User) {
        WS(ws);
        [[DMCCIMService sharedDMCIMService] getUserInfo:self.SId refresh:NO success:^(DMCCUserInfo *userInfo) {
            if (userInfo) {
                ws.nickTextField.text = userInfo.displayName;
            }
        } error:^(int errorCode) {
            
        }];
    } else if (self.type == MHNickNameType_Group) {
        DMCCGroupMember *gm = [[DMCCIMService sharedDMCIMService] getGroupMember:self.SId memberId:[DMCCIMService sharedDMCIMService].getUserID];
        if (![gm.alias isEqualToString:@"(null)"] && gm.alias.length > 0) {
            self.nickTextField.text = gm.alias;
        }
        
    }
}

- (UIButton *)saveBtn {
    if (!_saveBtn) {
        _saveBtn = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, 40, 40)];
        [_saveBtn setTitle:@"保存" forState:UIControlStateNormal];
        [_saveBtn addTarget:self action:@selector(saveBtnClick) forControlEvents:UIControlEventTouchUpInside];
        [_saveBtn setTitleColor:[UIColor colorWithRed:0 green:0 blue:0 alpha:0.5] forState:UIControlStateNormal];
    }
    return _saveBtn;
}

- (void)setUserName {
    if (self.nickTextField.text.length > 256) {
        [MHAlert showMessage:LocalizedString(@"NotWords")];
        return;
    }
    [[ZoloAPIManager instanceManager] settingUserName:self.nickTextField.text WithCompleteBlock:^(BOOL isSuccess) {
        
    }];
}

- (void)saveBtnClick {
    [MHAlert showLoadingStr:LocalizedString(@"AlertNowOpeating")];
    WS(ws);
    if (self.type == MHNickNameType_User) {
        [[DMCCIMService sharedDMCIMService] setFriend:self.SId alias:self.nickTextField.text success:^{
            [MHAlert showMessage:LocalizedString(@"AlertSuccess")];
            dispatch_async(dispatch_get_main_queue(), ^{
                [ws.navigationController popViewControllerAnimated:YES];
            });
        } error:^(int error_code) {
            [MHAlert showMessage:LocalizedString(@"AlertOpearFail")];
        }];
        
    } else if (self.type == MHNickNameType_Group) {
        [[DMCCIMService sharedDMCIMService] modifyGroupAlias:self.SId alias:self.nickTextField.text notifyLines:@[@0] notifyContent:nil success:^{
            [MHAlert showMessage:LocalizedString(@"AlertSuccess")];
            dispatch_async(dispatch_get_main_queue(), ^{
                [ws.navigationController popViewControllerAnimated:YES];
            });
        } error:^(int error_code) {
            [MHAlert showMessage:LocalizedString(@"AlertOpearFail")];
        }];
    } else if (self.type == MHNickNameType_My) {
        [[DMCCIMService sharedDMCIMService] modifyMyInfo:@{@(Modify_DisplayName) : self.nickTextField.text} success:^{
            [MHAlert showMessage:LocalizedString(@"AlertSuccess")];
            dispatch_async(dispatch_get_main_queue(), ^{
                [ws setUserName];
                [ws.navigationController popViewControllerAnimated:YES];
            });
        } error:^(int error_code) {
            [MHAlert showMessage:LocalizedString(@"AlertOpearFail")];
        }];
    } else if (self.type == MHNickNameType_GroupName) {
        [[DMCCIMService sharedDMCIMService] modifyGroupInfo:self.SId type:Modify_Group_Name newValue:self.nickTextField.text notifyLines:@[@(0)] notifyContent:nil success:^{
            [MHAlert showMessage:LocalizedString(@"AlertSuccess")];
            dispatch_async(dispatch_get_main_queue(), ^{
                [ws.navigationController popViewControllerAnimated:YES];
            });
        } error:^(int error_code) {
            [MHAlert showMessage:LocalizedString(@"AlertSettingFail")];
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
