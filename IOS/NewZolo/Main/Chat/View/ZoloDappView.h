//
//  ZoloDappView.h
//  NewZolo
//
//  Created by JTalking on 2022/8/30.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^DappBlock)(void);

@interface ZoloDappView : UIView

@property (nonatomic, copy) DappBlock dappBlock;

- (instancetype)initWithLitappInfo:(DMCCLitappInfo *)appInfo showNotice:(BOOL)isNotice;

@end

NS_ASSUME_NONNULL_END
