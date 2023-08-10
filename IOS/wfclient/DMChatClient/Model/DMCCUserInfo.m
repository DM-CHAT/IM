//
//  DMCCUserInfo.m
//  DMChatClient
//
//  Created by heavyrain on 2017/9/29.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCUserInfo.h"
#import "DMCCUtilities.h"

@implementation DMCCUserInfo
- (void)cloneFrom:(DMCCUserInfo *)other {
    self.userId = other.userId;
    self.name = other.name;
    self.displayName = other.displayName;
    self.portrait = other.portrait;
    self.gender = other.gender;
    self.mobile = other.mobile;
    self.email = other.email;
    self.address = other.address;
    self.company = other.company;
    self.social = other.social;
    self.extra = other.extra;
    self.updateDt = other.updateDt;
    self.social = other.social;
    self.type = other.type;
    self.deleted = other.deleted;
    self.describes = other.describes;
}

+ (NSString *) getNft:(NSString *)str {
    if(str != nil && str.length > 0) {
        NSDictionary *json = [OsnUtils json2Dics:str];
        if (json == nil) {
            return nil;
        }
        NSString *nftStr = json[@"nft"];
        if (nftStr == nil) {
            return nil;
        }
        if (nftStr.length > 0) {
            if ([nftStr hasPrefix:@"http"]) {
                return nftStr;
            }
        }
    }
    return nil;
}

@end
