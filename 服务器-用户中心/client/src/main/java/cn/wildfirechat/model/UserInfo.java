/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfirechat.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by heavyrainlee on 14/12/2017.
 */

public class UserInfo implements Parcelable, Comparable<UserInfo> {
    public String uid;
    public String name;
    public String displayName;
    // 用户在群里面给自己设置的备注，不同群不一样
    public String groupAlias;
    // 我为好友设置的备注
    public String friendAlias;
    public String portrait;
    public String urlSpace;
    public int gender;
    public String mobile;
    public String email;
    public String address;
    public String company;
    public String social;
    public String extra;
    //public String nft = "";
    public long updateDt;
    //0 normal; 1 robot; 2 thing;
    public int type;
    //0 normal; 1 deleted;
    public int deleted;
    public int payState;
    public String role;
    public String describes;

    public String getNft(){

        if (describes == null) {
            return null;
        }

        try {
            JSONObject desJson = JSONObject.parseObject(describes);
            String nft2 = desJson.getString("nft");
            if (nft2 == null) {
                return null;
            }

            if (nft2.startsWith("http")){
                //nft = nft2;
                return nft2;
            }

        } catch (Exception e) {

        }

        return null;
    }

    public String getAllowTemporaryChat(){

        if (describes == null) {
            return null;
        }

        try {
            JSONObject desJson = JSONObject.parseObject(describes);
            String nft2 = desJson.getString("AllowTemporaryChat");
            if (nft2 == null) {
                return null;
            }

            return nft2;

        } catch (Exception e) {

        }

        return null;
    }

    public UserInfo() {
    }
    public UserInfo(String uid){
        this.uid = uid;
    }


    @Override
    public int compareTo(@NonNull UserInfo userInfo) {
        return displayName.compareTo(userInfo.displayName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uid);
        dest.writeString(this.name);
        dest.writeString(this.displayName);
        dest.writeString(this.groupAlias);
        dest.writeString(this.friendAlias);
        dest.writeString(this.portrait);
        dest.writeString(this.urlSpace);
        dest.writeInt(this.gender);
        dest.writeString(this.mobile);
        dest.writeString(this.email);
        dest.writeString(this.address);
        dest.writeString(this.company);
        dest.writeString(this.social);
        dest.writeString(this.extra);
        //dest.writeString(this.nft);
        dest.writeLong(this.updateDt);
        dest.writeInt(this.type);
        dest.writeInt(this.deleted);
        dest.writeInt(this.payState);
        dest.writeString(this.role);
        dest.writeString(this.describes);
    }

    protected UserInfo(Parcel in) {
        this.uid = in.readString();
        this.name = in.readString();
        this.displayName = in.readString();
        this.groupAlias = in.readString();
        this.friendAlias = in.readString();
        this.portrait = in.readString();
        this.urlSpace = in.readString();
        this.gender = in.readInt();
        this.mobile = in.readString();
        this.email = in.readString();
        this.address = in.readString();
        this.company = in.readString();
        this.social = in.readString();
        this.extra = in.readString();
        //this.nft = in.readString();
        this.updateDt = in.readLong();
        this.type = in.readInt();
        this.deleted = in.readInt();
        this.payState = in.readInt();
        this.role = in.readString();
        this.describes = in.readString();
    }

    public String getRole(String key) {
        if (role != null) {
            JSONObject roleJson = JSONObject.parseObject(role);
            return roleJson.getString(key);
        }

        return  null;
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel source) {
            return new UserInfo(source);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserInfo userInfo = (UserInfo) o;

        if (updateDt != userInfo.updateDt) return false;
        if (type != userInfo.type) return false;
        return uid.equals(userInfo.uid);
    }

    @Override
    public int hashCode() {
        int result = uid.hashCode();
        result = 31 * result + (int) (updateDt ^ (updateDt >>> 32));
        result = 31 * result + type;
        return result;
    }
}
