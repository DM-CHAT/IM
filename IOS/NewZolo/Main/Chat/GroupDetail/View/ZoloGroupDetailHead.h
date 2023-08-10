//
//  ZoloGroupDetailHead.h
//  NewZolo
//
//  Created by JTalking on 2022/8/12.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^FriendAddBlock)(void);
typedef void(^FriendLessBlock)(void);
typedef void(^FriendSeeBlock)(NSInteger row);
typedef void(^FriendSeeMoreBlock)(void);

@interface ZoloGroupDetailHead : UITableViewHeaderFooterView

@property (nonatomic, strong) NSArray *memberData;

@property (nonatomic, copy) FriendLessBlock friendLessBlock;
@property (nonatomic, copy) FriendAddBlock friendAddBlock;
@property (nonatomic, copy) FriendSeeBlock friendSeeBlock;
@property (nonatomic, copy) FriendSeeMoreBlock friendSeeMoreBlock;

- (void)setMemberList:(NSArray *)memberList withIsGroupManager:(BOOL)isManager withIsShowMore:(BOOL)isShowMore group:(DMCCGroupInfo *)info;

@end

NS_ASSUME_NONNULL_END
