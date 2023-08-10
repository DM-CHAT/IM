//
//  OsnSDK.m
//  sdktest
//
//  Created by abc on 2021/1/17.
//  Copyright © 2021 test. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "osnsdk.h"
#import <sys/socket.h>
#import <arpa/inet.h>
#import "utils/OsnUtils.h"
#import "utils/HttpUtils.h"
#import "utils/EcUtils.h"

@interface OsnSDK (){
NSString *mOsnID;
NSString *mOsnKey;
NSString *mShadowKey;
NSString *mServiceID;
NSString *mAesKey;
NSString *mDeviceID;
NSString *mTempID;
NSString *mTempKey;
NSString *imLoginToken;
    
long mRID;
bool mLogined;
bool mInitSync;
long mMsgSyncID;
bool mMsgSynced;
NSString *mHost;
int mPort;
int mTimeoutCount;
int mHeartCount;
int mSock;
bool mConnect;
bool isAvailable;
dispatch_semaphore_t mHearter;
id<OSNListener> mOsnListener;
NSMutableDictionary *mIDMap;
dispatch_queue_t mQueue;
NSObject *msgProcesslock;
NSMutableArray *complateQueueList;
dispatch_semaphore_t msgComplateHearter;
    
//dispatch_queue_t tcpMsgQueue;
//dispatch_semaphore_t tcpMsgSem;
NSMutableArray *tcpMsgQueueList;
NSObject *tcpMsglock;
}
@end

