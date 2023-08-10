@interface OsnUserInfo : NSObject
@property NSString* userID;
@property NSString* name;
@property NSString* displayName;
@property NSString* portrait;
@property NSString* urlSpace;
@property NSString* describes;
@property NSString* role;
+ (OsnUserInfo*)toUserInfo:(NSDictionary*) json;
@end
