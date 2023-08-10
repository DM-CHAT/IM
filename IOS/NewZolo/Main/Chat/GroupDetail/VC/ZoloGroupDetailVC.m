//
//  ZoloGroupDetailVC.m
//  NewZolo
//
//  Created by JTalking on 2022/7/11.
//

#import "ZoloGroupDetailVC.h"
#import "ZoloGroupDetailCell.h"
#import "ZoloGroupDetailFoot.h"
#import "ZoloGroupDetailHead.h"
#import "ZoloChatRecordVC.h"
#import "ZoloFileRecordVC.h"
#import "ZoloGroupManagerVC.h"
#import "ZoloGroupNoticeVC.h"
#import "ZoloMyQrCodeVC.h"
#import "ZoloNickNameVC.h"
#import "ZoloGroupDappListVC.h"
#import "ZoloGroupAddFriendVC.h"
#import "ZoloContactDetailVC.h"
#import "ZoloAddFriendInfoVC.h"
#include <config/config.h>
#import "ZoloLessFriendVC.h"
#import "ZoloWalletViewController.h"
#import "ZoloMoreMemberVC.h"
#import "ZoloRedProbabilityController.h"
#import "ZoloShareCardVC.h"
#import "ZoloTagVC.h"
#import "ZoloChatSearchVC.h"
#import "ZoloTagOperateVC.h"
#import "ZoloComplaintVC.h"

@interface ZoloGroupDetailVC () <UITableViewDelegate, UITableViewDataSource>

@property (nonatomic, strong) NSArray *titleArray;
@property (nonatomic, strong)DMCCConversation *conversation;
@property (nonatomic, strong) NSArray *memberList;
@property (nonatomic, strong)DMCCConversationInfo *conversationInfo;
@property (nonatomic, strong) DMCCLitappInfo *litapp;
@property (nonatomic, assign) BOOL isShowBom;

@end

@implementation ZoloGroupDetailVC

- (instancetype)initWithConversation:(DMCCConversationInfo *)conversationInfo withDapp:(DMCCLitappInfo *)dapp isShowBom:(BOOL)isShowBom {
    if (self = [super init]) {
        self.conversationInfo = conversationInfo;
        self.conversation = conversationInfo.conversation;
        self.litapp = dapp;
        self.isShowBom = isShowBom;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [[DMCCIMService sharedDMCIMService] getGroupInfo:self.conversation.target refresh:YES];
    self.memberList = [[DMCCIMService sharedDMCIMService] getGroupZoneMembersTop:self.conversation.target begin:0 forceUpdate:NO];
    
    if (self.memberList != nil) {
        NSLog(@"=test2= getGroupZoneMembers : %ld", self.memberList.count);
    }else{
        NSLog(@"=test2= getGroupZoneMembers : 0");
    }
    
    self.navigationItem.title = LocalizedString(@"ContactDetail");
    self.tableView.backgroundColor = [FontManage MHGrayColor];
    self.tableView.separatorColor = [FontManage MHLineSeparatorColor];
    self.titleArray = @[@[LocalizedString(@"ContactGroupName"), LocalizedString(@"ContactQrCode"), LocalizedString(@"ContactGroupNotice"), LocalizedString(@"ContactGroupManager")], @[LocalizedString(@"ContactChatRecord"), LocalizedString(@"ContactFileRecord"), LocalizedString(@"ContactSelectGroupMoney"), LocalizedString(@"ShareCard"), LocalizedString(@"TagOperation")], @[LocalizedString(@"ContactMsgNoNotice"), LocalizedString(@"ContactOnRedPack"), LocalizedString(@"ContactTopChat"), LocalizedString(@"ContactSaveContact"), LocalizedString(@"RedPackBomSetting")], @[LocalizedString(@"ContactShowMyName"), LocalizedString(@"ContactShowNickName")]];
    [self registerCellWithNibName:NSStringFromClass([ZoloGroupDetailCell class]) isTableview:YES];
    [self registerHeaderFooterWithNibName:NSStringFromClass([ZoloGroupDetailFoot class])];
    [self registerHeaderFooterWithNibName:NSStringFromClass([ZoloGroupDetailHead class])];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(groupUpdata:) name:kGroupMemberAddOrLessUpdated object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(groupMemberUpdata:) name:kGroupMemberUpdated object:nil];
    
    [self.tableView reloadData];
    
    // 这里是否能开个线程来执行，不要影响主界面
    [[DMCCIMService sharedDMCIMService] getGroupZoneMembers:self.conversation.target begin:0 forceUpdate:YES];
}

