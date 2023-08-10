//
//  ZoloThemeVC.m
//  NewZolo
//
//  Created by JTalking on 2022/9/15.
//

#import "ZoloThemeVC.h"
#import "appDelegate.h"
#import "ZoloThemeCell.h"

@interface ZoloThemeVC ()

@property (nonatomic, strong) NSArray *titleArray;
@property (nonatomic, assign) NSInteger sIndex;

@end

@implementation ZoloThemeVC

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    self.titleArray = @[LocalizedString(@"ThemeSystem"), LocalizedString(@"ThemeDark"), LocalizedString(@"ThemeLight")];
    [self registerCellWithNibName:NSStringFromClass([ZoloThemeCell class]) isTableview:YES];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.titleArray.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ZoloThemeCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloThemeCell class])];
    cell.titleLab.text = self.titleArray[indexPath.row];
    if (@available(iOS 13.0, *)) {
        NSString *themeStr = [[NSUserDefaults standardUserDefaults] stringForKey:@"theme"];
        if ([themeStr isEqualToString:@"light"]) {
            self.sIndex = 2;
        } else if ([themeStr isEqualToString:@"dark"]) {
            self.sIndex = 1;
        } else {
            self.sIndex = 0;
        }
        if (indexPath.row == self.sIndex) {
            cell.img.hidden = NO;
        } else {
            cell.img.hidden = YES;
        }
    }
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    if (@available(iOS 13.0, *)) {
        NSString *str = @"";
        AppDelegate *appDelegate = (AppDelegate *)[UIApplication sharedApplication].delegate;
        if (indexPath.row == 0) {
            appDelegate.window.overrideUserInterfaceStyle = UIUserInterfaceStyleUnspecified;
            str = @"uns";
        } else if (indexPath.row == 1) {
            appDelegate.window.overrideUserInterfaceStyle = UIUserInterfaceStyleDark;
            str = @"dark";
        } else {
            appDelegate.window.overrideUserInterfaceStyle = UIUserInterfaceStyleLight;
            str = @"light";
        }
        [[NSUserDefaults standardUserDefaults] setValue:str forKey:@"theme"];
    }
    [self.tableView reloadData];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 0.001;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return self.scaleHeight(54);
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
