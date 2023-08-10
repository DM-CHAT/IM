//
//  ScanQRcode.m
//  QRcode_GHdemo
//
//  Created by xiangwang on 16/6/28.
//  Copyright © 2016年 Hope. All rights reserved.
//

#import "ScanQRcode.h"

#define GH_WIDTH    [[UIScreen mainScreen] bounds].size.width
#define GH_HEIGHT   [[UIScreen mainScreen] bounds].size.height
@interface ScanQRcode () <UIAlertViewDelegate>

@property (nonatomic, assign) BOOL isPush;

@end

@implementation ScanQRcode

-(instancetype)initWithIsPushToVC:(BOOL)isPush {
    if (self = [super init]) {
        self.isPush = isPush;
    }
    return self;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self.navigationController setNavigationBarHidden:NO animated:animated];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = [UIColor grayColor];
    self.title = @"扫描二维码";
    
    /** 设置导航栏 */
    [self setNav];
    
    [self callCamera];
}

/**
 *  调用系统相机
 */
- (void)callCamera
{
    //判断是否已授权
    if ([[[UIDevice currentDevice] systemVersion] floatValue] >= 7.0) {
        AVAuthorizationStatus authStatus = [AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeVideo];
        if (authStatus == AVAuthorizationStatusDenied||authStatus == AVAuthorizationStatusRestricted) {
            /** 底部弹出窗口 */
            UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"未获取授权使用摄像头" message:@"请在iOS“设置”-“隐私”-“相机”中打开" preferredStyle: UIAlertControllerStyleAlert];
            
            /** 知道了按钮 */
            WS(ws);
            UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:@"知道了" style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
                
                [ws.navigationController popViewControllerAnimated:YES];
                [ws dismissViewControllerAnimated:NO completion:nil];
                
            }];
            [alertController addAction:cancelAction];
            /** 弹出操作 */
            [self presentViewController:alertController animated:YES completion:nil];

        }else{
            
            UIButton * scanButton = [UIButton buttonWithType:UIButtonTypeRoundedRect];
            [scanButton setTitle:LocalizedString(@"Cancel") forState:UIControlStateNormal];
            [scanButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
            scanButton.frame = CGRectMake(100, GH_HEIGHT - 100, GH_WIDTH - 200, 50);
            [scanButton addTarget:self action:@selector(backAction) forControlEvents:UIControlEventTouchUpInside];
            [self.view addSubview:scanButton];
            
            UILabel * labIntroudction= [[UILabel alloc] initWithFrame:CGRectMake(0, 80, GH_WIDTH, 50)];
            labIntroudction.backgroundColor = [UIColor clearColor];
            labIntroudction.numberOfLines=1;
            labIntroudction.textAlignment = NSTextAlignmentCenter;
            labIntroudction.textColor=[UIColor whiteColor];
            labIntroudction.text = LocalizedString(@"ScanContent");
            labIntroudction.adjustsFontSizeToFitWidth = YES;
            [self.view addSubview:labIntroudction];
            
            _device = [ AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
            
            _input = [ AVCaptureDeviceInput deviceInputWithDevice:self.device error:nil];
            
            _output = [[ AVCaptureMetadataOutput alloc]init];
            [ _output setMetadataObjectsDelegate:self queue:dispatch_get_main_queue()];
            
            _session = [[ AVCaptureSession alloc]init];
            [ _session setSessionPreset:AVCaptureSessionPresetHigh];
            if ([ _session canAddInput:self.input])
            {
                [ _session addInput:self.input];
            }
            if ([ _session canAddOutput:self.output])
            {
                [ _session addOutput:self.output];
            }
            
            _output.metadataObjectTypes = @[AVMetadataObjectTypeQRCode];
            [ _output setRectOfInterest : CGRectMake (( 124 )/ GH_HEIGHT ,(( GH_WIDTH - 220 )/ 2 )/ GH_WIDTH , 220 / GH_HEIGHT , 220 / GH_WIDTH )];
            
            _preview =[AVCaptureVideoPreviewLayer layerWithSession:_session];
            _preview.videoGravity = AVLayerVideoGravityResizeAspectFill;
            _preview.frame = self.view.layer.bounds;
            //    _preview.tor
            [self.view.layer insertSublayer:_preview atIndex:0];
            
            
            //扫描框
            imageView = [[UIImageView alloc]initWithFrame:CGRectMake((GH_WIDTH-(GH_WIDTH-GH_WIDTH*0.5f))/2,GH_HEIGHT*0.2f,GH_WIDTH- GH_WIDTH*0.5f,GH_WIDTH-GH_WIDTH*0.5f)];
            imageView.image = [UIImage imageNamed:@"shelfscanning"];
            [self.view addSubview:imageView];
            upOrdown = NO;
            num =0;
            
            _line = [[UIImageView alloc] initWithFrame:CGRectMake((GH_WIDTH-(GH_WIDTH-GH_WIDTH*0.5f))/2,GH_HEIGHT*0.2f,GH_WIDTH- GH_WIDTH*0.5f,1)];
            _line.image = [UIImage imageNamed:@"line"];
            [self.view addSubview:_line];
            timer = [NSTimer scheduledTimerWithTimeInterval:.02 target:self selector:@selector(animation1) userInfo:nil repeats:YES];
            
            
            [ _session startRunning ];
            
            
            
        }

    }
}


