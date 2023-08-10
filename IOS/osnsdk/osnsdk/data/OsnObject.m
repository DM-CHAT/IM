
#import <Foundation/Foundation.h>
#import "OsnMemberInfo.h"
#import "OsnRequestInfo.h"
#import "OsnMessageInfo.h"
#import "OsnFriendInfo.h"
#import "OsnGroupInfo.h"
#import "OsnUserInfo.h"
#import "serviceInfo/OsnServiceInfo.h"
#import "serviceInfo/OsnIMInfo.h"
#import "serviceInfo/OsnLitappInfo.h"

@implementation OsnUserInfo

+ (OsnUserInfo*) toUserInfo:(NSMutableDictionary*) json{
    if(json == nil)
        return nil;
    OsnUserInfo *userInfo = [OsnUserInfo new];
    userInfo.userID = json[@"userID"];
    userInfo.name = json[@"name"];
    userInfo.displayName = json[@"displayName"];
    userInfo.portrait = json[@"portrait"];
    userInfo.urlSpace = json[@"urlSpace"];
    userInfo.describes = json[@"describes"];
    if (json[@"role"] !=nil) {
        userInfo.role = json[@"role"];
    }
    return userInfo;
}

@end

@implementation OsnMemberInfo
+ (OsnMemberInfo*)toMemberInfo:(NSMutableDictionary*)json{
    OsnMemberInfo *memberInfo = [OsnMemberInfo new];
    memberInfo.osnID = json[@"osnID"];
    memberInfo.groupID = json[@"groupID"];
    memberInfo.nickName = json[@"nickName"];
    memberInfo.remarks = json[@"remarks"];
    memberInfo.mute = ((NSNumber*)json[@"mute"]).intValue;
    memberInfo.type = ((NSString*)json[@"type"]).intValue;
    return memberInfo;
}
+ (NSArray<OsnMemberInfo*>*)toMemberInfos:(NSMutableDictionary*)json{
    NSArray<NSMutableDictionary*> *array = json[@"userList"];
    NSMutableArray<OsnMemberInfo*> *members = [NSMutableArray new];
    if(array != nil){
        for(NSMutableDictionary* o in array)
            [members addObject:[OsnMemberInfo toMemberInfo:o]];
    }
    return members;
}
@end

@implementation OsnGroupInfo

- (instancetype)init{
    self = [super init];
    self.userList = [NSMutableArray new];
    return self;
}
+ (OsnGroupInfo*) toGroupInfo:(NSMutableDictionary*) json{
    if(json == nil)
        return nil;
    OsnGroupInfo *groupInfo = [OsnGroupInfo new];
    groupInfo.groupID = json[@"groupID"];
    if(groupInfo.groupID == nil)
        groupInfo.groupID = json[@"receive_from"];
    groupInfo.name = json[@"name"];
    groupInfo.privateKey = @"";
    groupInfo.owner = json[@"owner"];
    groupInfo.type = ((NSNumber*)json[@"type"]).intValue;
    groupInfo.joinType = ((NSNumber*)json[@"joinType"]).intValue;
    groupInfo.passType = ((NSNumber*)json[@"passType"]).intValue;
    groupInfo.mute = ((NSNumber*)json[@"mute"]).intValue;
    groupInfo.singleMute = ((NSNumber*)json[@"singleMute"]).intValue;
    groupInfo.portrait = json[@"portrait"];
    groupInfo.attribute = json[@"attribute"];
    groupInfo.billboard = json[@"billboard"];
    groupInfo.memberCount = ((NSNumber*)json[@"memberCount"]).intValue;
    groupInfo.invitor = json[@"invitor"];
    groupInfo.approver = json[@"approver"];
    groupInfo.isMember = json[@"isMember"];
    groupInfo.extra = json[@"extra"];
    groupInfo.data = json;
    NSArray<NSDictionary*> *array = json[@"userList"];
    
    if(array != nil){
        if ([array.firstObject isKindOfClass:[NSDictionary class]]) {
            for(NSDictionary *o in array){
                OsnMemberInfo *memberInfo = [OsnMemberInfo new];
                memberInfo.osnID = o[@"osnID"];
                memberInfo.mute = [o[@"mute"] intValue];
                memberInfo.nickName = o[@"nickName"];
                memberInfo.groupID = o[@"groupID"];
                memberInfo.type = [o[@"type"] intValue];
                [groupInfo.userList addObject:memberInfo];
            }
        }
    }
    
    return groupInfo;
}



