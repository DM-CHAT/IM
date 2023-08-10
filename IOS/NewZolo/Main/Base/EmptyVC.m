//
//  QLEmptyVC.m
//  QiLinTuoZhan
//
//  Created by kangpeng on 2018/3/23.
//  Copyright © 2018年 lj. All rights reserved.
//

#import "EmptyVC.h"

@interface EmptyVC ()

@end

@implementation EmptyVC

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.frame = CGRectMake(0, 0, KScreenWidth, KScreenHeight);
    self.view.backgroundColor = [FontManage MHGrayColor];
    self.message.text = LocalizedString(@"EmptyPageContent");
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
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
