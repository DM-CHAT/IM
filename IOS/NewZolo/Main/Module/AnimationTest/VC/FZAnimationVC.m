//
//  FZAnimationVC.m
//  FZUBICell
//
//  Created by fzjy on 2020/6/1.
//  Copyright © 2020 梁兴炎. All rights reserved.
//

#import "FZAnimationVC.h"
#import "FZMyCell.h"
#import "FZAnimationViewController.h"

@interface FZAnimationVC ()
@property (nonatomic, strong) NSArray *titleArray;
@end

@implementation FZAnimationVC

- (void)viewDidLoad {
    [super viewDidLoad];
    [self registerCellWithNibName:NSStringFromClass([FZMyCell class]) isTableview:YES];
    [self.tableView reloadData];
    self.tableView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
    self.navigationItem.title = @"动画测试";
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return 20;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    FZMyCell *cell = [tableView dequeueReusableCellWithIdentifier:NSStringFromClass([FZMyCell class])];
    cell.title.text = [NSString stringWithFormat:@"测试动画----%ld", indexPath.row + 1];
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    [self.navigationController pushViewController:[[FZAnimationViewController alloc] initWithPageNumber:indexPath.row + 1] animated:YES];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 8;
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section {
    UIView *view = [UIView new];
    view.backgroundColor = [FontManage MHMainColor];
    return view;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 0.001;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return self.scaleHeight(54);
}

@end
