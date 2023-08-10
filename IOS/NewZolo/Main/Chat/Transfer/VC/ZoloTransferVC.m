//
//  ZoloTransferVC.m
//  NewZolo
//
//  Created by JTalking on 2022/9/1.
//

#import "ZoloTransferVC.h"
#import "ZoloVeriryPayView.h"
#import "LSTPopView.h"
#import <config/config.h>
#import "ZoloWalletViewController.h"

@interface ZoloTransferVC () <UITextFieldDelegate>

@property (weak, nonatomic) IBOutlet UIImageView *iconImg;
@property (weak, nonatomic) IBOutlet UILabel *nameLab;
@property (weak, nonatomic) IBOutlet UILabel *moneyLab;
@property (weak, nonatomic) IBOutlet UITextField *moneyTextfield;
@property (weak, nonatomic) IBOutlet UIButton *transBtn;
@property (weak, nonatomic) IBOutlet UILabel *transforLab;
@property (nonatomic, strong) DMCCUserInfo *userInfo; // 聊天对方信息
@property (nonatomic, strong) DMCCConversation *conversation;
@property (nonatomic, assign) int countSum;
@end

@implementation ZoloTransferVC

- (instancetype)initWithUserInfo:(DMCCUserInfo *)userInfo withConversation:(DMCCConversation *)conversation {
    if (self = [super init]) {
        self.userInfo = userInfo;
        self.conversation = conversation;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.

    [self.moneyTextfield becomeFirstResponder];
    self.nameLab.text = self.userInfo.displayName;
    NSString *alias = [[DMCCIMService sharedDMCIMService] getFriendAlias:self.userInfo.userId];
    if (alias.length > 0) {
        self.nameLab.text = alias;
    }
    self.moneyTextfield.delegate = self;
    [self.iconImg sd_setImageWithURL:[NSURL URLWithString:self.userInfo.portrait] placeholderImage:SDImageDefault];
    self.iconImg.layer.cornerRadius = 25;
    self.iconImg.layer.masksToBounds = YES;
    self.transforLab.textColor = [FontManage MHTitleSubColor];
    self.transBtn.layer.cornerRadius = 5;
    self.transBtn.layer.masksToBounds = YES;
    self.transforLab.text = LocalizedString(@"ChatTransforMoney");
    [self.transBtn setTitle:LocalizedString(@"ConTransfer") forState:UIControlStateNormal];
}

- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string {
    //首先设置：inputTF.keyboardType = UIKeyboardTypeDecimalPad;
    
    NSString * toBeString = [textField.text stringByReplacingCharactersInRange:range withString:string];
    //限制.后面最多有两位，且不能再输入.
    if ([textField.text rangeOfString:@"."].location != NSNotFound) {
        //有.了 且.后面输入了两位  停止输入
        if (toBeString.length > [toBeString rangeOfString:@"."].location+7) {
            return NO;
        }
        //有.了，不允许再输入.
        if ([string isEqualToString:@"."]) {
            return NO;
        }
    }
    
    //限制首位0，后面只能输入. 或 删除
    if ([textField.text isEqualToString:@"0"]) {
        if (!([string isEqualToString:@"."] || [string isEqualToString:@""])) {
            return NO;
        }
    }
    
    //首位. 前面补全0
    if ([toBeString isEqualToString:@"."]) {
        textField.text = @"0";
        return YES;
    }
    NSCharacterSet * characterSet = [[NSCharacterSet characterSetWithCharactersInString:@"1234567890."] invertedSet];
    NSString * filtered = [[string componentsSeparatedByCharactersInSet:characterSet] componentsJoinedByString:@""];
    return [string isEqualToString:filtered];
   
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    [self.view endEditing:YES];
}

- (IBAction)transBtnClick:(id)sender {
    if (self.moneyTextfield.text.length == 0) {
        [MHAlert showMessage:LocalizedString(@"AlertInputMoney")];
        return;
    }
    if (![MHHelperUtils validateMoney:self.moneyTextfield.text]) {
        [MHAlert showMessage:LocalizedString(@"AlertInputMoney")];
        return;
    }
    ZoloVeriryPayView *view = [[ZoloVeriryPayView alloc] initWithFrame:CGRectZero];
    view.moneyLab.text = [NSString stringWithFormat:@"USDT %.6f", self.moneyTextfield.text.doubleValue];
    LSTPopView *popView = [LSTPopView initWithCustomView:view
                                                popStyle:LSTPopStyleSmoothFromBottom
                                            dismissStyle:LSTDismissStyleSmoothToBottom];
    popView.hemStyle = LSTHemStyleBottom;
    LSTPopViewWK(popView)
    popView.popDuration = 0.5;
    popView.dismissDuration = 0.5;
    popView.isClickFeedback = YES;
    
    popView.sweepStyle = LSTSweepStyleY_Positive;
    popView.dragStyle = LSTDragStyleY_Positive;
    popView.sweepDismissStyle = LSTSweepDismissStyleSmooth;
    popView.bgClickBlock = ^{
        NSLog(@"点击了背景");
        [wk_popView dismiss];
    };
    WS(ws);
    view.veriryPayBlock = ^(NSString * _Nonnull code) {
        [wk_popView dismiss];
        [ws onMakePacket:code];
    };
    
    [popView pop];
}

- (void) orderPay:(NSString*)verifyCode serial:(NSString*)serial{
    long timestamp = [OsnUtils getTimeStamp];
    NSString *userID = [[DMCCIMService sharedDMCIMService] getUserID];
    long balance = [self.moneyTextfield.text floatValue]*1000000;
    NSString* text = @"";
    if(text.length == 0)
        text = LocalizedString(@"TransforSendLab");
    NSMutableDictionary* json = [NSMutableDictionary new];
    json[@"command"] = @"transfer";
    json[@"from"] = userID;
    NSMutableDictionary* data = [NSMutableDictionary new];
    data[@"type"] = @"transaction";
    data[@"from"] = userID;
    data[@"to"] = _conversation.target;
    data[@"count"] = @"";
    data[@"balance"] = @(balance);
    data[@"timestamp"] = @(timestamp);
    data[@"serial"] = @([serial intValue]+1);
    data[@"greetings"] = text;
    data[@"luckNum"] = @"";
    NSLog(@"order pay data: %@",[OsnUtils dic2Json:data]);
    NSString* encrypted = [OsnUtils aesEncrypt:[OsnUtils dic2Json:data] keyStr:verifyCode];
    json[@"body"] = encrypted;
    NSString* calc = [NSString stringWithFormat:@"%@%@%@",json[@"command"],json[@"from"],json[@"body"]];
    NSString* hash = [[DMCCIMService sharedDMCIMService] hashData:[calc dataUsingEncoding:NSUTF8StringEncoding]];
    NSString* sign = [[DMCCIMService sharedDMCIMService] signData:[hash dataUsingEncoding:NSUTF8StringEncoding]];
    json[@"hash"] = hash;
    json[@"sign"] = sign;
    json[@"language"] = [OSNLanguage getBaseLanguage];
    NSLog(@"order pay data: %@",[OsnUtils dic2Json:json]);
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    json = [HttpUtils doPosts:login.json.TRANSFER_URL data:[OsnUtils dic2Json:json]];
    NSLog(@"order pay result: %@",json==nil?@"null":[OsnUtils dic2Json:json]);
    json = [self getPayData:json];
    if(json == nil) {
        return;
    }
    self.countSum = 0;
    [self checkResult:json withDic:data withText:text];
}

- (void)checkResult:(NSString *)payId withDic:(NSDictionary *)dic withText:(NSString *)text {
    NSMutableDictionary* data = [[NSMutableDictionary alloc] initWithDictionary:dic];
    WS(ws);
    NSLog(@"=====checkResult=====");
    [MHAlert showLoadingStr:LocalizedString(@"AlertNowOpeating")];
    [[ZoloAPIManager instanceManager] checkPayResultWithPayId:payId withCompleteBlock:^(NSDictionary * _Nonnull json) {
        if (json) {
            if (json[@"error"]) {
                NSNumber *code = json[@"error"];
                if (code.longValue == 40000004) {
                    NSNumber *count = json[@"count"];
                    [MHAlert showMessage:[NSString stringWithFormat:LocalizedString(@"ErrorPaymentIncorrect"), count.intValue]];
                } else if (code.longValue == 40000006) {
                    
                    [MHAlert showCodeMessage:code.longValue];
                    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                        MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
                        NSString *url = [NSString stringWithFormat:@"%@%@", login.json.WALLET_URL, @"?isSetting=1"];
                        [ws.navigationController pushViewController:[[ZoloWalletViewController alloc] initWithUrlInfo:url isHidden:NO] animated:YES];
                    });
                    
                } else {
                    [MHAlert showCodeMessage:code.longValue];
                }
            } else {
                [MHAlert showMessage:LocalizedString(@"AlertSuccess")];
                NSString* txid = json[@"txid"];
                NSString* urlQuery = json[@"queryUrl"];
                NSString* urlFetch = json[@"url"];
                NSString* wallet = json[@"wallet"];
                NSString* coinType = json[@"coinType"];
                
                if(txid == nil){
                    return;
                }
                if(urlQuery == nil)
                    urlQuery = @"";
                if(urlFetch == nil)
                    urlFetch = @"";
                if (wallet == nil)
                    wallet = @"";
                if (coinType == nil)
                    coinType = @"";
                
                data[@"txid"] = txid;
                data[@"text"] = text;
                data[@"packetID"] = txid;
                data[@"unpackID"] = txid;
                data[@"urlQuery"] = urlQuery;
                data[@"urlFetch"] = urlFetch;
                data[@"wallet"] = wallet;
                data[@"coinType"] = coinType;
                
                DMCCRedPacketMessageContent *content = [DMCCRedPacketMessageContent new];
                content.ids = txid;
                content.info = [OsnUtils dic2Json:data];
                content.text = text;
                content.state = 0;
                [ws sendMessage:content];
            }
        } else {
            if (ws.countSum >= 4) {
                return;
            }
            ws.countSum ++;
            [NSThread sleepForTimeInterval:3];
            [ws checkResult:payId withDic:dic withText:text];
        }
    }];
}

