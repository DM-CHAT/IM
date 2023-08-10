//
//  DMCCFriend.h
//  DMChatClient
//
//  Created by heavyrain on 2021/5/16.
//  Copyright © 2021 WildFireChat. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "DMCCRedPacketInfo.h"

NS_ASSUME_NONNULL_BEGIN

@interface DMCCUnpackInfo : NSObject
@property(nonatomic, strong) NSString* user;
@property(nonatomic, strong) NSString* fetcher;
@property(nonatomic, strong) NSString* price;
@property(nonatomic, strong) NSString* packetID;
@property(nonatomic, strong) NSString* unpackID;
@property(nonatomic, assign) long timestamp;
+ (instancetype) fromJson:(NSDictionary*) json;
+ (instancetype) fromRedPacket:(DMCCRedPacketInfo*) redPacket;
@end

NS_ASSUME_NONNULL_END
