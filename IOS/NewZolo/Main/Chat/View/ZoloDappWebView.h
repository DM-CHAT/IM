//
//  ZoloDappWebView.h
//  NewZolo
//
//  Created by JTalking on 2022/8/30.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^FullBtnBlock)(void);
typedef void(^CloseBtnBlock)(void);

@interface ZoloDappWebView : UIView

@property (nonatomic, copy) FullBtnBlock fullBtnBlock;
@property (nonatomic, copy) CloseBtnBlock closeBtnBlock;

- (instancetype)initWithLitappInfo:(DMCCLitappInfo *)appInfo;
    
@end

NS_ASSUME_NONNULL_END
