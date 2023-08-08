/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.contact.newfriend;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.contact.ContactViewModel;
import cn.wildfire.chat.kit.group.GroupInfoActivity;
import cn.wildfire.chat.kit.group.GroupViewModel;
import cn.wildfire.chat.kit.user.UserInfoActivity;
import cn.wildfire.chat.kit.user.UserViewModel;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfirechat.model.FriendRequest;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.UserInfo;

import static cn.wildfirechat.model.FriendRequest.RequestType_ApplyMember;
import static cn.wildfirechat.model.FriendRequest.RequestType_Friend;
import static cn.wildfirechat.model.FriendRequest.RequestType_InviteGroup;

public class FriendRequestViewHolder extends RecyclerView.ViewHolder {
    private FriendRequestListFragment fragment;
    private FriendRequestListAdapter adapter;
    private FriendRequest friendRequest;
    private UserViewModel userViewModel;
    private GroupViewModel groupViewModel;
    private ContactViewModel contactViewModel;

    @BindView(R2.id.portraitImageView)
    ImageView portraitImageView;
    @BindView(R2.id.nameTextView)
    TextView nameTextView;
    @BindView(R2.id.introTextView)
    TextView introTextView;
    @BindView(R2.id.acceptButton)
    Button acceptButton;
    @BindView(R2.id.rejectButton)
    Button rejectButton;
    @BindView(R2.id.acceptStatusTextView)
    TextView acceptStatusTextView;

    public FriendRequestViewHolder(FriendRequestListFragment fragment, FriendRequestListAdapter adapter, View itemView) {
        super(itemView);
        this.fragment = fragment;
        this.adapter = adapter;
        ButterKnife.bind(this, itemView);
        userViewModel = ViewModelProviders.of(fragment).get(UserViewModel.class);
        groupViewModel = ViewModelProviders.of(fragment).get(GroupViewModel.class);
        contactViewModel = ViewModelProviders.of(fragment).get(ContactViewModel.class);
    }

