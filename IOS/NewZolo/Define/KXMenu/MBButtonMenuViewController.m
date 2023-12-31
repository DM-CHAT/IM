//
//  PDButtonMenuViewController.m
//  MBMenuController
//
//  Created by Moshe Berman on 3/11/13.
//  Copyright (c) 2013 Moshe Berman. All rights reserved.
//

#import "MBButtonMenuViewController.h"

#import <CoreGraphics/CoreGraphics.h>

@interface MBButtonMenuViewController ()

@property (nonatomic, strong) UIView *modalView;
@property (nonatomic, strong) UIView *buttonView;
@property (nonatomic, strong) UIScrollView *buttonWrapper;

@property (nonatomic, strong) NSArray *buttons;

@property (nonatomic, assign) CGFloat minimumButtonHeight;
@property (nonatomic, assign) CGFloat maximumButtonWidth;

@property (nonatomic, strong) UIView *shrunkView;

@end

@implementation MBButtonMenuViewController

- (id)init
{
    self = [super init];
    if (self) {
        
        //A dark gray color is the default
        _backgroundColor = [UIColor darkGrayColor];
        
        //  The modal view
        _modalView = [UIView new];
        [_modalView setBackgroundColor:[[UIColor blackColor] colorWithAlphaComponent:0.6]];
        
        //  The visible view
        _buttonView = [UIView new];
        [_buttonView setBackgroundColor:_backgroundColor];
        
        //  The button wrapper
        _buttonWrapper = [UIScrollView new];
        
        // Cancel Button Index
        _cancelButtonIndex = -1;
        
        //  Minimum Button Height
        _minimumButtonHeight = 40;
        
        //  Maximum button width
        _maximumButtonWidth = 340;
        
        //  Visible
        _visible = NO;
        
        //  Should the parent view shrink down?
        _shrinksParentView = NO;
        
    }
    return self;
}

- (id)initWithButtonTitles:(NSArray *) buttonTitles
{
    self = [self init];
    if (self) {
        _buttonTitles = buttonTitles;
    }
    return self;
}

#pragma mark - View Lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    //  1. Set view to be transparent - with autoresizing
    [[self view] setOpaque:NO];
    [[self view] setBackgroundColor:[UIColor clearColor]];
    //自动适应父视图大小
    [[self view] setAutoresizingMask:(UIViewAutoresizingFlexibleHeight | UIViewAutoresizingFlexibleWidth)];
    
    //  2. Add a modal overlay - with autoresizing
    [[self view] addSubview:_modalView];
    
//    [[self view] setFrame:CGRectMake([self view].frame.origin.x,[self view].frame.origin.y+[self view].frame.size.height/2,[self view].frame.size.width,[self view].frame.size.height/2)];
    
    //CGRectMake([self view].frame.origin.x,[self view].frame.origin.y,[self view].frame.size.width,[self view].frame.size.height/2));
   // CGRect buttonFrame = [[self view] bounds];
    
    [_modalView setFrame:[[self view] bounds]];
    [_modalView setAutoresizingMask:(UIViewAutoresizingFlexibleHeight | UIViewAutoresizingFlexibleWidth)];
    
    //  3. Add a sheet that's one third of the screen
    [[self view] addSubview:_buttonView];
    
    // Position at the bottom of the screen
    CGRect frame = [[self view] bounds];
    frame.origin.y = frame.size.height - (frame.size.height/4.2);
    frame.size.height /=  4.2;
    [_buttonView setFrame:frame];
    [_buttonView setAutoresizingMask:(UIViewAutoresizingFlexibleTopMargin | UIViewAutoresizingFlexibleWidth)];
    
    
    //  4. Put a scroll view in there
    [_buttonView addSubview:_buttonWrapper];
    [_buttonWrapper setFrame:CGRectMake(0, 0, frame.size.width, frame.size.height)];
    [_buttonWrapper setAutoresizingMask:UIViewAutoresizingFlexibleWidth];
    
    _buttonWrapper.backgroundColor = [UIColor lightGrayColor];
//        _buttonWrapper.backgroundColor = [[UIColor whiteColor] colorWithAlphaComponent:0];

    
    // 5. Render the buttons
    [self renderButtons];
}

#pragma mark - Overridden Setters

- (void) setButtonTitles:(NSArray *)buttonTitles
{
    _buttonTitles = buttonTitles;
    [self renderButtons];
}

