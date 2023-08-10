//
//  DMCCIMService.mm
//  DMChatClient
//
//  Created by heavyrain on 2017/11/5.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCIMService.h"
#import "DMCCMediaMessageContent.h"
#import <objc/runtime.h>
#import "DMCCNetworkService.h"
#import "DMCCGroupSearchInfo.h"
#import "DMCCUnknownMessageContent.h"
#import "DMCCRecallMessageContent.h"
#import "wav_amr.h"
#import "SqliteUtils.h"
#include <string>
#include <list>
#import <osnsdk/ZoloOssManager.h>

NSString *kSendingMessageStatusUpdated = @"kSendingMessageStatusUpdated";
NSString *kUploadMediaMessageProgresse = @"kUploadMediaMessageProgresse";
NSString *kConnectionStatusChanged = @"kConnectionStatusChanged";
NSString *kReceiveMessages = @"kReceiveMessages";
NSString *kRecallMessages = @"kRecallMessages";
NSString *kDeleteMessages = @"kDeleteMessages";
NSString *kMessageDelivered = @"kMessageDelivered";
NSString *kMessageReaded = @"kMessageReaded";
NSString *kMessageUpdated = @"kMessageUpdated";

static DMCCIMService * sharedSingleton = nil;

@interface DMCCIMService ()
@property(nonatomic, strong)NSMutableDictionary<NSNumber *, Class> *MessageContentMaps;
@property(nonatomic, assign)BOOL defaultSilentWhenPCOnline;
@end

@implementation DMCCIMService
+ (DMCCIMService *)sharedDMCIMService {
    if (sharedSingleton == nil) {
        @synchronized (self) {
            if (sharedSingleton == nil) {
                sharedSingleton = [[DMCCIMService alloc] init];
                sharedSingleton.MessageContentMaps = [[NSMutableDictionary alloc] init];
                sharedSingleton.defaultSilentWhenPCOnline = YES;
                sharedSingleton.osnsdk = [OsnSDK getInstance];
            }
        }
    }

    return sharedSingleton;
}

- (DMCCUserInfo*) toClientUser:(OsnUserInfo*) userInfo{
    DMCCUserInfo* u = [DMCCUserInfo new];
    u.userId = userInfo.userID;
    u.name = userInfo.name;
    u.displayName = userInfo.displayName;
    u.portrait = userInfo.portrait;
    u.urlSpace = userInfo.urlSpace;
    u.role = userInfo.role;
    u.describes = userInfo.describes;
    if(userInfo.describes != nil && userInfo.describes.length > 0){
        NSDictionary *json = [OsnUtils json2Dics:userInfo.describes];
        u.nft = json[@"nft"];
    }
    return u;
}

- (DMCCGroupMember*) toClientMember:(OsnMemberInfo*) memberInfo {
    DMCCGroupMember* m = [DMCCGroupMember new];
    m.groupId = memberInfo.groupID;
    m.memberId = memberInfo.osnID;
    if (memberInfo.type == MemberType_Normal)
        m.type = Member_Type_Normal;
    else if (memberInfo.type == MemberType_Owner)
        m.type = Member_Type_Owner;
    else if (memberInfo.type == MemberType_Admin)
        m.type = Member_Type_Manager;
    else
        m.type = Member_Type_Normal;
    m.alias = memberInfo.nickName;
    m.mute = memberInfo.mute;
    return m;
}
- (DMCCGroupInfo*) toClientGroup:(OsnGroupInfo*) groupInfo {
    DMCCGroupInfo* g = [DMCCGroupInfo new];
    g.target = groupInfo.groupID;
    g.name = groupInfo.name;
    g.portrait = groupInfo.portrait;
    g.owner = groupInfo.owner;
    g.type = (DMCCGroupType)groupInfo.type;
    g.joinType = groupInfo.joinType;
    g.passType = groupInfo.passType;
    g.mute = groupInfo.mute;
    g.memberCount = groupInfo.memberCount;
    g.isMember = groupInfo.isMember;
    g.notice = groupInfo.billboard == nil ? @"" : groupInfo.billboard;
    g.attribute = groupInfo.attribute;
    g.extra = groupInfo.extra;
    if(groupInfo.attribute != nil && groupInfo.attribute.length > 0){
        NSDictionary *json = [OsnUtils json2Dics:groupInfo.attribute];
        NSString *type = json[@"type"];
        if(type != nil && [type isEqualToString:@"bomb"])
            g.redPacket = 1;
    }
    return g;
}
- (DMCCLitappInfo*) toClientLitapp:(OsnLitappInfo*) osnLitappInfo {
    DMCCLitappInfo *litappInfo = [DMCCLitappInfo new];
    litappInfo.target = osnLitappInfo.target;
    litappInfo.name = osnLitappInfo.name;
    litappInfo.displayName = osnLitappInfo.displayName;
    litappInfo.portrait = osnLitappInfo.portrait;
    litappInfo.theme = osnLitappInfo.theme;
    litappInfo.url = osnLitappInfo.url;
    litappInfo.info = osnLitappInfo.info;
    return litappInfo;
}
- (void) orderPay:(NSString*) data code:(NSString*) code cb:(onResult)cb{
//    NSMutableDictionary* json = [NSMutableDictionary new];
//    json[@"data"] = data;
//    json[@"hash"] = [OsnUtils sha256s:((data+code).getBytes()));
//    osnsdk.orderPay(json.toString(), new OSNGeneralCallback() {
//        @Override
//        public void onSuccess(JSONObject data) {
//            try {
//                callback.onSuccess(data.toString());
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        }
//        @Override
//        public void onFailure(String error) {
//            try{
//                callback.onFailure(-1);
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        }
//    });
}
- (DMCCRedPacketInfo*) getRedPacket:(NSString*) packetID{
    return [SqliteUtils queryRedPacket:packetID];
}
- (void) getOwnerSign:(NSString*) groupID cb:(onResult)cb{
    [_osnsdk getOwnerSign:groupID cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        cb(isSuccess, json, error);
    }];
}
- (void) getGroupSign:(NSString*) groupID data:(NSString*) data cb:(onResult)cb{
    [_osnsdk getGroupSign:groupID info:data cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        cb(isSuccess, json, error);
    }];
}

- (void) allowAddFriend:(NSString*) groupID data:(int)type cb:(onResult)cb {
    [_osnsdk allowAddFriend:groupID type:type cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        cb(isSuccess, json, error);
    }];
}

- (void) timeIntervalWithGroupId:(NSString*) groupID data:(NSString *)value cb:(onResult)cb {
    [_osnsdk upAttirbuteWithGroupId:groupID key:@"TimeInterval" value:value cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        cb(isSuccess, json, error);
    }];
}

- (void)topMessageWithGroupId:(NSString*) groupID key:(NSString *)key data:(NSString *)value cb:(onResult)cb {
    [_osnsdk upAttirbuteWithGroupId:groupID key:key value:value cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        cb(isSuccess, json, error);
    }];
}

- (void) allowForwardWithGroupId:(NSString*) groupID data:(NSString *)value cb:(onResult)cb {
    [_osnsdk upAttirbuteWithGroupId:groupID key:@"isGroupForward" value:value cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        cb(isSuccess, json, error);
    }];
}

- (void) allowCopyWithGroupId:(NSString*) groupID data:(NSString *)value cb:(onResult)cb {
    [_osnsdk upAttirbuteWithGroupId:groupID key:@"isGroupCopy" value:value cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        cb(isSuccess, json, error);
    }];
}

- (void) addGroupJsonTypeWithGroupId:(NSString*) groupID data:(NSString *)value cb:(onResult)cb {
    [_osnsdk upExtraWithGroupId:groupID key:@"joinType" value:value cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        cb(isSuccess, json, error);
    }];
}

- (void) addGroupJsonPwdWithGroupId:(NSString*) groupID data:(NSString *)value cb:(onResult)cb {
    [_osnsdk upGroupPwdWithGroupId:groupID key:@"joinPwd" value:value cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        cb(isSuccess, json, error);
    }];
}

- (void) allowClearChatsWithGroupId:(NSString*) groupID data:(NSString *)value cb:(onResult)cb {
    [_osnsdk upAttirbuteWithGroupId:groupID key:@"clearTimes" value:value cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        cb(isSuccess, json, error);
    }];
}

- (void) removeTopChatsWithGroupId:(NSString*) groupID keyData:(NSString *)key cb:(onResult)cb {
    [_osnsdk removeAttirbuteWithGroupId:groupID key:key cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        cb(isSuccess, json, error);
    }];
}

- (void) removeTopChatsWithGroupId:(NSString*) groupID keysData:(NSArray *)keys cb:(onResult)cb {
    [_osnsdk removeAttirbuteWithGroupId:groupID keys:keys cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        cb(isSuccess, json, error);
    }];
}

- (void)addBillBoard:(NSString*) groupID data:(NSString *)text cb:(onResult)cb {
    [_osnsdk billboard:groupID text:text cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        cb(isSuccess, json, error);
    }];
}

- (void) openRedPacket:(NSString*) packetID unpackID:(NSString*) unpackID messageID:(long) messageID cb:(onResult)cb{
    DMCCMessage* message = [self getMessage:messageID];
    if (message != nil)
        [SqliteUtils updateMessage:messageID state:Message_Status_Opened];
    [SqliteUtils updateRedPacketState:packetID];
}

- (void) openRedPacket:(NSString*) packetID {
    [SqliteUtils updateRedPacketState:packetID];
}

- (void) fetchRedPacket:(NSString*) packetID unpackID:(NSString*) unpackID userID:(NSString*) userID cb:(onResult)cb{
//    NSMutableDictionary* json = [NSMutableDictionary new];
//    json[@"packetID"] = packetID;
//    json[@"unpackID"] = unpackID;
//    osnsdk.getRedPacket(json.toString(), userID, new OSNGeneralCallback() {
//        @Override
//        public void onSuccess(JSONObject data) {
//            try {
//                logInfo("fetch info: "+data);
//                SqliteUtils.updateRedPacketState(packetID);
//                UnpackInfo info = new UnpackInfo(data);
//                SqliteUtils.insertUnpack(info);
//                callback.onSuccess(data.toString());
//            }catch (Exception e){
//            }
//        }
//        @Override
//        public void onFailure(String error) {
//            try{
//                callback.onFailure(-1);
//            }catch (Exception e){
//            }
//        }
//    });
}
- (NSArray<DMCCUnpackInfo*>*) getUnpackList:(NSString*) unpackID{
    NSLog(@"query unpackID: %@",unpackID);
    return [SqliteUtils queryUnpacks:unpackID];
}
-(void)saveCallMessage:(DMCCMessage*)message{
    NSLog(@"saveCallMessage");
    [SqliteUtils insertMessage:message];
    [[NSNotificationCenter defaultCenter] postNotificationName:kReceiveMessages object:@[message]];
}
-(void)sendCallMessage:(DMCCMessage*)msg
               success:(void(^)())successBlock
                 error:(void(^)(int errorCode))errorBlock{
    DMCCCallMessageContent *call = (DMCCCallMessageContent*)msg.content;
    NSMutableDictionary* json = [NSMutableDictionary new];
    json[@"id"] = @(call.cid);
    json[@"type"] = @"call";
    json[@"callMode"] = @(call.mode);
    json[@"callType"] = @(call.type);
    json[@"callAction"] = @(call.action);
    json[@"url"] = call.url;
    json[@"user"] = call.user;
    json[@"urls"] = call.urls;
    json[@"users"] = call.users;
    json[@"voiceBaseUrl"] = call.voiceBaseUrl;
    json[@"voiceHostUrl"] = call.voiceHostUrl;
    [_osnsdk sendMessage:[OsnUtils dic2Json:json] userID:msg.conversation.target cb:^(bool isSuccess, NSDictionary *json, NSString *error) {
        if(isSuccess){
            successBlock();
        }else{
            errorBlock(-1);
        }
    }];
}

- (DMCCConversationInfo *)getMsgConversationInfo:(DMCCMessage *)message {
     return [SqliteUtils queryConversation:(int)message.conversation.type  target:message.conversation.target line:message.conversation.line];
}

