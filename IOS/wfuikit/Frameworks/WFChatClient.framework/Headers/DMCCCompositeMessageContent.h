//
//  DMCCCompositeMessageContent.h
//  WFChatClient
//
//  Created by Tom Lee on 2020/10/4.
//  Copyright © 2020 WildFireChat. All rights reserved.
//

#import "DMCCMessageContent.h"
@class DMCCMessage;

NS_ASSUME_NONNULL_BEGIN

@interface DMCCCompositeMessageContent : DMCCMessageContent
@property (nonatomic, strong)NSString *title;
@property (nonatomic, strong)NSArray<DMCCMessage *> *messages;
@end

NS_ASSUME_NONNULL_END
