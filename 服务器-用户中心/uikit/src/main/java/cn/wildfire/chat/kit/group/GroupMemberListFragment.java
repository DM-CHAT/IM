/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.group;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.wildfire.chat.kit.user.UserInfoActivity;
import cn.wildfire.chat.kit.user.UserViewModel;
import cn.wildfire.chat.kit.widget.ProgressFragment;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.GroupMember;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GetGroupMembersCallback;

public class GroupMemberListFragment extends ProgressFragment implements GroupMemberListAdapter.OnMemberClickListener {
    String TAG = GroupMemberListFragment.class.getSimpleName();
    private GroupInfo groupInfo;
    private GroupMemberListAdapter groupMemberListAdapter;
    private int startIndex;
    private int blockSize = 50;

    @BindView(R2.id.memberRecyclerView)
    RecyclerView memberRecyclerView;

    public static GroupMemberListFragment newInstance(GroupInfo groupInfo) {
        Bundle args = new Bundle();
        args.putParcelable("groupInfo", groupInfo);
        GroupMemberListFragment fragment = new GroupMemberListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        groupInfo = getArguments().getParcelable("groupInfo");
    }

    @Override
    protected int contentLayout() {
        return R.layout.group_member_list;
    }

    @Override
    protected void afterViews(View view) {
        super.afterViews(view);
        ButterKnife.bind(this, view);
        groupMemberListAdapter = new GroupMemberListAdapter(groupInfo);
        memberRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 5));
        memberRecyclerView.setAdapter(groupMemberListAdapter);
        groupMemberListAdapter.setOnMemberClickListener(this);
        UserViewModel userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
    //    userViewModel.userInfoLiveData().observe(this, userInfos -> loadAndShowGroupMembers());
        loadAndShowGroupMembers();
    }
    private void loadMemberBlock(int beginIndex){
        if (beginIndex >= groupInfo.memberCount) {
            return;
        }
        int count = Math.min(groupInfo.memberCount - startIndex, blockSize);
        Log.d(TAG, "loadMemberBlock index: "+startIndex+", count: "+count);
        if(count <= 0)
            return;

        int endIndex = beginIndex + blockSize;

        // 从数据库里读出来

        GroupViewModel groupViewModel = ViewModelProviders.of(getActivity()).get(GroupViewModel.class);


        //System.out.println("@@@@ loadMemberBlock 1 begin : " + beginIndex);
        groupViewModel.getGroupMemberFromDB(getContext(), groupInfo.target, beginIndex, endIndex)
                .observe(this, uiUserInfos -> {

                    if (uiUserInfos == null) {
                        //Log.d(TAG, "uiUserInfos == null");
                        return;
                    }
                    System.out.println("@@@@ getGroupMemberFromDB size" + uiUserInfos.size());
                    // Log.d(TAG, "getGroupMemberZoneLiveData size: " + uiUserInfos.size());
                    if (uiUserInfos.size() == 0) {
                        return;
                    }
                    showContent();
                    groupMemberListAdapter.addMembers(uiUserInfos);
                    groupMemberListAdapter.notifyDataSetChanged();

                    getActivity().setTitle(getString(R.string.member_list) + "(" + groupMemberListAdapter.getMemberShowCount() + ")");




                    /*startIndex += blockSize;
                    if(startIndex >= groupInfo.memberCount)
                        loadMemberBlock();*/

                });


        // 执行更新get member zone
        // System.out.println("@@@@ loadMemberBlock 2 begin : " + beginIndex);
        groupViewModel.getGroupMemberZoneLiveData(getActivity(),groupInfo.target, beginIndex, blockSize);





        /*groupViewModel.getGroupMemberZoneLiveData(getActivity(),groupInfo.target, beginIndex, count).observe(this, uiUserInfos -> {
            //System.out.println("@@@         uiUserInfos="+uiUserInfos);
            if(uiUserInfos == null){
                Log.d(TAG, "uiUserInfos == null");
                return;
            }
            //System.out.println("@@@         .size()="+uiUserInfos.size());
            Log.d(TAG, "getGroupMemberZoneLiveData size: "+uiUserInfos.size());
            if(uiUserInfos.size() == 0){
                return;
            }
            showContent();
            groupMemberListAdapter.addMembers(uiUserInfos);
            groupMemberListAdapter.notifyDataSetChanged();
            startIndex += uiUserInfos.size();
            getActivity().setTitle(getString(R.string.member_list)+"("+startIndex+")");
            if(count == uiUserInfos.size())
                loadMemberBlock();

        });*/
    }
    private void loadAndShowGroupMembers() {
//        GroupViewModel groupViewModel = ViewModelProviders.of(getActivity()).get(GroupViewModel.class);
//        groupViewModel.getGroupMemberUserInfosLiveData(groupInfo.target, false).observe(this, uiUserInfos -> {
//            showContent();
//            groupMemberListAdapter.setMembers(uiUserInfos);
//            groupMemberListAdapter.notifyDataSetChanged();
//        });
        startIndex = 0;
        groupMemberListAdapter.clearMembers();

        System.out.println("@@@@ clean > count members in db");

        ChatManager.Instance().getWorkHandler().post(() -> {
            ChatManager.Instance().clearOutGroupMembers(groupInfo.target, groupInfo.memberCount);
        });

        int beginIndex = 0;
        while (beginIndex < groupInfo.memberCount) {
            loadMemberBlock(beginIndex);
            beginIndex += blockSize;
        }

        showContent();
    }

    @Override
    public void onUserMemberClick(UserInfo userInfo) {
        GroupMember groupMember = ChatManager.Instance().getGroupMember(groupInfo.target, ChatManager.Instance().getUserId());
        if (groupInfo != null && groupInfo.privateChat == 1 && groupMember.type == GroupMember.GroupMemberType.Normal) {
            return;
        }
        Intent intent = new Intent(getActivity(), UserInfoActivity.class);
        intent.putExtra("userInfo", userInfo);
        startActivity(intent);
    }
}
