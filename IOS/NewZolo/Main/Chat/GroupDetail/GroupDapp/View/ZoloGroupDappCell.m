//
//  ZoloGroupDappCell.m
//  NewZolo
//
//  Created by JTalking on 2022/8/30.
//

#import "ZoloGroupDappCell.h"

@implementation ZoloGroupDappCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
}

- (void)setDappModel:(ZoloDappModel *)dappModel {
    _dappModel = dappModel;
    NSData *dataString = [dappModel.dapp_url_front dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *dic = [NSJSONSerialization JSONObjectWithData:dataString options:NSJSONReadingMutableContainers error:nil];
    [self.iconImg sd_setImageWithURL:[NSURL URLWithString:dic[@"portrait"]] placeholderImage:SDImageDefault];
    self.titleLab.text = dic[@"name"];
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end
