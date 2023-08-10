//
//  ZoloAddFriendVC.m
//  NewZolo
//
//  Created by JTalking on 2022/8/24.
//

#import "ZoloAddFriendVC.h"
#import "ZoloAddFriendCell.h"
#import "ZoloAddFriendFootView.h"
#import "ZoloAddFriendInfoVC.h"
#import "ZoloContactDetailVC.h"
#import "ZoloSingleChatVC.h"
#import "ZoloAddGroupInfoVC.h"

@interface ZoloAddFriendVC ()

@property (nonatomic, copy) NSString *userStr;

@end

@implementation ZoloAddFriendVC

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
    self.navigationItem.title = LocalizedString(@"AddFriend");
    [self registerCellWithNibName:NSStringFromClass([ZoloAddFriendCell class]) isTableview:YES];
    [self registerHeaderFooterWithNibName:NSStringFromClass([ZoloAddFriendFootView class])];
}
 
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return 1;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ZoloAddFriendCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloAddFriendCell class])];
    WS(ws);
    cell.textFieldBlock = ^(NSString * _Nonnull str) {
        ws.userStr = str;
        [ws addFriend];
    };
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 0.001;
}

- (UIView *)tableView:(UITableView *)tableView viewForFooterInSection:(NSInteger)section {
    ZoloAddFriendFootView *view = [tableView dequeueReusableHeaderFooterViewWithIdentifier:NSStringFromClass([ZoloAddFriendFootView class])];
    WS(ws);
    view.adddFriendBlock = ^{
        NSArray *array = ws.tableView.visibleCells;
        ZoloAddFriendCell *cell = array.firstObject;
        ws.userStr = cell.textField.text;
        [ws addFriend];
    };
    return view;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 520;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return self.scaleHeight(84);
}

- (DMCCConversationInfo *)getConversation:(NSString *)target {
    DMCCConversation *conversation = [DMCCConversation conversationWithType:Group_Type target:target line:0];
    DMCCConversationInfo *info = [[DMCCIMService sharedDMCIMService] getConversationInfo:conversation];
    return info;
}

