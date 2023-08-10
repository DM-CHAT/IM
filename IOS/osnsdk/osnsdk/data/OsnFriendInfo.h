#define FriendState_Wait    0
#define FriendState_Normal  1
#define FriendState_Deleted 2
#define FriendState_Blacked 3
#define FriendState_Syncst  4

@interface OsnFriendInfo : NSObject
@property NSString* userID;
@property NSString* friendID;
@property NSString* remarks;
@property int state;
+ (OsnFriendInfo*) init:(NSString*)userID friendID:(NSString*)friendID state:(int)state;
+ (OsnFriendInfo*) toFriendInfo:(NSDictionary*) json;
@end
