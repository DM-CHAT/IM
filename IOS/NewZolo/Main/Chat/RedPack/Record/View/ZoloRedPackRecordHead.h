//
//  ZoloRedPackRecordHead.h
//  NewZolo
//
//  Created by JTalking on 2022/9/9.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^BackBlock)(void);
typedef void(^WalletBtnBlock)(void);

@interface ZoloRedPackRecordHead : UITableViewHeaderFooterView

@property (weak, nonatomic) IBOutlet UIView *redView;
@property (weak, nonatomic) IBOutlet UILabel *moneyLab;
@property (weak, nonatomic) IBOutlet UIImageView *userIcon;

@property (weak, nonatomic) IBOutlet UIView *otherView;
@property (weak, nonatomic) IBOutlet UILabel *whoLab;
@property (weak, nonatomic) IBOutlet UILabel *winNum;
@property (weak, nonatomic) IBOutlet UILabel *winMoney;
@property (weak, nonatomic) IBOutlet UILabel *winB;

@property (weak, nonatomic) IBOutlet UILabel *transtagLab;
@property (weak, nonatomic) IBOutlet UILabel *redLab;
@property (weak, nonatomic) IBOutlet UILabel *redName;

@property (weak, nonatomic) IBOutlet UILabel *walletLal;
@property (weak, nonatomic) IBOutlet UIImageView *walletIcon;
@property (weak, nonatomic) IBOutlet UIButton *walletBtn;

@property (weak, nonatomic) IBOutlet UIView *grayView;

@property (weak, nonatomic) IBOutlet UILabel *remarkLab;


@property (nonatomic, copy) BackBlock backBlock;
@property (nonatomic, copy) WalletBtnBlock walletBtnBlock;


@end

NS_ASSUME_NONNULL_END
