//
//  MHParentViewController.m
//  JTalking
//
//  Created by JTalking on 2022/6/24.
//

#import "MHParentViewController.h"

@interface MHParentViewController () <UITableViewDelegate, UITableViewDataSource, UICollectionViewDelegate, UICollectionViewDataSource, UIGestureRecognizerDelegate>

@end

@implementation MHParentViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = [FontManage MHWhiteColor];
    self.page = 1;
    _tableStytle = UITableViewStyleGrouped;
    self.navigationController.interactivePopGestureRecognizer.delegate = (id)self;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
}

- (UITableView *)tableView {
    if (!_tableView) {
        _tableView = [[UITableView alloc] initWithFrame:CGRectMake(0, 0, KScreenWidth, KScreenHeight) style:_tableStytle];
        _tableView.delegate = self;
        _tableView.dataSource = self;
        [_tableView setSectionIndexBackgroundColor:[UIColor clearColor]];
        [_tableView setSectionIndexColor:[UIColor darkGrayColor]];
        _tableView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
        _tableView.separatorColor = [FontManage MHLineSeparatorColor];
        _tableView.backgroundColor = [UIColor clearColor];
        if (Sys_Version >= 11) {
            _tableView.estimatedRowHeight = 0;
            _tableView.estimatedSectionFooterHeight = 0;
            _tableView.estimatedSectionHeaderHeight = 0;
        }
        [self.view addSubview:_tableView];
    }
    return _tableView;
}

- (UICollectionView *)collectionView {
    if (!_collectionView) {
        _collectionView = [[UICollectionView alloc] initWithFrame:CGRectMake(0, 0, KScreenWidth, KScreenHeight) collectionViewLayout:_layout];
        _collectionView.dataSource = self;
        _collectionView.backgroundColor = [UIColor clearColor];
        _collectionView.delegate = self;
        [self.view addSubview:_collectionView];
    }
    return _collectionView;
}

- (UICollectionViewFlowLayout *)layout {
    if (!_layout) {
        _layout = [[UICollectionViewFlowLayout alloc] init];
    }
    return _layout;
}

// 加载数据
- (void)getData {
    
}

- (void)handelDatas:(NSArray *)datas withPageCount:(NSInteger)pageCount {
//    if (_page == 1 && (datas == nil || datas.count == 0)) {
//        //  没有数据
//        [self.view insertSubview:self.emptyVC.view aboveSubview:_tableView];
//    }else {
//        [self.emptyVC.view removeFromSuperview];
//    }
    if (_page > pageCount) {
        [self.tableView.mj_footer endRefreshingWithNoMoreData];
        return;
    }
    if (_page == 1) {
        _dataSource = [NSMutableArray arrayWithArray:datas];
        [self.tableView.mj_footer endRefreshing];
    }else {
        if (datas == nil || datas.count == 0) {
            //  没有更多数据
            [self.tableView.mj_footer endRefreshingWithNoMoreData];
        }else {
            [_dataSource addObjectsFromArray:datas];
            [self.tableView.mj_footer endRefreshing];
        }
    }
    [self.tableView reloadData];
    [self.tableView.mj_header endRefreshing];
}

- (void)handelCollectionDatas:(NSArray *)datas withPageCount:(NSInteger)pageCount {
//    if (_page == 1 && (datas == nil || datas.count == 0)) {
//        //  没有数据
//        [self.view addSubview:self.emptyVC.view];
//    }else {
//        [self.emptyVC.view removeFromSuperview];
//    }
    if (_page > pageCount) {
        [self.collectionView.mj_footer endRefreshingWithNoMoreData];
        return;
    }
    if (_page == 1) {
        _dataSource = [NSMutableArray arrayWithArray:datas];
        [self.collectionView.mj_footer endRefreshing];
    }else {
        if (datas == nil || datas.count == 0) {
            //  没有更多数据
            [self.collectionView.mj_footer endRefreshingWithNoMoreData];
        }else {
            [_dataSource addObjectsFromArray:datas];
            [self.collectionView.mj_footer endRefreshing];
        }
    }
    [self.collectionView reloadData];
    [self.collectionView.mj_header endRefreshing];
}

