//
//  MHNavigationVC.m
//  JTalking
//
//  Created by JTalking on 2022/6/24.
//

#import "MHNavigationVC.h"

@interface MHNavigationVC ()

@end

@implementation MHNavigationVC

+ (void)initialize {
    UINavigationBar *bar = [UINavigationBar appearance];
    [bar setShadowImage:[UIImage new]];
    [[UIBarButtonItem appearance] setTitleTextAttributes:@{NSFontAttributeName:[UIFont systemFontOfSize:19 weight:9], NSForegroundColorAttributeName : [UIColor clearColor]} forState:UIControlStateNormal];
    [[UIBarButtonItem appearance] setTitleTextAttributes:@{NSFontAttributeName:[UIFont systemFontOfSize:19 weight:9], NSForegroundColorAttributeName : [UIColor clearColor]} forState:UIControlStateHighlighted];
    [bar setBackgroundImage:[UIImage imageNamed:@"home_navbg"] forBarMetrics:UIBarMetricsDefault];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    if (@available(iOS 13.0, *)) {
       UINavigationBarAppearance *appearance = [[UINavigationBarAppearance alloc] init];
        appearance.backgroundImage = [UIImage imageNamed:@"home_navbg"];
        appearance.backgroundColor = [FontManage MHWhiteColor];
        appearance.backgroundEffect = nil;
        appearance.shadowImage = [UIImage new];
        appearance.shadowColor = [UIColor clearColor];
        appearance.titleTextAttributes = @{
           NSForegroundColorAttributeName : [FontManage MHBlockColor],
           NSFontAttributeName : [UIFont boldSystemFontOfSize:18],
       };
        
       self.navigationBar.standardAppearance = appearance;
       self.navigationBar.scrollEdgeAppearance = appearance;
    } else {
        [self.navigationBar setTitleTextAttributes:@{NSForegroundColorAttributeName : [UIColor whiteColor]}];
    }
}

- (void)pushViewController:(UIViewController *)viewController animated:(BOOL)animated {
    if (self.childViewControllers.count >= 1) {
        viewController.hidesBottomBarWhenPushed = YES;
        UIImage *backImg = [UIImage imageNamed:@"Back"];
        viewController.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithImage:backImg style:UIBarButtonItemStylePlain target:self action:@selector(leftBarButtonItemPop)];
    }
    [super pushViewController:viewController animated:animated];
}

- (void)leftBarButtonItemPop {
    [self popViewControllerAnimated:YES];
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
