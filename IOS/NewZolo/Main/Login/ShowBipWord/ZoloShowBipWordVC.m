//
//  ZoloShowBipWordVC.m
//  NewZolo
//
//  Created by MHHY on 2023/5/22.
//

#import "ZoloShowBipWordVC.h"

@interface ZoloShowBipWordVC ()

@property (weak, nonatomic) IBOutlet UITextView *wordTextView;
@property (nonatomic, copy) NSString *mnemonicString;

@end

@implementation ZoloShowBipWordVC

- (instancetype)initWithWords:(NSString *)words {
    if (self = [super init]) {
        self.mnemonicString = words;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    
    self.wordTextView.text = self.mnemonicString;
    self.wordTextView.layer.cornerRadius = 8;
    self.view.backgroundColor = MHColorFromHex(0x23262B);
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
