//
//  ZoloSearchView.h
//  NewZolo
//
//  Created by JTalking on 2022/10/25.
//

#import <UIKit/UIKit.h>

typedef void(^SearchBlock)(void);

NS_ASSUME_NONNULL_BEGIN

@interface ZoloSearchView : UIView

@property (nonatomic, copy) SearchBlock searchBlock;

@end

NS_ASSUME_NONNULL_END
