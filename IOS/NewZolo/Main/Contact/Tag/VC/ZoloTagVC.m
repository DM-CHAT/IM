//
//  ZoloTagVC.m
//  NewZolo
//
//  Created by JTalking on 2022/7/11.
//

#import "ZoloTagVC.h"
#import "EmptyVC.h"
#import "ZoloTagNameCell.h"
#import "ZoloTagModel.h"
#import "ZoloAddTagVC.h"

@interface ZoloTagVC ()

@property (nonatomic, strong) EmptyVC *emptyVC;
@property (nonatomic, strong) UIButton *addBtn;

@end

@implementation ZoloTagVC

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [self getAll];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.navigationItem.title = LocalizedString(@"Tag");
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:self.addBtn];
    [self registerCellWithNibName:NSStringFromClass([ZoloTagNameCell class]) isTableview:YES];
    [self getAll];
    [self getData];
    MJRefreshNormalHeader *header = [MJRefreshNormalHeader headerWithRefreshingTarget:self refreshingAction:@selector(getData)];
    header.lastUpdatedTimeLabel.hidden = YES;
    header.stateLabel.hidden = YES;
    self.tableView.mj_header = header;
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

- (void)getData {
    WS(ws);
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [self.tableView.mj_header endRefreshing];
    });
    [[ZoloAPIManager instanceManager] addTagNameListWithCompleteBlock:^(NSArray * _Nonnull data) {
        if (data) {
            ws.dataSource = [NSMutableArray arrayWithArray:data];
            
            if (ws.dataSource == nil || ws.dataSource.count == 0) {
                //  没有数据
                [ws.view insertSubview:ws.emptyVC.view aboveSubview:ws.tableView];
            }else {
                [ws.emptyVC.view removeFromSuperview];
            }
            
            [ws.tableView reloadData];
        }
    }];
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
    ZoloTagNameCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloTagNameCell class])];
    ZoloTagModel *tag = self.dataSource[indexPath.row];
    cell.tagName.text = tag.group_name;
    cell.tagModel =tag;
    
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    ZoloTagModel *tag = self.dataSource[indexPath.row];
    [self.navigationController pushViewController:[[ZoloAddTagVC alloc] initWithTagInfo:tag] animated:YES];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 0.001;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 8;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return self.scaleHeight(120);
}

- (EmptyVC *)emptyVC {
    if (!_emptyVC) {
        _emptyVC = [[EmptyVC alloc] init];
        _emptyVC.view.frame = CGRectMake(0, 0, KScreenWidth, KScreenHeight);
    }
    return _emptyVC;
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

// 编辑删除操作
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath {
    return YES;
}

- (NSArray*)tableView:(UITableView *)tableView editActionsForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewRowAction *action = [UITableViewRowAction rowActionWithStyle:UITableViewRowActionStyleDefault title:LocalizedString(@"MsgDel") handler:^(UITableViewRowAction * _Nonnull action, NSIndexPath * _Nonnull indexPath) {
        WS(ws);
        NSString *str = LocalizedString(@"AlertSureDelTag");
        [MHAlert showCustomeAlert:str withAlerttype:MHAlertTypeWarn withOkBlock:^(UIAlertAction *action) {
            if ([action.title isEqualToString:LocalizedString(@"Sure")]) {
                [ws removeUserItem:indexPath];
            }
        }];
    }];
    action.backgroundColor = [FontManage MHMsgRecColor];
    return @[action];
}

- (void)removeUserItem:(NSIndexPath *)indexPath {
    ZoloTagModel *tag = self.dataSource[indexPath.row];
    WS(ws);
    [[ZoloAPIManager instanceManager] delTagId:tag.id WithCompleteBlock:^(BOOL isSuccess) {
        if (isSuccess) {
            DMCCTagInfo *info = [DMCCTagInfo new];
            info.group_name = tag.group_name;
            [[DMCCIMService sharedDMCIMService] deleteTagWithInfo:info];
            [ws getData];
        }
    }];
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
