//
//  ZoloAPIManager.m
//  NewZolo
//
//  Created by JTalking on 2022/8/30.
//

#import "ZoloAPIManager.h"
#import "ZoloDappModel.h"
#import "ZoloServiceModel.h"
#import "ZoloTagModel.h"
#import "ZoloKeywordModel.h"
#import "ZoloNotice.h"

@interface ZoloAPIManager () {
    dispatch_queue_t msgQueue;
}

@end

@implementation ZoloAPIManager

static ZoloAPIManager *_APIManager;

+ (instancetype)allocWithZone:(struct _NSZone *)zone {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _APIManager = [super allocWithZone:zone];
    });
    return _APIManager;
}

+ (instancetype)instanceManager {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _APIManager = [[self alloc] init];
    });
    return _APIManager;
}

- (id)copyWithZone:(NSZone *)zone {
    return _APIManager;
}

/** 获取小程序列表 */
- (void)getDappListWithGroupId:(NSString *)groupId withCompleteBlock:(void(^)(NSArray *data))completeBlock {
    NSDictionary *dic = @{
                             @"groupId" : groupId
                         };
    [ZoloHttpTool setFormRequest];
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    [ZoloHttpTool GET:login.json.GroupList params:dic success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            NSArray *data = [ZoloDappModel jsonsToModelsWithJsons:dict[@"data"][@"records"]];
            completeBlock(data);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(nil);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(nil);
    }];
}

/** 查询绑定的小程序*/
- (void)checkBindDappWithGroupId:(NSString *)groupId ownerId:(NSString *)ownerId withCompleteBlock:(void(^)(NSDictionary *data))completeBlock {
    NSDictionary *dic = @{
                             @"groupId" : groupId,
                             @"owner" : ownerId
                         };
    [ZoloHttpTool setFormRequest];
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    [ZoloHttpTool GET:login.json.QueryAplets params:dic success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            completeBlock(dict[@"data"]);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(nil);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(nil);
    }];
}

/** 新增绑定的小程序*/
- (void)addBindDappWithDic:(NSDictionary *)dic withCompleteBlock:(void(^)(BOOL isSuccess))completeBlock {
    [ZoloHttpTool setJsonRequest];
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    [ZoloHttpTool POST:login.json.AddGroup params:dic success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            completeBlock(YES);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(NO);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(NO);
    }];
}

/** 获取交易参数*/
- (void)checkPayResultWithPayId:(NSString *)payId withCompleteBlock:(void(^)(NSDictionary *data))completeBlock {
    NSDictionary *dic = @{
                             @"messageId" : payId
                         };
    [ZoloHttpTool setFormRequest];
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    [ZoloHttpTool GET:login.json.GETTRANSFEREST_URL params:dic success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if (data == nil) {
            completeBlock(nil);
        } else if ([self invitedCode:dict[@"code"]]) {
            @try {
                NSMutableDictionary *mdic = [NSMutableDictionary dictionaryWithDictionary:dict[@"data"]];
                [mdic setObject:dict[@"wallet"] forKey:@"wallet"];
                [mdic setObject:dict[@"coinType"] forKey:@"coinType"];
                completeBlock(mdic);
            }
            @catch (NSException* e){
                NSLog(@"%@",e);
                completeBlock(nil);
            }
        } else {
            if ([dict[@"code"] longLongValue] == 40000004) {
                NSDictionary *dic = [OsnUtils json2Dics:dict[@"msg"]];
                NSDictionary *dics = @{
                    @"msg" : dic[@"error_count"],
                    @"count" : dic[@"count"],
                    @"error" : dict[@"code"]
                };
                completeBlock(dics);
            } else if ([dict[@"code"] longLongValue] == 40003007) {
                [MHAlert showMessage:LocalizedString(@"RedGroup")];
                completeBlock(nil);
            } else {
                NSDictionary *dic = @{
                    @"msg" : dict[@"msg"],
                    @"error" : dict[@"code"]
                };
                completeBlock(dic);
            }
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(nil);
    }];
}

/** 获取客服列表 */
- (void)getCustomListWithCompleteBlock:(void(^)(NSDictionary *data))completeBlock {
    [ZoloHttpTool setJsonRequest];
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    [ZoloHttpTool POST:login.json.KEFU_LIST params:@{} success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            completeBlock(dict);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(nil);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(nil);
    }];
}

