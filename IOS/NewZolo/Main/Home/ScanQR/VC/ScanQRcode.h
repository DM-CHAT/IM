//
//  ScanQRcode.h
//  QRcode_GHdemo
//
//  Created by xiangwang on 16/6/28.
//  Copyright © 2016年 Hope. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>

/** 返回扫码结果 */
typedef void(^ScanResultBlcok)(NSString *result);

@interface ScanQRcode : UIViewController<AVCaptureMetadataOutputObjectsDelegate>
{
    int num;
    BOOL upOrdown;
    NSTimer * timer;
    UIImageView * imageView;
}

-(instancetype)initWithIsPushToVC:(BOOL)isPush;

@property ( strong , nonatomic ) AVCaptureDevice * device;

@property ( strong , nonatomic ) AVCaptureDeviceInput * input;

@property ( strong , nonatomic ) AVCaptureMetadataOutput * output;

@property ( strong , nonatomic ) AVCaptureSession * session;

@property ( strong , nonatomic ) AVCaptureVideoPreviewLayer * preview;

@property ( nonatomic ) CGRect rectOfInterest NS_AVAILABLE_IOS (7_0);

@property (nonatomic, strong) AVCaptureVideoPreviewLayer *videoPreviewLayer;
@property (nonatomic, strong) AVCaptureSession *captureSession;
@property (nonatomic, retain) UIImageView * line;

@property (strong, nonatomic) UIView *boxView;
@property (nonatomic) BOOL isReading;
@property (strong, nonatomic) CALayer *scanLayer;

@property(nonatomic, copy) ScanResultBlcok resultBlcok;

@end