- (void)setupRefreshTableView {
    self.tableView.mj_header = [MJRefreshNormalHeader headerWithRefreshingTarget:self refreshingAction:@selector(refreshData)];
    self.tableView.mj_footer = [MJRefreshBackNormalFooter footerWithRefreshingTarget:self refreshingAction:@selector(loadMoreData)];
}

- (void)setupRefreshCollcetionView {
    self.collectionView.mj_header = [MJRefreshNormalHeader headerWithRefreshingTarget:self refreshingAction:@selector(refreshData)];
    self.collectionView.mj_footer = [MJRefreshBackNormalFooter footerWithRefreshingTarget:self refreshingAction:@selector(loadMoreData)];
}

- (void)refreshData {
    self.page = 1;
    [self getData];
}

- (void)loadMoreData {
    self.page = self.page + 1;
    [self getData];
}

// tableView
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return 0;
}
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    return nil;
}


// colletionView
- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return 0;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    return nil;
}

- (void)scrollViewWillBeginDragging:(UIScrollView *)scrollView {
    [self.view endEditing:YES];
}

-(UIScrollView *)scrollView {
    if (!_scrollView) {
        _scrollView = [[UIScrollView alloc] initWithFrame:CGRectMake(0, 0, KScreenWidth, self.view.height)];
        _scrollView.delegate = self;
        _scrollView.contentSize = CGSizeMake(KScreenWidth, KScreenHeight + 1);
        [self.view addSubview:_scrollView];
    }
    return _scrollView;
}

-(NSMutableArray *)dataSource {
    if (!_dataSource) {
        _dataSource = [NSMutableArray array];
    }
    return _dataSource;
}

- (void)registerCellWithNibName:(NSString *)nibName isTableview:(BOOL)isTableView {
    if (isTableView) {
        [self.tableView registerNib:[UINib nibWithNibName:nibName bundle:nil] forCellReuseIdentifier:nibName];
    } else {
        [self.collectionView registerNib:[UINib nibWithNibName:nibName bundle:nil] forCellWithReuseIdentifier:nibName];
    }
}

- (void)registerCellWithClassName:(NSString *)className isTableview:(BOOL)isTableView {
    if (isTableView) {
        [self.tableView registerClass:[className class] forCellReuseIdentifier:className];
    } else {
        [self.collectionView registerClass:[className class] forCellWithReuseIdentifier:className];
    }
}

- (void)registerHeaderFooterWithClassName:(NSString *)className {
    [self.tableView registerClass:[className class] forHeaderFooterViewReuseIdentifier:className];
}

- (void)registerHeaderFooterWithNibName:(NSString *)nibName {
    [self.tableView registerNib:[UINib nibWithNibName:nibName bundle:nil] forHeaderFooterViewReuseIdentifier:nibName];
}

- (void)registerHeaderWithClassName:(NSString *)className {
    [self.collectionView registerClass:[className class] forSupplementaryViewOfKind:UICollectionElementKindSectionHeader withReuseIdentifier:className];
}

- (void)registerFooterWithClassName:(NSString *)className {
    [self.collectionView registerClass:[className class] forSupplementaryViewOfKind:UICollectionElementKindSectionFooter withReuseIdentifier:className];
}

- (void)registerHeaderWithNibName:(NSString *)nibName {
    [self.collectionView registerNib:[UINib nibWithNibName:nibName bundle:nil] forSupplementaryViewOfKind:UICollectionElementKindSectionHeader withReuseIdentifier:nibName];
}

- (void)registerFooterWithNibName:(NSString *)nibName {
    [self.collectionView registerNib:[UINib nibWithNibName:nibName bundle:nil] forSupplementaryViewOfKind:UICollectionElementKindSectionFooter withReuseIdentifier:nibName];
}

- (void)collectionView:(UICollectionView *)collectionView willDisplaySupplementaryView:(UICollectionReusableView *)view forElementKind:(NSString *)elementKind atIndexPath:(NSIndexPath *)indexPath {
    if (@available(iOS 11.0, *)) {
        if ([elementKind isEqualToString:UICollectionElementKindSectionHeader]) {
            view.layer.zPosition = 0;
        }
    }
}

- (void)dealloc {
    [ZoloHttpTool cancelRequest];
    MHLog(@"======dealloc===%@===", [self class]);
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