- (void)groupMemberUpdata:(NSNotification *)notification {
    NSDictionary *dic = (NSDictionary*)notification.userInfo;
    NSArray *array = dic[@"members"];
    if (self.memberList.count == 0 || self.memberList.count != array.count) {
        self.memberList = array;
        [self.tableView reloadData];
    }
}

- (void)groupUpdata:(NSNotification *)notification {
    NSDictionary *dic = (NSDictionary*)notification.userInfo;
    NSArray *array = dic[@"members"];
    if (self.memberList.count == array.count) {
        return;
    }
    self.memberList = array;
    [self.tableView reloadData];
    
    if ([self isGroupOwner]) {
        if (self.memberList.count > 9) {
            NSMutableArray *num = [NSMutableArray arrayWithCapacity:0];
            for (int i = 0; i < 9; i++) {
                // 更新头像
                DMCCGroupMember *member = self.memberList[i];
                DMCCUserInfo *user = [[DMCCIMService sharedDMCIMService] getUserInfo:member.memberId inGroup:member.groupId refresh:NO];
                [num addObject:user.userId];
            }
            [self updateGroupImg:num];
        } else {
            if (self.memberList.count < 9 && array.count != 1) {
                // 更新头像
                NSMutableArray *num = [NSMutableArray arrayWithCapacity:0];
                for (DMCCGroupMember *member in self.memberList) {
                    DMCCUserInfo *user = [[DMCCIMService sharedDMCIMService] getUserInfo:member.memberId inGroup:member.groupId refresh:NO];
                    [num addObject:user.userId];
                }
                [self updateGroupImg:num];
            }
        }
    }
}

- (void)updateGroupImg:(NSArray *)imgArray {
    UIImage *portraitImage = [self loadCombineView:imgArray];
    NSData *portraitData = UIImageJPEGRepresentation(portraitImage, 1);
    WS(ws);
    [[DMCCIMService sharedDMCIMService] uploadMedia:@"" mediaData:portraitData mediaType:Media_Type_PORTRAIT success:^(NSString *remoteUrl) {
        NSLog(@"uploadMedia url: %@", remoteUrl);
        DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:ws.conversation.target refresh:NO];
        [[DMCCIMService sharedDMCIMService] modifyGroupInfo:group.target type:Modify_Group_Portrait newValue:remoteUrl notifyLines:@[@(0)] notifyContent:nil success:^{
            
        } error:^(int error_code) {
            [MHAlert showMessage:LocalizedString(@"AlertSettingFail")];
        }];
        
        [[ZoloAPIManager instanceManager] uploadImgWithGroupId:ws.conversation.target image:remoteUrl WithCompleteBlock:^(BOOL isSuccess) {
            
        }];
        
    } progress:^(long uploaded, long total) {
        
    } error:^(int error_code) {
        
    }];
}



- (UIImage*)loadCombineView:(NSArray<NSString*>*)memberIds {
    int PortraitWidth = 120;
    UIView *combineHeadView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, PortraitWidth, PortraitWidth)];
    [combineHeadView setBackgroundColor:MHColorFromHex(0xf9f9f9)];
    
    NSMutableArray *users = [[NSMutableArray alloc] init];
    if (![memberIds containsObject:[DMCCNetworkService sharedInstance].userId]) {
        DMCCUserInfo *user = [[DMCCIMService sharedDMCIMService] getUserInfo:[DMCCNetworkService sharedInstance].userId refresh:NO];
        [users addObject:user];
    }

    for (NSString *userId in memberIds) {
        DMCCUserInfo *user = [[DMCCIMService sharedDMCIMService] getUserInfo:userId refresh:NO];
        if (user != nil) {
            [users addObject:user];
        }
        if (users.count >= 9) {
            break;
        }
    }
    
    CGFloat padding = 5;
    int numPerRow = 3;
    if (users.count <= 4) {
        numPerRow = 2;
    }
    int row = (int)(users.count - 1) / numPerRow + 1;
    int column = numPerRow;
    int firstCol = (int)(users.count - (row - 1)*column);
    CGFloat width = (PortraitWidth - padding) / numPerRow - padding;
    CGFloat Y = (PortraitWidth - (row * (width + padding) + padding))/2;
    for (int i = 0; i < row; i++) {
        int c = column;
        if (i == 0) {
            c = firstCol;
        }
        CGFloat X = (PortraitWidth - (c * (width + padding) + padding))/2;
        for (int j = 0; j < c; j++) {
            UIImageView *imageView = [[UIImageView alloc] initWithFrame:CGRectMake(X + j *(width + padding) + padding, Y + i * (width + padding) + padding, width, width)];
            int index;
            if (i == 0) {
                index = j;
            } else {
                index = j + (i-1)*column + firstCol;
            }
            DMCCUserInfo *user = [users objectAtIndex:index];
            [imageView sd_setImageWithURL:[NSURL URLWithString:user.portrait] placeholderImage:SDImageDefault];
            [combineHeadView addSubview:imageView];
        }
    }
    UIGraphicsBeginImageContextWithOptions(combineHeadView.frame.size, NO, 2.0);
    [combineHeadView.layer renderInContext:UIGraphicsGetCurrentContext()];
    UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return image;
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return self.titleArray.count;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    NSArray *array = self.titleArray[section];
    return array.count;
}

