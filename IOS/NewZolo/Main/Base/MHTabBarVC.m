//
//  MHTabBarVC.m
//  JTalking
//
//  Created by JTalking on 2022/6/24.
//

#import "MHTabBarVC.h"
#import "MHNavigationVC.h"
#import "ZoloContactVC.h"
#import "ZoloHomeViewController.h"
#import "ZoloMyVC.h"
#import "MHLoginModel.h"
#import "AppDelegate.h"
#import "ZoloFindViewController.h"
#import <osnsdk/ZoloOssManager.h>

@interface MHTabBarVC ()

@property (nonatomic, strong) NSMutableArray *tabbarCons;

@end

@implementation MHTabBarVC

+ (void)initialize {
    NSMutableDictionary *attrs = [NSMutableDictionary dictionary];
    if (@available(iOS 13, *)) {
        attrs[NSForegroundColorAttributeName] = [UIColor labelColor];
    } else {
        attrs[NSForegroundColorAttributeName] = [UIColor blackColor];
    }
    
    NSMutableDictionary *sattrs = [NSMutableDictionary dictionary];
    if (@available(iOS 13, *)) {
        sattrs[NSForegroundColorAttributeName] = MHColorFromHex(0x4FA075);
    } else {
        sattrs[NSForegroundColorAttributeName] = MHColorFromHex(0x4FA075);
    }
    
    UITabBarItem *item = [UITabBarItem appearance];
    [item setTitleTextAttributes:attrs forState:UIControlStateNormal];
    [item setTitleTextAttributes:sattrs forState:UIControlStateSelected];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    [[UITabBar appearance] setBackgroundColor:[FontManage MHGrayColor]];
    [UITabBar appearance].translucent = NO;
    
    if (@available(iOS 15.0, *)) {
       UITabBarAppearance * appearance = [UITabBarAppearance new];
       self.tabBar.scrollEdgeAppearance = appearance;
    }
    self.tabBar.tintColor = [FontManage MHMainColor];
    
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login.json.MainDapp.length > 0) {
        @try {
            NSDictionary *dic = [OsnUtils json2Dic:[OsnUtils b64Decode:login.json.MainDapp]];
            DMCCLitappInfo *info = [DMCCLitappInfo new];
            info.target = dic[@"target"];
            info.name = dic[@"name"];
            info.url = dic[@"url"];
            info.portrait = dic[@"portrait"];
            [self setUpChildViewController:[[ZoloFindViewController alloc] initWithLitappInfo:info] title:LocalizedString(@"Tabfind") image:@"find_n" selectedImage:@"find_s"];
        }
        @catch (NSException *exception){
            NSLog(@"%@", exception);
        }
    }
    
    [self setUpChildViewController:[ZoloHomeViewController new] title:LocalizedString(@"Home") image:@"chat_off" selectedImage:@"chat_on"];
    [self setUpChildViewController:[ZoloContactVC new] title:LocalizedString(@"Contact") image:@"contacts_off" selectedImage:@"contacts_on"];
    [self setUpChildViewController:[ZoloMyVC new] title:LocalizedString(@"Me") image:@"my_off" selectedImage:@"my_on"];
    // 动态添加选项
    self.viewControllers = self.tabbarCons;
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(LoginOutKikoffUpdated) name:kLoginOutKikoffUpdated object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(ossDataExceedFun) name:ossDataExceed object:nil];
    
    [self getAllTagInfo];
    [self updateBadgeNumber];
    [self checkUpdateVersion];
}

- (void)ossDataExceedFun {
    [MHAlert showMessage:LocalizedString(@"DataExceed")];
}

- (void)getAllTagInfo {
    [[ZoloAPIManager instanceManager] getTagNameUserAllListWithCompleteBlock:^(NSArray * _Nonnull data) {
        
    }];
}

- (void)LoginOutKikoffUpdated {
    
    NSString *savedToken = [[NSUserDefaults standardUserDefaults] stringForKey:@"ospn_token"];
    NSString *savedUserId = [[NSUserDefaults standardUserDefaults] stringForKey:@"ospn_id"];

    if (savedToken.length > 0 && savedUserId.length > 0) {
        [MHAlert showNormalAlert:LocalizedString(@"AlertSureRefreshLogin") withAlerttype:MHAlertTypeWarn withOkBlock:^(UIAlertAction * _Nonnull action) {
            if ([action.title isEqualToString:LocalizedString(@"Sure")]) {
                [[NSUserDefaults standardUserDefaults] removeObjectForKey:@"ospn_token"];
                [[NSUserDefaults standardUserDefaults] removeObjectForKey:@"ospn_id"];
                [[NSUserDefaults standardUserDefaults] removeObjectForKey:@"mne_account_current"];
                AppDelegate *appDelegate = (AppDelegate *)[UIApplication sharedApplication].delegate;
                [appDelegate setRootView];
            }
        }];
    }
}

