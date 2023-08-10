//
//  ZoloChatImageMsgCell.h
//  NewZolo
//
//  Created by JTalking on 2022/7/12.
//

#import "ZoloBaseChatCell.h"

NS_ASSUME_NONNULL_BEGIN

@interface ZoloChatImageMsgCell : ZoloBaseChatCell

@property (weak, nonatomic) IBOutlet UIImageView *iconImg;
@property (weak, nonatomic) IBOutlet UIImageView *showImg;

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *leftMagin;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *imgW;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *imgH;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *meRightMargin;

@property (weak, nonatomic) IBOutlet UIImageView *nftImg;
@property (weak, nonatomic) IBOutlet UIImageView *iconBgImg;

@property (weak, nonatomic) IBOutlet UIButton *mulBtn;
@property (weak, nonatomic) IBOutlet UIView *mulView;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *iconLeftMargin;

@end

NS_ASSUME_NONNULL_END
