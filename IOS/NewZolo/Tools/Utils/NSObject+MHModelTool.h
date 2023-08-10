//
//  NSObject+MHModelTool.h
//  JTalking
//
//  Created by JTalking on 2022/6/24.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface NSObject (MHModelTool)

+ (NSArray *)jsonsToModelsWithJsons:(NSArray *)jsons;

@end

NS_ASSUME_NONNULL_END
