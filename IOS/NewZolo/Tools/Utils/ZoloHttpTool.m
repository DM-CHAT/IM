//
//  ZoloHttpTool.m
//  NewZolo
//
//  Created by JTalking on 2022/8/30.
//

#import "ZoloHttpTool.h"
#import "AFNetworking.h"

@implementation ZoloHttpTool

static AFHTTPSessionManager *manager;

+ (AFHTTPSessionManager*)defaultNetManager {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        manager = [[AFHTTPSessionManager alloc]init];
        AFHTTPResponseSerializer *serializer = [AFHTTPResponseSerializer serializer];
        serializer.acceptableContentTypes = [NSSet setWithObjects:@"application/json", @"text/json", @"text/javascript", @"application/json", @"text/json", @"text/javascript", @"text/plain", @"text/html", nil];
        manager.responseSerializer = serializer;
        manager.requestSerializer.timeoutInterval = 20;
    });
    return manager;
}

// 返回时取消网络请求
+ (void)cancelRequest {
  if ([manager.tasks count] > 0) {
    [manager.tasks makeObjectsPerformSelector:@selector(cancel)];
  }
}

+ (void)setJsonRequest {
    [ZoloHttpTool defaultNetManager].requestSerializer = [AFJSONRequestSerializer serializer];
    [[ZoloHttpTool defaultNetManager].requestSerializer setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    NSString *savedToken = [[NSUserDefaults standardUserDefaults] stringForKey:@"ospn_token"];
    NSString *current_account = [[NSUserDefaults standardUserDefaults]objectForKey:@"mne_account_current"];
    if (current_account.length == 0) {
        [[ZoloHttpTool defaultNetManager].requestSerializer setValue:savedToken forHTTPHeaderField:@"X-Token"];
    }
}

+ (void)setJsonRequestNoToken {
    [ZoloHttpTool defaultNetManager].requestSerializer = [AFJSONRequestSerializer serializer];
    [[ZoloHttpTool defaultNetManager].requestSerializer setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
}

+ (void)setFormRequest {
    [ZoloHttpTool defaultNetManager].requestSerializer = [AFHTTPRequestSerializer serializer];
    [[ZoloHttpTool defaultNetManager].requestSerializer setValue:@"application/x-www-form-urlencoded; charset=UTF-8" forHTTPHeaderField:@"Content-Type"];
    NSString *savedToken = [[NSUserDefaults standardUserDefaults] stringForKey:@"ospn_token"];
    NSString *current_account = [[NSUserDefaults standardUserDefaults]objectForKey:@"mne_account_current"];
    if (current_account.length == 0) {
        [[ZoloHttpTool defaultNetManager].requestSerializer setValue:savedToken forHTTPHeaderField:@"X-Token"];
    }
}

+ (void)setFormRequestNoToken {
    [ZoloHttpTool defaultNetManager].requestSerializer = [AFHTTPRequestSerializer serializer];
    [[ZoloHttpTool defaultNetManager].requestSerializer setValue:@"application/x-www-form-urlencoded; charset=UTF-8" forHTTPHeaderField:@"Content-Type"];
}

+ (void)GET:(NSString *)url params:(NSDictionary *)params success:(QLResponseSuccess)success fail:(QLResponseFail)fail {
    url = [url stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]];
    [[ZoloHttpTool defaultNetManager] GET:url parameters:params progress:^(NSProgress * _Nonnull downloadProgress) {
    } success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
        success(task,responseObject);
    } failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
        fail(task,error);
    }];
}

+ (void)POST:(NSString *)url params:(NSDictionary *)params success:(QLResponseSuccess)success fail:(QLResponseFail)fail {
    url = [url stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]];
    [[ZoloHttpTool defaultNetManager] POST:url parameters:params progress:^(NSProgress * _Nonnull uploadProgress) {
        
    } success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
        success(task,responseObject);
    } failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
        fail(task,error);
    }];
}

+ (void)PUT:(NSString *)url params:(NSDictionary *)params success:(QLResponseSuccess)success fail:(QLResponseFail)fail {
    url = [url stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]];
    [[ZoloHttpTool defaultNetManager] PUT:url parameters:params success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
        success(task,responseObject);
    } failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
        fail(task,error);
    }];
}

+ (void)DELETE:(NSString *)url params:(NSDictionary *)params success:(QLResponseSuccess)success fail:(QLResponseFail)fail {
    url = [url stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]];
    [[ZoloHttpTool defaultNetManager] DELETE:url parameters:params success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
        success(task,responseObject);
    } failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
        fail(task,error);
    }];
}
@end
