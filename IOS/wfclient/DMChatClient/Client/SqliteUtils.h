
#ifndef SqliteUtils_h
#define SqliteUtils_h

#import "Common.h"
#import "../Model/DMCCChannelInfo.h"
#import "../Model/DMCCChatroomInfo.h"
#import "../Model/DMCCChatroomMemberInfo.h"
#import "../Model/DMCCConversation.h"
#import "../Model/DMCCConversationInfo.h"
#import "../Model/DMCCConversationSearchInfo.h"
#import "../Model/DMCCDeliveryReport.h"
#import "../Model/DMCCFileRecord.h"
#import "../Model/DMCCFriend.h"
#import "../Model/DMCCFriendRequest.h"
#import "../Model/DMCCGroupInfo.h"
#import "../Model/DMCCGroupMember.h"
#import "../Model/DMCCGroupSearchInfo.h"
#import "../Model/DMCCPCOnlineInfo.h"
#import "../Model/DMCCQuoteInfo.h"
#import "../Model/DMCCReadReport.h"
#import "../Model/DMCCUnreadCount.h"
#import "../Model/DMCCUserInfo.h"
#import "../Model/DMCCLitappInfo.h"
#import "../Model/DMCCRedPacketInfo.h"
#import "../Model/DMCCUnpackInfo.h"
#import "../Model/DMCCTagInfo.h"
#import "../Model/DMCCWalletInfo.h"
#import "../Messages/DMCCAddGroupeMemberNotificationContent.h"
#import "../Messages/DMCCCallStartMessageContent.h"
#import "../Messages/DMCCCardMessageContent.h"
#import "../Messages/DMCCChangeGroupNameNotificationContent.h"
#import "../Messages/DMCCChangeGroupPortraitNotificationContent.h"
#import "../Messages/DMCCCompositeMessageContent.h"
#import "../Messages/DMCCConferenceInviteMessageContent.h"
#import "../Messages/DMCCCreateGroupNotificationContent.h"
#import "../Messages/DMCCDeleteMessageContent.h"
#import "../Messages/DMCCDismissGroupNotificationContent.h"
#import "../Messages/DMCCFileMessageContent.h"
#import "../Messages/DMCCFriendAddedMessageContent.h"
#import "../Messages/DMCCFriendGreetingMessageContent.h"
#import "../Messages/DMCCGroupJoinTypeNotificationContent.h"
#import "../Messages/DMCCGroupMemberAllowNotificationContent.h"
#import "../Messages/DMCCGroupMemberMuteNotificationContent.h"
#import "../Messages/DMCCGroupMuteNotificationContent.h"
#import "../Messages/DMCCGroupPrivateChatNotificationContent.h"
#import "../Messages/DMCCGroupSetManagerNotificationContent.h"
#import "../Messages/DMCCImageMessageContent.h"
#import "../Messages/DMCCKickoffGroupMemberNotificationContent.h"
#import "../Messages/DMCCKickoffGroupMemberVisiableNotificationContent.h"
#import "../Messages/DMCCLinkMessageContent.h"
#import "../Messages/DMCCLocationMessageContent.h"
#import "../Messages/DMCCMediaMessageContent.h"
#import "../Messages/DMCCMessage.h"
#import "../Messages/DMCCMessageContent.h"
#import "../Messages/DMCCModifyGroupAliasNotificationContent.h"
#import "../Messages/DMCCModifyGroupExtraNotificationContent.h"
#import "../Messages/DMCCModifyGroupMemberExtraNotificationContent.h"
#import "../Messages/DMCCNotificationMessageContent.h"
#import "../Messages/DMCCPCLoginRequestMessageContent.h"
#import "../Messages/DMCCPTTInviteMessageContent.h"
#import "../Messages/DMCCPTextMessageContent.h"
#import "../Messages/DMCCQuitGroupNotificationContent.h"
#import "../Messages/DMCCGroupNotifyContent.h"
#import "../Messages/DMCCQuitGroupVisibleNotificationContent.h"
#import "../Messages/DMCCRecallMessageContent.h"
#import "../Messages/DMCCSoundMessageContent.h"
#import "../Messages/DMCCStickerMessageContent.h"
#import "../Messages/DMCCTextMessageContent.h"
#import "../Messages/DMCCTipNotificationMessageContent.h"
#import "../Messages/DMCCTransferGroupOwnerNotificationContent.h"
#import "../Messages/DMCCTypingMessageContent.h"
#import "../Messages/DMCCUnknownMessageContent.h"
#import "../Messages/DMCCVideoMessageContent.h"
#import "../Messages/DMCCRedPacketMessageContent.h"
#import "../Messages/DMCCCallMessageContent.h"
#import <osnsdk/osnutils.h>