- (BOOL)isGroupOwner {
    DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:self.conversation.target refresh:NO];
    return [group.owner isEqualToString:[DMCCNetworkService sharedInstance].userId];
}

- (BOOL)isGroupManger {
    DMCCGroupMember *gm = [[DMCCIMService sharedDMCIMService] getGroupMember:self.conversation.target memberId:[DMCCNetworkService sharedInstance].userId];
    return gm.type == Member_Type_Manager;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ZoloGroupDetailCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloGroupDetailCell class])];
    NSArray *array = self.titleArray[indexPath.section];
    cell.nameLabel.text = array[indexPath.row];
    DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:self.conversation.target refresh:NO];
    cell.nameLabel.textColor = [FontManage MHBlockColor];
    WS(ws);
    switch (indexPath.section) {
        case 0:
        {
            if (indexPath.row == 0) {
                cell.remarkLabel.text = group.name;
                cell.remarkLabel.hidden = NO;
            } else {
                cell.remarkLabel.hidden = YES;
            }
            cell.switchBtn.hidden = YES;
            cell.arrowImg.hidden = NO;
        }
            break;
        case 1:
        {
            cell.arrowImg.hidden = NO;
            cell.remarkLabel.hidden = YES;
            cell.switchBtn.hidden = YES;
        }
            break;
        case 2:
        {
            cell.remarkLabel.hidden = YES;
            cell.switchBtn.hidden = NO;
            cell.arrowImg.hidden = YES;
            if (indexPath.row == 0) {
                // 消息免打扰
                [cell.switchBtn setOn:self.conversationInfo.isSilent];
                cell.topSwitchBlock = ^(BOOL isTop) {
                    [ws silentConversation:isTop];
                };
            }
            if (indexPath.row == 1) {
                // 升级红包群
                DMCCGroupInfo *groupInfo = [[DMCCIMService sharedDMCIMService] getGroupInfo:_conversation.target refresh:false];
                if(groupInfo != nil){
                    [cell.switchBtn setOn:groupInfo.redPacket];
                }
                cell.topSwitchBlock = ^(BOOL isTop) {
                    [MHAlert showMessage:LocalizedString(@"RedPackUpText")];
                    [ws updateRedPacketGroup:isTop success:nil error:^(int error_code) {
                       
                    }];
                };
            } else if (indexPath.row == 2) {
                // 置顶聊天
                [cell.switchBtn setOn:self.conversationInfo.isTop];
                cell.topSwitchBlock = ^(BOOL isTop) {
                    [ws topConversation:isTop];
                };
            } else if (indexPath.row == 3) {
                // 保存到通讯录
                [cell.switchBtn setOn:[[DMCCIMService sharedDMCIMService] isFavGroup:self.conversation.target]];
                cell.topSwitchBlock = ^(BOOL isTop) {
                    [[DMCCIMService sharedDMCIMService] setFavGroup:ws.conversation.target fav:isTop success:^{
                        
                    } error:^(int error_code) {
                        
                    }];
                    
                    [[DMCCIMService sharedDMCIMService] saveGroup:ws.conversation.target status:isTop cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
                        
                    }];
                };
            } else if (indexPath.row == 4) {
                cell.switchBtn.hidden = YES;
                cell.arrowImg.hidden = NO;
                cell.nameLabel.textColor = [FontManage MHMainColor];
                cell.nameLabel.text = LocalizedString(@"RedPackBomSetting");
            }
        }
            break;
        case 3:
        {
            cell.remarkLabel.hidden = YES;
            if (indexPath.row == 0) {
                cell.switchBtn.hidden = YES;
                cell.arrowImg.hidden = NO;
                cell.remarkLabel.hidden = NO;
                DMCCGroupMember *gm = [[DMCCIMService sharedDMCIMService] getGroupMember:self.conversation.target memberId:[DMCCIMService sharedDMCIMService].getUserID];
                if (![gm.alias isEqualToString:@"(null)"] && gm.alias.length > 0) {
                    cell.remarkLabel.text = gm.alias;
                } else {
                    cell.remarkLabel.text = @"";
                }
            } else if (indexPath.row == 1) {
                // 显示群成员昵称
                cell.switchBtn.hidden = NO;
                cell.arrowImg.hidden = YES;
                [cell.switchBtn setOn:![[DMCCIMService sharedDMCIMService] isHiddenGroupMemberName:self.conversation.target]];
                cell.topSwitchBlock = ^(BOOL isTop) {
                    [[DMCCIMService sharedDMCIMService] setHiddenGroupMemberName:!isTop group:ws.conversation.target success:^{
                        [[NSNotificationCenter defaultCenter] postNotificationName:FZ_EVENT_GROUPREFRESH_STATE object:ws.conversation];
                    } error:^(int error_code) {
                        
                    }];
                };
            } else if (indexPath.row == 2) {
                // 允许互加好友
                cell.switchBtn.hidden = NO;
                cell.arrowImg.hidden = YES;
                [cell.switchBtn setOn:[[DMCCIMService sharedDMCIMService] isAllowAddFriendGroupMemberName:self.conversation.target]];
                cell.topSwitchBlock = ^(BOOL isTop) {
                    [[DMCCIMService sharedDMCIMService] allowAddFriend:self.conversation.target data:isTop cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
                        
                    }];
                };
            } else {
                cell.switchBtn.hidden = YES;
                cell.arrowImg.hidden = NO;
            }
        }
            break;
        default:
            break;
    }
    
    return cell;
}

