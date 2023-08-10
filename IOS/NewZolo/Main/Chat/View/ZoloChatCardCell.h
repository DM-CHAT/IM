//
//  ZoloChatCardCell.h
//  NewZolo
//
//  Created by JTalking on 2022/7/12.
//

#import "ZoloBaseChatCell.h"

NS_ASSUME_NONNULL_BEGIN

@interface ZoloChatCardCell : ZoloBaseChatCell

@property (weak, nonatomic) IBOutlet UIImageView *iconImg;
@property (weak, nonatomic) IBOutlet UIImageView *cardIcon;
@property (weak, nonatomic) IBOutlet UILabel *cardName;
@property (weak, nonatomic) IBOutlet UILabel *remark;
@property (weak, nonatomic) IBOutlet UIView *cardView;
@property (weak, nonatomic) IBOutlet UILabel *displayName;


@property (weak, nonatomic) IBOutlet NSLayoutConstraint *leftMargin;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *rightMargin;

@property (weak, nonatomic) IBOutlet UIImageView *nftImg;
@property (weak, nonatomic) IBOutlet UIImageView *iconBgImg;

@property (weak, nonatomic) IBOutlet UIView *mulView;
@property (weak, nonatomic) IBOutlet UIButton *mulBtn;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *iconLeftMargin;

// 默认100 ， image300
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *cardViewHeight;
@property (weak, nonatomic) IBOutlet UIImageView *dappImage;

@end

NS_ASSUME_NONNULL_END
