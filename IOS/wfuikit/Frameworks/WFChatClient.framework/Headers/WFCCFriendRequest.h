//
//  WFCCFriendRequest.h
//  WFChatClient
//
//  Created by heavyrain on 2017/10/17.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import <Foundation/Foundation.h>

#define RequestType_Friend 0
#define RequestType_InviteGroup 1
#define RequestType_ApplyMember 2

#define RequestStatus_Sent 0
#define RequestStatus_Accepted 1
#define RequestStatus_Rejected 3

#define RequestDirection_Sent 0
#define RequestDirection_Recv 1

/**
 好友请求
 */
@interface WFCCFriendRequest : NSObject

@property(nonatomic, assign)int type;
@property(nonatomic, assign)NSString *userID;
@property(nonatomic, assign)NSString *originalUser;
/**
 方向
 */
@property(nonatomic, assign)int direction;

/**
 ID
 */
@property(nonatomic, strong)NSString *target;

/**
 请求说明
 */
@property(nonatomic, strong)NSString *reason;

/**
 请求扩展信息
 */
@property(nonatomic, strong)NSString *extra;

/**
 接受状态
 */
@property(nonatomic, assign)int status;

/**
 已读
 */
@property(nonatomic, assign)int readStatus;

/**
 发起时间
 */
@property(nonatomic, assign)long long timestamp;
@property(nonatomic, strong)NSString *invitation;

@end
