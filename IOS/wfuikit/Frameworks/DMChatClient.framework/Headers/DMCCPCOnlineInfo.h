//
//  DMCCPCOnlineInfo.h
//  DMChatClient
//
//  Created by Tom Lee on 2020/4/6.
//  Copyright © 2020 WildFireChat. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/**
 PC在线类型

 - PC_Online: PC客户端在线
 - Web_Online: Web客户端在线
 - WX_Online: WX小程序客户端在线
 */
typedef NS_ENUM(NSInteger, DMCCPCOnlineType) {
    PC_Online,
    Web_Online,
    WX_Online
};


@interface DMCCPCOnlineInfo : NSObject
+ (instancetype)infoFromStr:(NSString *)strInfo withType:(DMCCPCOnlineType)type;
@property(nonatomic, assign)DMCCPCOnlineType type;
@property(nonatomic, assign)BOOL isOnline;
@property(nonatomic, assign)int/*DMCCPlatformType*/ platform;
@property(nonatomic, strong)NSString *clientId;
@property(nonatomic, strong)NSString *clientName;
@property(nonatomic, assign)long long timestamp;
@end

NS_ASSUME_NONNULL_END
