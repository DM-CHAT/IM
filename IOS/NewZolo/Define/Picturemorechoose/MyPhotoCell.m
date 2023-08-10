//
//  MyPhotoCell.m
//  图片多选实现
//
//  Created by fzjy-ios on 2018/6/8.
//  Copyright © 2018年 fzjy-ios.phonewefwf. All rights reserved.
//

#import "MyPhotoCell.h"

@implementation MyPhotoCell

- (instancetype)initWithFrame:(CGRect)frame
{
    if (self = [super initWithFrame:frame]) {
        
        _phtotoView = [[UIImageView alloc] initWithFrame:self.bounds];
        //保持图片填充完整并且不变形
        _phtotoView.contentMode = UIViewContentModeScaleAspectFill;
        _phtotoView.layer.masksToBounds = YES;
        [self addSubview:_phtotoView];
        
        _progressView = [[UIProgressView alloc] initWithFrame:CGRectMake(self.bounds.size.width/4, self.bounds.size.height/4*3, self.bounds.size.width/2, self.bounds.size.height/4)];
        _progressView.progressViewStyle = UIProgressViewStyleDefault;
        _progressView.progressTintColor = UIColorFromRGB(0x000000);
        _progressView.trackTintColor = UIColorFromRGB(0xffffff);
        [_phtotoView addSubview:_progressView];
        
        _signImage = [[UIImageView alloc] initWithFrame:CGRectMake(self.frame.size.width-22-5, 5, 22, 22)];
        _signImage.layer.cornerRadius = 22/2;
        _signImage.image = WPhoto_btn_UnSelected;
        _signImage.layer.masksToBounds = YES;
        [_phtotoView addSubview:_signImage];
        
    }
    return self;
}

- (void)setProgressFloat:(CGFloat)progressFloat
{
    _progressFloat = progressFloat;
    [_progressView setProgress:progressFloat animated:YES];
}


@end
