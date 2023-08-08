package com.ospn.osnsdk.callback;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ospn.osnsdk.data.OsnFriendInfo;
import com.ospn.osnsdk.data.OsnGroupInfo;
import com.ospn.osnsdk.data.OsnMessageInfo;
import com.ospn.osnsdk.data.OsnRequestInfo;
import com.ospn.osnsdk.data.OsnUserInfo;
import com.ospn.osnsdk.data.serviceInfo.OsnLitappInfo;
import com.ospn.osnsdk.data.serviceInfo.OsnServiceInfo;

import java.io.OutputStream;
import java.util.List;

public interface OSNListener {
    void onConnectSuccess (String state);
    void onConnectFailed (String error);
    void onSetMessage(OsnMessageInfo msgInfo);
    List<String> onRecvMessage (List<OsnMessageInfo> msgList);
    void onRecvRequest(OsnRequestInfo request);
    void onFriendUpdate(List<OsnFriendInfo> friendList); //friend为本地数据，使用list
    void onFriendsUpdate(List<OsnFriendInfo> friendList);
    void onUserUpdate(OsnUserInfo userInfo, List<String> keys);
    void onGroupUpdate(String state, OsnGroupInfo groupInfo, List<String> keys);
    void onServiceInfo(List<OsnServiceInfo> infos);
    void onFindResult(List<OsnLitappInfo> infos);
    void onReceiveRecall(JSONObject data);
    void onDeleteMessageTo(JSONObject data);
    void onAllowGroupAddFriend(JSONObject data);
    void onGroupInfo(JSONObject data);
    void onUserInfo(JSONObject data);
    void cleanGroupFav();


    void logInfo(String text);
    String getConfig(String key);
    void setConfig(String key, String value);
}
