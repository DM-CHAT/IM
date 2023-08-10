//
//  ZoloAddFriendCell.m
//  NewZolo
//
//  Created by JTalking on 2022/8/24.
//

#import "ZoloAddFriendCell.h"

@interface ZoloAddFriendCell () <UITextFieldDelegate>

@property (weak, nonatomic) IBOutlet UIView *textfieldBg;

@end

@implementation ZoloAddFriendCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    self.textField.placeholder = LocalizedString(@"PleaseEnterOns");
    self.textField.delegate = self;
    self.textfieldBg.backgroundColor = [FontManage MHLineSeparatorColor];
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    if (_textFieldBlock) {
        _textFieldBlock(textField.text);
    }
    return YES;
}

- (BOOL)textFieldShouldBeginEditing:(UITextField *)textField {
    return YES;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}


@end
