#import <WebRTC/WebRTC.h>

@interface CallViewModel : NSObject
- (void)initModel;
- (void)launchCall:(RTCCameraPreviewView*) view audioOnly:(bool) isAudioOnly;
- (bool)pushCall:(NSString*) target replaceIP:(NSString*) replaceIP;
- (bool)pullCall:(RTCMTLVideoView*) view url:(NSString*) url;
- (NSString*)getUrl:(NSString*) target;
- (void)clear;
- (void)setSpeaker:(bool) isOn;
- (void)useSpeaker:(bool) isUse;
- (void)setAudio:(bool) enable;
- (void)setVedio:(bool) enable;
@end
