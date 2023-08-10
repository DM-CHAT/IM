//
//  ZoloGroupNoticeVC.m
//  NewZolo
//
//  Created by JTalking on 2022/8/18.
//

#import "ZoloGroupNoticeVC.h"
#import "YYTextView.h"
@interface ZoloGroupNoticeVC () <YYTextViewDelegate>

@property (nonatomic, strong) DMCCGroupInfo *info;
@property (weak, nonatomic) IBOutlet UIView *bgView;
@property (nonatomic, strong) YYTextView *textView;
@property (weak, nonatomic) IBOutlet UIButton *saveBtn;
@property (weak, nonatomic) IBOutlet UIButton *clearBtn;

@end

@implementation ZoloGroupNoticeVC

- (instancetype)initWithGroupInfo:(DMCCGroupInfo *)info {
    if (self = [super init]) {
        self.info = info;
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
    [self.navigationController setNavigationBarHidden:NO animated:animated];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    self.view.backgroundColor = MHColorFromHex(0x57B77D);
    [self.saveBtn setTitle:LocalizedString(@"SettingSave") forState:UIControlStateNormal];
    self.saveBtn.backgroundColor = [UIColor colorWithRed:255/255 green:255/255 blue:255/255 alpha:0.12];
    self.saveBtn.layer.cornerRadius = 24;
    self.saveBtn.layer.masksToBounds = YES;
    self.textView.text = self.info.notice;
    [self textView];
    [self.clearBtn setTitle:LocalizedString(@"ClearContent") forState:UIControlStateNormal];
}


- (YYTextView *)textView {
    if (!_textView) {
        _textView = [[YYTextView alloc] initWithFrame:CGRectMake(0, 0, KScreenWidth - 100, 230)];
        _textView.placeholderText = LocalizedString(@"ContactGroupInputNotice");
        _textView.placeholderFont = [UIFont systemFontOfSize:15];
        _textView.tintColor = [UIColor blackColor];
        _textView.font = [UIFont systemFontOfSize:17];
        _textView.delegate = self;
        [self.bgView addSubview:_textView];
    }
    return _textView;
}

- (void)textViewDidEndEditing:(YYTextView *)textView {
    
}

- (IBAction)publishBtnClick:(id)sender {
    [self setBtnClick];
}

- (IBAction)clearBtnClick:(id)sender {
    NSString *str = LocalizedString(@"AlertClear");
    WS(ws);
    [MHAlert showCustomeAlert:str withAlerttype:MHAlertTypeWarn withOkBlock:^(UIAlertAction *action) {
        if ([action.title isEqualToString:LocalizedString(@"Sure")]) {
            ws.textView.text = @"";
        }
    }];
}

- (void)setBtnClick {
    WS(ws);
    if (self.textView.text.length > 1500) {
        [MHAlert showMessage:LocalizedString(@"StringLength")];
        return;
    }
    [[DMCCIMService sharedDMCIMService] addBillBoard:self.info.target data:self.textView.text cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        if (isSuccess) {
            dispatch_async(dispatch_get_main_queue(), ^{
                [MHAlert showMessage:LocalizedString(@"AlertSuccess")];
                [ws.navigationController popViewControllerAnimated:YES];
            });
        } else {
            [MHAlert showMessage:LocalizedString(@"AlertSettingFail")];
        }
    }];
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    [self.view endEditing:YES];
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
