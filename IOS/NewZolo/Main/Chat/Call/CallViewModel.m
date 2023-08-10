#import "CallViewModel.h"

@interface CallStream : NSObject
@property RTCMediaStream *stream;
@property RTCPeerConnection *peer;
@property RTCMTLVideoView *view;
@end

@implementation CallStream
@end

@interface CallViewModel()<RTCPeerConnectionDelegate>
@property RTCPeerConnectionFactory *factory;
@property RTCPeerConnection *pushConnection;
@property RTCCameraVideoCapturer *videoCapture;
@property RTCMediaStream *mediaStream;
@property NSMutableDictionary *peers;
@property bool isUseSpeaker;
@property bool isAudioOnly;
@end

@implementation CallViewModel

NSString* hostUrl = @"http://0.0.0.0:1985";
NSString* baseUrl = @"webrtc://0.0.0.0:1985/live/android/";

-(void) initModel{
    [RTCPeerConnectionFactory initialize];
    if(!_factory){
        RTCDefaultVideoDecoderFactory *decoder = [[RTCDefaultVideoDecoderFactory alloc]init];
        RTCDefaultVideoEncoderFactory *encoder = [[RTCDefaultVideoEncoderFactory alloc]init];
        _factory = [[RTCPeerConnectionFactory alloc]initWithEncoderFactory:encoder decoderFactory:decoder];
    }
    _mediaStream = [_factory mediaStreamWithStreamId:@"stream"];
    _peers = [NSMutableDictionary new];
    [self initBase];
}

-(void) initBase{
    
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login.json.voiceBaseUrl != nil)
        baseUrl = login.json.voiceBaseUrl;
    if (login.json.voiceHostUrl != nil)
        hostUrl = login.json.voiceHostUrl;
    
}

-(void) replaceBase:(NSString *) ip{
    
    [self initBase];
    
    NSString * ipBase = [self getIP:baseUrl];
    baseUrl =[baseUrl stringByReplacingOccurrencesOfString:ipBase withString:ip];
    hostUrl =[hostUrl stringByReplacingOccurrencesOfString:ipBase withString:ip];
}

-(NSString *) getIP:(NSString *) str {
    
    NSArray *array = [str componentsSeparatedByString:@":"];
    if (array.count > 1) {
        return array[1];
    }
    return nil;
}

