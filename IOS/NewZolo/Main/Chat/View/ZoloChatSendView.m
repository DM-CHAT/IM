//
//  ZoloChatSendView.m
//  NewZolo
//
//  Created by JTalking on 2022/7/1.
//

#import "ZoloChatSendView.h"

@interface ZoloChatSendView () <YYTextViewDelegate>

@property (weak, nonatomic) IBOutlet UITextField *textMsgField;
@property (weak, nonatomic) IBOutlet UIButton *emjBtn;
@property (weak, nonatomic) IBOutlet UIButton *voiceBtn;
@property (weak, nonatomic) IBOutlet UIButton *voiceStatusBtn;
@property (nonatomic, strong) UIPanGestureRecognizer *pan;
@property (nonatomic, strong) UILongPressGestureRecognizer *longGes;
@property (weak, nonatomic) IBOutlet UIView *muteView;
@property (weak, nonatomic) IBOutlet UIView *textBgView;
@property (weak, nonatomic) IBOutlet UILabel *muteLab;

@end

@implementation ZoloChatSendView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self = [[[NSBundle mainBundle] loadNibNamed:@"ZoloChatSendView" owner:self options:nil] firstObject];
        self.textBgView.layer.cornerRadius = 5;
        self.textBgView.layer.masksToBounds = YES;
        self.textBgView.backgroundColor = [FontManage MHInputBgColor];
        self.backgroundColor = [FontManage MHWhiteColor];
        self.frame = CGRectMake(0, KScreenheight, KScreenWidth, 255);
        [self.voiceBtn setTitle:LocalizedString(@"PutTalk") forState:UIControlStateNormal];
        self.muteLab.text = LocalizedString(@"SettingMute");
        [self textView];
        [self longGes];
    }
    return self;
}

- (void)muteViewHidden:(BOOL)isHidden {
    self.muteView.hidden = isHidden;
}

- (void)emojViewNomal {
    self.emjBtn.selected = NO;
}

- (IBAction)emojiBtnClick:(id)sender {
    self.emjBtn.selected = !self.emjBtn.selected;
    if (self.emjBtn.isSelected) {
        self.voiceStatusBtn.selected = NO;
        self.voiceBtn.hidden = YES;
        self.textView.hidden = NO;
    }
    if (_emjBlock) {
        _emjBlock(self.emjBtn.selected);
    }
}

- (IBAction)addMoreBtnClick:(id)sender {
    if (_moreBtnBlock) {
        _moreBtnBlock();
    }
    self.emjBtn.selected = NO;
}

- (IBAction)voiceBtnClick:(UIButton *)sender {
    sender.selected = !sender.selected;
    
    self.emjBtn.selected = NO;
    
    if (_voiceBtnSelectBlock) {
        _voiceBtnSelectBlock(self.voiceBtn.selected);
    }
    
    if (sender.isSelected) {
        self.voiceBtn.hidden = NO;
        self.textView.hidden = YES;
    } else {
        self.voiceBtn.hidden = YES;
        self.textView.hidden = NO;
    }
}

- (UILongPressGestureRecognizer *)longGes {
    if (!_longGes) {
        _longGes = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(longAction:)];
        [self.voiceBtn addGestureRecognizer:_longGes];
    }
    return _longGes;
}