- (void)addFriend {
    if (self.userStr.length == 0) {
        return;
    }
    
    if ([self.userStr hasPrefix:@"OSNG"]) {
//        WS(ws);
        
        DMCCConversationInfo * convInfo = [self getConversation:self.userStr];
        if (convInfo != nil) {
            DMCCConversationInfo *info = [DMCCConversationInfo new];
            DMCCConversation *conversation = [DMCCConversation conversationWithType:Group_Type target:self.userStr line:0];
            info.conversation = conversation;
            [self.navigationController pushViewController:[[ZoloSingleChatVC alloc] initWithConversationInfo:info] animated:YES];
            return;
        }
        
        DMCCGroupInfo *groupInfo = [DMCCGroupInfo new];
        groupInfo.name = self.userStr;
        groupInfo.target = self.userStr;
        groupInfo.portrait = @"";
        ZoloAddGroupInfoVC *vc = [[ZoloAddGroupInfoVC alloc] initWithGroupInfo:groupInfo];
        [self.navigationController pushViewController:vc animated:YES];
        
//        [MHAlert showLoadingStr:LocalizedString(@"AlertOpreating")];
//        // 在这里加个转圈
//        [[DMCCIMService sharedDMCIMService] getGroupInfoEx:self.userStr
//                                                   refresh:true
//                                                        cb:^(bool isSuccess2, DMCCGroupInfo* groupInfo, NSString *error) {
//            [MHAlert dismiss];
//
//            if (isSuccess2) {
//                if (groupInfo.isMember !=nil) {
//                    if ([groupInfo.isMember isEqualToString:@"yes"]) {
//                        [[DMCCIMService sharedDMCIMService] insertConversation:self.userStr groupInfo:groupInfo];
//                        DMCCConversation *conversation = [DMCCConversation conversationWithType:Group_Type target:self.userStr line:0];
//                        DMCCConversationInfo *info = [DMCCConversationInfo new];
//                        info.conversation = conversation;
//
//                        dispatch_async(dispatch_get_main_queue(), ^{
//                            [ws.navigationController pushViewController:[[ZoloSingleChatVC alloc] initWithConversationInfo:info] animated:YES];
//                        });
//                        return;
//                    }
//                }
//            } else {
//                groupInfo = [DMCCGroupInfo new];
//                groupInfo.name = @"";
//                groupInfo.target = self.userStr;
//                groupInfo.portrait = @"";
//            }
//
//
//
//            dispatch_async(dispatch_get_main_queue(), ^{
//
//                ZoloAddGroupInfoVC *vc = [[ZoloAddGroupInfoVC alloc] initWithGroupInfo:groupInfo];
//
//                [ws.navigationController pushViewController:vc animated:YES];
//            });
//
//
//        }];
        
    } else if ([self.userStr hasPrefix:@"OSNU"]) {
        
        // 判断好友是否已经存在
        DMCCUserInfo *userInfo = [DMCCUserInfo new];
        userInfo.userId = self.userStr;
        if ([[DMCCIMService sharedDMCIMService] isMyFriend:userInfo.userId]) {
            [self.navigationController pushViewController:[[ZoloContactDetailVC alloc] initWithUserInfo:userInfo] animated:YES];
        } else {
            ZoloAddFriendInfoVC *vc = [[ZoloAddFriendInfoVC alloc] initWithUserInfo:userInfo];
            [self.navigationController pushViewController:vc animated:YES];
        }
        
//        [MHAlert showLoadingStr:LocalizedString(@"AlertOpreating")];
//        WS(ws);
//        [[DMCCIMService sharedDMCIMService] getUserInfo:self.userStr refresh:true success:^(DMCCUserInfo *userInfo) {
//            [MHAlert dismiss];
//            
//            dispatch_async(dispatch_get_main_queue(), ^{
//                // 判断好友是否已经存在
//                if ([[DMCCIMService sharedDMCIMService] isMyFriend:userInfo.userId]) {
//                    [ws.navigationController pushViewController:[[ZoloContactDetailVC alloc] initWithUserInfo:userInfo] animated:YES];
//                } else {
//                    ZoloAddFriendInfoVC *vc = [[ZoloAddFriendInfoVC alloc] initWithUserInfo:userInfo];
//                    [ws.navigationController pushViewController:vc animated:YES];
//                }
//            });
//        } error:^(int errorCode) {
//            [MHAlert dismiss];
//            dispatch_async(dispatch_get_main_queue(), ^{
//                // 判断好友是否已经存在
//                DMCCUserInfo *userInfo = [DMCCUserInfo new];
//                userInfo.userId = self.userStr;
//                if ([[DMCCIMService sharedDMCIMService] isMyFriend:userInfo.userId]) {
//                    [ws.navigationController pushViewController:[[ZoloContactDetailVC alloc] initWithUserInfo:userInfo] animated:YES];
//                } else {
//                    ZoloAddFriendInfoVC *vc = [[ZoloAddFriendInfoVC alloc] initWithUserInfo:userInfo];
//                    [ws.navigationController pushViewController:vc animated:YES];
//                }
//            });
//        }];
        
        
    } else {
        // 不支持的格式
        [self getOsnId];
    }
    
    
    /*
    if ([self.userStr hasPrefix:@"OSN"]) {
        WS(ws);
        [[DMCCIMService sharedDMCIMService] getUserInfo:self.userStr refresh:true success:^(DMCCUserInfo *userInfo) {
            dispatch_async(dispatch_get_main_queue(), ^{
                // 判断好友是否已经存在
                if ([[DMCCIMService sharedDMCIMService] isMyFriend:userInfo.userId]) {
                    [ws.navigationController pushViewController:[[ZoloContactDetailVC alloc] initWithUserInfo:userInfo] animated:YES];
                } else {
                    ZoloAddFriendInfoVC *vc = [[ZoloAddFriendInfoVC alloc] initWithUserInfo:userInfo];
                    [ws.navigationController pushViewController:vc animated:YES];
                }
            });
        } error:^(int errorCode) {
            dispatch_async(dispatch_get_main_queue(), ^{
                [MHAlert showMessage:LocalizedString(@"AlertAddFail")];
            });
        }];
    } else {
        [self getOsnId];
    }
     */
}

- (void)getOsnId {
    WS(ws);
    [MHAlert showLoadingStr:LocalizedString(@"AlertOpreating")];
    [[ZoloAPIManager instanceManager] getOsnId:self.userStr WithCompleteBlock:^(NSString * _Nonnull data) {
        [MHAlert dismiss];
        if (data) {
            [[DMCCIMService sharedDMCIMService] getUserInfo:data refresh:true success:^(DMCCUserInfo *userInfo) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    // 判断好友是否已经存在
                    if ([[DMCCIMService sharedDMCIMService] isMyFriend:userInfo.userId]) {
                        [ws.navigationController pushViewController:[[ZoloContactDetailVC alloc] initWithUserInfo:userInfo] animated:YES];
                    } else {
                        ZoloAddFriendInfoVC *vc = [[ZoloAddFriendInfoVC alloc] initWithUserInfo:userInfo];
                        [ws.navigationController pushViewController:vc animated:YES];
                    }
                });
            } error:^(int errorCode) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    [MHAlert showMessage:LocalizedString(@"AlertAddFail")];
                });
            }];
        } else {
            [MHAlert showMessage:LocalizedString(@"ErrorAccountNomal")];
        }
    }];
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