- (void) setCancelButtonIndex:(NSInteger)cancelButtonIndex
{
    //
    //  Prevent the user from setting an invalid
    //  cancel index. We allow for a value of -1
    //  in case the user wants to disable the
    //  cancel button altogether.
    //
    
    if (cancelButtonIndex < -1 || cancelButtonIndex > [_buttonTitles count]-1) {
        return;
    }
    
    //
    //  If the value is valid, set it and refresh
    //
    
    _cancelButtonIndex = cancelButtonIndex;
    [self renderButtons];
    
}

#pragma mark - Render Buttons

- (void) renderButtons
{
    
    //
    //  Resize the contentSize of the wrapper
    //
    
    [_buttonWrapper setContentSize:CGSizeMake(_buttonView.frame.size.width, [_buttonTitles count] * [self buttonHeight])];
    
    //  Empty existing buttons
    for (id button in _buttons) {
        [button removeFromSuperview];
    }
    
    //  Render new buttons
    for (NSUInteger i = 0; i < [_buttonTitles count]; i++) {
        
        NSString *title = _buttonTitles[i];
        
        //
        //  Add the buttons to the UI
        //
        
        UIButton *button = [self buttonWithTitle:title forIndex:i];
        
        
        UILabel *label1 = [[UILabel alloc]initWithFrame: CGRectMake(button.frame.origin.x,button.frame.origin.y+button.frame.size.height,button.frame.size.width,10)];
        
        if (i == _cancelButtonIndex-1) {
            [label1 setFrame: CGRectMake(button.frame.origin.x,button.frame.origin.y+button.frame.size.height,button.frame.size.width,8)];
        }else if(i < _cancelButtonIndex-1)
        {
        
            [label1 setFrame: CGRectMake(button.frame.origin.x,button.frame.origin.y+button.frame.size.height,button.frame.size.width,3)];
        }
        
        [label1 setBackgroundColor:[UIColor redColor]]; //可以去掉背景色
        
        [_buttonWrapper addSubview:button];
        
        //
        //  Handle touches in the app
        //
        
        [button addTarget:self action:@selector(buttonTapped:) forControlEvents:UIControlEventTouchUpInside];
        
    }
}

#pragma mark - Button Event Handling

- (void) buttonTapped:(UIButton *)sender
{
    NSUInteger index = -1;
    
    //
    //  Find the appropriate index
    //
    
    for (NSUInteger i = 0; i < [_buttonTitles count]; i++) {
        if ([[sender titleForState:UIControlStateNormal] isEqualToString:_buttonTitles[i]]) {
            index = i;
            break;
        }
    }
    
    //
    //  If the cancel button was tapped, call the cancel method
    //
    
    if (index == _cancelButtonIndex) {
        
        if([[self delegate] respondsToSelector:@selector(buttonMenuViewControllerDidCancel:)]){
            [[self delegate] buttonMenuViewControllerDidCancel:self];
        }
        
        return;
    }
    
    //
    //  If the delegate is wired up, fire a call to the delegate
    //
    
    if([[self delegate] respondsToSelector:@selector(buttonMenuViewController:buttonTappedAtIndex:)]){
        [[self delegate] buttonMenuViewController:self buttonTappedAtIndex:index];
    }
}

#pragma mark - Button Metrics

//
//  Returns a button for a given title and index
//

