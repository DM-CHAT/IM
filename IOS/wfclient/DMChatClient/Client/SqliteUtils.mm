
#import <Foundation/Foundation.h>
#import "SqliteUtils.h"
#import "sqlite3.h"
#import "DMCCUtilities.h"

@implementation SqliteUtils

sqlite3 *db = nil;
NSObject* lock = [NSObject new];
int tMessageID = 1;

NSString *DBVersion = @"13";

+ (void) initDB:(NSString*) name{
    if(db != nil)
        return;
    NSLog(@"initDB name: %@", name);
    NSString* dbpath=[[NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) lastObject]stringByAppendingPathComponent:name];
    sqlite3_open_v2(dbpath.UTF8String, &db, SQLITE_OPEN_READWRITE|SQLITE_OPEN_FULLMUTEX|SQLITE_OPEN_CREATE, NULL);
    
    NSString* sql = @"create table if not exists t_user(_id integer primary key autoincrement, "
            @"osnID char(128) UNIQUE, "
            @"name nvarchar(20), "
            @"portrait text, "
            @"displayName nvarchar(20), "
            @"urlSpace text, "
			@"nft text, "
            @"describes text,"
            @"tagID integer,"
            @"ugID integer,"
			@"payState int)";
    char *error = 0;
    sqlite3_exec(db, sql.UTF8String,NULL,NULL,&error);
    sql = @"create table if not exists t_friend(_id integer primary key autoincrement, "
            @"osnID char(128) , "
            @"friendID char(128) UNIQUE, "
            @"remarks char(128) , "
            @"state tinyint default 0)";
    sqlite3_exec(db, sql.UTF8String,NULL,NULL,&error);
    sql = @"create table if not exists t_friendRequest(_id integer primary key autoincrement, "
            @"type tinyint , "
            @"direction tinyint , "
            @"target char(128) , "
            @"originalUser char(128) , "
            @"userID char(128) , "
            @"reason char(128) , "
			@"invitation char(1024), "
            @"status tinyint , "
            @"readStatus tinyint , "
            @"timestamp long)";
    sqlite3_exec(db, sql.UTF8String,NULL,NULL,&error);
    sql = @"create table if not exists t_message(_id integer primary key autoincrement, "
            @"mid integer, "
            @"osnID char(128), "
            @"cType tinyint, "
            @"target char(128), "
            @"dir tinyint, "
            @"state tinyint default 0,"
            @"uid integer, "
            @"timestamp integer, "
            @"msgType tinyint default 0,"
            @"msgText text, "
            @"msgHasho text, "
            @"hashIndex char(255) UNIQUE, "
			@"msgHash text)";
    sqlite3_exec(db, sql.UTF8String,NULL,NULL,&error);
    sql = @"create table if not exists t_conversation(_id integer primary key autoincrement, "
            @"type tinyint, "
            @"target char(128), "
            @"line tinyint,"
            @"timestamp integer,"
            @"draft text,"
            @"unreadCount int default 0,"
            @"unreadMention int default 0,"
            @"unreadMentionAll int default 0,"
            @"isTop tinyint,"
            @"isSilent tinyint ,"
            @"tagID integer,"
            @"isMember integer,"
            @"unique(target,line) on conflict replace)";
    sqlite3_exec(db, sql.UTF8String,NULL,NULL,&error);
    sql = @"create table if not exists t_group(_id integer primary key autoincrement, "
            @"groupID char(128) unique, "
            @"name char(20), "
            @"portrait char(128), "
            @"owner char(128), "
            @"type tinyint, "
            @"memberCount int, "
            @"extra text, "
            @"updateDt long, "
            @"fav int default 0, "
			@"redPacket int default 0, "
			@"notice char(255), "
            @"mute tinyint default 0, "
            @"attribute text, "
            @"joinType tinyint default 0, "
            @"passType tinyint default 0, "
            @"privateChat tinyint default 0, "
            @"maxMemberCount long default 200, "
            @"tagID integer,"
            @"ugID integer,"
            @"showAlias tinyint default 0)";
    sqlite3_exec(db, sql.UTF8String,NULL,NULL,&error);
    sql = @"create table if not exists t_groupMember(_id integer primary key autoincrement, "
            @"groupID char(128), "
            @"memberID char(128), "
            @"alias char(128), "
            @"type tinyint default 0, "
			@"mute tinyint default 0, "
            @"updateDt long, "
            @"memberIndex int, "
            @"createDt long, UNIQUE(groupID, memberID) ON CONFLICT REPLACE)";
    sqlite3_exec(db, sql.UTF8String,NULL,NULL,&error);
    sql = @"create table if not exists t_litapp(_id integer primary key autoincrement, "
            @"target char(128) UNIQUE, "
            @"name char(64), "
            @"displayName char(64), "
            @"portrait varchar(512), "
            @"theme varchar(512), "
            @"url varchar(512), "
            @"sid integer,"
            @"param text,"
            @"info text)";
    sqlite3_exec(db, sql.UTF8String,NULL,NULL,&error);
	sql = @"CREATE TABLE IF NOT EXISTS t_redPacket "
            @"(_id integer primary key autoincrement, "
            @"packetID varchar(128) NOT NULL,"
            @"type varchar(32) NOT NULL,"
            @"user varchar(255) NOT NULL,"
            @"count varchar(32) NOT NULL,"
            @"price varchar(32) NOT NULL,"
            @"target varchar(255) NOT NULL,"
            @"text varchar(255) NOT NULL,"
            @"urlQuery varchar(511) NOT NULL,"
            @"urlFetch varchar(511) NOT NULL,"
            @"luckNum varchar(32) NOT NULL,"
            @"timestamp bigint,"
            @"state int,"
            @"wallet text,"
            @"coinType text,"
            @"unpackID varchar(128) NOT NULL,"
            @"unique (packetID,unpackID))";
	sqlite3_exec(db, sql.UTF8String,NULL,NULL,&error);
    sql = @"CREATE TABLE IF NOT EXISTS t_unpack_info "
            @"(_id integer primary key autoincrement, "
            @"user varchar(255) NOT NULL,"
            @"fetcher varchar(255) NOT NULL,"
            @"packetID varchar(128) NOT NULL,"
            @"unpackID varchar(128) NOT NULL,"
            @"price varchar(32) NOT NULL,"
            @"timestamp bigint,"
            @"unique (fetcher,unpackID))";
     sqlite3_exec(db, sql.UTF8String,NULL,NULL,&error);
    
    sql = @"CREATE TABLE IF NOT EXISTS t_tag_info "
            @"(_id integer primary key autoincrement, "
            @"tagID integer UNIQUE,"
            @"tagName text)";
     sqlite3_exec(db, sql.UTF8String,NULL,NULL,&error);
    
    sql = @"CREATE TABLE IF NOT EXISTS t_wallet "
            @"(_id integer primary key autoincrement, "
            @"osnID char(128) UNIQUE, "
            @"name char(128), "
            @"url varchar(512), "
            @"wallect text)";
     sqlite3_exec(db, sql.UTF8String,NULL,NULL,&error);
    
    sql = @"create table if not exists t_collet(_id integer primary key autoincrement, "
            @"osnID char(128) UNIQUE, "
            @"name char(64), "
            @"type int,"
            @"content text)";
    sqlite3_exec(db, sql.UTF8String,NULL,NULL,&error);
    
    [SqliteUtils updateTable];

    sqlite3_stmt *stmt = 0;
    sql = @"select mid from t_message order by mid DESC limit 1";
    if(sqlite3_prepare_v2(db, sql.UTF8String,-1, &stmt,nil) == SQLITE_OK) {
        while(sqlite3_step(stmt) == SQLITE_ROW) {
            tMessageID = sqlite3_column_int(stmt,0) + 1;
            break;
        }
    }
    sqlite3_finalize(stmt);
    NSLog(@"%d",tMessageID);
}

// 升级数据库
+ (void)updateTable {
    
    if (![DMCCUtilities isCheckVersionDBUpdate:DBVersion]) {
        NSLog(@"===DBVersion=不用更新===");
        return;
    }
    
    NSArray *tableArray = @[@"t_user", @"t_friend", @"t_friendRequest", @"t_message", @"t_conversation", @"t_group", @"t_groupMember", @"t_litapp" , @"t_redPacket", @"t_unpack_info", @"t_tag_info", @"t_wallet", @"t_collet"];
    
    NSArray *tableWordArray = @[@"_id,osnID,name,portrait,displayName,urlSpace,nft,describes,tagID,ugID,payState",
                                @"_id,osnID,friendID,remarks,state",
                                @"_id,type,direction,target,originalUser,userID,reason,invitation,status,readStatus,timestamp",
                                @"_id,mid,osnID,cType,target,dir,state,uid,timestamp,msgType,msgText,msgHasho,hashIndex,msgHash",
                                @"_id,type,target,line,timestamp,draft,unreadCount,unreadMention,unreadMentionAll,isTop,isSilent,tagID",
                                @"_id,groupID,name,portrait,owner,type,memberCount,extra,updateDt,fav,redPacket,notice,mute,attribute,joinType,passType,privateChat,maxMemberCount,tagID,ugID,showAlias",
                                @"_id,groupID,memberID,alias,type,mute,updateDt,createDt",
                                @"_id,target,name,displayName,portrait,theme,url,sid,param,info" ,
                                @"_id,packetID,type,user,count,price,target,text,urlQuery,urlFetch,luckNum,timestamp,state,wallet,coinType,unpackID",
                                @"_id,user,fetcher,packetID,unpackID,price,timestamp",
                                @"_id,tagID,tagName",
                                @"_id,osnID,name,url,wallect",
                                @"_id,osnID,name,type,content"];
    
    for (int i = 0; i < tableArray.count; i++) {
        
        NSString *tableName = tableArray[i];
        
        // 获取旧表的列数据
        NSArray * old_columnArr = [SqliteUtils getTableInfo:tableName];
      
        // 新表字段 数组
        NSArray * new_columnArr = [tableWordArray[i] componentsSeparatedByString:@","];
        
        // 空就返回
        if(old_columnArr==nil || new_columnArr==nil)return ;
      
        // 存储要插入的表字段
        NSMutableArray * insertColumn = [NSMutableArray new] ;
      
       // 判断是否需要更新
        BOOL isNew = NO;
        if(old_columnArr.count!=new_columnArr.count)isNew = YES ;
      
        // isHave 判断新表字段是否有在旧表中
        for (NSString * ncol in new_columnArr) {
            NSString * newcol = [ncol stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];
            BOOL isHave = NO ;
            for(int i=0;i<old_columnArr.count;i++){
                NSString * ocol = old_columnArr[i] ;
                if([ocol isEqualToString:newcol])
                {
                    isHave = YES ;
                    break ;
                }
            }
            
            if(isHave){
               [insertColumn addObject:ncol] ;
            }else{
               //没有找到有新增的,用空值填充，后期加入默认值配置和改名后匹配
               [insertColumn addObject:@"''"] ;
               isNew = YES ;
            }
        }
        
        /**
         
         复制数据
         旧表字段  insertColumn
         新表字段  new_columnArr
            
         */
          if(isNew){
              [SqliteUtils beginTransaction];
               //改名
               NSString *tmpTable = [NSString stringWithFormat:@"%@_tmp", tableName] ;
               NSString *sqlALERT =[NSString stringWithFormat:@"alter table %@ rename to %@", tableName,tmpTable];
               [SqliteUtils exec: sqlALERT];
               
               //建表
               [SqliteUtils createTable:tableName];
              
               //复制数据
               NSString * selectSql = [NSString stringWithFormat:@"select %@ from %@",[insertColumn componentsJoinedByString:@","],tmpTable] ;
               NSString * insertSql = [new_columnArr componentsJoinedByString:@","] ;
               NSString * sqlCopy = [NSString stringWithFormat:@"insert into %@ (%@) %@", tableName,insertSql,selectSql];
              [SqliteUtils exec: sqlCopy];
              
              //删表
              NSString *sqlDel = [NSString stringWithFormat:@"DROP TABLE %@ ",tmpTable];
              [SqliteUtils exec: sqlDel];
              [SqliteUtils commitTransaction];
          }
    }
}

