//
//  ZoloAudioView.m
//  NewZolo
//
//  Created by JTalking on 2022/8/10.
//

#import "ZoloAudioView.h"
#import <AudioToolbox/AudioToolbox.h>
#import <AVFoundation/AVFoundation.h>
#import "lame.h"

@interface ZoloAudioView ()

@property (weak, nonatomic) IBOutlet UIView *voiceBgView;
@property (weak, nonatomic) IBOutlet UIView *bgView;
@property (weak, nonatomic) IBOutlet UIButton *cancelBtn;
@property (weak, nonatomic) IBOutlet UIButton *wordBtn;

@property (weak, nonatomic) IBOutlet UILabel *endLab;
@property (weak, nonatomic) IBOutlet UILabel *wordLab;
@property (weak, nonatomic) IBOutlet UILabel *cancelLab;


@property (nonatomic, strong) AVAudioSession *audioSession;
@property (nonatomic, strong) AVAudioRecorder *audioRecorder;

@property (nonatomic, copy) NSString *curCafFilePath;
@property (nonatomic, copy) NSString *mp3FilePath;

@property (nonatomic,weak) CAReplicatorLayer *replicatorL;  // 复制图层
@property (nonatomic,weak) CAShapeLayer *levelLayer;        // 振幅layer
@property (nonatomic,strong) NSMutableArray *currentLevels; // 当前振幅数组
@property (nonatomic,strong) UIBezierPath *levelPath;       // 画振幅的path
@property (nonatomic,strong) CADisplayLink *levelTimer;     // 振幅计时器
@property (nonatomic,strong) NSMutableArray *allLevels;     // 所有收集到的振幅,预先保存，用于播放
@property (weak, nonatomic) IBOutlet UIView *Layerview;

@end

#define ALPHA 0.02f                 // 音频振幅调解相对值 (越小振幅就越高)

@implementation ZoloAudioView

static CGFloat const levelWidth = 3.0;
static CGFloat const levelMargin = 2.0;

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self = [[[NSBundle mainBundle] loadNibNamed:@"ZoloAudioView" owner:self options:nil] firstObject];
        self.frame = CGRectMake(0, 0, KScreenWidth, KScreenheight);
        AudioServicesPlaySystemSound(kSystemSoundID_Vibrate);
        self.cancelLab.text = LocalizedString(@"Cancel");
        self.endLab.text = LocalizedString(@"VoiceEndLab");
        self.wordLab.text = LocalizedString(@"VoiceWordLab");
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(audioStatus:)name:FZ_EVENT_AUDIO_STATE object:nil];
        self.bgView.backgroundColor = [FontManage MHLineSeparatorColor];
        [self setUpAudioSession];
    }
    return self;
}

- (NSMutableArray *)currentLevels {
    if (_currentLevels == nil) {
        _currentLevels = [NSMutableArray arrayWithArray:@[@0.05,@0.05,@0.05,@0.05,@0.05,@0.05,@0.05,@0.05,@0.05,@0.05,@0.05,@0.05,@0.05,@0.05,@0.05,@0.05,@0.05,@0.05,@0.05,@0.05]];
    }
    return _currentLevels;
}

- (CAShapeLayer *)levelLayer {
    if (_levelLayer == nil) {
        CAShapeLayer *layer = [CAShapeLayer layer];
        layer.frame = CGRectMake(kScreenwidth / 2.8, 0, kScreenwidth / 2.0, 50);
        layer.strokeColor = [UIColor whiteColor].CGColor;
        layer.lineWidth = levelWidth;
        [self.replicatorL addSublayer:layer];
        _levelLayer = layer;
    }
    return _levelLayer;
}

- (CAReplicatorLayer *)replicatorL {
    if (_replicatorL == nil) {
        CAReplicatorLayer *repL = [CAReplicatorLayer layer];
        repL.frame = self.layer.bounds;
        repL.instanceCount = 2;
        repL.instanceTransform = CATransform3DMakeRotation(M_PI, 0, 0, 1);
        [self.Layerview.layer addSublayer:repL];
        _replicatorL = repL;
        [self levelLayer];
    }
    return _replicatorL;
}

