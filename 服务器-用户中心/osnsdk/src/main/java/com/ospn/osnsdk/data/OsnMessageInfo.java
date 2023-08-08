package com.ospn.osnsdk.data;

import com.alibaba.fastjson.JSONObject;

public class OsnMessageInfo {
    public String userID;
    public String target;
    public String content;
    public long timeStamp;
    public boolean isGroup;
    public String originalUser;
    public String hash;
    public String hash0;
    public String mentionedType;
    public String mentionedTargets;

    public static OsnMessageInfo toMessage(JSONObject data){
        OsnMessageInfo messageInfo = new OsnMessageInfo();
        messageInfo.userID = data.getString("receive_from");
        messageInfo.target = data.getString("receive_to");
        messageInfo.timeStamp = data.getLong("receive_timestamp");
        messageInfo.content = data.getString("content");
        messageInfo.mentionedTargets = data.getString("mentionedTargets");
        messageInfo.mentionedType = data.getString("mentionedType");
        if(messageInfo.userID != null)
            messageInfo.isGroup = messageInfo.userID.startsWith("OSNG");
        else
            messageInfo.isGroup = false;
        messageInfo.originalUser = data.getString("originalUser");
        messageInfo.hash = data.getString("receive_hash");
        messageInfo.hash0 = data.getString("hash0");
        return messageInfo;
    }
}