- (DMCCMessage *)sendMsg:(DMCCConversation *)conversation
                 content:(DMCCMessageContent *)content
               timestamp:(long long)timestamp
                     pwd:(NSString *)pwd
                progress:(void(^)(long uploaded, long total))progressBlock
                 success:(void(^)(long long messageUd, long long timestamp))successBlock
                   error:(void(^)(int error_code))errorBlock {
    DMCCConversationInfo *conversationInfo = [SqliteUtils queryConversation:(int)conversation.type target:conversation.target line:conversation.line];
    if(conversationInfo == nil){
        [SqliteUtils insertConversation:(int)conversation.type target:conversation.target line:conversation.line];
        conversationInfo = [SqliteUtils queryConversation:(int)conversation.type target:conversation.target  line:conversation.line];
        [[DMCCNetworkService sharedInstance] onFriendListUpdated];
    }
    if(timestamp == 0)
        timestamp = [OsnUtils getTimeStamp];
    DMCCMessage* msg = [DMCCMessage new];
    msg.messageId = msg.messageUid = 0;
    msg.conversation = conversation;
    msg.fromUser = [[DMCCNetworkService sharedInstance] userId];
    msg.content = content;
    msg.direction = MessageDirection_Send;
    msg.status = Message_Status_Sending;
    msg.serverTime = timestamp;
    conversationInfo.timestamp = msg.serverTime;
    conversationInfo.lastMessage = msg;
    long mid = [SqliteUtils insertMessage:msg];
    msg.messageId = msg.messageUid = mid;

    [[DMCCNetworkService sharedInstance]onSendingMessage:msg];

    NSMutableDictionary* json = [NSMutableDictionary new];
    if([content isMemberOfClass:[DMCCTextMessageContent class]] ||
       [content isMemberOfClass:[DMCCCardMessageContent class]] ||
       [content isMemberOfClass:[DMCCRedPacketMessageContent class]] ||
        [content isMemberOfClass:[DMCCCallMessageContent class]]) {
        if ([content isMemberOfClass:[DMCCTextMessageContent class]]) {
            DMCCTextMessageContent* textMessageContent = (DMCCTextMessageContent*) content;
            json[@"type"] = @"text";
            json[@"data"] = textMessageContent.text;
            json[@"mentionedType"] = @(textMessageContent.mentionedType);
            json[@"mentionedTargets"] = textMessageContent.mentionedTargets;
            json[@"quoteInfo"] = [textMessageContent.quoteInfo encode];
        } else if ([content isMemberOfClass:[DMCCRedPacketMessageContent class]]){
            DMCCRedPacketMessageContent* redPacketMessageContent = (DMCCRedPacketMessageContent*)msg.content;
            json[@"type"] = @"redPacket";
            json[@"data"] = redPacketMessageContent.info;

            DMCCRedPacketInfo* redPacketInfo = [DMCCRedPacketInfo redPacketInfoWithJson:[OsnUtils json2Dics:redPacketMessageContent.info]];
            [SqliteUtils insertRedPacket:redPacketInfo];
 
        } else if ([content isMemberOfClass:[DMCCCardMessageContent class]]) {
            DMCCCardMessageContent* cardMessageContent = (DMCCCardMessageContent*) content;
            json[@"type"] = @"card";
            switch (cardMessageContent.type) {
                case CardType_User:
                    json[@"cardType"] = @"user";
                    break;
                case CardType_Group:
                    json[@"cardType"] = @"group";
                    break;
                case CardType_ChatRoom:
                    json[@"cardType"] = @"chatroom";
                    break;
                case CardType_Channel:
                    json[@"cardType"] = @"channel";
                    break;
                case CardType_Litapp:
                    json[@"cardType"] = @"litapp";
                    break;
                case CardType_Share:
                    json[@"cardType"] = @"share";
                    break;
            }
            json[@"target"] = cardMessageContent.targetId;
            json[@"name"] = cardMessageContent.name;
            json[@"displayName"] = cardMessageContent.displayName;
            json[@"portrait"] = cardMessageContent.portrait;
            json[@"theme"] = cardMessageContent.theme;
            json[@"url"] = cardMessageContent.url;
            json[@"info"] = cardMessageContent.info;
        } else if([content isMemberOfClass:[DMCCCallMessageContent class]]){
            DMCCCallMessageContent *callMessageContent = (DMCCCallMessageContent*)content;
            json[@"type"] = @"call";
            json[@"callMode"] = @(callMessageContent.mode);
            json[@"callType"] = @(callMessageContent.type);
            json[@"callAction"] = @(callMessageContent.action);
            json[@"url"] = callMessageContent.url;
        }
        [_osnsdk sendMessage:[OsnUtils dic2Json:json] userID:conversation.target cb:^(bool isSuccess, NSDictionary *json, NSString *error) {
            if(isSuccess){
                msg.status = Message_Status_Sent;
                [SqliteUtils updateMessage:mid state:Message_Status_Sent];
                [SqliteUtils updateMessage:mid state:Message_Status_Sent msgHash:json[@"msgHash"]];
                [SqliteUtils updateConversation:conversationInfo keys:@[@"timestamp"]];
                successBlock(mid, msg.serverTime);
            }else{
                msg.status = Message_Status_Send_Failure;
                [SqliteUtils updateMessage:mid state:Message_Status_Send_Failure];
                [SqliteUtils updateConversation:conversationInfo keys:@[@"timestamp"]];
                errorBlock(-1);
            }
            [[DMCCNetworkService sharedInstance]onSendingMessage:msg];
        }];
    } else {
        DMCCMediaMessageContent* mediaMessageContent = (DMCCMediaMessageContent*) content;
        if (mediaMessageContent.remoteUrl.length > 0) {
            OSSUploadType type = OSSUploadType_File;
            if([content isMemberOfClass:[DMCCFileMessageContent class]]){
                DMCCFileMessageContent* fileMessageContent = (DMCCFileMessageContent*) mediaMessageContent;
                json[@"type"] = @"file";
                json[@"name"] = fileMessageContent.name;
                json[@"localPath"] = fileMessageContent.localPath;
                json[@"remoteUrl"] = fileMessageContent.remoteUrl;
                json[@"size"] = @(fileMessageContent.size);
                json[@"url"] = fileMessageContent.remoteUrl;
                if (fileMessageContent.decKey.length > 0) {
                    json[@"decKey"] = fileMessageContent.decKey;
                }
                
            } else if([content isMemberOfClass:[DMCCImageMessageContent class]]){
                DMCCImageMessageContent* imageMessageContent = (DMCCImageMessageContent*) mediaMessageContent;
                json[@"type"] = @"image";
                json[@"name"] = imageMessageContent.name;
                json[@"width"] = @(imageMessageContent.size.width);
                json[@"height"] = @(imageMessageContent.size.height);
                type = OSSUploadType_Image;
                json[@"url"] = imageMessageContent.remoteUrl;
                if (imageMessageContent.decKey.length > 0) {
                    json[@"decKey"] = imageMessageContent.decKey;
                }
            } else if([content isMemberOfClass:[DMCCVideoMessageContent class]]){
                DMCCVideoMessageContent* videoMessageContent = (DMCCVideoMessageContent*) mediaMessageContent;
                json[@"type"] = @"video";
                json[@"name"] = videoMessageContent.name;
                json[@"duration"] = @(videoMessageContent.duration);
                type = OSSUploadType_Video;
                json[@"url"] = videoMessageContent.remoteUrl;
                if (videoMessageContent.decKey.length > 0) {
                    json[@"decKey"] = videoMessageContent.decKey;
                }
            } else if([content isMemberOfClass:[DMCCSoundMessageContent class]]){
                DMCCSoundMessageContent* soundMessageContent = (DMCCSoundMessageContent*) mediaMessageContent;
                json[@"type"] = @"voice";
                json[@"name"] = @"";
                json[@"duration"] = @(soundMessageContent.duration);
                type = OSSUploadType_Voice;
                json[@"url"] = soundMessageContent.remoteUrl;
                if (soundMessageContent.decKey.length > 0) {
                    json[@"decKey"] = soundMessageContent.decKey;
                }
            } else if([content isMemberOfClass:[DMCCStickerMessageContent class]]){
                DMCCStickerMessageContent* stickerMessageContent = (DMCCStickerMessageContent*) mediaMessageContent;
                json[@"type"] = @"sticker";
                json[@"name"] = @"";
                json[@"width"] = @(stickerMessageContent.size.width);
                json[@"height"] = @(stickerMessageContent.size.height);
                type = OSSUploadType_Gif;
                json[@"url"] = stickerMessageContent.remoteUrl;
            } else {
                NSLog(@"unknown type");
                return 0;
            }
            [self->_osnsdk sendMessage:[OsnUtils dic2Json:json] userID:conversation.target cb:^(bool isSuccess, NSDictionary *json, NSString *error){
                if(isSuccess){
                    msg.status = Message_Status_Sent;
                    [SqliteUtils updateMessage:mid state:Message_Status_Sent];
                    [SqliteUtils updateMessage:mid state:Message_Status_Sent msgHash:json[@"msgHash"]];
                    [SqliteUtils updateConversation:conversationInfo keys:@[@"timestamp"]];
                    successBlock(mid, conversationInfo.timestamp);
                } else {
                    msg.status = Message_Status_Send_Failure;
                    [SqliteUtils updateMessage:mid state:Message_Status_Send_Failure];
                    [SqliteUtils updateConversation:conversationInfo keys:@[@"timestamp"]];
                    errorBlock(-1);
                }
                [[DMCCNetworkService sharedInstance]onSendingMessage:msg];
            }];
            
        } else {
            NSRange idx = [mediaMessageContent.localPath rangeOfString:@"/" options:NSBackwardsSearch];
            NSString* name = idx.location == mediaMessageContent.localPath.length - 1
                    ? mediaMessageContent.localPath
                    : [mediaMessageContent.localPath substringFromIndex:idx.location+1];
            
            OSSUploadType type = OSSUploadType_File;
            if([content isMemberOfClass:[DMCCFileMessageContent class]]){
                DMCCFileMessageContent* fileMessageContent = (DMCCFileMessageContent*) mediaMessageContent;
                json[@"type"] = @"file";
                json[@"name"] = fileMessageContent.name;
                json[@"size"] = @(fileMessageContent.size);
                json[@"localPath"] = fileMessageContent.localPath;
                json[@"remoteUrl"] = fileMessageContent.remoteUrl;
                if ([fileMessageContent.localPath containsString:@"file:"]) {
                    type = OSSUploadType_CloudFile;
                } else {
                    type = OSSUploadType_File;
                }
                
            } else if([content isMemberOfClass:[DMCCImageMessageContent class]]){
                DMCCImageMessageContent* imageMessageContent = (DMCCImageMessageContent*) mediaMessageContent;
                json[@"type"] = @"image";
                json[@"name"] = name;
                json[@"width"] = @(imageMessageContent.size.width);
                json[@"height"] = @(imageMessageContent.size.height);
                type = OSSUploadType_Image;
            } else if([content isMemberOfClass:[DMCCVideoMessageContent class]]){
                DMCCVideoMessageContent* videoMessageContent = (DMCCVideoMessageContent*) mediaMessageContent;
                json[@"type"] = @"video";
                json[@"name"] = name;
                json[@"duration"] = @(videoMessageContent.duration);
                type = OSSUploadType_Video;
            } else if([content isMemberOfClass:[DMCCSoundMessageContent class]]){
                DMCCSoundMessageContent* soundMessageContent = (DMCCSoundMessageContent*) mediaMessageContent;
                json[@"type"] = @"voice";
                json[@"name"] = name;
                json[@"duration"] = @(soundMessageContent.duration);
                type = OSSUploadType_Voice;
            } else if([content isMemberOfClass:[DMCCStickerMessageContent class]]){
                DMCCStickerMessageContent* stickerMessageContent = (DMCCStickerMessageContent*) mediaMessageContent;
                json[@"type"] = @"sticker";
                json[@"name"] = name;
                json[@"width"] = @(stickerMessageContent.size.width);
                json[@"height"] = @(stickerMessageContent.size.height);
                type = OSSUploadType_Gif;
            } else {
                NSLog(@"unknown type");
                return 0;
            }
            NSMutableDictionary *dic = [NSMutableDictionary dictionaryWithDictionary:json];
            
            [[ZoloOssManager instanceManager] uploadDeskeyFileWithOssType:type WithName:mediaMessageContent.localPath password:pwd CompleteBlock:^(NSString * _Nonnull data, NSInteger size, NSString * _Nonnull pwd) {
                if (size == -1) {
                    [SqliteUtils deleteMessage:mid];
                    errorBlock(-1);
                }
                if (data.length > 0) {
                    if([content isMemberOfClass:[DMCCFileMessageContent class]]){
                        dic[@"size"] = @(size);
                        dic[@"remoteUrl"] = data;
                        [SqliteUtils updateMessage:mid state:MessageDirection_Send msgText:[OsnUtils dic2Json:dic]];
                    }
                    json[@"url"] = data;
                    json[@"size"] = @(size);
                    if (pwd.length > 0) {
                        json[@"decKey"] = pwd;
                    }
                    NSLog(@"url: %@",data);
                    [self->_osnsdk sendMessage:[OsnUtils dic2Json:json] userID:conversation.target cb:^(bool isSuccess, NSDictionary *json, NSString *error){
                        if(isSuccess){
                            msg.status = Message_Status_Sent;
                            [SqliteUtils updateMessage:mid state:Message_Status_Sent];
                            [SqliteUtils updateMessage:mid state:Message_Status_Sent msgHash:json[@"msgHash"]];
                            [SqliteUtils updateConversation:conversationInfo keys:@[@"timestamp"]];
                            successBlock(mid, conversationInfo.timestamp);
                        } else {
                            msg.status = Message_Status_Send_Failure;
                            [SqliteUtils updateMessage:mid state:Message_Status_Send_Failure];
                            [SqliteUtils updateConversation:conversationInfo keys:@[@"timestamp"]];
                            errorBlock(-1);
                        }
                        [[DMCCNetworkService sharedInstance]onSendingMessage:msg];
                    }];
                } else {
                    msg.status = Message_Status_Send_Failure;
                    [SqliteUtils updateMessage:mid state:Message_Status_Send_Failure];
                    [SqliteUtils updateConversation:conversationInfo keys:@[@"timestamp"]];
                    [[DMCCNetworkService sharedInstance]onSendingMessage:msg];
                    errorBlock(-1);
                }
            } ];
        }
    }
    return msg;
}
- (DMCCMessage *)send:(DMCCConversation *)conversation
              content:(DMCCMessageContent *)content
                  pwd:(NSString *)pwd
              success:(void(^)(long long messageUd, long long timestamp))successBlock
                error:(void(^)(int error_code))errorBlock {
    return [self sendMedia:conversation content:content expireDuration:0 pwd:(NSString *)pwd success:successBlock progress:nil error:errorBlock];
}

- (DMCCMessage *)sendMedia:(DMCCConversation *)conversation
                   content:(DMCCMessageContent *)content
                       pwd:(NSString *)pwd
                   success:(void(^)(long long messageUid, long long timestamp))successBlock
                  progress:(void(^)(long uploaded, long total))progressBlock
                     error:(void(^)(int error_code))errorBlock {
    return [self sendMedia:conversation content:content expireDuration:0 pwd:(NSString *)pwd success:successBlock progress:progressBlock error:errorBlock];
}

- (DMCCMessage *)send:(DMCCConversation *)conversation
              content:(DMCCMessageContent *)content
       expireDuration:(int)expireDuration
                  pwd:(NSString *)pwd
              success:(void(^)(long long messageUid, long long timestamp))successBlock
                error:(void(^)(int error_code))errorBlock {
    return [self sendMedia:conversation content:content expireDuration:0 pwd:(NSString *)pwd success:successBlock progress:nil error:errorBlock];
}

- (DMCCMessage *)send:(DMCCConversation *)conversation
              content:(DMCCMessageContent *)content
               toUsers:(NSArray<NSString *> *)toUsers
       expireDuration:(int)expireDuration
                  pwd:(NSString *)pwd
              success:(void(^)(long long messageUid, long long timestamp))successBlock
                error:(void(^)(int error_code))errorBlock {
    return [self sendMedia:conversation content:content toUsers:toUsers expireDuration:0 pwd:(NSString *)pwd success:successBlock progress:nil error:errorBlock];
}
- (DMCCMessage *)sendMedia:(DMCCConversation *)conversation
                   content:(DMCCMessageContent *)content
            expireDuration:(int)expireDuration
                       pwd:(NSString *)pwd
                   success:(void(^)(long long messageUid, long long timestamp))successBlock
                  progress:(void(^)(long uploaded, long total))progressBlock
                     error:(void(^)(int error_code))errorBlock {
    return [self sendMedia:conversation content:content toUsers:nil expireDuration:expireDuration pwd:(NSString*)pwd success:successBlock progress:progressBlock error:errorBlock];
}

- (DMCCMessage *)sendMedia:(DMCCConversation *)conversation
                   content:(DMCCMessageContent *)content
                   toUsers:(NSArray<NSString *>*)toUsers
            expireDuration:(int)expireDuration
                       pwd:(NSString *)pwd
                   success:(void(^)(long long messageUid, long long timestamp))successBlock
                  progress:(void(^)(long uploaded, long total))progressBlock
                     error:(void(^)(int error_code))errorBlock {
    if([content isMemberOfClass:[DMCCTypingMessageContent class]]){
        return nil;
    }
    return [self sendMsg:conversation content:content timestamp:0 pwd:pwd progress:progressBlock success:successBlock error:errorBlock];
}

- (BOOL)sendSavedMessage:(DMCCMessage *)message
          expireDuration:(int)expireDuration
                 success:(void(^)(long long messageUid, long long timestamp))successBlock
                   error:(void(^)(int error_code))errorBlock {
    return false;
}

- (void)recall:(DMCCMessage *)message
       toUsers:(NSString *)toUser
       success:(void(^)(void))successBlock
         error:(void(^)(int error_code))errorBlock {
    [_osnsdk recallMessage:message.messageHasho osnID:toUser cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        dispatch_async(dispatch_get_main_queue(), ^{
            if (isSuccess) {
                successBlock();
            } else {
                errorBlock(-1);
            }
        });
    }];
}
- (void)DeleteToFromMessage:(DMCCMessage *)message
                    toUsers:(NSString *)toUser
       success:(void(^)(void))successBlock
                      error:(void(^)(int error_code))errorBlock {
    [_osnsdk deleteToMessage:message.messageHasho osnID:toUser cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        dispatch_async(dispatch_get_main_queue(), ^{
            if (isSuccess) {
                successBlock();
            } else {
                errorBlock(-1);
            }
        });
    }];
}

- (NSArray<DMCCConversationInfo *> *)getConversationInfos:(NSArray<NSNumber *> *)conversationTypes lines:(NSArray<NSNumber *> *)lines{
    NSArray<DMCCConversationInfo*>* infoList = [[SqliteUtils listAllConversations:conversationTypes lines:lines[0].intValue]mutableCopy];
    for(DMCCConversationInfo *info in infoList){
        if (info.conversation.type == Notify_Type) {
            info.lastMessage = [SqliteUtils getLastNotify];
            break;
        }
    }
    NSMutableArray<DMCCConversationInfo*> *outList = [NSMutableArray new];
    for(DMCCConversationInfo *info in infoList){
        if (info.unreadCount.unread != 0) {
            [outList addObject:info];
        }
    }
    for(DMCCConversationInfo *info in infoList){
        if (info.unreadCount.unread == 0) {
            [outList addObject:info];
        }
    }
    NSLog(@"size: %ld",outList.count);
    return outList;
}

- (NSArray<DMCCConversationInfo *> *)getConversationInfoTags:(NSArray<NSNumber *> *)conversationTypes lines:(NSArray<NSNumber *> *)lines tags:(NSInteger)tagID{
    NSArray<DMCCConversationInfo*>* infoList = [[SqliteUtils tagslistAllConversations:conversationTypes tags:tagID lines:lines[0].intValue] mutableCopy];
    for(DMCCConversationInfo *info in infoList){
        if (info.conversation.type == Notify_Type) {
            info.lastMessage = [SqliteUtils getLastNotify];
            break;
        }
    }
    NSMutableArray<DMCCConversationInfo*> *outList = [NSMutableArray new];
    for(DMCCConversationInfo *info in infoList){
        if (info.unreadCount.unread != 0) {
            [outList addObject:info];
        }
    }
    for(DMCCConversationInfo *info in infoList){
        if (info.unreadCount.unread == 0) {
            [outList addObject:info];
        }
    }
    NSLog(@"size: %ld",outList.count);
    return outList;
}

- (DMCCConversationInfo *)getConversationInfo:(DMCCConversation *)conversation {
    //NSLog(@"getConversationInfo");
    return [SqliteUtils queryConversation:(int)conversation.type target:conversation.target line:conversation.line];
}
- (NSArray<DMCCMessage *> *)getMessages:(DMCCConversation *)conversation
                           contentTypes:(NSArray<NSNumber *> *)contentTypes
                                   from:(NSUInteger)fromIndex
                                  count:(NSInteger)count
                               withUser:(NSString *)user {
    NSLog(@"fromIndex: %ld, count: %ld, withUser: %@", fromIndex, count, user);
    DMCCMessage* msg = fromIndex == 0 ? [SqliteUtils getLastMessage:conversation] : [SqliteUtils queryMessage:fromIndex];
    if (msg == nil)
        return [NSArray new];
    return [SqliteUtils queryMessages:conversation timestamp:msg.serverTime before:true count:(int)count include:fromIndex == 0];
}

- (NSArray<DMCCMessage *> *)getMessagesBefore:(DMCCConversation *)conversation
                           contentTypes:(NSArray<NSNumber *> *)contentTypes
                                   from:(NSUInteger)fromIndex
                                  count:(NSInteger)count
                               withUser:(NSString *)user {
    NSLog(@"fromIndex: %ld, count: %ld, withUser: %@", fromIndex, count, user);
    DMCCMessage* msg = fromIndex == 0 ? [SqliteUtils getLastMessage:conversation] : [SqliteUtils queryMessage:fromIndex];
    if (msg == nil)
        return [NSArray new];
    return [SqliteUtils queryMessages:conversation timestamp:msg.serverTime before:false count:(int)count include:fromIndex == 0];
}

- (NSArray<DMCCMessage*>*)selectKeywordMessages:(DMCCConversation*)conversation keyword:(NSString*)keyword page:(int)index {
    return [SqliteUtils queryMessages:conversation keyword:keyword desc:NO limit:30 offset:index];
}

- (NSArray<DMCCMessage *> *)getMessages:(DMCCConversation *)conversation
                           contentTypes:(NSArray<NSNumber *> *)contentTypes
                               fromTime:(NSUInteger)fromTime
                                  count:(NSInteger)count
                               withUser:(NSString *)user {
    NSLog(@"getMessages fromTime");
    return nil;
}

- (NSArray<DMCCMessage *> *)getMessages:(DMCCConversation *)conversation
                          messageStatus:(NSArray<NSNumber *> *)messageStatus
                                   from:(NSUInteger)fromIndex
                                  count:(NSInteger)count
                               withUser:(NSString *)user {
    NSLog(@"getMessages status");
    return nil;
}

- (NSArray<DMCCMessage *> *)getMessages:(NSArray<NSNumber *> *)conversationTypes
                                           lines:(NSArray<NSNumber *> *)lines
                                    contentTypes:(NSArray<NSNumber *> *)contentTypes
                                            from:(NSUInteger)fromIndex
                                           count:(NSInteger)count
                                        withUser:(NSString *)user {
    NSLog(@"getMessages lines");
    return nil;
}

