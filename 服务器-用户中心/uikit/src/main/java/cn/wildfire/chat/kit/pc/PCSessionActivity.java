/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.pc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.OnClick;
import cn.wildfire.chat.kit.ChatManagerHolder;
import cn.wildfire.chat.kit.Config;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.conversation.ConversationActivity;
import cn.wildfire.chat.kit.net.OKHttpHelper;
import cn.wildfirechat.client.Platform;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.PCOnlineInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback;

public class PCSessionActivity extends WfcBaseActivity {

    @BindView(R2.id.kickOffPCButton)
    Button kickOffPCButton;
    @BindView(R2.id.descTextView)
    TextView descTextView;
    @BindView(R2.id.muteImageView)
    ImageView muteImageView;

    private PCOnlineInfo pcOnlineInfo;
    private boolean isMuteWhenPCOnline = false;


    @Override
    protected void beforeViews() {
        pcOnlineInfo = getIntent().getParcelableExtra("pcOnlineInfo");
        if (pcOnlineInfo == null) {
            finish();
        }

    }

    @Override
    protected void afterViews() {
        Platform platform = pcOnlineInfo.getPlatform();
        setTitle(platform.getPlatFormName() + " 已登录");
        kickOffPCButton.setText("退出 " + platform.getPlatFormName() + " 登录");
        descTextView.setText(platform.getPlatFormName() + " 已登录");

        isMuteWhenPCOnline = ChatManager.Instance().isMuteNotificationWhenPcOnline();
        muteImageView.setImageResource(isMuteWhenPCOnline ? R.mipmap.ic_turn_off_ringer_hover : R.mipmap.ic_turn_off_ringer);
    }

    @Override
    protected int contentLayout() {
        return R.layout.pc_session_activity;
    }

     private void logout(Activity activity) {
        //不要清除session，这样再次登录时能够保留历史记录。如果需要清除掉本地历史记录和服务器信息这里使用true
        ChatManagerHolder.gChatManager.disconnect(true, false);
        SharedPreferences sp = activity.getSharedPreferences("config", Context.MODE_PRIVATE);
        sp.edit().clear().apply();

        sp = activity.getSharedPreferences("moment", Context.MODE_PRIVATE);
        sp.edit().clear().apply();

        OKHttpHelper.clearCookies();

         System.exit(0);

        Intent intent = new Intent(activity, PCSessionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    @OnClick(R2.id.kickOffPCButton)
    void kickOffPC() {
        ChatManager.Instance().kickoffPCClient(pcOnlineInfo.getClientId(), new GeneralCallback() {
            @Override
            public void onSuccess() {
                if (isFinishing()) {
                    return;
                }
                Toast.makeText(PCSessionActivity.this, pcOnlineInfo.getPlatform() + " 已踢下线", Toast.LENGTH_SHORT).show();
                logout(PCSessionActivity.this);
            }

            @Override
            public void onFail(int errorCode) {
                if (isFinishing()) {
                    return;
                }
                Toast.makeText(PCSessionActivity.this, "" + errorCode, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick(R2.id.muteImageView)
    void mutePhone() {
        ChatManager.Instance().muteNotificationWhenPcOnline(!isMuteWhenPCOnline, new GeneralCallback() {
            @Override
            public void onSuccess() {
                if (isFinishing()) {
                    return;
                }
                Toast.makeText(PCSessionActivity.this, "操作成功", Toast.LENGTH_SHORT).show();
                isMuteWhenPCOnline = !isMuteWhenPCOnline;
                muteImageView.setImageResource(isMuteWhenPCOnline ? R.mipmap.ic_turn_off_ringer_hover : R.mipmap.ic_turn_off_ringer);
            }

            @Override
            public void onFail(int errorCode) {
                if (isFinishing()) {
                    return;
                }
                Toast.makeText(PCSessionActivity.this, "操作失败 " + errorCode, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick(R2.id.fileHelperImageView)
    void fileHelper() {
        Intent intent = ConversationActivity.buildConversationIntent(this, Conversation.ConversationType.Single, Config.FILE_TRANSFER_ID, 0);
        startActivity(intent);
    }
}