// 升级红包群
- (void) updateRedPacketGroup:(BOOL) mark
                      success:(void(^)(void))successBlock
                        error:(void(^)(int error_code))errorBlock{
    [[DMCCIMService sharedDMCIMService] getOwnerSign:_conversation.target cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        if(isSuccess){
            NSLog(@"getOwnerSign result: %@",[OsnUtils dic2Json:json]);
            DMCCGroupInfo *groupInfo = [[DMCCIMService sharedDMCIMService] getGroupInfo:self->_conversation.target refresh:false];
            if(groupInfo == nil)
                return;
            json[@"count"] = @(groupInfo.memberCount);
            json[@"language"] = [OSNLanguage getBaseLanguage];
            NSLog(@"SET_GROUP_URL data: %@",[OsnUtils dic2Json:json]);
            MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
            json = [HttpUtils doPosts:login.json.SET_GROUP_URL data:[OsnUtils dic2Json:json]];
            if(json == nil){
                NSLog(@"updateRedPacketGroup data == nil");
                errorBlock(-1);
            }else{
                NSLog(@"set group redPacket result: %@",[OsnUtils dic2Json:json]);
                if(((NSNumber*)json[@"code"]).intValue != 200){
                    errorBlock(-1);
                }
            }
        }else{
            NSLog(@"updateRedPacketGroup errorCode: %@",error);
            errorBlock(-1);
        }
    }];
}

- (void)topConversation:(BOOL)isTop {
    [[DMCCIMService sharedDMCIMService] setConversation:self.conversationInfo.conversation top:isTop success:^{
        [[NSNotificationCenter defaultCenter] postNotificationName:FZ_EVENT_CONVERSATIONGROUPLISTCHANG_STATE object:nil];
    } error:^(int error_code) {
    }];
}

- (void)silentConversation:(BOOL)isSilent {
    [[DMCCIMService sharedDMCIMService] setConversation:self.conversationInfo.conversation silent:isSilent success:^{
        [[NSNotificationCenter defaultCenter] postNotificationName:FZ_EVENT_CONVERSATIONLISTCHANG_STATE object:nil];
    } error:^(int error_code) {
        
    }];
}


