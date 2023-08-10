//
//  ZoloInformationCell.m
//  NewZolo
//
//  Created by JTalking on 2022/8/29.
//

#import "ZoloInformationCell.h"

@interface ZoloInformationCell ()

@property (weak, nonatomic) IBOutlet UILabel *infoLab;

@end

@implementation ZoloInformationCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
}

+ (void)calculateCellHeight:(DMCCMessage *)message chatType:(BOOL)isGroup mulSelect:(BOOL)isMulSelect byMsg:(BOOL)isMySend {
    if (message.msgCellHeight > 0) {
        return;
    }
    CGFloat height = [MHHelperUtils cellTextHeight:message.digest font:16 textWith:KScreenWidth - 120];
    height += 22;
    message.msgCellHeight = height;
}

- (void)setMessage:(DMCCMessage *)message {
    [super setMessage:message];
    
    if ([message.content isKindOfClass:[DMCCNotificationMessageContent class]]) {
        DMCCNotificationMessageContent *content = (DMCCNotificationMessageContent *)message.content;
        NSLog(@"==test== setMessage");
        self.infoLab.text = [content formatNotification:message];
    } else {
        self.infoLab.text = [message digest];
    }
    self.nickNameLabel.hidden = YES;
    self.errorImg.hidden = YES;
}


@end
