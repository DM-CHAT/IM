//
//  DMCCTextMessageContent.h
//  DMChatClient
//
//  Created by heavyrain on 2017/8/16.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCMessageContent.h"
#import "DMCCLitappInfo.h"

typedef NS_ENUM(NSInteger, DMCCCardType) {
    CardType_User = 0,
    CardType_Group = 1,
    CardType_ChatRoom = 2,
    CardType_Channel = 3,
    CardType_Litapp = 4,
    CardType_Share = 5,
};

/**
 名片消息
 */
@interface DMCCCardMessageContent : DMCCMessageContent

/**
 构造方法

 @param targetId 目标Id
 @param type 类型，0 用户，1 群组， 3 频道。
 @param fromUser 分享用户。
 @return 名片消息
 */
+ (instancetype)cardWithTarget:(NSString *)targetId type:(DMCCCardType)type from:(NSString *)fromUser;
+ (instancetype)cardWithTarget:(NSString *)targetId type:(DMCCCardType)type from:(NSString *)fromUser dappInfo:(DMCCLitappInfo *)litInfo;
/**
  名片类型
 */
@property (nonatomic, assign)DMCCCardType type;

/**
 用户ID
 */
@property (nonatomic, strong)NSString *targetId;

/**
 用户号
 */
@property (nonatomic, strong)NSString *name;

/**
 用户昵称
 */
@property (nonatomic, strong)NSString *displayName;

/**
 用户头像
 */
@property (nonatomic, strong)NSString *portrait;
@property (nonatomic, strong)NSString *theme;
@property (nonatomic, strong)NSString *url;
@property (nonatomic, strong)NSString *info;
/**
分享的用户ID
 */
@property (nonatomic, strong)NSString *fromUser;
@end