- (void)showGroupNameALert:(NSString *)message withAlerttype:(MHAlertType)alerttype{
    if (![self isGroupOwner]) {
        [MHAlert showMessage:LocalizedString(@"AlertNoOurthSee")];
        return;
    }
    NSString *title;
    switch (alerttype) {
        case MHAlertTypeWarn:
            title = LocalizedString(@"AlertWarn");
            break;
        case MHAlertTypeRemind:
            title = LocalizedString(@"AlertRemind");
            break;
    }
    UIViewController *topVC = [UIApplication sharedApplication].keyWindow.rootViewController;
    if ((topVC.presentedViewController) != nil) {
        topVC = topVC.presentedViewController;
    }
    WS(ws);
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:title message:message preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *ok = [UIAlertAction actionWithTitle:LocalizedString(@"Sure") style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        NSString *inputText = alert.textFields[0].text;
        if (inputText.length > 20) {
            [MHAlert showMessage:LocalizedString(@"NotWords")];
            return;
        }
        DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:ws.conversation.target refresh:NO];
        [[DMCCIMService sharedDMCIMService] modifyGroupInfo:group.target type:Modify_Group_Name newValue:alert.textFields[0].text notifyLines:@[@(0)] notifyContent:nil success:^{
            [MHAlert showMessage:LocalizedString(@"AlertSuccess")];
            dispatch_async(dispatch_get_main_queue(), ^{
                [[DMCCIMService sharedDMCIMService] getGroupInfo:ws.conversation.target refresh:YES];
                dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(2 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                    [ws.tableView reloadData];
                });
            });
        } error:^(int error_code) {
            [MHAlert showMessage:LocalizedString(@"AlertSettingFail")];
        }];
    }];
    DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:self.conversation.target refresh:NO];
    UIAlertAction *cancel = [UIAlertAction actionWithTitle:LocalizedString(@"Cancel") style:UIAlertActionStyleDefault handler:nil];
    [alert addTextFieldWithConfigurationHandler:^(UITextField * _Nonnull textField) {
        textField.placeholder = LocalizedString(@"ModifyGroupName");
        textField.text = group.name;
        textField.clearButtonMode = UITextFieldViewModeAlways;
    }];
    [alert addAction:cancel];
    [alert addAction:ok];
    [topVC presentViewController:alert animated:YES completion:nil];
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    if (indexPath.section == 0 && indexPath.row == 0) {
        [self showGroupNameALert:LocalizedString(@"ModifyGroupName") withAlerttype:MHAlertTypeRemind];
    } else if (indexPath.section == 0 && indexPath.row == 1) {
        [self.navigationController pushViewController:[[ZoloMyQrCodeVC alloc] initWithGroupQr:self.conversation withIsGroup:YES IsUser:NO] animated:YES];
    } else if (indexPath.section == 0 && indexPath.row == 2) {
        DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:self.conversation.target refresh:NO];
        [self.navigationController pushViewController:[[ZoloGroupNoticeVC alloc] initWithGroupInfo:group] animated:YES];
    } else if (indexPath.section == 0 && indexPath.row == 3) {
        DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:self.conversation.target refresh:NO];
        [self.navigationController pushViewController:[[ZoloGroupManagerVC alloc] initWithGroupInfo:group withDapp:self.litapp] animated:YES];
    } else if (indexPath.section == 1 && indexPath.row == 1) {
        [self.navigationController pushViewController:[ZoloFileRecordVC new] animated:YES];
    } else if (indexPath.section == 3 && indexPath.row == 0) {
        DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:self.conversation.target refresh:NO];
        [self.navigationController pushViewController:[[ZoloNickNameVC alloc] initWithSettingRemarkName:MHNickNameType_Group WithSId:group.target] animated:YES];
    } else if (indexPath.section == 2 && indexPath.row == 4) {
        MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
        [self.navigationController pushViewController:[[ZoloRedProbabilityController alloc] initWithUrlInfo:login.json.redPack groupId:self.conversation.target isHidden:NO] animated:YES];
    } else if (indexPath.section == 1 && indexPath.row == 2) {
        MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
        [self.navigationController pushViewController:[[ZoloWalletViewController alloc] initWithUrlInfo:login.json.GROUP_PROFIT_URL isHidden:NO] animated:YES];
    } else if (indexPath.section == 1 && indexPath.row == 3) {
        DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:self.conversation.target refresh:NO];
        [self.navigationController pushViewController:[[ZoloShareCardVC alloc] initWithGroupInfo:group] animated:YES];
    } else if (indexPath.section == 1 && indexPath.row == 4) {
        DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:self.conversation.target refresh:NO];
        [self.navigationController pushViewController:[[ZoloTagOperateVC alloc] initWithTagAddInfo:group] animated:YES];
    } else if (indexPath.section == 1 && indexPath.row == 0) {
        DMCCConversationInfo *conversationInfo = [[DMCCIMService sharedDMCIMService] getConversationInfo:self.conversation];
        [self.navigationController pushViewController:[[ZoloChatSearchVC alloc] initWithConversition:conversationInfo] animated:YES];
    }
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section {
    if (section == 0) {
        ZoloGroupDetailHead *head = [tableView dequeueReusableHeaderFooterViewWithIdentifier:NSStringFromClass([ZoloGroupDetailHead class])];
        
        NSMutableArray * array = [NSMutableArray arrayWithCapacity:0];
        BOOL isShow = NO;
        if (self.memberList.count > 25) {
            
            if ([self isGroupOwner] || [self isGroupManger]) {
                array = [NSMutableArray arrayWithArray:[self.memberList subarrayWithRange:NSMakeRange(0, 23)]];
            } else {
                array = [NSMutableArray arrayWithArray:[self.memberList subarrayWithRange:NSMakeRange(0, 24)]];
            }
            
            isShow = YES;
        } else {
            array = [NSMutableArray arrayWithArray:self.memberList];
        }
        DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:self.conversation.target refresh:NO];
        [head setMemberList:array withIsGroupManager:[self isGroupOwner] || [self isGroupManger] withIsShowMore:isShow group:group];
        WS(ws);
        head.friendAddBlock = ^{
            [ws addFriend];
        };
        head.friendLessBlock = ^{
            [ws lessFriend];
        };
        head.friendSeeMoreBlock = ^{
            
            DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:self.conversation.target refresh:NO];
            [ws.navigationController pushViewController:[[ZoloMoreMemberVC alloc] initWithGroupInfo:group] animated:YES];
        };
        head.friendSeeBlock = ^(NSInteger row) {
            if (![[DMCCIMService sharedDMCIMService] isAllowAddFriendGroupMemberName:self.conversation.target]) {
                [MHAlert showMessage:LocalizedString(@"StopMemberChat")];
                return;
            }
            // 判断好友是否已经存在
            DMCCGroupMember *member = ws.memberList[row];
            DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:member.memberId inGroup:member.groupId refresh:NO];
            if ([[DMCCIMService sharedDMCIMService] isMyFriend:userInfo.userId]) {
                [self.navigationController pushViewController:[[ZoloContactDetailVC alloc] initWithUserInfo:userInfo] animated:YES];
            } else {
                ZoloAddFriendInfoVC *vc = [[ZoloAddFriendInfoVC alloc] initWithUserInfo:userInfo];
                [ws.navigationController pushViewController:vc animated:YES];
            }
        };
        return head;
    } else {
        UIView *view = [UIView new];
        view.backgroundColor = [FontManage MHLineSeparatorColor];
        return view;
    }
}

