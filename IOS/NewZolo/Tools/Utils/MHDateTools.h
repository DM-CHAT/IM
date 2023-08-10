//
//  MHDateTools.h
//  JTalking
//
//  Created by JTalking on 2022/6/24.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface MHDateTools : NSObject

// 时间戳转字符串 年月日 时间
+ (NSString *)timeStampToString:(NSInteger)timestamp;

// 时间戳转字符串 - 年月日
+ (NSString *)timeStampToDateString:(NSInteger)timestamp;

// 时间戳转字符串 - 时分秒
+ (NSString *)timeStampToTimeString:(NSInteger)timestamp;

// 字符串转时间戳
+ (long)timeStampWithDate:(NSString *)str;

//获取当前系统时间
+ (NSString*)getCurrentTimes;
+ (NSString*)getCurrentDay;

+ (NSString *)formatTimeLabel:(int64_t)timestamp;

+ (NSString *)formatTimeDetailLabel:(int64_t)timestamp;

@end

NS_ASSUME_NONNULL_END
