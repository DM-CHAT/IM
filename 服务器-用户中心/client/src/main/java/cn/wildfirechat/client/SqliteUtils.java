package cn.wildfirechat.client;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.ospn.osnsdk.data.OsnFriendInfo;
import com.ospn.osnsdk.data.OsnMemberInfo;
import com.ospn.osnsdk.utils.ECUtils;

import java.util.ArrayList;
import java.util.List;

import cn.wildfirechat.ContentsInfo;
import cn.wildfirechat.ContentsQuoteInfo;
import cn.wildfirechat.message.CallMessageContent;
import cn.wildfirechat.message.CardMessageContent;
import cn.wildfirechat.message.FileMessageContent;
import cn.wildfirechat.message.ImageMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.RedPacketMessageContent;
import cn.wildfirechat.message.SoundMessageContent;
import cn.wildfirechat.message.StickerMessageContent;
import cn.wildfirechat.message.TextMessageContent;
import cn.wildfirechat.message.UnknownMessageContent;
import cn.wildfirechat.message.VideoMessageContent;
import cn.wildfirechat.message.core.MessageContentType;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.message.core.MessageStatus;
import cn.wildfirechat.message.notification.AddGroupMemberNotificationContent;
import cn.wildfirechat.message.notification.CreateGroupNotificationContent;
import cn.wildfirechat.message.notification.DismissGroupNotificationContent;
import cn.wildfirechat.message.notification.FriendAddedMessageContent;
import cn.wildfirechat.message.notification.FriendGreetingMessageContent;
import cn.wildfirechat.message.notification.GroupNotifyContent;
import cn.wildfirechat.message.notification.KickoffGroupMemberNotificationContent;
import cn.wildfirechat.message.notification.QuitGroupNotificationContent;
import cn.wildfirechat.message.notification.RecallMessageContent;
import cn.wildfirechat.message.notification.TransferGroupOwnerNotificationContent;
import cn.wildfirechat.model.CollectInfo;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.FriendRequest;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.GroupMember;
import cn.wildfirechat.model.LitappInfo;
import cn.wildfirechat.model.OrgTag;
import cn.wildfirechat.model.QuoteInfo;
import cn.wildfirechat.model.RedPacketInfo;
import cn.wildfirechat.model.UnpackInfo;
import cn.wildfirechat.model.UnreadCount;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.model.WalletsInfo;

import static android.content.ContentValues.TAG;
import static cn.wildfirechat.client.ClientService.logError;
import static cn.wildfirechat.client.ClientService.logInfo;
import static cn.wildfirechat.model.FriendRequest.RequestType_ApplyMember;
import static cn.wildfirechat.model.FriendRequest.RequestType_Friend;
import static cn.wildfirechat.model.FriendRequest.RequestType_InviteGroup;

public class SqliteUtils {
    private static SQLiteDatabase mDB = null;
    private static long tMessageID = 1;

    public static void initDB(String path){
        try {
            if(mDB != null)
                return;
            logInfo("db path: "+path);
            mDB = SQLiteDatabase.openOrCreateDatabase(path, null);

            String sql = "create table if not exists t_user(_id integer primary key autoincrement, " +
                    "osnID char(128) UNIQUE, " +
                    "name nvarchar(20), " +
                    "portrait text, " +
                    "displayName nvarchar(20), " +
                    "urlSpace text, "+
                    "role text, "+
                    "describes text, "+
                    "nft text, "+
                    "payState int)";
            mDB.execSQL(sql);
            sql = "create table if not exists t_friend(_id integer primary key autoincrement, " +
                    "osnID char(128) , " +
                    "friendID char(128) UNIQUE, " +
                    "remarks char(128) , " +
                    "state tinyint default 0)";
            mDB.execSQL(sql);
            sql = "create table if not exists t_friendRequest(_id integer primary key autoincrement, " +
                    "type tinyint , " + //add 2021.5.21
                    "direction tinyint , " +
                    "target char(128) , " +
                    "originalUser char(128) , " + //add 2021.5.21
                    "userID char(128) , " + //add 2021.5.21
                    "reason char(128) , " +
                    "invitation char(1024), "+
                    "status tinyint , " +
                    "readStatus tinyint , " +
                    "timestamp long)";
            mDB.execSQL(sql);
            sql = "create table if not exists t_message(_id integer primary key autoincrement, " +
                    "mid integer, " +
                    "osnID char(128), " +
                    "cType tinyint, " +
                    "target char(128), " +
                    "dir tinyint, " +
                    "state tinyint default 0," + //MessageStatus
                    "uid integer, " +
                    "timestamp integer, " +
                    "msgType tinyint default 0," +
                    "msgText text," +
                    "msgJson text," +
                    "msgHash0 text," +
                    "msgHash text," +
                    "hashIndex char(255) UNIQUE" +
                    ")";
            mDB.execSQL(sql);

            sql = "create table if not exists t_last_message(_id integer primary key autoincrement, " +
                    "osnID char(128), " +
                    "mid integer, " +
                    "cType tinyint, " +
                    "target char(128) NOT NULL UNIQUE, " +
                    "dir tinyint, " +
                    "state tinyint default 0," +
                    "uid integer, " +
                    "timestamp integer, " +
                    "msgType tinyint default 0," +
                    "msgText text," +
                    "msgJson text," +
                    "msgHash0 text," +
                    "msgHash text," +
                    "hashIndex char(255)" +
                    ")";
            mDB.execSQL(sql);

            sql = "create table if not exists t_conversation(_id integer primary key autoincrement, " +
                    "type tinyint, " +
                    "target char(128), " +
                    "line tinyint," +
                    "timestamp integer," +
                    "draft text," +
                    "unreadCount int default 0," +
                    "unreadMention int default 0," +
                    "unreadMentionAll int default 0," +
                    "isTop tinyint default 0," +
                    "isSilent tinyint default 0,"+
                    "tagId integer default -1,"+
                    "isMember int default 1," +
                    "unique(target,tagId) on conflict replace)";
            mDB.execSQL(sql);
            sql = "create table if not exists t_group(_id integer primary key autoincrement, " +
                    "groupID char(128) unique, " +
                    "name char(20), " +
                    "portrait char(128), " +
                    "attribute text, "+
                    "timeInterval text, "+
                    "owner char(128), " +
                    "type tinyint, " +
                    "memberCount int, " +
                    "extra text, " +
                    "updateDt long, " +
                    "fav int default 0, " +
                    "redPacket int default 0, " +
                    "notice varchar(255), " +
                    "mute tinyint default 0, " +
                    "joinType tinyint default 0, " +
                    "passType tinyint default 0, " +
                    "privateChat tinyint default 0, " +
                    "maxMemberCount long default 200, " +
                    "showAlias tinyint default 0)";
            mDB.execSQL(sql);
            sql = "create table if not exists t_groupMember(_id integer primary key autoincrement, " +
                    "groupID char(128), " +
                    "memberID char(128), " +
                    "alias char(20), " +
                    "type tinyint default 0, " +
                    "mute tinyint default 0, " +
                    "memberIndex int, " +
                    "updateDt long, " +
                    "createDt long, UNIQUE(groupID, memberID) ON CONFLICT REPLACE)";
            mDB.execSQL(sql);
            sql = "create table if not exists t_dapp(_id integer primary key autoincrement, " +
                    "target char(128) UNIQUE, " +
                    "name varchar(255), " +
                    "displayName varchar(255), " +
                    "portrait text, " +
                    "theme text, " +
                    "url varchar(512), "+
                    "param text, " +
                    "info text)";
            mDB.execSQL(sql);

            sql = "create table if not exists t_collectdapp(_id integer primary key autoincrement, " +
                    "target char(128) UNIQUE, " +
                    "name varchar(255), " +
                    "displayName varchar(255), " +
                    "portrait text, " +
                    "theme text, " +
                    "url varchar(512), "+
                    "param text, " +
                    "info text)";
            mDB.execSQL(sql);

            sql = "create table if not exists t_wallets2(_id integer primary key autoincrement, " +
                    "osnID char(128) UNIQUE, " +
                    "name varchar(255), " +
                    "wallets text)";
            mDB.execSQL(sql);

            sql = "CREATE TABLE IF NOT EXISTS t_pyq " +
                    "(_id integer primary key autoincrement, " +
                    "osnID varchar(255) NOT NULL," +
                    "pyqID bigint ," +
                    "pyqType int ," +
                    "pyqText varchar(255) DEFAULT NULL," +
                    "pyqPicture text DEFAULT NULL ," +
                    "pyqWebUrl varchar(511) DEFAULT NULL," +
                    "pyqWebText varchar(255) DEFAULT NULL," +
                    "pyqWebPicture varchar(255) DEFAULT NULL," +
                    "pyqPlace varchar(255) DEFAULT NULL," +
                    "pyqSyncTime text," +
                    "createTime bigint)";
            mDB.execSQL(sql);
            sql = "CREATE TABLE IF NOT EXISTS t_pyq_talk " +
                    "(_id integer primary key autoincrement, " +
                    "osnID varchar(255) NOT NULL," +
                    "fromID varchar(255) NOT NULL," +
                    "toID varchar(255) NOT NULL," +
                    "pyqID bigint NOT NULL ," +
                    "text varchar(255) NOT NULL ," +
                    "createTime bigint)";
            mDB.execSQL(sql);
            sql = "CREATE TABLE IF NOT EXISTS t_pyq_list " +
                    "(_id integer primary key autoincrement, " +
                    "osnID varchar(255) NOT NULL," +
                    "target varchar(255) NOT NULL," +
                    "pyqID bigint NOT NULL ," +
                    "createTime bigint," +
                    "unique (target,pyqID))";
            mDB.execSQL(sql);

            sql = "CREATE TABLE IF NOT EXISTS t_tag " +
                    "(_id integer primary key autoincrement, " +
                    "tagName varchar(255) NOT NULL," +
                    "tagId integer NOT NULL," +
                    "unique (tagName))";
            mDB.execSQL(sql);

            sql = "CREATE TABLE IF NOT EXISTS t_tag_user " +
                    "(_id integer primary key autoincrement, " +
                    "osnID varchar(255) NOT NULL," +
                    "tagId integer NOT NULL," +
                    "unique (osnId))";
            mDB.execSQL(sql);

            sql = "CREATE TABLE IF NOT EXISTS t_redPacket " +
                    "(_id integer primary key autoincrement, " +
                    "packetID varchar(128) NOT NULL," +
                    "type varchar(32) NOT NULL,"+
                    "user varchar(255) NOT NULL," +
                    "count varchar(32) NOT NULL,"+
                    "price varchar(32) NOT NULL,"+
                    "target varchar(255) NOT NULL," +
                    "text varchar(255) NOT NULL,"+
                    "urlQuery varchar(511) NOT NULL,"+
                    "urlFetch varchar(511) NOT NULL,"+
                    "luckNum varchar(32) NOT NULL,"+
                    "dapp varchar(255) NOT NULL," +
                    "timestamp bigint," +
                    "state int," +
                    "unpackID varchar(128) NOT NULL,"+
                    "wallet text," +
                    "coinType varchar(32)," +
                    "unique (packetID,unpackID))";
            mDB.execSQL(sql);
            sql = "CREATE TABLE IF NOT EXISTS t_unpack_info " +
                    "(_id integer primary key autoincrement, " +
                    "user varchar(255) NOT NULL," +
                    "fetcher varchar(255) NOT NULL," +
                    "packetID varchar(128) NOT NULL," +
                    "unpackID varchar(128) NOT NULL,"+
                    "price varchar(32) NOT NULL,"+
                    "timestamp bigint," +
                    "unique (fetcher,unpackID))";
            mDB.execSQL(sql);

            updateTable();

            Cursor cursor = mDB.rawQuery("select mid from t_message order by mid DESC limit 1", null);
            if(cursor.moveToNext())
                tMessageID = cursor.getInt(0)+1;
            cursor.close();

            logInfo("tMessageID: " + tMessageID);
        }
        catch (Exception e){
            System.out.println("@@@@@@     异常  ");
            logError(e);
        }
    }
    public static void closeDB(){
        if(mDB != null){
            mDB.close();
            mDB = null;
        }
    }
    public static void updateTable(){
        String[] sqls = {
                "alter table t_message add column msgJson text",
                "create unique index request_unique on t_friendRequest (timestamp)"
        };
        for(String sql : sqls) {
            try {
                mDB.execSQL(sql);
            }
            catch (Exception e){
                logError(e);
            }
        }
    }