@implementation OsnSDK
OsnSDK* inst = nil;
+ (OsnSDK*) getInstance{
    if(inst == nil)
        inst = [OsnSDK new];
    return inst;
}
- (id)init{
    self = [super init];
    self->mOsnID = nil;
    mOsnKey = nil;
    mServiceID = nil;
    imLoginToken = nil;
    mAesKey = nil;
    mDeviceID = nil;
    mTempID = nil;
    mTempKey = nil;
    mRID = [OsnUtils getTimeStamp];
    mLogined = false;
    mInitSync = false;
    mMsgSyncID = 0;
    mMsgSynced = false;
    mHost = nil;
    mPort = 8100;
    mTimeoutCount = 0;
    mHeartCount = 0;
    mSock = 0;
    mConnect = false;
    isAvailable = true;
    mHearter = dispatch_semaphore_create(0);
    mOsnListener = nil;
    mIDMap = [NSMutableDictionary new];
    mQueue = dispatch_queue_create("com.ospn.osnsdk", DISPATCH_QUEUE_CONCURRENT);
    msgProcesslock = [NSObject new];
    complateQueueList = [[NSMutableArray alloc] init];
    msgComplateHearter = dispatch_semaphore_create(0);
    
//    tcpMsgQueue = dispatch_queue_create("com.ospn.osnsdk", DISPATCH_QUEUE_CONCURRENT);
    //tcpMsgSem = dispatch_semaphore_create(0);
    tcpMsgQueueList = [[NSMutableArray alloc] init];
    tcpMsglock = [NSObject new];
    return self;
}
- (void)setCallback:(id<OSNListener>) cb{
    mOsnListener = cb;
}
- (NSString*)timestamp{
	long timestamp = [OsnUtils getTimeStamp];
    return [NSString stringWithFormat:@"%ld",timestamp];
}
- (NSMutableDictionary*)sendPackage2:(NSMutableDictionary*)json{
    @try{
        if(!mConnect)
            return nil;
        long timestamp;
        @synchronized (self) {
            timestamp = mRID++;
        }
        NSString *ids = [NSString stringWithFormat:@"%ld",timestamp];
        json[@"id"] = ids;

        NSLog(@"sendPackage %@:%@",json[@"command"],[OsnUtils dic2Json:json]);
        NSData *jsonData = [NSJSONSerialization dataWithJSONObject:json options:0 error:nil];
        Byte headData[4];
        headData[0] = (jsonData.length>>24)&0xff;
        headData[1] = (jsonData.length>>16)&0xff;
        headData[2] = (jsonData.length>>8)&0xff;
        headData[3] = (jsonData.length>>0)&0xff;
        @synchronized (self) {
            send(mSock,headData,4,0);
            send(mSock,jsonData.bytes,jsonData.length,0);
        }
        __block NSMutableDictionary *result = nil;
        dispatch_semaphore_t lock = dispatch_semaphore_create(0);
        SYNCallback synCallback = ^(NSString *ids, NSDictionary *data){
            result = [data mutableCopy];
            dispatch_semaphore_signal(lock);
        };
        mIDMap[ids] = synCallback;
        dispatch_semaphore_wait(lock, dispatch_time(DISPATCH_TIME_NOW,NSEC_PER_SEC*15));
        [mIDMap removeObjectForKey:ids];
        return result;
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
    return nil;
}
- (NSMutableDictionary*)sendPackage:(NSMutableDictionary*)json{
    @try{
        if(!mConnect)
            return nil;
        long timestamp;
        @synchronized (self) {
            timestamp = mRID++;
        }
        NSString *ids = [NSString stringWithFormat:@"%ld",timestamp];
        json[@"id"] = ids;

        NSLog(@"sendPackage %@:%@",json[@"command"],[OsnUtils dic2Json:json]);
        NSData *jsonData = [NSJSONSerialization dataWithJSONObject:json options:0 error:nil];
        Byte headData[4];
        headData[0] = (jsonData.length>>24)&0xff;
        headData[1] = (jsonData.length>>16)&0xff;
        headData[2] = (jsonData.length>>8)&0xff;
        headData[3] = (jsonData.length>>0)&0xff;
        @synchronized (self) {
            send(mSock,headData,4,0);
            send(mSock,jsonData.bytes,jsonData.length,0);
        }
        __block NSMutableDictionary *result = nil;
        dispatch_semaphore_t lock = dispatch_semaphore_create(0);
        SYNCallback synCallback = ^(NSString *ids, NSDictionary *data){
            result = [data mutableCopy];
            dispatch_semaphore_signal(lock);
        };
        mIDMap[ids] = synCallback;
        dispatch_semaphore_wait(lock, dispatch_time(DISPATCH_TIME_NOW,NSEC_PER_SEC*8));
        [mIDMap removeObjectForKey:ids];
        return result;
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
    return nil;
}

- (NSMutableDictionary*)shortMessage:(NSMutableDictionary*)json cb:(onResult)cb {
    
    int sock = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP);
    
    struct sockaddr_in ser = {0};
    ser.sin_family = AF_INET;
    ser.sin_port = htons(mPort);
    ser.sin_addr.s_addr = inet_addr( mHost.UTF8String );
    
    if(connect(sock, (struct sockaddr*)&ser, sizeof(ser)) < 0){
        NSLog(@"connect failed");
        dispatch_async(mQueue, ^{[self->mOsnListener onConnectFailed:@"sock connect error"];});
        if (cb != nil) {
            cb(false,nil,@"connect failed");
        }
        close(sock);
        return nil;
    }
    @try{
      
        long timestamp;
        @synchronized (self) {
            timestamp = mRID++;
        }
        NSString *ids = [NSString stringWithFormat:@"%ld",timestamp];
        json[@"id"] = ids;

        NSLog(@"sendPackage %@:%@",json[@"command"],[OsnUtils dic2Json:json]);
        NSData *jsonData = [NSJSONSerialization dataWithJSONObject:json options:0 error:nil];
        Byte headData[4];
        headData[0] = (jsonData.length>>24)&0xff;
        headData[1] = (jsonData.length>>16)&0xff;
        headData[2] = (jsonData.length>>8)&0xff;
        headData[3] = (jsonData.length>>0)&0xff;
        @synchronized (self) {
            send(sock,headData,4,0);
            send(sock,jsonData.bytes,jsonData.length,0);
        }
        __block NSMutableDictionary *result = nil;
        dispatch_semaphore_t lock = dispatch_semaphore_create(0);
        SYNCallback synCallback = ^(NSString *ids, NSDictionary *data){
            result = [data mutableCopy];
            dispatch_semaphore_signal(lock);
        };
        mIDMap[ids] = synCallback;
        dispatch_semaphore_wait(lock, dispatch_time(DISPATCH_TIME_NOW,NSEC_PER_SEC*5));
        [mIDMap removeObjectForKey:ids];
        
        close(sock);
        if (cb != nil) {
            if (result == nil) {
                cb(false, nil, @"");
            } else {
                cb(true, result, @"");
            }
        }
        return result;
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
    close(sock);
    if (cb != nil) {
        cb(false,nil,@"failed");
    }
    return nil;
}


- (NSMutableDictionary*) imRespond:(NSString*)command json:(NSMutableDictionary*) json original:(NSMutableDictionary*) original cb:(onResult)cb{
	//NSLog(@"imRespond %@: %@", command, [OsnUtils dic2Json:json]);
    if(![self isSuccess:json]){
        NSLog(@"error: (%@)%@",command,[self errCode:json]);
        if(cb != nil)
            cb(false,nil,[self errCode:json]);
        return nil;
    }
    NSMutableDictionary *data = [OsnUtils takeMessage:json key:mOsnKey osnID:mOsnID];
	if(data != nil){
        NSLog(@"data: %@", [OsnUtils dic2Json:data]);
		data[@"msgHash"] = original[@"hash"];
		[self mergeJson:json data:data];
    } else {
        NSLog(@"=test2= takeMessage failed. mOsnKey: %@", mOsnKey);
    }
	
    if(cb != nil){
        if(data == nil){
            NSLog(@"error: (%@) takeMessage", command);
            cb(false,nil,[self errCode:data]);
        } else {
            cb(true,data,nil);
		}
    }
    return data;
}




- (NSMutableDictionary*) imRequest2:(NSString*) command to:(NSString*) to data:(NSMutableDictionary*) data cb:(onResult)cb{
    @try {
        NSLog(@"%@: ",(data==nil?@"null":[OsnUtils dic2Json:data]));
        NSMutableDictionary *json = [OsnUtils makeMessage:command from:mOsnID to:to data:data key:mOsnKey];
        if(cb != nil){
            dispatch_async(mQueue, ^{
                NSMutableDictionary *result = [self sendPackage2:json];
                if(result != nil){
                    [self imRespond:command json:result original:json cb:cb];
                } else {
                    cb(false, nil, @"null");
                }
            });
            return nil;
        }
        NSMutableDictionary *result = [self sendPackage2:json];
        if(json != nil){
            return [self imRespond:command json:result original:json cb:cb];
        }
    }
    @catch (NSException *e){
        NSLog(@"%@",e);
        if(cb != nil)
            cb(false,nil,e.reason);
    }
    return nil;
}
- (NSMutableDictionary*) imRequest:(NSString*) command to:(NSString*) to data:(NSMutableDictionary*) data cb:(onResult)cb{
    @try {
        
        if (data == nil) {
            //NSLog(@"=test2= imRequest command %@ param nil.",command);
        } else {
            //NSLog(@"=test2= imRequest command %@ param %@: ",command,[OsnUtils dic2Json:data]);
        }
        
        //NSLog(@"=test2= %@: ",(data==nil?@"null":[OsnUtils dic2Json:data]));
        NSMutableDictionary *json = [OsnUtils makeMessage:command from:mOsnID to:to data:data key:mOsnKey];
        if (json == nil) {
            NSLog(@"=test2= imRequest json nil.");
            if (cb != nil) {
                cb(false, nil, @"null");
            }
            return nil;
        }
        if(cb != nil){
            dispatch_async(mQueue, ^{
                NSMutableDictionary *result = [self sendPackage:json];
				if(result != nil){
                	[self imRespond:command json:result original:json cb:cb];
				} else {
					cb(false, nil, @"null");
				}
            });
            return nil;
        }
        NSMutableDictionary *result = [self sendPackage:json];
		if(json != nil){
        	return [self imRespond:command json:result original:json cb:cb];
		}
    }
    @catch (NSException *e){
        NSLog(@"=test2= %@",e);
        if(cb != nil)
            cb(false,nil,e.reason);
    }
    return nil;
}
- (NSMutableDictionary*) imRequestForResend:(NSString*) command to:(NSString*) to data:(NSMutableDictionary*) data cb:(onResult)cb{
    
    @try {
      
        if (cb == nil) {
            return [self imRequest:command to:to data:data cb:cb];
        } else {
            
            [self imRequest:command to:to data:data cb:^(bool isSuccess, id t, NSString *error) {
                if(isSuccess){
                    cb(isSuccess, t, error);
                } else {
                    NSMutableDictionary *json = [OsnUtils makeMessage:command from:self->mOsnID to:to data:data key:self->mOsnKey];
                    if (json == nil) {
                        cb(false, json, @"");
                        return;
                    }
                    dispatch_async(self->mQueue, ^{
                        [self shortMessage:json cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error) {
                            cb(isSuccess, json, error);
                        }];
                    });
                }
            }];
            
        }
      
    }
    @catch (NSException *e){
        NSLog(@"=test2= %@",e);
        if(cb != nil)
            cb(false,nil,e.reason);
    }
    
  
    return nil;
}

- (bool) isSuccess:(NSDictionary*) json{
    if(json == nil)
        return false;
    NSString *errCode = json[@"errCode"];
    if(errCode == nil)
        return false;
    return [errCode isEqualToString:@"success"] || [errCode isEqualToString:@"0:success"];
}
- (NSString*) errCode:(NSDictionary*) json{
    if(json == nil)
        return @"null";
    NSString *errCode = json[@"errCode"];
    if(errCode == nil)
        return @"none";
    return errCode;
}
- (bool) login:(NSString*) user key:(NSString*) key type:(NSString*) type cb:(onResult)cb{
    @try{
        NSMutableDictionary *json = [NSMutableDictionary new];
        json[@"command"] = @"Login";
        json[@"type"] = type;
        json[@"user"] = user;
        if ([type isEqualToString:@"user"]) {
            NSString *t = [self timestamp];
            json[@"token"] = t;
            [mOsnListener setConfig:@"imLoginToken" data:t];
            imLoginToken = t;
            NSLog(@"==token1=%@=", t);
        } else {
            json[@"token"] = imLoginToken;
            NSLog(@"==token2=%@=", imLoginToken);
        }
        json[@"platform"] = @"ios";
		json[@"deviceID"] = mDeviceID;
        json[@"ver"] = @"2";
		NSString *challenge = [self timestamp];
		json[@"challenge"] = [OsnUtils aesEncrypt:challenge keyStr:key];
        NSMutableDictionary *data = [self sendPackage:json];
        if(![self isSuccess:data]){
            if ([data[@"errCode"] containsString:@"10032:"]) {
                [mOsnListener onConnectFailed:@"-1:KickOff"];
            }
            if(cb != nil)
                cb(false,nil,[self errCode:data]);
            return false;
        }
        data = [OsnUtils json2Dics:data[@"content"]];
        NSString *content = [OsnUtils aesDecrypt:data[@"data"] keyStr:key];
        data = [OsnUtils json2Dics:content];
        NSString* challenge1 = [NSString stringWithFormat:@"%lld", [(NSString*)data[@"challenge"] longLongValue] - 1];
        if(![challenge isEqualToString:challenge1]){
			NSLog(@"challenge no match");
			if(cb != nil)
				cb(false, nil, @"challenge no match");
			return false;
		}
		
        NSString *osnID = mOsnID;
        mAesKey = data[@"aesKey"];
        mOsnID = data[@"osnID"];
        NSString* osnKeyTemp = data[@"osnKey"];
        if (osnKeyTemp != nil) {
            if (osnKeyTemp.length > 20) {
                mOsnKey = data[@"osnKey"];
            }
        }
        mServiceID = data[@"serviceID"];
		
		[mOsnListener setConfig:@"osnID" data:mOsnID];
		[mOsnListener setConfig:@"osnKey" data:mOsnKey];
		[mOsnListener setConfig:@"aesKey" data:mAesKey];
		[mOsnListener setConfig:@"serviceID" data:mServiceID];

        mLogined = true;
        [mOsnListener onConnectSuccess:@"logined"];
        if(cb != nil)
            cb(true,data,nil);
        
        dispatch_async(mQueue, ^{[self workSyncing:osnID];});
        return true;
    }
    @catch(NSException* e){
        NSLog(@"%@",e);
        if(cb != nil)
            cb(false,nil,e.reason);
    }
    return false;
}

- (bool) loginV2:(NSString*) user key:(NSString*)password decPassword:(NSString *)decPassword loginInfo:(NSMutableDictionary *)loginInfo {
    @try{
        // pub key
        //NSString * decPassword = @"123";
        NSArray *temp=[password componentsSeparatedByString:@"-"];
        //NSString* shadowKeyData2 = [OsnUtils aesDecrypt:decPassword keyStr:temp[2]];
        
        NSData *decData = [OsnUtils b64Decode:temp[2]];
        //NSData *decKey = [OsnUtils sha256:[NSData dataWithData:[decPassword dataUsingEncoding:NSUTF8StringEncoding]]];
        NSData* decKey = [OsnUtils sha256:[decPassword dataUsingEncoding: NSASCIIStringEncoding]];
        //NSString* keyhash = [OsnUtils b64Encode:decKey];
        //NSLog(@"=test2= sha256 : %@",keyhash);
        NSData* shadowKeyData = [OsnUtils aesDecrypt:decData keyData:decKey];
        NSString* shadowAddress = [ECUtils genAddress:shadowKeyData];
        NSString* shadowPrivateKeyStr = [ECUtils genPrivateKeyStr:shadowKeyData];
        
        //[OsnUtils makeMessage:<#(NSString *)#> from:<#(NSString *)#> to:<#(NSString *)#> data:<#(NSDictionary *)#> key:<#(NSString *)#>]
        
        // gen sign
        NSString* token = [self timestamp];
        NSString *calc = [user stringByAppendingFormat:@"%@%@",loginInfo[@"challenge"],token];
        NSString *hash = [ECUtils osnHash:[calc dataUsingEncoding:NSUTF8StringEncoding]];
        NSString *sign = [ECUtils osnSign:shadowPrivateKeyStr data:[hash dataUsingEncoding:NSUTF8StringEncoding]];
        
        NSMutableDictionary *data = [NSMutableDictionary new];
        data[@"pubKey"] = shadowAddress;
        data[@"token"] = token;
        data[@"sign"] = sign;
        
        mOsnID = user;
        mOsnKey = temp[1];
        mServiceID = loginInfo[@"serviceID"];
        NSMutableDictionary * result = [self imRequest:@"LoginV2" to:mServiceID data:data cb:nil];
        if (result == nil) {
            return false;
        }
        // success.
        NSMutableDictionary *loginEncData = [OsnUtils takeMessage:result key:mOsnKey osnID:mOsnID];
        mAesKey = loginEncData[@"aesKey"];
        mShadowKey = temp[2];
        
        [mOsnListener setConfig:@"osnID" data:mOsnID];
        [mOsnListener setConfig:@"osnKey" data:mOsnKey];
        [mOsnListener setConfig:@"aesKey" data:mAesKey];
        [mOsnListener setConfig:@"serviceID" data:mServiceID];
        [mOsnListener setConfig:@"shadowKey" data:mShadowKey];
        [mOsnListener setConfig:@"imLoginToken" data:token];

        mLogined = true;
        [mOsnListener onConnectSuccess:@"logined"];
        //if(cb != nil)
        //    cb(true,data,nil);
        
        dispatch_async(mQueue, ^{[self workSyncing:user];});
        
        return true;
    }
    @catch(NSException* e){
        NSLog(@"%@",e);
    }
    return false;
}

- (NSMutableDictionary *) getLoginInfo:(NSString*) user key:(NSString*)key{
    
    NSMutableDictionary *json = [NSMutableDictionary new];
    json[@"command"] = @"GetLoginInfo";
    json[@"from"] = user;

    NSMutableDictionary *data = [self sendPackage:json];
    if (![self isSuccess:data]) {
        return nil;
    }
    
    NSMutableDictionary * content = [OsnUtils json2Dics:data[@"content"]];
    
    NSMutableDictionary * decData = [OsnUtils takeMessage:content key:key osnID:user];

    
    return decData;
}

- (void) setMsgSync:(long) timestamp {
	if(!mMsgSynced)
		return;
    if(timestamp < mMsgSyncID)
        return;
    mMsgSyncID = timestamp;
    [mOsnListener setConfig:@"msgSync" data:[NSString stringWithFormat:@"%ld",timestamp]];
}
- (void) mergeJson:(NSMutableDictionary*) json data:(NSMutableDictionary*) data {
    data[@"receive_from"] = json[@"from"];
    data[@"receive_to"] = json[@"to"];
    data[@"receive_hash"] = json[@"hash"];
    data[@"receive_timestamp"] = json[@"timestamp"];
    if (json[@"hash0"]) {
        data[@"receive_hasho"] = json[@"hash0"];
    } else {
        data[@"receive_hasho"] = json[@"hash"];
    }
}
- (NSArray*) getMessages:(NSMutableDictionary*) json {
    NSMutableArray<OsnMessageInfo*> *messages = [NSMutableArray new];
    @try{
        NSArray *array = json[@"msgList"];
        NSLog(@"msgList: %lu",(unsigned long)array.count);
        for(NSString *o in array){
            json = [OsnUtils json2Dic:[o dataUsingEncoding:NSUTF8StringEncoding]];
            NSString *command = json[@"command"];
            if(command != nil && [command isEqualToString:@"Message"]){
                NSMutableDictionary *data = [OsnUtils takeMessage:json key:mOsnKey osnID:mOsnID];
                if(data != nil){
					[self mergeJson:json data:data];
                    [messages addObject:[OsnMessageInfo toMessage:data]];
                }
            }
        }
    }
    @catch(NSException* e){
        NSLog(@"%@",e);
    }
    return messages;
}
- (void) completeMessage:(NSMutableDictionary*) info{
    @try {
        NSString* hash = info[@"receive_hash"];
        if(hash == nil){
            hash = info[@"hash"];
        }
        if(hash == nil){
            return;
        }
        NSMutableDictionary* data = [NSMutableDictionary new];
        data[@"hash"] = hash;
        data[@"sign"] = [ECUtils osnSign:mOsnKey data:[hash dataUsingEncoding:NSUTF8StringEncoding]];
        NSMutableDictionary* json = [OsnUtils wrapMessage:@"Complete" from:mOsnID to:info[@"receive_from"] data:data key:mOsnKey];
        json = [self sendPackage:json];
        NSLog(@"Complete Replay: %@", (json == nil ? @"null" : [OsnUtils dic2Json:json]));
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
}
- (void) completeMessages:(NSArray<NSMutableDictionary*>*) infos {
    @try {
        NSMutableDictionary* receiptList = [NSMutableDictionary new];
        for(NSMutableDictionary* info : infos){
            NSString* hash = info[@"hash"];
            NSString* sign = [ECUtils osnSign:mOsnKey data:[hash dataUsingEncoding:NSUTF8StringEncoding]];
            receiptList[hash] = sign;
            NSLog(@"%@ : %@", hash, sign);
        }
        NSMutableDictionary* data = [NSMutableDictionary new];
        data[@"receiptList"] = receiptList;

        NSMutableDictionary* json = [OsnUtils wrapMessage:@"Complete" from:mOsnID to:mServiceID data:data key:mOsnKey];
        json = [self sendPackage:json];
        NSLog(@"Complete Replay: %@", (json == nil ? @"null" : [OsnUtils dic2Json:json]));
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
}
- (void) completeMessageArray:(NSArray*) infos {
    @try {
        NSMutableDictionary* receiptList = [NSMutableDictionary new];
        for(NSString* hash : infos){
            NSString* sign = [ECUtils osnSign:mOsnKey data:[hash dataUsingEncoding:NSUTF8StringEncoding]];
            receiptList[hash] = sign;
            NSLog(@"%@ : %@", hash, sign);
        }
        NSMutableDictionary* data = [NSMutableDictionary new];
        data[@"receiptList"] = receiptList;

        NSMutableDictionary* json = [OsnUtils wrapMessage:@"Complete" from:mOsnID to:mServiceID data:data key:mOsnKey];
        json = [self sendPackage:json];
        NSLog(@"Complete Replay: %@", (json == nil ? @"null" : [OsnUtils dic2Json:json]));
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
}
- (bool) syncGroup{
    @try{
        NSMutableDictionary* json = [self imRequest:@"GetGroupList" to:mServiceID data:nil cb:nil];
        if(json == nil)
            return false;
        NSArray *groupList = json[@"groupList"];
        for(NSString *o in groupList){
            OsnGroupInfo *groupInfo = [OsnGroupInfo new];
            groupInfo.groupID = o;
            [mOsnListener onGroupUpdate:@"SyncGroup" info:groupInfo keys:nil];
        }
        return true;
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
    return false;
}
- (bool) syncFriend {
    @try {
        NSMutableDictionary* json = [self imRequest:@"GetFriendList" to:mServiceID data:nil cb:nil];
        if(json == nil)
            return false;
        NSArray *friendList = json[@"friendList"];
        NSMutableArray<OsnFriendInfo*> *friendInfoList = [NSMutableArray new];
        for(NSString *o in friendList){
            OsnFriendInfo *friendInfo = [OsnFriendInfo new];
            friendInfo.state = FriendState_Syncst;
            friendInfo.userID = mOsnID;
            friendInfo.friendID = o;
            [friendInfoList addObject:friendInfo];
        }
        if (friendInfoList.count != 0)
            [mOsnListener onFriendUpdate:friendInfoList];
        return true;
    }
    @catch (NSException *e) {
        NSLog(@"%@",e);
    }
    return false;
}
- (long) syncMessage:(long) timestamp count:(int)count{
    @try {
        NSMutableDictionary *data = [NSMutableDictionary new];
        data[@"timestamp"] = [NSNumber numberWithLong:timestamp];
        data[@"count"] = [NSNumber numberWithInt:count];
        NSDictionary *json = [self imRequest:@"MessageSync" to:mServiceID data:data cb:nil];
        if (json != nil) {
            NSArray *array = [json  objectForKey:@"msgList"];
            NSLog(@"msgList: %lu", (unsigned long)array.count);

            bool flag = false;
            NSMutableArray<NSMutableDictionary*> *messageInfos = [NSMutableArray new];

            for (NSString *o : array) {
                NSMutableDictionary *json = [OsnUtils json2Dic:[o dataUsingEncoding:NSUTF8StringEncoding]];
                NSString *command = json[@"command"];
                if(command != nil && [command isEqualToString:@"Message"]){
                    [messageInfos addObject:json];
                    flag = true;
                } else {
                    if(flag){
                        flag = false;
                        [self handleMessageReceives:messageInfos];
                        [messageInfos removeAllObjects];
                    }
                    [self handleMessage:o];
                }
            }
            if (messageInfos.count)
                [self handleMessageReceives:messageInfos];
            return (int)array.count;
        }
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
    return 0;
}
- (void) handleAddFriend:(NSMutableDictionary*) data{
    @try {
        OsnRequestInfo *request = [OsnRequestInfo new];
        request.reason = data[@"reason"];
        request.userID = data[@"receive_from"];
        request.friendID = data[@"receive_to"];
        request.timeStamp = ((NSNumber*)data[@"timestamp"]).longValue;
        request.isGroup = false;
		request.data = data;
        [mOsnListener onRecvRequest:request];
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
}
- (void) handleAgreeFriend:(NSMutableDictionary*) data{
    NSLog(@"agreeFriend data: %@",data);
}
- (void) handleInviteGroup:(NSMutableDictionary*) data{
    @try {
        OsnRequestInfo *request = [OsnRequestInfo new];
        request.reason = data[@"reason"];
        request.userID = data[@"receive_from"];
        request.friendID = data[@"receive_to"];
        request.timeStamp = ((NSNumber*)data[@"timestamp"]).longValue;
        request.originalUser = data[@"originalUser"];
        request.isGroup = true;
        request.isApply = false;
		request.data = data;
        [mOsnListener onRecvRequest:request];
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
}

- (void) handleJoinGroup:(NSMutableDictionary*) data{
    @try {
        OsnRequestInfo *request = [OsnRequestInfo new];
        request.reason = data[@"reason"];
        request.userID = data[@"receive_from"];
        request.friendID = data[@"receive_to"];
        request.timeStamp = ((NSNumber*)data[@"timestamp"]).longValue;
        request.originalUser = data[@"originalUser"];
        request.targetUser = data[@"userID"];
        request.isGroup = true;
        request.isApply = true;
		request.data = data;
        [mOsnListener onRecvRequest:request];
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
}
- (void) handleMessageReceives:(NSArray<NSMutableDictionary*>*) json {
    @try {
        NSMutableArray<OsnMessageInfo*> *messageInfos = [NSMutableArray new];
        for (NSMutableDictionary *o : json) {
            NSMutableDictionary *data = [OsnUtils takeMessage:o key:mOsnKey osnID:mOsnID];
            if(data != nil){
				[self mergeJson:o data:data];
                OsnMessageInfo *messageInfo = [OsnMessageInfo toMessage:data];
                [messageInfos addObject:messageInfo];
            }
        }
        [mOsnListener onRecvMessage:messageInfos];
        [self completeMessages:json];
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
}

- (void) handleMessageReceive:(NSMutableDictionary*) data{
    @try {
        
        OsnMessageInfo *messageInfo = [OsnMessageInfo toMessage:data];
        NSArray *list = [mOsnListener onRecvMessage:[[NSArray alloc]initWithObjects:messageInfo, nil]];
       
        [self pushComplete:list];
    }
    @catch (NSException* e){
    
        NSLog(@"%@",e);
    }
}


- (void)workProcessComplete {
    while (1) {
        [self processCompleteQueue];
    }
}

- (void)processCompleteQueue {
    NSMutableArray *hashList = [[NSMutableArray alloc] init];
    dispatch_semaphore_wait(msgComplateHearter, DISPATCH_TIME_FOREVER);
    
    if (complateQueueList.count == 0) {
        return;
    }
    
    [NSThread sleepForTimeInterval:0.5];
    
    @synchronized (msgProcesslock) {
        [hashList addObjectsFromArray:complateQueueList];
        [complateQueueList removeAllObjects];
    }
    
    [self completeMessageArray:hashList];
}

- (void)pushComplete:(NSArray *)hashList {
    @synchronized (msgProcesslock) {
        [complateQueueList addObjectsFromArray:hashList];
        dispatch_semaphore_signal(msgComplateHearter);
    }
}

- (void) handleGroupUpdate:(NSMutableDictionary*) data notice:(NSMutableDictionary*) notice{
    @try {
        NSArray *array = data[@"infoList"];
        OsnGroupInfo *groupInfo = [OsnGroupInfo toGroupInfo:data];
        if (groupInfo != nil) {
            if (notice != nil) {
                groupInfo.notice = notice;
                NSString *timestamp = notice[@"timestamp"];
                if (timestamp != nil) {
                    groupInfo.noticeServerTime = timestamp;
                }
                //[self setMsgSync:timestamp.longLongValue];
                //groupInfo.noticeServerTime = noticeTime;
            }
        }
        [mOsnListener onGroupUpdate:data[@"state"] info:groupInfo keys:array];
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
}
- (void) handleMessageSet:(NSMutableDictionary*) data {
    @try {
        OsnMessageInfo* messageInfo = [OsnMessageInfo toMessage:data];
        [mOsnListener onRecvMessage:[[NSArray alloc]initWithObjects:messageInfo, nil]];
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
}
- (void) handleUserUpdate:(NSMutableDictionary*) data{
    @try{
        OsnUserInfo *userInfo = [OsnUserInfo new];
        NSArray *array = data[@"infoList"];
        [mOsnListener onUserUpdate:userInfo keys:array];
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
}
- (void) handleFriendUpdate:(NSMutableDictionary*) data{
    @try{
        OsnFriendInfo *friendInfo = [OsnFriendInfo toFriendInfo:data];
        [mOsnListener onFriendUpdate:[[NSArray alloc]initWithObjects:friendInfo, nil]];
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
}
- (void) handleServiceInfo:(NSMutableDictionary*) data {
    @try{
        NSString* type = data[@"type"];
        if([type isEqualToString:@"infos"]){
            [mOsnListener onServiceInfo:[OsnServiceInfo toServiceInfos:data]];
        }
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
}
- (void) handleResult:(NSDictionary*) data {
    @try{
        // result的处理，首先是进行解密
        NSLog(@"[handleResult] %@", [OsnUtils dic2Json:data]);
        NSMutableArray<OsnLitappInfo*>* litappInfos = [NSMutableArray new];
        NSArray<NSDictionary*> *array = data[@"dapps"];
        for(NSDictionary* o : array){
            [litappInfos addObject:[OsnLitappInfo toLitappInfo:o]];
        }
        NSLog(@"%@",litappInfos);
        [mOsnListener onFindResult:litappInfos];
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
}

- (void) handleEncData:(NSDictionary*) data {
    @try{
        NSString* command = data[@"command"];
        NSLog(@"@@@@     command=%@",command);
        if ([command isEqualToString:@"Recall"]) {
            
            BOOL recall = [[NSUserDefaults standardUserDefaults] boolForKey:@"msg_recall"];
            if (recall) {
                return;
            }
            
            NSString* from = data[@"from"];
            NSString* to = data[@"to"];
            NSString* messageHash = data[@"messageHash"];
            NSString* messageHasho = data[@"messageHash0"];
            NSString* sign = data[@"sign"];

            /*if (!mOsnID.equals(to)){
                // 出错处理
                System.out.println("@@@ 1");
                return;
            }*/

            NSString* calc = [NSString stringWithFormat:@"%@%@%@%@",command,from,to,messageHash];
            NSString* hash = [ECUtils osnHash:[calc dataUsingEncoding:NSUTF8StringEncoding]];
            if (![ECUtils osnVerify:from data:[hash dataUsingEncoding:NSUTF8StringEncoding] sign:sign]){
                // 出错处理
                NSLog(@"@@@ 2");
                return;
            }
            //发送时间小于两分钟,撤销消息
            [mOsnListener onReceiveRecall:data];


        } else if ([command isEqualToString:@"DeleteMessage"]) {
            
            BOOL del = [[NSUserDefaults standardUserDefaults] boolForKey:@"msg_del"];
            if (del) {
                return;
            }
            
            NSString* from = data[@"from"];
            NSString* to = data[@"to"];
            NSString* messageHash = data[@"messageHash"];
            NSString* sign = data[@"sign"];

            /*if (!mOsnID.equals(to)){
                // 出错处理
                System.out.println("@@@ 1");
                return;
            }*/

            NSString* calc = [NSString stringWithFormat:@"%@%@%@%@",command,from,to,messageHash];
            NSString* hash = [ECUtils osnHash:[calc dataUsingEncoding:NSUTF8StringEncoding]];
            if (![ECUtils osnVerify:from data:[hash dataUsingEncoding:NSUTF8StringEncoding] sign:sign]){
                // 出错处理
                NSLog(@"@@@ 2");
                return;
            }
            //发送时间小于两分钟,撤销消息
            [mOsnListener onDeleteMessageTo:data];

        }
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
}

- (bool) checkGroupMessageSign:(NSMutableDictionary *)data {
    
    @try {
        
        NSString *sign = data[@"sign"];
        if (sign == nil) {
            return false;
        }
        
        NSString *originalUser = data[@"originalUser"];
        if (originalUser == nil) {
            return false;
        }
        
        NSString *content = data[@"content"];
        if (content == nil) {
            return false;
        }
        
        NSString *timestamp = data[@"timestamp"];
        if (timestamp == nil) {
            return false;
        }
        
        NSString* calc = [NSString stringWithFormat:@"%@%@%@",data[@"originalUser"], data[@"content"], data[@"timestamp"]];
        if (calc == nil) {
            return false;
        }
        NSString *hash = [ECUtils osnHash:[calc dataUsingEncoding:NSUTF8StringEncoding]];
        if (hash == nil) {
            return false;
        }
        return [ECUtils osnVerify:data[@"originalUser"] data:[hash dataUsingEncoding:NSUTF8StringEncoding]  sign:data[@"sign"]];
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
    
    return false;
}



- (void) handleMessage:(NSString*) msg{
    @try {
        NSMutableDictionary *json = [OsnUtils json2Dic:[msg dataUsingEncoding:NSUTF8StringEncoding]];
        NSString *command = json[@"command"];
        NSLog(@"%@: %@",command,msg);
        
        NSString *timestamp = json[@"timestamp"];
		[self setMsgSync:timestamp.longLongValue];

        NSMutableDictionary *data = nil;
        if([command isEqualToString:@"Result"])
            data = [OsnUtils takeMessage:json key:mTempKey osnID:mTempID];
        else
            data = [OsnUtils takeMessage:json key:mOsnKey osnID:mOsnID];
        if(data == nil){
            NSLog(@"[%@] error: takeMessage",command);
            //[self completeMessage: json];
            return;
        } else {

            NSString * from = json[@"from"];
            if (from != nil) {
                if ([from hasPrefix:@"OSNG"]) {
                    if (![self checkGroupMessageSign:data]) {
                        NSLog(@"=test2= group message has no sign");
                    }
                }
            }
        }
        
        
        [self mergeJson:json data:data];
		NSLog(@"data: %@", [OsnUtils dic2Json:json]);
        


		
		if([command isEqualToString:@"RejectMember"]
		    || [command isEqualToString:@"RejectFriend"]){
			[self completeMessage: data];
		} else if([command isEqualToString:@"AddFriend"]){
            [self handleAddFriend:data];
			[self completeMessage: data];
        } else if([command isEqualToString:@"AgreeFriend"]){
            [self handleAgreeFriend:data];
			[self completeMessage: data];
        } else if([command isEqualToString:@"InviteGroup"]
		    || [command isEqualToString:@"Invitation"]){
            [self handleInviteGroup:data];
			[self completeMessage: data];
        } else if([command isEqualToString:@"JoinGroup"]){
            [self handleJoinGroup:data];
			[self completeMessage: data];
        } else if([command isEqualToString:@"Message"]){
            [self handleMessageReceive:data];
        } else if([command isEqualToString:@"SetMessage"]){
        	[self handleMessageSet:data];
        } else if([command isEqualToString:@"UserUpdate"]){
            [self handleUserUpdate:data];
			[self completeMessage: data];
        } else if([command isEqualToString:@"FriendUpdate"]){
            [self handleFriendUpdate:data];
        } else if([command isEqualToString:@"GroupUpdate"]){
            [self handleGroupUpdate:data notice:json];
			[self completeMessage: data];
        } else if([command isEqualToString:@"Result"]){
            [self handleResult:data];
        } else if([command isEqualToString:@"KickOff"]){
            [mOsnListener onConnectFailed:@"-1:KickOff"];
        } else if([command isEqualToString:@"UserInfo"]) {
            [self handleUserInfoResult:data];
            [self completeMessage: data];
        }  else if([command isEqualToString:@"MemberInfo"]) {
            [self handleMemberInfoResult:data];
            [self completeMessage: data];
        } else if ([command isEqualToString:@"EncData"]) {
            [self handleEncData:data];
            [self completeMessage: data];
        }
        else {
            NSLog(@"unknow command: %@",command);
		}	
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
}

// 用户数据
- (void)handleUserInfoResult:(NSMutableDictionary *)data {
    @try {
        OsnUserInfo *userInfo = [OsnUserInfo new];
        userInfo.userID = data[@"userID"];
        userInfo.displayName = data[@"displayName"];
        userInfo.portrait = data[@"portrait"];
        [mOsnListener onRecvUserInfo:userInfo];
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
}

- (void)handleMemberInfoResult:(NSMutableDictionary *)data {
    @try {
        OsnUserInfo *userInfo = [OsnUserInfo new];
        userInfo.userID = data[@"userID"];
        userInfo.displayName = data[@"displayName"];
        userInfo.portrait = data[@"portrait"];
        [mOsnListener onRecvUserInfo:userInfo];
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
}

- (void) workSyncing:(NSString*) osnID{
    if (osnID == nil) {
        return;
    }
    if (!mInitSync || (osnID != nil && ![osnID isEqualToString:mOsnID])) {
        if ([self syncFriend] && [self syncGroup]) {
            mInitSync = true;
            [mOsnListener setConfig:@"initSync" data:@"true"];
        }
    }
    long timestamp = mMsgSyncID;
    while (true) {
        timestamp = [self syncMessage:timestamp count:20];
        if (timestamp == 0 || timestamp == mMsgSyncID)
            break;
        mMsgSyncID = timestamp;
    }
    mMsgSynced = true;
    [self setMsgSync:mMsgSyncID];
}
- (int) workRead:(Byte*)data size:(int)size{
    int ret = 0, idx = 0;
    while(idx < size){
        ret = (int)recv(mSock, data+idx, size-idx, 0);
        if(ret <= 0){
            NSLog(@"sock read error: %d",ret);
            break;
        }
        idx += ret;
    }
    return ret;
}
- (void) workReceive{
	NSLog(@"Start worker thread.");
    while(true) {
        @try {
            NSLog(@"connect to server: %@",mHost);
            
            mLogined = false;
            mConnect = false;
			mMsgSynced = false;
            mSock = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP);
            
            struct sockaddr_in ser = {0};
            ser.sin_family = AF_INET;
            ser.sin_port = htons(mPort);
            ser.sin_addr.s_addr = inet_addr( mHost.UTF8String );
            
            if(connect(mSock, (struct sockaddr*)&ser, sizeof(ser)) < 0){
                NSLog(@"connect failed");
                dispatch_async(mQueue, ^{[self->mOsnListener onConnectFailed:@"sock connect error"];});
                sleep(3);
                continue;
            }
            mConnect = true;
			dispatch_semaphore_signal(mHearter);
			
            NSLog(@"connect to server success");
            dispatch_async(mQueue, ^{[self->mOsnListener onConnectSuccess:@"connected"];});
            
            @try {
                Byte head[4] = {0};
                while(true){
                    if([self workRead:head size:4] <= 0)
                        break;
                    int length = ((head[0] & 0xff) << 24) | ((head[1] & 0xff) << 16) | ((head[2] & 0xff) << 8) | (head[3] & 0xff);
                    Byte *data = (Byte*)malloc(length);
                    if(!data){
                        NSLog(@"malloc error length: %d", length);
                        break;
                    }
                    if([self workRead:data size:length] <= 0)
                        break;
                    __block NSString *msg = [[NSString alloc]initWithData:[NSData dataWithBytesNoCopy:data length:length] encoding:NSUTF8StringEncoding];
                    
                    
                    
                    NSMutableDictionary* json = [OsnUtils json2Dics:msg];
                    NSString* ids = json[@"id"];
                    NSString* command = json[@"command"];
                  
                    if (command != nil && [command isEqualToString:@"Message"]) {
                    
                        [self pushTcpMsg:msg];
                        
                    } else {
                        dispatch_async(mQueue, ^{
                            
                            if (ids != nil) {
   
                                    SYNCallback callback = self->mIDMap[ids];
                                    if (callback != nil) {
                                        callback(ids, json);
                                        return;
                                    }
    
                            }

                            //if(self->mMsgSynced)
                           
                           [self handleMessage:msg];
                            //else
                            //    NSLog(@"drop message when sync: %@ : %@", json[@"command"], [OsnUtils dic2Json:json]);
                        });
                    }
                
                }
                dispatch_async(mQueue, ^{[self->mOsnListener onConnectFailed:@"sock read error"];});
            }
            @catch (NSException* e){
                NSLog(@"%@",e);
                dispatch_async(mQueue, ^{[self->mOsnListener onConnectFailed:@"exception"];});
            }
            close(mSock);
        } @catch (NSException* e) {
            NSLog(@"%@",e);
            dispatch_async(mQueue, ^{[self->mOsnListener onConnectFailed:@"exception"];});
        }
    }
}

- (void)workTcpMsg {
    NSMutableArray *strList = [[NSMutableArray alloc] init];
    while (1) {

        //dispatch_semaphore_wait(tcpMsgSem, DISPATCH_TIME_FOREVER);
        
        [NSThread sleepForTimeInterval:0.5];
        
        if (tcpMsgQueueList.count == 0) {
            continue;
        }
       
        @synchronized (tcpMsglock) {
            strList = [tcpMsgQueueList mutableCopy];
            [tcpMsgQueueList removeAllObjects];
        }
        
        for (NSString *msg in strList) {
            [self handleMessage:msg];
        }
        
        [strList removeAllObjects];
    }
}

- (void)pushTcpMsg:(NSString *)str {
    @synchronized (tcpMsglock) {
        [tcpMsgQueueList addObject:str];
        //dispatch_semaphore_signal(tcpMsgSem);
    }
}

- (void) workHeater{
	NSLog(@"Start heart thread");
    NSMutableDictionary *json = [NSMutableDictionary new];
    json[@"command"] = @"Heart";
    while(true){
        @try {
            dispatch_semaphore_wait(mHearter, dispatch_time(DISPATCH_TIME_NOW,NSEC_PER_SEC*20));
            if (mSock && mConnect) {
                if(mLogined){
                    ++mHeartCount;
                    NSMutableDictionary* result = [self sendPackage:json];
                    if (![self isSuccess:result]) {
                        ++mTimeoutCount;
                        NSLog(@"heart timeout");
                        close(mSock);
                    }
                }else if(mOsnID != nil){
                    [self loginWithOsnID:mOsnID cb:nil];
                }
            }
        }
        @catch (NSException* e){
            NSLog(@"%@",e);
        }
    }
}
- (void) initWorker{
    if(mSock != 0)
        return;

    dispatch_async(mQueue, ^{[self workReceive];});
    dispatch_async(mQueue, ^{[self workHeater];});
    dispatch_async(mQueue, ^{[self workProcessComplete];});
    dispatch_async(mQueue, ^{[self workTcpMsg];});
}

- (void) initSDK:(NSString*) ip cb:(id<OSNListener>)cb{
    mHost = ip;
    mOsnListener = cb;
    if (cb != nil) {
        mOsnID = [mOsnListener getConfig:@"osnID"];
        imLoginToken = [mOsnListener getConfig:@"imLoginToken"];
        mOsnKey = [mOsnListener getConfig:@"osnKey"];
        mAesKey = [mOsnListener getConfig:@"aesKey"];
        mShadowKey = [mOsnListener getConfig:@"shadowKey"];
        mServiceID = [mOsnListener getConfig:@"serviceID"];
        NSString* msgSync = [mOsnListener getConfig:@"msgSync"];
        mMsgSyncID = (msgSync == nil ? 0 : msgSync.intValue);
        if(mMsgSyncID == 0){
            mMsgSyncID = [OsnUtils getTimeStamp];
            [mOsnListener setConfig:@"msgSync" data:[NSString stringWithFormat:@"%ld",mMsgSyncID]];
        }
        NSString* initSync = [mOsnListener getConfig:@"initSync"];
        mInitSync = initSync != nil && [initSync isEqualToString:@"true"];
        mDeviceID = [mOsnListener getConfig:@"deviceID"];
        if(mDeviceID == nil){
            mDeviceID = [OsnUtils createUUID];
            [mOsnListener setConfig:@"deviceID" data:mDeviceID];
        }
        [self initWorker];
    }
}
- (void) createTempAccount{
    if (mTempID == nil || mTempKey == nil) {
        NSArray *newID = [ECUtils createOsnID:@"user"];
        if (newID != nil) {
            [mOsnListener setConfig:@"tempID" data:newID[0]];
            [mOsnListener setConfig:@"tempKey" data:newID[1]];
            mTempID = newID[0];
            mTempKey = newID[1];
        }
    }
}
- (void) resetHost:(NSString*) ip{
    @try {
        NSLog(@"reset host: %@", ip);
        mHost = ip;
        if(mSock != 0)
            close(mSock);
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
}
- (void) registerUser:(NSString*) userName pwd:(NSString*) password sid:(NSString*) serviceID cb:(onResult)cb{
    @try {
        NSMutableDictionary *json = [NSMutableDictionary new];
        json[@"username"] = userName;
        json[@"password"] = password;
        [self imRequest:@"Register" to:serviceID data:json cb:cb];
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
        if(cb != nil)
            cb(false,nil,e.reason);
    }
}
- (bool) loginWithOsnID:(NSString *)userID cb:(onResult)cb{
    return [self login:userID key:mAesKey type:@"osn" cb:cb];
}
- (bool) loginWithName:(NSString *)userName pwd:(NSString *)password cb:(onResult)cb{
    return [self login:userName key:[OsnUtils b64Encode:[OsnUtils sha256:[password dataUsingEncoding:NSUTF8StringEncoding]]] type:@"user" cb:cb];
}
- (bool) loginV2:(NSString *)userName pwd:(NSString *)password decPwd:(NSString*)decPwd cb:(onResult)cb{
    
    NSArray *temp=[password componentsSeparatedByString:@"-"];
    NSMutableDictionary * loginInfo = [self getLoginInfo:userName key:temp[1]];
    if (loginInfo == nil) {
        return false;
    }
    
    return [self loginV2:userName key:password decPassword:decPwd loginInfo:loginInfo];
}

- (void) logout:(onResult)cb{
    @try {
        mOsnID = nil;
        mLogined = false;
        [mOsnListener setConfig:@"osnID" data:mOsnID];
		[mOsnListener setConfig:@"initSync" data:@"false"];
        if(mSock != 0)
            close(mSock);
        if(cb != nil)
            cb(true,nil,nil);
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
        if(cb != nil)
            cb(false,nil,e.reason);
    }
}
- (NSString*) getUserID{
    return mOsnID;
}
- (NSString*) getServiceID{
    return mServiceID;
}
- (NSString*) getShadowKey{
    return mShadowKey;
}
- (OsnUserInfo*) getUserInfo:(NSString*) userID cb:(onResultT)cb{
    @try {
        if(cb == nil){
            NSDictionary *json = [self imRequestForResend:@"GetUserInfo" to:userID data:nil cb:nil];
            if(json != nil)
	            return [OsnUserInfo toUserInfo:json];
        }else{
	        [self imRequestForResend:@"GetUserInfo" to:userID data:nil cb:^(bool isSuccess, NSDictionary *json, NSString *error){
	            OsnUserInfo *userInfo = nil;
	            if(json != nil)
	                userInfo = [OsnUserInfo toUserInfo:json];
	            cb(isSuccess,userInfo,error);
	        }];
		}
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
    return nil;
}
- (OsnGroupInfo*) getGroupInfo:(NSString*) groupID cb:(onResultT)cb{
    @try {
        if(cb == nil){
            NSDictionary *json = [self imRequest:@"GetGroupInfo" to:groupID data:nil cb:nil];
            if(json != nil)
	            return [OsnGroupInfo toGroupInfo:json];
        }else{
	        [self imRequest2:@"GetGroupInfo" to:groupID data:nil cb:^(bool isSuccess, NSDictionary *json, NSString *error){
	            OsnGroupInfo *groupInfo = nil;
	            if(json != nil)
	                groupInfo = [OsnGroupInfo toGroupInfo:json];
	            cb(isSuccess,groupInfo,error);
	        }];
		}
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
    return nil;
}
- (NSArray*) getMemberInfo:(NSString*) groupID cb:(onResultT)cb{
    @try {
        if(cb == nil){
            NSDictionary *json = [self imRequest:@"GetMemberInfo" to:groupID data:nil cb:nil];
            if(json != nil)
	            return [OsnMemberInfo toMemberInfos:json];
        }else{
	        [self imRequest:@"GetMemberInfo" to:groupID data:nil cb:^(bool isSuccess, NSDictionary *json, NSString *error){
	            NSArray<OsnMemberInfo*> *memberInfos = nil;
	            if(json != nil)
	                memberInfos = [OsnMemberInfo toMemberInfos:json];
	            cb(isSuccess,memberInfos,error);
	        }];
		}
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
    return nil;
}
- (NSArray*) getMemberZoneInfo:(NSString*) groupID begin:(int)begin  cb:(onResultT)cb {
    @try {
        if(cb == nil){
            NSDictionary *json = [self imRequest:@"GetMemberZone" to:groupID data:nil cb:nil];
            if(json != nil){
                // need fix index
                return [OsnMemberInfo toMemberInfos:json];
            }
        }else{
            NSMutableDictionary* json = [NSMutableDictionary new];
            json[@"begin"] = @(begin);
            json[@"size"] = @50;
            [self imRequest:@"GetMemberZone" to:groupID data:json cb:^(bool isSuccess, NSDictionary *json, NSString *error){
                NSArray<OsnMemberInfo*> *memberInfos = nil;
                if(json != nil){
                    memberInfos = [OsnMemberInfo toMemberInfos:json];
                }
                cb(isSuccess,memberInfos,error);
            }];
        }
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
    return nil;
}
- (OsnServiceInfo*) getServiceInfo:(NSString*) serviceID cb:(onResultT)cb{
    @try {
        if(cb == nil){
            NSDictionary *json = [self imRequest:@"GetServiceInfo" to:serviceID data:nil cb:nil];
            if(json != nil)
	            return [OsnServiceInfo toServiceInfo:json];
        }else{
	        [self imRequest:@"GetServiceInfo" to:serviceID data:nil cb:^(bool isSuccess, NSDictionary *json, NSString *error){
	            OsnServiceInfo *serviceInfo = nil;
	            if(json != nil)
	                serviceInfo = [OsnServiceInfo toServiceInfo:json];
	            cb(isSuccess,serviceInfo,error);
	        }];
		}
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
    return nil;
}
- (void) findServiceInfo:(NSString*) keyword cb:(onResultT)cb{
    @try {
        NSMutableDictionary* json = [NSMutableDictionary new];
        json[@"keyword"] = keyword;
        json[@"type"] = @"findService";
        [self imRequest:@"Broadcast" to:nil data:json cb:cb];
    }
    @catch (NSException* e) {
        NSLog(@"%@",e);
    }
}
- (OsnFriendInfo*) getFriendInfo:(NSString*) friendID cb:(onResultT)cb{
    @try {
        NSMutableDictionary* data = [NSMutableDictionary new];
        data[@"friendID"] = friendID;
        if(cb == nil){
            NSDictionary *json = [self imRequest:@"GetFriendInfo" to:mServiceID data:data cb:nil];
            if(json != nil)
	            return [OsnFriendInfo toFriendInfo:json];
        }else{
	        [self imRequest:@"GetFriendInfo" to:mServiceID data:data cb:^(bool isSuccess, NSDictionary *json, NSString *error){
	            OsnFriendInfo *info = nil;
	            if(json != nil)
	                info = [OsnFriendInfo toFriendInfo:json];
	            cb(isSuccess,info,error);
	        }];
		}
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
    return nil;
}
- (void) modifyUserInfo:(NSArray*) keys info:(OsnUserInfo*) userInfo cb:(onResult)cb{
    @try {
        NSMutableDictionary *data = [@{}mutableCopy];
        for(NSString *k in keys){
            if([k isEqualToString:@"displayName"])
                data[@"displayName"] = userInfo.displayName;
            else if([k isEqualToString:@"portrait"])
                data[@"portrait"] = userInfo.portrait;
            else if([k isEqualToString:@"urlSpace"])
                data[@"urlSpace"] = userInfo.urlSpace;
            else if([k isEqualToString:@"describes"])
                data[@"describes"] = userInfo.describes;
        }
        [self imRequestForResend:@"SetUserInfo" to:mServiceID data:data cb:cb];
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
}

- (void)saveGroup:(NSString*)groupID status:(int)status cb:(onResult)cb{
    @try {
        NSMutableDictionary *data = [@{}mutableCopy];
        data[@"groupID"] = groupID;
        data[@"status"] = @(status);
        [self imRequestForResend:@"SaveGroup" to:mServiceID data:data cb:cb];
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
}

- (void) modifyFriendInfo:(NSArray*) keys info:(OsnFriendInfo*) friendInfo cb:(onResult)cb{
    @try {
        NSMutableDictionary *data = [@{}mutableCopy];
        data[@"friendID"] = friendInfo.friendID;
        for(NSString *k in keys){
            if([k isEqualToString:@"remarks"])
                data[@"remarks"] = friendInfo.remarks;
            else if([k isEqualToString:@"state"])
                data[@"state"] = @(friendInfo.state);
        }
        [self imRequestForResend:@"SetFriendInfo" to:mServiceID data:data cb:cb];
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
}
- (NSArray*) getFriendList:(onResultT)cb{
    @try {
        if(cb == nil){
            NSDictionary *json = [self imRequest:@"GetFriendList" to:mServiceID data:nil cb:nil];
            if(json != nil){
	            NSMutableArray *friendInfoList = [NSMutableArray array];
	            NSArray *friendList = [json objectForKey:@"friendList"];
	            for(NSString *o in friendList)
	                [friendInfoList addObject:o];
	            return friendInfoList;
			}
        }else{
	        [self imRequest:@"GetFriendList" to:mServiceID data:nil cb:^(bool isSuccess, NSDictionary *json, NSString *error){
	            NSMutableArray *friendInfoList = [NSMutableArray array];
	            if(json != nil){
	                NSArray *friendList = [json objectForKey:@"friendList"];
	                for(NSString *o in friendList)
	                    [friendInfoList addObject:o];
	            }
	            cb(isSuccess,friendInfoList,error);
	        }];
		}
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
    return nil;
}
- (NSArray*) getGroupList:(onResultT)cb{
    @try {
        if(cb == nil){
            NSMutableDictionary *json = [self imRequest:@"GetGroupList" to:mServiceID data:nil cb:nil];
            if(json != nil){
	            NSMutableArray *groupInfoList = [NSMutableArray new];
	            NSArray *groupList = json[@"groupList"];
	            for(NSString *o in groupList)
	                [groupInfoList addObject:o];
	            return groupInfoList;
			}
        }else{
	        [self imRequest:@"GetFriendList" to:mServiceID data:nil cb:^(bool isSuccess, NSDictionary *json, NSString *error){
	            NSMutableArray *friendInfoList = [NSMutableArray new];
	            if(json != nil){
	                NSArray *friendList = json[@"friendList"];
	                for(NSString *o in friendList)
	                    [friendInfoList addObject:o];
	            }
	            cb(isSuccess,friendInfoList,error);
	        }];
		}
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
    return nil;
}
- (void) inviteFriend:(NSString*) userID reason:(NSString*) reason cb:(onResult)cb{
    NSMutableDictionary *data = [NSMutableDictionary new];
    data[@"reason"] = reason;
    [self imRequestForResend:@"AddFriend" to:userID data:data cb:cb];
}
- (void) deleteFriend:(NSString*) userID cb:(onResult)cb{
    NSMutableDictionary *data = [NSMutableDictionary new];
    data[@"friendID"] = userID;
    [self imRequestForResend:@"DelFriend" to:mServiceID data:data cb:cb];
}
- (void)roleAddFriend:(NSString*)type cb:(onResult)cb{
    NSMutableDictionary *data = [NSMutableDictionary new];
    NSDictionary *dic = @{
        @"AddFriend" : type
    };
    data[@"role"] = [OsnUtils dic2Json:dic];
    [self imRequestForResend:@"UpRole" to:mServiceID data:data cb:cb];
}
- (void) acceptFriend:(NSString*) userID cb:(onResult)cb{
    [self imRequestForResend:@"AgreeFriend" to:userID data:nil cb:cb];
}
- (void) rejectFriend:(NSString*) userID cb:(onResult)cb{
    [self imRequestForResend:@"RejectFriend" to:userID data:nil cb:cb];
}
- (void) acceptMember:(NSString*) userID groupID:(NSString*)groupID cb:(onResult)cb{
    NSMutableDictionary *data = [NSMutableDictionary new];
    data[@"userID"] = userID;
    [self imRequestForResend:@"AgreeMember" to:groupID data:data cb:cb];
}
- (void) rejectMember:(NSString*) userID groupID:(NSString*)groupID cb:(onResult)cb{
    NSMutableDictionary *data = [NSMutableDictionary new];
    data[@"userID"] = userID;
    [self imRequestForResend:@"RejectMember" to:groupID data:data cb:cb];
}
- (void) getOwnerSign:(NSString*) groupID cb:(onResult)cb{
    NSMutableDictionary *data = [NSMutableDictionary new];
    data[@"groupID"] = groupID;
    data[@"owner"] = mOsnID;
    data[@"timestamp"] = [self timestamp];
    [self imRequest:@"GetOwnerSign" to:groupID data:data cb:cb];
}
- (void) getGroupSign:(NSString*) groupID info:(NSString*) info cb:(onResult)cb{
    NSMutableDictionary *data = [OsnUtils json2Dics:info];
    [self imRequest2:@"GetGroupSign" to:groupID data:data cb:cb];
}
- (void) setGroupOwner:(NSString*) groupID owner:(NSString*) owner cb:(onResult)cb{
    NSMutableDictionary *data = [NSMutableDictionary new];
    data[@"osnID"] = owner;
    [self imRequest:@"NewOwner" to:groupID data:data cb:cb];
}
- (void) addGroupManager:(NSString*) groupID memberIds:(NSArray<NSString*>*) memberIds cb:(onResult)cb{
    NSMutableDictionary *data = [NSMutableDictionary new];
    data[@"adminList"] = memberIds;
    [self imRequestForResend:@"AddAdmin" to:groupID data:data cb:cb];
}
- (void) delGroupManager:(NSString*) groupID memberIds:(NSArray<NSString*>*) memberIds cb:(onResult)cb{
    NSMutableDictionary *data = [NSMutableDictionary new];
    data[@"adminList"] = memberIds;
    [self imRequestForResend:@"DelAdmin" to:groupID data:data cb:cb];
}
- (void) orderPay:(NSString*) info cb:(onResult)cb{
    NSMutableDictionary *data = [NSMutableDictionary new];
    [self imRequest:@"OrderPay" to:mServiceID data:data cb:cb];
}
- (void) getRedPacket:(NSString*) info userID:(NSString*) userID cb:(onResult)cb{
    NSMutableDictionary *data = [OsnUtils json2Dics:info];
    [self imRequest:@"GetRedPacket" to:userID data:data cb:cb];
}
- (void) sendMessage:(NSString*) text userID:(NSString*) userID cb:(onResult)cb{
    NSMutableDictionary *data = [NSMutableDictionary new];
    data[@"content"] = text;
    if(!strncmp(userID.UTF8String,"OSNG",4)) {
        data[@"originalUser"] = mOsnID;
        
        
        long timestamp = [OsnUtils getTimeStamp];
        NSString* calc = [NSString stringWithFormat:@"%@%@%ld", mOsnID, text, timestamp];
        NSString* hash = [ECUtils osnHash:[calc dataUsingEncoding:NSUTF8StringEncoding]];
        data[@"sign"] = [ECUtils osnSign:mOsnKey data:[hash dataUsingEncoding:NSUTF8StringEncoding]];
        data[@"timestamp"] = @(timestamp);
        
        
    }
    [self imRequestForResend:@"Message" to:userID data:data cb:cb];
    
}
- (void) sendDynamic:(NSString*) text cb:(onResult)cb{
    NSMutableDictionary* data = [NSMutableDictionary new];
    [self imRequest:@"Dynamic" to:mServiceID data:data cb:cb];
}

- (void) sendDescribesWithValue:(NSString *)value cb:(onResult)cb{
    NSMutableDictionary *data = [NSMutableDictionary new];
    NSDictionary *dic = @{
        @"nft" : value
    };
    data[@"data"] = [OsnUtils dic2Json:dic];
    [self imRequest:@"UpDescribes" to:mServiceID data:data cb:cb];
}

- (void) allowTemporaryChatWithData:(NSString *)value cb:(onResult)cb {
    NSMutableDictionary *data = [NSMutableDictionary new];
    NSDictionary *dic = @{
        @"AllowTemporaryChat" : value
    };
    data[@"data"] = [OsnUtils dic2Json:dic];
    [self imRequest:@"UpDescribes" to:mServiceID data:data cb:cb];
}

- (void) sendRemoveDescribes:(NSString *)command Withcb:(onResult)cb {
    NSMutableDictionary *data = [NSMutableDictionary new];
    NSDictionary *dic = @{
        @"command" : command
    };
    data[@"data"] = [OsnUtils dic2Json:dic];
    [self imRequestForResend:@"RemoveDescribes" to:mServiceID data:data cb:cb];
}

- (void) sendBroadcast:(NSString*) content cb:(onResult)cb{
    NSMutableDictionary* data = [NSMutableDictionary new];
    [self imRequest:@"Broadcast" to:nil data:data cb:cb];
}
- (void) deleteMessage:(NSString*) hash osnID:(NSString*) osnID cb:(onResult)cb{
    NSMutableDictionary* data = [NSMutableDictionary new];
    if (osnID == nil) {
        return;
    }
    NSString* target = osnID == nil || !strcmp(osnID.UTF8String, "OSNU") ? mServiceID : osnID;
    data[@"type"] = @"delete";
    data[@"osnID"] = target;
    data[@"messageHash"] = hash;
    [self imRequest:@"SetMessage" to:target data:data cb:cb];
}
- (void) deleteToMessage:(NSString*) hash osnID:(NSString*) osnID cb:(onResult)cb{
    @try {
        NSMutableDictionary* data = [NSMutableDictionary new];
        data[@"command"] = @"DeleteMessage";
        data[@"from"] = mOsnID;
        data[@"to"] = osnID;
        data[@"messageHash"] = hash;
        NSString *calc = [NSString stringWithFormat:@"DeleteMessage%@%@%@", mOsnID, osnID, hash];
        NSString *hashStr = [ECUtils osnHash:[calc dataUsingEncoding:NSUTF8StringEncoding]];
        NSString *signStr = [ECUtils osnSign:mOsnKey data:[hashStr dataUsingEncoding:NSUTF8StringEncoding]];
        data[@"sign"] = signStr;
        [self imRequest:@"EncData" to:osnID data:data cb:cb];
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
}
- (void) recallMessage:(NSString*) hash osnID:(NSString*) osnID cb:(onResult)cb{
    @try {
        NSMutableDictionary* data = [NSMutableDictionary new];
        data[@"command"] = @"Recall";
        data[@"from"] = mOsnID;
        data[@"to"] = osnID;
        data[@"messageHash"] = hash;
        NSString *calc = [NSString stringWithFormat:@"Recall%@%@%@", mOsnID, osnID, hash];
        NSString *hashStr = [ECUtils osnHash:[calc dataUsingEncoding:NSUTF8StringEncoding]];
        NSString *signStr = [ECUtils osnSign:mOsnKey data:[hashStr dataUsingEncoding:NSUTF8StringEncoding]];
        data[@"sign"] = signStr;
        [self imRequest:@"EncData" to:osnID data:data cb:cb];
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
}
- (NSArray<OsnMessageInfo*>*) loadMessage:(NSString*) userID timestamp:(long)timestamp count:(int)count before:(bool) before cb:(onResultT)cb {
    @try {
        NSMutableDictionary *data = [NSMutableDictionary new];
        data[@"userID"] = userID;
        data[@"timestamp"] = @(timestamp);
        data[@"count"] = @(count);
        data[@"before"] = @(before);
        if(cb == nil){
            NSMutableDictionary *json = [self imRequest:@"MessageLoad" to:mServiceID data:data cb:cb];
            if(json == nil)
                return nil;
            return [self getMessages:json];
        }
        [self imRequest:@"MessageLoad" to:mServiceID data:data cb:^(bool isSuccess, NSMutableDictionary *json, NSString *error){
            NSArray<OsnMessageInfo*>* messages = [@[]mutableCopy];
            if(json != nil)
                messages = [self getMessages:json];
            cb(isSuccess,messages,error);
        }];
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
    return nil;
}
- (void) createGroup2:(NSString*) groupName membser:(NSArray*) member type:(int)type portrait:(NSString*) portrait cb:(onResult)cb{
    NSMutableDictionary *data = [NSMutableDictionary new];
    data[@"name"] = groupName;
    data[@"type"] = @(type);
    data[@"portrait"] = portrait;
    data[@"userList"] = member;
    [self imRequest:@"CreateGroup" to:mServiceID data:data cb:cb];
}
- (void) createGroup:(NSString*) groupName membser:(NSArray*) member type:(int)type portrait:(NSString*) portrait cb:(onResult)cb{
    NSMutableDictionary *data = [NSMutableDictionary new];
    data[@"command"] = @"CreateGroup";
    data[@"owner"] = mOsnID;
    data[@"name"] = groupName;
    data[@"type"] = @(type);
    data[@"portrait"] = portrait;
    data[@"userList"] = member;
    [self imRequest2:@"EncData" to:mServiceID data:data cb:cb];
}

- (void) joinGroup:(NSString*) groupID reason:(NSString*)reason invitation:(NSString*)invitation cb:(onResult)cb{
    NSMutableDictionary *data = [NSMutableDictionary new];
    data[@"reason"] = reason;
	data[@"invitation"] = invitation;
    [self imRequest:invitation == nil ? @"JoinGroup" : @"JoinGrp" to:groupID data:data cb:cb];
}
- (void) rejectGroup:(NSString*) groupID cb:(onResult)cb{
    [self imRequest:@"RejectGroup" to:groupID data:nil cb:cb];
}
- (void) addMember:(NSString*) groupID members:(NSArray*) members cb:(onResult)cb{
    NSMutableDictionary *data = [NSMutableDictionary new];
    data[@"state"] = @"AddMember";
    data[@"memberList"] = members;
    [self imRequestForResend:@"AddMember" to:groupID data:data cb:cb];
}
- (void) delMember:(NSString*) groupID members:(NSArray*) members cb:(onResult)cb{
    NSMutableDictionary *data = [NSMutableDictionary new];
    data[@"state"] = @"DelMember";
    data[@"memberList"] = members;
    [self imRequestForResend:@"DelMember" to:groupID data:data cb:cb];
}
- (void) quitGroup:(NSString*) groupID cb:(onResult)cb{
    NSMutableDictionary *data = [NSMutableDictionary new];
    data[@"state"] = @"QuitGroup";
    [self imRequestForResend:@"QuitGroup" to:groupID data:data cb:cb];
}
- (void) dismissGroup:(NSString*) groupID cb:(onResult)cb{
    NSMutableDictionary *data = [NSMutableDictionary new];
    data[@"state"] = @"DelGroup";
    [self imRequestForResend:@"DelGroup" to:groupID data:data cb:cb];
}
- (void) muteGroup:(NSString*) groupID state:(int) state members:(NSArray<NSString*>*) members mode:(int)mode cb:(onResult)cb{
    NSMutableDictionary* data = [NSMutableDictionary new];
    data[@"mute"] = @(state);
    data[@"members"] = members;
    data[@"mode"] = @(mode);
    [self imRequestForResend:@"Mute" to:groupID data:data cb:cb];
}
- (void) billboard:(NSString*) groupID text:(NSString*) text cb:(onResult)cb{
    NSMutableDictionary* data = [NSMutableDictionary new];
    data[@"text"] = text;
    [self imRequestForResend:@"Billboard" to:groupID data:data cb:cb];
}
- (void) allowAddFriend:(NSString*)groupID type:(int)type cb:(onResult)cb{
    NSMutableDictionary* data = [NSMutableDictionary new];
    data[@"value"] = type ? @"yes" : @"no";
    data[@"key"] = @"AllowAddFriend";
    [self imRequestForResend:@"UpAttribute" to:groupID data:data cb:cb];
}

- (void) upAttirbuteWithGroupId:(NSString*)groupID key:(NSString *)key value:(NSString *)value cb:(onResult)cb{
    NSMutableDictionary* data = [NSMutableDictionary new];
    data[@"value"] = value;
    data[@"key"] = key;
    [self imRequestForResend:@"UpAttribute" to:groupID data:data cb:cb];
}

- (void) upExtraWithGroupId:(NSString*)groupID key:(NSString *)key value:(NSString *)value cb:(onResult)cb{
    NSMutableDictionary* data = [NSMutableDictionary new];
    data[@"value"] = value;
    data[@"key"] = key;
    [self imRequestForResend:@"UpDescribe" to:groupID data:data cb:cb];
}

- (void) upGroupPwdWithGroupId:(NSString*)groupID key:(NSString *)key value:(NSString *)value cb:(onResult)cb{
    NSMutableDictionary* data = [NSMutableDictionary new];
    data[@"value"] = value;
    data[@"key"] = key;
    [self imRequestForResend:@"UpPrivateInfo" to:groupID data:data cb:cb];
}

- (void) removeAttirbuteWithGroupId:(NSString*)groupID key:(NSString *)key cb:(onResult)cb{
    NSMutableDictionary* data = [NSMutableDictionary new];
    data[@"keys"] = [NSMutableArray arrayWithObject:key];
    [self imRequestForResend:@"RemoveAttribute" to:groupID data:data cb:cb];
}

- (void) removeAttirbuteWithGroupId:(NSString*)groupID keys:(NSArray *)keys cb:(onResult)cb{
    NSMutableDictionary* data = [NSMutableDictionary new];
    data[@"keys"] = keys;
    [self imRequestForResend:@"RemoveAttribute" to:groupID data:data cb:cb];
}

- (void) setTempAccount:(NSString*) tempAcc cb:(onResult)cb{
    NSMutableDictionary* data = [NSMutableDictionary new];
    data[@"tempAcc"] = tempAcc;
    [self imRequest:@"SetTemp" to:mServiceID data:data cb:cb];
}
- (void) findObject:(NSString*) text cb:(onResult)cb{
    NSString* tempAcc = mTempID;
    NSMutableDictionary* data = [NSMutableDictionary new];
    data[@"command"] = @"findObj";
    data[@"from"] = tempAcc;
    data[@"text"] = text;
    NSLog(@"[findObject] data: %@", [OsnUtils dic2Json:data]);
    [self imRequest:@"FindObj" to:mServiceID data:data cb:cb];
}
- (void) modifyGroupInfo:(NSArray*) keys groupInfo:(OsnGroupInfo*) groupInfo cb:(onResult)cb{
    @try {
        NSMutableDictionary *data = [NSMutableDictionary new];
        for(NSString *k in keys){
            if([k isEqualToString:@"name"])
                data[@"name"] = groupInfo.name;
            else if([k isEqualToString:@"portrait"])
                data[@"portrait"] = groupInfo.portrait;
            else if([k isEqualToString:@"type"])
                data[@"type"] = @(groupInfo.type);
            else if([k isEqualToString:@"joinType"])
                data[@"joinType"] = @(groupInfo.joinType);
            else if([k isEqualToString:@"passType"])
                data[@"passType"] = @(groupInfo.passType);
            else if([k isEqualToString:@"mute"])
                data[@"mute"] = @(groupInfo.mute);
            else if ([k isEqualToString:@"attribute"])
                data[@"attribute"] = groupInfo.attribute;
            else if ([k isEqualToString:@"billboard"])
                data[@"billboard"] = groupInfo.billboard;
        }
        [self imRequestForResend:@"SetGroupInfo" to:groupInfo.groupID data:data cb:cb];
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
}
- (void) modifyMemberInfo:(NSArray*) keys memberInfo:(OsnMemberInfo*) memberInfo cb:(onResult)cb{
    @try {
        NSMutableDictionary *data = [NSMutableDictionary new];
        for(NSString *k in keys){
            if([k isEqualToString:@"nickName"])
                data[@"nickName"] = memberInfo.nickName;
            else if([k isEqualToString:@"status"])
                data[@"status"] = @(memberInfo.status);
            else if([k isEqualToString:@"type"])
                data[@"type"] = @(memberInfo.type);
        }
        [self imRequestForResend:@"SetMemberInfo" to:memberInfo.groupID data:data cb:cb];
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
}
- (void) uploadData:(NSString*) fileName type:(NSString*) type data:(NSData*) data cb:(onResult)cb progress:(onProgress)progress{
    HttpUtils *http = [HttpUtils new];
    NSString *url = [NSString stringWithFormat:@"http://%@:8800/",mHost];
    if(cb == nil){
        [http upload:url type:type name:fileName data:data cb:cb progress:progress];
    } else {
        dispatch_async(mQueue, ^(){
            [http upload:url type:type name:fileName data:data cb:cb progress:progress];
        });
    }
}
- (void) downloadData:(NSString*) remoteUrl localPath:(NSString*) localPath decKey:(NSString *)decKey cb:(onResult)cb progress:(onProgress)progress {
    NSLog(@"====2222==downloadData====decKey==");
    HttpUtils *http = [HttpUtils new];
    if(cb == nil){
        dispatch_async(mQueue, ^(){
            [http download:remoteUrl path:localPath decKey:decKey cb:cb progress:progress];
        });
    } else {
        dispatch_async(mQueue, ^(){
            [http download:remoteUrl path:localPath decKey:decKey cb:cb progress:progress];
        });
    }
}

- (void) downloadData:(NSString*) remoteUrl localPath:(NSString*) localPath cb:(onResult)cb progress:(onProgress)progress{
    NSLog(@"====111==downloadData======");
    HttpUtils *http = [HttpUtils new];
    if(cb == nil){
        dispatch_async(mQueue, ^(){
            [http download:remoteUrl path:localPath cb:cb progress:progress];
        });
    } else {
        dispatch_async(mQueue, ^(){
            [http download:remoteUrl path:localPath cb:cb progress:progress];
        });
    }
}

- (void) lpLogin:(OsnLitappInfo*) litappInfo url:(NSString*) url cb:(onResult)cb {
    [self simpleLogin:litappInfo.target url:url cb:cb];
}

- (void) simpleLogin:(NSString*) target url:(NSString*) url cb:(onResult)cb {
    dispatch_async(mQueue, ^(){
		NSString* error = nil;
        @try {
            long randClient = [OsnUtils getTimeStamp];
            NSMutableDictionary *json = [NSMutableDictionary new];
            json[@"command"] = @"GetServerInfo";
            json[@"user"] = self->mOsnID;
            json[@"random"] = @(randClient);
            NSData *data = [HttpUtils doPost:url data:[OsnUtils dic2Json:json]];
            json = [OsnUtils json2Dic:data];
            NSString *serviceID = json[@"serviceID"];
            NSString *randServer = json[@"random"];
            NSString *serverInfo = json[@"serviceInfo"];
            NSString *session = json[@"session"];
            
            if(![serviceID isEqualToString:target]){
                cb(false, nil, [NSString stringWithFormat:@"serviceID no equals litappID: %@, serviceID: %@",target,serviceID]);
            }else{
                NSString *datas = [NSString stringWithFormat:@"%@%@%@%@%@",self->mOsnID,@(randClient),serviceID,randServer,serverInfo];
	            NSString *hashCheck = [ECUtils osnHash:[datas dataUsingEncoding:NSUTF8StringEncoding]];
                NSString *signCheck = json[@"sign"];
	            NSLog(@"%@",hashCheck);
	            if([hashCheck isEqualToString:json[@"hash"]]
				    && [ECUtils osnVerify:serviceID data:[hashCheck dataUsingEncoding:NSUTF8StringEncoding] sign:signCheck]){
                    datas = [NSString stringWithFormat:@"%@%@%@%@",serviceID,randServer,self->mOsnID,@(randClient)];
		            NSString *hash = [ECUtils osnHash:[datas dataUsingEncoding:NSUTF8StringEncoding]];
                    NSString *sign = [ECUtils osnSign:self->mOsnKey data:[hash dataUsingEncoding:NSUTF8StringEncoding]];
		            [json removeAllObjects];
		            json[@"command"] = @"Login";
                    json[@"user"] = self->mOsnID;
		            json[@"hash"] = hash;
		            json[@"sign"] = sign;
		            json[@"session"] = session;
		            data = [HttpUtils doPost:url data:[OsnUtils dic2Json:json]];
		            json = [OsnUtils json2Dic:data];
		            if(![self isSuccess:json]){
						error = [self errCode:json];
		            } else {
		                NSString *sessionKey = json[@"sessionKey"];
                        NSData *sessionData = [ECUtils ecDecrypt2:self->mOsnKey data:sessionKey];
                        
                        char *sbuffer = NULL;
                        const char* sessionDataBuffer = (char *)[sessionData bytes];
                        int len = (int)strlen(sessionDataBuffer);
                        sbuffer = (char*)malloc(len);
                        strcpy(sbuffer, sessionDataBuffer);
                        NSData *sData = [NSData dataWithBytes:sbuffer length:len];
                        
		                NSMutableDictionary *result = [json mutableCopy];
		                result[@"sessionKey"] = [[NSString alloc]initWithData:sData encoding:NSUTF8StringEncoding];
		                cb(true,result,nil);
		            }
	            }else{
					error = @"verify error";
				}
			}
        }
        @catch (NSException* e){
            NSLog(@"%@",e);
            cb(false,nil,e.reason);
        }
		if(error != nil)
			cb(false,nil,error);
    });
}
- (NSString*) hashData:(NSData*)data{
    return [ECUtils osnHash:data];
}
- (NSString*) signData:(NSData*) data {
    return [ECUtils osnSign:mOsnKey data:data];
}
- (Boolean) verifyData:(NSString*) osnID data:(NSData*) data sign:(NSString*) sign{
    return [ECUtils osnVerify:osnID data:data sign:sign];
}
- (NSString*) encryptData:(NSString*) osnID data:(NSData*)data{
    return [ECUtils ecEncrypt2:osnID data:data];
}
@end
