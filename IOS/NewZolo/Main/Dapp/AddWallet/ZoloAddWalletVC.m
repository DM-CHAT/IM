//
//  ZoloAddWalletVC.m
//  NewZolo
//
//  Created by MHHY on 2023/4/24.
//

#import "ZoloAddWalletVC.h"

@interface ZoloAddWalletVC ()

@property (nonatomic, strong) DMCCLitappInfo *appInfo;
@property (weak, nonatomic) IBOutlet UILabel *walletTitle;
@property (weak, nonatomic) IBOutlet UIImageView *walletImg;
@property (weak, nonatomic) IBOutlet UILabel *walletIdTitle;
@property (weak, nonatomic) IBOutlet UILabel *walletIdLab;
@property (weak, nonatomic) IBOutlet UILabel *walletSubTitle;
@property (weak, nonatomic) IBOutlet UILabel *walletTitle1;
@property (weak, nonatomic) IBOutlet UILabel *walletTitle2;
@property (weak, nonatomic) IBOutlet UIButton *walletCancel;
@property (weak, nonatomic) IBOutlet UIButton *walletAgree;
@property (weak, nonatomic) IBOutlet UIView *subTitleview;

@end

@implementation ZoloAddWalletVC

- (instancetype)initWithLitappInfo:(DMCCLitappInfo *)appInfo {
    if (self = [super init]) {
        self.appInfo = appInfo;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    
    self.walletTitle.text = self.appInfo.name;
    NSDictionary *dic = [OsnUtils json2Dics:self.appInfo.param];
    [self.walletImg sd_setImageWithURL:dic[@"portrait"] placeholderImage:nil];
    self.walletIdLab.text = self.appInfo.target;
    self.walletIdTitle.text = [NSString stringWithFormat:@"%@ ID:", LocalizedString(@"WalletTitle")];
    self.walletSubTitle.text = LocalizedString(@"WalletSubTitle");
    self.walletTitle2.text = LocalizedString(@"WalletSubTitles");
    self.walletTitle1.text = LocalizedString(@"WalletSubTitle1");
    self.subTitleview.layer.cornerRadius = 10;
    self.subTitleview.layer.masksToBounds = YES;
    
    [self.walletAgree setTitle:LocalizedString(@"WalletAgreeAdd") forState:UIControlStateNormal];
    [self.walletCancel setTitle:LocalizedString(@"Cancel") forState:UIControlStateNormal];
    
    self.walletCancel.layer.cornerRadius = 10;
    self.walletCancel.layer.masksToBounds = YES;
    self.walletCancel.layer.borderColor = MHColorFromHex(0x747B84).CGColor;
    self.walletCancel.layer.borderWidth = 2;
    
    self.walletAgree.layer.cornerRadius = 10;
    self.walletAgree.layer.masksToBounds = YES;
    
}

- (IBAction)agreeBtnClick:(id)sender {
    DMCCWalletInfo *info = [DMCCWalletInfo new];
    info.osnID = self.appInfo.target;
    info.name = self.appInfo.name;
    info.url = self.appInfo.url;
    info.wallect = self.appInfo.param;
    [[DMCCIMService sharedDMCIMService] saveWalletInfoWithInfo:info];
    [[NSUserDefaults standardUserDefaults] setBool:YES forKey:@"isShowWalletHidden"];
    [MHAlert showMessage:LocalizedString(@"AlertSuccess")];
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (IBAction)cancelBtnClick:(id)sender {
    [self dismissViewControllerAnimated:YES completion:nil];
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
