//
//  ZoloWalletViewController.m
//  NewZolo
//
//  Created by JTalking on 2022/9/8.
//

#import "ZoloFileOpenController.h"
#import <WebKit/WebKit.h>

@interface ZoloFileOpenController () <UIDocumentInteractionControllerDelegate>

@property (strong, nonatomic) WKWebView* webView;
@property (nonatomic, copy) NSString *url;
@property (nonatomic, assign) BOOL isHideNav;
@property (nonatomic, strong)UIDocumentInteractionController *doVC;
@property (nonatomic, strong) AFHTTPSessionManager *sessionManager;
@property (nonatomic, strong) UIButton *addBtn;

@end

@implementation ZoloFileOpenController

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    if (self.isHideNav) {
        [self.navigationController setNavigationBarHidden:NO animated:animated];
    } else {
        [self.navigationController setNavigationBarHidden:YES animated:animated];
    }
}

- (UIButton *)addBtn {
    if (!_addBtn) {
        _addBtn = [[UIButton alloc] initWithFrame:CGRectMake(50, 0, 44, 44)];
        [_addBtn setTitle:LocalizedString(@"SettingSave") forState:UIControlStateNormal];
        [_addBtn setTitleColor:[FontManage MHBlockColor] forState:UIControlStateNormal];
        [_addBtn addTarget:self action:@selector(previewFileWithURL) forControlEvents:UIControlEventTouchUpInside];
    }
    return _addBtn;
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [self.navigationController setNavigationBarHidden:NO animated:animated];
}

-(AFHTTPSessionManager *)sessionManager
{
    if (!_sessionManager) {
        _sessionManager = [AFHTTPSessionManager manager];
        _sessionManager.requestSerializer.timeoutInterval = 45.f;
        _sessionManager.responseSerializer = [AFJSONResponseSerializer serializer];
        _sessionManager.responseSerializer.acceptableContentTypes = [NSSet setWithObjects:@"application/json", @"text/html", @"text/json", @"text/plain", @"text/javascript", @"text/xml", @"image/*", nil];
    }
    return _sessionManager;
}

- (void)previewFileWithURL {
    [MHAlert showLoadingStr:LocalizedString(@"loadLoading")];
    NSURL *URL = [NSURL URLWithString:self.url];
    if ([URL isFileURL]) {
        if ([[[NSFileManager alloc] init] fileExistsAtPath:URL.path]) {

            self.doVC = [UIDocumentInteractionController interactionControllerWithURL:URL];
            self.doVC.delegate = self;
            [self.doVC presentOpenInMenuFromRect:self.view.bounds inView:self.view animated:YES];
        }
    } else {
        if ([URL.scheme containsString:@"http"]) {
            NSString *filePathString = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES).firstObject stringByAppendingPathComponent:URL.absoluteString.lastPathComponent];
   
            NSURLSessionDownloadTask *downloadTask = [self.sessionManager downloadTaskWithRequest:[NSURLRequest requestWithURL:URL] progress:^(NSProgress * _Nonnull downloadProgress) {
                
            } destination:^NSURL * _Nonnull(NSURL * _Nonnull targetPath, NSURLResponse * _Nonnull response) {
                return [NSURL fileURLWithPath:filePathString];
            } completionHandler:^(NSURLResponse * _Nonnull response, NSURL * _Nullable filePath, NSError * _Nullable error) {
                
                self.doVC = [UIDocumentInteractionController interactionControllerWithURL:filePath];
                self.doVC.delegate = self;
                [self.doVC presentOpenInMenuFromRect:self.view.bounds inView:self.view animated:YES];
                
            }];
            //启动下载文件任务
            [downloadTask resume];
        }
    }
}

- (instancetype)initWithUrlInfo:(NSString *)url isHidden:(BOOL)isHideNav {
    if (self = [super init]) {
        self.url = url;
        self.isHideNav = isHideNav;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];

    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:self.addBtn];
    _webView = [[WKWebView alloc] initWithFrame:CGRectMake(0, 20 + iPhoneX_topH, KScreenWidth, KScreenHeight - iPhoneX_bottomH)];
    [self.view addSubview:_webView];
    [_webView loadRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:self.url]]];
}

- (void)dealloc {
    NSLog(@"==ZoloWalletViewController===dealloc==");
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
