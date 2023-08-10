//
//  ZoloSystemNoticeCell.h
//  NewZolo
//
//  Created by MHHY on 2023/4/10.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface ZoloSystemNoticeCell : UITableViewCell

@property (weak, nonatomic) IBOutlet UIView *redRemark;
@property (weak, nonatomic) IBOutlet UILabel *noticeContent;
@property (weak, nonatomic) IBOutlet UILabel *noticeTitle;
@property (weak, nonatomic) IBOutlet UILabel *noticeTime;

@end

NS_ASSUME_NONNULL_END
