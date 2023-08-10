//
//  WPFunctionView.h
//  图片多选实现
//
//  Created by fzjy-ios on 2018/6/8.
//  Copyright © 2018年 fzjy-ios.phonewefwf. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "WPMacros.h"
#import <Photos/Photos.h>
#import <AssetsLibrary/AssetsLibrary.h>

typedef void(^finishChooseBlock)(NSMutableArray *mychoosePhotoArr);
typedef void(^photoKitBlcok)(PHFetchResult *allPhoto);
typedef void(^AlAssetsLibraryBlcok)(ALAssetsGroup *group);
typedef void(^HanlerBlock)(double prpgress);
typedef void(^MangerBlock)(UIImage *result);

@interface WPFunctionView : UIView

//所选择的图片数组
+(void)finishChoosePhotos:(finishChooseBlock)block chooseArray:(NSMutableArray *)chooseArray;
//高版本使用PhotoKit框架
+(void)getHeightVersionAllPhotos:(photoKitBlcok)photosBlock;
//低版本使用ALAssetsLibrary框架
+(void)getLowVersionAllPhotos:(AlAssetsLibraryBlcok)photoGroupBlock;
//高版本使用PhotoKit选择图片
+(void)getChoosePicPHImageManager:(HanlerBlock)handler manger:(MangerBlock)manager asset:(PHAsset *)asset viewSize:(CGSize)viewSize;
//列表中按钮点击动画效果
+(void)shakeToShow:(UIImageView *)button;


@end
