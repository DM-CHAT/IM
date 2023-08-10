@interface OsnUtils : NSObject
+ (NSString*) b64Encode:(NSData*)data;
+ (NSData*) b64Decode:(NSString*)data;
+ (NSString*) dic2Json:(NSDictionary*)json;
+ (NSMutableDictionary*)json2Dic:(NSData*)data;
+ (NSMutableDictionary*)json2Dics:(NSString*)data;
+ (NSData*) sha256:(NSData*) data;
+ (long) getTimeStamp;
+ (NSString*) createUUID;
+ (NSString*) aesEncrypt:(NSData*)data keyData:(NSData*) key;
+ (NSString*) aesEncrypt:(NSString*) data keyStr:(NSString*) key;
+ (NSData*) aesDecrypt:(NSData*) data keyData:(NSData*) key;
+ (NSString*) aesDecrypt:(NSString*) data keyStr:(NSString*) key;
+ (NSMutableDictionary*) wrapMessage:(NSString*) command from:(NSString*) from to:(NSString*) to data:(NSMutableDictionary*) data key:(NSString*) key;
+ (NSMutableDictionary*) makeMessage:(NSString*) command from:(NSString*) from to:(NSString*) to data:(NSDictionary*) data key:(NSString*) key;
+ (NSMutableDictionary*) takeMessage:(NSMutableDictionary*) json key:(NSString*) key osnID:(NSString*)osnID;
@end
