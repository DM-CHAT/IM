//
//  ZoloGroupDetailFoot.h
//  NewZolo
//
//  Created by JTalking on 2022/7/11.
//

#import <UIKit/UIKit.h>

typedef void(^ClearChatBlock)(void);
typedef void(^DeleteChatBlock)(void);
typedef void(^ComplaintChatBlock)(void);

NS_ASSUME_NONNULL_BEGIN

@interface ZoloGroupDetailFoot : UITableViewHeaderFooterView

@property (weak, nonatomic) IBOutlet UIButton *jubaoBtn;
@property (weak, nonatomic) IBOutlet UIButton *clearChatBtn;
@property (weak, nonatomic) IBOutlet UIButton *deleteBtn;


@property (nonatomic, copy) ComplaintChatBlock complaintChatBlock;
@property (nonatomic, copy) ClearChatBlock clearChatBlock;
@property (nonatomic, copy) DeleteChatBlock deleteChatBlock;

@end

NS_ASSUME_NONNULL_END