- (void)updateLevelLayer {
    
    self.levelPath = [UIBezierPath bezierPath];
    
    CGFloat height = CGRectGetHeight(self.levelLayer.frame);
    for (int i = 0; i < self.currentLevels.count; i++) {
        CGFloat x = i * (levelWidth + levelMargin) + 5;
        CGFloat pathH = [self.currentLevels[i] floatValue] * height;
        CGFloat startY = height / 2.0 - pathH / 2.0;
        CGFloat endY = height / 2.0 + pathH / 2.0;
        [_levelPath moveToPoint:CGPointMake(x, startY)];
        [_levelPath addLineToPoint:CGPointMake(x, endY)];
    }
    
    self.levelLayer.path = _levelPath.CGPath;
}

#pragma mark - displayLink
- (void)startMeterTimer {
    [self stopMeterTimer];
    self.levelTimer = [CADisplayLink displayLinkWithTarget:self selector:@selector(updateMeter)];

    if ([[UIDevice currentDevice].systemVersion floatValue] > 10.0) {
        self.levelTimer.preferredFramesPerSecond = 10;
    }else {
        self.levelTimer.frameInterval = 6;
    }
    [self.levelTimer addToRunLoop:[NSRunLoop currentRunLoop] forMode:NSRunLoopCommonModes];
}

- (float)levels {
    [self.audioRecorder updateMeters];
    double aveChannel = pow(10, (ALPHA * [self.audioRecorder averagePowerForChannel:0]));
    if (aveChannel <= 0.05f) aveChannel = 0.05f;
    
    if (aveChannel >= 1.0f) aveChannel = 1.0f;
    
    return aveChannel;
    
}

// 停止定时器
- (void)stopMeterTimer {
    [self.levelTimer invalidate];
}

- (NSMutableArray *)allLevels {
    if (_allLevels == nil) {
        _allLevels = [NSMutableArray array];
    }
    return _allLevels;
}

- (void)updateMeter {
    CGFloat level = [self levels];
    [self.currentLevels removeLastObject];
    [self.currentLevels insertObject:@(level) atIndex:0];
    [self.allLevels addObject:@(level)];
    [self updateLevelLayer];
}

- (void)audioStatus:(NSNotification *)notification {
    NSNumber *info = [notification object];
    [self startMeterTimer];
    switch (info.intValue) {
        case MHAudioType_Cancel:
        {
            self.hidden = YES;
            [self cancelButtonAction];
            if (_audioViewBlock) {
                _audioViewBlock();
            }
            [self stopMeterTimer];
        }
            break;
        case MHAudioType_Word:
        {
            self.hidden = YES;
            [self cancelButtonAction];
            [MHAlert showMessage:LocalizedString(@"AlertStayTuned")];
            if (_audioViewBlock) {
                _audioViewBlock();
            }
            [self stopMeterTimer];
        }
            break;
        case MHAudioType_RedayCancel:
        {
            self.cancelBtn.selected = YES;
        }
            break;
        case MHAudioType_ReadyWord:
        {
            self.wordBtn.selected = YES;
        }
            break;
        case MHAudioType_Send:
        {
            self.hidden = YES;
            [self sendButtonAction];
            [self stopMeterTimer];
        }
            break;
        case MHAudioType_RedaySend:
        {
            self.wordBtn.selected = NO;
            self.cancelBtn.selected = NO;
        }
            break;
        default:
            break;
    }
}

//取消
- (void)cancelButtonAction {
    if (self.mp3FilePath) {
        [[NSFileManager defaultManager] removeItemAtURL:[NSURL URLWithString:self.mp3FilePath] error:nil];
    }
}

//发送
- (void)sendButtonAction {
    [self stopAudioRecord];
    if (self.audioCompletionBlock) {
        self.audioCompletionBlock(self.mp3FilePath);
    }
}

