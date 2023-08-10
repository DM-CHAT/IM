//
//  DMCCConversationInfo.h
//  WFChatClient
//
//  Created by heavyrain on 2017/8/29.
//  Copyright © 2017年 wildfire chat. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "DMCCConversation.h"
#import "DMCCMessage.h"
#import "DMCCUnreadCount.h"

/**
 会话信息
 */
@interface DMCCConversationInfo : NSObject

/**
 会话
 */
@property (nonatomic, strong)DMCCConversation *conversation;

/**
 最后一条消息
 */
@property (nonatomic, strong)DMCCMessage *lastMessage;

/**
 草稿
 */
@property (nonatomic, strong)NSString *draft;

/**
 最后一条消息的时间戳
 */
@property (nonatomic, assign)long long timestamp;

/**
 未读数
 */
@property (nonatomic, strong)DMCCUnreadCount *unreadCount;

/**
 是否置顶
 */
@property (nonatomic, assign)BOOL isTop;

/**
 是否设置了免打扰
 */
@property (nonatomic, assign)BOOL isSilent;

@end


