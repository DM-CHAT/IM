/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.contact.newfriend;

import static cn.wildfirechat.model.FriendRequest.RequestType_Friend;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.wildfire.chat.kit.contact.ContactViewModel;
import cn.wildfire.chat.kit.group.GroupViewModel;
import cn.wildfire.chat.kit.user.UserViewModel;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfirechat.model.FriendRequest;

public class FriendRequestListFragment extends Fragment {
    @BindView(R2.id.noNewFriendLinearLayout)
    LinearLayout noNewFriendLinearLayout;
    @BindView(R2.id.newFriendListLinearLayout)
    LinearLayout newFriendLinearLayout;
    @BindView(R2.id.friendRequestListRecyclerView)
    RecyclerView recyclerView;
    @BindView(R2.id.iv_default)
    ImageView iv_default;

    private ContactViewModel contactViewModel;
    private FriendRequestListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contact_new_friend_fragment, container, false);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

    private void init() {
        contactViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);
        UserViewModel userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        userViewModel.userInfoLiveData().observe(getActivity(), userInfos -> {
            if (adapter != null) {
                adapter.onUserInfosUpdate(userInfos);
            }
        });
        GroupViewModel groupViewModel = ViewModelProviders.of(this).get(GroupViewModel.class);
        groupViewModel.groupInfoUpdateLiveData().observe(getActivity(), groupInfos -> {
            if (adapter != null) {
                adapter.onGroupInfosUpdate(groupInfos);
            }
        });

        List<FriendRequest> requests = contactViewModel.getFriendRequest();
        /*List<String> listTarget = new ArrayList<>();
        for(int i=0;i<requests.size();i++){
            if(requests.get(i).target.startsWith("OSNG")){
                listTarget.add(requests.get(i).originalUser);
            }else{
                listTarget.add(requests.get(i).target);
            }
        }
        if(listTarget != null){
            for (int i = 0; i < listTarget.size() - 1; i++) {
                for (int j = listTarget.size() - 1; j > i; j--) {
                    if (listTarget.get(j).equals(listTarget.get(i))) {
                        if(requests.size()>j){
                            requests.remove(j);
                        }
                    }
                }
            }
        }*/

        if (requests != null && requests.size() > 0) {
       //     noNewFriendLinearLayout.setVisibility(View.GONE);
            newFriendLinearLayout.setVisibility(View.VISIBLE);
            iv_default.setVisibility(View.GONE);
            adapter = new FriendRequestListAdapter(FriendRequestListFragment.this);
            adapter.setFriendRequests(requests);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(adapter);
        } else {
       //     noNewFriendLinearLayout.setVisibility(View.VISIBLE);
            newFriendLinearLayout.setVisibility(View.GONE);
            iv_default.setVisibility(View.VISIBLE);
        }
        contactViewModel.clearUnreadFriendRequestStatus();
    }
}
