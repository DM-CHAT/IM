//
//  WFChatClient.h
//  WFChatClient
//
//  Created by heavyrain on 2017/11/5.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import <UIKit/UIKit.h>

//! Project version number for WFChatClient.
FOUNDATION_EXPORT double WFChatClientVersionNumber;

//! Project version string for WFChatClient.
FOUNDATION_EXPORT const unsigned char WFChatClientVersionString[];

// In this header, you should import all the public headers of your framework using statements like #import <WFChatClient/PublicHeader.h>


#import <WFChatClient/DMCCIMService.h>
#import <WFChatClient/DMCCNetworkService.h>
#import <WFChatClient/Common.h>

#import <WFChatClient/DMCCMessage.h>
#import <WFChatClient/DMCCMessageContent.h>
#import <WFChatClient/DMCCAddGroupeMemberNotificationContent.h>
#import <WFChatClient/DMCCCreateGroupNotificationContent.h>
#import <WFChatClient/DMCCDismissGroupNotificationContent.h>
#import <WFChatClient/DMCCImageMessageContent.h>
#import <WFChatClient/DMCCKickoffGroupMemberNotificationContent.h>
#import <WFChatClient/DMCCMediaMessageContent.h>
#import <WFChatClient/DMCCNotificationMessageContent.h>
#import <WFChatClient/DMCCTipNotificationMessageContent.h>
#import <WFChatClient/DMCCQuitGroupNotificationContent.h>
#import <WFChatClient/DMCCGroupNotifyContent.h>
#import <WFChatClient/DMCCSoundMessageContent.h>
#import <WFChatClient/DMCCFileMessageContent.h>
#import <WFChatClient/DMCCTextMessageContent.h>
#import <WFChatClient/DMCCPTextMessageContent.h>
#import <WFChatClient/DMCCUnknownMessageContent.h>
#import <WFChatClient/DMCCChangeGroupNameNotificationContent.h>
#import <WFChatClient/DMCCChangeGroupPortraitNotificationContent.h>
#import <WFChatClient/DMCCModifyGroupAliasNotificationContent.h>
#import <WFChatClient/DMCCTransferGroupOwnerNotificationContent.h>
#import <WFChatClient/DMCCStickerMessageContent.h>
#import <WFChatClient/DMCCLocationMessageContent.h>
#import <WFChatClient/DMCCCallStartMessageContent.h>
#import <WFChatClient/DMCCTypingMessageContent.h>
#import <WFChatClient/DMCCRecallMessageContent.h>
#import <WFChatClient/DMCCVideoMessageContent.h>
#import <WFChatClient/DMCCFriendAddedMessageContent.h>
#import <WFChatClient/DMCCFriendGreetingMessageContent.h>
#import <WFChatClient/DMCCGroupPrivateChatNotificationContent.h>
#import <WFChatClient/DMCCGroupJoinTypeNotificationContent.h>
#import <WFChatClient/DMCCGroupSetManagerNotificationContent.h>
#import <WFChatClient/DMCCGroupMemberMuteNotificationContent.h>
#import <WFChatClient/DMCCGroupMemberAllowNotificationContent.h>
#import <WFChatClient/DMCCDeleteMessageContent.h>
#import <WFChatClient/DMCCGroupMuteNotificationContent.h>
#import <WFChatClient/DMCCPCLoginRequestMessageContent.h>
#import <WFChatClient/DMCCCardMessageContent.h>
#import <WFChatClient/DMCCRedPacketMessageContent.h>
#import <WFChatClient/DMCCThingsDataContent.h>
#import <WFChatClient/DMCCThingsLostEventContent.h>
#import <WFChatClient/DMCCConferenceInviteMessageContent.h>
#import <WFChatClient/DMCCCompositeMessageContent.h>
#import <WFChatClient/DMCCLinkMessageContent.h>
#import <WFChatClient/DMCCKickoffGroupMemberVisibleNotificationContent.h>
#import <WFChatClient/DMCCQuitGroupVisibleNotificationContent.h>
#import <WFChatClient/DMCCModifyGroupMemberExtraNotificationContent.h>
#import <WFChatClient/DMCCModifyGroupExtraNotificationContent.h>
#import <WFChatClient/DMCCPTTInviteMessageContent.h>
#import <WFChatClient/DMCCConversation.h>
#import <WFChatClient/DMCCConversationInfo.h>
#import <WFChatClient/DMCCConversationSearchInfo.h>
#import <WFChatClient/DMCCGroupSearchInfo.h>
#import <WFChatClient/DMCCFriendRequest.h>
#import <WFChatClient/DMCCFriend.h>
#import <WFChatClient/DMCCGroupInfo.h>
#import <WFChatClient/DMCCGroupMember.h>
#import <WFChatClient/DMCCUserInfo.h>
#import <WFChatClient/DMCCChatroomInfo.h>
#import <WFChatClient/DMCCUnreadCount.h>
#import <WFChatClient/DMCCUtilities.h>
#import <WFChatClient/DMCCPCOnlineInfo.h>
#import <WFChatClient/DMCCDeliveryReport.h>
#import <WFChatClient/DMCCReadReport.h>
#import <WFChatClient/DMCCFileRecord.h>
#import <WFChatClient/DMCCQuoteInfo.h>
#import <WFChatClient/DMCCEnums.h>
#import <WFChatClient/DMCCRedPacketInfo.h>
#import <WFChatClient/DMCCUnpackInfo.h>

