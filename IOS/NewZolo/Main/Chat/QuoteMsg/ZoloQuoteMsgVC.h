//
//  ZoloQuoteMsgVC.h
//  NewZolo
//
//  Created by MHHY on 2023/5/17.
//

#import "MHParentViewController.h"

NS_ASSUME_NONNULL_BEGIN

@interface ZoloQuoteMsgVC : MHParentViewController

- (instancetype)initWithQuoteInfo:(DMCCQuoteInfo *)quoteInfo;

@property (weak, nonatomic) IBOutlet UITextView *msgTextView;

@end

NS_ASSUME_NONNULL_END
