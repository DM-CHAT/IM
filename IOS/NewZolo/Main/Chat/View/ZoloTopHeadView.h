//
//  ZoloNoticeHeadView.h
//  NewZolo
//
//  Created by JTalking on 2022/9/22.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^DelectBtnBlock)(void);

@interface ZoloTopHeadView : UIView

- (instancetype)initWithShowNotice:(BOOL)isNotice;

@property (nonatomic, strong) NSArray *dataSource;
@property (weak, nonatomic) IBOutlet UIButton *topCloseBtn;
@property (nonatomic, copy) DelectBtnBlock delectBtnBlock;

@end

NS_ASSUME_NONNULL_END