    public static void insertUser(UserInfo userInfo){
        if(mDB == null)
            return;
        try{
            String sql = "insert or replace into t_user(osnID,name,displayName,portrait,urlSpace,role,describes,nft,payState) " +
                    "values(?,?,?,?,?,?,?,?,?)";
            mDB.execSQL(sql, new Object[]{userInfo.uid,userInfo.name,userInfo.displayName,userInfo.portrait,userInfo.urlSpace, userInfo.role,userInfo.describes,"",userInfo.payState});
        }
        catch (Exception e){
            logError(e);
        }
    }
    public static void updateUser(UserInfo userInfo, List<String> keys){
        try{
            String sql;
            for(String k:keys) {
                switch(k){
                    case "displayName":
                        sql = "update t_user set displayName=? where osnID=?";
                        mDB.execSQL(sql, new Object[]{userInfo.displayName, userInfo.uid});
                        break;
                    case "portrait":
                        sql = "update t_user set portrait=? where osnID=?";
                        mDB.execSQL(sql, new Object[]{userInfo.portrait, userInfo.uid});
                        break;
                    case "urlSpace":
                        sql = "update t_user set urlSpace=? where osnID=?";
                        mDB.execSQL(sql, new Object[]{userInfo.urlSpace, userInfo.uid});
                        break;
                    case "role":
                        sql = "update t_user set role=? where osnID=?";
                        mDB.execSQL(sql, new Object[]{userInfo.role, userInfo.uid});
                    case "describes":
                        sql = "update t_user set describes=? where osnID=?";
                        mDB.execSQL(sql, new Object[]{userInfo.describes,userInfo.uid});
					/*case "nft":
                        sql = "update t_user set nft=? where osnID=?";
                        mDB.execSQL(sql, new Object[]{userInfo.nft, userInfo.uid});
                        break;*/
                    case "payState":
                        sql = "update t_user set payState=? where osnID=?";
                        mDB.execSQL(sql, new Object[]{userInfo.payState, userInfo.uid});
                        break;
                }
            }
        }
        catch (Exception e){
            logError(e);
        }
    }
    public static UserInfo queryUser(String userID){
        UserInfo userInfo = null;
        if(mDB == null){
            return userInfo;
        }
        try{
            Cursor cursor = mDB.rawQuery("select (select remarks from t_friend where friendID=?) " +
                    "as remarks,* from t_user where osnID=?", new String[]{userID, userID});
            if(cursor.moveToFirst())
                userInfo = getUserInfo(cursor);
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return userInfo;
    }
    public static List<UserInfo> queryUsers(String keyword){
        List<UserInfo> userInfos = new ArrayList<>();
        try{
            Cursor cursor = mDB.rawQuery("select * from t_user where displayName like ?", new String[]{"%"+keyword+"%"});
            while(cursor.moveToNext())
                userInfos.add(getUserInfo(cursor));
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return userInfos;
    }

    /*public static List<GroupInfo> queryFavGroups(String keyword){
        List<GroupInfo> groupList = new ArrayList<>();
        try{
            //Cursor cursor = mDB.rawQuery("select * from t_group where fav<>0 and displayName like ?", new String[]{"%"+keyword+"%"});
            Cursor cursor = mDB.rawQuery("select * from t_group where fav<>0", null);
            while(cursor.moveToNext()){
                groupList.add(getGroupInfo(cursor));
            }
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return groupList;
    }*/

    public static List<GroupInfo> queryFavGroups(String keyword){
        List<GroupInfo> groupList = new ArrayList<>();
        try{
        //    Cursor cursor = mDB.rawQuery("select * from t_group where displayName like ?", new String[]{"%"+keyword+"%"});
            Cursor cursor = mDB.rawQuery("select * from t_group where name like ?", new String[]{"%"+keyword+"%"});
            //Cursor cursor = mDB.rawQuery("select * from t_group where fav<>0 and displayName like ?", new String[]{"%"+keyword+"%"});
            //Cursor cursor = mDB.rawQuery("select * from t_group where fav<>0", null);
            while(cursor.moveToNext()){
                groupList.add(getGroupInfo(cursor));
            }
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return groupList;
    }

    public static void clearGroupFav(){
        System.out.println("sync group clearGroupFav");
        try{
            String sql = "update t_group set fav=? where fav<>0";
            mDB.execSQL(sql, new Object[]{0});
        }
        catch (Exception e){
            System.out.println("sync group clearGroupFav Exception");
            System.out.println("sync group " + e.getMessage());
            //logError(e);
        }
    }

    public static void insertFriendRequest(FriendRequest friendRequest){
        try {
            String sql = "insert or replace into t_friendRequest(type,direction,target,originalUser,userID,reason,status,readStatus,timestamp,invitation) " +
                    "values(?,?,?,?,?,?,?,?,?,?)";
            mDB.execSQL(sql,new Object[]{friendRequest.type,friendRequest.direction,friendRequest.target,friendRequest.originalUser,friendRequest.userID,friendRequest.reason,friendRequest.status,friendRequest.readStatus,friendRequest.timestamp,friendRequest.invitation});
        }
        catch (Exception e){
            logError(e);
        }
    }
    public static void updateFriendRequests(List<FriendRequest> friendRequestList){
        try{
            for(FriendRequest request : friendRequestList){
                String sql = "update t_friendRequest set status=?, readStatus=? where target=?";
                if(request.type == RequestType_InviteGroup ||
                        request.type == RequestType_ApplyMember){
                    sql += " and userID=?";
                    mDB.execSQL(sql, new Object[]{request.status,request.readStatus,request.target,request.userID});
                }else {
                    mDB.execSQL(sql, new Object[]{request.status,request.readStatus,request.target});
                }
            }
        }
        catch (Exception e){
            logError(e);
        }
    }
    public static FriendRequest queryFriendRequest(String userID){
        FriendRequest request = null;
        try{
            Cursor cursor = mDB.rawQuery("select * from t_friendRequest where target=?", new String[]{userID});
            //if(cursor.moveToFirst())
            if(cursor.moveToNext())
                request = getFriendRequest(cursor);
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return request;
    }
    public static FriendRequest queryFriendRequest(String userID, String groupID){
        FriendRequest request = null;
        try{

            Cursor cursor = mDB.rawQuery("select * from t_friendRequest where target=? and userID=?", new String[]{groupID,userID});
            //if(cursor.moveToFirst())
            if(cursor.moveToNext())
                request = getFriendRequest(cursor);
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return request;
    }
    public static List<FriendRequest> listFriendRequest(){
        List<FriendRequest> friendRequestList = new ArrayList<>();
        try{
            Cursor cursor = mDB.rawQuery("select * from t_friendRequest order by timestamp desc", null);
            while(cursor.moveToNext()){
                FriendRequest request = getFriendRequest(cursor);
                friendRequestList.add(request);
            }
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }

        List<FriendRequest> friendRequestList2 = removeSave(friendRequestList);

        return friendRequestList2;
    }

    static List<FriendRequest> removeSave(List<FriendRequest> src){

        List<FriendRequest> result = new ArrayList<>();

        for (FriendRequest fr : src) {

            if (fr.target.startsWith("OSNU")) {

                if (!isExistUser(result, fr.target)) {
                    result.add(fr);
                }

            } else if (fr.target.startsWith("OSNG")) {

                if (!isExistGroup(result, fr.target, fr.originalUser)) {
                    result.add(fr);
                }

            }
        }

        return result;
    }

    static boolean isExistUser(List<FriendRequest> src, String user) {

        for (FriendRequest fr : src) {
            if (fr.target.equalsIgnoreCase(user)) {
                return true;
            }
        }

        return false;
    }

    static boolean isExistGroup(List<FriendRequest> src, String group, String user) {

        for (FriendRequest fr : src) {
            if (fr.target.equalsIgnoreCase(group) && fr.originalUser.equalsIgnoreCase(user)) {
                return true;
            }
        }

        return false;
    }




    public static List<FriendRequest> queryUnreadFriendRequest(){
        List<FriendRequest> friendRequestList = new ArrayList<>();
        if(mDB == null){
            return friendRequestList;
        }
        try{
            Cursor cursor = mDB.rawQuery("select * from t_friendRequest where readStatus=0 order by timestamp desc", null);
            while(cursor.moveToNext()){
                FriendRequest request = getFriendRequest(cursor);
                friendRequestList.add(request);
            }
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return friendRequestList;
    }

    public static void insertFriend(OsnFriendInfo friendInfo){
        if(mDB == null)
            return;
        try {
            String sql = "insert or replace into t_friend(osnID,friendID,state,remarks) values(?,?,?,?)";
            mDB.execSQL(sql, new Object[]{friendInfo.userID,friendInfo.friendID,friendInfo.state,friendInfo.remarks});
        }
        catch (Exception e){
            logError(e);
        }
    }
    public static void deleteFriend(String friendID){
        List<String> list1 = listFriends();

        try{
            String sql = "delete from t_friend where friendID=?";
            mDB.execSQL(sql, new Object[]{friendID});
        }
        catch (Exception e){
            logError(e);
        }
        List<String> list2 = listFriends();
    }
    public static void updateFriend(OsnFriendInfo friendInfo){
        try{
            String sql = "update t_friend set state=?,remarks=? where friendID=?";
            mDB.execSQL(sql, new Object[]{friendInfo.state,friendInfo.remarks,friendInfo.friendID});
        }
        catch (Exception e){
            logError(e);
        }
    }
    public static void updateFriend(OsnFriendInfo friendInfo, List<String> keys){
        try{
            String sql = null;
            for(String k:keys) {
                switch(k){
                    case "state":
                        sql = "update t_friend set state=? where friendID=?";
                        mDB.execSQL(sql, new Object[]{friendInfo.state,friendInfo.friendID});
                        break;
                    case "remarks":
                        sql = "update t_friend set remarks=? where friendID=?";
                        mDB.execSQL(sql, new Object[]{friendInfo.remarks,friendInfo.friendID});
                        break;
                }

            }
        }
        catch (Exception e){
            logError(e);
        }
    }
    public static List<String> listFriends(){
        List<String> userList = new ArrayList<>();
        if(mDB == null){
            return userList;
        }
        try{
            Cursor cursor = mDB.rawQuery("select friendID from t_friend", null);
            while (cursor.moveToNext()){
                String friendID = cursor.getString(0);
                userList.add(friendID);
            }
            cursor.close();
            return userList;
        }
        catch (Exception e){
            logError(e);
        }
        return userList;
    }
    public static List<String> listFriends(int state){
        List<String> userList = new ArrayList<>();
        try{
            Cursor cursor = mDB.rawQuery("select friendID from t_friend where state=?", new String[]{String.valueOf(state)});
            while (cursor.moveToNext()){
                String friendID = cursor.getString(0);
                userList.add(friendID);
            }
            cursor.close();
            return userList;
        }
        catch (Exception e){
            logError(e);
        }
        return userList;
    }
    public static OsnFriendInfo queryFriend(String friendID){
        OsnFriendInfo friendInfo = null;
        if(mDB == null)
            return null;
        try{
            Cursor cursor = mDB.rawQuery("select * from t_friend where friendID=?", new String[]{friendID});
            if(cursor.moveToNext()) {
                friendInfo = new OsnFriendInfo();
                friendInfo.userID = cursor.getString(cursor.getColumnIndex("osnID"));
                friendInfo.friendID = friendID;
                friendInfo.remarks = cursor.getString(cursor.getColumnIndex("remarks"));
                friendInfo.state = cursor.getInt(cursor.getColumnIndex("state"));
            }
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return friendInfo;
    }
    public static void clearFriend(){

        try{
            String sql = "delete from t_friend";
            mDB.execSQL(sql);
        }
        catch (Exception e){
            logError(e);
        }

    }




    public static boolean insertTag(String name, int tagId){
        try{
            //System.out.println("@@@1     content="+name +"   tagId="+tagId);
            String sql = "insert into t_tag(tagName,tagId) values(?,?)";
            mDB.execSQL(sql, new Object[]{name,tagId});
            return true;
        }
        catch (Exception e){
            System.out.println("@@@ error : " +e.getMessage());
            //logError(e);
        }
        return false;
    }
    public static OrgTag queryTag(int id){
        OrgTag orgTag = null;
        try{
            Cursor cursor = mDB.rawQuery("select * from t_tag where id=?", new String[]{String.valueOf(id)});
            if(cursor.moveToNext()){
                orgTag = getOrgTag(cursor);
            }
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return orgTag;
    }

    public static List<OrgTag> listTag(){
        List<OrgTag> orgTagList = new ArrayList<>();
        try{
            Cursor cursor = mDB.rawQuery("select * from t_tag", null);

            while(cursor.moveToNext()){
                OrgTag orgTag = getOrgTag(cursor);
                if (orgTag != null) {
                    orgTagList.add(orgTag);
                }
            }
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        for (OrgTag tag : orgTagList) {
            /*System.out.println("@@@   tag name : " + tag.tagName);
            System.out.println("@@@   tag id : " + tag.id);*/
        }
        return orgTagList;
    }
    public static boolean updateTagName(OrgTag orgTag){
        try{
            String sql = "update t_tag set tagName=? " +
                    "where tagId=?";
            mDB.execSQL(sql, new Object[]{orgTag.tagName,orgTag.tagId});
            return true;
        }
        catch (Exception e){
            logError(e);
        }
        return false;
    }

    public static List<String> listTagUser(int tagId){
        List<String> list = new ArrayList<>();
        try{
            Cursor cursor = mDB.rawQuery("select (osnID) from t_tag_user where tagId=?", new String[]{String.valueOf(tagId)});
            System.out.println("@@@   cursor="+cursor.getCount());
            while(cursor.moveToNext()){
                String osnId = cursor.getString(cursor.getColumnIndex("osnID"));
                if (osnId != null){
                    list.add(osnId);
                }
            }
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return list;
    }
    public static String queryTagUser(String osnid){
        String osnId = "";
        try{
            Cursor cursor = mDB.rawQuery("select * from t_tag_user where osnID=?", new String[]{osnid});
            if(cursor.moveToNext()){
                osnId = cursor.getString(cursor.getColumnIndex("osnID"));
                return osnId;
            }

            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return null;
    }
    public static boolean insertTagUser(String osnId, int tagId){
        updateConversationTag(osnId, tagId);
        try{
            String sql = "insert into t_tag_user(osnID,tagId) values(?,?)";
            mDB.execSQL(sql, new Object[]{osnId,tagId});
            return true;
        }
        catch (Exception e){
            logError(e);
        }
        return false;
    }
    public static boolean updateTagUser(String osnID, int tagId){
        updateConversationTag(osnID, tagId);
        try{
            String sql = "update t_tag_user set tagId=? " +
                    "where osnID=?";
            mDB.execSQL(sql, new Object[]{tagId,osnID});
            return true;
        }
        catch (Exception e){
            logError(e);
        }
        return false;
    }

    public static boolean deleteTagUser(String osnID){
        try{
            String sql = "delete from t_tag_user where osnID=? ";
            mDB.execSQL(sql, new Object[]{osnID});
            return true;
        }
        catch (Exception e){
            logError(e);
        }
        return false;
    }

    public static void deleteConversationTag(int tagId){

        int[] types = new int[]{0,1};
        int[] lines = new int[]{0};

        List<ConversationInfo> convs = listAllConversations(types, lines, tagId);
        List<String> users = new ArrayList<>();
        List<String> groups = new ArrayList<>();

        for (ConversationInfo conv : convs) {
            if (conv.conversation.target.startsWith("OSNG")) {
                updateConversationTag(conv.conversation.target, -2);
                groups.add(conv.conversation.target);
            } else {
                updateConversationTag(conv.conversation.target, -1);
            }
        }

        System.out.println("@@@  deleteConversationTag " + convs.size() + "  " + users.size() + "  " +groups.size());

        /*if (users.size() > 0) {
            updateConversationTag(-1, users);
        }

        if (groups.size() > 0) {
            updateConversationTag(-2, groups);
        }*/


        /*try{
            String sql = "update t_conversation set tagId=-1 " +
                    "where tagId=? ";
            mDB.execSQL(sql, new Object[]{tagId});
        }
        catch (Exception e){
            System.out.println("@@@ error:" + e.getMessage());
            //logError(e);
        }*/
    }

    /*public static void updateConversationTag(int tagId, List<String> osnIdSet){

        for (String osnId : osnIdSet) {
            try{
                String sql = "update t_conversation set tagId=-1 " +
                        "where tagId=? and target=?";
                mDB.execSQL(sql, new Object[]{tagId, osnId});
            }
            catch (Exception e){
                System.out.println("@@@ error:" + e.getMessage());
                //logError(e);
                return;
            }
        }
    }*/



    public static boolean deleteTag(int tagId){
        deleteTagUsers(tagId);
        // 加这一句
        deleteConversationTag(tagId);
        try{
            String sql = "delete from t_tag where _id=? ";
            mDB.execSQL(sql, new Object[]{tagId});
            return true;
        }
        catch (Exception e){
            logError(e);
        }
        return false;
    }

    public static void deleteTagUsers(int tagId){
        try{
            String sql = "delete from t_tag_user where tagId=? ";
            mDB.execSQL(sql, new Object[]{String.valueOf(tagId)});
        }
        catch (Exception e){
            logError(e);
        }
    }
   /* public static void deleteTag(int tagId){

        deleteTagUsers(tagId);
        try{
            String sql = "delete from t_tag where tagId=? ";
            mDB.execSQL(sql, new Object[]{String.valueOf(tagId)});
        }
        catch (Exception e){
            logError(e);
        }
    }*/

    /*public static int getUserTagId(String osnID){

        int tagId = -1;

        try{
            Cursor cursor = mDB.rawQuery("select * from t_tag_user where osnID=?", new String[]{osnID});
            if(cursor.moveToNext()){
                tagId = cursor.getInt(cursor.getColumnIndex("tagId"));
            }

            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }

        if (tagId == -1) {
            if (osnID.startsWith("OSNG")) {
                tagId = -2;
            }
        }
        return tagId;
    }*/

    public static int getUserTagId(String osnID){
        int tagId = -1;
        try{
            Cursor cursor = mDB.rawQuery("select * from t_tag_user where osnID=?", new String[]{osnID});
            if(cursor.moveToNext()){
                tagId = cursor.getInt(cursor.getColumnIndex("tagId"));
            }

            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }

        if (tagId == -1) {
            if (osnID.startsWith("OSNG")) {
                tagId = -2;
            }
        }
        return tagId;
    }

    public static void insertConversation(int type, String target, int line){

        int tagId = -1;

        if (type == 4) {
            tagId = -1;
        } else {
            tagId = getUserTagId(target);
        }

        //System.out.println("@@@@ insert conversation : "+target);
        try{
            String sql = "insert into t_conversation(type,target,line,timestamp,tagId) values(?,?,?,?,?)";
            mDB.execSQL(sql, new Object[]{type,target,line,System.currentTimeMillis(),tagId});
        }
        catch (Exception e){
            logInfo("@@@@ insert conversation error.");
            logError(e);
        }

        //ConversationInfo conv = queryConversation(type, target, 0);
        //System.out.println("@@@@ query conversation : "+conv);

    }
    public static void deleteConversation(int type, String target, int line){
        try{
            String sql = "delete from t_conversation where type=? and target=? and line=?";
            mDB.execSQL(sql, new Object[]{type,target,line});
        }
        catch (Exception e){
            logError(e);
        }
    }
    public static ConversationInfo queryConversation(int type, String target, int line){
        ConversationInfo conversationInfo = null;
        try{
            Cursor cursor = mDB.rawQuery("select * from t_conversation where type=? and target=? and line=?", new String[]{String.valueOf(type), target, String.valueOf(line)});
            if(cursor.moveToNext())
                conversationInfo = getConversation(cursor);
            cursor.close();
        }
        catch (Exception e){
            System.out.println("@@@@ queryConversation failed.");
            logError(e);
        }
        return conversationInfo;
    }
    public static void updateConversation(ConversationInfo conversationInfo){
        try{
            String sql = "update t_conversation set timestamp=?, draft=?, unreadCount=?, " +
                    "unreadMention=?, unreadMentionAll=?, isTop=?, isSilent=? " +
                    "where type=? and target=? and line=? and tagId=?";
            mDB.execSQL(sql, new Object[]{conversationInfo.timestamp,conversationInfo.draft,
                    conversationInfo.unreadCount.unread,conversationInfo.unreadCount.unreadMention,
                    conversationInfo.unreadCount.unreadMentionAll,(conversationInfo.isTop?1:0),
                    (conversationInfo.isSilent?1:0),conversationInfo.conversation.type.getValue(),
                    conversationInfo.conversation.target,conversationInfo.conversation.line,
                    conversationInfo.tagId});
        }
        catch (Exception e){
            logError(e);
        }
    }

    public static void updateConversationTag(String osnID, int tagId){
        System.out.println("@@@ updateConversationTag: " + osnID);
        System.out.println("@@@ updateConversationTag: " + tagId);
        try{
            String sql = "update t_conversation set tagId=? " +
                    "where target=?";
            mDB.execSQL(sql, new Object[]{tagId, osnID});
        }
        catch (Exception e){
            System.out.println("@@@ error:" + e.getMessage());
            //logError(e);
        }
    }

    public static void updateConversation(ConversationInfo conversationInfo, List<String> keys){
        try{
            ArrayList<Object> arrayList = new ArrayList<>();
            for(String k : keys){
                String sql = null;
                arrayList.clear();
                switch(k){
                    case "top":
                        sql = "update t_conversation set isTop=?";
                        arrayList.add((conversationInfo.isTop?1:0));
                        break;
                    case "silent":
                        sql = "update t_conversation set isSilent=?";
                        arrayList.add((conversationInfo.isSilent?1:0));
                        break;
                    case "draft":
                        sql = "update t_conversation set draft=?";
                        arrayList.add(conversationInfo.draft);
                        break;
                    case "timestamp":
                        sql = "update t_conversation set timestamp=?";
                        arrayList.add(conversationInfo.timestamp);
                        break;
                    case "tagId":
                        sql = "update t_conversation set tagId=?";
                        arrayList.add(conversationInfo.tagId);
                        break;
                    case "isMember":
                        sql = "update t_conversation set isMember=?";
                        arrayList.add(conversationInfo.isMember);
                        break;
                }
                if(sql == null)
                    continue;
                arrayList.add(conversationInfo.conversation.type.getValue());
                arrayList.add(conversationInfo.conversation.target);
                arrayList.add(conversationInfo.conversation.line);
                sql += " where type=? and target=? and line=?";
                mDB.execSQL(sql, arrayList.toArray());
            }
        }
        catch (Exception e){
            logError(e);
        }
    }
    public static void clearConversation(){
        try{
            String sql = "delete from t_conversation";
            mDB.execSQL(sql);
        }
        catch (Exception e){
            logError(e);
        }
    }
    public static void clearConversationUnread(int type, String target, int line){
        try{
            String sql = "update t_conversation set unreadCount=0, unreadMention=0,unreadMentionAll=0 where type=? and target=? and line=?";
            mDB.execSQL(sql, new Object[]{type,target,line});
        }
        catch (Exception e){
            logError(e);
        }
    }
    public static List<ConversationInfo> listConversations(int type, int line){
        List<ConversationInfo> conversationInfoList = new ArrayList<>();
        try{
            Cursor cursor = mDB.rawQuery("select * from t_conversation where type=? and line=? order by timestamp desc", new String[]{String.valueOf(type),String.valueOf(line)});
            while(cursor.moveToNext()){
                ConversationInfo conversationInfo = getConversation(cursor);
                conversationInfoList.add(conversationInfo);
            }
        }
        catch (Exception e){
            logError(e);
        }
        return conversationInfoList;
    }
    public static List<ConversationInfo> listAllConversations(int[] types, int[] lines, int tagId){

        if (tagId == -3) {
            return listAllConversations(types, lines);
        }

        JSONObject json = new JSONObject();
        json.put("types", types);
        json.put("lines", lines);
        //System.out.println("@@@ json:" + json);
        List<ConversationInfo> conversationInfoList = new ArrayList<>();
        if(mDB == null){
            return conversationInfoList;
        }
        try{
            String[] args = new String[types.length*3];
            StringBuilder sql = new StringBuilder("select * from t_conversation where ");

            //sql.append("tagId=? and ");
            //args[0] = String.valueOf(tagId);

            for(int i = 0; i < types.length; ++i){
                if(i > 0)
                    sql.append("or ");
                sql.append("(tagId=? and type=? and line=?) ");
                args[i*3] = String.valueOf(tagId);
                args[i*3+1] = String.valueOf(types[i]);
                args[i*3+1+1] = String.valueOf(lines[0]);
            }

            sql.append("order by timestamp desc");

            Cursor cursor = mDB.rawQuery(sql.toString(), args);

            while(cursor.moveToNext()){
                ConversationInfo conversationInfo = getConversation(cursor);
                conversationInfoList.add(conversationInfo);
            }
            //System.out.println("@@@ sql list all size : " + conversationInfoList.size());
        }
        catch (Exception e){
            logError(e);
        }
        return conversationInfoList;
    }

    public static List<ConversationInfo> listAllConversations(int[] types, int[] lines){
        JSONObject json = new JSONObject();
        json.put("types", types);
        json.put("lines", lines);
        //System.out.println("@@@ json:" + json);
        List<ConversationInfo> conversationInfoList = new ArrayList<>();
        if(mDB == null){
            return conversationInfoList;
        }
        try{
            String[] args = new String[types.length*2];
            StringBuilder sql = new StringBuilder("select * from t_conversation where ");

            //sql.append("tagId=? and ");
            //args[0] = String.valueOf(tagId);

            for(int i = 0; i < types.length; ++i){
                if(i > 0)
                    sql.append("or ");
                sql.append("(type=? and line=?) ");

                args[i*2] = String.valueOf(types[i]);
                args[i*2+1] = String.valueOf(lines[0]);
            }

            sql.append("order by timestamp desc");
            Cursor cursor = mDB.rawQuery(sql.toString(), args);

            while(cursor.moveToNext()){
                ConversationInfo conversationInfo = getConversation(cursor);
                if (conversationInfo.unreadCount.unread != 0) {

                }
                conversationInfoList.add(conversationInfo);
            }
            //System.out.println("@@@ sql list all size : " + conversationInfoList.size());
        }
        catch (Exception e){
            logError(e);
        }
        return conversationInfoList;
    }

    public static long insertMessage(Message msg){
        try{
            String data;
            JSONObject json = new JSONObject();
            switch(msg.content.getMessageContentType()){
                case MessageContentType.ContentType_Text:
                    TextMessageContent textMessageContent = (TextMessageContent)msg.content;
                    json.put("text",textMessageContent.getContent());
                    String contents = msg.contents;
                    if(contents != null){
                        System.out.println("[Quote] insert message :" + msg.contents);
                        JSONObject jsonObject = JSONObject.parseObject(msg.contents);
                        String quoteInfo = jsonObject.getString("quoteInfo");
                        json.put("quoteInfo",quoteInfo);
                    }
                    break;
                case MessageContentType.ContentType_File:
                    FileMessageContent fileMessageContent = (FileMessageContent)msg.content;
                    json.put("localPath", fileMessageContent.localPath);
                    json.put("remoteUrl", fileMessageContent.remoteUrl);
                    json.put("decKey",fileMessageContent.decKey);
                    json.put("name", fileMessageContent.getName());
                    json.put("size", fileMessageContent.getSize());
                    break;
                case MessageContentType.ContentType_Voice:
                    SoundMessageContent soundMessageContent = (SoundMessageContent)msg.content;
                    json.put("localPath", soundMessageContent.localPath);
                    json.put("remoteUrl", soundMessageContent.remoteUrl);
                    json.put("decKey",soundMessageContent.decKey);
                    json.put("duration",soundMessageContent.getDuration());
                    break;
                case MessageContentType.ContentType_Video:
                    VideoMessageContent videoMessageContent = (VideoMessageContent)msg.content;
                    json.put("localPath", videoMessageContent.localPath);
                    json.put("remoteUrl", videoMessageContent.remoteUrl);
                    json.put("decKey",videoMessageContent.decKey);
                    byte[] thumbnail = videoMessageContent.getThumbnailBytes();
                    if(thumbnail != null)
                        json.put("thumbnail", Base64.encodeToString(thumbnail,Base64.NO_WRAP));
                    break;
                case MessageContentType.ContentType_Image:
                    ImageMessageContent imageMessageContent = (ImageMessageContent)msg.content;
                    json.put("localPath", imageMessageContent.localPath);
                    json.put("remoteUrl", imageMessageContent.remoteUrl);
                    json.put("width", imageMessageContent.getImageWidth());
                    json.put("height", imageMessageContent.getImageHeight());
                    json.put("decKey",imageMessageContent.decKey);
                    break;
                case MessageContentType.ContentType_Sticker:
                    StickerMessageContent stickerMessageContent = (StickerMessageContent)msg.content;
                    json.put("localPath", stickerMessageContent.localPath);
                    json.put("remoteUrl", stickerMessageContent.remoteUrl);
                    json.put("width", stickerMessageContent.width);
                    json.put("height", stickerMessageContent.height);
                    break;
                case MessageContentType.ContentType_Friend_Added:
                case MessageContentType.ContentType_Friend_Greeting:
                    break;
                case MessageContentType.ContentType_CREATE_GROUP:
                    CreateGroupNotificationContent createGroupNotificationContent = (CreateGroupNotificationContent)msg.content;
                    json.put("creator",createGroupNotificationContent.creator);
                    json.put("groupName",createGroupNotificationContent.groupName);
                    break;
                case MessageContentType.ContentType_KICKOF_GROUP_MEMBER:
                    KickoffGroupMemberNotificationContent kickoffGroupMemberNotificationContent = (KickoffGroupMemberNotificationContent)msg.content;
                    json.put("operator",kickoffGroupMemberNotificationContent.operator);
                    json.put("members", kickoffGroupMemberNotificationContent.kickedMembers);
                    break;
                case MessageContentType.ContentType_ADD_GROUP_MEMBER:
                    AddGroupMemberNotificationContent addGroupMemberNotificationContent = (AddGroupMemberNotificationContent)msg.content;
                    json.put("invitor",addGroupMemberNotificationContent.invitor);
                    json.put("invitees",addGroupMemberNotificationContent.invitees);
                    break;
                case MessageContentType.ContentType_QUIT_GROUP:
                    QuitGroupNotificationContent quitGroupNotificationContent = (QuitGroupNotificationContent)msg.content;
                    json.put("operator",quitGroupNotificationContent.operator);
                    break;
                case MessageContentType.ContentType_DISMISS_GROUP:
                    DismissGroupNotificationContent dismissGroupNotificationContent = (DismissGroupNotificationContent)msg.content;
                    json.put("operator",dismissGroupNotificationContent.operator);
                    break;
                case MessageContentType.ContentType_TRANSFER_GROUP_OWNER:
                    TransferGroupOwnerNotificationContent transferGroupOwnerNotificationContent = (TransferGroupOwnerNotificationContent)msg.content;
                    json.put("operator",transferGroupOwnerNotificationContent.operator);
                    json.put("newOwner",transferGroupOwnerNotificationContent.newOwner);
                    break;
                case MessageContentType.ContentType_General_Notification:
                    GroupNotifyContent groupNotifyContent = (GroupNotifyContent)msg.content;
                    json.put("info", groupNotifyContent.info);
                    break;
                case MessageContentType.ContentType_Card:
                    CardMessageContent cardMessageContent = (CardMessageContent)msg.content;
                    json.put("cardType",cardMessageContent.getType());
                    json.put("target",cardMessageContent.getTarget());
                    json.put("name",cardMessageContent.getName());
                    json.put("displayName",cardMessageContent.getDisplayName());
                    json.put("portrait",cardMessageContent.getPortrait());
                    json.put("theme",cardMessageContent.getTheme());
                    json.put("url",cardMessageContent.getUrl());
                    json.put("info",cardMessageContent.getInfo());
                    break;
                case MessageContentType.ContentType_RedPacket:
                    RedPacketMessageContent redPacketMessageContent = (RedPacketMessageContent)msg.content;
                    json.put("id", redPacketMessageContent.id);
                    //json.put("state", redPacketMessageContent.state);
                    json.put("text", redPacketMessageContent.text);
                    json.put("info", redPacketMessageContent.info);
                    break;
                case MessageContentType.ContentType_Call:
                    CallMessageContent callMessageContent = (CallMessageContent) msg.content;
                    json.put("id", callMessageContent.id);
                    json.put("type", callMessageContent.type);
                    json.put("mode", callMessageContent.mode);
                    json.put("action", callMessageContent.action);
                    json.put("status", callMessageContent.status);
                    json.put("url", callMessageContent.url);
                    json.put("user", callMessageContent.user);
                    json.put("duration", callMessageContent.duration);
                    json.put("voiceBaseUrl",callMessageContent.voiceBaseUrl);
                    json.put("voiceHostUrl",callMessageContent.voiceHostUrl);
                    break;
                default:
                    logInfo("unknown type: "+msg.content.getMessageContentType());
                    break;
            }

            json.put("decKey",msg.content.decKey);
            data = json.toString();
            System.out.println("[SqliteUtils] Video decKey:" + msg.content.decKey);


            long mid = tMessageID++;


            String hashIndex = msg.messageHash;
            if (hashIndex == null) {
                hashIndex = createHashIndex(mid, msg, data);
            }

            //System.out.println("@@@@@ save message:" + data);
            if (msg.messageHash0 == null) {
                msg.messageHash0 = msg.messageHash;
            }

            String sql = "insert into t_message(mid,uid,osnID,cType,target,dir,state,timestamp,msgType,msgText,msgHash0,msgHash,hashIndex) " +
                    "values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
            mDB.execSQL(sql, new Object[]{mid,mid,msg.sender,msg.conversation.type.getValue(),msg.conversation.target,
                    msg.direction.value(),msg.status.value(),msg.serverTime,msg.content.getMessageContentType(),
                    data,msg.messageHash0,msg.messageHash,hashIndex});
            //logInfo("mid: " + mid+", timestamp: "+msg.serverTime);
            insertLastMessage(msg, mid, hashIndex, data);
            return mid;
        }
        catch (Exception e){
            //System.out.println("@@@@@ insertMessage Exception="+e.getMessage());

            logError(e);
        }
        return 0;
    }

    public static long insertLastMessage(Message msg, long mid, String hashIndex, String data){

        //做一个容错
        Conversation conv = new Conversation(msg.conversation.type, msg.conversation.target);

        Message lastMessage = getLastMessage(conv);
        if (lastMessage != null) {
            if (lastMessage.serverTime > msg.serverTime) {
                System.out.println("@@@@ insertLastMessage thie message server time < last message time.");
                return 0;
            }
        }


        try{



            String sql = "replace into t_last_message(osnID,mid,uid,cType," +
                    "target,dir,state,timestamp," +
                    "msgType,msgText,msgHash0,msgHash,hashIndex) " +
                    "values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
            mDB.execSQL(sql, new Object[]{
                    msg.sender,mid,mid,msg.conversation.type.getValue(),
                    msg.conversation.target, msg.direction.value(), 1, msg.serverTime,
                    msg.content.getMessageContentType(),
                    data,msg.messageHash0,msg.messageHash,hashIndex});
            return mid;
        }
        catch (Exception e){
            System.out.println("@@@@@ insertLastMessage Exception");
            logError(e);
        }
        return 0;
    }





    private static String createHashIndex(long mid, Message message1, String data) {
        String calc = "" + mid
                + mid
                + message1.sender
                + message1.conversation.type.getValue()
                + message1.conversation.target
                + message1.direction.value()
                + message1.status.value()
                + message1.serverTime
                + message1.content.getMessageContentType()
                + data
                + message1.messageHash0
                + message1.messageHash;

        return ECUtils.osnHash(calc.getBytes());
    }

    public static void deleteMessage(long mid){
        try{
            logInfo("mid: "+mid);
            String sql = "delete from t_message where mid=?";
            mDB.execSQL(sql, new Object[]{mid});
        }
        catch (Exception e){
            logError(e);
        }
    }

    public static void deleteTimeMessage(long times,String target){
        try {
            String sql = "delete from t_message where timestamp<? and target=?";
            mDB.execSQL(sql,new String[]{String.valueOf(times), target});
        }catch (Exception e){
            logError(e);
        }
    }
    public static void clearMessage(String target){
        try{
            String sql = "delete from t_message where target=?";
            mDB.execSQL(sql, new Object[]{target});
        }
        catch (Exception e){
            logError(e);
        }
    }
    public static Message queryMessage(long mid){
        Message message = null;
        try{
            Cursor cursor = mDB.rawQuery("select * from t_message where mid=?",new String[]{String.valueOf(mid)});
            if(cursor.moveToNext())
                message = getMessage(cursor);
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return message;
    }

    public static Message queryMessageHash0(String msgHash0){
        Message message = null;
        System.out.println("@@@         msgHash0:   "+msgHash0);
        try{
            Cursor cursor = mDB.rawQuery("select * from t_message where msgHash0=?",new String[]{String.valueOf(msgHash0)});
            if(cursor.moveToNext())
                message = getMessage(cursor);
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return message;
    }

    public static boolean queryMessage(long timestamp, String target){
        boolean hasMsg = false;
        try{
            Cursor cursor = mDB.rawQuery("select * from t_message where timestamp=? and target=?",new String[]{String.valueOf(timestamp),target});
            if(cursor.moveToNext())
                hasMsg = true;
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return hasMsg;
    }
    public static Message queryMessage(String hash){
        Message message = null;
        try{
            Cursor cursor = mDB.rawQuery("select * from t_message where msgHash=?",new String[]{hash});
            if(cursor.moveToNext())
                message = getMessage(cursor);
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return message;
    }

    public static boolean isMessageExist(String hash){
        Message message = null;
        try{
            Cursor cursor = mDB.rawQuery("select (hashIndex) from t_message where hashIndex=?",new String[]{hash});
            if(cursor.moveToNext())
                return true;
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return false;
    }

    public static Message queryGroupMessageWithHash0(String hash){
        Message message = null;
        try{
            Cursor cursor = mDB.rawQuery("select * from t_message where msgHash0=?",new String[]{hash});
            if(cursor.moveToNext())
                message = getMessage(cursor);
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return message;
    }
    public static List<Message> queryMessages(Conversation conversation, long timestamp, boolean before, int count, boolean include){
        //System.out.println("@@@@@ queryMessages ");
        List<Message> messageList = new ArrayList<>();
        try{
            Cursor cursor = before
                    ? mDB.rawQuery("select * from (select * from t_message where target=? and cType=? and timestamp"+(include?"<=":"<")+"? order by timestamp desc limit ?) tmp order by timestamp",
                    new String[]{conversation.target,String.valueOf(conversation.type.getValue()),String.valueOf(timestamp),String.valueOf(count)})
                    : mDB.rawQuery("select * from t_message where target=? and cType=? and timestamp"+(include?">=":">")+"? order by timestamp limit ?",
                    new String[]{conversation.target,String.valueOf(conversation.type.getValue()),String.valueOf(timestamp),String.valueOf(count)});
            while(cursor.moveToNext()){
                Message message = getMessage(cursor);
                messageList.add(message);
            }
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return messageList;
    }
    public static List<Message> queryMessages(Conversation conversation, String keyword, boolean desc, int limit, int offset){
        List<Message> messageList = new ArrayList<>();
        try{
            Cursor cursor = mDB.rawQuery("select * from t_message where target=? and msgText like ? limit ? offset ?",
                    new String[]{conversation.target,"%"+keyword+"%",String.valueOf(limit),String.valueOf(offset)});
            while(cursor.moveToNext()){
                Message message = getMessage(cursor);
                messageList.add(message);
            }
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return messageList;
    }
    public static List<Message> queryFailureMessage(){
        List<Message> messageList = new ArrayList<>();
        try{
            Cursor cursor = mDB.rawQuery("select * from t_message where state=?",
                    new String[]{String.valueOf(MessageStatus.Send_Failure.value())});
            while(cursor.moveToNext()){
                Message message = getMessage(cursor);
                messageList.add(message);
            }
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return messageList;
    }
    public static void updateMessage(long mid, String msgJson){
        try{
            String sql = "update t_message set msgJson=? where mid=?";
            mDB.execSQL(sql, new Object[]{msgJson,mid});
        }
        catch (Exception e){
            logError(e);
        }
    }
    public static void updateMessage(long mid, int state){
        try{
            String sql = "update t_message set state=? where mid=?";
            mDB.execSQL(sql, new Object[]{state,mid});
        }
        catch (Exception e){
            logError(e);
        }
    }
    public static void updateMessage(long mid, int state, String msgHash){
        try{
            System.out.println("[QuoteMessage] updateMessage mid:" + mid);
            System.out.println("[QuoteMessage] updateMessage msgHash:" + msgHash);
            String sql = "update t_message set state=?,msgHash=?,msgHash0=? where mid=?";
            mDB.execSQL(sql, new Object[]{state,msgHash,msgHash,mid});
        }
        catch (Exception e){
            System.out.println("[QuoteMessage] updateMessage Exception");
            logError(e);
        }
    }

    public static void recallMessage(Message message){
        try{
            String data;
            JSONObject json = new JSONObject();
            RecallMessageContent content = (RecallMessageContent)message.content;
            json.put("operatorId", content.operatorId);
            data = json.toString();
            String sql = "update t_message set msgText=?, msgType=? where mid=?";
            mDB.execSQL(sql, new Object[]{data,message.content.getMessageContentType(),message.messageUid});
        }
        catch (Exception e){
            logError(e);
        }
    }

    public static void updateHash(Message message){
        try{
            String sql = "update t_message set msgHash=?, msgHash0=? where mid=?";
            mDB.execSQL(sql, new Object[]{message.messageHash, message.messageHash0,message.messageUid});
        }
        catch (Exception e){
            logError(e);
        }
    }
/*

    public static Message getLastMessage(Conversation conversation){
        Message message = null;
        try {
            Cursor cursor = mDB.rawQuery("select * from t_message where target=? and cType=? order by timestamp desc limit 1",
                    new String[]{conversation.target, String.valueOf(conversation.type.getValue())});
            if(cursor.moveToNext())
                message = getMessage(cursor);
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return message;
    }
*/

    public static Message getLastMessage(Conversation conversation){
        Message message = null;
        try {
            //System.out.println("[getLastMessage] cType = " + conversation.type.getValue());
            Cursor cursor = mDB.rawQuery("select * from t_last_message where target=?",
                    new String[]{conversation.target});
            if(cursor.moveToNext()){
                System.out.println("[getLastMessage] count: " + cursor.getCount());
                message = getMessage(cursor);
            } else {
                System.out.println("[getLastMessage] cursor null cType = " + conversation.type.getValue());
            }

            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return message;
    }


    public static Message getLastNotify(){
        Message message = null;
        try {
            Cursor cursor = mDB.rawQuery("select * from t_message where cType=? order by timestamp desc limit 1",
                    new String[]{"5"});
            if(cursor.moveToNext())
                message = getMessage(cursor);
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return message;
    }
    public static void insertGroup(GroupInfo groupInfo){
        try {
            System.out.println("[SqliteUtils] insertGroup group extra : " + groupInfo.extra);
            String sql = "insert or replace into t_group(" +
                    "groupID,name,portrait,attribute," +
                    "timeInterval,owner,type,joinType," +
                    "passType,mute,memberCount,fav," +
                    "redPacket,notice,showAlias,extra) " +
                    "values(?,?,?,?," +
                    "?,?,?,?," +
                    "?,?,?,?," +
                    "?,?,?,?)";
            mDB.execSQL(sql, new Object[]{
                    groupInfo.target, groupInfo.name, groupInfo.portrait, groupInfo.attribute,
                    groupInfo.timeInterval, groupInfo.owner, groupInfo.type.value(), groupInfo.joinType,
                    groupInfo.passType, groupInfo.mute, groupInfo.memberCount,groupInfo.fav,
                    groupInfo.redPacket, groupInfo.notice,groupInfo.showAlias, groupInfo.extra});
        }
        catch (Exception e){
            logError(e);
        }
    }
    public static void deleteGroup(String groupID){
        try{
            String sql = "delete from t_group where groupID=?";
            mDB.execSQL(sql, new Object[]{groupID});
        }
        catch (Exception e){
            logError(e);
        }
    }
    public static void updateGroup(GroupInfo groupInfo, List<String> keys){
        try{
            String sql = null;
            ArrayList<Object> arrayList = new ArrayList<>();
            for(String k:keys) {
                arrayList.clear();
                switch(k){
                    case "name":
                        sql = "update t_group set name=?";
                        arrayList.add(groupInfo.name);
                        break;
                    case "portrait":
                        sql = "update t_group set portrait=?";
                        arrayList.add(groupInfo.portrait);
                        break;
                    case "attribute":
                        sql = "update t_group set attribute=?";
                        arrayList.add(groupInfo.attribute);
                        break;
                    case "timeInterval":
                        sql = "update t_group set timeInterval=?";
                        arrayList.add(groupInfo.timeInterval);
                        break;
                    case "fav":
                        sql = "update t_group set fav=?";
                        arrayList.add(groupInfo.fav);
                        break;
                    case "redPacket":
                        sql = "update t_group set redPacket=?";
                        arrayList.add(groupInfo.redPacket);
                        break;
                    case "showAlias":
                        sql = "update t_group set showAlias=?";
                        arrayList.add(groupInfo.showAlias);
                        break;
                    case "memberCount":
                        sql = "update t_group set memberCount=?";
                        arrayList.add(groupInfo.memberCount);
                        break;
                    case "type":
                        sql = "update t_group set type=?";
                        arrayList.add(groupInfo.type.value());
                        break;
                    case "joinType":
                        sql = "update t_group set joinType=?";
                        arrayList.add(groupInfo.joinType);
                        break;
                    case "passType":
                        sql = "update t_group set passType=?";
                        arrayList.add(groupInfo.passType);
                        break;
                    case "mute":
                        sql = "update t_group set mute=?";
                        arrayList.add(groupInfo.mute);
                        break;
                    case "extra":
                        sql = "update t_group set extra=?";
                        arrayList.add(groupInfo.extra);
                        break;
                    case "notice":
                    case "billboard":
                        sql = "update t_group set notice=?";
                        arrayList.add(groupInfo.notice);
                        break;
                }
                sql += " where groupID=?";
                arrayList.add(groupInfo.target);
                mDB.execSQL(sql, arrayList.toArray());
            }
        }
        catch (Exception e){
            logError(e);
        }
    }
    public static GroupInfo queryGroup(String groupID){
        GroupInfo groupInfo = null;
        try{
            Cursor cursor = mDB.rawQuery("select * from t_group where groupID=?", new String[]{groupID});
            if(cursor.moveToNext())
                groupInfo = getGroupInfo(cursor);
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return groupInfo;
    }
    public static List<GroupInfo> listGroups(){
        List<GroupInfo> groupInfoList = new ArrayList<>();
        try{
            Cursor cursor = mDB.rawQuery("select * from t_group", null);
            while(cursor.moveToNext()){
                GroupInfo groupInfo = getGroupInfo(cursor);
                groupInfoList.add(groupInfo);
            }
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return groupInfoList;
    }

    public static List<WalletsInfo> listWallets() {
        List<WalletsInfo> dataList = new ArrayList<>();
        if(mDB == null){
            return dataList;
        }
        try{
            Cursor cursor = mDB.rawQuery("select * from t_wallets2", null);
            while(cursor.moveToNext()){
                WalletsInfo walletsInfo = getWalletsInfo(cursor);
                dataList.add(walletsInfo);
            }
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return dataList;
    }

    public static void deleteWallets(String osnID){
        try{
            String sql = "delete from t_wallets2 where osnID=?";
            mDB.execSQL(sql, new Object[]{osnID});
        }
        catch (Exception e){
            logError(e);
        }
    }

    public static List<CollectInfo> listCollect() {
        List<CollectInfo> dataList = new ArrayList<>();
        if(mDB == null){
            return dataList;
        }
        try{
            Cursor cursor = mDB.rawQuery("select * from t_collect", null);
            while(cursor.moveToNext()){
                CollectInfo collectInfo = getCollectInfo(cursor);
                dataList.add(collectInfo);
            }
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return dataList;
    }

    public static String readWallet(String walletId) {

        try{
            Cursor cursor = mDB.rawQuery("select * from t_wallets2 where osnID=?", new String[]{walletId});
            //Cursor cursor = mDB.rawQuery("select * from t_group where groupID=?", new String[]{groupID});
            WalletsInfo walletsInfo = null;
            if (cursor.moveToNext()){
                walletsInfo = getWalletsInfo(cursor);
            }
            cursor.close();
            if (walletsInfo != null) {
                return walletsInfo.wallets;
            }
        }
        catch (Exception e){
            logError(e);
        }
        return null;
    }

    public static String readCollect(String collect) {

        try{
            Cursor cursor = mDB.rawQuery("select * from t_collect where osnID=?", new String[]{collect});
            //Cursor cursor = mDB.rawQuery("select * from t_group where groupID=?", new String[]{groupID});
            CollectInfo collectInfo = null;
            if (cursor.moveToNext()){
                collectInfo = getCollectInfo(cursor);
            }
            cursor.close();
            if (collectInfo != null) {
                return collectInfo.collect;
            }
        }
        catch (Exception e){
            logError(e);
        }
        return null;
    }


    public static List<LitappInfo> listLitapps(){
        List<LitappInfo> litappInfos = new ArrayList<>();
        if(mDB == null){
            return litappInfos;
        }
        try{
            Cursor cursor = mDB.rawQuery("select * from t_dapp", null);
            while(cursor.moveToNext()){
                LitappInfo litappInfo = getLitappInfo(cursor);
                litappInfos.add(litappInfo);
            }
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return litappInfos;
    }

    public static List<LitappInfo> listCollectLitapps(){
        List<LitappInfo> litappInfos = new ArrayList<>();
        if(mDB == null){
            return litappInfos;
        }
        try{
            Cursor cursor = mDB.rawQuery("select * from t_collectdapp", null);
            while(cursor.moveToNext()){
                LitappInfo litappInfo = getLitappInfo(cursor);
                litappInfos.add(litappInfo);
            }
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return litappInfos;
    }

    public static void insertLitapp(LitappInfo litappInfo){
        try {
            String sql = "insert or replace into t_dapp(" +
                    "target,name,displayName,portrait," +
                    "theme,url,param,info) " +
                    "values(?,?,?,?,?,?,?,?)";
            mDB.execSQL(sql, new Object[]{
                    litappInfo.target, litappInfo.name, litappInfo.displayName, litappInfo.portrait,
                    litappInfo.theme,litappInfo.url, litappInfo.param, litappInfo.info});
            System.out.println("[t_dapp] insert :" + litappInfo.getDappInfo().toString());
        }
        catch (Exception e){
            System.out.println("[t_dapp] insert failed.");
            logError(e);
        }
    }

    public static void insertCollectLitapp(LitappInfo litappInfo){
        try {
            String sql = "insert or replace into t_collectdapp(" +
                    "target,name,displayName,portrait," +
                    "theme,url,param,info) " +
                    "values(?,?,?,?,?,?,?,?)";
            mDB.execSQL(sql, new Object[]{
                    litappInfo.target, litappInfo.name, litappInfo.displayName, litappInfo.portrait,
                    litappInfo.theme, litappInfo.url,litappInfo.param, litappInfo.info});
            System.out.println("[t_collectdapp] insert :" + litappInfo.getDappInfo().toString());
        }
        catch (Exception e){
            System.out.println("[t_collectdapp] insert failed.");
            logError(e);
        }
    }

    public static boolean insertWallets(WalletsInfo walletsInfo){
        if (mDB == null) {
            System.out.println("[t_wallets2] insertWallets mDB == null");
            return false;
        }
        try {
            System.out.println("[t_wallets2] insertWallets "+walletsInfo.wallets);
            String sql = "insert or replace into t_wallets2(OsnID,name,wallets) " +
                    "values(?,?,?)";
            mDB.execSQL(sql, new Object[]{walletsInfo.OsnID,walletsInfo.name,walletsInfo.wallets});
            return true;
        }
        catch (Exception e){
            System.out.println("[t_wallets2] insertWallets deful  = "+ e.toString());
            logError(e);
        }
        return false;
    }

    public static LitappInfo queryLitapp(String target){
        LitappInfo litappInfo = null;
        try{
            Cursor cursor = mDB.rawQuery("select * from t_dapp where target=?", new String[]{target});
            if(cursor.moveToNext())
                litappInfo = getLitappInfo(cursor);
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return litappInfo;
    }

    public static LitappInfo queryCollectLitapp(String target){
        LitappInfo litappInfo = null;
        try{
            Cursor cursor = mDB.rawQuery("select * from t_collectdapp where target=?", new String[]{target});
            if(cursor.moveToNext())
                litappInfo = getLitappInfo(cursor);
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return litappInfo;
    }

    public static void deleteLitapp(String target){
        try{
            String sql = "delete from t_dapp where target=?";
            mDB.execSQL(sql, new Object[]{target});
        }
        catch (Exception e){
            logError(e);
        }
    }

    public static void deleteCollectLitapp(String target){
        try{
            String sql = "delete from t_collectdapp where target=?";
            mDB.execSQL(sql, new Object[]{target});
        }
        catch (Exception e){
            logError(e);
        }
    }

    public static void insertMembers(List<GroupMember> members){

        if (members == null){
            return;
        }
        if (members.size() == 0) {
            return;
        }

        int min = members.get(0).index;
        int max = members.get(members.size()-1).index;
        System.out.println("@@@@ insertMembers 2 group:" +members.get(0).groupId+
                " zone[" +min+
                "," +max+
                "] size:" + members.size());
        deleteMembers(members.get(0).groupId, min, max);

        /*if (members.size() == 7) {
            for (GroupMember m : members) {

                GroupMember temp = queryMember(m.groupId, m.memberId);
                if (temp !=null) {
                    System.out.println("@@@@ fix member : " + m.memberId);
                }
            }
        }*/


        try {
            for (GroupMember m : members) {
                //System.out.println("@@@@ insertMembers member index :" + m.index);
                String sql = "insert or replace into t_groupMember(groupID,memberID,type,alias,mute,memberIndex) " +
                        "values(?,?,?,?,?,?)";
                mDB.execSQL(sql, new Object[]{m.groupId,m.memberId,m.type.value(),m.alias,m.mute,m.index});
            }
        }
        catch (Exception e){
            System.out.println("@@@@ insertMembers Exception : "+ e.getMessage());
            logError(e);
        }
    }

    public static void insertMembersNoIndex(List<GroupMember> members){

        if (members == null){
            return;
        }
        if (members.size() == 0) {
            return;
        }

        try {
            for (GroupMember m : members) {
                m.index = 999999;
                String sql = "insert or replace into t_groupMember(groupID,memberID,type,alias,mute,memberIndex) " +
                        "values(?,?,?,?,?,?)";
                mDB.execSQL(sql, new Object[]{m.groupId,m.memberId,m.type.value(),m.alias,m.mute,m.index});
            }
        }
        catch (Exception e){
            System.out.println("@@@@ insertMembersNoIndex Exception : "+ e.getMessage());
            logError(e);
        }
    }

    public static void deleteMembers(List<OsnMemberInfo> members){
        try{
            for(OsnMemberInfo m:members) {
                String sql = "delete from t_groupMember where groupID=? and memberID=?";
                mDB.execSQL(sql, new Object[]{m.groupID,m.osnID});
            }
        }
        catch (Exception e){
            logError(e);
        }
    }
    public static void deleteMembers(String groupID, int min, int max){
        try{
            System.out.println("@@@@ deleteMembers group:" +groupID+
                    "  zone[" +min+
                    "," +max+
                    "].");
            String sql = "delete from t_groupMember where groupID=? and memberIndex>=? and memberIndex<=?";
            System.out.println("@@@@ deleteMembers sql:"+sql);
            mDB.execSQL(sql, new Object[]{groupID, min, max});

        } catch (Exception e) {
            System.out.println("@@@@ deleteMembers Exception." + e.getMessage());
            logError(e);
        }
    }
    public static void deleteMembers(String groupID, int min){
        try{
            System.out.println("@@@@ deleteMembers group:"+groupID+"  index:" + min);
            String sql = "delete from t_groupMember where groupID=? and memberIndex>=?";
            System.out.println("@@@@ deleteMembers sql:"+sql);
            mDB.execSQL(sql, new Object[]{groupID, min});

        } catch (Exception e) {
            System.out.println("@@@@ deleteMembers group Exception :" + e.getMessage());
            logError(e);
        }
    }

    public static void updateMember(GroupMember groupMember){
        try{
            String sql = "update t_groupMember set alias=?, type=?, mute=? where groupID=? and memberID=?";
            mDB.execSQL(sql, new Object[]{groupMember.alias,groupMember.type,groupMember.mute,groupMember.groupId,groupMember.memberId});
        }
        catch (Exception e){
            logError(e);
        }
    }
    public static void updateMember(GroupMember groupMember, List<String> keys){
        try{
            ArrayList<Object> arrayList = new ArrayList<>();
            String sql = null;
            for(String k:keys) {
                arrayList.clear();
                switch(k){
                    case "alias":
                        sql = "update t_groupMember set alias=?";
                        arrayList.add(groupMember.alias);
                        break;
                    case "type":
                        sql = "update t_groupMember set type=?";
                        arrayList.add(groupMember.type.value());
                        break;
                    case "mute":
                        sql = "update t_groupMember set mute=?";
                        arrayList.add(groupMember.mute);
                        break;
                }
                sql += " where groupID=? and memberID=?";
                arrayList.add(groupMember.groupId);
                arrayList.add(groupMember.memberId);
                mDB.execSQL(sql, arrayList.toArray());
            }
        }
        catch (Exception e){
            logError(e);
        }
    }
    public static void clearMembers(String groupID){
        try{
            String sql = "delete from t_groupMember where groupID=?";
            mDB.execSQL(sql, new Object[]{groupID});
        }
        catch (Exception e){
            logError(e);
        }
    }
    public static List<GroupMember> queryMembers(String groupID){

        System.out.println("@@@@ queryMembers 1 group:"+groupID);

        List<GroupMember> memberList = new ArrayList<>();
        try{
            Cursor cursor = mDB.rawQuery("select * from t_groupMember where groupID=?", new String[]{groupID});
            while(cursor.moveToNext()){
                GroupMember groupMember = getMember(cursor);
                //System.out.println("@@@@ queryMembers 1 index:" + groupMember.index);
                memberList.add(groupMember);
            }
            cursor.close();
            return memberList;
        }
        catch (Exception e){
            System.out.println("@@@@ queryMembers 1 Exception:"+e.getMessage());
            //logError(e);
        }
        System.out.println("@@@@ queryMembers 1 query size:" + memberList);
        return memberList;
    }

    public static List<GroupMember> queryMembersTop(String groupID){

        //System.out.println("@@@@ queryMembersTop group:"+groupID);

        List<GroupMember> memberList = new ArrayList<>();
        try{
            Cursor cursor = mDB.rawQuery("select * from t_groupMember " +
                    "where groupID=? and memberIndex>=0 and memberIndex<26" +
                    " order by type DESC limit 26", new String[]{groupID});
            while(cursor.moveToNext()){
                GroupMember groupMember = getMember(cursor);
                //System.out.println("@@@@ queryMembers 1 index:" + groupMember.index);
                memberList.add(groupMember);
            }
            cursor.close();
            return memberList;
        }
        catch (Exception e){
            System.out.println("@@@@ queryMembersTop Exception:"+e.getMessage());
            //logError(e);
        }
        //System.out.println("@@@@ queryMembersTop query size:" + memberList);
        return memberList;
    }

    public static List<GroupMember> queryMembersAll(String groupID){

        List<GroupMember> memberList = new ArrayList<>();
        try{
            Cursor cursor = mDB.rawQuery("select * from t_groupMember where groupID=? order by type DESC", new String[]{groupID});
            while(cursor.moveToNext()){
                GroupMember groupMember = getMember(cursor);
                //System.out.println("@@@@ queryMembers 1 index:" + groupMember.index);
                memberList.add(groupMember);
            }
            cursor.close();
            return memberList;
        }
        catch (Exception e){
            System.out.println("@@@@ queryMembersTop Exception:"+e.getMessage());
            //logError(e);
        }
        //System.out.println("@@@@ queryMembersTop query size:" + memberList);
        return memberList;
    }

    public static List<GroupMember> queryMembersManager(String groupID){

        List<GroupMember> memberList = new ArrayList<>();
        try{
            Cursor cursor = mDB.rawQuery("select * from t_groupMember where groupID=? and type>0 and type<3 order by type DESC", new String[]{groupID});
            while(cursor.moveToNext()){
                GroupMember groupMember = getMember(cursor);
                //System.out.println("@@@@ queryMembers 1 index:" + groupMember.index);
                memberList.add(groupMember);
            }
            cursor.close();
            return memberList;
        }
        catch (Exception e){
            System.out.println("@@@@ queryMembersTop Exception:"+e.getMessage());
            //logError(e);
        }
        //System.out.println("@@@@ queryMembersTop query size:" + memberList);
        return memberList;
    }


    public static List<GroupMember> queryMembers(String groupID, int begin, int end){

        System.out.println("@@@@ queryMembers 2 group:" +groupID+
                " zone[" +begin+
                "," +end+
                "]");

        List<GroupMember> memberList = new ArrayList<>();
        try{
            //String sql = "select * from t_groupMember where groupID=? and memberIndex&gt;=? and memberIndex&lt;?";
            String sql = "select * from t_groupMember where groupID=? and memberIndex>=? and memberIndex<?";
            /*String sql = "select * from t_groupMember where groupID=? and memberIndex>=" +begin+
                    " and memberIndex<" + end;*/

            Cursor cursor = mDB.rawQuery(
                    sql,
                    new String[]{groupID, String.valueOf(begin), String.valueOf(end)}
            );
            //Cursor cursor = mDB.rawQuery("select * from t_groupMember limit 20", null);

            while(cursor.moveToNext()){
                GroupMember groupMember = getMember(cursor);
                //System.out.println("@@@@ queryMembers 2 index :"+groupMember.index);
                memberList.add(groupMember);
            }
            cursor.close();

            //return memberList;
        }
        catch (Exception e){
            System.out.println("@@@@ queryMembers 2 Exception:"+e.getMessage());
            logError(e);
        }
        System.out.println("@@@@ queryMembers 2 query size:" + memberList.size());
        return memberList;
    }

    public static GroupMember queryMember(String groupID, String memberID){
        GroupMember groupMember = null;
        try{
            Cursor cursor = mDB.rawQuery("select * from t_groupMember where groupID=? and memberID=?", new String[]{groupID,memberID});
            if(cursor.moveToNext())
                groupMember = getMember(cursor);
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return groupMember;
    }

    public static void insertRedPacket(RedPacketInfo redPacketInfo){
        if(mDB == null)
            return;
        try {
            String sql = "insert or replace into t_redPacket(packetID,type,user,count,price,target," +
                    "text,state,unpackID,timestamp,urlQuery,urlFetch,luckNum,dapp,wallet,coinType) " +
                    "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            mDB.execSQL(sql, new Object[]{redPacketInfo.packetID,redPacketInfo.type,redPacketInfo.user,redPacketInfo.count,
                    redPacketInfo.price,redPacketInfo.target,redPacketInfo.text,redPacketInfo.state,
                    redPacketInfo.unpackID,redPacketInfo.timestamp,redPacketInfo.urlQuery,
                    redPacketInfo.urlFetch,redPacketInfo.luckNum,redPacketInfo.dapp,redPacketInfo.wallet,redPacketInfo.coinType});
        }
        catch (Exception e){
            logError(e);
        }
    }
    public static RedPacketInfo queryRedPacket(String packetID){
        RedPacketInfo redPacketInfo = null;
        try{
            Cursor cursor = mDB.rawQuery("select * from t_redPacket where packetID=?", new String[]{packetID});
            if(cursor.moveToNext())
                redPacketInfo = getRedPacket(cursor);
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return redPacketInfo;
    }
    public static void updateRedPacketState(String packetID){
        try{
            String sql = "update t_redPacket set state=1 where packetID=?";
            mDB.execSQL(sql, new Object[]{packetID});
        }
        catch (Exception e){
            logError(e);
        }
    }
    public static void insertUnpack(UnpackInfo unpackInfo){
        if(mDB == null)
            return;
        try {
            String sql = "insert or replace into t_unpack_info(user,fetcher,packetID,unpackID,price,timestamp) " +
                    "values(?,?,?,?,?,?)";
            mDB.execSQL(sql, new Object[]{unpackInfo.user,unpackInfo.fetcher,unpackInfo.packetID,unpackInfo.unpackID,unpackInfo.price,unpackInfo.timestamp});
        }
        catch (Exception e){
            logError(e);
        }
    }
    public static List<UnpackInfo> queryUnpacks(String unpackID){
        List<UnpackInfo> unpackInfoList = new ArrayList<>();
        try{
            Cursor cursor = mDB.rawQuery("select * from t_unpack_info where unpackID=?", new String[]{unpackID});
            while(cursor.moveToNext()){
                UnpackInfo unpackInfo = getUnpack(cursor);
                if(unpackID != null)
                    unpackInfoList.add(unpackInfo);
            }
            cursor.close();
        }
        catch (Exception e){
            logError(e);
        }
        return unpackInfoList;
    }

    private static LitappInfo getLitappInfo(Cursor cursor){
        LitappInfo litappInfo = new LitappInfo();
        litappInfo.target = cursor.getString(cursor.getColumnIndex("target"));
        litappInfo.name = cursor.getString(cursor.getColumnIndex("name"));
        litappInfo.displayName = cursor.getString(cursor.getColumnIndex("displayName"));
        litappInfo.portrait = cursor.getString(cursor.getColumnIndex("portrait"));
        litappInfo.theme = cursor.getString(cursor.getColumnIndex("theme"));
        litappInfo.url = cursor.getString(cursor.getColumnIndex("url"));
        litappInfo.info = cursor.getString(cursor.getColumnIndex("info"));
        litappInfo.param = cursor.getString(cursor.getColumnIndex("param"));
        return litappInfo;
    }
    private static WalletsInfo getWalletsInfo(Cursor cursor){
        WalletsInfo walletsInfo = new WalletsInfo();
        walletsInfo.OsnID = cursor.getString(cursor.getColumnIndex("osnID"));
        walletsInfo.name = cursor.getString(cursor.getColumnIndex("name"));
        walletsInfo.wallets = cursor.getString(cursor.getColumnIndex("wallets"));
        return walletsInfo;
    }
    private static CollectInfo getCollectInfo(Cursor cursor){
        CollectInfo collectInfo = new CollectInfo();
        collectInfo.OsnID = cursor.getString(cursor.getColumnIndex("osnID"));
        collectInfo.name = cursor.getString(cursor.getColumnIndex("name"));
        collectInfo.collect = cursor.getString(cursor.getColumnIndex("collect"));
        collectInfo.type = cursor.getInt(cursor.getColumnIndex("type"));
        return collectInfo;
    }

    private static GroupInfo getGroupInfo(Cursor cursor){
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.target = cursor.getString(cursor.getColumnIndex("groupID"));
        groupInfo.name = cursor.getString(cursor.getColumnIndex("name"));
        groupInfo.portrait = cursor.getString(cursor.getColumnIndex("portrait"));
        groupInfo.attribute = cursor.getString(cursor.getColumnIndex("attribute"));
        groupInfo.timeInterval = cursor.getString(cursor.getColumnIndex("timeInterval"));
        groupInfo.owner = cursor.getString(cursor.getColumnIndex("owner"));
        groupInfo.type = GroupInfo.GroupType.type(cursor.getInt(cursor.getColumnIndex("type")));
        groupInfo.memberCount = cursor.getInt(cursor.getColumnIndex("memberCount"));
        groupInfo.extra = cursor.getString(cursor.getColumnIndex("extra"));
        if (groupInfo.extra != null) {
            System.out.println("[SqliteUtils] extra:" + groupInfo.extra);
        }
        groupInfo.updateDt = cursor.getLong(cursor.getColumnIndex("updateDt"));
        groupInfo.fav = cursor.getInt(cursor.getColumnIndex("fav"));
        groupInfo.redPacket = cursor.getInt(cursor.getColumnIndex("redPacket"));
        groupInfo.mute = cursor.getInt(cursor.getColumnIndex("mute"));
        groupInfo.joinType = cursor.getInt(cursor.getColumnIndex("joinType"));
        groupInfo.passType = cursor.getInt(cursor.getColumnIndex("passType"));
        groupInfo.privateChat = cursor.getInt(cursor.getColumnIndex("privateChat"));
        groupInfo.maxMemberCount = cursor.getInt(cursor.getColumnIndex("maxMemberCount"));
        groupInfo.showAlias = cursor.getInt(cursor.getColumnIndex("showAlias"));
        groupInfo.notice = cursor.getString(cursor.getColumnIndex("notice"));
        return groupInfo;
    }
    private static UserInfo getUserInfo(Cursor cursor){
        UserInfo userInfo = new UserInfo();
        userInfo.uid = cursor.getString(cursor.getColumnIndex("osnID"));
        userInfo.name = cursor.getString(cursor.getColumnIndex("name"));
        userInfo.portrait = cursor.getString(cursor.getColumnIndex("portrait"));
        userInfo.displayName = cursor.getString(cursor.getColumnIndex("displayName"));
        userInfo.urlSpace = cursor.getString(cursor.getColumnIndex("urlSpace"));
        userInfo.role = cursor.getString(cursor.getColumnIndex("role"));
        userInfo.describes = cursor.getString(cursor.getColumnIndex("describes"));
        userInfo.friendAlias = cursor.getString(cursor.getColumnIndex("remarks"));
        //userInfo.nft = cursor.getString(cursor.getColumnIndex("nft"));
        userInfo.payState = cursor.getInt(cursor.getColumnIndex("payState"));
        userInfo.getNft();
        return userInfo;
    }
    private static Message getMessage(Cursor cursor){
        try {
            Message message = new Message();
            message.messageId = cursor.getLong(cursor.getColumnIndex("mid"));
            message.sender = cursor.getString(cursor.getColumnIndex("osnID"));
            message.conversation = new Conversation(Conversation.ConversationType.type(cursor.getInt(cursor.getColumnIndex("cType"))),
                    cursor.getString(cursor.getColumnIndex("target")), 0);
            message.direction = MessageDirection.direction(cursor.getInt(cursor.getColumnIndex("dir")));
            message.status = MessageStatus.status(cursor.getInt(cursor.getColumnIndex("state")));
            message.messageUid = cursor.getLong(cursor.getColumnIndex("uid"));
            message.serverTime = cursor.getLong(cursor.getColumnIndex("timestamp"));
            message.messageHash = cursor.getString(cursor.getColumnIndex("msgHash"));
            message.messageHash0 = cursor.getString(cursor.getColumnIndex("msgHash0"));
            message.messageJson = cursor.getString(cursor.getColumnIndex("msgJson"));
            String data = cursor.getString(cursor.getColumnIndex("msgText"));
            int msgType = cursor.getInt(cursor.getColumnIndex("msgType"));
            JSONArray array;
            JSONObject json = JSON.parseObject(data);
            switch (msgType) {
                case MessageContentType.ContentType_Text:
                    TextMessageContent textMessageContent = new TextMessageContent();
                    //textMessageContent.setContent(json.getString("text"));
                    textMessageContent.setContent2(data);

                    message.content = textMessageContent;
                    break;
                case MessageContentType.ContentType_Friend_Added:
                    message.content = new FriendAddedMessageContent();
                    break;
                case MessageContentType.ContentType_Friend_Greeting:
                    message.content = new FriendGreetingMessageContent();
                    break;
                case MessageContentType.ContentType_CREATE_GROUP:
                    CreateGroupNotificationContent groupNotificationContent = new CreateGroupNotificationContent();
                    groupNotificationContent.creator = json.getString("creator");
                    groupNotificationContent.groupName = json.getString("groupName");
                    message.content = groupNotificationContent;
                    break;
                case MessageContentType.ContentType_Image:
                    ImageMessageContent imageMessageContent = new ImageMessageContent();
                    imageMessageContent.localPath = json.getString("localPath");
                    imageMessageContent.remoteUrl = json.getString("remoteUrl");
                    imageMessageContent.imageWidth = json.getDoubleValue("width");
                    imageMessageContent.imageHeight = json.getDoubleValue("height");
                    imageMessageContent.decKey = json.getString("decKey");
                    message.content = imageMessageContent;
                    break;
                case MessageContentType.ContentType_File:
                    FileMessageContent fileMessageContent = new FileMessageContent();
                    fileMessageContent.localPath = json.getString("localPath");
                    fileMessageContent.remoteUrl = json.getString("remoteUrl");
                    fileMessageContent.setName(json.getString("name"));
                    fileMessageContent.setSize(json.getIntValue("size"));
                    fileMessageContent.decKey = json.getString("decKey");
                    message.content = fileMessageContent;
                    break;
                case MessageContentType.ContentType_Voice:
                    SoundMessageContent soundMessageContent = new SoundMessageContent();
                    soundMessageContent.localPath = json.getString("localPath");
                    soundMessageContent.remoteUrl = json.getString("remoteUrl");
                    soundMessageContent.setDuration(json.getIntValue("duration"));
                    message.content = soundMessageContent;
                    break;
                case MessageContentType.ContentType_Video:
                    VideoMessageContent videoMessageContent = new VideoMessageContent();
                    videoMessageContent.localPath = json.getString("localPath");
                    videoMessageContent.remoteUrl = json.getString("remoteUrl");
                    if(json.containsKey("thumbnail"))
                        videoMessageContent.setThumbnailBytes(Base64.decode(json.getString("thumbnail"),0));
                    message.content = videoMessageContent;
                    break;
                case MessageContentType.ContentType_Sticker:
                    StickerMessageContent stickerMessageContent = new StickerMessageContent();
                    stickerMessageContent.localPath = json.getString("localPath");
                    stickerMessageContent.remoteUrl = json.getString("remoteUrl");
                    stickerMessageContent.height = json.getIntValue("height");
                    stickerMessageContent.width = json.getIntValue("width");
                    message.content = stickerMessageContent;
                    break;
                case MessageContentType.ContentType_KICKOF_GROUP_MEMBER:
                    KickoffGroupMemberNotificationContent kickoffGroupMemberNotificationContent = new KickoffGroupMemberNotificationContent();
                    kickoffGroupMemberNotificationContent.kickedMembers = new ArrayList<>();
                    kickoffGroupMemberNotificationContent.operator = json.getString("operator");
                    array = json.getJSONArray("members");
                    if(array != null)
                        kickoffGroupMemberNotificationContent.kickedMembers.addAll(array.toJavaList(String.class));
                    message.content = kickoffGroupMemberNotificationContent;
                    break;
                case MessageContentType.ContentType_ADD_GROUP_MEMBER:
                    AddGroupMemberNotificationContent addGroupMemberNotificationContent = new AddGroupMemberNotificationContent();
                    addGroupMemberNotificationContent.invitor = json.getString("invitor");
                    addGroupMemberNotificationContent.invitees = new ArrayList<>();
                    array = json.getJSONArray("invitees");
                    if(array != null)
                        addGroupMemberNotificationContent.invitees.addAll(array.toJavaList(String.class));
                    message.content = addGroupMemberNotificationContent;
                    break;
                case MessageContentType.ContentType_QUIT_GROUP:
                    QuitGroupNotificationContent quitGroupNotificationContent = new QuitGroupNotificationContent();
                    quitGroupNotificationContent.operator = json.getString("operator");
                    message.content = quitGroupNotificationContent;
                    break;
                case MessageContentType.ContentType_DISMISS_GROUP:
                    DismissGroupNotificationContent dismissGroupNotificationContent = new DismissGroupNotificationContent();
                    dismissGroupNotificationContent.operator = json.getString("operator");
                    message.content = dismissGroupNotificationContent;
                    break;
                case MessageContentType.ContentType_TRANSFER_GROUP_OWNER:
                    TransferGroupOwnerNotificationContent transferGroupOwnerNotificationContent = new TransferGroupOwnerNotificationContent();
                    transferGroupOwnerNotificationContent.operator = json.getString("operator");
                    transferGroupOwnerNotificationContent.newOwner = json.getString("newOwner");
                    message.content = transferGroupOwnerNotificationContent;
                    break;
                case MessageContentType.ContentType_General_Notification:
                    GroupNotifyContent groupNotifyContent = new GroupNotifyContent();
                    groupNotifyContent.info = json.getString("info");
                    message.content = groupNotifyContent;
                    break;
                case MessageContentType.ContentType_Card:
                    CardMessageContent cardMessageContent = new CardMessageContent();
                    cardMessageContent.setType(json.getIntValue("cardType"));
                    cardMessageContent.setTarget(json.getString("target"));
                    cardMessageContent.setName(json.getString("name"));
                    cardMessageContent.setDisplayName(json.getString("displayName"));
                    cardMessageContent.setPortrait(json.getString("portrait"));
                    cardMessageContent.setTheme(json.getString("theme"));
                    cardMessageContent.setUrl(json.getString("url"));
                    cardMessageContent.setInfo(json.getString("info"));
                    message.content = cardMessageContent;
                    break;
                case MessageContentType.ContentType_RedPacket:
                    RedPacketMessageContent redPacketMessageContent = new RedPacketMessageContent();
                    redPacketMessageContent.id = json.getString("id");
                    redPacketMessageContent.state = message.status == MessageStatus.Opened?1:0;
                    redPacketMessageContent.text = json.getString("text");
                    redPacketMessageContent.info = json.getString("info");
                    message.content = redPacketMessageContent;
                    break;
                case MessageContentType.ContentType_Recall:
                    RecallMessageContent recallMessageContent = new RecallMessageContent();
                    recallMessageContent.operatorId = json.getString("operatorId");
                    message.content = recallMessageContent;
                    break;
                case MessageContentType.ContentType_Call:
                    CallMessageContent callMessageContent = new CallMessageContent();
                    callMessageContent.id = json.getIntValue("id");
                    callMessageContent.type = json.getIntValue("type");
                    callMessageContent.mode = json.getIntValue("mode");
                    callMessageContent.action = json.getIntValue("action");
                    callMessageContent.status = json.getString("status");
                    callMessageContent.url = json.getString("url");
                    callMessageContent.user = json.getString("user");
                    callMessageContent.duration = json.getIntValue("duration");
                    callMessageContent.voiceHostUrl = json.getString("voiceHostUrl");
                    callMessageContent.voiceBaseUrl = json.getString("voiceBaseUrl");
                    message.content = callMessageContent;
                    break;
                default:
                    message.content = new UnknownMessageContent();
                    logInfo("unknown msgType: " + msgType);
                    break;
            }
            if (message.content!=null) {
                message.decKey = json.getString("decKey");
                message.content.decKey = message.decKey;
                message.content.password = json.getString("password");
            }

            return message;
        }
        catch (Exception e){
            logError(e);
        }
        return null;
    }
    private static ConversationInfo getConversation(Cursor cursor){
        ConversationInfo conversationInfo = new ConversationInfo();
        conversationInfo.conversation = new Conversation(Conversation.ConversationType.type(cursor.getInt(1)),cursor.getString(2),cursor.getInt(3));
        conversationInfo.lastMessage = getLastMessage(conversationInfo.conversation);
        conversationInfo.timestamp = cursor.getLong(cursor.getColumnIndex("timestamp"));
        conversationInfo.draft = cursor.getString(cursor.getColumnIndex("draft"));
        conversationInfo.unreadCount = new UnreadCount();
        conversationInfo.unreadCount.unread = cursor.getInt(cursor.getColumnIndex("unreadCount"));
        conversationInfo.unreadCount.unreadMention = cursor.getInt(cursor.getColumnIndex("unreadMention"));
        conversationInfo.unreadCount.unreadMentionAll = cursor.getInt(cursor.getColumnIndex("unreadMentionAll"));
        conversationInfo.isTop = cursor.getInt(cursor.getColumnIndex("isTop")) != 0;
        conversationInfo.isSilent = cursor.getInt(cursor.getColumnIndex("isSilent")) != 0;
        conversationInfo.tagId = cursor.getInt(cursor.getColumnIndex("tagId"));
        conversationInfo.isMember = cursor.getInt(cursor.getColumnIndex("isMember"));
        conversationInfo.unreadCount.tagId = conversationInfo.tagId;
        //System.out.println("@@@  sql tagId : " +conversationInfo.tagId);
        return conversationInfo;
    }
    private static OrgTag getOrgTag(Cursor cursor){
        OrgTag tag = new OrgTag();
        try {
            tag.id = cursor.getInt(cursor.getColumnIndex("_id"));
            tag.tagName = cursor.getString(cursor.getColumnIndex("tagName"));
            tag.tagId = cursor.getInt(cursor.getColumnIndex("tagId"));
        } catch (Exception e) {

        }
        return tag;
    }

    private static GroupMember getMember(Cursor cursor){
        GroupMember groupMember = new GroupMember();
        groupMember.groupId = cursor.getString(cursor.getColumnIndex("groupID"));
        groupMember.memberId = cursor.getString(cursor.getColumnIndex("memberID"));
        groupMember.alias = cursor.getString(cursor.getColumnIndex("alias"));
        groupMember.type = GroupMember.GroupMemberType.type(cursor.getInt(cursor.getColumnIndex("type")));
        groupMember.updateDt = cursor.getLong(cursor.getColumnIndex("updateDt"));
        groupMember.createDt = cursor.getLong(cursor.getColumnIndex("createDt"));
        groupMember.mute = cursor.getInt(cursor.getColumnIndex("mute"));
        groupMember.index = cursor.getInt(cursor.getColumnIndex("memberIndex"));
        return groupMember;
    }
    private static FriendRequest getFriendRequest(Cursor cursor){
        FriendRequest request = new FriendRequest();
        request.type = cursor.getInt(cursor.getColumnIndex("type"));
        request.direction = cursor.getInt(cursor.getColumnIndex("direction"));
        request.target = cursor.getString(cursor.getColumnIndex("target"));
        request.originalUser = cursor.getString(cursor.getColumnIndex("originalUser"));
        request.userID = cursor.getString(cursor.getColumnIndex("userID"));
        request.reason = cursor.getString(cursor.getColumnIndex("reason"));
        request.status = cursor.getInt(cursor.getColumnIndex("status"));
        request.readStatus = cursor.getInt(cursor.getColumnIndex("readStatus"));
        request.timestamp = cursor.getLong(cursor.getColumnIndex("timestamp"));
        request.invitation = cursor.getString(cursor.getColumnIndex("invitation"));
        return request;
    }
    private static RedPacketInfo getRedPacket(Cursor cursor){
        RedPacketInfo info = new RedPacketInfo();
        info.type = cursor.getString(cursor.getColumnIndex("type"));
        info.user = cursor.getString(cursor.getColumnIndex("user"));
        info.count = cursor.getString(cursor.getColumnIndex("count"));
        info.price = cursor.getString(cursor.getColumnIndex("price"));
        info.target = cursor.getString(cursor.getColumnIndex("target"));
        info.packetID = cursor.getString(cursor.getColumnIndex("packetID"));
        info.text = cursor.getString(cursor.getColumnIndex("text"));
        info.state = cursor.getInt(cursor.getColumnIndex("state"));
        info.unpackID = cursor.getString(cursor.getColumnIndex("unpackID"));
        info.timestamp = cursor.getLong(cursor.getColumnIndex("timestamp"));
        info.urlQuery = cursor.getString(cursor.getColumnIndex("urlQuery"));
        info.urlFetch = cursor.getString(cursor.getColumnIndex("urlFetch"));
        info.luckNum = cursor.getString(cursor.getColumnIndex("luckNum"));
        info.dapp = cursor.getString(cursor.getColumnIndex("dapp"));
        info.wallet = cursor.getString(cursor.getColumnIndex("wallet"));
        info.coinType = cursor.getString(cursor.getColumnIndex("coinType"));
        return info;
    }
    private static UnpackInfo getUnpack(Cursor cursor){
        UnpackInfo info = new UnpackInfo();
        info.user = cursor.getString(cursor.getColumnIndex("user"));
        info.fetcher = cursor.getString(cursor.getColumnIndex("fetcher"));
        info.price = cursor.getString(cursor.getColumnIndex("price"));
        info.packetID = cursor.getString(cursor.getColumnIndex("packetID"));
        info.unpackID = cursor.getString(cursor.getColumnIndex("unpackID"));
        info.timestamp = cursor.getLong(cursor.getColumnIndex("timestamp"));
        return info;
    }
}
