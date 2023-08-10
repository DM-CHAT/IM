//
//  ZoloSendRedPackVC.m
//  NewZolo
//
//  Created by JTalking on 2022/9/8.
//

#import "ZoloSendRedPackVC.h"
#import "ZoloVeriryPayView.h"
#import "LSTPopView.h"
#import <config/config.h>
#import "ZoloRoleView.h"
#import "ZoloWalletViewController.h"

@interface ZoloSendRedPackVC () <UITextFieldDelegate>

@property (weak, nonatomic) IBOutlet UITextField *numTextField;
@property (weak, nonatomic) IBOutlet UITextField *moneyTextField;
@property (weak, nonatomic) IBOutlet UITextField *remarkLab;
@property (weak, nonatomic) IBOutlet UILabel *moneyLab;
@property (weak, nonatomic) IBOutlet UIButton *moneyBtn;
@property (weak, nonatomic) IBOutlet UITextField *luckTextField;

@property (weak, nonatomic) IBOutlet UIView *numView;
@property (weak, nonatomic) IBOutlet UIView *moneyView;
@property (weak, nonatomic) IBOutlet UIView *remarkView;
@property (weak, nonatomic) IBOutlet UIView *luckView;

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *marginTop;

@property (weak, nonatomic) IBOutlet UIButton *gameBtn;
@property (weak, nonatomic) IBOutlet UIImageView *gameIng;


@property (weak, nonatomic) IBOutlet UILabel *redNumLab;
@property (weak, nonatomic) IBOutlet UILabel *redSumLab;
@property (weak, nonatomic) IBOutlet UILabel *redLuckLab;


@property (nonatomic, assign) NSInteger type;

@property (nonatomic, strong) DMCCConversation *conversation;
@property (nonatomic, assign) int countSum;

@property (nonatomic, strong) NSDictionary *redDic;

@end

@implementation ZoloSendRedPackVC

