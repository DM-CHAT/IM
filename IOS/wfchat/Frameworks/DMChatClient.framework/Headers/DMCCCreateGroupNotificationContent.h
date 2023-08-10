//
//  DMCCCreateGroupNotificationContent.h
//  DMChatClient
//
//  Created by heavyrain on 2017/9/19.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCNotificationMessageContent.h"

/**
 建群的通知消息
 */
@interface DMCCCreateGroupNotificationContent : DMCCNotificationMessageContent

/**
 群组ID
 */
@property (nonatomic, strong)NSString *groupId;

/**
 创建者ID
 */
@property (nonatomic, strong)NSString *creator;

/**
 群名
 */
@property (nonatomic, strong)NSString *groupName;

@end