- (void)lessFriend {
    ZoloLessFriendVC *vc = [[ZoloLessFriendVC alloc] initWithMemeberArray:self.memberList groupId:self.conversation.target];
    WS(ws);
    vc.confirmBlock = ^(NSArray * _Nonnull contactArray) {
        [[DMCCIMService sharedDMCIMService] kickoffMembers:contactArray fromGroup:ws.conversation.target notifyLines:@[@(0)] notifyContent:nil success:^{
          [[DMCCIMService sharedDMCIMService] getGroupMembers:ws.conversation.target forceUpdate:YES];
        } error:^(int error_code) {
          
        }];
    };
    [self.navigationController pushViewController:vc animated:YES];
}

- (void)addFriend {
    DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:self.conversation.target refresh:NO];
    ZoloGroupAddFriendVC *vc = [[ZoloGroupAddFriendVC alloc] initWithGroupInfo:group];
    WS(ws);
    vc.confirmBlock = ^(NSArray * _Nonnull contactArray) {
        [[DMCCIMService sharedDMCIMService] addMembers:contactArray toGroup:ws.conversation.target memberExtra:nil notifyLines:@[@(0)] notifyContent:nil success:^{
            dispatch_async(dispatch_get_main_queue(), ^{
                ws.memberList = [[DMCCIMService sharedDMCIMService] getGroupMembers:ws.conversation.target forceUpdate:YES];
                [ws.tableView reloadData];
                
                for (NSString *user in contactArray) {
                    DMCCConversation *conversation1 = [DMCCConversation new];
                    conversation1.target = user;
 
                    DMCCCardMessageContent *content = [DMCCCardMessageContent cardWithTarget:group.target type:CardType_Group from:user];
                  
                    [[DMCCIMService sharedDMCIMService] send:conversation1 content:content pwd:nil success:^(long long messageUid, long long timestamp) {
                        
                    } error:^(int error_code) {
                        
                    }];
                }
            });
        } error:^(int error_code) {
        }];
    };
    [self.navigationController pushViewController:vc animated:YES];
}

