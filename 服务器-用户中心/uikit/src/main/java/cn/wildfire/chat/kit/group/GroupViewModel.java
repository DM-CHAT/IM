/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */
package cn.wildfire.chat.kit.group;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import cn.wildfire.chat.kit.ChatManagerHolder;
import cn.wildfire.chat.kit.GlideApp;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.common.OperateResult;
import cn.wildfire.chat.kit.contact.model.UIUserInfo;
import cn.wildfire.chat.kit.conversation.forward.ForwardActivity;
import cn.wildfire.chat.kit.info.AddanApletsInfo;
import cn.wildfire.chat.kit.litapp.AddanApletsActivity;
import cn.wildfire.chat.kit.litapp.AddanApletsAdapter;
import cn.wildfire.chat.kit.user.UserViewModel;
import cn.wildfire.chat.kit.utils.OssHelper;
import cn.wildfire.chat.kit.utils.OssHelperGroup;
import cn.wildfire.chat.kit.utils.PinyinUtils;
import cn.wildfire.chat.kit.utils.ProgresDialog;
import cn.wildfire.chat.kit.utils.SPUtils;
import cn.wildfire.chat.kit.utils.portrait.CombineBitmapTools;
import cn.wildfirechat.message.MessageContent;
import cn.wildfirechat.message.MessageContentMediaType;
import cn.wildfirechat.message.notification.NotificationMessageContent;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.GroupMember;
import cn.wildfirechat.model.ModifyGroupInfoType;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback;
import cn.wildfirechat.remote.GeneralCallback2;
import cn.wildfirechat.remote.GetGroupInfoCallback;
import cn.wildfirechat.remote.GetGroupMembersCallback;
import cn.wildfirechat.remote.GetGroupsCallback;
import cn.wildfirechat.remote.OnGroupInfoUpdateListener;
import cn.wildfirechat.remote.OnGroupMembersUpdateListener;
import cn.wildfirechat.remote.UploadMediaCallback;
import cn.wildfirechat.remote.UserSettingScope;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GroupViewModel extends ViewModel implements OnGroupInfoUpdateListener, OnGroupMembersUpdateListener {
    private MutableLiveData<List<GroupInfo>> groupInfoUpdateLiveData;
    private MutableLiveData<List<GroupMember>> groupMembersUpdateLiveData;
    public GroupViewModel() {
        super();
        ChatManager.Instance().addGroupInfoUpdateListener(this);
        ChatManager.Instance().addGroupMembersUpdateListener(this);
    }
    @Override
    protected void onCleared() {
        ChatManager.Instance().removeGroupInfoUpdateListener(this);
        ChatManager.Instance().removeGroupMembersUpdateListener(this);
    }
    public MutableLiveData<List<GroupInfo>> groupInfoUpdateLiveData() {
        if (groupInfoUpdateLiveData == null) {
            groupInfoUpdateLiveData = new MutableLiveData<>();
        }
        return groupInfoUpdateLiveData;
    }
    public MutableLiveData<List<GroupMember>> groupMembersUpdateLiveData() {
        if (groupMembersUpdateLiveData == null) {
            groupMembersUpdateLiveData = new MutableLiveData<>();
        }
        return groupMembersUpdateLiveData;
    }
    public MutableLiveData<List<UIUserInfo>> getGroupMemberUIUserInfosLiveData(String groupId, boolean refresh) {
        MutableLiveData<List<UIUserInfo>> groupMemberLiveData = new MutableLiveData<>();
        ChatManager.Instance().getWorkHandler().post(() -> {
            List<GroupMember> members = ChatManager.Instance().getGroupMemberAll(groupId);
            List<String> memberIds = new ArrayList<>(members.size());
            for (GroupMember member : members) {
                memberIds.add(member.memberId);
            }
            List<UserInfo> userInfos = ChatManager.Instance().getUserInfos(memberIds, groupId);
            List<UIUserInfo> users = UIUserInfo.fromUserInfos(userInfos);
            groupMemberLiveData.postValue(users);
        });
        return groupMemberLiveData;
    }
    public MutableLiveData<List<UserInfo>> getGroupMemberUserInfosLiveData(String groupId, boolean refresh) {
        MutableLiveData<List<UserInfo>> groupMemberLiveData = new MutableLiveData<>();
        ChatManager.Instance().getWorkHandler().post(() -> {
            List<GroupMember> members = ChatManager.Instance().getGroupMembers(groupId, refresh);
            List<String> memberIds = new ArrayList<>(members.size());
            for (GroupMember member : members) {
                memberIds.add(member.memberId);
            }
            List<UserInfo> userInfos = ChatManager.Instance().getUserInfos(memberIds, groupId);
            groupMemberLiveData.postValue(userInfos);
        });
        return groupMemberLiveData;
    }
    public MutableLiveData<List<UserInfo>> getGroupMemberZoneLiveData(Context context,String groupId, int start, int count) {
        MutableLiveData<List<UserInfo>> groupMemberLiveData = new MutableLiveData<>();
        ChatManager.Instance().getWorkHandler().post(() -> {
            ChatManager.Instance().getGroupMemberZone(groupId, start, count, new GetGroupMembersCallback() {
                @Override
                public void onSuccess(List<GroupMember> groupMembers) {
                }

                @Override
                public void onFail(int errorCode) {
                }
            });
        });
        return groupMemberLiveData;
    }

    public MutableLiveData<List<UserInfo>> getGroupMemberFromDB(Context context,String groupId, int begin, int end) {
        MutableLiveData<List<UserInfo>> groupMemberLiveData = new MutableLiveData<>();
        ChatManager.Instance().getWorkHandler().post(() -> {
            ChatManager.Instance().getGroupMemberFromDB(groupId, begin, end, new GetGroupMembersCallback() {
                @Override
                public void onSuccess(List<GroupMember> groupMembers) {
                    List<String> memberIds = new ArrayList<>(groupMembers.size());
                    for (GroupMember member : groupMembers) {
                        memberIds.add(member.memberId);
                    }
                    List<UserInfo> userInfos = ChatManager.Instance().getUserInfos(memberIds, groupId);
                    groupMemberLiveData.postValue(userInfos);
                }

                @Override
                public void onFail(int errorCode) {
                    try {
                        //Toast.makeText(context,context.getString(R.string.service_error),Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {

                    }

                }
            });
        });
        return groupMemberLiveData;
    }

    @Override
    public void onGroupInfoUpdate(List<GroupInfo> groupInfos) {
        if (groupInfoUpdateLiveData != null) {
            groupInfoUpdateLiveData.setValue(groupInfos);
        }
    }
    public MutableLiveData<OperateResult<String>> createGroup(Context context, List<UserInfo> checkedUsers, MessageContent notifyMsg, List<Integer> lines) {
        List<String> selectedIds = new ArrayList<>(checkedUsers.size());
        List<UserInfo> selectedUsers = new ArrayList<>();
        for (UserInfo userInfo : checkedUsers) {
            selectedIds.add(userInfo.uid);
            selectedUsers.add(userInfo);
        }
        String id = ChatManager.Instance().getUserId();
        if (!selectedIds.contains(id)) {
            selectedIds.add(id);
            selectedUsers.add(ChatManager.Instance().getUserInfo(id, false));
        }
        String groupName = "";
        for (int i = 0; i < 3 && i < selectedUsers.size(); i++) {
            groupName += selectedUsers.get(i).displayName + "、";
        }
        groupName = groupName.substring(0, groupName.length() - 1);
        if (selectedUsers.size() > 3) {
            groupName += " ...";
        }
        groupName = groupName.substring(0, groupName.length() - 1);
        MutableLiveData<OperateResult<String>> groupLiveData = new MutableLiveData<>();
        String finalGroupName = groupName;
        ChatManager.Instance().getWorkHandler().post(() -> {
            // 创建群头像
            String groupPortrait = null;
            try {
                groupPortrait = generateGroupPortrait(context, selectedUsers);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 上传群头像
            String groupPortraitLink = uploadGroupPortrait(context, groupPortrait);

            // 直连建群
            SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
            String token = sp.getString("token", null);
            String create_url = (String) SPUtils.get(context,"create_url","");
            String osnId = createGroupFromLink(
                    create_url,
                    finalGroupName,
                    token,
                    selectedIds,
                    groupPortraitLink);
            if (osnId != null) {
                groupLiveData.postValue(new OperateResult<>(osnId, 0));
                return;
            }

            // 走主网建群
            String owner2= (String) SPUtils.get(context,"owner","");
            ChatManager.Instance().createGroup(null,
                    finalGroupName,
                    groupPortraitLink,
                    owner2,
                    GroupInfo.GroupType.Restricted,
                    selectedIds,
                    lines,
                    notifyMsg,
                    new GeneralCallback2() {
                @Override
                public void onSuccess(String groupId) {
                    groupLiveData.setValue(new OperateResult<>(groupId, 0));
                }
                @Override
                public void onFail(int errorCode) {
                    groupLiveData.setValue(new OperateResult<>(errorCode));
                }
            });


            /*if (groupPortrait != null) {

                OssHelper.getInstance(context);
                OssHelperGroup.getInstance(context).upload(groupPortrait, OssHelper.GroupPortraitDirectory, new OssHelperGroup.CallBack() {
                    @Override
                    public void success() {

                    }

                    @Override
                    public void success(String remoteUrl, String fileName) {

                    }

                    @Override
                    public void success(String remoteUrl) {
                        System.out.println("@@@     remoteUrl : "+remoteUrl);
                        String owner2= (String) SPUtils.get(context,"owner","");

                        SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
                        String token = sp.getString("token", null);
                        String create_url = (String) SPUtils.get(context,"create_url","");


                        if(create_url.equals("")|| create_url == null){
                            ChatManager.Instance().createGroup(null, finalGroupName, remoteUrl,owner2, GroupInfo.GroupType.Restricted, selectedIds, lines, notifyMsg, new GeneralCallback2() {
                                @Override
                                public void onSuccess(String groupId) {
                                    groupLiveData.setValue(new OperateResult<>(groupId, 0));
                                }
                                @Override
                                public void onFail(int errorCode) {
                                    groupLiveData.setValue(new OperateResult<>(errorCode));
                                }
                            });
                        }else{

                            OkHttpClient okHttpClient = new OkHttpClient();
                            JSONObject requestData = new JSONObject();
                            String json = "";
                            JSONArray array = new JSONArray();
                            array.addAll(selectedIds);
                            try {
                                requestData.put("name",finalGroupName);
                                requestData.put("owner",ChatManager.Instance().getUserId());
                                requestData.put("portrait",remoteUrl);
                                requestData.put("type",0);
                                requestData.put("userList",array);
                                json = requestData.toString();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                                    , json);

                            final Request request = new Request.Builder()
                                    .url(create_url)
                                    .post(requestBody)
                                    .addHeader("X-Token",token)
                                    .build();
                            Call call = okHttpClient.newCall(request);
                            call.enqueue(new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    System.out.println("@@@   创建群失败： "+e);
                                }

                                @Override
                                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                    String result = response.body().string();
                                    System.out.println("@@@   创建群成功: "+result);
                                    if(result == null || result.equals("")){
                                        return;
                                    }
                                    try {
                                        Gson gson = new Gson();
                                        CreatGroupInfo creatGroupInfo = gson.fromJson(result,CreatGroupInfo.class);
                                        if(creatGroupInfo.getCode() == 200){
                                            groupLiveData.postValue(new OperateResult<>(creatGroupInfo.getData(), 0));
                                        }else{
                                            groupLiveData.postValue(new OperateResult<>(creatGroupInfo.getCode()));
                                        }

                                    }catch (Exception e){
                                        return;
                                    }

                                }
                            });
                        }

                    }

                    @Override
                    public void fail() {
                        String owner2= (String) SPUtils.get(context,"owner","");
                        ChatManager.Instance().createGroup(null, finalGroupName, "",owner2, GroupInfo.GroupType.Restricted, selectedIds, lines, notifyMsg, new GeneralCallback2() {
                            @Override
                            public void onSuccess(String groupId) {
                                groupLiveData.setValue(new OperateResult<>(groupId, 0));
                            }
                            @Override
                            public void onFail(int errorCode) {
                                groupLiveData.setValue(new OperateResult<>(errorCode));
                            }
                        });
                    }
                });
            } else {
                String owner2 = (String) SPUtils.get(context,"owner","");
                ChatManager.Instance().createGroup(null, finalGroupName, null,owner2, GroupInfo.GroupType.Restricted, selectedIds, lines, notifyMsg, new GeneralCallback2() {
                    @Override
                    public void onSuccess(String groupId) {
                        groupLiveData.setValue(new OperateResult<>(groupId, 0));
                    }
                    @Override
                    public void onFail(int errorCode) {
                        groupLiveData.setValue(new OperateResult<>(errorCode));
                    }
                });
            }*/
        });
        return groupLiveData;
    }

    private String createGroupFromLink(
            String link,
            String finalGroupName,
            String token,
            List<String> selectedIds,
            String portrait
    ){
        if (link == null) {
            return null;
        }
        if (link.equalsIgnoreCase("")) {
            return null;
        }
        Object lock = new Object();
        final String[] groupId = {null};

        OkHttpClient okHttpClient = new OkHttpClient();
        JSONObject requestData = new JSONObject();
        String json = "";
        JSONArray array = new JSONArray();
        array.addAll(selectedIds);
        try {
            requestData.put("name", finalGroupName);
            requestData.put("owner",ChatManager.Instance().getUserId());
            requestData.put("portrait", portrait);
            requestData.put("type",0);
            requestData.put("userList",array);
            json = requestData.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , json);

        final Request request = new Request.Builder()
                .url(link)
                .post(requestBody)
                .addHeader("X-Token",token)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                System.out.println("@@@   创建群失败： "+e);
                synchronized (lock) {
                    lock.notify();
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String result = response.body().string();
                System.out.println("@@@   创建群成功: "+result);
                if(result == null || result.equals("")){
                    synchronized (lock) {
                        lock.notify();
                    }
                    return;
                }
                try {
                    Gson gson = new Gson();
                    CreatGroupInfo creatGroupInfo = gson.fromJson(result,CreatGroupInfo.class);
                    if(creatGroupInfo.getCode() == 200){
                        groupId[0] = creatGroupInfo.getData();
                        //groupLiveData.postValue(new OperateResult<>(creatGroupInfo.getData(), 0));
                    }else{
                        //groupLiveData.postValue(new OperateResult<>(creatGroupInfo.getCode()));
                    }

                }catch (Exception e){
                    return;
                }
                synchronized (lock) {
                    lock.notify();
                }
            }
        });

        try {
            synchronized (lock) {
                lock.wait(16000);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return groupId[0];
    }

    private String uploadGroupPortrait(Context context, String groupPortrait) {

        if (groupPortrait == null) {
            return "";
        }
        Object lock = new Object();

        final String[] remotePortraitLink = {""};

        OssHelper.getInstance(context);
        OssHelperGroup.getInstance(context).upload(groupPortrait, OssHelper.GroupPortraitDirectory, new OssHelperGroup.CallBack() {
            @Override
            public void success() {
                synchronized (lock) {
                    lock.notify();
                }
            }

            @Override
            public void success(String remoteUrl, String fileName) {
                synchronized (lock) {
                    lock.notify();
                }
            }

            @Override
            public void success(String remoteUrl) {
                System.out.println("@@@     remoteUrl : "+remoteUrl);
                remotePortraitLink[0] = remoteUrl;
                synchronized (lock) {
                    lock.notify();
                }
            }

            @Override
            public void fail() {
                synchronized (lock) {
                    lock.notify();
                }
            }
        });

        try {
            synchronized (lock) {
                lock.wait(8000);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return remotePortraitLink[0];
    }

    public MutableLiveData<Boolean> joinGroup(Context context, String groupId, String reason) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        // TODO need update group portrait or not?
        ChatManager.Instance().joinGroup(groupId, reason, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(true);
            }
            @Override
            public void onFail(int errorCode) {
                result.setValue(false);
            }
        });
        return result;
    }
    public MutableLiveData<Boolean> addGroupMember(Context context, GroupInfo groupInfo, List<String> memberIds, MessageContent notifyMsg, List<Integer> notifyLines) {
        updatePortrait(context, groupInfo.target, memberIds, null);
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        // TODO need update group portrait or not?
        ChatManager.Instance().addGroupMembers(groupInfo.target, memberIds, notifyLines, notifyMsg, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(true);
            }
            @Override
            public void onFail(int errorCode) {
                result.setValue(false);
            }
        });
        return result;
    }
    public MutableLiveData<Boolean> removeGroupMember(Context context, GroupInfo groupInfo, List<String> memberIds, MessageContent notifyMsg, List<Integer> notifyLines) {
        updatePortrait(context, groupInfo.target, null, memberIds);
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        ChatManagerHolder.gChatManager.removeGroupMembers(groupInfo.target, memberIds, notifyLines, notifyMsg, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(true);
            }
            @Override
            public void onFail(int errorCode) {
                result.setValue(false);
            }
        });
        return result;
    }
    public MutableLiveData<OperateResult<Boolean>> setGroupManager(String groupId, boolean isSet, List<String> memberIds, NotificationMessageContent notifyMsg, List<Integer> lines) {
        MutableLiveData<OperateResult<Boolean>> result = new MutableLiveData<>();
        if(isSet){
            ChatManager.Instance().addGroupManager(groupId, memberIds, new GeneralCallback2(){
                @Override
                public void onSuccess(String data) {
                    result.setValue(new OperateResult<>(0));
                }

                @Override
                public void onFail(int errorCode) {
                    result.setValue(new OperateResult<>(errorCode));
                }
            });
        }else{
            ChatManager.Instance().delGroupManager(groupId, memberIds, new GeneralCallback2(){
                @Override
                public void onSuccess(String data) {
                    result.setValue(new OperateResult<>(0));
                }

                @Override
                public void onFail(int errorCode) {
                    result.setValue(new OperateResult<>(errorCode));
                }
            });
        }
//        ChatManager.Instance().setGroupManager(groupId, isSet, memberIds, lines, notifyMsg, new GeneralCallback() {
//            @Override
//            public void onSuccess() {
//                result.setValue(new OperateResult<>(0));
//            }
//            @Override
//            public void onFail(int errorCode) {
//                result.setValue(new OperateResult<>(errorCode));
//            }
//        });
        return result;
    }
    public MutableLiveData<OperateResult<Boolean>> muteGroupMember(String groupId, boolean mute, List<String> memberIds, NotificationMessageContent notifyMsg, List<Integer> lines) {
        MutableLiveData<OperateResult<Boolean>> result = new MutableLiveData<>();
        ChatManager.Instance().muteGroupMember(groupId, mute, memberIds, lines, notifyMsg, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(new OperateResult<>(0));
            }
            @Override
            public void onFail(int errorCode) {
                result.setValue(new OperateResult<>(errorCode));
            }
        });
        return result;
    }
    public MutableLiveData<OperateResult<Boolean>> allowGroupMember(String groupId, boolean allow, List<String> memberIds, NotificationMessageContent notifyMsg, List<Integer> lines) {
        MutableLiveData<OperateResult<Boolean>> result = new MutableLiveData<>();
        ChatManager.Instance().allowGroupMember(groupId, allow, memberIds, lines, notifyMsg, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(new OperateResult<>(0));
            }
            @Override
            public void onFail(int errorCode) {
                result.setValue(new OperateResult<>(errorCode));
            }
        });
        return result;
    }
    public MutableLiveData<OperateResult<Boolean>> muteAll(String groupId, boolean mute, MessageContent notifyMsg, List<Integer> notifyLines) {
        MutableLiveData<OperateResult<Boolean>> result = new MutableLiveData<>();

        ChatManager.Instance().muteGroup(groupId, mute, null, notifyLines, notifyMsg, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(new OperateResult<>(0));
            }
            @Override
            public void onFail(int errorCode) {
                result.setValue(new OperateResult<>(errorCode));
            }
        });

        /*ChatManager.Instance().modifyGroupInfo(groupId, ModifyGroupInfoType.Modify_Group_Mute, mute ? "1" : "0", notifyLines, notifyMsg, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(new OperateResult<>(0));
            }
            @Override
            public void onFail(int errorCode) {
                result.setValue(new OperateResult<>(errorCode));
            }
        });*/
        return result;
    }
    public MutableLiveData<OperateResult<Boolean>> enablePrivateChat(String groupId, boolean enablePrivateChat, MessageContent notifyMsg, List<Integer> notifyLines) {
        MutableLiveData<OperateResult<Boolean>> result = new MutableLiveData<>();
        ChatManager.Instance().modifyGroupInfo(groupId, ModifyGroupInfoType.Modify_Group_PrivateChat, enablePrivateChat ? "0" : "1", notifyLines, notifyMsg, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(new OperateResult<>(0));
            }
            @Override
            public void onFail(int errorCode) {
                result.setValue(new OperateResult<>(errorCode));
            }
        });
        return result;
    }
    public MutableLiveData<OperateResult<Boolean>> setGroupJoinType(
            String groupId,
            int joinType,
            MessageContent notifyMsg,
            List<Integer> notifyLines,
            Context context
    ) {

        MutableLiveData<OperateResult<Boolean>> result = new MutableLiveData<>();

        /**
         * 0  自由加群
         * 1  成员邀请
         * 2  管理员邀请
         * 3  申请加群
         * 4  密码加群
         * 5
         *
         * **/

        String joinTypeStr = getJoinType(joinType);

        ProgresDialog progresDialog = new ProgresDialog(context);
        progresDialog.show();

        ChatManager.Instance().upDescribe("joinType", joinTypeStr,
                groupId, new GeneralCallback2(){
                    @Override
                    public void onSuccess(String res) {
                        progresDialog.dismiss();
                        System.out.println("[GroupViewModel] UpDescribe success." + res);
                        result.setValue(new OperateResult<>(0));
                        ChatManager.Instance().getGroupInfo(groupId, true);
                    }
                    @Override
                    public void onFail(int errorCode) {
                        progresDialog.dismiss();
                        System.out.println("[GroupViewModel] UpDescribe failed." + errorCode);
                        result.setValue(new OperateResult<>(errorCode));
                    }
                });

        // 搞完以后需要刷新groupInfo


        /*ChatManager.Instance().modifyGroupInfo(groupId, ModifyGroupInfoType.Modify_Group_JoinType, joinType + "", notifyLines, notifyMsg, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(new OperateResult<>(0));
            }
            @Override
            public void onFail(int errorCode) {
                result.setValue(new OperateResult<>(errorCode));
            }
        });*/
        return result;
    }

    public boolean setGroupJoinType(
            String groupId,
            int joinType,
            Context context
    ) {


        /**
         * 0  自由加群
         * 1  成员邀请
         * 2  管理员邀请
         * 3  申请加群
         * 4  密码加群
         * 5
         *
         * **/

        String joinTypeStr = getJoinType(joinType);

        ProgresDialog progresDialog = new ProgresDialog(context);
        progresDialog.show();

        Object lock = new Object();
        final boolean[] result = {false};

        ChatManager.Instance().upDescribe("joinType", joinTypeStr,
                groupId, new GeneralCallback2(){
                    @Override
                    public void onSuccess(String res) {
                        progresDialog.dismiss();
                        System.out.println("[GroupViewModel] UpDescribe success." + res);
                        ChatManager.Instance().getGroupInfo(groupId, true);
                        result[0] = true;
                        synchronized (lock) {
                            lock.notify();
                        }
                    }
                    @Override
                    public void onFail(int errorCode) {
                        progresDialog.dismiss();
                        System.out.println("[GroupViewModel] UpDescribe failed." + errorCode);
                        result[0] = false;
                        synchronized (lock) {
                            lock.notify();
                        }
                    }
                });

        try {
            synchronized (lock) {
                lock.wait();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result[0];
    }

    private String getJoinType(int position) {
        /**
         * 0  自由加群
         * 1  成员邀请
         * 2  管理员邀请
         * 3  申请加群
         * 4  密码加群
         * 5
         *
         * **/
        String joinType = "free";
        switch (position) {
            case 0:
                joinType = "free";
                break;
            case 1:
                joinType = "member";
                break;
            case 2:
                joinType = "admin";
                break;
            case 3:
                joinType = "verify";
                break;
            case 4:
                joinType = "password";
                break;

            default:
                //joinType = "free";
                break;
        }
        return joinType;
    }

    public MutableLiveData<OperateResult<Boolean>> setGroupPassType(String groupId, int passType, MessageContent notifyMsg, List<Integer> notifyLines) {
        MutableLiveData<OperateResult<Boolean>> result = new MutableLiveData<>();
        ChatManager.Instance().modifyGroupInfo(groupId, ModifyGroupInfoType.Modify_Group_PassType, passType + "", notifyLines, notifyMsg, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(new OperateResult<>(0));
            }
            @Override
            public void onFail(int errorCode) {
                result.setValue(new OperateResult<>(errorCode));
            }
        });
        return result;
    }
    public MutableLiveData<OperateResult<Boolean>> setGroupType(String groupId, int type, MessageContent notifyMsg, List<Integer> notifyLines) {
        MutableLiveData<OperateResult<Boolean>> result = new MutableLiveData<>();
        ChatManager.Instance().modifyGroupInfo(groupId, ModifyGroupInfoType.Modify_Group_Type, type + "", notifyLines, notifyMsg, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(new OperateResult<>(0));
            }
            @Override
            public void onFail(int errorCode) {
                result.setValue(new OperateResult<>(errorCode));
            }
        });
        return result;
    }
    public MutableLiveData<OperateResult<Boolean>> setGroupSearchType(String groupId, int searchType, MessageContent notifyMsg, List<Integer> notifyLines) {
        MutableLiveData<OperateResult<Boolean>> result = new MutableLiveData<>();
        ChatManager.Instance().modifyGroupInfo(groupId, ModifyGroupInfoType.Modify_Group_Searchable, searchType + "", notifyLines, notifyMsg, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(new OperateResult<>(0));
            }
            @Override
            public void onFail(int errorCode) {
                result.setValue(new OperateResult<>(errorCode));
            }
        });
        return result;
    }
    public @Nullable
    GroupInfo getGroupInfo(String groupId, boolean refresh) {
        return ChatManager.Instance().getGroupInfo(groupId, refresh);
    }


    public List<GroupMember> getGroupMembers(String groupId, boolean forceRefresh) {
        return ChatManager.Instance().getGroupMembers(groupId, forceRefresh);
    }
    public MutableLiveData<List<GroupMember>> getGroupMembersLiveData(String groupId, boolean refresh) {
        MutableLiveData<List<GroupMember>> data = new MutableLiveData<>();
        ChatManager.Instance().getWorkHandler().post(() -> {
            List<GroupMember> members = ChatManager.Instance().getGroupMembers(groupId, refresh);
            data.postValue(members);
        });
        return data;
    }
    public MutableLiveData<List<UIUserInfo>> getGroupManagerUIUserInfosLiveData(String groupId, boolean refresh) {
        MutableLiveData<List<UIUserInfo>> data = new MutableLiveData<>();
        ChatManager.Instance().getWorkHandler().post(() -> {
            List<GroupMember> managers = getGroupManagers(groupId);
            List<UIUserInfo> userInfos = managerMemberToUIUserInfo(groupId, managers);
            data.postValue(userInfos);
        });
        return data;
    }
    public MutableLiveData<List<UIUserInfo>> getMutedOrAllowedMemberUIUserInfosLiveData(String groupId, boolean muted, boolean refresh) {
        MutableLiveData<List<UIUserInfo>> data = new MutableLiveData<>();
        ChatManager.Instance().getWorkHandler().post(() -> {
            List<GroupMember> mutedMembers = getMutedOrAllowedMembers(groupId, muted);
            List<UIUserInfo> userInfos = mutedOrAllowedMemberToUIUserInfo(groupId, muted, mutedMembers);
            data.postValue(userInfos);
        });
        return data;
    }
    public List<GroupMember> getGroupManagers(String groupId) {
        List<GroupMember> managers = ChatManager.Instance().getGroupManagers(groupId);
        /*List<GroupMember> managers = new ArrayList<>();
        if (members != null) {
            for (GroupMember member : members) {
                if (member.type == GroupMember.GroupMemberType.Manager || member.type == GroupMember.GroupMemberType.Owner) {
                    managers.add(member);
                }
            }
        }*/
        return managers;
    }
    public List<String> getGroupManagerIds(String groupId) {
        List<GroupMember> managers = getGroupManagers(groupId);
        List<String> mangerIds = new ArrayList<>();
        if (managers != null) {
            for (GroupMember manager : managers) {
                mangerIds.add(manager.memberId);
            }
        }
        return mangerIds;
    }
    public List<GroupMember> getMutedOrAllowedMembers(String groupId, boolean muted) {
        List<GroupMember> members = ChatManager.Instance().getGroupMemberAll(groupId);
        List<GroupMember> managers = new ArrayList<>();
        if (members != null) {
            for (GroupMember member : members) {
//                if ((muted && member.type == GroupMember.GroupMemberType.Allowed)
//                    || !muted && member.type == GroupMember.GroupMemberType.Muted) {
//                    managers.add(member);
//                }

                if (muted) {
                    if (member.mute == 0) {
                        managers.add(member);
                    }
                } else {
                    if (member.mute != 0) {
                        managers.add(member);
                    }
                }

                /*if ((muted && member.mute == 0)
                        || !muted && member.mute == 1) {
                    managers.add(member);
                }*/
            }
        }
        return managers;
    }
    public List<String> getMutedOrAllowedMemberIds(String groupId, boolean muted) {
        List<GroupMember> mutedMembers = getMutedOrAllowedMembers(groupId, muted);
        List<String> mutedIds = new ArrayList<>();
        if (mutedMembers != null) {
            for (GroupMember manager : mutedMembers) {
                mutedIds.add(manager.memberId);
            }
        }
        return mutedIds;
    }
    private List<UIUserInfo> managerMemberToUIUserInfo(String groupId, List<GroupMember> members) {
        if (members == null || members.isEmpty()) {
            return null;
        }
        List<String> memberIds = new ArrayList<>(members.size());
        for (GroupMember member : members) {
            memberIds.add(member.memberId);
        }
        List<UIUserInfo> uiUserInfos = new ArrayList<>();
        List<UserInfo> userInfos = UserViewModel.getUsers(memberIds, groupId);
        boolean showManagerCategory = false;
        for (UserInfo userInfo : userInfos) {
            UIUserInfo info = new UIUserInfo(userInfo);
            String name = ChatManager.Instance().getGroupMemberDisplayName(userInfo);
            if (!TextUtils.isEmpty(name)) {
                String pinyin = PinyinUtils.getPinyin(name);
                char c = pinyin.toUpperCase().charAt(0);
                if (c >= 'A' && c <= 'Z') {
                    info.setSortName(pinyin);
                } else {
                    // 为了让排序排到最后
                    info.setSortName("{" + pinyin);
                }
            } else {
                info.setSortName("");
            }
            for (GroupMember member : members) {
                if (userInfo.uid.equals(member.memberId)) {
                    if (member.type == GroupMember.GroupMemberType.Manager) {
                        info.setCategory(WfcUIKit.getString(R.string.administrator));
                        if (!showManagerCategory) {
                            showManagerCategory = true;
                            info.setShowCategory(true);
                        }
                        uiUserInfos.add(info);
                    } else {
                        info.setCategory(WfcUIKit.getString(R.string.owner));
                        info.setShowCategory(true);
                        uiUserInfos.add(0, info);
                    }
                    break;
                }
            }
        }
        return uiUserInfos;
    }
    private List<UIUserInfo> mutedOrAllowedMemberToUIUserInfo(String groupId, boolean muted, List<GroupMember> members) {
        if (members == null || members.isEmpty()) {
            return null;
        }
        List<String> memberIds = new ArrayList<>(members.size());
        for (GroupMember member : members) {
            memberIds.add(member.memberId);
        }
        List<UIUserInfo> uiUserInfos = new ArrayList<>();
        List<UserInfo> userInfos = UserViewModel.getUsers(memberIds, groupId);
        boolean showManagerCategory = false;
        for (UserInfo userInfo : userInfos) {
            UIUserInfo info = new UIUserInfo(userInfo);
            String name = ChatManager.Instance().getGroupMemberDisplayName(userInfo);
            if (!TextUtils.isEmpty(name)) {
                String pinyin = PinyinUtils.getPinyin(name);
                char c = pinyin.toUpperCase().charAt(0);
                if (c >= 'A' && c <= 'Z') {
                    info.setSortName(pinyin);
                } else {
                    // 为了让排序排到最后
                    info.setSortName("{" + pinyin);
                }
            } else {
                info.setSortName("");
            }
            info.setCategory(muted ? WfcUIKit.getString(R.string.white_list) : WfcUIKit.getString(R.string.mute_list));
            if (!showManagerCategory) {
                showManagerCategory = true;
                info.setShowCategory(true);
            }
            uiUserInfos.add(info);
        }
        return uiUserInfos;
    }
    public GroupMember getGroupMember(String groupId, String memberId) {
        return ChatManager.Instance().getGroupMember(groupId, memberId);
    }
    public String getGroupMemberDisplayName(String groupId, String memberId) {
        return ChatManager.Instance().getGroupMemberDisplayName(groupId, memberId);
    }
    public MutableLiveData<OperateResult<List<GroupInfo>>> getFavGroups() {
        MutableLiveData<OperateResult<List<GroupInfo>>> result = new MutableLiveData<>();
        ChatManager.Instance().getFavGroups(new GetGroupsCallback() {
            @Override
            public void onSuccess(List<GroupInfo> groupInfos) {
                result.setValue(new OperateResult<>(groupInfos, 0));
            }
            @Override
            public void onFail(int errorCode) {
                result.setValue(new OperateResult<>(null, 0));
            }
        });
        return result;
    }
    public MutableLiveData<OperateResult<Boolean>> modifyGroupInfo(String groupId, ModifyGroupInfoType modifyType, String newValue, MessageContent notifyMsg, List<Integer> notifyLines) {
        MutableLiveData<OperateResult<Boolean>> result = new MutableLiveData<>();
        ChatManager.Instance().modifyGroupInfo(groupId, modifyType, newValue, notifyLines, notifyMsg, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(new OperateResult<>(true, 0));
            }
            @Override
            public void onFail(int errorCode) {
                result.setValue(new OperateResult<>(false, errorCode));
            }
        });
        return result;
    }
    public MutableLiveData<OperateResult> modifyMyGroupAlias(String groupId, String alias, MessageContent notifyMsg, List<Integer> notifyLines) {
        MutableLiveData<OperateResult> result = new MutableLiveData<>();
        ChatManager.Instance().modifyGroupAlias(groupId, alias, notifyLines, notifyMsg, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(new OperateResult<>(0));
            }
            @Override
            public void onFail(int errorCode) {
                result.setValue(new OperateResult<>(errorCode));
            }
        });
        return result;
    }
    public MutableLiveData<OperateResult<Boolean>> setFavGroup(String groupId, boolean fav) {
        MutableLiveData<OperateResult<Boolean>> result = new MutableLiveData<>();
        ChatManager.Instance().setFavGroup(groupId, fav, new GeneralCallback() {
            @Override
            public void onSuccess() {
            //    Toast.makeText(context, "Synchronization succeeded.", Toast.LENGTH_SHORT).show();
                result.setValue(new OperateResult<>(0));
            }
            @Override
            public void onFail(int errorCode) {
           //     Toast.makeText(context, "Synchronization failure", Toast.LENGTH_SHORT).show();
                result.setValue(new OperateResult<>(errorCode));
            }
        });
        return result;
    }
    public MutableLiveData<Boolean> quitGroup(String groupId, List<Integer> lines, MessageContent notifyMsg) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        ChatManager.Instance().quitGroup(groupId, lines, notifyMsg, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(true);
            }
            @Override
            public void onFail(int errorCode) {
                result.setValue(false);
            }
        });
        return result;
    }
    public MutableLiveData<Boolean> dismissGroup(String groupId, List<Integer> lines, MessageContent notifyMsg) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        ChatManager.Instance().dismissGroup(groupId, lines, notifyMsg, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(true);
            }
            @Override
            public void onFail(int errorCode) {
                result.setValue(false);
            }
        });
        return result;
    }
    public void updatePortrait(Context context, String groupId, List<String> addUsers, List<String> delUsers){
        ChatManager.Instance().getWorkHandler().post(() -> {
            try {
                List<GroupMember> members = ChatManager.Instance().getGroupMembers(groupId, false);
                List<UserInfo> users = new ArrayList<>();
                for (GroupMember m : members) {
                    boolean finded = false;
                    if(delUsers != null){
                        for(String u : delUsers){
                            if(u.equalsIgnoreCase(m.memberId)){
                                finded = true;
                                break;
                            }
                        }
                    }
                    if(!finded){
                        UserInfo userInfo = ChatManager.Instance().getUserInfo(m.memberId, false);
                        users.add(userInfo);
                    }
                }
                if(addUsers != null){
                    for(String u : addUsers){
                        UserInfo userInfo = ChatManager.Instance().getUserInfo(u, false);
                        users.add(userInfo);
                    }
                }
                String portrait = generateGroupPortrait(context, users);


                OssHelper.getInstance(context).uploadFile(portrait, OssHelper.GroupPortraitDirectory, new OssHelper.CallBack() {
                    @Override
                    public void success() {

                    }

                    @Override
                    public void success(String remoteUrl, String fileName) {
                        String realRemoteUrl = OssHelper.ossHelper.getImageRemoteUrl(fileName, OssHelper.GroupPortraitDirectory);
                        modifyGroupInfo(groupId, ModifyGroupInfoType.Modify_Group_Portrait, realRemoteUrl, null, new ArrayList<>());
                    }

                    @Override
                    public void fail() {
                        //
                    }
                });

                /*ChatManager.Instance().uploadMediaFile(portrait, MessageContentMediaType.PORTRAIT.getValue(), new UploadMediaCallback() {
                    @Override
                    public void onSuccess(String result) {
                        modifyGroupInfo(groupId, ModifyGroupInfoType.Modify_Group_Portrait, result, null, new ArrayList<>());
                    }
                    @Override
                    public void onProgress(long uploaded, long total) {

                    }
                    @Override
                    public void onFail(int errorCode) {


                    }
                });*/


            }
            catch (Exception e){
                e.printStackTrace();
            }
        });
    }
    private @Nullable
    String generateGroupPortrait(Context context, List<UserInfo> userInfos) throws Exception {
        List<Bitmap> bitmaps = new ArrayList<>();
        for (UserInfo userInfo : userInfos) {
            Drawable drawable;
            try {
                drawable = GlideApp.with(context).load(userInfo.portrait).placeholder(R.mipmap.avatar_def).submit(240, 240).get();
            } catch (Exception e) {
                e.printStackTrace();
                drawable = GlideApp.with(context).load(R.mipmap.avatar_def).submit(240, 240).get();
            }
            if (drawable instanceof BitmapDrawable) {
                bitmaps.add(((BitmapDrawable) drawable).getBitmap());
            }
        }
        Bitmap bitmap = CombineBitmapTools.combimeBitmap(context, 240, 240, bitmaps);
        if (bitmap == null) {
            return null;
        }
        //create a file to write bitmap data
        File f = new File(context.getCacheDir(), System.currentTimeMillis() + ".png");
        f.createNewFile();
        //Convert bitmap to byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapData = bos.toByteArray();
        //write the bytes in file
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(bitmapData);
        fos.flush();
        fos.close();
        return f.getAbsolutePath();
    }
    @Override
    public void onGroupMembersUpdate(String groupId, List<GroupMember> groupMembers) {
        if (groupMembersUpdateLiveData != null && groupMembers != null && !groupMembers.isEmpty()) {
            groupMembersUpdateLiveData.setValue(groupMembers);
        }
    }
}