#pragma mark -
#pragma mark - 上下动画
-(void)animation1
{
    if (upOrdown == NO) {
        num ++;
        _line.frame = CGRectMake((GH_WIDTH-(GH_WIDTH-GH_WIDTH*0.5f))/2, GH_HEIGHT*0.2f+2*num, imageView.frame.size.width, 2);
        if (2*num > GH_WIDTH-GH_WIDTH*0.5f-5) {
            upOrdown = YES;
        }
    }
    else {
        num --;
        _line.frame = CGRectMake((GH_WIDTH-(GH_WIDTH-GH_WIDTH*0.5f))/2, GH_HEIGHT*0.2f+2*num, imageView.frame.size.width, 2);
        if (num == 2) {
            upOrdown = NO;
        }
    }
    
}

#pragma mark - AVCaptureMetadataOutputObjectsDelegate
- (void)captureOutput:(AVCaptureOutput *)captureOutput didOutputMetadataObjects:(NSArray *)metadataObjects fromConnection:( AVCaptureConnection *)connection
{
    NSString *stringValue;
    if ([metadataObjects count] > 0 )
    {
        // 停止扫描
        [ _session stopRunning ];
        AVMetadataMachineReadableCodeObject * metadataObject = [metadataObjects objectAtIndex:0];
        stringValue = metadataObject.stringValue;
        _line.hidden = YES;//隐藏动画效果
        NSLog(@"扫描结果=%@",stringValue);
        
        /** 拿到结果返回 */
        [self.navigationController popViewControllerAnimated:YES];
        [self dismissViewControllerAnimated:NO completion:nil];
        
        if (self.resultBlcok) {
            self.resultBlcok(stringValue);
        }
    }
}

/** 设置导航栏 */
- (void)setNav {
    /** 左边返回按钮 */
    UIButton *button = [UIButton buttonWithType:(UIButtonTypeCustom)];
    [button setTitle:@"返回" forState:(UIControlStateNormal)];
    button.titleLabel.font = [UIFont systemFontOfSize:16];
    [button setImage:[UIImage imageNamed:@"back"] forState:(UIControlStateNormal)];
    [button setTitleColor:[FontManage MHMainColor] forState:(UIControlStateNormal)];
    [button setTitleColor:[FontManage MHMainColor] forState:(UIControlStateHighlighted)];
    [button setImageEdgeInsets:UIEdgeInsetsMake(0,-15, 0, 0)];
    [button setTitleEdgeInsets:UIEdgeInsetsMake(0, -12, 0, 0)];
    [button addTarget:self action:@selector(clickBack:) forControlEvents:(UIControlEventTouchUpInside)];
    self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:button];
    button.frame = CGRectMake(0, 0, 55, 24);
}

/** 返回 */
- (void)clickBack:(UIButton *)bbi {
    [self backAction];
}

#pragma mark - 取消按钮
-(void)backAction {
    _line.hidden = NO;
    [ _session stopRunning];
    _device = nil;
    _input = nil;
    _output = nil;
    _session = nil;
    timer = nil;
    _preview = nil;
    if (_isPush) {
        [self.navigationController popViewControllerAnimated:YES];
    } else {
        [self dismissViewControllerAnimated:NO completion:nil];
    }
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)dealloc {
    NSLog(@"====ScanQRcode========dealloc===");
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
