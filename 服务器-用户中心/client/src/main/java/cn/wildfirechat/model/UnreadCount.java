/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfirechat.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UnreadCount implements Parcelable {
//    public UnreadCount(ProtoUnreadCount protocolUnreadCount) {
//        this.unread = protocolUnreadCount.getUnread();
//        this.unreadMention = protocolUnreadCount.getUnreadMention();
//        this.unreadMentionAll = protocolUnreadCount.getUnreadMentionAll();
//    }

    public UnreadCount() {
        unread = 0;
        unreadMention = 0;
        unreadMentionAll = 0;
        unreadFriends=0;
        unreadGroup=0;
        unreadkefu=0;
        index = 0;
        tagId = 0;
        getUnreadObj(-1);
        getUnreadObj(-2);
    }

    public UnreadCount(Map<Integer, OrgTag> tagMap) {
        this();

        /*for (Integer i : tagMap.keySet()) {
            getUnreadObj(tagMap.get(i).tagId);
        }*/

    }

    public boolean hasUnread(){
        return unread != 0 || unreadMention != 0 || unreadMentionAll != 0;
    }

    public int unreadFriends;
    public int unreadGroup;
    public int unreadkefu;

    public Map<Integer, UnreadObj> unreadMap = new HashMap<>();




    /**
     * 单聊未读数
     */
    public int unread;
    /**
     * 群聊@数
     */
    public int unreadMention;
    /**
     * 群聊@All数
     */
    public int unreadMentionAll;

    public int tagId;

    public int index;


    public class UnreadObj{
        public int unread;
        public int unreadMention;
    }

    public void printMap(){

        Set<Integer> keys = unreadMap.keySet();
    }

    public UnreadObj getUnreadObj(int tag) {
        UnreadObj obj = unreadMap.get(tag);
        if (obj == null) {
            obj = new UnreadObj();
            unreadMap.put(tag, obj);
        }
        return obj;
    }

    public void ZeroUnread() {
        for (Integer i : unreadMap.keySet()){
            UnreadObj value = unreadMap.get(i);
            if (value != null){
                value.unread = 0;
                value.unreadMention = 0;
            }
        }
    }


    public void clearUnreadMap() {
        unreadMap.clear();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.unread);
        dest.writeInt(this.unreadMention);
        dest.writeInt(this.unreadMentionAll);
        dest.writeInt(this.unreadFriends);
        dest.writeInt(this.unreadGroup);
        dest.writeInt(this.unreadkefu);
        dest.writeInt(this.index);
        dest.writeInt(this.tagId);

    }

    protected UnreadCount(Parcel in) {
        this.unread = in.readInt();
        this.unreadMention = in.readInt();
        this.unreadMentionAll = in.readInt();
        this.unreadFriends = in.readInt();
        this.unreadGroup = in.readInt();
        this.unreadkefu = in.readInt();
        this.index = in.readInt();
        this.tagId = in.readInt();
    }

    public static final Creator<UnreadCount> CREATOR = new Creator<UnreadCount>() {
        @Override
        public UnreadCount createFromParcel(Parcel source) {
            return new UnreadCount(source);
        }

        @Override
        public UnreadCount[] newArray(int size) {
            return new UnreadCount[size];
        }
    };
}
