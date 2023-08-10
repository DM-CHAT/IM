//
//  MyPhotoCell.h
//  图片多选实现
//
//  Created by fzjy-ios on 2018/6/8.
//  Copyright © 2018年 fzjy-ios.phonewefwf. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "WPMacros.h"

@interface MyPhotoCell : UICollectionViewCell

@property (strong, nonatomic) UIImageView *phtotoView;
@property (nonatomic,assign) BOOL chooseStatus;
@property (nonatomic, copy) NSString *representedAssetIdentifier;
@property (strong, nonatomic) UIProgressView *progressView;
@property (nonatomic,assign) CGFloat progressFloat;
@property (strong, nonatomic) UIImageView *signImage;

@end
