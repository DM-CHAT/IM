@interface OsnServiceInfo : NSObject
@property NSString* type;
+ (OsnServiceInfo*)toServiceInfo:(NSDictionary*) json;
+ (NSArray<OsnServiceInfo*>*)toServiceInfos:(NSDictionary*) json;
@end
