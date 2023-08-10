//
//  ZoloAddFriendCell.h
//  NewZolo
//
//  Created by JTalking on 2022/8/24.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
typedef void(^TitleTextFieldBlock)(NSString *str);

@interface ZoloAddFriendCell : UITableViewCell

@property (weak, nonatomic) IBOutlet UITextField *textField;
@property (nonatomic, copy) TitleTextFieldBlock textFieldBlock;

@end

NS_ASSUME_NONNULL_END
