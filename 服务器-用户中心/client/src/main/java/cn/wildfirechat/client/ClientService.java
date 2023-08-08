/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfirechat.client;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.ospn.osnsdk.OSNManager;
import com.ospn.osnsdk.Osnsdk;
import com.ospn.osnsdk.callback.OSNGeneralCallback;
import com.ospn.osnsdk.callback.OSNGeneralCallbackT;
import com.ospn.osnsdk.callback.OSNListener;
import com.ospn.osnsdk.callback.OSNTransferCallback;
import com.ospn.osnsdk.data.OsnFriendInfo;
import com.ospn.osnsdk.data.OsnGroupInfo;
import com.ospn.osnsdk.data.OsnMemberInfo;
import com.ospn.osnsdk.data.OsnMessageInfo;
import com.ospn.osnsdk.data.OsnRequestInfo;
import com.ospn.osnsdk.data.OsnUserInfo;
import com.ospn.osnsdk.data.serviceInfo.OsnLitappInfo;
import com.ospn.osnsdk.data.serviceInfo.OsnServiceInfo;
import com.ospn.osnsdk.utils.OsnUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import cn.wildfirechat.ContentsInfo;
import cn.wildfirechat.ErrorCode;
import cn.wildfirechat.message.CallMessageContent;
import cn.wildfirechat.message.CallStartMessageContent;
import cn.wildfirechat.message.CardMessageContent;
import cn.wildfirechat.message.FileMessageContent;
import cn.wildfirechat.message.ImageMessageContent;
import cn.wildfirechat.message.LocationMessageContent;
import cn.wildfirechat.message.MediaMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.MessageContent;
import cn.wildfirechat.message.MessageContentMediaType;
import cn.wildfirechat.message.PTextMessageContent;
import cn.wildfirechat.message.RedPacketMessageContent;
import cn.wildfirechat.message.SoundMessageContent;
import cn.wildfirechat.message.StickerMessageContent;
import cn.wildfirechat.message.TextMessageContent;
import cn.wildfirechat.message.TypingMessageContent;
import cn.wildfirechat.message.VideoMessageContent;
import cn.wildfirechat.message.core.ContentTag;
import cn.wildfirechat.message.core.MessageContentType;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.message.core.MessagePayload;
import cn.wildfirechat.message.core.MessageStatus;
import cn.wildfirechat.message.notification.AddGroupMemberNotificationContent;
import cn.wildfirechat.message.notification.ChangeGroupNameNotificationContent;
import cn.wildfirechat.message.notification.ChangeGroupPortraitNotificationContent;
import cn.wildfirechat.message.notification.CreateGroupNotificationContent;
import cn.wildfirechat.message.notification.DeleteMessageContent;
import cn.wildfirechat.message.notification.DismissGroupNotificationContent;
import cn.wildfirechat.message.notification.FriendAddedMessageContent;
import cn.wildfirechat.message.notification.FriendGreetingMessageContent;
import cn.wildfirechat.message.notification.GroupJoinTypeNotificationContent;
import cn.wildfirechat.message.notification.GroupMuteMemberNotificationContent;
import cn.wildfirechat.message.notification.GroupMuteNotificationContent;
import cn.wildfirechat.message.notification.GroupNotifyContent;
import cn.wildfirechat.message.notification.GroupPrivateChatNotificationContent;
import cn.wildfirechat.message.notification.GroupSetManagerNotificationContent;
import cn.wildfirechat.message.notification.KickoffGroupMemberNotificationContent;
import cn.wildfirechat.message.notification.ModifyGroupAliasNotificationContent;
import cn.wildfirechat.message.notification.QuitGroupNotificationContent;
import cn.wildfirechat.message.notification.RecallMessageContent;
import cn.wildfirechat.message.notification.TipNotificationContent;
import cn.wildfirechat.message.notification.TransferGroupOwnerNotificationContent;
import cn.wildfirechat.model.ChannelInfo;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.ConversationSearchResult;
import cn.wildfirechat.model.FriendRequest;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.GroupMember;
import cn.wildfirechat.model.GroupSearchResult;
import cn.wildfirechat.model.LitappInfo;
import cn.wildfirechat.model.ModifyGroupInfoType;
import cn.wildfirechat.model.ModifyMyInfoEntry;
import cn.wildfirechat.model.ModifyMyInfoType;
import cn.wildfirechat.model.NullGroupInfo;
import cn.wildfirechat.model.NullGroupMember;
import cn.wildfirechat.model.NullUserInfo;
import cn.wildfirechat.model.OrgTag;
import cn.wildfirechat.model.QuoteInfo;
import cn.wildfirechat.model.ReadEntry;
import cn.wildfirechat.model.RedPacketInfo;
import cn.wildfirechat.model.UnpackInfo;
import cn.wildfirechat.model.UnreadCount;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.model.WalletsInfo;
import cn.wildfirechat.remote.RecoverReceiver;
import cn.wildfirechat.remote.UserSettingScope;

import static cn.wildfirechat.client.ClientSvcApi.UpdateGroupInfo;
import static cn.wildfirechat.client.ConnectionStatus.ConnectionStatusConnected;
import static cn.wildfirechat.client.ConnectionStatus.ConnectionStatusConnecting;
import static cn.wildfirechat.client.ConnectionStatus.ConnectionStatusKickOffline;
import static cn.wildfirechat.client.ConnectionStatus.ConnectionStatusLogout;
import static cn.wildfirechat.client.ConnectionStatus.ConnectionStatusReceiveing;
import static cn.wildfirechat.client.ConnectionStatus.ConnectionStatusUnconnected;
import static cn.wildfirechat.message.CardMessageContent.CardType_Channel;
import static cn.wildfirechat.message.CardMessageContent.CardType_ChatRoom;
import static cn.wildfirechat.message.CardMessageContent.CardType_Group;
import static cn.wildfirechat.message.CardMessageContent.CardType_Litapp;
import static cn.wildfirechat.message.CardMessageContent.CardType_Share;
import static cn.wildfirechat.message.CardMessageContent.CardType_User;
import static cn.wildfirechat.message.MessageContentMediaType.GENERAL;
import static cn.wildfirechat.message.core.MessageStatus.Send_Failure;
import static cn.wildfirechat.message.core.MessageStatus.Sending;
import static cn.wildfirechat.model.FriendRequest.RequestType_ApplyMember;
import static cn.wildfirechat.model.FriendRequest.RequestType_Friend;
import static cn.wildfirechat.model.FriendRequest.RequestType_InviteGroup;
import static cn.wildfirechat.model.GroupMember.GroupMemberType.Manager;
import static cn.wildfirechat.model.GroupMember.GroupMemberType.Normal;
import static com.ospn.osnsdk.data.OsnMemberInfo.MemberType_Admin;
import static com.ospn.osnsdk.utils.OsnUtils.b64Decode;

public class ClientService extends Service {
    public static ClientService inst = null;
    public final static int MAX_IPC_SIZE = 800 * 1024;
    private final ClientServiceStub mBinder = new ClientServiceStub();
    private final Map<Integer, Class<? extends MessageContent>> contentMapper = new HashMap<>();
    private int mConnectionStatus;
    private String mBackupDeviceToken;
    private int mBackupPushType;
    private Handler handler;
    private Context mContext;
    private String mHost = null;
    private String mUserId = null;
    private boolean logined;
    private boolean isNetworkOn = false;
    private SharedPreferences mSp = null;
    private BufferedOutputStream mLogger = null;
    private static String mLogFileName = "client.log";
    private List<String> keywords = null;
    private final Osnsdk osnsdk = OSNManager.Instance();
    private final Object mShareLock = new Object();
    private int mNotifyIconId = 0;
    private long messageSound = 0;
    private final RemoteCallbackList<IOnReceiveMessageListener> onReceiveMessageListeners = new WfcRemoteCallbackList<>();
    private final RemoteCallbackList<IOnConnectionStatusChangeListener> onConnectionStatusChangeListenes = new WfcRemoteCallbackList<>();
    private final RemoteCallbackList<IOnFriendUpdateListener> onFriendUpdateListenerRemoteCallbackList = new WfcRemoteCallbackList<>();
    private final RemoteCallbackList<IOnUserInfoUpdateListener> onUserInfoUpdateListenerRemoteCallbackList = new WfcRemoteCallbackList<>();
    private final RemoteCallbackList<IOnGroupInfoUpdateListener> onGroupInfoUpdateListenerRemoteCallbackList = new WfcRemoteCallbackList<>();
    private final RemoteCallbackList<IOnLitappInfoUpdateListener> onLitappInfoUpdateListenerRemoteCallbackList = new WfcRemoteCallbackList<>();
    private final RemoteCallbackList<IOnLitappInfoResultListener> onLitappInfoResultListenerRemoteCallbackList = new WfcRemoteCallbackList<>();
    private final RemoteCallbackList<IOnSettingUpdateListener> onSettingUpdateListenerRemoteCallbackList = new WfcRemoteCallbackList<>();
    private final RemoteCallbackList<IOnChannelInfoUpdateListener> onChannelInfoUpdateListenerRemoteCallbackList = new WfcRemoteCallbackList<>();
    private final RemoteCallbackList<IOnGroupMembersUpdateListener> onGroupMembersUpdateListenerRemoteCallbackList = new WfcRemoteCallbackList<>();
    private final RemoteCallbackList<IOnConferenceEventListener> onConferenceEventListenerRemoteCallbackList = new WfcRemoteCallbackList<>();
    private static final SimpleDateFormat mFormater= new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ");





