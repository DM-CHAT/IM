//
//  WFCCFriend.h
//  WFChatClient
//
//  Created by heavyrain on 2021/5/16.
//  Copyright © 2021 WildFireChat. All rights reserved.
//

#import <Foundation/Foundation.h>	

NS_ASSUME_NONNULL_BEGIN

@interface WFCCRedPacketInfo : NSObject
@property(nonatomic, strong) NSString* type;
@property(nonatomic, strong) NSString* user;
@property(nonatomic, strong) NSString* count;
@property(nonatomic, strong) NSString* price;
@property(nonatomic, strong) NSString* target;
@property(nonatomic, strong) NSString* text;
@property(nonatomic, strong) NSString* packetID;
@property(nonatomic, strong) NSString* unpackID;
@property(nonatomic, strong) NSString* urlQuery;
@property(nonatomic, strong) NSString* urlFetch;
@property(nonatomic, strong) NSString* luckNum;
@property(nonatomic, assign) long timestamp;
@property(nonatomic, assign) int state;
+ (instancetype) redPacketInfoWithJson:(NSDictionary*)json;
@end

NS_ASSUME_NONNULL_END
