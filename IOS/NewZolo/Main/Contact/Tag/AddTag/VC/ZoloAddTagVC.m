//
//  ZoloAddTagVC.m
//  NewZolo
//
//  Created by JTalking on 2022/10/27.
//

#import "ZoloAddTagVC.h"
#import "ZoloUserSelectTagVC.h"
#import "ZoloTagUserCell.h"
#import "EmptyVC.h"
#import "ZoloMoveTagVC.h"

@interface ZoloAddTagVC ()

@property (nonatomic, strong) UIButton *addBtn;
@property (nonatomic, strong) UIButton *moveBtn;
@property (nonatomic, strong) EmptyVC *emptyVC;

@end

@implementation ZoloAddTagVC

- (instancetype)initWithTagInfo:(ZoloTagModel *)info {
    if (self = [super init]) {
        self.info = info;
    }
    return self;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self getData];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [self registerCellWithNibName:NSStringFromClass([ZoloTagUserCell class]) isTableview:YES];
    self.navigationItem.title = self.info.group_name;
    [self.btnView addSubview:self.addBtn];
    [self.btnView addSubview:self.moveBtn];
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:self.btnView];
}

- (UIView *)btnView {
    if (!_btnView) {
        _btnView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 84, 44)];
    }
    return _btnView;
}

- (void)getData {
    
    NSArray *users = [NSMutableArray arrayWithArray:[[DMCCIMService sharedDMCIMService] getUserWithTagId:self.info.id]];
    NSArray *groups = [NSMutableArray arrayWithArray:[[DMCCIMService sharedDMCIMService] getGroupWithTagId:self.info.id]];
    self.dataSource = [NSMutableArray arrayWithCapacity:0];
    [self.dataSource addObjectsFromArray:users];
    [self.dataSource addObjectsFromArray:groups];
    
    if (self.dataSource == nil || self.dataSource.count == 0) {
        //  没有数据
        [self.view insertSubview:self.emptyVC.view aboveSubview:self.tableView];
    }else {
        [self.emptyVC.view removeFromSuperview];
    }
    [self.tableView reloadData];
}

- (UIButton *)moveBtn {
    if (!_moveBtn) {
        _moveBtn = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, 44, 44)];
        [_moveBtn setImage:[UIImage imageNamed:@"tagMove"] forState:UIControlStateNormal];
        [_moveBtn addTarget:self action:@selector(moveBtnClick) forControlEvents:UIControlEventTouchUpInside];
    }
    return _moveBtn;
}

- (void)moveBtnClick {
    [self.navigationController pushViewController:[[ZoloMoveTagVC alloc] initWithTagInfo:self.info] animated:YES];
}

- (UIButton *)addBtn {
    if (!_addBtn) {
        _addBtn = [[UIButton alloc] initWithFrame:CGRectMake(50, 0, 44, 44)];
        [_addBtn setImage:[UIImage imageNamed:@"add"] forState:UIControlStateNormal];
        [_addBtn addTarget:self action:@selector(addBtnClick) forControlEvents:UIControlEventTouchUpInside];
    }
    return _addBtn;
}

- (EmptyVC *)emptyVC {
    if (!_emptyVC) {
        _emptyVC = [[EmptyVC alloc] init];
        _emptyVC.view.frame = CGRectMake(0, 0, KScreenWidth, KScreenHeight);
    }
    return _emptyVC;
}

- (void)addBtnClick {
    ZoloUserSelectTagVC *vc = [[ZoloUserSelectTagVC alloc] initWithTagInfo:self.info];
    [self.navigationController pushViewController:vc animated:YES];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.dataSource.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ZoloTagUserCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloTagUserCell class])];
    id info = self.dataSource[indexPath.row];
    if ([info isKindOfClass:[DMCCUserInfo class]]) {
        DMCCUserInfo *user = self.dataSource[indexPath.row];
        cell.nameLab.text = user.displayName;
        [cell.img sd_setImageWithURL:[NSURL URLWithString:user.portrait] placeholderImage:SDImageDefault];
    } else {
        DMCCGroupInfo *group = self.dataSource[indexPath.row];
        cell.nameLab.text = group.name;
        [cell.img sd_setImageWithURL:[NSURL URLWithString:group.portrait] placeholderImage:SDImageDefault];
    }

    return cell;
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
