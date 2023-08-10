//
//  ZoloFileRecordVC.m
//  NewZolo
//
//  Created by JTalking on 2022/8/18.
//

#import "ZoloFileRecordVC.h"
#import "EmptyVC.h"
@interface ZoloFileRecordVC ()
@property (nonatomic, strong) EmptyVC *emptyVC;
@end

@implementation ZoloFileRecordVC

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.

    [self.view insertSubview:self.emptyVC.view aboveSubview:self.tableView];
      
}

- (EmptyVC *)emptyVC {
    if (!_emptyVC) {
        _emptyVC = [[EmptyVC alloc] init];
        _emptyVC.view.frame = CGRectMake(0, 0, KScreenWidth, KScreenHeight);
    }
    return _emptyVC;
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
