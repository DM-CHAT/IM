//
//  ZoloNoticeHeadView.m
//  NewZolo
//
//  Created by JTalking on 2022/9/22.
//

#import "ZoloTopHeadView.h"
#import "ZoloTopMsgCell.h"

@interface ZoloTopHeadView () <UITableViewDelegate, UITableViewDataSource>

@property (weak, nonatomic) IBOutlet UITableView *topTableView;

@end

@implementation ZoloTopHeadView

- (instancetype)initWithShowNotice:(BOOL)isNotice {
    if (self = [super initWithFrame:CGRectZero]) {
        self = [[[NSBundle mainBundle] loadNibNamed:@"ZoloTopHeadView" owner:self options:nil] firstObject];
        int height = 64;
        if (isNotice) {
            height += 32;
        }
        self.frame = CGRectMake(0, height + iPhoneX_topH, kScreenwidth, 32);
        self.topTableView.delegate = self;
        self.topTableView.dataSource = self;
        self.topTableView.separatorStyle = UITableViewCellSeparatorStyleNone;
        [self.topTableView registerNib:[UINib nibWithNibName:@"ZoloTopMsgCell" bundle:nil] forCellReuseIdentifier:@"ZoloTopMsgCell"];
    }
    return self;
}

- (void)setDataSource:(NSArray *)dataSource {
    _dataSource = dataSource;
    [self.topTableView reloadData];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.dataSource.count > 0 ? 1 : 0;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ZoloTopMsgCell *cell = [tableView dequeueReusableCellWithIdentifier:@"ZoloTopMsgCell"];
    NSString *str = self.dataSource.lastObject;
    NSDictionary *dic = [OsnUtils json2Dics:str];
    DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:dic[@"fromUser"] refresh:NO];
    cell.textLab.text = [NSString stringWithFormat:@"%@:%@", userInfo.displayName, dic[@"data"]];
    NSString *alias = [[DMCCIMService sharedDMCIMService] getFriendAlias:userInfo.userId];
    if (alias.length > 0) {
        cell.textLab.text = [NSString stringWithFormat:@"%@:%@", alias, dic[@"data"]];
    }
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    if (_delectBtnBlock) {
        _delectBtnBlock();
    }
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 0.001;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return self.scaleHeight(24);
}

- (IBAction)tapCloseClick:(id)sender {
    if (_delectBtnBlock) {
        _delectBtnBlock();
    }
}


@end
