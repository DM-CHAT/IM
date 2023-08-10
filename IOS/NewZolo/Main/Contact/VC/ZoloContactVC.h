//
//  ZoloContactVC.h
//  NewZolo
//
//  Created by JTalking on 2022/6/30.
//

#import "MHParentViewController.h"

NS_ASSUME_NONNULL_BEGIN

@interface ZoloContactVC : MHParentViewController

@property (strong, nonatomic) NSDictionary *allDataSource;/**<排序后的整个数据源*/
@property (strong, nonatomic) NSArray *indexDataSource;/**<索引数据源*/

- (void)sortData;

@end

NS_ASSUME_NONNULL_END
