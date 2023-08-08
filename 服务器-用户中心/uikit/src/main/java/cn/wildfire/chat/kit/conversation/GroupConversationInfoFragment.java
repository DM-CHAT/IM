/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversation;

import static cn.wildfirechat.message.CardMessageContent.CardType_Channel;
import static cn.wildfirechat.message.CardMessageContent.CardType_Group;
import static cn.wildfirechat.message.CardMessageContent.CardType_Litapp;
import static cn.wildfirechat.message.CardMessageContent.CardType_User;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.kyleduo.switchbutton.SwitchButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.wildfire.chat.kit.AppServiceProvider;
import cn.wildfire.chat.kit.BuildConfig;
import cn.wildfire.chat.kit.ChatManagerHolder;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.WfcScheme;
import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.common.ComplaintActivity;
import cn.wildfire.chat.kit.contact.ContactListActivity;
import cn.wildfire.chat.kit.contact.ContactListActivity1;
import cn.wildfire.chat.kit.conversation.ext.core.ConversationExt;
import cn.wildfire.chat.kit.conversation.file.FileRecordActivity;
import cn.wildfire.chat.kit.conversation.forward.ForwardActivity;
import cn.wildfire.chat.kit.conversation.forward.ForwardPromptView;
import cn.wildfire.chat.kit.conversationlist.ConversationListViewModel;
import cn.wildfire.chat.kit.conversationlist.ConversationListViewModelFactory;
import cn.wildfire.chat.kit.group.AddGroupMemberActivity;
import cn.wildfire.chat.kit.group.GroupAnnouncement;
import cn.wildfire.chat.kit.group.GroupMemberListActivity;
import cn.wildfire.chat.kit.group.GroupViewModel;
import cn.wildfire.chat.kit.group.RemoveGroupMemberActivity;
import cn.wildfire.chat.kit.group.SetGroupAnnouncementActivity;
import cn.wildfire.chat.kit.group.SetGroupNameActivity;
import cn.wildfire.chat.kit.group.manage.GroupManageActivity;
import cn.wildfire.chat.kit.info.RedPacketBombInfo;
import cn.wildfire.chat.kit.litapp.AddanApletsActivity;
import cn.wildfire.chat.kit.net.OKHttpHelper;
import cn.wildfire.chat.kit.net.SimpleCallback;
import cn.wildfire.chat.kit.qrcode.QRCodeActivity;
import cn.wildfire.chat.kit.redpacket.RedPacketUtils;
import cn.wildfire.chat.kit.search.SearchMessageActivity;
import cn.wildfire.chat.kit.tag.TagSelectionActivity;
import cn.wildfire.chat.kit.user.UserInfoActivity;
import cn.wildfire.chat.kit.user.UserViewModel;
import cn.wildfire.chat.kit.utils.SPUtils;
import cn.wildfire.chat.kit.utils.SelfDialog;
import cn.wildfire.chat.kit.web.WebViewActivity;
import cn.wildfire.chat.kit.web.WebViewActivity1;
import cn.wildfire.chat.kit.widget.OptionItemView;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.widget.OptionItemView1;
import cn.wildfirechat.message.CardMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.TextMessageContent;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.message.core.MessageStatus;
import cn.wildfirechat.model.ChannelInfo;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.GroupMember;
import cn.wildfirechat.model.LitappInfo;
import cn.wildfirechat.model.ModifyGroupInfoType;
import cn.wildfirechat.model.NullGroupInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback2;
import cn.wildfirechat.remote.GetGroupMembersCallback;
import cn.wildfirechat.remote.UserSettingScope;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GroupConversationInfoFragment extends Fragment implements ConversationMemberAdapter.OnMemberClickListener, CompoundButton.OnCheckedChangeListener {

    @BindView(R2.id.progressBar)
    ProgressBar progressBar;

    @BindView(R2.id.contentNestedScrollView)
    NestedScrollView contentNestedScrollView;

    // group
    @BindView(R2.id.groupLinearLayout_0)
    LinearLayout groupLinearLayout_0;
    @BindView(R2.id.rl_groupName)
    RelativeLayout rl_groupName;
    @BindView(R2.id.descTextView)
    TextView descTextView;
    @BindView(R2.id.groupQRCodeOptionItemView)
    OptionItemView groupQRCodeOptionItemView;
    @BindView(R2.id.groupNoticeLinearLayout)
    LinearLayout noticeLinearLayout;
    @BindView(R2.id.groupNoticeTextView)
    TextView noticeTextView;
    @BindView(R2.id.groupManageOptionItemView)
    OptionItemView groupManageOptionItemView;
    @BindView(R2.id.groupManageDividerLine)
    View groupManageDividerLine;
    @BindView(R2.id.showAllMemberButton)
    Button showAllGroupMemberButton;

    @BindView(R2.id.groupLinearLayout_1)
    LinearLayout groupLinearLayout_1;
    @BindView(R2.id.myGroupNickNameOptionItemView)
    OptionItemView myGroupNickNameOptionItemView;
    @BindView(R2.id.showGroupMemberAliasSwitchButton)
    SwitchButton showGroupMemberNickNameSwitchButton;

    @BindView(R2.id.quitButton)
    TextView quitGroupButton;
    @BindView(R2.id.reportButton)
    TextView reportButton;

    @BindView(R2.id.markGroupLinearLayout)
    LinearLayout markGroupLinearLayout;
    @BindView(R2.id.markGroupSwitchButton)
    SwitchButton markGroupSwitchButton;

    // common
    @BindView(R2.id.memberRecyclerView)
    RecyclerView memberReclerView;
    @BindView(R2.id.stickTopSwitchButton)
    SwitchButton stickTopSwitchButton;
    @BindView(R2.id.silentSwitchButton)
    SwitchButton silentSwitchButton;
    @BindView(R2.id.ll_groupNotice)
    LinearLayout ll_groupNotice;


    @BindView(R2.id.profitOptionItemView)
    OptionItemView profitOptionItemView;
    @BindView(R2.id.share_cardInfo)
    OptionItemView shareCardInfo;

    private ConversationInfo conversationInfo;
    private ConversationMemberAdapter conversationMemberAdapter;
    private ConversationViewModel conversationViewModel;
    private UserViewModel userViewModel;

    private GroupViewModel groupViewModel;
    private GroupInfo groupInfo;
    // me in group
    private GroupMember groupMember;
    String TAG = GroupConversationInfoFragment.class.getSimpleName();
    private SelfDialog selfDialog;

    public static GroupConversationInfoFragment newInstance(ConversationInfo conversationInfo) {
        GroupConversationInfoFragment fragment = new GroupConversationInfoFragment();
        Bundle args = new Bundle();
        args.putParcelable("conversationInfo", conversationInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        assert args != null;
        conversationInfo = args.getParcelable("conversationInfo");
        assert conversationInfo != null;
    }

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.conversation_info_group_fragment, container, false);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAndShowGroupNotice();
    }

    private void init() {

        if(isEnable()){
            profitOptionItemView.setVisibility(View.VISIBLE);
        }else{
            profitOptionItemView.setVisibility(View.GONE);
        }
        /*ChatManager.Instance().getWorkHandler().post(new Runnable() {
            @Override
            public void run() {
                redPacketBomb(conversationInfo.conversation);
            }
        });*/
        conversationViewModel = WfcUIKit.getAppScopeViewModel(ConversationViewModel.class);
        userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        groupLinearLayout_0.setVisibility(View.VISIBLE);
        groupLinearLayout_1.setVisibility(View.VISIBLE);
        markGroupLinearLayout.setVisibility(View.VISIBLE);
        markGroupSwitchButton.setOnCheckedChangeListener(this);
        quitGroupButton.setVisibility(View.VISIBLE);
        reportButton.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        groupViewModel = ViewModelProviders.of(this).get(GroupViewModel.class);
        System.out.println("@@@ open group info view flush group info.");
        ChatManager.Instance().getWorkHandler().post(new Runnable() {
            @Override
            public void run() {
                groupInfo = groupViewModel.getGroupInfo(conversationInfo.conversation.target, true);
            }
        });

        String myself = ChatManager.Instance().getUserId();

        if (groupInfo == null) {
            groupInfo = new NullGroupInfo(conversationInfo.conversation.target);
        }

        groupMember = ChatManager.Instance().getGroupMember(groupInfo.target, myself);
        if (groupMember == null) {
            groupMember = new GroupMember();
            groupMember.groupId = groupInfo.target;
            groupMember.memberId = myself;
            groupMember.type = GroupMember.GroupMemberType.Normal;
            if (myself.equalsIgnoreCase(groupInfo.owner)) {
                groupMember.type = GroupMember.GroupMemberType.Owner;
            }
            groupMember.mute = 0;
        }

        System.out.println("@@@ group info view, myself id:" + myself);
        System.out.println("@@@ group info view, group owner id:" + groupInfo.owner);
        if (groupMember == null || groupMember.type == GroupMember.GroupMemberType.Removed) {
            Toast.makeText(getActivity(), getString(R.string.no_member_of_group), Toast.LENGTH_SHORT).show();
            getActivity().finish();
            return;
        }

        if(groupMember.type == GroupMember.GroupMemberType.Owner
                || groupMember.type == GroupMember.GroupMemberType.Manager
                || myself.equalsIgnoreCase(groupInfo.owner)
        ){
            ll_groupNotice.setVisibility(View.VISIBLE);
        }else{
            ll_groupNotice.setVisibility(View.GONE);
        }
        loadAndShowGroupMembers(false);
        //    userViewModel.userInfoLiveData().observe(getViewLifecycleOwner(), userInfos -> loadAndShowGroupMembers(false));
        observerFavGroupsUpdate();
        observerGroupInfoUpdate();
        //     observerGroupMembersUpdate();
        // 每次进入群详情页面，刷新一次前25个member
        System.out.println("@@@ open group info view flush member top 25");
        ChatManager.Instance().getGroupMemberZone(groupInfo.target, 0, 25, new GetGroupMembersCallback() {
            @Override
            public void onSuccess(List<GroupMember> groupMembers) {
                //showGroupMembers(groupMembers);
                List<GroupMember> members = ChatManager.Instance().getGroupMembers(groupInfo.target, false);
                showGroupMembers(members);
            }

            @Override
            public void onFail(int errorCode) {

            }
        });
    }

    private boolean isEnable() {
        return ChatManager.Instance().getHideEnable();
        //return false;
    }
    private void observerFavGroupsUpdate() {
        groupViewModel.getFavGroups().observe(getViewLifecycleOwner(), listOperateResult -> {
            if (listOperateResult.isSuccess()) {
                for (GroupInfo info : listOperateResult.getResult()) {
                    if (groupInfo.target.equals(info.target)) {
                        markGroupSwitchButton.setChecked(true);
                        break;
                    }
                }
            }
        });
    }


    private void observerGroupMembersUpdate() {
        groupViewModel.groupMembersUpdateLiveData().observe(getViewLifecycleOwner(), groupMembers -> {
            loadAndShowGroupMembers(false);
        });
    }

    private void observerGroupInfoUpdate() {
        groupViewModel.groupInfoUpdateLiveData().observe(getViewLifecycleOwner(), groupInfos -> {
            for (GroupInfo groupInfo : groupInfos) {
                if (groupInfo.target.equals(this.groupInfo.target)) {
                    this.groupInfo = groupInfo;
                    //     groupNameOptionItemView.setDesc(groupInfo.name);
                    descTextView.setText(groupInfo.name);
                    loadAndShowGroupNotice();
                    //System.out.println("@@@            5555555555555555");
                    loadAndShowGroupMembers(false);
                    break;
                }
            }

        });
    }


    private void loadAndShowGroupMembers(boolean refresh) {
        groupViewModel.getGroupMembersLiveData(conversationInfo.conversation.target, refresh)
                .observe(getViewLifecycleOwner(), groupMembers -> {
                    progressBar.setVisibility(View.GONE);
                    //System.out.println("@@@    groupMembers : " + groupMembers.size());
                    showGroupMembers(groupMembers);
                    showGroupManageViews();
                    contentNestedScrollView.setVisibility(View.VISIBLE);
                });
    }

    private void loadAndShowGroupNotice() {
       /* if (groupInfo.notice == null || groupInfo.notice.isEmpty()) {
            noticeTextView.setVisibility(View.GONE);
        } else {
            noticeTextView.setVisibility(View.VISIBLE);
            noticeTextView.setText(groupInfo.notice);
        }*/
    }

    private void showGroupManageViews() {
        List<GroupMember> managers = ChatManager.Instance().getGroupManagers(groupInfo.target);
        if (groupMember.type == GroupMember.GroupMemberType.Manager
                || groupMember.type == GroupMember.GroupMemberType.Owner
                || groupMember.memberId.equalsIgnoreCase(groupInfo.owner)
        ) {
            groupManageOptionItemView.setVisibility(View.VISIBLE);
            ll_groupNotice.setVisibility(View.VISIBLE);
        }
        for (GroupMember member : managers) {
            if(member.memberId.equals(ChatManager.Instance().getUserId())){
                groupManageOptionItemView.setVisibility(View.VISIBLE);
                ll_groupNotice.setVisibility(View.VISIBLE);
            }
        }

        //showGroupMemberNickNameSwitchButton.setChecked("1".equals(userViewModel.getUserSetting(UserSettingScope.GroupHideNickname, groupInfo.target)));
        showGroupMemberNickNameSwitchButton.setCheckedNoEvent("1".equals(userViewModel.getUserSetting(UserSettingScope.GroupHideNickname, groupInfo.target)));
        showGroupMemberNickNameSwitchButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            userViewModel.setUserSetting(UserSettingScope.GroupHideNickname, groupInfo.target, isChecked ? "1" : "0");
            System.out.println("@@@            666666666666666");
            //    loadAndShowGroupMembers(false);
        });

        myGroupNickNameOptionItemView.setDesc(groupMember.alias);
        //     groupNameOptionItemView.setDesc(groupInfo.name);
        descTextView.setText(groupInfo.name);


        stickTopSwitchButton.setChecked(conversationInfo.isTop);
        silentSwitchButton.setChecked(conversationInfo.isSilent);

        stickTopSwitchButton.setOnCheckedChangeListener(this);
        silentSwitchButton.setOnCheckedChangeListener(this);

        if (groupInfo != null && ChatManagerHolder.gChatManager.getUserId().equals(groupInfo.owner)) {
            quitGroupButton.setText(R.string.delete_and_dismiss);
        } else {
            quitGroupButton.setText(R.string.delete_and_exit);
        }
    }

    private void showGroupMembers(List<GroupMember> groupMembers) {
        if (groupMembers == null || groupMembers.isEmpty()) {
            return;
        }
        String userId = ChatManager.Instance().getUserId();
        List<String> memberIds = new ArrayList<>();
        for (GroupMember member : groupMembers) {
            if (member.memberId.equalsIgnoreCase(groupInfo.owner)) {
                if (memberIds.isEmpty())
                    memberIds.add(member.memberId);
                else {
                    String first = memberIds.set(0, member.memberId);
                    memberIds.add(first);
                }
            } else {
                memberIds.add(member.memberId);
            }
        }

        boolean enableRemoveMember = getEnableRemoveMember(groupInfo, groupMembers, userId);
        boolean enableAddMember = getEnableAddMember(groupInfo, groupMembers, userId);
        /*if (groupInfo.joinType == 2) {
            if (groupMember.type == GroupMember.GroupMemberType.Owner
                    || groupMember.type == GroupMember.GroupMemberType.Manager
                    || groupMember.memberId.equalsIgnoreCase(groupInfo.owner)
            ) {
                enableAddMember = true;
                enableRemoveMember = true;
            }
        } else {
            enableAddMember = true;
            if (groupMember.type != GroupMember.GroupMemberType.Normal || userId.equals(groupInfo.owner)) {
                enableRemoveMember = true;
            }
        }*/
        int maxShowMemberCount = 25;
        if (enableAddMember) {
            maxShowMemberCount--;
        }
        if (enableRemoveMember) {
            maxShowMemberCount--;
        }
        if (memberIds.size() >= maxShowMemberCount) {
            showAllGroupMemberButton.setVisibility(View.VISIBLE);
            memberIds = memberIds.subList(0, maxShowMemberCount);
        }

        /*List<GroupMember> managers = ChatManager.Instance().getGroupManagers(groupInfo.target);
        for (GroupMember member : managers) {
            if(member.memberId.equals(ChatManager.Instance().getUserId())){
                enableRemoveMember = true;
                enableAddMember = true;
            }
        }*/
        /*if (groupMember.type == GroupMember.GroupMemberType.Manager
                || groupMember.type == GroupMember.GroupMemberType.Owner
                || groupMember.memberId.equalsIgnoreCase(groupInfo.owner)
        ) {
            enableRemoveMember = true;
            enableAddMember = true;
        }*/

        conversationMemberAdapter = new ConversationMemberAdapter(conversationInfo, enableAddMember, enableRemoveMember);
        List<UserInfo> members = UserViewModel.getUsers(memberIds, groupInfo.target);

        conversationMemberAdapter.setMembers(members);
        conversationMemberAdapter.setOnMemberClickListener(this);
        memberReclerView.setAdapter(conversationMemberAdapter);
        memberReclerView.setLayoutManager(new GridLayoutManager(getActivity(), 5));
        memberReclerView.setNestedScrollingEnabled(false);
        memberReclerView.setHasFixedSize(true);
        memberReclerView.setFocusable(false);

    }

    private boolean getEnableAddMember(GroupInfo groupInfo, List<GroupMember> groupMembers, String userId) {
        String joinType = groupInfo.getJoinType();

        if (userId.equalsIgnoreCase(groupInfo.owner)) {
            return true;
        }

        if (joinType.equalsIgnoreCase("free")) {
            return true;
        } else if (joinType.equalsIgnoreCase("member")) {
            return true;
        } else if (joinType.equalsIgnoreCase("admin")) {
            if (isAdmin(groupMembers, userId)) {
                return true;
            }
        }

        return false;
    }

    private boolean getEnableRemoveMember(GroupInfo groupInfo, List<GroupMember> groupMembers, String userId) {

        if (userId.equalsIgnoreCase(groupInfo.owner)) {
            return true;
        }

        if (isAdmin(groupMembers, userId)) {
            return true;
        }

        return false;
    }

    private boolean isAdmin(List<GroupMember> groupMembers, String userId) {

        for (GroupMember member : groupMembers) {
            if (member.type == GroupMember.GroupMemberType.Manager) {
                if (userId.equalsIgnoreCase(member.memberId)) {
                    return true;
                }
            }
        }

        return false;
    }


    @OnClick(R2.id.rl_groupName)
    void updateGroupName() {
        /*if (groupInfo.type != GroupInfo.GroupType.Restricted
                || (groupMember.type == GroupMember.GroupMemberType.Manager || groupMember.type == GroupMember.GroupMemberType.Owner)) {
            Intent intent = new Intent(getActivity(), SetGroupNameActivity.class);
            intent.putExtra("groupInfo", groupInfo);
            startActivity(intent);
        }*/
        if (groupMember.type == GroupMember.GroupMemberType.Manager
                || groupMember.type == GroupMember.GroupMemberType.Owner
                ||       groupMember.memberId.equalsIgnoreCase(groupInfo.owner)
        ) {
            Intent intent = new Intent(getActivity(), SetGroupNameActivity.class);
            intent.putExtra("groupInfo", groupInfo);
            startActivity(intent);
        }
    }

    @OnClick(R2.id.groupNoticeLinearLayout)
    void updateGroupNotice() {
       /* if (groupInfo.type != GroupInfo.GroupType.Restricted
                || (groupMember.type == GroupMember.GroupMemberType.Manager || groupMember.type == GroupMember.GroupMemberType.Owner)) {*/
        if (groupInfo.type != GroupInfo.GroupType.Restricted
                || (groupMember.type == GroupMember.GroupMemberType.Owner)
                || groupMember.memberId.equalsIgnoreCase(  groupInfo.owner)
        ) {
            Intent intent = new Intent(getActivity(), SetGroupAnnouncementActivity.class);
            intent.putExtra("groupInfo", groupInfo);
            startActivity(intent);
        }
    }

    @OnClick(R2.id.groupManageOptionItemView)
    void manageGroup() {
        Intent intent = new Intent(getActivity(), GroupManageActivity.class);
        intent.putExtra("groupInfo", groupInfo);
        startActivity(intent);
    }

    @OnClick(R2.id.groupTagOptionItemView)
    void tagOnClick(){
        Intent intent = new Intent(getActivity(), TagSelectionActivity.class);
        intent.putExtra("uid",groupInfo.target);
        startActivity(intent);
    }

    @OnClick(R2.id.showAllMemberButton)
    void showAllGroupMember() {

        if (groupInfo.AllowAddFriend()) {

        } else {
            Toast.makeText(getActivity(), getString(R.string.disable_talk1), Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(getActivity(), GroupMemberListActivity.class);
        intent.putExtra("groupInfo", groupInfo);
        startActivity(intent);
    }



    @OnClick(R2.id.myGroupNickNameOptionItemView)
    void updateMyGroupAlias() {
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .input(getString(R.string.please_input_nickname), groupMember.alias, true, (dialog1, input) -> {
                    if (TextUtils.isEmpty(groupMember.alias)) {
                        if (TextUtils.isEmpty(input.toString().trim())) {
                            return;
                        }
                    } else if (groupMember.alias.equals(input.toString().trim())) {
                        return;
                    }

                    groupViewModel.modifyMyGroupAlias(groupInfo.target, input.toString().trim(), null, Collections.singletonList(0))
                            .observe(GroupConversationInfoFragment.this, operateResult -> {
                                if (operateResult.isSuccess()) {
                                    groupMember.alias = input.toString().trim();
                                    myGroupNickNameOptionItemView.setDesc(input.toString().trim());
                                } else {
                                    Toast.makeText(getActivity(), getString(R.string.modify_nickname_failure) + operateResult.getErrorCode(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .negativeText(getString(R.string.cancel))
                .positiveText(getString(R.string.confirm))
                .onPositive((dialog12, which) -> {
                    dialog12.dismiss();
                })
                .build();
        dialog.show();
    }

    void quitGroup2() {
        groupViewModel.quitGroup(conversationInfo.conversation.target, Collections.singletonList(0), null).observe(GroupConversationInfoFragment.this, aBoolean -> {
            Log.d(TAG, "quitGroup result: " + aBoolean);
            if (aBoolean != null && aBoolean) {
                String MainDapp = (String) SPUtils.get(getContext(),"MainDapp","");
                Intent intent = null;
                if(MainDapp.length() == 0){
                    intent  = new Intent(getContext().getPackageName() + ".main1");
                }else{
                    intent  = new Intent(getContext().getPackageName() + ".main");
                }

                //    Intent intent = new Intent(getContext().getPackageName() + ".main");
                ChatManager.Instance().getWorkHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        ChatManager.Instance().removeConversation(conversationInfo.conversation,false);
                    }
                });
                startActivity(intent);
            } else {
                Toast.makeText(getActivity(), getString(R.string.quit_group_failure), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick(R2.id.quitButton)
    void quitGroup() {
        if (groupInfo == null)
            return;
        AlertDialog.Builder dialog = new AlertDialog.Builder(this.getActivity());
        dialog.setTitle(getString(R.string.string_remind))
                .setMessage(R.string.quit_delete_whether)
                .setNegativeButton(getString(R.string.cancel), (dialog1, which1) -> {
                })
                .setPositiveButton(getString(R.string.confirm), (dialog1, which1) -> {
                    if (userViewModel.getUserId().equals(groupInfo.owner)) {
                        if (groupInfo.redPacket != 0) {
                            JSONObject json = new JSONObject();
                            json.put("groupID", groupInfo.target);
                            Log.d(TAG, "group zero data: " + json.toString());
                            String url_GROUP_ZERO = (String) SPUtils.get(getActivity(),"GROUP_ZERO","");
                            OKHttpHelper.postJsonWithToken(url_GROUP_ZERO, json.toString(),
                                    new SimpleCallback<String>() {
                                        @Override
                                        public void onSuccess1(String t) {

                                        }

                                        @Override
                                        public void onUiSuccess(String s) {
                                            try {
                                                Log.d(TAG, "group zero result: " + s);
                                                JSONObject data = JSON.parseObject(s);
                                                data = data.getJSONObject("data");
                                                String owner = data.getString("osnID");
                                                if (owner == null || owner.isEmpty()) {
                                                    quitGroup2();
                                                    Toast.makeText(GroupConversationInfoFragment.this.getActivity(), getString(R.string.server_parameter_error), Toast.LENGTH_LONG).show();
                                                    return;
                                                }
                                                ChatManager.Instance().setGroupOwner(groupInfo.target, owner, new GeneralCallback2() {
                                                    @Override
                                                    public void onSuccess(String result) {
                                                        System.out.println("@@@              setGroupOwner success: "+result);
                                                        GroupConversationInfoFragment.this.getActivity().runOnUiThread(GroupConversationInfoFragment.this::quitGroup2);
                                                    }

                                                    @Override
                                                    public void onFail(int errorCode) {
                                                        Toast.makeText(GroupConversationInfoFragment.this.getActivity(), getString(R.string.error_hit) + " owner", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onUiFailure(int code, String msg) {
                                        }
                                    });
                        } else {
                            groupViewModel.dismissGroup(conversationInfo.conversation.target, Collections.singletonList(0), null).observe(this, aBoolean -> {
                                if (aBoolean != null && aBoolean) {
                                    String MainDapp = (String) SPUtils.get(getContext(),"MainDapp","");
                                    Intent intent = null;
                                    if(MainDapp.length() == 0){
                                        intent  = new Intent(getContext().getPackageName() + ".main1");
                                    }else{
                                        intent  = new Intent(getContext().getPackageName() + ".main");
                                    }
                                    //    Intent intent = new Intent(getContext().getPackageName() + ".main");
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(getActivity(), getString(R.string.dismiss_group_failure), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        quitGroup2();
                    }
                }).show();
    }

    @OnClick(R2.id.reportButton)
    void reportGroup() {
        Intent intent = new Intent(getContext(), ComplaintActivity.class);
        getActivity().startActivity(intent);
    }

    @OnClick(R2.id.clearMessagesOptionItemView)
    void clearMessage() {

        selfDialog = new SelfDialog(getActivity());
        //getString(R.string.clear_chat_log)
        selfDialog.setTitle(getString(R.string.clear_chat_log));
        selfDialog.setMessage(getString(R.string.clean_chat_whether));
        selfDialog.setYesOnclickListener(getString(R.string.string_confirm), new SelfDialog.onYesOnclickListener() {
            @Override
            public void onYesClick() {
                selfDialog.dismiss();
                conversationViewModel.clearConversationMessage(conversationInfo.conversation);
            }
        });
        selfDialog.setNoOnclickListener(getString(R.string.string_cancel), new SelfDialog.onNoOnclickListener() {
            @Override
            public void onNoClick() {
                selfDialog.dismiss();
            }
        });
        selfDialog.show();
    }

    @OnClick(R2.id.groupQRCodeOptionItemView)
    void showGroupQRCode() {
        String qrCodeValue = WfcScheme.QR_CODE_PREFIX_GROUP + groupInfo.target;
        Intent intent = QRCodeActivity.buildQRCodeIntent(getActivity(), getString(R.string.group_qrcode), groupInfo.portrait, qrCodeValue, groupInfo.target, groupInfo.name,groupInfo.target);
        startActivity(intent);
    }

    @OnClick(R2.id.searchMessageOptionItemView)
    void searchGroupMessage() {
        Intent intent = new Intent(getActivity(), SearchMessageActivity.class);
        intent.putExtra("conversation", conversationInfo.conversation);
        startActivity(intent);
    }

    @OnClick(R2.id.fileRecordOptionItemView)
    void fileRecord() {
        Intent intent = new Intent(getActivity(), FileRecordActivity.class);
        intent.putExtra("conversation", conversationInfo.conversation);
        startActivity(intent);
    }

    @OnClick(R2.id.profitOptionItemView)
    void showProfit() {
        String url_GROUP_PROFIT_URL = (String) SPUtils.get(getActivity(),"GROUP_PROFIT_URL","");
        Intent intent = new Intent(getActivity(), WebViewActivity.class);
        intent.putExtra("url", url_GROUP_PROFIT_URL + groupInfo.target);
        startActivity(intent);
    }

    @OnClick(R2.id.share_cardInfo)
    void shareCardInfo() {
        /*Intent intent = new Intent(getActivity(), ContactListActivity1.class);
        intent.putExtra("groupInfo", groupInfo);
        intent.putExtra("pick", true);
        startActivity(intent);*/

        Message msg = new Message();
        msg.content = new CardMessageContent(
                CardType_Group,
                groupInfo.target,
                groupInfo.name,
                groupInfo.name,
                groupInfo.portrait);
        msg.conversation = this.conversationInfo.conversation;
        msg.sender = ChatManager.Instance().getUserId();
        msg.direction = MessageDirection.Send;
        msg.status = MessageStatus.Sending;
        msg.serverTime = System.currentTimeMillis();

        Intent intent = new Intent(getActivity(), ForwardActivity.class);
        intent.putExtra("message", msg);
        startActivity(intent);


        /*Intent intent = new Intent(getActivity(), ContactListActivity.class);
        ArrayList<String> filterUserList = new ArrayList<>();
        filterUserList.add(conversationInfo.conversation.target);
        intent.putExtra("pick",true);
        intent.putExtra(ContactListActivity.FILTER_USER_LIST, filterUserList);
        startActivity(intent);*/
    }

    @OnClick(R2.id.markGroupSwitchButton)
    void markGroupSwitch(){
        boolean isMarkGroup = markGroupSwitchButton.isChecked();
        System.out.println("@@@    isMarkGroup="+isMarkGroup);
        //markGroup(isMarkGroup);
        groupViewModel.setFavGroup(groupInfo.target, isMarkGroup);
    }

    @Override
    public void onUserMemberClick(UserInfo userInfo) {
        System.out.println("@@@   点击了最顶层群员头像");

        if (groupInfo != null
                && groupInfo.privateChat == 1
                && groupMember.type != GroupMember.GroupMemberType.Owner
                && groupMember.type != GroupMember.GroupMemberType.Manager
                && !userInfo.uid.equals(groupInfo.owner)) {

        }
        if (groupInfo.AllowAddFriend()) {

        } else {
            Toast.makeText(getActivity(), getString(R.string.disable_talk1), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(getActivity(), UserInfoActivity.class);
        intent.putExtra("userInfo", userInfo);
        startActivity(intent);
    }


    @Override
    public void onAddMemberClick() {
        Intent intent = new Intent(getActivity(), AddGroupMemberActivity.class);
        intent.putExtra("groupInfo", groupInfo);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onRemoveMemberClick() {
        if (groupInfo != null) {
            Intent intent = new Intent(getActivity(), RemoveGroupMemberActivity.class);
            intent.putExtra("groupInfo", groupInfo);
            startActivity(intent);
            getActivity().finish();
        }
    }

    private void stickTop(boolean top) {
        ConversationListViewModel conversationListViewModel = ViewModelProviders
                .of(this, new ConversationListViewModelFactory(Arrays.asList(Conversation.ConversationType.Single, Conversation.ConversationType.Group, Conversation.ConversationType.Channel), Arrays.asList(0)))
                .get(ConversationListViewModel.class);
        conversationListViewModel.setConversationTop(conversationInfo, top);
    }

    private void markGroup(boolean mark) {
        if (mark != (groupInfo.fav == 1))
            groupViewModel.setFavGroup(groupInfo.target, mark);
    }

    private void silent(boolean silent) {
        conversationViewModel.setConversationSilent(conversationInfo.conversation, silent);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        /*if (id == R.id.markGroupSwitchButton) {
            markGroup(isChecked);
        } else */if (id == R.id.stickTopSwitchButton) {
            stickTop(isChecked);
        } else if (id == R.id.silentSwitchButton) {
            silent(isChecked);
        }
    }
}
