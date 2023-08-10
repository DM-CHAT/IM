//
//  ZoloCreateWordVC.m
//  NewZolo
//
//  Created by MHHY on 2023/5/19.
//

#import "ZoloCreateWordVC.h"
#import "ZoloSettingPasswordVC.h"

@interface ZoloCreateWordVC ()

@property (weak, nonatomic) IBOutlet UITextView *wordTextView;
@property (weak, nonatomic) IBOutlet UIButton *createBtn;
@property (nonatomic, copy) NSString *mnemonicString;
@property (nonatomic, copy) NSString *url;

@end

@implementation ZoloCreateWordVC

- (instancetype)initWithWords:(NSString *)words loginUrl:(NSString *)url {
    if (self = [super init]) {
        self.mnemonicString = words;
        self.url = url;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    
    self.wordTextView.text = self.mnemonicString;
    self.wordTextView.layer.cornerRadius = 8;
    self.view.backgroundColor = MHColorFromHex(0x23262B);
    self.createBtn.layer.cornerRadius = 15;
}



- (IBAction)createBtnClick:(id)sender {
    
    [self.navigationController pushViewController:[[ZoloSettingPasswordVC alloc] initWithWords:self.mnemonicString loginUrl:self.url] animated:YES];
}

- (IBAction)backBtnClick:(id)sender {
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
