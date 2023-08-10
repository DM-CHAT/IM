//
//  ZoloSoundManager.m
//  NewZolo
//
//  Created by JTalking on 2022/8/5.
//

#import "ZoloSoundManager.h"
#import <AudioToolbox/AudioToolbox.h>

static SystemSoundID AudioAndVideNotify;

@implementation ZoloSoundManager

static ZoloSoundManager *_SoundManager;

+ (instancetype)allocWithZone:(struct _NSZone *)zone {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _SoundManager = [super allocWithZone:zone];
    });
    return _SoundManager;
}

+ (instancetype)sharedManager {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _SoundManager = [[self alloc] init];
    });
    return _SoundManager;
}

- (id)copyWithZone:(NSZone *)zone {
    return _SoundManager;
}

- (void)prePlayVideoNotifySound {
    @try {
        if (self.isSounding) {
            return;
        }
        
        AudioServicesCreateSystemSoundID((__bridge CFURLRef)[NSURL fileURLWithPath:[[NSBundle mainBundle] pathForResource:@"audio_send" ofType:@"mp3"]], &AudioAndVideNotify);
        
        [self playVideoNotifySound];
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
}

- (void)playVideoNotifySound {
    @try {
        self.isSounding = YES;
        AudioServicesAddSystemSoundCompletion (AudioAndVideNotify, NULL, NULL,
                                               &sompletionCallback,
                                            (__bridge void*)self);
        AudioServicesPlaySystemSound(AudioAndVideNotify);
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
}

- (void)stopAlertSound{
    self.isSounding = NO;
    [self stopAlertSoundWithSoundID:AudioAndVideNotify];
}

- (void)stopAlertSoundWithSoundID:(SystemSoundID)sound {
    @try {
        AudioServicesDisposeSystemSoundID(sound);
        AudioServicesRemoveSystemSoundCompletion(sound);
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
}

static void sompletionCallback (SystemSoundID  mySSID, void* data) {
    AudioServicesPlaySystemSound(AudioAndVideNotify);
}

@end
