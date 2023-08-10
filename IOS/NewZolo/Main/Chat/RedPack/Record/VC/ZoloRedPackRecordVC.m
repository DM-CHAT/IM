//
//  ZoloRedPackRecordVC.m
//  NewZolo
//
//  Created by JTalking on 2022/9/9.
//

#import "ZoloRedPackRecordVC.h"
#import "ZoloRedPackRecordCell.h"
#import "ZoloRedPackRecordHead.h"
#import "ZoloDappViewController.h"

@interface ZoloRedPackRecordVC () <UITableViewDelegate, UITableViewDataSource>

@property (nonatomic, strong) DMCCRedPacketInfo *info;
@property (nonatomic, strong)NSMutableArray<DMCCUnpackInfo*> *unpackInfoList;
@property (nonatomic, assign)NSInteger odds;

@end

@implementation ZoloRedPackRecordVC

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self.navigationController setNavigationBarHidden:YES animated:animated];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [self.navigationController setNavigationBarHidden:NO animated:animated];
}

- (instancetype)initWithRedPacket:(DMCCRedPacketInfo*)info {
    if (self = [super init]) {
        self.info = info;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.extendedLayoutIncludesOpaqueBars = YES;
    if (@available(iOS 11.0, *)) {
          self.tableView.contentInsetAdjustmentBehavior = UIScrollViewContentInsetAdjustmentNever;
      } else {
          self.automaticallyAdjustsScrollViewInsets = NO;
    }
    _unpackInfoList = [NSMutableArray new];
    [self registerCellWithNibName:NSStringFromClass([ZoloRedPackRecordCell class]) isTableview:YES];
    [self registerHeaderFooterWithNibName:NSStringFromClass([ZoloRedPackRecordHead class])];
    [self listGroupPacket:self.info];
}

- (void) listGroupPacket:(DMCCRedPacketInfo *)info {
    NSMutableDictionary* data = [NSMutableDictionary new];
    data[@"get"] = @"txid";
    data[@"txid"] = info.unpackID;
    NSLog(@"list actors data: %@",[OsnUtils dic2Json:data]);
    data = [HttpUtils doPosts:info.urlQuery data:[OsnUtils dic2Json:data]];
    if(data == nil)
        return;
    NSLog(@"list actors result: %@", [OsnUtils dic2Json:data]);
    NSMutableDictionary* json = [self getRedData:data];
    if(json == nil)
        return;
    _odds = ((NSNumber*)json[@"odds"]).intValue;
    NSArray<NSDictionary*> *array = json[@"actors"];
    for(NSDictionary *o in array){
        DMCCUnpackInfo *unpackInfo = [DMCCUnpackInfo new];
        unpackInfo.packetID = info.packetID;
        unpackInfo.unpackID = info.packetID;
        unpackInfo.fetcher = o[@"osnid"];
        unpackInfo.price = ((NSNumber*)o[@"balance"]).stringValue;
        unpackInfo.timestamp = ((NSNumber*)o[@"timestamp"]).longValue;
        [_unpackInfoList addObject:unpackInfo];
    }
    [self.tableView reloadData];
}

- (NSMutableDictionary*) getRedData:(NSDictionary*)data{
    if(data == nil)
        return nil;
    NSNumber *code = data[@"code"];
    if(code.longValue != 200){
        [MHAlert showCodeMessage:code.longValue];
        return nil;
    }
    return data[@"data"];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return _unpackInfoList.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ZoloRedPackRecordCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloRedPackRecordCell class])];
    
    DMCCUnpackInfo *info = _unpackInfoList[indexPath.row];
    DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService]getUserInfo:info.fetcher refresh:YES];
    [cell.imgIcon sd_setImageWithURL:[NSURL URLWithString:userInfo.portrait] placeholderImage:SDImageDefault];
    if (self.info.coinType.length > 0) {
        cell.moneyLab.text = [NSString stringWithFormat:@"%@ %.6f", self.info.coinType, info.price.doubleValue / 1000000];
    } else {
        cell.moneyLab.text = [NSString stringWithFormat:@"USDT %.6f", info.price.doubleValue / 1000000];
    }
    cell.nameLab.text = userInfo.displayName;
    cell.timeLab.text = [MHDateTools timeStampToString:info.timestamp];

    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return self.scaleHeight(400);
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section {
    ZoloRedPackRecordHead *head = [tableView dequeueReusableHeaderFooterViewWithIdentifier:NSStringFromClass([ZoloRedPackRecordHead class])];
    DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService]getUserInfo:self.info.user refresh:false];
    [head.userIcon sd_setImageWithURL:[NSURL URLWithString:userInfo.portrait] placeholderImage:SDImageDefault];
    head.redName.text = LocalizedString(@"ChatRedSendLab");
    NSDictionary *walletDic = [NSDictionary new];
    if (self.info.wallet) {
        head.walletLal.hidden = NO;
        head.walletIcon.hidden = NO;
        head.walletBtn.hidden = NO;
        
        walletDic = [OsnUtils json2Dics:self.info.wallet];
        head.walletLal.text = walletDic[@"name"];
        [head.walletIcon sd_setImageWithURL:walletDic[@"portrait"] placeholderImage:SDImageDefault];
    }
    
    WS(ws);
    head.backBlock = ^{
        [ws.navigationController popViewControllerAnimated:YES];
    };
    
    head.walletBtnBlock = ^{
        DMCCLitappInfo *litapp = [DMCCLitappInfo new];
        litapp.name = walletDic[@"name"];
        litapp.portrait = walletDic[@"portrait"];
        litapp.target = walletDic[@"target"];
        litapp.url = walletDic[@"url"];
        litapp.info = walletDic[@"info"];
        litapp.param = walletDic[@"param"];
        
        ZoloDappViewController *vc = [[ZoloDappViewController alloc] initWithLitappInfo:litapp];
        MHNavigationVC *nav = [[MHNavigationVC alloc] initWithRootViewController:vc];
        nav.modalPresentationStyle = UIModalPresentationFullScreen;
        [ws presentViewController:nav animated:YES completion:nil];
    };
    
    if ([self.info.type isEqualToString:@"bomb"]) { // 扫雷
        double allPrice = 0;
        bool isWin = false;
        for(DMCCUnpackInfo *info in _unpackInfoList){
            allPrice += info.price.floatValue;
            NSString *endNum = [info.price substringFromIndex:info.price.length-1];
            if(endNum != nil && [endNum isEqualToString:self.info.luckNum])
                isWin = true;
        }
        allPrice /= 100;
        DMCCUserInfo* userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:self.info.user refresh:false ];
        head.redView.hidden = YES;
        head.otherView.hidden = NO;
        if (_odds == 0) {
            head.winB.hidden = YES;
            head.winMoney.hidden = YES;
            head.whoLab.hidden = YES;
        } else {
            head.winB.hidden = NO;
            head.winMoney.hidden = NO;
            head.whoLab.hidden = NO;
            head.winB.text = [NSString stringWithFormat:@"%@: %.2f", LocalizedString(@"win_Mul") , (float)_odds/10];
            head.winMoney.text = [NSString stringWithFormat:@"%@: %.2f", LocalizedString(@"win_price") , (float)allPrice*_odds/10];
            head.whoLab.text = [NSString stringWithFormat:@"%@[%@]%@", LocalizedString(@"bless_win") ,userInfo.displayName, LocalizedString(@"win_Lab")];
        }
        head.winNum.text = [NSString stringWithFormat:@"%@: %@", LocalizedString(@"win_Last") , self.info.luckNum];
        head.redName.text = self.info.text;
    } else { // 手气
        head.redView.hidden = NO;
        head.otherView.hidden = YES;
         
        BOOL isGetRed = NO;
        DMCCUnpackInfo *getInfo = nil;
        for (DMCCUnpackInfo *info in _unpackInfoList) {
            if ([info.fetcher isEqualToString:[DMCCIMService sharedDMCIMService].getUserID]) {
                isGetRed = YES;
                getInfo = info;
                break;
            }
        }
        if (isGetRed) {
            if (self.info.coinType.length > 0) {
                head.moneyLab.text = [NSString stringWithFormat:@"%@ %.6f", self.info.coinType, getInfo.price.doubleValue / 1000000];
            } else {
                head.moneyLab.text = [NSString stringWithFormat:@"USDT %.6f", getInfo.price.doubleValue / 1000000];
            }
        } else {
            head.moneyLab.text = LocalizedString(@"RedPackGetFinish");
        }
        
        head.remarkLab.text = [NSString stringWithFormat:LocalizedString(@"RedPackTitle"), self.info.count.integerValue, self.info.price.floatValue / 1000000];
    }
    
    return head;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 8;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return self.scaleHeight(54);
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
