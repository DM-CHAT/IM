#import "DMCCMessageContent.h"

#define CallTypeVideo     0
#define CallTypeAudio     1
#define CallTypeAction    2
#define CallModeSingle    0
#define CallModeMulti     1
#define CallActionInvite  0
#define CallActionAnswer  1
#define CallActionReject  2
#define CallActionFinish  3
#define CallActionStream  4
#define CallActionStreams 5
#define CallActionCancel  6

@interface DMCCCallMessageContent : DMCCMessageContent
@property (nonatomic, assign) int cid;
@property (nonatomic, assign) int type;
@property (nonatomic, assign) int mode;
@property (nonatomic, assign) int action;
@property (nonatomic, assign) long duration;
@property (nonatomic, strong) NSString* status;
@property (nonatomic, strong) NSString* url;
@property (nonatomic, strong) NSString* voiceHostUrl;
@property (nonatomic, strong) NSString* voiceBaseUrl;
@property (nonatomic, strong) NSString* user;
@property (nonatomic, strong) NSMutableArray* urls;
@property (nonatomic, strong) NSMutableArray* users;
@end
