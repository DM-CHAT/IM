
#import "DMCCCallMessageContent.h"
#import "DMCCIMService.h"
#import "Common.h"
#import <config/config.h>

@implementation DMCCCallMessageContent
- (DMCCMessagePayload *)encode {
    return nil;
}

- (void)decode:(DMCCMessagePayload *)payload {
}

+ (int)getContentType {
    return MESSAGE_CONTENT_TYPE_CALL;
}

+ (int)getContentFlags {
    return DMCCPersistFlag_PERSIST_AND_COUNT;
}

+ (void)load {
    [[DMCCIMService sharedDMCIMService] registerMessageContent:self];
}

- (NSString *)digest:(DMCCMessage *)message {
    DMCCCallMessageContent *soundContent = (DMCCCallMessageContent *)message.content;
    NSString *str = @"";
    if (soundContent.type == 1) {
        str = LocalizedString(@"audioCall");
    } else {
        str = LocalizedString(@"videoCall");
    }
    return [NSString stringWithFormat:@"[%@]", str];
}
@end
