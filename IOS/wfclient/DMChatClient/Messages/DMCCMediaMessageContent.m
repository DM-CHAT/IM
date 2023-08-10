//
//  DMCCMediaMessageContent.m
//  DMChatClient
//
//  Created by heavyrain on 2017/9/6.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCMediaMessageContent.h"
#import "DMCCUtilities.h"
#import "Common.h"


@implementation DMCCMediaMessageContent
- (NSString *)localPath {
    _localPath = [DMCCUtilities getSendBoxFilePath:_localPath];
    return _localPath;
}
@end
