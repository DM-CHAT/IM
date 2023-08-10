//
//  DMCCNetworkService.mm
//  DMChatClient
//
//  Created by heavyrain on 2017/11/5.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#include "DMCCNetworkService.h"
#import <UIKit/UIKit.h>
#import <SystemConfiguration/SCNetworkReachability.h>
#import <sys/xattr.h>
#import <CommonCrypto/CommonDigest.h>

#import "app_callback.h"
#include <list>
#import "DMCCIMService.h"
#import "DMCCNetworkStatus.h"
#import "DMCCRecallMessageContent.h"
#import "DMCCCallMessageContent.h"
#import "SqliteUtils.h"
#import "../Utility/DMCCUtilities.h"
#import <config/config.h>
#import <AudioToolbox/AudioToolbox.h>

const NSString *SDKVERSION = @"0.1";

NSString *kGroupInfoUpdated = @"kGroupInfoUpdated";
NSString *kGroupMemberUpdated = @"kGroupMemberUpdated";
NSString *kGroupMemberAddOrLessUpdated = @"kGroupMemberAddOrLessUpdated";
NSString *kUserInfoUpdated = @"kUserInfoUpdated";
NSString *kFriendListUpdated = @"kFriendListUpdated";
NSString *kFriendRequestUpdated = @"kFriendRequestUpdated";
NSString *kSettingUpdated = @"kSettingUpdated";
NSString *kChannelInfoUpdated = @"kChannelInfoUpdated";
NSString *kDeleteMsgInfoUpdated = @"deleteMsgInfoUpdated";
NSString *kRecallMsgInfoUpdated = @"recallMsgInfoUpdated";
NSString *kLoginOutKikoffUpdated = @"kLoginOutKikoffUpdated";

@protocol RefreshGroupInfoDelegate <NSObject>
- (void)onGroupInfoUpdated:(NSArray<DMCCGroupInfo *> *)updatedGroupInfo;
@end

@protocol RefreshGroupMemberDelegate <NSObject>
- (void)onGroupMemberUpdated:(NSString *)groupId members:(NSArray<DMCCGroupMember *> *)updatedGroupMembers;
@end

@protocol RefreshChannelInfoDelegate <NSObject>
- (void)onChannelInfoUpdated:(NSArray<DMCCChannelInfo *> *)updatedChannelInfo;
@end

@protocol RefreshUserInfoDelegate <NSObject>
- (void)onUserInfoUpdated:(NSArray<DMCCUserInfo *> *)updatedUserInfo;
@end

@protocol RefreshFriendListDelegate <NSObject>
- (void)onFriendListUpdated;
@end

@protocol RefreshFriendRequestDelegate <NSObject>
- (void)onFriendRequestUpdated:(NSArray<NSString *> *)newFriendRequests;
@end

@protocol RefreshSettingDelegate <NSObject>
- (void)onSettingUpdated;
@end

@interface DMCCNetworkService () <ConnectionStatusDelegate, ReceiveMessageDelegate, RefreshUserInfoDelegate, RefreshGroupInfoDelegate, DMCCNetworkStatusDelegate, RefreshFriendListDelegate, RefreshFriendRequestDelegate, RefreshSettingDelegate, RefreshChannelInfoDelegate, RefreshGroupMemberDelegate, ConferenceEventDelegate,
OSNListener> {
    
    dispatch_semaphore_t msgSem;
    dispatch_queue_t msgQueue;

}
@property(nonatomic, assign)ConnectionStatus currentConnectionStatus;
//@property(nonatomic, strong)NSString *userId;
//@property(nonatomic, strong)NSString *passwd;

@property(nonatomic, strong)NSString *serverHost;

@property(nonatomic, assign)UIBackgroundTaskIdentifier bgTaskId;
@property(nonatomic, strong)NSTimer *forceConnectTimer;
@property(nonatomic, strong)NSTimer *suspendTimer;
@property(nonatomic, strong)NSTimer *endBgTaskTimer;
@property(nonatomic, strong)NSString *deviceToken;
@property(nonatomic, strong)NSString *voipDeviceToken;

@property(nonatomic, assign)BOOL requestProxying;
@property(nonatomic, strong) NSMutableArray *messageFilterList;
@property(nonatomic, strong) NSMutableArray *messageMaps;

@property(nonatomic, assign)BOOL deviceTokenUploaded;
@property(nonatomic, assign)BOOL voipDeviceTokenUploaded;
- (void)reportEvent_OnForeground:(BOOL)isForeground;

@property(nonatomic, assign)NSUInteger backgroudRunTime;
@property (nonatomic, strong) NSObject *lock;

@end

@implementation DMCCNetworkService
NSMutableArray<NSString*> *keywords = nil;
dispatch_queue_t workQueue = dispatch_queue_create("client.worker", DISPATCH_QUEUE_CONCURRENT);
static DMCCNetworkService * sharedSingleton = nil;
+ (void)startLog {
    NSString* logPath = [[NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0] stringByAppendingString:@"/log"];
    
    // set do not backup for logpath
    const char* attrName = "com.apple.MobileBackup";
    u_int8_t attrValue = 1;
    setxattr([logPath UTF8String], attrName, &attrValue, sizeof(attrValue), 0, 0);
    
    // init xlog
//#if DEBUG
//    xlogger_SetLevel(kLevelVerbose);
//    appender_set_console_log(true);
//#else
//    xlogger_SetLevel(kLevelInfo);
//    appender_set_console_log(false);
//#endif
//    appender_open(kAppednerAsync, [logPath UTF8String], "Test", NULL);
}

+ (void)stopLog {
//    appender_close();
}

+ (NSArray<NSString *> *)getLogFilesPath {
    NSString* logPath = [[NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0] stringByAppendingString:@"/log"];
    
    NSFileManager *myFileManager = [NSFileManager defaultManager];
    NSDirectoryEnumerator *myDirectoryEnumerator = [myFileManager enumeratorAtPath:logPath];

    BOOL isDir = NO;
    BOOL isExist = NO;

    NSMutableArray *output = [[NSMutableArray alloc] init];
    for (NSString *path in myDirectoryEnumerator.allObjects) {
        isExist = [myFileManager fileExistsAtPath:[NSString stringWithFormat:@"%@/%@", logPath, path] isDirectory:&isDir];
        if (!isDir) {
            if ([path containsString:@"Test_"]) {
                [output addObject:[NSString stringWithFormat:@"%@/%@", logPath, path]];
            }
        }
    }

    return output;
}
- (void)onSendingMessage:(DMCCMessage*)msg {
    dispatch_async(dispatch_get_main_queue(), ^{
        [[NSNotificationCenter defaultCenter] postNotificationName:kSendingMessageStatusUpdated object:@(msg.messageId) userInfo:[NSDictionary dictionaryWithObjectsAndKeys:msg,@"message",@(msg.status),@"status", nil]];
    });
}
- (void)onRecallMessage:(long long)messageUid {
    dispatch_async(dispatch_get_main_queue(), ^{
        [[NSNotificationCenter defaultCenter] postNotificationName:kRecallMessages object:@(messageUid)];
        if ([self.receiveMessageDelegate respondsToSelector:@selector(onRecallMessage:)]) {
            [self.receiveMessageDelegate onRecallMessage:messageUid];
        }
    });
}
- (void)onDeleteMessage:(long long)messageUid {
    dispatch_async(dispatch_get_main_queue(), ^{
        [[NSNotificationCenter defaultCenter] postNotificationName:kDeleteMessages object:@(messageUid)];
        if ([self.receiveMessageDelegate respondsToSelector:@selector(onDeleteMessage:)]) {
            [self.receiveMessageDelegate onDeleteMessage:messageUid];
        }
    });
}

- (void)pushMsgWork {
    while (1) {
        [self pushMsgQueue];
    }
}

- (void)pushMsgQueue {
    NSMutableArray *messageList = [[NSMutableArray alloc] init];
    dispatch_semaphore_wait(msgSem, DISPATCH_TIME_FOREVER);
    
    if (_messageMaps.count == 0) {
        return;
    }
    
    [NSThread sleepForTimeInterval:0.5];
    
    @synchronized (_lock) {
        [messageList addObjectsFromArray:_messageMaps];
        [_messageMaps removeAllObjects];
    }
    dispatch_async(dispatch_get_main_queue(), ^{
        [[NSNotificationCenter defaultCenter] postNotificationName:kReceiveMessages object:messageList];
    });
}

- (void)pushMessage:(NSArray *)msgList {
    @synchronized (_lock) {
        [_messageMaps addObjectsFromArray:msgList];
        dispatch_semaphore_signal(msgSem);
    }
}

- (void)onReceiveMessage:(NSArray<DMCCMessage *> *)messages hasMore:(BOOL)hasMore {
//    dispatch_async(dispatch_get_main_queue(), ^{
//        NSMutableArray *messageList = [messages mutableCopy];
//        for (DMCCMessage *message in messages) {
//            for (id<ReceiveMessageFilter> filter in self.messageFilterList) {
//                @try {
//                    if ([filter onReceiveMessage:message]) {
//                        [messageList removeObject:message];
//                        break;
//                    }
//                } @catch (NSException *exception) {
//                    NSLog(@"%@", exception);
//                    break;
//                }
//
//            }
//        }
//        [[NSNotificationCenter defaultCenter] postNotificationName:kReceiveMessages object:messages];
//        [self.receiveMessageDelegate onReceiveMessage:messageList hasMore:hasMore];
//    });
}

- (void)onMessageReaded:(NSArray<DMCCReadReport *> *)readeds {
    dispatch_async(dispatch_get_main_queue(), ^{
        [[NSNotificationCenter defaultCenter] postNotificationName:kMessageReaded object:readeds];
        
        if ([self.receiveMessageDelegate respondsToSelector:@selector(onMessageReaded:)]) {
            [self.receiveMessageDelegate onMessageReaded:readeds];
        }
    });
}

- (void)onMessageDelivered:(NSArray<DMCCDeliveryReport *> *)delivereds {
    dispatch_async(dispatch_get_main_queue(), ^{
        [[NSNotificationCenter defaultCenter] postNotificationName:kMessageDelivered object:delivereds];
        
        if ([self.receiveMessageDelegate respondsToSelector:@selector(onMessageDelivered:)]) {
            [self.receiveMessageDelegate onMessageDelivered:delivereds];
        }
    });
}

