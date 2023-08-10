//
//  DMCCUnknownMessageContent.h
//  WFChatClient
//
//  Created by heavyrain on 2017/8/16.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCMessageContent.h"

/**
 未知消息。所有未注册的消息都会解析为为止消息，主要用于新旧版本兼容
 */
@interface DMCCUnknownMessageContent : DMCCMessageContent

/**
 原消息类型
 */
@property (nonatomic, assign)NSInteger orignalType;


/**
 原消息Payload
 */
@property (nonatomic, strong)DMCCMessagePayload *orignalPayload;

@end
