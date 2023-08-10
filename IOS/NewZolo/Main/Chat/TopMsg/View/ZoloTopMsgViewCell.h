//
//  ZoloKeywordCell.h
//  NewZolo
//
//  Created by JTalking on 2022/10/29.
//

#import <UIKit/UIKit.h>

typedef void(^DelBtnBlock)(void);

NS_ASSUME_NONNULL_BEGIN

@interface ZoloTopMsgViewCell : UITableViewCell

@property (weak, nonatomic) IBOutlet UILabel *nameLab;
@property (weak, nonatomic) IBOutlet UILabel *idLab;
@property (weak, nonatomic) IBOutlet UILabel *titleLab;
@property (nonatomic, copy) DelBtnBlock delBtnBlock;
@property (weak, nonatomic) IBOutlet UILabel *timeLab;

@end

NS_ASSUME_NONNULL_END
