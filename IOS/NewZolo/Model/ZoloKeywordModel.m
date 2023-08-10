//
//  ZoloKeywordModel.m
//  NewZolo
//
//  Created by JTalking on 2022/10/29.
//

#import "ZoloKeywordModel.h"

@implementation ZoloKeywordModel

+ (NSDictionary *)modelContainerPropertyGenericClass {
    return @{
             @"keywordList" : [ZoloKeywdModel class]
             };
}


@end

@implementation ZoloKeywdModel

@end