- (NSArray<DMCCMessage *> *)getMessages:(NSArray<NSNumber *> *)conversationTypes
                                           lines:(NSArray<NSNumber *> *)lines
                                   messageStatus:(NSArray<NSNumber *> *)messageStatus
                                            from:(NSUInteger)fromIndex
                                           count:(NSInteger)count
                                        withUser:(NSString *)user {
    NSLog(@"getMessages status lines");
    return nil;
}

- (NSArray<DMCCMessage *> *)getUserMessages:(NSString *)userId
                               conversation:(DMCCConversation *)conversation
                               contentTypes:(NSArray<NSNumber *> *)contentTypes
                                       from:(NSUInteger)fromIndex
                                      count:(NSInteger)count {
    NSLog(@"getUserMessages");
    return nil;
}

- (NSArray<DMCCMessage *> *)getUserMessages:(NSString *)userId
                          conversationTypes:(NSArray<NSNumber *> *)conversationTypes
                                      lines:(NSArray<NSNumber *> *)lines
                               contentTypes:(NSArray<NSNumber *> *)contentTypes
                                       from:(NSUInteger)fromIndex
                                      count:(NSInteger)count {
    NSLog(@"getUserMessages line");
    return nil;
}

- (void)getRemoteMessages:(DMCCConversation *)conversation
                   before:(long long)beforeMessageUid
                    count:(NSUInteger)count
                  success:(void(^)(NSArray<DMCCMessage *> *messages))successBlock
                    error:(void(^)(int error_code))errorBlock {
    NSLog(@"beforeMID: %lld, count: %ld",beforeMessageUid,count);
    successBlock([NSArray new]);
//    DMCCMessage* message = [SqliteUtils queryMessage:beforeMessageUid];
//    long timestamp = message == nil ? [OsnUtils getTimeStamp] : message.serverTime;
//    [_osnsdk loadMessage:conversation.target timestamp:timestamp count:(int)count before:true cb:^(bool isSuccess, id t, NSString *error) {
//        if(isSuccess){
//            NSArray<OsnMessageInfo*>*osnMsgs = (NSArray<OsnMessageInfo*>*)t;
//            NSArray<DMCCMessage*>* messages = [[DMCCNetworkService sharedInstance]recvMessage:osnMsgs history:false];
//            successBlock(messages);
//        } else {
//            errorBlock(-1);
//        }
//    }];
}

- (DMCCMessage *)getMessage:(long)messageId {
    return [SqliteUtils queryMessage:messageId];
}

- (DMCCMessage *)getMessageByUid:(long long)messageUid {
    return [SqliteUtils queryMessage:messageUid];
}

- (DMCCMessage *)getMessageByHashO:(NSString *)messageHashO {
    return [SqliteUtils queryGroupMessageWithHash0:messageHashO];
}

- (DMCCUnreadCount *)getUnreadCount:(DMCCConversation *)conversation {
    NSLog(@"target: %@",conversation.target);
    DMCCConversationInfo* info = [SqliteUtils queryConversation:(int)conversation.type target:conversation.target line:conversation.line];
    if(info != nil)
        return info.unreadCount;
    return [DMCCUnreadCount new];
}

- (DMCCUnreadCount *)getUnreadCount:(NSArray<NSNumber *> *)conversationTypes lines:(NSArray<NSNumber *> *)lines {
    return nil;
}

- (NSInteger)getUnreadCountWithType:(int)type {
    NSInteger count = 0;
    NSArray *infoList = [SqliteUtils queryConversation:type];
    for (DMCCConversationInfo* info in infoList) {
        count += info.unreadCount.unread;
    }
    NSLog(@"type: %ld, count = %ld", type, count);
    return count;
}

- (NSInteger)getUnreadCountWithTagId:(int)tagId {
    NSInteger count = 0;
    NSArray *infoList = [SqliteUtils queryConversationWithTagId:tagId];
    for (DMCCConversationInfo* info in infoList) {
        if (!info.isSilent) {
            count += info.unreadCount.unread;
        }
    }
    NSLog(@"type: %ld, count = %ld", tagId, count);
    return count;
}

- (void)clearUnreadStatus:(DMCCConversation *)conversation {
    NSLog(@"clearUnreadStatus");
    [SqliteUtils clearConversationUnread:(int)conversation.type target:conversation.target line:conversation.line];
}

- (void)clearUnreadStatus:(NSArray<NSNumber *> *)conversationTypes
                    lines:(NSArray<NSNumber *> *)lines {
    NSLog(@"clearUnreadStatus lines");
}
- (void)clearAllUnreadStatus {
    NSLog(@"clearAllUnreadStatus");
}

- (void)clearMessageUnreadStatus:(long)messageId {
    NSLog(@"clearMessageUnreadStatus:(long)messageId");
}

- (void)setMediaMessagePlayed:(long)messageId {
    NSLog(@"setMediaMessagePlayed %ld", messageId);
    DMCCMessage *message = [self getMessage:messageId];
    if (!message) {
        return;
    }
    [SqliteUtils updateMessage:messageId state:Message_Status_Played];
}

- (BOOL)setMessage:(long)messageId localExtra:(NSString *)extra {
    NSLog(@"setMessage id: %ld, extra: %@", messageId, extra);
    return false;
}

- (NSMutableDictionary<NSString *, NSNumber *> *)getConversationRead:(DMCCConversation *)conversation {
    NSLog(@"getConversationRead");
    return [NSMutableDictionary new];
}

- (NSMutableDictionary<NSString *, NSNumber *> *)getMessageDelivery:(DMCCConversation *)conversation {
    NSLog(@"getMessageDelivery");
    return nil;
}

- (long long)getMessageDeliveryByUser:(NSString *)userId {
    NSLog(@"getMessageDeliveryByUser");
    if(!userId) {
        return 0;
    }
    return 0;
}

- (BOOL)updateMessage:(long)messageId status:(DMCCMessageStatus)status {
    NSLog(@"updateMessage status: %ld",status);
    [SqliteUtils updateMessage:messageId state:(int)status];
    return YES;
}

- (void)removeConversation:(DMCCConversation *)conversation clearMessage:(BOOL)clearMessage {
    NSLog(@"removeConversation target: %@, clear: %d", conversation.target, clearMessage);
    [SqliteUtils deleteConversation:(int)conversation.type target:conversation.target line:conversation.line];
}

- (void)clearMessages:(DMCCConversation *)conversation {
    NSLog(@"clearMessages target: %@",conversation.target);
    [SqliteUtils clearMessage:conversation.target];
}

- (void)clearMessages:(DMCCConversation *)conversation before:(int64_t)before {
    NSLog(@"clearMessages target: %@, before:%lld",conversation.target, before);
    [SqliteUtils clearMessage:conversation.target];
}

- (void)delMessages:(long long)time target:(NSString *)target {
    [SqliteUtils queryDelMessage:time target:target];
}

- (void)setConversation:(DMCCConversation *)conversation top:(BOOL)top
                success:(void(^)(void))successBlock
                  error:(void(^)(int error_code))errorBlock {
    NSLog(@"setConversation top: %d",top);
    DMCCConversationInfo* info = [SqliteUtils queryConversation:(int)conversation.type target:conversation.target line:conversation.line];
    info.isTop = top;
    [SqliteUtils updateConversation:info keys:@[@"top"]];
    successBlock();
}

- (void)setConversation:(DMCCConversation *)conversation draft:(NSString *)draft {
    NSLog(@"setConversation draft: %@",draft);
    DMCCConversationInfo* info = [SqliteUtils queryConversation:(int)conversation.type target:conversation.target line:conversation.line];
    info.draft = draft;
    [SqliteUtils updateConversation:info keys:@[@"draft"]];
}

- (void)setConversation:(DMCCConversation *)conversation
              timestamp:(long long)timestamp {
    NSLog(@"setConversation timestamp: %lld",timestamp);
    DMCCConversationInfo* info = [SqliteUtils queryConversation:(int)conversation.type target:conversation.target line:conversation.line];
    info.timestamp = timestamp;
    [SqliteUtils updateConversation:info keys:@[@"timestamp"]];
}

- (void)setConversation:(DMCCConversation *)conversation
              tagID:(NSInteger)tagID {
    NSLog(@"setConversation tagID: %ld",tagID);
    DMCCConversationInfo* info = [SqliteUtils queryConversation:(int)conversation.type target:conversation.target line:conversation.line];
    info.tagID = tagID;
    [SqliteUtils updateConversation:info keys:@[@"tagID"]];
}

- (long)getFirstUnreadMessageId:(DMCCConversation *)conversation {
    NSLog(@"getFirstUnreadMessageId");
    return 0;
}

- (void)clearRemoteConversationMessage:(DMCCConversation *)conversation
                               success:(void(^)(void))successBlock
                                 error:(void(^)(int error_code))errorBlock {
    NSLog(@"clearRemoteConversationMessage");
}

- (void)searchUser:(NSString *)keyword
        searchType:(DMCCSearchUserType)searchType
              page:(int)page
           success:(void(^)(NSArray<DMCCUserInfo *> *machedUsers))successBlock
             error:(void(^)(int errorCode))errorBlock {
    
    if(keyword.length == 0) {
        successBlock(@[]);
    }
    
    if (self.userSource) {
        [self.userSource searchUser:keyword searchType:searchType page:page success:successBlock error:errorBlock];
        return;
    }
    
    NSArray<DMCCUserInfo *> *machedUsers = [SqliteUtils queryUsers:keyword];
    successBlock(machedUsers);
}

- (void)searchGroup:(NSString *)keyword
              page:(int)page
           success:(void(^)(NSArray<DMCCGroupInfo *> *machedGroups))successBlock
             error:(void(^)(int errorCode))errorBlock {
    
    if(keyword.length == 0) {
        successBlock(@[]);
    }
    
    NSArray<DMCCGroupInfo *> *machedGroups = [SqliteUtils queryGroups:keyword];
    successBlock(machedGroups);
}

- (void)getUserInfo:(NSString *)userId
            refresh:(BOOL)refresh
            success:(void(^)(DMCCUserInfo *userInfo))successBlock
              error:(void(^)(int errorCode))errorBlock {
    if (!userId.length) {
        return;
    }
    
//    if ([self.userSource respondsToSelector:@selector(getUserInfo:refresh:success:error:)]) {
//        [self.userSource getUserInfo:userId refresh:refresh success:successBlock error:errorBlock];
//        return;
//    }
    __block DMCCUserInfo* info = [SqliteUtils queryUser:userId];
    if(info == nil || refresh){
        [_osnsdk getUserInfo:userId cb:^(bool isSuccess, id t, NSString *error) {
            if(isSuccess){
                OsnUserInfo* ui = (OsnUserInfo*)t;
                info = [self toClientUser:ui];
                [SqliteUtils insertUser:info];
                successBlock(info);
            } else {
                errorBlock(-1);
            }
        }];
    }else if(info != nil){
        successBlock(info);
    }
}

- (BOOL)isMyFriend:(NSString *)userId {
    if(!userId)
        return NO;
    return [SqliteUtils queryFriend:userId] != nil;
}

- (NSArray<NSString *> *)getMyFriendList:(BOOL)refresh {
    NSLog(@"getMyFriendList");
    return [SqliteUtils listFriends];
}

- (NSArray<DMCCFriend *> *)getFriendList:(BOOL)refresh {
    NSLog(@"getFriendList");
    if (refresh) {
        [_osnsdk syncFriend];
    }
    NSMutableArray<DMCCFriend *>* outList = [NSMutableArray new];
    NSArray<NSString*>* friendList = [SqliteUtils listFriends];
    for(NSString* friends in friendList){
        OsnFriendInfo* info = [SqliteUtils queryFriend:friends];
        DMCCFriend* wfriend = [DMCCFriend new];
        wfriend.userId = info.userID;
        wfriend.alias = info.remarks;
        wfriend.extra = info.remarks;
        wfriend.timestamp = 0;
        [outList addObject:wfriend];
    }
    return outList;

}

- (NSArray<DMCCUserInfo *> *)searchFriends:(NSString *)keyword {
    if(!keyword)
        return nil;
    NSLog(@"searchFriends keyword: %@",keyword);
    NSMutableArray<DMCCUserInfo*>* friendInfos = [NSMutableArray new];
    NSArray<NSString*>* friends = [SqliteUtils listFriends];
    for(NSString *f in friends){
        DMCCUserInfo* info = [SqliteUtils queryUser:f];
        if(info != nil && [info.displayName containsString:keyword]){
            [friendInfos addObject:info];
        }
    }
    return friendInfos;
}
- (NSArray<DMCCGroupSearchInfo *> *)searchGroups:(NSString *)keyword {
    if(!keyword)
        return nil;
    return @[];
}


- (void)loadFriendRequestFromRemote {
    NSLog(@"loadFriendRequestFromRemote");
}

- (NSArray<DMCCFriendRequest *> *)getIncommingFriendRequest {
    return [SqliteUtils listFriendRequest];
}

- (NSArray<DMCCFriendRequest *> *)getOutgoingFriendRequest {
    return nil;
}

- (DMCCFriendRequest *)getFriendRequest:(NSString *)userId direction:(int)direction {
    if(!userId)
        return nil;
    return [SqliteUtils queryFriendRequest:userId];
}

- (void)clearUnreadFriendRequestStatus {
    NSArray<DMCCFriendRequest*>* requests = [SqliteUtils queryUnreadFriendRequest];
    for(DMCCFriendRequest* r in requests){
        r.readStatus = 1;
    }
    [SqliteUtils updateFriendRequests:requests];
}

- (int)getUnreadFriendRequestStatus {
    NSArray<DMCCFriendRequest*>* requests = [SqliteUtils queryUnreadFriendRequest];
    return requests == nil ? 0 : (int)requests.count;
}

- (void)sendFriendRequest:(NSString *)userId
                   reason:(NSString *)reason
                    extra:(NSString *)extra
                  success:(void(^)())successBlock
                    error:(void(^)(int error_code))errorBlock {
    [_osnsdk inviteFriend:userId reason:reason cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        if(isSuccess){
            successBlock();
        }else{
            errorBlock(-1);
        }
    }];
}


- (void)handleFriendRequest:(NSString *)userId
                     accept:(BOOL)accpet
                      extra:(NSString *)extra
                    success:(void(^)())successBlock
                      error:(void(^)(int error_code))errorBlock {
    if(!userId) {
        if(errorBlock) {
            errorBlock(-1);
        }
        return;
    }
    NSLog(@"userId: %@, accept: %d", userId, accpet);
    DMCCFriendRequest* request = [SqliteUtils queryFriendRequest:userId];
    request.status = accpet;
    [SqliteUtils updateFriendRequests:@[request]];
    if(accpet){
        [_osnsdk acceptFriend:userId cb:^(bool isSuccess, NSDictionary *json, NSString *error) {
            if(isSuccess)
                successBlock();
            else
                errorBlock(-1);
        }];
    } else {
        [_osnsdk rejectFriend:userId cb:^(bool isSuccess, NSDictionary *json, NSString *error) {
            if(isSuccess)
                successBlock();
            else
                errorBlock(-1);
        }];
    }
}

- (void)handleFriendAddGroupRequest:(NSString *)userId
                     accept:(BOOL)accpet
                             tagart:(NSString *)target
                      extra:(NSString *)extra
                    success:(void(^)())successBlock
                      error:(void(^)(int error_code))errorBlock {
    if(!userId) {
        if(errorBlock) {
            errorBlock(-1);
        }
        return;
    }
    NSLog(@"userId: %@, accept: %d", userId, accpet);
    DMCCFriendRequest* request = [SqliteUtils queryFriendRequest:target];
    request.status = accpet;
    [SqliteUtils updateFriendRequests:@[request]];
    if(accpet){
        [_osnsdk acceptMember:userId groupID:target cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
            if(isSuccess)
                successBlock();
            else
                errorBlock(-1);
        }];
    } else {
        [_osnsdk rejectMember:userId groupID:target cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
            if(isSuccess)
                successBlock();
            else
                errorBlock(-1);
        }];
    }
}


- (void)deleteFriend:(NSString *)userId
             success:(void(^)())successBlock
               error:(void(^)(int error_code))errorBlock {
    if(!userId) {
        if(errorBlock) {
            errorBlock(-1);
        }
        return;
    }
    [_osnsdk deleteFriend:userId cb:^(bool isSuccess, NSDictionary *json, NSString *error) {
        if(isSuccess) {
            [SqliteUtils deleteFriend:userId];
            successBlock();
        }
        else
            errorBlock(-1);
    }];
}

- (NSString *)getFriendAlias:(NSString *)friendId {
    if(!friendId) {
        return nil;
    }
    OsnFriendInfo* info = [SqliteUtils queryFriend:friendId];
    if ([info.remarks isEqualToString:@"(null)"]) {
        info.remarks = @"";
    }
    return info == nil ? nil : info.remarks;
}

- (void)setFriend:(NSString *)friendId
            alias:(NSString *)alias
          success:(void(^)(void))successBlock
            error:(void(^)(int error_code))errorBlock {
    if(!friendId) {
        if(errorBlock) {
            errorBlock(-1);
        }
        return;
    }
    OsnFriendInfo* info = [OsnFriendInfo new];
    info.friendID = friendId;
    info.remarks = alias;
    [_osnsdk modifyFriendInfo:@[@"remarks"] info:info cb:^(bool isSuccess, NSDictionary *json, NSString *error) {
        if(isSuccess){
            [SqliteUtils updateFriend:info];
            successBlock();
        } else {
            errorBlock(-1);
        }
    }];
}

- (NSString *)getFriendExtra:(NSString *)friendId {
    if(!friendId)
        return nil;
    return nil;
}

