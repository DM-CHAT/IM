//
//  DMCCTextMessageContent.m
//  DMChatClient
//
//  Created by heavyrain on 2017/8/16.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCCardMessageContent.h"
#import "DMCCIMService.h"
#import "Common.h"
#import <config/config.h>

@implementation DMCCCardMessageContent
- (DMCCMessagePayload *)encode {
    DMCCMessagePayload *payload = [super encode];
    payload.contentType = [self.class getContentType];
    payload.content = self.targetId;

    NSMutableDictionary *dataDict = [NSMutableDictionary dictionary];
    if (self.name) {
        [dataDict setObject:self.name forKey:@"n"];
    }
    if (self.displayName) {
        [dataDict setObject:self.displayName forKey:@"d"];
    }
    
    if (self.portrait) {
        [dataDict setObject:self.portrait forKey:@"p"];
    }
    if (self.type) {
        [dataDict setObject:@(self.type) forKey:@"t"];
    }
    if(self.fromUser) {
        [dataDict setObject:self.fromUser forKey:@"f"];
    }
    payload.binaryContent = [NSJSONSerialization dataWithJSONObject:dataDict
                                                            options:kNilOptions
                                                              error:nil];
    
    return payload;
}

- (void)decode:(DMCCMessagePayload *)payload {
    [super decode:payload];
    self.targetId = payload.content;
    
    NSError *__error = nil;
    NSDictionary *dictionary = [NSJSONSerialization JSONObjectWithData:payload.binaryContent
                                                               options:kNilOptions
                                                                 error:&__error];
    if (!__error) {
        self.name = dictionary[@"n"];
        self.displayName = dictionary[@"d"];
        self.portrait = dictionary[@"p"];
        self.type = [dictionary[@"t"] intValue];
        self.fromUser = dictionary[@"f"];
    }
}

+ (int)getContentType {
    return MESSAGE_CONTENT_TYPE_CARD;
}

+ (int)getContentFlags {
    return DMCCPersistFlag_PERSIST_AND_COUNT;
}

+ (instancetype)cardWithTarget:(NSString *)targetId type:(DMCCCardType)type from:(NSString *)fromUser {
    DMCCCardMessageContent *content = [[DMCCCardMessageContent alloc] init];
    content.targetId = targetId;
    content.type = type;
    content.fromUser = fromUser;
    if (type == 0) {
        DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:targetId refresh:NO];
        content.name = userInfo.name;
        content.displayName = userInfo.displayName;
        content.portrait = userInfo.portrait;
    } else if(type == 1) {
        DMCCGroupInfo *groupInfo = [[DMCCIMService sharedDMCIMService] getGroupInfo:targetId refresh:NO];
        content.name = groupInfo.name;
        content.displayName = groupInfo.name;
        content.portrait = groupInfo.portrait;
    } else if(type == 3) {
        DMCCChannelInfo *channelInfo = [[DMCCIMService sharedDMCIMService] getChannelInfo:targetId refresh:NO];
        content.name = channelInfo.name;
        content.displayName = channelInfo.name;
        content.portrait = channelInfo.portrait;
    } else if (type == 4) {
        DMCCLitappInfo *litapp = [[DMCCIMService sharedDMCIMService] getLitappInfoWithTarget:targetId refresh:NO success:nil error:nil];
        content.name = litapp.name;
        content.displayName = litapp.name;
        content.portrait = litapp.portrait;
        content.url = litapp.url;
    }
    
    return content;
}

+ (instancetype)cardWithTarget:(NSString *)targetId type:(DMCCCardType)type from:(NSString *)fromUser dappInfo:(DMCCLitappInfo *)litInfo {
    DMCCCardMessageContent *content = [[DMCCCardMessageContent alloc] init];
    content.targetId = targetId;
    content.type = type;
    content.fromUser = fromUser;
    if (type == 0) {
        DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:targetId refresh:NO];
        content.name = userInfo.name;
        content.displayName = userInfo.displayName;
        content.portrait = userInfo.portrait;
    } else if(type == 1) {
        DMCCGroupInfo *groupInfo = [[DMCCIMService sharedDMCIMService] getGroupInfo:targetId refresh:NO];
        content.name = groupInfo.name;
        content.displayName = groupInfo.name;
        content.portrait = groupInfo.portrait;
    } else if(type == 3) {
        DMCCChannelInfo *channelInfo = [[DMCCIMService sharedDMCIMService] getChannelInfo:targetId refresh:NO];
        content.name = channelInfo.name;
        content.displayName = channelInfo.name;
        content.portrait = channelInfo.portrait;
    } else if (type == 4) {
        content.name = litInfo.name;
        content.displayName = litInfo.name;
        content.portrait = litInfo.portrait;
        content.info = litInfo.info;
        content.url = litInfo.url;
    }
    
    return content;
}

+ (void)load {
    [[DMCCIMService sharedDMCIMService] registerMessageContent:self];
}

- (NSString *)digest:(DMCCMessage *)message {
    if (self.displayName.length) {
        return [NSString stringWithFormat:@"%@%@", LocalizedString(@"card_digest"), self.displayName];
    }
    return LocalizedString(@"card_digest");
}
@end
