//
//  ZoloComplaintVC.m
//  NewZolo
//
//  Created by MHHY on 2023/6/12.
//

#import "ZoloComplaintVC.h"
#import "ZoloGroupDetailCell.h"

@interface ZoloComplaintVC ()

@property (nonatomic, strong) NSArray *titleArray;
@property (nonatomic, assign) BOOL isGroup;

@end

@implementation ZoloComplaintVC

- (instancetype)initWithIsGroup:(BOOL)isGroup {
    if (self = [super init]) {
        self.isGroup = isGroup;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    self.navigationItem.title = LocalizedString(@"ComplaintTitle");
    
    [self registerCellWithNibName:NSStringFromClass([ZoloGroupDetailCell class]) isTableview:YES];
    if (self.isGroup) {
        self.titleArray = @[LocalizedString(@"ComplaintTitleG1"),LocalizedString(@"ComplaintTitleG2"),LocalizedString(@"ComplaintTitleG3"),LocalizedString(@"ComplaintTitleG4"),LocalizedString(@"ComplaintTitleG5"),LocalizedString(@"ComplaintTitleG6")];
    } else {
        self.titleArray = @[LocalizedString(@"ComplaintTitle1"),LocalizedString(@"ComplaintTitle2"),LocalizedString(@"ComplaintTitle3"),LocalizedString(@"ComplaintTitle4"),LocalizedString(@"ComplaintTitle5"),LocalizedString(@"ComplaintTitle6"),LocalizedString(@"ComplaintTitle7")];
    }
    
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.titleArray.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ZoloGroupDetailCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([ZoloGroupDetailCell class])];
    cell.switchBtn.hidden = YES;
    cell.arrowImg.hidden = NO;
    cell.nameLabel.text = self.titleArray[indexPath.row];
 
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    [MHAlert showLoadingStr:LocalizedString(@"AlertNowOpeating")];
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(2 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [MHAlert showLoadingStr:LocalizedString(@"ComplaintSucess")];
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(2 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            [self.navigationController popViewControllerAnimated:YES];
        });
    });
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 0.001;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 8;
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
