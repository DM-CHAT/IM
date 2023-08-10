//
//  ZoloReplyView.h
//  NewZolo
//
//  Created by JTalking on 2022/11/16.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^ReplyViewBlock)(void);

@interface ZoloReplyView : UIView

@property (weak, nonatomic) IBOutlet UILabel *nameLab;
@property (weak, nonatomic) IBOutlet UILabel *contentLab;
@property (nonatomic, copy) ReplyViewBlock replyViewBlock;

@end

NS_ASSUME_NONNULL_END
