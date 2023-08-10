#import <osnsdk/OsnServiceInfo.h>

@interface OsnIMInfo : OsnServiceInfo
@property NSString* urlSpace;
+ (OsnIMInfo*)toIMInfo:(NSDictionary*) json;
@end
