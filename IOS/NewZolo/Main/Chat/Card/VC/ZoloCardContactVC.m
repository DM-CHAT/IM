//
//  ZoloCardContactVC.m
//  NewZolo
//
//  Created by JTalking on 2022/9/2.
//

#import "ZoloCardContactVC.h"
#import "ZoloCardGroupVC.h"
#import "ZoloCardSmallProgramVC.h"
#import "ZoloCardContactHeadView.h"

@interface ZoloCardContactVC ()

@property (nonatomic, strong) UIButton *closeBtn;
@property (nonatomic, strong) DMCCUserInfo *userInfo;
@property (nonatomic, strong) DMCCGroupInfo *groupInfo;
@property (nonatomic, strong) ZoloCardContactHeadView *contactHeadView;
@property (nonatomic, assign) BOOL isGroup;

@end

@implementation ZoloCardContactVC

- (instancetype)initWithUserInfo:(id)info {
    if (self = [super init]) {
        if ([info isKindOfClass:[DMCCUserInfo class]]) {
            self.userInfo = (DMCCUserInfo*)info;
            self.isGroup = NO;
        } else if ([info isKindOfClass:[DMCCGroupInfo class]]) {
            self.groupInfo = (DMCCGroupInfo*)info;
            self.isGroup = YES;
        }
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.tableView.tableHeaderView = self.contactHeadView;
    self.navigationItem.title = LocalizedString(@"ChatSelectCard");
    self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:self.closeBtn];
}

- (void)getData:(BOOL)isFresh {
    [self.dataSource removeAllObjects];
    NSMutableArray *friendArray = [NSMutableArray arrayWithArray:[[DMCCIMService sharedDMCIMService] getMyFriendList:isFresh]];
    for (NSString *string in friendArray) {
        [self getUserInfo:string array:friendArray.count];
    }
    if (friendArray.count == 0) {
        [self.tableView.mj_header endRefreshing];
    }
}

- (void)getUserInfo:(NSString *)friendId array:(NSInteger)arrayCount {
    if ([friendId isEqualToString:self.userInfo.userId]) {
        return;
    }
    WS(ws);
    [[DMCCIMService sharedDMCIMService] getUserInfo:friendId refresh:NO success:^(DMCCUserInfo *userInfo) {
        if (userInfo) {
            [ws.dataSource addObject:userInfo];
            if (ws.dataSource.count == (self.isGroup ? arrayCount : arrayCount - 1)) {
                [ws sortData];
            }
        }
    } error:^(int errorCode) {
        
    }];
}

- (ZoloCardContactHeadView *)contactHeadView {
    if (!_contactHeadView) {
        _contactHeadView = [[ZoloCardContactHeadView alloc] initWithFrame:CGRectZero];
        WS(ws);
        _contactHeadView.selectedBlock = ^(NSInteger index) {
            [ws pushToVCWithIndex:index];
        };
    }
    return _contactHeadView;
}

- (void)pushToVCWithIndex:(NSInteger)index {
    WS(ws);
    switch (index) {
        case 0:  // 群聊
        {
            ZoloCardGroupVC *vc = [[ZoloCardGroupVC alloc] initWithGroupInfo:self.groupInfo];
            vc.groupSelectBlock = ^(DMCCGroupInfo * _Nonnull groupInfo) {
                [ws groupSelectClick:groupInfo];
            };
            [self.navigationController pushViewController:vc animated:YES];
        }
            break;
        case 1:  // 小程序
        {
            ZoloCardSmallProgramVC *vc = [ZoloCardSmallProgramVC new];
            vc.litAppSelectBlock = ^(DMCCLitappInfo * _Nonnull litAppInfo) {
                [ws litAppSelectClick:litAppInfo];
            };
            [self.navigationController pushViewController:vc animated:YES];
        }
            break;
        default:
            break;
    }
}

- (void)groupSelectClick:(DMCCGroupInfo *)groupInfo {
    if (_cardGroupReturnBlock) {
        _cardGroupReturnBlock(groupInfo);
    }
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (void)litAppSelectClick:(DMCCLitappInfo *)litAppInfo {
    if (_cardLitAppReturnBlock) {
        _cardLitAppReturnBlock(litAppInfo);
    }
    [self dismissViewControllerAnimated:YES completion:nil];
}


- (UIButton *)closeBtn {
    if (!_closeBtn) {
        _closeBtn = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, 44, 44)];
        [_closeBtn setImage:[UIImage imageNamed:@"close_off"] forState:UIControlStateNormal];
        [_closeBtn addTarget:self action:@selector(closeBtnClick) forControlEvents:UIControlEventTouchUpInside];
    }
    return _closeBtn;
}

- (void)closeBtnClick {
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    NSArray *value = [self.allDataSource objectForKey:self.indexDataSource[indexPath.section]];
    DMCCUserInfo *user = value[indexPath.row];
    if (_cardReturenBlock) {
        _cardReturenBlock(user);
    }
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
