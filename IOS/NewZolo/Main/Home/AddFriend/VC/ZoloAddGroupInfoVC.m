//
//  ZoloAddGroupInfoVC.m
//  NewZolo
//
//  Created by JTalking on 2022/9/14.
//

#import "ZoloAddGroupInfoVC.h"
#import "ZoloConDetailHeadView.h"
#import "ZoloAddFriendInfoFoot.h"
#import "ZoloAddFriendVerifyVC.h"
#import "ZoloSingleChatVC.h"
#import "ZoloAddGroupCell.h"

@interface ZoloAddGroupInfoVC ()

@property (nonatomic, strong) DMCCGroupInfo *groupInfo;
@property (nonatomic, copy) NSString *resionTitle;

@end

@implementation ZoloAddGroupInfoVC

- (instancetype)initWithGroupInfo:(DMCCGroupInfo *)groupInfo {
    if (self = [super init]) {
        self.groupInfo = groupInfo;
    }
    if (groupInfo.name == nil || [groupInfo.name isEqualToString:@""]) {
        //DMCCGroupInfo *groupInfo2 = [[DMCCIMService sharedDMCIMService] getGroupInfoEx:groupInfo.target refresh:true];
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [self registerHeaderFooterWithNibName:NSStringFromClass([ZoloAddFriendInfoFoot class])];
    [self registerHeaderFooterWithNibName:NSStringFromClass([ZoloConDetailHeadView class])];
    [self registerCellWithNibName:NSStringFromClass([ZoloAddGroupCell class]) isTableview:YES];
    self.tableView.backgroundColor = [FontManage MHLineSeparatorColor];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return 1;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ZoloAddGroupCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloAddGroupCell class])];
    WS(ws);
    cell.addGroupBlock = ^(NSString *str) {
        ws.resionTitle = str;
    };
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 150;
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section {
    ZoloConDetailHeadView *headView = [tableView dequeueReusableHeaderFooterViewWithIdentifier:NSStringFromClass([ZoloConDetailHeadView class])];
    [headView.icon sd_setImageWithURL:[NSURL URLWithString:self.groupInfo.portrait] placeholderImage:SDImageDefault];
    headView.nickName.text = self.groupInfo.name;
    headView.remark.text = [NSString stringWithFormat:@"ID:%@", self.groupInfo.target];
    return  headView;
}

- (UIView *)tableView:(UITableView *)tableView viewForFooterInSection:(NSInteger)section {
    ZoloAddFriendInfoFoot *foot = [tableView dequeueReusableHeaderFooterViewWithIdentifier:NSStringFromClass([ZoloAddFriendInfoFoot class])];
    [foot.addBtn setTitle:LocalizedString(@"AddGroup") forState:UIControlStateNormal];
    WS(ws);
    foot.addBtnBLock = ^{
        [ws addUserBtnCLick];
    };
    return foot;
}

- (void)addUserBtnCLick {
    WS(ws);
    [[DMCCIMService sharedDMCIMService] addGroup:self.groupInfo.target reason:self.resionTitle invitation:@"JoinGroup" success:^{
        dispatch_async(dispatch_get_main_queue(), ^{
            [MHAlert showLoadingStr:LocalizedString(@"CallActionInvite")];
            [ws.navigationController popToRootViewControllerAnimated:YES];
        });
        
    } error:^(int error_code) {
        
    }];
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 48;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 160;
}

@end
