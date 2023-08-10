//
//  ZoloAddTagVC.h
//  NewZolo
//
//  Created by JTalking on 2022/10/27.
//

#import "MHParentViewController.h"
#import "ZoloTagModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface ZoloAddTagVC : MHParentViewController

@property (nonatomic, strong) ZoloTagModel *info;
@property (nonatomic, strong) UIView *btnView;

- (instancetype)initWithTagInfo:(ZoloTagModel *)info;

@end

NS_ASSUME_NONNULL_END
