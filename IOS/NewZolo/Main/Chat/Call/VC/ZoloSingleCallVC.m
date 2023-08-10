#import "ZoloSingleCallVC.h"
#import "CallViewModel.h"
#import "AppDelegate.h"
#import "ZoloSoundManager.h"

@interface ZoloSingleCallVC () <FloatingWindowTouchDelegate>
@property (nonatomic,strong) UIImageView *imgUser;
@property (nonatomic,strong) UIImageView *imgAccept;
@property (nonatomic,strong) UIImageView *imgReject;
@property (nonatomic,strong) UIImageView *imgSeapker;
@property (nonatomic,strong) UIImageView *imgMute;
@property (nonatomic,strong) UIImageView *suofang;
@property (nonatomic, strong) UILabel *timeLab;
@property (nonatomic, strong) UILabel *nameLab;
@property (nonatomic ,strong)NSTimer *timer;
@property (nonatomic,strong) RTCCameraPreviewView *bigView;
@property (nonatomic,strong) RTCMTLVideoView *litView;
//@property (nonatomic,strong) RTCEAGLVideoView *litView;
@property (nonatomic,strong) CallViewModel *callViewModel;
@property (nonatomic,strong) DMCCUserInfo *userInfo;
@property (nonatomic,assign) bool isLaunch;
@property (nonatomic,assign) long duration;
@property (nonatomic,assign) BOOL isSpeaker;
@property (nonatomic,assign) BOOL isMute;
@property(nonatomic ,assign)NSInteger time;

@end



@implementation ZoloSingleCallVC


NSString* scHostUrl = @"http://0.0.0.0:1985";
NSString* scBaseUrl = @"webrtc://0.0.0.0:1985/live/android/";


- (void)viewDidLoad {
    [super viewDidLoad];
    [self initBase];
    [RTCPeerConnectionFactory initialize];
    [self initView];
    [self initCall];
    self.time = 0;
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onReceiveMessages:) name:kReceiveMessages object:nil];
    self.view.backgroundColor = MHColorFromHex(0x161617);
    [[ZoloSoundManager sharedManager] stopAlertSound];
    [[ZoloSoundManager sharedManager] prePlayVideoNotifySound];
    
}

-(void) initBase{
    
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    if (login.json.voiceBaseUrl != nil)
        scBaseUrl = login.json.voiceBaseUrl;
    if (login.json.voiceHostUrl != nil)
        scHostUrl = login.json.voiceHostUrl;
    
}

-(void) replaceBase:(NSString *) ip{
    
    [self initBase];
    
    NSString * ipBase = [self getIP:scBaseUrl];
    scBaseUrl =[scBaseUrl stringByReplacingOccurrencesOfString:ipBase withString:ip];
    scHostUrl =[scHostUrl stringByReplacingOccurrencesOfString:ipBase withString:ip];
}

-(NSString *) getIP:(NSString *) str {
    
    NSArray *array = [str componentsSeparatedByString:@":"];
    if (array.count > 1) {
        return array[1];
    }
    return nil;
}

