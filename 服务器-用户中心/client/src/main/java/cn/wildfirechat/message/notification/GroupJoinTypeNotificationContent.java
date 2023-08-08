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

import static cn.wildfirechat.message.core.MessageContentType.CONTENT_TYPE_CHANGE_JOINTYPE;

@ContentTag(type = CONTENT_TYPE_CHANGE_JOINTYPE, flag = PersistFlag.Persist)
public class GroupJoinTypeNotificationContent extends GroupNotificationMessageContent {
    public String operator;

    //在group type为Restricted时，0 开放加入权限（群成员可以拉人，用户也可以主动加入）；1 只能群成员拉人入群；2 只能群管理拉人入群
    public int type;

    @Override
    public String formatNotification(Message message, Context context) {
        StringBuilder sb = new StringBuilder();
        if (fromSelf) {
            sb.append(context.getString(R.string.you));
        } else {
            sb.append(ChatManager.Instance().getGroupMemberDisplayName(groupId, operator));
        }
        switch (type) {
            case 0:
                sb.append(context.getString(R.string.open_group_join));
                break;
            case 1:
                sb.append(context.getString(R.string.invite_group_join));
                break;
            case 2:
                sb.append(context.getString(R.string.close_group_join));
                break;
            default:
                break;
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
            objWrite.put("n", type + "");
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
                operator = jsonObject.optString("o");
                type = Integer.parseInt(jsonObject.optString("n", "0"));
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
        dest.writeInt(this.type);
    }

    public GroupJoinTypeNotificationContent() {
    }

    protected GroupJoinTypeNotificationContent(Parcel in) {
        super(in);
        this.operator = in.readString();
        this.type = in.readInt();
    }

    public static final Creator<GroupJoinTypeNotificationContent> CREATOR = new Creator<GroupJoinTypeNotificationContent>() {
        @Override
        public GroupJoinTypeNotificationContent createFromParcel(Parcel source) {
            return new GroupJoinTypeNotificationContent(source);
        }

        @Override
        public GroupJoinTypeNotificationContent[] newArray(int size) {
            return new GroupJoinTypeNotificationContent[size];
        }
    };
}
