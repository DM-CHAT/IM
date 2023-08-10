//
//  MHDateTools.m
//  JTalking
//
//  Created by JTalking on 2022/6/24.
//

#import "MHDateTools.h"

@implementation MHDateTools

// 时间戳转字符串
+ (NSString *)timeStampToString:(NSInteger)timestamp {
    NSDate *confromTimesp = [NSDate dateWithTimeIntervalSince1970:timestamp / 1000];
    NSDateFormatter *dateFormat=[[NSDateFormatter alloc]init];
    [dateFormat setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
    NSString* string=[dateFormat stringFromDate:confromTimesp];
    return string;
}

// 时间戳转字符串 - 年月日
+ (NSString *)timeStampToDateString:(NSInteger)timestamp {
    NSDate *confromTimesp = [NSDate dateWithTimeIntervalSince1970:timestamp / 1000];
    NSDateFormatter *dateFormat=[[NSDateFormatter alloc]init];
    [dateFormat setDateFormat:@"yyyy-MM-dd"];
    NSString* string=[dateFormat stringFromDate:confromTimesp];
    return string;
}

// 时间戳转字符串 - 时分秒
+ (NSString *)timeStampToTimeString:(NSInteger)timestamp {
    NSDate *confromTimesp = [NSDate dateWithTimeIntervalSince1970:timestamp / 1000];
    NSDateFormatter *dateFormat=[[NSDateFormatter alloc]init];
    [dateFormat setDateFormat:@"HH:mm:ss"];
    NSString* string=[dateFormat stringFromDate:confromTimesp];
    return string;
}

//字符串转时间戳
+ (long)timeStampWithDate:(NSString *)str {
    NSDateFormatter *formatter = [[NSDateFormatter alloc]init];
    [formatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
    NSDate *resDate = [formatter dateFromString:str];
    return [resDate timeIntervalSince1970] * 1000;
}

//获取当前系统时间
+ (NSString*)getCurrentTimes {
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:@"YYYY-MM-dd HH:mm:ss"];
    NSDate *datenow = [NSDate date];
    NSString *currentTimeString = [formatter stringFromDate:datenow];
    return currentTimeString;
}

//获取当前系统时间
+ (NSString*)getCurrentDay {
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:@"YYYY-MM-dd"];
    NSDate *datenow = [NSDate date];
    NSString *currentTimeString = [formatter stringFromDate:datenow];
    return currentTimeString;
}

+ (NSString *)formatTimeLabel:(int64_t)timestamp {
    if (timestamp == 0) {
        return nil;
    }
    
    NSDate *date = [NSDate dateWithTimeIntervalSince1970:timestamp/1000];
    NSDate *current = [[NSDate alloc] init];
    NSCalendar *calendar = [NSCalendar currentCalendar];
    
    NSInteger years = [calendar component:NSCalendarUnitYear fromDate:date];
    NSInteger curYears = [calendar component:NSCalendarUnitYear fromDate:current];

    if ([calendar isDateInToday:date]) {
        NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
        [formatter setDateFormat:@"HH:mm"];
        return [formatter stringFromDate:date];
    } else if([calendar isDateInYesterday:date]) {
        return LocalizedString(@"DataToolsY");
    } else {
        if (years == curYears) {
            NSInteger weeks = [calendar component:NSCalendarUnitWeekOfYear fromDate:date];
            NSInteger curWeeks = [calendar component:NSCalendarUnitWeekOfYear fromDate:current];
            
            NSInteger weekDays = [calendar component:NSCalendarUnitWeekday fromDate:date];
            if (weeks == curWeeks) {
                switch (weekDays) {
                    case 1:
                        return LocalizedString(@"DataToolsSev");
                        break;
                    case 2:
                        return LocalizedString(@"DataToolsOne");
                        break;
                    case 3:
                        return LocalizedString(@"DataToolsTwo");
                        break;
                    case 4:
                        return LocalizedString(@"DataToolsThr");
                        break;
                    case 5:
                        return LocalizedString(@"DataToolsFor");
                        break;
                    case 6:
                        return LocalizedString(@"DataToolsFive");
                        break;
                    case 7:
                        return LocalizedString(@"DataToolsSix");
                        break;
                        
                    default:
                        break;
                }
                return [NSString stringWithFormat:@"%ld", (long)weekDays];
            } else {
                NSInteger month = [calendar component:NSCalendarUnitMonth fromDate:date];
                NSInteger day = [calendar component:NSCalendarUnitDay fromDate:date];
                return [NSString stringWithFormat:@"%d/%d", (int)month, (int)day];
            }
        } else {
            NSInteger month = [calendar component:NSCalendarUnitMonth fromDate:date];
            NSInteger day = [calendar component:NSCalendarUnitDay fromDate:date];
            return [NSString stringWithFormat:@"%d/%d/%d",(int)years,(int)month, (int)day];
        }
        
    }
}

