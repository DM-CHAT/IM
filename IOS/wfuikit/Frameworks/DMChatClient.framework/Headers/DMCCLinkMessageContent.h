//
//  DMCCLinkMessageContent.h
//  DMChatClient
//
//  Created by heavyrain on 2017/8/16.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCMessageContent.h"

/**
 链接消息
 */
@interface DMCCLinkMessageContent : DMCCMessageContent

/**
 链接标题
 */
@property (nonatomic, strong)NSString *title;

/**
 内容摘要
 */
@property (nonatomic, strong)NSString *contentDigest;

/**
 链接地址
 */
@property (nonatomic, strong)NSString *url;

/**
 链接图片地址
 */
@property (nonatomic, strong)NSString *thumbnailUrl;

@end
