//
//  ZoloGroupNoticeVC.m
//  NewZolo
//
//  Created by JTalking on 2022/8/18.
//

#import "ZoloSeeSystemNoticeVC.h"
#import "ZoloNotice.h"

@interface ZoloSeeSystemNoticeVC ()

@property (nonatomic, strong) ZoloNotice *info;

@property (weak, nonatomic) IBOutlet UITextView *textView;
@property (weak, nonatomic) IBOutlet UILabel *textLab;
@property (weak, nonatomic) IBOutlet UILabel *showLab;
@property (weak, nonatomic) IBOutlet UIScrollView *scrollview;

@end

@implementation ZoloSeeSystemNoticeVC

- (instancetype)initWithNoticeInfo:(ZoloNotice *)notice {
    if (self = [super init]) {
        self.info = notice;
    }
    return self;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self.navigationController setNavigationBarHidden:YES animated:animated];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [self.navigationController setNavigationBarHidden:NO animated:animated];
}

- (IBAction)backClick:(id)sender {
    [self.navigationController popViewControllerAnimated:YES];
}


- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    
    self.scrollview.scrollEnabled = YES;
    self.scrollview.contentSize = CGSizeMake(UIScreen.mainScreen.bounds.size.width, UIScreen.mainScreen.bounds.size.height + 1000);
    
    self.view.backgroundColor = MHColorFromHex(0x57B77D);
    self.textLab.text = LocalizedString(@"ChatNotice");
    self.textView.text = [NSString stringWithFormat:@"%@", self.info.remark];
    self.showLab.text = self.info.remark;
    self.textView.textColor = [UIColor blackColor];
    self.textView.backgroundColor = [UIColor whiteColor];
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