- (void)longAction:(UILongPressGestureRecognizer *)longGes {
    if (_voiceBtnBlock) {
        _voiceBtnBlock();
    }
    CGPoint postion = [longGes locationInView:self.voiceBtn];
    NSInteger pointX = kScreenwidth / 2;
    if (longGes.state == UIGestureRecognizerStateEnded) {
        if (postion.x < 100 && postion.y < -150) {
            MHAudioType type = MHAudioType_Cancel;
            [[NSNotificationCenter defaultCenter] postNotificationName:FZ_EVENT_AUDIO_STATE object:@(type)];
        } else if (postion.x > pointX && postion.y < -150) {
            MHAudioType type = MHAudioType_Word;
            [[NSNotificationCenter defaultCenter] postNotificationName:FZ_EVENT_AUDIO_STATE object:@(type)];
        } else {
            MHAudioType type = MHAudioType_Send;
            [[NSNotificationCenter defaultCenter] postNotificationName:FZ_EVENT_AUDIO_STATE object:@(type)];
        }
    } else {
        if (postion.x < 100 && postion.y < -150) {
            MHAudioType type = MHAudioType_RedayCancel;
            [[NSNotificationCenter defaultCenter] postNotificationName:FZ_EVENT_AUDIO_STATE object:@(type)];
        } else if (postion.x > pointX && postion.y < -150) {
            MHAudioType type = MHAudioType_ReadyWord;
            [[NSNotificationCenter defaultCenter] postNotificationName:FZ_EVENT_AUDIO_STATE object:@(type)];
        }else {
            MHAudioType type = MHAudioType_RedaySend;
            [[NSNotificationCenter defaultCenter] postNotificationName:FZ_EVENT_AUDIO_STATE object:@(type)];
        }
    }
}

// 是否与其他手势共存，一般使用默认值(默认返回NO：不与任何手势共存)
- (BOOL)gestureRecognizer:(UIPanGestureRecognizer *)gestureRecognizer shouldRecognizeSimultaneouslyWithGestureRecognizer:(UIGestureRecognizer *)otherGestureRecognizer {
    return YES;
}

- (void)setEmojText:(NSString *)emoJString {
    self.textView.text = [NSString stringWithFormat:@"%@%@", self.textView.text, emoJString];
}

- (NSString *)getTextFieldContent {
    NSString *str = self.textView.text;
    self.textView.text = @"";
    return str;
}

- (YYTextView *)textView {
    if (!_textView) {
        int textW = 0;
        if (iphone6Plus_5_5) {
            textW = 80;
        } else if (iphone6_4_7) {
            textW = 120;
        } else if (iPhoneX) {
            textW = 124;
        }
        _textView = [[YYTextView alloc] initWithFrame:CGRectMake(5, 5, self.textBgView.frame.size.width - textW, 45)];
        _textView.tintColor = [FontManage MHTextViewTintColor];
        _textView.font = [UIFont systemFontOfSize:17];
        _textView.textColor = [FontManage MHBlockColor];
        _textView.delegate = self;
        _textView.returnKeyType = UIReturnKeySend;
        [self.textBgView addSubview:_textView];
    }
    return _textView;
}

#pragma mark - YYTextViewDelegate

- (void)textViewDidEndEditing:(YYTextView *)textView {
    
}

- (BOOL)textViewShouldBeginEditing:(YYTextView *)textView {
    // 控制键盘输入是，如果表情键盘还在打开状态，就关闭
    if (self.emjBtn.selected) {
        [self emojiBtnClick:nil];
    }
    return YES;
}


//// 每次输入都会调用
- (BOOL)textView:(YYTextView *)textView shouldChangeTextInRange:(NSRange)range replacementText:(NSString *)text {
    NSString *str = nil;
    if (textView.text.length >= 2000) {
        [MHAlert showMessage:LocalizedString(@"AlertSendMoreWordNum")];
        str = [textView.text substringToIndex:2000];

        if ([text isEqualToString:@"\n"]){
            if (_returnBlock) {
                _returnBlock(str);
            }
            self.textView.text = @"";
            return NO;
        }
        if (text.length == 0) {
            return YES;
        }
        return NO;
    }
    if ([text isEqualToString:@"\n"]){

        if (textView.text.length >= 200) {

            if (_returnBlock) {
                _returnBlock(str);
            }
            self.textView.text = @"";

            return NO;

        }

        if (_returnBlock) {
            _returnBlock(textView.text);
        }
        self.textView.text = @"";
        return NO;
    }

    if(self.chatType == Group_Type) {
        if ([text isEqualToString:@"@"]) {
            if (_noticeToBlock) {
                _noticeToBlock();
            }
        }
    }

    return YES;
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

@end
