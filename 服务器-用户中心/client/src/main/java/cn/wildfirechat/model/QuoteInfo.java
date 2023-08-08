/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfirechat.model;

import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import cn.wildfirechat.message.Message;
import cn.wildfirechat.remote.ChatManager;

public class QuoteInfo implements Parcelable {
    private long messageUid;
    private String userId;
    private String userDisplayName;
    private String messageDigest;
    private long messageId;

    public String getMessageHash() {
        return messageHash;
    }

    public void setMessageHash(String messageHash) {
        this.messageHash = messageHash;
    }

    public String getMessageHash0() {
        return messageHash0;
    }

    public void setMessageHash0(String messageHash0) {
        this.messageHash0 = messageHash0;
    }

    private String messageHash;
    private String messageHash0;

    public long getMessageUid() {
        return messageUid;
    }

    public long getMessageId(){
        return messageId;
    }

    public void setMessageId(long messageId){
        this.messageId = messageId;
    }

    public void setMessageUid(long messageUid) {
        this.messageUid = messageUid;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserDisplayName() {
        return userDisplayName;
    }

    public void setUserDisplayName(String userDisplayName) {
        this.userDisplayName = userDisplayName;
    }

    public String getMessageDigest() {
        return messageDigest;
    }

    public void setMessageDigest(String messageDigest) {
        this.messageDigest = messageDigest;
    }
    public static QuoteInfo initWithMessage(Message message, Activity activity) {
        QuoteInfo info = new QuoteInfo();
        if (message != null) {
            info.messageUid = message.messageUid;
            info.messageId = message.messageId;
            info.userId = message.sender;

            info.messageHash = message.messageHash;
            info.messageHash0 = message.messageHash0;
            UserInfo userInfo = ChatManager.Instance().getUserInfo(message.sender, false);
            info.userDisplayName = userInfo.displayName;
            info.messageDigest = message.content.digest(message, activity);
            if (info.messageDigest.length() > 48) {
                info.messageDigest = info.messageDigest.substring(0, 48);
            }
        }
        return info;

    }

//    public static QuoteInfo initWithMessage(long messageUid) {
//        Message message = ChatManager.Instance().getMessageByUid(messageUid);
//        return initWithMessage(message, null);
//    }

    public JSONObject encode() {
        JSONObject object = new JSONObject();
        try {
            object.put("u", messageUid);
            object.put("i", userId);
            object.put("n", userDisplayName);
            object.put("d", messageDigest);
            object.put("hash",messageHash);
            object.put("hash0",messageHash0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    public void decode(JSONObject object) {
        messageUid = object.optLong("u");
        userId = object.optString("i");
        userDisplayName = object.optString("n");
        messageDigest = object.optString("d");
        messageHash = object.optString("hash");
        messageHash0 = object.optString("hash0");
    }

    public void setQuoteInfo(long u, String i, String n, String d,String hash,String hash0){
        messageUid = u;
        userId = i;
        userDisplayName = n;
        messageDigest = d;
        messageHash = hash;
        messageHash0 = hash0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.messageUid);
        dest.writeString(this.userId);
        dest.writeString(this.userDisplayName);
        dest.writeString(this.messageDigest);
        dest.writeLong(this.messageId);
        dest.writeString(this.messageHash);
        dest.writeString(this.messageHash0);
    }

    public QuoteInfo() {
    }

    protected QuoteInfo(Parcel in) {
        this.messageUid = in.readLong();
        this.userId = in.readString();
        this.userDisplayName = in.readString();
        this.messageDigest = in.readString();
        this.messageId = in.readLong();
        this.messageHash0 = in.readString();
        this.messageHash = in.readString();
    }

    public static final Creator<QuoteInfo> CREATOR = new Creator<QuoteInfo>() {
        @Override
        public QuoteInfo createFromParcel(Parcel source) {
            return new QuoteInfo(source);
        }

        @Override
        public QuoteInfo[] newArray(int size) {
            return new QuoteInfo[size];
        }
    };
}