- (BOOL)isBlackListed:(NSString *)userId {
    if(!userId)
        return NO;
    OsnFriendInfo* info = [SqliteUtils queryFriend:userId];
    if(info == nil){
        NSLog(@"isBlackListed no my friend: %@",userId);
        return YES;
    }
    NSLog(@"%d", info.state);
    return info.state == FriendState_Blacked;
}

- (NSArray<NSString *> *)getBlackList:(BOOL)refresh {
    return [SqliteUtils listFriends:FriendState_Blacked];
}

- (void)setBlackList:(NSString *)userId
       isBlackListed:(BOOL)isBlackListed
             success:(void(^)(void))successBlock
               error:(void(^)(int error_code))errorBlock {
    if(!userId) {
        if(errorBlock) {
            errorBlock(-1);
        }
        return;
    }
    OsnFriendInfo* info = [SqliteUtils queryFriend:userId];
    if(info == nil){
        NSLog(@"setBlackList no my friend: %@", userId);
        errorBlock(-1);
        return;
    }
    info.state = isBlackListed ? FriendState_Blacked : FriendState_Normal;
    [_osnsdk modifyFriendInfo:@[@"state"] info:info cb:^(bool isSuccess, NSDictionary *json, NSString *error) {
        dispatch_async(dispatch_get_main_queue(), ^{
            if(isSuccess){
                [SqliteUtils updateFriend:info keys:@[@"state"]];
                successBlock();
            } else {
                errorBlock(-1);
            }
        });
    }];
}
- (DMCCUserInfo *)getUserInfo:(NSString *)userId refresh:(BOOL)refresh {
    if (!userId) {
        return nil;
    }
    
    if ([self.userSource respondsToSelector:@selector(getUserInfo:refresh:)]) {
        return [self.userSource getUserInfo:userId refresh:refresh];
    }
    
    return [self getUserInfo:userId inGroup:nil refresh:refresh];
}

- (DMCCUserInfo *)getUserInfo:(NSString *)userId inGroup:(NSString *)groupId refresh:(BOOL)refresh {
    if (!userId) {
        return nil;
    }

    if ([self.userSource respondsToSelector:@selector(getUserInfo:inGroup:refresh:)]) {
        return [self.userSource getUserInfo:userId inGroup:groupId refresh:refresh];
    }
    
    NSLog(@"getUserInfo userID: %@, groupID: %@, refresh: %d", userId, groupId, refresh);
    __block DMCCUserInfo* info = [SqliteUtils queryUser:userId];
    if(info == nil || refresh){
        [_osnsdk getUserInfo:userId cb:^(bool isSuccess, id t, NSString *error) {
            if(isSuccess){
                OsnUserInfo *ui = (OsnUserInfo*)t;
                info = [self toClientUser:ui];
                [SqliteUtils insertUser:info];
                [[DMCCNetworkService sharedInstance]onUserInfoUpdated:@[info]];
            }
        }];
    }
    if(info == nil){
        info = [DMCCUserInfo new];
        info.userId = userId;
    }
    return info;
}

- (NSArray<DMCCUserInfo *> *)getUserInfos:(NSArray<NSString *> *)userIds inGroup:(NSString *)groupId {
    if ([userIds count] == 0) {
        return nil;
    }

    if ([self.userSource respondsToSelector:@selector(getUserInfos:inGroup:)]) {
        return [self.userSource getUserInfos:userIds inGroup:groupId];;
    }
    
    NSMutableArray<DMCCUserInfo*>* infos = [NSMutableArray new];
    for(int i = 0; i < userIds.count; ++i){
        DMCCUserInfo* ui = [self getUserInfo:userIds[i] inGroup:groupId refresh:false];
        [infos addObject:ui];
    }
    return infos;
}

- (NSArray<DMCCUserInfo *> *)getUserWithTagId:(NSInteger)tagId {
    return [SqliteUtils queryUsersWithTagID:tagId];
}

- (NSArray<DMCCGroupInfo *> *)getGroupWithTagId:(NSInteger)tagId {
    return [SqliteUtils listGroupsWithTagID:tagId];
}

- (void)uploadMedia:(NSString *)fileName
          mediaData:(NSData *)mediaData
          mediaType:(DMCCMediaType)mediaType
            success:(void(^)(NSString *remoteUrl))successBlock
           progress:(void(^)(long uploaded, long total))progressBlock
              error:(void(^)(int error_code))errorBlock {
    NSLog(@"fileName: %@, mediaType: %ld", fileName, mediaType);
    
    NSString* type = mediaType == Media_Type_PORTRAIT ? @"portrait" : @"cache";
    
    [[ZoloOssManager instanceManager] uploadImageWithType:OSSUploadImageType_User WithName:mediaData CompleteBlock:^(NSString * _Nonnull str) {
        if (str.length > 0) {
            successBlock(str);
        } else {
            errorBlock(-1);
        }
    }];
    
//    [_osnsdk uploadData:fileName type:type data:mediaData cb:^(bool isSuccess, NSDictionary *json, NSString *error) {
//        if(isSuccess){
//            successBlock(json[@"url"]);
//        }else{
//            errorBlock(-1);
//        }
//    } progress:^(long progress, long total) {
//        progressBlock(progress, total);
//    }];
}

- (BOOL)syncUploadMedia:(NSString *)fileName
              mediaData:(NSData *)mediaData
              mediaType:(DMCCMediaType)mediaType
                success:(void(^)(NSString *remoteUrl))successBlock
            progress:(void(^)(long uploaded, long total))progressBlock
                  error:(void(^)(int error_code))errorBlock {
    NSLog(@"syncUploadMedia fileName: %@", fileName);
    NSCondition *condition = [[NSCondition alloc] init];
    __block BOOL success = NO;

    [condition lock];
    [[DMCCIMService sharedDMCIMService] uploadMedia:fileName mediaData:mediaData mediaType:mediaType success:^(NSString *remoteUrl) {
        successBlock(remoteUrl);
        
        success = YES;
        [condition lock];
        [condition signal];
        [condition unlock];
    } progress:^(long uploaded, long total) {
        progressBlock(uploaded, total);
    } error:^(int error_code) {
        errorBlock(error_code);
        success = NO;
        [condition lock];
        [condition signal];
        [condition unlock];
    }];
    
    [condition wait];
    [condition unlock];
    
    return success;
}

- (void)getUploadUrl:(NSString *)fileName
           mediaType:(DMCCMediaType)mediaType
            success:(void(^)(NSString *uploadUrl, NSString *downloadUrl, NSString *backupUploadUrl, int type))successBlock
               error:(void(^)(int error_code))errorBlock {
    NSLog(@"getUploadUrl fileName: %@",fileName);
}

- (BOOL)isSupportBigFilesUpload {
    return false;
}

-(void)modifyMyInfo:(NSDictionary<NSNumber */*ModifyMyInfoType*/, NSString *> *)values
            success:(void(^)())successBlock
              error:(void(^)(int error_code))errorBlock {
    NSLog(@"");
    NSMutableArray<NSString*>* keys = [NSMutableArray new];
    OsnUserInfo* info = [OsnUserInfo new];
    for(NSNumber* key in values){
        if(Modify_DisplayName == key.integerValue){
            [keys addObject:@"displayName"];
            info.displayName = values[key];
        } else if(Modify_Portrait == key.integerValue){
            [keys addObject:@"portrait"];
            info.portrait = values[key];
        } else if(Modify_UrlSpace == key.integerValue){
            [keys addObject:@"urlSpace"];
            info.urlSpace = values[key];
        }
    }
    [_osnsdk modifyUserInfo:keys info:info cb:^(bool isSuccess, NSDictionary *json, NSString *error) {
        dispatch_async(dispatch_get_main_queue(), ^{
            if(isSuccess)
                successBlock();
            else
                errorBlock(-1);
        });
    }];
}

- (BOOL)isGlobalSilent {
    NSString *strValue = [[DMCCIMService sharedDMCIMService] getUserSetting:UserSettingScope_Global_Silent key:@""];
    return [strValue isEqualToString:@"1"];
}

- (void)setGlobalSilent:(BOOL)silent
                success:(void(^)(void))successBlock
                  error:(void(^)(int error_code))errorBlock {
    [[DMCCIMService sharedDMCIMService] setUserSetting:UserSettingScope_Global_Silent key:@"" value:silent?@"1":@"0" success:^{
        if (successBlock) {
            successBlock();
        }
    } error:^(int error_code) {
        if (errorBlock) {
            errorBlock(error_code);
        }
    }];
}

- (BOOL)isVoipNotificationSilent {
    NSString *strValue = [[DMCCIMService sharedDMCIMService] getUserSetting:UserSettingScope_Voip_Silent key:@""];
    return [strValue isEqualToString:@"1"];
}

- (BOOL)isAllowTemporaryChat:(NSString *)userId {
    NSString *strValue = @"";
    DMCCUserInfo* userInfo = [SqliteUtils queryUser:userId];
    if(userInfo.describes != nil && userInfo.describes.length > 0){
        NSDictionary *json = [OsnUtils json2Dics:userInfo.describes];
        strValue = json[@"AllowTemporaryChat"];
    }
    return [strValue isEqualToString:@"yes"];
}

- (void)setVoipNotificationSilent:(BOOL)silent
                          success:(void(^)(void))successBlock
                            error:(void(^)(int error_code))errorBlock {
    [[DMCCIMService sharedDMCIMService] setUserSetting:UserSettingScope_Voip_Silent key:@"" value:silent?@"1":@"0" success:^{
        if (successBlock) {
            successBlock();
        }
    } error:^(int error_code) {
        if (errorBlock) {
            errorBlock(error_code);
        }
    }];
}
- (BOOL)isEnableSyncDraft {
    NSString *strValue = [[DMCCIMService sharedDMCIMService] getUserSetting:UserSettingScope_Disable_Sync_Draft key:@""];
    return ![strValue isEqualToString:@"1"];
}

- (void)setEnableSyncDraft:(BOOL)enable
                    success:(void(^)(void))successBlock
                      error:(void(^)(int error_code))errorBlock {
    [[DMCCIMService sharedDMCIMService] setUserSetting:UserSettingScope_Disable_Sync_Draft key:@"" value:enable?@"0":@"1" success:^{
        if (successBlock) {
            successBlock();
        }
    } error:^(int error_code) {
        if (errorBlock) {
            errorBlock(error_code);
        }
    }];
}

- (BOOL)isUserEnableReceipt {
    NSString *strValue = [[DMCCIMService sharedDMCIMService] getUserSetting:UserSettingScope_DisableRecipt key:@""];
    return ![strValue isEqualToString:@"1"];
}

- (void)setUserEnableReceipt:(BOOL)enable
                success:(void(^)(void))successBlock
                  error:(void(^)(int error_code))errorBlock {
    [[DMCCIMService sharedDMCIMService] setUserSetting:UserSettingScope_DisableRecipt key:@"" value:enable?@"0":@"1" success:^{
        if (successBlock) {
            successBlock();
        }
    } error:^(int error_code) {
        if (errorBlock) {
            errorBlock(error_code);
        }
    }];
}

- (void)getNoDisturbingTimes:(void(^)(int startMins, int endMins))resultBlock
                       error:(void(^)(int error_code))errorBlock {
    NSString *strValue = [[DMCCIMService sharedDMCIMService] getUserSetting:UserSettingScope_No_Disturbing key:@""];
    if (strValue.length) {
        NSArray<NSString *> *arrs = [strValue componentsSeparatedByString:@"|"];
        if (arrs.count == 2) {
            int startMins = [arrs[0] intValue];
            int endMins = [arrs[1] intValue];
            resultBlock(startMins, endMins);
        } else {
            if(errorBlock) {
                errorBlock(-1);
            }
        }
    } else {
        if(errorBlock) {
            errorBlock(-1);
        }
    }
}

- (void)setNoDisturbingTimes:(int)startMins
                     endMins:(int)endMins
                     success:(void(^)(void))successBlock
                       error:(void(^)(int error_code))errorBlock {
    [[DMCCIMService sharedDMCIMService] setUserSetting:UserSettingScope_No_Disturbing key:@"" value:[NSString stringWithFormat:@"%d|%d", startMins, endMins] success:successBlock error:errorBlock];
}

- (void)clearNoDisturbingTimes:(void(^)(void))successBlock
                         error:(void(^)(int error_code))errorBlock {
    [[DMCCIMService sharedDMCIMService] setUserSetting:UserSettingScope_No_Disturbing key:@"" value:@"" success:successBlock error:errorBlock];
}

- (BOOL)isNoDisturbing {
    __block BOOL isNoDisturbing = NO;
    [self getNoDisturbingTimes:^(int startMins, int endMins) {
        NSCalendar *calendar = [NSCalendar calendarWithIdentifier:NSCalendarIdentifierGregorian];
        NSDateComponents *nowCmps = [calendar components:NSCalendarUnitHour|NSCalendarUnitMinute fromDate:[NSDate date]];
        int nowMins = (int)(nowCmps.hour * 60 + nowCmps.minute);
        if (endMins > startMins) {
            if (endMins > nowMins && nowMins > startMins) {
                isNoDisturbing = YES;
            }
        } else {
            if (endMins > nowMins || nowMins > startMins) {
                isNoDisturbing = YES;
            }
        }
        
    } error:^(int error_code) {
        
    }];
    return isNoDisturbing;
}

- (BOOL)isHiddenNotificationDetail {
    NSString *strValue = [[DMCCIMService sharedDMCIMService] getUserSetting:UserSettingScope_Hidden_Notification_Detail key:@""];
    return [strValue isEqualToString:@"1"];
}

- (void)setHiddenNotificationDetail:(BOOL)hidden
                success:(void(^)(void))successBlock
                  error:(void(^)(int error_code))errorBlock {
    [[DMCCIMService sharedDMCIMService] setUserSetting:UserSettingScope_Hidden_Notification_Detail key:@"" value:hidden?@"1":@"0" success:^{
        if (successBlock) {
            successBlock();
        }
    } error:^(int error_code) {
        if (errorBlock) {
            errorBlock(error_code);
        }
    }];
}

//UserSettingScope_Hidden_Notification_Detail = 4,
- (BOOL)isHiddenGroupMemberName:(NSString *)groupId {
    NSString *strValue = [[DMCCIMService sharedDMCIMService] getUserSetting:UserSettingScope_Group_Hide_Nickname key:groupId];
    return [strValue isEqualToString:@"0"];
}


- (BOOL)isAllowAddFriendGroupMemberName:(NSString *)groupId {
    NSString *strValue = [[DMCCIMService sharedDMCIMService] getUserSetting:UserSettingScope_allowAddFriend key:groupId];
    return [strValue isEqualToString:@"1"];;
}

- (NSString *)getTimeIntervalGroupId:(NSString *)groupId {
    NSString *strValue = [[DMCCIMService sharedDMCIMService] getUserSetting:UserSettingScope_TimeInterval key:groupId];
    return strValue;
}

- (BOOL)isForwardGroupMemberName:(NSString *)groupId {
    NSString *strValue = [[DMCCIMService sharedDMCIMService] getUserSetting:UserSettingScope_Forward key:groupId];
    return [strValue isEqualToString:@"1"];;
}

- (BOOL)isCopyGroupMemberName:(NSString *)groupId {
    NSString *strValue = [[DMCCIMService sharedDMCIMService] getUserSetting:UserSettingScope_Copy key:groupId];
    return [strValue isEqualToString:@"1"];;
}

- (NSString *)getClearChatsGroupMemberName:(NSString *)groupId {
    NSString *strValue = [[DMCCIMService sharedDMCIMService] getUserSetting:UserSettingScope_Clear key:groupId];
    return strValue;
}

- (NSString *)getGroupJoinTypeWithGroupId:(NSString *)groupId {
    NSString *strValue = [[DMCCIMService sharedDMCIMService] getUserSetting:UserSettingScope_GroupJoinType key:groupId];
    return strValue;
}

- (void)roleAddFriendWithType:(NSString*)type cb:(onResult)cb {
    [_osnsdk roleAddFriend:type cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        cb(isSuccess, json, error);
    }];
}

- (void)setHiddenGroupMemberName:(BOOL)hidden
                           group:(NSString *)groupId
                            success:(void(^)(void))successBlock
                              error:(void(^)(int error_code))errorBlock {
    [[DMCCIMService sharedDMCIMService] setUserSetting:UserSettingScope_Group_Hide_Nickname key:groupId value:hidden?@"0":@"1" success:^{
        if (successBlock) {
            successBlock();
        }
    } error:^(int error_code) {
        if (errorBlock) {
            errorBlock(error_code);
        }
    }];
}


- (BOOL)deleteMessage:(long)messageId {
    [SqliteUtils deleteMessage:messageId];
    return true;
}

- (void)deleteRemoteMessage:(long long)messageUid
                    success:(void(^)(void))successBlock
                      error:(void(^)(int error_code))errorBlock  {
    NSLog(@"deleteRemoteMessage");
}

- (NSArray<DMCCConversationSearchInfo *> *)searchConversation:(NSString *)keyword inConversation:(NSArray<NSNumber *> *)conversationTypes lines:(NSArray<NSNumber *> *)lines {
    NSLog(@"searchConversation keyword: %@", keyword);
    return nil;
}

- (NSArray<DMCCMessage *> *)searchMessage:(DMCCConversation *)conversation
                                  keyword:(NSString *)keyword
                                    order:(BOOL)desc
                                    limit:(int)limit
                                   offset:(int)offset {
    NSLog(@"searchMessage keyword: %@", keyword);
    if (keyword.length == 0 || limit == 0) {
        return nil;
    }
    return nil;
}

- (NSArray<DMCCMessage *> *)searchMessage:(NSArray<NSNumber *> *)conversationTypes
                                    lines:(NSArray<NSNumber *> *)lines
                             contentTypes:(NSArray<NSNumber *> *)contentTypes
                                  keyword:(NSString *)keyword
                                     from:(NSUInteger)fromIndex
                                    count:(NSInteger)count {
    NSLog(@"searchMessage keyword: %@, types: ", keyword);
    return nil;
}