-(void)initCall{
    _callViewModel = [CallViewModel new];
    [_callViewModel initModel];
    if([_status isEqualToString:@"launch"]){
        _isLaunch = true;
        [_callViewModel launchCall:_bigView audioOnly:_isAudioOnly];
        if(![_callViewModel pushCall:_userInfo.userId replaceIP:nil])
            NSLog(@"push call failed");
        [self sendCallMessage:CallActionInvite];
    }else{
        _isLaunch = false;
        [_callViewModel launchCall:_bigView audioOnly:_isAudioOnly];
    }
    _duration = [[NSDate date] timeIntervalSince1970] * 1000;
    _isSpeaker = true;
    [_callViewModel useSpeaker:_isSpeaker];
    _isMute = true;
    [_callViewModel setAudio:_isMute];
}
-(void)initView{
    NSString *userId = [[DMCCIMService sharedDMCIMService] getUserID];
    _userInfo = [[DMCCIMService sharedDMCIMService] getUserInfo:userId refresh:false];
    DMCCUserInfo *targetUser = [[DMCCIMService sharedDMCIMService] getUserInfo:_conversation.target refresh:false];
    int width = self.view.bounds.size.width;
    int height = self.view.bounds.size.height;
    _bigView = [[RTCCameraPreviewView alloc]initWithFrame:CGRectMake(0, 0, width, height)];
    _litView = [[RTCMTLVideoView alloc]initWithFrame:CGRectMake(width-20-120, 100, 120, 160)];
    
    _imgUser = [[UIImageView alloc] initWithFrame:CGRectMake((width-100)/2, 100, 100, 100)];
    [_imgUser sd_setImageWithURL:[NSURL URLWithString:targetUser.portrait] placeholderImage:SDImageDefault];
    _imgAccept = [[UIImageView alloc] initWithFrame:CGRectMake(50, height-200, 80, 80)];
    _imgAccept.image = [UIImage imageNamed:@"av_video_answer.png"];
    _imgAccept.userInteractionEnabled = true;
    [_imgAccept addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(acceptCall:)]];
    
    _nameLab = [[UILabel alloc] initWithFrame:CGRectMake(0, 220, kScreenwidth, 30)];
    _nameLab.textAlignment = NSTextAlignmentCenter;
    _nameLab.textColor = [UIColor whiteColor];
    _nameLab.text = targetUser.displayName;
    
    _timeLab = [[UILabel alloc] initWithFrame:CGRectMake(0, 260, kScreenwidth, 30)];
    _timeLab.textAlignment = NSTextAlignmentCenter;
    _timeLab.textColor = [UIColor whiteColor];
    _timeLab.hidden = YES;
    
    _imgReject = [[UIImageView alloc] initWithFrame:CGRectMake(width-50-80, height-200, 80, 80)];
    _imgReject.image = [UIImage imageNamed:@"av_hang_up.png"];
    _imgReject.userInteractionEnabled = true;
    [_imgReject addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(rejectCall:)]];
    
    _imgSeapker = [[UIImageView alloc] initWithFrame:CGRectMake((width - 50)/2 - 100, height-186, 50, 50)];
    _imgSeapker.image = [UIImage imageNamed:@"call_speaker_n.png"];
    _imgSeapker.userInteractionEnabled = true;
    _imgSeapker.hidden = YES;
    [_imgSeapker addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(seapkerClick)]];
    
    _imgMute = [[UIImageView alloc] initWithFrame:CGRectMake((width - 50)/2 + 100, height-186, 50, 50)];
    _imgMute.image = [UIImage imageNamed:@"call_mute_n.png"];
    _imgMute.userInteractionEnabled = true;
    _imgMute.hidden = YES;
    [_imgMute addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(muteClick)]];
    
    _suofang = [[UIImageView alloc] initWithFrame:CGRectMake(20, 60, 30, 30)];
    _suofang.image = [UIImage imageNamed:@"suof.png"];
    _suofang.userInteractionEnabled = true;
    [_suofang addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(shouqiClick)]];

    [self.view addSubview:_bigView];
    [self.view addSubview:_litView];
    [self.view addSubview:_imgUser];
    if([_status isEqualToString:@"launch"]){
        _imgReject.frame = CGRectMake((self.view.bounds.size.width-80)/2, self.view.bounds.size.height-200, 80, 80);
        _imgMute.hidden = NO;
        _imgSeapker.hidden = NO;
    }else{
        [self.view addSubview:_imgAccept];
    }
    [self.view addSubview:_imgReject];
    [self.view addSubview:_imgSeapker];
    [self.view addSubview:_imgMute];
//    [self.view addSubview:_suofang];
    [self.view addSubview:_nameLab];
    [self.view addSubview:_timeLab];
}

- (void)seapkerClick {
    _isSpeaker = !_isSpeaker;
    [_callViewModel setSpeaker:_isSpeaker];
    if (_isSpeaker) {
        _imgSeapker.image = [UIImage imageNamed:@"call_speaker_n.png"];
    } else {
        _imgSeapker.image = [UIImage imageNamed:@"call_speaker_s.png"];
    }
}

- (void)muteClick {
    _isMute = !_isMute;
    [_callViewModel setAudio:_isMute];
    if (_isMute) {
        _imgMute.image = [UIImage imageNamed:@"call_mute_n.png"];
    } else {
        _imgMute.image = [UIImage imageNamed:@"call_mute_s.png"];
    }
}

-(void)shouqiClick {
    AppDelegate *deleage = (AppDelegate *)[UIApplication sharedApplication].delegate;
    deleage.floatWindow.isCannotTouch = NO;
    __weak typeof (self) weakSelf = self;
    deleage.floatWindow.floatDelegate = weakSelf;
    [deleage.floatWindow startWithTime:self.time presentview:self.view inRect:CGRectMake(100, 100, 100, 100)];
    [UIView animateWithDuration:0.3f animations:^{
        [deleage setRootView];
    }];
}

