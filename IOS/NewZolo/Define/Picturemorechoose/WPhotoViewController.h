//
//  WPhotoViewController.h
//  图片多选实现
//
//  Created by fzjy-ios on 2018/6/8.
//  Copyright © 2018年 fzjy-ios.phonewefwf. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface WPhotoViewController : UIViewController

@property (nonatomic,assign) NSInteger selectPhotoOfMax;//!< 选择照片的最多张数

//回调方法
@property (nonatomic, copy) void(^selectPhotoBack)(NSMutableArray *photosArr);

- (void)statusDisplay:(NSIndexPath *)indexpath;

@end
