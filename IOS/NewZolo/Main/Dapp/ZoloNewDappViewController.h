//
//  ViewController.h
//  Calendar
//
//  Created by Calendar on 2022/7/14.
//

#import <UIKit/UIKit.h>

typedef void(^PayResultBlock)(NSString *result);

NS_ASSUME_NONNULL_BEGIN

@interface ZoloNewDappViewController : UIViewController

@property (nonatomic, copy) PayResultBlock payResultBlock;

- (instancetype)initWithLitappInfo:(DMCCLitappInfo *)appInfo;


@end

NS_ASSUME_NONNULL_END
