//
//  ZoloQuoteMsgVC.m
//  NewZolo
//
//  Created by MHHY on 2023/5/17.
//

#import "ZoloQuoteMsgVC.h"

@interface ZoloQuoteMsgVC ()

@property (nonatomic, strong) DMCCQuoteInfo *quoteInfo;

@end

@implementation ZoloQuoteMsgVC

- (instancetype)initWithQuoteInfo:(DMCCQuoteInfo *)quoteInfo {
    if (self = [super init]) {
        self.quoteInfo = quoteInfo;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    
    self.navigationItem.title = LocalizedString(@"RedPackSeeDetail");
    self.msgTextView.editable = NO;
    self.msgTextView.textColor = [UIColor blackColor];
    self.msgTextView.backgroundColor = [UIColor whiteColor];
    self.msgTextView.text = self.quoteInfo.messageDigest;
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
