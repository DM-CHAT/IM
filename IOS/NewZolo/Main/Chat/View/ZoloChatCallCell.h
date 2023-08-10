//
//  ZoloChatVoiceCell.h
//  NewZolo
//
//  Created by JTalking on 2022/7/12.
//

#import "ZoloBaseChatCell.h"

NS_ASSUME_NONNULL_BEGIN

@interface ZoloChatCallCell : ZoloBaseChatCell

@property (weak, nonatomic) IBOutlet UIImageView *iconImg;
@property (weak, nonatomic) IBOutlet UIView *voiceView;

@property (weak, nonatomic) IBOutlet UIImageView *voiceImg;
@property (weak, nonatomic) IBOutlet UILabel *timeLabel;

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *leftMargin;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *rightMargin;

@property (weak, nonatomic) IBOutlet UIImageView *nftImg;

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *iconLeftMargin;

@end

NS_ASSUME_NONNULL_END