    public void logInfoFile(String text){
        try {
            if(mLogger == null){
                String path = getFilesDir().getAbsolutePath() + "/" + mLogFileName;
                System.out.println("logfile: "+path);
                File file = new File(path);
                if(file.exists() && file.length() > 20*1024*1024){
                    System.out.println("delete logfile");
                    file.delete();
                }
                mLogger = new BufferedOutputStream(new FileOutputStream(path, true));
            }
            mLogger.write(text.getBytes());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void logError(Exception e){
        StringBuilder sb = new StringBuilder();
        Date date = new Date(System.currentTimeMillis());
        String time = mFormater.format(date);
        sb.append(time);
        for(StackTraceElement element : e.getStackTrace()){
            sb.append(element.toString()).append("\r\n");
        }
        inst.logInfoFile(sb.toString());
        e.printStackTrace();
    }
    public static void logInfo(String info){
        try{
            StringBuilder sb = new StringBuilder();
            Date date = new Date(System.currentTimeMillis());
            String time = mFormater.format(date);
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String traceInfo = "["+Thread.currentThread().getId()+" " + stackTrace[3].getClassName() + "." + stackTrace[3].getMethodName() + "] ";
            sb.append(time).append(traceInfo).append(info).append("\r\n");
            inst.logInfoFile(sb.toString());
            synchronized (mFormater){
                System.out.print(traceInfo);
                System.out.print(info);
                System.out.println();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    OSNListener mListener = new OSNListener() {
        @Override
        public void logInfo(String text){
            logInfoFile(text);
        }
        @Override
        public String getConfig(String key) {
            try {
                return mSp.getString(key, null);
            } catch (Exception e) {
            }
            return null;
        }

        @Override
        public void setConfig(String key, String value) {
            try {
                if (value == null){
                    mSp.edit().remove(key);
                }
                else{
                    mSp.edit().putString(key, value).commit();
                }

            }catch (Exception e){

            }

        }
        @Override
        public void onConnectSuccess(String state) {
            try {
                if (state.equalsIgnoreCase("logined"))
                    mUserId = osnsdk.getUserID();
                onConnectionStatusChanged(ConnectionStatusConnected);
            }catch (Exception e){

            }
        }

        @Override
        public void onConnectFailed(String error) {
            try {
                logined = false;
                if (error.contains("KickOff")){
                    //    onConnectionStatusChanged(ConnectionStatusKickOffline);
                    logout(ConnectionStatusKickOffline);
                }else {
                    onConnectionStatusChanged(ConnectionStatusUnconnected);
                }
            }catch (Exception e){

            }
        }

        @Override
        public void onSetMessage(OsnMessageInfo msgInfo) {
            try{
                JSONObject data = JSON.parseObject(msgInfo.content);
                String type = data.getString("type");
                String hash = data.getString("messageHash");
                logInfo("type: "+type+", hash: "+hash);
                if(type == null || hash == null){
                    return;
                }
                Message msg = SqliteUtils.queryMessage(hash);
                if(msg == null){
                    logInfo("no found message hash: "+hash);
                    return;
                }
                Message message = new Message();
                message.messageId = 0;
                message.direction = MessageDirection.Receive;
                message.status = MessageStatus.Readed;
                message.messageUid = 0;
                message.serverTime = System.currentTimeMillis();
                SqliteUtils.deleteMessage(msg.messageId);
                if(type.equalsIgnoreCase("delete")){
                    message.content = new DeleteMessageContent(msg.sender, msg.messageUid);
                    //SqliteUtils.insertMessage(message);
                    onDeleteMessage(msg.messageUid);
                } else {
                    message.content = new RecallMessageContent(msg.sender, msg.messageUid);
                    //SqliteUtils.insertMessage(message);
                    onRecallMessage(msg.messageUid);
                }
            } catch (Exception e) {
                logError(e);
            }
        }



        @Override
        public List<String> onRecvMessage(List<OsnMessageInfo> msgList) {

            //List<Message> messageList = recvMessage(msgList, false);
            RecvMessageResultInfo returnInfo = recvMessage(msgList, false);
            List<Message> messageList = returnInfo.messages;
            List<String> hashSet = returnInfo.completeList;
            if (messageList.isEmpty()) {
                if (hashSet.isEmpty()) {
                    return null;
                }
                return hashSet;
            }
            boolean silent = false;
            synchronized (mShareLock) {
                for (Message message : messageList) {

                    ConversationInfo conversationInfo = SqliteUtils.queryConversation(message.conversation.type.getValue(), message.conversation.target, message.conversation.line);
                    if (conversationInfo == null) {
                        SqliteUtils.insertConversation(message.conversation.type.getValue(), message.conversation.target, message.conversation.line);
                        conversationInfo = SqliteUtils.queryConversation(message.conversation.type.getValue(), message.conversation.target, message.conversation.line);



                        if (conversationInfo.conversation.type == Conversation.ConversationType.Group) {
                            addGroupDrop(conversationInfo.conversation.target);
                        }
                    }
                    //@消息
                    if(!message.contents.isEmpty()){
                        JSONObject jsonObject = JSONObject.parseObject(message.contents);
                        String type1 = jsonObject.getString("type");
                        if(type1.equals("redPacket")){

                        }else{
                            Gson gson = new Gson();
                            ContentsInfo contentsInfo = gson.fromJson(message.contents,ContentsInfo.class);
                            List<String> mentionedTargets = contentsInfo.getMentionedTargets();
                            String type = contentsInfo.getType();
                            if(type.equals("text")){
                                if(mentionedTargets != null){
                                    for(int i=0;i<mentionedTargets.size();i++){
                                        if(mentionedTargets.get(i).equals(mUserId)){
                                            conversationInfo.unreadCount.unreadMention += 1;
                                        }
                                    }
                                }
                            }
                        }
                    }


                    conversationInfo.timestamp = message.serverTime;
                    conversationInfo.lastMessage = message;
                    conversationInfo.unreadCount.unread += 1;
                    conversationInfo.unreadCount.unreadFriends = 10;
                    conversationInfo.unreadCount.unreadGroup= 11;
                    conversationInfo.unreadCount.unreadkefu = 12;
                    SqliteUtils.updateConversation(conversationInfo);

                    if (message.conversation.type == Conversation.ConversationType.Service) {
                        conversationInfo = SqliteUtils.queryConversation(Conversation.ConversationType.Notify.getValue(), "0", message.conversation.line);
                        if (conversationInfo == null) {
                            SqliteUtils.insertConversation(Conversation.ConversationType.Notify.getValue(), "0", message.conversation.line);
                            conversationInfo = SqliteUtils.queryConversation(Conversation.ConversationType.Notify.getValue(), "0", message.conversation.line);
                        }
                        conversationInfo.timestamp = message.serverTime;
                        conversationInfo.lastMessage = message;
                        conversationInfo.unreadCount.unread += 1;
                        conversationInfo.unreadCount.unreadFriends = 20;
                        conversationInfo.unreadCount.unreadGroup= 21;
                        conversationInfo.unreadCount.unreadkefu = 22;
                        SqliteUtils.updateConversation(conversationInfo);
                    }

                    silent = silent | conversationInfo.isSilent;
                }
            }
            onReceiveMessage(messageList, false);

            if (!silent){
                playMessageTipAudio();
            }

            //showNotification(messageList);
            return hashSet;
        }

        @Override
        public void onRecvRequest(OsnRequestInfo request) {
            try {
                if (request.isGroup) {
                    FriendRequest friendRequest = new FriendRequest();
                    friendRequest.type = request.isApply ? RequestType_ApplyMember : RequestType_InviteGroup;
                    friendRequest.readStatus = 0;
                    friendRequest.direction = FriendRequest.Direction_Recv;
                    friendRequest.reason = request.reason;
                    friendRequest.status = 0;
                    friendRequest.target = request.userID;
                    friendRequest.originalUser = request.originalUser == null ? "" : request.originalUser;
                    friendRequest.userID = request.targetUser;
                    friendRequest.timestamp = request.timeStamp;
                    friendRequest.invitation = request.data == null ? null : request.data.getString("invitation");
                    SqliteUtils.insertFriendRequest(friendRequest);
                    onFriendRequestUpdated(new String[]{request.reason});
                    //showNotification(friendRequest);
                } else {
                    if (mBinder.isMyFriend(request.userID))
                        osnsdk.acceptFriend(request.userID, null);
                    else {
                        FriendRequest friendRequest = new FriendRequest();
                        friendRequest.type = RequestType_Friend;
                        friendRequest.readStatus = 0;
                        friendRequest.direction = FriendRequest.Direction_Recv;
                        friendRequest.reason = request.reason;
                        friendRequest.status = 0;
                        friendRequest.target = request.userID;
                        friendRequest.originalUser = "";
                        friendRequest.userID = "";
                        friendRequest.timestamp = request.timeStamp;
                        friendRequest.invitation = request.data == null ? null : request.data.getString("invitation");
                        SqliteUtils.insertFriendRequest(friendRequest);
                        onFriendRequestUpdated(new String[]{request.reason});
                        //showNotification(friendRequest);
                    }
                }

                playMessageTipAudio();
            } catch (Exception e) {
                logError(e);
            }
        }

        @Override
        public void onFriendUpdate(List<OsnFriendInfo> friendList) {
            List<String> friends = new ArrayList<>();
            for (OsnFriendInfo f : friendList) {
                friends.add(f.friendID);
                if (SqliteUtils.queryFriend(f.friendID) != null) {
                    if (f.state == OsnFriendInfo.Deleted) {
                        logInfo("delete friend - userID:" + f.userID + ", friendID: " + f.friendID);
                        SqliteUtils.deleteFriend(f.friendID);
                    } else if (f.state == OsnFriendInfo.Blacked) {
                        logInfo("blacked friend - friendID: " + f.friendID);
                        SqliteUtils.updateFriend(f);
                    }
                    continue;
                }

                if (f.state == OsnFriendInfo.Normal) {
                    SqliteUtils.insertFriend(f);
                    addFriend(f);
                } else if (f.state == OsnFriendInfo.Syncst) {
                    OsnFriendInfo friendInfo = osnsdk.getFriendInfo(f.friendID, null);
                    if (friendInfo == null) {
                        logInfo("get friendInfo failed: " + f.friendID);
                        continue;
                    }
                    logInfo("syncst friend: " + friendInfo);
                    SqliteUtils.insertFriend(friendInfo);
                } else {
                    logInfo("friend state is wait: " + f.friendID);
                    SqliteUtils.insertFriend(f);
                }
            }
            onFriendListUpdated(friends.toArray(new String[1]));

            //更新好友列表

            /*List<String> list = SqliteUtils.listFriends();
            List<String> deleteList = new ArrayList<>();
            try {
                if(list != null){
                    if(list.size() > friends.size()){
                        deleteList =  SPUtils.getDiffrent(list,friends);
                        for(String s : deleteList){
                            SqliteUtils.deleteFriend(s);
                        }
                    }
                }
            }catch (Exception e){

            }*/

        }

        @Override
        public void onFriendsUpdate(List<OsnFriendInfo> friendList) {

            System.out.println("@@@ friend list : " + friendList.size());
            SqliteUtils.clearFriend();

            for (OsnFriendInfo f : friendList) {
                SqliteUtils.insertFriend(f);
            }
        }

        @Override
        public void onUserUpdate(OsnUserInfo osnUserInfo, List<String> keys) {
            UserInfo userInfo = SqliteUtils.queryUser(osnUserInfo.userID);
            if (userInfo == null)
                return;
            for (String k : keys) {
                if (k.equalsIgnoreCase("displayName"))
                    userInfo.displayName = osnUserInfo.displayName;
                else if (k.equalsIgnoreCase("portrait"))
                    userInfo.portrait = osnUserInfo.portrait;
                else if (k.equalsIgnoreCase("urlSpace"))
                    userInfo.urlSpace = osnUserInfo.urlSpace;
                else if(k.equalsIgnoreCase("describes")){
                    JSONObject json = JSON.parseObject(osnUserInfo.describes);
                    //userInfo.nft = json.getString("nft");
                }
            }
            SqliteUtils.updateUser(userInfo, keys);
            onUserInfoUpdated(Collections.singletonList(userInfo));
        }

        @Override
        public void cleanGroupFav(){
            SqliteUtils.clearGroupFav();
        }

        @Override
        public void onGroupUpdate(String state, OsnGroupInfo osnGroupInfo, List<String> keys) {
            try {
                System.out.println("@@@@ onGroupUpdate state : " + state);
                switch (state) {
                    case "NeedShare":
                        groupNeedShare(osnGroupInfo);
                        break;
                    case "AddAdmin":
                        groupAddAdmin(osnGroupInfo);
                        break;
                    case "DelAdmin":
                        groupDelAdmin(osnGroupInfo);
                        break;
                    case "NewlyGroup":
                        groupNewlyGroup(osnGroupInfo);
                        break;
                    case "SyncGroup":
                        groupSyncGroup(osnGroupInfo);
                        break;
                    case "UpdateGroup":
                        groupUpdateGroup(osnGroupInfo, keys);
                        break;
                    case "UpdateMember":
                        groupUpdateMember(osnGroupInfo, keys);
                        break;
                    case "DelMember":
                        groupDelMember(osnGroupInfo, state);
                        break;
                    case "AddMember":
                        groupAddMember(osnGroupInfo);
                        break;
                    case "QuitGroup":
                        groupQuitGroup(osnGroupInfo, state);
                        break;
                    case "DelGroup":
                        groupDelGroup(osnGroupInfo);
                        break;
                    case "UpgradeBomb":
                        groupUpgradeBomb(osnGroupInfo, state);
                        break;
                    case "win":
                        groupWin(osnGroupInfo, state);
                        break;
                    case "Mute":
                    case "Allow":
                        groupMute(osnGroupInfo);
                        break;
                    default:
                        logInfo("unknown GroupUpdate state: " + state);
                        break;
                }
            } catch (Exception e) {
                logError(e);
            }
        }

        @Override
        public void onServiceInfo(List<OsnServiceInfo> infos) {
            try {
                List<LitappInfo> infoLists = new ArrayList<>();
                for(OsnServiceInfo info : infos){
                    if (info instanceof OsnLitappInfo) {
                        LitappInfo litappInfo = toClientLitapp((OsnLitappInfo) info);
                        infoLists.add(litappInfo);
                    }
                }
                onLitappInfoUpdated(infoLists);
            }catch (Exception e){

            }
        }
        @Override
        public void onFindResult(List<OsnLitappInfo> infos){
            try {
                List<LitappInfo> infoLists = new ArrayList<>();
                for(OsnLitappInfo info : infos){
                    LitappInfo litappInfo = toClientLitapp(info);
                    SqliteUtils.insertLitapp(litappInfo);
                    infoLists.add(litappInfo);
                }
                onLitappInfoResult(infoLists);
            }catch (Exception e){

            }
        }

        @Override
        public void onReceiveRecall(JSONObject data){
            //System.out.println("@@@@ onReceiveRecall begin");
            String to = data.getString("to");
            String from = data.getString("from");
            String messageHash = data.getString("messageHash");
            System.out.println("@@@@ " + messageHash);

            if (to.startsWith("OSNG")){
                System.out.println("@@@@ group");
                // 判断消息是否是from发来的消息
                Message message = SqliteUtils.queryGroupMessageWithHash0(messageHash);
                if (message != null) {
                    if (message.sender.equalsIgnoreCase(from)){
                        message.content = new RecallMessageContent(message.conversation.target, 0);
                        SqliteUtils.recallMessage(message);
                    }
                } else {
                    System.out.println("@@@ message is null");
                }
                return;
            }
            //System.out.println("@@@ " + messageHash);
            //


            Message message = SqliteUtils.queryMessage(messageHash);
            if (message != null) {

                // 判断一下 from 是否相同
                if (message.sender.equalsIgnoreCase(from)) {
                    message.content = new RecallMessageContent(message.conversation.target, 0);
                    SqliteUtils.recallMessage(message);
                }
                onRecallMessage(message.messageUid);
            } else {
                System.out.println("@@@ message is null");
            }


        }




        @Override
        public void onGroupInfo(JSONObject data){

            try {
                //System.out.println("@@@@@ onGroupInfo begin.");
                OsnGroupInfo osnGroupInfo = OsnGroupInfo.toGroupInfo(data);
                if (osnGroupInfo == null) {
                    return;
                }
                GroupInfo groupInfo = toClientGroup(osnGroupInfo);
                UpdateGroupInfo(osnGroupInfo, mUserId);
                /*
                // query db
                GroupInfo groupInfoOld = SqliteUtils.queryGroup(osnGroupInfo.groupID);
                // update group
                if (groupInfoOld != null) {
                    GroupInfo groupInfo = toClientGroup(osnGroupInfo);
                    groupInfo.fav = groupInfoOld.fav;
                    groupInfo.showAlias = groupInfoOld.showAlias;
                    SqliteUtils.insertGroup(groupInfo);
                } else {
                    GroupInfo groupInfo = toClientGroup(osnGroupInfo);
                    groupInfo.fav = 0;
                    groupInfo.showAlias = 0;
                    SqliteUtils.insertGroup(groupInfo);
                }

                // 更新一下isMember
                ConversationInfo conv = SqliteUtils.queryConversation(1, osnGroupInfo.groupID, 0);
                if (conv != null) {
                    int isMember = osnGroupInfo.isMember();
                    if (conv.isMember != isMember) {
                        conv.isMember = isMember;
                        SqliteUtils.updateConversation(conv, Collections.singletonList("isMember"));
                    }
                }

                // 处理一下single mute
                GroupMember groupMember = SqliteUtils.queryMember(osnGroupInfo.groupID, mUserId);
                if (groupMember != null) {
                    if (osnGroupInfo.singleMute != groupMember.mute) {
                        groupMember.mute = osnGroupInfo.singleMute;;
                        SqliteUtils.updateMember(groupMember, Collections.singletonList("mute"));
                    }
                } else {
                    // 暂时还没有group member
                    if (osnGroupInfo.singleMute != 0) {
                        // 被禁言才管，没有被禁言管他个鸟呀？
                        groupMember = new GroupMember();
                        groupMember.groupId = osnGroupInfo.groupID;
                        groupMember.memberId = mUserId;
                        UserInfo userInfo = SqliteUtils.queryUser(mUserId);
                        groupMember.alias = userInfo.displayName;
                        groupMember.type = Normal;
                        groupMember.updateDt = System.currentTimeMillis();
                        groupMember.createDt = System.currentTimeMillis();
                        groupMember.mute = osnGroupInfo.singleMute;
                        groupMember.index = 999999;
                        //List<GroupMember> gmList = new ArrayList<>();
                        SqliteUtils.insertMembersNoIndex(Collections.singletonList(groupMember));
                    }
                }*/


            } catch (Exception e) {
                System.out.println("@@@@@ onGroupInfo Exception");
            }
            System.out.println("@@@@@ onGroupInfo end.");
        }


        @Override
        public void onUserInfo(JSONObject data){
            try {
                OsnUserInfo osnUserInfo = OsnUserInfo.toUserInfo(data);
                UserInfo userInfo = toClientUser(osnUserInfo);
                UserInfo ui = SqliteUtils.queryUser(userInfo.uid);
                if (ui == null) {
                    SqliteUtils.insertUser(userInfo);
                } else {
                    List<String> keys = new ArrayList<>();

                    if (ui.displayName.equalsIgnoreCase(userInfo.displayName)) {
                        keys.add("displayName");
                    }
                    if (ui.portrait.equalsIgnoreCase(userInfo.portrait)) {
                        keys.add("portrait");
                    }
                    if (ui.role.equalsIgnoreCase(userInfo.role)) {
                        keys.add("role");
                    }
                    if (ui.describes.equalsIgnoreCase(userInfo.describes)) {
                        keys.add("describes");
                    }
                    if (keys.size() > 0) {
                        SqliteUtils.updateUser(userInfo, keys);
                    }
                }

            } catch (Exception e) {
                System.out.println("@@@@@ onGroupInfo Exception");
            }
            System.out.println("@@@@@ onGroupInfo end.");
        }


        @Override
        public void onDeleteMessageTo(JSONObject data){


            String to = data.getString("to");
            String from = data.getString("from");
            String messageHash = data.getString("messageHash");
            System.out.println("@@@@ " + messageHash);

            if (to.startsWith("OSNG")){
                System.out.println("@@@@ group");
                // 判断消息是否是from发来的消息
                Message message = SqliteUtils.queryGroupMessageWithHash0(messageHash);
                if (message != null) {
                    SqliteUtils.deleteMessage(message.messageId);
                } else {
                    System.out.println("@@@ message is null");
                }
                return;
            }
            //System.out.println("@@@ " + messageHash);
            //



            System.out.println("@@@ " + messageHash);
            //
            Message message = SqliteUtils.queryMessage(messageHash);
            if (message != null) {

                if (message.sender.equalsIgnoreCase(from)) {
                    SqliteUtils.deleteMessage(message.messageId);
                }

            } else {
                System.out.println("@@@ message is null");
            }


        }

        @Override
        public void onAllowGroupAddFriend(JSONObject data) {
            System.out.println("@@@   data1="+data);
        }


    };

    private UserInfo toRemarkName(UserInfo userInfo){
        OsnFriendInfo u = SqliteUtils.queryFriend(userInfo.uid);
        if(u != null && u.remarks != null)
            userInfo.displayName = u.remarks;
        return userInfo;
    }
    private UserInfo toClientUser(OsnUserInfo userInfo) {
        UserInfo u = new UserInfo();
        u.uid = userInfo.userID;
        u.name = userInfo.name;
        u.displayName = userInfo.displayName;
        u.portrait = userInfo.portrait;
        u.urlSpace = userInfo.urlSpace;
        u.role = userInfo.role;
        u.describes = userInfo.describes;
        if(userInfo.describes != null && !userInfo.describes.isEmpty()){
            JSONObject json = JSON.parseObject(userInfo.describes);
            /*u.nft = json.getString("nft");
            if(u.nft == null)
                u.nft = "";*/
        }
        return u;
    }
    private GroupMember toClientMember(OsnMemberInfo memberInfo) {
        GroupMember m = new GroupMember();
        m.groupId = memberInfo.groupID;
        m.memberId = memberInfo.osnID;
        if (memberInfo.type == OsnMemberInfo.MemberType_Normal)
            m.type = Normal;
        else if (memberInfo.type == OsnMemberInfo.MemberType_Owner)
            m.type = GroupMember.GroupMemberType.Owner;
        else if (memberInfo.type == MemberType_Admin)
            m.type = Manager;
        else
            m.type = Normal;
        m.alias = memberInfo.nickName;
        m.mute = memberInfo.mute;
        return m;
    }

    private GroupInfo toClientGroup(OsnGroupInfo groupInfo) {
        GroupInfo g = new GroupInfo();
        g.target = groupInfo.groupID;
        g.name = groupInfo.name;
        g.portrait = groupInfo.portrait;
        g.owner = groupInfo.owner;
        g.type = GroupInfo.GroupType.type(groupInfo.type);
        g.joinType = groupInfo.joinType;
        g.passType = groupInfo.passType;
        g.mute = groupInfo.mute;
        g.memberCount = groupInfo.memberCount;
        g.notice = groupInfo.billboard == null ? "" : groupInfo.billboard;
        g.attribute = groupInfo.attribute;
        if(groupInfo.attribute != null){
            JSONObject json = JSON.parseObject(groupInfo.attribute);
            if(json != null){
                String type = json.getString("type");
                if (type != null) {
                    if (type.equalsIgnoreCase("bomb"))
                        g.redPacket = 1;
                }

            }
        }
        return g;
    }
    private LitappInfo toClientLitapp(OsnLitappInfo osnLitappInfo) {
        LitappInfo litappInfo = new LitappInfo();
        litappInfo.target = osnLitappInfo.target;
        litappInfo.name = osnLitappInfo.name;
        litappInfo.displayName = osnLitappInfo.displayName;
        litappInfo.portrait = osnLitappInfo.portrait;
        litappInfo.theme = osnLitappInfo.theme;
        litappInfo.url = osnLitappInfo.url;
        litappInfo.info = osnLitappInfo.info;
        return litappInfo;
    }
    private OsnLitappInfo toSdkLitapp(LitappInfo litappInfo) {
        OsnLitappInfo osnLitappInfo = new OsnLitappInfo();
        osnLitappInfo.target = litappInfo.target;
        osnLitappInfo.name = litappInfo.name;
        osnLitappInfo.displayName = litappInfo.displayName;
        osnLitappInfo.portrait = litappInfo.portrait;
        osnLitappInfo.theme = litappInfo.theme;
        osnLitappInfo.url = litappInfo.url;
        return osnLitappInfo;
    }
    private boolean filterMessage(Message msg) {
        boolean filted = false;
        if (keywords != null && msg.content instanceof TextMessageContent) {
            TextMessageContent textMessageContent = (TextMessageContent) msg.content;
            String text = textMessageContent.getContent();
            for (String k : keywords) {
                int index = text.indexOf(k);
                if (index != -1) {
                    text = text.replace(k, "***");
                    textMessageContent.setContent(text);
                    filted = true;
                }
            }
        }
        return filted;
    }
    class RecvMessageResultInfo {
        public List<Message> messages;
        public List<String> completeList;
    }
    private RecvMessageResultInfo recvMessage(List<OsnMessageInfo> msgList, boolean isHistory) {
        List<Message> messages = new ArrayList<>();
        List<String> completeList = new ArrayList<>();
        try {
            for (OsnMessageInfo messageInfo : msgList) {
                if (messageInfo.hash != null) {
                    if (messageInfo.hash.length() != 0) {
                        if (SqliteUtils.isMessageExist(messageInfo.hash)){
                            completeList.add(messageInfo.hash);
                            continue;
                        }
                    }
                }

                /*if (SqliteUtils.queryMessage(messageInfo.timeStamp, messageInfo.userID))
                    continue;*/
                Message message = new Message();
                message.messageId = 0;
                message.sender = messageInfo.userID;
                message.messageHash = messageInfo.hash == null ? "" : messageInfo.hash;
                message.messageHash0 = messageInfo.hash0;
                message.contents = messageInfo.content;

                //System.out.println("@@@@@ recv");
                //System.out.println("@@@@@ message:"+ messageInfo.content);
                //System.out.println("@@@@@ from:"+ messageInfo.userID);





                if (messageInfo.userID.equalsIgnoreCase(mUserId)) {
                    if (messageInfo.target.startsWith("OSNG"))
                        message.conversation = new Conversation(Conversation.ConversationType.Group, messageInfo.target, 0);
                    else if (messageInfo.target.startsWith("OSNU"))
                        message.conversation = new Conversation(Conversation.ConversationType.Single, messageInfo.target, 0);
                    else
                        message.conversation = new Conversation(Conversation.ConversationType.Service, messageInfo.target, 0);
                    message.direction = MessageDirection.Send;
                    message.status = MessageStatus.Sent;
                } else {
                    if (messageInfo.userID.startsWith("OSNG")) {
                        message.sender = messageInfo.originalUser;
                        message.conversation = new Conversation(Conversation.ConversationType.Group, messageInfo.userID, 0);
                    } else if (messageInfo.userID.startsWith("OSNU"))
                        message.conversation = new Conversation(Conversation.ConversationType.Single, messageInfo.userID, 0);
                    else
                        message.conversation = new Conversation(Conversation.ConversationType.Service, messageInfo.userID, 0);
                    message.direction = MessageDirection.Receive;
                    message.status = isHistory ? MessageStatus.Readed : MessageStatus.Unread;
                }
                message.messageUid = 0;
                message.serverTime = messageInfo.timeStamp;
                //logInfo("data: "+messageInfo.content);
                JSONObject json = JSON.parseObject(messageInfo.content);

                String password = null;
                if (json != null) {
                    password = json.getString("password");
                }

                String msgType = json.getString("type");


                if (msgType.equalsIgnoreCase("text")) {
                    TextMessageContent textMessageContent = new TextMessageContent();
                    textMessageContent.setContent2(messageInfo.content);


                    /*textMessageContent.setContent(json.getString("data"));
                    String contents = messageInfo.content;
                    if(contents != null){
                        Gson gson = new Gson();
                        ContentsQuoteInfo contentsQuoteInfo = gson.fromJson(message.contents,ContentsQuoteInfo.class);
                        if(contentsQuoteInfo.getQuoteInfo() != null){
                            QuoteInfo quoteInfo = new QuoteInfo();
                            quoteInfo.setMessageUid(contentsQuoteInfo.getQuoteInfo().getU());
                            quoteInfo.setUserId(contentsQuoteInfo.getQuoteInfo().getI());
                            quoteInfo.setMessageDigest(contentsQuoteInfo.getQuoteInfo().getD());
                            quoteInfo.setUserDisplayName(contentsQuoteInfo.getQuoteInfo().getN());
                            textMessageContent.setQuoteInfo(quoteInfo);
                        }
                    }
                    */
                    message.content = textMessageContent;
                    filterMessage(message);
                } else if (msgType.equalsIgnoreCase("card")) {
                    CardMessageContent cardMessageContent = new CardMessageContent();
                    switch (json.getString("cardType")) {
                        case "user":
                            cardMessageContent.setType(CardType_User);
                            break;
                        case "group":
                            cardMessageContent.setType(CardType_Group);
                            break;
                        case "chatroom":
                            cardMessageContent.setType(CardType_ChatRoom);
                            break;
                        case "channel":
                            cardMessageContent.setType(CardType_Channel);
                            break;
                        case "litapp":
                            cardMessageContent.setType(CardType_Litapp);
                            break;
                        case "share":
                            cardMessageContent.setType(CardType_Share);
                            break;
                    }
                    cardMessageContent.setTarget(json.getString("target"));
                    cardMessageContent.setName(json.getString("name"));
                    cardMessageContent.setDisplayName(json.getString("displayName"));
                    cardMessageContent.setPortrait(json.getString("portrait"));
                    cardMessageContent.setTheme(json.getString("theme"));
                    cardMessageContent.setUrl(json.getString("url"));
                    cardMessageContent.setInfo(json.getString("info"));
                    message.content = cardMessageContent;
                } else if(msgType.equalsIgnoreCase("redPacket")){
                    JSONObject data = JSON.parseObject(json.getString("data"));
                    RedPacketInfo redPacketInfo = new RedPacketInfo(data);
                    SqliteUtils.insertRedPacket(redPacketInfo);
                    logInfo("insert redPacket: "+data.toString());
                    message.content = new RedPacketMessageContent(data);
                }
                /*else if(msgType.equalsIgnoreCase("call")) {
                    completeList.add(messageInfo.hash);
                    //messages.add(message);
                    continue;
                } */
                else if(msgType.equalsIgnoreCase("call")){
                    CallMessageContent callMessageContent = new CallMessageContent();
                    callMessageContent.id = json.getIntValue("id");
                    callMessageContent.type = json.getIntValue("callType");
                    callMessageContent.mode = json.getIntValue("callMode");
                    callMessageContent.action = json.getIntValue("callAction");
                    callMessageContent.url = json.getString("url");
                    callMessageContent.user = json.getString("user");
                    callMessageContent.voiceBaseUrl = json.getString("voiceBaseUrl");
                    callMessageContent.voiceHostUrl = json.getString("voiceHostUrl");
                    if(json.containsKey("urls"))
                        callMessageContent.urls = json.getJSONArray("urls").toJavaList(String.class);
                    if(json.containsKey("users"))
                        callMessageContent.users = json.getJSONArray("users").toJavaList(String.class);
                    message.content = callMessageContent;
                } else {
                    //
                    String url = json.getString("url");
                    String name = json.getString("name");
                    //String path = getExternalCacheDir().getAbsolutePath() + "/" +System.currentTimeMillis() +  name;
                    // 文件的保存路径要变
                    String path = getFilesDir().getAbsolutePath() + "/" +System.currentTimeMillis() +  name;
                    System.out.println("[ClientService] file path:" + path);

                    String decKey = json.getString("decKey");
                    if (decKey == null) {
                        if (msgType.equalsIgnoreCase("sticker") ||
                                msgType.equalsIgnoreCase("video")) {
                            String tmpPath = path + ".tmp";
                            //osnsdk.downloadData(url, tmpPath, null);
                            //FileUtil.renameFile(tmpPath, path);
                        }
                    }else{

                        //String unzipPath = null;

                        if (msgType.equalsIgnoreCase("sticker") ||
                                msgType.equalsIgnoreCase("video")) {

                            // 这里解压缩
                            /*String tmpPath = path + ".zip";
                            osnsdk.downloadData(url, tmpPath, null);

                            File[] unzipFiles = new File[0];
                            try {
                                unzipFiles = CompressUtil.unzip(tmpPath, decKey);
                            } catch (Exception e) {

                            }

                            if (unzipFiles != null) {
                                if (unzipFiles.length > 0) {
                                    FileUtil.delAllFile(tmpPath);
                                    FileUtil.renameFile(unzipFiles[0].toString(), path);
                                }
                            }*/

                        }
                    }

                    switch (msgType) {
                        case "file":
                            FileMessageContent fileMessageContent = new FileMessageContent();
                            fileMessageContent.remoteUrl = url;

                            String pathFile = getExternalCacheDir().getAbsolutePath() + "/" +System.currentTimeMillis() +  name;

                            fileMessageContent.localPath = pathFile;
                            fileMessageContent.decKey = decKey;
                            fileMessageContent.setName(json.getString("name"));
                            fileMessageContent.setSize(json.getInteger("size"));
                            message.content = fileMessageContent;
                            break;
                        case "image":

                            ImageMessageContent imageMessageContent = new ImageMessageContent();
                            imageMessageContent.remoteUrl = url;
                            imageMessageContent.localPath = path;
                            imageMessageContent.decKey = decKey;
                            imageMessageContent.imageHeight = json.getDouble("width");
                            imageMessageContent.imageHeight = json.getDouble("height");
                            message.content = imageMessageContent;
                            break;
                        case "voice":
                            SoundMessageContent soundMessageContent = new SoundMessageContent();
                            soundMessageContent.remoteUrl = url;
                            soundMessageContent.localPath = path;
                            soundMessageContent.setDuration(json.getIntValue("duration"));
                            message.content = soundMessageContent;
                            break;
                        case "video":
                            VideoMessageContent videoMessageContent = new VideoMessageContent();
                            videoMessageContent.remoteUrl = url;
                            videoMessageContent.localPath = path;
                            if (json.containsKey("thumbnail"))
                                videoMessageContent.setThumbnailBytes(Base64.decode(json.getString("thumbnail"), 0));
                            message.content = videoMessageContent;
                            break;
                        case "sticker":
                            StickerMessageContent stickerMessageContent = new StickerMessageContent();
                            stickerMessageContent.remoteUrl = url;
                            stickerMessageContent.localPath = path;
                            stickerMessageContent.width = json.getIntValue("width");
                            stickerMessageContent.height = json.getIntValue("height");
                            message.content = stickerMessageContent;
                            break;
                        default:
                            logInfo("unknown msgType:" + json.getString("type"));
                            break;
                    }

                    message.decKey = decKey;
                    message.content.decKey = decKey;
                }
                if (message.content != null) {
                    message.content.password = password;
                    if(message.content instanceof CallMessageContent) {
                        messages.add(message);
                        completeList.add(messageInfo.hash);
                    } else {
                        message.messageId = message.messageUid = SqliteUtils.insertMessage(message);
                    }
                    if (message.messageId != 0) {
                        completeList.add(messageInfo.hash);
                        messages.add(message);
                    }
                    /*message.messageId = message.messageUid = SqliteUtils.insertMessage(message);
                    if (message.messageId != 0) {
                        completeList.add(messageInfo.hash);
                        messages.add(message);
                    }*/
                }
            }
        } catch (Exception e) {
            System.out.println("@@@@@ recvMessage Exception");
        }
        RecvMessageResultInfo returnInfo = new RecvMessageResultInfo();
        returnInfo.messages = messages;
        returnInfo.completeList = completeList;
        return returnInfo;
    }

    static String MediaDir = "/media";
    public static String getMediaBaseDir(Context context) {
        File baseDir = context.getFilesDir();
        String basePath = baseDir.getPath() + MediaDir;
        File dir = new File(basePath);
        if(!dir.exists()){
            dir.mkdirs();
        }
        return basePath;
    }

    private List<GroupMember> updateMemberZone(List<OsnMemberInfo> memberList) {
        List<GroupMember> members = new ArrayList<>();
        try {
            if (memberList == null || memberList.isEmpty())
                return members;
            for (OsnMemberInfo m : memberList) {
                if (m.osnID != null) {
                    if (m.nickName == null) {
                        UserInfo userInfo = SqliteUtils.queryUser(m.osnID);
                        if (userInfo == null) {
                            mBinder.getUserInfo(m.osnID, null, true);
                        }
                        if (userInfo != null)
                            m.nickName = userInfo.name;
                    }
                    GroupMember groupMember = toClientMember(m);
                    members.add(groupMember);
                    SqliteUtils.updateMember(groupMember);
                }
            }
        } catch (Exception e) {
            logError(e);
        }
        return members;
    }
    private List<GroupMember> insertMembers(List<OsnMemberInfo> memberList, int begin) {
        List<GroupMember> members = new ArrayList<>();



        try {
            if (memberList == null || memberList.isEmpty())
                return members;
            System.out.println("@@@@@ insertMembers begin. size : " +memberList.size());
            int index = begin;
            for (OsnMemberInfo m : memberList) {
                // System.out.println("@@@@@ member nick name:" + m.nickName);
                if (m.osnID != null) {

                    if (m.nickName == null) {

                        UserInfo userInfo = SqliteUtils.queryUser(m.osnID);
                        if (userInfo == null) {
                            mBinder.getUserInfo(m.osnID, null, true);
                        }


                        if (userInfo != null) {
                            if (m.osnID.equalsIgnoreCase(mUserId)) {
                                //System.out.println("@@@@@ myself name:" + userInfo.name);
                                System.out.println("@@@@@ myself displayName:" + userInfo.displayName);
                                //System.out.println("@@@@@ myself groupAlias:" + userInfo.groupAlias);
                                //System.out.println("@@@@@ myself friendAlias:" + userInfo.friendAlias);
                                m.nickName = userInfo.displayName;
                            } else {
                                m.nickName = userInfo.name;
                            }
                        } else {
                            m.nickName = "Dao user";
                        }


                    }
                    GroupMember groupMember = toClientMember(m);
                    groupMember.index = index;
                    index ++;
                    members.add(groupMember);

                }
            }
            SqliteUtils.insertMembers(members);
        } catch (Exception e) {
            logError(e);
        }
        return members;
    }

    private List<GroupMember> updateMember(String groupID, List<OsnMemberInfo> memberList) {
        List<GroupMember> members = new ArrayList<>();
        try {
            if (memberList == null || memberList.isEmpty())
                memberList = osnsdk.getMemberInfo(groupID, null);
            if (memberList == null || memberList.isEmpty())
                return members;

            System.out.println("@@@ updateMember member count : " + memberList.size());


            for (OsnMemberInfo m : memberList) {
                members.add(toClientMember(m));
            }

            //SqliteUtils.clearMembers(groupID);
            if (members.size() != 0)
                SqliteUtils.insertMembers(members);
            System.out.println("@@@ updateMember insert count : " + members.size());


            for (OsnMemberInfo m : memberList) {
                if (m.osnID != null) {
                    if (m.nickName == null) {
                        UserInfo userInfo = SqliteUtils.queryUser(m.osnID);
//                        if (userInfo == null) {
//                            OsnUserInfo u = osnsdk.getUserInfo(m.osnID, null);
//                            if (u != null) {
//                                userInfo = toClientUser(u);
//                                SqliteUtils.insertUser(userInfo);
//                            }
//                        }
                        if (userInfo == null) {
                            osnsdk.getUserInfo(m.osnID, new OSNGeneralCallbackT<OsnUserInfo>() {
                                @Override
                                public void onFailure(String error) {
                                    logInfo("error: "+error);
                                }

                                @Override
                                public void onSuccess(OsnUserInfo osnUserInfo) {
                                    UserInfo userInfo = toClientUser(osnUserInfo);
                                    SqliteUtils.insertUser(userInfo);
                                }
                            });
                            userInfo = new NullUserInfo(m.osnID);
                        }
                        if (userInfo != null)
                            m.nickName = userInfo.name;
                    }
                    members.add(toClientMember(m));
                }
                //logInfo("@@@ memberID: " + m.osnID + ", nickName: " + m.nickName + ", type: " + m.type);
            }
            /*if (members.size() != 0)
                SqliteUtils.insertMembers(members);*/
        } catch (Exception e) {
            logError(e);
        }
        return members;
    }
    private void updateGroup(OsnGroupInfo osnGroupInfo, boolean isUpdateMember, int isFav, int showAlias) {
        try {
            logInfo("groupData: "+osnGroupInfo.data.toString());
            GroupInfo groupInfo = toClientGroup(osnGroupInfo);
            groupInfo.fav = isFav;
            groupInfo.showAlias = showAlias;
            SqliteUtils.insertGroup(groupInfo);
            //if (isUpdateMember)
            //    updateMember(osnGroupInfo.groupID, osnGroupInfo.userList);
        } catch (Exception e) {
            logError(e);
        }
    }
    private void addGroup(OsnGroupInfo groupInfo, int isFav) {
        try {
            logInfo("groupID: " + groupInfo.groupID + ", name: " + groupInfo.name);
            if(SqliteUtils.queryGroup(groupInfo.groupID) != null){
                logInfo("group already exist: "+groupInfo.groupID);
                //return;
            } else {
                updateGroup(groupInfo, true, isFav, 0);
            }

            if(isFav == 0) {
                ConversationInfo conversationInfo = SqliteUtils.queryConversation(Conversation.ConversationType.Group.getValue(), groupInfo.groupID, 0);
                if (conversationInfo == null)
                    SqliteUtils.insertConversation(Conversation.ConversationType.Group.getValue(), groupInfo.groupID, 0);
            }

            Message message = new Message();
            message.sender = groupInfo.owner;
            message.direction = MessageDirection.Receive;
            message.status = MessageStatus.Readed;
            message.conversation = new Conversation(Conversation.ConversationType.Group, groupInfo.groupID, 0);
            message.serverTime = groupInfo.genGroupNoticeTime();//System.currentTimeMillis();
            CreateGroupNotificationContent createGroupNotificationContent = new CreateGroupNotificationContent();
            createGroupNotificationContent.groupId = groupInfo.groupID;
            createGroupNotificationContent.groupName = groupInfo.name;
            createGroupNotificationContent.creator = groupInfo.owner;
            message.content = createGroupNotificationContent;

            if (groupInfo.notice != null) {
                message.messageHash = groupInfo.notice.getString("hash");
                message.messageHash0 = groupInfo.notice.getString("hash0");
                System.out.println("@@@@@ notice hash : "+message.messageHash);
            }

            long mid = SqliteUtils.insertMessage(message);
            if (mid != 0) {
                onReceiveMessage(Collections.singletonList(message), false);
            } else {
                System.out.println("@@@@@ notice insert failed.");
            }

            onGroupInfoUpdated(Collections.singletonList(toClientGroup(groupInfo)));
        } catch (Exception e) {
            logError(e);
        }
    }

    private void addGroup2(OsnGroupInfo groupInfo, int isFav) {
        try {
            logInfo("groupID: " + groupInfo.groupID + ", name: " + groupInfo.name);
            if(SqliteUtils.queryGroup(groupInfo.groupID) != null){
                logInfo("group already exist: "+groupInfo.groupID);
                //return;
            } else {
                updateGroup(groupInfo, true, isFav, 0);
            }

            if(isFav == 0) {
                ConversationInfo conversationInfo = SqliteUtils.queryConversation(Conversation.ConversationType.Group.getValue(), groupInfo.groupID, 0);
                if (conversationInfo == null)
                    SqliteUtils.insertConversation(Conversation.ConversationType.Group.getValue(), groupInfo.groupID, 0);
            }

            /*Message message = new Message();
            message.sender = groupInfo.owner;
            message.direction = MessageDirection.Receive;
            message.status = MessageStatus.Readed;
            message.conversation = new Conversation(Conversation.ConversationType.Group, groupInfo.groupID, 0);
            message.serverTime = System.currentTimeMillis();
            CreateGroupNotificationContent createGroupNotificationContent = new CreateGroupNotificationContent();
            createGroupNotificationContent.groupId = groupInfo.groupID;
            createGroupNotificationContent.groupName = groupInfo.name;
            createGroupNotificationContent.creator = groupInfo.owner;
            message.content = createGroupNotificationContent;
            //SqliteUtils.insertMessage(message);
            onReceiveMessage(Collections.singletonList(message), false);*/

            onGroupInfoUpdated(Collections.singletonList(toClientGroup(groupInfo)));
        } catch (Exception e) {
            logError(e);
        }
    }

    private void addGroupNull(String groupID, int isFav) {
        try {
            OsnGroupInfo groupInfo = new OsnGroupInfo();
            groupInfo.groupID = groupID;
            groupInfo.owner = mUserId;
            groupInfo.name = groupID;
            OsnMemberInfo memberInfo = new OsnMemberInfo();
            memberInfo.osnID = mUserId;
            memberInfo.groupID = groupID;
            memberInfo.type = OsnMemberInfo.MemberType_Normal;
            groupInfo.userList.add(memberInfo);
            addGroup2(groupInfo, isFav);
        } catch (Exception e) {
            logError(e);
        }
    }
    private void addGroupNull2(String groupID, int isFav) {
        try {
            GroupInfo groupInfo = SqliteUtils.queryGroup(groupID);
            if (groupInfo == null) {
                groupInfo = new GroupInfo();
                groupInfo.target = groupID;
                groupInfo.name = groupID;
            }
            groupInfo.fav = isFav;
            SqliteUtils.insertGroup(groupInfo);

            GroupMember groupMember = SqliteUtils.queryMember(groupID, mUserId);
            if (groupMember == null) {
                groupMember = new GroupMember();
                groupMember.groupId = groupID;
                groupMember.memberId = mUserId;
                UserInfo userInfo = SqliteUtils.queryUser(mUserId);
                groupMember.alias = userInfo.displayName;
                groupMember.type = Normal;
                groupMember.updateDt = System.currentTimeMillis();
                groupMember.createDt = System.currentTimeMillis();
                groupMember.mute = 0;
                groupMember.index = 999999;
                SqliteUtils.insertMembersNoIndex(Collections.singletonList(groupMember));
            }
        } catch (Exception e) {
            logError(e);
        }
    }
    private void addGroupDrop(String groupID){
        osnsdk.getGroupInfo(groupID, new OSNGeneralCallbackT<OsnGroupInfo>() {
            @Override
            public void onFailure(String error) {
                logInfo("SyncGroup null groupID: " + groupID);
                addGroupNull(groupID, 0);
            }

            @Override
            public void onSuccess(OsnGroupInfo osnGroupInfo) {
                UpdateGroupInfo(osnGroupInfo, mUserId);
                /*int isMember = osnGroupInfo.isMember();
                ConversationInfo conv = SqliteUtils.queryConversation(1, osnGroupInfo.groupID, 0);
                if (conv != null) {
                    if (conv.isMember != isMember) {
                        conv.isMember = isMember;
                        SqliteUtils.updateConversation(conv, Collections.singletonList("isMember"));
                    }
                }
                addGroup2(osnGroupInfo, 0);*/
            }
        });
    }
    private void addMemberNotify(OsnGroupInfo osnGroupInfo, List<OsnMemberInfo> members) {
        Message message = new Message();
        message.sender = osnGroupInfo.groupID;
        message.direction = MessageDirection.Receive;
        message.status = MessageStatus.Readed;
        message.conversation = new Conversation(Conversation.ConversationType.Group, osnGroupInfo.groupID, 0);
        message.serverTime = osnGroupInfo.genGroupNoticeTime();//System.currentTimeMillis();

        List<String> memberList = new ArrayList<>();
        for (OsnMemberInfo memberInfo : members)
            memberList.add(memberInfo.osnID);
        AddGroupMemberNotificationContent addGroupMemberNotificationContent = new AddGroupMemberNotificationContent();
        addGroupMemberNotificationContent.groupId = osnGroupInfo.groupID;
        addGroupMemberNotificationContent.invitees = memberList;
        addGroupMemberNotificationContent.invitor = osnGroupInfo.invitor;
        message.content = addGroupMemberNotificationContent;

        if (osnGroupInfo.notice != null) {
            message.messageHash = osnGroupInfo.notice.getString("hash");
            message.messageHash0 = osnGroupInfo.notice.getString("hash0");
            System.out.println("@@@@@ notice add member hash : "+message.messageHash);
        }

        long mid = SqliteUtils.insertMessage(message);
        if (mid != 0) {
            onReceiveMessage(Collections.singletonList(message), false);
        } else {
            System.out.println("@@@@@ notice add member insert failed.");
        }
        //SqliteUtils.insertMessage(message);
        //onReceiveMessage(Collections.singletonList(message), false);
    }
    private void delMemberNotify(OsnGroupInfo osnGroupInfo, List<OsnMemberInfo> members, String state) {
        Message message = new Message();
        message.sender = osnGroupInfo.groupID;
        message.direction = MessageDirection.Receive;
        message.status = MessageStatus.Readed;
        message.conversation = new Conversation(Conversation.ConversationType.Group, osnGroupInfo.groupID, 0);
        message.serverTime = osnGroupInfo.genGroupNoticeTime();//System.currentTimeMillis();

        for (OsnMemberInfo memberInfo : members) {
            if (state.equalsIgnoreCase("DelMember")) {
                KickoffGroupMemberNotificationContent kickoffGroupMemberNotificationContent = new KickoffGroupMemberNotificationContent();
                kickoffGroupMemberNotificationContent.groupId = osnGroupInfo.groupID;
                kickoffGroupMemberNotificationContent.operator = osnGroupInfo.approver;
                kickoffGroupMemberNotificationContent.kickedMembers = new ArrayList<>();
                kickoffGroupMemberNotificationContent.kickedMembers.add(memberInfo.osnID);
                message.content = kickoffGroupMemberNotificationContent;
            } else if (state.equalsIgnoreCase("QuitGroup")) {
                QuitGroupNotificationContent quitGroupNotificationContent = new QuitGroupNotificationContent();
                quitGroupNotificationContent.groupId = osnGroupInfo.groupID;
                quitGroupNotificationContent.operator = memberInfo.osnID;
                message.content = quitGroupNotificationContent;
            }

            //SqliteUtils.insertMessage(message);
        }

        if (osnGroupInfo.notice != null) {
            message.messageHash = osnGroupInfo.notice.getString("hash");
            message.messageHash0 = osnGroupInfo.notice.getString("hash0");
            System.out.println("@@@@@ notice del member hash : "+message.messageHash);
        }

        long mid = SqliteUtils.insertMessage(message);
        if (mid != 0) {
            onReceiveMessage(Collections.singletonList(message), false);
        } else {
            System.out.println("@@@@@ notice del member insert failed.");
        }

        //onReceiveMessage(Collections.singletonList(message), false);
    }
    private void delGroupNotify(String groupID, OsnGroupInfo osnGroupInfo) {
        Message message = new Message();
        message.sender = groupID;
        message.direction = MessageDirection.Receive;
        message.status = MessageStatus.Readed;
        message.conversation = new Conversation(Conversation.ConversationType.Group, groupID, 0);
        message.serverTime = osnGroupInfo.genGroupNoticeTime();//serverTime;//System.currentTimeMillis();
        DismissGroupNotificationContent dismissGroupNotificationContent = new DismissGroupNotificationContent();
        dismissGroupNotificationContent.groupId = groupID;
        dismissGroupNotificationContent.operator = groupID;
        message.content = dismissGroupNotificationContent;

        if (osnGroupInfo.notice != null) {
            message.messageHash = osnGroupInfo.notice.getString("hash");
            message.messageHash0 = osnGroupInfo.notice.getString("hash0");
            System.out.println("@@@@@ notice del group hash : "+message.messageHash);
        }

        long mid = SqliteUtils.insertMessage(message);
        if (mid != 0) {
            onReceiveMessage(Collections.singletonList(message), false);
        } else {
            System.out.println("@@@@@ notice del group insert failed.");
        }

        //SqliteUtils.insertMessage(message);
        //onReceiveMessage(Collections.singletonList(message), false);
    }
    private void delGroup(String groupID, String state) {
        SqliteUtils.deleteGroup(groupID);
        SqliteUtils.deleteConversation(Conversation.ConversationType.Group.getValue(), groupID, 0);
        SqliteUtils.clearMembers(groupID);
    }
    private void addFriend(OsnFriendInfo friendInfo) {
        Message message = new Message();
        message.sender = friendInfo.friendID;
        message.conversation = new Conversation(Conversation.ConversationType.Single, friendInfo.friendID, 0);
        message.direction = MessageDirection.Receive;
        message.status = MessageStatus.Readed;
        message.serverTime = System.currentTimeMillis();

        TextMessageContent textMessageContent = new TextMessageContent();
        FriendRequest request = SqliteUtils.queryFriendRequest(friendInfo.friendID);
        textMessageContent.setContent(request == null ? getString(R.string.hello) : request.reason);
        message.content = textMessageContent;
        SqliteUtils.insertMessage(message);

        message.content = new FriendGreetingMessageContent();
        message.serverTime = System.currentTimeMillis();
        SqliteUtils.insertMessage(message);

        message.content = new FriendAddedMessageContent();
        message.serverTime = System.currentTimeMillis();
        SqliteUtils.insertMessage(message);
        SqliteUtils.insertConversation(Conversation.ConversationType.Single.getValue(), friendInfo.friendID, 0);
    }
    private List<OsnMemberInfo> getMemberX(List<OsnMemberInfo> m0, List<GroupMember> m1, boolean exclude) {
        List<OsnMemberInfo> list = new ArrayList<>();
        for (OsnMemberInfo m : m0) {
            boolean finded = false;
            for (GroupMember o : m1) {
                if (m.osnID.equalsIgnoreCase(o.memberId)) {
                    finded = true;
                    break;
                }
            }
            if (finded) {
                if (!exclude)
                    list.add(m);
            } else {
                if (exclude)
                    list.add(m);
            }
        }
        return list;
    }
    private void createDynamic(String data, String json){
    }
    private GroupNotifyContent getGroupNotify(String state, OsnGroupInfo groupInfo){
        GroupNotifyContent groupNotifyContent = new GroupNotifyContent();
        try {
            if (state.equalsIgnoreCase("UpgradeBomb"))
                groupNotifyContent.info = getString(R.string.group_upgrade);
            else if (state.equalsIgnoreCase("win")) {
                UserInfo userInfo = mBinder.getUserInfo(groupInfo.data.getString("winner"), null, false);
                groupNotifyContent.info = String.valueOf(Character.toChars(0x1f389))
                        //+String.valueOf(Character.toChars(0x1f389))
                        //+String.valueOf(Character.toChars(0x1f389))
                        +getString(R.string.bless_win) + "[";
                if (userInfo == null)
                    groupNotifyContent.info += getString(R.string.you);
                else
                    groupNotifyContent.info += userInfo.displayName;
                double price = Double.parseDouble(groupInfo.data.getString("balance"))/100;
                groupNotifyContent.info += "]" + getString(R.string.group_who_win)
                        + ", " + getString(R.string.win_price) + price;
                //+String.valueOf(Character.toChars(0x1f389))
                //+String.valueOf(Character.toChars(0x1f389))
                //+String.valueOf(Character.toChars(0x1f389));
            }
        }catch (Exception e){
            logError(e);
        }
        return groupNotifyContent;
    }
    private void initDB(String dbPath) {
        SharedPreferences sp = mContext.getSharedPreferences("ospnConfig", Context.MODE_PRIVATE);
        if (dbPath == null) {
            dbPath = sp.getString("dbPath", null);
        } else {
            sp.edit().putString("dbPath", dbPath).apply();
        }

        System.out.println("db path : " + dbPath);
        if (dbPath == null) {
            Log.e("dbPath", dbPath);
            return;
        }
        SqliteUtils.initDB(dbPath);
    }
    private void initDB2() {
        mUserId = osnsdk.getUserID();
        System.out.println("onCreate db path mUserId: " + mUserId);
        if (mUserId != null) {
            String sdcard = getFilesDir().getAbsolutePath();
            String dbPath = sdcard + "/" + mUserId + ".db";
            System.out.println("onCreate db path : " + dbPath);
            initDB(dbPath);
        }
    }
    void groupNeedShare(OsnGroupInfo osnGroupInfo){
        GroupInfo groupInfo = SqliteUtils.queryGroup(osnGroupInfo.groupID);
        if(groupInfo == null){
            logInfo("group no found: "+osnGroupInfo.groupID);
            return;
        }
        JSONObject json = new JSONObject();
        json.put("type", "card");
        json.put("cardType", "group");
        json.put("target", groupInfo.target);
        json.put("name", groupInfo.name);
        json.put("displayName", groupInfo.name);
        json.put("portrait", groupInfo.portrait);
        json.put("theme", null);
        json.put("url", null);
        json.put("info", null);
        for(OsnMemberInfo memberInfo : osnGroupInfo.userList){
            osnsdk.sendMessage(json.toString(), memberInfo.osnID, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    System.out.println("@@@   1");
                    GroupInfo groupInfo = null;
                    try {
                        String time = String.valueOf(System.currentTimeMillis());
                        groupInfo.timeInterval = time;
                        List<GroupInfo> groupInfoList = new ArrayList<>();
                        groupInfoList.add(groupInfo);
                        SqliteUtils.updateGroup(groupInfo,Collections.singletonList("timeInterval"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(String error) {

                }
            });
        }
    }



    void recall(String to, String msgHash) {
        osnsdk.recall(to, msgHash, new OSNGeneralCallback() {
            @Override
            public void onSuccess(JSONObject data) {

            }

            @Override
            public void onFailure(String error) {

            }
        });
    }

//    void setRole(String key,String value){
//        osnsdk.setRole(key, value, new OSNGeneralCallback() {
//            @Override
//            public void onSuccess(JSONObject data) {
//
//            }
//
//            @Override
//            public void onFailure(String error) {
//
//            }
//        });
//    }

    void groupAddAdmin(OsnGroupInfo osnGroupInfo){
        try {
            List<GroupMember> memberList = new ArrayList<>();
            for(OsnMemberInfo memberInfo : osnGroupInfo.userList){
                GroupMember member = SqliteUtils.queryMember(osnGroupInfo.groupID, memberInfo.osnID);
                if(member != null){
                    member.type = Manager;
                    memberList.add(member);
                    SqliteUtils.updateMember(member, Collections.singletonList("type"));
                }
            }
            onGroupMembersUpdated(osnGroupInfo.groupID, memberList);
        }catch (Exception e){

        }
    }
    void groupDelAdmin(OsnGroupInfo osnGroupInfo){
        try {
            List<GroupMember> memberList = new ArrayList<>();
            for(OsnMemberInfo memberInfo : osnGroupInfo.userList){
                GroupMember member = SqliteUtils.queryMember(osnGroupInfo.groupID, memberInfo.osnID);
                if(member != null){
                    member.type = Normal;
                    memberList.add(member);
                    SqliteUtils.updateMember(member, Collections.singletonList("type"));
                }
            }
            onGroupMembersUpdated(osnGroupInfo.groupID, memberList);
        }catch (Exception e){

        }
    }
    void groupNewlyGroup(OsnGroupInfo osnGroupInfo){
        ConversationInfo conv = SqliteUtils.queryConversation(Conversation.ConversationType.Group.getValue(), osnGroupInfo.groupID, 0);
        if (conv != null) {
            if (conv.isMember == 0) {
                conv.isMember = 1;
                SqliteUtils.updateConversation(conv, Collections.singletonList("isMember"));
            }
        }
        addGroup(osnGroupInfo, 0);
    }
    void groupSyncGroup(OsnGroupInfo osnGroupInfo){

        GroupInfo groupInfo = SqliteUtils.queryGroup(osnGroupInfo.groupID);
        if (groupInfo == null) {
            System.out.println("@@@@@ groupSyncGroup addGroupNull: " + osnGroupInfo.groupID);
            addGroupNull2(osnGroupInfo.groupID, 1);
            osnsdk.getGroupInfo(osnGroupInfo.groupID, new OSNGeneralCallbackT<OsnGroupInfo>() {
                @Override
                public void onFailure(String error) {
                    System.out.println("@@@@@ SyncGroup null groupID: " + osnGroupInfo.groupID);
                    //addGroupNull(osnGroupInfo.groupID, 1);
                }

                @Override
                public void onSuccess(OsnGroupInfo osnGroupInfo) {
                    UpdateGroupInfo(osnGroupInfo, mUserId, 1);
                    /*int isMember = osnGroupInfo.isMember();
                    ConversationInfo conv = SqliteUtils.queryConversation(1, osnGroupInfo.groupID, 0);
                    if (conv != null) {
                        if (conv.isMember != isMember) {
                            conv.isMember = isMember;
                            SqliteUtils.updateConversation(conv, Collections.singletonList("isMember"));
                        }
                    }
                    addGroup2(osnGroupInfo, 1);*/
                }
            });
        } else {
            System.out.println("sync group set fav : " + groupInfo.target);
            groupInfo.fav = 1;
            SqliteUtils.updateGroup(groupInfo, Collections.singletonList("fav"));
        }
    }
    void groupUpdateGroup(OsnGroupInfo osnGroupInfo, List<String> keys){
        GroupInfo groupInfo = SqliteUtils.queryGroup(osnGroupInfo.groupID);
        if (groupInfo != null) {
            for (String k : keys) {
                switch (k) {
                    case "name":
                        groupInfo.name = osnGroupInfo.name;
                        logInfo("new group name: " + groupInfo.name);
                        break;
                    case "portrait":
                        groupInfo.portrait = osnGroupInfo.portrait;
                        logInfo("new group portrait: " + groupInfo.portrait);
                        break;
                    case "type":
                        groupInfo.type = GroupInfo.GroupType.type(osnGroupInfo.type);
                        logInfo("new group type: " + groupInfo.type.value());
                        break;
                    case "joinType":
                        groupInfo.joinType = osnGroupInfo.joinType;
                        logInfo("new group joinType: " + groupInfo.joinType);
                        break;
                    case "passType":
                        groupInfo.passType = osnGroupInfo.passType;
                        logInfo("new group passType: " + groupInfo.passType);
                        break;
                    case "mute":
                        groupInfo.mute = osnGroupInfo.mute;
                        logInfo("new group mute: " + groupInfo.mute);
                        break;
                    case "notice":
                    case "billboard":
                        groupInfo.notice = osnGroupInfo.billboard;
                        logInfo("new group notice: " + groupInfo.notice);
                        break;
                    case "attribute":
                        JSONObject json = JSON.parseObject(osnGroupInfo.attribute);
                        groupInfo.attribute = osnGroupInfo.attribute;
                        String type = json.getString("type");
                        if (type != null) {
                            if (type.equalsIgnoreCase("bomb")) {
                                groupInfo.redPacket = 1;
                            } else {
                                groupInfo.redPacket = 0;
                            }
                        }

                        //groupInfo.redPacket = json.getIntValue("redPacket");
                        break;
                }
            }
            SqliteUtils.updateGroup(groupInfo, keys);
            onGroupInfoUpdated(Collections.singletonList(groupInfo));
        }
    }
    void groupUpdateMember(OsnGroupInfo osnGroupInfo, List<String> keys){
        GroupInfo groupInfo = SqliteUtils.queryGroup(osnGroupInfo.groupID);
        if (groupInfo == null) {
            logInfo("no my groupID: " + osnGroupInfo.groupID);
            return;
        }
        List<GroupMember> memberList = new ArrayList<>();
        for (OsnMemberInfo m : osnGroupInfo.userList) {
            GroupMember groupMember = SqliteUtils.queryMember(m.groupID, m.osnID);
            if (groupMember == null) {
                logInfo("no my memberID: " + m.osnID);
                continue;
            }
            memberList.add(groupMember);
            List<String> keyx = new ArrayList<>();
            for (String k : keys) {
                if (k.equalsIgnoreCase("nickName")) {
                    groupMember.alias = m.nickName;
                    logInfo("new member alias: " + m.nickName + ", osnID: " + m.osnID);
                    keyx.add("alias");
                } else if (k.equalsIgnoreCase("type")) {
                    if (m.type == OsnMemberInfo.MemberType_Normal)
                        groupMember.type = Normal;
                    else if (m.type == OsnMemberInfo.MemberType_Owner)
                        groupMember.type = GroupMember.GroupMemberType.Owner;
                    else if (m.type == MemberType_Admin)
                        groupMember.type = Manager;
                    keyx.add("type");
                    logInfo("new member type: " + m.type + ", osnID: " + m.osnID);
                }
            }
            SqliteUtils.updateMember(groupMember, keyx);
        }
        onGroupMembersUpdated(osnGroupInfo.groupID, memberList);
    }
    void groupDelMember(OsnGroupInfo osnGroupInfo, String state){
        GroupInfo groupInfo = SqliteUtils.queryGroup(osnGroupInfo.groupID);
        if (groupInfo != null) {
            List<GroupMember> memberList = SqliteUtils.queryMembers(osnGroupInfo.groupID);
            List<OsnMemberInfo> delList = getMemberX(osnGroupInfo.userList, memberList, false);
            if (!delList.isEmpty()) {
                for (OsnMemberInfo memberInfo : delList) {


                    logInfo("@@@@ delete members: " + memberInfo.osnID);
                    if (memberInfo.osnID != null) {
                        if (mUserId != null) {
                            if (memberInfo.osnID.equalsIgnoreCase(mUserId)) {
                                ConversationInfo conv = SqliteUtils.queryConversation(1, osnGroupInfo.groupID, 0);
                                if (conv != null) {
                                    if (conv.isMember != 0) {
                                        conv.isMember = 0;
                                        SqliteUtils.updateConversation(conv, Collections.singletonList("isMember"));
                                    }
                                }
                            }
                        }
                    }





                }
                SqliteUtils.deleteMembers(delList);
                groupInfo.memberCount -= delList.size();
                SqliteUtils.updateGroup(groupInfo, Collections.singletonList("memberCount"));
                onGroupInfoUpdated(Collections.singletonList(groupInfo));
                onGroupMembersUpdated(osnGroupInfo.groupID, memberList);
                delMemberNotify(osnGroupInfo, delList, state);
            } else
                logInfo("@@@@ del is empty");
        }
    }
    void groupAddMember(OsnGroupInfo osnGroupInfo){
        GroupInfo groupInfo = SqliteUtils.queryGroup(osnGroupInfo.groupID);
        if (groupInfo != null) {
            List<GroupMember> memberList = SqliteUtils.queryMembers(osnGroupInfo.groupID);
            List<OsnMemberInfo> addList = getMemberX(osnGroupInfo.userList, memberList, true);
            if (!addList.isEmpty()) {
                memberList.clear();
                for (OsnMemberInfo m : addList) {
                    if (m.nickName == null) {
                        UserInfo userInfo = SqliteUtils.queryUser(m.osnID);
                        if (userInfo == null) {
                            OsnUserInfo u = osnsdk.getUserInfo(m.osnID, null);
                            if (u != null) {
                                userInfo = toClientUser(u);
                                SqliteUtils.insertUser(userInfo);
                            }
                        }
                        if (userInfo != null)
                            m.nickName = userInfo.name;
                    }
                    memberList.add(toClientMember(m));
                    logInfo("add members: " + m.osnID);
                }
                groupInfo.memberCount += memberList.size();
                SqliteUtils.updateGroup(groupInfo, Collections.singletonList("memberCount"));
                SqliteUtils.insertMembersNoIndex(memberList);
                onGroupInfoUpdated(Collections.singletonList(groupInfo));
                onGroupMembersUpdated(osnGroupInfo.groupID, memberList);
                addMemberNotify(osnGroupInfo, addList);
            } else
                logInfo("add is empty");
        }
    }
    void groupQuitGroup(OsnGroupInfo osnGroupInfo, String state){
        GroupInfo groupInfo = SqliteUtils.queryGroup(osnGroupInfo.groupID);
        if (groupInfo != null) {
            OsnMemberInfo memberInfo = osnGroupInfo.userList.get(0);
            if (memberInfo.osnID.equalsIgnoreCase(mUserId))
                delGroup(osnGroupInfo.groupID, "quit");
            else {
                SqliteUtils.deleteMembers(osnGroupInfo.userList);
                List<GroupMember> memberList = SqliteUtils.queryMembers(osnGroupInfo.groupID);
                onGroupMembersUpdated(osnGroupInfo.groupID, memberList);
            }
            logInfo("quitGroup: " + memberInfo.osnID);
            delMemberNotify(osnGroupInfo, Collections.singletonList(memberInfo), state);
        }
    }
    void groupDelGroup(OsnGroupInfo osnGroupInfo){
        GroupInfo groupInfo = SqliteUtils.queryGroup(osnGroupInfo.groupID);
        if (groupInfo != null) {
            delGroup(osnGroupInfo.groupID, "dismiss");
            delGroupNotify(osnGroupInfo.groupID, osnGroupInfo);
            logInfo("delGroup: " + osnGroupInfo.groupID);
        }
    }
    void groupUpgradeBomb(OsnGroupInfo osnGroupInfo, String state){
        GroupInfo groupInfo = SqliteUtils.queryGroup(osnGroupInfo.groupID);
        if (groupInfo != null) {
            groupInfo.redPacket = 1;
            SqliteUtils.updateGroup(groupInfo, Collections.singletonList("redPacket"));
            Message message = new Message();
            message.sender = groupInfo.target;
            message.direction = MessageDirection.Receive;
            message.status = MessageStatus.Readed;
            message.conversation = new Conversation(Conversation.ConversationType.Group, osnGroupInfo.groupID, 0);
            message.serverTime = osnGroupInfo.genGroupNoticeTime();//System.currentTimeMillis();
            message.content = getGroupNotify(state, osnGroupInfo);

            if (osnGroupInfo.notice != null) {
                message.messageHash = osnGroupInfo.notice.getString("hash");
                message.messageHash0 = osnGroupInfo.notice.getString("hash0");
                System.out.println("@@@@@ notice bomb group hash : "+message.messageHash);
            }

            long mid = SqliteUtils.insertMessage(message);
            if (mid != 0) {
                onReceiveMessage(Collections.singletonList(message), false);
            } else {
                System.out.println("@@@@@ notice bomb group insert failed.");
            }

            //SqliteUtils.insertMessage(message);
            //onReceiveMessage(Collections.singletonList(message), false);
            onGroupInfoUpdated(Collections.singletonList(groupInfo));
            logInfo("upgradeBomb: " + osnGroupInfo.groupID);
        }
    }
    void groupWin(OsnGroupInfo osnGroupInfo, String state){
        Message message = new Message();
        message.sender = osnGroupInfo.groupID;
        message.direction = MessageDirection.Receive;
        message.status = MessageStatus.Readed;
        message.conversation = new Conversation(Conversation.ConversationType.Group, osnGroupInfo.groupID, 0);
        message.serverTime = System.currentTimeMillis();
        message.content = getGroupNotify(state, osnGroupInfo);

        if (osnGroupInfo.notice != null) {
            message.messageHash = osnGroupInfo.notice.getString("hash");
            message.messageHash0 = osnGroupInfo.notice.getString("hash0");
            System.out.println("@@@@@ notice win hash : "+message.messageHash);
        }

        long mid = SqliteUtils.insertMessage(message);
        if (mid != 0) {
            onReceiveMessage(Collections.singletonList(message), false);
        } else {
            System.out.println("@@@@@ notice win insert failed.");
        }
        //SqliteUtils.insertMessage(message);
        //onReceiveMessage(Collections.singletonList(message), false);
    }
    public void logout(int status){
        logined = false;
        mUserId = null;

        mSp.edit().clear().apply();
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        sp.edit().clear().apply();

        osnsdk.logout(null);
        SqliteUtils.closeDB();
        onConnectionStatusChanged(status);
    }
    /*void groupMute(OsnGroupInfo osnGroupInfo){
        GroupMember groupMember = SqliteUtils.queryMember(osnGroupInfo.groupID, mUserId);
        if (groupMember == null){
            logInfo("no in the member");
            return;
        }
        groupMember.mute = osnGroupInfo.mute;
        SqliteUtils.updateMember(groupMember, Collections.singletonList("mute"));
        onGroupMembersUpdated(osnGroupInfo.groupID, Collections.singletonList(groupMember));

        GroupNotifyContent groupNotifyContent = new GroupNotifyContent();
        groupNotifyContent.info = osnGroupInfo.data.getString("text");
        Message message = new Message();
        message.sender = osnGroupInfo.groupID;
        message.direction = MessageDirection.Receive;
        message.status = MessageStatus.Readed;
        message.conversation = new Conversation(Conversation.ConversationType.Group, osnGroupInfo.groupID, 0);
        message.serverTime = System.currentTimeMillis();
        message.content = groupNotifyContent;
        SqliteUtils.insertMessage(message);
        onReceiveMessage(Collections.singletonList(message), false);
    }*/

    void groupMute(OsnGroupInfo osnGroupInfo){
        GroupMember groupMember = SqliteUtils.queryMember(osnGroupInfo.groupID, mUserId);
        if (groupMember == null)
            logInfo("no in the member");
//        GroupMember groupMember = SqliteUtils.queryMember(osnGroupInfo.groupID, mUserId);
//        if (groupMember == null){
//            logInfo("no in the member");
//            return;
//        }
//        groupMember.mute = osnGroupInfo.mute;
//        SqliteUtils.updateMember(groupMember, Collections.singletonList("mute"));
//        onGroupMembersUpdated(osnGroupInfo.groupID, Collections.singletonList(groupMember));
        GroupInfo groupInfo = SqliteUtils.queryGroup(osnGroupInfo.groupID);
        if(groupInfo == null){
            logInfo("no find group: "+osnGroupInfo.groupID);
            return;
        }
        //groupMember.mute = osnGroupInfo.mute;
        //SqliteUtils.updateMember(groupMember, Collections.singletonList("mute"));
        onGroupMembersUpdated(osnGroupInfo.groupID, Collections.singletonList(groupMember));
        groupInfo.mute = osnGroupInfo.mute;
        SqliteUtils.updateGroup(groupInfo, Collections.singletonList("mute"));
        onGroupInfoUpdated(Collections.singletonList(groupInfo));

        GroupNotifyContent groupNotifyContent = new GroupNotifyContent();
        groupNotifyContent.info = osnGroupInfo.data.getString("text");
        Message message = new Message();
        message.sender = osnGroupInfo.groupID;
        message.direction = MessageDirection.Receive;
        message.status = MessageStatus.Readed;
        message.conversation = new Conversation(Conversation.ConversationType.Group, osnGroupInfo.groupID, 0);
        message.serverTime = osnGroupInfo.genGroupNoticeTime();//System.currentTimeMillis();
        message.content = groupNotifyContent;

        if (osnGroupInfo.notice != null) {
            message.messageHash = osnGroupInfo.notice.getString("hash");
            message.messageHash0 = osnGroupInfo.notice.getString("hash0");
            System.out.println("@@@@@ notice mute hash : "+message.messageHash);
        }

        long mid = SqliteUtils.insertMessage(message);
        if (mid != 0) {
            onReceiveMessage(Collections.singletonList(message), false);
        } else {
            System.out.println("@@@@@ notice mute insert failed.");
        }

        //SqliteUtils.insertMessage(message);
        //onReceiveMessage(Collections.singletonList(message), false);
    }

    public void resendThread(){
        new Thread(()->{
            try{
                Object isOk = new Object();
                final String[] status = {"0"};
                List<Message> messages = SqliteUtils.queryFailureMessage();
                for(Message msg : messages){
                    status[0] = "0";
                    osnsdk.sendMessage(msg.messageJson, msg.conversation.target, new OSNGeneralCallback() {
                        @Override
                        public void onSuccess(JSONObject data) {
                            logInfo("resend msg success: "+msg.messageJson);
                            mBinder.sendResult(true, data, msg.messageId, null, null, msg.serverTime);
                            synchronized (isOk){
                                isOk.notify();
                            }
                        }
                        @Override
                        public void onFailure(String error) {
                            logInfo("resend msg failure: "+msg.messageJson);
                            status[0] = "1";
                            synchronized (isOk){
                                isOk.notify();
                            }
                        }
                    });
                    synchronized (isOk){
                        isOk.wait(10000);
                    }
                    if(status[0].equalsIgnoreCase("0"))
                        break;
                }
                Thread.sleep(10000);
            }catch (Exception e){
                logError(e);
            }
        }).start();
    }
    public void networkListen(){
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        NetworkRequest request = builder.build();
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        connMgr.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                osnsdk.networkOn();
                isNetworkOn = true;
                logInfo("onAvailable: "+network.toString());
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                osnsdk.networkOn();
                isNetworkOn = false;
                logInfo("onLost: "+network.toString());
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities);
                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        logInfo("onCapabilitiesChanged: TRANSPORT_WIFI");
                    } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        logInfo("onCapabilitiesChanged: TRANSPORT_CELLULAR");
                    } else {
                        logInfo("onCapabilitiesChanged: other");
                    }
                }
            }
        });
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //this.clientId = intent.getStringExtra("clientId");
        return mBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        inst = this;

        handler = new Handler(Looper.getMainLooper());
        mContext = this;
        mSp = getSharedPreferences("osnp.config", Context.MODE_PRIVATE);

        try {
            mBinder.registerMessageContent(GroupNotifyContent.class.getName());
            mBinder.registerMessageContent(AddGroupMemberNotificationContent.class.getName());
            mBinder.registerMessageContent(CallStartMessageContent.class.getName());
            mBinder.registerMessageContent(ChangeGroupNameNotificationContent.class.getName());
            mBinder.registerMessageContent(ChangeGroupPortraitNotificationContent.class.getName());
            mBinder.registerMessageContent(CreateGroupNotificationContent.class.getName());
            mBinder.registerMessageContent(DismissGroupNotificationContent.class.getName());
            mBinder.registerMessageContent(FileMessageContent.class.getName());
            mBinder.registerMessageContent(ImageMessageContent.class.getName());
            //mBinder.registerMessageContent(ImageTextMessageContent.class.getName());
            mBinder.registerMessageContent(KickoffGroupMemberNotificationContent.class.getName());
            mBinder.registerMessageContent(LocationMessageContent.class.getName());
            mBinder.registerMessageContent(ModifyGroupAliasNotificationContent.class.getName());
            mBinder.registerMessageContent(QuitGroupNotificationContent.class.getName());
            mBinder.registerMessageContent(RecallMessageContent.class.getName());
            mBinder.registerMessageContent(DeleteMessageContent.class.getName());
            mBinder.registerMessageContent(SoundMessageContent.class.getName());
            mBinder.registerMessageContent(StickerMessageContent.class.getName());
            mBinder.registerMessageContent(TextMessageContent.class.getName());
            mBinder.registerMessageContent(PTextMessageContent.class.getName());
            mBinder.registerMessageContent(TipNotificationContent.class.getName());
            mBinder.registerMessageContent(FriendAddedMessageContent.class.getName());
            mBinder.registerMessageContent(FriendGreetingMessageContent.class.getName());
            mBinder.registerMessageContent(TransferGroupOwnerNotificationContent.class.getName());
            mBinder.registerMessageContent(VideoMessageContent.class.getName());
            mBinder.registerMessageContent(TypingMessageContent.class.getName());
            mBinder.registerMessageContent(GroupMuteNotificationContent.class.getName());
            mBinder.registerMessageContent(GroupJoinTypeNotificationContent.class.getName());
            mBinder.registerMessageContent(GroupPrivateChatNotificationContent.class.getName());
            mBinder.registerMessageContent(GroupSetManagerNotificationContent.class.getName());
            mBinder.registerMessageContent(GroupMuteMemberNotificationContent.class.getName());
        } catch (RemoteException e) {
            logError(e);
        }

        mUserId = osnsdk.getUserID();
        System.out.println("onCreate db path mUserId: " + mUserId);
        if (mUserId != null) {
            String sdcard = getFilesDir().getAbsolutePath();
            String dbPath = sdcard + "/" + mUserId + ".db";
            System.out.println("onCreate db path : " + dbPath);
            initDB(dbPath);
        }

        networkListen();
        //resendThread();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    public void onConnectionStatusChanged(int status) {
        handler.post(() -> {
            int i = onConnectionStatusChangeListenes.beginBroadcast();
            IOnConnectionStatusChangeListener listener;
            while (i > 0) {
                i--;
                listener = onConnectionStatusChangeListenes.getBroadcastItem(i);
                try {
                    listener.onConnectionStatusChange(status);
                } catch (RemoteException e) {
                    logError(e);
                }
            }
            onConnectionStatusChangeListenes.finishBroadcast();
        });
    }
    public void onRecallMessage(long messageUid) {
        handler.post(() -> {
            int receiverCount = onReceiveMessageListeners.beginBroadcast();
            IOnReceiveMessageListener listener;
            while (receiverCount > 0) {
                receiverCount--;
                listener = onReceiveMessageListeners.getBroadcastItem(receiverCount);
                try {
                    listener.onRecall(messageUid);
                } catch (RemoteException e) {
                    logError(e);
                }
            }
            onReceiveMessageListeners.finishBroadcast();
        });
    }
    public void onDeleteMessage(long messageUid) {
        handler.post(() -> {
            int receiverCount = onReceiveMessageListeners.beginBroadcast();
            IOnReceiveMessageListener listener;
            while (receiverCount > 0) {
                receiverCount--;
                listener = onReceiveMessageListeners.getBroadcastItem(receiverCount);
                try {
                    listener.onDelete(messageUid);
                } catch (RemoteException e) {
                    logError(e);
                }
            }
            onReceiveMessageListeners.finishBroadcast();
        });
    }

