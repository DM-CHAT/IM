//
//  ZoloPersonVC.m
//  NewZolo
//
//  Created by JTalking on 2022/7/6.
//

#import "ZoloPersonVC.h"
#import "ZoloPersonHeadView.h"
#import "ZoloPersonCell.h"
#import "ZoloNickNameVC.h"
#import "ZoloMyQrCodeVC.h"
#import "TZImagePickerController.h"

@interface ZoloPersonVC ()

@property (nonatomic, strong) NSArray *titleArray;
@property (nonatomic, strong) DMCCUserInfo *userInfo;

@end

@implementation ZoloPersonVC

- (instancetype)initWithUserInfo:(DMCCUserInfo *)userInfo {
    if (self = [super init]) {
        self.userInfo = userInfo;
    }
    return self;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self getData];
    [self.navigationController setNavigationBarHidden:NO animated:animated];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [self getData];
    self.titleArray = @[LocalizedString(@"ContactAlterUserName"), LocalizedString(@"ContactQrCode")];
    [self registerCellWithNibName:NSStringFromClass([ZoloPersonCell class]) isTableview:YES];
    [self registerHeaderFooterWithNibName:NSStringFromClass([ZoloPersonHeadView class])];
    self.tableView.backgroundColor = [FontManage MHGrayColor];
    self.tableView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
}

- (void)getData {
    WS(ws);
    [[DMCCIMService sharedDMCIMService] getUserInfo:self.userInfo.userId refresh:YES success:^(DMCCUserInfo *userInfo) {
        if (userInfo) {
            ws.userInfo = userInfo;
            dispatch_async(dispatch_get_main_queue(), ^{
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
    return self.titleArray.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ZoloPersonCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloPersonCell class])];
    cell.remarkLabel.text = self.titleArray[indexPath.row];
    cell.nickNameLabal.hidden = YES;
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    if (indexPath.row == 0) {
        [self.navigationController pushViewController:[[ZoloNickNameVC alloc] initWithSettingRemarkName:MHNickNameType_My WithSId:self.userInfo.userId] animated:YES];
    } else if (indexPath.row == 1) {
        [self.navigationController pushViewController:[ZoloMyQrCodeVC new] animated:YES];
    }
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section {
    ZoloPersonHeadView *headView = [tableView dequeueReusableHeaderFooterViewWithIdentifier:NSStringFromClass([ZoloPersonHeadView class])];
    [headView.icon sd_setImageWithURL:[NSURL URLWithString:self.userInfo.portrait] placeholderImage:SDImageDefault];
    headView.nickName.text = self.userInfo.displayName;
    headView.remark.text = [NSString stringWithFormat:@"ID:%@", self.userInfo.userId];
    WS(ws);
    headView.longGesClickBlock = ^{
        [ws longPressClick];
    };
    headView.personEditBlock = ^{
        [ws editClickBlock];
    };
    return headView;
}

- (void)longPressClick {
    [[UIPasteboard generalPasteboard] setString:self.userInfo.userId];
    [MHAlert showMessage:LocalizedString(@"AlertCopySucess")];
}

- (void)editClickBlock {
    TZImagePickerController *imagePickerVc = [[TZImagePickerController alloc] initWithMaxImagesCount:1 delegate:nil];
    imagePickerVc.allowCrop = YES;
    WS(ws);
    [imagePickerVc setDidFinishPickingPhotosHandle:^(NSArray<UIImage *> *photos, NSArray *assets, BOOL isSelectOriginalPhoto) {
        [ws uploadImage:photos.firstObject];
    }];
    [self presentViewController:imagePickerVc animated:YES completion:nil];
}

- (void)uploadImage:(UIImage *)originImage {
    UIImage *captureImage = [MHHelperUtils thumbnailWithImage:originImage maxSize:CGSizeMake(600, 600)];
    NSData *data = UIImageJPEGRepresentation(captureImage, 1);
    WS(ws);
    [[DMCCIMService sharedDMCIMService] uploadMedia:nil mediaData:data mediaType:Media_Type_PORTRAIT success:^(NSString *remoteUrl) {
        [[DMCCIMService sharedDMCIMService] modifyMyInfo:@{@(Modify_Portrait) : remoteUrl} success:^{
            [[DMCCIMService sharedDMCIMService] getUserInfo:[[OsnSDK getInstance] getUserID] refresh:YES success:^(DMCCUserInfo *userInfo) {
                if (userInfo) {
                    ws.userInfo = userInfo;
                    [ws removeNft];
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [ws.tableView reloadData];
                    });
                }
            } error:^(int errorCode) {
                
            }];
        } error:^(int error_code) {
            
        }];
        [[ZoloAPIManager instanceManager] uploadUserImgWithImage:remoteUrl WithCompleteBlock:^(BOOL isSuccess) {
            
        }];
    } progress:^(long uploaded, long total) {
        
    } error:^(int error_code) {
        
    }];
}

- (void)removeNft {
    WS(ws);
    [[DMCCIMService sharedDMCIMService] sendRemoveNFTDescribesWithcb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        if (isSuccess) {
            [ws getData];
        }
    }];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 150;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 160;
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
