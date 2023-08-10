//
//  MHAlert.m
//  JTalking
//
//  Created by JTalking on 2022/6/24.
//

#import "MHAlert.h"
#import "MHCurrentVCTool.h""

@implementation MHAlert

/**
 弹出只有一个“确定"按钮的警告框
 - parameter message:   弹出框的信息内容
 - parameter alerttype: 弹出框的标题类型，有“警告”、“提醒”
 */
+ (void)showNormalAlert:(NSString *)message withAlerttype:(MHAlertType)alerttype withOkBlock:(okBlock)okBlock {
    NSString *title;
    switch (alerttype) {
        case MHAlertTypeWarn:
            title = LocalizedString(@"AlertWarn");
            break;
        case MHAlertTypeRemind:
            title = LocalizedString(@"AlertRemind");
            break;
    }
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:title message:message preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *ok = [UIAlertAction actionWithTitle:LocalizedString(@"Sure") style:UIAlertActionStyleDefault handler:okBlock];
    [alert addAction:ok];
    UIViewController * topVC = [MHCurrentVCTool getPresentedVC];
    [topVC presentViewController:alert animated:YES completion:nil];
}

/**
 弹出“确定""取消"按钮的警告框
 - parameter message:   弹出框的信息内容
 - parameter alerttype: 弹出框的标题类型，有“警告”、“提醒”
 */
+ (void)showCustomeAlert:(NSString *)message withAlerttype:(MHAlertType)alerttype withOkBlock:(okBlock)okBlock {
    NSString *title;
    switch (alerttype) {
        case MHAlertTypeWarn:
            title = LocalizedString(@"AlertWarn");
            break;
        case MHAlertTypeRemind:
            title = LocalizedString(@"AlertRemind");
            break;
    }
    UIViewController *topVC = [UIApplication sharedApplication].keyWindow.rootViewController;
    if ((topVC.presentedViewController) != nil) {
        topVC = topVC.presentedViewController;
    }
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:title message:message preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *ok = [UIAlertAction actionWithTitle:LocalizedString(@"Sure") style:UIAlertActionStyleDefault handler:okBlock];
    UIAlertAction *cancel = [UIAlertAction actionWithTitle:LocalizedString(@"Cancel") style:UIAlertActionStyleDefault handler:nil];
    [alert addAction:cancel];
    [alert addAction:ok];
    [topVC presentViewController:alert animated:YES completion:nil];
}

// 全文字显示
+ (void)showMessage:(NSString *)message {
    [SVProgressHUD showImage:[UIImage imageNamed:@""] status:message];
    [SVProgressHUD setDefaultAnimationType:SVProgressHUDAnimationTypeNative];
    [SVProgressHUD setDefaultStyle:SVProgressHUDStyleDark];
    [SVProgressHUD setDefaultMaskType:SVProgressHUDMaskTypeClear];
    [SVProgressHUD dismissWithDelay:2];
}

// 错误提示
+ (void)showError:(NSString *)str {
    [SVProgressHUD showErrorWithStatus:str];
    [SVProgressHUD setDefaultStyle:SVProgressHUDStyleDark];
    [SVProgressHUD setDefaultMaskType:SVProgressHUDMaskTypeClear];
    [SVProgressHUD dismissWithDelay:2];
}

// 成功提示
+ (void)showSuccess:(NSString *)str {
    [SVProgressHUD showSuccessWithStatus:str];
    [SVProgressHUD setDefaultStyle:SVProgressHUDStyleDark];
    [SVProgressHUD setDefaultMaskType:SVProgressHUDMaskTypeClear];
    [SVProgressHUD dismissWithDelay:2];
}

// 带菊花
+ (void)showLoadingStr:(NSString *)str {
    [SVProgressHUD showWithStatus:str];
    [SVProgressHUD setDefaultAnimationType:SVProgressHUDAnimationTypeNative];
    [SVProgressHUD setDefaultStyle:SVProgressHUDStyleDark];
    [SVProgressHUD setDefaultMaskType:SVProgressHUDMaskTypeClear];
    [SVProgressHUD dismissWithDelay:2];
    [SVProgressHUD setMaxSupportedWindowLevel:NSIntegerMax];
    [SVProgressHUD setContainerView:[UIApplication sharedApplication].delegate.window];
}

// 带菊花不超时
+ (void)showLoadingDelayStr:(NSString *)str {
    [SVProgressHUD showWithStatus:str];
    [SVProgressHUD setDefaultAnimationType:SVProgressHUDAnimationTypeNative];
    [SVProgressHUD setDefaultStyle:SVProgressHUDStyleDark];
    [SVProgressHUD setDefaultMaskType:SVProgressHUDMaskTypeClear];
    [SVProgressHUD setMaxSupportedWindowLevel:NSIntegerMax];
    [SVProgressHUD setContainerView:[UIApplication sharedApplication].delegate.window];
}

// 根据code全文字显示
+ (void)showCodeMessage:(long)msgCode {
    NSString *msg = @"";
    switch (msgCode) {
        case 40000001:
            msg = LocalizedString(@"ErrorInput");
            break;
        case 40000002:
            msg = LocalizedString(@"ErrorAccountExist");
            break;
        case 40000003:
            msg = LocalizedString(@"ErrorAccountLocked");
            break;
        case 40000006:
            msg = LocalizedString(@"ErrorTransaction");
            break;
        case 40000004:
            msg = LocalizedString(@"ErrorPaymentIncorrect");
            break;
        case 40000005:
        case 40003002:
            msg = LocalizedString(@"ErrorFormatInput");
            break;
        case 40000099:
            msg = LocalizedString(@"ErrorUnknown");
            break;
        case 40003003:
            msg = LocalizedString(@"ErrorDuplicateTransfers");
            break;
        case 40003004:
            msg = LocalizedString(@"ErrorEnoughBalance");
            break;
        case 40003005:
            msg = LocalizedString(@"ErrorInsufficient");
            break;
        case 40003006:
            msg = LocalizedString(@"ErrorRedPacket");
            break;
        case 40003007:
            msg = LocalizedString(@"ErrorGroupRedPack");
            break;
        case 40003008:
            msg = LocalizedString(@"ErrorMinimumNumber");
            break;
        case 40003010:
            msg = LocalizedString(@"ErrorSystem");
            break;
        case 40003015:
            msg = LocalizedString(@"ErrorBalanceFormat");
            break;
        case 40004006:
        case 40004007:
            msg = LocalizedString(@"ErrorAccountExist");
            break;
        case 60000000:
            msg = LocalizedString(@"ErrorAccountTradableTen");
            break;
        case 60000001:
            msg = LocalizedString(@"ErrorSendRed");
            break;
        case 60000002:
            msg = LocalizedString(@"ErrorAccountTradable");
            break;
        default:
            msg = LocalizedString(@"AlertOpearFail");
            break;
    }
    [SVProgressHUD showImage:[UIImage imageNamed:@""] status:msg];
    [SVProgressHUD setDefaultAnimationType:SVProgressHUDAnimationTypeNative];
    [SVProgressHUD setDefaultStyle:SVProgressHUDStyleDark];
    [SVProgressHUD setDefaultMaskType:SVProgressHUDMaskTypeClear];
    [SVProgressHUD dismissWithDelay:2];
}

+ (void)dismiss {
    [SVProgressHUD dismiss];
}

@end
