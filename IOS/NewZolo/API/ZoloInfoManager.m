//
//  ZoloInfoManager.m
//  NewZolo
//
//  Created by JTalking on 2022/10/6.
//

#import "ZoloInfoManager.h"

@implementation ZoloInfoManager

static ZoloInfoManager *_UserManager;

+ (instancetype)allocWithZone:(struct _NSZone *)zone {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _UserManager = [super allocWithZone:zone];
    });
    return _UserManager;
}

+ (instancetype)sharedUserManager {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _UserManager = [[self alloc] init];
    });
    return _UserManager;
}

- (id)copyWithZone:(NSZone *)zone {
    return _UserManager;
}

- (instancetype)init {
    self = [super init];
    if (self) {
        NSDictionary *userDict = [[NSUserDefaults standardUserDefaults] objectForKey:@"currentInfo"];
        if (userDict != nil) {
            self.currentInfo = [MHLoginModel yy_modelWithDictionary:userDict];
        }
    }
    return self;
}

- (void)setCurrentInfo:(MHLoginModel *)currentInfo {
    if (currentInfo) {
        _currentInfo = currentInfo;
        NSDictionary *userDict = currentInfo.yy_modelToJSONObject;
        [[NSUserDefaults standardUserDefaults] setObject:userDict forKey:@"currentInfo"];
    } else {
        _currentInfo = nil;
        [[NSUserDefaults standardUserDefaults] removeObjectForKey:@"currentInfo"];
    }
}

/** 保存当前用户 */
- (void)savecurrentInfo:(MHLoginModel *)user {
    _currentInfo = user;
    NSDictionary *userDict = user.yy_modelToJSONObject;
    [[NSUserDefaults standardUserDefaults] setObject:userDict forKey:@"currentInfo"];
}

/** 退出登录 */
- (void)loginOut {
    _currentInfo = nil;
    [[NSUserDefaults standardUserDefaults] removeObjectForKey:@"currentInfo"];
}

@end