    @OnClick(R2.id.acceptButton)
    void accept() {
        if(friendRequest.type == RequestType_Friend) {
            contactViewModel.acceptFriendRequest(friendRequest.target).observe(fragment, aBoolean -> {
                if (aBoolean) {
                    this.friendRequest.status = 1;
                    acceptButton.setVisibility(View.GONE);
                    rejectButton.setVisibility(View.GONE);
                } else {
                    Toast.makeText(fragment.getActivity(), WfcUIKit.getString(R.string.operation_failure), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else{
            contactViewModel.acceptGroupRequest(friendRequest.userID, friendRequest.target).observe(fragment, aBoolean -> {
                if (aBoolean) {
                    this.friendRequest.status = 1;
                    acceptButton.setVisibility(View.GONE);
                    rejectButton.setVisibility(View.GONE);
                } else {
                    Toast.makeText(fragment.getActivity(), WfcUIKit.getString(R.string.operation_failure), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    @OnClick(R2.id.rejectButton)
    void reject() {
        if(friendRequest.type == RequestType_Friend){
            contactViewModel.rejectFriendRequest(friendRequest.target).observe(fragment, aBoolean -> {
                if (aBoolean) {
                    this.friendRequest.status = 1;
                    acceptButton.setVisibility(View.GONE);
                    rejectButton.setVisibility(View.GONE);
                } else {
                    Toast.makeText(fragment.getActivity(), WfcUIKit.getString(R.string.operation_failure), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else{
            contactViewModel.rejectGroupRequest(friendRequest.userID, friendRequest.target).observe(fragment, aBoolean -> {
                if (aBoolean) {
                    this.friendRequest.status = 1;
                    acceptButton.setVisibility(View.GONE);
                    rejectButton.setVisibility(View.GONE);
                } else {
                    Toast.makeText(fragment.getActivity(), WfcUIKit.getString(R.string.operation_failure), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void onBind(FriendRequest friendRequest) {
        this.friendRequest = friendRequest;
        if(friendRequest.type == RequestType_Friend){
            UserInfo userInfo = userViewModel.getUserInfo(friendRequest.target, false);

            if (userInfo != null && !TextUtils.isEmpty(userInfo.displayName)) {
                nameTextView.setText(userInfo.displayName);
            } else {
                nameTextView.setText("<" + friendRequest.target + ">");
            }
            if (!TextUtils.isEmpty(friendRequest.reason)) {
                introTextView.setText(friendRequest.reason);
            }
            if (userInfo != null) {
                Glide.with(fragment).load(userInfo.portrait).apply(new RequestOptions().placeholder(R.mipmap.avatar_def).centerCrop()).into(portraitImageView);
            }
        } else {
            if(friendRequest.type == RequestType_InviteGroup){
                UserInfo userInfo = userViewModel.getUserInfo(friendRequest.originalUser, false);
                GroupInfo groupInfo = groupViewModel.getGroupInfo(friendRequest.target, false);
                nameTextView.setText(groupInfo == null ? "<" + friendRequest.target + ">" : groupInfo.name);
                introTextView.setText((userInfo == null ? WfcUIKit.getString(R.string.user) : userInfo.name) + WfcUIKit.getString(R.string.invite_group));
                if (groupInfo != null) {
                    Glide.with(fragment).load(groupInfo.portrait).apply(new RequestOptions().placeholder(R.mipmap.ic_group_cheat).centerCrop()).into(portraitImageView);
                }
            }else{
                UserInfo userInviter = null;
                if(friendRequest.originalUser != null && !friendRequest.originalUser.isEmpty())
                    userInviter = userViewModel.getUserInfo(friendRequest.originalUser, false);
                UserInfo userInfo = userViewModel.getUserInfo(friendRequest.userID, false);
                GroupInfo groupInfo = groupViewModel.getGroupInfo(friendRequest.target, false);
                nameTextView.setText(userInfo == null ? ("<"+friendRequest.userID+">") : userInfo.displayName);
                String intro;
                if(userInviter == null)
                    intro = (userInfo == null ? friendRequest.userID : userInfo.displayName) + WfcUIKit.getString(R.string.request_join);
                else
                    intro = userInviter.displayName + WfcUIKit.getString(R.string.invite) + (userInfo == null ? friendRequest.userID : userInfo.displayName) + WfcUIKit.getString(R.string.join_group);

                intro = (userInfo == null ? friendRequest.userID : userInfo.displayName) + WfcUIKit.getString(R.string.request_join);
                intro += groupInfo == null ? friendRequest.target : groupInfo.name;
                introTextView.setText(intro);
                if (userInfo != null) {
                    Glide.with(fragment).load(userInfo.portrait).apply(new RequestOptions().placeholder(R.mipmap.avatar_def).centerCrop()).into(portraitImageView);
                }
            }
        }

        // TODO status

        switch (friendRequest.status) {
            case 0:
                acceptButton.setVisibility(View.VISIBLE);
                rejectButton.setVisibility(View.VISIBLE);
                acceptStatusTextView.setVisibility(View.GONE);
                break;
            case 1:
                acceptButton.setVisibility(View.GONE);
                rejectButton.setVisibility(View.GONE);
                acceptStatusTextView.setText(WfcUIKit.getString(R.string.added));
                break;
            default:
                acceptButton.setVisibility(View.GONE);
                rejectButton.setVisibility(View.GONE);
                acceptStatusTextView.setText(WfcUIKit.getString(R.string.rejected));
                break;
        }
    }
    @OnClick(R2.id.portraitImageView)
    void showUserInfo(){
        if(friendRequest.type == RequestType_Friend){
            UserInfo userInfo = userViewModel.getUserInfo(friendRequest.target, false);
            Intent intent = new Intent(fragment.getContext(), UserInfoActivity.class);
            intent.putExtra("userInfo", userInfo);
            fragment.startActivity(intent);
        } else if(friendRequest.type == RequestType_InviteGroup) {
            Intent intent = new Intent(fragment.getContext(), GroupInfoActivity.class);
            intent.putExtra("groupId", friendRequest.target);
            fragment.startActivity(intent);
        } else if(friendRequest.type == RequestType_ApplyMember) {
            UserInfo userInfo = userViewModel.getUserInfo(friendRequest.userID, false);
            Intent intent = new Intent(fragment.getContext(), UserInfoActivity.class);
            intent.putExtra("userInfo", userInfo);
            fragment.startActivity(intent);
        }
    }
}
