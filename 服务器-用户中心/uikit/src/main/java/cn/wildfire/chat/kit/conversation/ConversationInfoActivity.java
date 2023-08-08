/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversation;

import android.content.Intent;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.litapp.LitappActivity;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.LitappInfo;
import cn.wildfirechat.remote.ChatManager;

public class ConversationInfoActivity extends WfcBaseActivity {

    private ConversationInfo conversationInfo;

    @Override
    protected int contentLayout() {
        return R.layout.fragment_container_activity;
    }

    @Override
    protected void afterViews() {
        conversationInfo = getIntent().getParcelableExtra("conversationInfo");
        Fragment fragment = null;
        switch (conversationInfo.conversation.type) {
            case Single:
                fragment = SingleConversationInfoFragment.newInstance(conversationInfo);
                break;
            case Group:
                fragment = GroupConversationInfoFragment.newInstance(conversationInfo);
                break;
            case ChatRoom:
                // TODO
                break;
            case Channel:
                fragment = ChannelConversationInfoFragment.newInstance(conversationInfo);
                break;
            case Service:
                String target = conversationInfo.conversation.target;
                if(target.equals("") || target == null){

                }else{
                    LitappInfo litappInfo = null;// = ChatManager.Instance().getLitappInfo(target,false);
                    String walletInfo = ChatManager.Instance().getWallet(target);
                    if (walletInfo == null) {
                        litappInfo = ChatManager.Instance().getCollectLitapp(target);
                        if (litappInfo == null) {
                            litappInfo = ChatManager.Instance().getLitappInfo(target,false);
                        }
                    } else {
                        com.alibaba.fastjson.JSONObject walletJson = com.alibaba.fastjson.JSONObject.parseObject(walletInfo);
                        litappInfo = new LitappInfo(walletJson);
                    }

                    Intent intent = new Intent(ConversationInfoActivity.this, LitappActivity.class);
                    intent.putExtra("litappInfo",litappInfo);
                    startActivity(intent);
                    finish();
                    return;
                }
                break;
            default:
                break;
        }
        if (fragment == null) {
            Toast.makeText(this, "todo", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerFrameLayout, fragment)
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
