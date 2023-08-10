//
//  ZoloAudioView.h
//  NewZolo
//
//  Created by JTalking on 2022/8/10.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^AudioViewBlock)(void);
typedef void(^AudioRecordCompletionBlock)(NSString *audioFileString);

@interface ZoloAudioView : UIView

@property (nonatomic, copy) AudioRecordCompletionBlock audioCompletionBlock;
@property (nonatomic, copy) AudioViewBlock audioViewBlock;

@end

NS_ASSUME_NONNULL_END
