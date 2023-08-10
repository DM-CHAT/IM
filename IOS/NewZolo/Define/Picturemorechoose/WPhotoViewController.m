//
//  WPhotoViewController.m
//  图片多选实现
//
//  Created by fzjy-ios on 2018/6/8.
//  Copyright © 2018年 fzjy-ios.phonewefwf. All rights reserved.
//

#import "WPhotoViewController.h"
#import <Photos/Photos.h>
#import <AssetsLibrary/AssetsLibrary.h>
#import "WPMacros.h"
#import "MyPhotoCell.h"
#import "WPFunctionView.h"


@interface WPhotoViewController ()<UICollectionViewDelegate,UICollectionViewDelegateFlowLayout,UICollectionViewDataSource>
@property (strong, nonatomic) UICollectionView *ado_collectioView;//!< 显示照片控件
@property (strong, nonatomic) NSMutableArray *allPhotoArr;//!< 所有照片组内的url数组（内部是最大的相册的照片url，这个相册一般名字是 所有照片或All Photos）
@property (strong, nonatomic) UIButton *finishBtn;//!< 完成的Btn
@property (strong, nonatomic) NSMutableArray *photoGroupArr;//!< 所有照片组的数组（内部是所有相册的组）
@property (strong, nonatomic) NSMutableArray *chooseCellArray;//!< 所选的图片所在cell的序列号
@property (strong, nonatomic) NSMutableArray *choosePhotoArr;
@property (strong, nonatomic) PHCachingImageManager *imageManager;
@property (strong, nonatomic) NSMutableArray *chooseArray;//!< 所选择的图片数组

@end

@implementation WPhotoViewController

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self.navigationController setNavigationBarHidden:NO animated:animated];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = [UIColor whiteColor];
    [self setNav];
    [self.view addSubview:[self finishBtn]];
    [self.view addSubview:[self ado_collectioView]];
    [self getAllPhotos];
}
#pragma mark FinishButton
-(UIButton *)finishBtn {
    if (!_finishBtn) {
        UIButton *finishBtn = [UIButton buttonWithType:UIButtonTypeCustom];
        finishBtn.frame = CGRectMake(0, SelfView_H-TabBar_H-navView_H, SelfView_W, TabBar_H);
        finishBtn.backgroundColor = UIColorFromRGB(0xf9f9f9);
        finishBtn.layer.borderWidth = 0.5;
        [finishBtn addTarget:self action:@selector(finishChoosePhotos:) forControlEvents:(UIControlEventTouchUpInside)];
        _finishBtn = finishBtn;
        [self finishColorAndTextChange:_choosePhotoArr.count];
    }
    return _finishBtn;
}
//完成按钮
- (void)finishChoosePhotos:(UIButton *)finishBtn
{
    NSString *finishStr = [NSString stringWithFormat:@"0/%ld 完成",(long)_selectPhotoOfMax];
    if (![finishBtn.titleLabel.text isEqualToString:finishStr] && _chooseArray.count) {
        //所选的图片数组传过来
        [WPFunctionView finishChoosePhotos:^(NSMutableArray *mychoosePhotoArr) {
            //把图片数组回调传给调用控制界面
            _selectPhotoBack(mychoosePhotoArr);
//            [self btnClickBack];
            [self.navigationController popViewControllerAnimated:NO];
        } chooseArray:_chooseArray];
    }
    
}

