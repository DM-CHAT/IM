//
//  ZoloCreateWalletVC.m
//  NewZolo
//
//  Created by MHHY on 2023/5/19.
//

#import "ZoloCreateWalletVC.h"
#import "ZoloInputWordVC.h"
#import "ZoloCreateWordVC.h"
#import "ZoloAcountLoginVC.h"

#import "NSData+MyChange.h"
#import "NSString+MyChange.h"

#import <CommonCrypto/CommonHMAC.h>
#import <CommonCrypto/CommonKeyDerivation.h>

@interface ZoloCreateWalletVC () <UIGestureRecognizerDelegate>

@property (weak, nonatomic) IBOutlet UIButton *ceateBtn;
@property (weak, nonatomic) IBOutlet UIButton *inputBtn;
@property (weak, nonatomic) IBOutlet UIButton *loginBtn;

@property (nonatomic, copy) NSString *loginUrl;

@end

@implementation ZoloCreateWalletVC

- (instancetype)initWithLoginUrl:(NSString *)url {
    if (self = [super init]) {
        self.loginUrl = url;
    }
    return self;
}

-(void)viewDidAppear:(BOOL)animated {
    self.navigationController.interactivePopGestureRecognizer.delegate = self;
}
    
- (BOOL)gestureRecognizerShouldBegin:(UIGestureRecognizer *)gestureRecognizer {
    //YES：允许右滑返回 NO：禁止右滑返回
    return NO;
}


- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self.navigationController setNavigationBarHidden:YES animated:animated];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    
    self.view.backgroundColor = [UIColor blackColor];
    self.ceateBtn.layer.cornerRadius = 15;
    self.inputBtn.layer.cornerRadius = 15;
    self.inputBtn.layer.borderColor = MHColorFromHex(0x747B84).CGColor;
    self.inputBtn.layer.borderWidth = 1;
    self.loginBtn.layer.cornerRadius = 15;
    self.loginBtn.layer.borderColor = MHColorFromHex(0x747B84).CGColor;
    self.loginBtn.layer.borderWidth = 1;
    
    [self.ceateBtn setTitle:LocalizedString(@"CreateBipWord") forState:UIControlStateNormal];
    [self.inputBtn setTitle:LocalizedString(@"InputBipWord") forState:UIControlStateNormal];
    [self.loginBtn setTitle:LocalizedString(@"LoginBipWord") forState:UIControlStateNormal];
    
}

- (IBAction)createWordClick:(id)sender {
    
    NSString *mnemonicString = [self generateMnemonicString:@128 language:@"english"];
    
    [self.navigationController pushViewController:[[ZoloCreateWordVC alloc] initWithWords:mnemonicString loginUrl:self.loginUrl] animated:YES];
    
}

- (IBAction)inputWordClick:(id)sender {
    
    [self.navigationController pushViewController:[[ZoloInputWordVC alloc] initWithLoginUrl:self.loginUrl] animated:YES];
    
}

- (IBAction)loginWordBtnClick:(id)sender {
    
    [self.navigationController pushViewController:[[ZoloAcountLoginVC alloc] initWithLoginUrl:self.loginUrl] animated:YES];
}

#pragma mark -
#pragma mark - Generate Mnemonic String

- (NSString *)generateMnemonicString:(NSNumber *)strlength language:(NSString *)language
{
    //输入长度必须为128、160、192、224、256
    if([strlength integerValue] % 32 != 0)
    {
        [NSException raise:@"Strength must be divisible by 32" format:@"Strength Was: %@",strlength];
    }
    
    //创建比特数组
    NSMutableData *bytes = [NSMutableData dataWithLength:([strlength integerValue]/8)];
    
    //生成随机data
    int status = SecRandomCopyBytes(kSecRandomDefault, bytes.length, bytes.mutableBytes);
    
    //如果生成成功
    if(status == 0)
    {
        NSString *hexString = [bytes my_hexString];

        return [self mnemonicStringFromRandomHexString:hexString language:language];
    }
    else
    {
        [NSException raise:@"Unable to get random data!" format:@"Unable to get random data!"];
    }
    return nil;
}

#pragma mark -
#pragma mark - Generate Mnemonic From Hex String

- (NSString *)mnemonicStringFromRandomHexString:(NSString *)seed language:(NSString *)language
{
    //将16进制转换为NSData
    NSData *seedData = [seed my_dataFromHexString];

    //计算 sha256 哈希
    NSMutableData *hash = [NSMutableData dataWithLength:CC_SHA256_DIGEST_LENGTH];
    CC_SHA256(seedData.bytes, (int)seedData.length, hash.mutableBytes);


    NSMutableArray *checkSumBits = [NSMutableArray arrayWithArray:[[NSData dataWithData:hash] my_hexToBitArray]];

    NSMutableArray *seedBits = [NSMutableArray arrayWithArray:[seedData my_hexToBitArray]];

    for(int i = 0 ; i < (int)seedBits.count / 32 ; i++)
    {
        [seedBits addObject:checkSumBits[i]];
    }

    NSString *path = [NSString stringWithFormat:@"%@/%@.txt",[[NSBundle mainBundle] bundlePath], language];
    NSString *fileText = [NSString stringWithContentsOfFile:path encoding:NSUTF8StringEncoding error:NULL];
    NSArray *lines = [fileText componentsSeparatedByCharactersInSet:[NSCharacterSet newlineCharacterSet]];
    

    NSMutableArray *words = [NSMutableArray arrayWithCapacity:(int)seedBits.count / 11];
    
    for(int i = 0 ; i < (int)seedBits.count / 11 ; i++)
    {
        NSUInteger wordNumber = strtol([[[seedBits subarrayWithRange:NSMakeRange(i * 11, 11)] componentsJoinedByString:@""] UTF8String], NULL, 2);

        [words addObject:lines[wordNumber]];
    }

    return [words componentsJoinedByString:@" "];
    
}

- (IBAction)backBtnClick:(id)sender {
    [self.navigationController popToRootViewControllerAnimated:YES];
}


/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