- (void)onConferenceEvent:(NSString *)event {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.conferenceEventDelegate onConferenceEvent:event];
    });
}

- (void)addReceiveMessageFilter:(id<ReceiveMessageFilter>)filter {
    [self.messageFilterList addObject:filter];
}

- (void)removeReceiveMessageFilter:(id<ReceiveMessageFilter>)filter {
    [self.messageFilterList removeObject:filter];
}

- (void)onDisconnected {
//  mars::baseevent::OnDestroy();
}

- (void)setCurrentConnectionStatus:(ConnectionStatus)currentConnectionStatus {
    NSLog(@"Connection status changed to (%ld)", (long)currentConnectionStatus);
    if (_currentConnectionStatus != currentConnectionStatus) {
        _currentConnectionStatus = currentConnectionStatus;
        
        dispatch_async(dispatch_get_main_queue(), ^{
            [[NSNotificationCenter defaultCenter] postNotificationName:kConnectionStatusChanged object:@(self.currentConnectionStatus)];
            
            if (self.connectionStatusDelegate) {
                [self.connectionStatusDelegate onConnectionStatusChanged:currentConnectionStatus];
            }
        });
    }
}
- (void)onConnectionStatusChanged:(ConnectionStatus)status {
    self.currentConnectionStatus = status;
    NSLog(@"onConnectionStatusChanged: %ld", (long)status);
    if (status == kConnectionStatusKickedoff) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [[NSNotificationCenter defaultCenter] postNotificationName:kLoginOutKikoffUpdated object:nil userInfo:nil];
        });
    }
}

+ (DMCCNetworkService *)sharedInstance {
    if (sharedSingleton == nil) {
        @synchronized (self) {
            if (sharedSingleton == nil) {
                sharedSingleton = [[DMCCNetworkService alloc] init];
            }
        }
    }
    
    return sharedSingleton;
}

- (instancetype)init {
    self = [super init];
    if (self) {
        _currentConnectionStatus = kConnectionStatusLogout;
        _messageFilterList = [[NSMutableArray alloc] init];
        
        _messageMaps = [[NSMutableArray alloc] init];
        _lock = [NSObject new];
        msgSem = dispatch_semaphore_create(0);
        
        [[NSNotificationCenter defaultCenter] addObserver:self
                                               selector:@selector(onAppSuspend)
                                                   name:UIApplicationDidEnterBackgroundNotification
                                                 object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self
                                               selector:@selector(onAppResume)
                                                   name:UIApplicationDidBecomeActiveNotification
                                                 object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self
                                               selector:@selector(onAppTerminate)
                                                   name:UIApplicationWillTerminateNotification
                                                 object:nil];
        _osnsdk = [OsnSDK getInstance];
        
        self.userId = [_osnsdk getUserID];
        if (self.userId.length != 0) {
            [self initDB:[NSString stringWithFormat:@"%@.db",self.userId]];
        }
        
        msgQueue = dispatch_queue_create("com.ospn.msgnet", DISPATCH_QUEUE_CONCURRENT);
        dispatch_async(msgQueue, ^{[self pushMsgWork];});

    }
    self.soundTime = 0;
    return self;
}

- (void)startBackgroundTask {
    if (!_logined) {
        return;
    }
    
    if (_bgTaskId !=  UIBackgroundTaskInvalid) {
        [[UIApplication sharedApplication] endBackgroundTask:_bgTaskId];
    }
    __weak typeof(self) ws = self;
    _bgTaskId = [[UIApplication sharedApplication] beginBackgroundTaskWithExpirationHandler:^{
        if (ws.suspendTimer) {
            [ws.suspendTimer invalidate];
            ws.suspendTimer = nil;
        }
        
        if(ws.endBgTaskTimer) {
            [ws.endBgTaskTimer invalidate];
            ws.endBgTaskTimer = nil;
        }
        if(ws.forceConnectTimer) {
            [ws.forceConnectTimer invalidate];
            ws.forceConnectTimer = nil;
        }
        
        ws.bgTaskId = UIBackgroundTaskInvalid;
    }];
}

- (void)onAppSuspend {
    if (!_logined) {
        return;
    }
    
    [self reportEvent_OnForeground:NO];
    
    
    self.backgroudRunTime = 0;
    [self startBackgroundTask];
    
    [self checkBackGroundTask];
}

- (void)checkBackGroundTask {
    if(_suspendTimer) {
        [_suspendTimer invalidate];
    }
    if(_endBgTaskTimer) {
        [_endBgTaskTimer invalidate];
        _endBgTaskTimer = nil;
    }
    
    NSTimeInterval timeInterval = 3;
    
    _suspendTimer = [NSTimer scheduledTimerWithTimeInterval:timeInterval
                                                     target:self
                                                   selector:@selector(suspend)
                                                   userInfo:nil
                                                    repeats:NO];

}
- (void)suspend {
  if(_bgTaskId != UIBackgroundTaskInvalid) {
      self.backgroudRunTime += 3;
      BOOL inCall = NO;
      Class cls = NSClassFromString(@"WFAVEngineKit");
      
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wundeclared-selector"
      if (cls && [cls respondsToSelector:@selector(isCallActive)] && [cls performSelector:@selector(isCallActive)]) {
          inCall = YES;
      }
#pragma clang diagnostic pop
      
//      if ((mars::stn::GetTaskCount() > 0 && self.backgroudRunTime < 60) || (inCall && self.backgroudRunTime < 1800)) {
//          [self checkBackGroundTask];
//      } else {
//          mars::stn::ClearTasks();
//          _endBgTaskTimer = [NSTimer scheduledTimerWithTimeInterval:1
//                                                       target:self
//                                                     selector:@selector(endBgTask)
//                                                     userInfo:nil
//                                                      repeats:NO];
//      }
  }
}
- (void)endBgTask {
  if(_bgTaskId !=  UIBackgroundTaskInvalid) {
    [[UIApplication sharedApplication] endBackgroundTask:_bgTaskId];
    _bgTaskId =  UIBackgroundTaskInvalid;
  }
  
  if (_suspendTimer) {
    [_suspendTimer invalidate];
    _suspendTimer = nil;
  }
  
  if(_endBgTaskTimer) {
    [_endBgTaskTimer invalidate];
    _endBgTaskTimer = nil;
  }
    
    if (_forceConnectTimer) {
        [_forceConnectTimer invalidate];
        _forceConnectTimer = nil;
    }
    
    self.backgroudRunTime = 0;
}

- (void)onAppResume {
  if (!_logined) {
    return;
  }
  [self reportEvent_OnForeground:YES];
//  mars::baseevent::OnNetworkChange();
//  mars::stn::MakesureLonglinkConnected();
  [self endBgTask];
}

- (void)onAppTerminate {
//    mars::stn::AppWillTerminate();
}

- (void)dealloc {
    
}

- (void) createMars {
//  mars::app::SetCallback(mars::app::AppCallBack::Instance());
//  mars::stn::setConnectionStatusCallback(new CSCB(self));
//  mars::stn::setReceiveMessageCallback(new RPCB(self));
//    mars::stn::setConferenceEventCallback(new CONFCB(self));
//  mars::stn::setRefreshUserInfoCallback(new GUCB(self));
//  mars::stn::setRefreshGroupInfoCallback(new GGCB(self));
//    mars::stn::setRefreshGroupMemberCallback(new GGMCB(self));
//  mars::stn::setRefreshChannelInfoCallback(new GCHCB(self));
//  mars::stn::setRefreshFriendListCallback(new GFLCB(self));
//    mars::stn::setRefreshFriendRequestCallback(new GFRCB(self));
//  mars::stn::setRefreshSettingCallback(new GSCB(self));
//  mars::baseevent::OnCreate();
}
//- (BOOL)connect:(NSString *)host {
////    bool newDB = mars::stn::Connect([host UTF8String]);
//
//  dispatch_async(dispatch_get_main_queue(), ^{
//    if ([UIApplication sharedApplication].applicationState == UIApplicationStateBackground) {
//      dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(2 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
//        if ([UIApplication sharedApplication].applicationState == UIApplicationStateBackground) {
//          [self onAppSuspend];
//        }
//      });
//    }
//  });
//
//    [self reportEvent_OnForeground:YES];
////    mars::stn::MakesureLonglinkConnected();
//
////    if (newDB) {
////        return YES;
////    }
//    return NO;
//}

- (void)forceConnectTimeOut {
    if ([UIApplication sharedApplication].applicationState == UIApplicationStateBackground) {
        [self onAppSuspend];
    }
}

- (void)forceConnect:(NSUInteger)second {
    __weak typeof(self)ws = self;
  dispatch_async(dispatch_get_main_queue(), ^{
    if (ws.logined &&[UIApplication sharedApplication].applicationState == UIApplicationStateBackground) {
        [self onAppResume];
        [self startBackgroundTask];
        if(second > 0) {
            ws.forceConnectTimer = [NSTimer scheduledTimerWithTimeInterval:second
                                                         target:self
                                                       selector:@selector(forceConnectTimeOut)
                                                       userInfo:nil
                                                        repeats:NO];
        }
    }
  });
}

- (void)cancelForceConnect {
    __weak typeof(self)ws = self;
    dispatch_async(dispatch_get_main_queue(), ^{
        if (ws.forceConnectTimer) {
            [ws.forceConnectTimer invalidate];
            ws.forceConnectTimer = nil;
        }
    if ([UIApplication sharedApplication].applicationState == UIApplicationStateBackground) {
        [self onAppSuspend];
    }
    });
}

- (long long)serverDeltaTime {
//    return mars::stn::getServerDeltaTime();
    return 0;
}

- (void)useSM4 {
//    mars::stn::useEncryptSM4();
}

- (void)setLiteMode:(BOOL)isLiteMode {
//    mars::stn::setLiteMode(isLiteMode ? true:false);
}