#pragma mark GetAllPhotos
- (void)getAllPhotos
{
    if (phoneVersion.integerValue >= 8) {
        //高版本使用photoKit框架
        [self getHeightVersionAllPhotos];
    }else{
        //低版本使用ALAssetsLibrary框架
        [self getLowVersionAllPhotos];
    }
}
#pragma mark 高版本使用PhotoKit框架
- (void)getHeightVersionAllPhotos
{
    [WPFunctionView getHeightVersionAllPhotos:^(PHFetchResult *allPhoto) {
        _imageManager = [[PHCachingImageManager alloc] init];
        if (!_allPhotoArr) {
            _allPhotoArr = [[NSMutableArray alloc] init];
        }
        for (NSInteger i = 0; i < allPhoto.count; i++) {
            PHAsset *asset = allPhoto[i];
            if (asset.mediaType == PHAssetMediaTypeImage) {
                [_allPhotoArr addObject:asset];
            }
            NSString *cellId = [NSString stringWithFormat:@"cell%ld",(long)i];
            [self.ado_collectioView registerClass:[MyPhotoCell class] forCellWithReuseIdentifier:cellId];
        }
        [self.ado_collectioView reloadData];
    }];
}
#pragma mark 低版本使用ALAssetsLibrary框架
- (void)getLowVersionAllPhotos
{
    [WPFunctionView getLowVersionAllPhotos:^(ALAssetsGroup *group) {
        if (!_photoGroupArr) {
            _photoGroupArr = [[NSMutableArray alloc] init];
        }
        if (group != nil) {
            [_photoGroupArr addObject:group];
        }else{
            ALAssetsGroup *allPhotoGroup = _photoGroupArr[_photoGroupArr.count -1];
            if (!_allPhotoArr) {
                _allPhotoArr = [[NSMutableArray alloc] init];
            }
            //获取相册分组里面的照片内容
            [allPhotoGroup enumerateAssetsUsingBlock:^(ALAsset *result, NSUInteger index, BOOL *stop) {
                if (result&&[[result valueForProperty:ALAssetPropertyType]isEqualToString:ALAssetTypePhoto]) {
                    //照片内容url加入数组
                    [_allPhotoArr addObject:result.defaultRepresentation.url];
                }else{
                    //刷新显示
                    if (_allPhotoArr.count) {
                        for (NSInteger i = 0; i<_allPhotoArr.count; i++) {
                            NSString *cellId = [NSString stringWithFormat:@"cell%ld", (long)i];
                            [self.ado_collectioView registerClass:[MyPhotoCell class] forCellWithReuseIdentifier:cellId];
                        }
                        [self.ado_collectioView reloadData];
                    }
                }
            }];
        }
    }];
}

#pragma mark Collection
- (UICollectionView *)ado_collectioView
{
    if (!_ado_collectioView) {
        //初始化布局UICollectionViewFlowLayout
        UICollectionViewFlowLayout *layout = [[UICollectionViewFlowLayout alloc] init];
        [layout setItemSize:CGSizeMake((SelfView_W-50)/4, (SelfView_W-50)/4)];
        [layout setScrollDirection:(UICollectionViewScrollDirectionVertical)];
        layout.sectionInset = UIEdgeInsetsMake(10, 10, 10, 10);
        //初始化collectionView
        _ado_collectioView = [[UICollectionView alloc] initWithFrame:CGRectMake(0, 0, SelfView_W, SelfView_H - TabBar_H) collectionViewLayout:layout];
        _ado_collectioView.autoresizingMask = UIViewAutoresizingFlexibleHeight | UIViewAutoresizingFlexibleWidth;
        _ado_collectioView.backgroundColor = [UIColor whiteColor];
        _ado_collectioView.delegate = self;
        _ado_collectioView.dataSource = self;
        //注册cell
        [_ado_collectioView registerClass:[MyPhotoCell class] forCellWithReuseIdentifier:@"cellId"];
    }
    return _ado_collectioView;
}

