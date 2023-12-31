/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfirechat.message.notification;

import android.app.Activity;
import android.content.Context;
import android.os.Parcel;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import cn.wildfirechat.client.ClientService;
import cn.wildfirechat.client.R;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.core.ContentTag;
import cn.wildfirechat.message.core.MessagePayload;
import cn.wildfirechat.message.core.PersistFlag;
import cn.wildfirechat.remote.ChatManager;

import static cn.wildfirechat.message.core.MessageContentType.ContentType_MODIFY_GROUP_ALIAS;

/**
 * Created by heavyrainlee on 20/12/2017.
 */

@ContentTag(type = ContentType_MODIFY_GROUP_ALIAS, flag = PersistFlag.Persist)
public class ModifyGroupAliasNotificationContent extends GroupNotificationMessageContent {
    public String operateUser;
    public String alias;
    public String memberId;

    public ModifyGroupAliasNotificationContent() {
    }

    @Override
    public String formatNotification(Message message, Context context) {
        StringBuilder sb = new StringBuilder();
        if (fromSelf) {
            sb.append(context.getString(R.string.you));
        } else {
            sb.append(ChatManager.Instance().getUserDisplayName(operateUser));
        }
        sb.append(context.getString(R.string.modify));
        if(!TextUtils.isEmpty(memberId) && !memberId.equals(operateUser)) {
            sb.append(ChatManager.Instance().getUserDisplayName(memberId));
            sb.append(context.getString(R.string.de));
        }
        sb.append(context.getString(R.string.group_card));
        sb.append(alias);

        return sb.toString();
    }

    @Override
    public MessagePayload encode() {
        MessagePayload payload = super.encode();

        try {
            JSONObject objWrite = new JSONObject();
            objWrite.put("g", groupId);
            objWrite.put("o", operateUser);
            objWrite.put("n", alias);
            if(!TextUtils.isEmpty(memberId)) {
                objWrite.put("m", memberId);
            }

            payload.binaryContent = objWrite.toString().getBytes();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return payload;
    }

    @Override
    public void decode(MessagePayload payload) {
        try {
            if (payload.binaryContent != null) {
                JSONObject jsonObject = new JSONObject(new String(payload.binaryContent));
                groupId = jsonObject.optString("g");
                operateUser = jsonObject.optString("o");
                alias = jsonObject.optString("n");
                memberId = jsonObject.optString("m");
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
        dest.writeString(this.operateUser);
        dest.writeString(this.alias);
    }

    protected ModifyGroupAliasNotificationContent(Parcel in) {
        super(in);
        this.operateUser = in.readString();
        this.alias = in.readString();
    }

    public static final Creator<ModifyGroupAliasNotificationContent> CREATOR = new Creator<ModifyGroupAliasNotificationContent>() {
        @Override
        public ModifyGroupAliasNotificationContent createFromParcel(Parcel source) {
            return new ModifyGroupAliasNotificationContent(source);
        }

        @Override
        public ModifyGroupAliasNotificationContent[] newArray(int size) {
            return new ModifyGroupAliasNotificationContent[size];
        }
    };
}
