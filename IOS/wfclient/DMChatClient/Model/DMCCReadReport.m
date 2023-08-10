//
//  DMCCConversation.m
//  DMChatClient
//
//  Created by heavyrain on 2017/8/16.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCReadReport.h"

@implementation DMCCReadReport
+(instancetype)readed:(DMCCConversation *)conversation
               userId:(NSString *)userId
            timestamp:(long long)timestamp {
    DMCCReadReport *d = [[DMCCReadReport alloc] init];
    d.conversation = conversation;
    d.userId = userId;
    d.timestamp = timestamp;
    return d;;
}
@end
