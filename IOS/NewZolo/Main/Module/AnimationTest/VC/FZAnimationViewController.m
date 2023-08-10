//
//  FZAnimationViewController.m
//  FZUBICell
//
//  Created by fzjy on 2020/6/1.
//  Copyright © 2020 梁兴炎. All rights reserved.
//

#import "FZAnimationViewController.h"
#import "LOTAnimationView.h"

@interface FZAnimationViewController ()

@property (nonatomic, strong) LOTAnimationView *lottieLogo;
@property (nonatomic, assign) NSInteger pageNum;

@end

@implementation FZAnimationViewController

- (instancetype)initWithPageNumber:(NSInteger)number {
    if (self = [super init]) {
        self.pageNum = number;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.navigationItem.title = @"动画测试";
    [self.view addSubview:self.lottieLogo];
}

- (LOTAnimationView *)lottieLogo {
    if (!_lottieLogo) {
        _lottieLogo = [LOTAnimationView animationNamed:[NSString stringWithFormat:@"animation_%ld", self.pageNum]];
        _lottieLogo.contentMode = UIViewContentModeScaleAspectFit;
        _lottieLogo.loopAnimation = YES;
    }
    return _lottieLogo;
}

- (void)viewWillAppear:(BOOL)animated {
  [super viewWillAppear:animated];
  [self.lottieLogo play];
}

- (void)viewDidDisappear:(BOOL)animated {
  [super viewDidDisappear:animated];
  [self.lottieLogo pause];
}

- (void)viewDidLayoutSubviews {
  [super viewDidLayoutSubviews];
  CGRect lottieRect = CGRectMake(0, 0, self.view.bounds.size.width, self.view.bounds.size.height);
  self.lottieLogo.frame = lottieRect;
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