    public void onUserReceivedMessage(Map<String, Long> map) {
        handler.post(() -> {
            int receiverCount = onReceiveMessageListeners.beginBroadcast();
            IOnReceiveMessageListener listener;
            while (receiverCount > 0) {
                receiverCount--;
                listener = onReceiveMessageListeners.getBroadcastItem(receiverCount);
                try {
                    listener.onDelivered(map);
                } catch (RemoteException e) {
                    logError(e);
                }
            }
            onReceiveMessageListeners.finishBroadcast();
        });
    }
    public void onUserReadedMessage(List<ReadEntry> list) {
        handler.post(() -> {
            List<ReadEntry> l = new ArrayList<>();
//            for (ProtoReadEntry entry : list) {
//                ReadEntry r = new ReadEntry();
//                r.conversation = new Conversation(Conversation.ConversationType.type(entry.conversationType), entry.target, entry.line);
//                r.userId = entry.userId;
//                r.readDt = entry.readDt;
//                l.add(r);
//            }

            int receiverCount = onReceiveMessageListeners.beginBroadcast();
            IOnReceiveMessageListener listener;
            while (receiverCount > 0) {
                receiverCount--;
                listener = onReceiveMessageListeners.getBroadcastItem(receiverCount);
                try {
                    listener.onReaded(l);
                } catch (RemoteException e) {
                    logError(e);
                }
            }
            onReceiveMessageListeners.finishBroadcast();
        });
    }
    private void onReceiveMessageInternal(List<Message> messages) {
        int receiverCount = onReceiveMessageListeners.beginBroadcast();
        IOnReceiveMessageListener listener;
        while (receiverCount > 0) {
            receiverCount--;
            listener = onReceiveMessageListeners.getBroadcastItem(receiverCount);
            try {
//                SafeIPCMessageEntry entry;
//                int startIndex = 0;
//                do {
//                    entry = buildSafeIPCMessages(protoMessages, startIndex, false);
//                    listener.onReceive(entry.messages, entry.messages.size() > 0 && startIndex != protoMessages.length - 1);
//                    startIndex = entry.index + 1;
//                } while (entry.index > 0 && entry.index < protoMessages.length - 1);
                listener.onReceive(messages, false);
            } catch (Exception e) {
                logError(e);
            }
        }
        onReceiveMessageListeners.finishBroadcast();
    }
    public void onReceiveMessage(List<Message> messages, boolean hasMore) {
        if (mConnectionStatus == ConnectionStatusReceiveing && hasMore) {
            return;
        }
        if (messages.isEmpty()) {
            return;
        }
        handler.post(() -> onReceiveMessageInternal(messages));
    }
    public void onFriendListUpdated(String[] friendList) {
        handler.post(() -> {
            int i = onFriendUpdateListenerRemoteCallbackList.beginBroadcast();
            IOnFriendUpdateListener listener;
            while (i > 0) {
                i--;
                listener = onFriendUpdateListenerRemoteCallbackList.getBroadcastItem(i);
                try {
                    listener.onFriendListUpdated(Arrays.asList(friendList));
                } catch (RemoteException e) {
                    logError(e);
                }
            }
            onFriendUpdateListenerRemoteCallbackList.finishBroadcast();
        });
    }
    public void onFriendRequestUpdated(String[] newRequestList) {
        handler.post(() -> {
            int i = onFriendUpdateListenerRemoteCallbackList.beginBroadcast();
            IOnFriendUpdateListener listener;
            while (i > 0) {
                i--;
                listener = onFriendUpdateListenerRemoteCallbackList.getBroadcastItem(i);
                try {
                    listener.onFriendRequestUpdated(Arrays.asList(newRequestList));
                } catch (RemoteException e) {
                    logError(e);
                }
            }
            onFriendUpdateListenerRemoteCallbackList.finishBroadcast();
        });
    }
    public void onGroupInfoUpdated(List<GroupInfo> groups) {
        if (groups == null || groups.isEmpty()) {
            return;
        }
        handler.post(() -> {
            int i = onGroupInfoUpdateListenerRemoteCallbackList.beginBroadcast();
            IOnGroupInfoUpdateListener listener;
            while (i > 0) {
                i--;
                listener = onGroupInfoUpdateListenerRemoteCallbackList.getBroadcastItem(i);
                try {
                    listener.onGroupInfoUpdated(groups);
                } catch (RemoteException e) {
                    logError(e);
                }
            }
            onGroupInfoUpdateListenerRemoteCallbackList.finishBroadcast();
        });
    }
    public void onLitappInfoUpdated(List<LitappInfo> litappInfos) {
        if (litappInfos == null || litappInfos.isEmpty()) {
            return;
        }
        handler.post(() -> {
            int i = onLitappInfoUpdateListenerRemoteCallbackList.beginBroadcast();
            IOnLitappInfoUpdateListener listener;
            while (i > 0) {
                i--;
                listener = onLitappInfoUpdateListenerRemoteCallbackList.getBroadcastItem(i);
                try {
                    listener.onLitappInfoUpdated(litappInfos);
                } catch (RemoteException e) {
                    logError(e);
                }
            }
            onLitappInfoUpdateListenerRemoteCallbackList.finishBroadcast();
        });
    }
    public void onLitappInfoResult(List<LitappInfo> litappInfos) {
        if (litappInfos == null || litappInfos.isEmpty()) {
            return;
        }
        handler.post(() -> {
            int i = onLitappInfoResultListenerRemoteCallbackList.beginBroadcast();
            IOnLitappInfoResultListener listener;
            while (i > 0) {
                i--;
                listener = onLitappInfoResultListenerRemoteCallbackList.getBroadcastItem(i);
                try {
                    listener.onLitappInfoResult(litappInfos);
                } catch (RemoteException e) {
                    logError(e);
                }
            }
            onLitappInfoResultListenerRemoteCallbackList.finishBroadcast();
        });
    }
    public void onGroupMembersUpdated(String groupId, List<GroupMember> members) {

        List<GroupMember> membersCopy = new ArrayList<>();

        for (GroupMember member : members) {
            if (member == null) {
                System.out.println("[onGroupMembersUpdated] member null.");
            } else {
                if (member.memberId == null) {
                    System.out.println("[onGroupMembersUpdated] member id null.");
                } else {
                    membersCopy.add(member);
                }
            }
        }
        if (membersCopy.size() == 0) {
            return;
        }

        handler.post(() -> {
            int i = onGroupMembersUpdateListenerRemoteCallbackList.beginBroadcast();
            IOnGroupMembersUpdateListener listener;
            while (i > 0) {
                i--;
                listener = onGroupMembersUpdateListenerRemoteCallbackList.getBroadcastItem(i);
                try {
                    listener.onGroupMembersUpdated(groupId, membersCopy);
                } catch (RemoteException e) {
                    logError(e);
                    return;
                }
            }
            onGroupMembersUpdateListenerRemoteCallbackList.finishBroadcast();
        });
    }
    public void onSettingUpdated() {
        handler.post(() -> {
            int i = onSettingUpdateListenerRemoteCallbackList.beginBroadcast();
            IOnSettingUpdateListener listener;
            while (i > 0) {
                i--;
                listener = onSettingUpdateListenerRemoteCallbackList.getBroadcastItem(i);
                try {
                    listener.onSettingUpdated();
                } catch (RemoteException e) {
                    logError(e);
                }
            }
            onSettingUpdateListenerRemoteCallbackList.finishBroadcast();
        });
    }
    public void onUserInfoUpdated(List<UserInfo> users) {
        handler.post(() -> {
            int i = onUserInfoUpdateListenerRemoteCallbackList.beginBroadcast();
            IOnUserInfoUpdateListener listener;
            while (i > 0) {
                i--;
                listener = onUserInfoUpdateListenerRemoteCallbackList.getBroadcastItem(i);
                try {
                    listener.onUserInfoUpdated(users);
                } catch (RemoteException e) {
                    logError(e);
                }
            }
            onUserInfoUpdateListenerRemoteCallbackList.finishBroadcast();
        });
    }
    public void onConferenceEvent(String s) {
        handler.post(() -> {
            int i = onConferenceEventListenerRemoteCallbackList.beginBroadcast();
            IOnConferenceEventListener listener;
            while (i > 0) {
                i--;
                listener = onConferenceEventListenerRemoteCallbackList.getBroadcastItem(i);
                try {
                    listener.onConferenceEvent(s);
                } catch (RemoteException e) {
                    logError(e);
                }
            }
            onConferenceEventListenerRemoteCallbackList.finishBroadcast();
        });
    }
    private class ClientServiceStub extends IRemoteClient.Stub {
        public void createTempAccount(){
            osnsdk.createTempAccount();
        }

