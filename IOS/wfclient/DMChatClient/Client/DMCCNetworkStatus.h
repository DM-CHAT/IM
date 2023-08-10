//
//  DMCCNetworkStatus.h
//  DMChatClient
//
//  Created by heavyrain on 2017/11/5.
//  Copyright © 2017年 WildFireChat. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <SystemConfiguration/SCNetworkReachability.h>

@protocol DMCCNetworkStatusDelegate

-(void) ReachabilityChange:(UInt32)uiFlags;

@end

@interface DMCCNetworkStatus : NSObject {
	__unsafe_unretained id<DMCCNetworkStatusDelegate> m_delDMCNetworkStatus;
}

+ (DMCCNetworkStatus*)sharedInstance;

-(void) Start:(__unsafe_unretained id<DMCCNetworkStatusDelegate>)delDMCNetworkStatus;
-(void) Stop;
-(void) ChangeReach;
- (SCNetworkConnectionFlags)connFlags;
- (bool)isConnectionAvaible;
@end
