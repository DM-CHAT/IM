//
//  XCQRCodeVC.m
//  SGQRCodeExample
//
//  Created by kingsic on 2022/7/11.
//  Copyright © 2022 kingsic. All rights reserved.
//

#import "WBQRCodeVC.h"
#import "SGQRCode.h"
//#import "WebViewController.h"

@interface WBQRCodeVC ()<SGScanCodeDelegate, UINavigationControllerDelegate, UIImagePickerControllerDelegate> {
    SGScanCode *scanCode;
}
@property (nonatomic, strong) SGScanView *scanView;
@property (nonatomic, strong) UILabel *promptLabel;
@property (nonatomic, strong) UIButton *setBtn;

@end

@implementation WBQRCodeVC

- (void)dealloc {
    NSLog(@"XCQRCodeVC - dealloc");
    
    [self stop];
}

- (void)start {
    [scanCode startRunning];
    [self.scanView startScanning];
}

- (void)stop {
    [scanCode stopRunning];
    [self.scanView stopScanning];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    self.view.backgroundColor = [UIColor blackColor];
    
    [self configureNav];
    
    [self configureUI];
    
    [self configureQRCode];
}

- (void)configureUI {
    [self.view addSubview:self.scanView];
    [self.view addSubview:self.promptLabel];
}

- (void)configureQRCode {
    scanCode = [SGScanCode scanCode];
    scanCode.preview = self.view;
    scanCode.delegate = self;
    CGFloat w = 0.7;
    CGFloat x = 0.5 * (1 - w);
    CGFloat h = (self.view.frame.size.width / self.view.frame.size.height) * w;
    CGFloat y = 0.5 * (1 - h);
    /// 扫描范围。对应辅助扫描框的frame（borderFrame）设置
    scanCode.rectOfInterest = CGRectMake(y, x, h, w);
    [scanCode startRunning];
}

- (void)scanCode:(SGScanCode *)scanCode result:(NSString *)result {
    [self stop];
    
    [scanCode playSoundEffect:@"SGQRCode.bundle/scan_end_sound.caf"];

    /** 拿到结果返回 */
    [self.navigationController popViewControllerAnimated:YES];
 
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.25 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        if (self.resultBlcok) {
            self.resultBlcok(result);
        }
    });
}

- (void)configureNav {
    self.navigationItem.title = LocalizedString(@"Scan");
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:self.setBtn];
}

- (UIButton *)setBtn {
    if (!_setBtn) {
        _setBtn = [[UIButton alloc] initWithFrame:CGRectZero];
        [_setBtn setTitle:LocalizedString(@"SeletctPhoto") forState:UIControlStateNormal];
        [_setBtn setTitleColor:[FontManage MHBlockColor] forState:UIControlStateNormal];
        [_setBtn addTarget:self action:@selector(rightBarButtonItenAction) forControlEvents:UIControlEventTouchUpInside];
    }
    return _setBtn;
}

- (void)rightBarButtonItenAction {
    [SGPermission permissionWithType:SGPermissionTypePhoto completion:^(SGPermission * _Nonnull permission, SGPermissionStatus status) {
        if (status == SGPermissionStatusNotDetermined) {
            [permission request:^(BOOL granted) {
                if (granted) {
                   
                    [self _enterImagePickerController];
                } else {
                    
                }
            }];
        } else if (status == SGPermissionStatusAuthorized) {
            
            [self _enterImagePickerController];
        } else if (status == SGPermissionStatusDenied) {
           
            NSDictionary *infoDict = [[NSBundle mainBundle] infoDictionary];
            NSString *app_Name = [infoDict objectForKey:@"CFBundleDisplayName"];
            if (app_Name == nil) {
                app_Name = [infoDict objectForKey:@"CFBundleName"];
            }
            
            NSString *messageString = [NSString stringWithFormat:LocalizedString(@"SeletctPhotoAllowFind"), app_Name];
            UIAlertController *alertC = [UIAlertController alertControllerWithTitle:LocalizedString(@"Alert") message:messageString preferredStyle:(UIAlertControllerStyleAlert)];
            UIAlertAction *alertA = [UIAlertAction actionWithTitle:LocalizedString(@"Sure") style:(UIAlertActionStyleDefault) handler:nil];
            
            [alertC addAction:alertA];
            [self presentViewController:alertC animated:YES completion:nil];
        } else if (status == SGPermissionStatusRestricted) {
            
            UIAlertController *alertC = [UIAlertController alertControllerWithTitle:LocalizedString(@"Alert") message:LocalizedString(@"SeletctPhotoFind") preferredStyle:(UIAlertControllerStyleAlert)];
            UIAlertAction *alertA = [UIAlertAction actionWithTitle:LocalizedString(@"Sure") style:(UIAlertActionStyleDefault) handler:nil];
            [alertC addAction:alertA];
            [self presentViewController:alertC animated:YES completion:nil];
        }
    }];
}