        public String[] createOsnIdFromMnemonic(String b64Seed, String password)
        {
            byte[] seed = b64Decode(b64Seed);
            return osnsdk.createOsnIdFromMnemonic(seed, password);
            //return null;
        }

        public List getWalletsInfo(){
            return SqliteUtils.listWallets();
        }

        @Override
        public void deleteWallets(String osnID){
            SqliteUtils.deleteWallets(osnID);
        }

        @Override
        public void deleteDBConfig() throws RemoteException {
            //  mSp.edit().remove("dbPath");
            //mSp.edit().putString("dbPath", "").apply();
            SPUtils.remove(mContext,"dbPath");
        }

        public String getWallets(String walletId){
            return SqliteUtils.readWallet(walletId);
        }

        public void insertWallets(String wallet){
            try {
                WalletsInfo walletsInfo = new WalletsInfo();

                JSONObject walletJson = JSONObject.parseObject(wallet);
                JSONObject paramJson = walletJson.getJSONObject("param");

                walletsInfo.name = paramJson.getString("name");
                walletsInfo.OsnID = walletJson.getString("target");
                walletsInfo.wallets = wallet;

                SqliteUtils.insertWallets(walletsInfo);

            } catch (Exception e) {

            }
        }

        public void setVoiceBaseUrl(String voiceBaseUrl){
            SPUtils.put(mContext,"voiceBaseUrl",voiceBaseUrl);
        }
        public String getVoiceBaseUrl(){
            String voiceBaseUrl1 = (String) SPUtils.get(mContext,"voiceBaseUrl","");
            return voiceBaseUrl1;
        }

        public void setVoiceHostUrl(String voiceHostUrl){
            SPUtils.put(mContext,"voiceHostUrl",voiceHostUrl);
        }

        public String getVoiceHostUrl(){
            String voiceHostUrl = (String) SPUtils.get(mContext,"voiceHostUrl","");
            return voiceHostUrl;
        }
        public String getMainDapp(){
            String mainDapp = (String) SPUtils.get(mContext,"MainDapp","");
            return mainDapp;
        }
        public void setMainDapp(String mainDapp){
            SPUtils.put(mContext,"MainDapp",mainDapp);
        }

        /*public void spSet(String key, Object object){
            SPUtils.put(mContext, key, object);
        }
        public Object spGet(String key, Object defaultObject){
            return SPUtils.get(mContext, key, defaultObject);
        }*/

        public void removeVoiceSetup(){
            SPUtils.remove(mContext, "voiceBaseUrl");
            SPUtils.remove(mContext, "voiceHostUrl");
        }




        public void setHideEnable1(String str){
            osnsdk.setHideEnable1(str);
        }

        public String getShadowKeyPubId(String decPwd){
            return osnsdk.getShadowKeyPubId(decPwd);
        }
        public String signByShadowKey(String hash, String decPwd){
            return osnsdk.signByShadowKey(hash, decPwd);
        }

        public boolean getEnable(String key){
            return osnsdk.getEnable(key);
        }

        public void setEnable(String key, String value) {
            osnsdk.setEnable(key, value);
        }



        @Override
        public void setRole(String key, String value, IGeneralCallback callback) throws RemoteException {
            osnsdk.setRole(key,value, null);
        }

        public void setHideEnable2(String str){
            osnsdk.setHideEnable2(str);
        }
        public void setHideEnable3(String str){
            osnsdk.setHideEnable3(str);
        }


        public boolean getHideEnable(){
            return osnsdk.getHideEnable();
        }

        public boolean getHideEnable3(){
            return osnsdk.getHideEnable3();
        }


