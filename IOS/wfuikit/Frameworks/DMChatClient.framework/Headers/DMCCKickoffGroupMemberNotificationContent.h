//
//  DMCCKickoffGroupMemberNotificationContent.h
//  DMChatClient
//
//  Created by heavyrain on 2017/9/20.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCNotificationMessageContent.h"

/**
 群组踢人的通知消息
 */
@interface DMCCKickoffGroupMemberNotificationContent : DMCCNotificationMessageContent

/**
 群组ID
 */
@property (nonatomic, strong)NSString *groupId;

/**
 操作者ID
 */
@property (nonatomic, strong)NSString *operateUser;

/**
 被踢成员的ID列表
 */
@property (nonatomic, strong)NSArray<NSString *> *kickedMembers;

@end
