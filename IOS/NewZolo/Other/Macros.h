//
//  Macros.h
//  MHUBICell
//
//  Created by 梁兴炎 on 2020/3/7.
//  Copyright © 2020 梁兴炎. All rights reserved.
//

#import <UIKit/UIKit.h>

/** 全局通知、枚举属性定义 */
#ifndef Macros_h
#define Macros_h

#define FZ_EVENT_AUDIO_STATE                                    @"FZEventAudioStatus"                          // 语音状态变化
#define FZ_EVENT_MESSAGELISTCHANG_STATE                         @"MessageListChanged"                          // 消息列表状态变化
#define FZ_EVENT_CONVERSATIONLISTCHANG_STATE                    @"ConversationListChanged"                     // 删除会话状态变化
#define FZ_EVENT_CONVERSATIONGROUPLISTCHANG_STATE               @"ConversationGroupListChanged"                // 删除群聊会话状态变化
#define FZ_EVENT_DELETEUSER_STATE                               @"deleteUserChanged"                           // 删除用户状态变化
#define FZ_EVENT_GROUPREFRESH_STATE                             @"GroupRefreshChanged"                         // 群组刷新状态变化
#define FZ_EVENT_MSGSTATUS_STATE                                @"MsgStatusChanged"                            // 未读状态变化
#define FZ_EVENT_MSGINSERTSTATUS_STATE                          @"MsgInsertStatusChanged"                      // 消息插入
#define FZ_EVENT_TAGSTATUS_STATE                                @"TagStatusChanged"                            // 标签变化

/** alert 提示 */
typedef NS_ENUM(NSInteger, MHAlertType) {
    MHAlertTypeWarn,                                                                                        // 警告
    MHAlertTypeRemind                                                                                       // 提醒
};

/** 语音界面类型 */
typedef NS_ENUM(NSInteger, MHAudioType) {
    MHAudioType_Cancel,                                                                                        // 取消
    MHAudioType_Word,                                                                                          // 文字
    MHAudioType_RedayCancel,                                                                                   // 准备取消
    MHAudioType_ReadyWord,                                                                                     // 准备文字
    MHAudioType_Send,                                                                                          // 发送
    MHAudioType_RedaySend                                                                                      // 准备发送
};

/** 收藏类型 */
typedef NS_ENUM(NSInteger, MHCollctType) {
    MHCollctType_Litapp = 1,                                                                                   // 小程序
};

#endif /* Macros_h */
