//
//  DMCCFriend.m
//  DMChatClient
//
//  Created by heavyrain on 2021/5/16.
//  Copyright © 2021 WildFireChat. All rights reserved.
//

#import "DMCCRedPacketInfo.h"

@implementation DMCCRedPacketInfo
+ (instancetype) redPacketInfoWithJson:(NSDictionary*)json{
    DMCCRedPacketInfo* info = [DMCCRedPacketInfo new];
    info.type = json[@"type"];
    if([info.type isEqualToString:@"transaction"])
        info.type = @"normal";
    else if([info.type isEqualToString:@"random"])
        info.type = @"loot";
    else if([info.type isEqualToString:@"bomb"])
        info.type = @"bomb";
    info.user = json[@"from"];
    info.count = json[@"count"];
    info.price = json[@"balance"];
    info.target = json[@"to"];
    info.packetID = json[@"txid"];
    info.text = json[@"greetings"];
    info.unpackID = json[@"txid"];
    info.timestamp = ((NSNumber*)json[@"timestamp"]).longValue;
    info.state = ((NSNumber*)json[@"state"]).intValue;
    info.urlFetch = json[@"urlFetch"];
    info.urlQuery = json[@"urlQuery"];
    info.luckNum = json[@"luckNum"];
    info.wallet = [DMCCRedPacketInfo dic2Json:json[@"wallet"]];
    info.coinType = json[@"coinType"];
    return info;
}

+ (NSString*) dic2Json:(NSDictionary*)json{
    
    NSData *data = [NSJSONSerialization dataWithJSONObject:json options:0 error:nil];
    return [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
}

@end
