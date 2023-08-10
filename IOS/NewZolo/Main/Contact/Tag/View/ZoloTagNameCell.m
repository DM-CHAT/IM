//
//  ZoloTagNameCell.m
//  NewZolo
//
//  Created by JTalking on 2022/10/27.
//

#import "ZoloTagNameCell.h"

@implementation ZoloTagNameCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    self.bgView.backgroundColor = [FontManage MHLineSeparatorColor];
}

- (void)setTagModel:(ZoloTagModel *)tagModel {
    _tagModel = tagModel;
    
    NSArray* arrays = [[DMCCIMService sharedDMCIMService] getUserWithTagId:tagModel.id];
    NSMutableArray *nameArray = [NSMutableArray arrayWithCapacity:0];
    for (DMCCUserInfo *userInfo in arrays) {
        [nameArray addObject:userInfo.displayName];
    }
    
    NSArray *groups = [[DMCCIMService sharedDMCIMService] getGroupWithTagId:tagModel.id];
    for (DMCCGroupInfo *groupInfo in groups) {
        [nameArray addObject:groupInfo.name];
    }
    
    if (nameArray.count > 0) {
        self.userNames.text = [nameArray componentsJoinedByString:@","];
    } else {
        self.userNames.text = LocalizedString(@"AddMemberOrGroup");
    }
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end