/** 用户注销 */
- (void)deleteUserWithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock {
    [ZoloHttpTool setFormRequest];
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    [ZoloHttpTool GET:login.json.DEL_ACCOUNT_URL params:@{} success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            completeBlock(YES);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(NO);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(NO);
    }];
}

/** 设置用户昵称 */
- (void)settingUserName:(NSString *)name WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock {
    NSDictionary *dic = @{
                             @"name" : name
                         };
    [ZoloHttpTool setJsonRequest];
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    [ZoloHttpTool POST:login.json.SET_NAME params:dic success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            completeBlock(YES);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(NO);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(NO);
    }];
}

/** 获取红包规则 */
- (void)getRedRoleWithLanguage:(NSString *)Language WithCompleteBlock:(void(^)(NSDictionary *data))completeBlock {
    NSDictionary *dic = @{
                             @"language" : Language
                         };
    [ZoloHttpTool setJsonRequest];
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    [ZoloHttpTool POST:login.json.BOMB_ROLE_URL params:dic success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            completeBlock(dict[@"data"]);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(nil);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(nil);
    }];
}
/** 推送 */

- (void)pushWithName:(NSString *)name osnID:(NSString *)osnID language:(NSString *)language WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock {
    NSDictionary *dic = @{
                            @"name" : name,
                            @"osnID" : osnID,
                            @"language" : language,
                            @"vendor" : @"apple"
                         };
    [ZoloHttpTool setJsonRequest];
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    [ZoloHttpTool POST:login.json.APP_DEVICE params:dic success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            completeBlock(YES);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(NO);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(NO);
    }];
}

/** 获取服务器列表*/
- (void)getServiceListWithCompleteBlock:(void(^)(NSArray *data))completeBlock {
    NSString *url = [NSString stringWithFormat:@"https://luckmoney8888.com/api/im/serviceProviders"];
    [ZoloHttpTool setFormRequest];
    [ZoloHttpTool GET:url params:@{} success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            NSArray *data = [ZoloServiceModel jsonsToModelsWithJsons:dict[@"data"]];
            completeBlock(data);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(nil);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(nil);
    }];
}

/** 获取osnID*/
- (void)getOsnId:(NSString *)string WithCompleteBlock:(void(^)(NSString *data))completeBlock {
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }

    if (login.json.PREFIX == nil) {
        completeBlock(string);
        return;
    }
    
    NSString *url = [NSString stringWithFormat:@"%@/im/getOsnId", login.json.PREFIX];
    [ZoloHttpTool setFormRequest];
    NSDictionary *dic = @{
                            @"phone" : string,
                         };
    [ZoloHttpTool GET:url params:dic success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            completeBlock(dict[@"data"][@"osn_id"]);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(string);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(string);
    }];
}

/** 新增tag名称*/
- (void)addTagNameWithName:(NSString *)tagName type:(NSInteger)type osnId:(NSString *)osnId tagId:(NSInteger)tagId WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock {
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/im/addUserGroup", login.json.PREFIX];
    [ZoloHttpTool setJsonRequest];
    NSDictionary *dic = @{
                            @"groupName" : tagName,
                            @"type" : @(type)
                         };
    NSMutableDictionary *mdic = [NSMutableDictionary dictionaryWithDictionary:dic];
    if (type == 2 || type == 3) {
        [mdic setValue:osnId forKey:@"osnId"];
        [mdic setValue:@(tagId) forKey:@"parentId"];
    }
    [ZoloHttpTool POST:url params:mdic success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            completeBlock(YES);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(NO);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(NO);
    }];
}

/** 查询标签列表*/
- (void)addTagNameListWithCompleteBlock:(void(^)(NSArray *data))completeBlock {
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/im/findList", login.json.PREFIX];
    [ZoloHttpTool setJsonRequest];
    NSDictionary *dic = @{
                            @"type" : @(1)
                         };
    [ZoloHttpTool POST:url params:dic success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            NSArray *data = [ZoloTagModel jsonsToModelsWithJsons:dict[@"data"]];
        
            if (data.count == 0) {
                NSArray *tagInfos = [[DMCCIMService sharedDMCIMService] getTagList];
                for (DMCCTagInfo *tag in tagInfos) {
                    [[DMCCIMService sharedDMCIMService] deleteTagWithInfo:tag];
                }
            }
            
            for (ZoloTagModel *tagInfo in data) {
                DMCCTagInfo *info = [DMCCTagInfo new];
                info.id = tagInfo.id;
                info.group_name = tagInfo.group_name;
                [[DMCCIMService sharedDMCIMService] saveTagInfoWithInfo:info];
            }
                        
            [[NSNotificationCenter defaultCenter] postNotificationName:FZ_EVENT_TAGSTATUS_STATE object:nil];
            
            completeBlock(data);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(nil);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(nil);
    }];
}

