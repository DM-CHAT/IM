//
//  DMCCDeleteMessageContent.h
//  DMChatClient
//
//  Created by heavyrain on 2017/8/16.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCMessageContent.h"

/**
 文本消息
 */
@interface DMCCDeleteMessageContent : DMCCMessageContent

/**
 被删除消息的Uid
 */
@property (nonatomic, assign)long long messageUid;

/**
 撤回用户Id
 */
@property (nonatomic, strong)NSString *operatorId;
@end