#define DMC_CLIENT_ID @"DMC_client_id"
- (NSString *)getClientId {
    //当应用在appstore上架后，开发者账户下的所有应用在同一个手机上具有相同的vendor id。详情请参考(IDFV(identifierForVendor)使用陷阱)https://easeapi.com/blog/blog/63-ios-idfv.html
    //这样如果同一个IM服务有多个应用，多个应用安装到同一个手机上，这样所有应用将具有相同的clientId，导致互踢现象产生。
    //处理办法就是不使用identifierForVendor，随机生成UUID，然后固定使用这个UUID就行了，请参考下面注释掉的代码
    /*
    NSString *clientId = [[NSUserDefaults standardUserDefaults] objectForKey:DMC_CLIENT_ID];
    if(!clientId.length) {
        CFUUIDRef uuidObject = CFUUIDCreate(kCFAllocatorDefault);
        clientId = (NSString *)CFBridgingRelease(CFUUIDCreateString(kCFAllocatorDefault, uuidObject));
        CFRelease(uuidObject);
        [[NSUserDefaults standardUserDefaults] setObject:clientId forKey:DMC_CLIENT_ID];
        [[NSUserDefaults standardUserDefaults] synchronize];
    }
    return clientId;
     */
    return [UIDevice currentDevice].identifierForVendor.UUIDString;
}
- (void) initDB:(NSString *)dbPath{
    if(dbPath == nil){
        dbPath = [[NSUserDefaults standardUserDefaults] objectForKey:@"ospn_dbPath"];
    }else{
        [[NSUserDefaults standardUserDefaults] setValue:dbPath forKey:@"ospn_dbPath"];
    }
    if (dbPath != nil)
        [SqliteUtils initDB:dbPath];
}
- (BOOL)connect:(NSString *)userId token:(NSString *)token password:(NSString *)password{
    if(userId == nil){
        [_osnsdk initSDK:self.serverHost cb:self];
        
        self.userId = [_osnsdk getUserID];
        if (self.userId.length != 0) {
            [self initDB:[NSString stringWithFormat:@"%@.db",self.userId]];
        }
        
        return true;
    }
    
    //[self onConnectionStatusChanged:kConnectionStatusConnecting];
//    if (keywords == nil) {
//        dispatch_async(workQueue, ^{
//            NSString* filterUrl = [NSString stringWithFormat:@"http://%@:8300/keywordFilter",self.serverHost];
//            NSData* data = [HttpUtils doGet:filterUrl];
//            if (data != nil) {
//                keywords = [NSMutableArray new];
//                NSDictionary *json = [OsnUtils json2Dic:data];
//                NSLog(@"keywords: %@", json[@"keywords"]);
//                NSArray<NSString*> *keywordList = json[@"keywords"];
//                for(NSString *o in keywordList){
//                    [keywords addObject:o];
//                }
//            }
//        });
//    }

    if (!strncmp(userId.UTF8String, "OSN", 3)) {
        NSArray *temp=[token componentsSeparatedByString:@"-"];
        if (temp != nil && temp.count == 3) {
            self.logined = [_osnsdk loginV2:userId pwd:token decPwd:password cb:nil];
        } else {
            self.logined = [_osnsdk loginWithOsnID:userId cb:nil];
        }
        
    } else
        self.logined = [_osnsdk loginWithName:userId pwd:token cb:nil];
    if (self.logined) {
        self.userId = [_osnsdk getUserID];
        if (self.userId.length != 0) {
            [self initDB:[NSString stringWithFormat:@"%@.db",self.userId]];
        }
    }

    NSLog(@"login state: %d", self.logined);
    if(self.logined){
        //[self onConnectionStatusChanged:kConnectionStatusConnected];
        //[[DMCCNetworkStatus sharedInstance] Start:[DMCCNetworkService sharedInstance]];
    }
    
    dispatch_async(dispatch_get_main_queue(), ^{
      if ([UIApplication sharedApplication].applicationState == UIApplicationStateBackground) {
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(2 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
          if ([UIApplication sharedApplication].applicationState == UIApplicationStateBackground) {
            [self onAppSuspend];
          }
        });
      }
    });
      
    [self reportEvent_OnForeground:YES];
    return self.logined;
}

- (void)disconnect:(BOOL)disablePush clearSession:(BOOL)clearSession {
//    self.logined = false;
//    self.userId = nil;
//    [_osnsdk logout:nil];
//    dispatch_async(dispatch_get_main_queue(), ^{
//        self.currentConnectionStatus = kConnectionStatusLogout;
//    });
//    [[DMCCNetworkStatus sharedInstance] Stop];
    [self logout:kConnectionStatusLogout];
}

- (ConnectionStatus) getConnectionStatus{
    return _currentConnectionStatus;
}

- (void) setHost:(NSString*)ip{
    [_osnsdk resetHost:ip];
}

- (void)setServerAddress:(NSString *)host {
    self.serverHost = host;
}

- (NSString *)getHost {
    return self.serverHost;
}

- (NSString *)getHostEx {
    return self.serverHost;
}

- (void)destroyMars {
  [[DMCCNetworkStatus sharedInstance] Stop];
}


// event reporting
- (void)reportEvent_OnForeground:(BOOL)isForeground {
}

- (void)setDeviceToken:(NSString *)token {
    if (token.length == 0) {
        return;
    }

    _deviceToken = token;

    if (!self.isLogined || self.currentConnectionStatus != kConnectionStatusConnected) {
        self.deviceTokenUploaded = NO;
        return;
    }
  
//    NSString *appName =
//    [[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleIdentifier"];
//    mars::stn::setDeviceToken([appName UTF8String], [token UTF8String], mars::app::AppCallBack::Instance()->GetPushType());
    self.deviceTokenUploaded =YES;
}
- (void)setBackupAddressStrategy:(int)strategy {
}
- (void)setBackupAddress:(NSString *)host port:(int)port {
}

- (void)setVoipDeviceToken:(NSString *)token {
    if (token.length == 0) {
        return;
    }
    
    _voipDeviceToken = token;
    
    if (!self.isLogined || self.currentConnectionStatus != kConnectionStatusConnected) {
        self.voipDeviceTokenUploaded = NO;
        return;
    }
    
//    NSString *appName =
//    [[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleIdentifier"];
//    mars::stn::setDeviceToken([appName UTF8String], [token UTF8String], 2);
    self.voipDeviceTokenUploaded = YES;
}

- (NSString *)encodedCid {
//    return [NSString stringWithUTF8String:mars::stn::GetEncodedCid().c_str()];
    return nil;
}
- (void)onGroupInfoUpdated:(NSArray<DMCCGroupInfo *> *)updatedGroupInfo {
  dispatch_async(dispatch_get_main_queue(), ^{
    for (DMCCGroupInfo *groupInfo in updatedGroupInfo) {
      [[NSNotificationCenter defaultCenter] postNotificationName:kGroupInfoUpdated object:groupInfo.target userInfo:@{@"groupInfo":groupInfo}];
    }
  });
}

- (void)onGroupMemberUpdated:(NSString *)groupId members:(NSArray<DMCCGroupMember *> *)updatedGroupMembers {
    dispatch_async(dispatch_get_main_queue(), ^{
        [[NSNotificationCenter defaultCenter] postNotificationName:kGroupMemberUpdated object:groupId userInfo:@{@"members":updatedGroupMembers}];
    });
}

- (void)onGroupMemberAddOrLessUpdated:(NSString *)groupId members:(NSArray<DMCCGroupMember *> *)updatedGroupMembers {
    dispatch_async(dispatch_get_main_queue(), ^{
        [[NSNotificationCenter defaultCenter] postNotificationName:kGroupMemberAddOrLessUpdated object:groupId userInfo:@{@"members":updatedGroupMembers}];
    });
}

- (void)onChannelInfoUpdated:(NSArray<DMCCChannelInfo *> *)updatedChannelInfo {
    dispatch_async(dispatch_get_main_queue(), ^{
        for (DMCCChannelInfo *channelInfo in updatedChannelInfo) {
            [[NSNotificationCenter defaultCenter] postNotificationName:kChannelInfoUpdated object:channelInfo.channelId userInfo:@{@"channelInfo":channelInfo}];
        }
    });
}

- (void)onUserInfoUpdated:(NSArray<DMCCUserInfo *> *)updatedUserInfo {
  dispatch_async(dispatch_get_main_queue(), ^{
    for (DMCCUserInfo *userInfo in updatedUserInfo) {
      [[NSNotificationCenter defaultCenter] postNotificationName:kUserInfoUpdated object:userInfo.userId userInfo:@{@"userInfo":userInfo}];
    }
  });
}

- (void)onFriendListUpdated {
    dispatch_async(dispatch_get_main_queue(), ^{
        [[NSNotificationCenter defaultCenter] postNotificationName:kFriendListUpdated object:nil];
    });
}

- (void)onFriendRequestUpdated:(NSArray<NSString *> *)newFriendRequests {
    dispatch_async(dispatch_get_main_queue(), ^{
        [[NSNotificationCenter defaultCenter] postNotificationName:kFriendRequestUpdated object:newFriendRequests];
    });
}

- (NSData *)encodeData:(NSData *)data {
    return nil;
}

- (void)onSettingUpdated {
  dispatch_async(dispatch_get_main_queue(), ^{
    [[NSNotificationCenter defaultCenter] postNotificationName:kSettingUpdated object:nil];
  });
}

- (NSData *)decodeData:(NSData *)data {
    return nil;
}


- (NSData *)decodeData:(NSData *)data gzip:(BOOL)gzip type:(int)type {
    return nil;
}

#pragma mark DMCCNetworkStatusDelegate
-(void) ReachabilityChange:(UInt32)uiFlags {
//    if ((uiFlags & kSCNetworkReachabilityFlagsConnectionRequired) == 0) {
//            mars::baseevent::OnNetworkChange();
//    } else if(!uiFlags) {
//        if(self.currentConnectionStatus == kConnectionStatusConnected)
//            mars::baseevent::OnNetworkChange();
//    }
}

- (bool) filterMessage:(DMCCMessage*) msg {
    bool filted = false;
    if (keywords != nil && [msg.content isMemberOfClass:[DMCCTextMessageContent class]]) {
        DMCCTextMessageContent* textMessageContent = (DMCCTextMessageContent*) msg.content;
        NSString* text = textMessageContent.text;
        for (NSString* k : keywords) {
            NSRange index = [text rangeOfString:k];
            if (index.length > 0) {
                textMessageContent.text = [text stringByReplacingCharactersInRange:index withString:@"***"];
                filted = true;
            }
        }
    }
    return filted;
}

