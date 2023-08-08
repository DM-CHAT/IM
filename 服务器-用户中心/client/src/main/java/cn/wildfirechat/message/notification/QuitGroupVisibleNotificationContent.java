/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfirechat.message.notification;

import android.app.Activity;
import android.content.Context;
import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import cn.wildfirechat.client.ClientService;
import cn.wildfirechat.client.R;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.core.ContentTag;
import cn.wildfirechat.message.core.MessagePayload;
import cn.wildfirechat.message.core.PersistFlag;
import cn.wildfirechat.remote.ChatManager;

import static cn.wildfirechat.message.core.MessageContentType.ContentType_QUIT_GROUP_VISIABLE;

/**
 * Created by heavyrainlee on 20/12/2017.
 */

@ContentTag(type = ContentType_QUIT_GROUP_VISIABLE, flag = PersistFlag.Persist)
public class QuitGroupVisibleNotificationContent extends GroupNotificationMessageContent {
    public String operator;

    public QuitGroupVisibleNotificationContent() {
    }

    @Override
    public String formatNotification(Message message, Context context) {
        StringBuilder sb = new StringBuilder();
        if (fromSelf) {
            sb.append(context.getString(R.string.you_quit_group));
        } else {
            sb.append(ChatManager.Instance().getGroupMemberDisplayName(groupId, operator));
            sb.append(context.getString(R.string.quit_group));
        }

        return sb.toString();
    }

    @Override
    public MessagePayload encode() {
        MessagePayload payload = super.encode();

        try {
            JSONObject objWrite = new JSONObject();
            objWrite.put("g", groupId);
            objWrite.put("o", operator);
            payload.binaryContent = objWrite.toString().getBytes();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return payload;
    }

    @Override
    public void decode(MessagePayload payload) {
        try {
            if (payload.content != null) {
                JSONObject jsonObject = new JSONObject(new String(payload.binaryContent));
                groupId = jsonObject.optString("g");
                operator = jsonObject.optString("o");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.operator);
    }

    protected QuitGroupVisibleNotificationContent(Parcel in) {
        super(in);
        this.operator = in.readString();
    }

    public static final Creator<QuitGroupVisibleNotificationContent> CREATOR = new Creator<QuitGroupVisibleNotificationContent>() {
        @Override
        public QuitGroupVisibleNotificationContent createFromParcel(Parcel source) {
            return new QuitGroupVisibleNotificationContent(source);
        }

        @Override
        public QuitGroupVisibleNotificationContent[] newArray(int size) {
            return new QuitGroupVisibleNotificationContent[size];
        }
    };
}