/** 删除标签用户或群组*/
- (void)delTagUserList:(NSArray *)ids WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock {
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/im/delUserLab", login.json.PREFIX];
    [ZoloHttpTool setFormRequest];
    NSDictionary *dic = @{
                            @"ids" : [ids componentsJoinedByString:@","],
                            @"osnId" : [[DMCCIMService sharedDMCIMService]getUserID]
                         };
    [ZoloHttpTool POST:url params:dic success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            
            completeBlock(YES);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(NO);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(NO);
    }];
}

/** 移动标签用户或群组*/
- (void)moveTagUserList:(NSArray *)ugs tagId:(NSInteger)tagId group_name:(NSString *)group_name WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock {
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/im/transferLab", login.json.PREFIX];
    [ZoloHttpTool setFormRequest];
    NSDictionary *dic = @{
                            @"ids" :  [ugs componentsJoinedByString:@","],
                            @"parentId" : @(tagId),
                            @"groupName" : group_name
                         };
    [ZoloHttpTool POST:url params:dic success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            completeBlock(YES);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(NO);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(NO);
    }];
}

/** 删除标签*/
- (void)delTagId:(NSInteger)tagId WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock {
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/im/del", login.json.PREFIX];
    [ZoloHttpTool setJsonRequest];
    NSDictionary *dic = @{
                            @"id" : @(tagId)
                         };
    [ZoloHttpTool GET:url params:dic success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            completeBlock(YES);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(NO);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(NO);
    }];
}

/** 查询标签所有人员列表*/
- (void)getTagNameUserAllListWithCompleteBlock:(void(^)(NSArray *data))completeBlock {
    msgQueue = dispatch_queue_create("com.ospn.chatApi", DISPATCH_QUEUE_CONCURRENT);
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/im/getLabAllUser", login.json.PREFIX];
    [ZoloHttpTool setJsonRequest];
    [ZoloHttpTool GET:url params:@{} success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        
        dispatch_async(self->msgQueue, ^{
        
            NSData *data = (NSData*)responseObject;
            NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
            if ([self invitedCode:dict[@"code"]]) {
                
                NSArray *userList = [ZoloTagModel jsonsToModelsWithJsons:dict[@"data"][@"userList"]];
                NSArray *groupList = [ZoloTagModel jsonsToModelsWithJsons:dict[@"data"][@"groupList"]];
                
                if (userList.count == 0) {
                    [[DMCCIMService sharedDMCIMService] updateUserTagWithkey:@[@"tagID"]];
                }
                
                if (groupList.count == 0) {
                    [[DMCCIMService sharedDMCIMService] updateGroupTagWithkey:@[@"tagID"]];
                }
                
                for (ZoloTagModel *tagInfo in userList) {
                    [[DMCCIMService sharedDMCIMService] updataTagWithUserInfo:tagInfo.osn_id tagName:tagInfo.group_name key:@[@"tagID"]];
                    [[DMCCIMService sharedDMCIMService] updataTagUgIDWithUserInfo:tagInfo.osn_id tagUgId:tagInfo.id key:@[@"ugID"]];
                    DMCCConversation *conversion = [DMCCConversation conversationWithType:Single_Type target:tagInfo.osn_id line:0];
                    DMCCConversationInfo *conversationInfo = [[DMCCIMService sharedDMCIMService] getConversationInfo:conversion];
                    if (conversationInfo) {
                        [[DMCCIMService sharedDMCIMService] setConversation:conversationInfo.conversation tagID:tagInfo.parent_id];
                    }
                }
                
                for (ZoloTagModel *tagInfo in groupList) {
                    [[DMCCIMService sharedDMCIMService] updataTagWithGroupInfo:tagInfo.osn_id tagName:tagInfo.group_name key:@[@"tagID"]];
                    [[DMCCIMService sharedDMCIMService] updataTagUgIdWithGroupInfo:tagInfo.osn_id tagUgId:tagInfo.id key:@[@"ugID"]];
                    DMCCConversation *conversion = [DMCCConversation conversationWithType:Group_Type target:tagInfo.osn_id line:0];
                    DMCCConversationInfo *conversationInfo = [[DMCCIMService sharedDMCIMService] getConversationInfo:conversion];
                    if (conversationInfo) {
                        [[DMCCIMService sharedDMCIMService] setConversation:conversationInfo.conversation tagID:tagInfo.parent_id];
                    }
                }
                
                completeBlock(nil);
            } else {
                [MHAlert showMessage:dict[@"msg"]];
                completeBlock(nil);
            }
            
        });
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(nil);
    }];
}