//返回个数item
- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section
{
    return self.allPhotoArr.count;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath
{
    if (_allPhotoArr.count) {
        NSString *cellId = [NSString stringWithFormat:@"cell%ld",(long)indexPath.row];
        MyPhotoCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:cellId forIndexPath:indexPath];
        
        if ([phoneVersion integerValue] >= 8) {
            PHAsset *asset = _allPhotoArr[_allPhotoArr.count - indexPath.item - 1];
            cell.progressView.hidden = YES;
            cell.representedAssetIdentifier = asset.localIdentifier;
            CGFloat scale = [UIScreen mainScreen].scale;
            CGSize cellSize = cell.frame.size;
            CGSize AssetGridThumbnailSize = CGSizeMake(cellSize.width * scale, cellSize.height * scale);
            
            [_imageManager requestImageForAsset:asset
                                     targetSize:AssetGridThumbnailSize
                                    contentMode:PHImageContentModeDefault
                                        options:nil
                                  resultHandler:^(UIImage * _Nullable result, NSDictionary * _Nullable info) {
                                      if ([cell.representedAssetIdentifier isEqualToString:asset.localIdentifier]) {
                                          cell.phtotoView.image = result;
                                      }
                                  }];
        }else{
            if (!cell.phtotoView.image) {
                cell.progressView.hidden = YES;
                NSURL *url = self.allPhotoArr[self.allPhotoArr.count - indexPath.row - 1];
                ALAssetsLibrary *assetLibrary = [[ALAssetsLibrary alloc] init];
                [assetLibrary assetForURL:url resultBlock:^(ALAsset *asset) {
                    UIImage *image = [UIImage imageWithCGImage:asset.thumbnail];
                    cell.phtotoView.image = image;
                } failureBlock:^(NSError *error) {
                    NSLog(@"error=%@", error);
                }];
            }
        }
        return cell;
    }else{
        MyPhotoCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"cellId" forIndexPath:indexPath];
        return cell;
    }
}

//添加的方法
- (void)statusDisplay:(NSIndexPath *)indexpath
{
    [self collectionView:self.ado_collectioView didSelectItemAtIndexPath:indexpath];
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath
{
    if (!_chooseArray) {
        _chooseArray = [[NSMutableArray alloc] init];
    }
    if (!_chooseCellArray) {
        _chooseCellArray = [[NSMutableArray alloc] init];
    }
    MyPhotoCell *cell = (MyPhotoCell *)[collectionView cellForItemAtIndexPath:indexPath];
    if ([phoneVersion integerValue] >= 8) {
        PHAsset *asset = _allPhotoArr[_allPhotoArr.count-indexPath.row-1];
        [WPFunctionView getChoosePicPHImageManager:^(double progress) {
            cell.progressView.hidden = NO;
            cell.progressFloat = progress;
        } manger:^(UIImage *result) {
            cell.progressView.hidden = YES;
            if (!result) {
                return;
            } else {
                if (cell.chooseStatus == NO) {
                    if ((_chooseArray.count+_choosePhotoArr.count)< _selectPhotoOfMax) {
                        [_chooseArray addObject:result];
                        [_chooseCellArray addObject:[NSString stringWithFormat:@"%ld",(long)indexPath.row]];
                        [self finishColorAndTextChange:_chooseArray.count+_choosePhotoArr.count];
                        UIImageView *signImage = [[UIImageView alloc]initWithFrame:CGRectMake(cell.frame.size.width-22-5, 5, 22, 22)];
                        signImage.layer.cornerRadius = 22/2;
                        signImage.image = WPhoto_Btn_Selected;
                        signImage.layer.masksToBounds = YES;
                        [cell addSubview:signImage];
                        [WPFunctionView shakeToShow:signImage];
                        cell.chooseStatus = YES;
                    }
                } else{
                    for (NSInteger i = 2; i<cell.subviews.count; i++) {
                        [cell.subviews[i] removeFromSuperview];
                    }
                    for (NSInteger j = 0; j<_chooseCellArray.count; j++) {
                        
                        NSIndexPath *ip = [NSIndexPath indexPathForRow:[_chooseCellArray[j] integerValue] inSection:0];
                        
                        if (indexPath.row == ip.row) {
                            [_chooseArray removeObjectAtIndex:j];
                        }
                    }
                    [_chooseArray removeObject:result];
                    [_chooseCellArray removeObject:[NSString stringWithFormat:@"%ld",(long)indexPath.row]];
                    [self finishColorAndTextChange:_chooseArray.count+_choosePhotoArr.count];
                    cell.chooseStatus = NO;
                }
            }
        } asset:asset viewSize:self.view.bounds.size];
    } else {
        if (cell.chooseStatus == NO) {
            if ((_chooseArray.count+_choosePhotoArr.count) < _selectPhotoOfMax) {
                [_chooseArray addObject:_allPhotoArr[_allPhotoArr.count-indexPath.row-1]];
                [_chooseCellArray addObject:[NSString stringWithFormat:@"%ld",(long)indexPath.row]];
                [self finishColorAndTextChange:_chooseArray.count+_choosePhotoArr.count];
                UIImageView *signImage = [[UIImageView alloc]initWithFrame:CGRectMake(cell.frame.size.width-22-5, 5, 22, 22)];
                signImage.layer.cornerRadius = 22/2;
                signImage.image = WPhoto_Btn_Selected;
                signImage.layer.masksToBounds = YES;
                [cell addSubview:signImage];
                [WPFunctionView shakeToShow:signImage];
                cell.chooseStatus = YES;
            }
        } else{
            for (NSInteger i = 2; i<cell.subviews.count; i++) {
                [cell.subviews[i] removeFromSuperview];
            }
            [_chooseArray removeObject:_allPhotoArr[_allPhotoArr.count-indexPath.row-1]];
            [_chooseCellArray removeObject:[NSString stringWithFormat:@"%ld",(long)indexPath.row]];
            [self finishColorAndTextChange:_chooseArray.count+_choosePhotoArr.count];
            cell.chooseStatus = NO;
        }
    }
}

//设置导航栏
- (void)setNav {
    self.title = WPhoto_Center_Text;
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:WPhoto_Right_Text style:(UIBarButtonItemStylePlain) target:self action:@selector(quitChoose)];
}

