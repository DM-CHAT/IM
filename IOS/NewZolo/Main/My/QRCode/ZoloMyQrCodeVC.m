//
//  ZoloMyQrCodeVC.m
//  NewZolo
//
//  Created by JTalking on 2022/7/6.
//

#import "ZoloMyQrCodeVC.h"
#import "HXPhotoTools.h"

@interface ZoloMyQrCodeVC ()

@property (weak, nonatomic) IBOutlet UIImageView *icon;
@property (weak, nonatomic) IBOutlet UILabel *nickNameLabel;
@property (weak, nonatomic) IBOutlet UILabel *zoloNum;
@property (weak, nonatomic) IBOutlet UIButton *shareBtn;
@property (weak, nonatomic) IBOutlet UIImageView *qrImg;
@property (weak, nonatomic) IBOutlet UILabel *titleLab;
@property (weak, nonatomic) IBOutlet UIButton *saveBtn;

@property (nonatomic, strong) DMCCUserInfo *userInfo;

@property (nonatomic, strong) DMCCConversation *conversation;
@property (nonatomic, assign) BOOL isGroup;
@property (nonatomic, assign) BOOL isUser;

@end

@implementation ZoloMyQrCodeVC

- (instancetype)initWithGroupQr:(DMCCConversation *)conversation withIsGroup:(BOOL)isGroup IsUser:(BOOL)isUser {
    if (self = [super init]) {
        self.isGroup = isGroup;
        self.isUser = isUser;
        self.conversation = conversation;
    }
    return self;
}

- (IBAction)backBtnClick:(id)sender {
    [self.navigationController popViewControllerAnimated:YES];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self.navigationController setNavigationBarHidden:YES animated:animated];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    if (self.isGroup || self.isUser) {
        [self.navigationController setNavigationBarHidden:NO animated:animated];
    }
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    self.icon.layer.masksToBounds = YES;
    self.icon.layer.cornerRadius = 40;
    self.icon.layer.borderColor = [UIColor whiteColor].CGColor;
    self.icon.layer.borderWidth = 2;
    self.titleLab.text = [NSString stringWithFormat:@"%@%@", self.isGroup ? LocalizedString(@"Group") : LocalizedString(@"Me"),LocalizedString(@"ContactQrCode")];
    self.view.backgroundColor = MHColorFromHex(0x57B77D);
    [self.shareBtn setTitle:LocalizedString(@"ContactCopyTo") forState:UIControlStateNormal];
    self.shareBtn.backgroundColor = [UIColor colorWithRed:255/255 green:255/255 blue:255/255 alpha:0.12];
    self.shareBtn.layer.cornerRadius = 12;
    self.shareBtn.layer.masksToBounds = YES;
    [self.saveBtn setTitle:LocalizedString(@"YBImageImageSave") forState:UIControlStateNormal];
    self.saveBtn.backgroundColor = [UIColor colorWithRed:255/255 green:255/255 blue:255/255 alpha:0.12];
    self.saveBtn.layer.cornerRadius = 12;
    self.saveBtn.layer.masksToBounds = YES;
    
    if (_isGroup) {
        DMCCGroupInfo *info = [[DMCCIMService sharedDMCIMService] getGroupInfo:self.conversation.target refresh:NO];
        [self.icon sd_setImageWithURL:[NSURL URLWithString:info.portrait] placeholderImage:SDImageDefault];
        self.nickNameLabel.text = info.name;
        self.zoloNum.text = [NSString stringWithFormat:@"ID:%@", info.target];
        [self QRcode:self.conversation.target];
    } else if (_isUser) {
        WS(ws);
        [[DMCCIMService sharedDMCIMService] getUserInfo:self.conversation.target refresh:NO success:^(DMCCUserInfo *userInfo) {
            [ws.icon sd_setImageWithURL:[NSURL URLWithString:userInfo.portrait] placeholderImage:SDImageDefault];
            ws.nickNameLabel.text = userInfo.displayName;
            ws.zoloNum.text = [NSString stringWithFormat:@"ID:%@", userInfo.userId];
            [ws QRcode:userInfo.userId];
            ws.userInfo = userInfo;
        } error:^(int errorCode) {
            
        }];
    } else {
        [self getData];
    }
}

