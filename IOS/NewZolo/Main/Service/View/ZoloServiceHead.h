//
//  ZoloServiceHead.h
//  NewZolo
//
//  Created by JTalking on 2022/10/7.
//

#import <UIKit/UIKit.h>

typedef void(^SearchBlock)(NSString *searchStr);

NS_ASSUME_NONNULL_BEGIN

@interface ZoloServiceHead : UICollectionReusableView

@property (nonatomic, copy) SearchBlock searchBlock;

@end

NS_ASSUME_NONNULL_END