- (void)createGroup:(NSString *)groupId
               name:(NSString *)groupName
           portrait:(NSString *)groupPortrait
               type:(DMCCGroupType)type
         groupExtra:(NSString *)groupExtra
            members:(NSArray *)groupMembers
        memberExtra:(NSString *)memberExtra
        notifyLines:(NSArray<NSNumber *> *)notifyLines
      notifyContent:(DMCCMessageContent *)notifyContent
            success:(void(^)(NSString *groupId))successBlock
              error:(void(^)(int error_code))errorBlock {
    NSLog(@"createGroup groupId: %@, groupName: %@, groupType: %ld, groupPortrait: %@", groupId, groupName, type, groupPortrait);
    for(NSString* m in groupMembers){
        NSLog(@"member: %@", m);
    }
    [_osnsdk createGroup:groupName membser:groupMembers type:(int)type portrait:groupPortrait cb:^(bool isSuccess, NSDictionary *json, NSString *error) {
        dispatch_async(dispatch_get_main_queue(), ^{
            if(isSuccess){
                successBlock(json[@"groupID"]);
            } else {
                errorBlock(-1);
            }
        });
    }];
}
- (void)inviteUser:(NSString*)groupID userList:(NSArray<NSString*>*)userList{
    DMCCGroupInfo *groupInfo = [SqliteUtils queryGroup:groupID];
    NSMutableDictionary *json = [NSMutableDictionary new];
    json[@"type"] = @"card";
    json[@"cardType"] = @"group";
    json[@"target"] = groupID;
    json[@"name"] = groupInfo.name;
    json[@"displayName"] = groupInfo.name;
    json[@"portrait"] = groupInfo.portrait;
    json[@"theme"] = @"";
    json[@"url"] = @"";
    json[@"info"] = @"";
    for(NSString *user in userList){
        [_osnsdk sendMessage:[OsnUtils dic2Json:json] userID:user cb:nil];
    }
}
- (void)addMembers:(NSArray *)members
           toGroup:(NSString *)groupId
       memberExtra:(NSString *)memberExtra
       notifyLines:(NSArray<NSNumber *> *)notifyLines
     notifyContent:(DMCCMessageContent *)notifyContent
           success:(void(^)())successBlock
             error:(void(^)(int error_code))errorBlock {
    NSLog(@"addMembers groupID: %@", groupId);
    if(groupId.length == 0) {
        if(errorBlock) {
            errorBlock(-1);
        }
        return;
    }
    [_osnsdk addMember:groupId members:members cb:^(bool isSuccess, NSDictionary *json, NSString *error) {
        if(isSuccess){
            NSArray<NSString*>* userList = json[@"userList"];
            if(userList != nil){
                [self inviteUser:groupId userList:userList];
            }
            successBlock();
        }
        else
            errorBlock(-1);
    }];
}

- (void)kickoffMembers:(NSArray *)members
             fromGroup:(NSString *)groupId
           notifyLines:(NSArray<NSNumber *> *)notifyLines
         notifyContent:(DMCCMessageContent *)notifyContent
               success:(void(^)())successBlock
                 error:(void(^)(int error_code))errorBlock {

    NSLog(@"kickoffMembers groupID: %@", groupId);
    if(groupId.length == 0) {
        if(errorBlock) {
            errorBlock(-1);
        }
        return;
    }
    [_osnsdk delMember:groupId members:members cb:^(bool isSuccess, NSDictionary *json, NSString *error) {
        if(isSuccess)
            successBlock();
        else
            errorBlock(-1);
    }];
}

- (void)quitGroup:(NSString *)groupId
      notifyLines:(NSArray<NSNumber *> *)notifyLines
    notifyContent:(DMCCMessageContent *)notifyContent
          success:(void(^)())successBlock
            error:(void(^)(int error_code))errorBlock {

    NSLog(@"quitGroup groupID: %@", groupId);
    if(groupId.length == 0) {
        if(errorBlock) {
            errorBlock(-1);
        }
        return;
    }
    [_osnsdk quitGroup:groupId cb:^(bool isSuccess, NSDictionary *json, NSString *error) {
        if(isSuccess)
            successBlock();
        else
            errorBlock(-1);
    }];
}

- (void)dismissGroup:(NSString *)groupId
         notifyLines:(NSArray<NSNumber *> *)notifyLines
       notifyContent:(DMCCMessageContent *)notifyContent
             success:(void(^)())successBlock
               error:(void(^)(int error_code))errorBlock {

    NSLog(@"dismissGroup groupID: %@", groupId);
    if(groupId.length == 0) {
        if(errorBlock) {
            errorBlock(-1);
        }
        return;
    }
    [_osnsdk dismissGroup:groupId cb:^(bool isSuccess, NSDictionary *json, NSString *error) {
        if(isSuccess)
            successBlock();
        else
            errorBlock(-1);
    }];
}

- (void)modifyGroupInfo:(NSString *)groupId
                   type:(ModifyGroupInfoType)type
               newValue:(NSString *)newValue
            notifyLines:(NSArray<NSNumber *> *)notifyLines
          notifyContent:(DMCCMessageContent *)notifyContent
                success:(void(^)(void))successBlock
                  error:(void(^)(int error_code))errorBlock {
    OsnGroupInfo* info = [OsnGroupInfo new];
    info.groupID = groupId;
    NSMutableArray<NSString*>* keys = [NSMutableArray new];
    if(type == Modify_Group_Name){
        [keys addObject:@"name"];
        info.name = newValue;
    } else if(type == Modify_Group_Portrait){
        [keys addObject:@"portrait"];
        info.portrait = newValue;
    } else if(type == Modify_Group_Type){
        [keys addObject:@"type"];
        info.type = [newValue intValue];
    } else if(type == Modify_Group_JoinType){
        [keys addObject:@"joinType"];
        info.joinType = [newValue intValue];
    } else if(type == Modify_Group_PassType){
        [keys addObject:@"passType"];
        info.passType = [newValue intValue];
    } else if(type == Modify_Group_Mute){
        [keys addObject:@"mute"];
        info.mute = [newValue intValue];
    } else if(type == Modify_Group_BillBord){
        [keys addObject:@"billboard"];
        info.billboard = newValue;
    }
    [_osnsdk modifyGroupInfo:keys groupInfo:info cb:^(bool isSuccess, NSDictionary *json, NSString *error) {
        dispatch_async(dispatch_get_main_queue(), ^{
            if(isSuccess)
                successBlock();
            else
                errorBlock(-1);
        });
    }];
}

- (void) delGroup:(NSString*) groupID {
    [SqliteUtils deleteGroup:groupID];
    [SqliteUtils deleteConversation:Group_Type target:groupID line:0];
    [SqliteUtils clearMembers:groupID];
}

- (void)modifyGroupAlias:(NSString *)groupId
                   alias:(NSString *)newAlias
             notifyLines:(NSArray<NSNumber *> *)notifyLines
           notifyContent:(DMCCMessageContent *)notifyContent
                 success:(void(^)())successBlock
                   error:(void(^)(int error_code))errorBlock {
    NSLog(@"modifyGroupAlias alias: %@", newAlias);
    if(groupId.length == 0) {
        if(errorBlock) {
            errorBlock(-1);
        }
        return;
    }
    OsnMemberInfo* info = [OsnMemberInfo new];
    info.groupID = groupId;
    info.osnID = [_osnsdk getUserID];
    info.nickName = newAlias;
    [_osnsdk modifyMemberInfo:@[@"nickName"] memberInfo:info cb:^(bool isSuccess, NSDictionary *json, NSString *error) {
        if(isSuccess)
            successBlock();
        else
            errorBlock(-1);
    }];
}

- (void)modifyGroupMemberAlias:(NSString *)groupId
                      memberId:(NSString *)memberId
                         alias:(NSString *)newAlias
                   notifyLines:(NSArray<NSNumber *> *)notifyLines
                 notifyContent:(DMCCMessageContent *)notifyContent
                       success:(void(^)(void))successBlock
                         error:(void(^)(int error_code))errorBlock {
    NSLog(@"modifyGroupMemberAlias alias: %@", newAlias);
    if(groupId.length == 0) {
        if(errorBlock) {
            errorBlock(-1);
        }
        return;
    }
    OsnMemberInfo* info = [OsnMemberInfo new];
    info.groupID = groupId;
    info.osnID = memberId;
    info.nickName = newAlias;
    [_osnsdk modifyMemberInfo:@[@"nickName"] memberInfo:info cb:^(bool isSuccess, NSDictionary *json, NSString *error) {
        if(isSuccess)
            successBlock();
        else
            errorBlock(-1);
    }];
}

- (void)modifyGroupMemberExtra:(NSString *)groupId
                         extra:(NSString *)extra
                   notifyLines:(NSArray<NSNumber *> *)notifyLines
                 notifyContent:(DMCCMessageContent *)notifyContent
                       success:(void(^)(void))successBlock
                         error:(void(^)(int error_code))errorBlock {
    [self modifyGroupMemberExtra:groupId memberId:[DMCCNetworkService sharedInstance].userId extra:extra notifyLines:notifyLines notifyContent:notifyContent success:successBlock error:errorBlock];
}

- (void)modifyGroupMemberExtra:(NSString *)groupId
                      memberId:(NSString *)memberId
                         extra:(NSString *)extra
                   notifyLines:(NSArray<NSNumber *> *)notifyLines
                 notifyContent:(DMCCMessageContent *)notifyContent
                       success:(void(^)(void))successBlock
                         error:(void(^)(int error_code))errorBlock {
    NSLog(@"modifyGroupMemberExtra");
    if(groupId.length == 0) {
        if(errorBlock) {
            errorBlock(-1);
        }
        return;
    }
}
- (NSArray<DMCCGroupMember*>*) updateMember:(NSString*) groupID members:(NSArray<OsnMemberInfo*>*) memberList {
    NSMutableArray<DMCCGroupMember*>* members = [NSMutableArray new];
    if (memberList == nil || !memberList.count)
        memberList = [_osnsdk getMemberInfo:groupID cb:nil];
    if (memberList == nil || !memberList.count)
        return members;

    [SqliteUtils clearMembers:groupID];
    for (OsnMemberInfo* m in memberList) {
        if (m.osnID != nil) {
            if (m.nickName == nil) {
                DMCCUserInfo* userInfo = [SqliteUtils queryUser:m.osnID];
                if (userInfo == nil) {
                    OsnUserInfo* u = [_osnsdk getUserInfo:m.osnID cb:nil];
                    if (u != nil) {
                        userInfo = [self toClientUser:u];
                        [SqliteUtils insertUser:userInfo];
                    }
                }
                if (userInfo != nil)
                    m.nickName = userInfo.name;
            }
            [members addObject:[self toClientMember:m]];
        }
        NSLog(@"memberID: %@, nickName: %@, type: %d",m.osnID,m.nickName,m.type);
    }
    if (members.count)
        [SqliteUtils insertMembers:members];
    return members;
}

- (void) insertMembers:(NSString*)groupID members:(NSMutableArray<DMCCGroupMember*>*)members {
    
    //NSMutableArray<DMCCGroupMember*>* syncList = [NSMutableArray new];
    if (members == nil) {
        return;
    }
    if (members.count == 0) {
        return;
    }
    
    for (DMCCGroupMember* m in members) {
        //NSLog(@"=test2= mute : %d", m.mute);
        DMCCUserInfo* userInfo = [SqliteUtils queryUser:m.memberId];
        if (userInfo == nil) {
            NSLog(@"=test2= sync user id : %@", m.memberId);
            OsnUserInfo* u = [_osnsdk getUserInfo:m.memberId cb:nil];
            if (u != nil) {
                userInfo = [self toClientUser:u];
                [SqliteUtils insertUser:userInfo];
            }
        }
        if (m.alias == nil) {
            if (userInfo != nil) {
                if (userInfo.displayName.length > 0) {
                    m.alias = userInfo.displayName;
                } else {
                    m.alias = userInfo.name;
                }
            }
            else {
                m.alias = @"Dao user";
            }
        }
    }
    NSLog(@"=test2= insert members : %ld", members.count);
    [SqliteUtils insertMembers:members];
}


- (NSArray<DMCCGroupMember *> *)getGroupMembers:(NSString *)groupId
                             forceUpdate:(BOOL)forceUpdate {
    NSLog(@"getGroupMembers groupID: %@", groupId);
    if(groupId.length == 0) {
        return nil;
    }
    __block NSMutableArray<DMCCGroupMember*>* members = [[SqliteUtils queryMembers:groupId]mutableCopy];
    if(members == nil || forceUpdate){
        [_osnsdk getMemberInfo:groupId cb:^(bool isSuccess, id t, NSString *error) {
            if(isSuccess){
                NSArray<OsnMemberInfo*>* oms = (NSArray<OsnMemberInfo*>*)t;
                members = [[self updateMember:groupId members:oms]mutableCopy];
                [[DMCCNetworkService sharedInstance]onGroupMemberUpdated:groupId members:members];
            }
        }];
        return members;
    }
    bool isValied = false;
    for (DMCCGroupMember* m in members) {
        if ([m.memberId isEqualToString:[_osnsdk getUserID]]) {
            isValied = true;
            break;
        }
        NSLog(@"memberID: %@, alias: %@, type: %ld", m.memberId, m.alias, m.type);
    }
    if (!isValied)
        [members removeAllObjects];
    return members;
}

- (NSArray<DMCCGroupMember *> *)getGroupZoneMembers:(NSString *)groupId
                                              begin:(int)begin
                             forceUpdate:(BOOL)forceUpdate {
    NSLog(@"getGroupZoneMembers groupID: %@", groupId);
    if(groupId.length == 0) {
        return nil;
    }
    __block NSMutableArray<DMCCGroupMember*>* members = [[SqliteUtils queryMembers:groupId]mutableCopy];
    
    long memberCount = 0;
    if (members != nil) {
        memberCount = members.count;
    }
    if(members == nil || members.count == 0 || forceUpdate){
        [_osnsdk getMemberZoneInfo:groupId begin:begin cb:^(bool isSuccess, id t, NSString *error) {
            if(isSuccess){
                NSArray<OsnMemberInfo*>* oms = (NSArray<OsnMemberInfo*>*)t;
                
                

                
                
                
                
                
                members = [NSMutableArray new];
                
                // transfer
                //NSMutableArray<DMCCGroupMember*>* memberList = [NSMutableArray new];
                int index = begin;
                for (OsnMemberInfo* m in oms) {
                    NSLog(@"=test2= mute : %d",m.mute);
                    DMCCGroupMember* dm = [self toClientMember:m];
                    dm.index = index;
                    index ++;
                    [members addObject:dm];
                }
                if (members.count > 0) {
                    [self insertMembers:groupId members:members];
                }
                
                

                
                if(memberCount == 0 && !forceUpdate) {
                    [[DMCCNetworkService sharedInstance]onGroupMemberUpdated:groupId members:members];
                }
                //members = [[self updateMember:groupId members:oms]mutableCopy];
            }
        }];
        return members;
    }
    bool isValied = false;
    for (DMCCGroupMember* m in members) {
        if ([m.memberId isEqualToString:[_osnsdk getUserID]]) {
            isValied = true;
            break;
        }
        NSLog(@"memberID: %@, alias: %@, type: %ld", m.memberId, m.alias, m.type);
    }
    if (!isValied)
        [members removeAllObjects];
    
    //NSLog(@"=test2= getGroupMembers count: %ld", members.count);
    return members;
}

- (NSArray<DMCCGroupMember *> *)getGroupZoneMembersTop:(NSString *)groupId
                                              begin:(int)begin
                             forceUpdate:(BOOL)forceUpdate {
    if (groupId == nil) {
        return nil;
    }
    if(groupId.length == 0) {
        return nil;
    }
    __block NSMutableArray<DMCCGroupMember*>* members = [[SqliteUtils queryMembersTop:groupId]mutableCopy];
    return members;
}
- (void)delOutGroupMember:(NSString *)groupId index:(int)index{
    if (groupId == nil) {
        return;
    }
    if(groupId.length == 0) {
        return;
    }
    [SqliteUtils deleteMembers:groupId index:index];
}

- (NSArray<DMCCGroupMember *> *)getGroupMembers:(NSString *)groupId
                             type:(DMCCGroupMemberType)memberType {
    NSLog(@"getGroupMembers groupID: %@, memberType: %ld", groupId, memberType);
    if(groupId.length == 0) {
        return nil;
    }
    NSArray<DMCCGroupMember*>* members = [SqliteUtils queryMembers:groupId];
    if(members == nil || !members.count)
        return members;
    NSMutableArray<DMCCGroupMember*>* memberList = [NSMutableArray new];
    for(DMCCGroupMember* m in members){
        if(m.type == memberType)
            [memberList addObject:m];
    }
    return memberList;
}

- (void)getGroupMembers:(NSString *)groupId
                refresh:(BOOL)refresh
                success:(void(^)(NSString *groupId, NSArray<DMCCGroupMember *> *))successBlock
                  error:(void(^)(int errorCode))errorBlock {
    if(groupId.length == 0) {
        if(errorBlock) {
            errorBlock(-1);
        }
        return;
    }
    __block NSArray<DMCCGroupMember*>* members = [SqliteUtils queryMembers:groupId];
    if(members == nil || refresh){
        [_osnsdk getMemberInfo:groupId cb:^(bool isSuccess, id t, NSString *error) {
            if(isSuccess){
                NSArray<OsnMemberInfo*>* oms = (NSArray<OsnMemberInfo*>*)t;
                members = [[self updateMember:groupId members:oms]mutableCopy];
                successBlock(groupId, members);
            } else {
                errorBlock(-1);
            }
        }];
    } else {
        if(members == nil)
            errorBlock(-1);
        else
            successBlock(groupId, members);
    }
}

