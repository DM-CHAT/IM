//
//  MHLoginModel.h
//  NewZolo
//
//  Created by JTalking on 2022/6/29.
//

#import <Foundation/Foundation.h>
@class MHJsonModel;
NS_ASSUME_NONNULL_BEGIN

@interface MHLoginModel : NSObject

@property (nonatomic, copy) NSString *password;
@property (nonatomic, copy) NSString *refreshToken;
@property (nonatomic, copy) NSString *username;
@property (nonatomic, assign) NSInteger language;
@property (nonatomic, copy) NSString *token;
@property (nonatomic, copy) NSString *command;
@property (nonatomic, strong) MHJsonModel *json;
@property (nonatomic, copy) NSString *ip;

@end

@interface MHJsonModel : NSObject

@property (nonatomic, copy) NSString *ACCOUNT_PREFIX_URL;
@property (nonatomic, copy) NSString *APP_DEVICE;
@property (nonatomic, copy) NSString *APP_URL;
@property (nonatomic, copy) NSString *AccessKeyId;
@property (nonatomic, copy) NSString *AccessKeySecret;
@property (nonatomic, copy) NSString *AddGroup;
@property (nonatomic, copy) NSString *BOMB_ROLE_URL;
@property (nonatomic, copy) NSString *BUCKETNAME;
@property (nonatomic, copy) NSString *BombRole;
@property (nonatomic, copy) NSString *ENDPOINT;
@property (nonatomic, copy) NSString *GETTRANSFEREST_URL;
@property (nonatomic, copy) NSString *GROUP_PROFIT_URL;
@property (nonatomic, copy) NSString *GROUP_UPDATE_COUNT;
@property (nonatomic, copy) NSString *GROUP_ZERO;
@property (nonatomic, copy) NSString *GroupList;
@property (nonatomic, copy) NSString *GroupPortraitDirectory;
@property (nonatomic, copy) NSString *HOST_IP;
@property (nonatomic, copy) NSString *KEFU_LIST;
@property (nonatomic, copy) NSString *LOGIN_URL;
@property (nonatomic, copy) NSString *QueryAplets;
@property (nonatomic, copy) NSString *RemoteTempFilePath;
@property (nonatomic, copy) NSString *SET_GROUP_URL;
@property (nonatomic, copy) NSString *SET_NAME;
@property (nonatomic, copy) NSString *TRANSFER_URL;
@property (nonatomic, copy) NSString *TempDirectory;
@property (nonatomic, copy) NSString *UserPortraitDirectory;
@property (nonatomic, copy) NSString *WALLET_URL;
@property (nonatomic, copy) NSString *PREFIX;
@property (nonatomic, copy) NSString *DEL_ACCOUNT_URL;
@property (nonatomic, copy) NSString *Alias;
@property (nonatomic, copy) NSString *HIDE_ENABLE;
@property (nonatomic, copy) NSString *MainDapp;
@property (nonatomic, copy) NSString *JpushAppKey;
@property (nonatomic, copy) NSString *voiceHostUrl;
@property (nonatomic, copy) NSString *voiceBaseUrl;
@property (nonatomic, copy) NSString *redPack;
@property (nonatomic, copy) NSString *create_group_url;
@property (nonatomic, copy) NSString *buglyKey;

@end

//"ACCOUNT_PREFIX_URL" = "https://luckmoney8888.com/api/account/getAccount";
//"APP_DEVICE" = "https://luckmoney8888.com/api/user/device";
//"APP_URL" = "https://luckmoney8888.com/static/download.html";
//AccessKeyId = LTAI5tQ2JnUwihLqmPSvHDkB;
//AccessKeySecret = DZB9JYIrlt6gJeJVtIwP9xlqZkWgT3;
//AddGroup = "https://luckmoney8888.com/api/groupProgram/add";
//"BOMB_ROLE_URL" = "https://luckmoney8888.com/api/transfer/getBombRole";
//BUCKETNAME = "zolo-image1";
//BombRole = "https://luckmoney8888.com/api/bomb_role/hideEnable";
//ENDPOINT = "https://oss-ap-southeast-1.aliyuncs.com";
//"GETTRANSFEREST_URL" = "https://luckmoney8888.com/api/transfer/getTransferResult";
//"GROUP_PROFIT_URL" = "https://luckmoney8888.com/index.html#/profit?group_id=";
//"GROUP_UPDATE_COUNT" = "https://luckmoney8888.com/api/group/getUpgroupUserCount";
//"GROUP_ZERO" = "https://luckmoney8888.com/api/group/zeroGroupOwner";
//GroupList = "https://luckmoney8888.com/api/program/list";
//GroupPortraitDirectory = "groupPortrait/";
//"HOST_IP" = "8.219.11.57";
//"KEFU_LIST" = "https://luckmoney8888.com/api/config/custormer";
//"LOGIN_URL" = "https://luckmoney8888.com/#/login";
//QueryAplets = "https://luckmoney8888.com/api/groupProgram/findOne";
//RemoteTempFilePath = "https://zolo-image1.oss-ap-southeast-1.aliyuncs.com/";
//"SET_GROUP_URL" = "https://luckmoney8888.com/api/transfer/upgradeGroup";
//"SET_NAME" = "https://luckmoney8888.com/api/user/setName";
//"TRANSFER_URL" = "https://luckmoney8888.com/api/transfer/transfer";
//TempDirectory = "temp/";
//UserPortraitDirectory = "userPortrait/";
//"WALLET_URL" = "https://luckmoney8888.com/index.html#/wallet";
//pefix = "https://luckmoney8888.com/api";

NS_ASSUME_NONNULL_END
