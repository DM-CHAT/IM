//
//  DMCCTextMessageContent.m
//  DMChatClient
//
//  Created by heavyrain on 2017/8/16.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import "DMCCLocationMessageContent.h"
#import "DMCCIMService.h"
#import "Common.h"
#import "DMCCUtilities.h"

@implementation DMCCLocationMessageContent
- (DMCCMessagePayload *)encode {
    DMCCMessagePayload *payload = [super encode];
    payload.contentType = [self.class getContentType];
    payload.searchableContent = self.title;
    payload.binaryContent = UIImageJPEGRepresentation(self.thumbnail,1);
    
    NSMutableDictionary *dataDict = [NSMutableDictionary dictionary];
    [dataDict setObject:@(self.coordinate.latitude) forKey:@"lat"];
    [dataDict setObject:@(self.coordinate.longitude) forKey:@"long"];
    payload.content = [[NSString alloc] initWithData:[NSJSONSerialization dataWithJSONObject:dataDict
                                                                                     options:kNilOptions
                                                                                       error:nil] encoding:NSUTF8StringEncoding];
    return payload;
}

- (void)decode:(DMCCMessagePayload *)payload {
    [super decode:payload];
    self.title = payload.searchableContent;
    self.thumbnail = [UIImage imageWithData:payload.binaryContent];
    
    NSError *__error = nil;
    NSDictionary *dictionary = [NSJSONSerialization JSONObjectWithData:[payload.content dataUsingEncoding:NSUTF8StringEncoding]
                                                               options:kNilOptions
                                                                 error:&__error];
    if (!__error) {
        double latitude = [dictionary[@"lat"] doubleValue];
        double longitude = [dictionary[@"long"] doubleValue];
        self.coordinate = CLLocationCoordinate2DMake(latitude, longitude);
    }

}

+ (int)getContentType {
    return MESSAGE_CONTENT_TYPE_LOCATION;
}

+ (int)getContentFlags {
    return DMCCPersistFlag_PERSIST_AND_COUNT;
}


+ (instancetype)contentWith:(CLLocationCoordinate2D) coordinate title:(NSString *)title thumbnail:(UIImage *)thumbnail {
    DMCCLocationMessageContent *content = [[DMCCLocationMessageContent alloc] init];
    content.coordinate = coordinate;
    content.title = title;
    content.thumbnail = [DMCCUtilities generateThumbnail:thumbnail withWidth:180 withHeight:120];;
    return content;
}

+ (void)load {
    [[DMCCIMService sharedDMCIMService] registerMessageContent:self];
}

- (NSString *)digest:(DMCCMessage *)message {
  return @"[位置]";
}
@end
