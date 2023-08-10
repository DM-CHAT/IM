//
//  DMCCUnreadCount.m
//  DMChatClient
//
//  Created by WF Chat on 2018/9/30.
//  Copyright © 2018 WildFireChat. All rights reserved.
//

#import "DMCCUnreadCount.h"

@implementation DMCCUnreadCount
+(instancetype)countOf:(int)unread mention:(int)mention mentionAll:(int)mentionAll {
    DMCCUnreadCount *count = [[DMCCUnreadCount alloc] init];
    count.unread = unread;
    count.unreadMention = mention;
    count.unreadMentionAll = mentionAll;
    return count;
}
@end
