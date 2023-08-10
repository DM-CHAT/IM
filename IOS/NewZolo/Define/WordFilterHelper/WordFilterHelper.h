//
//  WordFilterHelper.h
//  Flashfish
//
//  Created by zhangcong on 2017/8/18.
//  Copyright © 2017年 zhangcong. All rights reserved.
//

#import <Foundation/Foundation.h>
#define EXIST @"isExists"
@interface WordFilterHelper : NSObject

@property (nonatomic,strong) NSMutableDictionary *root;

@property (nonatomic,assign) BOOL isFilterClose;

- (instancetype)initWithFilter;

-(void)insertWords:(NSString *)words;

- (NSString *)filter:(NSString *)str ;

- (void)freeFilter;

@end
