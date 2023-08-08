/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfirechat.message.notification;

import android.app.Activity;
import android.content.Context;
import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.core.ContentTag;
import cn.wildfirechat.message.core.MessagePayload;
import cn.wildfirechat.message.core.PersistFlag;

import static cn.wildfirechat.message.core.MessageContentType.ContentType_General_Notification;

/**
 * Created by heavyrainlee on 20/12/2017.
 */

@ContentTag(type = ContentType_General_Notification, flag = PersistFlag.Persist)
public class GroupNotifyContent extends GroupNotificationMessageContent {
    public String info;

    public GroupNotifyContent() {
    }

    @Override
    public String formatNotification(Message message, Context context) {
        return info;
    }

    @Override
    public MessagePayload encode() {
        MessagePayload payload = super.encode();
        try {
            JSONObject objWrite = new JSONObject();
            objWrite.put("g", groupId);
            objWrite.put("o", info);
            return payload;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void decode(MessagePayload payload) {
        try {
            if (payload.binaryContent != null) {
                JSONObject jsonObject = new JSONObject(new String(payload.binaryContent));
                info = jsonObject.optString("o");
                groupId = jsonObject.optString("g");
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
        dest.writeString(this.info);
    }

    protected GroupNotifyContent(Parcel in) {
        super(in);
        this.info = in.readString();
    }

    public static final Creator<GroupNotifyContent> CREATOR = new Creator<GroupNotifyContent>() {
        @Override
        public GroupNotifyContent createFromParcel(Parcel source) {
            return new GroupNotifyContent(source);
        }

        @Override
        public GroupNotifyContent[] newArray(int size) {
            return new GroupNotifyContent[size];
        }
    };
}