- (void)initializeLocalNotification {
    dispatch_async(dispatch_get_main_queue(), ^{
        UILocalNotification *notification = [[UILocalNotification alloc] init];
        notification.fireDate = [[NSDate date] dateByAddingTimeInterval:15.0f];
        notification.timeZone = [NSTimeZone defaultTimeZone];
        notification.userInfo = @{};
        notification.alertBody = LocalizedString(@"NotesMsg");
        notification.soundName = UILocalNotificationDefaultSoundName;
        notification.applicationIconBadgeNumber = 1;
        if ([[UIApplication sharedApplication] respondsToSelector:@selector(registerUserNotificationSettings:)]) {
            UIUserNotificationType type =  UIUserNotificationTypeAlert | UIUserNotificationTypeBadge | UIUserNotificationTypeSound;
            UIUserNotificationSettings *settings = [UIUserNotificationSettings settingsForTypes:type
                                                                                     categories:nil];
            [[UIApplication sharedApplication] registerUserNotificationSettings:settings];
        }
        [[UIApplication sharedApplication] scheduleLocalNotification:notification];
    });
}

- (RecvMessageReturnInfo*) recvMessage:(NSArray<OsnMessageInfo*>*) msgList history:(bool) isHistory {
   
    NSMutableArray *newList = [NSMutableArray arrayWithCapacity:0];
    NSMutableArray<DMCCMessage*>* messages = [NSMutableArray new];
    for (OsnMessageInfo* messageInfo in msgList) {
        if ([SqliteUtils isMessageExist:messageInfo.hashx]) {
            [newList addObject:messageInfo.hashx];
            continue;
        }
        DMCCMessage* message = [DMCCMessage new];
        message.messageId = 0;
        message.fromUser = messageInfo.userID;
        message.saveFrome =messageInfo.userID;
        message.saveTo = messageInfo.target;
        
        if ([messageInfo.userID isEqualToString:self.userId]) {
            if (!strncmp(messageInfo.target.UTF8String, "OSNG", 4))
                message.conversation = [DMCCConversation conversationWithType:Group_Type target:messageInfo.target line:0];
            else if (!strncmp(messageInfo.target.UTF8String, "OSNU", 4))
                message.conversation = [DMCCConversation conversationWithType:Single_Type target:messageInfo.target line:0];
            else
                message.conversation = [DMCCConversation conversationWithType:Service_Type target:messageInfo.target line:0];
            message.direction = MessageDirection_Send;
            message.status = Message_Status_Sent;
        } else {
            if (!strncmp(messageInfo.userID.UTF8String, "OSNG", 4)) {
                message.fromUser = messageInfo.originalUser;
                message.conversation = [DMCCConversation conversationWithType:Group_Type target:messageInfo.userID line:0];
            } else if (!strncmp(messageInfo.userID.UTF8String, "OSNU", 4))
                message.conversation = [DMCCConversation conversationWithType:Single_Type target:messageInfo.userID line:0];
            else
                message.conversation = [DMCCConversation conversationWithType:Service_Type target:messageInfo.userID line:0];
            message.direction = MessageDirection_Receive;
            message.status = isHistory ? Message_Status_Readed : Message_Status_Unread;
        }
        
       
        message.messageUid = 0;
        message.serverTime = messageInfo.timeStamp;
        message.messageHash = messageInfo.hashx;
        message.messageHasho = messageInfo.hasho;
        NSDictionary* json = nil;
        if([messageInfo.content isKindOfClass:[NSDictionary class]])
            json = (NSDictionary*)messageInfo.content;
        else
            json = [OsnUtils json2Dic:[messageInfo.content dataUsingEncoding:NSUTF8StringEncoding]];
        NSString* msgType = json[@"type"];
      
        if ([msgType isEqualToString:@"text"]) {
            
            
            DMCCTextMessageContent* textMessageContent = [DMCCTextMessageContent new];
            textMessageContent.text = json[@"data"];
            textMessageContent.mentionedType = [json[@"mentionedType"] intValue];
            textMessageContent.mentionedTargets = json[@"mentionedTargets"];
            DMCCQuoteInfo *quo = [DMCCQuoteInfo new];
            [quo decode:json[@"quoteInfo"]];
            textMessageContent.quoteInfo = quo;
            message.content = textMessageContent;
            [self filterMessage:message];
            
        } else if ([msgType isEqualToString:@"card"]) {
            
            
            DMCCCardMessageContent* cardMessageContent = [DMCCCardMessageContent new];
            NSString* cardType = json[@"cardType"];
            if([cardType isEqualToString:@"user"])
                cardMessageContent.type = CardType_User;
            else if([cardType isEqualToString:@"group"])
                cardMessageContent.type = CardType_Group;
            else if([cardType isEqualToString:@"chatroom"])
                cardMessageContent.type = CardType_ChatRoom;
            else if([cardType isEqualToString:@"channel"])
                cardMessageContent.type = CardType_Channel;
            else if([cardType isEqualToString:@"litapp"])
                cardMessageContent.type = CardType_Litapp;
            else if([cardType isEqualToString:@"share"])
                cardMessageContent.type = CardType_Share;
            cardMessageContent.targetId = json[@"target"];
            cardMessageContent.name = json[@"name"];
            cardMessageContent.displayName = json[@"displayName"];
            cardMessageContent.portrait = json[@"portrait"];
            cardMessageContent.theme = json[@"theme"];
            cardMessageContent.url = json[@"url"];
            cardMessageContent.info = json[@"info"];
            message.content = cardMessageContent;
        } else if([msgType isEqualToString:@"redPacket"]){
            NSDictionary* data = @{};
            if ([json[@"data"] isKindOfClass:[NSString class]]) {
                data = [OsnUtils json2Dics:json[@"data"]];
            } else {
                data = json[@"data"];
            }
            DMCCRedPacketInfo* redPacketInfo = [DMCCRedPacketInfo redPacketInfoWithJson:data];
            [SqliteUtils insertRedPacket:redPacketInfo];
            NSLog(@"insert redPacket: %@",json[@"data"]);
            message.content = [DMCCRedPacketMessageContent redPacketWithJson:data];
        } else if([msgType isEqualToString:@"call"]){
            DMCCCallMessageContent* callMessageContent = [DMCCCallMessageContent new];
            callMessageContent.cid = ((NSNumber*)json[@"id"]).intValue;
            callMessageContent.type = ((NSNumber*)json[@"callType"]).intValue;
            callMessageContent.mode = ((NSNumber*)json[@"callMode"]).intValue;
            callMessageContent.action = ((NSNumber*)json[@"callAction"]).intValue;
            callMessageContent.duration = -1;
            callMessageContent.url = json[@"url"];
            callMessageContent.user = json[@"user"];
            callMessageContent.urls = json[@"urls"];
            callMessageContent.users = json[@"users"];
            callMessageContent.voiceHostUrl = json[@"voiceHostUrl"];
            callMessageContent.voiceBaseUrl = json[@"voiceBaseUrl"];
            message.content = callMessageContent;
        } else {
            
            NSString* url = json[@"url"];
            NSString* path = @"";
            
//            NSString *caches = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES).firstObject;
//            NSString* path = [NSString stringWithFormat:@"%@/%ld%@",caches,[OsnUtils getTimeStamp],name];
            
            if ([msgType isEqualToString:@"image"] ||
                    [msgType isEqualToString:@"sticker"] ||
                    [msgType isEqualToString:@"video"] ||
                [msgType isEqualToString:@"voice"]) {
                 
//                 if ([url hasSuffix:@".zip"] || decKey.length > 0) {
//                    [_osnsdk downloadData:url localPath:path decKey:decKey cb:nil progress:nil];
//                 } else {
//                     [_osnsdk downloadData:url localPath:path cb:nil progress:nil];
//                 }
        
            }
            
            if([msgType isEqualToString:@"file"]){
                DMCCFileMessageContent* fileMessageContent = [DMCCFileMessageContent new];
                fileMessageContent.remoteUrl = url;
                fileMessageContent.localPath = path;
                fileMessageContent.name = json[@"name"];
                fileMessageContent.size = ((NSNumber*)json[@"size"]).intValue;
                fileMessageContent.decKey = json[@"decKey"];
                message.content = fileMessageContent;
            } else if([msgType isEqualToString:@"image"]){
                DMCCImageMessageContent* imageMessageContent = [DMCCImageMessageContent new];
                imageMessageContent.remoteUrl = url;
                imageMessageContent.localPath = path;
                imageMessageContent.decKey = json[@"decKey"];
                imageMessageContent.name = json[@"name"];
                imageMessageContent.size = CGSizeMake(((NSNumber*)json[@"width"]).doubleValue, ((NSNumber*)json[@"height"]).doubleValue);
//                imageMessageContent.thumbnail = [DMCCUtilities fileToThumbnail:path];
                message.content = imageMessageContent;
            } else if([msgType isEqualToString:@"voice"]){
                DMCCSoundMessageContent* soundMessageContent = [DMCCSoundMessageContent new];
                soundMessageContent.remoteUrl = url;
                soundMessageContent.localPath = path;
                soundMessageContent.decKey = json[@"decKey"];
                soundMessageContent.duration = ((NSNumber*)json[@"duration"]).intValue;
                message.content = soundMessageContent;
            } else if([msgType isEqualToString:@"video"]){
                DMCCVideoMessageContent* videoMessageContent = [DMCCVideoMessageContent new];
                videoMessageContent.remoteUrl = url;
                videoMessageContent.localPath = path;
                videoMessageContent.decKey = json[@"decKey"];
                videoMessageContent.name = json[@"name"];
//                videoMessageContent.thumbnail = [DMCCUtilities getFileThumbnailImage:path];
                message.content = videoMessageContent;
            } else if([msgType isEqualToString:@"sticker"]){
                DMCCStickerMessageContent* stickerMessageContent = [DMCCStickerMessageContent new];
                stickerMessageContent.remoteUrl = url;
                stickerMessageContent.localPath = path;
                stickerMessageContent.size = CGSizeMake(((NSNumber*)json[@"width"]).doubleValue, ((NSNumber*)json[@"height"]).doubleValue);
                message.content = stickerMessageContent;
            } else {
                NSLog(@"unknown msgType: %@", msgType);
            }

        }
      
        if (message.content != nil) {
            if([message.content isMemberOfClass:[DMCCCallMessageContent class]]){
                message.messageUid = -1;
                
                long nowTime = [OsnUtils getTimeStamp];
                
                if (nowTime - message.serverTime < 30*1000) {
                    
                    [messages addObject:message];
                    [newList addObject:messageInfo.hashx];
                    
                } else {
                    
                }
               
            }else{
                message.messageId = message.messageUid = [SqliteUtils insertMessage:message];
            }
            if (message.messageUid != -1) {
                [messages addObject:message];
                [newList addObject:messageInfo.hashx];
            }
        }
        
    }
    
    RecvMessageReturnInfo *info = [RecvMessageReturnInfo new];
    info.messages = messages;
    info.nLists = newList;
    
    return info;
}

