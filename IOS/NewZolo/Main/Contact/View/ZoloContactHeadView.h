//
//  ZoloContactHeadView.h
//  NewZolo
//
//  Created by JTalking on 2022/6/30.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^SearchBlock)(void);
typedef void (^SelectedBlock)(NSInteger index);

@interface ZoloContactHeadView : UIView

@property (weak, nonatomic) IBOutlet UICollectionView *collectionView;
@property (weak, nonatomic) IBOutlet UICollectionViewFlowLayout *flowLayout;
@property (nonatomic, copy) SelectedBlock selectedBlock;
@property (nonatomic, strong) NSArray *data;
@property (nonatomic, copy) SearchBlock searchBlock;
- (void)refreshData;

@end

NS_ASSUME_NONNULL_END
