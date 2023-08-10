//
//  ZoloNoticeHeadView.h
//  NewZolo
//
//  Created by JTalking on 2022/9/22.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^NoticeViewBlock)(void);

@interface ZoloNoticeHeadView : UIView

@property (nonatomic, copy) NoticeViewBlock noticeViewBlock;

@property (weak, nonatomic) IBOutlet UILabel *noticeLab;

@end

NS_ASSUME_NONNULL_END
