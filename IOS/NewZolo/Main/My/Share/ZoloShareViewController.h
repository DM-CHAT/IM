//
//  ZoloShareViewController.h
//  NewZolo
//
//  Created by JTalking on 2023/2/3.
//

#import "MHParentViewController.h"

NS_ASSUME_NONNULL_BEGIN

@interface ZoloShareViewController : MHParentViewController

- (instancetype)initWithActive:(BOOL)isActive withUrl:(NSString *)url shareUrl:(NSString *)shareUrl;

@end

NS_ASSUME_NONNULL_END