- (instancetype)initWithRedPackType:(NSInteger)type withConversation:(DMCCConversation *)conversation {
    if (self = [super init]) {
        self.type = type;
        self.conversation = conversation;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    self.navigationItem.title = LocalizedString(@"ChatRedTilte");
    self.redNumLab.text = LocalizedString(@"ChatRedNum");
    self.redSumLab.text = LocalizedString(@"ChatRedSum");
    self.redLuckLab.text = LocalizedString(@"ChatRedLuck");
    self.remarkLab.text = LocalizedString(@"ChatRedSendLab");
    [self.moneyBtn setTitle:LocalizedString(@"ChatRedSend") forState:UIControlStateNormal];
    [self.gameBtn setTitle:LocalizedString(@"ChatRedGameRele") forState:UIControlStateNormal];
    [self.gameBtn setTitleColor:[FontManage MHTitleSubColor] forState:UIControlStateNormal];
    [self.numTextField becomeFirstResponder];
    self.moneyTextField.delegate = self;
    self.luckTextField.delegate = self;
    self.numTextField.delegate = self;
    if (self.type == 5) {
        self.luckView.hidden = NO;
        self.marginTop.constant = 18.0;
        self.gameBtn.hidden = NO;
        self.gameIng.hidden = NO;
        [self getRedRoleData];
    } else {
        self.luckView.hidden = YES;
        self.gameBtn.hidden = YES;
        self.gameIng.hidden = YES;
        self.marginTop.constant = -60.0;
    }
        
    self.numView.layer.masksToBounds = YES;
    self.numView.layer.cornerRadius = 5;
    self.numView.backgroundColor = [FontManage MHInputBgColor];
    self.moneyView.layer.masksToBounds = YES;
    self.moneyView.layer.cornerRadius = 5;
    self.moneyView.backgroundColor = [FontManage MHInputBgColor];
    self.remarkView.layer.masksToBounds = YES;
    self.remarkView.layer.cornerRadius = 5;
    self.remarkView.backgroundColor = [FontManage MHInputBgColor];
    self.luckView.layer.masksToBounds = YES;
    self.luckView.layer.cornerRadius = 5;
    self.luckView.backgroundColor = [FontManage MHInputBgColor];
    self.moneyBtn.layer.masksToBounds = YES;
    self.moneyBtn.layer.cornerRadius = 5;
}

- (void)getRedRoleData {
    NSString *language = [[NSUserDefaults standardUserDefaults] stringForKey:@"ospn_language"];
    WS(ws);
    [[ZoloAPIManager instanceManager] getRedRoleWithLanguage:language WithCompleteBlock:^(NSDictionary * _Nonnull data) {
        if (data) {
            ws.redDic = data;
            NSArray *array = data[@"count"];
            if (array.count > 0) {
                ws.numTextField.placeholder = [array componentsJoinedByString:@","];
            }
            NSDictionary *dic = data[@"balance"];
            ws.moneyTextField.placeholder = [NSString stringWithFormat:@"%lld-%lld", [dic[@"min"] longLongValue] / 100, [dic[@"max"] longLongValue] / 100] ;
        }
    }];
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    [self.view endEditing:YES];
}

- (void)textFieldDidEndEditing:(UITextField *)textField {
    if (textField.tag == 20) {
        self.moneyLab.text = [NSString stringWithFormat:@"USDT %.6f", textField.text.doubleValue];
    }
}

- (BOOL)textFieldShouldBeginEditing:(UITextField *)textField {
    return YES;
}

//限制只能输入金额
- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string {
    //首先设置：inputTF.keyboardType = UIKeyboardTypeDecimalPad;
    if (textField.tag == 20) {
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
    } else {
        NSString * str = [NSString stringWithFormat:@"%@%@",textField.text,string];
        //匹配以0开头的数字
        NSPredicate * predicate0 = [NSPredicate predicateWithFormat:@"SELF MATCHES %@",@"^[0][0-9]+$"];
        //匹配两位小数、整数
        NSPredicate * predicate1 = [NSPredicate predicateWithFormat:@"SELF MATCHES %@",@"^(([1-9]{1}[0-9]*|[0])?[0-9]{0,0})$"];
        BOOL sucess = ![predicate0 evaluateWithObject:str] && [predicate1 evaluateWithObject:str] ? YES : NO;
        return sucess;
    }
}

- (IBAction)moneyBtnClick:(id)sender {
    DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:self.conversation.target refresh:NO];
    if (self.numTextField.text.length == 0) {
        [MHAlert showMessage:LocalizedString(@"AlertRedNum")];
        return;
    }
    NSString *numRed = self.numTextField.text;
    if (numRed.intValue > group.memberCount) {
        [MHAlert showMessage:LocalizedString(@"GroupRedNumber")];
        return;
    }
    if (self.moneyTextField.text.length == 0) {
        [MHAlert showMessage:LocalizedString(@"AlertRedCount")];
        return;
    }
    // 4 手气红包  5 扫雷红包
    if (self.type == 5) {
        if (self.luckTextField.text.length == 0) {
            [MHAlert showMessage:LocalizedString(@"AlertRedNumber")];
            return;
        }
    }
    if (![MHHelperUtils validateMoney:self.moneyTextField.text]) {
        [MHAlert showMessage:LocalizedString(@"AlertRedCount")];
        return;
    }
    
    if (self.remarkLab.text.length > 50) {
        [MHAlert showMessage:LocalizedString(@"StringLength")];
        return;
    }
    
    NSString *luckNum = self.luckTextField.text;
    if (luckNum.intValue >= 10) {
        [MHAlert showMessage:LocalizedString(@"AlertRedNumber")];
        return;
    }
    
    ZoloVeriryPayView *view = [[ZoloVeriryPayView alloc] initWithFrame:CGRectZero];
    view.moneyLab.text = [NSString stringWithFormat:@"USDT %.6f", self.moneyTextField.text.doubleValue];
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
    long balance = [self.moneyTextField.text floatValue]*1000000;
    NSString* text = self.remarkLab.text;
    if(text.length == 0)
        text = LocalizedString(@"ChatRedSendLab");
    NSMutableDictionary* json = [NSMutableDictionary new];
    json[@"command"] = @"transfer";
    json[@"from"] = userID;
    NSMutableDictionary* data = [NSMutableDictionary new];
    
    // 4 手气红包  5 扫雷红包
    if(_type == 4)
        data[@"type"] = @"random";
    else
        data[@"type"] = @"bomb";
    
    data[@"from"] = userID;
    data[@"to"] = _conversation.target;
    data[@"count"] = self.numTextField.text;
    data[@"balance"] = @(balance);
    data[@"timestamp"] = @(timestamp);
    data[@"serial"] = @([serial intValue]+1);
    data[@"greetings"] = text;
    data[@"luckNum"] = self.luckTextField.text;
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
    if(json == nil)
        return;
    self.countSum = 0;
    [self checkResult:json withDic:data withText:text];
}

- (void)checkResult:(NSString *)payId withDic:(NSDictionary *)dic withText:(NSString *)text {
    NSMutableDictionary* data = [[NSMutableDictionary alloc] initWithDictionary:dic];
    WS(ws);
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
//        if(code.intValue == 40000004){
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

- (IBAction)gameBtnClick:(id)sender {
    ZoloRoleView *view = [[ZoloRoleView alloc] initWithFrame:CGRectZero];
    view.frame = CGRectMake(0, 0, 300, 380);
    view.rolelab.text = self.redDic[@"gameRole"];
    LSTPopView *popView = [LSTPopView initWithCustomView:view
                                                popStyle:LSTPopStyleSmoothFromTop
                                            dismissStyle:LSTDismissStyleSmoothToBottom];
    popView.popStyle = LSTPopStyleNO;
    popView.dismissStyle = LSTDismissStyleNO;
    popView.popDuration = 1.0;
    popView.dismissDuration = 1.0;
    LSTPopViewWK(popView)
    popView.bgClickBlock = ^{
        [wk_popView dismiss];
    };
    [popView pop];
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
