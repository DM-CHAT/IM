/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfirechat.message.notification;

import android.app.Activity;
import android.content.Context;
import android.os.Parcel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.wildfirechat.client.R;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.core.ContentTag;
import cn.wildfirechat.message.core.MessagePayload;
import cn.wildfirechat.message.core.PersistFlag;
import cn.wildfirechat.remote.ChatManager;

import static cn.wildfirechat.message.core.MessageContentType.ContentType_ADD_GROUP_MEMBER;

/**
 * Created by heavyrainlee on 20/12/2017.
 */

@ContentTag(type = ContentType_ADD_GROUP_MEMBER, flag = PersistFlag.Persist)
public class AddGroupMemberNotificationContent extends GroupNotificationMessageContent {
    public String invitor;
    public List<String> invitees;

    public AddGroupMemberNotificationContent() {
    }

    @Override
    public String formatNotification(Message message, Context context) {
        StringBuilder sb = new StringBuilder();
//        if (invitees.size() == 1 && invitees.get(0).equals(invitor)) {
//            if (ChatManager.Instance().getUserId().equals(invitor)) {
//                sb.append(activity.getString(R.string.you_joined_group));
//            } else {
//                sb.append(ChatManager.Instance().getGroupMemberDisplayName(groupId, invitor));
//                sb.append(activity.getString(R.string.joined_group));
//            }
//            return sb.toString();
//        }
        if(invitees.size() == 0)
            return "invitees empty";
        if (invitor == null || invitor.isEmpty()) {
            if (ChatManager.Instance().getUserId().equals(invitees.get(0))) {
                sb.append(context.getString(R.string.you_joined_group));
            } else {
                for(String member : invitees){
                    sb.append(ChatManager.Instance().getGroupMemberDisplayName(groupId, member));
                    sb.append(" ");
                }
                sb.append(context.getString(R.string.joined_group));
            }
            return sb.toString();
        }
        if (fromSelf) {
            sb.append(context.getString(R.string.you_invite));
        } else {
            sb.append(ChatManager.Instance().getGroupMemberDisplayName(groupId, invitor));
            sb.append(context.getString(R.string.invite));
        }

        if (invitees != null) {
            for (String member : invitees) {
                sb.append(" ");
                sb.append(ChatManager.Instance().getGroupMemberDisplayName(groupId, member));
            }
        }

        sb.append(context.getString(R.string.joined_group1));
        return sb.toString();
    }

    @Override
    public MessagePayload encode() {
        MessagePayload payload = super.encode();
        try {
            JSONObject objWrite = new JSONObject();
            objWrite.put("g", groupId);
            objWrite.put("o", invitor);
            JSONArray objArray = new JSONArray();
            for (int i = 0; i < invitees.size(); i++) {
                objArray.put(i, invitees.get(i));
            }
            objWrite.put("ms", objArray);

            payload.binaryContent = objWrite.toString().getBytes();
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
                invitor = jsonObject.optString("o");
                groupId = jsonObject.optString("g");
                JSONArray jsonArray = jsonObject.optJSONArray("ms");
                invitees = new ArrayList<>();
                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        invitees.add(jsonArray.getString(i));
                    }
                }
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
        dest.writeString(this.invitor);
        dest.writeStringList(this.invitees);
    }

    protected AddGroupMemberNotificationContent(Parcel in) {
        super(in);
        this.invitor = in.readString();
        this.invitees = in.createStringArrayList();
    }

    public static final Creator<AddGroupMemberNotificationContent> CREATOR = new Creator<AddGroupMemberNotificationContent>() {
        @Override
        public AddGroupMemberNotificationContent createFromParcel(Parcel source) {
            return new AddGroupMemberNotificationContent(source);
        }

        @Override
        public AddGroupMemberNotificationContent[] newArray(int size) {
            return new AddGroupMemberNotificationContent[size];
        }
    };
}