- (AVCaptureDeviceFormat *)selectFormatForDevice:(AVCaptureDevice *)device width:(int)width height:(int)height {
    NSArray<AVCaptureDeviceFormat *> *formats = [RTCCameraVideoCapturer supportedFormatsForDevice:device];
    AVCaptureDeviceFormat *selectedFormat = nil;
    int currentDiff = INT_MAX;
    for (AVCaptureDeviceFormat *format in formats) {
        CMVideoDimensions dimension = CMVideoFormatDescriptionGetDimensions(format.formatDescription);
        int diff = abs(width - dimension.width) + abs(height - dimension.height);
        if (diff < currentDiff) {
            selectedFormat = format;
            currentDiff = diff;
        }
    }
    return selectedFormat;
}
- (void)launchCall:(RTCCameraPreviewView*) view audioOnly:(bool) isAudioOnly{
    _isAudioOnly = isAudioOnly;
    RTCAudioTrack * audioTrack = [_factory audioTrackWithTrackId:@"audio0"];
    [_mediaStream addAudioTrack:audioTrack];
    if(!isAudioOnly){
        NSArray<AVCaptureDevice *> *captureDevices = [RTCCameraVideoCapturer captureDevices];
        if(captureDevices == nil || captureDevices.count == 0)
            return;
        AVCaptureDevice * device = captureDevices[1];
        RTCVideoSource *videoSource = [_factory videoSource];
        _videoCapture = [[RTCCameraVideoCapturer alloc] initWithDelegate:videoSource];
        RTCVideoTrack *videoTrack = [_factory videoTrackWithSource:videoSource trackId:@"video0"];
        [_videoCapture startCaptureWithDevice:device format:nil fps:5.0];
        view.captureSession = _videoCapture.captureSession;
        [_mediaStream addVideoTrack:videoTrack];
    }
}
-(bool) pushCall:(NSString*) target
       replaceIP:(NSString* ) replaceIP{
    
    if (replaceIP != nil) {
        [self replaceBase:replaceIP];
    }
    
    RTCConfiguration* rtcConfig = [[RTCConfiguration alloc]init];
    rtcConfig.sdpSemantics = RTCSdpSemanticsUnifiedPlan;
    RTCMediaConstraints* constraints = [[RTCMediaConstraints alloc] initWithMandatoryConstraints:nil optionalConstraints: @{@"DtlsSrtpKeyAgreement":kRTCMediaConstraintsValueTrue}];
    _pushConnection = [_factory peerConnectionWithConfiguration:rtcConfig
                                                   constraints:constraints
                                                      delegate:self];
    if(_pushConnection == nil){
        NSLog(@"createPeerConnection push call failed");
        return false;
    }
    RTCRtpTransceiverInit *transceiverInit = [[RTCRtpTransceiverInit alloc] init];
    transceiverInit.direction = RTCRtpTransceiverDirectionSendOnly;
    if(!_isAudioOnly)
        [_pushConnection addTransceiverWithTrack:_mediaStream.videoTracks[0] init:transceiverInit];
    [_pushConnection addTransceiverWithTrack:_mediaStream.audioTracks[0] init:transceiverInit];
    [_pushConnection setBweMinBitrateBps:nil currentBitrateBps:nil maxBitrateBps:@(100*1024*8)];
    [self createOffer:_pushConnection url:[self getUrl:target] flag:true constraints:constraints];
    return true;
}
-(bool) pullCall:(RTCMTLVideoView*) view url:(NSString*) url{
    if([_peers objectForKey:url]){
        NSLog(@"already pull url: %@", url);
        return true;
    }
    NSLog(@"receiveCall: %@", url);
    
    NSString * replaceIP = [self getIP:url];
    [self replaceBase:replaceIP];
    
    RTCConfiguration* rtcConfig = [[RTCConfiguration alloc]init];
    rtcConfig.sdpSemantics = RTCSdpSemanticsUnifiedPlan;
    RTCMediaConstraints* constraints = [[RTCMediaConstraints alloc] initWithMandatoryConstraints:nil optionalConstraints: nil];
    RTCPeerConnection *pullConnection = [_factory peerConnectionWithConfiguration:rtcConfig
                                                   constraints:constraints
                                                      delegate:self];
    if(pullConnection == nil){
        NSLog(@"createPeerConnection pull call failed");
        return false;
    }
    [self addPeer:url peer:pullConnection];
    [self addView:url view:view];
    RTCRtpTransceiverInit *transceiverInit = [[RTCRtpTransceiverInit alloc] init];
    transceiverInit.direction = RTCRtpTransceiverDirectionRecvOnly;
    if(!_isAudioOnly)
        [pullConnection addTransceiverOfType:RTCRtpMediaTypeVideo init:transceiverInit];
    [pullConnection addTransceiverOfType:RTCRtpMediaTypeAudio init:transceiverInit];
    [self createOffer:pullConnection url:url flag:false constraints:constraints];
    return true;
}
-(void) createOffer:(RTCPeerConnection*) peerConnection url:(NSString*)url flag:(bool)isPublish constraints:(RTCMediaConstraints*)constraints{
    [peerConnection offerForConstraints:constraints completionHandler:^(RTCSessionDescription * _Nullable sdp, NSError * _Nullable error) {
        if(error != nil){
            NSLog(@"offerForConstraints failed: %@", error)
            return;
        }
        if(sdp.type == RTCSdpTypeOffer){
            __weak RTCPeerConnection* weakPeer = peerConnection;
            [peerConnection setLocalDescription:sdp completionHandler:^(NSError * _Nullable error) {
                if(error != nil){
                    NSLog(@"setLocalDescription falied: %@", error);
                    return;
                }
                NSString * replaceIP = [self getIP:url];
                [self replaceBase:replaceIP];
                [self createSession:sdp url:url peer:weakPeer action:isPublish];
            }];
        }else{
            NSLog(@"unknown sdp type: %ld", (long)sdp.type);
        }
    }];
}
-(void) createSession:(RTCSessionDescription*) offerSdp url:(NSString*) url peer:(RTCPeerConnection*) peerConnection action:(bool) isPublish{
    
    NSLog(@"=====createSession===%@==", url);
    
    NSDictionary *json = @{@"sdp":offerSdp.sdp ,@"streamurl":url};
    NSData *data = [NSJSONSerialization dataWithJSONObject:json options:0 error:nil];
    NSString *request = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
    NSLog(@"session request: %@", request);
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    NSString *reqUrl = [NSString stringWithFormat:@"%@/rtc/v1/%@",hostUrl,(isPublish?@"publish/":@"play/")];
    //if (login.json.voiceHostUrl.length > 0) {
    //    reqUrl = [NSString stringWithFormat:@"%@/rtc/v1/%@",hostUrl,(isPublish?@"publish/":@"play/")];
    //}
    NSMutableDictionary *respone = [HttpUtils doPosts:reqUrl data:request];
    if(respone != nil){
        NSLog(@"session result: %@", [OsnUtils dic2Json:respone]);
        if(((NSNumber*)respone[@"code"]).intValue != 0)
            return;
        RTCSessionDescription *remoteSdp = [[RTCSessionDescription alloc]initWithType:RTCSdpTypeAnswer sdp:respone[@"sdp"]];
        [peerConnection setRemoteDescription:remoteSdp completionHandler:^(NSError * _Nullable error) {
            NSLog(@"setRemoteDescription: %@", error);
        }];
    }
}
-(void)exitCall:(NSString*)url{
    CallStream *call = [_peers objectForKey:url];
    if(call == nil)
        return;
    if(call.stream != nil && call.stream.videoTracks.count != 0){
        RTCVideoTrack *track = call.stream.videoTracks[0];
        [track removeRenderer:call.view];
    }
    if(call.peer != nil){
        [call.peer close];
    }
}
-(void)clear{
    if(_videoCapture != nil)
        [_videoCapture stopCapture];
    if(_pushConnection != nil)
        [_pushConnection close];
    if(_peers != nil){
        [_peers enumerateKeysAndObjectsUsingBlock:^(id  _Nonnull key, id  _Nonnull obj, BOOL * _Nonnull stop) {
            [self exitCall:key];
        }];
    }
}
-(NSString*) getUrl:(NSString*) target{
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    //if (login.json.voiceBaseUrl.length > 0) {
    //    return [NSString stringWithFormat:@"%@%@",login.json.voiceBaseUrl,target];
    //} else {
        return [NSString stringWithFormat:@"%@%@",baseUrl,target];
    //}
}
-(NSString*) convertAnswerSdp:(NSString*) offerSdp sdp:(NSString*) answerSdp {
    if(answerSdp == nil || answerSdp.length == 0)
        return answerSdp;
    int indexOfOfferVideo = (int)[offerSdp rangeOfString:@"m=video"].location;
    int indexOfOfferAudio = (int)[offerSdp rangeOfString:@"m=audio"].location;
    if (indexOfOfferVideo == -1 || indexOfOfferAudio == -1) {
        return answerSdp;
    }
    int indexOfAnswerVideo = (int)[answerSdp rangeOfString:@"m=video"].location;
    int indexOfAnswerAudio = (int)[answerSdp rangeOfString:@"m=audio"].location;
    if (indexOfAnswerVideo == -1 || indexOfAnswerAudio == -1) {
        return answerSdp;
    }
    bool isFirstOfferVideo = indexOfOfferVideo < indexOfOfferAudio;
    bool isFirstAnswerVideo = indexOfAnswerVideo < indexOfAnswerAudio;
    if(isFirstOfferVideo == isFirstAnswerVideo)
        return answerSdp;
    int length = MAX(indexOfAnswerAudio, indexOfAnswerVideo) - MIN(indexOfAnswerAudio, indexOfAnswerVideo);
    NSRange range = NSMakeRange(MIN(indexOfAnswerAudio, indexOfAnswerVideo), length);
    return [NSString stringWithFormat:@"%@%@%@",[answerSdp substringToIndex:MIN(indexOfAnswerAudio, indexOfAnswerVideo)],[answerSdp substringFromIndex:MAX(indexOfAnswerAudio, indexOfAnswerVideo)],[answerSdp substringWithRange:range]];
}
-(void) setAudio:(bool) enable{
    if(_mediaStream.audioTracks.count == 0)
        return;
    RTCAudioTrack *track = _mediaStream.audioTracks[0];
    [track setIsEnabled:enable];
}
-(void) setVedio:(bool) enable{
    if(_mediaStream.videoTracks.count == 0)
        return;
    RTCVideoTrack *track = _mediaStream.videoTracks[0];
    [track setIsEnabled:enable];
}
-(void) setSpeaker:(bool) isOn {
    [RTCAudioSession.sharedInstance lockForConfiguration];
    NSError *err = nil;
    [RTCAudioSession.sharedInstance setCategory:AVAudioSessionCategoryPlayAndRecord withOptions:AVAudioSessionCategoryOptionMixWithOthers error:&err];
    if(err != nil)
        NSLog(@"setCategory failed: %@", err);
    [RTCAudioSession.sharedInstance overrideOutputAudioPort:(isOn ? AVAudioSessionPortOverrideSpeaker:AVAudioSessionPortOverrideNone) error:&err];
    if(err != nil)
        NSLog(@"overrideOutputAudioPort failed: %@", err);
    [RTCAudioSession.sharedInstance setActive:true error:&err];
    if(err != nil)
        NSLog(@"setActive failed: %@", err);
    [RTCAudioSession.sharedInstance unlockForConfiguration];
}
- (void)useSpeaker:(bool) isUse{
    _isUseSpeaker = isUse;
}
-(void)addView:(NSString*)url view:(RTCMTLVideoView*)view{
    CallStream *call = [_peers objectForKey:url];
    if(call == nil){
        call = [CallStream new];
        [_peers setValue:call forKey:url];
    }
    call.view = view;
}
-(void)addPeer:(NSString*)url peer:(RTCPeerConnection*)peer{
    CallStream *call = [_peers objectForKey:url];
    if(call == nil){
        call = [CallStream new];
        [_peers setValue:call forKey:url];
    }
    call.peer = peer;
}
-(void)addStream:(NSString*)url stream:(RTCMediaStream*)stream{
    CallStream *call = [_peers objectForKey:url];
    if(call == nil){
        call = [CallStream new];
        [_peers setValue:call forKey:url];
    }
    call.stream = stream;
}
- (void)peerConnection:(RTC_OBJC_TYPE(RTCPeerConnection) *)peerConnection
          didAddStream:(RTC_OBJC_TYPE(RTCMediaStream) *)stream{
    NSLog(@"didAddStream");
    [_peers enumerateKeysAndObjectsUsingBlock:^(id  _Nonnull key, id  _Nonnull obj, BOOL * _Nonnull stop) {
        CallStream *call = obj;
        if(call.peer == peerConnection){
            if(stream.videoTracks.count != 0)
                [stream.videoTracks[0] addRenderer:call.view];
            call.stream = stream;
        }
    }];
    if(_isUseSpeaker)
        [self setSpeaker:true];
}
- (void)peerConnection:(RTC_OBJC_TYPE(RTCPeerConnection) *)peerConnection
       didRemoveStream:(RTC_OBJC_TYPE(RTCMediaStream) *)stream{
    NSLog(@"didRemoveStream");
}
- (void)peerConnection:(RTC_OBJC_TYPE(RTCPeerConnection) *)peerConnection didChangeSignalingState:(RTCSignalingState)stateChanged{
    NSLog(@"didChangeSignalingState: %ld", stateChanged);
}
- (void)peerConnectionShouldNegotiate:(RTC_OBJC_TYPE(RTCPeerConnection) *)peerConnection{
    NSLog(@"peerConnectionShouldNegotiate");
}
- (void)peerConnection:(RTC_OBJC_TYPE(RTCPeerConnection) *)peerConnection didChangeIceConnectionState:(RTCIceConnectionState)newState{
    NSLog(@"didChangeIceConnectionState: %ld", newState);
}
- (void)peerConnection:(RTC_OBJC_TYPE(RTCPeerConnection) *)peerConnection didChangeIceGatheringState:(RTCIceGatheringState)newState{
    NSLog(@"didChangeIceGatheringState: %ld", newState);
}
- (void)peerConnection:(RTC_OBJC_TYPE(RTCPeerConnection) *)peerConnection didGenerateIceCandidate:(RTC_OBJC_TYPE(RTCIceCandidate) *)candidate{
    NSLog(@"didGenerateIceCandidate");
}
- (void)peerConnection:(RTC_OBJC_TYPE(RTCPeerConnection) *)peerConnection didRemoveIceCandidates:(NSArray<RTC_OBJC_TYPE(RTCIceCandidate) *> *)candidates{
    NSLog(@"didRemoveIceCandidates");
}
- (void)peerConnection:(RTC_OBJC_TYPE(RTCPeerConnection) *)peerConnection didOpenDataChannel:(RTC_OBJC_TYPE(RTCDataChannel) *)dataChannel{
    NSLog(@"didOpenDataChannel");
}
@end
