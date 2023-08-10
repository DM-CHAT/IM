//
//  WBQRCodeVC.h
//  SGQRCodeExample
//
//  Created by kingsic on 2018/2/8.
//  Copyright © 2018年 kingsic. All rights reserved.
//

#import <UIKit/UIKit.h>

typedef void(^ScanResultBlcok)(NSString *result);

@interface WBQRCodeVC : UIViewController

@property(nonatomic, copy) ScanResultBlcok resultBlcok;

@end