#pragma mark 取消全部选择
- (void)quitChoose {
    if (_ado_collectioView && _chooseArray && _chooseCellArray) {
        if (_chooseArray.count && _chooseCellArray.count) {
            
            for (NSInteger i = 0; i < _chooseCellArray.count; i++) {
                NSIndexPath *indexPath = [NSIndexPath indexPathForRow:[_chooseCellArray[i] integerValue] inSection:0];
                MyPhotoCell *cell = (MyPhotoCell *)[_ado_collectioView cellForItemAtIndexPath:indexPath];
                
                for (NSInteger j = 2; j < cell.subviews.count; j++) {
                    [cell.subviews[j] removeFromSuperview];
                }
                cell.chooseStatus = NO;
            }
            [_chooseArray removeAllObjects];
            [_chooseCellArray removeAllObjects];
            [self finishColorAndTextChange:_chooseArray.count + _choosePhotoArr.count];
        }
    }
   
}
-(void)finishColorAndTextChange:(NSInteger)choosePhotoCount
{
    [_finishBtn setTitle:[NSString stringWithFormat:@"%lu/%ld 完成",choosePhotoCount,(long)_selectPhotoOfMax] forState:(UIControlStateNormal)];
    NSString *finishStr = [NSString stringWithFormat:@"0/%ld 完成",(long)_selectPhotoOfMax];
    if ([_finishBtn.titleLabel.text isEqualToString:finishStr] && !_chooseArray.count) {
        [_finishBtn setTitleColor:UIColorFromRGB(0xbbbbbb) forState:(UIControlStateNormal)];
    }else{
        [_finishBtn setTitleColor:UIColorFromRGB(0xcc3366) forState:(UIControlStateNormal)];
    }
}

//懒加载
- (NSMutableArray *)allPhotoArr
{
    if (!_allPhotoArr) {
        _allPhotoArr = [[NSMutableArray alloc] init];
    }
    return _allPhotoArr;
}

@end
