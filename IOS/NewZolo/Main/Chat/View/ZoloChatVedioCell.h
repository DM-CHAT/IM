//
//  ZoloChatVedioCell.h
//  NewZolo
//
//  Created by JTalking on 2022/7/12.
//

#import "ZoloBaseChatCell.h"

NS_ASSUME_NONNULL_BEGIN

@interface ZoloChatVedioCell : ZoloBaseChatCell


@property (weak, nonatomic) IBOutlet UIImageView *iconImg;
@property (weak, nonatomic) IBOutlet UIImageView *thumBg;
@property (weak, nonatomic) IBOutlet UIView *vedioView;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *leftMargin;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *rightMargin;

@property (weak, nonatomic) IBOutlet UIImageView *nftImg;
@property (weak, nonatomic) IBOutlet UIImageView *iconBgImg;

@property (weak, nonatomic) IBOutlet UIView *mulView;
@property (weak, nonatomic) IBOutlet UIButton *mulBtn;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *iconLeftMargin;

@end

NS_ASSUME_NONNULL_END
