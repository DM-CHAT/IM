//
//  DMCCMessage.h
//  DMChatClient
//
//  Created by heavyrain on 2017/8/16.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "DMCCConversation.h"
#import "DMCCMessageContent.h"

/**
 消息方向

 - MessageDirection_Send: 发送
 - MessageDirection_Receive: 接收
 */
typedef NS_ENUM(NSInteger, DMCCMessageDirection) {
    MessageDirection_Send,
    MessageDirection_Receive
};

/**
 消息状态

 - Message_Status_Sending: 发送中
 - Message_Status_Sent: 发送成功
 - Message_Status_Send_Failure: 发送失败
 - Message_Status_Unread: 未读
 - Message_Status_Readed: 已读
 - Message_Status_Played: 已播放(媒体消息)
 */
typedef NS_ENUM(NSInteger, DMCCMessageStatus) {
    Message_Status_Sending,
    Message_Status_Sent,
    Message_Status_Send_Failure,
    Message_Status_Mentioned,
    Message_Status_AllMentioned,
    Message_Status_Unread,
    Message_Status_Readed,
    Message_Status_Played,
    Message_Status_Opened
};

/**
 消息实体
 */
@interface DMCCMessage : NSObject

/**
    消息高度
 */
@property (nonatomic, assign)NSInteger msgCellHeight;

@property (nonatomic, assign)NSInteger msgCellWidth;

/**
 消息ID，当前用户本地唯一
 */
@property (nonatomic, assign)long messageId;

/**
 消息UID，所有用户全局唯一
 */
@property (nonatomic, assign)long long messageUid;

/**
 消息所属的会话
 */
@property (nonatomic, strong)DMCCConversation *conversation;

/**
 消息发送者的用户ID
 */
@property (nonatomic, strong)NSString * fromUser;

/**
 消息在会话中定向发送给该用户的
 */
@property (nonatomic, strong)NSArray<NSString *> *toUsers;

@property (nonatomic, strong)NSString * saveFrome;
@property (nonatomic, strong)NSString * saveTo;

/**
 消息内容
 */
@property (nonatomic, strong)DMCCMessageContent *content;

/**
 消息方向
 */
@property (nonatomic, assign)DMCCMessageDirection direction;

/**
 消息状态
 */
@property (nonatomic, assign)DMCCMessageStatus status;

/**
 消息的发送时间
 */
@property (nonatomic, assign)long long serverTime;

/**
 消息本地附加信息
 */
@property (nonatomic, strong)NSString * localExtra;
@property (nonatomic, strong)NSString * messageHash;
@property (nonatomic, strong)NSString * messageHasho;
- (NSString *)digest;

@property (nonatomic, assign) BOOL isMulSelect;

@end
