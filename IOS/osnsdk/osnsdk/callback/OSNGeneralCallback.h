typedef void(^onResult)(bool isSuccess, NSMutableDictionary *json, NSString *error);
typedef void(^onResultT)(bool isSuccess, id t, NSString *error);
typedef void(^onProgress)(long progress, long total);
