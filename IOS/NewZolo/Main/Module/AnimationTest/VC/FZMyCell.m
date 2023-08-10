//
//  FZMyCell.m
//  FZUBICell
//
//  Created by fzjy on 2020/3/19.
//  Copyright © 2020 梁兴炎. All rights reserved.
//

#import "FZMyCell.h"

@implementation FZMyCell

- (void)awakeFromNib {
    [super awakeFromNib];
    self.title.font = [FontManage MHFont16];
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end
