package com.ospn.osnsdk.data;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OsnGroupInfo {
    public String groupID;
    public String name;
    public String privateKey;
    public String owner;
    public String portrait;
    public String attribute;
    public String billboard;
    public String invitor;
    public String approver;
    public String isMember;
    public String extra;
    public JSONObject data;
    public JSONObject notice;
    public long noticeServerTime = 0;
    public int memberCount;
    public int type;
    public int joinType;
    public int passType;
    public int mute;
    public int singleMute = 0;
    public List<OsnMemberInfo> userList;

    public OsnGroupInfo(){
        userList = new ArrayList<>();
    }
    public static OsnGroupInfo toGroupInfo(JSONObject data){
        OsnGroupInfo groupInfo = new OsnGroupInfo();
        groupInfo.groupID = data.getString("groupID");
        if(groupInfo.groupID == null)
            groupInfo.groupID = data.getString("receive_from");
        groupInfo.data = data;
        groupInfo.name = data.getString("name");
        groupInfo.privateKey = "";
        groupInfo.owner = data.getString("owner");
        groupInfo.type = data.getIntValue("type");
        groupInfo.joinType = data.getIntValue("joinType");
        groupInfo.passType = data.getIntValue("passType");
        groupInfo.mute = data.getIntValue("mute");
        groupInfo.portrait = data.getString("portrait");
        groupInfo.attribute = data.getString("attribute");
        groupInfo.billboard = data.getString("billboard");
        groupInfo.memberCount = data.getIntValue("memberCount");
        groupInfo.invitor = data.getString("invitor");
        groupInfo.approver = data.getString("approver");
        groupInfo.isMember = data.getString("isMember");
        groupInfo.extra = data.getString("extra");
        JSONArray array = data.getJSONArray("userList");
        if(array != null) {
            for (Object o : array) {
                OsnMemberInfo memberInfo;
                if(o instanceof String){
                    memberInfo = new OsnMemberInfo();
                    memberInfo.osnID = (String)o;
                    memberInfo.groupID = groupInfo.groupID;
                }
                else{
                    JSONObject m = (JSONObject) o;
                    m.put("groupID", groupInfo.groupID);
                    memberInfo = OsnMemberInfo.toMemberInfo(m);
                }
                groupInfo.userList.add(memberInfo);
            }
        }
        if (data.containsKey("singleMute")){
            groupInfo.singleMute = data.getIntValue("singleMute");
        }

        return groupInfo;
    }
    public OsnMemberInfo hasMember(String osnID){
        for(OsnMemberInfo m:userList){
            if(m.osnID.equalsIgnoreCase(osnID))
                return m;
        }
        return null;
    }
    public int isMember() {
        if (isMember != null) {
            if (isMember.equalsIgnoreCase("yes")) {
                return 1;
            }
        }

        return 0;
    }

    public long genGroupNoticeTime() {
        if (noticeServerTime == 0) {
            return System.currentTimeMillis();
        }
        return noticeServerTime;
    }
}
