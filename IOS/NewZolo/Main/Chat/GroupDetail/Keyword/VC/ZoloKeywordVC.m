//
//  ZoloKeywordVC.m
//  NewZolo
//
//  Created by JTalking on 2022/10/29.
//

#import "ZoloKeywordVC.h"
#import "ZoloKeywordCell.h"
#import "ZolokeywordView.h"
#import "ZoloKeywordModel.h"

@interface ZoloKeywordVC ()

@property (nonatomic, strong) ZolokeywordView *keywordView;
@property (nonatomic, strong) UIButton *addBtn;
@property (nonatomic, strong) DMCCGroupInfo *info;

@end

@implementation ZoloKeywordVC

- (instancetype)initWithGroupInfo:(DMCCGroupInfo *)info {
    if (self = [super init]) {
        self.info = info;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    self.tableView.tableHeaderView = self.keywordView;
    [self registerCellWithNibName:NSStringFromClass([ZoloKeywordCell class]) isTableview:YES];
    [self getData];
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:self.addBtn];
}

- (void)getData {
    WS(ws);
    [[ZoloAPIManager instanceManager] getKeywordListAndGroupMuteTimeWithGroupId:self.info.target WithCompleteBlock:^(ZoloKeywordModel * _Nonnull data) {
        if (data) {
            ws.dataSource = [NSMutableArray arrayWithArray:data.keywordList];
            [ws.tableView reloadData];
        }
    }];
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
    [self addTagNameALert:LocalizedString(@"SetingOneKeyword") withAlerttype:MHAlertTypeRemind];
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
        textField.placeholder = LocalizedString(@"SetingInputKeyword");
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
    [[DMCCIMService sharedDMCIMService] getOwnerSign:self.info.target cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        if (isSuccess) {
            [ws setGroupSing:json content:str];
        }
    }];
}

- (void)setGroupSing:(NSDictionary *)dic content:(NSString *)str {
    WS(ws);
    [[ZoloAPIManager instanceManager] setGroupKeywordWithGroupId:self.info.target content:str license:[OsnUtils dic2Json:dic] WithCompleteBlock:^(BOOL isSuccess) {
        if (isSuccess) {
            [ws getData];
        }
    }];
}

- (ZolokeywordView *)keywordView {
    if (!_keywordView) {
        _keywordView = [[ZolokeywordView alloc] initWithFrame:CGRectZero];
        
    }
    return _keywordView;
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.dataSource.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ZoloKeywordCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloKeywordCell class])];
    ZoloKeywdModel *model = self.dataSource[indexPath.row];
    cell.idLab.text = [NSString stringWithFormat:@"%ld", indexPath.row + 1];
    cell.nameLab.text = model.content;
    WS(ws);
    cell.delBtnBlock = ^{
        [ws delBtnClick:indexPath.row];
    };
    
    return cell;
}

- (void)delBtnClick:(NSInteger)index {
    ZoloKeywdModel *model = self.dataSource[index];
    WS(ws);
    [[ZoloAPIManager instanceManager] deleteKeywordWithGroupId:self.info.target keywordId:model.id WithCompleteBlock:^(BOOL isSuccess) {
        if (isSuccess) {
            [ws getData];
        }
    }];
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
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
