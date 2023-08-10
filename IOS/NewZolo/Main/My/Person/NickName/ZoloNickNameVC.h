//
//  ZoloNickNameVC.h
//  NewZolo
//
//  Created by JTalking on 2022/7/8.
//

#import "MHParentViewController.h"

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, MHNickNameType) {
    MHNickNameType_My,                                                                                         // 自己昵称
    MHNickNameType_User,                                                                                       // 用户昵称
    MHNickNameType_Group,                                                                                      // 本群组昵称
    MHNickNameType_GroupName                                                                                   // 群昵称
};

@interface ZoloNickNameVC : MHParentViewController

- (instancetype)initWithSettingRemarkName:(MHNickNameType)type WithSId:(NSString *)SId;

@end

NS_ASSUME_NONNULL_END
