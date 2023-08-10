#import <Foundation/Foundation.h>
#import "OsnUserInfo.h"
#import "OsnGroupInfo.h"
#import "OsnFriendInfo.h"
#import "OsnMemberInfo.h"
#import "OsnMessageInfo.h"
#import "OsnRequestInfo.h"
#import "OsnIMInfo.h"
#import "OsnLitappInfo.h"
#import "OsnServiceInfo.h"
#import "OSNGeneralCallback.h"
#import "OSNListener.h"
#import "SYNCallback.h"
#import "EcUtils.h"
#import "HttpUtils.h"
#import "OsnUtils.h"

@interface OsnSDK : NSObject
+ (OsnSDK*) getInstance;
- (bool) syncFriend;
- (bool) syncGroup;
- (void) findObject:(NSString*) text cb:(onResult)cb;
- (void) createTempAccount;
- (void) initSDK:(NSString*) ip cb:(id<OSNListener>)cb;
- (void) resetHost:(NSString*) ip;
- (void) registerUser:(NSString*) userName pwd:(NSString*) password sid:(NSString*) serviceID cb:(onResult)cb;
- (bool) loginWithOsnID:(NSString *)userID cb:(onResult)cb;
- (bool) loginWithName:(NSString *)userName pwd:(NSString *)password cb:(onResult)cb;
- (bool) loginV2:(NSString *)userName pwd:(NSString *)password decPwd:(NSString *)decPwd cb:(onResult)cb;
- (void) logout:(onResult)cb;
- (NSString*) getUserID;
- (NSString*) getServiceID;
- (NSString*) getShadowKey;
- (OsnUserInfo*) getUserInfo:(NSString*) userID cb:(onResultT)cb;
- (OsnGroupInfo*) getGroupInfo:(NSString*) groupID cb:(onResultT)cb;
- (NSArray*) getMemberInfo:(NSString*) groupID cb:(onResultT)cb;
- (NSArray*) getMemberZoneInfo:(NSString*) groupID begin:(int)begin  cb:(onResultT)cb;
- (OsnServiceInfo*) getServiceInfo:(NSString*) serviceID cb:(onResultT)cb;
- (OsnFriendInfo*) getFriendInfo:(NSString*) friendID cb:(onResultT)cb;
- (void) modifyUserInfo:(NSArray*) keys info:(OsnUserInfo*) userInfo cb:(onResult)cb;
- (void) modifyFriendInfo:(NSArray*) keys info:(OsnFriendInfo*) friendInfo cb:(onResult)cb;
- (NSArray*) getFriendList:(onResultT)cb;
- (NSArray*) getGroupList:(onResultT)cb;
- (void) inviteFriend:(NSString*) userID reason:(NSString*) reason cb:(onResult)cb;
- (void) deleteFriend:(NSString*) userID cb:(onResult)cb;
- (void)roleAddFriend:(NSString*)type cb:(onResult)cb;
- (void) acceptFriend:(NSString*) userID cb:(onResult)cb;
- (void) rejectFriend:(NSString*) userID cb:(onResult)cb;
- (void) acceptMember:(NSString*) userID groupID:(NSString*)groupID cb:(onResult)cb;
- (void) rejectMember:(NSString*) userID groupID:(NSString*)groupID cb:(onResult)cb;
- (void) orderPay:(NSString*) info cb:(onResult)cb;
- (void) getOwnerSign:(NSString*) groupID cb:(onResult)cb;
- (void) getGroupSign:(NSString*) groupID info:(NSString*) info cb:(onResult)cb;
- (void) setGroupOwner:(NSString*) groupID owner:(NSString*) owner cb:(onResult)cb;
- (void) addGroupManager:(NSString*) groupID memberIds:(NSArray<NSString*>*) memberIds cb:(onResult)cb;
- (void) delGroupManager:(NSString*) groupID memberIds:(NSArray<NSString*>*) memberIds cb:(onResult)cb;
- (void) getRedPacket:(NSString*) info userID:(NSString*) userID cb:(onResult)cb;
- (void) sendMessage:(NSString*) text userID:(NSString*) userID cb:(onResult)cb;
- (void) sendDynamic:(NSString*) text cb:(onResult)cb;
- (void) sendBroadcast:(NSString*) content cb:(onResult)cb;
- (void) deleteMessage:(NSString*) hash osnID:(NSString*) osnID cb:(onResult)cb;
- (void) deleteToMessage:(NSString*) hash osnID:(NSString*) osnID cb:(onResult)cb;
- (void) recallMessage:(NSString*) hash osnID:(NSString*) osnID cb:(onResult)cb;
- (NSArray<OsnMessageInfo*>*) loadMessage:(NSString*) userID timestamp:(long)timestamp count:(int)count before:(bool) before cb:(onResultT)cb;
- (void) createGroup:(NSString*) groupName membser:(NSArray*) member type:(int)type portrait:(NSString*) portrait cb:(onResult)cb;
- (void) joinGroup:(NSString*) groupID reason:(NSString*)reason invitation:(NSString*)invitation cb:(onResult)cb;
- (void) addMember:(NSString*) groupID members:(NSArray*) members cb:(onResult)cb;
- (void) delMember:(NSString*) groupID members:(NSArray*) members cb:(onResult)cb;
- (void) quitGroup:(NSString*) groupID cb:(onResult)cb;
- (void) rejectGroup:(NSString*) groupID cb:(onResult)cb;
- (void) dismissGroup:(NSString*) groupID cb:(onResult)cb;
- (void) allowAddFriend:(NSString*)groupID type:(int)type cb:(onResult)cb;
- (void) muteGroup:(NSString*) groupID state:(int) state members:(NSArray<NSString*>*) members mode:(int)mode cb:(onResult)cb;
- (void) billboard:(NSString*) groupID text:(NSString*) text cb:(onResult)cb;
- (void) modifyGroupInfo:(NSArray*) keys groupInfo:(OsnGroupInfo*) groupInfo cb:(onResult)cb;
- (void) modifyMemberInfo:(NSArray*) keys memberInfo:(OsnMemberInfo*) memberInfo cb:(onResult)cb;
- (void) uploadData:(NSString*) fileName type:(NSString*)type data:(NSData*) data cb:(onResult)cb progress:(onProgress)progress;
- (void) downloadData:(NSString*) remoteUrl localPath:(NSString*) localPath decKey:(NSString *)decKey cb:(onResult)cb progress:(onProgress)progress;
- (void) downloadData:(NSString*) remoteUrl localPath:(NSString*) localPath cb:(onResult)cb progress:(onProgress)progress;
- (void) lpLogin:(OsnLitappInfo*) litappInfo url:(NSString*) url cb:(onResult)cb;
- (void) simpleLogin:(NSString*) target url:(NSString*) url cb:(onResult)cb;
- (void) setTempAccount:(NSString*) tempAcc cb:(onResult)cb;
- (void) sendDescribesWithValue:(NSString *)value cb:(onResult)cb;
- (void) allowTemporaryChatWithData:(NSString *)value cb:(onResult)cb;
- (void) sendRemoveDescribes:(NSString *)command Withcb:(onResult)cb;
- (void) upAttirbuteWithGroupId:(NSString*)groupID key:(NSString *)key value:(NSString *)value cb:(onResult)cb;
- (NSString*) hashData:(NSData*) data;
- (NSString*) signData:(NSData*) data;
- (Boolean) verifyData:(NSString*) osnID data:(NSData*)data sign:(NSString*) sign;
- (NSString*) encryptData:(NSString*) osnID data:(NSData*) data;
- (void) removeAttirbuteWithGroupId:(NSString*)groupID key:(NSString *)key cb:(onResult)cb;
- (void) removeAttirbuteWithGroupId:(NSString*)groupID keys:(NSArray *)keys cb:(onResult)cb;
- (void)saveGroup:(NSString*)groupID status:(int)status cb:(onResult)cb;
- (void) upExtraWithGroupId:(NSString*)groupID key:(NSString *)key value:(NSString *)value cb:(onResult)cb;
- (void) upGroupPwdWithGroupId:(NSString*)groupID key:(NSString *)key value:(NSString *)value cb:(onResult)cb;
@end
 
