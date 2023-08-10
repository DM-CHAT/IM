//
//  DMCCConversationSearchInfo.h
//  DMChatClient
//
//  Created by heavyrain on 2017/10/22.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "DMCCConversation.h"
#import "DMCCMessage.h"

/**
 会话搜索信息
 */
@interface DMCCConversationSearchInfo : NSObject

/**
 会话
 */
@property (nonatomic, strong)DMCCConversation *conversation;

/**
 命中的消息
 */
@property (nonatomic, strong)DMCCMessage *marchedMessage;

/**
 命中数量
 */
@property (nonatomic, assign)int marchedCount;

/**
 搜索关键字
 */
@property (nonatomic, strong)NSString *keyword;
@end
