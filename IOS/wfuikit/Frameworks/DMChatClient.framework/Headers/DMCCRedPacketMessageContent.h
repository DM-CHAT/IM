//
//  DMCCTextMessageContent.h
//  DMChatClient
//
//  Created by heavyrain on 2017/8/16.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCMessageContent.h"

@interface DMCCRedPacketMessageContent : DMCCMessageContent
+ (instancetype)redPacketWithJson:(NSDictionary *)json;
@property (nonatomic, strong) NSString* ids;
@property (nonatomic, strong) NSString* text;
@property (nonatomic, strong) NSString* info;
@property (nonatomic, assign) int state;
@end
