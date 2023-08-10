//
//  DMCCMessage.m
//  DMChatClient
//
//  Created by heavyrain on 2017/8/16.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCMessage.h"
#import "Common.h"


@implementation DMCCMessage
- (NSString *)digest {
    return [self.content digest:self];
}
@end
