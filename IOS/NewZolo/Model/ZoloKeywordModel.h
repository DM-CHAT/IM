//
//  ZoloKeywordModel.h
//  NewZolo
//
//  Created by JTalking on 2022/10/29.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface ZoloKeywordModel : NSObject

@property (nonatomic, assign) NSInteger type;
@property (nonatomic, assign) NSInteger minute;
@property (nonatomic, strong) NSArray *keywordList;

@end

@interface ZoloKeywdModel : NSObject

@property (nonatomic, assign) NSInteger id;
@property (nonatomic, copy) NSString *content;

@end

NS_ASSUME_NONNULL_END
