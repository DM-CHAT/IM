//
//  ZoloTransferRecordVC.m
//  NewZolo
//
//  Created by JTalking on 2022/9/9.
//

#import "ZoloTransferRecordVC.h"
#import "ZoloDappViewController.h"

@interface ZoloTransferRecordVC ()

@property (weak, nonatomic) IBOutlet UILabel *namelab;
@property (weak, nonatomic) IBOutlet UILabel *moneyLab;
@property (weak, nonatomic) IBOutlet UILabel *reTime;
@property (weak, nonatomic) IBOutlet UILabel *transLab;
@property (nonatomic, strong) DMCCRedPacketInfo *info;

@property (weak, nonatomic) IBOutlet UIImageView *walletImg;
@property (weak, nonatomic) IBOutlet UILabel *walletLab;
@property (nonatomic, strong) NSDictionary *walletDic;
@property (weak, nonatomic) IBOutlet UIButton *walletBtn;
@property (weak, nonatomic) IBOutlet UIButton *collectBtn;

@end

@implementation ZoloTransferRecordVC

- (instancetype)initWithRedPacketInfo:(DMCCRedPacketInfo *)info {
    if (self = [super init]) {
        self.info = info;
    }
    return self;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self.navigationController setNavigationBarHidden:YES animated:animated];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [self.navigationController setNavigationBarHidden:NO animated:animated];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    
    self.collectBtn.layer.cornerRadius = 8;
    self.collectBtn.layer.masksToBounds = YES;
    [self.collectBtn setTitle:LocalizedString(@"CollectBtnTitle") forState:UIControlStateNormal];
    
    NSDictionary *walletDic = [NSDictionary new];
    if (self.info.wallet) {
        self.walletLab.hidden = NO;
        self.walletImg.hidden = NO;
        self.walletBtn.hidden = NO;
        walletDic = [OsnUtils json2Dics:self.info.wallet];
        self.walletDic = walletDic;
        self.walletLab.text = walletDic[@"name"];
        [self.walletImg sd_setImageWithURL:walletDic[@"portrait"] placeholderImage:SDImageDefault];
    }
    if (self.info.coinType.length > 0) {
        self.moneyLab.text = [NSString stringWithFormat:@"%@ %.6f", self.info.coinType, [self.info.price doubleValue] / 1000000];
    } else {
        self.moneyLab.text = [NSString stringWithFormat:@"USDT %.6f", [self.info.price doubleValue] / 1000000];
    }
    self.reTime.text = [MHDateTools timeStampToString:self.info.timestamp];
    if ([self.info.user isEqualToString:[DMCCIMService sharedDMCIMService].getUserID]) {
        self.namelab.text = LocalizedString(@"TransSucess");
        self.transLab.text = LocalizedString(@"TransforTime");
        self.collectBtn.hidden = YES;
    } else {
        self.transLab.text = LocalizedString(@"TransforTime");
        if (self.info.state == 1) {
            self.namelab.text = LocalizedString(@"TransforRes");
            self.collectBtn.hidden = YES;
        } else {
            self.namelab.text = LocalizedString(@"CollectWaitTitle");
            self.collectBtn.hidden = NO;
        }
    }
}

- (IBAction)collectionBtnClick:(id)sender {
    NSMutableDictionary* data = [NSMutableDictionary new];
    data[@"command"] = @"open";
    data[@"dappId"] = _walletDic[@"target"];
    data[@"txid"] = self.info.unpackID;
    data[@"from"] = [[DMCCIMService sharedDMCIMService] getUserID];
    data[@"timestamp"] = @([OsnUtils getTimeStamp]);
    NSString* calc = [NSString stringWithFormat:@"%@%@%@%@", data[@"dappId"], data[@"txid"], data[@"from"], data[@"timestamp"]];
    NSString* hash = [[DMCCIMService sharedDMCIMService] hashData:[calc dataUsingEncoding:NSUTF8StringEncoding]];
    NSString* sign = [[DMCCIMService sharedDMCIMService] signData:[hash dataUsingEncoding:NSUTF8StringEncoding]];
    data[@"userSign"] = sign;
    data = [HttpUtils doPosts:self.info.urlFetch data:[OsnUtils dic2Json:data]];
    
    if ([data[@"code"] integerValue] == 200) {
        self.namelab.text = LocalizedString(@"TransforRes");
        [[DMCCIMService sharedDMCIMService] openRedPacket:_info.packetID];
        self.collectBtn.hidden = YES;
        if (_receiveBlock) {
            _receiveBlock();
        }
    }
}

- (IBAction)walleBtnClick:(id)sender {
    DMCCLitappInfo *litapp = [DMCCLitappInfo new];
    litapp.name = _walletDic[@"name"];
    litapp.portrait = _walletDic[@"portrait"];
    litapp.target = _walletDic[@"target"];
    litapp.url = _walletDic[@"url"];
    litapp.info = _walletDic[@"info"];
    litapp.param = _walletDic[@"param"];

    ZoloDappViewController *vc = [[ZoloDappViewController alloc] initWithLitappInfo:litapp];
    MHNavigationVC *nav = [[MHNavigationVC alloc] initWithRootViewController:vc];
    nav.modalPresentationStyle = UIModalPresentationFullScreen;
    [self presentViewController:nav animated:YES completion:nil];
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
