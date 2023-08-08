/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.group.manage;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kyleduo.switchbutton.SwitchButton;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.contact.model.FooterValue;
import cn.wildfire.chat.kit.group.GroupViewModel;
import cn.wildfire.chat.kit.litapp.AddanApletsActivity;
import cn.wildfire.chat.kit.net.OKHttpHelper;
import cn.wildfire.chat.kit.net.SimpleCallback;
import cn.wildfire.chat.kit.utils.ProgresDialog;
import cn.wildfire.chat.kit.utils.SPUtils;
import cn.wildfire.chat.kit.web.WebViewActivity1;
import cn.wildfire.chat.kit.widget.OptionItemView;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.widget.OptionItemView1;
import cn.wildfirechat.client.ClientService;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.GroupMember;
import cn.wildfirechat.model.NullGroupInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback2;

public class GroupManageFragment extends Fragment {
    private GroupInfo groupInfo;
    @BindView(R2.id.joinOptionItemView)
    OptionItemView joinOptionItemView;
    @BindView(R2.id.passOptionItemView)
    OptionItemView passOptionItemView;
    @BindView(R2.id.searchOptionItemView)
    OptionItemView searchOptionItemView;
    @BindView(R2.id.groupTypeOptionItemView)
    OptionItemView groupTypeOptionItemView;
    @BindView(R2.id.timeOptionItemView)
    OptionItemView timeOptionItemView;
    @BindView(R2.id.addanapplets)
    OptionItemView addanapplets;
    @BindView(R2.id.mutualFrienndsButton)
    SwitchButton mutualFrienndsButton;
    @BindView(R2.id.modifyGroupMessageForWardButton)
    SwitchButton modifyGroupMessageForWardButton;
    @BindView(R2.id.modifyGroupMessageCopyButton)
    SwitchButton modifyGroupMessageCopyButton;
    @BindView(R2.id.updateRedPacketButton)
    SwitchButton updateRedPacketButton;
    @BindView(R2.id.clearTimeOptionItemView)
    OptionItemView clearTimeOptionItemView;
    @BindView(R2.id.redPecketSetting)
    OptionItemView1 redPecketSetting;
    @BindView(R2.id.setGroupPwd)
    OptionItemView setGroupPwd;

    private GroupViewModel groupViewModel;

    private String[] passTypes;
    private String[] joinTypes;
    private String[] timeTypes;
    private String[] clearTimeTypes;
    private long cleatTime;
    private Dialog dialog;
    private GroupMember groupMember;

    public static GroupManageFragment newInstance(GroupInfo groupInfo) {

        Bundle args = new Bundle();
        args.putParcelable("groupInfo", groupInfo);
        GroupManageFragment fragment = new GroupManageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        groupInfo = getArguments().getParcelable("groupInfo");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.group_manage_fragment, container, false);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