/**
    设置关键字
    license  调 GetOwnerSign 获取
 */
- (void)setGroupKeywordWithGroupId:(NSString *)groupId content:(NSString *)content license:(NSString *)license WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock {
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/im/addOrUpdateKeyword", login.json.PREFIX];
    [ZoloHttpTool setJsonRequest];
    NSDictionary *dic = @{
                            @"content" : content,
                            @"groupId" : groupId,
                            @"license" : license,
                         };
    [ZoloHttpTool POST:url params:dic success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            completeBlock(YES);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(NO);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(NO);
    }];
}

/** 查询关键字列表*/
- (void)getKeywordListAndGroupMuteTimeWithGroupId:(NSString *)groupId WithCompleteBlock:(void(^)(ZoloKeywordModel *data))completeBlock {
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/im/keywordListAndTaboo", login.json.PREFIX];
    [ZoloHttpTool setJsonRequest];
    NSDictionary *dic = @{
                            @"groupId" : groupId
                         };
    [ZoloHttpTool GET:url params:dic success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            ZoloKeywordModel *data = [ZoloKeywordModel yy_modelWithJSON:dict[@"data"]];
            completeBlock(data);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(nil);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(nil);
    }];
}

/** 删除关键字*/
- (void)deleteKeywordWithGroupId:(NSString *)groupId keywordId:(NSInteger)keywordId WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock {
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/im/delKeyword", login.json.PREFIX];
    [ZoloHttpTool setJsonRequest];
    NSDictionary *dic = @{
                            @"id" : @(keywordId),
                            @"groupId" : groupId
                         };
    [ZoloHttpTool GET:url params:dic success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            completeBlock(YES);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(NO);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(NO);
    }];
}

/** 是否显示扫雷红包*/
- (void)bombHiddenWithGroupId:(NSString *)groupId WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock {
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/im/bombHidden", login.json.PREFIX];
    [ZoloHttpTool setJsonRequest];
    NSDictionary *dic = @{
                            @"groupId" : groupId
                         };
    [ZoloHttpTool GET:url params:dic success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            NSNumber *isBom = dict[@"data"];
            if (isBom.intValue) {
                completeBlock(YES);
            } else {
                completeBlock(NO);
            }
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(NO);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(NO);
    }];
}

/** 服务器上传img*/
- (void)uploadImgWithGroupId:(NSString *)groupId image:(NSString *)img WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock {
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/user/updateHeadImg", login.json.PREFIX];
    [ZoloHttpTool setJsonRequest];
    NSDictionary *dic = @{
                            @"groupId" : groupId,
                            @"headImg" : img
                         };
    [ZoloHttpTool GET:url params:dic success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            completeBlock(YES);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(NO);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(NO);
    }];
}
/** 服务器上传i用户mg*/
- (void)uploadUserImgWithImage:(NSString *)img WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock {
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/user/updateHeadImg", login.json.PREFIX];
    [ZoloHttpTool setJsonRequest];
    NSDictionary *dic = @{
                            @"headImg" : img
                         };
    [ZoloHttpTool GET:url params:dic success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            completeBlock(YES);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(NO);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(NO);
    }];
}

/** share 内容 */
- (void)getShareContentWithCompleteBlock:(void(^)(NSDictionary *data))completeBlock {
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/im/shareContent", login.json.PREFIX];
    [ZoloHttpTool setJsonRequest];
    [ZoloHttpTool GET:url params:@{} success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            completeBlock(dict[@"data"]);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(nil);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(nil);
    }];
}

/** 判断是否更新 */
- (void)getCheckUpdateWithCompleteBlock:(void(^)(NSDictionary *data))completeBlock {
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/im/getAppVersion", login.json.PREFIX];
    [ZoloHttpTool setJsonRequest];
    [ZoloHttpTool GET:url params:@{} success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            completeBlock(dict[@"data"]);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(nil);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(nil);
    }];
}