- (void) addFriend:(OsnFriendInfo*) friendInfo {
    DMCCMessage* message = [DMCCMessage new];
    message.fromUser = friendInfo.friendID;
    message.conversation = [DMCCConversation  conversationWithType:Single_Type target:friendInfo.friendID line:0];
    message.direction = MessageDirection_Receive;
    message.status = Message_Status_Readed;
    message.serverTime = [OsnUtils getTimeStamp];

    DMCCTextMessageContent* textMessageContent = [DMCCTextMessageContent new];
    DMCCFriendRequest* request = [SqliteUtils queryFriendRequest:friendInfo.friendID];
    textMessageContent.text = request == nil ? LocalizedString(@"hello") : request.reason;
    message.content = textMessageContent;
    [SqliteUtils insertMessage:message];

    message.content = [DMCCFriendGreetingMessageContent new];
    message.serverTime = [OsnUtils getTimeStamp];
    [SqliteUtils insertMessage:message];

    message.content = [DMCCFriendAddedMessageContent new];
    message.serverTime = [OsnUtils getTimeStamp];
    [SqliteUtils insertMessage:message];
    [SqliteUtils insertConversation:Single_Type target:friendInfo.friendID line:0];
}
- (void) updateGroup:(OsnGroupInfo*) osnGroupInfo update:(bool)isUpdateMember fav:(int)fav{
    DMCCGroupInfo* groupInfo = [[DMCCIMService sharedDMCIMService] toClientGroup:osnGroupInfo];
    DMCCGroupInfo* info = [SqliteUtils queryGroup:osnGroupInfo.groupID];
    groupInfo.fav = fav;
    groupInfo.showAlias = info.showAlias;
    
    //if (osnGroupInfo.singleMute)
    {
        DMCCGroupMember* myself = [SqliteUtils queryMember:groupInfo.target memberID:_userId];
        if (myself == nil) {
            myself = [DMCCGroupMember new];
            myself.type = Member_Type_Normal;
            myself.groupId = groupInfo.target;
            myself.memberId = _userId;
            myself.alias = nil;
            myself.mute = osnGroupInfo.singleMute;
            // insert
            [SqliteUtils insertMember:myself];
        } else {
            myself.mute = osnGroupInfo.singleMute;
            // update
            [SqliteUtils updateMember:myself keys:@[@"mute"]];
        }
    }
    
    [SqliteUtils insertGroup:groupInfo];
    if (isUpdateMember) {
        if (osnGroupInfo.userList != nil) {
            if (osnGroupInfo.userList.count > 0) {
                [[DMCCIMService sharedDMCIMService] updateMember:osnGroupInfo.groupID members:osnGroupInfo.userList];
            }
        }
    }
        
}
- (void) addGroup:(OsnGroupInfo*) groupInfo fav:(int)fav{
    NSLog(@"groupID: %@, name: %@, fav:%d",groupInfo.groupID,groupInfo.name,fav);

    [self updateGroup:groupInfo update:true fav:fav];
    DMCCConversationInfo* conversationInfo = [SqliteUtils queryConversation:Group_Type target:groupInfo.groupID line:0];
    if (conversationInfo == nil)
        [SqliteUtils insertConversation:Group_Type target:groupInfo.groupID line:0];

    DMCCMessage* message = [DMCCMessage new];
    message.fromUser = groupInfo.owner;
    message.direction = MessageDirection_Receive;
    message.status = Message_Status_Readed;
    message.conversation = [DMCCConversation conversationWithType:Group_Type target:groupInfo.groupID line:0];
    
    DMCCCreateGroupNotificationContent* createGroupNotificationContent = [DMCCCreateGroupNotificationContent new];
    createGroupNotificationContent.groupName = groupInfo.name;
    createGroupNotificationContent.creator = groupInfo.owner;
    message.content = createGroupNotificationContent;
    
    message.serverTime = [groupInfo getNoticeServerTime];
    if (groupInfo.notice != nil) {
        message.messageHash = groupInfo.notice[@"hash"];
        message.messageHasho = groupInfo.notice[@"hash0"];
    }
    
    long mid = [SqliteUtils insertMessage:message];
    if (mid != -1) {
        NSLog(@"=test2= new group insert message success.");
        DMCCGroupInfo* info = [[DMCCIMService sharedDMCIMService]toClientGroup:groupInfo];
        [self onGroupInfoUpdated:@[info]];
    } else {
        NSLog(@"=test2= new group insert message failed.");
    }

    
}
- (void) addGroup2:(OsnGroupInfo*) groupInfo fav:(int)fav{
    NSLog(@"groupID: %@, name: %@, fav:%d",groupInfo.groupID,groupInfo.name,fav);

    [self updateGroup:groupInfo update:true fav:fav];
    DMCCConversationInfo* conversationInfo = [SqliteUtils queryConversation:Group_Type target:groupInfo.groupID line:0];
    if (conversationInfo == nil)
        [SqliteUtils insertConversation:Group_Type target:groupInfo.groupID line:0];

    /*DMCCMessage* message = [DMCCMessage new];
    message.fromUser = groupInfo.owner;
    message.direction = MessageDirection_Receive;
    message.status = Message_Status_Readed;
    message.conversation = [DMCCConversation conversationWithType:Group_Type target:groupInfo.groupID line:0];
    message.serverTime = [OsnUtils getTimeStamp];
    DMCCCreateGroupNotificationContent* createGroupNotificationContent = [DMCCCreateGroupNotificationContent new];
    createGroupNotificationContent.groupName = groupInfo.name;
    createGroupNotificationContent.creator = groupInfo.owner;
    message.content = createGroupNotificationContent;*/
//    [SqliteUtils insertMessage:message];

    //[self onReceiveMessage:@[message] hasMore:false];
    DMCCGroupInfo* info = [[DMCCIMService sharedDMCIMService]toClientGroup:groupInfo];
    [self onGroupInfoUpdated:@[info]];
}
- (void) addGroupNull:(NSString*) groupID fav:(int)fav{
    OsnGroupInfo* groupInfo = [OsnGroupInfo new];
    groupInfo.groupID = groupID;
    groupInfo.owner = self.userId;
    groupInfo.name = groupID;
    OsnMemberInfo* memberInfo = [OsnMemberInfo new];
    memberInfo.osnID = self.userId;
    memberInfo.groupID = groupID;
    memberInfo.type = MemberType_Normal;
    [groupInfo.userList addObject:memberInfo];
    [self addGroup2:groupInfo fav:fav];
}
- (void) addGroupDrop:(NSString*) groupID{
    [_osnsdk getGroupInfo:groupID cb:^(bool isSuccess, id t, NSString *error) {
        OsnGroupInfo* osnGroupInfo = (OsnGroupInfo*)t;
        if(isSuccess){
            NSLog(@"SyncGroup null groupID: %@",groupID);
            [self addGroup2:osnGroupInfo fav:0];
        } else {
            [self addGroupNull:osnGroupInfo.groupID fav:0];
        }
    }];
}
- (NSArray<OsnMemberInfo*>*) getMemberX:(NSArray<OsnMemberInfo*>*) m0 m1:(NSArray<DMCCGroupMember*>*) m1 exclude:(bool) exclude {
    NSMutableArray<OsnMemberInfo*>* list = [NSMutableArray new];
    for (OsnMemberInfo* m in m0) {
        bool finded = false;
        for (DMCCGroupMember* o in m1) {
            if ([m.osnID isEqualToString:o.memberId]) {
                finded = true;
                break;
            }
        }
        if (finded) {
            if (!exclude)
                [list addObject:m];
        } else {
            if (exclude)
                [list addObject:m];
        }
    }
    return list;
}
- (void) addMemberNotify:(NSString*) groupID members:(NSArray<OsnMemberInfo*>*) members osnGroupInfo:(OsnGroupInfo*)osnGroupInfo{
    DMCCMessage* message = [DMCCMessage new];
    message.fromUser = groupID;
    message.direction = MessageDirection_Receive;
    message.status = Message_Status_Readed;
    message.conversation = [DMCCConversation conversationWithType:Group_Type target:groupID line:0];
    
    message.serverTime = [osnGroupInfo getNoticeServerTime];
    if (osnGroupInfo.notice != nil) {
        message.messageHash = osnGroupInfo.notice[@"hash"];
        message.messageHasho = osnGroupInfo.notice[@"hash0"];
    }

    NSMutableArray<NSString*>* memberList = [NSMutableArray new];
    for (OsnMemberInfo* memberInfo in members)
        [memberList addObject:memberInfo.osnID];
    DMCCAddGroupeMemberNotificationContent* addGroupMemberNotificationContent = [DMCCAddGroupeMemberNotificationContent new];
    addGroupMemberNotificationContent.invitees = memberList;
    addGroupMemberNotificationContent.invitor = groupID;
    message.content = addGroupMemberNotificationContent;
    
    long mid = [SqliteUtils insertMessage:message];
    if (mid != -1) {
        [self onReceiveMessage:@[message] hasMore:false];
    }
    
}

- (void) delMemberNotify:(NSString*) groupID members:(NSArray<OsnMemberInfo*>*) members state:(NSString*) state osnGroupInfo:(OsnGroupInfo*)osnGroupInfo{
    DMCCMessage* message = [DMCCMessage new];
    message.fromUser = groupID;
    message.direction = MessageDirection_Receive;
    message.status = Message_Status_Readed;
    message.conversation = [DMCCConversation conversationWithType:Group_Type target:groupID line:0];
    message.serverTime = [osnGroupInfo getNoticeServerTime];

    for (OsnMemberInfo* memberInfo in members) {
        if ([state isEqualToString:@"DelMember"]) {
            DMCCKickoffGroupMemberNotificationContent* kickoffGroupMemberNotificationContent = [DMCCKickoffGroupMemberNotificationContent new];
            kickoffGroupMemberNotificationContent.operateUser = groupID;
            kickoffGroupMemberNotificationContent.kickedMembers = @[memberInfo.osnID];
            message.content = kickoffGroupMemberNotificationContent;
        } else if ([state isEqualToString:@"QuitGroup"]) {
            DMCCQuitGroupNotificationContent* quitGroupNotificationContent = [DMCCQuitGroupNotificationContent new];
            quitGroupNotificationContent.quitMember = memberInfo.osnID;
            message.content = quitGroupNotificationContent;
        }
        // ??? delete one by one ???
        [SqliteUtils insertMessage:message];
    }
    [self onReceiveMessage:@[message] hasMore:false];
}

