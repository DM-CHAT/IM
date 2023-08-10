//
//  DMCCThingsDataContent.h
//  DMChatClient
//
//  Created by heavyrain on 2017/8/16.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCMessageContent.h"

/**
 物联网数据
 */
@interface DMCCThingsDataContent : DMCCMessageContent
/**
 二进制数据内容
 */
@property (nonatomic, strong)NSData *data;
@end