        @Override
        public void findObject(String text, final IGeneralCallback callback) {

            osnsdk.findObject(text, new OSNGeneralCallback() {

                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        callback.onSuccess();
                    } catch (Exception e) {
                        logError(e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        callback.onFailure(-1);
                    } catch (Exception e) {
                        logError(e);
                    }
                }
            });
        }



        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public void setLanguage(String language){
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Resources resources = getResources();
            Configuration config = resources.getConfiguration();
            config.setLocale(locale);
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }
        @Override
        public void simpleLogin(String target, String url, IGeneralCallback2 callback) {
            osnsdk.simpleLogin(target, url, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        callback.onSuccess(data.toString());
                    } catch (Exception e) {
                        logError(e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        logInfo(error);
                        callback.onFailure(-1);
                    } catch (Exception e) {
                        logError(e);
                    }
                }
            });
        }
        @Override
        public void litappLogin(LitappInfo litappInfo, String url, IGeneralCallback2 callback) {
            System.out.println("@@@ 1234");
            osnsdk.lpLogin(toSdkLitapp(litappInfo), url, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        callback.onSuccess(data.toString());
                    } catch (Exception e) {
                        logError(e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        logInfo(error);
                        callback.onFailure(-1);
                    } catch (Exception e) {
                        logError(e);
                    }
                }
            });
        }

        @Override
        public void register(String userName, String password, String serviceID, IGeneralCallback callback) throws RemoteException {
            System.out.println("db path init sdk 1.");
            osnsdk.initSDK(mHost, mListener);
            osnsdk.register(userName, password, serviceID, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        callback.onSuccess();
                    } catch (Exception e) {
                        logError(e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        callback.onFailure(-1);
                    } catch (Exception e) {
                        logError(e);
                    }
                }
            });
        }

        @Override
        public boolean connect(String userName, String userPwd) throws RemoteException {
            if (userName == null) {
                System.out.println("db path init sdk 2." + osnsdk);
                osnsdk.initSDK(mHost, mListener);
                initDB2();
                return true;
            }

            onConnectionStatusChanged(ConnectionStatusConnecting);
            /*if (keywords == null) {
                new Thread(() -> {
                    String filterUrl = "http://" + mHost + ":8300/keywordFilter";
                    String data = HttpUtils.doGet(filterUrl);
                    if (data != null) {
                        logInfo("keywords: " + data);
                        JSONObject json = JSON.parseObject(data);
                        JSONArray keywordList = json.getJSONArray("keywords");
                        keywords = keywordList.toJavaList(String.class);
                    }
                }).start();
            }*/

            if (userName.startsWith("OSN")) {
                logined = osnsdk.loginWithOsnID(userName, null);
            }
            else
                logined = osnsdk.loginWithName(userName, userPwd, null);
            if (logined) {
                mUserId = osnsdk.getUserID();
                String sdcard = getFilesDir().getAbsolutePath();
                String dbPath = sdcard + "/" + mUserId + ".db";
                System.out.println("@@@   111111    dbPath :"+dbPath);
                initDB(dbPath);
            }
            return logined;
        }

        @Override
        public boolean connect2(String userName, String userPwd, String password2) throws RemoteException {
            if (userName == null) {
                System.out.println("db path init sdk 3.");
                osnsdk.initSDK(mHost, mListener);
                initDB2();
                return true;
            }

            onConnectionStatusChanged(ConnectionStatusConnecting);

            if (!userName.startsWith("OSN")) {
                return false;
            }

            String[] pwd = userPwd.split("-");
            if (pwd == null) {
                return false;
            }
            if (pwd.length != 3) {
                return false;
            }

            if (!pwd[0].equals("VER2")) {
                return false;
            }

            logined = osnsdk.loginV2(userName, userPwd, password2, null);


            if (logined) {
                mUserId = osnsdk.getUserID();
                String sdcard = getFilesDir().getAbsolutePath();
                String dbPath = sdcard + "/" + mUserId + ".db";
                System.out.println("@@@   222222    dbPath :"+dbPath);
                initDB(dbPath);
            }
            return logined;
        }

        @Override
        public void setOnReceiveMessageListener(IOnReceiveMessageListener listener) throws RemoteException {
            onReceiveMessageListeners.register(listener);
        }

        @Override
        public void setOnConnectionStatusChangeListener(IOnConnectionStatusChangeListener listener) throws RemoteException {
            onConnectionStatusChangeListenes.register(listener);
        }


        @Override
        public void setOnUserInfoUpdateListener(IOnUserInfoUpdateListener listener) throws RemoteException {
            onUserInfoUpdateListenerRemoteCallbackList.register(listener);
        }

        @Override
        public void setOnGroupInfoUpdateListener(IOnGroupInfoUpdateListener listener) throws RemoteException {
            onGroupInfoUpdateListenerRemoteCallbackList.register(listener);
        }

        @Override
        public void setOnFriendUpdateListener(IOnFriendUpdateListener listener) throws RemoteException {
            onFriendUpdateListenerRemoteCallbackList.register(listener);
        }

        @Override
        public void setOnSettingUpdateListener(IOnSettingUpdateListener listener) throws RemoteException {
            onSettingUpdateListenerRemoteCallbackList.register(listener);
        }

        @Override
        public void setOnChannelInfoUpdateListener(IOnChannelInfoUpdateListener listener) throws RemoteException {
            onChannelInfoUpdateListenerRemoteCallbackList.register(listener);
        }

        @Override
        public void setOnConferenceEventListener(IOnConferenceEventListener listener) throws RemoteException {
            onConferenceEventListenerRemoteCallbackList.register(listener);
        }

        @Override
        public void setOnLitappInfoUpdateListener(IOnLitappInfoUpdateListener listener) throws RemoteException {
            onLitappInfoUpdateListenerRemoteCallbackList.register(listener);
        }

        @Override
        public void setOnLitappInfoResultListener(IOnLitappInfoResultListener listener) throws RemoteException {
            onLitappInfoResultListenerRemoteCallbackList.register(listener);
        }

        @Override
        public void setOnGroupMembersUpdateListener(IOnGroupMembersUpdateListener listener) throws RemoteException {
            onGroupMembersUpdateListenerRemoteCallbackList.register(listener);
        }


        @Override
        public void disconnect(boolean disablePush, boolean clearSession) throws RemoteException {
            /*onConnectionStatusChanged(ConnectionStatusLogout);
            mSp.edit().clear().apply();

            logined = false;
            mUserId = null;

            osnsdk.logout(null);
            SqliteUtils.closeDB();*/
            logout(ConnectionStatusLogout);
        }

        @Override
        public void setForeground(int isForeground) throws RemoteException {
//            BaseEvent.onForeground(isForeground == 1);
        }

        @Override
        public void onNetworkChange() {
            //BaseEvent.onNetworkChange();
        }

        @Override
        public void setServerAddress(String host) throws RemoteException {
            mHost = host;
        }

        @Override
        public void registerMessageContent(String msgContentCls) throws RemoteException {
            try {
                Class cls = Class.forName(msgContentCls);
                Constructor c = cls.getConstructor();
                if (c.getModifiers() != Modifier.PUBLIC) {
                    throw new IllegalArgumentException("the default constructor of your custom messageContent class should be public");
                }
                ContentTag tag = (ContentTag) cls.getAnnotation(ContentTag.class);
                if (tag != null) {
                    Class curClazz = contentMapper.get(tag.type());
                    if (curClazz != null && !curClazz.equals(cls)) {
                        throw new IllegalArgumentException("messageContent type duplicate " + msgContentCls);
                    }
                    contentMapper.put(tag.type(), cls);
//                    try {
//                        ProtoLogic.registerMessageFlag(tag.type(), tag.flag().getValue());
//                    } catch (Throwable e) {
//                        // ref to: https://github.com/Tencent/mars/issues/334
//                        ProtoLogic.registerMessageFlag(tag.type(), tag.flag().getValue());
//                    }
                } else {
                    throw new IllegalStateException("ContentTag annotation must be set!");
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("custom messageContent class can not found: " + msgContentCls);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("custom messageContent class must have a default constructor");
            }
        }

        private void sendResult(boolean isSuccess, JSONObject data, long mid, ConversationInfo conversationInfo, final ISendMessageCallback callback, long timestamp){
            try {
                if(isSuccess){
                    if(data != null) {
                        System.out.println("[QuoteMessage] updateMessage data:" + data);
                        SqliteUtils.updateMessage(mid, MessageStatus.Sent.value(), data.getString("msgHash"));
                    }
                    if (conversationInfo != null)
                        SqliteUtils.updateConversation(conversationInfo, Collections.singletonList("timestamp"));
                    if(callback != null)
                        callback.onSuccess(mid, timestamp);
                }else{
                    SqliteUtils.updateMessage(mid, Send_Failure.value());
                    if (conversationInfo != null)
                        SqliteUtils.updateConversation(conversationInfo, Collections.singletonList("timestamp"));
                    if(callback != null)
                        callback.onFailure(-1);
                }
            } catch (Exception e) {
                logError(e);
            }
        }
        private void sendAlone(JSONObject json, long mid, ConversationInfo conversationInfo, final ISendMessageCallback callback, long timestamp){
            if(!isNetworkOn){
                sendResult(false, null, mid, conversationInfo, callback, timestamp);
                SqliteUtils.updateMessage(mid, json.toString());
                return;
            }
            osnsdk.sendMessageAlone(json.toString(), conversationInfo.conversation.target, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    sendResult(true, data, mid, conversationInfo, callback, timestamp);
                }

                @Override
                public void onFailure(String error) {
                    sendResult(true, null, mid, conversationInfo, callback, timestamp);
                    SqliteUtils.updateMessage(mid, json.toString());
                    SqliteUtils.updateMessage(mid, Send_Failure.value());
                }
            });
        }
        private void sendmsg(int retry, JSONObject json, String target, long mid, ConversationInfo conversationInfo, final ISendMessageCallback callback, long timestamp){
            osnsdk.sendMessage(json.toString(), target, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    sendResult(true, data, mid, conversationInfo, callback, timestamp);
                }
                @Override
                public void onFailure(String error) {
                    try {
                        if(retry >= 2){
                            sendmsg(retry+1, json, target, mid, conversationInfo, callback, timestamp);
                        }else {
                            sendResult(false, null, mid, conversationInfo, callback, timestamp);
                        }
                    } catch (Exception e) {
                        logError(e);
                    }
                }
            });
        }
        private void sendmsg(Message msg, final ISendMessageCallback callback, int expireDuration, long mid, long timestamp) throws RemoteException {
            try {
                msg.sender = mUserId;
                ConversationInfo conversationInfo = SqliteUtils.queryConversation(msg.conversation.type.getValue(), msg.conversation.target, msg.conversation.line);
                if (conversationInfo == null)
                    logInfo("conversationInfo == null");
                else {
                    conversationInfo.timestamp = msg.serverTime;
                }

                JSONObject json = new JSONObject();
                int msgType = msg.content.getMessageContentType();
                if (msgType == MessageContentType.ContentType_Text ||
                        msgType == MessageContentType.ContentType_Call ||
                        msgType == MessageContentType.ContentType_Card ||
                        msgType == MessageContentType.ContentType_RedPacket) {
                    if (msgType == MessageContentType.ContentType_Text) {
                        TextMessageContent textMessageContent = (TextMessageContent) msg.content;
                        json.put("type", "text");
                        json.put("data", textMessageContent.getContent());
                        if(textMessageContent.getQuoteInfo() != null){
                            QuoteInfo quoteInfo = new QuoteInfo();
                            quoteInfo.setMessageDigest(textMessageContent.getQuoteInfo().getMessageDigest());
                            String hash = textMessageContent.getQuoteInfo().getMessageHash();
                            String hash0 = textMessageContent.getQuoteInfo().getMessageHash0();
                            quoteInfo.setMessageHash(hash);    //个人聊天
                            quoteInfo.setMessageHash0(hash0);   // 群聊天
                            quoteInfo.setUserId(textMessageContent.getQuoteInfo().getUserId());
                            quoteInfo.setMessageUid(textMessageContent.getQuoteInfo().getMessageUid());
                            quoteInfo.setUserDisplayName(textMessageContent.getQuoteInfo().getUserDisplayName());
                            textMessageContent.setQuoteInfo(quoteInfo);
                            json.put("quoteInfo",textMessageContent.getQuoteInfo());
                        }

                        json.put("mentionedType",textMessageContent.mentionedType);
                        json.put("mentionedTargets",textMessageContent.mentionedTargets);
                    } else if (msgType == MessageContentType.ContentType_RedPacket){
                        RedPacketMessageContent redPacketMessageContent = (RedPacketMessageContent)msg.content;
                        json.put("type", "redPacket");


                        //json.put("data", redPacketMessageContent.info);
                        JSONObject dataJson = JSONObject.parseObject(redPacketMessageContent.info);
                        String wallet = dataJson.getString("wallet");
                        if (wallet != null) {
                            JSONObject walletJson = JSONObject.parseObject(wallet);
                            dataJson.remove("wallet");
                            dataJson.put("wallet",walletJson);
                        }




                        json.put("data", dataJson);


                        System.out.println("@@@ info3 : " + redPacketMessageContent.info);
                        System.out.println("@@@ info4 : " + json.toString());
                        RedPacketInfo redPacketInfo = new RedPacketInfo(JSON.parseObject(redPacketMessageContent.info));
                        SqliteUtils.insertRedPacket(redPacketInfo);

                    } else if (msgType == MessageContentType.ContentType_Card) {
                        CardMessageContent cardMessageContent = (CardMessageContent) msg.content;
                        json.put("type", "card");
                        switch (cardMessageContent.getType()) {
                            case CardType_User:
                                json.put("cardType", "user");
                                break;
                            case CardType_Group:
                                json.put("cardType", "group");
                                break;
                            case CardType_ChatRoom:
                                json.put("cardType", "chatroom");
                                break;
                            case CardType_Channel:
                                json.put("cardType", "channel");
                                break;
                            case CardType_Litapp:
                                json.put("cardType", "litapp");
                                break;
                            case CardType_Share:
                                json.put("cardType", "share");
                                break;
                        }
                        json.put("target", cardMessageContent.getTarget());
                        json.put("name", cardMessageContent.getName());
                        json.put("displayName", cardMessageContent.getDisplayName());
                        json.put("portrait", cardMessageContent.getPortrait());
                        json.put("theme", cardMessageContent.getTheme());
                        json.put("url", cardMessageContent.getUrl());
                        json.put("info", cardMessageContent.getInfo());

                   /* }else if(msgType == MessageContentType.ContentType_Call){
                        CallMessageContent callMessageContent = (CallMessageContent) msg.content;
                        json.put("type", "call");
                        json.put("callMode", callMessageContent.mode);
                        json.put("callType", callMessageContent.type);
                        json.put("callAction", callMessageContent.action);
                        json.put("url", callMessageContent.url);*/
                    }


                    //sendmsg(0, json, msg.conversation.target, mid, conversationInfo, callback, timestamp);
                    JSONObject msg2 = osnsdk.sendMessage(json.toString(), msg.conversation.target, new OSNGeneralCallback() {
                        @Override
                        public void onSuccess(JSONObject data) {
                            sendResult(true, data, mid, conversationInfo, callback, timestamp);
                            System.out.println("@@@   2");

                            if (msg.conversation.target.startsWith("OSNG")) {
                                //存储发送时间
                                GroupInfo groupInfo = null;
                                try {
                                    groupInfo = getGroupInfo(msg.conversation.target, false);
                                    String time = String.valueOf(System.currentTimeMillis());
                                    groupInfo.timeInterval = time;
                                    List<GroupInfo> groupInfoList = new ArrayList<>();
                                    groupInfoList.add(groupInfo);
                                    SqliteUtils.updateGroup(groupInfo,Collections.singletonList("timeInterval"));
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }

                        }

                        @Override
                        public void onFailure(String error) {
                            sendResult(false, null, mid, conversationInfo, callback, timestamp);
                            //sendAlone(json, mid, conversationInfo, callback, timestamp);
                        }
                    });

                    if (msg2 != null) {
                        msg.messageHash0 = msg2.getString("hash");
                        msg.messageHash = msg.messageHash0;
                        SqliteUtils.updateHash(msg);
                    }

                } else {
                    MediaMessageContent mediaMessageContent = (MediaMessageContent) msg.content;
                    String remoteUrl = mediaMessageContent.remoteUrl;
                    int idx = mediaMessageContent.localPath.lastIndexOf('/');
                    String name = idx == 0 || idx == mediaMessageContent.localPath.length() - 1
                            ? mediaMessageContent.localPath
                            : mediaMessageContent.localPath.substring(idx + 1);
                    switch (msgType) {
                        case MessageContentType.ContentType_File:
                            FileMessageContent fileMessageContent = (FileMessageContent) mediaMessageContent;
                            json.put("type", "file");
                            json.put("name", fileMessageContent.getName());
                            json.put("size", fileMessageContent.getSize());
                            json.put("url",remoteUrl);
                            json.put("decKey", msg.decKey);
                            break;
                        case MessageContentType.ContentType_Image:
                            ImageMessageContent imageMessageContent = (ImageMessageContent) mediaMessageContent;
                            json.put("type", "image");
                            json.put("name", name);
                            json.put("width", imageMessageContent.getImageWidth());
                            json.put("height", imageMessageContent.getImageHeight());
                            json.put("url",remoteUrl);
                            json.put("decKey", msg.decKey);
                            break;
                        case MessageContentType.ContentType_Video:
                            VideoMessageContent videoMessageContent = (VideoMessageContent) mediaMessageContent;
                            //小视频自动下载，不需要缩略图
                            //byte[] thumbnail = videoMessageContent.getThumbnailBytes();
                            json.put("type", "video");
                            json.put("name", name);
                            json.put("url",videoMessageContent.remoteUrl);
                            json.put("decKey", msg.decKey);
                            //if(thumbnail != null)
                            //    json.put("thumbnail", Base64.encodeToString(thumbnail,Base64.NO_WRAP));
                            break;
                        case MessageContentType.ContentType_Voice:
                            SoundMessageContent soundMessageContent = (SoundMessageContent) mediaMessageContent;
                            json.put("type", "voice");
                            json.put("name", name);
                            json.put("duration", soundMessageContent.getDuration());
                            json.put("url",remoteUrl);
                            json.put("decKey", msg.decKey);
                            break;
                        case MessageContentType.ContentType_Sticker:
                            StickerMessageContent stickerMessageContent = (StickerMessageContent) mediaMessageContent;
                            json.put("type", "sticker");
                            json.put("name", name);
                            json.put("width", stickerMessageContent.width);
                            json.put("height", stickerMessageContent.height);
                            json.put("url",stickerMessageContent.remoteUrl);
                            json.put("decKey", msg.decKey);
                            break;
                        default:
                            logInfo("unknown type: " + msg.content.getMessageContentType());
                            return;
                    }
                    /*FileInputStream fileInputStream = new FileInputStream(new File(mediaMessageContent.localPath));
                    byte[] fileData = new byte[fileInputStream.available()];
                    fileInputStream.read(fileData);
                    fileInputStream.close();
                    Log.d("test",json.toString());*/
                    String finalRemoteUrl = remoteUrl;

                    osnsdk.sendMessage(json.toString(), msg.conversation.target, new OSNGeneralCallback() {
                        @Override
                        public void onSuccess(JSONObject data) {
                            Log.d("test",data.toString());
                            sendResult(true, data, mid, conversationInfo, callback, timestamp);
                            System.out.println("@@@   3");

                            GroupInfo groupInfo = null;
                            try {
                                groupInfo = getGroupInfo(msg.conversation.target, false);
                                String time = String.valueOf(System.currentTimeMillis());
                                groupInfo.timeInterval = time;
                                List<GroupInfo> groupInfoList = new ArrayList<>();
                                groupInfoList.add(groupInfo);
                                SqliteUtils.updateGroup(groupInfo,Collections.singletonList("timeInterval"));
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(String error) {
                            //sendAlone(json, mid, conversationInfo, callback, timestamp);
                            sendResult(false, null, mid, conversationInfo, callback, timestamp);
                        }
                    });
                }
            } catch (Exception e) {
                logError(e);
                if (callback != null)
                    callback.onFailure(-1);
            }
        }

        @Override
        public void dynamic(String data, IGeneralCallback2 callback){
            osnsdk.sendDynamic(data, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        //createDynamic(data, json);
                        callback.onSuccess(data.toString());
                    }catch (Exception e){
                    }
                }
                @Override
                public void onFailure(String error) {
                    try{
                        callback.onFailure(-1);
                    }catch (Exception e){
                    }
                }
            });
        }
        @Override
        public void orderPay(String data, String code, IGeneralCallback2 callback){
            JSONObject json = new JSONObject();
            json.put("data", data);
            json.put("hash", OsnUtils.sha256s((data+code).getBytes()));
            osnsdk.orderPay(json.toString(), new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        callback.onSuccess(data.toString());
                    }catch (Exception e){
                        logError(e);
                    }
                }
                @Override
                public void onFailure(String error) {
                    try{
                        callback.onFailure(-1);
                    }catch (Exception e){
                        logError(e);
                    }
                }
            });
        }
        public RedPacketInfo getRedPacket(String packetID){
            return SqliteUtils.queryRedPacket(packetID);
        }
        public void getOwnerSign(String groupID, IGeneralCallback2 callback){
            osnsdk.getOwnerSign(groupID, new OSNGeneralCallback(){

                @Override
                public void onSuccess(JSONObject data) {
                    try{callback.onSuccess(data.toString());}catch (Exception e){logError(e);}
                }

                @Override
                public void onFailure(String error) {
                    try{callback.onFailure(-1);}catch (Exception e){logError(e);}
                }
            });
        }
        public void getGroupSign(String groupID, String data, IGeneralCallback2 callback){
            osnsdk.getGroupSign(groupID, data, new OSNGeneralCallback(){

                @Override
                public void onSuccess(JSONObject data) {
                    try{callback.onSuccess(data.toString());}catch (Exception e){logError(e);}
                }

                @Override
                public void onFailure(String error) {
                    try{callback.onFailure(-1);}catch (Exception e){logError(e);}
                }
            });
        }
        public void addGroupManager(String groupID, List<String> memberIds, IGeneralCallback2 callback){
            osnsdk.addGroupManager(groupID, memberIds, new OSNGeneralCallback(){
                @Override
                public void onSuccess(JSONObject data) {
                    try{callback.onSuccess(data.toString());}catch (Exception e){logError(e);}
                }

                @Override
                public void onFailure(String error) {
                    try{callback.onFailure(-1);}catch (Exception e){logError(e);}
                }
            });
        }
        public void delGroupManager(String groupID, List<String> memberIds, IGeneralCallback2 callback){
            osnsdk.delGroupManager(groupID, memberIds, new OSNGeneralCallback(){
                @Override
                public void onSuccess(JSONObject data) {
                    try{callback.onSuccess(data.toString());}catch (Exception e){logError(e);}
                }

                @Override
                public void onFailure(String error) {
                    try{callback.onFailure(-1);}catch (Exception e){logError(e);}
                }
            });
        }

        public void setGroupOwner(String groupID, String owner, IGeneralCallback2 callback){
            osnsdk.setGroupOwner(groupID, owner, new OSNGeneralCallback(){
                @Override
                public void onSuccess(JSONObject data) {
                    try{callback.onSuccess(data.toString());}catch (Exception e){logError(e);}
                }

                @Override
                public void onFailure(String error) {
                    try{callback.onFailure(-1);}catch (Exception e){logError(e);}
                }
            });
        }
        public void openRedPacket(String packetID, String unpackID, long messageID, IGeneralCallback2 callback){
            try {
                Message message = getMessage(messageID);
                if (message != null)
                    SqliteUtils.updateMessage(messageID, MessageStatus.Opened.value());
                SqliteUtils.updateRedPacketState(packetID);
            } catch (RemoteException e) {
                logError(e);
            }
        }
        public void fetchRedPacket(String packetID, String unpackID, String userID, IGeneralCallback2 callback){
            JSONObject json = new JSONObject();
            json.put("packetID", packetID);
            json.put("unpackID", unpackID);
            osnsdk.getRedPacket(json.toString(), userID, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        logInfo("fetch info: "+data);
                        SqliteUtils.updateRedPacketState(packetID);
                        UnpackInfo info = new UnpackInfo(data);
                        SqliteUtils.insertUnpack(info);
                        callback.onSuccess(data.toString());
                    }catch (Exception e){
                    }
                }
                @Override
                public void onFailure(String error) {
                    try{
                        callback.onFailure(-1);
                    }catch (Exception e){
                    }
                }
            });
        }
        public List<UnpackInfo> getUnpackList(String unpackID){
            logInfo("query unpackID: "+unpackID);
            return SqliteUtils.queryUnpacks(unpackID);
        }

        @Override
        public void sendSavedMessage(Message msg, int expireDuration, ISendMessageCallback callback) throws RemoteException {
            logInfo("");
            throw new RemoteException();
        }

        public void sendCallMessage(Message msg, IGeneralCallback callback) {
            String voiceHostUrl = (String) SPUtils.get(mContext,"voiceHostUrl","");
            String voiceBaseUrl = (String) SPUtils.get(mContext,"voiceBaseUrl","");
            CallMessageContent callMessageContent = (CallMessageContent) msg.content;
            JSONObject json = new JSONObject();
            json.put("id", callMessageContent.id);
            json.put("type", "call");
            json.put("callMode", callMessageContent.mode);
            json.put("callType", callMessageContent.type);
            json.put("callAction", callMessageContent.action);
            json.put("url", callMessageContent.url);
            json.put("user", callMessageContent.user);
            json.put("urls", callMessageContent.urls);
            json.put("users", callMessageContent.users);
            json.put("voiceHostUrl",voiceHostUrl);
            json.put("voiceBaseUrl",voiceBaseUrl);
            osnsdk.sendMessage(json.toString(), msg.conversation.target, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        callback.onSuccess();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        callback.onFailure(-1);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void send(Message msg, final ISendMessageCallback callback, int expireDuration) throws RemoteException {
            //logInfo("");
            if (msg.content instanceof TypingMessageContent) {
                callback.onPrepared(0, msg.serverTime);
                callback.onSuccess(0, msg.serverTime);
                return;
            }
            if (msg.conversation.target.startsWith("OSNS")) {
                callback.onFailure(-1);
                return;
            }
            if (SqliteUtils.queryConversation(msg.conversation.type.getValue(), msg.conversation.target, msg.conversation.line) == null)
                SqliteUtils.insertConversation(msg.conversation.type.getValue(), msg.conversation.target, msg.conversation.line);
            if(msg.content.messageType != null){
                if(msg.content.messageType.equals(GENERAL)){
                    TextMessageContent textMessageContent = (TextMessageContent)msg.content;
                    if(textMessageContent.getQuoteInfo() != null){
                        JSONObject jsonObject = new JSONObject();
                        JSONObject jsonObject1 = new JSONObject();
                        jsonObject.put("data",((TextMessageContent) msg.content).content);
                        jsonObject.put("mentionedType",msg.content.mentionedType);
                        jsonObject.put("type", "type");
                        jsonObject1.put("d",textMessageContent.getQuoteInfo().getMessageDigest());
                        jsonObject1.put("i",textMessageContent.getQuoteInfo().getUserId());
                        jsonObject1.put("u",textMessageContent.getQuoteInfo().getMessageUid());
                        jsonObject1.put("n",textMessageContent.getQuoteInfo().getUserDisplayName());
                        jsonObject1.put("hash",textMessageContent.getQuoteInfo().getMessageHash());
                        jsonObject1.put("hash0",textMessageContent.getQuoteInfo().getMessageHash0());
                        jsonObject.put("quoteInfo",jsonObject1);
                        String contents = jsonObject.toJSONString();
                        msg.contents = contents;
                    }
                }
            }

            /*long mid = SqliteUtils.insertMessage(msg);
            msg.messageId = msg.messageUid = mid;
            callback.onPrepared(mid, msg.serverTime);
            sendmsg(msg, callback, expireDuration, mid, msg.serverTime);*/
            long mid = -1;
            if(!(msg.content instanceof CallMessageContent))
                mid = SqliteUtils.insertMessage(msg);
            msg.messageId = msg.messageUid = mid;
            callback.onPrepared(mid, msg.serverTime);
            sendmsg(msg, callback, expireDuration, mid, msg.serverTime);

            // 这里上传oss
        }

        @Override
        public void updateRedPaket(RedPacketInfo redPacketInfo) throws RemoteException {
            SqliteUtils.insertRedPacket(redPacketInfo);
        }

        public void broadcast(String json, final IGeneralCallback callback) throws RemoteException {
            logInfo(json);
            osnsdk.sendBroadcast(json, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        callback.onSuccess();
                    } catch (Exception e) {
                        logInfo(e.toString());
                    }
                }
                @Override
                public void onFailure(String error) {
                    try {
                        callback.onFailure(-1);
                    } catch (Exception e) {
                        logError(e);
                    }
                }
            });
        }

        public void recallNotify(){
            Message message = new Message();
            message.messageId = 0;
            message.direction = MessageDirection.Receive;
            message.status = MessageStatus.Readed;
            message.messageUid = 0;
            message.serverTime = System.currentTimeMillis();
            message.content = new RecallMessageContent("", 0);
            //message.messageId = message.messageUid = SqliteUtils.insertMessage(message);
            //onRecallMessage(message.messageUid);
        }
        @Override
        public void recall(long messageUid, final IGeneralCallback callback) throws RemoteException {
            logInfo("");
            Message message = SqliteUtils.queryMessage(messageUid);
            if(message == null){
                callback.onFailure(-1);
                return;
            }
            message.content = new RecallMessageContent(osnsdk.getUserID(), 0);
            SqliteUtils.recallMessage(message);
            callback.onSuccess();
        }

        @Override
        public void recall2(long messageUid, IGeneralCallback callback) throws RemoteException {
            logInfo("");
            // 从数据库中获取message
            Message message = SqliteUtils.queryMessage(messageUid);
            System.out.println("@@@   从数据库中获取messageHash: "+message.messageHash);
            if(message == null){
                callback.onFailure(-1);
                return;
            }
            // 发送Recall 消息
            osnsdk.recall(message.conversation.target, message.messageHash, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    // 成功，执行操作
                    message.content = new RecallMessageContent(osnsdk.getUserID(), 0);
                    SqliteUtils.recallMessage(message);
                    try {
                        callback.onSuccess();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(String error) {
                    // 失败，给个提示
                    try {
                        callback.onFailure(-1);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void sendNeedShare(List<String> userList, String groupName, String portrait, String groupId) {
            userList.remove(mUserId);
            JSONObject json = new JSONObject();
            json.put("type", "card");
            json.put("cardType", "group");
            json.put("target", groupId);
            json.put("name", groupName);
            json.put("displayName", groupName);
            json.put("portrait", portrait);
            json.put("theme", null);
            json.put("url", null);
            json.put("info", null);
            for(String osnID : userList){
                osnsdk.sendMessageNoCallback(json.toString(), osnID);
            }
        }

        @Override
        public void allowGroupAddFriend(String value, String groupId, IGeneralCallback2 callback) {
            osnsdk.allowGroupAddFriend(value, groupId, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        callback.onSuccess(data.toString());
                    } catch (Exception e) {
                        logError(e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        logInfo(error);
                        callback.onFailure(-1);
                    } catch (Exception e) {
                        logError(e);
                    }
                }
            });
        }
        @Override
        public void isGroupForward(String value, String groupId, IGeneralCallback2 callback) {
            osnsdk.isGroupForward(value, groupId, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        callback.onSuccess(data.toString());
                    } catch (Exception e) {
                        logError(e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        logInfo(error);
                        callback.onFailure(-1);
                    } catch (Exception e) {
                        logError(e);
                    }
                }
            });
        }

        @Override
        public void isGroupCopy(String value, String groupId, IGeneralCallback2 callback) {
            osnsdk.isGroupCopy(value, groupId, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        callback.onSuccess(data.toString());
                    } catch (Exception e) {
                        logError(e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        logInfo(error);
                        callback.onFailure(-1);
                    } catch (Exception e) {
                        logError(e);
                    }
                }
            });
        }

        @Override
        public void isClearChats(String value, String groupId, IGeneralCallback2 callback) {
            osnsdk.isClearChats(value, groupId, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        callback.onSuccess(data.toString());
                    } catch (Exception e) {
                        logError(e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        logInfo(error);
                        callback.onFailure(-1);
                    } catch (Exception e) {
                        logError(e);
                    }
                }
            });
        }


        @Override
        public void topMessage(String time ,String value, String groupId, IGeneralCallback2 callback) {
            osnsdk.topMessage(time,value, groupId, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        callback.onSuccess(data.toString());
                    } catch (Exception e) {
                        logError(e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        logInfo(error);
                        callback.onFailure(-1);
                    } catch (Exception e) {
                        logError(e);
                    }
                }
            });
        }

        @Override
        public void deleteTopMessage(List<String> time ,String groupId, IGeneralCallback2 callback) {
            osnsdk.deleteTopMessage(time, groupId, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        callback.onSuccess(data.toString());
                    } catch (Exception e) {
                        logError(e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        logInfo(error);
                        callback.onFailure(-1);
                    } catch (Exception e) {
                        logError(e);
                    }
                }
            });
        }

        @Override
        public void upAttribute(String key,String value, String groupId, IGeneralCallback2 callback) {
            osnsdk.upAttribute(key,value, groupId, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        callback.onSuccess(data.toString());
                    } catch (Exception e) {
                        logError(e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        logInfo(error);
                        callback.onFailure(-1);
                    } catch (Exception e) {
                        logError(e);
                    }
                }
            });
        }

        @Override
        public void upDescribes(String key, String value, String user, IGeneralCallback2 callback) {
            osnsdk.upDescribes(key,value, user, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        callback.onSuccess(data.toString());
                    } catch (Exception e) {
                        logError(e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        logInfo(error);
                        callback.onFailure(-1);
                    } catch (Exception e) {
                        logError(e);
                    }
                }
            });
        }

        @Override
        public void upDescribe(String key, String value, String group, IGeneralCallback2 callback) {
            osnsdk.upDescribe(key,value, group, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        System.out.println("[ClientService] UpDescribe success." + data);
                        callback.onSuccess(data.toString());
                    } catch (Exception e) {
                        logError(e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        System.out.println("[ClientService] UpDescribe failed." + error);
                        callback.onFailure(-1);
                    } catch (Exception e) {
                        logError(e);
                    }
                }
            });
        }

        @Override
        public void upPrivateInfo(String key, String value, String group, IGeneralCallback2 callback) {
            osnsdk.upPrivateInfo(key,value, group, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        System.out.println("[ClientService] upPrivateInfo success." + data);
                        callback.onSuccess(data.toString());
                    } catch (Exception e) {
                        logError(e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        System.out.println("[ClientService] UpDescribe failed." + error);
                        callback.onFailure(-1);
                    } catch (Exception e) {
                        logError(e);
                    }
                }
            });
        }


        @Override
        public void removeDescribes(String key, IGeneralCallback2 callback) {
            osnsdk.removeDescribes(key, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        callback.onSuccess(data.toString());
                    } catch (Exception e) {
                        logError(e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        logInfo(error);
                        callback.onFailure(-1);
                    } catch (Exception e) {
                        logError(e);
                    }
                }
            });
        }

        @Override
        public void deleteTimeMessage(String clearTimes,String target, IGeneralCallback callback) throws RemoteException {
            long newTime = System.currentTimeMillis();
            long oldTime = newTime - Long.valueOf(clearTimes);
            SqliteUtils.deleteTimeMessage(oldTime,target);
        }

        @Override
        public void deleteMessageTo(long messageUid, IGeneralCallback callback) throws RemoteException {
            logInfo("");
            // 从数据库中获取message
            Message message = SqliteUtils.queryMessage(messageUid);
            if(message == null){
                callback.onFailure(-1);
                return;
            }
            String msgHash = message.messageHash;
            if (message.conversation.target.startsWith("OSNG")) {
                msgHash = message.messageHash0;
            }

            // 发送Recall 消息
            osnsdk.deleteMessageTo(message.conversation.target, msgHash, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    // 成功，执行操作
                    osnsdk.deleteMessage(message.messageHash, message.conversation.target, null);
                    SqliteUtils.deleteMessage(message.messageId);
                    try {
                        callback.onSuccess();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(String error) {
                    // 失败，给个提示
                }
            });
        }
        @Override
        public long getServerDeltaTime() throws RemoteException {
            return 0;
        }

        @Override
        public List<ConversationInfo> getConversationList(int[] conversationTypes, int[] lines, int tagId) throws RemoteException {
            Set<String> added = new HashSet<>();
            List<ConversationInfo> infoList = new ArrayList<>();
            List<ConversationInfo> out = SqliteUtils.listAllConversations(conversationTypes, lines, tagId);
            for (ConversationInfo conversationInfo : out) {
                //System.out.println("@@@@ tag id : " + conversationInfo.tagId);
                //notify only one item
                if (conversationInfo.conversation.type == Conversation.ConversationType.Notify) {
                    conversationInfo.lastMessage = SqliteUtils.getLastNotify();
                    //break;
                }
                if(conversationInfo.isTop) {
                    infoList.add(conversationInfo);
                    added.add(conversationInfo.conversation.target);
                }
            }
            for (ConversationInfo conversationInfo : out) {
                if (conversationInfo.unreadCount.hasUnread() && !added.contains(conversationInfo.conversation.target)) {
                    infoList.add(conversationInfo);
                    added.add(conversationInfo.conversation.target);
                }
            }
            for (ConversationInfo conversationInfo : out) {
                if (!conversationInfo.unreadCount.hasUnread() && !added.contains(conversationInfo.conversation.target)) {
                    infoList.add(conversationInfo);
                    added.add(conversationInfo.conversation.target);
                }
            }
            logInfo("size: " + infoList.size());
            return infoList;
        }

        @Override
        public boolean updateTagUser(String osnId, int tagId){

            if (SqliteUtils.queryTagUser(osnId) == null) {
                return SqliteUtils.insertTagUser(osnId, tagId);
            } else {
                return SqliteUtils.updateTagUser(osnId, tagId);
            }

            /*if (!SqliteUtils.updateTagUser(osnId, tagId)) {
                System.out.println("@@@    cesh1");
                return SqliteUtils.insertTagUser(osnId, tagId);
            }
            System.out.println("@@@    cesh2");
            return true;*/
        }


        @Override
        public boolean insertTagUser(String osnId, int tagId){
            return SqliteUtils.insertTagUser(osnId,tagId);
        }

        @Override
        public boolean deleteTagUser(String osnID){
            return SqliteUtils.deleteTagUser(osnID);
        }

        @Override
        public boolean deleteTag(int tagId){
            return SqliteUtils.deleteTag(tagId);
        }

        @Override
        public ConversationInfo getConversation(int conversationType, String target, int line) throws RemoteException {
            //logInfo("");
            return SqliteUtils.queryConversation(conversationType, target, line);
        }

        @Override
        public long getFirstUnreadMessageId(int conversationType, String target, int line) throws RemoteException {
            logInfo("");
            return 0;
        }

        @Override
        public List<Message> getMessages(Conversation conversation, long fromIndex, boolean before, int count, String withUser) throws RemoteException {
            logInfo("fromIndex: " + fromIndex + ", before: " + before + ", count: " + count + ", withUser: " + withUser);
            if (fromIndex == 0) {
                long fromTime = System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 365;
                return SqliteUtils.queryMessages(conversation, fromTime, before, count, true);
            }
            Message msg = fromIndex == 0 ? SqliteUtils.getLastMessage(conversation) : SqliteUtils.queryMessage(fromIndex);
            if (msg == null)
                return new ArrayList<>();
            return SqliteUtils.queryMessages(conversation, msg.serverTime, before, count, fromIndex == 0);
        }

        @Override
        public List<Message> getMessagesEx(int[] conversationTypes, int[] lines, int[] contentTypes, long fromIndex, boolean before, int count, String withUser) throws RemoteException {
            logInfo("");
            //            ProtoMessage[] protoMessages = ProtoLogic.getMessagesEx(conversationTypes, lines, contentTypes, fromIndex, before, count, withUser);
//            SafeIPCMessageEntry entry = buildSafeIPCMessages(protoMessages, 0, before);
//            if (entry.messages.size() != protoMessages.length) {
//                android.util.Log.e(TAG, "getMessagesEx, drop messages " + (protoMessages.length - entry.messages.size()));
//            }
//            return entry.messages;
            return null;
        }

        @Override
        public List<Message> getMessagesEx2(int[] conversationTypes, int[] lines, int[] messageStatus, long fromIndex, boolean before, int count, String withUser) throws RemoteException {
            logInfo("");
            //            ProtoMessage[] protoMessages = ProtoLogic.getMessagesEx2(conversationTypes, lines, messageStatus, fromIndex, before, count, withUser);
//            SafeIPCMessageEntry entry = buildSafeIPCMessages(protoMessages, 0, before);
//            if (entry.messages.size() != protoMessages.length) {
//                android.util.Log.e(TAG, "getMessagesEx2, drop messages " + (protoMessages.length - entry.messages.size()));
//            }
//            return entry.messages;
            return null;
        }

        @Override
        public void getMessagesInTypesAsync(Conversation conversation, int[] contentTypes, long fromIndex, boolean before, int count, String withUser, IGetMessageCallback callback) throws RemoteException {
            logInfo("");
        }

        @Override
        public void getMessagesInStatusAsync(Conversation conversation, int[] messageStatus, long fromIndex, boolean before, int count, String withUser, IGetMessageCallback callback) throws RemoteException {
            logInfo("");
        }

        @Override
        public void getMessagesAsync(Conversation conversation, long fromIndex, boolean before, int count, String withUser, IGetMessageCallback callback) throws RemoteException {
            logInfo("");
            List<Message> messageList = getMessages(conversation, fromIndex, before, count, withUser);
            for (Message msg : messageList) {
                if (msg.status == Sending) {
                    if (msg.serverTime+1000 *15 < System.currentTimeMillis()) {
                        msg.status = Send_Failure;
                        SqliteUtils.updateMessage(msg.messageId, Send_Failure.value());
                    }
                }
            }
            callback.onSuccess(messageList, messageList.size() == count);
        }

        @Override
        public void getMessagesExAsync(int[] conversationTypes, int[] lines, int[] contentTypes, long fromIndex, boolean before, int count, String withUser, IGetMessageCallback callback) throws RemoteException {
            logInfo("");
        }

        @Override
        public void getMessagesEx2Async(int[] conversationTypes, int[] lines, int[] messageStatus, long fromIndex, boolean before, int count, String withUser, IGetMessageCallback callback) throws RemoteException {
            logInfo("");
        }

        @Override
        public void getUserMessages(String userId, Conversation conversation, long fromIndex, boolean before, int count, IGetMessageCallback callback) throws RemoteException {
            logInfo("");
        }

        @Override
        public void getUserMessagesEx(String userId, int[] conversationTypes, int[] lines, int[] contentTypes, long fromIndex, boolean before, int count, IGetMessageCallback callback) throws RemoteException {
            logInfo("");
        }

        @Override
        public void getRemoteMessages(Conversation conversation, long beforeMessageUid, int count, IGetRemoteMessageCallback callback) throws RemoteException {
            logInfo("beforeMID: " + beforeMessageUid + ", count: " + count);
            callback.onSuccess(new ArrayList<>());
//            Message message = SqliteUtils.queryMessage(beforeMessageUid);
//            long timestamp = message == null ? System.currentTimeMillis() : message.serverTime;
//            osnsdk.loadMessage(conversation.target, timestamp, count, true, new OSNGeneralCallbackT<List<OsnMessageInfo>>() {
//                @Override
//                public void onFailure(String error) {
//                    try {
//                        callback.onFailure(-1);
//                    } catch (RemoteException e) {
//                        logError(e);
//                    }
//                }
//
//                @Override
//                public void onSuccess(List<OsnMessageInfo> osnMessageInfos) {
//                    List<Message> messages = recvMessage(osnMessageInfos, true);
//                    try {
//                        callback.onSuccess(messages);
//                    } catch (RemoteException e) {
//                        logError(e);
//                    }
//                }
//            });
        }

        @Override
        public void getConversationFileRecords(Conversation conversation, String fromUser, long beforeMessageUid, int count, IGetFileRecordCallback callback) throws RemoteException {
            logInfo("");
        }

        @Override
        public void getMyFileRecords(long beforeMessageUid, int count, IGetFileRecordCallback callback) throws RemoteException {
            logInfo("");
        }

        @Override
        public void deleteFileRecord(long messageUid, IGeneralCallback callback) throws RemoteException {
            logInfo("");
        }

        @Override
        public void searchFileRecords(String keyword, Conversation conversation, String fromUser, long beforeMessageUid, int count, IGetFileRecordCallback callback) throws RemoteException {
            logInfo("");
        }

        @Override
        public void searchMyFileRecords(String keyword, long beforeMessageUid, int count, IGetFileRecordCallback callback) throws RemoteException {
            logInfo("");
        }


        @Override
        public Message getMessage(long messageId) throws RemoteException {
            logInfo("messageId: " + messageId);
            return SqliteUtils.queryMessage(messageId);
        }

        @Override
        public Message getMessageByUid(String msgHash0) throws RemoteException {
            logInfo("");
            return SqliteUtils.queryMessageHash0(msgHash0);
        }

        @Override
        public Message getMessageByHash(String msgHash) throws RemoteException {
            return SqliteUtils.queryMessage(msgHash);
        }

        @Override
        public Message insertMessage(Message message, boolean notify) throws RemoteException {
            logInfo("");
            return null;
        }

        @Override
        public boolean updateMessageContent(Message message) throws RemoteException {
            logInfo("");
            return false;
        }

        @Override
        public boolean updateMessageStatus(long messageId, int messageStatus) throws RemoteException {
            logInfo("");
            return true;
        }

        @Override
        public UnreadCount getUnreadCount(int conversationType, String target, int line) throws RemoteException {
            logInfo("");
            ConversationInfo conversationInfo = SqliteUtils.queryConversation(conversationType, target, line);
            if (conversationInfo != null)
                return conversationInfo.unreadCount;
            return new UnreadCount();
        }

        @Override
        public UnreadCount getUnreadCountEx(int[] conversationTypes, int[] lines) throws RemoteException {
            logInfo("");
            return null;
        }

        @Override
        public boolean clearUnreadStatus(int conversationType, String target, int line) throws RemoteException {
            logInfo("");
            SqliteUtils.clearConversationUnread(conversationType, target, line);
            return true;
        }

        @Override
        public boolean clearUnreadStatusEx(int[] conversationTypes, int[] lines) throws RemoteException {
            logInfo("");
            return false;
        }

        @Override
        public void clearAllUnreadStatus() throws RemoteException {
            logInfo("");
        }

        @Override
        public void clearMessages(int conversationType, String target, int line) throws RemoteException {
            logInfo("");
            SqliteUtils.clearMessage(target);
        }

        @Override
        public void clearMessagesEx(int conversationType, String target, int line, long before) throws RemoteException {
            logInfo("");
        }

        @Override
        public void setMediaMessagePlayed(long messageId) {
            logInfo("mid: " + messageId);
            try {
                Message message = getMessage(messageId);
                if (message != null)
                    SqliteUtils.updateMessage(messageId, MessageStatus.Played.value());
            } catch (RemoteException e) {
                logError(e);
            }
        }
        @Override
        /*public void callMessage(Message message) {
            logInfo("callMessage");*/
        public void saveCallMessage(Message message) {
            logInfo("saveCallMessage");
            try {
                SqliteUtils.insertMessage(message);
                onReceiveMessage(Collections.singletonList(message), false);
            } catch (Exception e) {
                logError(e);
            }
        }

        @Override
        public void clearConversation(boolean clearMsg) throws RemoteException {
            logInfo("clearMsg: "+clearMsg);
            SqliteUtils.clearConversation();
        }
        @Override
        public void removeConversation(int conversationType, String target, int line, boolean clearMsg) throws RemoteException {
            logInfo("type: " + conversationType + ", target: " + target + ", line: " + line);
            SqliteUtils.deleteConversation(conversationType, target, line);
        }

        @Override
        public void setConversationTop(int conversationType, String target, int line, boolean top, IGeneralCallback callback) throws RemoteException {
            logInfo("target: "+target+", top: "+top+"callback: "+callback);
            ConversationInfo conversationInfo = SqliteUtils.queryConversation(conversationType, target, line);
            if (conversationInfo != null) {
                conversationInfo.isTop = top;
                SqliteUtils.updateConversation(conversationInfo, Collections.singletonList("top"));
                callback.onSuccess();
            }else{
                callback.onFailure(-1);
            }
        }

        @Override
        public void setConversationDraft(int conversationType, String target, int line, String draft) throws RemoteException {
            logInfo("");
            ConversationInfo conversationInfo = SqliteUtils.queryConversation(conversationType, target, line);
            if (conversationInfo == null) {
                SqliteUtils.insertConversation(conversationType, target, line);
                conversationInfo = SqliteUtils.queryConversation(conversationType, target, line);
            }
            if (conversationInfo != null) {
                conversationInfo.draft = draft;
                SqliteUtils.updateConversation(conversationInfo, Collections.singletonList("draft"));
            }
        }

        @Override
        public void setConversationSilent(int conversationType, String target, int line, boolean silent, IGeneralCallback callback) throws RemoteException {
            logInfo("");
            ConversationInfo conversationInfo = SqliteUtils.queryConversation(conversationType, target, line);
            if (conversationInfo != null) {
                conversationInfo.isSilent = silent;
                SqliteUtils.updateConversation(conversationInfo, Collections.singletonList("silent"));
            }
        }

        @Override
        public Map getConversationRead(int conversationType, String target, int line) throws RemoteException {
            logInfo("");
//            ConversationInfo conversationInfo = SqliteUtils.queryConversation(conversationType,target,line);
//            if(conversationInfo == null && target.startsWith("OSNU"))
//                SqliteUtils.insertConversation(conversationType,target,line);
            return new HashMap();
        }

        @Override
        public Map getMessageDelivery(int conversationType, String target) throws RemoteException {
            logInfo("");
            return null;
        }

        @Override
        public void setConversationTimestamp(int conversationType, String target, int line, long timestamp) throws RemoteException {
            logInfo("");
            ConversationInfo conversationInfo = SqliteUtils.queryConversation(conversationType, target, line);
            if (conversationInfo != null) {
                conversationInfo.timestamp = timestamp;
                SqliteUtils.updateConversation(conversationInfo, Collections.singletonList("timestamp"));
            }
        }

        @Override
        public void searchUser(String keyword, int searchType, int page, final ISearchUserCallback callback) throws RemoteException {
            logInfo("");
            List<UserInfo> userInfos = SqliteUtils.queryUsers(keyword);
            callback.onSuccess(userInfos);
        }

        @Override
        public boolean isMyFriend(String userId) throws RemoteException {
            logInfo("");
            return SqliteUtils.queryFriend(userId) != null;
        }

        @Override
        public List<String> getMyFriendList(boolean refresh) throws RemoteException {
            logInfo("refresh: " + refresh);
            return SqliteUtils.listFriends();
        }

        @Override
        public boolean isBlackListed(String userId) throws RemoteException {
            OsnFriendInfo friendInfo = SqliteUtils.queryFriend(userId);
            if (friendInfo == null) {
                logInfo("no my friend: " + userId);
                return false;
            }
            logInfo(friendInfo.state == OsnFriendInfo.Blacked ? "true" : "false" + friendInfo.state);
            return friendInfo.state == OsnFriendInfo.Blacked;
        }

        @Override
        public List<String> getBlackList(boolean refresh) throws RemoteException {
            logInfo("");
            return SqliteUtils.listFriends(OsnFriendInfo.Blacked);
        }

        @Override
        public List<UserInfo> getMyFriendListInfo(boolean refresh) throws RemoteException {
            logInfo("");
            List<String> users = getMyFriendList(refresh);
            if (users == null)
                return null;
            List<UserInfo> userInfos = new ArrayList<>();
            UserInfo userInfo;
            for (String user : users) {
                userInfo = getUserInfo(user, null, false);
                if (userInfo == null)
                    userInfo = new UserInfo(user);
                userInfos.add(userInfo);
            }
            return userInfos;
        }

        public boolean syncFriend(){

            try {
                return osnsdk.syncFriend();
            }catch (Exception e){

            }
            return false;

        }

        public boolean syncGroup(){

            try {
                return osnsdk.syncGroup();
            }catch (Exception e){

            }
            return false;

        }

        @Override
        public void loadFriendRequestFromRemote() throws RemoteException {
            logInfo("");
        }

        @Override
        public String getUserSetting(int scope, String key) throws RemoteException {
            try {
                String value = null;
                if (scope == UserSettingScope.FavoriteGroup) {
                    GroupInfo groupInfo = SqliteUtils.queryGroup(key);
                    value = String.valueOf(groupInfo.fav);
                } else if (scope == UserSettingScope.GroupHideNickname) {
                    GroupInfo groupInfo = SqliteUtils.queryGroup(key);
                    value = String.valueOf(groupInfo.showAlias);
                }
                logInfo("scope: " + scope + ", key: " + key + ", value: " + value);
                return value;
            } catch (Exception e) {
                logError(e);
            }
            return null;
        }

        @Override
        public Map<String, String> getUserSettings(int scope) throws RemoteException {
            try {
                logInfo("scope: " + scope);
                Map<String, String> uMap = new HashMap<>();
                if (scope == UserSettingScope.FavoriteGroup) {
                    List<GroupInfo> groupInfoList = SqliteUtils.listGroups();
                    for (GroupInfo g : groupInfoList)
                        uMap.put(g.target, String.valueOf(g.fav));
                } else if (scope == UserSettingScope.GroupHideNickname) {
                    List<GroupInfo> groupInfoList = SqliteUtils.listGroups();
                    for (GroupInfo g : groupInfoList)
                        uMap.put(g.target, String.valueOf(g.showAlias));
                }
                for (String k : uMap.keySet()) {
                    logInfo("key: " + k + ", value: " + uMap.get(k));
                }
                return uMap;
            } catch (Exception e) {
                logError(e);
            }
            return null;
        }
        void showStack(){
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            for(StackTraceElement stackTraceElement : stackTraceElements){
                Log.d("showStack", stackTraceElement.toString());
            }
        }

        @Override
        public void setUserSetting(int scope, String key, String value, final IGeneralCallback callback) throws RemoteException {
            try {
                logInfo("scope: " + scope + ", key: " + key + ", value: " + value);
                if (scope == UserSettingScope.FavoriteGroup) {
                    GroupInfo groupInfo = SqliteUtils.queryGroup(key);
                    groupInfo.fav = Integer.parseInt(value);
                    SqliteUtils.updateGroup(groupInfo, Collections.singletonList("fav"));
                    osnsdk.saveGroup(groupInfo.target, Integer.parseInt(value), new OSNGeneralCallback() {
                        @Override
                        public void onSuccess(JSONObject data) {
                            try {
                                if (callback != null)
                                    callback.onSuccess();
                            } catch (Exception e) {
                                logError(e);
                            }
                        }

                        @Override
                        public void onFailure(String error) {
                            try {
                                if (callback != null)
                                    callback.onFailure(-1);
                            } catch (Exception e) {
                                logError(e);
                            }
                        }
                    });
//                    OsnMemberInfo info = new OsnMemberInfo();
//                    info.status = Integer.parseInt(value);
//                    info.groupID = groupInfo.target;
//                    osnsdk.modifyMemberInfo(Collections.singletonList("status"), info, new OSNGeneralCallback() {
//                        @Override
//                        public void onSuccess(JSONObject data) {
//                            try {
//                                if (callback != null)
//                                    callback.onSuccess();
//                            } catch (Exception e) {
//                                logError(e);
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(String error) {
//                            try {
//                                if (callback != null)
//                                    callback.onFailure(-1);
//                            } catch (Exception e) {
//                                logError(e);
//                            }
//                        }
//                    });
                } else if (scope == UserSettingScope.GroupHideNickname) {
                    GroupInfo groupInfo = SqliteUtils.queryGroup(key);
                    groupInfo.showAlias = Integer.parseInt(value);
                    SqliteUtils.updateGroup(groupInfo, Collections.singletonList("showAlias"));
                    if (callback != null)
                        callback.onSuccess();
                }
            } catch (Exception e) {
                logError(e);
            }
        }

        private String getLogPath() {
            logInfo("");
            return getCacheDir().getAbsolutePath() + "/log";
        }

        @Override
        public void startLog() throws RemoteException {
            logInfo("");
        }

        @Override
        public void stopLog() throws RemoteException {
            logInfo("");
        }

        @Override
        public void setDeviceToken(String token, int pushType) throws RemoteException {
            logInfo("");
            if (TextUtils.isEmpty(token)) {
                return;
            }
            mBackupDeviceToken = token;
        }

        @Override
        public List<FriendRequest> getFriendRequest(boolean incomming) throws RemoteException {
            List<FriendRequest> friendRequests = SqliteUtils.listFriendRequest();
            logInfo("size: "+friendRequests.size());
            logInfo(JSONObject.toJSONString(friendRequests));
            return friendRequests;
        }

        @Override
        public FriendRequest getOneFriendRequest(String userId, boolean incomming) throws RemoteException {
            logInfo("");
            return null;
        }

        @Override
        public String getFriendAlias(String userId) throws RemoteException {
            logInfo("");
            OsnFriendInfo friendInfo = SqliteUtils.queryFriend(userId);
            return friendInfo == null ? null : friendInfo.remarks;
        }

        @Override
        public String getFriendExtra(String userId) throws RemoteException {
            logInfo("");
            return null;
        }

        @Override
        public void setFriendAlias(String userId, String alias, IGeneralCallback callback) throws RemoteException {
            logInfo("");
            OsnFriendInfo friendInfo = new OsnFriendInfo();
            friendInfo.friendID = userId;
            friendInfo.remarks = alias;
            osnsdk.modifyFriendInfo(Collections.singletonList("remarks"), friendInfo, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    if (callback != null) {
                        try {
                            friendInfo.remarks = alias;
                            SqliteUtils.updateFriend(friendInfo, Collections.singletonList("remarks"));
                            callback.onSuccess();
                            UserInfo userInfo = getUserInfo(userId, null, false);
                            onUserInfoUpdated(Collections.singletonList(userInfo));
                        } catch (RemoteException e) {
                            logError(e);
                        }
                    }
                }

                @Override
                public void onFailure(String error) {
                    if (callback != null) {
                        try {
                            callback.onFailure(-1);
                        } catch (RemoteException e) {
                            logError(e);
                        }
                    }
                }
            });
        }

        @Override
        public void clearUnreadFriendRequestStatus() throws RemoteException {
            List<FriendRequest> requestList = SqliteUtils.queryUnreadFriendRequest();
            logInfo("size: "+requestList.size());
            for (FriendRequest request : requestList)
                request.readStatus = 1;
            SqliteUtils.updateFriendRequests(requestList);
        }

        @Override
        public int getUnreadFriendRequestStatus() throws RemoteException {
            List<FriendRequest> requestList = SqliteUtils.queryUnreadFriendRequest();
            logInfo("size: "+requestList.size());
            return requestList.size();
        }

        @Override
        public void removeFriend(String userId, final IGeneralCallback callback) throws RemoteException {
            logInfo("");
            osnsdk.deleteFriend(userId, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        callback.onSuccess();
                    } catch (Exception e) {
                        logError(e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        callback.onFailure(-1);
                    } catch (Exception e) {
                        logError(e);
                    }
                }
            });
        }

        @Override
        public void sendFriendRequest(String userId, String reason, final IGeneralCallback callback) throws RemoteException {
            logInfo("@@@ AddFriend begin : " + System.currentTimeMillis());
            osnsdk.inviteFriend(userId, reason, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        logInfo("@@@ AddFriend onSuccess : " + System.currentTimeMillis());
//                        FriendRequest request = new FriendRequest();
//                        request.target = userId;
//                        request.reason = reason;
//                        request.direction = FriendRequest.Direction_Sent;
//                        SqliteUtils.insertFriendRequest(request);
                        callback.onSuccess();
                    } catch (RemoteException e) {
                        logError(e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        logInfo("@@@ AddFriend onFailure : " + System.currentTimeMillis());
                        callback.onFailure(-1);
                    } catch (RemoteException e) {
                        logError(e);
                    }
                }
            });
        }

        @Override
        public void handleGroupRequest(String userId, String groupId, boolean accept, final IGeneralCallback callback) throws RemoteException {
            logInfo("userId: " + userId + ", groupId: " + groupId + ", accept: " + accept);
            FriendRequest request = SqliteUtils.queryFriendRequest(userId, groupId);
            request.status = accept ? FriendRequest.RequestStatus_Accepted : FriendRequest.RequestStatus_Rejected;
            SqliteUtils.updateFriendRequests(Collections.singletonList(request));
            logInfo("request: "+JSONObject.toJSONString(request));
            if (accept) {
                if (request.type == RequestType_ApplyMember) {
                    osnsdk.acceptMember(userId, groupId, new OSNGeneralCallback() {
                        @Override
                        public void onSuccess(JSONObject data) {
                            try {
                                callback.onSuccess();
                            } catch (RemoteException e) {
                                logError(e);
                            }
                        }

                        @Override
                        public void onFailure(String error) {
                            try {
                                callback.onFailure(-1);
                            } catch (RemoteException e) {
                                logError(e);
                            }
                        }
                    });
                } else {
                    osnsdk.joinGroup(groupId, null, request.invitation, new OSNGeneralCallback() {
                        @Override
                        public void onSuccess(JSONObject data) {
                            try {
                                callback.onSuccess();
                            } catch (RemoteException e) {
                                logError(e);
                            }
                        }

                        @Override
                        public void onFailure(String error) {
                            try {
                                callback.onFailure(-1);
                            } catch (RemoteException e) {
                                logError(e);
                            }
                        }
                    });
                }
            } else {
                if (request.type == RequestType_ApplyMember) {
//                    osnsdk.rejectMember(userId, groupId, new OSNGeneralCallback() {
//                        @Override
//                        public void onSuccess(JSONObject data) {
//                            try {
//                                callback.onSuccess();
//                            } catch (RemoteException e) {
//                                logError(e);
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(String error) {
//                            try {
//                                callback.onFailure(-1);
//                            } catch (RemoteException e) {
//                                logError(e);
//                            }
//                        }
//                    });
                    try {
                        callback.onSuccess();
                    } catch (RemoteException e) {
                        logError(e);
                    }
                } else {
                    osnsdk.rejectGroup(groupId, new OSNGeneralCallback() {
                        @Override
                        public void onSuccess(JSONObject data) {
                            try {
                                callback.onSuccess();
                            } catch (RemoteException e) {
                                logError(e);
                            }
                        }

                        @Override
                        public void onFailure(String error) {
                            try {
                                callback.onFailure(-1);
                            } catch (RemoteException e) {
                                logError(e);
                            }
                        }
                    });
                }
            }
        }

        @Override
        public void handleFriendRequest(String userId, boolean accept, String extra, final IGeneralCallback callback) throws RemoteException {
            logInfo("userId: " + userId + ", accept: " + accept);
            FriendRequest request = SqliteUtils.queryFriendRequest(userId);
            request.status = accept ? FriendRequest.RequestStatus_Accepted : FriendRequest.RequestStatus_Rejected;
            SqliteUtils.updateFriendRequests(Collections.singletonList(request));
            if (accept) {
                osnsdk.acceptFriend(userId, new OSNGeneralCallback() {
                    @Override
                    public void onSuccess(JSONObject data) {
                        try {
                            callback.onSuccess();
                        } catch (RemoteException e) {
                            logError(e);
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        try {
                            callback.onFailure(-1);
                        } catch (RemoteException e) {
                            logError(e);
                        }
                    }
                });
            } else {
                osnsdk.rejectFriend(userId, new OSNGeneralCallback() {
                    @Override
                    public void onSuccess(JSONObject data) {
                        try {
                            callback.onSuccess();
                        } catch (RemoteException e) {
                            logError(e);
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        try {
                            callback.onFailure(-1);
                        } catch (RemoteException e) {
                            logError(e);
                        }
                    }
                });
            }
        }

        @Override
        public void setBlackList(String userId, boolean isBlacked, final IGeneralCallback callback) throws RemoteException {
            logInfo("");
            OsnFriendInfo friendInfo = SqliteUtils.queryFriend(userId);
            if (friendInfo == null) {
                if (callback != null)
                    callback.onFailure(-1);
                return;
            }
            friendInfo.state = isBlacked ? OsnFriendInfo.Blacked : OsnFriendInfo.Normal;
            osnsdk.modifyFriendInfo(Collections.singletonList("state"), friendInfo, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        SqliteUtils.updateFriend(friendInfo, Collections.singletonList("state"));
                        if (callback != null)
                            callback.onSuccess();
                    } catch (Exception e) {
                        logError(e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        if (callback != null)
                            callback.onFailure(-1);
                    } catch (Exception e) {
                        logError(e);
                    }
                }
            });
        }

        @Override
        public void joinChatRoom(String chatRoomId, IGeneralCallback callback) throws RemoteException {
            logInfo("");
        }

        @Override
        public void quitChatRoom(String chatRoomId, IGeneralCallback callback) throws RemoteException {
            logInfo("");
        }

        @Override
        public void getChatRoomInfo(String chatRoomId, long updateDt, IGetChatRoomInfoCallback callback) throws RemoteException {
            logInfo("");
        }

        @Override
        public void getChatRoomMembersInfo(String chatRoomId, int maxCount, IGetChatRoomMembersInfoCallback callback) throws RemoteException {
            logInfo("");
        }

        @Override
        public void clearFriend(final IGeneralCallback callback) throws RemoteException {
            SqliteUtils.clearFriend();
        }

        @Override
        public boolean insertTag(String name, int tagId) {
            return SqliteUtils.insertTag(name, tagId);
        }

        @Override
        public List<OrgTag> listTag(){
            return SqliteUtils.listTag();
        }

        @Override
        public List<String> listTagUser(int tagId){
            return SqliteUtils.listTagUser(tagId);
        }

        @Override
        public String QueryTagUser(String osnId){
            return SqliteUtils.queryTagUser(osnId);
        }

        @Override
        public void deleteFriend(String userId, final IGeneralCallback callback) throws RemoteException {
            logInfo("");
            osnsdk.deleteFriend(userId, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        callback.onSuccess();
                    } catch (Exception e) {
                        logError(e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        callback.onFailure(-1);
                    } catch (Exception e) {
                        logError(e);
                    }
                }
            });
        }
        public void getGroupDetail(String groupId, String type, IGeneralCallback2 callback){
            logInfo("groupId: "+groupId+", type: "+type);
            osnsdk.getGroupDetail(groupId, type, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        GroupInfo groupInfo = SqliteUtils.queryGroup(groupId);
                        if(type.equalsIgnoreCase("notice"))
                            groupInfo.notice = data.getString("notice");
                        SqliteUtils.updateGroup(groupInfo, Collections.singletonList(type));
                        callback.onSuccess(groupInfo.notice);
                    } catch (RemoteException e) {
                        logError(e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        callback.onFailure(-1);
                    } catch (RemoteException e) {
                        logError(e);
                    }
                }
            });
        }



        @Override
        public GroupInfo getGroupInfo(String groupId, boolean refresh) throws RemoteException {
            //     logInfo("@@@   groupID: " + groupId + ", refresh: " + refresh);
            if (groupId == null) {
                return new NullGroupInfo("null");
            }
            if (groupId.equalsIgnoreCase("null"))
                return new NullGroupInfo("null");



            GroupInfo groupInfo = SqliteUtils.queryGroup(groupId);
            if (groupInfo == null || refresh) {
                osnsdk.getGroupInfo(groupId, new OSNGeneralCallbackT<OsnGroupInfo>() {
                    @Override
                    public void onFailure(String error) {

                    }

                    @Override
                    public void onSuccess(OsnGroupInfo osnGroupInfo) {
                        UpdateGroupInfo(osnGroupInfo,mUserId);

                        /*int isMember = osnGroupInfo.isMember();
                        ConversationInfo conv = SqliteUtils.queryConversation(1, osnGroupInfo.groupID, 0);
                        if (conv != null) {
                            if (conv.isMember != isMember) {
                                conv.isMember = isMember;
                                SqliteUtils.updateConversation(conv, Collections.singletonList("isMember"));
                            }
                        }*/

                        GroupInfo groupInfo = SqliteUtils.queryGroup(osnGroupInfo.groupID);
                        /*if(groupInfo != null){
                            updateGroup(osnGroupInfo, true, groupInfo.fav, groupInfo.showAlias);
                            groupInfo = SqliteUtils.queryGroup(osnGroupInfo.groupID);
                        }else{
                            groupInfo = toClientGroup(osnGroupInfo);
                            SqliteUtils.insertGroup(groupInfo);
                        }*/
                        onGroupInfoUpdated(Collections.singletonList(groupInfo));
                    }
                });
            }
            if (groupInfo == null)
                groupInfo = new NullGroupInfo(groupId);

            return groupInfo;
        }

        @Override
        public GroupInfo getGroupInfoFromDB(String groupId) throws RemoteException {

            if (groupId == null) {
                return new NullGroupInfo("null");
            }
            if (groupId.equalsIgnoreCase("null"))
                return new NullGroupInfo("null");

            GroupInfo groupInfo = SqliteUtils.queryGroup(groupId);
            if (groupInfo == null){
                return new NullGroupInfo(groupId);
            }
            return groupInfo;
        }

        @Override
        public void getGroupInfoEx(String groupId, boolean refresh, IGetGroupCallback callback) throws RemoteException {
            logInfo("groupID: " + groupId + ", refresh: " + refresh);
            osnsdk.getGroupInfo(groupId, new OSNGeneralCallbackT<OsnGroupInfo>() {
                @Override
                public void onFailure(String error) {
                    try {
                        callback.onFailure(-1);
                    } catch (Exception e) {
                        logError(e);
                    }
                }

                @Override
                public void onSuccess(OsnGroupInfo osnGroupInfo) {
                    try {

                        UpdateGroupInfo(osnGroupInfo, mUserId);

                        /*int isMember = osnGroupInfo.isMember();
                        ConversationInfo conv = SqliteUtils.queryConversation(1, osnGroupInfo.groupID, 0);
                        if (conv != null) {
                            if (conv.isMember != isMember) {
                                conv.isMember = isMember;
                                SqliteUtils.updateConversation(conv, Collections.singletonList("isMember"));
                            }
                        }
                        GroupInfo groupInfo = SqliteUtils.queryGroup(osnGroupInfo.groupID);
                        if (groupInfo == null) {
                            groupInfo = toClientGroup(osnGroupInfo);
                            SqliteUtils.insertGroup(groupInfo);
                        } else {
                            updateGroup(osnGroupInfo, false, groupInfo.fav, groupInfo.showAlias);
                        }*/
                        GroupInfo groupInfo = SqliteUtils.queryGroup(osnGroupInfo.groupID);

                        //System.out.println("@@@ getGroupInfoEx isMember " + osnGroupInfo.isMember);
                        groupInfo.isMember = osnGroupInfo.isMember;
                        //System.out.println("@@@ getGroupInfoEx isMember2 " + groupInfo.isMember);
                        callback.onSuccess(groupInfo);
                    } catch (Exception e) {
                        //System.out.println("@@@ ClientService getGroupInfoEx Exception");

                        logError(e);
                    }
                }
            });
        }

        @Override
        public UserInfo getUserInfo(String userId, String groupId, boolean refresh) throws RemoteException {
            logInfo("userID: " + userId + ", groupID: " + groupId + ", refresh: " + refresh);
            if (userId == null)
                userId = osnsdk.getUserID();
            if (userId.equalsIgnoreCase("null"))
                return new NullUserInfo(userId);

            UserInfo userInfo = SqliteUtils.queryUser(userId);
            if (userInfo == null || refresh) {
                osnsdk.getUserInfo(userId, new OSNGeneralCallbackT<OsnUserInfo>() {
                    @Override
                    public void onFailure(String error) {
                    }

                    @Override
                    public void onSuccess(OsnUserInfo osnUserInfo) {
                        UserInfo userInfo = toClientUser(osnUserInfo);
                        SqliteUtils.insertUser(userInfo);
                        userInfo = toRemarkName(userInfo);
                        onUserInfoUpdated(Collections.singletonList(userInfo));
                    }
                });
            }
            if (userInfo == null)
                userInfo = new NullUserInfo(userId);
            else
                userInfo = toRemarkName(userInfo);
            return userInfo;
        }

        @Override
        public List<UserInfo> getUserInfos(List<String> userIds, String groupId) throws RemoteException {
            logInfo("");
            List<UserInfo> userInfos = new ArrayList<>();
            for (String u : userIds) {
                UserInfo userInfo = getUserInfo(u, groupId, false);
                userInfos.add(userInfo);
            }
            return userInfos;
        }

        @Override
        public void getUserInfoEx(String userId, boolean refresh, IGetUserCallback callback) throws RemoteException {
            logInfo("userID: " + userId + ", refresh: " + refresh);
            osnsdk.getUserInfo(userId, new OSNGeneralCallbackT<OsnUserInfo>() {
                @Override
                public void onFailure(String error) {
                    try {
                        callback.onFailure(-1);
                    } catch (Exception e) {
                        logError(e);
                    }
                }

                @Override
                public void onSuccess(OsnUserInfo osnUserInfo) {
                    try {
                        UserInfo userInfo = toClientUser(osnUserInfo);
                        SqliteUtils.insertUser(userInfo);
                        callback.onSuccess(userInfo);
                    } catch (Exception e) {
                        logError(e);
                    }
                }
            });
        }
        public void uploadLogger(final IUploadMediaCallback callback){
            try {
                String type = "cache";
                String path = getFilesDir().getAbsolutePath() + "/" + mLogFileName;
                FileInputStream fileInputStream = new FileInputStream(path);
                osnsdk.uploadData(mLogFileName, type, fileInputStream, new OSNTransferCallback() {
                    @Override
                    public void onSuccess(String data) {
                        try {
                            logInfo("result: "+data);
                            callback.onSuccess(data);
                        } catch (RemoteException e) {
                            logError(e);
                        }
                    }

                    @Override
                    public void onProgress(long progress, long total) {
                    }

                    @Override
                    public void onFailure(String error) {
                        try {
                            callback.onFailure(-1);
                        } catch (RemoteException e) {
                            logError(e);
                        }
                    }
                });
            }catch (Exception e){
                logError(e);
            }
        }
        @Override
        public void uploadMedia(String fileName, byte[] data, int mediaType, final IUploadMediaCallback callback) throws RemoteException {
            logInfo("fileName: " + fileName + ", mediaType: " + mediaType);
            String type = mediaType == MessageContentMediaType.PORTRAIT.getValue() ? "portrait" : "cache";
            osnsdk.uploadData(fileName, type, data, new OSNTransferCallback() {
                @Override
                public void onSuccess(String data) {
                    try {
                        JSONObject json = JSON.parseObject(data);
                        callback.onSuccess(json.getString("url"));
                        return;
                    } catch (Exception e) {
                        logError(e);
                    }
                    try {
                        callback.onFailure(-1);
                    } catch (Exception e) {
                        logError(e);
                    }
                }

                @Override
                public void onProgress(long progress, long total) {
                    try {
                        callback.onProgress(progress, total);
                    } catch (Exception e) {
                        logError(e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        callback.onFailure(-1);
                    } catch (Exception e) {
                        logError(e);
                    }
                }
            });
        }

        @Override
        public void uploadMediaFile(String mediaPath, int mediaType, IUploadMediaCallback callback) throws RemoteException {
            logInfo("");
            try {
                BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(mediaPath));
                int length = bufferedInputStream.available();
                byte[] data = new byte[length];
                bufferedInputStream.read(data);

//                String fileName = "";
//                if (mediaPath.contains("/")) {
//                    fileName = mediaPath.substring(mediaPath.lastIndexOf("/") + 1, mediaPath.length());
//                }
                uploadMedia(mediaPath, data, mediaType, callback);
            } catch (Exception e) {
                logError(e);
                logError(e);
                callback.onFailure(ErrorCode.FILE_NOT_EXIST);
            }
        }

        @Override
        public void modifyMyInfo(List<ModifyMyInfoEntry> values, final IGeneralCallback callback) throws RemoteException {
            List<String> keys = new ArrayList<>();
            OsnUserInfo userInfo = new OsnUserInfo();
            for (ModifyMyInfoEntry v : values) {
                if (v.type == ModifyMyInfoType.Modify_DisplayName) {
                    keys.add("displayName");
                    userInfo.displayName = v.value;
                } else if (v.type == ModifyMyInfoType.Modify_Portrait) {
                    keys.add("portrait");
                    userInfo.portrait = v.value;
                } else if (v.type == ModifyMyInfoType.Modify_UrlSpace) {
                    keys.add("urlSpace");
                    userInfo.urlSpace = v.value;
                } else if (v.type == ModifyMyInfoType.Modify_Nft){
                    keys.add("describes");
                    JSONObject json = new JSONObject();
                    json.put("nft", v.value);
                    userInfo.describes = json.toString();
                }
            }
            logInfo("keys: "+JSONObject.toJSONString(keys)+", userInfo: "+JSONObject.toJSONString(userInfo));
            osnsdk.modifyUserInfo(keys, userInfo, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        callback.onSuccess();
                    } catch (Exception e) {
                        logError(e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        callback.onFailure(-1);
                    } catch (Exception e) {
                        logError(e);
                    }
                }
            });
        }

        @Override
        public boolean deleteMessage(long messageId) throws RemoteException {
            logInfo("");
            Message message = SqliteUtils.queryMessage(messageId);
            if(message == null){
                return false;
            }
            long timestamp = System.currentTimeMillis();
            if(message.sender.equalsIgnoreCase(mUserId) &&
                    timestamp > message.serverTime &&
                    (timestamp - message.serverTime) < 5*60*1000){
                //osnsdk.deleteMessage(message.messageHash, message.conversation.target, null);
            }
            SqliteUtils.deleteMessage(messageId);
            return true;
        }

        @Override
        public List<ConversationSearchResult> searchConversation(String keyword, int[] conversationTypes, int[] lines) throws RemoteException {
            logInfo("");
            List<ConversationSearchResult> output = new ArrayList<>();
            return output;
        }

        @Override
        public List<Message> searchMessage(Conversation conversation, String keyword, boolean desc, int limit, int offset) throws RemoteException {
            logInfo("key: " + keyword + ", desc: " + desc + ", limit: " + limit + ", offset: " + offset);
            return SqliteUtils.queryMessages(conversation, keyword, desc, limit, offset);
        }

        @Override
        public void searchMessagesEx(int[] conversationTypes, int[] lines, int[] contentTypes, String keyword, long fromIndex, boolean before, int count, IGetMessageCallback callback) throws RemoteException {
            logInfo("");
        }


        @Override
        public List<GroupSearchResult> searchGroups(String keyword) throws RemoteException {
            //logInfo("");
            List<GroupSearchResult> output = new ArrayList<>();
            List<GroupInfo> groups = SqliteUtils.queryFavGroups(keyword);
            for (GroupInfo group : groups) {
                if (group.name.contains(keyword)) {
                    GroupSearchResult result = new GroupSearchResult();
                    result.groupInfo = group;
                    result.marchedType = 0;
                    output.add(result);
                }
            }
            return output;
        }

        @Override
        public List<UserInfo> searchFriends(String keyworkd) throws RemoteException {
            logInfo("");
            List<UserInfo> friendInfos = new ArrayList<>();
            List<String> friends = SqliteUtils.listFriends();
            for (String f : friends) {
                UserInfo userInfo = SqliteUtils.queryUser(f);
                if (userInfo == null) {
                    OsnUserInfo osnUserInfo = osnsdk.getUserInfo(f, null);
                    if (osnUserInfo != null)
                        userInfo = toClientUser(osnUserInfo);
                }
                if (userInfo != null) {
                    if (userInfo.displayName.contains(keyworkd))
                        friendInfos.add(userInfo);
                }
            }
            return friendInfos;
        }

        @Override
        public String getEncodedClientId() throws RemoteException {
            logInfo("");
            return null;
        }

        @Override
        public void createGroup(String groupId, String groupName, String groupPortrait,String owner2, int groupType, List<String> memberIds, int[] notifyLines, MessagePayload notifyMsg, final IGeneralCallback2 callback) throws RemoteException {
            logInfo("groupId: " + groupId + ", groupName: " + groupName + ", groupType: " + groupType);
            for (String member : memberIds)
                logInfo("member: " + member);

            osnsdk.createGroup2(groupName, memberIds, groupType, groupPortrait,owner2, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        callback.onSuccess(data.getString("groupID"));
                    } catch (Exception e) {
                        logError(e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        callback.onFailure(-1);
                    } catch (Exception e) {
                        logError(e);
                    }
                }
            });
        }

        @Override
        public void joinGroup(String groupId, String reason, final IGeneralCallback callback) {
            logInfo("groupId: " + groupId+", reason: "+reason);
            osnsdk.joinGroup(groupId, reason, null, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        callback.onSuccess();
                    } catch (Exception e) {
                        logError(e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        callback.onFailure(-1);
                    } catch (Exception e) {
                        logError(e);
                    }
                }
            });
        }

        @Override
        public void addGroupMembers(String groupId, List<String> memberIds, int[] notifyLines, MessagePayload notifyMsg, final IGeneralCallback callback) throws RemoteException {
            logInfo("");
            osnsdk.addMember(groupId, memberIds, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        callback.onSuccess();
                    } catch (Exception e) {
                        logError(e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        callback.onFailure(-1);
                    } catch (Exception e) {
                        logError(e);
                    }
                }
            });
        }

        @Override
        public void removeGroupMembers(String groupId, List<String> memberIds, int[] notifyLines, MessagePayload notifyMsg, final IGeneralCallback callback) throws RemoteException {
            logInfo("");
            osnsdk.delMember(groupId, memberIds, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        callback.onSuccess();
                    } catch (Exception e) {
                        logError(e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        callback.onFailure(-1);
                    } catch (Exception e) {
                        logError(e);
                    }
                }
            });
        }

        @Override
        public void quitGroup(String groupId, int[] notifyLines, MessagePayload notifyMsg, final IGeneralCallback callback) throws RemoteException {
            logInfo("");
            osnsdk.quitGroup(groupId, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        callback.onSuccess();
                    } catch (Exception e) {
                        logError(e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        callback.onFailure(-1);
                    } catch (Exception e) {
                        logError(e);
                    }
                }
            });
        }

        @Override
        public void dismissGroup(String groupId, int[] notifyLines, MessagePayload notifyMsg, final IGeneralCallback callback) throws RemoteException {
            logInfo("");
            osnsdk.dismissGroup(groupId, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        callback.onSuccess();
                        delGroup(groupId, "dismiss");
                    } catch (Exception e) {
                        logError(e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        callback.onFailure(-1);
                    } catch (Exception e) {
                        logError(e);
                    }
                }
            });
        }

        @Override
        public void modifyGroupInfo(String groupId, int modifyType, String newValue, int[] notifyLines, MessagePayload notifyMsg, final IGeneralCallback callback) throws RemoteException {
            logInfo("groupID: " + groupId + ", type: " + modifyType + ", value: " + newValue);

            OsnGroupInfo groupInfo = new OsnGroupInfo();
            groupInfo.groupID = groupId;

            List<String> keys = new ArrayList<>();
            if (modifyType == ModifyGroupInfoType.Modify_Group_Name.getValue()) {
                keys.add("name");
                groupInfo.name = newValue;
            } else if (modifyType == ModifyGroupInfoType.Modify_Group_Portrait.getValue()) {
                keys.add("portrait");
                groupInfo.portrait = newValue;
            } else if (modifyType == ModifyGroupInfoType.Modify_Group_Type.getValue()) {
                keys.add("type");
                groupInfo.type = Integer.parseInt(newValue);
            } else if (modifyType == ModifyGroupInfoType.Modify_Group_JoinType.getValue()) {
                keys.add("joinType");
                groupInfo.joinType = Integer.parseInt(newValue);
            } else if (modifyType == ModifyGroupInfoType.Modify_Group_PassType.getValue()) {
                keys.add("passType");
                groupInfo.passType = Integer.parseInt(newValue);
            } else if (modifyType == ModifyGroupInfoType.Modify_Group_Mute.getValue()) {
                keys.add("mute");
                groupInfo.mute = Integer.parseInt(newValue);
            } else if (modifyType == ModifyGroupInfoType.Modify_Group_RedPacket.getValue()){
                keys.add("attribute");
                JSONObject json = new JSONObject();
                json.put("redPacket", newValue);
                groupInfo.attribute = json.toString();
            } else if (modifyType == ModifyGroupInfoType.Modify_Group_Notice.getValue()){
                osnsdk.billboard(groupId, newValue, new OSNGeneralCallback() {
                    @Override
                    public void onSuccess(JSONObject data) {
                        try {
                            callback.onSuccess();
                        } catch (Exception e) {
                            logError(e);
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        try {
                            callback.onFailure(-1);
                        } catch (Exception e) {
                            logError(e);
                        }
                    }
                });
                return;
            }
            osnsdk.modifyGroupInfo(keys, groupInfo, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        callback.onSuccess();
                    } catch (Exception e) {
                        logError(e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        callback.onFailure(-1);
                    } catch (Exception e) {
                        logError(e);
                    }
                }
            });
        }

        @Override
        public void modifyGroupAlias(String groupId, String newAlias, int[] notifyLines, MessagePayload notifyMsg, final IGeneralCallback callback) throws RemoteException {
            logInfo("");
            OsnMemberInfo info = new OsnMemberInfo();
            info.groupID = groupId;
            info.osnID = mUserId;
            info.nickName = newAlias;
            osnsdk.modifyMemberInfo(Collections.singletonList("nickName"), info, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        if (callback != null)
                            callback.onSuccess();
                    } catch (Exception e) {
                        logError(e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        if (callback != null)
                            callback.onFailure(-1);
                    } catch (Exception e) {
                        logError(e);
                    }
                }
            });
        }

        @Override
        public void modifyGroupMemberAlias(String groupId, String memberId, String newAlias, int[] notifyLines, MessagePayload notifyMsg, IGeneralCallback callback) throws RemoteException {
            logInfo("");
            OsnMemberInfo info = new OsnMemberInfo();
            info.groupID = groupId;
            info.osnID = memberId;
            info.nickName = newAlias;
            osnsdk.modifyMemberInfo(Collections.singletonList("nickName"), info, new OSNGeneralCallback() {
                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        if (callback != null)
                            callback.onSuccess();
                    } catch (Exception e) {
                        logError(e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        if (callback != null)
                            callback.onFailure(-1);
                    } catch (Exception e) {
                        logError(e);
                    }
                }
            });
        }

        @Override
        public List<GroupMember> getGroupMembers(String groupId, boolean forceUpdate) throws RemoteException {
            logInfo("groupID: " + groupId + ", forceUpdate: " + forceUpdate);
            List<GroupMember> members = SqliteUtils.queryMembersTop(groupId);
            if (members == null || forceUpdate) {
                osnsdk.getMemberInfoZone(groupId, 0, 25, new OSNGeneralCallbackT<List<OsnMemberInfo>>() {
                    @Override
                    public void onFailure(String error) {

                    }

                    @Override
                    public void onSuccess(List<OsnMemberInfo> members) {

                        List<GroupMember> memberList = insertMembers(members, 0);
                        //List<GroupMember> memberList = updateMember(groupId, members);
                        onGroupMembersUpdated(groupId, memberList);
                    }
                });
            }
            /*boolean isValied = false;
            for (GroupMember m : members) {
                if (m.memberId.equalsIgnoreCase(mUserId)) {
                    isValied = true;
                    break;
                }
                logInfo("memberID: " + m.memberId + ", alias: " + m.alias + ", type: " + m.type);
            }
            if (!isValied)
                members.clear();*/
            return members;
        }

        @Override
        public List<GroupMember> getGroupMemberAll(String groupId){
            List<GroupMember> members = SqliteUtils.queryMembersAll(groupId);
            return members;
        }

        @Override
        public List<GroupMember> getGroupManagers(String groupId){
            List<GroupMember> members = SqliteUtils.queryMembersManager(groupId);
            return members;
        }


        /*@Override
        public List<GroupMember> getGroupMembers(String groupId, boolean forceUpdate) throws RemoteException {
            logInfo("groupID: " + groupId + ", forceUpdate: " + forceUpdate);
            List<GroupMember> members = SqliteUtils.queryMembers(groupId);
            if (members == null || forceUpdate) {
                osnsdk.getMemberInfo(groupId, new OSNGeneralCallbackT<List<OsnMemberInfo>>() {
                    @Override
                    public void onFailure(String error) {
                    }

                    @Override
                    public void onSuccess(List<OsnMemberInfo> members) {
                        List<GroupMember> memberList = updateMember(groupId, members);
                        onGroupMembersUpdated(groupId, memberList);
                    }
                });
            }
            boolean isValied = false;
            for (GroupMember m : members) {
                if (m.memberId.equalsIgnoreCase(mUserId)) {
                    isValied = true;
                    break;
                }
                logInfo("memberID: " + m.memberId + ", alias: " + m.alias + ", type: " + m.type);
            }
            if (!isValied)
                members.clear();
            return members;
        }*/


        @Override
        public List<GroupMember> getGroupMembersByType(String groupId, int type) throws RemoteException {
            logInfo("");
            List<GroupMember> out = new ArrayList<>();
            return out;
        }

        @Override
        public GroupMember getGroupMember(String groupId, String memberId) throws RemoteException {
            GroupMember groupMember = SqliteUtils.queryMember(groupId, memberId);
            if (groupMember == null)
                groupMember = new NullGroupMember(groupId, memberId);
            logInfo("memberID: " + memberId + ", type: " + groupMember.type);
            return groupMember;
        }
        public void getGroupMemberZone(String groupId, int start, int count, IGetGroupMemberCallback callback){
            logInfo("start: "+start+", count: "+count+", groupID: "+groupId);
            osnsdk.getMemberInfoZone(groupId, start, count, new OSNGeneralCallbackT<List<OsnMemberInfo>>() {
                @Override
                public void onFailure(String error) {
                    try {
                        callback.onFailure(-1);
                    } catch (RemoteException e) {
                        logError(e);
                    }
                }

                @Override
                public void onSuccess(List<OsnMemberInfo> osnMemberInfos) {
                    try {
                        //logInfo("@@@ size: "+osnMemberInfos.size());
                        //System.out.println("@@@@ getGroupMemberZone insert members");
                        System.out.println("@@@@ getGroupMemberZone insert members: begin(" +start+
                                ") size :" + osnMemberInfos.size());
                        //List<GroupMember> memberList = updateMemberZone(osnMemberInfos);
                        List<GroupMember> memberList = insertMembers(osnMemberInfos, start);
                        callback.onSuccess(memberList);
                    } catch (RemoteException e) {
                        logError(e);
                    }
                }
            });
        }

        public void getGroupMemberFromDB(String groupId, int begin, int end, IGetGroupMemberCallback callback){
            try {
                List<GroupMember> members = SqliteUtils.queryMembers(groupId, begin, end);
                if (members == null) {
                    callback.onFailure(-1);
                    return;
                }
                if (members.size() == 0) {
                    callback.onFailure(-1);
                    return;
                }
                callback.onSuccess(members);
            } catch (Exception e) {

            }
        }

        public void clearOutGroupMembers(String groupId, int min) {
            SqliteUtils.deleteMembers(groupId, min);
        }
        @Override
        public void getGroupMemberEx(String groupId, boolean forceUpdate, IGetGroupMemberCallback callback) throws RemoteException {
            logInfo("");
        }

        @Override
        public void transferGroup(String groupId, String newOwner, int[] notifyLines, MessagePayload notifyMsg, final IGeneralCallback callback) throws RemoteException {
            logInfo("");
        }

        @Override
        public void setGroupManager(String groupId, boolean isSet, List<String> memberIds, int[] notifyLines, MessagePayload notifyMsg, IGeneralCallback callback) throws RemoteException {
            logInfo("");
            String[] memberArray = new String[memberIds.size()];
            for (int i = 0; i < memberIds.size(); i++) {
                memberArray[i] = memberIds.get(i);
            }
            OsnMemberInfo info = new OsnMemberInfo();
            info.groupID = groupId;
            for(String memberId : memberIds){
                info.osnID = memberId;
                info.type = MemberType_Admin;
                osnsdk.modifyMemberInfo(Collections.singletonList("type"), info, new OSNGeneralCallback() {
                    @Override
                    public void onSuccess(JSONObject data) {
                        try {
                            if (callback != null)
                                callback.onSuccess();
                        } catch (Exception e) {
                            logError(e);
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        try {
                            if (callback != null)
                                callback.onFailure(-1);
                        } catch (Exception e) {
                            logError(e);
                        }
                    }
                });
            }
        }

        @Override
        public boolean isGroupMember(String groupId, String userId) {
            logInfo("groupId: " + groupId + ", userId: " + userId);
            return SqliteUtils.queryMember(groupId, userId) != null;
        }

        @Override
        public void muteOrAllowGroupMember(String groupId, boolean isSet, int mode, List<String> memberIds, boolean isAllow, int[] notifyLines, MessagePayload notifyMsg, IGeneralCallback callback) throws RemoteException {
            logInfo("groupID: "+groupId+", isSet: "+isSet+", isAllow: "+isAllow);
            if (memberIds != null) {
                String[] memberArray = new String[memberIds.size()];
                for (int i = 0; i < memberIds.size(); i++) {
                    memberArray[i] = memberIds.get(i);
                }
            }

            osnsdk.muteGroup(groupId, isSet, mode, memberIds, new OSNGeneralCallback(){

                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        callback.onSuccess();
                        List<GroupMember> memberList = new ArrayList<>();
                        if (memberIds != null) {
                            for(String member : memberIds){
                                GroupMember groupMember = SqliteUtils.queryMember(groupId, member);
                                groupMember.mute = isSet?1:0;
                                memberList.add(groupMember);
                                SqliteUtils.updateMember(groupMember, Collections.singletonList("mute"));
                            }

                            onGroupMembersUpdated(groupId, memberList);
                        }


                    } catch (RemoteException e) {
                        logError(e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        callback.onFailure(-1);
                    } catch (RemoteException e) {
                        logError(e);
                    }
                }
            });
        }

        @Override
        public void muteGroup(String groupId, boolean isSet, int mode, List<String> memberIds, boolean isAllow, int[] notifyLines, MessagePayload notifyMsg, IGeneralCallback callback) throws RemoteException {


            osnsdk.muteGroup(groupId, isSet, mode, null, new OSNGeneralCallback(){

                @Override
                public void onSuccess(JSONObject data) {
                    try {
                        callback.onSuccess();



                    } catch (RemoteException e) {
                        logError(e);
                    }
                }

                @Override
                public void onFailure(String error) {
                    try {
                        callback.onFailure(-1);
                    } catch (RemoteException e) {
                        logError(e);
                    }
                }
            });
        }


        @Override
        public byte[] encodeData(byte[] data) throws RemoteException {
            return null;
        }

        @Override
        public byte[] decodeData(byte[] data) throws RemoteException {
            return null;
        }

        @Override
        public String getHost() throws RemoteException {
            return mHost;
        }

        @Override
        public void setHost(String host) throws RemoteException {
            mHost = host;
            osnsdk.resetHost(host);
        }

        @Override
        public void createChannel(String channelId, String channelName, String channelPortrait, String desc, String extra, ICreateChannelCallback callback) throws RemoteException {
            logInfo("");
        }

        @Override
        public void modifyChannelInfo(String channelId, int modifyType, String newValue, IGeneralCallback callback) throws RemoteException {
            logInfo("");
        }

        @Override
        public ChannelInfo getChannelInfo(String channelId, boolean refresh) throws RemoteException {
            logInfo("");
            return null;
        }

        @Override
        public void searchChannel(String keyword, ISearchChannelCallback callback) throws RemoteException {
            logInfo("");
        }

        @Override
        public boolean isListenedChannel(String channelId) throws RemoteException {
            logInfo("");
            return false;
        }

        @Override
        public void listenChannel(String channelId, boolean listen, IGeneralCallback callback) throws RemoteException {
            logInfo("");
        }

        @Override
        public void destoryChannel(String channelId, IGeneralCallback callback) throws RemoteException {
            logInfo("");
        }

        @Override
        public List<String> getMyChannels() throws RemoteException {
            logInfo("");
            List<String> out = new ArrayList<>();
            return out;
        }

        @Override
        public List<String> getListenedChannels() throws RemoteException {
            logInfo("");
            List<String> out = new ArrayList<>();
            return out;
        }

        @Override
        public List<LitappInfo> getLitappList() {
            return SqliteUtils.listLitapps();
        }

        @Override
        public List<LitappInfo> getCollectLitappList() {
            return SqliteUtils.listCollectLitapps();
        }

        @Override
        public void deleteCollectLitapp(String target){
            SqliteUtils.deleteCollectLitapp(target);
        }

        @Override
        public void deleteLitapp(String target){
            SqliteUtils.deleteLitapp(target);
        }

        @Override
        public void addCollectLitapp(LitappInfo litapp) {
            SqliteUtils.insertCollectLitapp(litapp);
        }

        @Override
        public void addLitapp(LitappInfo litapp) {
            SqliteUtils.insertLitapp(litapp);
        }

        public LitappInfo getLitapp(String target) {
            return SqliteUtils.queryLitapp(target);
        }

        public LitappInfo getCollectLitapp(String target) {
            return SqliteUtils.queryCollectLitapp(target);
        }

        @Override
        public LitappInfo getLitappInfo(String target, boolean refresh) {
            LitappInfo litappInfo = getLitapp(target);
            if (litappInfo == null || refresh) {
                osnsdk.getServiceInfo(target, new OSNGeneralCallbackT<OsnServiceInfo>() {
                    @Override
                    public void onFailure(String error) {
                    }

                    @Override
                    public void onSuccess(OsnServiceInfo osnServiceInfo) {
                        if (osnServiceInfo instanceof OsnLitappInfo) {
                            LitappInfo litappInfo = toClientLitapp((OsnLitappInfo) osnServiceInfo);
                            addLitapp(litappInfo);
                            onLitappInfoUpdated(Collections.singletonList(litappInfo));
                        }
                    }
                });
            }
            return litappInfo;
        }

        @Override
        public void getLitappInfoEx(String target, boolean refresh, IGetLitappCallback callback) {
            LitappInfo litappInfo = getLitapp(target);
            if (litappInfo == null || refresh) {
                osnsdk.getServiceInfo(target, new OSNGeneralCallbackT<OsnServiceInfo>() {
                    @Override
                    public void onFailure(String error) {
                        try {
                            callback.onFailure(-1);
                        } catch (Exception e) {
                            logError(e);
                        }
                    }

                    @Override
                    public void onSuccess(OsnServiceInfo osnServiceInfo) {
                        try {
                            if (osnServiceInfo instanceof OsnLitappInfo) {
                                LitappInfo litappInfo = toClientLitapp((OsnLitappInfo) osnServiceInfo);
                                addLitapp(litappInfo);
                                callback.onSuccess(litappInfo);
                            }
                        } catch (Exception e) {
                            logError(e);
                        }
                    }
                });
            }
        }

        @Override
        public String hashData(byte[] data) {
            return osnsdk.hashData(data);
        }
        @Override
        public String signData(byte[] data) {
            return osnsdk.signData(data);
        }
        @Override
        public boolean verifyData(String osnID, byte[] data, String sign){
            return osnsdk.verifyData(osnID, data, sign);
        }
        @Override
        public String encryptData(String osnID, byte[] data) {
            return osnsdk.encryptData(osnID, data);
        }

        @Override
        public String getImageThumbPara() throws RemoteException {
            logInfo("");
            return null;
        }



        @Override
        public void kickoffPCClient(String pcClientId, IGeneralCallback callback) throws RemoteException {
            logInfo("");
        }

        @Override
        public void getApplicationId(String applicationId, IGeneralCallback2 callback) throws RemoteException {
            logInfo("");
        }

        @Override
        public void getAuthorizedMediaUrl(long messageUid, int mediaType, String mediaPath, IGeneralCallback2 callback) throws RemoteException {
            logInfo("");
        }

        @Override
        public int getMessageCount(Conversation conversation) throws RemoteException {
            logInfo("");
            return 0;
        }

        @Override
        public boolean begainTransaction() throws RemoteException {
            logInfo("");
            return false;
        }

        @Override
        public void commitTransaction() throws RemoteException {
            logInfo("");
        }

        @Override
        public boolean isCommercialServer() throws RemoteException {
            logInfo("");
            return false;
        }

        @Override
        public boolean isReceiptEnabled() throws RemoteException {
            logInfo("");
            return false;
        }

        @Override
        public void sendConferenceRequest(long sessionId, String roomId, String request, String data, IGeneralCallback2 callback) throws RemoteException {
            logInfo("");
        }
        public void setIconId(int id){
            mNotifyIconId = id;
        }
    }
    private class WfcRemoteCallbackList<E extends IInterface> extends RemoteCallbackList<E> {
        @Override
        public void onCallbackDied(E callback, Object cookie) {
            Intent intent = new Intent(ClientService.this, RecoverReceiver.class);
            sendBroadcast(intent);
        }
    }
    private void playMessageTipAudio(){
        long timestamp = System.currentTimeMillis();
        if(timestamp - messageSound > 10000){
            messageSound = timestamp;
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone ringtone = RingtoneManager.getRingtone(this, uri);
            ringtone.play();
        }
    }

