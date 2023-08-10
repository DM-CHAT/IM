//
//  ZoloVersionViewController.m
//  NewZolo
//
//  Created by MHHY on 2023/3/9.
//

#import "ZoloVersionViewController.h"
#import "ZoloWalletViewController.h"

@interface ZoloVersionViewController () <UITextViewDelegate>

@property (weak, nonatomic) IBOutlet UILabel *titleLab;
@property (weak, nonatomic) IBOutlet UILabel *versionLab;
@property (weak, nonatomic) IBOutlet UILabel *versionUpLab;
@property (weak, nonatomic) IBOutlet UIView *versionUpView;
@property (weak, nonatomic) IBOutlet UILabel *privacyLab;
@property (weak, nonatomic) IBOutlet UILabel *userPriLab;

@property (weak, nonatomic) IBOutlet UITextView *vTextView;

@end

@implementation ZoloVersionViewController

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self.navigationController setNavigationBarHidden:YES animated:animated];
    [self getData];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [self.navigationController setNavigationBarHidden:NO animated:animated];
}

- (IBAction)backBtnClick:(id)sender {
    [self.navigationController popViewControllerAnimated:YES];
}


- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.titleLab.text = LocalizedString(@"VersionTo");
    self.privacyLab.text = LocalizedString(@"AppPrivacyPolicy");
    self.userPriLab.text = LocalizedString(@"UserPriLab");
    NSString *key = @"CFBundleShortVersionString";
    NSString *currentVersion = [NSBundle mainBundle].infoDictionary[key];
    self.versionLab.text = [NSString stringWithFormat:@"%@:%@", LocalizedString(@"CurrentVersion"), currentVersion];
    self.versionUpLab.text = LocalizedString(@"VersionUpdate");
    
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(privacyClick)];
    [self.privacyLab addGestureRecognizer:tap];
    
    UITapGestureRecognizer *tap1 = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(versionUpClick)];
    [self.versionUpView addGestureRecognizer:tap1];
    
    UITapGestureRecognizer *tap2 = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(userPriClick)];
    [self.userPriLab addGestureRecognizer:tap2];
    
    self.vTextView.delegate = self;
    self.vTextView.editable = NO;
    self.vTextView.textAlignment = NSTextAlignmentCenter;
    self.vTextView.font = [UIFont systemFontOfSize:20];
    
    NSString *agreeStr = [NSString stringWithFormat:@"                     %@              %@",LocalizedString(@"AppPrivacyPolicy"),LocalizedString(@"UserPriLab")];
    
    NSMutableAttributedString *diffString = [[NSMutableAttributedString alloc] initWithString:agreeStr];
    
   [diffString addAttribute:NSLinkAttributeName
                          value:@"privacy://"
                          range:[[diffString string] rangeOfString:[NSString stringWithFormat:@"%@",LocalizedString(@"AppPrivacyPolicy")]]];

   [diffString addAttribute:NSLinkAttributeName
                      value:@"delegate://"
                      range:[[diffString string] rangeOfString:[NSString stringWithFormat:@"%@",LocalizedString(@"UserPriLab")]]];

    NSMutableParagraphStyle*paragraph = [[NSMutableParagraphStyle alloc] init];
    paragraph.alignment =NSTextAlignmentCenter;
    paragraph.lineSpacing =10;
    
    self.vTextView.linkTextAttributes = @{
        NSForegroundColorAttributeName: [FontManage MHBlockColor],
        NSFontAttributeName: [UIFont systemFontOfSize:20],
        NSParagraphStyleAttributeName: paragraph
    };
   self.vTextView.attributedText = diffString;
    
    // Do any additional setup after loading the view from its nib.
}

#pragma mark - UITextViewDelegate
- (BOOL)textView:(UITextView *)textView shouldInteractWithURL:(NSURL *)URL inRange:(NSRange)characterRange interaction:(UITextItemInteraction)interaction {
    
    if ([[URL scheme] isEqualToString:@"privacy"]){
        [self.navigationController pushViewController:[[ZoloWalletViewController alloc] initWithUrlInfo:LocalizedString(@"AppPrivacyPolicyWeb") isHidden:YES] animated:YES];
    }
    
    else if ([[URL scheme] isEqualToString:@"delegate"]) {
        [self.navigationController pushViewController:[[ZoloWalletViewController alloc] initWithUrlInfo:LocalizedString(@"AppUserPriPolicyWeb") isHidden:YES] animated:YES];
    }
    return YES;
}

- (void)versionUpClick {
    [self checkUpdateVersion];
}

// 检查版本更新
- (void)checkUpdateVersion {
    NSString *key = @"CFBundleShortVersionString";
    NSString *currentVersion = [NSBundle mainBundle].infoDictionary[key];
    [[ZoloAPIManager instanceManager] getCheckUpdateWithCompleteBlock:^(NSDictionary * _Nonnull data) {
        if (data) {
            NSString *updateVersion = data[@"updateVersion"];
            NSString *isShow = data[@"isShow"];
            if (isShow.intValue == 1) {
                if (![currentVersion isEqualToString:data[@"iosVersion"]]) {
                    if (updateVersion.intValue == 1) {
                        [MHAlert showNormalAlert:LocalizedString(@"AppVersion") withAlerttype:MHAlertTypeRemind withOkBlock:^(UIAlertAction * _Nonnull action) {
                            if ([action.title isEqualToString:LocalizedString(@"Sure")]) {
                                [[UIApplication sharedApplication] openURL:[NSURL URLWithString:data[@"iosUrl"]] options:@{} completionHandler:nil];
                            }
                        }];
                    } else {
                        [MHAlert showCustomeAlert:LocalizedString(@"AppVersion") withAlerttype:MHAlertTypeRemind withOkBlock:^(UIAlertAction * _Nonnull action) {
                            if ([action.title isEqualToString:LocalizedString(@"Sure")]) {
                                [[UIApplication sharedApplication] openURL:[NSURL URLWithString:data[@"iosUrl"]] options:@{} completionHandler:nil];
                            }
                        }];
                    }
                }else {
                    [MHAlert showMessage:LocalizedString(@"IsVersionUpdate")];
                }
            } else {
                [MHAlert showMessage:LocalizedString(@"IsVersionUpdate")];
            }
        }
    }];
}

- (void)privacyClick {
    [self.navigationController pushViewController:[[ZoloWalletViewController alloc] initWithUrlInfo:LocalizedString(@"AppPrivacyPolicyWeb") isHidden:YES] animated:YES];
}

- (void)userPriClick {
    [self.navigationController pushViewController:[[ZoloWalletViewController alloc] initWithUrlInfo:LocalizedString(@"AppUserPriPolicyWeb") isHidden:YES] animated:YES];
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
