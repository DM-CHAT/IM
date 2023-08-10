#import "MHParentViewController.h"

@interface ZoloSingleCallVC : MHParentViewController
@property (nonatomic, strong) DMCCConversation *conversation;
@property (nonatomic, strong) DMCCCallMessageContent *receiveCall;
@property (nonatomic, strong) NSString *status;
@property (nonatomic, assign) bool isAudioOnly;
@end