@interface SqliteUtils : NSObject
+ (void) initDB:(NSString*)path;
+ (void) closeDb;
+ (void) insertUser:(DMCCUserInfo*) userInfo;
+ (void) updateUser:(DMCCUserInfo*) userInfo;
+ (void) updateUser:(DMCCUserInfo*) userInfo keys:(NSArray<NSString*>*) keys;
+ (NSArray<DMCCUserInfo*>*) queryUsersWithTagID:(NSInteger)tagID;
+ (DMCCUserInfo*) queryUser:(NSString*) userID;
+ (NSArray<DMCCUserInfo*>*) queryUsers:(NSString*) keyword;

+ (void) insertFriendRequest:(DMCCFriendRequest*) friendRequest;
+ (void) updateFriendRequests:(NSArray<DMCCFriendRequest*>*) friendRequestList;
+ (DMCCFriendRequest*) queryFriendRequest:(NSString*) userID;
+ (DMCCFriendRequest*) queryFriendRequest:(NSString*) userID groupID:(NSString*) groupID;
+ (NSArray<DMCCFriendRequest*>*) listFriendRequest;
+ (NSArray<DMCCFriendRequest*>*) queryUnreadFriendRequest;
+ (void) insertFriend:(OsnFriendInfo*) friendInfo;
+ (void) deleteFriend:(NSString*) friendID;
+ (void) updateFriend:(OsnFriendInfo*) friendInfo;
+ (void) updateFriend:(OsnFriendInfo*) friendInfo keys:(NSArray<NSString*>*) keys;
+ (NSArray<NSString*>*) listFriends;
+ (NSArray<NSString*>*) listFriends:(int) state;
+ (OsnFriendInfo*) queryFriend:(NSString*) friendID;

+ (void) insertConversation:(int) type target:(NSString*) target line:(int) line;
+ (void) deleteConversation:(int) type target:(NSString*) target line:(int) line;
+ (DMCCConversationInfo*) queryConversation:(int) type target:(NSString*) target line:(int) line;
+ (NSArray*) queryConversation:(int) type;
+ (void) updateConversation:(DMCCConversationInfo*) conversationInfo;
+ (void) updateConversation:(DMCCConversationInfo*) conversationInfo keys:(NSArray<NSString*>*) keys;
+ (void) clearConversation:(int) type target:(NSString*) target line:(int) line;
+ (void) clearConversationUnread:(int) type target:(NSString*) target line:(int) line;
+ (NSArray<DMCCConversationInfo*>*) listConversations:(int) type line:(int) line;
+ (NSArray<DMCCConversationInfo*>*) listAllConversations:(NSArray<NSNumber*>*) types lines:(int) lines;
+ (NSArray<DMCCConversationInfo*>*) tagslistAllConversations:(NSArray<NSNumber*>*) types tags:(NSInteger)tagId lines:(int) lines;
+ (long) insertMessage:(DMCCMessage*) msg;
+ (void) deleteMessage:(long) mid;
+ (void) clearMessage:(NSString*) target;
+ (DMCCMessage*) queryMessage:(long) mid;
+ (DMCCMessage*) queryMessageWithHash:(NSString*) hash;
+ (DMCCMessage*) queryGroupMessageWithHash0:(NSString*) hash;
+ (bool) queryMessage:(long long) timestamp target:(NSString*) target;
+ (NSArray<DMCCMessage*>*) queryMessages:(DMCCConversation*) conversation timestamp:(long long) timestamp before:(bool) before count:(int) count include:(bool) include;
+ (NSArray<DMCCMessage*>*) queryMessages:(DMCCConversation*) conversation keyword:(NSString*) keyword desc:(bool) desc limit:(int) limit offset:(int) offset;
+ (void) updateMessage:(long) mid state:(int) state;
+ (void) recallMessage:(DMCCMessage*) message;
+ (void) updateHash:(DMCCMessage*) message;
+ (void) updateMessage:(long) mid state:(int) state msgHash:(NSString*) msgHash;
+ (DMCCMessage*) getLastMessage:(DMCCConversation*) conversation;
+ (DMCCMessage*) getLastNotify;

