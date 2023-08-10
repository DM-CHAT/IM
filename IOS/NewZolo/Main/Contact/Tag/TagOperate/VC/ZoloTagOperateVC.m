//
//  ZoloTagOperateVC.m
//  NewZolo
//
//  Created by MHHY on 2023/4/20.
//

#import "ZoloTagOperateVC.h"
#import "ZoloTagOperateCell.h"
#import "EmptyVC.h"
#import "ZoloTagModel.h"

@interface ZoloTagOperateVC ()

@property (nonatomic, strong) EmptyVC *emptyVC;
@property (nonatomic, strong) UIButton *addBtn;
@property (nonatomic, strong) UIButton *setBtn;
@property (nonatomic, assign) NSInteger currentIndex;
@property (nonatomic, strong) DMCCUserInfo *userInfo;
@property (nonatomic, strong) DMCCGroupInfo *groupInfo;
@property (nonatomic, assign) BOOL isUser;

@end

@implementation ZoloTagOperateVC

- (instancetype)initWithTagAddInfo:(id)info {
    if (self = [super init]) {
        if ([info isKindOfClass:[DMCCUserInfo class]]) {
            DMCCUserInfo *user = (DMCCUserInfo *)info;
            self.userInfo = user;
            self.isUser = YES;
            [self getUserInfo];
        } else if ([info isKindOfClass:[DMCCGroupInfo class]]) {
            DMCCGroupInfo *group = (DMCCGroupInfo *)info;
            self.groupInfo = group;
            self.isUser = NO;
        }
    }
    return self;
}

- (void)getUserInfo {
    WS(ws);
    [[DMCCIMService sharedDMCIMService] getUserInfo:self.userInfo.userId refresh:NO success:^(DMCCUserInfo *userInfo) {
        if (userInfo) {
            ws.userInfo = userInfo;
        }
    } error:^(int errorCode) {
        
    }];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.navigationItem.title = LocalizedString(@"Tag");
    [self registerCellWithNibName:NSStringFromClass([ZoloTagOperateCell class]) isTableview:YES];
    [self getData];
}

- (void)getData {
    WS(ws);
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [self.tableView.mj_header endRefreshing];
    });
    [[ZoloAPIManager instanceManager] addTagNameListWithCompleteBlock:^(NSArray * _Nonnull data) {
        if (data) {
            ws.dataSource = [NSMutableArray arrayWithArray:data];
            if (ws.isUser) {
                [ws getUserCurentIndex];
            } else {
                [ws getGroupCurentIndex];
            }
            
            if (ws.dataSource == nil || ws.dataSource.count == 0) {
                //  没有数据
                [ws.view insertSubview:ws.emptyVC.view aboveSubview:ws.tableView];
            }else {
                [ws.emptyVC.view removeFromSuperview];
            }
            
            if (ws.dataSource.count == 0) {
                ws.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:ws.addBtn];
            } else {
                ws.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:ws.setBtn];
            }
            
            [ws.tableView reloadData];
        }
    }];
}

- (void)getUserCurentIndex {
    for (int i = 0; i < self.dataSource.count; i++) {
        ZoloTagModel *tag = self.dataSource[i];
        if (tag.id == self.userInfo.tagID) {
            self.currentIndex = i;
            break;
        }
    }
    [self.tableView reloadData];
}

- (void)getGroupCurentIndex {
    for (int i = 0; i < self.dataSource.count; i++) {
        ZoloTagModel *tag = self.dataSource[i];
        if (tag.id == self.groupInfo.tagID) {
            self.currentIndex = i;
            break;
        }
    }
    [self.tableView reloadData];
}

- (EmptyVC *)emptyVC {
    if (!_emptyVC) {
        _emptyVC = [[EmptyVC alloc] init];
        _emptyVC.view.frame = CGRectMake(0, 0, KScreenWidth, KScreenHeight);
    }
    return _emptyVC;
}

