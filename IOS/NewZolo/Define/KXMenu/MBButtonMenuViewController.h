//
//  PDButtonMenuViewController.h
//  MBMenuController
//
//
//  Logion 弹出更多菜单
//

#import <UIKit/UIKit.h>

@protocol MBButtonMenuViewControllerDelegate;

@interface MBButtonMenuViewController : UIViewController

@property (nonatomic, readonly, getter = isVisible) BOOL visible;
@property (nonatomic, assign) BOOL shrinksParentView;

@property (nonatomic, strong) UIColor *backgroundColor UI_APPEARANCE_SELECTOR;

@property (nonatomic, strong) NSArray *buttonTitles;
@property (nonatomic, assign) NSInteger cancelButtonIndex;

@property (nonatomic, weak) id<MBButtonMenuViewControllerDelegate> delegate;


- (id)initWithButtonTitles:(NSArray *) buttonTitles;

- (void) showInView:(UIView*)view;
- (void) hide;

@end

@protocol MBButtonMenuViewControllerDelegate <NSObject>

- (void) buttonMenuViewController:(MBButtonMenuViewController *)buttonMenu buttonTappedAtIndex:(NSUInteger)index;
- (void) buttonMenuViewControllerDidCancel:(MBButtonMenuViewController *)buttonMenu;

@end
