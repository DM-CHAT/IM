//
//  ZoloCardSmallProgramVC.m
//  NewZolo
//
//  Created by JTalking on 2022/9/2.
//

#import "ZoloCardSmallProgramVC.h"

@interface ZoloCardSmallProgramVC ()

@end

@implementation ZoloCardSmallProgramVC

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    DMCCLitappInfo *info = self.dataSource[indexPath.row];
    if (_litAppSelectBlock) {
        _litAppSelectBlock(info);
    }
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