- (UIView *)tableView:(UITableView *)tableView viewForFooterInSection:(NSInteger)section {
    ZoloGroupDetailFoot *foot = [tableView dequeueReusableHeaderFooterViewWithIdentifier:NSStringFromClass([ZoloGroupDetailFoot class])];
    if ([self isGroupOwner]) {
        [foot.deleteBtn setTitle:LocalizedString(@"ContactGroupDissDelete") forState:UIControlStateNormal];
    } else {
        [foot.deleteBtn setTitle:LocalizedString(@"ContactGroupQuite") forState:UIControlStateNormal];
    }
    [foot.jubaoBtn setTitle:LocalizedString(@"ComplaintTitle") forState:UIControlStateNormal];
    [foot.clearChatBtn setTitle:LocalizedString(@"ContactClearChat") forState:UIControlStateNormal];
    WS(ws);
    
    foot.complaintChatBlock = ^{
        [ws.navigationController pushViewController:[[ZoloComplaintVC alloc] initWithIsGroup:YES] animated:YES];
    };
    
    foot.clearChatBlock = ^{
        NSString *str = LocalizedString(@"AlertClear");
        [MHAlert showCustomeAlert:str withAlerttype:MHAlertTypeWarn withOkBlock:^(UIAlertAction *action) {
            if ([action.title isEqualToString:LocalizedString(@"Sure")]) {
                [[DMCCIMService sharedDMCIMService] clearMessages:ws.conversation];
                [[NSNotificationCenter defaultCenter] postNotificationName:FZ_EVENT_MESSAGELISTCHANG_STATE object:ws.conversation];
            }
        }];
    };
    foot.deleteChatBlock = ^{
        
        if ([ws isGroupOwner]) {
            NSString *str = LocalizedString(@"AlertSureDisDel");
            [MHAlert showCustomeAlert:str withAlerttype:MHAlertTypeWarn withOkBlock:^(UIAlertAction *action) {
                if ([action.title isEqualToString:LocalizedString(@"Sure")]) {
                    [[DMCCIMService sharedDMCIMService] dismissGroup:ws.conversation.target notifyLines:@[@(0)] notifyContent:nil success:^{
                        dispatch_async(dispatch_get_main_queue(), ^{
                            [ws delTagGroup];
                            [[DMCCIMService sharedDMCIMService] removeConversation:ws.conversation clearMessage:YES];
                            [[DMCCIMService sharedDMCIMService] delGroup:ws.conversation.target];
                            [[NSNotificationCenter defaultCenter] postNotificationName:FZ_EVENT_CONVERSATIONGROUPLISTCHANG_STATE object:nil];
                            [ws.navigationController popToRootViewControllerAnimated:YES];
                        });
                    } error:^(int error_code) {
                        
                    }];
                }
            }];
        } else {
            NSString *str = LocalizedString(@"AlertSureQuitGroup");
            [MHAlert showCustomeAlert:str withAlerttype:MHAlertTypeWarn withOkBlock:^(UIAlertAction *action) {
                if ([action.title isEqualToString:LocalizedString(@"Sure")]) {
                    [[DMCCIMService sharedDMCIMService] quitGroup:ws.conversation.target notifyLines:@[@(0)] notifyContent:nil success:^{
                        dispatch_async(dispatch_get_main_queue(), ^{
                            [ws delTagGroup];
                            [[DMCCIMService sharedDMCIMService] removeConversation:ws.conversation clearMessage:YES];
                            [[DMCCIMService sharedDMCIMService] delGroup:ws.conversation.target];
                            [[NSNotificationCenter defaultCenter] postNotificationName:FZ_EVENT_CONVERSATIONGROUPLISTCHANG_STATE object:nil];
                            [ws.navigationController popToRootViewControllerAnimated:YES];
                        });
                    } error:^(int error_code) {
                        
                    }];
                }
            }];
        }
     
    };
    return foot;
}

