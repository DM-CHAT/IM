/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.wildfire.chat.kit.contact.newfriend.FriendRequestListActivity;
import cn.wildfire.chat.kit.conversation.ConversationActivity;
import cn.wildfire.chat.kit.user.UserInfoActivity;
import cn.wildfire.chat.kit.utils.SPUtils;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.message.notification.RecallMessageContent;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GetUserInfoCallback;

import static androidx.core.app.NotificationCompat.CATEGORY_MESSAGE;
import static androidx.core.app.NotificationCompat.DEFAULT_ALL;
import static cn.wildfirechat.message.core.PersistFlag.Persist_And_Count;
import static cn.wildfirechat.model.Conversation.ConversationType.Single;

public class WfcNotificationManager {
    private WfcNotificationManager() {

    }

    private static WfcNotificationManager notificationManager;

    public synchronized static WfcNotificationManager getInstance() {
        if (notificationManager == null) {
            notificationManager = new WfcNotificationManager();
        }
        return notificationManager;
    }

    public void clearAllNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        notificationConversations.clear();
    }

    private void showNotification(Context context, String tag, int id, String title, String content, PendingIntent pendingIntent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "wfc_notification";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "wildfire chat message", NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true); //是否在桌面icon右上角展示小红点
            channel.setLightColor(Color.RED); //小红点颜色
            channel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
            notificationManager.createNotificationChannel(channel);
        }

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);
        //builder.setSmallIcon(R.mipmap.ic_launcher);
        UiWrapper.getNotificationIcon(builder);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            //如果小于7.0系统,设置背景色
            builder.setColor(Color.RED);
        }
        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setAutoCancel(true);
        builder.setSound(defaultSoundUri);
        builder.setCategory(CATEGORY_MESSAGE);
        builder.setDefaults(DEFAULT_ALL);
        builder.setContentIntent(pendingIntent);

        notificationManager.notify(tag, id, builder.build());
    }

    public void handleRecallMessage(Context context, Message message) {
        handleReceiveMessage(context, Collections.singletonList(message));
    }

    public void handleReceiveMessage(Context context, List<Message> messages) {

        if (messages == null || messages.isEmpty()) {
            return;
        }

        if (ChatManager.Instance().isGlobalSilent()) {
            return;
        }

        boolean hiddenNotificationDetail = ChatManager.Instance().isHiddenNotificationDetail();

        for (Message message : messages) {
            if (message.direction == MessageDirection.Send || (message.content.getPersistFlag() != Persist_And_Count && !(message.content instanceof RecallMessageContent))) {
                continue;
            }
            ConversationInfo conversationInfo = ChatManager.Instance().getConversation(message.conversation);
            if (conversationInfo.isSilent) {
                continue;
            }

            String pushContent = hiddenNotificationDetail ? WfcUIKit.getString(R.string.new_message) : message.content.pushContent;
            if (TextUtils.isEmpty(pushContent)) {
                pushContent = message.content.digest(message, WfcUIKit.getActivity());
            }

            int unreadCount = ChatManager.Instance().getUnreadCount(message.conversation).unread;
            if (unreadCount > 1) {
                pushContent = "[" + unreadCount + WfcUIKit.getString(R.string.no)+"]" + pushContent;
            }

            String title = "";
            if (message.conversation.type == Single) {
                String name = ChatManager.Instance().getUserDisplayName(message.conversation.target);
                title = TextUtils.isEmpty(name) ? WfcUIKit.getString(R.string.new_message) : name;
            } else if (message.conversation.type == Conversation.ConversationType.Group) {
                GroupInfo groupInfo = ChatManager.Instance().getGroupInfo(message.conversation.target, false);
                title = groupInfo == null ? WfcUIKit.getString(R.string.group_cheat) : groupInfo.name;
            } else {
                title = WfcUIKit.getString(R.string.new_message);
            }
            String MainDapp = (String) SPUtils.get(context,"MainDapp","");
            Intent mainIntent = null;
            if(MainDapp.length() == 0){
                mainIntent  = new Intent(context.getPackageName() + ".main1");
            }else{
                mainIntent  = new Intent(context.getPackageName() + ".main");
            }

         //   Intent mainIntent = new Intent(context.getPackageName() + ".main");
            Intent conversationIntent = new Intent(context, ConversationActivity.class);
            conversationIntent.putExtra("conversation", message.conversation);

            PendingIntent pendingIntent;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                pendingIntent = PendingIntent.getActivities(context, notificationId(message.conversation), new Intent[]{mainIntent, conversationIntent}, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            } else {
                pendingIntent = PendingIntent.getActivities(context, notificationId(message.conversation), new Intent[]{mainIntent, conversationIntent}, PendingIntent.FLAG_ONE_SHOT);
            }
        //    PendingIntent pendingIntent = PendingIntent.getActivities(context, notificationId(message.conversation), new Intent[]{mainIntent, conversationIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
            String tag = "wfc notification tag";
            showNotification(context, tag, notificationId(message.conversation), title, pushContent, pendingIntent);
        }
    }

    public void handleFriendRequest(Context context, List<String> friendRequests) {

        if (ChatManager.Instance().isGlobalSilent()) {
            return;
        }
        ChatManager.Instance().getUserInfo(friendRequests.get(0), true, new GetUserInfoCallback() {
            @Override
            public void onSuccess(UserInfo userInfo) {
                String text = userInfo.displayName;
                if (friendRequests.size() > 1) {
                    text += WfcUIKit.getString(R.string.deng);
                }
                text += WfcUIKit.getString(R.string.request_friend);
                String title = WfcUIKit.getString(R.string.friend_request);
                showFriendRequestNotification(context, title, text);
            }

            @Override
            public void onFail(int errorCode) {

            }
        });
    }

    private void showFriendRequestNotification(Context context, String title, String text) {
        String MainDapp = (String) SPUtils.get(context,"MainDapp","");
        Intent mainIntent = null;
        if(MainDapp.length() == 0){
            mainIntent  = new Intent(context.getPackageName() + ".main1");
        }else{
            mainIntent  = new Intent(context.getPackageName() + ".main");
        }

     //   Intent mainIntent = new Intent(context.getPackageName() + ".main");
        Intent friendRequestListIntent = new Intent(context, FriendRequestListActivity.class);
        int notificationId = 10000;

        PendingIntent pendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivities(context, notificationId, new Intent[]{mainIntent, friendRequestListIntent}, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivities(context, notificationId, new Intent[]{mainIntent, friendRequestListIntent}, PendingIntent.FLAG_ONE_SHOT);
        }

   //     PendingIntent pendingIntent = PendingIntent.getActivities(context, notificationId, new Intent[]{mainIntent, friendRequestListIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
        String tag = "wfc friendRequest notification tag";
        showNotification(context, tag, notificationId, title, text, pendingIntent);

    }

    private List<Conversation> notificationConversations = new ArrayList<>();

    private int notificationId(Conversation conversation) {
        if (!notificationConversations.contains(conversation)) {
            notificationConversations.add(conversation);
        }
        return notificationConversations.indexOf(conversation);
    }
}
