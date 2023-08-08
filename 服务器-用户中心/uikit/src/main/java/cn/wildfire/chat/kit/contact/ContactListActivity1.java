/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.contact;

import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.LitappInfo;
import cn.wildfirechat.model.UserInfo;

public class ContactListActivity1 extends WfcBaseActivity {
    public static String FILTER_USER_LIST = "filterUserList";

    @Override
    protected int contentLayout() {
        return R.layout.fragment_container_activity;
    }

    @Override
    protected void afterViews() {
        Intent intent = getIntent();
        ArrayList<String> filterUserList = intent.getStringArrayListExtra(FILTER_USER_LIST);
        UserInfo userInfo = intent.getParcelableExtra("userinfo");
        LitappInfo litappInfo = intent.getParcelableExtra("litappInfo");
        GroupInfo groupInfo = intent.getParcelableExtra("groupInfo");
        boolean forward = intent.getBooleanExtra("forward", false);
        ContactListFragment1 fragment = new ContactListFragment1();
        Bundle bundle = new Bundle();
        bundle.putBoolean("pick", intent.getBooleanExtra("pick",false));
        bundle.putStringArrayList(FILTER_USER_LIST, filterUserList);
        if (userInfo != null){
            bundle.putParcelable("userinfo", userInfo);
        }
        if (litappInfo != null){
            bundle.putParcelable("litappInfo", litappInfo);
        }
        if (groupInfo != null){
            bundle.putParcelable("groupInfo", groupInfo);
        }
        if (forward){
            bundle.putBoolean("forward", true);
            ArrayList<Message> messages = getIntent().getParcelableArrayListExtra("messages");
            if (messages == null || messages.isEmpty()) {
                Message message = getIntent().getParcelableExtra("message");
                bundle.putParcelable("message",message);
            }else{
                bundle.putParcelableArrayList("messages", messages);
            }
        }
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerFrameLayout, fragment)
                .commit();
    }
}