-(void)assistiveTocuhs {
    AppDelegate *deleage = (AppDelegate *)[UIApplication sharedApplication].delegate;
    deleage.floatWindow.isCannotTouch = YES;
}

-(void) acceptCall:(UITapGestureRecognizer*)tap{
//    ringtone.stop();
    [[ZoloSoundManager sharedManager] stopAlertSound];
    _imgReject.frame = CGRectMake((self.view.bounds.size.width-80)/2, self.view.bounds.size.height-200, 80, 80);
    [_imgAccept removeFromSuperview];
    _imgMute.hidden = NO;
    _imgSeapker.hidden = NO;
    [self startTime];
    if (!_isAudioOnly) {
        _imgUser.frame = CGRectMake(60, 60, 40, 40);
        _nameLab.frame = CGRectMake(110, 50, kScreenwidth - 200, 30);
        _timeLab.frame = CGRectMake(110, 80, kScreenwidth - 200, 30);
        _nameLab.textAlignment = NSTextAlignmentLeft;
        _timeLab.textAlignment = NSTextAlignmentLeft;
    }
    [self.view addSubview:_litView];
    _status = @"calling";
    NSLog(@"acceptCall url: %@", _receiveCall.url);
    NSString * replaceIP = [self getIP:_receiveCall.url];
    if(![_callViewModel pushCall:_userInfo.userId replaceIP:replaceIP])
        NSLog(@"push call failed");
    if(![_callViewModel pullCall:_litView url:_receiveCall.url])
        NSLog(@"pull call failed");
    //NSString * replaceIP = [self getIP:_receiveCall.url];
    //[self replaceBase:replaceIP];
    [self sendCallMessage:CallActionAnswer];
}

- (void)closeTime {
    [self.timer invalidate];
    self.timer = nil;
}

- (void)startTime {
    if (self.timer) {
        [self.timer invalidate];
        self.timer = NULL;
    }
    self.timer = [NSTimer scheduledTimerWithTimeInterval:1 target:self selector:@selector(changeTimeLable) userInfo:nil repeats:YES] ;
    [[NSRunLoop currentRunLoop] addTimer:self.timer forMode:NSRunLoopCommonModes];
    [_timer fire];
}

- (void)changeTimeLable {
    self.timeLab.hidden = NO;
    self.timeLab.text = [self changeTimeFormater:self.time];
    self.time++;
}

- (NSString *)changeTimeFormater:(NSInteger)time{
    NSInteger minutecount = time / 60;
    
    NSInteger secondcount = time % 60;
    NSString *timeString;
    if (minutecount > 60) {
        NSInteger hour = minutecount / 60;
        minutecount = hour % 60;
        if (hour > 10) {
            if (minutecount < 10 && secondcount < 10) {
                timeString = [NSString stringWithFormat:@"%ld:0%ld:0%ld",hour,minutecount,secondcount];
                return timeString;
            }
            if (minutecount < 10) {
                timeString = [NSString stringWithFormat:@"%ld:%ld:%ld",hour,minutecount,secondcount];
                return timeString;
            }
            if (secondcount < 10) {
                timeString = [NSString stringWithFormat:@"%ld:%ld:0%ld",hour,minutecount,secondcount];
                return timeString;
                
            }
        } else {
            if (minutecount < 10 && secondcount < 10) {
                timeString = [NSString stringWithFormat:@"0%ld:0%ld:0%ld",hour,minutecount,secondcount];
                return timeString;
            }
            if (minutecount < 10) {
                timeString = [NSString stringWithFormat:@"0%ld:%ld:%ld",hour,minutecount,secondcount];
                return timeString;
            }
            if (secondcount < 10) {
                timeString = [NSString stringWithFormat:@"0%ld:%ld:0%ld",hour,minutecount,secondcount];
                return timeString;
                
            }
        }
        
    }
    if (minutecount < 10 && secondcount < 10) {
        timeString = [NSString stringWithFormat:@"0%ld:0%ld",minutecount,secondcount];
        return timeString;
    }
    if (minutecount < 10) {
        timeString = [NSString stringWithFormat:@"0%ld:%ld",minutecount,secondcount];
        return timeString;
    }
    if (secondcount < 10) {
        timeString = [NSString stringWithFormat:@"%ld:0%ld",minutecount,secondcount];
        return timeString;
        
    }
    return [NSString stringWithFormat:@"%ld:%ld",minutecount,secondcount];
}