- (void)getData {
    WS(ws);
    [[DMCCIMService sharedDMCIMService] getUserInfo:[[OsnSDK getInstance] getUserID] refresh:NO success:^(DMCCUserInfo *userInfo) {
        if (userInfo) {
            dispatch_async(dispatch_get_main_queue(), ^{
                [ws loadUserInfo:userInfo];
            });
        }
    } error:^(int errorCode) {
        
    }];
}

- (void)loadUserInfo:(DMCCUserInfo *)userInfo {
    _userInfo = userInfo;
    [self.icon sd_setImageWithURL:[NSURL URLWithString:userInfo.portrait] placeholderImage:SDImageDefault];
    self.nickNameLabel.text = userInfo.displayName;
    self.zoloNum.text = [NSString stringWithFormat:@"ID:%@", userInfo.userId];
    [self QRcode:userInfo.userId];
}

/** 生成二维码 */
- (void)QRcode:(NSString *)stringcode {
    //二维码滤镜
    CIFilter *filter = [CIFilter filterWithName:@"CIQRCodeGenerator"];
    //恢复滤镜的默认属性
    [filter setDefaults];
    //将字符串转换成NSData
    NSString *qrStr = @"";
    if (_isGroup) {
        qrStr = [NSString stringWithFormat:@"ospn://group/%@", stringcode];
    } else {
        qrStr = [NSString stringWithFormat:@"ospn://user/%@", stringcode];
    }
    
    
    NSData *data = [qrStr dataUsingEncoding:NSUTF8StringEncoding];
    //通过KVO设置滤镜inputmessage数据
    [filter setValue:data forKey:@"inputMessage"];
    //获得滤镜输出的图像
    CIImage *outputImage = [filter outputImage];
    //将CIImage转换成UIImage,并放大显示
    self.qrImg.image = [self createNonInterpolatedUIImageFormCIImage:outputImage withSize:155.0];
}


//改变二维码大小
- (UIImage *)createNonInterpolatedUIImageFormCIImage:(CIImage *)image withSize:(CGFloat) size {
    CGRect extent = CGRectIntegral(image.extent);
    CGFloat scale = MIN(size/CGRectGetWidth(extent), size/CGRectGetHeight(extent));
    // 创建bitmap;
    size_t width = CGRectGetWidth(extent) * scale;
    size_t height = CGRectGetHeight(extent) * scale;
    CGColorSpaceRef cs = CGColorSpaceCreateDeviceGray();
    CGContextRef bitmapRef = CGBitmapContextCreate(nil, width, height, 8, 0, cs, (CGBitmapInfo)kCGImageAlphaNone);
    CIContext *context = [CIContext contextWithOptions:nil];
    CGImageRef bitmapImage = [context createCGImage:image fromRect:extent];
    CGContextSetInterpolationQuality(bitmapRef, kCGInterpolationNone);
    CGContextScaleCTM(bitmapRef, scale, scale);
    CGContextDrawImage(bitmapRef, extent, bitmapImage);
    // 保存bitmap到图片
    CGImageRef scaledImage = CGBitmapContextCreateImage(bitmapRef);
    CGContextRelease(bitmapRef);
    CGImageRelease(bitmapImage);
    return [UIImage imageWithCGImage:scaledImage];
    
}

- (IBAction)shareBtnClick:(id)sender {
    if (self.isGroup) {
        DMCCGroupInfo *info = [[DMCCIMService sharedDMCIMService] getGroupInfo:self.conversation.target refresh:NO];
        [[UIPasteboard generalPasteboard] setString:info.target];
    } else {
        [[UIPasteboard generalPasteboard] setString:self.userInfo.userId];
    }
    [MHAlert showMessage:LocalizedString(@"AlertCopySucess")];
}

- (IBAction)saveCurentClick:(id)sender {
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [MHAlert showMessage:LocalizedString(@"AlertSuccess")];
        [HXPhotoTools savePhotoToCustomAlbumWithName:@"" photo:[self saveCurrentView]];
    });
}

- (UIImage*)saveCurrentView {
    UIGraphicsBeginImageContextWithOptions(self.view.frame.size, NO, 2.0);
    [self.view.layer renderInContext:UIGraphicsGetCurrentContext()];
    UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return image;
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
