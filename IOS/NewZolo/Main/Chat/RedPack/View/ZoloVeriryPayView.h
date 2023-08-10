//
//  ZoloVeriryPayView.h
//  NewZolo
//
//  Created by JTalking on 2022/9/8.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^VeriryPayBlock)(NSString *code);

@interface ZoloVeriryPayView : UIView

@property (nonatomic, copy) VeriryPayBlock veriryPayBlock;
@property (weak, nonatomic) IBOutlet UILabel *moneyLab;

@end

NS_ASSUME_NONNULL_END
