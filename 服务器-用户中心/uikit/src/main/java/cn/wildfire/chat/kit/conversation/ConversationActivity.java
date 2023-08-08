/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversation;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;

import java.io.IOException;

import cn.wildfire.chat.kit.BuildConfig;
import cn.wildfire.chat.kit.GlideApp;
import cn.wildfire.chat.kit.IMServiceStatusViewModel;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.WfcBaseActivity1;
import cn.wildfire.chat.kit.info.RedPacketBombInfo;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ConversationActivity extends WfcBaseActivity1 {
    private boolean isInitialized = false;
    private ConversationFragment conversationFragment;
    private Conversation conversation;
    private RelativeLayout rl_back;
    private TextView tv_name;
    private ImageView iv_title;

    @Override
    protected int contentLayout() {
        return R.layout.fragment_container_activity1;
    }

    private void setConversationBackground() {
        // you can setup your conversation background here
//        getWindow().setBackgroundDrawableResource(R.mipmap.splash);
    }

    @Override
    protected void afterMenus(Menu menu) {
        MenuItem menuItem=menu.findItem(R.id.menu_conversation_info);
        //单聊的时候对方头像替换右上角默认用户详情图标
        if (conversation != null) {
            if (conversation.type == Conversation.ConversationType.Single) {
                UserInfo userInfo = ChatManager.Instance().getUserInfo(conversation.target, false);
                GlideApp
                        .with(this)
                        .load(userInfo.portrait)
                        .transforms(new CircleCrop(), new RoundedCorners(10))
                        .placeholder(R.mipmap.avatar_def)
                        .into(new SimpleTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                menuItem.setIcon(resource);
                            }
                        });
                Glide.with(this)
                        .load(userInfo.portrait)
                        .transform(new CircleCrop(),new RoundedCorners(10))
                        .placeholder(R.mipmap.avatar_def)
                        .into(iv_title);
            }
        }

    }

    @Override
    protected void afterViews() {
        iv_title = findViewById(R.id.iv_title);
        tv_name = findViewById(R.id.tv_name);
        rl_back = findViewById(R.id.rl_back);
        IMServiceStatusViewModel imServiceStatusViewModel = ViewModelProviders.of(this).get(IMServiceStatusViewModel.class);
        imServiceStatusViewModel.imServiceStatusLiveData().observe(this, aBoolean -> {
            if (!isInitialized && aBoolean) {
                init();
                isInitialized = true;
            }
        });
        //init();
        conversationFragment = new ConversationFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.containerFrameLayout, conversationFragment, "content")
                .commit();

        setConversationBackground();

    }

    @Override
    protected int menu() {
        return R.menu.conversation;
    }

    public ConversationFragment getConversationFragment() {
        return conversationFragment;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_conversation_info) {
            showConversationInfo();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!conversationFragment.onBackPressed()) {
            super.onBackPressed();
        }
    }

    private void showConversationInfo() {
        Intent intent = new Intent(this, ConversationInfoActivity.class);
        ConversationInfo conversationInfo = ChatManager.Instance().getConversation(conversation);
        if (conversationInfo == null) {
            Toast.makeText(this, getString(R.string.get_conversation_info_error), Toast.LENGTH_SHORT).show();
            return;
        }
        intent.putExtra("conversationInfo", conversationInfo);
        startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        conversation = intent.getParcelableExtra("conversation");
        if (conversation == null) {
            finish();
        }
        long initialFocusedMessageId = intent.getLongExtra("toFocusMessageId", -1);
        String channelPrivateChatUser = intent.getStringExtra("channelPrivateChatUser");
        conversationFragment.setupConversation(conversation, null, initialFocusedMessageId, channelPrivateChatUser);

    }

    private void init() {
        Intent intent = getIntent();
        conversation = intent.getParcelableExtra("conversation");
        String conversationTitle = intent.getStringExtra("conversationTitle");
        long initialFocusedMessageId = intent.getLongExtra("toFocusMessageId", -1);
        if (conversation == null) {
            finish();
        }
        conversationFragment.setupConversation(conversation, conversationTitle, initialFocusedMessageId, null);

    }

    public static Intent buildConversationIntent(Context context, Conversation.ConversationType type, String target, int line) {
        return buildConversationIntent(context, type, target, line, -1);
    }

    public static Intent buildConversationIntent(Context context, Conversation.ConversationType type, String target, int line, long toFocusMessageId) {
        Conversation conversation = new Conversation(type, target, line);
        return buildConversationIntent(context, conversation, null, toFocusMessageId);
    }

    public static Intent buildConversationIntent(Context context, Conversation.ConversationType type, String target, int line, String channelPrivateChatUser) {
        Conversation conversation = new Conversation(type, target, line);
        return buildConversationIntent(context, conversation, null, -1);
    }

    public static Intent buildConversationIntent(Context context, Conversation conversation, String channelPrivateChatUser, long toFocusMessageId) {
        Intent intent = new Intent(context, ConversationActivity.class);
        intent.putExtra("conversation", conversation);
        intent.putExtra("toFocusMessageId", toFocusMessageId);
        intent.putExtra("channelPrivateChatUser", channelPrivateChatUser);
        return intent;
    }
}
