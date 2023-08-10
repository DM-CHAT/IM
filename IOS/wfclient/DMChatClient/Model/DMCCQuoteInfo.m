//
//  DMCCQuoteInfo.m
//  DMChatClient
//
//  Created by dali on 2020/10/4.
//  Copyright © 2020 WildFireChat. All rights reserved.
//

#import "DMCCQuoteInfo.h"
#import "DMCCUserInfo.h"
#import "DMCCMessage.h"
#import "DMCCIMService.h"


@implementation DMCCQuoteInfo
- (instancetype)initWithMessageUid:(long long)messageUid {
    self = [super init];
    if (self) {
        DMCCMessage *msg = [[DMCCIMService sharedDMCIMService] getMessageByUid:messageUid];
        if (msg) {
            self.messageUid = messageUid;
            self.userId = msg.fromUser;
            DMCCUserInfo *user = [[DMCCIMService sharedDMCIMService] getUserInfo:msg.fromUser refresh:NO];
            self.userDisplayName = user.displayName;
            self.messageDigest = [msg.content digest:msg];
            NSUInteger maxBytes = 120;
            if (self.messageDigest.length > 48) {
                __block NSUInteger seenBytes = 0;
                __block NSUInteger truncLength = 0;
                NSRange fullLength = (NSRange){0, [self.messageDigest length]};
                [self.messageDigest enumerateSubstringsInRange:fullLength
                                      options:NSStringEnumerationByComposedCharacterSequences
                                   usingBlock:
                    ^(NSString *substring, NSRange substringRange,
                      NSRange _, BOOL *stop)
                    {
                        seenBytes += [substring lengthOfBytesUsingEncoding:NSUTF8StringEncoding];
                        if( seenBytes > maxBytes ){
                            *stop = YES;
                            return;
                        }
                        else {
                            truncLength += substringRange.length;
                        }
                }];

                self.messageDigest = [self.messageDigest substringToIndex:truncLength];
            }
        } else {
            return nil;
        }
    }
    return self;
}

- (NSDictionary *)encode {
    NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
    [dict setObject:@(self.messageUid) forKey:@"u"];
    if (self.userId.length) {
        [dict setObject:self.userId forKey:@"i"];
    }
    if (self.userDisplayName.length) {
        [dict setObject:self.userDisplayName forKey:@"n"];
    }
    if (self.messageDigest.length) {
        [dict setObject:self.messageDigest forKey:@"d"];
    }
    if (self.messageHasho.length) {
        [dict setObject:self.messageHasho forKey:@"hash"];
    }
    return [dict copy];
}

- (void)decode:(NSDictionary *)dictData {
    if(dictData[@"messageUid"]) {
        self.messageUid = [dictData[@"messageUid"] longLongValue];
        self.userId = dictData[@"userId"];
        self.userDisplayName = dictData[@"userDisplayName"];
        self.messageDigest = dictData[@"messageDigest"];
        self.messageHasho = dictData[@"messageHash"];
    } else {
        self.messageUid = [dictData[@"u"] longLongValue];
        self.userId = dictData[@"i"];
        self.userDisplayName = dictData[@"n"];
        self.messageDigest = dictData[@"d"];
        self.messageHasho = dictData[@"hash"];
    }
}
@end