- (void) delGroupNotify:(OsnGroupInfo*) osnGroupInfo {
    DMCCMessage* message = [DMCCMessage new];
    message.fromUser = osnGroupInfo.groupID;
    message.direction = MessageDirection_Receive;
    message.status = Message_Status_Readed;
    message.conversation = [DMCCConversation conversationWithType:Group_Type target:osnGroupInfo.groupID line:0];
    message.serverTime = [osnGroupInfo getNoticeServerTime];
    if (osnGroupInfo.notice != nil) {
        message.messageHash = osnGroupInfo.notice[@"hash"];
        message.messageHasho = osnGroupInfo.notice[@"hash0"];
    }
    DMCCDismissGroupNotificationContent* dismissGroupNotificationContent = [DMCCDismissGroupNotificationContent new];
    dismissGroupNotificationContent.operateUser = osnGroupInfo.groupID;
    message.content = dismissGroupNotificationContent;
    long mid = [SqliteUtils insertMessage:message];
    if (mid != -1) {
        [self onReceiveMessage:@[message] hasMore:false];
    }
}

- (void) delGroup:(NSString*) groupID state:(NSString*) state {
    [SqliteUtils deleteGroup:groupID];
    [SqliteUtils deleteConversation:Group_Type target:groupID line:0];
    [SqliteUtils clearMembers:groupID];
}
- (void) groupNewlyGroup:(OsnGroupInfo*) osnGroupInfo{
    //NSLog(@"new group name: %@",osnGroupInfo.name);
    NSLog(@"==test== groupNewlyGroup : %@-%@", osnGroupInfo.groupID, osnGroupInfo.name);
    [self addGroup:osnGroupInfo fav:0];
}
- (void) groupSyncGroup:(OsnGroupInfo*) osnGroupInfo{
    DMCCGroupInfo* groupInfo = [SqliteUtils queryGroup:osnGroupInfo.groupID];
    if (groupInfo == nil) {
        [_osnsdk getGroupInfo:osnGroupInfo.groupID cb:^(bool isSuccess, id t, NSString *error) {
            OsnGroupInfo* osnGroupInfo = (OsnGroupInfo*)t;
            if(isSuccess){
                [self addGroup2:osnGroupInfo fav:1];
            } else {
                [self addGroupNull:osnGroupInfo.groupID fav:1];
            }
        }];
    }
}
- (void) groupUpdateGroup:(OsnGroupInfo*) osnGroupInfo keys:(NSArray<NSString*>*) keys{
    DMCCGroupInfo* groupInfo = [SqliteUtils queryGroup:osnGroupInfo.groupID];
    if (groupInfo != nil) {
        for (NSString* k in keys) {
            if([k isEqualToString:@"name"]){
                groupInfo.name = osnGroupInfo.name;
                NSLog(@"new group name: %@",groupInfo.name);
            } else if([k isEqualToString:@"portrait"]){
                groupInfo.portrait = osnGroupInfo.portrait;
                NSLog(@"new group portrait: %@",groupInfo.portrait);
            } else if([k isEqualToString:@"type"]){
                groupInfo.type = (DMCCGroupType)osnGroupInfo.type;
                NSLog(@"new group type: %ld",groupInfo.type);
            } else if([k isEqualToString:@"joinType"]){
                groupInfo.joinType = osnGroupInfo.joinType;
                NSLog(@"new group joinType: %d",groupInfo.joinType);
            } else if([k isEqualToString:@"passType"]){
                groupInfo.passType = osnGroupInfo.passType;
                NSLog(@"new group passType: %d",groupInfo.passType);
            } else if([k isEqualToString:@"mute"]){
                groupInfo.mute = osnGroupInfo.mute;
                NSLog(@"new group mute: %d",groupInfo.mute);
            } else if ([k isEqualToString:@"attribute"]) {
                groupInfo.attribute = osnGroupInfo.attribute;
                NSLog(@"new group attribute: %@",groupInfo.attribute);
            } else if ([k isEqualToString:@"billboard"]) {
                groupInfo.notice = osnGroupInfo.billboard;
                NSLog(@"new group notice: %@",groupInfo.notice);
            }
        }
        [SqliteUtils updateGroup:groupInfo keys:keys];
        [self onGroupInfoUpdated:@[groupInfo]];
    }
}
- (void) groupUpdateMember:(OsnGroupInfo*) osnGroupInfo keys:(NSArray<NSString*>*) keys{
    NSMutableArray<DMCCGroupMember*>* memberList;
    DMCCGroupInfo* groupInfo = [SqliteUtils queryGroup:osnGroupInfo.groupID];
    if (groupInfo != nil) {
        memberList = [NSMutableArray new];
        for (OsnMemberInfo* m in osnGroupInfo.userList) {
            
            DMCCGroupMember* groupMember = [SqliteUtils queryMember:m.groupID memberID:m.osnID];
            if (groupMember == nil) {
                NSLog(@"no my memberID: %@",m.osnID);
                continue;
            }
            [memberList addObject:groupMember];
            NSMutableArray<NSString*>* keyx = [NSMutableArray new];
            for (NSString* k in keys) {
                if ([k isEqualToString:@"nickname"] || [k isEqualToString:@"nickName"]) {
                    groupMember.alias = m.nickName;
                    NSLog(@"new member alias: %@, osnID: %@",m.nickName,m.osnID);
                    [keyx addObject:@"alias"];
                } else if ([k isEqualToString:@"type"]) {
                    if (m.type == MemberType_Normal)
                        groupMember.type = Member_Type_Normal;
                    else if (m.type == MemberType_Owner)
                        groupMember.type = Member_Type_Owner;
                    else if (m.type == MemberType_Admin)
                        groupMember.type = Member_Type_Manager;
                    [keyx addObject:@"type"];
                    NSLog(@"new member type: %d, osnID: %@",m.type,m.osnID);
                }
            }
            [SqliteUtils updateMember:groupMember keys:keyx];
        }
        [self onGroupMemberUpdated:osnGroupInfo.groupID members:memberList];
    }
}
- (void) groupDelMember:(OsnGroupInfo*) osnGroupInfo state:(NSString*) state{
    NSMutableArray<DMCCGroupMember*>* memberList;
    DMCCGroupInfo *groupInfo = [SqliteUtils queryGroup:osnGroupInfo.groupID];
    if (groupInfo != nil) {
        memberList = [[SqliteUtils queryMembers:osnGroupInfo.groupID]mutableCopy];
        NSMutableArray<OsnMemberInfo*>* delList = [[self getMemberX:osnGroupInfo.userList m1:memberList exclude:false]mutableCopy];
        if (delList.count) {
            for (OsnMemberInfo* memberInfo in delList) {
                NSLog(@"=test2= delete members: %@",memberInfo.osnID);
                
                // 处理一下删除的是自己
                if ([memberInfo.osnID isEqualToString: [self userId]]) {
                    DMCCConversationInfo* convInfo = [SqliteUtils queryConversation:Group_Type target:osnGroupInfo.groupID line:0];
                    if (convInfo != nil) {
                        if (convInfo.isMember != 0) {
                            convInfo.isMember = 0;
                            [SqliteUtils updateConversation:convInfo keys:@[@"isMember"]];
                        }
                    }
                }
            }
                
            [SqliteUtils deleteMembers:delList];
            groupInfo.memberCount -= delList.count;
            [SqliteUtils updateGroup:groupInfo keys:@[@"memberCount"]];
            [self onGroupInfoUpdated:@[groupInfo]];
            [self onGroupMemberUpdated:osnGroupInfo.groupID members:memberList];
            [self onGroupMemberAddOrLessUpdated:osnGroupInfo.groupID members:memberList];
            [self delMemberNotify:osnGroupInfo.groupID members:delList state:state osnGroupInfo:osnGroupInfo];
        } else
            NSLog(@"del is empty");
    }
}
- (void) groupAddMember:(OsnGroupInfo*) osnGroupInfo{
    NSMutableArray<DMCCGroupMember*>* memberList;
    DMCCGroupInfo *groupInfo = [SqliteUtils queryGroup:osnGroupInfo.groupID];
    if (groupInfo != nil) {
        memberList = [[SqliteUtils queryMembers:osnGroupInfo.groupID]mutableCopy];
        NSMutableArray<OsnMemberInfo*>* addList = [[self getMemberX:osnGroupInfo.userList m1:memberList exclude:true]mutableCopy];
        if (addList.count) {
            [memberList removeAllObjects];
            for (OsnMemberInfo* m in addList) {
                if (m.nickName == nil) {
                    DMCCUserInfo* userInfo = [SqliteUtils queryUser:m.osnID];
                    if (userInfo == nil) {
                        OsnUserInfo* u = [_osnsdk getUserInfo:m.osnID cb:nil];
                        if (u != nil) {
                            userInfo = [[DMCCIMService sharedDMCIMService]toClientUser:u];
                            [SqliteUtils insertUser:userInfo];
                        }
                    }
                    if (userInfo != nil)
                        m.nickName = userInfo.name;
                }
                [memberList addObject:[[DMCCIMService sharedDMCIMService] toClientMember:m]];
                NSLog(@"add members: %@",m.osnID);
            }
            groupInfo.memberCount += memberList.count;
            [SqliteUtils updateGroup:groupInfo keys:@[@"memberCount"]];
            [SqliteUtils insertMembers:memberList];
            [self onGroupInfoUpdated:@[groupInfo]];
            memberList = [[SqliteUtils queryMembersTop:osnGroupInfo.groupID]mutableCopy];
            [self onGroupMemberUpdated:osnGroupInfo.groupID members:memberList];
            [self onGroupMemberAddOrLessUpdated:osnGroupInfo.groupID members:memberList];
            [self addMemberNotify:osnGroupInfo.groupID members:addList osnGroupInfo:osnGroupInfo];
        } else
            NSLog(@"add is empty");
    }
}
- (void) groupQuitGroup:(OsnGroupInfo*) osnGroupInfo state:(NSString*) state{
    NSMutableArray<DMCCGroupMember*>* memberList;
    DMCCGroupInfo* groupInfo = [SqliteUtils queryGroup:osnGroupInfo.groupID];
    if (groupInfo != nil) {
        OsnMemberInfo* memberInfo = osnGroupInfo.userList[0];
        if ([memberInfo.osnID isEqualToString:self.userId])
            [self delGroup:osnGroupInfo.groupID state:@"quit"];
        else {
            [SqliteUtils deleteMembers:osnGroupInfo.userList];
            memberList = [[SqliteUtils queryMembers:osnGroupInfo.groupID]mutableCopy];
            [self onGroupMemberUpdated:osnGroupInfo.groupID members:memberList];
        }
        NSLog(@"quitGroup: %@",memberInfo.osnID);
        [self delMemberNotify:osnGroupInfo.groupID members:@[memberInfo] state:state osnGroupInfo:osnGroupInfo];
    }
}
- (void) groupDelGroup:(OsnGroupInfo*) osnGroupInfo{
    DMCCGroupInfo *groupInfo = [SqliteUtils queryGroup:osnGroupInfo.groupID];
    if (groupInfo != nil) {
        [self delGroup:osnGroupInfo.groupID state:@"dismiss"];
        [self delGroupNotify:osnGroupInfo];
        NSLog(@"delGroup: %@",osnGroupInfo.groupID);
    }
}
- (void) groupUpgradeBomb:(OsnGroupInfo*) osnGroupInfo state:(NSString*) state{
    DMCCGroupInfo *groupInfo = [SqliteUtils queryGroup:osnGroupInfo.groupID];
    if (groupInfo != nil) {
        groupInfo.redPacket = 1;
        [SqliteUtils updateGroup:groupInfo keys:@[@"redPacket"]];
        DMCCMessage *message = [DMCCMessage new];
        message.fromUser = groupInfo.target;
        message.direction = MessageDirection_Receive;
        message.status = Message_Status_Readed;
        message.conversation = [DMCCConversation conversationWithType:Group_Type target:osnGroupInfo.groupID line:0];
        
        
        message.serverTime = [osnGroupInfo getNoticeServerTime];
        if (osnGroupInfo.notice != nil) {
            message.messageHash = osnGroupInfo.notice[@"hash"];
            message.messageHasho = osnGroupInfo.notice[@"hash0"];
        }

        message.content = [self getGroupNotify:state info:osnGroupInfo];
        long mid = [SqliteUtils insertMessage:message];
        if (mid != -1) {
            [self onReceiveMessage:[NSArray arrayWithObject:message] hasMore:false];
        }
        
        //onGroupInfoUpdated(Collections.singletonList(groupInfo));
        NSLog(@"upgradeBomb: %@", osnGroupInfo.groupID);
    }
}
- (void) groupWin:(OsnGroupInfo*) osnGroupInfo state:(NSString*) state{
    DMCCMessage *message = [DMCCMessage new];
    message.fromUser = osnGroupInfo.groupID;
    message.direction = MessageDirection_Receive;
    message.status = Message_Status_Readed;
    message.conversation = [DMCCConversation conversationWithType:Group_Type target:osnGroupInfo.groupID line:0];
    message.serverTime = [OsnUtils getTimeStamp];
    message.content = [self getGroupNotify:state info:osnGroupInfo];
    message.messageId = message.messageUid = [SqliteUtils insertMessage:message];
    if (osnGroupInfo.notice) {
        message.messageHash = osnGroupInfo.notice[@"hash"];
        message.messageHasho = osnGroupInfo.notice[@"hash0"];
    }
    [self onReceiveMessage:@[message] hasMore:false];
}

