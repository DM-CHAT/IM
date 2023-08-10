//
//  WFCCFileRecord.h
//  WFChatClient
//
//  Created by dali on 2020/8/2.
//  Copyright © 2020 WildFireChat. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "DMCCConversation.h"

NS_ASSUME_NONNULL_BEGIN

@interface WFCCFileRecord : NSObject
@property (nonatomic, strong)DMCCConversation *conversation;
@property (nonatomic, assign)long long messageUid;
@property (nonatomic, strong)NSString *userId;
@property (nonatomic, strong)NSString *name;
@property (nonatomic, strong)NSString *url;
@property (nonatomic, assign)int size;
@property (nonatomic, assign)int downloadCount;
@property (nonatomic, assign)long long timestamp;
@end

NS_ASSUME_NONNULL_END
