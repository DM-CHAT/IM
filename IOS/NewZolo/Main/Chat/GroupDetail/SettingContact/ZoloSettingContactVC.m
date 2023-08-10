//
//  ZoloSettingContactVC.m
//  NewZolo
//
//  Created by JTalking on 2022/8/23.
//

#import "ZoloSettingContactVC.h"
#import "ZoloSettingContactCell.h"

@interface ZoloSettingContactVC ()

@property (nonatomic, strong) DMCCGroupInfo *info;
@property (nonatomic, strong) UIButton *setBtn;
@property (nonatomic, assign) NSInteger type;

@end

@implementation ZoloSettingContactVC

// type 1 管理员  2 群成员
- (instancetype)initWithGroupInfo:(DMCCGroupInfo *)info withType:(NSInteger)type {
    if (self = [super init]) {
        self.info = info;
        self.type = type;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [self getData];
    [self registerCellWithNibName:NSStringFromClass([ZoloSettingContactCell class]) isTableview:YES];
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:self.setBtn];
}

- (UIButton *)setBtn {
    if (!_setBtn) {
        _setBtn = [[UIButton alloc] initWithFrame:CGRectZero];
        [_setBtn setTitle:LocalizedString(@"Sure") forState:UIControlStateNormal];
        [_setBtn setTitleColor:[FontManage MHBlockColor] forState:UIControlStateNormal];
        [_setBtn addTarget:self action:@selector(setBtnClick) forControlEvents:UIControlEventTouchUpInside];
    }
    return _setBtn;
}

- (void)setBtnClick {
    NSMutableArray *array = [[NSMutableArray alloc] initWithCapacity:0];
    for (DMCCGroupMember *member in self.dataSource) {
        if (member.isSelect) {
            [array addObject:member];
        }
    }
    if (_confirmBlock) {
        _confirmBlock(array);
    }
    [self.navigationController popViewControllerAnimated:YES];
}

- (void)getData {
    NSArray *memberList = [[DMCCIMService sharedDMCIMService] getGroupMembers:self.info.target forceUpdate:NO];
    if (self.type == 1) {
        for (DMCCGroupMember *member in memberList) {
            if (member.type == Member_Type_Normal && ![member.memberId isEqualToString:self.info.owner]) {
                [self.dataSource addObject:member];
            }
        }
    } else if (self.type == 3) {
        for (DMCCGroupMember *member in memberList) {
            if (![member.memberId isEqualToString:[DMCCIMService sharedDMCIMService].getUserID]) {
                [self.dataSource addObject:member];
            }
        }
    } else {
        for (DMCCGroupMember *member in memberList) {
            if (member.mute != 1 && member.type != Member_Type_Manager && ![member.memberId isEqualToString:self.info.owner]) {
                [self.dataSource addObject:member];
            }
        }
    }
    
    [self.tableView reloadData];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.dataSource.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ZoloSettingContactCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloSettingContactCell class])];
    DMCCGroupMember *member = self.dataSource[indexPath.row];
    DMCCUserInfo *user = [[DMCCIMService sharedDMCIMService] getUserInfo:member.memberId inGroup:member.groupId refresh:NO];
    [cell.icon sd_setImageWithURL:[NSURL URLWithString:user.portrait] placeholderImage:SDImageDefault];
    cell.nickName.text = user.displayName;
    cell.selectImg.image = member.isSelect ? [UIImage imageNamed:@"select_s"] : [UIImage imageNamed:@"select_n"];
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    DMCCGroupMember *member = self.dataSource[indexPath.row];
    member.isSelect = !member.isSelect;
    [self.tableView reloadData];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 0.001;
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
