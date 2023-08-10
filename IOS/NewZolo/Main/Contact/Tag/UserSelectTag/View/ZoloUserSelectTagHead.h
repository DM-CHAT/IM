//
//  ZoloUserSelectTagHead.h
//  NewZolo
//
//  Created by JTalking on 2022/10/28.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void (^SelectedBlock)();

@interface ZoloUserSelectTagHead : UIView

@property (nonatomic, copy) SelectedBlock selectedBlock;

@end

NS_ASSUME_NONNULL_END
