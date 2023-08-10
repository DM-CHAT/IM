//
//  DMCCNotificationMessageContent.h
//  DMChatClient
//
//  Created by heavyrain on 2017/9/19.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCMessageContent.h"


@class DMCCMessage;
/**
 通知消息的协议
 */
@protocol DMCCNotificationMessageContent <DMCCMessageContent>

/**
 获取通知的提示内容

 @return 提示内容
 */
- (NSString *)formatNotification:(DMCCMessage *)message;
@end

/**
 通知消息
 */
@interface DMCCNotificationMessageContent : DMCCMessageContent <DMCCNotificationMessageContent>

@end
