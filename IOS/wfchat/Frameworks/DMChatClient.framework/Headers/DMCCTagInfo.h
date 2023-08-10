//
//  DMCCTagInfo.h
//  DMChatClient
//
//  Created by JTalking on 2022/10/27.
//  Copyright © 2022 WildFireChat. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface DMCCTagInfo : NSObject

@property (nonatomic, assign) NSInteger id;
@property (nonatomic, copy) NSString *group_name;
@property (nonatomic, assign) NSInteger count;

@end

NS_ASSUME_NONNULL_END
