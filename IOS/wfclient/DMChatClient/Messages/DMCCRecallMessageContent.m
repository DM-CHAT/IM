//
//  DMCCTextMessageContent.m
//  DMChatClient
//
//  Created by heavyrain on 2017/8/16.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCRecallMessageContent.h"
#import "DMCCIMService.h"
#import "DMCCNetworkService.h"
#import "Common.h"
#import <config/config.h>

@implementation DMCCRecallMessageContent
- (DMCCMessagePayload *)encode {
    //注意：在proto层收到撤回命令或主动撤回成功会直接更新被撤回的消息，如果修改encode&decode，需要同步修改
    DMCCMessagePayload *payload = [super encode];
    payload.contentType = [self.class getContentType];
    payload.content = self.operatorId;
    payload.binaryContent = [[[NSNumber numberWithLongLong:self.messageUid] stringValue] dataUsingEncoding:NSUTF8StringEncoding];
    return payload;
}

- (void)decode:(DMCCMessagePayload *)payload {
    [super decode:payload];
    //注意：在proto层收到撤回命令或主动撤回成功会直接更新被撤回的消息，如果修改encode&decode，需要同步修改
    self.operatorId = payload.content;
    self.messageUid = [[[NSString alloc] initWithData:payload.binaryContent encoding:NSUTF8StringEncoding] longLongValue];
    if (self.extra.length) {
        NSError *__error = nil;
        NSDictionary *dictionary = [NSJSONSerialization JSONObjectWithData:[payload.extra dataUsingEncoding:NSUTF8StringEncoding]
                                                                   options:kNilOptions
                                                                     error:&__error];
        if (!__error) {
            self.originalSender = dictionary[@"s"];
            self.originalContentType = [dictionary[@"t"] intValue];
            self.originalSearchableContent = dictionary[@"sc"];
            self.originalContent = dictionary[@"c"];
            self.originalExtra = dictionary[@"e"];
            self.originalMessageTimestamp = [dictionary[@"ts"] longLongValue];
        }
    }
}

+ (int)getContentType {
    return MESSAGE_CONTENT_TYPE_RECALL;
}

+ (int)getContentFlags {
    return DMCCPersistFlag_PERSIST;
}


+ (void)load {
    [[DMCCIMService sharedDMCIMService] registerMessageContent:self];
}

- (NSString *)formatNotification:(DMCCMessage *)message {
    return [self digest:message];
}


- (NSString *)digest:(DMCCMessage *)message {
    if ([self.operatorId isEqualToString:[DMCCNetworkService sharedInstance].userId]) {
        return LocalizedString(@"You_recall_message");
    } else {
        DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:message.fromUser refresh:NO];
        if (userInfo.friendAlias.length) {
            return [NSString stringWithFormat:@"%@%@", userInfo.friendAlias, LocalizedString(@"Recall_message")];
        }
        if (userInfo.displayName != nil) {
            NSString *str = [NSString stringWithFormat:@"%@%@", userInfo.displayName, LocalizedString(@"Recall_message")];
            return str;
        }
        return [NSString stringWithFormat:@"%@%@", self.operatorId, LocalizedString(@"Recall_message")];
    }
}
@end
