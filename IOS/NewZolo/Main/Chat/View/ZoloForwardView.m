//
//  ZoloForwardView.m
//  NewZolo
//
//  Created by JTalking on 2022/8/16.
//

#import "ZoloForwardView.h"
#import "ZoloForwardCell.h"

@interface ZoloForwardView () <UITableViewDelegate, UITableViewDataSource>

@property (weak, nonatomic) IBOutlet UITableView *tableView;
@property (nonatomic, strong) DMCCUserInfo *myInfo;
@property (nonatomic, strong) DMCCMessage *msg;
@property (nonatomic, assign) DMCCConversationType chatType;

@property (nonatomic, assign) BOOL isOwner;
@property (nonatomic, assign) BOOL isSasstion;

@end

@implementation ZoloForwardView

- (void)setMyInfo:(DMCCUserInfo *)myInfo withMessage:(DMCCMessage *)message type:(DMCCConversationType)chatType isOwner:(BOOL)isOwner {
    self.myInfo = myInfo;
    self.msg = message;
    self.chatType = chatType;
    self.isOwner = isOwner;
}

- (void)setSasstion:(BOOL)isSasstion {
    self.isSasstion = isSasstion;
}

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self = [[[NSBundle mainBundle] loadNibNamed:@"ZoloForwardView" owner:self options:nil] firstObject];
        self.frame = CGRectMake(40, 0, KScreenWidth / 2.0, 80);
        [self.tableView registerNib:[UINib nibWithNibName:@"ZoloForwardCell" bundle:nil] forCellReuseIdentifier:@"ZoloForwardCell"];
        self.tableView.delegate = self;
        self.tableView.dataSource = self;
        self.layer.cornerRadius = 8;
        self.layer.masksToBounds = YES;
        self.tableView.backgroundColor = [FontManage MHAlertColor];
    }
    return self;
}

- (void)setTitleArray:(NSArray *)titleArray {
    _titleArray = titleArray;
    
    self.height = titleArray.count * 60;
    [self.tableView reloadData];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.titleArray.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ZoloForwardCell *cell = [tableView dequeueReusableCellWithIdentifier:@"ZoloForwardCell"];
    cell.nameLabel.text = self.titleArray[indexPath.row];
    cell.chatImg.image = [UIImage imageNamed:self.imgArray[indexPath.row]];
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    if (_forwardBlock) {
        _forwardBlock(indexPath.row);
    }
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 0.001;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 0.001;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    if (self.isSasstion) {
        return self.scaleHeight(54);
    }
    if (indexPath.row == 2) {
        if ([self.msg.fromUser isEqualToString:self.myInfo.userId]) {
            // 判断超出2分钟的消息，停止撤回
            long long currentTime = [MHDateTools timeStampWithDate:[MHDateTools getCurrentTimes]];
            if ((currentTime - 0.04 * 3600 * 1000) > self.msg.serverTime) {
                self.height = (self.titleArray.count - 1) * 60;
                return 0.001;
            } else {
                self.height = (self.titleArray.count) * 60;
                return self.scaleHeight(54);
            }
        } else {
            self.height = (self.titleArray.count - 1) * 60;
            return 0.001;
        }
    } else if (indexPath.row == 3) {
        if ([self.msg.fromUser isEqualToString:self.myInfo.userId] && self.chatType == Single_Type) {
            return self.scaleHeight(54);
        } else if (self.chatType == Group_Type && [self.msg.content isKindOfClass:[DMCCTextMessageContent class]]) {
            return self.scaleHeight(54);
        } else {
            if ([self.msg.fromUser isEqualToString:self.myInfo.userId]) {
                // 判断超出2分钟的消息，停止撤回
                long long currentTime = [MHDateTools timeStampWithDate:[MHDateTools getCurrentTimes]];
                if ((currentTime - 0.04 * 3600 * 1000) > self.msg.serverTime) {
                    self.height = (self.titleArray.count - 1) * 60;
                } else {
                    self.height = (self.titleArray.count) * 60;
                }
            } else {
                self.height = (self.titleArray.count - 1) * 60;
            }
            return self.scaleHeight(54);
        }
    } else if (indexPath.row == 5) {
        if (self.chatType == Group_Type) {
            if (self.isOwner) {
                // 判断超出2分钟的消息，停止撤回
                long long currentTime = [MHDateTools timeStampWithDate:[MHDateTools getCurrentTimes]];
                if ((currentTime - 0.04 * 3600 * 1000) > self.msg.serverTime) {
                    self.height = (self.titleArray.count - 1) * 60;
                } else {
                    self.height = (self.titleArray.count) * 60;
                }
                return self.scaleHeight(54);
            } else {
                // 判断超出2分钟的消息，停止撤回
                long long currentTime = [MHDateTools timeStampWithDate:[MHDateTools getCurrentTimes]];
                if ((currentTime - 0.04 * 3600 * 1000) > self.msg.serverTime) {
                    self.height = (self.titleArray.count - 2) * 60;
                } else {
                    self.height = (self.titleArray.count - 2) * 60;
                }
                return 0.001;
            }
        } else {
            return self.scaleHeight(54);
        }
    }
    return self.scaleHeight(54);
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

@end
