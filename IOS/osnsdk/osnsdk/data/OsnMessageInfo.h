@interface OsnMessageInfo : NSObject
@property NSString* userID;
@property NSString* target;
@property NSString* content;
@property long timeStamp;
@property Boolean isGroup;
@property NSString* originalUser;
@property NSString* hashx;
@property NSString* hasho;
+ (OsnMessageInfo*)toMessage:(NSMutableDictionary*)data;
@end
