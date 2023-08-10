//
//  MHAlert.h
//  JTalking
//
//  Created by JTalking on 2022/6/24.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
NS_ASSUME_NONNULL_BEGIN

typedef void(^okBlock)(UIAlertAction* action);

@interface MHAlert : NSObject

+ (void)showNormalAlert:(NSString *)message withAlerttype:(MHAlertType)alerttype withOkBlock:(okBlock)okBlock;
+ (void)showCustomeAlert:(NSString *)message withAlerttype:(MHAlertType)alerttype withOkBlock:(okBlock)okBlock;
+ (void)showMessage:(NSString *)message;
+ (void)showError:(NSString *)str;
+ (void)showSuccess:(NSString *)str;
+ (void)showLoadingStr:(NSString *)str;
+ (void)showCodeMessage:(long)msgCode;
+ (void)dismiss;
+ (void)showLoadingDelayStr:(NSString *)str;

@end

NS_ASSUME_NONNULL_END
