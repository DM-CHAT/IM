//
//  DMCCQuitGroupNotificationContent.h
//  DMChatClient
//
//  Created by heavyrain on 2017/9/20.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCNotificationMessageContent.h"

/**
 退群的通知消息
 */
@interface DMCCQuitGroupNotificationContent : DMCCNotificationMessageContent

/**
 群组ID
 */
@property (nonatomic, strong)NSString *groupId;

/**
 退群成员的ID
 */
@property (nonatomic, strong)NSString *quitMember;
@end