#pragma mark - 消息发送
- (void)sendMessage:(DMCCMessageContent *)content {
    [[DMCCIMService sharedDMCIMService] send:self.conversation content:content pwd:nil success:^(long long messageUid, long long timestamp) {
        [[NSNotificationCenter defaultCenter] postNotificationName:FZ_EVENT_MSGINSERTSTATUS_STATE object:@(messageUid)];
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.navigationController popViewControllerAnimated:YES];
        });
    } error:^(int error_code) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.navigationController popViewControllerAnimated:YES];
        });
    }];
}

- (void) onMakePacket:(NSString*)verifyCode{
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    NSDictionary *json = [HttpUtils doPosts:[NSString stringWithFormat:@"%@/%@",login.json.ACCOUNT_PREFIX_URL,[[DMCCIMService sharedDMCIMService] getUserID]] data:@""];
    NSLog(@"get serial result: %@", json==nil?@"null":[OsnUtils dic2Json:json]);
    json = [self getPayData:json];
    if(json == nil)
        return;
    NSString *serial = json[@"serial"];
    [self orderPay:verifyCode serial:serial];
}

- (NSMutableDictionary*) getPayData:(NSDictionary*)data {
    if(data == nil)
        return nil;
    NSNumber *code = data[@"code"];
    if(code.longValue != 200){
        [MHAlert showCodeMessage:code.longValue];
//        if(code.intValue == 40000004) {
//            data = [OsnUtils json2Dics:data[@"msg"]];
//            NSString *msg = [NSString stringWithFormat:@"%@ %@",data[@"msg"],data[@"error_count"]];
//            [MHAlert showMessage:msg];
//        }else{
//            [MHAlert showMessage:data[@"msg"]];
//        }
        return nil;
    }
    return data[@"data"];
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