- (DMCCGroupMember *)getGroupMember:(NSString *)groupId
                           memberId:(NSString *)memberId {
    if (!groupId || !memberId) {
        return nil;
    }
    return [SqliteUtils queryMember:groupId memberID:memberId];
}

- (void)transferGroup:(NSString *)groupId
                   to:(NSString *)newOwner
          notifyLines:(NSArray<NSNumber *> *)notifyLines
        notifyContent:(DMCCMessageContent *)notifyContent
              success:(void(^)())successBlock
                error:(void(^)(int error_code))errorBlock {
    NSLog(@"transferGroup groupID: %@", groupId);
    if(groupId.length == 0) {
        if(errorBlock) {
            errorBlock(-1);
        }
        return;
    }
}

- (void)setGroupManager:(NSString *)groupId
                  isSet:(BOOL)isSet
              memberIds:(NSArray<NSString *> *)memberIds
            notifyLines:(NSArray<NSNumber *> *)notifyLines
          notifyContent:(DMCCMessageContent *)notifyContent
                success:(void(^)(void))successBlock
                  error:(void(^)(int error_code))errorBlock {
    NSLog(@"setGroupManager groupID: %@", groupId);
    [_osnsdk addGroupManager:groupId memberIds:memberIds cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        if (isSuccess) {
            successBlock();
        } else {
            if(errorBlock) {
                errorBlock(-1);
            }
        }
    }];
}

- (void)delGroupManager:(NSString *)groupId
                  isSet:(BOOL)isSet
              memberIds:(NSArray<NSString *> *)memberIds
            notifyLines:(NSArray<NSNumber *> *)notifyLines
          notifyContent:(DMCCMessageContent *)notifyContent
                success:(void(^)(void))successBlock
                  error:(void(^)(int error_code))errorBlock {
    NSLog(@"delGroupManager groupID: %@", groupId);
    [_osnsdk delGroupManager:groupId memberIds:memberIds cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        if (isSuccess) {
            successBlock();
        } else {
            if(errorBlock) {
                errorBlock(-1);
            }
        }
    }];
}

- (void)muteGroupMember:(NSString *)groupId
                     isSet:(BOOL)isSet
                   mode:(BOOL)mode
                 memberIds:(NSArray<NSString *> *)memberIds
               notifyLines:(NSArray<NSNumber *> *)notifyLines
             notifyContent:(DMCCMessageContent *)notifyContent
                   success:(void(^)(void))successBlock
                     error:(void(^)(int error_code))errorBlock {
    NSLog(@"muteGroupMember groupID: %@", groupId);
    
    [_osnsdk muteGroup:groupId state:isSet members:memberIds mode:mode cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        if (isSuccess) {
            if (!mode) {
                for (NSString *memberId in memberIds) {
                    DMCCGroupMember *groupMember = [SqliteUtils queryMember:groupId memberID:memberId];
                    if (groupMember == nil){
                        NSLog(@"no in the member");
                        return;
                    }
                    groupMember.mute = isSet ? 1 :0;
                    [SqliteUtils updateMember:groupMember keys:@[@"mute"]];
                }
            } else {
                [[DMCCIMService sharedDMCIMService] setMuteGroup:groupId mute:isSet?1:0 success:^{
                    
                } error:^(int errorCode) {
                    
                }];
            }
            successBlock();
        } else {
            if(errorBlock) {
                errorBlock(-1);
            }
        }
    }];
}

- (void)allowGroupMember:(NSString *)groupId
                     isSet:(BOOL)isSet
                 memberIds:(NSArray<NSString *> *)memberIds
               notifyLines:(NSArray<NSNumber *> *)notifyLines
             notifyContent:(DMCCMessageContent *)notifyContent
                   success:(void(^)(void))successBlock
                     error:(void(^)(int error_code))errorBlock {
    NSLog(@"allowGroupMember groupID: %@", groupId);
    if(!groupId.length) {
        if(errorBlock) {
            errorBlock(-1);
        }
        return;
    }
}

- (NSArray<NSString *> *)getFavGroups {
    NSDictionary *favGroupDict = [[DMCCIMService sharedDMCIMService] getUserSettings:UserSettingScope_Favourite_Group];
    NSMutableArray *ids = [[NSMutableArray alloc] init];
    [favGroupDict enumerateKeysAndObjectsUsingBlock:^(id  _Nonnull key, id  _Nonnull obj, BOOL * _Nonnull stop) {
        if ([obj isEqualToString:@"1"]) {
            [ids addObject:key];
        }
    }];
    return ids;
}

- (NSArray<NSString *> *)getFavGroups:(BOOL)isFresh {
    if (isFresh) {
        [_osnsdk syncGroup];
    }
    NSDictionary *favGroupDict = [[DMCCIMService sharedDMCIMService] getUserSettings:UserSettingScope_Favourite_Group];
    NSMutableArray *ids = [[NSMutableArray alloc] init];
    [favGroupDict enumerateKeysAndObjectsUsingBlock:^(id  _Nonnull key, id  _Nonnull obj, BOOL * _Nonnull stop) {
        if ([obj isEqualToString:@"1"]) {
            [ids addObject:key];
        }
    }];
    return ids;
}

- (BOOL)isFavGroup:(NSString *)groupId {
    NSString *strValue = [[DMCCIMService sharedDMCIMService] getUserSetting:UserSettingScope_Favourite_Group key:groupId];
    if ([strValue isEqualToString:@"1"]) {
        return YES;
    }
    return NO;
}

- (BOOL)isMuteGroup:(NSString *)groupId {
    NSString *strValue = [[DMCCIMService sharedDMCIMService] getUserSetting:UserSettingScope_Mute_Group key:groupId];
    if ([strValue isEqualToString:@"1"]) {
        return YES;
    }
    return NO;
}

- (void)setFavGroup:(NSString *)groupId fav:(BOOL)fav success:(void(^)(void))successBlock error:(void(^)(int errorCode))errorBlock {
    [[DMCCIMService sharedDMCIMService] setUserSetting:UserSettingScope_Favourite_Group key:groupId value:fav? @"1" : @"0" success:successBlock error:errorBlock];
}

- (void)setMuteGroup:(NSString *)groupId mute:(BOOL)mute success:(void(^)(void))successBlock error:(void(^)(int errorCode))errorBlock {
    [[DMCCIMService sharedDMCIMService] setUserSetting:UserSettingScope_Mute_Group key:groupId value:mute? @"1" : @"0" success:successBlock error:errorBlock];
}

//- (void) updateGroup:(OsnGroupInfo*) osnGroupInfo isUpdate:(bool)isUpdateMember isFav:(bool)isFav{
//    DMCCGroupInfo* groupInfo = [self toClientGroup:osnGroupInfo];
//    groupInfo.fav = isFav;
//    [SqliteUtils insertGroup:groupInfo];
//    if (isUpdateMember)
//        [self updateMember:osnGroupInfo.groupID members:osnGroupInfo.userList];
//}
- (DMCCGroupInfo *)getGroupInfo:(NSString *)groupId refresh:(BOOL)refresh {
    if (!groupId) {
        return nil;
    }
    __block DMCCGroupInfo* info = [SqliteUtils queryGroup:groupId];
    if(info == nil || refresh){
        [_osnsdk getGroupInfo:groupId cb:^(bool isSuccess, id t, NSString *error) {
            if(isSuccess){
                // update conversation is member
                DMCCGroupInfo* info2 =[self toClientGroup:t];
                int isMember = 0;
                if (info2!=nil){
                    if (info2.isMember != nil) {
                        if ([info2.isMember isEqualToString:@"yes"]) {
                            isMember = 1;
                        }
                    }
                }
                DMCCConversationInfo* convInfo = [SqliteUtils queryConversation:Group_Type target:groupId line:0];
                if (convInfo != nil) {
                    // update conversation
                    if (convInfo.isMember != isMember) {
                        convInfo.isMember = isMember;
                        [SqliteUtils updateConversation:convInfo keys:@[@"isMember"]];
                    }
                }
                // update conversation is member end.
                
                info = [SqliteUtils queryGroup:groupId];
                if(info != nil){
                    [[DMCCNetworkService sharedInstance] updateGroup:t update:true fav:info.fav];
                    info = [SqliteUtils queryGroup:groupId];
                }else{
                    info = [self toClientGroup:t];
                }
                info = [[DMCCIMService sharedDMCIMService] toClientGroup:t];
                [[DMCCNetworkService sharedInstance] onGroupInfoUpdated:@[info]];
            }
        }];
    }
    return info;
}

- (DMCCGroupInfo *)getGroupInfoEx:(NSString *)groupId refresh:(BOOL)refresh{
    if (!groupId) {
        return nil;
    }
    __block DMCCGroupInfo* info = [SqliteUtils queryGroup:groupId];
    if(info == nil || refresh){
        OsnGroupInfo *infoEx = [_osnsdk getGroupInfo:groupId cb:nil];
        if (infoEx == nil) {
            return nil;
        }
        info = [[DMCCIMService sharedDMCIMService] toClientGroup:infoEx];
    }
    return info;
}

- (DMCCGroupInfo *)getGroupInfoEx:(NSString *)groupId refresh:(BOOL)refresh cb:(onResultT)cb{
    if (!groupId) {
        return nil;
    }
    __block DMCCGroupInfo* info = [SqliteUtils queryGroup:groupId];
    if(info == nil || refresh){
        if (cb != nil) {
            
            
            [_osnsdk getGroupInfo:groupId cb:^(bool isSuccess, id t, NSString *error) {
                if (isSuccess) {
                    DMCCGroupInfo* info =[self toClientGroup:t];
                    cb(isSuccess,info,error);
                } else {
                    cb(isSuccess,t,error);
                }
                
            }];
            

            
        } else {
            OsnGroupInfo *infoEx = [_osnsdk getGroupInfo:groupId cb:nil];
            if (infoEx == nil) {
                return nil;
            }
            info = [[DMCCIMService sharedDMCIMService] toClientGroup:infoEx];
        }

    }
    return info;
}

- (void)insertConversation:(NSString *)groupId groupInfo:(DMCCGroupInfo*)groupInfo{
    [SqliteUtils insertConversation:Group_Type target:groupInfo.target line:0];
    [SqliteUtils insertGroup:groupInfo];
    /*
    DMCCGroupMember * memberInfo = [SqliteUtils queryMember:groupId memberID:self.getUserID];
    if (memberInfo == nil) {
        DMCCGroupMember * memberInfo = [DMCCGroupMember new];
        memberInfo.type=Member_Type_Normal;
        memberInfo.groupId = groupInfo.target;
        memberInfo.memberId = self.getUserID;
        memberInfo.mute = 0;
        [SqliteUtils insertMember:memberInfo];
    }*/
}
- (void)insertGroupNull:(NSString*)groupId{
    DMCCGroupInfo* groupInfo = [DMCCGroupInfo new];
    groupInfo.name = groupId;
    groupInfo.target = groupId;
    groupInfo.portrait = @"";
    [SqliteUtils insertGroup:groupInfo];
}


- (void)getGroupInfo:(NSString *)groupId
             refresh:(BOOL)refresh
             success:(void(^)(DMCCGroupInfo *groupInfo))successBlock
               error:(void(^)(int errorCode))errorBlock {
    if(!groupId.length) {
        if(errorBlock) {
            errorBlock(-1);
        }
        return;
    }
    
    __block DMCCGroupInfo* info = [SqliteUtils queryGroup:groupId];
    if(info == nil || refresh){
        [_osnsdk getGroupInfo:groupId cb:^(bool isSuccess, id t, NSString *error) {
            if(isSuccess){
                info = [SqliteUtils queryGroup:groupId];
                if(info != nil){
                    [[DMCCNetworkService sharedInstance] updateGroup:t update:true fav:info.fav];
                    info = [SqliteUtils queryGroup:groupId];
                }else{
                    info = [self toClientGroup:t];
                }
                successBlock(info);
            } else {
                errorBlock(-1);
            }
        }];
    }
}

/**
 加入群
 
 @param groupId 群ID
 @param successBlock 成功的回调
 @param errorBlock 失败的回调
 */
- (void)addGroup:(NSString *)groupId
          reason:(NSString *)reason
      invitation:(NSString *)invitation
                success:(void(^)(void))successBlock
           error:(void(^)(int error_code))errorBlock {
    [_osnsdk joinGroup:groupId reason:reason invitation:nil cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        if (isSuccess) {
            successBlock();
        } else {
            errorBlock(-1);
        }
    }];
}

- (NSString *)getUserSetting:(UserSettingScope)scope key:(NSString *)key {
    NSString* value = nil;
    if (scope == UserSettingScope_Favourite_Group) {
        DMCCGroupInfo* groupInfo = [SqliteUtils queryGroup:key];
        value = [NSString stringWithFormat:@"%d", groupInfo.fav];
    } else if (scope == UserSettingScope_Group_Hide_Nickname) {
        DMCCGroupInfo* groupInfo = [SqliteUtils queryGroup:key];
        value = [NSString stringWithFormat:@"%d",groupInfo.showAlias];
    }  else if (scope == UserSettingScope_Mute_Group) {
        DMCCGroupInfo* groupInfo = [SqliteUtils queryGroup:key];
        value = [NSString stringWithFormat:@"%d",groupInfo.mute];
    } else if (scope == UserSettingScope_allowAddFriend) {
        DMCCGroupInfo* groupInfo = [SqliteUtils queryGroup:key];
        if (groupInfo.attribute) {
            NSDictionary *json = [OsnUtils json2Dics:groupInfo.attribute];
            NSString *type = json[@"AllowAddFriend"];
            if(type != nil) {
                if ([type isEqualToString:@"no"]) {
                    value = @"0";
                } else {
                    value = @"1";
                }
            }else {
                value = @"1";
            }
                
        }
    } else if (scope == UserSettingScope_TimeInterval) {
        DMCCGroupInfo* groupInfo = [SqliteUtils queryGroup:key];
        if (groupInfo.attribute) {
            NSDictionary *json = [OsnUtils json2Dics:groupInfo.attribute];
            NSString *type = json[@"TimeInterval"];
            if(type != nil) {
                value = type;
            }else {
                value = @"0";
            }
        }
    } else if (scope == UserSettingScope_Forward) {
        DMCCGroupInfo* groupInfo = [SqliteUtils queryGroup:key];
        if (groupInfo.attribute) {
            NSDictionary *json = [OsnUtils json2Dics:groupInfo.attribute];
            NSString *type = json[@"isGroupForward"];
            if(type != nil) {
                if ([type isEqualToString:@"no"]) {
                    value = @"0";
                } else {
                    value = @"1";
                }
            }else {
                value = @"1";
            }
        }
    } else if (scope == UserSettingScope_Copy) {
        DMCCGroupInfo* groupInfo = [SqliteUtils queryGroup:key];
        if (groupInfo.attribute) {
            NSDictionary *json = [OsnUtils json2Dics:groupInfo.attribute];
            NSString *type = json[@"isGroupCopy"];
            if(type != nil) {
                if ([type isEqualToString:@"no"]) {
                    value = @"0";
                } else {
                    value = @"1";
                }
            }else {
                value = @"1";
            }
        }
    } else if (scope == UserSettingScope_Clear) {
        DMCCGroupInfo* groupInfo = [SqliteUtils queryGroup:key];
        if (groupInfo.attribute) {
            NSDictionary *json = [OsnUtils json2Dics:groupInfo.attribute];
            NSString *type = json[@"clearTimes"];
            if(type != nil) {
                value = type;
            }else {
                value = @"0";
            }
        }
    } else if (scope == UserSettingScope_GroupJoinType) {
        DMCCGroupInfo* groupInfo = [SqliteUtils queryGroup:key];
        if (groupInfo.extra) {
            NSDictionary *json = [OsnUtils json2Dics:groupInfo.extra];
            NSString *type = json[@"joinType"];
            if(type != nil) {
                value = type;
            }else {
                value = @"";
            }
        }
    } else if (scope == UserSettingScope_GroupJoinPwd) {
        DMCCGroupInfo* groupInfo = [SqliteUtils queryGroup:key];
        if (groupInfo.extra) {
            NSDictionary *json = [OsnUtils json2Dics:groupInfo.extra];
            NSString *type = json[@"joinPwd"];
            if(type != nil) {
                value = type;
            }else {
                value = @"";
            }
        }
    }
    NSLog(@"scope: %ld, key: %@, value: %@",scope, key, value);
    return value;
}

// 查询置顶消息
- (NSArray *)getGroupTopMessageWithGroupId:(NSString *)groupId {
    DMCCGroupInfo* groupInfo = [SqliteUtils queryGroup:groupId];
    NSMutableArray *values = [NSMutableArray arrayWithCapacity:0];
    if (groupInfo.attribute) {
        NSDictionary *json = [OsnUtils json2Dics:groupInfo.attribute];
        for (NSString *key in json) {
            if ([key containsString:@"top_"]) {
                [values addObject:json[key]];
            }
        }
    }
    return values;
}

// 查询是否已经置顶消息
- (BOOL)checkGroupTopMessageWithGroupId:(NSString *)groupId withHashO:(NSString *)hashO {
    NSArray *array = [[DMCCIMService sharedDMCIMService] getGroupTopMessageWithGroupId:groupId];
    BOOL isHash = NO;
    for (NSString *value in array) {
        NSDictionary *dic = [OsnUtils json2Dics:value];
        if ([dic[@"hasho"] isEqualToString:hashO]) {
            isHash = YES;
            break;
        }
    }
    return isHash;
}

