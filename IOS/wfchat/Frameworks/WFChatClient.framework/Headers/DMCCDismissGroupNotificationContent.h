//
//  DMCCDismissGroupNotificationContent.h
//  WFChatClient
//
//  Created by heavyrain on 2017/9/20.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCNotificationMessageContent.h"

/**
 群解散的通知消息
 */
@interface DMCCDismissGroupNotificationContent : DMCCNotificationMessageContent

/**
 群组ID
 */
@property (nonatomic, strong)NSString *groupId;

/**
 操作者ID
 */
@property (nonatomic, strong)NSString *operateUser;

@end
