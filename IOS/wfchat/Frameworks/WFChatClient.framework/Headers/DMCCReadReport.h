//
//  DMCCConversation.h
//  WFChatClient
//
//  Created by heavyrain on 2017/8/16.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "DMCCConversation.h"
/**
 会话
 */
@interface DMCCReadReport : NSObject

+(instancetype)readed:(DMCCConversation *)conversation
               userId:(NSString *)userId
            timestamp:(long long)timestamp;

@property (nonatomic, strong)DMCCConversation *conversation;
@property (nonatomic, strong)NSString *userId;
@property (nonatomic, assign)long long timestamp;

@end