- (UIButton *) buttonWithTitle:(NSString *)title forIndex:(NSUInteger)index
{
    
    UIButton *button = [UIButton buttonWithType:UIButtonTypeCustom];
    
    
    
    //
    //  Resize the button
    //
    
    CGFloat buttonHeight = [self buttonHeight];
    CGFloat buttonWidth = [self buttonWidth];
    
    CGRect buttonFrame = CGRectMake(0, (index * buttonHeight) + [self verticalSpacingBetweenButtons], buttonWidth, buttonHeight);
    //xls
    if (index == _cancelButtonIndex-1) {
        buttonFrame.size.height -= 6;
     }else if(index < _cancelButtonIndex-1)
     {
         buttonFrame.size.height -= 0.5;
     }
    [button setFrame:buttonFrame];
    
    //
    //  Apply autoresizing masks
    //
    
    [button setAutoresizingMask:(UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleRightMargin)];
    
    //
    //  Apply the title
    //
    
    [button setTitle:title forState:UIControlStateNormal];
    
    //
    //  Style the button
    //
    
//    if (index == _cancelButtonIndex) {
//        [button setTitleColor:[self backgroundColor] forState:UIControlStateNormal];
//        [button setBackgroundColor:[UIColor colorWithWhite:1.0 alpha:0.5]];
//    }
//    else{
        [button setBackgroundColor:[UIColor colorWithWhite:1.0 alpha:1.0]];
        [button setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
        [button setTitleColor:[UIColor blackColor] forState:UIControlStateHighlighted];
//        button.layer.borderColor = [[UIColor colorWithRed:204/255.0 green:204/255.0 blue:204/255.0 alpha:1] CGColor];
////        [button setBackgroundImage:[button buttonImageFromColor:[UIColor colorWithRed:235/255.0 green:235/255.0 blue:235/255.0 alpha:1]] forState:UIControlStateHighlighted];
//        
//        CGRect rect = CGRectMake(0, 0, button.frame.size.width, button.frame.size.height);
//        UIGraphicsBeginImageContext(rect.size);
//        CGContextRef context = UIGraphicsGetCurrentContext();
//        UIColor* color= [UIColor colorWithRed:235/255.0 green:235/255.0 blue:235/255.0 alpha:1];
//        
//        CGContextSetFillColorWithColor(context, [color CGColor]);
//        CGContextFillRect(context, rect);
//        UIImage *img = UIGraphicsGetImageFromCurrentImageContext();
//        UIGraphicsEndImageContext();
//        [button setBackgroundImage:img forState:UIControlStateHighlighted];// setBackgroundImage:img];
        
    //}
    
    //[button setTitleColor:[UIColor darkGrayColor] forState:UIControlStateHighlighted];
    
    //
    //  Return
    //
    
    return button;
    
}

//
//  Returns the height of the buttons
//

-(CGFloat) buttonHeight
{
    CGFloat calculatedHeight = [_buttonView frame].size.height / (CGFloat)[_buttonTitles count];
    return MAX(calculatedHeight, _minimumButtonHeight);
}

//
//  Returns the width of the buttons
//

- (CGFloat) buttonWidth
{
    return [_buttonWrapper contentSize].width;
}

//
//  Returns the space between the buttons
//

- (CGFloat) verticalSpacingBetweenButtons
{
    CGFloat space = 0;
    
    //  If there enough buttons to scroll, show no space
    if ([_buttonTitles count] * [self buttonHeight] > [_buttonWrapper contentSize].height) {
        return space;
    }
    
    space =  [_buttonWrapper contentSize].height - ([_buttonTitles count] * [self buttonHeight]);
    space /= (CGFloat)[_buttonTitles count];
    
    return space/2;
}

#pragma mark - Presentation

- (void) showInView:(UIView*)view
{
    //  1. Hide the modal
    [[self modalView] setAlpha:0];
    
    //  2. Install the modal view
    //[[view superview] addSubview:[self view]];
    [view addSubview:[self view]];
    _shrunkView = view;
    [[self view] setFrame:[[[self view] superview] bounds]];
    //  3. Show the buttons
    [[self buttonView] setTransform:CGAffineTransformMakeTranslation(0, [[self buttonView] frame].size.height)];
    //  4. Animate everything into place
    [UIView
     animateWithDuration:0.3
     animations:^{
         //  Shrink the main view by 15 percent
//         CGAffineTransform t = CGAffineTransformScale(CGAffineTransformIdentity, .9, .9);
//         [view setTransform:t];
         //  Fade in the modal
         [[self modalView] setAlpha:0.7];
         //  Slide the buttons into place
         [[self buttonView] setTransform:CGAffineTransformIdentity];
         
     }
     completion:^(BOOL finished) {
         _visible = YES;
     }];
    
}

- (void) hide
{
    
    //  2. Animate everything out of place
    [UIView animateWithDuration:0.3 animations:^{
         
         //  Shrink the main view by 15 percent
         CGAffineTransform t = CGAffineTransformIdentity;
         [_shrunkView setTransform:t];
         
         //  Fade in the modal
         [[self modalView] setAlpha:0.0];
         
         //  Slide the buttons into place
         
         t = CGAffineTransformTranslate(t, 0, [[self buttonView] frame].size.height);
         [[self buttonView] setTransform:t];
         
     }
     
     completion:^(BOOL finished) {
         [[self view] removeFromSuperview];
         _visible = NO;
     }];
    
}

#pragma mark - Memory Management

//
//
//

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