+ (void)createTable:(NSString *)tableName {
    if ([tableName isEqualToString:@"t_user"]) {
        NSString* sql = @"create table if not exists t_user(_id integer primary key autoincrement, "
                @"osnID char(128) UNIQUE, "
                @"name nvarchar(20), "
                @"portrait text, "
                @"displayName nvarchar(20), "
                @"urlSpace text, "
                @"nft text, "
                @"describes text,"
                @"tagID integer,"
                @"ugID integer,"
                @"payState int)";
        char *error = 0;
        sqlite3_exec(db, sql.UTF8String,NULL,NULL,&error);
    } else if ([tableName isEqualToString:@"t_friend"]) {
        NSString* sql = @"create table if not exists t_friend(_id integer primary key autoincrement, "
                @"osnID char(128) , "
                @"friendID char(128) UNIQUE, "
                @"remarks char(128) , "
                @"state tinyint default 0)";
        char *error = 0;
        sqlite3_exec(db, sql.UTF8String,NULL,NULL,&error);
    } else if ([tableName isEqualToString:@"t_friendRequest"]) {
        NSString* sql = @"create table if not exists t_friendRequest(_id integer primary key autoincrement, "
                @"type tinyint , "
                @"direction tinyint , "
                @"target char(128) , "
                @"originalUser char(128) , "
                @"userID char(128) , "
                @"reason char(128) , "
                @"invitation char(1024), "
                @"status tinyint , "
                @"readStatus tinyint , "
                @"timestamp long)";
        char *error = 0;
        sqlite3_exec(db, sql.UTF8String,NULL,NULL,&error);
    } else if ([tableName isEqualToString:@"t_message"]) {
        NSString* sql = @"create table if not exists t_message(_id integer primary key autoincrement, "
                @"mid integer, "
                @"osnID char(128), "
                @"cType tinyint, "
                @"target char(128), "
                @"dir tinyint, "
                @"state tinyint default 0,"
                @"uid integer, "
                @"timestamp integer, "
                @"msgType tinyint default 0,"
                @"msgText text, "
                @"msgHasho text, "
                @"hashIndex char(255) UNIQUE, "
                @"msgHash text)";
        char *error = 0;
        sqlite3_exec(db, sql.UTF8String,NULL,NULL,&error);
    } else if ([tableName isEqualToString:@"t_conversation"]) {
        NSString* sql = @"create table if not exists t_conversation(_id integer primary key autoincrement, "
                @"type tinyint, "
                @"target char(128), "
                @"line tinyint,"
                @"timestamp integer,"
                @"draft text,"
                @"unreadCount int default 0,"
                @"unreadMention int default 0,"
                @"unreadMentionAll int default 0,"
                @"isTop tinyint,"
                @"isSilent tinyint ,"
                @"tagID integer,"
                @"isMember integer,"
                @"unique(target,line) on conflict replace)";
        char *error = 0;
        sqlite3_exec(db, sql.UTF8String,NULL,NULL,&error);
    } else if ([tableName isEqualToString:@"t_group"]) {
        NSString* sql = @"create table if not exists t_group(_id integer primary key autoincrement, "
                @"groupID char(128) unique, "
                @"name char(20), "
                @"portrait char(128), "
                @"owner char(128), "
                @"type tinyint, "
                @"memberCount int, "
                @"extra text, "
                @"updateDt long, "
                @"fav int default 0, "
                @"redPacket int default 0, "
                @"notice char(255), "
                @"mute tinyint default 0, "
                @"attribute text, "
                @"joinType tinyint default 0, "
                @"passType tinyint default 0, "
                @"privateChat tinyint default 0, "
                @"maxMemberCount long default 200, "
                @"tagID integer,"
                @"ugID integer,"
                @"showAlias tinyint default 0)";
        char *error = 0;
        sqlite3_exec(db, sql.UTF8String,NULL,NULL,&error);
    } else if ([tableName isEqualToString:@"t_groupMember"]) {
        NSString* sql = @"create table if not exists t_groupMember(_id integer primary key autoincrement, "
                @"groupID char(128), "
                @"memberID char(128), "
                @"alias char(128), "
                @"type tinyint default 0, "
                @"mute tinyint default 0, "
                @"updateDt long, "
                @"memberIndex int, "
                @"createDt long, UNIQUE(groupID, memberID) ON CONFLICT REPLACE)";
        char *error = 0;
        sqlite3_exec(db, sql.UTF8String,NULL,NULL,&error);
    } else if ([tableName isEqualToString:@"t_litapp"]) {
        NSString* sql = @"create table if not exists t_litapp(_id integer primary key autoincrement, "
                @"target char(128) UNIQUE, "
                @"name char(64), "
                @"displayName char(64), "
                @"portrait varchar(512), "
                @"theme varchar(512), "
                @"url varchar(512), "
                @"sid integer,"
                @"param text,"
                @"info text)";
        char *error = 0;
        sqlite3_exec(db, sql.UTF8String,NULL,NULL,&error);
    } else if ([tableName isEqualToString:@"t_redPacket"]) {
        NSString* sql = @"CREATE TABLE IF NOT EXISTS t_redPacket "
                @"(_id integer primary key autoincrement, "
                @"packetID varchar(128) NOT NULL,"
                @"type varchar(32) NOT NULL,"
                @"user varchar(255) NOT NULL,"
                @"count varchar(32) NOT NULL,"
                @"price varchar(32) NOT NULL,"
                @"target varchar(255) NOT NULL,"
                @"text varchar(255) NOT NULL,"
                @"urlQuery varchar(511) NOT NULL,"
                @"urlFetch varchar(511) NOT NULL,"
                @"luckNum varchar(32) NOT NULL,"
                @"timestamp bigint,"
                @"state int,"
                @"wallet text,"
                @"coinType text,"
                @"unpackID varchar(128) NOT NULL,"
                @"unique (packetID,unpackID))";
        char *error = 0;
        sqlite3_exec(db, sql.UTF8String,NULL,NULL,&error);
    } else if ([tableName isEqualToString:@"t_unpack_info"]) {
        NSString* sql = @"CREATE TABLE IF NOT EXISTS t_unpack_info "
                @"(_id integer primary key autoincrement, "
                @"user varchar(255) NOT NULL,"
                @"fetcher varchar(255) NOT NULL,"
                @"packetID varchar(128) NOT NULL,"
                @"unpackID varchar(128) NOT NULL,"
                @"price varchar(32) NOT NULL,"
                @"timestamp bigint,"
                @"unique (fetcher,unpackID))";
        char *error = 0;
         sqlite3_exec(db, sql.UTF8String,NULL,NULL,&error);
    } else if ([tableName isEqualToString:@"t_tag_info"]) {
        NSString* sql = @"CREATE TABLE IF NOT EXISTS t_tag_info "
                @"(_id integer primary key autoincrement, "
                @"tagID integer UNIQUE,"
                @"tagName text)";
        char *error = 0;
         sqlite3_exec(db, sql.UTF8String,NULL,NULL,&error);
    } else if ([tableName isEqualToString:@"t_wallet"]) {
        NSString* sql = @"CREATE TABLE IF NOT EXISTS t_wallet "
                @"(_id integer primary key autoincrement, "
                @"osnID char(128) UNIQUE, "
                @"name char(128), "
                @"url varchar(512), "
                @"wallect text)";
        char *error = 0;
         sqlite3_exec(db, sql.UTF8String,NULL,NULL,&error);
    } else if ([tableName isEqualToString:@"t_collet"]) {
        NSString* sql = @"create table if not exists t_collet(_id integer primary key autoincrement, "
                @"osnID char(128) UNIQUE, "
                @"name char(64), "
                @"type int,"
                @"content text)";
        char *error = 0;
        sqlite3_exec(db, sql.UTF8String,NULL,NULL,&error);
    }
}

+ (void)beginTransaction
{
    char *errmsg;
    sqlite3_exec(db, "BEGIN", NULL, NULL, &errmsg);
}

+ (void)commitTransaction
{
    char *errmsg;
    sqlite3_exec(db, "COMMIT", NULL, NULL, &errmsg);
}

+(NSArray *)getTableInfo:(NSString*)tablename
{
    sqlite3_stmt *statement;
    const char * getColumn = [[NSString stringWithFormat:@"PRAGMA table_info(%@)",tablename] UTF8String];
    sqlite3_prepare_v2(db,getColumn, -1, &statement, nil) ;
    NSMutableArray * columnArr = [NSMutableArray new] ;
    while (sqlite3_step(statement) == SQLITE_ROW) {
        char  * nameData = (char *)sqlite3_column_text(statement, 1);
        NSString *columnName = [[NSString alloc] initWithUTF8String:nameData];
        [columnArr addObject:columnName] ;
    }
    return columnArr ;
}