// 检查版本更新 26 > 26
- (void)checkUpdateVersion {
    NSString *key = @"CFBundleVersion";
    NSString *currentVersion = [NSBundle mainBundle].infoDictionary[key];
    [[ZoloAPIManager instanceManager] getCheckUpdateWithCompleteBlock:^(NSDictionary * _Nonnull data) {
        if (data) {
            NSString *updateVersion = data[@"updateVersion"];
            NSString *isShow = data[@"isShow"];
            NSString *userStr = data[@"user"];
            NSNumber *iosCode = data[@"iosCode"];
            if (isShow.intValue == 1 && [self checkUser:userStr]) {
                if (iosCode.integerValue > currentVersion.integerValue) {
                    if (updateVersion.intValue == 1) {
                        [MHAlert showNormalAlert:LocalizedString(@"AppVersion") withAlerttype:MHAlertTypeRemind withOkBlock:^(UIAlertAction * _Nonnull action) {
                            if ([action.title isEqualToString:LocalizedString(@"Sure")]) {
                                [[UIApplication sharedApplication] openURL:[NSURL URLWithString:data[@"iosUrl"]] options:@{} completionHandler:nil];
                            }
                        }];
                    } else {
                        [MHAlert showCustomeAlert:LocalizedString(@"AppVersion") withAlerttype:MHAlertTypeRemind withOkBlock:^(UIAlertAction * _Nonnull action) {
                            if ([action.title isEqualToString:LocalizedString(@"Sure")]) {
                                [[UIApplication sharedApplication] openURL:[NSURL URLWithString:data[@"iosUrl"]] options:@{} completionHandler:nil];
                            }
                        }];
                    }
                }
            }
        }
    }];
}

- (BOOL)checkUser:(NSString *)userString {
    NSArray *users = [userString componentsSeparatedByString:@","];
    NSString *cUser = [[DMCCIMService sharedDMCIMService] getUserID];
    BOOL isShow = YES;
    for (NSString *user in users) {
        if ([user isEqualToString:cUser]) {
            isShow = NO;
            break;
        }
    }
    return isShow;
}

/**用于动态添加选项*/
- (void)setUpChildViewController:(UIViewController *)vc title:(NSString *)title image:(NSString *)image selectedImage:(NSString *)selectedImage {
    vc.title = title;
    vc.tabBarItem.image = [[UIImage imageNamed:image] imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal];
    vc.tabBarItem.selectedImage = [[UIImage imageNamed:selectedImage] imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal];
    MHNavigationVC *nav = [[MHNavigationVC alloc] initWithRootViewController:vc];
    [self.tabbarCons addObject:nav];
}

- (NSMutableArray *)tabbarCons {
    if (!_tabbarCons) {
        _tabbarCons = [NSMutableArray arrayWithCapacity:0];
    }
    return _tabbarCons;
}

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

// 未读计数
- (void)updateBadgeNumber {
    NSMutableArray *tagArray = [NSMutableArray arrayWithCapacity:0];
    
    DMCCTagInfo *user = [DMCCTagInfo new];
    user.group_name = @"好友";
    user.id = -1;
    
    [tagArray addObject:user];
    
    DMCCTagInfo *group = [DMCCTagInfo new];
    group.group_name = @"群组";
    group.id = -2;
    [tagArray addObject:group];
    
    
    NSArray *tagInfos = [[DMCCIMService sharedDMCIMService] getTagList];
    [tagArray addObjectsFromArray:tagInfos];
    
    NSInteger msgCount = 0;
    
    for (DMCCTagInfo *info in tagArray) {
        NSInteger count = [[DMCCIMService sharedDMCIMService] getUnreadCountWithTagId:info.id];
        info.count = count;
        msgCount += count;
    }

    if (msgCount > 0) {
        MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
        if (login.json.MainDapp.length > 0) {
            [self.tabBar showBadgeOnItemIndex:1];
        } else {
            [self.tabBar showBadgeOnItemIndex:0];
        }
    }
    else {
        MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
        if (login.json.MainDapp.length > 0) {
            [self.tabBar hideBadgeOnItemIndex:1];
        } else {
            [self.tabBar hideBadgeOnItemIndex:0];
        }
    }
    int count = [[DMCCIMService sharedDMCIMService] getUnreadFriendRequestStatus];
    if (count > 0) {
        MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
        if (login.json.MainDapp.length > 0) {
            [self.tabBar showBadgeOnItemIndex:2];
        } else {
            [self.tabBar showBadgeOnItemIndex:1];
        }
    }
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
