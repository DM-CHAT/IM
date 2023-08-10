@protocol OSNListener <NSObject>
- (void) onConnectSuccess:(NSString*) state;
- (void) onConnectFailed:(NSString*) error;
- (NSArray *) onRecvMessage:(NSArray<OsnMessageInfo*>*) msgList;
- (void) onRecvRequest:(OsnRequestInfo*) request;
- (void) onRecvUserInfo:(OsnUserInfo*) userInfo;
- (void) onFriendUpdate:(NSArray<OsnFriendInfo*>*) userIDList;
- (void) onUserUpdate:(OsnUserInfo*) userInfo keys:(NSArray<NSString*>*) keys;
- (void) onGroupUpdate:(NSString*) state info:(OsnGroupInfo*) groupInfo keys:(NSArray<NSString*>*) keys;
- (void) onServiceInfo:(NSArray<OsnServiceInfo*>*) infos;
- (void) onFindResult:(NSArray<OsnLitappInfo*>*) infos;
- (void) onReceiveRecall:(NSDictionary *)data;
- (void) onDeleteMessageTo:(NSDictionary *)data;
- (NSString*) getConfig:(NSString*) key;
- (void) setConfig:(NSString*) key data:(NSString*) value;
@end
