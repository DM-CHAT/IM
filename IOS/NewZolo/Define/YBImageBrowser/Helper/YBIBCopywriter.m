//
//  YBIBCopywriter.m
//  YBImageBrowserDemo
//
//  Created by 波儿菜 on 2018/9/13.
//  Copyright © 2018年 波儿菜. All rights reserved.
//

#import "YBIBCopywriter.h"

@implementation YBIBCopywriter

#pragma mark - life cycle

+ (instancetype)sharedCopywriter {
    static YBIBCopywriter *copywriter = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        copywriter = [YBIBCopywriter new];
    });
    return copywriter;
}

- (instancetype)init {
    self = [super init];
    if (self) {
        _type = YBIBCopywriterTypeSimplifiedChinese;
        NSArray *appleLanguages = [[NSUserDefaults standardUserDefaults] objectForKey:@"AppleLanguages"];
        if (appleLanguages && appleLanguages.count > 0) {
            NSString *languages = appleLanguages[0];
            if (![languages hasPrefix:@"zh-Hans"]) {
                _type = YBIBCopywriterTypeEnglish;
            }
        }
        
        [self initCopy];
    }
    return self;
}

#pragma mark - private

- (void)initCopy {
    BOOL en = self.type == YBIBCopywriterTypeEnglish;
    
    self.videoIsInvalid = LocalizedString(@"YBImageVideoInvalid");
    self.videoError = LocalizedString(@"YBImageVideoError");
    self.unableToSave = LocalizedString(@"YBImageNoSava");
    self.imageIsInvalid = LocalizedString(@"YBImageImageInvalid");
    self.downloadFailed = LocalizedString(@"YBImageImageLoadFail");
    self.getPhotoAlbumAuthorizationFailed = LocalizedString(@"AlertPhotoNoFind");
    self.saveToPhotoAlbumSuccess = LocalizedString(@"YBImageImageSaved");
    self.saveToPhotoAlbumFailed = LocalizedString(@"AlertOpearFail");
    self.saveToPhotoAlbum = LocalizedString(@"YBImageImageSave");
    self.cancel = LocalizedString(@"Cancel");
}

#pragma mark - public

- (void)setType:(YBIBCopywriterType)type {
    _type = type;
    [self initCopy];
}

@end