- (UIButton *)addBtn {
    if (!_addBtn) {
        _addBtn = [[UIButton alloc] initWithFrame:CGRectMake(50, 0, 44, 44)];
        [_addBtn setImage:[UIImage imageNamed:@"add"] forState:UIControlStateNormal];
        [_addBtn addTarget:self action:@selector(addBtnClick) forControlEvents:UIControlEventTouchUpInside];
    }
    return _addBtn;
}

- (void)addBtnClick {
    [self addTagNameALert:LocalizedString(@"NewTagName") withAlerttype:MHAlertTypeRemind];
}

- (void)addTagNameALert:(NSString *)message withAlerttype:(MHAlertType)alerttype{
    NSString *title;
    switch (alerttype) {
        case MHAlertTypeWarn:
            title = LocalizedString(@"AlertWarn");
            break;
        case MHAlertTypeRemind:
            title = LocalizedString(@"AlertRemind");
            break;
    }
    UIViewController *topVC = [UIApplication sharedApplication].keyWindow.rootViewController;
    if ((topVC.presentedViewController) != nil) {
        topVC = topVC.presentedViewController;
    }
    WS(ws);
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:title message:message preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *ok = [UIAlertAction actionWithTitle:LocalizedString(@"Sure") style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [ws addTagName:alert.textFields[0].text];
    }];
    UIAlertAction *cancel = [UIAlertAction actionWithTitle:LocalizedString(@"Cancel") style:UIAlertActionStyleDefault handler:nil];
    [alert addTextFieldWithConfigurationHandler:^(UITextField * _Nonnull textField) {
        textField.placeholder = LocalizedString(@"InputNewTagName");
    }];
    [alert addAction:cancel];
    [alert addAction:ok];
    [topVC presentViewController:alert animated:YES completion:nil];
}

- (void)addTagName:(NSString *)str {
    if (str.length == 0) {
        return;
    }
    WS(ws);
    [[ZoloAPIManager instanceManager] addTagNameWithName:str type:1 osnId:@"" tagId:0 WithCompleteBlock:^(BOOL isSuccess) {
        if (isSuccess) {
            [ws getData];
        }
    }];
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
    ZoloTagModel *tag = self.dataSource[self.currentIndex];
    WS(ws);
    if (self.isUser) {
        [[ZoloAPIManager instanceManager] addTagNameWithName:tag.group_name type:2 osnId:self.userInfo.userId tagId:tag.id WithCompleteBlock:^(BOOL isSuccess) {
            if (isSuccess) {
                [ws getAll];
                [MHAlert showMessage:LocalizedString(@"AlertSuccess")];
                [ws.navigationController popToRootViewControllerAnimated:YES];
            }
        }];
    } else {
        [[ZoloAPIManager instanceManager] addTagNameWithName:tag.group_name type:3 osnId:self.groupInfo.target tagId:tag.id WithCompleteBlock:^(BOOL isSuccess) {
            if (isSuccess) {
                [ws getAll];
                [MHAlert showMessage:LocalizedString(@"AlertSuccess")];
                [ws.navigationController popToRootViewControllerAnimated:YES];
            }
        }];
    }
}

- (void)getAll {
    [[ZoloAPIManager instanceManager] getTagNameUserAllListWithCompleteBlock:^(NSArray * _Nonnull data) {
        
    }];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.dataSource.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ZoloTagOperateCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloTagOperateCell class])];
    ZoloTagModel *tag = self.dataSource[indexPath.row];
    cell.tagName.text = tag.group_name;
    if (indexPath.row == self.currentIndex) {
        cell.tagImg.image = [UIImage imageNamed:@"select_s"];
    } else {
        cell.tagImg.image = [UIImage imageNamed:@"select_n"];
    }
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    self.currentIndex = indexPath.row;
    [self.tableView reloadData];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 0.001;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 8;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return self.scaleHeight(80);
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
