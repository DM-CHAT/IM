//
//  DMCCTextMessageContent.m
//  DMChatClient
//
//  Created by heavyrain on 2017/8/16.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCRedPacketMessageContent.h"
#import "DMCCIMService.h"
#import "Common.h"
#import <config/config.h>

@implementation DMCCRedPacketMessageContent
- (DMCCMessagePayload *)encode {
    DMCCMessagePayload *payload = [super encode];
    payload.contentType = [self.class getContentType];
    payload.content = self.ids;

    NSMutableDictionary *dataDict = [NSMutableDictionary dictionary];
    if (self.ids) {
        [dataDict setObject:self.ids forKey:@"i"];
    }
    if (self.text) {
        [dataDict setObject:self.text forKey:@"t"];
    }
    
    if (self.info) {
        [dataDict setObject:self.info forKey:@"o"];
    }
    if (self.state) {
        [dataDict setObject:@(self.state) forKey:@"t"];
    }
    payload.binaryContent = [NSJSONSerialization dataWithJSONObject:dataDict
                                                            options:kNilOptions
                                                              error:nil];
    
    return payload;
}

- (void)decode:(DMCCMessagePayload *)payload {
    [super decode:payload];
    self.ids = payload.content;
    
    NSError *__error = nil;
    NSDictionary *dictionary = [NSJSONSerialization JSONObjectWithData:payload.binaryContent
                                                               options:kNilOptions
                                                                 error:&__error];
    if (!__error) {
        self.ids = dictionary[@"i"];
        self.text = dictionary[@"t"];
        self.info = dictionary[@"o"];
        self.state = [dictionary[@"t"] intValue];
    }
}

+ (int)getContentType {
    return MESSAGE_CONTENT_TYPE_RED_PACKET;
}

+ (int)getContentFlags {
    return DMCCPersistFlag_PERSIST_AND_COUNT;
}


+ (instancetype)redPacketWithJson:(NSDictionary *)json {
    DMCCRedPacketMessageContent *content = [DMCCRedPacketMessageContent new];
    content.ids = json[@"packetID"];
    content.text = json[@"text"];
    content.info = json[@"@info"];
    content.state = ((NSNumber*)json[@"state"]).intValue;
    return content;
}

+ (void)load {
    [[DMCCIMService sharedDMCIMService] registerMessageContent:self];
}

- (NSString *)digest:(DMCCMessage *)message {
    DMCCRedPacketMessageContent *content = (DMCCRedPacketMessageContent *)message.content;
    DMCCRedPacketInfo *info = [[DMCCIMService sharedDMCIMService] getRedPacket:content.ids];
    if (content.info) {
        NSData *dataString = [content.info dataUsingEncoding:NSUTF8StringEncoding];
        NSDictionary *dataDic = [NSJSONSerialization JSONObjectWithData:dataString options:NSJSONReadingMutableContainers error:nil];
        if ([dataDic[@"type"] isEqualToString:@"transaction"]) { // 转账
            return LocalizedString(@"ChatTransfer");
        } else if ([dataDic[@"type"] isEqualToString:@"random"]) { // 手气
            return LocalizedString(@"red_packet_digest");
        } else if ([dataDic[@"type"] isEqualToString:@"bomb"]) { // 扫雷
            return LocalizedString(@"red_packet_digest");
        } else if ([info.type isEqualToString:@"normal"]) { // 转账
            return LocalizedString(@"ChatTransfer");
        } else if ([info.type isEqualToString:@"loot"]) { // 手气
            return LocalizedString(@"red_packet_digest");
        } else if ([info.type isEqualToString:@"bomb"]) { // 扫雷
            return LocalizedString(@"red_packet_digest");
        } else {
            return LocalizedString(@"red_packet_digest");
        }
    } else {
        if ([info.type isEqualToString:@"normal"]) { // 转账
            return LocalizedString(@"ChatTransfer");
        } else if ([info.type isEqualToString:@"loot"]) { // 手气
            return LocalizedString(@"red_packet_digest");
        } else if ([info.type isEqualToString:@"bomb"]) { // 扫雷
            return LocalizedString(@"red_packet_digest");
        } else {
            return LocalizedString(@"red_packet_digest");
        }
    }
}
@end
