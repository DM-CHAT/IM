#define MemberType_Wait   0
#define MemberType_Normal 1
#define MemberType_Owner  2
#define MemberType_Admin  3

@interface OsnMemberInfo : NSObject
@property NSString* osnID;
@property NSString* groupID;
@property NSString* remarks;
@property NSString* nickName;
@property int type;
@property int mute;
@property int status;
+ (OsnMemberInfo*)toMemberInfo:(NSDictionary*)json;
+ (NSArray<OsnMemberInfo*>*)toMemberInfos:(NSDictionary*)json;
@end
