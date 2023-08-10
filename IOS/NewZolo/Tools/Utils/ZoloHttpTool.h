//
//  ZoloHttpTool.h
//  NewZolo
//
//  Created by JTalking on 2022/8/30.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/** 宏定义请求成功的block */
typedef void (^QLResponseSuccess)(NSURLSessionDataTask * task,id responseObject);

/** 宏定义请求失败的block */
typedef void (^QLResponseFail)(NSURLSessionDataTask * task, NSError * error);

@interface ZoloHttpTool : NSObject

+ (void)setJsonRequest;
+ (void)setFormRequest;
+ (void)cancelRequest;
+ (void)setFormRequestNoToken;
+ (void)setJsonRequestNoToken;

/** 普通get方法请求网络数据 */
+ (void)GET:(NSString *)url params:(NSDictionary *)params success:(QLResponseSuccess)success fail:(QLResponseFail)fail;

/** 普通post方法请求网络数据 */
+ (void)POST:(NSString *)url params:(NSDictionary *)params success:(QLResponseSuccess)success fail:(QLResponseFail)fail;

/** 普通put方法请求网络数据 */
+ (void)PUT:(NSString *)url params:(NSDictionary *)params success:(QLResponseSuccess)success fail:(QLResponseFail)fail;

/** 普通delete方法请求网络数据 */
+ (void)DELETE:(NSString *)url params:(NSDictionary *)params success:(QLResponseSuccess)success fail:(QLResponseFail)fail;

@end

NS_ASSUME_NONNULL_END
