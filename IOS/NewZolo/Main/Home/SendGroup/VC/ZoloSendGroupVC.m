//
//  ZoloSendGroupVC.m
//  NewZolo
//
//  Created by JTalking on 2022/7/7.
//

#import "ZoloSendGroupVC.h"
#import "ZoloSendGroupCell.h"
#import "ZoloSendGroupHead.h"
#import "ZoloSendGroupFootView.h"
#import "ZoloSingleChatVC.h"

@interface ZoloSendGroupVC ()

@property (nonatomic, strong) ZoloSendGroupHead *headView;
@property (nonatomic, strong) ZoloSendGroupFootView *footView;
@property (nonatomic, strong) NSMutableArray *createArray;

@end

@implementation ZoloSendGroupVC

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.tableView.contentInset = UIEdgeInsetsMake(0, 0, 70, 0);
    self.tableView.tableHeaderView = self.headView;
    [self registerCellWithNibName:NSStringFromClass([ZoloSendGroupCell class]) isTableview:YES];
    [self getData];
}

- (ZoloSendGroupHead *)headView {
    if (!_headView) {
        _headView = [[ZoloSendGroupHead alloc] initWithFrame:CGRectZero];
    }
    return _headView;
}

- (ZoloSendGroupFootView *)footView {
    if (!_footView) {
        _footView =[[ZoloSendGroupFootView alloc] initWithFrame:CGRectZero];
        WS(ws);
        _footView.finishBlock = ^{
            [ws createGroup];
        };
        [self.view addSubview:_footView];
    }
    return _footView;
}

- (void)createGroup {
    [MHAlert showLoadingStr:LocalizedString(@"AlertOpreating")];
    NSMutableArray *data = [NSMutableArray arrayWithCapacity:0];
    for (DMCCUserInfo *info in self.createArray) {
        [data addObject:info.userId];
    }
    [self createGroup:data];
}

- (void)createGroup:(NSArray<NSString *> *)contacts {
    NSMutableArray<NSString *> *memberIds = [contacts mutableCopy];
    if (![memberIds containsObject:[DMCCNetworkService sharedInstance].userId]) {
        [memberIds insertObject:[DMCCNetworkService sharedInstance].userId atIndex:0];
    }

    __block NSString *name;
    DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:[memberIds objectAtIndex:0]  refresh:NO];
    name = userInfo.displayName;
    
    for (int i = 1; i < MIN(8, memberIds.count); i++) {
        userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:[memberIds objectAtIndex:i]  refresh:NO];
        if (userInfo.displayName.length > 0) {
            if (name.length + userInfo.displayName.length + 1 > 16) {
                name = [name stringByAppendingString:[NSString stringWithFormat:@"%@%@", LocalizedString(@"de"),LocalizedString(@"group")]];
                break;
            }
            name = [name stringByAppendingFormat:@",%@", userInfo.displayName];
        }
    }
    if (name.length == 0) {
        name = DMCString(@"GroupChat");
    }
    UIImage *portraitImage = [self loadCombineView:memberIds];
    NSData *portraitData = UIImageJPEGRepresentation(portraitImage,1);
    WS(ws);
    [[DMCCIMService sharedDMCIMService] uploadMedia:name mediaData:portraitData mediaType:Media_Type_PORTRAIT success:^(NSString *remoteUrl) {
        NSLog(@"uploadMedia url: %@", remoteUrl);
            dispatch_async(dispatch_get_main_queue(), ^{
                MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
                if (login.json.create_group_url.length > 0) {
                    [[ZoloAPIManager instanceManager] createGroupWithMemberList:memberIds portrait:remoteUrl type:GroupType_Normal name:name withCompleteBlock:^(NSString * _Nonnull data) {
                        if (data) {
                            NSLog(@"create group success");
                            [MHAlert dismiss];
                            if (ws.sendGroupBlock) {
                                ws.sendGroupBlock(data);
                                [[DMCCIMService sharedDMCIMService] getGroupInfo:data refresh:YES];
                            }
                            [[ZoloAPIManager instanceManager] uploadImgWithGroupId:data image:remoteUrl WithCompleteBlock:^(BOOL isSuccess) {
                            
                            }];
                            [ws dismissViewControllerAnimated:YES completion:nil];
                        }
                    }];
                } else {
                    [[DMCCIMService sharedDMCIMService] createGroup:nil name:name portrait:remoteUrl type:GroupType_Normal groupExtra:nil members:memberIds memberExtra:nil notifyLines:@[@(0)] notifyContent:nil success:^(NSString *groupId) {
                              NSLog(@"create group success");
                          [MHAlert dismiss];
                          if (ws.sendGroupBlock) {
                              ws.sendGroupBlock(groupId);
                              [[DMCCIMService sharedDMCIMService] getGroupInfo:groupId refresh:YES];
                          }
                          [[ZoloAPIManager instanceManager] uploadImgWithGroupId:groupId image:remoteUrl WithCompleteBlock:^(BOOL isSuccess) {
                              
                          }];
                          [ws dismissViewControllerAnimated:YES completion:nil];
                          
                      } error:^(int error_code) {
                          NSLog(@"create group failure");
                          [ws dismissViewControllerAnimated:YES completion:nil];
                      }];
                }
            });
        }
       progress:^(long uploaded, long total) {
        }
          error:^(int error_code) {
        dispatch_async(dispatch_get_main_queue(), ^{
         
        });
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

- (void)getData {
    [self.dataSource removeAllObjects];
    NSMutableArray *friendArray = [NSMutableArray arrayWithArray:[[DMCCIMService sharedDMCIMService] getMyFriendList:YES]];
    for (NSString *string in friendArray) {
        [self getUserInfo:string];
    }
}

- (void)getUserInfo:(NSString *)friendId {
    WS(ws);
    [[DMCCIMService sharedDMCIMService] getUserInfo:friendId refresh:NO success:^(DMCCUserInfo *userInfo) {
        if (userInfo) {
            [ws.dataSource addObject:userInfo];
            dispatch_async(dispatch_get_main_queue(), ^{
                [ws.tableView.mj_header endRefreshing];
                [ws.tableView reloadData];
            });
        }
    } error:^(int errorCode) {
        
    }];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.dataSource.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ZoloSendGroupCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloSendGroupCell class])];
    cell.userInfo = self.dataSource[indexPath.row];
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    DMCCUserInfo *info = self.dataSource[indexPath.row];
    info.isSelect = !info.isSelect;
    [self.tableView reloadData];
    [self refreshFootView];
}

- (void)refreshFootView {
    NSMutableArray *data = [NSMutableArray arrayWithCapacity:0];
    for (DMCCUserInfo *info in self.dataSource) {
        if (info.isSelect) {
            [data addObject:info];
        }
    }
    self.createArray = [NSMutableArray arrayWithArray:data];
    [self.footView setData:data];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 0.001;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 0.001;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return self.scaleHeight(80);
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