+ (void) insertGroup:(DMCCGroupInfo*) groupInfo;
+ (void) deleteGroup:(NSString*) groupID;
+ (void) updateGroup:(DMCCGroupInfo*) groupInfo keys:(NSArray<NSString*>*) keys;
+ (DMCCGroupInfo*) queryGroup:(NSString*) groupID;
+ (NSArray<DMCCGroupInfo*>*) listGroups;

+ (NSArray<DMCCLitappInfo*>*) listLitapps;
+ (void) insertLitapp:(DMCCLitappInfo*) litappInfo;
+ (DMCCLitappInfo*) queryLitapp:(NSString*) target;
+ (void) deleteLitapp:(NSString*) target;

+ (void) insertMembers:(NSArray<DMCCGroupMember*>*) members;
+ (void) insertMember:(DMCCGroupMember*) m;
+ (void) deleteMembers:(NSArray<OsnMemberInfo*>*) members;
+ (void) updateMember:(DMCCGroupMember*) groupMember keys:(NSArray<NSString*>*) keys;
+ (void) clearMembers:(NSString*) groupID;
+ (NSArray<DMCCGroupMember*>*) queryMembers:(NSString*) groupID;
+ (NSArray<DMCCGroupMember*>*) queryMembersTop:(NSString*) groupID;
+ (DMCCGroupMember*) queryMember:(NSString*) groupID memberID:(NSString*) memberID;
+ (int) getMaxMemberIndex:(NSString *) groupID;
+ (void) deleteMembers:(NSString *) groupId min:(int)min max:(int)max;
+ (void) deleteMembers:(NSString *) groupId index:(int)index;

+ (void) insertRedPacket:(DMCCRedPacketInfo*) redPacketInfo;
+ (DMCCRedPacketInfo*) queryRedPacket:(NSString*) packetID;
+ (void) updateRedPacketState:(NSString*) packetID;
+ (void) updateRedPacketContextState:(NSString*) packetID;
+ (void) insertUnpack:(DMCCUnpackInfo*) unpackInfo;
+ (NSArray<DMCCUnpackInfo*>*) queryUnpacks:(NSString*) unpackID;

+ (void) insertTag:(DMCCTagInfo*) tagInfo;
+ (DMCCTagInfo*) queryTagInfoRequest:(NSString*) tagName;
+ (DMCCTagInfo*) queryTagIDInfoRequest:(NSString*) tagID;
+ (NSArray<NSString*>*) listTagInfos;
+ (void) deleteTag:(DMCCTagInfo*) tagInfo;
+ (NSArray<DMCCGroupInfo*>*) listGroupsWithTagID:(NSInteger)tagId;
+ (NSArray*) queryConversationWithTagId:(int) tagId;
+ (void) updateMessage:(long) mid state:(int) state msgText:(NSString*) text;
+ (void) queryDelMessage:(long long) timestamp target:(NSString*) target;
+ (BOOL) isMessageExist:(NSString *) hash;
+ (NSArray<DMCCGroupInfo*>*) queryGroups:(NSString*) keyword;
+ (void) deleteFriendRequest:(NSString*) target;
+ (NSArray<NSString*>*) listWalletInfos;
+ (DMCCWalletInfo*) queryWalletInfoRequest:(NSString*) osnID;
+ (void) insertWallet:(DMCCWalletInfo*) walletInfo;
+ (void) insertCollect:(DMCCCollectInfo*) collectInfo;
+ (NSArray<NSString*>*) listCollectInfos;
+ (DMCCCollectInfo*) queryCollectInfoRequest:(NSString*) osnID;
+ (void) deleteCollect:(DMCCCollectInfo*) info;
+ (void)deleteWallet:(NSString*) osnID;

@end

#endif /* SqliteUtils_h */
