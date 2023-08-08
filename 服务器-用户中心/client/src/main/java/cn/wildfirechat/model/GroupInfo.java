/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfirechat.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by heavyrainlee on 17/12/2017.
 */


public class GroupInfo implements Parcelable {
    public enum GroupType {
        //member can add quit change group name and portrait, owner can do all the operations
        Normal(0),
        //every member can add quit change group name and portrait, no one can kickoff others
        Free(1),
        //member can only quit, owner can do all the operations
        Restricted(2);

        private int value;

        GroupType(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }

        public static GroupType type(int type) {
            if (type >= 0 && type < GroupType.values().length) {
                return GroupType.values()[type];
            }

            throw new IllegalArgumentException("GroupType " + type + " is invalid");
        }
    }

    public String target;
    public String name;
    public String portrait;
    public String owner;
    public GroupType type;
    public int memberCount;
    public String extra;
    public String notice = "";
    public long updateDt;
    public int fav;
    public int showAlias;
    public int redPacket;
    public String attribute;
    public String timeInterval;
    public String isMember;

    //0 正常；1 全局禁言
    public int mute;

    //在group type为Restricted时，0 开放加入权限（群成员可以拉人，用户也可以主动加入）；1 只能群成员拉人入群；2 只能群管理拉人入群
    public int joinType;

    //0-无验证 1-群组验证 2-管理验证
    public int passType;

    //是否运行群中普通成员私聊。0 允许，1不允许
    public int privateChat;

    //是否可以搜索到该群，功能暂未实现
    public int searchable;

    //是否可以查看群历史消息, 0 不允许，1允许。仅专业版有效
    public int historyMessage;

    //群最大成员数。仅专业版有效
    public int maxMemberCount;

    public GroupInfo() {
    }

    public boolean AllowAddFriend() {

        if (attribute == null) {
            // 允许
            return true;
        }
        try {
            JSONObject attrJson = JSON.parseObject(attribute);
            String allowAddFriend = attrJson.getString("AllowAddFriend");
            if (allowAddFriend != null) {
                if (allowAddFriend.equals("no")) {
                    return false;
                }
            }
        } catch (Exception e) {

        }


        return true;
    }

    public String getJoinType() {

        String str = getExtra("joinType");
        if (str == null) {
            return "free";
        }
        return str;
    }

    public boolean getBombEnable() {
        System.out.println("@@@ attribute : " + attribute);
        if (attribute == null) {
            return false;
        }
        try {
            JSONObject attrJson = JSON.parseObject(attribute);
            String allowAddFriend = attrJson.getString("BombEnable");
            if (allowAddFriend != null) {
                if (allowAddFriend.equals("yes")) {
                    return true;
                }
            }
        } catch (Exception e) {

        }


        return false;
    }

    public boolean isGroupForward() {

        if (attribute == null) {
            // 允许
            return true;
        }
        try {
            JSONObject attrJson = JSON.parseObject(attribute);
            String allowAddFriend = attrJson.getString("isGroupForward");
            if (allowAddFriend != null) {
                if (allowAddFriend.equals("no")) {
                    return false;
                }
            }
        } catch (Exception e) {

        }


        return true;
    }

    public boolean isGroupCopy() {

        if (attribute == null) {
            // 允许
            return true;
        }
        try {
            JSONObject attrJson = JSON.parseObject(attribute);
            String allowAddFriend = attrJson.getString("isGroupCopy");
            if (allowAddFriend != null) {
                if (allowAddFriend.equals("no")) {
                    return false;
                }
            }
        } catch (Exception e) {

        }


        return true;
    }

    public String getClearTimes() {

        if (attribute == null) {
            // 允许
            return "0";
        }
        try {
            JSONObject attrJson = JSON.parseObject(attribute);
            String allowAddFriend = attrJson.getString("clearTimes");
            if (allowAddFriend != null) {
                return allowAddFriend;
            }
            return "0";
        } catch (Exception e) {

        }
        return "0";
    }

