#import <osnsdk/OsnMemberInfo.h>

@interface OsnGroupInfo : NSObject
@property NSString* groupID;
@property NSString* name;
@property NSString* privateKey;
@property NSString* isMember;
@property NSString* owner;
@property NSString* portrait;
@property NSString* attribute;
@property NSString* billboard;
@property NSString* invitor;
@property NSString* approver;
@property NSString* extra;
@property NSString* noticeServerTime;
@property NSDictionary* data;
@property NSMutableDictionary* notice;
@property int memberCount;
@property int type;
@property int joinType;
@property int passType;
@property int mute;
@property int singleMute;
@property NSMutableArray<OsnMemberInfo*>* userList;
+ (OsnGroupInfo*)toGroupInfo:(NSDictionary*) json;
- (long) getNoticeServerTime;
- (OsnMemberInfo*)hasMember:(NSString*) osnID;
@end
