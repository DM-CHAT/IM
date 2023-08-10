//
//  DMCCConversation.m
//  DMChatClient
//
//  Created by heavyrain on 2017/8/16.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCConversation.h"

@implementation DMCCConversation
+(instancetype)conversationWithType:(DMCCConversationType)type target:(NSString *)target line:(int)line {
    DMCCConversation *conversation = [[DMCCConversation alloc] init];
    conversation.type = type;
    conversation.target = target;
    conversation.line = line;
    return conversation;
}
- (BOOL)isEqual:(id)object {
    if ([object isMemberOfClass:[DMCCConversation class]]) {
        DMCCConversation *o = (DMCCConversation *)object;
        if (self.type == o.type && [self.target isEqual:o.target] && self.line == o.line) {
            return YES;
        }
    }
    return NO;
}
@end