+ (NSString *)formatTimeDetailLabel:(int64_t)timestamp {
    if (timestamp == 0) {
        return nil;
    }
    
    NSDate *date = [NSDate dateWithTimeIntervalSince1970:timestamp/1000];
    
    NSDate *current = [[NSDate alloc] init];
    NSCalendar *calendar = [NSCalendar currentCalendar];
    
    NSInteger months = [calendar component:NSCalendarUnitMonth fromDate:date];
    NSInteger curMonths = [calendar component:NSCalendarUnitMonth fromDate:current];
    NSInteger years = [calendar component:NSCalendarUnitYear fromDate:date];
    NSInteger curYears = [calendar component:NSCalendarUnitYear fromDate:current];
    
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:@"HH:mm"];
    NSString *hourTimeStr =  [formatter stringFromDate:date];
    
    NSInteger weeks = [calendar component:NSCalendarUnitWeekOfYear fromDate:date];
    NSInteger curWeeks = [calendar component:NSCalendarUnitWeekOfYear fromDate:current];
    
    
    NSInteger weekDays = [calendar component:NSCalendarUnitWeekday fromDate:date];
    if ([calendar isDateInToday:date]) {
        return hourTimeStr;
    } else if([calendar isDateInYesterday:date]) {
        return [NSString stringWithFormat:@"%@ %@", LocalizedString(@"DataToolsY"),hourTimeStr];
    } else if (years != curYears) {
        [formatter setDateFormat:@"yyyy'/'MM'/'dd' 'HH':'mm"];
        return [formatter stringFromDate:date];
    } else if(months != curMonths) {
        if(weeks == curWeeks) {
            return [NSString stringWithFormat:@"%@ %@", [MHDateTools formatWeek:weekDays], hourTimeStr];
        }
        
        [formatter setDateFormat:@"MM'/'dd' 'HH':'mm"];
        return [formatter stringFromDate:date];
    } else {
        if(weeks == curWeeks) {
            return [NSString stringWithFormat:@"%@ %@", [MHDateTools formatWeek:weekDays], hourTimeStr];
        }
        [formatter setDateFormat:@"MM'/'dd' 'HH':'mm"];
        return [formatter stringFromDate:date];
    }
}
+ (NSString *)formatWeek:(NSUInteger)weekDays {
    weekDays = weekDays % 7;
    switch (weekDays) {
        case 2:
            return LocalizedString(@"DataToolsOne");
        case 3:
            return LocalizedString(@"DataToolsTwo");
        case 4:
            return LocalizedString(@"DataToolsThr");
        case 5:
            return LocalizedString(@"DataToolsFor");
        case 6:
            return LocalizedString(@"DataToolsFive");
        case 0:
            return LocalizedString(@"DataToolsSix");
        case 1:
            return LocalizedString(@"DataToolsSev");
            
        default:
            break;
    }
    return nil;
}
@end
