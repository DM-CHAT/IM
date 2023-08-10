//
//  ZoloContactDetailHead.h
//  NewZolo
//
//  Created by JTalking on 2022/7/6.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^SendMessageBlock)(void);
typedef void(^DeleteUserBlock)(void);

@interface ZoloContactDetailFoot : UITableViewHeaderFooterView

@property (weak, nonatomic) IBOutlet UIButton *sendMsgBtn;
@property (weak, nonatomic) IBOutlet UIButton *deleteUserBtn;


@property (nonatomic, copy) SendMessageBlock sendMessageBlock;
@property (nonatomic, copy) DeleteUserBlock deleteUserBlock;

@end

NS_ASSUME_NONNULL_END