//配置音频会话
- (void)setUpAudioSession {
    self.audioSession = [AVAudioSession sharedInstance];
    NSError *setCategoryError = nil;
    [self.audioSession setCategory:AVAudioSessionCategoryPlayAndRecord error:&setCategoryError];
    if (setCategoryError) {
        NSLog(@"%@", setCategoryError.localizedDescription);
    }
    [self.audioSession setActive:YES error:nil];
    
    NSString *audioFilePath = [self generateAudioFilePathWithDate:[NSDate date] andExt:@"caf"];
    self.curCafFilePath = audioFilePath;
    NSURL *audioFileUrl = [NSURL fileURLWithPath:audioFilePath];
    //录音通道数  1 或 2 ，要转换成mp3格式必须为双通道
    //kAudioFormatMPEGLayer3 设置为MP3会导致audioRecorder无法初始化
    ////设置录音采样率(Hz) 如：AVSampleRateKey==8000/44100/96000（影响音频的质量）, 采样率必须要设为11025才能使转化成mp3格式后不会失真
    NSDictionary *recordSetting = @{
                                    AVSampleRateKey: @11025.0f,                         // 采样率
                                    AVFormatIDKey: @(kAudioFormatLinearPCM),           // 音频格式
                                    AVLinearPCMBitDepthKey: @16,                       // 采样位数
                                    AVNumberOfChannelsKey: @2,                         // 音频通道
                                    AVEncoderAudioQualityKey: @(AVAudioQualityLow)    // 录音质量
    };
    NSError *recorderError = nil;
    self.audioRecorder = [[AVAudioRecorder alloc] initWithURL:audioFileUrl settings:recordSetting error:&recorderError];
    if (recorderError) {
        NSLog(@"录制器初始化  %@",recorderError.localizedDescription);
    }
    self.audioRecorder.meteringEnabled = YES;
    [self.audioRecorder prepareToRecord];
    [self.audioRecorder record];
}

#pragma mark - 私有方法
//生成音频文件路径地址
- (NSString *)generateAudioFilePathWithDate:(NSDate *)date andExt:(NSString *)ext {
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
       [formatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
       NSString *dateString = [formatter stringFromDate:date];
    NSString *dateStr = [NSString stringWithFormat:@"%ld", [MHDateTools timeStampWithDate:dateString]];
    NSString *directoryPath = [self getAudioDirectoryPath];
    if (![[NSFileManager defaultManager] fileExistsAtPath:directoryPath]) {
        [[NSFileManager defaultManager] createDirectoryAtPath:directoryPath withIntermediateDirectories:YES attributes:nil error:nil];
    }
    NSString *filePath = [NSString stringWithFormat:@"%@/%@.%@",directoryPath,dateStr,ext];
    return filePath;
}

- (NSString *)getAudioDirectoryPath {
    NSString *cachePath = [NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) firstObject];
    NSString *directoryPath = [NSString stringWithFormat:@"%@/soundFile",cachePath];
    return directoryPath;
}

//暂停录制
- (void)stopAudioRecord {
    if ([self.audioRecorder isRecording]) {
        [self.audioRecorder stop];
        self.mp3FilePath = [self audio_PCMtoMP3WithFilePath:self.curCafFilePath];
    }
}

- (NSString *)audio_PCMtoMP3WithFilePath:(NSString *)filePath {
    NSString *cafFilePath = filePath;    //caf文件路径
    NSString *mp3FileName = [self generateAudioFilePathWithDate:[NSDate date] andExt:@"mp3"];
    
    @try {
        int read, write;
        
        FILE *pcm = fopen([cafFilePath cStringUsingEncoding:1], "rb");  //source 被转换的音频文件位置
        fseek(pcm, 4*1024, SEEK_CUR);                                   //skip file header
        FILE *mp3 = fopen([mp3FileName cStringUsingEncoding:1], "wb");  //output 输出生成的Mp3文件位置
        
        const int PCM_SIZE = 8192;
        const int MP3_SIZE = 8192;
        short int pcm_buffer[PCM_SIZE*2];
        unsigned char mp3_buffer[MP3_SIZE];
        
        lame_t lame = lame_init();
        lame_set_num_channels(lame, 2);//设置1为单通道，默认为2双通道
        lame_set_in_samplerate(lame, 11025.0);//11025.0        8000.0
        //        lame_set_VBR(lame, vbr_default);
        lame_set_brate(lame, 16);
        lame_set_mode(lame, 3);
        lame_set_quality(lame, 2);
        lame_init_params(lame);
        
        do {
            read = (int)fread(pcm_buffer, 2*sizeof(short int), PCM_SIZE, pcm);
            if (read == 0)
            write = lame_encode_flush(lame, mp3_buffer, MP3_SIZE);
            else
            write = lame_encode_buffer_interleaved(lame, pcm_buffer, read, mp3_buffer, MP3_SIZE);
            
            fwrite(mp3_buffer, write, 1, mp3);
            
        } while (read != 0);
        
        lame_close(lame);
        fclose(mp3);
        fclose(pcm);
    }
    @catch (NSException *exception) {
        NSLog(@"%@",[exception description]);
    }
    @finally {

    }
    return mp3FileName;
}

- (void)dealloc {
    NSLog(@"======ZoloAudioView=======");
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

@end
