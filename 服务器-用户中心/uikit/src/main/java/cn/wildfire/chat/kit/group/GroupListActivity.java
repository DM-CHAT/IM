/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.channel.ChannelListFragment;
import cn.wildfire.chat.kit.conversation.CreateConversationActivity;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.UserInfo;

public class GroupListActivity extends WfcBaseActivity {

    private boolean forResult;
    private boolean pick;
    /**
     * intent里面置为{@code true}时，返回groupInfo，不直接打开群会话界面
     */
    public static final String INTENT_FOR_RESULT = "forResult";

    /**
     * for result时，单选，还是多选？
     */
    // TODO
    public static final String MODE_SINGLE = "single";
    public static final String MODE_MULTI = "multi";
    private ImageView iv_add;
    private UserInfo userInfo;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        pick = getIntent().getBooleanExtra("pick", false);
        userInfo = getIntent().getParcelableExtra("userinfo");
        super.onCreate(savedInstanceState);

        iv_add = findViewById(R.id.iv_add);
        iv_add.setVisibility(View.VISIBLE);
        iv_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupListActivity.this, CreateConversationActivity.class);
                startActivity(intent);
            }
        });
    }

    // TODO activity or fragment?
    public static Intent buildIntent(boolean pickForResult, boolean isMultiMode) {

        return null;
    }

    @Override
    protected int contentLayout() {
        return R.layout.fragment_container_activity;
    }


    @Override
    protected void afterViews() {
        if(pick){
            Bundle bundle = new Bundle();
            bundle.putBoolean("pick", pick);
            if (userInfo != null){
                bundle.putParcelable("userinfo", userInfo);
            }
            GroupListFragment fragment = new GroupListFragment();
            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerFrameLayout, fragment)
                    .commit();
        }
        else {
            forResult = getIntent().getBooleanExtra(INTENT_FOR_RESULT, false);
            GroupListFragment fragment = new GroupListFragment();
            if (forResult) {
                fragment.setOnGroupItemClickListener(groupInfo -> {
                    Intent intent = new Intent();
                    // TODO 多选
                    ArrayList<GroupInfo> groupInfos = new ArrayList<>();
                    groupInfos.add(groupInfo);
                    intent.putParcelableArrayListExtra("groupInfos", groupInfos);
                    setResult(RESULT_OK, intent);
                    finish();
                });
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerFrameLayout, fragment)
                    .commit();
        }
    }
}