- (NSDictionary<NSString *, NSString *> *)getUserSettings:(UserSettingScope)scope {
    NSMutableDictionary *result = [NSMutableDictionary new];
    if(scope == UserSettingScope_Favourite_Group){
        NSArray<DMCCGroupInfo*>* infos = [SqliteUtils listGroups];
        for(DMCCGroupInfo* g in infos){
            if(g.fav){
                result[g.target] = @"1";
            }
        }
    } else if(scope == UserSettingScope_Group_Hide_Nickname){
        NSArray<DMCCGroupInfo*>* infos = [SqliteUtils listGroups];
        for(DMCCGroupInfo* g in infos){
            if(g.showAlias){
                result[g.target] = @"1";
            }
        }
    }
    return result;
}

- (void)setUserSetting:(UserSettingScope)scope key:(NSString *)key value:(NSString *)value
               success:(void(^)())successBlock
                 error:(void(^)(int error_code))errorBlock {
    if(scope == UserSettingScope_Favourite_Group || scope == UserSettingScope_Group_Hide_Nickname || scope == UserSettingScope_Mute_Group){
        DMCCGroupInfo* info = [SqliteUtils queryGroup:key];
        if(info != nil){
            if(scope == UserSettingScope_Favourite_Group){
                info.fav = value.intValue;
                [SqliteUtils updateGroup:info keys:@[@"fav"]];
            } else if(scope == UserSettingScope_Group_Hide_Nickname){
                info.showAlias = value.intValue;
                [SqliteUtils updateGroup:info keys:@[@"showAlias"]];
            } else if(scope == UserSettingScope_Mute_Group){
                info.mute = value.intValue;
                [SqliteUtils updateGroup:info keys:@[@"mute"]];
            }
            successBlock();
            return;
        }
    }
    errorBlock(-1);
}

- (void)setConversation:(DMCCConversation *)conversation silent:(BOOL)silent
                success:(void(^)())successBlock
                  error:(void(^)(int error_code))errorBlock {
    NSLog(@"setConversation silent: %d",silent);
    DMCCConversationInfo* info = [SqliteUtils queryConversation:(int)conversation.type target:conversation.target line:conversation.line];
    info.isSilent = silent;
    [SqliteUtils updateConversation:info keys:@[@"silent"]];
    if(successBlock != nil)
        successBlock();
}

- (DMCCMessageContent *)messageContentFromPayload:(DMCCMessagePayload *)payload {
    int contenttype = payload.contentType;
    Class contentClass = self.MessageContentMaps[@(contenttype)];
    if (contentClass != nil) {
        id messageInstance = [[contentClass alloc] init];
        
        if ([contentClass conformsToProtocol:@protocol(DMCCMessageContent)]) {
            if ([messageInstance respondsToSelector:@selector(decode:)]) {
                [messageInstance performSelector:@selector(decode:)
                                      withObject:payload];
            }
        }
        return messageInstance;
    }
    DMCCUnknownMessageContent *unknownMsg = [[DMCCUnknownMessageContent alloc] init];
    [unknownMsg decode:payload];
    return unknownMsg;
}

- (DMCCMessage *)insert:(DMCCConversation *)conversation
                 sender:(NSString *)sender
                content:(DMCCMessageContent *)content
                 status:(DMCCMessageStatus)status
                 notify:(BOOL)notify
             serverTime:(long long)serverTime {
    NSLog(@"insert");
    return nil;
}

- (void)updateMessage:(long)messageId
              content:(DMCCMessageContent *)content {
    NSLog(@"updateMessage content");
}

- (void)updateMessage:(long)messageId
              content:(DMCCMessageContent *)content
            timestamp:(long long)timestamp {
    NSLog(@"updateMessage timestamp");
}

- (void)registerMessageContent:(Class)contentClass {
    int contenttype;
    if (class_getClassMethod(contentClass, @selector(getContentType))) {
        contenttype = [contentClass getContentType];
        if(self.MessageContentMaps[@(contenttype)]) {
            NSLog(@"****************************************");
            NSLog(@"Error, duplicate message content type %d", contenttype);
            NSLog(@"****************************************");
        }
        self.MessageContentMaps[@(contenttype)] = contentClass;
//        int contentflag = [contentClass getContentFlags];
//        mars::stn::MessageDB::Instance()->RegisterMessageFlag(contenttype, contentflag);
    } else {
        return;
    }
}

- (void)joinChatroom:(NSString *)chatroomId
             success:(void(^)(void))successBlock
               error:(void(^)(int error_code))errorBlock {
    if(!chatroomId) {
        if(errorBlock) {
            errorBlock(-1);
        }
        return;
    }
    NSLog(@"joinChatroom");
}

- (void)quitChatroom:(NSString *)chatroomId
             success:(void(^)(void))successBlock
               error:(void(^)(int error_code))errorBlock {
    if(!chatroomId) {
        if(errorBlock) {
            errorBlock(-1);
        }
        return;
    }
    NSLog(@"quitChatroom");
}

- (void)getChatroomInfo:(NSString *)chatroomId
                upateDt:(long long)updateDt
                success:(void(^)(DMCCChatroomInfo *chatroomInfo))successBlock
                  error:(void(^)(int error_code))errorBlock {
    if(!chatroomId) {
        if(errorBlock) {
            errorBlock(-1);
        }
        return;
    }
    NSLog(@"getChatroomInfo");
}

- (void)getChatroomMemberInfo:(NSString *)chatroomId
                     maxCount:(int)maxCount
                      success:(void(^)(DMCCChatroomMemberInfo *memberInfo))successBlock
                        error:(void(^)(int error_code))errorBlock {
    if (maxCount <= 0) {
        maxCount = 30;
    }
    if(!chatroomId) {
        if(errorBlock) {
            errorBlock(-1);
        }
        return;
    }
    NSLog(@"getChatroomMemberInfo");
}

- (void)createChannel:(NSString *)channelName
             portrait:(NSString *)channelPortrait
               status:(int)status
                 desc:(NSString *)desc
                extra:(NSString *)extra
              success:(void(^)(DMCCChannelInfo *channelInfo))successBlock
                error:(void(^)(int error_code))errorBlock {
    if (!extra) {
        extra = @"";
    }
    NSLog(@"createChannel");
}

- (void)destoryChannel:(NSString *)channelId
              success:(void(^)(void))successBlock
                error:(void(^)(int error_code))errorBlock {
    if(!channelId) {
        if(errorBlock) {
            errorBlock(-1);
        }
        return;
    }
    NSLog(@"destoryChannel");
}

- (DMCCChannelInfo *)getChannelInfo:(NSString *)channelId
                            refresh:(BOOL)refresh {
    NSLog(@"getChannelInfo");
    if(!channelId) {
        return nil;
    }
    return nil;
}

- (void)modifyChannelInfo:(NSString *)channelId
                     type:(ModifyChannelInfoType)type
                 newValue:(NSString *)newValue
                  success:(void(^)(void))successBlock
                    error:(void(^)(int error_code))errorBlock {
    NSLog(@"modifyChannelInfo");
    if(!channelId || !newValue) {
        if(errorBlock) {
            errorBlock(-1);
        }
        return;
    }
}

- (void)searchChannel:(NSString *)keyword success:(void(^)(NSArray<DMCCChannelInfo *> *machedChannels))successBlock error:(void(^)(int errorCode))errorBlock {
    
    if(!keyword.length) {
        successBlock(@[]);
        return;
    }
}

- (BOOL)isListenedChannel:(NSString *)channelId {
    if([@"1" isEqualToString:[self getUserSetting:UserSettingScope_Listened_Channel key:channelId]]) {
        return YES;
    }
    return NO;
}

- (void)listenChannel:(NSString *)channelId listen:(BOOL)listen success:(void(^)(void))successBlock error:(void(^)(int errorCode))errorBlock {
    if(!channelId) {
        if(errorBlock) {
            errorBlock(-1);
        }
        return;
    }
}

- (NSArray<NSString *> *)getMyChannels {
    NSDictionary *myChannelDict = [[DMCCIMService sharedDMCIMService] getUserSettings:UserSettingScope_My_Channel];
    NSMutableArray *ids = [[NSMutableArray alloc] init];
    [myChannelDict enumerateKeysAndObjectsUsingBlock:^(id  _Nonnull key, id  _Nonnull obj, BOOL * _Nonnull stop) {
        if ([obj isEqualToString:@"1"]) {
            [ids addObject:key];
        }
    }];
    return ids;
}
- (NSArray<NSString *> *)getListenedChannels {
    NSDictionary *myChannelDict = [[DMCCIMService sharedDMCIMService] getUserSettings:UserSettingScope_Listened_Channel];
    NSMutableArray *ids = [[NSMutableArray alloc] init];
    [myChannelDict enumerateKeysAndObjectsUsingBlock:^(id  _Nonnull key, id  _Nonnull obj, BOOL * _Nonnull stop) {
        if ([obj isEqualToString:@"1"]) {
            [ids addObject:key];
        }
    }];
    return ids;
}

- (NSArray<DMCCPCOnlineInfo *> *)getPCOnlineInfos {
    NSString *pcOnline = [self getUserSetting:UserSettingScope_PC_Online key:@"PC"];
    NSString *webOnline = [self getUserSetting:UserSettingScope_PC_Online key:@"Web"];
    NSString *wxOnline = [self getUserSetting:UserSettingScope_PC_Online key:@"WX"];
    
    NSMutableArray *output = [[NSMutableArray alloc] init];
    if (pcOnline.length) {
        [output addObject:[DMCCPCOnlineInfo infoFromStr:pcOnline withType:PC_Online]];
    }
    if (webOnline.length) {
        [output addObject:[DMCCPCOnlineInfo infoFromStr:webOnline withType:Web_Online]];
    }
    if (wxOnline.length) {
        [output addObject:[DMCCPCOnlineInfo infoFromStr:wxOnline withType:WX_Online]];
    }
    return output;
}

- (void)kickoffPCClient:(NSString *)pcClientId
                success:(void(^)(void))successBlock
                  error:(void(^)(int error_code))errorBlock {
    if(!pcClientId) {
        if(errorBlock) {
            errorBlock(-1);
        }
        return;
    }
}

- (BOOL)isMuteNotificationWhenPcOnline {
    NSString *strValue = [[DMCCIMService sharedDMCIMService] getUserSetting:UserSettingScope_Mute_When_PC_Online key:@""];
    if ([strValue isEqualToString:@"1"]) {
        return !self.defaultSilentWhenPCOnline;
    }
    return self.defaultSilentWhenPCOnline;
}

- (void)setDefaultSilentWhenPcOnline:(BOOL)defaultSilent {
    self.defaultSilentWhenPCOnline = defaultSilent;
}

- (void)muteNotificationWhenPcOnline:(BOOL)isMute
                             success:(void(^)(void))successBlock
                               error:(void(^)(int error_code))errorBlock {
    if(!self.defaultSilentWhenPCOnline) {
        isMute = !isMute;
    }
    [[DMCCIMService sharedDMCIMService] setUserSetting:UserSettingScope_Mute_When_PC_Online key:@"" value:isMute? @"0" : @"1" success:successBlock error:errorBlock];
}

- (void)getConversationFiles:(DMCCConversation *)conversation
                    fromUser:(NSString *)userId
            beforeMessageUid:(long long)messageUid
                       count:(int)count
                     success:(void(^)(NSArray<DMCCFileRecord *> *files))successBlock
                       error:(void(^)(int error_code))errorBlock {
    NSLog(@"getConversationFiles");
}

- (void)getMyFiles:(long long)beforeMessageUid
             count:(int)count
           success:(void(^)(NSArray<DMCCFileRecord *> *files))successBlock
             error:(void(^)(int error_code))errorBlock {
    NSLog(@"getMyFiles");
}

- (void)searchMyFiles:(NSString *)keyword
     beforeMessageUid:(long long)beforeMessageUid
                count:(int)count
              success:(void(^)(NSArray<DMCCFileRecord *> *files))successBlock
                error:(void(^)(int error_code))errorBlock {
    if (!keyword.length) {
        if(errorBlock) {
            errorBlock(-1);
        }
        return;
    }
}

- (void)deleteFileRecord:(long long)messageUid
                 success:(void(^)(void))successBlock
                   error:(void(^)(int error_code))errorBlock {
    NSLog(@"deleteFileRecord");
}
       
- (void)searchFiles:(NSString *)keyword
       conversation:(DMCCConversation *)conversation
           fromUser:(NSString *)userId
   beforeMessageUid:(long long)messageUid
              count:(int)count
            success:(void(^)(NSArray<DMCCFileRecord *> *files))successBlock
              error:(void(^)(int error_code))errorBlock {
    NSLog(@"searchFiles");
    if (!keyword.length) {
        if(errorBlock) {
            errorBlock(-1);
        }
        return;
    }
}

- (void)getAuthorizedMediaUrl:(long long)messageUid
                    mediaType:(DMCCMediaType)mediaType
                    mediaPath:(NSString *)mediaPath
                      success:(void(^)(NSString *authorizedUrl, NSString *backupAuthorizedUrl))successBlock
                        error:(void(^)(int error_code))errorBlock {
    NSLog(@"getAuthorizedMediaUrl");
}

- (NSData *)getWavData:(NSString *)amrPath {
    if ([@"mp3" isEqualToString:[amrPath pathExtension]]) {
        return [NSData dataWithContentsOfFile:amrPath];
    } else {
        NSMutableData *data = [[NSMutableData alloc] init];
        decode_amr([amrPath UTF8String], data);
        return data;
    }
}

- (NSString *)imageThumbPara {
//    std::string cstr = mars::stn::GetImageThumbPara();
//    if (cstr.empty()) {
//        return nil;
//    }
//    return [NSString stringWithUTF8String:cstr.c_str()];
    return nil;
}

- (long)insertMessage:(DMCCMessage *)message {
    NSLog(@"insertMessage");
    [SqliteUtils insertMessage:message];
    return 0;
}

- (int)getMessageCount:(DMCCConversation *)conversation {
    NSLog(@"getMessageCount");
    return 0;
}

- (BOOL)beginTransaction {
    return false;
}

- (void)commitTransaction {
}

- (BOOL)isCommercialServer {
    return false;
}

- (BOOL)isReceiptEnabled {
    return false;
}

- (BOOL)isGlobalDisableSyncDraft {
    return NO;
}

- (void)sendConferenceRequest:(long long)sessionId
                         room:(NSString *)roomId
                      request:(NSString *)request
                         data:(NSString *)data
                      success:(void(^)(NSString *authorizedUrl))successBlock
                        error:(void(^)(int error_code))errorBlock {
    [self sendConferenceRequest:sessionId room:roomId request:request data:data success:successBlock error:errorBlock];
}

- (void)sendConferenceRequest:(long long)sessionId
                         room:(NSString *)roomId
                      request:(NSString *)request
                     advanced:(BOOL)advanced
                         data:(NSString *)data
                      success:(void(^)(NSString *authorizedUrl))successBlock
                        error:(void(^)(int error_code))errorBlock {
}

- (NSArray<NSString *> *)getFavUsers {
    NSDictionary *favUserDict = [[DMCCIMService sharedDMCIMService] getUserSettings:UserSettingScope_Favourite_User];
    NSMutableArray *ids = [[NSMutableArray alloc] init];
    [favUserDict enumerateKeysAndObjectsUsingBlock:^(id  _Nonnull key, id  _Nonnull obj, BOOL * _Nonnull stop) {
        if ([obj isEqualToString:@"1"]) {
            [ids addObject:key];
        }
    }];
    return ids;
}

- (BOOL)isFavUser:(NSString *)userId {
    NSString *strValue = [[DMCCIMService sharedDMCIMService] getUserSetting:UserSettingScope_Favourite_User key:userId];
    if ([strValue isEqualToString:@"1"]) {
        return YES;
    }
    return NO;
}

- (void)setFavUser:(NSString *)userId fav:(BOOL)fav success:(void(^)(void))successBlock error:(void(^)(int errorCode))errorBlock {
    [[DMCCIMService sharedDMCIMService] setUserSetting:UserSettingScope_Favourite_User key:userId value:fav? @"1" : @"0" success:successBlock error:errorBlock];
}

- (void)requireLock:(NSString *)lockId
           duration:(NSUInteger)duration
            success:(void(^)(void))successBlock
              error:(void(^)(int error_code))errorBlock {
}

- (void)releaseLock:(NSString *)lockId
            success:(void(^)(void))successBlock
              error:(void(^)(int error_code))errorBlock {
}

- (NSString*) getUserID{
    return [_osnsdk getUserID];
}
- (NSString*) getShadowKey{
    return [_osnsdk getShadowKey];
}
- (NSString*) hashData:(NSData*)data{
    return [_osnsdk hashData:data];
}
- (NSString*) signData:(NSData*) data {
    return [_osnsdk signData:data];
}
- (Boolean) verifyData:(NSString*) osnID data:(NSData*) data sign:(NSString*) sign{
    return [_osnsdk verifyData:osnID data:data sign:sign];
}
- (NSString*) encryptData:(NSString*) osnID data:(NSData*)data{
    return [_osnsdk encryptData:osnID data:data];
}
- (DMCCLitappInfo*) getLitapp:(NSString *)osnID{
    return [SqliteUtils queryLitapp:osnID];
}
- (DMCCCollectInfo*) getCollectInfo:(NSString *)osnID{
    return [SqliteUtils queryCollectInfoRequest:osnID];
}
- (DMCCWalletInfo*) getWalletInfo:(NSString *)osnID{
    return [SqliteUtils queryWalletInfoRequest:osnID];
}
- (NSArray<DMCCLitappInfo*>*) getLitAppList {
    return [SqliteUtils listLitapps];
}

- (void)saveLitappWithInfo:(DMCCLitappInfo *)info {
    [SqliteUtils insertLitapp:info];
}

- (void)saveTagInfoWithInfo:(DMCCTagInfo *)info {
    [SqliteUtils insertTag:info];
}

- (void)saveWalletInfoWithInfo:(DMCCWalletInfo *)info {
    [SqliteUtils insertWallet:info];
}

- (void)saveCollectInfoWithInfo:(DMCCCollectInfo *)info {
    [SqliteUtils insertCollect:info];
}

