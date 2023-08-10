//
//  ZoloKeywordCell.h
//  NewZolo
//
//  Created by JTalking on 2022/10/29.
//

#import <UIKit/UIKit.h>

typedef void(^DelBtnBlock)(void);

NS_ASSUME_NONNULL_BEGIN

@interface ZoloKeywordCell : UITableViewCell

@property (weak, nonatomic) IBOutlet UILabel *nameLab;
@property (weak, nonatomic) IBOutlet UILabel *idLab;
@property (weak, nonatomic) IBOutlet UILabel *titleLab;
@property (nonatomic, copy) DelBtnBlock delBtnBlock;

@end

NS_ASSUME_NONNULL_END
