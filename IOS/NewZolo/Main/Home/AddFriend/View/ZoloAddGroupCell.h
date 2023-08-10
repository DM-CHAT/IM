//
//  ZoloAddGroupCell.h
//  NewZolo
//
//  Created by MHHY on 2023/6/8.
//

#import <UIKit/UIKit.h>

typedef void(^AddGroupBlock)(NSString *str);

NS_ASSUME_NONNULL_BEGIN

@interface ZoloAddGroupCell : UITableViewCell

@property (nonatomic, copy) AddGroupBlock addGroupBlock;

@end

NS_ASSUME_NONNULL_END