- (OsnMemberInfo*) hasMember:(NSString*) osnID{
    for(OsnMemberInfo* m in self.userList){
        if([osnID isEqualToString:m.osnID])
            return m;
    }
    return nil;
}

- (long) getNoticeServerTime{
    if (self.notice == nil) {
        return [[NSDate date] timeIntervalSince1970] * 1000;
    }
    
    NSString * timestamp = self.notice[@"timestamp"];
    if (timestamp == nil) {
        return [[NSDate date] timeIntervalSince1970] * 1000;
    }
    
    return timestamp.longLongValue;
}
@end

@implementation OsnFriendInfo

+ (OsnFriendInfo*) init:(NSString*) userID  friendID:(NSString*) friendID state:(int) state{
    OsnFriendInfo *friendInfo = [OsnFriendInfo new];
    friendInfo.userID = userID;
    friendInfo.friendID = friendID;
    friendInfo.state = state;
    return friendInfo;
}
+ (OsnFriendInfo*) toFriendInfo:(NSDictionary*) json{
    OsnFriendInfo *friendInfo = [OsnFriendInfo new];
    friendInfo.userID = json[@"userID"];
    friendInfo.friendID = json[@"friendID"];
    friendInfo.remarks = json[@"remarks"];
    friendInfo.state = ((NSNumber*)json[@"state"]).intValue;
    return friendInfo;
}

@end

@implementation OsnMessageInfo
+ (OsnMessageInfo*)toMessage:(NSMutableDictionary*)data{
    OsnMessageInfo *messageInfo = [OsnMessageInfo new];
    messageInfo.userID = data[@"receive_from"];
    messageInfo.target = data[@"receive_to"];
    messageInfo.timeStamp = ((NSString*)data[@"receive_timestamp"]).longLongValue;
    messageInfo.content = [data[@"content"] stringByReplacingOccurrencesOfString:@"'" withString:@"’"];
    if(messageInfo.userID != nil)
        messageInfo.isGroup = [messageInfo.userID isEqualToString:@"OSNG"];
    else
        messageInfo.isGroup = false;
    messageInfo.originalUser = data[@"originalUser"];
    messageInfo.hashx = data[@"receive_hash"];
    if (data[@"receive_hasho"]) {
        messageInfo.hasho = data[@"receive_hasho"];
    } else {
        messageInfo.hasho = data[@"receive_hash"];
    }
    return messageInfo;
}
@end

@implementation OsnRequestInfo
@end

@implementation OsnServiceInfo
+ (OsnServiceInfo*)toServiceInfo:(NSDictionary*) json{
    NSString *type = json[@"type"];
    if(type == nil)
        type = @"Litapp";
    if([type isEqualToString:@"IMS"])
        return [OsnIMInfo toIMInfo:json];
    else if([type isEqualToString:@"Litapp"])
        return [OsnLitappInfo toLitappInfo:json];
    return nil;
}
+ (NSArray<OsnServiceInfo*>*) toServiceInfos:(NSDictionary*) json{
    NSMutableArray<OsnServiceInfo*>* infos = [NSMutableArray new];
    NSArray* litapps = json[@"litapps"];
    for(NSDictionary* o in litapps){
        [infos addObject:[OsnLitappInfo toServiceInfo:o]];
    }
    return infos;
}
@end

@implementation OsnIMInfo
+ (OsnIMInfo*)toIMInfo:(NSDictionary*) json{
    OsnIMInfo *info = [OsnIMInfo new];
    info.type = json[@"type"];
    info.urlSpace = json[@"urlSpace"];
    return info;
}
@end

@implementation OsnLitappInfo
+ (OsnLitappInfo*)toLitappInfo:(NSDictionary*) json{
    OsnLitappInfo *info = [OsnLitappInfo new];
    info.type = json[@"type"];
    info.target = json[@"target"];
    info.name = json[@"name"];
    info.displayName = json[@"displayName"];
    info.portrait = json[@"portrait"];
    info.theme = json[@"theme"];
    info.url = json[@"url"];
    info.info = json[@"info"];
    return info;
}
@end
