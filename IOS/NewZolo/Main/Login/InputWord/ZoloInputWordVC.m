//
//  ZoloInputWordVC.m
//  NewZolo
//
//  Created by MHHY on 2023/5/19.
//

#import "ZoloInputWordVC.h"
#import "ZoloSettingPasswordVC.h"
#import "YYTextView.h"

@interface ZoloInputWordVC ()  <YYTextViewDelegate>

@property (weak, nonatomic) IBOutlet UITextView *wordTextView;
@property (weak, nonatomic) IBOutlet UIButton *inputBtn;

@property (weak, nonatomic) IBOutlet UIView *wordView;

@property (nonatomic, strong) YYTextView *textView;

@property (nonatomic, strong) NSString *loginUrl;

@end

@implementation ZoloInputWordVC

- (instancetype)initWithLoginUrl:(NSString *)url {
    if (self = [super init]) {
        self.loginUrl = url;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    
    [self textView];
    self.wordTextView.layer.cornerRadius = 8;
    self.view.backgroundColor = MHColorFromHex(0x23262B);
}

- (YYTextView *)textView {
    if (!_textView) {
        _textView = [[YYTextView alloc] initWithFrame:CGRectMake(0, 0, self.wordView.width, self.wordView.height)];
        _textView.placeholderText = LocalizedString(@"InputWordLogin");
        _textView.placeholderFont = [UIFont systemFontOfSize:20];
        _textView.tintColor = [UIColor whiteColor];
        _textView.font = [UIFont systemFontOfSize:20];
        _textView.delegate = self;
        [self.wordView addSubview:_textView];
    }
    return _textView;
}

- (IBAction)inoutBtnClick:(id)sender {
    
    NSString *inputText = self.textView.text;
    NSArray *words = [inputText componentsSeparatedByString:@" "];
    if (words.count == 12) {
        [self.navigationController pushViewController:[[ZoloSettingPasswordVC alloc] initWithWords:inputText loginUrl:self.loginUrl] animated:YES];
    } else {
        [MHAlert showMessage:LocalizedString(@"ErrorInput")];
    }
}

- (IBAction)backBtnClick:(id)sender {
    [self.navigationController popViewControllerAnimated:YES];
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