- (void)groupAddAdmin:(OsnGroupInfo*) osnGroupInfo{
    for (OsnMemberInfo *info in osnGroupInfo.userList) {
        DMCCGroupMember *groupMember = [SqliteUtils queryMember:osnGroupInfo.groupID memberID:info.osnID];
        if (groupMember) {
            groupMember.type = Member_Type_Manager;
            [SqliteUtils updateMember:groupMember keys:@[@"type"]];
        }
    }
}

- (void)groupDelAdmin:(OsnGroupInfo*) osnGroupInfo{
    for (OsnMemberInfo *info in osnGroupInfo.userList) {
        DMCCGroupMember *groupMember = [SqliteUtils queryMember:osnGroupInfo.groupID memberID:info.osnID];
        if (groupMember) {
            groupMember.type = Member_Type_Normal;
            [SqliteUtils updateMember:groupMember keys:@[@"type"]];
        }
    }
}

- (void) groupMute:(OsnGroupInfo*) osnGroupInfo{
    DMCCGroupMember *groupMember = [SqliteUtils queryMember:osnGroupInfo.groupID memberID:_userId];
    if (groupMember == nil){
        NSLog(@"no in the member");
        return;
    }
    groupMember.mute = osnGroupInfo.mute;
    [SqliteUtils updateMember:groupMember keys:@[@"mute"]];
    [self onGroupMemberUpdated:osnGroupInfo.groupID members:@[groupMember]];

    DMCCGroupNotifyContent *groupNotifyContent = [DMCCGroupNotifyContent new];
    groupNotifyContent.info = osnGroupInfo.data[@"text"];
    DMCCMessage *message = [DMCCMessage new];
    message.fromUser = osnGroupInfo.groupID;
    message.direction = MessageDirection_Receive;
    message.status = Message_Status_Readed;
    message.conversation = [DMCCConversation conversationWithType:Group_Type target:osnGroupInfo.groupID line:0];
    
    message.serverTime = [osnGroupInfo getNoticeServerTime];
    if (osnGroupInfo.notice != nil) {
        message.messageHash = osnGroupInfo.notice[@"hash"];
        message.messageHasho = osnGroupInfo.notice[@"hash0"];
    }
    message.content = groupNotifyContent;
    long mid = [SqliteUtils insertMessage:message];
    if (mid != -1) {
        [self onReceiveMessage:@[message] hasMore:false];
    }
}
- (DMCCGroupNotifyContent*) getGroupNotify:(NSString*) state info:(OsnGroupInfo*) groupInfo{
    DMCCGroupNotifyContent *groupNotifyContent = [DMCCGroupNotifyContent new];
    @try {
        if ([state isEqualToString:@"UpgradeBomb"])
            groupNotifyContent.info = LocalizedString(@"group_upgrade");
        else if ([state isEqualToString:@"win"]) {
            double price = ((NSNumber*)groupInfo.data[@"balance"]).doubleValue/1000000;
            DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:groupInfo.data[@"winner"] refresh:false];
            groupNotifyContent.info = [NSString stringWithFormat:@"%@%@[%@]%@, %.6f", @"🎉", LocalizedString(@"bless_win"),(userInfo==nil?LocalizedString(@"you"):userInfo.displayName),LocalizedString(@"group_who_win"),price];
        }
    }
    @catch (NSException *e){
        NSLog(@"%@",e);
    }
    return groupNotifyContent;
}
- (void) onConnectSuccess:(NSString*) state{
    if ([state isEqualToString:@"logined"]){
        self.userId = [_osnsdk getUserID];
        [self onConnectionStatusChanged:kConnectionStatusConnected];
    }
    else if([state isEqualToString:@"connected"])
        [self onConnectionStatusChanged:kConnectionStatusConnected];
}
- (void) onConnectFailed:(NSString*) error{
    self.logined = false;
    if ([error containsString:@"KickOff"])
        [self logout:kConnectionStatusKickedoff];
    else
        [self onConnectionStatusChanged:kConnectionStatusUnconnected];
}
- (NSArray *) onRecvMessage:(NSArray<OsnMessageInfo*>*) msgList{
   
    RecvMessageReturnInfo *info = [self recvMessage:msgList history:false];
   
    if (info == nil) {
        return [NSMutableArray arrayWithCapacity:0];
    }
    NSArray<DMCCMessage*>* messageList = info.messages;
    NSArray *newList = info.nLists;
    if (messageList == nil) {
        return newList;
    }
    if (messageList.count == 0)
        return newList;
    
    for (DMCCMessage* message in messageList) {
        
        DMCCConversationInfo* conversationInfo = [SqliteUtils queryConversation:(int)message.conversation.type  target:message.conversation.target line:message.conversation.line];
        if (conversationInfo == nil) {
            [SqliteUtils insertConversation:(int)message.conversation.type target:message.conversation.target line:message.conversation.line];
            conversationInfo = [SqliteUtils queryConversation:(int)message.conversation.type target:message.conversation.target line:message.conversation.line];
            if (conversationInfo.conversation.type == Group_Type) {
                [self addGroupDrop:conversationInfo.conversation.target];
            }
        }
        conversationInfo.timestamp = message.serverTime;
        conversationInfo.lastMessage = message;
        conversationInfo.unreadCount.unread += 1;
        
        if (message.conversation.type == Group_Type) {
            if ([message.content isKindOfClass:[DMCCTextMessageContent class]]) {
                DMCCTextMessageContent *content = (DMCCTextMessageContent *)message.content;
                if (content.mentionedType == 1) {
                    for (NSString *str in content.mentionedTargets) {
                        if ([str isEqualToString:[[DMCCIMService sharedDMCIMService] getUserID]]) {
                            conversationInfo.unreadCount.unreadMention += 1;
                        }
                    }
                }
            }
        }
        [SqliteUtils updateConversation:conversationInfo];
        
        if (message.conversation.type == Service_Type) {
            conversationInfo = [SqliteUtils queryConversation:Notify_Type target:@"0" line:message.conversation.line];
            if (conversationInfo == nil) {
                [SqliteUtils insertConversation:Notify_Type target:@"0" line:message.conversation.line];
                conversationInfo = [SqliteUtils queryConversation:Notify_Type target:@"0" line:message.conversation.line];
            }
            conversationInfo.timestamp = message.serverTime;
            conversationInfo.lastMessage = message;
            conversationInfo.unreadCount.unread += 1;
            [SqliteUtils updateConversation:conversationInfo];
        }
        // 判断消息免打扰
        if (!conversationInfo.isSilent) {
            [self initializeLocalNotification];
            if (self.soundTime + 1000 * 5 < [OsnUtils getTimeStamp]) {
                AudioServicesPlaySystemSound(1003);
                AudioServicesPlaySystemSound(kSystemSoundID_Vibrate);
                self.soundTime = [OsnUtils getTimeStamp];
            }
        }
    }
   
    [self pushMessage:messageList];

    return newList;
}
- (void) onRecvRequest:(OsnRequestInfo*) request{
    if (request.isGroup) {
        DMCCFriendRequest* friendRequest = [DMCCFriendRequest new];
        friendRequest.type = request.isApply ? RequestType_ApplyMember : RequestType_InviteGroup;
        friendRequest.readStatus = 0;
        friendRequest.direction = RequestDirection_Recv;
        friendRequest.reason = request.reason;
        friendRequest.status = 0;
        friendRequest.target = request.userID;
        friendRequest.originalUser = request.originalUser;
        friendRequest.userID = request.targetUser;
        friendRequest.timestamp = [OsnUtils getTimeStamp];
        [SqliteUtils insertFriendRequest:friendRequest];

        [self onFriendRequestUpdated:@[request.reason]];
    } else {
        if ([[DMCCIMService sharedDMCIMService]isMyFriend:request.userID])
            [_osnsdk acceptFriend:request.userID cb:nil];
        else {
            DMCCFriendRequest* friendRequest = [DMCCFriendRequest new];
            friendRequest.type = RequestType_Friend;
            friendRequest.readStatus = 0;
            friendRequest.direction = RequestDirection_Recv;
            friendRequest.reason = request.reason;
            friendRequest.status = 0;
            friendRequest.target = request.userID;
            friendRequest.timestamp = [OsnUtils getTimeStamp];
            [SqliteUtils insertFriendRequest:friendRequest];
            [self onFriendRequestUpdated:@[request.reason]];
        }
    }
}