- (void)delTagGroup {
    
    DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:self.conversation.target refresh:NO];
    if (group.ugID == -200) {
        return;
    }
    NSMutableArray *array = [[NSMutableArray alloc] initWithCapacity:0];
    NSMutableArray *gArray = [[NSMutableArray alloc] initWithCapacity:0];
    
    [array addObject:@(group.ugID)];
    [gArray addObject:group.target];
    
    [[ZoloAPIManager instanceManager] delTagUserList:array WithCompleteBlock:^(BOOL isSuccess) {
        if (isSuccess) {
            [[DMCCIMService sharedDMCIMService] updateGroupWithGroups:gArray withTagWithkey:@[@"tagID"]];
        }
    }];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    if (section == 0) {
        if (self.memberList.count >= 25) {
            int addnum = 0;
            if ([self isGroupOwner]) {
                addnum = 2;
            } else {
                addnum = 1;
            }
            
            float num = (23 + addnum) / 5;
            float numO = (23 + addnum) % 5;
            if (numO != 0) {
                num += 1;
            }
            return 80 * num + 20;
        } else {
            int addnum = 0;
            if ([self isGroupOwner]) {
                addnum = 2;
            } else {
                addnum = 1;
            }
            
            float num = (self.memberList.count + addnum) / 5;
            float numO = (self.memberList.count + addnum) % 5;
            if (numO != 0) {
                num += 1;
            }
            return 80 * num + 20;
        }
    }
    return 12;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    if (section == 3) {
        return 180;
    }
    return 0.001;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    if (indexPath.section == 1 && indexPath.row == 2) {
        MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
        BOOL isShow = [[NSUserDefaults standardUserDefaults] boolForKey:@"isShowHidden"];
        if (login.json.HIDE_ENABLE.intValue || isShow) {
            if ([self isGroupOwner]) {
                return self.scaleHeight(54);
            } else {
                return 0.001;
            }
        } else {
            return 0.001;
        }
    } else if (indexPath.section == 2 && indexPath.row == 1) {
        MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
        BOOL isShow = [[NSUserDefaults standardUserDefaults] boolForKey:@"isShowHidden"];
        if (login.json.HIDE_ENABLE.intValue || isShow) {
            if ([self isGroupOwner]) {
                return self.scaleHeight(54);
            } else {
                return 0.001;
            }
        } else {
            return 0.001;
        }
    } else if (indexPath.section == 0 && indexPath.row == 3) {
        if ([self isGroupOwner] || [self isGroupManger]) {
            return self.scaleHeight(54);
        } else {
            return 0.001;
        }
    } else if (indexPath.section == 2 && indexPath.row == 4) {
        if ([self isGroupOwner] && self.isShowBom) {
            return self.scaleHeight(54);
        } else {
            return 0.001;
        }
    } else if (indexPath.section == 3 && indexPath.row == 2) {
        if ([self isGroupOwner]) {
            return self.scaleHeight(54);
        } else {
            return 0.001;
        }
    }  else if (indexPath.section == 0 && indexPath.row == 2) {
        if ([self isGroupOwner] || [self isGroupManger]) {
            return self.scaleHeight(54);
        } else {
            return 0.001;
        }
    }
//    else if (indexPath.section == 1 && indexPath.row == 0) {
//        return 0.001;
//    }
    else if (indexPath.section == 1 && indexPath.row == 1) {
        return 0.001;
    }
//    else if (indexPath.section == 2 && indexPath.row == 0) {
//        return 0.001;
//    } else if (indexPath.section == 3 && indexPath.row == 0) {
//        return 0.001;
//    }
    
    else if (indexPath.section == 0 && indexPath.row == 1) {
        DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:self.conversation.target refresh:NO];
        if (group.joinType == 2) {
            if ([self isGroupOwner] || [self isGroupManger]) {
                return self.scaleHeight(54);
            } else {
                return 0.001;
            }
        } else {
            return self.scaleHeight(54);
        }
    } else if (indexPath.section == 1 && indexPath.row == 3) {
        DMCCGroupInfo *group = [[DMCCIMService sharedDMCIMService] getGroupInfo:self.conversation.target refresh:NO];
        if (group.joinType == 2) {
            if ([self isGroupOwner] || [self isGroupManger]) {
                return self.scaleHeight(54);
            } else {
                return 0.001;
            }
        } else {
            return self.scaleHeight(54);
        }
    }
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
