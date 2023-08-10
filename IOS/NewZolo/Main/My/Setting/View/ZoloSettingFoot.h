//
//  ZoloSettingFoot.h
//  NewZolo
//
//  Created by JTalking on 2022/9/15.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^QuitBlock)(void);
typedef void(^DeleteBlock)(void);

@interface ZoloSettingFoot : UITableViewHeaderFooterView

@property (weak, nonatomic) IBOutlet UIButton *quitBtn;
@property (weak, nonatomic) IBOutlet UIButton *deleteBtn;

@property (nonatomic, copy) QuitBlock quitBlock;
@property (nonatomic, copy) DeleteBlock deleteBlock;

@end

NS_ASSUME_NONNULL_END
