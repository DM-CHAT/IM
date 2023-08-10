@interface OsnLitappInfo : OsnServiceInfo
@property NSString* target;
@property NSString* name;
@property NSString* displayName;
@property NSString* portrait;
@property NSString* theme;
@property NSString* url;
@property NSString* info;
+ (OsnLitappInfo*)toLitappInfo:(NSDictionary*) json;
@end
