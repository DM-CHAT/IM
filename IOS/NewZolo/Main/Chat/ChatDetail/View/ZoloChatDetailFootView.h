//
//  ZoloChatDetailFootView.h
//  NewZolo
//
//  Created by JTalking on 2022/8/15.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^DeleteChatBlock)(void);

@interface ZoloChatDetailFootView : UITableViewHeaderFooterView

@property (weak, nonatomic) IBOutlet UIButton *deleteBtn;

@property (nonatomic, copy) DeleteChatBlock deleteChatBlock;

@end

NS_ASSUME_NONNULL_END
