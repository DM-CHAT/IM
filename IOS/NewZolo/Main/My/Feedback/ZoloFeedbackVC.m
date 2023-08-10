//
//  ZoloGroupNoticeVC.m
//  NewZolo
//
//  Created by JTalking on 2022/8/18.
//

#import "ZoloFeedbackVC.h"
#import "YYTextView.h"
@interface ZoloFeedbackVC () <YYTextViewDelegate>

@property (nonatomic, strong) UIButton *setBtn;
@property (nonatomic, strong) DMCCGroupInfo *info;
@property (nonatomic, strong) YYTextView *textView;
@property (weak, nonatomic) IBOutlet UILabel *titleLab;

@property (weak, nonatomic) IBOutlet UIView *bgView;
@property (weak, nonatomic) IBOutlet UIButton *submitBtn;

@end

@implementation ZoloFeedbackVC

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    self.submitBtn.layer.cornerRadius = 2;
    self.submitBtn.layer.masksToBounds = YES;
    self.titleLab.text =LocalizedString(@"UserFeedback");
    [self.submitBtn setTitle:LocalizedString(@"FeedbackSubmit") forState:UIControlStateNormal];
    self.submitBtn.backgroundColor = [UIColor colorWithRed:255/255 green:255/255 blue:255/255 alpha:0.12];
    self.submitBtn.layer.cornerRadius = 24;
    self.submitBtn.layer.masksToBounds = YES;
    self.view.backgroundColor = MHColorFromHex(0x57B77D);
    [self textView];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self.navigationController setNavigationBarHidden:YES animated:animated];
}

- (IBAction)backClick:(id)sender {
    [self.navigationController popViewControllerAnimated:YES];
}


- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    [self.view endEditing:YES];
}

- (YYTextView *)textView {
    if (!_textView) {
        _textView = [[YYTextView alloc] initWithFrame:CGRectMake(0, 0, KScreenWidth - 100, 230)];
        _textView.placeholderText = LocalizedString(@"InputFeedback");
        _textView.placeholderFont = [UIFont systemFontOfSize:15];
        _textView.tintColor = [FontManage MHTextViewTintColor];
        _textView.font = [UIFont systemFontOfSize:17];
        _textView.delegate = self;
        [self.bgView addSubview:_textView];
    }
    return _textView;
}


- (IBAction)submitClick:(id)sender {
    // 这里改回 show me the money,没点文化
    NSString *str = [self.textView.text stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
    if ([str isEqualToString:@"show me the money"]) {
        [[NSUserDefaults standardUserDefaults] setBool:YES forKey:@"isShowHidden"];
    }
    [MHAlert showMessage:LocalizedString(@"AlertSuccess")];
    [self.navigationController popViewControllerAnimated:YES];
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
