//
//  ZoloTagModel.h
//  NewZolo
//
//  Created by JTalking on 2022/10/27.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface ZoloTagModel : NSObject

@property (nonatomic, assign) NSInteger id;
@property (nonatomic, copy) NSString *group_name;
@property (nonatomic, copy) NSString *osn_id;
@property (nonatomic, copy) NSString *user_id;
@property (nonatomic, assign) NSInteger type;
@property (nonatomic, assign) NSInteger parent_id;

@end

NS_ASSUME_NONNULL_END
