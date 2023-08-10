@interface OsnRequestInfo : NSObject
@property NSString* reason;
@property NSString* userID;
@property NSString* friendID;
@property NSString* originalUser;
@property NSString* targetUser;
@property NSDictionary* data;
@property long timeStamp;
@property Boolean isGroup;
@property Boolean isApply;
@end
