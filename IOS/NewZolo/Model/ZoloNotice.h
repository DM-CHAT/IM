//
//  ZoloNotice.h
//  NewZolo
//
//  Created by MHHY on 2023/4/10.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface ZoloNotice : NSObject

@property (nonatomic, strong) NSNumber *create_time;
@property (nonatomic, assign) NSInteger id;
@property (nonatomic, assign) NSInteger language;
@property (nonatomic, copy) NSString *remark;
@property (nonatomic, copy) NSString *title;
@property (nonatomic, assign) NSInteger state;
@property (nonatomic, assign) NSInteger isRead;

@end

NS_ASSUME_NONNULL_END
