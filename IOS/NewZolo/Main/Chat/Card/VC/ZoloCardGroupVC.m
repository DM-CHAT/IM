//
//  ZoloCardGroupVC.m
//  NewZolo
//
//  Created by JTalking on 2022/9/2.
//

#import "ZoloCardGroupVC.h"

@interface ZoloCardGroupVC ()

@property (nonatomic, strong) DMCCGroupInfo *info;

@end

@implementation ZoloCardGroupVC

- (instancetype)initWithGroupInfo:(DMCCGroupInfo*)info {
    if (self == [super init]) {
        self.info = info;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
}

- (void)getGroupInfo:(NSString *)groupId {
    if ([groupId isEqualToString:self.info.target]) {
        return;
    }
    DMCCGroupInfo *groupInfo = [[DMCCIMService sharedDMCIMService] getGroupInfo:groupId refresh:NO];
    if (groupInfo) {
        groupInfo.target = groupId;
        [self.dataSource addObject:groupInfo];
    }
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    DMCCGroupInfo *groupInfo = self.dataSource[indexPath.row];
    if (_groupSelectBlock) {
        _groupSelectBlock(groupInfo);
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