- (void) onFriendUpdate:(NSArray<OsnFriendInfo*>*) friendList{
    NSMutableArray<NSString*>* friends = [NSMutableArray new];
    // 与本地数据不相等，清空表数据
    if (friendList.count > 1) {
        NSMutableArray *friendArray = [NSMutableArray arrayWithArray:[[DMCCIMService sharedDMCIMService] getMyFriendList:NO]];
        if (friendList.count != friendArray.count) {
            for (NSString *strLocal in friendArray) {
                BOOL isHav = NO;
                for (OsnFriendInfo *strNew in friendList) {
                    if ([strLocal isEqualToString:strNew.friendID]) {
                        isHav = YES;
                        break ;
                    }
                }
                if (!isHav) {
                    [SqliteUtils deleteFriend:strLocal];
                }
            }
        }
    }
        
    for (OsnFriendInfo* f : friendList) {
        [friends addObject:f.friendID];
        if ([SqliteUtils queryFriend:f.friendID] != nil) {
            if (f.state == FriendState_Deleted) {
                NSLog(@"delete friend - userID:%@, friendID: %@",f.userID,f.friendID);
                [SqliteUtils deleteFriend:f.friendID];
            } else if (f.state == FriendState_Blacked) {
                NSLog(@"blacked friend - friendID: %@",f.friendID);
                [SqliteUtils updateFriend:f];
            }
            continue;
        }

        if (f.state == FriendState_Normal) {
            [SqliteUtils insertFriend:f];
            [self addFriend:f];
        } else if (f.state == FriendState_Syncst) {
            OsnFriendInfo* friendInfo = [_osnsdk getFriendInfo:f.friendID cb:nil];
            if (friendInfo == nil) {
                NSLog(@"get friendInfo failed: %@",f.friendID);
                continue;
            }
            NSLog(@"syncst friend: %@",friendInfo);
            [SqliteUtils insertFriend:friendInfo];
        } else if (f.state == FriendState_Deleted) {
            NSLog(@"delete friend - userID:%@, friendID: %@",f.userID,f.friendID);
            [SqliteUtils deleteFriend:f.friendID];
        } else {
            NSLog(@"friend state is wait: %@", f.friendID);
            [SqliteUtils insertFriend:f];
        }
    }
    [self onFriendListUpdated];
}
- (void) onUserUpdate:(OsnUserInfo*) osnUserInfo keys:(NSArray<NSString*>*) keys{
    DMCCUserInfo* userInfo = [SqliteUtils queryUser:osnUserInfo.userID];
    if (userInfo == nil)
        return;
    for (NSString* k in keys) {
        if ([k isEqualToString:@"displayName"])
            userInfo.displayName = osnUserInfo.displayName;
        else if ([k isEqualToString:@"portrait"])
            userInfo.portrait = osnUserInfo.portrait;
        else if ([k isEqualToString:@"urlSpace"])
            userInfo.urlSpace = osnUserInfo.urlSpace;
    }
    [SqliteUtils updateUser:userInfo keys:keys];
    [self onUserInfoUpdated:@[userInfo]];
}
- (void) onGroupUpdate:(NSString*) state info:(OsnGroupInfo*) osnGroupInfo keys:(NSArray<NSString*>*) keys{
    NSLog(@"=test2= state: %@",state);
    if([state isEqualToString:@"NewlyGroup"]){
        [self groupNewlyGroup:osnGroupInfo];
    } else if([state isEqualToString:@"SyncGroup"]){
        [self groupSyncGroup:osnGroupInfo];
    } else if([state isEqualToString:@"UpdateGroup"]){
        [self groupUpdateGroup:osnGroupInfo keys:keys];
    } else if([state isEqualToString:@"UpdateMember"]){
        [self groupUpdateMember:osnGroupInfo keys:keys];
    } else if([state isEqualToString:@"DelMember"]){
        [self groupDelMember:osnGroupInfo state:state];
    } else if([state isEqualToString:@"AddMember"]){
        [self groupAddMember:osnGroupInfo];
    } else if([state isEqualToString:@"QuitGroup"]){
        [self groupQuitGroup:osnGroupInfo state:state];
    } else if([state isEqualToString:@"DelGroup"]){
        [self groupDelGroup:osnGroupInfo];
    } else if([state isEqualToString:@"UpgradeBomb"]){
        [self groupUpgradeBomb:osnGroupInfo state:state];
    } else if([state isEqualToString:@"win"]){
        [self groupWin:osnGroupInfo state:state];
    } else if([state isEqualToString:@"Mute"] || [state isEqualToString:@"Allow"]){
        [self groupMute:osnGroupInfo];
    } else if ([state isEqualToString:@"AddAdmin"]) {
        [self groupAddAdmin:osnGroupInfo];
    } else if ([state isEqualToString:@"DelAdmin"]) {
        [self groupDelAdmin:osnGroupInfo];
    } else {
        NSLog(@"=test2= unknown GroupUpdate ate: %@",state);
    }
}
- (void)onServiceInfo:(NSArray<OsnServiceInfo *> *)infos {
}

- (void) onRecvUserInfo:(OsnUserInfo*) data{
    DMCCUserInfo *userInfo = [[DMCCIMService sharedDMCIMService]toClientUser:data];
    [SqliteUtils insertUser:userInfo];
}

- (void) onReceiveRecall:(NSDictionary*) data{
    //System.out.println("@@@@ onReceiveRecall begin");
    NSString* to = data[@"to"];
    NSString* from = data[@"from"];
    NSString* messageHash = data[@"messageHash"];
    NSLog(@"@@@@ %@",messageHash);

    if (strncmp(to.UTF8String, "OSNG", 4)){
        NSLog(@"@@@@ group");
        // 判断消息是否是from发来的消息
        DMCCMessage* message = [SqliteUtils queryGroupMessageWithHash0:messageHash];
        if (message != nil) {
            if ([message.fromUser isEqualToString:from]){
                DMCCRecallMessageContent *content = [DMCCRecallMessageContent new];
                message.content = content;
                content.operatorId = message.conversation.target;
                [SqliteUtils recallMessage:message];
            }
        } else {
            NSLog(@"@@@ message is null");
        }
        
        dispatch_async(dispatch_get_main_queue(), ^{
          [[NSNotificationCenter defaultCenter] postNotificationName:kRecallMsgInfoUpdated object:nil];
        });
        
        return;
    }
    //System.out.println("@@@ " + messageHash);
    //

    DMCCMessage* message = [SqliteUtils queryMessageWithHash:messageHash];
    if (message != nil) {
        DMCCRecallMessageContent *content = [DMCCRecallMessageContent new];
        message.content = content;
        content.operatorId = message.conversation.target;
        [SqliteUtils recallMessage:message];
    } else {
        NSLog(@"@@@ message is null");
    }
    dispatch_async(dispatch_get_main_queue(), ^{
      [[NSNotificationCenter defaultCenter] postNotificationName:kRecallMsgInfoUpdated object:nil];
    });
}

- (void) onDeleteMessageTo:(NSDictionary*) data{

    NSString* to = data[@"to"];
    NSString* from = data[@"from"];
    NSString* messageHash = data[@"messageHash"];
    NSLog(@"@@@@ %@",messageHash);

    if (strncmp(to.UTF8String, "OSNG", 4)){
        NSLog(@"@@@@ group");
        // 判断消息是否是from发来的消息
        DMCCMessage* message = [SqliteUtils queryGroupMessageWithHash0:messageHash];
        if (message != nil) {
            [SqliteUtils deleteMessage:message.messageId];
        } else {
            NSLog(@"@@@ message is null");
        }
        
        dispatch_async(dispatch_get_main_queue(), ^{
          [[NSNotificationCenter defaultCenter] postNotificationName:kDeleteMsgInfoUpdated object:nil];
        });
        
        return;
    }
    //System.out.println("@@@ " + messageHash);
    //



    NSLog(@"@@@ %@",messageHash);
    //
    DMCCMessage* message = [SqliteUtils queryMessageWithHash:messageHash];
    if (message != nil) {

        [SqliteUtils deleteMessage:message.messageId];
    } else {
        NSLog(@"@@@ message is null");
    }
    dispatch_async(dispatch_get_main_queue(), ^{
      [[NSNotificationCenter defaultCenter] postNotificationName:kDeleteMsgInfoUpdated object:nil];
    });
}

- (void)logout:(ConnectionStatus)status{
    _logined = false;
    _userId = nil;
    [_osnsdk logout:nil];
    [SqliteUtils closeDb];
    _currentConnectionStatus = status;
    [self onConnectionStatusChanged:status];
    //[[DMCCNetworkStatus sharedInstance] Stop];
}

- (NSString *)getConfig:(NSString *)key {
    return [[NSUserDefaults standardUserDefaults] objectForKey:key];
}

- (void)setConfig:(NSString *)key data:(NSString *)value {
    [[NSUserDefaults standardUserDefaults] setValue:value forKey:key];
}

@end

@implementation RecvMessageReturnInfo
@end
