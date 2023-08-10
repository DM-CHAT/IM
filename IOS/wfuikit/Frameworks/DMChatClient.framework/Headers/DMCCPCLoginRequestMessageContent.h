//
//  DMCCPCLoginRequestMessageContent.h
//  DMChatClient
//
//  Created by heavyrain on 2017/9/19.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCNotificationMessageContent.h"
#import "DMCCIMService.h"

/**
 建群的通知消息
 */
@interface DMCCPCLoginRequestMessageContent : DMCCMessageContent

/**
 PC登录SessionID
 */
@property (nonatomic, strong)NSString *sessionId;

/**
 PC登录类型
 */
@property (nonatomic, assign)DMCCPlatformType platform;

@end
