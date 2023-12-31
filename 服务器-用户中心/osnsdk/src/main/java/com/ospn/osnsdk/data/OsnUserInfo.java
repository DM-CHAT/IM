package com.ospn.osnsdk.data;

import com.alibaba.fastjson.JSONObject;

public class OsnUserInfo {
    public String userID;
    public String name;
    public String displayName;
    public String portrait;
    public String urlSpace;
    public String describes;
    public String role;

    public static OsnUserInfo toUserInfo(JSONObject json){
        OsnUserInfo userInfo = new OsnUserInfo();
        userInfo.userID = json.getString("userID");
        userInfo.name = json.getString("name");
        userInfo.displayName = json.getString("displayName");
        userInfo.portrait = json.getString("portrait");
        userInfo.urlSpace = json.getString("urlSpace");
        userInfo.describes = json.getString("describes");
        userInfo.role = json.getString("role");
        return userInfo;
    }
}