/** 创建群聊*/
- (void)createGroupWithMemberList:(NSArray *)member portrait:(NSString *)portrait type:(NSInteger)type name:(NSString *)name withCompleteBlock:(void(^)(NSString *data))completeBlock {
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    [ZoloHttpTool setJsonRequest];
    NSDictionary *dic = @{
                            @"userList" : member,
                            @"portrait" : portrait,
                            @"type" : @(type),
                            @"name" : name,
                            @"owner" : [[DMCCIMService sharedDMCIMService] getUserID],
                            @"command" : @"CreateGroup"
                         };
    [ZoloHttpTool POST:login.json.create_group_url params:dic success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            completeBlock(dict[@"data"]);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(nil);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(nil);
    }];
}

/** 保存dapp*/
- (void)saveDappWithDappJson:(NSString *)str withCompleteBlock:(void(^)(BOOL isSuccess))completeBlock {
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/im/saveProgram", login.json.PREFIX];
    [ZoloHttpTool setJsonRequest];
    NSDictionary *dic = @{
                            @"data_json" : str
                         };
    [ZoloHttpTool POST:url params:dic success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            completeBlock(YES);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(NO);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(NO);
    }];
}

/** 获取dapp*/
- (void)getUserDappWithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock {
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/im/programList", login.json.PREFIX];
    [ZoloHttpTool setJsonRequest];
    [ZoloHttpTool GET:url params:@{} success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            NSArray *data = dict[@"data"];
            for (NSDictionary *litDappDic in data) {
                DMCCLitappInfo *info = [DMCCLitappInfo yy_modelWithJSON:litDappDic[@"data_json"]];
                info.sid = [litDappDic[@"id"] integerValue];
                [[DMCCIMService sharedDMCIMService] saveLitappWithInfo:info];
            }
            completeBlock(YES);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(NO);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(NO);
    }];
}

/** 删除dapp*/
- (void)deleteDappWithId:(NSInteger)dappId WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock {
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/im/delProgram", login.json.PREFIX];
    [ZoloHttpTool setJsonRequest];
    NSDictionary *dic = @{
                            @"id" : @(dappId)
                         };
    [ZoloHttpTool GET:url params:dic success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            completeBlock(YES);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(NO);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(NO);
    }];
}

/** 获取当前用户账号*/
- (void)getUserInfoWithCompleteBlock:(void(^)(NSString *data))completeBlock {
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/user/getAccount", login.json.PREFIX];
    [ZoloHttpTool setJsonRequest];
    [ZoloHttpTool GET:url params:@{} success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            completeBlock(dict[@"data"]);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(nil);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(nil);
    }];
}

/** 获取code验证码*/
- (void)updataPwdCodeWithPhone:(NSString *)phone WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock {
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/login/modifyPasswordCode", login.json.PREFIX];
    [ZoloHttpTool setJsonRequest];
    NSDictionary *dic = @{
                            @"phone" : phone
                         };
    [ZoloHttpTool GET:url params:dic success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            completeBlock(YES);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(NO);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(NO);
    }];
}

/** 根据code修改密码*/
- (void)updataPasswordWithPhone:(NSString *)phone code:(NSString *)code password:(NSString *)password WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock {
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/login/modifyPassword2", login.json.PREFIX];
    [ZoloHttpTool setJsonRequest];
    NSDictionary *dic = @{
                            @"phone" : phone,
                            @"code" : code,
                            @"password" : password,
                            @"language" : @(0)
                         };
    [ZoloHttpTool POST:url params:dic success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            completeBlock(YES);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(NO);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(NO);
    }];
}

/** 获取登录配置*/

- (void)getLoginConfigWithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock {
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/im/getConfigration", login.json.PREFIX];
    [ZoloHttpTool setFormRequest];
    [ZoloHttpTool GET:url params:@{} success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
            MHJsonModel *model = [MHJsonModel yy_modelWithJSON:dict[@"data"]];
            login.json = model;
            [[ZoloInfoManager sharedUserManager] setCurrentInfo:login];
            completeBlock(YES);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(NO);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(NO);
    }];
}

