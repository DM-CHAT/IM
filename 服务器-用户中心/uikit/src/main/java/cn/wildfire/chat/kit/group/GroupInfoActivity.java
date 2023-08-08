/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.group;

import android.app.ProgressDialog;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.wildfire.chat.kit.GlideApp;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.conversation.ConversationActivity;
import cn.wildfire.chat.kit.user.UserViewModel;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.GroupMember;
import cn.wildfirechat.model.NullConversationInfo;
import cn.wildfirechat.model.NullGroupInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GetGroupInfoCallback;

public class GroupInfoActivity extends WfcBaseActivity {
    private String userId;
    private String groupId;
    private GroupInfo groupInfo;
    private boolean isJoined = false;

    private GroupViewModel groupViewModel;
    @BindView(R2.id.groupNameTextView)
    TextView groupNameTextView;
    @BindView(R2.id.portraitImageView)
    ImageView groupPortraitImageView;
    @BindView(R2.id.actionButton)
    Button actionButton;
    @BindView(R2.id.groupJoinText)
    EditText groupJoinText;

    private MaterialDialog dialog;
    private ProgressDialog progressDialog;

    @Override
    protected void afterViews() {
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");
        boolean refresh = intent.getBooleanExtra("refresh", false);
        groupViewModel = ViewModelProviders.of(this).get(GroupViewModel.class);

        groupViewModel.groupInfoUpdateLiveData().observe(this, groupInfos -> {
            for (GroupInfo info : groupInfos) {
                if (info.target.equals(groupId)) {
                    this.groupInfo = info;
                    dismissLoading();
                    updateActionButtonStatus();
                    showGroupInfo(info);
                }
            }
        });
        GroupInfo groupInfo1 = ChatManager.Instance().getGroupInfo(groupId,false);
        //
        String joinTypeStr = groupInfo1.getJoinType();
        if(joinTypeStr == null){
            return;
        }else if(joinTypeStr.equalsIgnoreCase("free")){
            actionButton.setVisibility(View.VISIBLE);
        }else if(joinTypeStr.equalsIgnoreCase("password")){
            actionButton.setVisibility(View.VISIBLE);
        }else if(joinTypeStr.equalsIgnoreCase("verify")){
            actionButton.setVisibility(View.VISIBLE);
        }else{
            actionButton.setVisibility(View.GONE);
        }


        //Conversation.ConversationType type = 0;

        //groupInfo = new NullGroupInfo(groupId);
        //System.out.println("@@@@ group id1 :" +groupId);
        groupInfo = ChatManager.Instance().getGroupInfoFromDB(groupId);
        //System.out.println("@@@@ group name :" +groupInfo.name);
        //System.out.println("@@@@ group id2 :" +groupInfo.target);
        showGroupInfo(groupInfo);
        updateActionButtonStatus();


        // 加一个强制刷新
        if (refresh) {
            // 强制刷新
            joinGroup();
        } else {


            Conversation conversation = new Conversation(Conversation.ConversationType.Group, groupId);
            ConversationInfo conversationInfo = ChatManager.Instance().getConversation(conversation);

            // 会话不为空，且会话显示是成员的时候，进入群聊
            if (conversationInfo != null) {
                if (!(conversationInfo instanceof NullConversationInfo)) {

                    System.out.println("@@@@ group:" + groupId + "    is member :" + conversationInfo.isMember);

                    if (conversationInfo.isMember != 0) {
                        entryGroup();
                        return;
                    }

                }
            }

            joinGroup();
        }





        /*if (conversationInfo == null || conversationInfo instanceof NullConversationInfo){



            progressDialog = new ProgressDialog(this);
            progressDialog.show();

            ChatManager.Instance().getGroupInfo(groupId, true, new GetGroupInfoCallback() {
                @Override
                public void onSuccess(GroupInfo groupInfos) {
                    System.out.println("@@@ getGroupInfo onSuccess.");
                    groupInfo = groupInfos;
                    progressDialog.dismiss();

                    System.out.println("@@@ getGroupInfo isMember : " + groupInfo.isMember);
                    if (groupInfo.isMember != null) {
                        if (groupInfo.isMember.equals("yes")) {
                            isJoined = true;
                        }
                    }
                    updateActionButtonStatus();
                    showGroupInfo(groupInfo);



                    //UserViewModel userViewModel = ViewModelProviders.of(GroupInfoActivity.this).get(UserViewModel.class);
                    //userId = userViewModel.getUserId();

                    *//*if(groupInfo == null || groupInfo instanceof NullGroupInfo){
                        showLoading();
                    } else {
                        updateActionButtonStatus();
                        showGroupInfo(groupInfo);
                    }*//*
                }

                @Override
                public void onFail(int errorCode) {
                    System.out.println("@@@ getGroupInfo onFail.");
                    *//*groupInfo = groupViewModel.getGroupInfo(groupId, true);
                    if(groupInfo == null || groupInfo instanceof NullGroupInfo){
                        showLoading();
                    } else {
                        updateActionButtonStatus();
                        showGroupInfo(groupInfo);
                    }*//*



                    //showLoading();

                    progressDialog.dismiss();
                    Toast.makeText(GroupInfoActivity.this, "time out.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {

            groupInfo = groupViewModel.getGroupInfo(groupId, true);
            if(groupInfo == null || groupInfo instanceof NullGroupInfo){
                // showLoading();
            } else {
                isJoined = true;
                updateActionButtonStatus();
                showGroupInfo(groupInfo);
                //GroupInfoActivity.this.isJoined = true;
            }
        }*/
    }