//    private void showNotification(Context context, String tag, int id, String title, String content, PendingIntent pendingIntent) {
//        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        String channelId = "zolo_notification";
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(channelId, "zolo chat message", NotificationManager.IMPORTANCE_HIGH);
//            channel.enableLights(true); //是否在桌面icon右上角展示小红点
//            channel.setLightColor(Color.RED); //小红点颜色
//            channel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
//            notificationManager.createNotificationChannel(channel);
//        }
//
//        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
//            builder.setColor(Color.RED);
//        }
//        builder.setSmallIcon(mNotifyIconId);
//        builder.setContentTitle(title);
//        builder.setContentText(content);
//        builder.setAutoCancel(true);
//        builder.setSound(defaultSoundUri);
//        builder.setCategory(CATEGORY_MESSAGE);
//        builder.setDefaults(DEFAULT_ALL);
//        builder.setContentIntent(pendingIntent);
//        notificationManager.notify(tag, id, builder.build());
//    }
//    public void showNotification(FriendRequest friendRequest) throws RemoteException {
//        mBinder.getUserInfoEx(friendRequest.userID, true, new IGetUserCallback.Stub() {
//            @Override
//            public void onSuccess(UserInfo userInfo) throws RemoteException {
//                String text = userInfo.displayName;
//                text += getString(R.string.request_friend);
//                String title = getString(R.string.friend_request);
//                Intent mainIntent = new Intent(ClientService.this.getPackageName() + ".main");
//                Intent friendRequestListIntent = new Intent(ClientService.this, FriendRequestListActivity.class);
//                int notificationId = 1;
//                PendingIntent pendingIntent = PendingIntent.getActivities(ClientService.this, notificationId, new Intent[]{mainIntent, friendRequestListIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
//                String tag = "zolo friendRequest notification tag";
//                showNotification(ClientService.this, tag, notificationId, title, text, pendingIntent);
//            }
//
//            @Override
//            public void onFailure(int errorCode) throws RemoteException {
//
//            }
//        });
//    }
//    public void showNotification(List<Message> messages) {
//        if (messages == null || messages.isEmpty()) {
//            return;
//        }
//        for (Message message : messages) {
//            if (message.direction == MessageDirection.Send || (message.content.getPersistFlag() != Persist_And_Count && !(message.content instanceof RecallMessageContent))) {
//                continue;
//            }
//            ConversationInfo conversationInfo = ChatManager.Instance().getConversation(message.conversation);
//            if (conversationInfo.isSilent) {
//                continue;
//            }
//
//            String pushContent = message.content.pushContent;
//            if (TextUtils.isEmpty(pushContent)) {
//                pushContent = message.content.digest(message, this);
//            }
//
//            int unreadCount = ChatManager.Instance().getUnreadCount(message.conversation).unread;
//            if (unreadCount > 1) {
//                pushContent = "[" + unreadCount + getString(R.string.no)+"]" + pushContent;
//            }
//
//            String title = "";
//            if (message.conversation.type == Single) {
//                String name = ChatManager.Instance().getUserDisplayName(message.conversation.target);
//                title = TextUtils.isEmpty(name) ? getString(R.string.new_message) : name;
//            } else if (message.conversation.type == Conversation.ConversationType.Group) {
//                GroupInfo groupInfo = ChatManager.Instance().getGroupInfo(message.conversation.target, false);
//                title = groupInfo == null ? getString(R.string.group_cheat) : groupInfo.name;
//            } else {
//                title = getString(R.string.new_message);
//            }
//            Intent mainIntent = new Intent(getPackageName() + ".main");
//            Intent conversationIntent = new Intent(this, ConversationActivity.class);
//            conversationIntent.putExtra("conversation", message.conversation);
//            int notificationId = 0;
//            PendingIntent pendingIntent = PendingIntent.getActivities(this, notificationId, new Intent[]{mainIntent, conversationIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
//            String tag = "zolo notification tag";
//            showNotification(this, tag, notificationId, title, pushContent, pendingIntent);
//        }
//    }
}
