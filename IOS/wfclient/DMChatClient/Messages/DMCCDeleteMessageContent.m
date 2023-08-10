//
//  DMCCTextMessageContent.m
//  DMChatClient
//
//  Created by heavyrain on 2017/8/16.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCDeleteMessageContent.h"
#import "DMCCIMService.h"
#import "DMCCNetworkService.h"
#import "Common.h"


@implementation DMCCDeleteMessageContent
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
}

+ (int)getContentType {
    return MESSAGE_CONTENT_TYPE_DELETE;
}

+ (int)getContentFlags {
    return DMCCPersistFlag_NOT_PERSIST;
}


+ (void)load {
    [[DMCCIMService sharedDMCIMService] registerMessageContent:self];
}


- (NSString *)digest:(DMCCMessage *)message {
    return nil;
}
@end