- (void)_enterImagePickerController {
    [self stop];

    UIImagePickerController *imagePicker = [[UIImagePickerController alloc] init];
    imagePicker.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
    imagePicker.delegate = self;
    imagePicker.modalPresentationStyle = UIModalPresentationCustom;
    [self presentViewController:imagePicker animated:YES completion:nil];
}

#pragma mark - - UIImagePickerControllerDelegate 的方法
- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker {
    [self dismissViewControllerAnimated:YES completion:nil];
    [self start];
}

- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary<NSString *,id> *)info {
    UIImage *image = [info objectForKey:UIImagePickerControllerOriginalImage];
    
    WS(ws);
    
    [scanCode readQRCode:image completion:^(NSString *result) {
        if (result == nil) {
            [self dismissViewControllerAnimated:YES completion:nil];
            [self start];
            
        } else {
            
            [self dismissViewControllerAnimated:YES completion:^{
                /** 拿到结果返回 */
                [ws.navigationController popViewControllerAnimated:YES];
                dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.25 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                    if (ws.resultBlcok) {
                        ws.resultBlcok(result);
                    }
                });
            }];
        }
    }];
}

- (SGScanView *)scanView {
    if (!_scanView) {
        SGScanViewConfigure *configure = [[SGScanViewConfigure alloc] init];
        configure.cornerLocation = SGCornerLoactionInside;
        configure.cornerWidth = 1;
        configure.cornerLength = 25;
        configure.isShowBorder = YES;
        configure.scanlineStep = 2;
        configure.scanline = @"SGQRCode.bundle/scan_scanline";
        configure.autoreverses = YES;

        CGFloat x = 0;
        CGFloat y = 0;
        CGFloat w = self.view.frame.size.width;
        CGFloat h = self.view.frame.size.height;
        _scanView = [[SGScanView alloc] initWithFrame:CGRectMake(x, y, w, h) configure:configure];
        [_scanView startScanning];
    }
    return _scanView;
}

- (UILabel *)promptLabel {
    if (!_promptLabel) {
        _promptLabel = [[UILabel alloc] init];
        _promptLabel.backgroundColor = [UIColor clearColor];
        CGFloat promptLabelX = 0;
        CGFloat promptLabelY = 0.73 * self.view.frame.size.height;
        CGFloat promptLabelW = self.view.frame.size.width - 40;
        CGFloat promptLabelH = 50;
        _promptLabel.frame = CGRectMake(promptLabelX + 20, promptLabelY, promptLabelW, promptLabelH);
        _promptLabel.textAlignment = NSTextAlignmentCenter;
        _promptLabel.numberOfLines = 0;
        _promptLabel.font = [UIFont boldSystemFontOfSize:13.0];
        _promptLabel.textColor = [[UIColor whiteColor] colorWithAlphaComponent:0.6];
        _promptLabel.text = LocalizedString(@"SeletctPhotoTitle");
    }
    return _promptLabel;
}

@end