- (void)deleteCollectWithInfo:(DMCCCollectInfo *)info {
    [SqliteUtils deleteCollect:info];
}

- (void)deleteTagWithInfo:(DMCCTagInfo *)info {
    [SqliteUtils deleteTag:info];
}

- (void)deleteLitappWithInfo:(DMCCLitappInfo *)info {
    [SqliteUtils deleteLitapp:info.target];
}

- (void)deleteWalletWithInfo:(DMCCWalletInfo *)info {
    [SqliteUtils deleteWallet:info.osnID];
}

- (void)deleteFriendRequestWithTarget:(NSString *)target {
    [SqliteUtils deleteFriendRequest:target];
}

- (void)loginLitappWithInfo:(DMCCLitappInfo *)info
           url:(NSString *)url
            success:(void(^)(NSMutableDictionary *json))successBlock
                      error:(void(^)(int error_code))errorBlock {
    OsnLitappInfo *litapp = [OsnLitappInfo new];
    litapp.target = info.target;
    [_osnsdk lpLogin:litapp url:url cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        if (isSuccess) {
            successBlock(json);
        } else {
            errorBlock(-1);
        }
    }];
}

- (void)settingDescribesWithValue:(NSString *)value cb:(onResult)cb {
    [_osnsdk sendDescribesWithValue:value cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        cb(isSuccess, json, error);
    }];
}

- (void)allowTemporaryChatWithDataValue:(NSString *)value cb:(onResult)cb {
    [_osnsdk allowTemporaryChatWithData:value cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        cb(isSuccess, json, error);
    }];
}

- (void) sendRemoveNFTDescribesWithcb:(onResult)cb {
    [_osnsdk sendRemoveDescribes:@"nft" Withcb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        cb(isSuccess, json, error);
    }];
}

- (DMCCLitappInfo *) getLitappInfoWithTarget:(NSString*) target
               refresh:(bool) refresh
                success:(void(^)(DMCCLitappInfo*))successBlock
                                       error:(void(^)(int error_code))errorBlock {
    DMCCLitappInfo *litappInfo = [self getLitapp:target];
    if (litappInfo == nil || refresh) {
        [_osnsdk getServiceInfo:target cb:^(bool isSuccess, id t, NSString *error) {
            if(isSuccess){
                if([t isMemberOfClass:[OsnLitappInfo class]]){
                    DMCCLitappInfo *litappInfo = [self toClientLitapp:(OsnLitappInfo*)t];
                    successBlock(litappInfo);
                }
            }else{
                if(errorBlock != nil)
                    errorBlock(-1);
            }
        }];
    }
    return litappInfo;
}

- (void) getLitappInfo:(NSString*) target
               refresh:(bool) refresh
               success:(void(^)(DMCCLitappInfo*))successBlock
                 error:(void(^)(int error_code))errorBlock{
    DMCCLitappInfo *litappInfo = [self getLitapp:target];
    if (litappInfo == nil || refresh) {
        [_osnsdk getServiceInfo:target cb:^(bool isSuccess, id t, NSString *error) {
            if(isSuccess){
                if([t isMemberOfClass:[OsnLitappInfo class]]){
                    DMCCLitappInfo *litappInfo = [self toClientLitapp:(OsnLitappInfo*)t];
                    successBlock(litappInfo);
                }
            }else{
                if(errorBlock != nil)
                    errorBlock(-1);
            }
        }];
    }
}

- (void)updataTagWithUserInfo:(NSString*)userId tagName:(NSString *)tagName key:(NSArray *)keys {
    DMCCUserInfo* userInfo = [SqliteUtils queryUser:userId];
    DMCCTagInfo* tagInfo = [SqliteUtils queryTagInfoRequest:tagName];
    for (NSString* k in keys) {
        if ([k isEqualToString:@"tagID"])
            userInfo.tagID = tagInfo.id;
    }
    [SqliteUtils updateUser:userInfo keys:keys];
}

- (void)updataTagUgIDWithUserInfo:(NSString*)userId tagUgId:(NSInteger)tagUgId key:(NSArray *)keys {
    DMCCUserInfo* userInfo = [SqliteUtils queryUser:userId];
    for (NSString* k in keys) {
        if ([k isEqualToString:@"ugID"])
            userInfo.ugID = tagUgId;
    }
    [SqliteUtils updateUser:userInfo keys:keys];
}

- (void)updataTagWithGroupInfo:(NSString*)groupID tagName:(NSString *)tagName key:(NSArray *)keys {
    DMCCGroupInfo *groupInfo = [SqliteUtils queryGroup:groupID];
    DMCCTagInfo* tagInfo = [SqliteUtils queryTagInfoRequest:tagName];
    for (NSString* k in keys) {
        if ([k isEqualToString:@"tagID"])
            groupInfo.tagID = tagInfo.id;
    }
    [SqliteUtils updateGroup:groupInfo keys:keys];
}


- (void)updataTagUgIdWithGroupInfo:(NSString*)groupID tagUgId:(NSInteger)ugId key:(NSArray *)keys {
    DMCCGroupInfo *groupInfo = [SqliteUtils queryGroup:groupID];
    for (NSString* k in keys) {
        if ([k isEqualToString:@"ugID"])
            groupInfo.ugID = ugId;
    }
    [SqliteUtils updateGroup:groupInfo keys:keys];
}

- (NSArray *) getTagList {
    NSMutableArray<DMCCTagInfo *>* outList = [NSMutableArray new];
    NSArray<NSString*>* tagList = [SqliteUtils listTagInfos];
    for(NSString* tagID in tagList){
        DMCCTagInfo* info = [SqliteUtils queryTagIDInfoRequest:tagID];
        [outList addObject:info];
    }
    return outList;
}

- (NSArray *) getWalletInfoList {
    NSMutableArray<DMCCWalletInfo *>* outList = [NSMutableArray new];
    NSArray<NSString*>* walletList = [SqliteUtils listWalletInfos];
    for(NSString* tagID in walletList){
        DMCCWalletInfo* info = [SqliteUtils queryWalletInfoRequest:tagID];
        [outList addObject:info];
    }
    return outList;
}

- (NSArray *) getColletInfoList {
    NSMutableArray<DMCCCollectInfo *>* outList = [NSMutableArray new];
    NSArray<NSString*>* walletList = [SqliteUtils listCollectInfos];
    for(NSString* tagID in walletList){
        DMCCCollectInfo* info = [SqliteUtils queryCollectInfoRequest:tagID];
        [outList addObject:info];
    }
    return outList;
}

- (void)updateUserTagWithkey:(NSArray *)keys {
    NSMutableArray *friendArray = [NSMutableArray arrayWithArray:[[DMCCIMService sharedDMCIMService] getMyFriendList:NO]];
    for (NSString *string in friendArray) {
        [[DMCCIMService sharedDMCIMService] getUserInfo:string refresh:NO success:^(DMCCUserInfo *userInfo) {
            if (userInfo) {
                userInfo.tagID = -1;
                [SqliteUtils updateUser:userInfo keys:keys];
                
                DMCCConversation *conversion = [DMCCConversation conversationWithType:Single_Type target:userInfo.userId line:0];
                DMCCConversationInfo *conversationInfo = [[DMCCIMService sharedDMCIMService] getConversationInfo:conversion];
                if (conversationInfo) {
                    [[DMCCIMService sharedDMCIMService] setConversation:conversationInfo.conversation tagID:-1];
                }
            }
        } error:^(int errorCode) {
         
        }];
    }
}

- (void)updateGroupTagWithkey:(NSArray *)keys {
    
    NSMutableArray *newGroups = [NSMutableArray arrayWithCapacity:0];
    NSArray *groupList = [SqliteUtils listGroups];
    
    for (DMCCGroupInfo *group in groupList) {
        [newGroups addObject:group.target];
    }
    NSArray *ids = [[DMCCIMService sharedDMCIMService] getFavGroups];
    [newGroups addObjectsFromArray:ids];
    
    NSSet *sets = [NSSet setWithArray:newGroups];
    NSArray *groups = [sets allObjects];
    
    for (NSString *groupId in groups) {
        DMCCGroupInfo *groupInfo = [[DMCCIMService sharedDMCIMService] getGroupInfo:groupId refresh:NO];
        if (groupInfo) {
            groupInfo.tagID = -2;
            [SqliteUtils updateGroup:groupInfo keys:keys];
            DMCCConversation *conversion = [DMCCConversation conversationWithType:Group_Type target:groupInfo.target line:0];
            DMCCConversationInfo *conversationInfo = [[DMCCIMService sharedDMCIMService] getConversationInfo:conversion];
            if (conversationInfo) {
                [[DMCCIMService sharedDMCIMService] setConversation:conversationInfo.conversation tagID:-2];
            }
        }
    }
}

- (void)updateUserWithUsers:(NSArray *)users withTagWithkey:(NSArray *)keys {
    for (NSString *string in users) {
        [[DMCCIMService sharedDMCIMService] getUserInfo:string refresh:NO success:^(DMCCUserInfo *userInfo) {
            if (userInfo) {
                userInfo.tagID = -1;
                [SqliteUtils updateUser:userInfo keys:keys];
                
                DMCCConversation *conversion = [DMCCConversation conversationWithType:Single_Type target:userInfo.userId line:0];
                DMCCConversationInfo *conversationInfo = [[DMCCIMService sharedDMCIMService] getConversationInfo:conversion];
                if (conversationInfo) {
                    [[DMCCIMService sharedDMCIMService] setConversation:conversationInfo.conversation tagID:-1];
                }
            }
        } error:^(int errorCode) {
         
        }];
    }
}

- (void)updateGroupWithGroups:(NSArray *)groups withTagWithkey:(NSArray *)keys {
    for (NSString *groupId in groups) {
        DMCCGroupInfo *groupInfo = [[DMCCIMService sharedDMCIMService] getGroupInfo:groupId refresh:NO];
        if (groupInfo) {
            groupInfo.tagID = -2;
            [SqliteUtils updateGroup:groupInfo keys:keys];
            DMCCConversation *conversion = [DMCCConversation conversationWithType:Group_Type target:groupInfo.target line:0];
            DMCCConversationInfo *conversationInfo = [[DMCCIMService sharedDMCIMService] getConversationInfo:conversion];
            if (conversationInfo) {
                [[DMCCIMService sharedDMCIMService] setConversation:conversationInfo.conversation tagID:-2];
            }
        }
    }
}

- (void)saveGroup:(NSString*)groupID status:(int)status cb:(onResult)cb {
    [_osnsdk saveGroup:groupID status:status cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
        cb(isSuccess, json, error);
    }];
}

- (void)downWithMessage:(DMCCMessage *)message cb:(onResult)cb progress:(onProgress)progress {
    
    if ([message.content isKindOfClass:[DMCCVideoMessageContent class]]) {
        DMCCVideoMessageContent *videoContent = (DMCCVideoMessageContent *)message.content;
        
        NSString *url = videoContent.remoteUrl;
        NSString *decKey = videoContent.decKey;
        
        NSString *caches = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES).firstObject;
        NSString* path = [NSString stringWithFormat:@"%@/%ld%@",caches, [OsnUtils getTimeStamp], videoContent.name];
                    
         if ([url hasSuffix:@".zip"] || decKey.length > 0) {
             [_osnsdk downloadData:url localPath:path decKey:decKey cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
                 
                 NSMutableDictionary *dic = [NSMutableDictionary new];
                 
                 dic[@"localPath"] = path;
                 dic[@"remoteUrl"] = videoContent.remoteUrl;
                 dic[@"decKey"] = videoContent.decKey;
                 dic[@"name"] = videoContent.name;
                 
                 [SqliteUtils updateMessage:message.messageId state:message.status msgText:[OsnUtils dic2Json:dic]];
                 
                 cb(isSuccess, json, error);
                 
             } progress:progress];
         } else {
             [_osnsdk downloadData:url localPath:path cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
                 
                 NSMutableDictionary *dic = [NSMutableDictionary new];
                 
                 dic[@"localPath"] = path;
                 dic[@"remoteUrl"] = videoContent.remoteUrl;
                 dic[@"decKey"] = videoContent.decKey;
                 dic[@"name"] = videoContent.name;
                 
                 [SqliteUtils updateMessage:message.messageId state:message.status msgText:[OsnUtils dic2Json:dic]];
                 
                 cb(isSuccess, json, error);
             } progress:progress];
         }
    } else if ([message.content isKindOfClass:[DMCCImageMessageContent class]]) {
        
        DMCCImageMessageContent *imageContent = (DMCCImageMessageContent *)message.content;
        
        NSString *url = imageContent.remoteUrl;
        NSString *decKey = imageContent.decKey;
        
        NSString *caches = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES).firstObject;
        NSString* path = [NSString stringWithFormat:@"%@/%ld%@",caches, [OsnUtils getTimeStamp], imageContent.name];
                    
         if ([url hasSuffix:@".zip"] || decKey.length > 0) {
             [_osnsdk downloadData:url localPath:path decKey:decKey cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
                 
                 NSMutableDictionary *dic = [NSMutableDictionary new];
                 
                 dic[@"localPath"] = path;
                 dic[@"remoteUrl"] = imageContent.remoteUrl;
                 dic[@"decKey"] = imageContent.decKey;
                 dic[@"name"] = imageContent.name;
                 
                 [SqliteUtils updateMessage:message.messageId state:message.status msgText:[OsnUtils dic2Json:dic]];
                 
                 cb(isSuccess, json, error);
                 
             } progress:progress];
         } else {
             [_osnsdk downloadData:url localPath:path cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
                 
                 NSMutableDictionary *dic = [NSMutableDictionary new];
                 
                 dic[@"localPath"] = path;
                 dic[@"remoteUrl"] = imageContent.remoteUrl;
                 dic[@"decKey"] = imageContent.decKey;
                 dic[@"name"] = imageContent.name;
                 
                 [SqliteUtils updateMessage:message.messageId state:message.status msgText:[OsnUtils dic2Json:dic]];
                 
                 cb(isSuccess, json, error);
             } progress:progress];
         }
        
    } else if ([message.content isKindOfClass:[DMCCFileMessageContent class]]) {
        
        DMCCFileMessageContent *fileContent = (DMCCFileMessageContent *)message.content;
        
        NSString *url = fileContent.remoteUrl;
        NSString *decKey = fileContent.decKey;
        
        NSString *caches = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES).firstObject;
        NSString* path = [NSString stringWithFormat:@"%@/%ld%@",caches, [OsnUtils getTimeStamp], fileContent.name];
                    
         if ([url hasSuffix:@".zip"] || decKey.length > 0) {
             [_osnsdk downloadData:url localPath:path decKey:decKey cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
                 
                 NSMutableDictionary *dic = [NSMutableDictionary new];
                 
                 dic[@"localPath"] = path;
                 dic[@"remoteUrl"] = fileContent.remoteUrl;
                 dic[@"decKey"] = fileContent.decKey;
                 dic[@"name"] = fileContent.name;
                 dic[@"size"] = @(fileContent.size);
                 
                 [SqliteUtils updateMessage:message.messageId state:message.status msgText:[OsnUtils dic2Json:dic]];
                 
                 cb(isSuccess, json, error);
                 
             } progress:progress];
         } else {
             [_osnsdk downloadData:url localPath:path cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
                 
                 NSMutableDictionary *dic = [NSMutableDictionary new];
                 
                 dic[@"localPath"] = path;
                 dic[@"remoteUrl"] = fileContent.remoteUrl;
                 dic[@"decKey"] = fileContent.decKey;
                 dic[@"name"] = fileContent.name;
                 dic[@"size"] = @(fileContent.size);
                 
                 [SqliteUtils updateMessage:message.messageId state:message.status msgText:[OsnUtils dic2Json:dic]];
                 
                 cb(isSuccess, json, error);
             } progress:progress];
         }
        
    } else if ([message.content isKindOfClass:[DMCCSoundMessageContent class]]) {
        
        NSDateFormatter *formatter = [[NSDateFormatter alloc]init];
        formatter.dateFormat = @"yyyyMMddHHmmss";
        NSString *str = [formatter stringFromDate:[NSDate date]];
        NSString *fileName = [NSString stringWithFormat:@"%@.mp3",str];
        
        DMCCSoundMessageContent *soundContent = (DMCCSoundMessageContent *)message.content;
        
        NSString *url = soundContent.remoteUrl;
        NSString *decKey = soundContent.decKey;
        
        NSString *caches = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES).firstObject;
        NSString* path = [NSString stringWithFormat:@"%@/%ld%@",caches, [OsnUtils getTimeStamp], fileName];
                    
         if ([url hasSuffix:@".zip"] || decKey.length > 0) {
             [_osnsdk downloadData:url localPath:path decKey:decKey cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
                 
                 NSMutableDictionary *dic = [NSMutableDictionary new];
                 
                 dic[@"localPath"] = path;
                 dic[@"remoteUrl"] = soundContent.remoteUrl;
                 dic[@"decKey"] = soundContent.decKey;
                 dic[@"duration"] = @(soundContent.duration);
                 
                 [SqliteUtils updateMessage:message.messageId state:message.status msgText:[OsnUtils dic2Json:dic]];
                 
                 cb(isSuccess, json, error);
                 
             } progress:progress];
         } else {
             [_osnsdk downloadData:url localPath:path cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
                 
                 NSMutableDictionary *dic = [NSMutableDictionary new];
                 
                 dic[@"localPath"] = path;
                 dic[@"remoteUrl"] = soundContent.remoteUrl;
                 dic[@"decKey"] = soundContent.decKey;
                 dic[@"duration"] = @(soundContent.duration);
                 
                 [SqliteUtils updateMessage:message.messageId state:message.status msgText:[OsnUtils dic2Json:dic]];
                 
                 cb(isSuccess, json, error);
             } progress:progress];
         }
        
    }
    
}

@end
