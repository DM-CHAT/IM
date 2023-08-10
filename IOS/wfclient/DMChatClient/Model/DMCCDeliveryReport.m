//
//  DMCCConversation.m
//  DMChatClient
//
//  Created by heavyrain on 2017/8/16.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCDeliveryReport.h"

@implementation DMCCDeliveryReport
+(instancetype)delivered:(NSString *)userId
               timestamp:(long long)timestamp {
    DMCCDeliveryReport *d = [[DMCCDeliveryReport alloc] init];
    d.userId = userId;
    d.timestamp = timestamp;
    return d;;
}
@end
