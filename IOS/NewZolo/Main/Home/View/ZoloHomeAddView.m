//
//  ZoloHomeAddView.m
//  NewZolo
//
//  Created by JTalking on 2022/7/7.
//

#import "ZoloHomeAddView.h"
#import "ZoloHomeAddCell.h"

@interface ZoloHomeAddView () <UITableViewDelegate, UITableViewDataSource>

@property (weak, nonatomic) IBOutlet UITableView *tableView;
@property (nonatomic, strong) NSArray *titleArray;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *tableWidth;

@end

@implementation ZoloHomeAddView


- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        self = [[[NSBundle mainBundle] loadNibNamed:@"ZoloHomeAddView" owner:self options:nil] firstObject];
        self.frame = CGRectMake(0, 0, KScreenWidth, KScreenheight);
        
        NSString *language = [[NSUserDefaults standardUserDefaults] stringForKey:@"ospn_language"];
        int width = 156.0;
        if ([language isEqualToString:@"1"] || [language isEqualToString:@"2"]) {
            width = 180.0;
        } else if ([language isEqualToString:@"8"] || [language isEqualToString:@"3"] || [language isEqualToString:@"7"]) {
            width = 210.0;
        } else if ([language isEqualToString:@"0"] || [language isEqualToString:@"6"] || [language isEqualToString:@"4"]) {
            width = 146.0;
        } else if ([language isEqualToString:@"5"]) {
            width = 250.0;
        } else {
            width = 220.0;
        }
        self.tableWidth.constant = width;
        
        [self.tableView registerNib:[UINib nibWithNibName:@"ZoloHomeAddCell" bundle:nil] forCellReuseIdentifier:@"ZoloHomeAddCell"];
        self.titleArray = @[LocalizedString(@"SendGroupChat"), LocalizedString(@"AddFriend"), LocalizedString(@"Scan")];
        self.tableView.delegate = self;
        self.tableView.dataSource = self;
        self.tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
        [self.tableView reloadData];
    }
    return self;
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.titleArray.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ZoloHomeAddCell *cell = [tableView dequeueReusableCellWithIdentifier:@"ZoloHomeAddCell"];
    cell.img.image = [UIImage imageNamed:[NSString stringWithFormat:@"home%ld", indexPath.row + 1]];
    cell.nameLabel.text = self.titleArray[indexPath.row];
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    if (_addViewBlock) {
        _addViewBlock(indexPath.row);
    }
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 0.001;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 0.001;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 44;
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    [self hiddleAddView];
}

- (void)hiddleAddView {
    if (_addViewBlock) {
        _addViewBlock(4);
    }
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

@end
