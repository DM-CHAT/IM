//
//  DMCCCollectInfo.h
//  DMChatClient
//
//  Created by MHHY on 2023/5/17.
//  Copyright © 2023 WildFireChat. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface DMCCCollectInfo : NSObject

@property (nonatomic, copy) NSString *osnID;
@property (nonatomic, copy) NSString *name;
@property (nonatomic, assign) NSInteger type;
@property (nonatomic, copy) NSString *content;

@end

NS_ASSUME_NONNULL_END
