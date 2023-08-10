//
//  ZoloCardContactHeadView.m
//  NewZolo
//
//  Created by JTalking on 2022/9/2.
//

#import "ZoloCardContactHeadView.h"

@implementation ZoloCardContactHeadView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        MHLoginModel *login = [ZoloInfoManager sharedUserManager].currentInfo;
        if (login.json.MainDapp.length > 0) {
            self.data = @[LocalizedString(@"GroupChat"), LocalizedString(@"SmallProgram")];
        } else {
            self.data = @[LocalizedString(@"GroupChat")];
        }
    }
    return self;
}

- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath {
    return CGSizeMake(KScreenWidth, 100);
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

@end
