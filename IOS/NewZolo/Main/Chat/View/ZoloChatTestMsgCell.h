//
//  ZoloChatTestMsgCell.h
//  NewZolo
//
//  Created by JTalking on 2022/7/1.
//

#import "ZoloBaseChatCell.h"
@class AttributedLabel;
NS_ASSUME_NONNULL_BEGIN

@interface ZoloChatTestMsgCell : ZoloBaseChatCell

@property (weak, nonatomic) IBOutlet UIImageView *icon;
@property (weak, nonatomic) IBOutlet AttributedLabel *msg;

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *leftMargin;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *rightMargin;

@property (weak, nonatomic) IBOutlet UIImageView *nftImg;

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *iconLeftMargin;

@end

NS_ASSUME_NONNULL_END
