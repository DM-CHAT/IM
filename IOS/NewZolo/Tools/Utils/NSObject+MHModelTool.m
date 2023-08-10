//
//  NSObject+MHModelTool.m
//  JTalking
//
//  Created by JTalking on 2022/6/24.
//

#import "NSObject+MHModelTool.h"

@implementation NSObject (MHModelTool)

+ (NSArray *)jsonsToModelsWithJsons:(NSArray *)jsons {
    NSMutableArray *models = [NSMutableArray array];
    for (NSDictionary *json in jsons) {
        [models addObject:[[self class] yy_modelWithJSON:json]];
    }
    return models;
}

@end
