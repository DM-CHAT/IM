//
//  ZoloServiceModel.h
//  NewZolo
//
//  Created by JTalking on 2022/10/7.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface ZoloServiceModel : NSObject

@property (nonatomic, copy) NSString *appIntroduction;
@property (nonatomic, copy) NSString *appRemark;
@property (nonatomic, copy) NSString *createTime;
@property (nonatomic, copy) NSString *iconUrl;
@property (nonatomic, copy) NSString *serviceName;
@property (nonatomic, copy) NSString *serviceRemark;
@property (nonatomic, copy) NSString *url;
@property (nonatomic, assign) NSInteger id;
@property (nonatomic, assign) NSInteger status;

@end



NS_ASSUME_NONNULL_END
