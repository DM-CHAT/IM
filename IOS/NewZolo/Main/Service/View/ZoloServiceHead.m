//
//  ZoloServiceHead.m
//  NewZolo
//
//  Created by JTalking on 2022/10/7.
//

#import "ZoloServiceHead.h"

@interface ZoloServiceHead () <UITextFieldDelegate>

@property (weak, nonatomic) IBOutlet UIView *searchView;
@property (weak, nonatomic) IBOutlet UITextField *searchTextfield;
@property (weak, nonatomic) IBOutlet UILabel *serviceName;

@end

@implementation ZoloServiceHead

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    
    self.searchView.layer.cornerRadius = 15;
    self.searchView.layer.masksToBounds = YES;
    self.serviceName.text = LocalizedString(@"SelectService");
    self.searchTextfield.placeholder = LocalizedString(@"SearchTitle");
    
    self.searchTextfield.delegate = self;
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    if (_searchBlock) {
        _searchBlock(textField.text);
    }
    return YES;
}

@end
