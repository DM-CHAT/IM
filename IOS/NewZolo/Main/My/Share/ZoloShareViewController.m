//
//  ZoloShareViewController.m
//  NewZolo
//
//  Created by JTalking on 2023/2/3.
//

#import "ZoloShareViewController.h"
#import "HXPhotoTools.h"
#import <WebKit/WebKit.h>

@interface ZoloShareViewController () <WKScriptMessageHandler>

@property (weak, nonatomic) IBOutlet UIImageView *img;
@property (nonatomic, strong) UIButton *setBtn;
@property (nonatomic, strong)UIDocumentInteractionController *doVC;
@property (nonatomic, assign) BOOL isActive;
@property (strong, nonatomic) WKWebView* webView;
@property (nonatomic, copy) NSString *url;
@property (nonatomic, copy) NSString *shareUrl;
@property (nonatomic, strong) UIButton *copyBtn;
@property (nonatomic, strong) UIView *btnView;

@end

@implementation ZoloShareViewController

- (instancetype)initWithActive:(BOOL)isActive withUrl:(NSString *)url shareUrl:(NSString *)shareUrl {
    if (self = [super init]) {
        _isActive = isActive;
        self.url = url;
        self.shareUrl = shareUrl;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.

    self.navigationItem.title = LocalizedString(@"appShare");
    
    NSString *lanuStr = @"";
    NSString *str = [OSNLanguage getBaseLanguage];
    if ([str isEqualToString:@"0"]) {
        lanuStr = @"_zh.png";
    } else if ([str isEqualToString:@"1"]) {
        lanuStr = @"_vn.png";
    } else {
        lanuStr = @"_en.png";
    }
    
    NSString *language = [[NSUserDefaults standardUserDefaults]objectForKey:@"ospn_language"];
    if(strstr(_shareUrl.UTF8String, "?")){
        _shareUrl = [_shareUrl stringByAppendingString:@"&"];
    }else{
        _shareUrl = [_shareUrl stringByAppendingString:@"?"];
    }
    _shareUrl = [_shareUrl stringByAppendingFormat:@"language=%@",language];
    
    
    [self.btnView addSubview:self.setBtn];
    [self.btnView addSubview:self.copyBtn];
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:self.btnView];
    
    WKUserContentController *userContentController = [[WKUserContentController alloc] init];
    [userContentController addScriptMessageHandler:self name:@"osnsdk"];
    
    NSString *javaScriptSource = @"var osnsdk = new Object();"
        @"osnsdk.run = function(args){"
        @"return window.webkit.messageHandlers.osnsdk.postMessage(args);}";
    WKUserScript *userScript = [[WKUserScript alloc] initWithSource:javaScriptSource injectionTime:WKUserScriptInjectionTimeAtDocumentEnd forMainFrameOnly:YES];
    [userContentController addUserScript:userScript];

    WKWebViewConfiguration *configuration = [[WKWebViewConfiguration alloc] init];
    configuration.userContentController = userContentController;

    _webView = [[WKWebView alloc] initWithFrame:CGRectMake(0, 20 + iPhoneX_topH, KScreenWidth, KScreenHeight - iPhoneX_bottomH) configuration:configuration];
    [self.view addSubview:_webView];
    
    [MHAlert showLoadingStr:LocalizedString(@"loadLoading")];
    
    _url = [_url stringByAppendingFormat:@"%@", lanuStr];
    [_webView loadRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:self.url]]];
}

- (UIView *)btnView {
    if (!_btnView) {
        _btnView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 100, 100)];
    }
    return _btnView;
}

- (UIButton *)setBtn {
    if (!_setBtn) {
        _setBtn = [[UIButton alloc] initWithFrame:CGRectMake(50, 0, 44, 44)];
        [_setBtn setImage:[UIImage imageNamed:@"share-1"] forState:UIControlStateNormal];
        [_setBtn setTitleColor:[FontManage MHBlockColor] forState:UIControlStateNormal];
        [_setBtn addTarget:self action:@selector(setBtnClick) forControlEvents:UIControlEventTouchUpInside];
    }
    return _setBtn;
}

- (UIButton *)copyBtn {
    if (!_copyBtn) {
        _copyBtn = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, 44, 44)];
        [_copyBtn setImage:[UIImage imageNamed:@"my_copy"] forState:UIControlStateNormal];
        [_copyBtn setTitleColor:[FontManage MHBlockColor] forState:UIControlStateNormal];
        [_copyBtn addTarget:self action:@selector(copyBtnClick) forControlEvents:UIControlEventTouchUpInside];
    }
    return _copyBtn;
}

- (void)copyBtnClick {
    [MHAlert showLoadingStr:LocalizedString(@"AlertCopySucess")];
    NSString *urlStr = self.shareUrl;
    [[UIPasteboard generalPasteboard] setString:urlStr];
}

- (void)setBtnClick {
    [MHAlert showLoadingStr:LocalizedString(@"AlertNowOpeating")];
    [self clickShare];
}

- (void)clickShare {
    NSString *textToShare =  _isActive ? LocalizedString(@"ShareText") : LocalizedString(@"ShareNomalText");
    UIImage *imageToShare = _isActive ? [UIImage imageNamed:@"dm_bg"] : [UIImage imageNamed:@"dm_ibg"];
    NSString *urlStr = self.shareUrl;
    
    NSURL *urlToShare = [NSURL URLWithString:urlStr];
    
    NSArray *activityItems = @[textToShare, imageToShare, urlToShare];
    
    UIActivityViewController *activityVC = [[UIActivityViewController alloc]initWithActivityItems:activityItems applicationActivities:nil];
    
    activityVC.excludedActivityTypes =
    @[UIActivityTypePrint,UIActivityTypeMessage,UIActivityTypeMail,
    UIActivityTypePrint,UIActivityTypeAddToReadingList,UIActivityTypeOpenInIBooks,
    UIActivityTypeCopyToPasteboard,UIActivityTypeAssignToContact,UIActivityTypeSaveToCameraRoll];
    
    [self presentViewController:activityVC animated:YES completion:nil];
    
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [MHAlert dismiss];
    });
    
     // 分享之后的回调
    activityVC.completionWithItemsHandler = ^(UIActivityType  _Nullable activityType, BOOL completed, NSArray * _Nullable returnedItems, NSError * _Nullable activityError) {
        if (completed) {
            
        } else  {
    
        }
    };
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [self.navigationController setNavigationBarHidden:NO animated:animated];
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
