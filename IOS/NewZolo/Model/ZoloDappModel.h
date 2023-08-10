//
//  ZoloDappModel.h
//  NewZolo
//
//  Created by JTalking on 2022/8/30.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface ZoloDappModel : NSObject

@property (nonatomic, copy) NSString *create_time;
@property (nonatomic, copy) NSString *dapp_url_back;
@property (nonatomic, copy) NSString *dapp_url_front;
@property (nonatomic, copy) NSString *ospnId;
@property (nonatomic, strong) NSNumber *id;

@end

NS_ASSUME_NONNULL_END
