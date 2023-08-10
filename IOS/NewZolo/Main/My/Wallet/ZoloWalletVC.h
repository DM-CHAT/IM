//
//  ViewController.h
//  Calendar
//
//  Created by Calendar on 2022/7/14.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface ZoloWalletVC : UIViewController

- (instancetype)initWithLitappInfo:(DMCCLitappInfo *)appInfo withUrl:(NSString *)url withConversation:(DMCCConversation *)conversation;

@end

NS_ASSUME_NONNULL_END
