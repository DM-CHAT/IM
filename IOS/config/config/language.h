#define LocalizedString(key) [OSNLanguage getStringForKey:key withTable:@"InfoPlist"]
#define DMCString(key) [OSNLanguage getStringForKey:key withTable:@"DMC"]
#define DMCCString(key) [OSNLanguage getStringForKey:key withTable:@"client"]

#import <Foundation/Foundation.h>

@interface OSNLanguage : NSObject

+(NSString *)getStringForKey:(NSString *)key withTable:(NSString *)table;
+(void)setLanguage:(NSString*)language;
+(NSString*)getBaseLanguage;
+(NSString*)getLanguage;

@end