    private void entryGroup() {
        groupInfo = groupViewModel.getGroupInfo(groupId, true);
        if(groupInfo == null || groupInfo instanceof NullGroupInfo){
            // showLoading();
        } else {
            isJoined = true;
            updateActionButtonStatus();
            showGroupInfo(groupInfo);
            //GroupInfoActivity.this.isJoined = true;
        }
    }

    private void joinGroup() {

        progressDialog = new ProgressDialog(this);
        progressDialog.show();

        ChatManager.Instance().getGroupInfo(groupId, true, new GetGroupInfoCallback() {
            @Override
            public void onSuccess(GroupInfo groupInfos) {
                System.out.println("@@@ getGroupInfo onSuccess.");
                groupInfo = groupInfos;
                progressDialog.dismiss();

                System.out.println("@@@ getGroupInfo isMember : " + groupInfo.isMember);
                if (groupInfo.isMember != null) {
                    if (groupInfo.isMember.equals("yes")) {
                        isJoined = true;
                    }
                }
                updateActionButtonStatus();
                showGroupInfo(groupInfo);
            }

            @Override
            public void onFail(int errorCode) {
                System.out.println("@@@ getGroupInfo onFail.");

                progressDialog.dismiss();
                Toast.makeText(GroupInfoActivity.this, "time out.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateActionButtonStatus() {
        if (isJoined) {
            actionButton.setText(getString(R.string.entry_group));
        } else {
            actionButton.setText(getString(R.string.joins_group));
        }
    }

    private void showLoading() {
        if (dialog == null) {
            dialog = new MaterialDialog.Builder(this)
                .progress(true, 100)
                .build();
            dialog.show();
        }
    }

    private void dismissLoading() {
        if (dialog == null || !dialog.isShowing()) {
            return;
        }
        dialog.dismiss();
        dialog = null;
    }

    private void showGroupInfo(GroupInfo groupInfo) {
        if (groupInfo == null) {
            return;
        }
        System.out.println("@@@@ showGroupInfo group name : " + groupInfo.name);
        GlideApp.with(this)
            .load(groupInfo.portrait)
            .placeholder(R.mipmap.ic_group_cheat)
            .into(groupPortraitImageView);
        groupNameTextView.setText(groupInfo.name);
    }

    @Override
    protected int contentLayout() {
        return R.layout.group_info_activity;
    }

    @OnClick(R2.id.actionButton)
    void action() {
        if (isJoined) {
            Intent intent = ConversationActivity.buildConversationIntent(this, Conversation.ConversationType.Group, groupId, 0);
            startActivity(intent);
            finish();
        } else {
            groupViewModel.joinGroup(this, groupId, groupJoinText.getText().toString()).observe(this, new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    if (aBoolean) {
                        //Intent intent = ConversationActivity.buildConversationIntent(GroupInfoActivity.this, Conversation.ConversationType.Group, groupId, 0);
                        //startActivity(intent);
                        Toast.makeText(GroupInfoActivity.this, "Application has been sent and is awaiting review.", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(GroupInfoActivity.this, R.string.join_grouop_fail, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
