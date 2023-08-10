//
//  MHParentViewController.h
//  JTalking
//
//  Created by JTalking on 2022/6/24.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface MHParentViewController : UIViewController

@property (assign, nonatomic) NSInteger page;
@property (nonatomic, strong) UITableView *tableView;
@property (nonatomic, strong) UICollectionViewFlowLayout *layout;
@property (nonatomic, strong) UICollectionView *collectionView;
@property (nonatomic, strong) NSMutableArray *dataSource;
@property (nonatomic, assign) UITableViewStyle tableStytle;
@property (nonatomic, strong) UIScrollView *scrollView;
@property (nonatomic,strong)UIButton *backBtn;

- (void)setupNaviBarColor:(UIColor *)bgColor titleColor:(UIColor *)titleColor;
- (void)backBtnAction;

- (void)handelDatas:(NSArray *)datas withPageCount:(NSInteger)pageCount;
- (void)handelCollectionDatas:(NSArray *)datas withPageCount:(NSInteger)pageCount;
- (void)refreshData;
- (void)loadMoreData;
- (void)setupRefreshTableView;
- (void)setupRefreshCollcetionView;
- (void)getData;

/** tableView or collectionView cell */
- (void)registerCellWithNibName:(NSString *)nibName isTableview:(BOOL)isTableView;
- (void)registerCellWithClassName:(NSString *)className isTableview:(BOOL)isTableView;


/** tableView headFooterClass */
- (void)registerHeaderFooterWithClassName:(NSString *)className;
/** tableView headFooterNib */
- (void)registerHeaderFooterWithNibName:(NSString *)nibName;

/** collectionView HeadClass */
- (void)registerHeaderWithClassName:(NSString *)className;
/** collectionView FooterClass */
- (void)registerFooterWithClassName:(NSString *)className;
/** collectionView HeadNib */
- (void)registerHeaderWithNibName:(NSString *)nibName;
/** collectionView FooterNib */
- (void)registerFooterWithNibName:(NSString *)nibName;

@end

NS_ASSUME_NONNULL_END
