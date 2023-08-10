//
//  ZoloNoticeVC.m
//  NewZolo
//
//  Created by JTalking on 2022/9/15.
//

#import "ZoloSystemNoticeVC.h"
#import "ZoloSystemNoticeCell.h"
#import "EmptyVC.h"
#import "ZoloNotice.h"
#import "ZoloSeeSystemNoticeVC.h"

@interface ZoloSystemNoticeVC ()

@property(nonatomic, strong)NSMutableArray *conversations;
@property (nonatomic, strong) EmptyVC *emptyVC;

@end

@implementation ZoloSystemNoticeVC

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    [self getData];
    self.navigationItem.title = LocalizedString(@"ChatNotice");
    [self registerCellWithNibName:NSStringFromClass([ZoloSystemNoticeCell class]) isTableview:YES];
}

- (void)getData {
    NSString *language = [OSNLanguage getBaseLanguage];
    WS(ws);
    [MHAlert showLoadingStr:LocalizedString(@"loadLoading")];
    [[ZoloAPIManager instanceManager] getChatPushNoticeList:language.intValue WithCompleteBlock:^(NSArray * _Nonnull data) {
        [MHAlert dismiss];
        if (data) {
            ws.dataSource = [NSMutableArray arrayWithArray:data];
            [ws.tableView reloadData];
            if (ws.dataSource.count == 0) {
                //  没有数据
                [ws.view insertSubview:ws.emptyVC.view aboveSubview:ws.tableView];
            }else {
                [ws.emptyVC.view removeFromSuperview];
            }
        }
    }];
}

- (EmptyVC *)emptyVC {
    if (!_emptyVC) {
        _emptyVC = [[EmptyVC alloc] init];
        _emptyVC.view.frame = CGRectMake(0, 0, KScreenWidth, KScreenHeight);
    }
    return _emptyVC;
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.dataSource.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ZoloSystemNoticeCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloSystemNoticeCell class])];
    ZoloNotice *notice = self.dataSource[indexPath.row];
    cell.noticeContent.text = notice.remark;
    cell.noticeTitle.text = notice.title;
    cell.noticeTime.text = [MHDateTools timeStampToString:notice.create_time.longLongValue];
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    ZoloNotice *notice = self.dataSource[indexPath.row];
    [self.navigationController pushViewController:[[ZoloSeeSystemNoticeVC alloc] initWithNoticeInfo:notice] animated:YES];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 0.001;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 0.001;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 100;
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