    private void init() {

        String myself = ChatManager.Instance().getUserId();
        groupMember = ChatManager.Instance().getGroupMember(groupInfo.target, myself);
        if(groupMember != null){
            if (groupMember.memberId.equalsIgnoreCase(groupInfo.owner)) {
                System.out.println("@@@    群主");
                setGroupPwd.setVisibility(View.VISIBLE);
            }else{
                setGroupPwd.setVisibility(View.GONE);
            }
        }


      //  String[] joinTypes = getResources().getStringArray(R.array.group_join_type);
        joinTypes = new String[]{
                getResources().getString(R.string.no_restrictions_entry),
                getResources().getString(R.string.group_members_people),
                getResources().getString(R.string.only_group_management_people),
                getResources().getString(R.string.join_group_type_verify),
                getResources().getString(R.string.join_group_type_password)
        };


        String joinTypeStr = groupInfo.getJoinType();
        int jt = getJoinType(joinTypeStr);
        joinOptionItemView.setDesc(joinTypes[jt]);


    //    String[] passTypes = getResources().getStringArray(R.array.group_pass_type);
        passTypes = new String[]{getResources().getString(R.string.no_validation),getResources().getString(R.string.need_to_verify)};

        passOptionItemView.setDesc(passTypes[groupInfo.passType]);

        timeTypes = new String[]{"0 s","1 s","3 s","5 s","15 s", "30 s","60 s"};

        if(groupInfo.getTimeInterval() == 0){
            timeOptionItemView.setDesc(timeTypes[0]);
        }else if(groupInfo.getTimeInterval() == 1){
            timeOptionItemView.setDesc(timeTypes[1]);
        }else if(groupInfo.getTimeInterval() == 3){
            timeOptionItemView.setDesc(timeTypes[2]);
        }else if(groupInfo.getTimeInterval() == 5){
            timeOptionItemView.setDesc(timeTypes[3]);
        }else if(groupInfo.getTimeInterval() == 15){
            timeOptionItemView.setDesc(timeTypes[4]);
        }else if(groupInfo.getTimeInterval() == 30){
            timeOptionItemView.setDesc(timeTypes[5]);
        }else if(groupInfo.getTimeInterval() == 60){
            timeOptionItemView.setDesc(timeTypes[6]);
        }

        clearTimeTypes = new String[]{"0"+getString(R.string.clear_day),"1"+getString(R.string.clear_day),"2"+getString(R.string.clear_day),
                "7"+getString(R.string.clear_day),"15"+getString(R.string.clear_day),"30"+getString(R.string.clear_day)};
        if(!groupInfo.getClearTimes().equals("0")){
            cleatTime = Long.valueOf(groupInfo.getClearTimes()) / 3600 / 1000 / 24;
            clearTimeOptionItemView.setDesc(cleatTime + getString(R.string.clear_day));
        }

        String[] types = getResources().getStringArray(R.array.group_type);
        groupTypeOptionItemView.setDesc(types[groupInfo.type.value()]);

        groupViewModel = ViewModelProviders.of(this).get(GroupViewModel.class);
        groupViewModel.groupInfoUpdateLiveData().observe(getActivity(), new Observer<List<GroupInfo>>() {
            @Override
            public void onChanged(List<GroupInfo> groupInfos) {
                for (GroupInfo info : groupInfos) {
                    if (info.target.equals(groupInfo.target)) {
                        groupInfo = info;
                        break;
                    }
                }
            }
        });

        if (groupInfo.AllowAddFriend()) {
            mutualFrienndsButton.setChecked(true);
        } else {
            mutualFrienndsButton.setChecked(false);
        }

        if(groupInfo.isGroupForward()){
            modifyGroupMessageForWardButton.setChecked(true);
        }else{
            modifyGroupMessageForWardButton.setChecked(false);
        }

        if(groupInfo.isGroupCopy()){
            modifyGroupMessageCopyButton.setChecked(true);
        }else {
            modifyGroupMessageCopyButton.setChecked(false);
        }

        updateRedPacketButton.setChecked(groupInfo.redPacket == 1);
        updateRedPacketButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                updateRedPacketGroup(isChecked);
            }
        });

        if(groupInfo.getBombEnable()){
            redPecketSetting.setVisibility(View.VISIBLE);
        }else{
            redPecketSetting.setVisibility(View.GONE);
        }
    }

    private int getJoinType(String type) {
        /**
         * 0  自由加群
         * 1  成员邀请
         * 2  管理员邀请
         * 3  申请加群
         * 4  密码加群
         * 5
         *
         * **/
        //String joinType = "free";
        switch (type) {
            case "free":
                return 0;
            case "member":
                return 1;
            case "admin":
                return 2;
            case "verify":
                return 3;
            case "password":
                return 4;
            case "none":
                return 5;
            default:
                break;
        }
        return 0;
    }

    @OnClick(R2.id.redPecketSetting)
    void setRedPecketSetting(){
        String url = (String) SPUtils.get(getActivity(),"redPack","");

        SharedPreferences sp = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
        String token = sp.getString("token", null);
        String language = sp.getString("language","0");
        String webUrl = url +"?language="+language+ "&token="+token + "&groupId="+groupInfo.target;
        Intent intent = new Intent(getActivity(), WebViewActivity1.class);
        intent.putExtra("url", webUrl);
        startActivity(intent);
    }

    @OnClick(R2.id.managerOptionItemView)
    void showGroupManagerSetting() {
        Intent intent = new Intent(getActivity(), GroupManagerListActivity.class);
        intent.putExtra("groupInfo", groupInfo);
        startActivity(intent);
    }

    @OnClick(R2.id.muteOptionItemView)
    void showGroupMuteSetting() {
        Intent intent = new Intent(getActivity(), GroupMuteOrAllowActivity.class);
        intent.putExtra("groupInfo", groupInfo);
        startActivity(intent);

    }

    @OnClick(R2.id.permissionOptionItemView)
    void showMemberPermissionSetting() {
        Intent intent = new Intent(getActivity(), GroupMemberPermissionActivity.class);
        intent.putExtra("groupInfo", groupInfo);
        startActivity(intent);
    }

    @OnClick(R2.id.addanapplets)
    void addanApplets(){
        Intent intent = new Intent(getActivity(), AddanApletsActivity.class);
        intent.putExtra("target", groupInfo.target);
        intent.putExtra("userID", groupInfo.owner);
        startActivity(intent);
    }

    @OnClick(R2.id.mutualFrienndsButton)
    void mutualFrienndsButton() {
        if (mutualFrienndsButton.isChecked()) {
            //允许
            ChatManager.Instance().allowGroupAddFriend("yes", groupInfo.target, new GeneralCallback2() {
                @Override
                public void onSuccess(String result) {
                    System.out.println("@@@     允许添加好友result=" + result);
                }

                @Override
                public void onFail(int errorCode) {
                    System.out.println("@@@     errorCode=" + errorCode);
                }
            });
        } else {
            //不允许
            ChatManager.Instance().allowGroupAddFriend("no", groupInfo.target, new GeneralCallback2() {
                @Override
                public void onSuccess(String result) {
                    System.out.println("@@@     不允许添加好友result=" + result);
                }

                @Override
                public void onFail(int errorCode) {
                    System.out.println("@@@     errorCode=" + errorCode);
                }
            });
        }
    }

    @OnClick(R2.id.modifyGroupMessageForWardButton)
    void modifyGroupMessageForWardButton(){
        if (modifyGroupMessageForWardButton.isChecked()) {
            //允许
            ChatManager.Instance().isGroupForward("yes", groupInfo.target, new GeneralCallback2() {
                @Override
                public void onSuccess(String result) {
                    System.out.println("@@@     允许转发群信息result=" + result);
                }

                @Override
                public void onFail(int errorCode) {
                    System.out.println("@@@     errorCode=" + errorCode);
                }
            });
        } else {
            //不允许
            ChatManager.Instance().isGroupForward("no", groupInfo.target, new GeneralCallback2() {
                @Override
                public void onSuccess(String result) {
                    System.out.println("@@@     不允许转发群信息result=" + result);
                }

                @Override
                public void onFail(int errorCode) {
                    System.out.println("@@@     errorCode=" + errorCode);
                }
            });
        }
    }

    @OnClick(R2.id.modifyGroupMessageCopyButton)
    void modifyGroupMessageCopyButton(){
        if (modifyGroupMessageCopyButton.isChecked()) {
            //允许
            ChatManager.Instance().isGroupCopy("yes", groupInfo.target, new GeneralCallback2() {
                @Override
                public void onSuccess(String result) {
                    System.out.println("@@@     允许复制群信息result=" + result);
                }

                @Override
                public void onFail(int errorCode) {
                    System.out.println("@@@     errorCode=" + errorCode);
                }
            });
        } else {
            //不允许
            ChatManager.Instance().isGroupCopy("no", groupInfo.target, new GeneralCallback2() {
                @Override
                public void onSuccess(String result) {
                    System.out.println("@@@     不允许复制群信息result=" + result);
                }

                @Override
                public void onFail(int errorCode) {
                    System.out.println("@@@     errorCode=" + errorCode);
                }
            });
        }
    }

    @OnClick(R2.id.setGroupPwd)
    void setGroupPwd(){
        View views = View.inflate(getActivity(), cn.wildfire.chat.kit.R.layout.dialog_keyword, null);
        Button btn_cancel = views.findViewById(cn.wildfire.chat.kit.R.id.btn_cancel);
        Button btn_confirm= views.findViewById(cn.wildfire.chat.kit.R.id.btn_confirm);
        EditText edt_keywords = views.findViewById(cn.wildfire.chat.kit.R.id.edt_keywords);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(views);
        builder.setCancelable(true);

        dialog = builder.create();
        dialog.show();

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = edt_keywords.getText().toString();
                if(!content.isEmpty()){
                    dialog.dismiss();
                    ProgresDialog progresDialog = new ProgresDialog(getActivity());
                    progresDialog.show();

                    Object lock = new Object();
                    ChatManager.Instance().upPrivateInfo("joinPwd", content,
                            groupInfo.target, new GeneralCallback2(){
                                @Override
                                public void onSuccess(String res) {
                                    progresDialog.dismiss();
                                    System.out.println("[GroupViewModel] upPrivateInfo success." + res);
                                    synchronized (lock) {
                                        lock.notify();
                                    }
                                }
                                @Override
                                public void onFail(int errorCode) {
                                    progresDialog.dismiss();
                                    System.out.println("[GroupViewModel] upPrivateInfo failed." + errorCode);
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

                }else{
                    dialog.dismiss();
                    Toast.makeText(getActivity(), getString(cn.wildfire.chat.kit.R.string.input_empty), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @OnClick(R2.id.joinOptionItemView)
    void showJoinTypeSetting() {
        new MaterialDialog.Builder(getActivity())
            .items(joinTypes)
            .itemsCallback((dialog, itemView, position, text) -> {

                if (groupViewModel.setGroupJoinType(
                        groupInfo.target,
                        position,
                        getActivity()) ) {

                    System.out.println("[GroupManageFragment] UpDescribe success." + text);
                    joinOptionItemView.setDesc((String) text);

                } else {
                    Toast.makeText(getActivity(), R.string.modify_group_join_type_failed, Toast.LENGTH_SHORT).show();
                }

                /*groupViewModel.setGroupJoinType(
                        groupInfo.target,
                        position,
                        null,
                        Collections.singletonList(0),
                        getActivity()
                        )
                    .observe(GroupManageFragment.this, booleanOperateResult -> {
                        if (booleanOperateResult.isSuccess()) {
                            // 这里刷新有问题
                            System.out.println("[GroupManageFragment] UpDescribe success." + text);
                            joinOptionItemView.setDesc((String) text);
                        } else {
                            Toast.makeText(getActivity(), R.string.modify_group_join_type_failed, Toast.LENGTH_SHORT).show();
                        }
                    });*/
            })
            .show();
    }

    @OnClick(R2.id.passOptionItemView)
    void showPassTypeSetting() {
        new MaterialDialog.Builder(getActivity())
                .items(passTypes)
                .itemsCallback((dialog, itemView, position, text) -> {
                    groupViewModel.setGroupPassType(groupInfo.target, position, null, Collections.singletonList(0))
                            .observe(GroupManageFragment.this, booleanOperateResult -> {
                                if (booleanOperateResult.isSuccess()) {
                                    passOptionItemView.setDesc((String) text);
                                } else {
                                    Toast.makeText(getActivity(), R.string.modify_group_verify_type_failed, Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .show();
    }

    @OnClick(R2.id.groupTypeOptionItemView)
    void showTypeSetting() {
        new MaterialDialog.Builder(getActivity())
            .items(R.array.group_type)
            .itemsCallback((dialog, itemView, position, text) -> {
                groupViewModel.setGroupType(groupInfo.target, position, null, Collections.singletonList(0))
                    .observe(GroupManageFragment.this, booleanOperateResult -> {
                        if (booleanOperateResult.isSuccess()) {
                            groupTypeOptionItemView.setDesc((String) text);
                        } else {
                            Toast.makeText(getActivity(), R.string.modify_group_type_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
            })
            .show();
    }

    @OnClick(R2.id.setKeyWord)
    void setKeyWord(){
        Intent intent = new Intent(getActivity(),SetKeyWordActivity.class);
        intent.putExtra("groupId",groupInfo.target);
        startActivity(intent);
    }

    @OnClick(R2.id.searchOptionItemView)
    void showSearchSetting() {
        new MaterialDialog.Builder(getActivity())
            .items(R.array.group_search_type)
            .itemsCallback((dialog, itemView, position, text) -> {
                searchOptionItemView.setDesc((String) text);
            })
            .show();
    }

    @OnClick(R2.id.timeOptionItemView)
    void timeOption(){
        new MaterialDialog.Builder(getActivity())
                .items(timeTypes)
                .itemsCallback((dialog, itemView, position, text) -> {
                    String time = (String) text;
                    timeOptionItemView.setDesc(time);
                    ChatManager.Instance().getWorkHandler().post(new Runnable() {
                        @Override
                        public void run() {


                            String timeInterval = "0";
                            if(time.equals("0 s")){

                            }else if(time.equals("1 s")){
                                timeInterval = "1";
                            }else if(time.equals("3 s")){
                                timeInterval = "3";
                            }else if(time.equals("5 s")){
                                timeInterval = "5";
                            }else if(time.equals("15 s")){
                                timeInterval = "15";
                            }else if(time.equals("30 s")){
                                timeInterval = "30";
                            }else if(time.equals("60 s")){
                                timeInterval = "60";
                            }

                            ChatManager.Instance().upAttribute("TimeInterval", timeInterval, groupInfo.target, new GeneralCallback2() {
                                @Override
                                public void onSuccess(String result) {
                                    System.out.println("@@@    时间设置成功");
                                }

                                @Override
                                public void onFail(int errorCode) {

                                }
                            });
                        }
                    });

                })
                .show();
    }

    @OnClick(R2.id.clearTimeOptionItemView)
    void clearTimeOptionItemView(){
        new MaterialDialog.Builder(getActivity())
                .items(clearTimeTypes)
                .itemsCallback((dialog, itemView, position, text) -> {
                    String time = (String) text;
                    clearTimeOptionItemView.setDesc(time);
                    ChatManager.Instance().getWorkHandler().post(new Runnable() {
                        @Override
                        public void run() {


                            long clearTime = 0;
                            if(time.equals("0")){
                                clearTime = 0;
                            }else if(time.equals("0"+getString(R.string.clear_day))){
                                clearTime = 0;
                            } else if(time.equals("1"+getString(R.string.clear_day))){
                                clearTime = 24 * 3600 * 1000;
                            }else if(time.equals("2"+getString(R.string.clear_day))){
                                clearTime = 48 * 3600 * 1000;
                            }else if(time.equals("7"+getString(R.string.clear_day))){
                                clearTime = 168 * 3600 * 1000;
                            }else if(time.equals("15"+getString(R.string.clear_day))){
                                clearTime = 360 * 3600 * 1000;
                            }else if(time.equals("30"+getString(R.string.clear_day))){
                                clearTime = 720 * 3600 * 1000;
                            }

                            ChatManager.Instance().getClearChats(String.valueOf(clearTime), groupInfo.target, new GeneralCallback2() {
                                @Override
                                public void onSuccess(String result) {
                                    System.out.println("@@@    清空时间设置成功");
                                }

                                @Override
                                public void onFail(int errorCode) {

                                }
                            });
                        }
                    });

                })
                .show();
    }

    private void updateRedPacketGroup(boolean mark) {
        ChatManager.Instance().getOwnerSign(groupInfo.target, new GeneralCallback2() {
            @Override
            public void onSuccess(String result) {
                try {
                    String url_SET_GROUP_URL = (String) SPUtils.get(getActivity(),"SET_GROUP_URL","");
                    JSONObject json = JSON.parseObject(result);
                    json.put("count", groupInfo.memberCount);
                    json.put("language", WfcBaseActivity.getBaseLanguage());
                    OKHttpHelper.postJson(url_SET_GROUP_URL, json.toString(), new SimpleCallback<String>() {
                        @Override
                        public void onSuccess1(String t) {

                        }

                        @Override
                        public void onUiSuccess(String result) {
                            try {
                                JSONObject data = JSON.parseObject(result);
                                if (data.getIntValue("code") != 200) {
                                    updateRedPacketButton.setCheckedNoEvent(!mark);
                                    Toast.makeText(getContext(), data.getString("msg"), Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onUiFailure(int code, String msg) {
                            updateRedPacketButton.setCheckedNoEvent(!mark);
                            Toast.makeText(getContext(), R.string.operation_failure, Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    updateRedPacketButton.setCheckedNoEvent(!mark);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail(int errorCode) {
                getActivity().runOnUiThread(() -> {
                    updateRedPacketButton.setCheckedNoEvent(!mark);
                    Toast.makeText(getContext(), R.string.operation_failure, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
