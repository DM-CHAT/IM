#define CNS @"zh-Hans"
#define VI @"vi"
#define EN @"en"
#define Jap @"ja"
#define Kor @"ko"
#define Gem @"de"
#define CNTRA @"zh-Hant"
#define Spa @"es"
#define Tur @"tr"
#define Ids @"id"

#define LANGUAGE_SET @"langeuageSet"
#define LANGUAGE_BASE @"langeuageBase"

#import "language.h"

static NSBundle *_bundle = nil;
static NSString *_language = nil;
static NSString *_baseLanguage = nil;

@interface OSNLanguage()
@end

@implementation OSNLanguage

+(void)initLanguage
{
    _language = [[NSUserDefaults standardUserDefaults]objectForKey:LANGUAGE_SET];
    _baseLanguage = [[NSUserDefaults standardUserDefaults]objectForKey:LANGUAGE_BASE];
    if (!_language){
        _language = CNS;
        _baseLanguage = @"0";
    }
    NSString *path = [[NSBundle mainBundle]pathForResource:_language ofType:@"lproj"];
    _bundle = [NSBundle bundleWithPath:path];
}

+(NSString *)getStringForKey:(NSString *)key withTable:(NSString *)table
{
    if(!_bundle)
        [OSNLanguage initLanguage];
    return NSLocalizedStringFromTableInBundle(key, table, _bundle, @"");
}

+(NSString*)getBaseLanguage{
    return _baseLanguage;
}

+(NSString*)getLanguage{
    return _language;
}

+(void)setLanguage:(NSString *)language
{
    _baseLanguage = language;
    if([language isEqualToString:@"0"]){
        language = CNS;
    }else if([language isEqualToString:@"1"]){
        language = VI;
    }else if([language isEqualToString:@"2"]){
        language = EN;
    }else if([language isEqualToString:@"3"]){
        language = Jap;
    }else if([language isEqualToString:@"4"]){
        language = Kor;
    }else if([language isEqualToString:@"5"]){
        language = Gem;
    }else if([language isEqualToString:@"6"]){
        language = CNTRA;
    }else if([language isEqualToString:@"7"]){
        language = Spa;
    }else if([language isEqualToString:@"8"]){
        language = Tur;
    }else if([language isEqualToString:@"9"]){
        language = Ids;
    }else{
        language = CNS;
    }
    
    if ([language isEqualToString:EN] || [language isEqualToString:CNS] || [language isEqualToString:VI] || [language isEqualToString:Jap] || [language isEqualToString:Kor] || [language isEqualToString:Gem] || [language isEqualToString:CNTRA] || [language isEqualToString:Spa] || [language isEqualToString:Tur] || [language isEqualToString:Ids])
    {
        NSString *path = [[NSBundle mainBundle]pathForResource:language ofType:@"lproj"];
        _bundle = [NSBundle bundleWithPath:path];
    }
    
    _language = language;
    [[NSUserDefaults standardUserDefaults]setObject:_language forKey:LANGUAGE_SET];
    [[NSUserDefaults standardUserDefaults]setObject:_baseLanguage forKey:LANGUAGE_BASE];
    [[NSUserDefaults standardUserDefaults] setValue:_baseLanguage forKey:@"ospn_language"];
    [[NSUserDefaults standardUserDefaults]synchronize];
}

@end
