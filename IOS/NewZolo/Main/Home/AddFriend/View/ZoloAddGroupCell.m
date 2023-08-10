//
//  ZoloAddGroupCell.m
//  NewZolo
//
//  Created by MHHY on 2023/6/8.
//

#import "ZoloAddGroupCell.h"

@interface ZoloAddGroupCell () <UITextFieldDelegate>

@property (weak, nonatomic) IBOutlet UILabel *titleLab;
@property (weak, nonatomic) IBOutlet UITextField *reasionTextField;

@end

@implementation ZoloAddGroupCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    
    self.titleLab.text = [NSString stringWithFormat:@"%@/%@", LocalizedString(@"AddGroupInputPwd"), LocalizedString(@"UserRemark")];
    self.reasionTextField.placeholder = [NSString stringWithFormat:@"%@/%@", LocalizedString(@"AddGroupInputPwd"), LocalizedString(@"UserRemark")];
    self.reasionTextField.delegate = self;
}

- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string {
    NSString * str = [NSString stringWithFormat:@"%@%@",textField.text,string];
    if (_addGroupBlock) {
        _addGroupBlock(str);
    }
    return YES;
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    if (_addGroupBlock) {
        _addGroupBlock(textField.text);
    }
    return YES;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end
