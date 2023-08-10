//
//  ZoloAPIManager.h
//  NewZolo
//
//  Created by JTalking on 2022/8/30.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN
@class ZoloKeywordModel, ZoloNotice;

@interface ZoloAPIManager : NSObject

+ (instancetype)instanceManager;

/** 获取小程序列表 */
- (void)getDappListWithGroupId:(NSString *)groupId withCompleteBlock:(void(^)(NSArray *data))completeBlock;

/** 查询绑定的小程序*/
- (void)checkBindDappWithGroupId:(NSString *)groupId ownerId:(NSString *)ownerId withCompleteBlock:(void(^)(NSDictionary *data))completeBlock;

/** 新增绑定的小程序*/
- (void)addBindDappWithDic:(NSDictionary *)dic withCompleteBlock:(void(^)(BOOL isSuccess))completeBlock;

/** 获取交易参数*/
- (void)checkPayResultWithPayId:(NSString *)payId withCompleteBlock:(void(^)(NSDictionary *data))completeBlock;

/** 获取客服列表 */
- (void)getCustomListWithCompleteBlock:(void(^)(NSDictionary *data))completeBlock;

/** 用户注销 */
- (void)deleteUserWithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock;

/** 设置用户昵称 */
- (void)settingUserName:(NSString *)name WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock;

/** 获取红包规则 */
- (void)getRedRoleWithLanguage:(NSString *)Language WithCompleteBlock:(void(^)(NSDictionary *data))completeBlock;

/** 推送 */
- (void)pushWithName:(NSString *)name osnID:(NSString *)osnID language:(NSString *)language WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock;

/** 获取服务器列表*/
- (void)getServiceListWithCompleteBlock:(void(^)(NSArray *data))completeBlock;

/** 获取osnID*/
- (void)getOsnId:(NSString *)string WithCompleteBlock:(void(^)(NSString *data))completeBlock;

/** 新增tag名称*/
- (void)addTagNameWithName:(NSString *)tagName type:(NSInteger)type osnId:(NSString *)osnId tagId:(NSInteger)tagId WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock;

/** 查询标签列表*/
- (void)addTagNameListWithCompleteBlock:(void(^)(NSArray *data))completeBlock;

/** 删除标签*/
- (void)delTagId:(NSInteger)tagId WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock;

/** 查询标签所有人员列表*/
- (void)getTagNameUserAllListWithCompleteBlock:(void(^)(NSArray *data))completeBlock;

/** 删除标签用户或群组*/
- (void)delTagUserList:(NSArray *)ids WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock;

/** 移动标签用户或群组*/
- (void)moveTagUserList:(NSArray *)ugs tagId:(NSInteger)tagId group_name:(NSString *)group_name WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock;

/**
    设置关键字
    license  调 GetOwnerSign 获取
 */
- (void)setGroupKeywordWithGroupId:(NSString *)groupId content:(NSString *)content license:(NSString *)license WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock;

/** 查询关键字列表*/
- (void)getKeywordListAndGroupMuteTimeWithGroupId:(NSString *)groupId WithCompleteBlock:(void(^)(ZoloKeywordModel *data))completeBlock;

/** 删除关键字*/
- (void)deleteKeywordWithGroupId:(NSString *)groupId keywordId:(NSInteger)keywordId WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock;

/** 是否显示扫雷红包*/
- (void)bombHiddenWithGroupId:(NSString *)groupId WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock;

/** 服务器上传img*/
- (void)uploadImgWithGroupId:(NSString *)groupId image:(NSString *)img WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock;

/** 服务器上传i用户mg*/
- (void)uploadUserImgWithImage:(NSString *)img WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock;
    
/** share 内容 */
- (void)getShareContentWithCompleteBlock:(void(^)(NSDictionary *data))completeBlock;

/** 判断是否更新 */
- (void)getCheckUpdateWithCompleteBlock:(void(^)(NSDictionary *data))completeBlock;

/** 创建群聊*/
- (void)createGroupWithMemberList:(NSArray *)member portrait:(NSString *)portrait type:(NSInteger)type name:(NSString *)name withCompleteBlock:(void(^)(NSString *data))completeBlock;

/** 保存dapp*/
- (void)saveDappWithDappJson:(NSString *)str withCompleteBlock:(void(^)(BOOL isSuccess))completeBlock;

/** 获取dapp*/
- (void)getUserDappWithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock;

/** 删除dapp*/
- (void)deleteDappWithId:(NSInteger)dappId WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock;

/** 获取当前用户账号*/
- (void)getUserInfoWithCompleteBlock:(void(^)(NSString *data))completeBlock;

/** 获取code验证码*/
- (void)updataPwdCodeWithPhone:(NSString *)phone WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock;

/** 根据code修改密码*/
- (void)updataPasswordWithPhone:(NSString *)phone code:(NSString *)code password:(NSString *)password WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock;

/** 获取登录配置*/
- (void)getLoginConfigWithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock;

/** 获取弹框公告*/
- (void)getChatPushNotice:(NSInteger)launge WithCompleteBlock:(void(^)(ZoloNotice *data))completeBlock;

/** 获取弹框列表*/
- (void)getChatPushNoticeList:(NSInteger)launge WithCompleteBlock:(void(^)(NSArray *data))completeBlock;

/** 获取确认弹框*/
- (void)getChatConfirmNoticeWithIndex:(NSInteger)index WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock;

/** 扫码登录小程序*/
- (void)scanLoginDappWithDic:(NSDictionary *)dic WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock;

/** 绑定账号：type 1 手机号  2 邮箱  */
- (void)bindAccount:(NSString *)city account:(NSString *)account type:(NSInteger)type WithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock;

/** 是否需要弹窗 结果：window=0弹窗  window=1不弹窗 */
- (void)bindWindowWithCompleteBlock:(void(^)(BOOL isSuccess))completeBlock;

/** 助记词登录  */
- (void)bip39LoginWithUserString:(NSString *)str withUrl:(NSString *)url WithCompleteBlock:(void(^)(NSDictionary *data))completeBlock;

@end

NS_ASSUME_NONNULL_END