    public String getTopMessage(String key){
        if (attribute == null) {
            // 允许
            return null;
        }
        try {
            JSONObject attrJson = JSON.parseObject(attribute);
            String allowAddFriend = attrJson.getString(key);
            if (allowAddFriend != null) {
                return allowAddFriend;
            }
            return null;
        } catch (Exception e) {

        }
        return null;
    }

    public List<String> getTopList() {
        List<String> list = new ArrayList<>();
        if (attribute == null) {
            // 允许
            return list;
        }
        try {
            Gson gson = new Gson();
            Map<String, String> map = gson.fromJson(attribute, Map.class);
            for (String key : map.keySet()){
                String keyStr = key;
                //String value = map.get(keyStr);
                if (keyStr.contains("top_")){
                    list.add(keyStr);
                }
            }
            return list;
        } catch (Exception e) {

        }
        return list;


    }

    public int getTimeInterval() {

        // 转int
        try {
            String timeSec = getAttribute("TimeInterval");
            int time = Integer.valueOf(timeSec);
            return time;
        }catch (Exception e){

        }
        return 0;
    }

    public String getAttribute(String key) {
        if (attribute == null) {
            // 允许
            return null;
        }
        try {
            JSONObject attrJson = JSON.parseObject(attribute);
            return attrJson.getString(key);
        } catch (Exception e) {

        }
        return null;
    }

    public String getExtra(String key) {
        if (extra == null) {
            // 允许
            return null;
        }
        try {
            JSONObject attrJson = JSON.parseObject(extra);
            return attrJson.getString(key);
        } catch (Exception e) {

        }
        return null;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.target);
        dest.writeString(this.name);
        dest.writeString(this.portrait);
        dest.writeString(this.attribute);
        dest.writeString(this.timeInterval);
        dest.writeString(this.owner);
        dest.writeInt(this.type == null ? -1 : this.type.ordinal());
        dest.writeInt(this.memberCount);
        dest.writeString(this.extra);
        dest.writeLong(this.updateDt);
        dest.writeInt(this.mute);
        dest.writeInt(this.joinType);
        dest.writeInt(this.passType);
        dest.writeInt(this.privateChat);
        dest.writeInt(this.searchable);
        dest.writeInt(this.historyMessage);
        dest.writeInt(this.maxMemberCount);
        dest.writeInt(this.fav);
        dest.writeInt(this.showAlias);
        dest.writeInt(this.redPacket);
        dest.writeString(this.notice);
        dest.writeString(this.isMember);
    }

    protected GroupInfo(Parcel in) {
        this.target = in.readString();
        this.name = in.readString();
        this.portrait = in.readString();
        this.attribute = in.readString();
        this.timeInterval = in.readString();
        this.owner = in.readString();
        int tmpType = in.readInt();
        this.type = tmpType == -1 ? null : GroupType.values()[tmpType];
        this.memberCount = in.readInt();
        this.extra = in.readString();
        this.updateDt = in.readLong();
        this.mute = in.readInt();
        this.joinType = in.readInt();
        this.passType = in.readInt();
        this.privateChat = in.readInt();
        this.searchable = in.readInt();
        this.historyMessage = in.readInt();
        this.maxMemberCount = in.readInt();
        this.fav = in.readInt();
        this.showAlias = in.readInt();
        this.redPacket = in.readInt();
        this.notice = in.readString();
        this.isMember = in.readString();
    }

    public JSONObject getPublicGroupInfo() {
        JSONObject json = new JSONObject();
        json.put("groupID", target);
        json.put("name", name);
        json.put("portrait", portrait);
        json.put("owner", owner);
        json.put("memberCount", memberCount);
        json.put("attribute", attribute);

        return json;



    }

    public static final Creator<GroupInfo> CREATOR = new Creator<GroupInfo>() {
        @Override
        public GroupInfo createFromParcel(Parcel source) {
            return new GroupInfo(source);
        }

        @Override
        public GroupInfo[] newArray(int size) {
            return new GroupInfo[size];
        }
    };
}
