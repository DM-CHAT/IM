//
//  DMCCFriend.m
//  DMChatClient
//
//  Created by heavyrain on 2021/5/16.
//  Copyright © 2021 WildFireChat. All rights reserved.
//

#import "DMCCUnpackInfo.h"
#import "DMCCRedPacketInfo.h"

@implementation DMCCUnpackInfo
+ (instancetype) fromJson:(NSDictionary*) json{
    DMCCUnpackInfo *info = [DMCCUnpackInfo new];
    info.user = json[@"user"];
    info.fetcher = json[@"fetcher"];
    info.price = json[@"price"];
    info.packetID = json[@"packetID"];
    info.unpackID = json[@"unpackID"];
    info.timestamp = (NSInteger)json[@"timestamp"];
    return info;
}
+ (instancetype) fromRedPacket:(DMCCRedPacketInfo*) redPacket{
    DMCCUnpackInfo *info = [DMCCUnpackInfo new];
    info.user = redPacket.user;
    info.fetcher = redPacket.target;
    info.price = redPacket.price;
    info.packetID = redPacket.packetID;
    info.unpackID = redPacket.unpackID;
    info.timestamp = redPacket.timestamp;
    return info;
}
@end
