//
//  ZoloServiceVC.m
//  NewZolo
//
//  Created by JTalking on 2022/10/7.
//

#import "ZoloServiceVC.h"
#import "ZoloServiceCell.h"
#import "ZoloServiceHead.h"
#import "ZoloServiceModel.h"
#import "AppDelegate.h"
#import "ZOLOLoginViewController.h"
#import <config/config.h>
#import "ZoloEulaView.h"
#import "LSTPopView.h"
#import "ZoloEulaConfirView.h"

@interface ZoloServiceVC ()

@end

@implementation ZoloServiceVC

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self.navigationController setNavigationBarHidden:YES animated:animated];
    
    NSString *language = [[NSUserDefaults standardUserDefaults]objectForKey:@"ospn_language"];
    if (language.length > 0) {
        [OSNLanguage setLanguage:language];
    } else {
        [OSNLanguage setLanguage:[self getLaungue]];
    }
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [self getData];
    });
}

- (void)showEulaView {
    ZoloEulaView *view = [[ZoloEulaView alloc] initWithFrame:CGRectZero];
    view.frame = CGRectMake(0, 0, kScreenwidth*0.8, KScreenheight/2);
    view.layer.cornerRadius = 16;
    view.layer.masksToBounds = YES;
    LSTPopView *popView = [LSTPopView initWithCustomView:view
                                                popStyle:LSTPopStyleSmoothFromTop
                                            dismissStyle:LSTDismissStyleSmoothToBottom];
    popView.popStyle = LSTPopStyleNO;
    popView.dismissStyle = LSTDismissStyleNO;
    popView.popDuration = 1.0;
    popView.dismissDuration = 1.0;
    LSTPopViewWK(popView)
    WS(ws);
    view.noticeBtnBlock = ^(BOOL isAgree) {
        [wk_popView dismiss];
        if (isAgree) {
            
        } else {
            [ws showEulaConfirmView];
        }
    };
    [popView pop];
}

- (void)showEulaConfirmView {
    ZoloEulaConfirView *view = [[ZoloEulaConfirView alloc] initWithFrame:CGRectZero];
    view.frame = CGRectMake(0, 0, kScreenwidth*0.8, KScreenheight/3);
    view.layer.cornerRadius = 16;
    view.layer.masksToBounds = YES;
    LSTPopView *popView = [LSTPopView initWithCustomView:view
                                                popStyle:LSTPopStyleSmoothFromTop
                                            dismissStyle:LSTDismissStyleSmoothToBottom];
    popView.popStyle = LSTPopStyleNO;
    popView.dismissStyle = LSTDismissStyleNO;
    popView.popDuration = 1.0;
    popView.dismissDuration = 1.0;
    LSTPopViewWK(popView)
    WS(ws);
    view.noticeBtnBlock = ^(BOOL isAgree) {
        [wk_popView dismiss];
        if (isAgree) {
            [ws showEulaView];
        } else {
            exit(0);
        }
    };
    [popView pop];
}

- (NSString *)getLaungue {
    NSString *languageCode = [NSLocale preferredLanguages][0];// 返回的也是国际通用语言Code+国际通用国家地区代码
    NSString *countryCode = [NSString stringWithFormat:@"-%@", [[NSLocale currentLocale] objectForKey:NSLocaleCountryCode]];
    if (languageCode) {
        languageCode = [languageCode stringByReplacingOccurrencesOfString:countryCode withString:@""];
    }
    if ([languageCode isEqualToString:@"zh-Hans"]) {
        return @"0";
    } else if ([languageCode isEqualToString:@"vi"]) {
        return @"1";
    } else if ([languageCode isEqualToString:@"en"]) {
        return @"2";
    } else {
        return @"2";
    }
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.layout.minimumLineSpacing = 20;
    self.layout.minimumInteritemSpacing = 20;
    self.layout.scrollDirection = UICollectionViewScrollDirectionVertical;
    self.collectionView.backgroundColor = [UIColor clearColor];
    [self registerCellWithNibName:NSStringFromClass([ZoloServiceCell class]) isTableview:NO];
    UIImage *image = [UIImage imageNamed:@"lbg"];
    self.view.layer.contents = (id) image.CGImage;
    self.view.layer.backgroundColor = [UIColor clearColor].CGColor;
    
    [self registerHeaderWithNibName:NSStringFromClass([ZoloServiceHead class])];
    [self getData];
    MJRefreshNormalHeader *header = [MJRefreshNormalHeader headerWithRefreshingTarget:self refreshingAction:@selector(getData)];
    header.lastUpdatedTimeLabel.hidden = YES;
    header.stateLabel.hidden = YES;
    self.collectionView.mj_header = header;
    self.collectionView.contentInset = UIEdgeInsetsMake(-20, 0, 0, 0);
    
    [self showEulaView];
}

- (void)getData {
    WS(ws);
    [[ZoloAPIManager instanceManager] getServiceListWithCompleteBlock:^(NSArray * _Nonnull data) {
        [ws.collectionView.mj_header endRefreshing];
        if (data) {
            ws.dataSource = [NSMutableArray arrayWithArray:data];
            [ws.collectionView reloadData];
        }
    }];
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return self.dataSource.count;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    ZoloServiceCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:NSStringFromClass([ZoloServiceCell class]) forIndexPath:indexPath];
    ZoloServiceModel *model = self.dataSource[indexPath.item];
    [cell.iocn sd_setImageWithURL:[NSURL URLWithString:model.iconUrl] placeholderImage:SDImageDefault];
    cell.nameLab.text = model.serviceName;
    return cell;
}

/**
 
 1. if input im:http://xxxxxx
            remove im:    open web
 else
        output     “no server”
 
 */

- (UICollectionReusableView *)collectionView:(UICollectionView *)collectionView viewForSupplementaryElementOfKind:(NSString *)kind atIndexPath:(NSIndexPath *)indexPath {
    ZoloServiceHead *headFootView = [collectionView dequeueReusableSupplementaryViewOfKind:UICollectionElementKindSectionHeader withReuseIdentifier:NSStringFromClass([ZoloServiceHead class]) forIndexPath:indexPath];
    WS(ws);
    headFootView.searchBlock = ^(NSString *searchStr) {
        if ([searchStr hasPrefix:@"im:http"]) {
            [ws.navigationController pushViewController:[[ZOLOLoginViewController alloc]initWithUrlInfo:[searchStr substringFromIndex:3]] animated:YES];
        } else {
            [MHAlert showMessage:@"no server"];
        }
    };
    return headFootView;
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    ZoloServiceModel *model = self.dataSource[indexPath.item];
    
    [self.navigationController pushViewController:[[ZOLOLoginViewController alloc]initWithUrlInfo:model.url] animated:YES];
}

- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout referenceSizeForHeaderInSection:(NSInteger)section {
    return CGSizeMake(0, 150);
}

- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath {
    ZoloServiceModel *model = self.dataSource[indexPath.item];
    if (model.status == 1) {
        return CGSizeMake((kScreenwidth - 60) / 2, 60);
    } else {
        return CGSizeMake(0.001, 0.001);
    }
    
}

- (UIEdgeInsets)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout insetForSectionAtIndex:(NSInteger)section {
    return UIEdgeInsetsMake(20, 20, 20, 20);
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
