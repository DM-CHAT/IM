//
//  ZoloMoveTagVC.m
//  NewZolo
//
//  Created by JTalking on 2022/10/31.
//

#import "ZoloMoveTagVC.h"
#import "ZoloTagUserCell.h"
#import "ZoloTagMoveView.h"
#import "ZoloSettingContactCell.h"

@interface ZoloMoveTagVC () <UIActionSheetDelegate>

@property (nonatomic, strong) ZoloTagMoveView *tagMoveView;
@property (nonatomic, strong) NSMutableArray *tagNameArray;

@end

@implementation ZoloMoveTagVC

- (void)viewDidLoad {
    [super viewDidLoad];
    [self registerCellWithNibName:NSStringFromClass([ZoloSettingContactCell class]) isTableview:YES];
    self.btnView.hidden = YES;
    [self.view addSubview:self.tagMoveView];
    
    NSArray *tagInfos = [[DMCCIMService sharedDMCIMService] getTagList];
    for (DMCCTagInfo *tag in tagInfos) {
        if (self.info.id != tag.id) {
            [self.tagNameArray addObject:tag];
        }
    }
}

- (NSMutableArray *)tagNameArray {
    if (!_tagNameArray) {
        _tagNameArray = [NSMutableArray arrayWithCapacity:0];
    }
    return _tagNameArray;
}

- (ZoloTagMoveView *)tagMoveView {
    if (!_tagMoveView) {
        _tagMoveView = [[ZoloTagMoveView alloc] initWithFrame:CGRectZero];
        WS(ws);
        _tagMoveView.tagDelBtnBlock = ^{
            [ws delBtnClick];
        };
        _tagMoveView.tagMoveBtnBlock = ^{
            [ws moveBtnClick];
        };
        
        _tagMoveView.tagAllBtnBlock = ^(BOOL isAll) {
            [ws tagAllBtnClick:isAll];
        };
    }
    return _tagMoveView;
}

- (void)delBtnClick {
    NSMutableArray *array = [[NSMutableArray alloc] initWithCapacity:0];
    
    NSMutableArray *uArray = [[NSMutableArray alloc] initWithCapacity:0];
    NSMutableArray *gArray = [[NSMutableArray alloc] initWithCapacity:0];
    
    for (id info in self.dataSource) {
        if ([info isKindOfClass:[DMCCUserInfo class]]) {
            DMCCUserInfo *user = (DMCCUserInfo *)info;
            if (user.isSelect) {
                [array addObject:@(user.ugID)];
                [uArray addObject:user.userId];
            }
        } else {
            DMCCGroupInfo *group = (DMCCGroupInfo *)info;
            if (group.isSelect) {
                [array addObject:@(group.ugID)];
                [gArray addObject:group.target];
            }
        }
    }
    WS(ws);
    [[ZoloAPIManager instanceManager] delTagUserList:array WithCompleteBlock:^(BOOL isSuccess) {
        if (isSuccess) {
            [ws updateLocalDB:uArray group:gArray];
            [ws.navigationController popViewControllerAnimated:YES];
        }
    }];
}

- (void)updateLocalDB:(NSArray *)users group:(NSArray *)groups {
    [[DMCCIMService sharedDMCIMService] updateUserWithUsers:users withTagWithkey:@[@"tagID"]];
    [[DMCCIMService sharedDMCIMService] updateGroupWithGroups:groups withTagWithkey:@[@"tagID"]];
}

- (void)moveBtnClick {
    UIActionSheet *actionSheet = [[UIActionSheet alloc]initWithTitle:LocalizedString(@"PlaseSelect") delegate:self cancelButtonTitle:nil destructiveButtonTitle:nil otherButtonTitles:nil];
    for (int i = 0; i < self.tagNameArray.count; i++) {
        DMCCTagInfo *tag = self.tagNameArray[i];
        [actionSheet addButtonWithTitle:tag.group_name];
    }
    [actionSheet addButtonWithTitle:LocalizedString(@"Cancel")];
    actionSheet.cancelButtonIndex = actionSheet.numberOfButtons -1;
    actionSheet.actionSheetStyle = UIActionSheetStyleBlackOpaque;
    [actionSheet showInView:self.view];
}

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    if (buttonIndex != (self.tagNameArray.count)) {
        
        NSMutableArray *mArray = [NSMutableArray arrayWithCapacity:0];
        DMCCTagInfo *tagInfo = self.tagNameArray[buttonIndex];
        for (id info in self.dataSource) {
            if ([info isKindOfClass:[DMCCUserInfo class]]) {
                DMCCUserInfo *user = (DMCCUserInfo *)info;
                if (user.isSelect) {
                    ZoloTagModel *tag = [ZoloTagModel new];
                    tag.group_name = tagInfo.group_name;
                    tag.parent_id = tagInfo.id;
                    tag.id = user.ugID;
                    tag.osn_id = user.userId;
                    tag.type = 2;
                    [mArray addObject:@(tag.id)];
                }
            } else {
                DMCCGroupInfo *group = (DMCCGroupInfo *)info;
                if (group.isSelect) {
                    ZoloTagModel *tag = [ZoloTagModel new];
                    tag.group_name = tagInfo.group_name;
                    tag.parent_id = tagInfo.id;
                    tag.id = group.ugID;
                    tag.osn_id = group.target;
                    tag.type = 3;
                    [mArray addObject:@(tag.id)];
                }
            }
        }
        WS(ws);
        [[ZoloAPIManager instanceManager] moveTagUserList:mArray tagId:tagInfo.id group_name:tagInfo.group_name WithCompleteBlock:^(BOOL isSuccess) {
            if (isSuccess) {
                [ws.navigationController popViewControllerAnimated:YES];
            }
        }];
    }
}

- (void)tagAllBtnClick:(BOOL)isAll {
    for (id info in self.dataSource) {
        if ([info isKindOfClass:[DMCCUserInfo class]]) {
            DMCCUserInfo *user = (DMCCUserInfo *)info;
            user.isSelect = isAll;
        } else {
            DMCCGroupInfo *group = (DMCCGroupInfo *)info;
            group.isSelect = isAll;
        }
    }
    [self.tableView reloadData];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ZoloSettingContactCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloSettingContactCell class])];
    id info = self.dataSource[indexPath.row];
    if ([info isKindOfClass:[DMCCUserInfo class]]) {
        DMCCUserInfo *user = self.dataSource[indexPath.row];
        [cell.icon sd_setImageWithURL:[NSURL URLWithString:user.portrait] placeholderImage:SDImageDefault];
        cell.nickName.text = user.displayName;
        cell.selectImg.image = user.isSelect ? [UIImage imageNamed:@"select_s"] : [UIImage imageNamed:@"select_n"];
    } else {
        DMCCGroupInfo *group = self.dataSource[indexPath.row];
        [cell.icon sd_setImageWithURL:[NSURL URLWithString:group.portrait] placeholderImage:SDImageDefault];
        cell.nickName.text = group.name;
        cell.selectImg.image = group.isSelect ? [UIImage imageNamed:@"select_s"] : [UIImage imageNamed:@"select_n"];
    }

    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    id info = self.dataSource[indexPath.row];
    if ([info isKindOfClass:[DMCCUserInfo class]]) {
        DMCCUserInfo *user = self.dataSource[indexPath.row];
        user.isSelect = !user.isSelect;
    } else {
        DMCCGroupInfo *group = self.dataSource[indexPath.row];
        group.isSelect = !group.isSelect;
    }
    [self.tableView reloadData];
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
