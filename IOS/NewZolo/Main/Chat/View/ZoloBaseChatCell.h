//
//  ZoloBaseChatCell.h
//  NewZolo
//
//  Created by JTalking on 2022/6/30.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^CellLongBlock)(void);
typedef void(^CellLongIconBlock)(void);
typedef void(^CellTapBlock)(void);
typedef void(^CellIconTapBlock)(void);
typedef void(^CellResendTapBlock)(void);
typedef void(^CellTapUrlBlock)(NSString *url);
typedef void(^CellTapPhoneBlock)(NSString *phone);
typedef void(^CellMulSelectBlock)(void);
typedef void(^CellQuoteTapBlock)(void);

@interface ZoloBaseChatCell : UITableViewCell

@property (nonatomic, strong) DMCCUserInfo *userInfo; // 聊天对方信息
@property (nonatomic, strong) DMCCUserInfo *myInfo;
@property (nonatomic, assign) DMCCConversationType chatType;
@property (nonatomic, strong) DMCCMessage *message;
@property (nonatomic, strong)DMCCConversation *conversation;
// 点击事件回调
@property (nonatomic, strong) UITapGestureRecognizer *tapGes;
@property (nonatomic, strong) UILongPressGestureRecognizer *longGes;
@property (nonatomic, strong) UILongPressGestureRecognizer *longIconGes;
@property (nonatomic, strong) UITapGestureRecognizer *iconTapGes;
@property (nonatomic, strong) UITapGestureRecognizer *resendTapGes;
@property (nonatomic, strong) UITapGestureRecognizer *mulTapGes;
@property (nonatomic, strong) UITapGestureRecognizer *quoteTapGes;
@property (nonatomic, copy) CellLongBlock cellLongBlock;
@property (nonatomic, copy) CellTapBlock cellTapBlock;
@property (nonatomic, copy) CellIconTapBlock cellIconTapBlock;
@property (nonatomic, copy) CellResendTapBlock cellResendTapBlock;
@property (nonatomic, copy) CellLongIconBlock cellLongIconBlock;
@property (nonatomic, copy) CellTapUrlBlock cellTapUrlBlock;
@property (nonatomic, copy) CellTapPhoneBlock cellTapPhoneBlock;
@property (nonatomic, copy) CellMulSelectBlock cellMulSelectBlock;
@property (nonatomic, copy) CellQuoteTapBlock cellQuoteTapBlock;

@property (nonatomic, strong) UILabel *nickNameLabel;
@property (nonatomic, strong) UILabel *msgTimeLabel;
@property (nonatomic, strong) UIImageView *errorImg;

@property (nonatomic, assign) BOOL isMulSelect;

- (void)setChatType:(DMCCConversationType)type myInfo:(DMCCUserInfo *)myInfo userInfo:(DMCCUserInfo *)userInfo withConversation:(DMCCConversation *)conversation;

+ (void)calculateCellHeight:(DMCCMessage *)message chatType:(BOOL)isGroup mulSelect:(BOOL)isMulSelect byMsg:(BOOL)isMySend;

@end

NS_ASSUME_NONNULL_END