/** 获取弹框公告*/
- (void)getChatPushNotice:(NSInteger)launge WithCompleteBlock:(void(^)(ZoloNotice *data))completeBlock {
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/im/getNotice", login.json.PREFIX];
    [ZoloHttpTool setFormRequest];
    NSDictionary *dic = @{
                            @"language" : @(launge)
                         };
    [ZoloHttpTool GET:url params:dic success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            ZoloNotice *model = [ZoloNotice yy_modelWithJSON:dict[@"data"]];
            completeBlock(model);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(nil);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(nil);
    }];
}

/** 获取弹框列表*/
- (void)getChatPushNoticeList:(NSInteger)launge WithCompleteBlock:(void(^)(NSArray *data))completeBlock {
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/im/getNoticeList", login.json.PREFIX];
    [ZoloHttpTool setFormRequest];
    NSDictionary *dic = @{
                            @"language" : @(launge)
                         };
    [ZoloHttpTool GET:url params:dic success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            NSArray *data = [ZoloNotice jsonsToModelsWithJsons:dict[@"data"]];
            completeBlock(data);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(nil);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(nil);
    }];
}

/** 获取确认弹框*/
- (void)getChatConfirmNoticeWithIndex:(NSInteger)index WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock {
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/im/affirmNotice", login.json.PREFIX];
    [ZoloHttpTool setFormRequest];
    NSDictionary *dic = @{
                            @"id" : @(index)
                         };
    [ZoloHttpTool GET:url params:dic success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            completeBlock(YES);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(NO);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(NO);
    }];
}

/** 扫码登录小程序*/
- (void)scanLoginDappWithDic:(NSDictionary *)dic WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock {
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    NSString *url = dic[@"url"];
    [ZoloHttpTool setJsonRequest];
    [ZoloHttpTool POST:url params:dic success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            completeBlock(YES);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(NO);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(NO);
    }];
}

/** 是否需要弹窗 结果：window=0弹窗  window=1不弹窗 */
- (void)bindWindowWithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock {
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/im/bindWindow", login.json.PREFIX];
    [ZoloHttpTool setFormRequestNoToken];
    NSDictionary *dic = @{
                            @"osnId" : [DMCCIMService sharedDMCIMService].getUserID
                         };
    [ZoloHttpTool GET:url params:dic success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            NSNumber *window = dict[@"data"][@"window"];
            if (window.intValue == 0) {
                completeBlock(YES);
            } else {
                completeBlock(NO);
            }
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(NO);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(NO);
    }];
}

/** 绑定账号：type 1 手机号  2 邮箱  */
- (void)bindAccount:(NSString *)city account:(NSString *)account type:(NSInteger)type WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock {
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login == nil) {
        return;
    }
    NSString *url = [NSString stringWithFormat:@"%@/im/bindAccount", login.json.PREFIX];
    
    NSDictionary *dic = @{
                            @"account" : account,
                            @"osnId" : [DMCCIMService sharedDMCIMService].getUserID
                         };
    
    NSMutableDictionary *mdic = [[NSMutableDictionary alloc] initWithDictionary:dic];
    
    if (type == 1) {
        [mdic setValue:city forKey:@"city"];
    }
    
    [ZoloHttpTool setJsonRequestNoToken];
    [ZoloHttpTool POST:url params:mdic success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            completeBlock(YES);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(NO);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(NO);
    }];
}

/** 助记词登录  */
- (void)bip39LoginWithUserString:(NSString *)str withUrl:(NSString *)url WithCompleteBlock:(void(^)(NSDictionary *data))completeBlock {
    [ZoloHttpTool setJsonRequest];
    NSDictionary *dic = @{
                            @"user" : str
                         };
    [ZoloHttpTool POST:url params:dic success:^(NSURLSessionDataTask * _Nonnull task, id  _Nonnull responseObject) {
        NSData *data = (NSData*)responseObject;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
        if ([self invitedCode:dict[@"code"]]) {
            completeBlock(dict[@"data"]);
        } else {
            [MHAlert showMessage:dict[@"msg"]];
            completeBlock(nil);
        }
    } fail:^(NSURLSessionDataTask * _Nonnull task, NSError * _Nonnull error) {
        completeBlock(nil);
    }];
}

#pragma mark - 验证code
- (BOOL)invitedCode:(NSNumber *)code {
    switch (code.intValue) {
        case 200: // 成功
          {
            return YES;
          }
            break;
        case 0: // 成功
          {
              return YES;
          }
              break;
        default:
            break;
    }
    return NO;
}

@end