+ (void) closeDb{
    if(db == nil)
        return;
    sqlite3_close(db);
    db = nil;
}
+ (BOOL) exec:(NSString*) sql{
    @try {
        @synchronized (lock) {
            char *error = 0;
            if (sqlite3_exec(db, sql.UTF8String, NULL, NULL, &error) != SQLITE_OK) {
                NSLog(@"error: %s, sql: %@", error, sql);
                sqlite3_free(error);
                return false;
            }
            return true;
        }
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
    return false;
}
+ (sqlite3_stmt*) query:(NSString*) sql{
        const char *error = 0;
        sqlite3_stmt *stmt = 0;
        //int ret = sqlite3_prepare_v2(db, sql.UTF8String,-1, &stmt, &error);
        int ret = sqlite3_prepare_v2(db, sql.UTF8String,-1, &stmt, 0);
        if(ret != SQLITE_OK) {
            NSLog(@"sqlite3_prepare_v2 error: %s, ret: %d", error, ret);
            //sqlite3_free((void*)error);
            return nil;
        }
        return stmt;
}
+ (NSString*) getText:(sqlite3_stmt*) stmt name:(NSString*)name{
    for(int i = 0; i < sqlite3_column_count(stmt); ++i){
        const char *cname = sqlite3_column_name(stmt, i);
        if([name isEqualToString:@(cname)]){
            const unsigned char *text = sqlite3_column_text(stmt, i);
            if(text == NULL)
                return nil;
            return [NSString stringWithUTF8String:(const char*)text];
        }
    }
    return nil;
}
+ (int) getInt:(sqlite3_stmt*) stmt name:(NSString*)name{
    for(int i = 0; i < sqlite3_column_count(stmt); ++i){
        const char *cname = sqlite3_column_name(stmt, i);
        if([name isEqualToString:@(cname)])
            return sqlite3_column_int(stmt, i);
    }
    return 0;
}
+ (sqlite3_int64) getLong:(sqlite3_stmt*) stmt name:(NSString*)name{
    for(int i = 0; i < sqlite3_column_count(stmt); ++i){
        const char *cname = sqlite3_column_name(stmt, i);
        if([name isEqualToString:@(cname)])
            return sqlite3_column_int64(stmt, i);
    }
    return 0;
}

+ (void) insertWallet:(DMCCWalletInfo*) walletInfo{
    NSString* sql = [NSString stringWithFormat:@"insert or replace into t_wallet(osnID,name,url,wallect) values('%@','%@','%@','%@')",walletInfo.osnID,walletInfo.name,walletInfo.url,walletInfo.wallect];
    [SqliteUtils exec:sql];
}

+ (void) insertCollect:(DMCCCollectInfo*) collectInfo{
    NSString* sql = [NSString stringWithFormat:@"insert or replace into t_collet(osnID,name,type,content) values('%@','%@', '%ld','%@')",collectInfo.osnID,collectInfo.name,collectInfo.type, collectInfo.content];
    [SqliteUtils exec:sql];
}

+ (DMCCWalletInfo*) queryWalletInfoRequest:(NSString*) osnID {
    DMCCWalletInfo* request = nil;
    NSString *sql = [NSString stringWithFormat:@"select * from t_wallet where osnID='%@'", osnID];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    if(sqlite3_step(stmt) == SQLITE_ROW){
        request = [SqliteUtils getWalletInfoRequest:stmt];
    }
    sqlite3_finalize(stmt);
    return request;
}

+ (void)deleteWallet:(NSString*) osnID{
    NSString* sql = [NSString stringWithFormat:@"delete from t_wallet where osnID='%@'",osnID];
    [SqliteUtils exec:sql];
}

+ (DMCCCollectInfo*) queryCollectInfoRequest:(NSString*) osnID {
    DMCCCollectInfo* request = nil;
    NSString *sql = [NSString stringWithFormat:@"select * from t_collet where osnID='%@'", osnID];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    if(sqlite3_step(stmt) == SQLITE_ROW){
        request = [SqliteUtils getColletInfoRequest:stmt];
    }
    sqlite3_finalize(stmt);
    return request;
}

+ (void) insertTag:(DMCCTagInfo*) tagInfo{
    NSString* sql = [NSString stringWithFormat:@"insert or replace into t_tag_info(tagID,tagName) values('%ld','%@')",tagInfo.id,tagInfo.group_name];
    [SqliteUtils exec:sql];
}

+ (void) deleteTag:(DMCCTagInfo*) tagInfo{
    NSString* sql = [NSString stringWithFormat:@"delete from t_tag_info where tagName='%@'",tagInfo.group_name];
    [SqliteUtils exec:sql];
}

+ (void) deleteCollect:(DMCCCollectInfo*) info{
    NSString* sql = [NSString stringWithFormat:@"delete from t_collet where osnID='%@'",info.osnID];
    [SqliteUtils exec:sql];
}

+ (DMCCTagInfo*) queryTagInfoRequest:(NSString*) tagName {
    DMCCTagInfo* request = nil;
    NSString *sql = [NSString stringWithFormat:@"select * from t_tag_info where tagName='%@'", tagName];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    if(sqlite3_step(stmt) == SQLITE_ROW){
        request = [SqliteUtils getTagInfoRequest:stmt];
    }
    sqlite3_finalize(stmt);
    return request;
}

+ (DMCCTagInfo*) queryTagIDInfoRequest:(NSString*) tagID {
    DMCCTagInfo* request = nil;
    NSString *sql = [NSString stringWithFormat:@"select * from t_tag_info where tagID='%@'", tagID];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    if(sqlite3_step(stmt) == SQLITE_ROW){
        request = [SqliteUtils getTagInfoRequest:stmt];
    }
    sqlite3_finalize(stmt);
    return request;
}

+ (void) insertUser:(DMCCUserInfo*) userInfo{
    DMCCUserInfo* oldUserInfo = [SqliteUtils queryUser:userInfo.userId];
    NSInteger tag = -1;
    NSInteger ug = -100;
    if (oldUserInfo) {
        tag = oldUserInfo.tagID;
        ug = oldUserInfo.ugID;
    }
    NSString* sql = [NSString stringWithFormat:@"insert or replace into t_user(osnID,name,displayName,portrait,urlSpace,nft, describes,payState,tagID,ugID) values('%@','%@','%@','%@','%@','%@','%@','%d','%ld', '%ld')",userInfo.userId,userInfo.name,userInfo.displayName,userInfo.portrait,userInfo.urlSpace,userInfo.nft,userInfo.describes,userInfo.payState, tag, ug];
    [SqliteUtils exec:sql];
}
+ (void) updateUser:(DMCCUserInfo*) userInfo{
    NSString* sql = [NSString stringWithFormat:@"update t_user set displayName='%@',portrait='%@' where osnID='%@'",userInfo.displayName,userInfo.portrait,userInfo.userId];
    [SqliteUtils exec:sql];
}
+ (void) updateUser:(DMCCUserInfo*) userInfo keys:(NSArray<NSString*>*) keys{
    NSString* sql = nil;
    for(NSString* k in keys) {
        if([k isEqualToString:@"displayName"])
            sql = [NSString stringWithFormat:@"update t_user set displayName='%@' where osnID='%@'",userInfo.displayName,userInfo.userId];
        else if([k isEqualToString:@"portrait"])
            sql = [NSString stringWithFormat:@"update t_user set portrait='%@' where osnID='%@'",userInfo.portrait,userInfo.userId];
        else if([k isEqualToString:@"urlSpace"])
            sql = [NSString stringWithFormat:@"update t_user set urlSpace='%@' where osnID='%@'",userInfo.urlSpace,userInfo.userId];
        else if([k isEqualToString:@"nft"])
            sql = [NSString stringWithFormat:@"update t_user set nft='%@' where osnID='%@'",userInfo.nft,userInfo.userId];
        else if([k isEqualToString:@"payState"])
            sql = [NSString stringWithFormat:@"update t_user set payState='%d' where osnID='%@'",userInfo.payState,userInfo.userId];
        else if([k isEqualToString:@"describes"])
            sql = [NSString stringWithFormat:@"update t_user set describes='%@' where osnID='%@'",userInfo.describes,userInfo.userId];
        else if([k isEqualToString:@"tagID"])
            sql = [NSString stringWithFormat:@"update t_user set tagID='%ld' where osnID='%@'",userInfo.tagID,userInfo.userId];
        else if([k isEqualToString:@"ugID"])
            sql = [NSString stringWithFormat:@"update t_user set ugID='%ld' where osnID='%@'",userInfo.ugID,userInfo.userId];
    }
    [SqliteUtils exec:sql];
}
+ (DMCCUserInfo*) queryUser:(NSString*) userID{
    DMCCUserInfo* userInfo = nil;
    NSString *sql = [NSString stringWithFormat:@"select (select remarks from t_friend where friendID='%@') as remarks,* from t_user where osnID='%@'", userID, userID];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    if(sqlite3_step(stmt) == SQLITE_ROW){
        userInfo = [SqliteUtils getUserInfo:stmt];
    }
    sqlite3_finalize(stmt);
    return userInfo;
}
+ (NSArray<DMCCUserInfo*>*) queryUsers:(NSString*) keyword{
    NSMutableArray<DMCCUserInfo*>* userInfos = [NSMutableArray new];
    NSString *sql = [NSString stringWithFormat:@"select a.* from (select u.* from t_friend as f left join t_user as u on f.friendID = u.osnID where f.osnID = '%@') as a where a.displayName like '%%%@%%'", [DMCCIMService sharedDMCIMService].getUserID , keyword];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    while(sqlite3_step(stmt) == SQLITE_ROW){
        [userInfos addObject:[SqliteUtils getUserInfo:stmt]];
    }
    sqlite3_finalize(stmt);
    return userInfos;
}

 
+ (NSArray<DMCCGroupInfo*>*) queryGroups:(NSString*) keyword{
    NSMutableArray<DMCCGroupInfo*>* groupInfos = [NSMutableArray new];
    NSString *sql = [NSString stringWithFormat:@"select * from t_group where name like '%%%@%%'", keyword];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    while(sqlite3_step(stmt) == SQLITE_ROW){
        [groupInfos addObject:[SqliteUtils getGroupInfo:stmt]];
    }
    sqlite3_finalize(stmt);
    return groupInfos;
}

+ (NSArray<DMCCUserInfo*>*) queryUsersWithTagID:(NSInteger)tagID {
    NSMutableArray<DMCCUserInfo*>* userInfos = [NSMutableArray new];
    NSString *sql = [NSString stringWithFormat:@"select * from t_user where tagID = %ld", tagID];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    while(sqlite3_step(stmt) == SQLITE_ROW){
        [userInfos addObject:[SqliteUtils getUserInfo:stmt]];
    }
    sqlite3_finalize(stmt);
    return userInfos;
}

+ (void) insertFriendRequest:(DMCCFriendRequest*) friendRequest{
    NSString* sql = [NSString stringWithFormat:@"insert or replace into t_friendRequest(type,direction,target,originalUser,userID,reason,status,readStatus,timestamp,invitation) values('%d','%d','%@','%@','%@','%@',%d,%d,%lld,'%@')",friendRequest.type,friendRequest.direction,friendRequest.target,friendRequest.originalUser,friendRequest.userID,friendRequest.reason,friendRequest.status,friendRequest.readStatus,friendRequest.timestamp,friendRequest.invitation];
    [SqliteUtils exec:sql];
}
+ (void) updateFriendRequests:(NSArray<DMCCFriendRequest*>*) friendRequestList{
    for(DMCCFriendRequest* request in friendRequestList){
        NSString *sql = [NSString stringWithFormat:@"update t_friendRequest set status=%d, readStatus=%d where target='%@' and userID='%@'",request.status,request.readStatus,request.target,request.userID];
        [SqliteUtils exec:sql];
    }
}
+ (DMCCFriendRequest*) queryFriendRequest:(NSString*) userID{
    DMCCFriendRequest* request = nil;
    NSString *sql = [NSString stringWithFormat:@"select * from t_friendRequest where target='%@'", userID];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    if(sqlite3_step(stmt) == SQLITE_ROW){
        request = [SqliteUtils getFriendRequest:stmt];
    }
    sqlite3_finalize(stmt);
    return request;
}
+ (DMCCFriendRequest*) queryFriendRequest:(NSString*) userID groupID:(NSString*) groupID{
    DMCCFriendRequest* request = nil;
    NSString *sql = [NSString stringWithFormat:@"select * from t_friendRequest where target='%@' and userID='%@'", groupID, userID];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    if(sqlite3_step(stmt) == SQLITE_ROW){
        request = [SqliteUtils getFriendRequest:stmt];
    }
    sqlite3_finalize(stmt);
    return request;
}
+ (NSArray<DMCCFriendRequest*>*) listFriendRequest{
    NSMutableArray<DMCCFriendRequest*>* friendRequestList = [NSMutableArray new];
    NSString *sql = [NSString stringWithFormat:@"select * from t_friendRequest order by timestamp desc"];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    while(sqlite3_step(stmt) == SQLITE_ROW){
        DMCCFriendRequest *request = [SqliteUtils getFriendRequest:stmt];
        [friendRequestList addObject:request];
    }
    sqlite3_finalize(stmt);
    return friendRequestList;
}
+ (NSArray<DMCCFriendRequest*>*) queryUnreadFriendRequest{
    NSMutableArray<DMCCFriendRequest*>* friendRequestList = [NSMutableArray new];
    NSString *sql = [NSString stringWithFormat:@"select * from t_friendRequest where readStatus=0"];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    while(sqlite3_step(stmt) == SQLITE_ROW){
        DMCCFriendRequest *request = [SqliteUtils getFriendRequest:stmt];
        [friendRequestList addObject:request];
    }
    sqlite3_finalize(stmt);
    return friendRequestList;
}

+ (void) deleteFriendRequest:(NSString*) target{
    NSString* sql = [NSString stringWithFormat:@"delete from t_friendRequest where target='%@'",target];
    [SqliteUtils exec:sql];
}

+ (void) insertFriend:(OsnFriendInfo*) friendInfo{
    NSString* sql = [NSString stringWithFormat:@"insert or replace into t_friend(osnID,friendID,state,remarks) values('%@','%@',%d,'%@')",friendInfo.userID,friendInfo.friendID,friendInfo.state,friendInfo.remarks];
    [SqliteUtils exec:sql];
}
+ (void) deleteFriend:(NSString*) friendID{
    NSString* sql = [NSString stringWithFormat:@"delete from t_friend where friendID='%@'",friendID];
    [SqliteUtils exec:sql];
}
+ (void) updateFriend:(OsnFriendInfo*) friendInfo{
    NSString* sql = [NSString stringWithFormat:@"update t_friend set state=%d,remarks='%@' where friendID='%@'",friendInfo.state,friendInfo.remarks,friendInfo.friendID];
    [SqliteUtils exec:sql];
}
+ (void) updateFriend:(OsnFriendInfo*) friendInfo keys:(NSArray<NSString*>*) keys{
    NSString* sql = nil;
    for(NSString* k in keys) {
        if([k isEqualToString:@"state"])
            sql = [NSString stringWithFormat:@"update t_friend set state=%d where friendID='%@'",friendInfo.state,friendInfo.friendID];
        else if([k isEqualToString:@"remarks"])
            sql = [NSString stringWithFormat:@"update t_friend set remarks='%@' where friendID='%@'",friendInfo.remarks,friendInfo.friendID];
        [SqliteUtils exec:sql];
    }
}
+ (NSArray<NSString*>*) listFriends{
    NSMutableArray<NSString*>* userList = [NSMutableArray new];
    NSString *sql = [NSString stringWithFormat:@"select friendID from t_friend"];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    while(sqlite3_step(stmt) == SQLITE_ROW){
        [userList addObject:[SqliteUtils getText:stmt name:@"friendID"]];
    }
    sqlite3_finalize(stmt);
    return userList;
}
+ (NSArray<NSString*>*) listWalletInfos{
    NSMutableArray<NSString*>* infoList = [NSMutableArray new];
    NSString *sql = [NSString stringWithFormat:@"select osnID from t_wallet"];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    while(sqlite3_step(stmt) == SQLITE_ROW){
        [infoList addObject:[SqliteUtils getText:stmt name:@"osnID"]];
    }
    sqlite3_finalize(stmt);
    return infoList;
}
+ (NSArray<NSString*>*) listCollectInfos{
    NSMutableArray<NSString*>* infoList = [NSMutableArray new];
    NSString *sql = [NSString stringWithFormat:@"select osnID from t_collet"];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    while(sqlite3_step(stmt) == SQLITE_ROW){
        [infoList addObject:[SqliteUtils getText:stmt name:@"osnID"]];
    }
    sqlite3_finalize(stmt);
    return infoList;
}

+ (NSArray<NSString*>*) listTagInfos{
    NSMutableArray<NSString*>* tagList = [NSMutableArray new];
    NSString *sql = [NSString stringWithFormat:@"select tagID from t_tag_info"];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    while(sqlite3_step(stmt) == SQLITE_ROW){
        [tagList addObject:[SqliteUtils getText:stmt name:@"tagID"]];
    }
    sqlite3_finalize(stmt);
    return tagList;
}
+ (NSArray<NSString*>*) listFriends:(int) state{
    NSMutableArray<NSString*>* userList = [NSMutableArray new];
    NSString *sql = [NSString stringWithFormat:@"select friendID from t_friend where state=%d", state];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    while(sqlite3_step(stmt) == SQLITE_ROW){
        [userList addObject:[SqliteUtils getText:stmt name:@"friendID"]];
    }
    sqlite3_finalize(stmt);
    return userList;
}
+ (OsnFriendInfo*) queryFriend:(NSString*) friendID{
    OsnFriendInfo *friendInfo = nil;
    NSString *sql = [NSString stringWithFormat:@"select * from t_friend where friendID='%@'", friendID];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    if(sqlite3_step(stmt) == SQLITE_ROW){
        friendInfo = [OsnFriendInfo new];
        friendInfo.userID = [SqliteUtils getText:stmt name:@"userID"];
        friendInfo.friendID = [SqliteUtils getText:stmt name:@"friendID"];
        friendInfo.remarks = [SqliteUtils getText:stmt name:@"remarks"];
        friendInfo.state = [SqliteUtils getInt:stmt name:@"state"];
    }
    sqlite3_finalize(stmt);
    return friendInfo;
}

+ (void) insertConversation:(int) type target:(NSString*) target line:(int) line{
    if (target == nil) {
        NSLog(@"=test2= target null");
        return;
    }
    if ([target isEqualToString:@""]) {
        NSLog(@"=test2= target is space");
        return;
    }
    NSInteger tag = 0;
    if (type == 0) {
        tag = -1;
        DMCCUserInfo* userInfo = [SqliteUtils queryUser:target];
        if (userInfo) {
            tag = userInfo.tagID;
        }
    } else if (type == 1) {
        tag = -2;
        DMCCGroupInfo *groupInfo = [SqliteUtils queryGroup:target];
        if (groupInfo) {
            tag = groupInfo.tagID;
        }
    }
    NSInteger isMmeber = 1;
    NSString* sql = [NSString stringWithFormat:@"insert into t_conversation(type,target,line,timestamp,tagID,isMember) values(%d,'%@',%d,%ld,%ld,%ld)",type,target,line,[OsnUtils getTimeStamp], tag, isMmeber];
    [SqliteUtils exec:sql];
}
+ (void) deleteConversation:(int) type target:(NSString*) target line:(int) line{
    NSString* sql = [NSString stringWithFormat:@"delete from t_conversation where type=%d and target='%@' and line=%d",type,target,line];
    [SqliteUtils exec:sql];
}
+ (DMCCConversationInfo*) queryConversation:(int) type target:(NSString*) target line:(int) line{
    DMCCConversationInfo *conversationInfo = nil;
    NSString *sql = [NSString stringWithFormat:@"select * from t_conversation where type=%d and target='%@' and line=%d",type, target,line];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    if(sqlite3_step(stmt) == SQLITE_ROW)
        conversationInfo = [SqliteUtils getConversation:stmt];
    sqlite3_finalize(stmt);
    return conversationInfo;
}

+ (NSArray*) queryConversation:(int) type {
    NSMutableArray<DMCCConversationInfo*>* conversationInfoList = [NSMutableArray new];
    NSString* sql = [NSString stringWithFormat:@"select * from t_conversation where type='%d'",type];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    while(sqlite3_step(stmt) == SQLITE_ROW){
        DMCCConversationInfo* conversationInfo = [SqliteUtils getConversation:stmt];
        [conversationInfoList addObject:conversationInfo];
    }
    sqlite3_finalize(stmt);
    return conversationInfoList;
}

+ (NSArray*) queryConversationWithTagId:(int) tagId {
    NSMutableArray<DMCCConversationInfo*>* conversationInfoList = [NSMutableArray new];
    NSString* sql = [NSString stringWithFormat:@"select * from t_conversation where tagID='%d'",tagId];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    while(sqlite3_step(stmt) == SQLITE_ROW){
        DMCCConversationInfo* conversationInfo = [SqliteUtils getConversation:stmt];
        [conversationInfoList addObject:conversationInfo];
    }
    sqlite3_finalize(stmt);
    return conversationInfoList;
}

+ (void) updateConversation:(DMCCConversationInfo*) conversationInfo{
    NSString *sql = [NSString stringWithFormat:@"update t_conversation set timestamp=%lld, draft='%@', unreadCount=%d, unreadMention=%d, unreadMentionAll=%d, isTop=%d, isSilent=%d where type=%ld and target='%@' and line=%d",conversationInfo.timestamp,conversationInfo.draft==nil?@"":conversationInfo.draft,conversationInfo.unreadCount.unread,conversationInfo.unreadCount.unreadMention,conversationInfo.unreadCount.unreadMentionAll,(conversationInfo.isTop?1:0),(conversationInfo.isSilent?1:0),conversationInfo.conversation.type,conversationInfo.conversation.target,conversationInfo.conversation.line];
    [SqliteUtils exec:sql];
}
+ (void) updateConversation:(DMCCConversationInfo*) conversationInfo keys:(NSArray<NSString*>*) keys{
    for(NSString* k in keys) {
        NSString* sql = nil;
        if([k isEqualToString:@"top"])
            sql = [NSString stringWithFormat:@"update t_conversation set isTop=%d",conversationInfo.isTop?1:0];
        else if([k isEqualToString:@"silent"])
            sql = [NSString stringWithFormat:@"update t_conversation set isSilent=%d",conversationInfo.isSilent?1:0];
        else if([k isEqualToString:@"draft"])
            sql = [NSString stringWithFormat:@"update t_conversation set draft='%@'",conversationInfo.draft == nil ? @"" : conversationInfo.draft];
        else if([k isEqualToString:@"timestamp"])
            sql = [NSString stringWithFormat:@"update t_conversation set timestamp=%lld",conversationInfo.timestamp];
        else if([k isEqualToString:@"tagID"])
            sql = [NSString stringWithFormat:@"update t_conversation set tagID=%ld",conversationInfo.tagID];
        else if([k isEqualToString:@"isMember"])
            sql = [NSString stringWithFormat:@"update t_conversation set isMember=%ld",conversationInfo.isMember];
        
        if(sql == nil)
            continue;
        sql = [sql stringByAppendingFormat:@" where type=%ld and target='%@' and line=%d",
                         conversationInfo.conversation.type,
                         conversationInfo.conversation.target,
                         conversationInfo.conversation.line];
        [SqliteUtils exec:sql];
    }
}
+ (void) clearConversation:(int) type target:(NSString*) target line:(int) line{
    NSString* sql = [NSString stringWithFormat:@"delete from t_conversation where type=%d and target='%@' and line=%d",type,target,line];
    [SqliteUtils exec:sql];
}
+ (void) clearConversationUnread:(int) type target:(NSString*) target line:(int) line{
    NSString* sql = [NSString stringWithFormat:@"update t_conversation set unreadCount=0, unreadMention=0,unreadMentionAll=0 where type=%d and target='%@' and line=%d",type,target,line];
    [SqliteUtils exec:sql];
}
+ (NSArray<DMCCConversationInfo*>*) listConversations:(int) type line:(int) line{
    NSMutableArray<DMCCConversationInfo*>* conversationInfoList = [NSMutableArray new];
    NSString *sql = [NSString stringWithFormat:@"select * from t_conversation where type=%d and line=%d order by timestamp desc", type,line];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    while(sqlite3_step(stmt) == SQLITE_ROW){
        DMCCConversationInfo *info = [SqliteUtils getConversation:stmt];
        [conversationInfoList addObject:info];
    }
    sqlite3_finalize(stmt);
    return conversationInfoList;
}
+ (NSArray<DMCCConversationInfo*>*) listAllConversations:(NSArray<NSNumber*>*) types lines:(int) lines{
    NSMutableArray<DMCCConversationInfo*>* conversationInfoList = [NSMutableArray new];
    NSString *sql = [NSString stringWithFormat:@"select * from t_conversation where "];
    for(int i = 0; i < types.count; ++i){
        if(i > 0)
            sql = [sql stringByAppendingString:@"or "];
        sql = [sql stringByAppendingFormat:@"type=%@ and line=%d ", types[i], 0];
    }
    sql = [sql stringByAppendingString:@"order by timestamp desc"];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    while(sqlite3_step(stmt) == SQLITE_ROW){
        DMCCConversationInfo *info = [SqliteUtils getConversation:stmt];
        [conversationInfoList addObject:info];
    }
    sqlite3_finalize(stmt);
    return conversationInfoList;
}

+ (NSArray<DMCCConversationInfo*>*) tagslistAllConversations:(NSArray<NSNumber*>*) types tags:(NSInteger)tagId lines:(int) lines{
    NSMutableArray<DMCCConversationInfo*>* conversationInfoList = [NSMutableArray new];
    NSString *sql = [NSString stringWithFormat:@"select * from t_conversation where "];
    for(int i = 0; i < types.count; ++i){
        if(i > 0)
            sql = [sql stringByAppendingString:@"or "];
        sql = [sql stringByAppendingFormat:@"type=%@ and line=%d and tagID=%ld ", types[i], 0, tagId];
    }
    sql = [sql stringByAppendingString:@"order by timestamp desc"];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    while(sqlite3_step(stmt) == SQLITE_ROW){
        DMCCConversationInfo *info = [SqliteUtils getConversation:stmt];
        [conversationInfoList addObject:info];
    }
    sqlite3_finalize(stmt);
    return conversationInfoList;
}

+ (long) insertMessage:(DMCCMessage*) msg{
    int type = 0;
    NSMutableDictionary* json = [NSMutableDictionary new];
    if([msg.content isMemberOfClass:[DMCCTextMessageContent class]]){
        type = MESSAGE_CONTENT_TYPE_TEXT;
        DMCCTextMessageContent* textMessageContent = (DMCCTextMessageContent*)msg.content;
        json[@"text"] = textMessageContent.text;
        json[@"mentionedType"] = @(textMessageContent.mentionedType);
        json[@"mentionedTargets"] = textMessageContent.mentionedTargets;
        if (textMessageContent.quoteInfo) {
            json[@"quoteInfo"] = [textMessageContent.quoteInfo encode];
        }
    } else if([msg.content isMemberOfClass:[DMCCFileMessageContent class]]){
        type = MESSAGE_CONTENT_TYPE_FILE;
        DMCCFileMessageContent* fileMessageContent = (DMCCFileMessageContent*)msg.content;
        json[@"localPath"] = fileMessageContent.localPath;
        json[@"remoteUrl"] = fileMessageContent.remoteUrl;
        json[@"name"] = fileMessageContent.name;
        json[@"decKey"] = fileMessageContent.decKey;
        json[@"size"] = @(fileMessageContent.size);
    } else if([msg.content isMemberOfClass:[DMCCSoundMessageContent class]]){
        type = MESSAGE_CONTENT_TYPE_SOUND;
        DMCCSoundMessageContent* soundMessageContent = (DMCCSoundMessageContent*)msg.content;
        json[@"localPath"] = soundMessageContent.localPath;
        json[@"remoteUrl"] = soundMessageContent.remoteUrl;
        json[@"decKey"] = soundMessageContent.decKey;
        json[@"duration"] = @(soundMessageContent.duration);
    } else if([msg.content isMemberOfClass:[DMCCVideoMessageContent class]]){
        type = MESSAGE_CONTENT_TYPE_VIDEO;
        DMCCVideoMessageContent* videoMessageContent = (DMCCVideoMessageContent*)msg.content;
        json[@"localPath"] = videoMessageContent.localPath;
        json[@"remoteUrl"] = videoMessageContent.remoteUrl;
        json[@"decKey"] = videoMessageContent.decKey;
        json[@"name"] = videoMessageContent.name;
    } else if([msg.content isMemberOfClass:[DMCCImageMessageContent class]]){
        type = MESSAGE_CONTENT_TYPE_IMAGE;
        DMCCImageMessageContent* imageMessageContent = (DMCCImageMessageContent*)msg.content;
        json[@"localPath"] = imageMessageContent.localPath;
        json[@"remoteUrl"] = imageMessageContent.remoteUrl;
        json[@"width"] = @(imageMessageContent.size.width);
        json[@"height"] = @(imageMessageContent.size.height);
        json[@"decKey"] = imageMessageContent.decKey;
        json[@"name"] = imageMessageContent.name;
    } else if([msg.content isMemberOfClass:[DMCCStickerMessageContent class]]){
        type = MESSAGE_CONTENT_TYPE_STICKER;
        DMCCStickerMessageContent* stickerMessageContent = (DMCCStickerMessageContent*)msg.content;
        json[@"localPath"] = stickerMessageContent.localPath;
        json[@"remoteUrl"] = stickerMessageContent.remoteUrl;
        json[@"width"] = @(stickerMessageContent.size.width);
        json[@"height"] = @(stickerMessageContent.size.height);
    } else if([msg.content isMemberOfClass:[DMCCCreateGroupNotificationContent class]]){
        type = MESSAGE_CONTENT_TYPE_CREATE_GROUP;
        DMCCCreateGroupNotificationContent* createGroupNotificationContent = (DMCCCreateGroupNotificationContent*)msg.content;
        json[@"creator"] = createGroupNotificationContent.creator;
        json[@"groupName"] = createGroupNotificationContent.groupName;
    } else if ([msg.content isMemberOfClass:[DMCCRecallMessageContent class]]) {
        type = MESSAGE_CONTENT_TYPE_RECALL;
        DMCCRecallMessageContent* recallNotificationContent = (DMCCRecallMessageContent*)msg.content;
        json[@"operatorId"] = recallNotificationContent.operatorId;
    } else if([msg.content isMemberOfClass:[DMCCKickoffGroupMemberNotificationContent class]]){
        type = MESSAGE_CONTENT_TYPE_KICKOF_GROUP_MEMBER;
        DMCCKickoffGroupMemberNotificationContent* kickoffGroupMemberNotificationContent = (DMCCKickoffGroupMemberNotificationContent*)msg.content;
        json[@"operator"] = kickoffGroupMemberNotificationContent.operateUser;
        json[@"members"] = kickoffGroupMemberNotificationContent.kickedMembers;
    } else if([msg.content isMemberOfClass:[DMCCAddGroupeMemberNotificationContent class]]){
        type = MESSAGE_CONTENT_TYPE_ADD_GROUP_MEMBER;
        DMCCAddGroupeMemberNotificationContent* addGroupMemberNotificationContent = (DMCCAddGroupeMemberNotificationContent*)msg.content;
        json[@"invitor"] = addGroupMemberNotificationContent.invitor;
        json[@"invitees"] = addGroupMemberNotificationContent.invitees;
    } else if([msg.content isMemberOfClass:[DMCCQuitGroupNotificationContent class]]){
        type = MESSAGE_CONTENT_TYPE_QUIT_GROUP;
        DMCCQuitGroupNotificationContent* quitGroupNotificationContent = (DMCCQuitGroupNotificationContent*)msg.content;
        json[@"operator"] = quitGroupNotificationContent.quitMember;
    } else if([msg.content isMemberOfClass:[DMCCDismissGroupNotificationContent class]]){
        type = MESSAGE_CONTENT_TYPE_DISMISS_GROUP;
        DMCCDismissGroupNotificationContent* dismissGroupNotificationContent = (DMCCDismissGroupNotificationContent*)msg.content;
        json[@"operator"] = dismissGroupNotificationContent.operateUser;
    } else if([msg.content isMemberOfClass:[DMCCTransferGroupOwnerNotificationContent class]]){
        type = MESSAGE_CONTENT_TYPE_TRANSFER_GROUP_OWNER;
        DMCCTransferGroupOwnerNotificationContent* transferGroupOwnerNotificationContent = (DMCCTransferGroupOwnerNotificationContent*)msg.content;
        json[@"operator"] = transferGroupOwnerNotificationContent.operateUser;
        json[@"newOwner"] = transferGroupOwnerNotificationContent.owner;
    } else if([msg.content isMemberOfClass:[DMCCGroupNotifyContent class]]){
        type = MESSAGE_CONTENT_TYPE_General_Notification;
        DMCCGroupNotifyContent* groupNotifyContent = (DMCCGroupNotifyContent*)msg.content;
        json[@"info"] = groupNotifyContent.info;
    } else if([msg.content isMemberOfClass:[DMCCCardMessageContent class]]){
        type = MESSAGE_CONTENT_TYPE_CARD;
        DMCCCardMessageContent* cardMessageContent = (DMCCCardMessageContent*)msg.content;
        json[@"cardType"] = @(cardMessageContent.type);
        json[@"target"] = cardMessageContent.targetId;
        json[@"name"] = cardMessageContent.name;
        json[@"displayName"] = cardMessageContent.displayName;
        json[@"portrait"] = cardMessageContent.portrait;
        json[@"theme"] = cardMessageContent.theme;
        json[@"url"] = cardMessageContent.url;
    } else if([msg.content isMemberOfClass:[DMCCRedPacketMessageContent class]]){
        type = MESSAGE_CONTENT_TYPE_RED_PACKET;
        DMCCRedPacketMessageContent* redPacket = (DMCCRedPacketMessageContent*)msg.content;
        json[@"id"] = redPacket.ids;
        json[@"text"] = redPacket.text;
        json[@"info"] = redPacket.info;
        json[@"state"] = @(redPacket.state);
    } else if([msg.content isMemberOfClass:[DMCCFriendAddedMessageContent class]]){
        type = MESSAGE_FRIEND_ADDED_NOTIFICATION;
    } else if([msg.content isMemberOfClass:[DMCCFriendGreetingMessageContent class]]){
        type = MESSAGE_FRIEND_GREETING;
    } else if([msg.content isMemberOfClass:[DMCCCallMessageContent class]]){
        type = MESSAGE_CONTENT_TYPE_CALL;
        DMCCCallMessageContent* call = (DMCCCallMessageContent*)msg.content;
        json[@"id"] = @(call.cid);
        json[@"type"] = @(call.type);
        json[@"mode"] = @(call.mode);
        json[@"action"] = @(call.action);
        json[@"duration"] = @(call.duration);
        json[@"status"] = call.status;
        json[@"url"] = call.url;
        json[@"user"] = call.user;
    } else {
        NSLog(@"unknown msg type");
    }
    
    long mid = tMessageID++;
    NSString *data = [OsnUtils dic2Json:json];
    
    NSString *hashIndex = @"";
    if (msg.messageHash.length > 0) {
        hashIndex = msg.messageHash;
    } else {
        NSString *calc = [NSString stringWithFormat:@"%ld%lld%@", mid, msg.serverTime, data];
        hashIndex = [ECUtils osnHash:[calc dataUsingEncoding:NSUTF8StringEncoding]];
    }
    
    NSString* sql = [NSString stringWithFormat:@"insert into t_message(mid,uid,osnID,cType,target,dir,state,timestamp,msgType,msgText,msgHasho,msgHash,hashIndex) values('%ld','%ld','%@','%ld','%@','%ld','%ld','%lld','%d','%@','%@','%@','%@')",mid,mid,msg.fromUser,msg.conversation.type,msg.conversation.target,msg.direction,msg.status,msg.serverTime,type,data,msg.messageHasho,msg.messageHash, hashIndex];
    //NSLog(@"groupNewlyGroup insert hash index : %@", hashIndex);
    if ([SqliteUtils exec:sql]) {
        return mid;
    }
    //NSLog(@"groupNewlyGroup failed mid: %ld, timestamp: %lld",mid,msg.serverTime);
    return -1;
}

+ (BOOL) isMessageExist:(NSString *) hash {
    @try {
        BOOL ret = false;
        NSLog(@"hash: %@",hash);
        NSString* sql = [NSString stringWithFormat:@"select * from t_message where hashIndex='%@'", hash];
        sqlite3_stmt *stmt = [SqliteUtils query:sql];
        if(sqlite3_step(stmt) == SQLITE_ROW){
            ret = true;
        }
        sqlite3_finalize(stmt);
        return ret;
    }
    @catch (NSException* e){
        NSLog(@"%@",e);
    }
    return false;
}

+ (void) deleteMessage:(long) mid{
    NSLog(@"mid: %ld",mid);
    NSString* sql = [NSString stringWithFormat:@"delete from t_message where mid=%ld",mid];
    [SqliteUtils exec:sql];
}
+ (void) clearMessage:(NSString*) target{
    NSString* sql = [NSString stringWithFormat:@"delete from t_message where target='%@'", target];
    [SqliteUtils exec:sql];
}
+ (DMCCMessage*) queryMessage:(long) mid{
    DMCCMessage* message = nil;
    NSString *sql = [NSString stringWithFormat:@"select * from t_message where mid=%ld",mid];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    if(sqlite3_step(stmt) == SQLITE_ROW)
        message = [SqliteUtils getMessage:stmt];
    sqlite3_finalize(stmt);
    return message;
}
+ (DMCCMessage*) queryMessageWithHash:(NSString*) hash{
    DMCCMessage* message = nil;
    NSString *sql = [NSString stringWithFormat:@"select * from t_message where msgHasho='%@'",hash];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    if(sqlite3_step(stmt) == SQLITE_ROW)
        message = [SqliteUtils getMessage:stmt];
    sqlite3_finalize(stmt);
    return message;
}
+ (DMCCMessage*) queryGroupMessageWithHash0:(NSString*) hash{
    DMCCMessage* message = nil;
    NSString *sql = [NSString stringWithFormat:@"select * from t_message where msgHasho='%@'",hash];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    if(sqlite3_step(stmt) == SQLITE_ROW)
        message = [SqliteUtils getMessage:stmt];
    sqlite3_finalize(stmt);
    return message;
}
+ (bool) queryMessage:(long long) timestamp target:(NSString*) target{
    bool hasMsg = false;
    NSString *sql = [NSString stringWithFormat:@"select * from t_message where timestamp=%lld and target='%@'",timestamp,target];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    if(sqlite3_step(stmt) == SQLITE_ROW)
        hasMsg = true;
    sqlite3_finalize(stmt);
    return hasMsg;
}

+ (void) queryDelMessage:(long long) timestamp target:(NSString*) target{
    NSString *sql = [NSString stringWithFormat:@"delete from t_message where timestamp<=%lld and target='%@'",timestamp,target];
    [SqliteUtils exec:sql];
}

+ (NSArray<DMCCMessage*>*) queryMessages:(DMCCConversation*) conversation timestamp:(long long) timestamp before:(bool) before count:(int) count include:(bool) include{
    NSMutableArray<DMCCMessage*>* messageList = [NSMutableArray new];
    NSString *sql = nil;
    if(before){
        sql = [NSString stringWithFormat:@"select * from (select * from t_message where target='%@' and cType=%ld and timestamp%@%lld order by timestamp desc limit %d) tmp order by timestamp desc",conversation.target,conversation.type,include?@"<=":@"<",timestamp,count];
    } else {
        sql = [NSString stringWithFormat:@"select * from (select * from t_message where target='%@' and cType=%ld and timestamp%@%lld order by timestamp asc limit %d) tmp order by timestamp asc",conversation.target,conversation.type,include?@">=":@">",timestamp,count];
    }
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    while(sqlite3_step(stmt) == SQLITE_ROW){
        DMCCMessage *message = [SqliteUtils getMessage:stmt];
        [messageList addObject:message];
    }
    sqlite3_finalize(stmt);
    return messageList;
}
 
// limit 返回数量  offset 起止数
+ (NSArray<DMCCMessage*>*) queryMessages:(DMCCConversation*) conversation keyword:(NSString*) keyword desc:(bool) desc limit:(int) limit offset:(int) offset{
    NSMutableArray<DMCCMessage*>* messageList = [NSMutableArray new];
    NSString* sql = [NSString stringWithFormat:@"select * from t_message where target='%@' and msgText like '%%%@%%' limit %d offset %d",conversation.target,keyword,limit,offset];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    while(sqlite3_step(stmt) == SQLITE_ROW){
        DMCCMessage *message = [SqliteUtils getMessage:stmt];
        [messageList addObject:message];
    }
    sqlite3_finalize(stmt);
    return messageList;
}
+ (void) updateMessage:(long) mid state:(int) state{
    NSString* sql = [NSString stringWithFormat:@"update t_message set state=%d where mid=%ld",state,mid];
    [SqliteUtils exec:sql];
}
+ (void) recallMessage:(DMCCMessage*) message{
    NSString *data;
    NSMutableDictionary* json = [NSMutableDictionary new];
    DMCCRecallMessageContent* content = (DMCCRecallMessageContent*)message.content;
    json[@"operatorId"] = content.operatorId;
    data = [OsnUtils dic2Json:json];
    NSString* sql = [NSString stringWithFormat:@"update t_message set msgText='%@', msgType=%d where mid=%lld",data,MESSAGE_CONTENT_TYPE_RECALL,message.messageUid];
    [SqliteUtils exec:sql];
}

+ (void) updateHash:(DMCCMessage*) message{
    NSString* sql = [NSString stringWithFormat:@"update t_message set msgHash='%@', msgHasho='%@' where mid=%lld",message.messageHash, message.messageHasho,message.messageUid];
    [SqliteUtils exec:sql];
}
+ (void) updateMessage:(long) mid state:(int) state msgHash:(NSString*) msgHash{
    NSString* sql = [NSString stringWithFormat:@"update t_message set state=%d, msgHash='%@', msgHasho='%@' where mid=%ld",state,msgHash,msgHash,mid];
    [SqliteUtils exec:sql];
}
+ (void) updateMessage:(long) mid state:(int) state msgText:(NSString*) text{
    NSString* sql = [NSString stringWithFormat:@"update t_message set state=%d, msgText='%@' where mid=%ld",state,text,mid];
    [SqliteUtils exec:sql];
}

+ (DMCCMessage*) getLastMessage:(DMCCConversation*) conversation{
    DMCCMessage* message = nil;
    NSString* sql = [NSString stringWithFormat:@"select * from t_message where target='%@' and cType=%ld order by timestamp desc limit 1",conversation.target,conversation.type];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    if(sqlite3_step(stmt) == SQLITE_ROW){
        message = [SqliteUtils getMessage:stmt];
    }
    sqlite3_finalize(stmt);
    return message;
}
+ (DMCCMessage*) getLastNotify{
    DMCCMessage* message = nil;
    NSString* sql = [NSString stringWithFormat:@"select * from t_message where cType=5 order by timestamp desc limit 1"];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    if(sqlite3_step(stmt) == SQLITE_ROW){
        message = [SqliteUtils getMessage:stmt];
    }
    sqlite3_finalize(stmt);
    return message;
}

+ (void) insertGroup:(DMCCGroupInfo*) groupInfo{
    DMCCGroupInfo *oldGroupInfo = [SqliteUtils queryGroup:groupInfo.target];
    NSInteger tag = -2;
    NSInteger ug = -200;
    if (oldGroupInfo) {
        tag = oldGroupInfo.tagID;
        ug = oldGroupInfo.ugID;
    }
    NSString* sql = [NSString stringWithFormat:@"insert or replace into t_group(groupID,name,portrait,owner,type,joinType,passType,mute,attribute,memberCount,fav,redPacket,notice, tagID, ugID, showAlias,extra) values('%@','%@','%@','%@',%ld,%d,%d,%d,'%@',%ld,%d,%d,'%@',%ld,%ld, %d,'%@')",groupInfo.target,groupInfo.name,groupInfo.portrait,groupInfo.owner,groupInfo.type,groupInfo.joinType,groupInfo.passType,groupInfo.mute,groupInfo.attribute,groupInfo.memberCount,groupInfo.fav,groupInfo.redPacket,groupInfo.notice, tag, ug, groupInfo.showAlias, groupInfo.extra];
    [SqliteUtils exec:sql];
}
+ (void) deleteGroup:(NSString*) groupID{
    NSString* sql = [NSString stringWithFormat:@"delete from t_group where groupID='%@'",groupID];
    [SqliteUtils exec:sql];
}
+ (void) updateGroup:(DMCCGroupInfo*) groupInfo keys:(NSArray<NSString*>*) keys{
    NSString* sql = nil;
    for(NSString* k in keys) {
        if([k isEqualToString:@"name"])
            sql = [NSString stringWithFormat:@"update t_group set name='%@' ",groupInfo.name];
        else if([k isEqualToString:@"portrait"])
            sql = [NSString stringWithFormat:@"update t_group set portrait='%@' ",groupInfo.portrait];
        else if([k isEqualToString:@"fav"])
            sql = [NSString stringWithFormat:@"update t_group set fav=%d",groupInfo.fav];
        else if([k isEqualToString:@"showAlias"])
            sql = [NSString stringWithFormat:@"update t_group set showAlias=%d",groupInfo.showAlias];
        else if([k isEqualToString:@"redPacket"])
            sql = [NSString stringWithFormat:@"update t_group set redPacket=%d",groupInfo.redPacket];
        else if([k isEqualToString:@"memberCount"])
            sql = [NSString stringWithFormat:@"update t_group set memberCount=%ld",groupInfo.memberCount];
        else if([k isEqualToString:@"type"])
            sql = [NSString stringWithFormat:@"update t_group set type=%ld",groupInfo.type];
        else if([k isEqualToString:@"joinType"])
            sql = [NSString stringWithFormat:@"update t_group set joinType=%d",groupInfo.joinType];
        else if([k isEqualToString:@"passType"])
            sql = [NSString stringWithFormat:@"update t_group set passType=%d",groupInfo.passType];
        else if([k isEqualToString:@"mute"])
            sql = [NSString stringWithFormat:@"update t_group set mute=%d",groupInfo.mute];
        else if([k isEqualToString:@"attribute"]) {
            sql = [NSString stringWithFormat:@"update t_group set attribute='%@'", groupInfo.attribute];
        }
        else if([k isEqualToString:@"extra"]) {
            sql = [NSString stringWithFormat:@"update t_group set extra='%@'", groupInfo.extra];
        }
        else if([k isEqualToString:@"notice"] || [k isEqualToString:@"billboard"])
            sql = [NSString stringWithFormat:@"update t_group set notice='%@'",groupInfo.notice];
        else if([k isEqualToString:@"tagID"])
            sql = [NSString stringWithFormat:@"update t_group set tagID='%ld'",groupInfo.tagID];
        else if([k isEqualToString:@"ugID"])
            sql = [NSString stringWithFormat:@"update t_group set ugID='%ld'",groupInfo.ugID];
        sql = [sql stringByAppendingFormat:@" where groupID='%@'",groupInfo.target];
        [SqliteUtils exec:sql];
    }
}
+ (DMCCGroupInfo*) queryGroup:(NSString*) groupID{
    DMCCGroupInfo* groupInfo = nil;
    NSString* sql = [NSString stringWithFormat:@"select * from t_group where groupID='%@'",groupID];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    if(sqlite3_step(stmt) == SQLITE_ROW){
        groupInfo = [SqliteUtils getGroupInfo:stmt];
    }
    sqlite3_finalize(stmt);
    return groupInfo;
}

+ (NSArray<DMCCGroupInfo*>*) listGroups{
    NSMutableArray<DMCCGroupInfo*>* groupInfoList = [NSMutableArray new];
    NSString* sql = [NSString stringWithFormat:@"select * from t_group"];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    while(sqlite3_step(stmt) == SQLITE_ROW){
        DMCCGroupInfo *info = [SqliteUtils getGroupInfo:stmt];
        [groupInfoList addObject:info];
    }
    sqlite3_finalize(stmt);
    return groupInfoList;
}

+ (NSArray<DMCCGroupInfo*>*) listGroupsWithTagID:(NSInteger)tagId {
    NSMutableArray<DMCCGroupInfo*>* groupInfoList = [NSMutableArray new];
    NSString* sql = [NSString stringWithFormat:@"select * from t_group where tagID='%ld'", tagId];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    while(sqlite3_step(stmt) == SQLITE_ROW){
        DMCCGroupInfo *info = [SqliteUtils getGroupInfo:stmt];
        [groupInfoList addObject:info];
    }
    sqlite3_finalize(stmt);
    return groupInfoList;
}

+ (NSArray<DMCCLitappInfo*>*) listLitapps {
    NSMutableArray<DMCCLitappInfo*>* litappInfos = [NSMutableArray new];
    NSString* sql = [NSString stringWithFormat:@"select * from t_litapp order by _id desc"];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    while(sqlite3_step(stmt) == SQLITE_ROW){
        DMCCLitappInfo *info = [SqliteUtils getLitappInfo:stmt];
        [litappInfos addObject:info];
    }
    sqlite3_finalize(stmt);
    return litappInfos;
}

+ (void) insertLitapp:(DMCCLitappInfo*) litappInfo {
    NSString* sql = [NSString stringWithFormat:@"insert or replace into t_litapp(target,name,displayName,portrait,theme,url,info,sid,param) values('%@','%@','%@','%@','%@','%@','%@','%ld','%@')",litappInfo.target,litappInfo.name,litappInfo.displayName,litappInfo.portrait,litappInfo.theme,litappInfo.url,litappInfo.info,litappInfo.sid, litappInfo.param];
    [SqliteUtils exec:sql];
}

+ (void) deleteLitapp:(NSString*) target{
    NSString* sql = [NSString stringWithFormat:@"delete from t_litapp where target='%@'",target];
    [SqliteUtils exec:sql];
}

+ (DMCCLitappInfo*) queryLitapp:(NSString*) target {
    DMCCLitappInfo* litappInfo = nil;
    NSString* sql = [NSString stringWithFormat:@"select * from t_litapp where target='%@'", target];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    if(sqlite3_step(stmt) == SQLITE_ROW){
        litappInfo = [SqliteUtils getLitappInfo:stmt];
    }
    sqlite3_finalize(stmt);
    return litappInfo;
}

+ (void) insertMembers:(NSArray<DMCCGroupMember*>*) members{
    if (members == nil) {
        return;
    }
    if (members.count>0) {
        int min = members.firstObject.index;
        int max = members.lastObject.index;
        [self deleteMembers:members.firstObject.groupId min:min max:max];
    }
    
    for (DMCCGroupMember* m in members) {
        NSString* sql = [NSString stringWithFormat:@"insert or replace into t_groupMember(groupID,memberID,type,alias,mute,memberIndex) values('%@','%@',%ld,'%@',%d, %d)",m.groupId,m.memberId,m.type,m.alias,m.mute, m.index];
        [SqliteUtils exec:sql];
    }
}

+ (void) deleteMembers:(NSString *) groupId min:(int)min max:(int)max {
    NSString* sql = [NSString stringWithFormat:@"delete from t_groupMember where groupID='%@' and memberIndex>=%d and memberIndex<=%d",groupId,min,max];
    [SqliteUtils exec:sql];
}

+ (void) deleteMembers:(NSString *) groupId index:(int)index {
    NSString* sql = [NSString stringWithFormat:@"delete from t_groupMember where groupID='%@' and memberIndex>=%d",groupId,index];
    [SqliteUtils exec:sql];
}

+ (void) insertMember:(DMCCGroupMember*) m{
    
    DMCCGroupMember* mTemp = [self queryMember:m.groupId memberID:m.memberId];
    if (mTemp != nil) {
        return;
    }
    
    int max = [self getMaxMemberIndex:m.groupId];
    m.index = max + 1;
    
    NSString* sql = [NSString stringWithFormat:@"insert or replace into t_groupMember(groupID,memberID,type,alias,mute,memberIndex) values('%@','%@',%ld,'%@',%d,%d)",m.groupId,m.memberId,m.type,m.alias,m.mute,m.index];
    [SqliteUtils exec:sql];
    
}

+ (int) getMaxMemberIndex:(NSString *) groupID{
    
    DMCCGroupMember* groupMember = nil;
    int max = -1;
    
    NSString* sql = [NSString stringWithFormat:@"select MAX(index) from t_groupMember where groupID='%@'",groupID];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    if(sqlite3_step(stmt) == SQLITE_ROW){
        groupMember = [SqliteUtils getMember:stmt];
        max = groupMember.index;
    }
    sqlite3_finalize(stmt);
    return max;
}

+ (void) deleteMembers:(NSArray<OsnMemberInfo*>*) members{
    for(OsnMemberInfo *m in members) {
        NSString* sql = [NSString stringWithFormat:@"delete from t_groupMember where groupID='%@' and memberID='%@'",m.groupID,m.osnID];
        [SqliteUtils exec:sql];
    }
}
+ (void) updateMember:(DMCCGroupMember*) groupMember keys:(NSArray<NSString*>*) keys{
    NSString* sql = nil;
    for(NSString* k in keys) {
        if([k isEqualToString:@"alias"])
            sql = [NSString stringWithFormat:@"update t_groupMember set alias='%@'",groupMember.alias];
        else if([k isEqualToString:@"type"])
            sql = [NSString stringWithFormat:@"update t_groupMember set type=%ld",groupMember.type];
        else if([k isEqualToString:@"mute"])
            sql = [NSString stringWithFormat:@"update t_groupMember set mute=%d",groupMember.mute];
        sql = [sql stringByAppendingFormat:@" where groupID='%@' and memberID='%@'",groupMember.groupId,groupMember.memberId];
        [SqliteUtils exec:sql];
    }
}
+ (void) clearMembers:(NSString*) groupID{
    NSString* sql = [NSString stringWithFormat:@"delete from t_groupMember where groupID='%@'",groupID];
    [SqliteUtils exec:sql];
}
+ (NSArray<DMCCGroupMember*>*) queryMembers:(NSString*) groupID{
    NSMutableArray<DMCCGroupMember*>* memberList = [NSMutableArray new];
    NSString* sql = [NSString stringWithFormat:@"select * from t_groupMember where groupID='%@' order by type DESC",groupID];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    while(sqlite3_step(stmt) == SQLITE_ROW){
        DMCCGroupMember* member = [SqliteUtils getMember:stmt];
        [memberList addObject:member];
    }
    sqlite3_finalize(stmt);
    return memberList;
}
+ (NSArray<DMCCGroupMember*>*) queryMembersTop:(NSString*) groupID{
    NSMutableArray<DMCCGroupMember*>* memberList = [NSMutableArray new];
    NSString* sql = [NSString stringWithFormat:@"select * from t_groupMember where groupID='%@' order by type DESC limit 26",groupID];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    while(sqlite3_step(stmt) == SQLITE_ROW){
        DMCCGroupMember* member = [SqliteUtils getMember:stmt];
        [memberList addObject:member];
    }
    sqlite3_finalize(stmt);
    return memberList;
}
+ (DMCCGroupMember*) queryMember:(NSString*) groupID memberID:(NSString*) memberID{
    DMCCGroupMember* groupMember = nil;
    NSString* sql = [NSString stringWithFormat:@"select * from t_groupMember where groupID='%@' and memberID='%@'",groupID,memberID];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    if(sqlite3_step(stmt) == SQLITE_ROW){
        groupMember = [SqliteUtils getMember:stmt];
    }
    sqlite3_finalize(stmt);
    return groupMember;
}

+ (void) insertRedPacket:(DMCCRedPacketInfo*) redPacketInfo{
    NSString* sql = [NSString stringWithFormat:@"insert or replace into t_redPacket(packetID,type,user,count,price,target,text,unpackID,urlQuery,urlFetch,luckNum,state,timestamp,wallet,coinType) values('%@','%@','%@','%@','%@','%@','%@','%@','%@','%@','%@',%d,%ld,'%@','%@')",redPacketInfo.packetID,redPacketInfo.type,redPacketInfo.user,redPacketInfo.count,redPacketInfo.price,redPacketInfo.target,redPacketInfo.text,redPacketInfo.unpackID,redPacketInfo.urlQuery,redPacketInfo.urlFetch,redPacketInfo.luckNum,redPacketInfo.state,redPacketInfo.timestamp, redPacketInfo.wallet, redPacketInfo.coinType];
    [SqliteUtils exec:sql];
}
+ (DMCCRedPacketInfo*) queryRedPacket:(NSString*) packetID{
    DMCCRedPacketInfo* redPacketInfo = nil;
    NSString* sql = [NSString stringWithFormat:@"select * from t_redPacket where packetID='%@'",packetID];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    if(sqlite3_step(stmt) == SQLITE_ROW){
        redPacketInfo = [DMCCRedPacketInfo new];
        redPacketInfo.type = [SqliteUtils getText:stmt name:@"type"];
        redPacketInfo.user = [SqliteUtils getText:stmt name:@"user"];
        redPacketInfo.count = [SqliteUtils getText:stmt name:@"count"];
        redPacketInfo.price = [SqliteUtils getText:stmt name:@"price"];
        redPacketInfo.target = [SqliteUtils getText:stmt name:@"target"];
        redPacketInfo.text = [SqliteUtils getText:stmt name:@"text"];
        redPacketInfo.packetID = [SqliteUtils getText:stmt name:@"packetID"];
        redPacketInfo.unpackID = [SqliteUtils getText:stmt name:@"unpackID"];
        redPacketInfo.urlQuery = [SqliteUtils getText:stmt name:@"urlQuery"];
        redPacketInfo.urlFetch = [SqliteUtils getText:stmt name:@"urlFetch"];
        redPacketInfo.luckNum = [SqliteUtils getText:stmt name:@"luckNum"];
        redPacketInfo.timestamp = [SqliteUtils getLong:stmt name:@"timestamp"];
        redPacketInfo.state = [SqliteUtils getInt:stmt name:@"state"];
        redPacketInfo.wallet = [SqliteUtils getText:stmt name:@"wallet"];
        redPacketInfo.coinType = [SqliteUtils getText:stmt name:@"coinType"];
    }
    sqlite3_finalize(stmt);
    return redPacketInfo;
}
+ (void) updateRedPacketState:(NSString*) packetID{
    NSString* sql = [NSString stringWithFormat:@"update t_redPacket set state=1 where packetID='%@'",packetID];
    [SqliteUtils exec:sql];
}
+ (void) updateRedPacketContextState:(NSString*) packetID{
    NSString* sql = [NSString stringWithFormat:@"update t_redPacket set state=1 where packetID='%@'",packetID];
    [SqliteUtils exec:sql];
}
+ (void) insertUnpack:(DMCCUnpackInfo*) unpackInfo{
    NSString* sql = [NSString stringWithFormat:@"insert or replace into t_unpack_info(user,fetcher,packetID,unpackID,price,timestamp) values('%@','%@','%@','%@','%@','%ld')",unpackInfo.user,unpackInfo.fetcher,unpackInfo.packetID,unpackInfo.unpackID,unpackInfo.price,unpackInfo.timestamp];
    [SqliteUtils exec:sql];
}
+ (NSArray<DMCCUnpackInfo*>*) queryUnpacks:(NSString*) unpackID{
    NSMutableArray<DMCCUnpackInfo*>* unpackInfoList = [NSMutableArray new];
    NSString* sql = [NSString stringWithFormat:@"select * from t_unpack_info where unpackID='%@'",unpackID];
    sqlite3_stmt *stmt = [SqliteUtils query:sql];
    while(sqlite3_step(stmt) == SQLITE_ROW){
        DMCCUnpackInfo* info = [DMCCUnpackInfo new];
        info.user = [SqliteUtils getText:stmt name:@"user"];
        info.fetcher = [SqliteUtils getText:stmt name:@"fetcher"];
        info.packetID = [SqliteUtils getText:stmt name:@"packetID"];
        info.unpackID = [SqliteUtils getText:stmt name:@"unpackID"];
        info.price = [SqliteUtils getText:stmt name:@"price"];
        info.timestamp = [SqliteUtils getLong:stmt name:@"timestamp"];
        [unpackInfoList addObject:info];
    }
    sqlite3_finalize(stmt);
    return unpackInfoList;
}
+ (DMCCLitappInfo*) getLitappInfo:(sqlite3_stmt*) stmt{
    DMCCLitappInfo* litappInfo = [DMCCLitappInfo new];
    litappInfo.target = [SqliteUtils getText:stmt name:@"target"];
    litappInfo.name = [SqliteUtils getText:stmt name:@"name"];
    litappInfo.displayName = [SqliteUtils getText:stmt name:@"displayName"];
    litappInfo.portrait = [SqliteUtils getText:stmt name:@"portrait"];
    litappInfo.theme = [SqliteUtils getText:stmt name:@"theme"];
    litappInfo.url = [SqliteUtils getText:stmt name:@"url"];
    litappInfo.info = [SqliteUtils getText:stmt name:@"info"];
    litappInfo.sid = [SqliteUtils getInt:stmt name:@"sid"];
    litappInfo.param = [SqliteUtils getText:stmt name:@"param"];
    return litappInfo;
}
+ (DMCCGroupInfo*) getGroupInfo:(sqlite3_stmt*) stmt{
    DMCCGroupInfo* groupInfo = [DMCCGroupInfo new];
    groupInfo.target = [SqliteUtils getText:stmt name:@"groupID"];
    groupInfo.name = [SqliteUtils getText:stmt name:@"name"];
    groupInfo.portrait = [SqliteUtils getText:stmt name:@"portrait"];
    groupInfo.owner = [SqliteUtils getText:stmt name:@"owner"];
    groupInfo.type = (DMCCGroupType)[SqliteUtils getInt:stmt name:@"type"];
    groupInfo.memberCount = [SqliteUtils getInt:stmt name:@"memberCount"];
    groupInfo.extra = [SqliteUtils getText:stmt name:@"extra"];
    groupInfo.updateTimestamp = [SqliteUtils getLong:stmt name:@"updateDt"];
    groupInfo.fav = [SqliteUtils getInt:stmt name:@"fav"];
    groupInfo.redPacket = [SqliteUtils getInt:stmt name:@"redPacket"];
    groupInfo.mute = [SqliteUtils getInt:stmt name:@"mute"];
    groupInfo.attribute = [SqliteUtils getText:stmt name:@"attribute"];
    groupInfo.joinType = [SqliteUtils getInt:stmt name:@"joinType"];
    groupInfo.passType = [SqliteUtils getInt:stmt name:@"passType"];
    groupInfo.privateChat = [SqliteUtils getInt:stmt name:@"privateChat"];
    groupInfo.maxMemberCount = [SqliteUtils getInt:stmt name:@"maxMemberCount"];
    groupInfo.showAlias = [SqliteUtils getInt:stmt name:@"showAlias"];
    groupInfo.notice = [SqliteUtils getText:stmt name:@"notice"];
    groupInfo.tagID = [SqliteUtils getLong:stmt name:@"tagID"];
    groupInfo.ugID = [SqliteUtils getLong:stmt name:@"ugID"];
    return groupInfo;
}
+ (DMCCUserInfo*) getUserInfo:(sqlite3_stmt*) stmt{
    DMCCUserInfo* userInfo = [DMCCUserInfo new];
    userInfo.userId = [SqliteUtils getText:stmt name:@"osnID"];
    userInfo.name = [SqliteUtils getText:stmt name:@"name"];
    userInfo.portrait = [SqliteUtils getText:stmt name:@"portrait"];
    userInfo.displayName = [SqliteUtils getText:stmt name:@"displayName"];
    userInfo.urlSpace = [SqliteUtils getText:stmt name:@"urlSpace"];
    userInfo.friendAlias = [SqliteUtils getText:stmt name:@"friendAlias"];
    userInfo.nft = [SqliteUtils getText:stmt name:@"nft"];
    userInfo.payState = [SqliteUtils getInt:stmt name:@"payState"];
    userInfo.describes = [SqliteUtils getText:stmt name:@"describes"];
    userInfo.tagID = [SqliteUtils getLong:stmt name:@"tagID"];
    userInfo.ugID = [SqliteUtils getLong:stmt name:@"ugID"];
    return userInfo;
}
+ (DMCCMessage*) getMessage:(sqlite3_stmt*) stmt{
    DMCCMessage* message = [DMCCMessage new];
    message.messageId = [SqliteUtils getInt:stmt name:@"mid"];
    message.fromUser = [SqliteUtils getText:stmt name:@"osnID"];
    message.conversation = [DMCCConversation new];
    message.conversation.type = (DMCCConversationType)[SqliteUtils getInt:stmt name:@"cType"];
    message.conversation.target = [SqliteUtils getText:stmt name:@"target"];
    message.conversation.line = 0;
    message.direction = (DMCCMessageDirection)[SqliteUtils getInt:stmt name:@"dir"];
    message.status = (DMCCMessageStatus)[SqliteUtils getInt:stmt name:@"state"];
    message.messageUid = [SqliteUtils getInt:stmt name:@"uid"];
    message.serverTime = [SqliteUtils getLong:stmt name:@"timestamp"];
    message.messageHash = [SqliteUtils getText:stmt name:@"msgHash"];
    message.messageHasho = [SqliteUtils getText:stmt name:@"msgHasho"];
    NSString* data = [SqliteUtils getText:stmt name:@"msgText"];
    int msgType = [SqliteUtils getInt:stmt name:@"msgType"];
    NSMutableDictionary *json = [OsnUtils json2Dic:[data dataUsingEncoding:NSUTF8StringEncoding]];
    
    if(msgType == MESSAGE_CONTENT_TYPE_TEXT){
        DMCCTextMessageContent* textMessageContent = [DMCCTextMessageContent new];
        textMessageContent.text = json[@"text"];
        DMCCQuoteInfo *quo = [DMCCQuoteInfo new];
        [quo decode:json[@"quoteInfo"]];
        textMessageContent.quoteInfo = quo;
        message.content = textMessageContent;
    } else if(msgType == MESSAGE_FRIEND_ADDED_NOTIFICATION){
        message.content = [DMCCFriendAddedMessageContent new];
    } else if(msgType == MESSAGE_FRIEND_GREETING){
        message.content = [DMCCFriendGreetingMessageContent new];
    } else if(msgType == MESSAGE_CONTENT_TYPE_CREATE_GROUP){
        DMCCCreateGroupNotificationContent* groupNotificationContent = [DMCCCreateGroupNotificationContent new];
        groupNotificationContent.creator = json[@"creator"];
        groupNotificationContent.groupName = json[@"groupName"];
        message.content = groupNotificationContent;
    } else if (msgType == MESSAGE_CONTENT_TYPE_RECALL) {
        DMCCRecallMessageContent *recallContent = [DMCCRecallMessageContent new];
        recallContent.operatorId = json[@"operatorId"];
        message.content = recallContent;
    } else if(msgType == MESSAGE_CONTENT_TYPE_IMAGE){
        DMCCImageMessageContent* imageMessageContent = [DMCCImageMessageContent new];
        imageMessageContent.localPath = json[@"localPath"];
        imageMessageContent.remoteUrl = json[@"remoteUrl"];
        imageMessageContent.decKey = json[@"decKey"];
        imageMessageContent.name = json[@"name"];
        imageMessageContent.size = CGSizeMake(((NSNumber*)json[@"width"]).intValue, ((NSNumber*)json[@"height"]).intValue);
        imageMessageContent.thumbnail = [DMCCUtilities fileToThumbnail:imageMessageContent.localPath];
        message.content = imageMessageContent;
    } else if(msgType == MESSAGE_CONTENT_TYPE_FILE){
        DMCCFileMessageContent* fileMessageContent = [DMCCFileMessageContent new];
        fileMessageContent.localPath = json[@"localPath"];
        fileMessageContent.remoteUrl = json[@"remoteUrl"];
        fileMessageContent.name = json[@"name"];
        fileMessageContent.decKey = json[@"decKey"];
        fileMessageContent.size = ((NSNumber*)json[@"size"]).intValue;
        message.content = fileMessageContent;
    } else if(msgType == MESSAGE_CONTENT_TYPE_SOUND){
        DMCCSoundMessageContent* soundMessageContent = [DMCCSoundMessageContent new];
        soundMessageContent.localPath = json[@"localPath"];
        soundMessageContent.remoteUrl = json[@"remoteUrl"];
        soundMessageContent.decKey = json[@"decKey"];
        soundMessageContent.duration = ((NSNumber*)json[@"duration"]).intValue;
        message.content = soundMessageContent;
    } else if(msgType == MESSAGE_CONTENT_TYPE_VIDEO){
        DMCCVideoMessageContent* videoMessageContent = [DMCCVideoMessageContent new];
        videoMessageContent.localPath = json[@"localPath"];
        videoMessageContent.remoteUrl = json[@"remoteUrl"];
        videoMessageContent.decKey = json[@"decKey"];
        videoMessageContent.name = json[@"name"];
//            if(json.containsKey("thumbnail"))
//                videoMessageContent.setThumbnailBytes(Base64.decode(json.getString("thumbnail"),0));
        message.content = videoMessageContent;
    } else if(msgType == MESSAGE_CONTENT_TYPE_STICKER){
        DMCCStickerMessageContent* stickerMessageContent = [DMCCStickerMessageContent new];
        stickerMessageContent.localPath = json[@"localPath"];
        stickerMessageContent.remoteUrl = json[@"remoteUrl"];
        stickerMessageContent.size = CGSizeMake(((NSNumber*)json[@"width"]).intValue, ((NSNumber*)json[@"height"]).intValue);
        message.content = stickerMessageContent;
    } else if(msgType == MESSAGE_CONTENT_TYPE_KICKOF_GROUP_MEMBER){
        DMCCKickoffGroupMemberNotificationContent* kickoffGroupMemberNotificationContent = [DMCCKickoffGroupMemberNotificationContent new];
        kickoffGroupMemberNotificationContent.kickedMembers = json[@"members"];
        kickoffGroupMemberNotificationContent.operateUser = json[@"operator"];
        message.content = kickoffGroupMemberNotificationContent;
    } else if(msgType == MESSAGE_CONTENT_TYPE_ADD_GROUP_MEMBER){
        DMCCAddGroupeMemberNotificationContent* addGroupMemberNotificationContent = [DMCCAddGroupeMemberNotificationContent new];
        addGroupMemberNotificationContent.invitor = json[@"invitor"];
        addGroupMemberNotificationContent.invitees = json[@"invitees"];
        message.content = addGroupMemberNotificationContent;
    } else if(msgType == MESSAGE_CONTENT_TYPE_QUIT_GROUP){
        DMCCQuitGroupNotificationContent* quitGroupNotificationContent = [DMCCQuitGroupNotificationContent new];
        quitGroupNotificationContent.quitMember = json[@"operator"];
        message.content = quitGroupNotificationContent;
    } else if(msgType == MESSAGE_CONTENT_TYPE_DISMISS_GROUP){
        DMCCDismissGroupNotificationContent* dismissGroupNotificationContent = [DMCCDismissGroupNotificationContent new];
        dismissGroupNotificationContent.operateUser = json[@"operator"];
        message.content = dismissGroupNotificationContent;
    } else if(msgType == MESSAGE_CONTENT_TYPE_TRANSFER_GROUP_OWNER){
        DMCCTransferGroupOwnerNotificationContent* transferGroupOwnerNotificationContent = [DMCCTransferGroupOwnerNotificationContent new];
        transferGroupOwnerNotificationContent.operateUser = json[@"operator"];
        transferGroupOwnerNotificationContent.owner = json[@"newOwner"];
        message.content = transferGroupOwnerNotificationContent;
    } else if(msgType == MESSAGE_CONTENT_TYPE_General_Notification){
        DMCCGroupNotifyContent* groupNotifyContent = [DMCCGroupNotifyContent new];
        groupNotifyContent.info = json[@"info"];
        message.content = groupNotifyContent;
    } else if(msgType == MESSAGE_CONTENT_TYPE_CARD){
        DMCCCardMessageContent* cardMessageContent = [DMCCCardMessageContent new];
        cardMessageContent.type = (DMCCCardType)((NSNumber*)json[@"cardType"]).intValue;
        cardMessageContent.targetId = json[@"target"];
        cardMessageContent.name = json[@"name"];
        cardMessageContent.displayName = json[@"displayName"];
        cardMessageContent.portrait = json[@"portrait"];
        cardMessageContent.theme = json[@"theme"];
        cardMessageContent.url = json[@"url"];
        cardMessageContent.info = json[@"info"];
        message.content = cardMessageContent;
    } else if(msgType == MESSAGE_CONTENT_TYPE_RED_PACKET){
        DMCCRedPacketMessageContent* redPacketMessageContent = [DMCCRedPacketMessageContent new];
        redPacketMessageContent.ids = json[@"id"];
        redPacketMessageContent.text = json[@"text"];
        redPacketMessageContent.info = json[@"info"];
        redPacketMessageContent.state = ((NSNumber*)json[@"state"]).intValue;
        message.content = redPacketMessageContent;
    } else if(msgType == MESSAGE_CONTENT_TYPE_CALL){
        DMCCCallMessageContent* callMessageContent = [DMCCCallMessageContent new];
        callMessageContent.cid = ((NSNumber*)json[@"id"]).intValue;
        callMessageContent.type = ((NSNumber*)json[@"type"]).intValue;
        callMessageContent.mode = ((NSNumber*)json[@"mode"]).intValue;
        callMessageContent.action = ((NSNumber*)json[@"action"]).intValue;
        callMessageContent.duration = ((NSNumber*)json[@"duration"]).intValue;
        callMessageContent.status = json[@"status"];
        callMessageContent.url = json[@"url"];
        callMessageContent.user = json[@"user"];
        message.content = callMessageContent;
    } else {
        message.content = [DMCCUnknownMessageContent new];
        NSLog(@"unknown msgType: %d",msgType);
    }
    return message;
}
+ (DMCCConversationInfo*) getConversation:(sqlite3_stmt*) stmt{
    DMCCConversationInfo* conversationInfo = [DMCCConversationInfo new];
    conversationInfo.conversation = [DMCCConversation new];
    conversationInfo.conversation.type = (DMCCConversationType)[SqliteUtils getInt:stmt name:@"type"];
    conversationInfo.conversation.target = [SqliteUtils getText:stmt name:@"target"];
    conversationInfo.conversation.line = [SqliteUtils getInt:stmt name:@"line"];
    conversationInfo.lastMessage = [SqliteUtils getLastMessage:conversationInfo.conversation];
    conversationInfo.timestamp = [SqliteUtils getLong:stmt name:@"timestamp"];
    conversationInfo.draft = [SqliteUtils getText:stmt name:@"draft"];
    conversationInfo.unreadCount = [DMCCUnreadCount new];
    conversationInfo.unreadCount.unread = [SqliteUtils getInt:stmt name:@"unreadCount"];
    conversationInfo.unreadCount.unreadMention = [SqliteUtils getInt:stmt name:@"unreadMention"];
    conversationInfo.unreadCount.unreadMentionAll = [SqliteUtils getInt:stmt name:@"unreadMentionAll"];
    conversationInfo.isTop = [SqliteUtils getInt:stmt name:@"isTop"] != 0;
    conversationInfo.isSilent = [SqliteUtils getInt:stmt name:@"isSilent"] != 0;
    conversationInfo.isMember = [SqliteUtils getInt:stmt name:@"isMember"];
    if(conversationInfo.draft == nil)
        conversationInfo.draft = @"";
    return conversationInfo;
}
+ (DMCCGroupMember*) getMember:(sqlite3_stmt*) stmt{
    DMCCGroupMember* groupMember = [DMCCGroupMember new];
    groupMember.groupId = [SqliteUtils getText:stmt name:@"groupID"];
    groupMember.memberId = [SqliteUtils getText:stmt name:@"memberID"];
    groupMember.alias = [SqliteUtils getText:stmt name:@"alias"];
    groupMember.type = (DMCCGroupMemberType)[SqliteUtils getInt:stmt name:@"type"];
    groupMember.mute = [SqliteUtils getInt:stmt name:@"mute"];
    groupMember.updateTime = [SqliteUtils getLong:stmt name:@"updateDt"];
    groupMember.createTime = [SqliteUtils getLong:stmt name:@"createDt"];
    groupMember.index = [SqliteUtils getInt:stmt name:@"memberIndex"];
    return groupMember;
}
+ (DMCCFriendRequest*) getFriendRequest:(sqlite3_stmt*) stmt{
    DMCCFriendRequest* request = [DMCCFriendRequest new];
    request.type = [SqliteUtils getInt:stmt name:@"type"];
    request.direction = [SqliteUtils getInt:stmt name:@"direction"];
    request.target = [SqliteUtils getText:stmt name:@"target"];
    request.originalUser = [SqliteUtils getText:stmt name:@"originalUser"];
    request.userID = [SqliteUtils getText:stmt name:@"userID"];
    request.reason = [SqliteUtils getText:stmt name:@"reason"];
    request.status = [SqliteUtils getInt:stmt name:@"status"];
    request.readStatus = [SqliteUtils getInt:stmt name:@"readStatus"];
    request.timestamp = [SqliteUtils getLong:stmt name:@"timestamp"];
    request.invitation = [SqliteUtils getText:stmt name:@"invitation"];
    return request;
}
+ (DMCCTagInfo*) getTagInfoRequest:(sqlite3_stmt*) stmt{
    DMCCTagInfo* request = [DMCCTagInfo new];
    request.id = [SqliteUtils getLong:stmt name:@"tagID"];
    request.group_name = [SqliteUtils getText:stmt name:@"tagName"];
    return request;
}

+ (DMCCWalletInfo*) getWalletInfoRequest:(sqlite3_stmt*) stmt{
    DMCCWalletInfo* request = [DMCCWalletInfo new];
    request.osnID = [SqliteUtils getText:stmt name:@"osnID"];
    request.name = [SqliteUtils getText:stmt name:@"name"];
    request.url = [SqliteUtils getText:stmt name:@"url"];
    request.wallect = [SqliteUtils getText:stmt name:@"wallect"];
    return request;
}

+ (DMCCCollectInfo*) getColletInfoRequest:(sqlite3_stmt*) stmt{
    DMCCCollectInfo* request = [DMCCCollectInfo new];
    request.osnID = [SqliteUtils getText:stmt name:@"osnID"];
    request.name = [SqliteUtils getText:stmt name:@"name"];
    request.type = [SqliteUtils getInt:stmt name:@"type"];
    request.content = [SqliteUtils getText:stmt name:@"content"];
    return request;
}

@end