-(void) rejectCall:(UITapGestureRecognizer*)tap{
    if([_status isEqualToString:@"invite"]){
        _status = @"refuse";
        [self sendCallMessage:CallActionReject];
    }else if([_status isEqualToString:@"launch"]){
        _status = @"cancel";
        [self sendCallMessage:CallActionCancel];
    }else{
        _status = @"finish";
        [self sendCallMessage:CallActionFinish];
    }
    [self finishCall:nil];
}
-(void)finishCall:(NSString *)hash {
    [[ZoloSoundManager sharedManager] stopAlertSound];
    [self closeTime];
    [_callViewModel clear];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:kReceiveMessages object:nil];
    AppDelegate *deleage = (AppDelegate *)[UIApplication sharedApplication].delegate;
    [deleage.floatWindow close];
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        deleage.floatWindow = nil;
    });
    [self callFinish:hash];
    [self dismissViewControllerAnimated:YES completion:nil];
}
-(DMCCCallMessageContent*) getMessage:(int) action{
    DMCCCallMessageContent* content = [DMCCCallMessageContent new];
    content.type = _isAudioOnly ? CallTypeAudio : CallTypeVideo;
    content.mode = CallModeSingle;
    content.action = action;
    content.url = [_callViewModel getUrl:_userInfo.userId];
    content.user = _userInfo.userId;
    content.status = @"";
    content.duration = -1;
    MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
    content.voiceBaseUrl = scBaseUrl;
    content.voiceHostUrl = scHostUrl;
    return content;
}
-(void) sendCallMessage:(int) action{
    DMCCCallMessageContent *content = [self getMessage:action];
    DMCCMessage *message = [DMCCMessage new];
    message.conversation = [DMCCConversation conversationWithType:Call_Type target:_conversation.target line:0];
    message.content = content;
    message.fromUser = [[DMCCIMService sharedDMCIMService] getUserID];
    [[DMCCIMService sharedDMCIMService] sendCallMessage:message success:^(void) {
        } error:^(int errorCode) {
        }];
}
-(void) callFinish:(NSString *)hash {
    DMCCCallMessageContent* content = [self getMessage:CallActionFinish];
    long times = [[NSDate date] timeIntervalSince1970] * 1000;
    content.duration = (int) (times - _duration);
    content.status = _status;
    DMCCMessage *message = [DMCCMessage new];
    message.conversation = _conversation;
    message.content = content;
    if(_isLaunch){
        message.direction = MessageDirection_Send;
        message.fromUser = [[DMCCIMService sharedDMCIMService] getUserID];
        message.status = Message_Status_Sent;
    }else{
        message.direction = MessageDirection_Receive;
        message.fromUser = _conversation.target;
        message.status = Message_Status_Readed;
    }
    message.serverTime = times;
    message.messageHash = hash;
    [[DMCCIMService sharedDMCIMService] saveCallMessage:message];
}

- (void)onReceiveMessages:(NSNotification *)notification {
    NSArray<DMCCMessage *> *messages = notification.object;
    for(DMCCMessage *message in messages){
        if([message.content.class getContentType] == MESSAGE_CONTENT_TYPE_CALL && message.direction == MessageDirection_Receive){
            [[ZoloSoundManager sharedManager] stopAlertSound];
            DMCCCallMessageContent *call = (DMCCCallMessageContent*)message.content;
            if(call.action == CallActionAnswer){
                [self startTime];
                if (!_isAudioOnly) {
                    _imgUser.frame = CGRectMake(60, 60, 40, 40);
                    _nameLab.frame = CGRectMake(110, 50, kScreenwidth - 200, 30);
                    _timeLab.frame = CGRectMake(110, 80, kScreenwidth - 200, 30);
                    _nameLab.textAlignment = NSTextAlignmentLeft;
                    _timeLab.textAlignment = NSTextAlignmentLeft;
                }
                [self.view addSubview:_litView];
                if([_callViewModel pullCall:_litView url:call.url]){
                    _status = @"calling";
                }
            }else{
                if(call.action == CallActionReject){
                    _status = @"reject";
                }else if(call.action == CallActionFinish){
                    _status = @"finish";
                }else if(call.action == CallActionCancel){
                    _status = @"reject";
                }
                [self finishCall:message.messageHash];
            }
        }
    }
}

-(void)viewDidDisappear:(BOOL)animated{
    AppDelegate *delegate = (AppDelegate *)[UIApplication sharedApplication].delegate;
    if(!delegate.floatWindow.isShow){
        delegate.floatWindow.floatDelegate = nil;
    }
    [super viewDidDisappear:animated];
}

@end
