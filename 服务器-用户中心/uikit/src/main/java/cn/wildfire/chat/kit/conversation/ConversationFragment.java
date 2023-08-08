/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import cn.wildfire.chat.kit.BuildConfig;
import cn.wildfire.chat.kit.ChatManagerHolder;
import cn.wildfire.chat.kit.GlideApp;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.channel.ChannelViewModel;
import cn.wildfire.chat.kit.chatroom.ChatRoomViewModel;
import cn.wildfire.chat.kit.common.OperateResult;
import cn.wildfire.chat.kit.conversation.ext.core.ConversationExtension;
import cn.wildfire.chat.kit.conversation.mention.MentionSpan;
import cn.wildfire.chat.kit.conversation.message.model.UiMessage;
import cn.wildfire.chat.kit.conversation.multimsg.MultiMessageAction;
import cn.wildfire.chat.kit.conversation.multimsg.MultiMessageActionManager;
import cn.wildfire.chat.kit.conversation.top.TopMessageActivity;
import cn.wildfire.chat.kit.group.ActivityGroupAnnounceMent;
import cn.wildfire.chat.kit.group.GroupViewModel;
import cn.wildfire.chat.kit.group.PickGroupMemberActivity;
import cn.wildfire.chat.kit.group.manage.KeyWordListInfo;
import cn.wildfire.chat.kit.group.manage.SetKeyWordActivity;
import cn.wildfire.chat.kit.info.AddanApletsInfo;
import cn.wildfire.chat.kit.info.RedPacketBombInfo;
import cn.wildfire.chat.kit.info.ShowApletsInfo;
import cn.wildfire.chat.kit.litapp.LitappActivity;
import cn.wildfire.chat.kit.third.utils.UIUtils;
import cn.wildfire.chat.kit.user.UserInfoActivity;
import cn.wildfire.chat.kit.user.UserViewModel;
import cn.wildfire.chat.kit.utils.SPUtils;
import cn.wildfire.chat.kit.viewmodel.MessageViewModel;
import cn.wildfire.chat.kit.viewmodel.SettingViewModel;
import cn.wildfire.chat.kit.widget.InputAwareLayout;
import cn.wildfire.chat.kit.widget.KeyboardAwareLinearLayout;
//import cn.wildfirechat.avenginekit.AVEngineKit;
import cn.wildfirechat.message.CallMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.MessageContent;
import cn.wildfirechat.message.TypingMessageContent;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.message.core.PersistFlag;
import cn.wildfirechat.message.notification.TipNotificationContent;
import cn.wildfirechat.model.ChannelInfo;
import cn.wildfirechat.model.ChatRoomInfo;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.GroupMember;
import cn.wildfirechat.model.LitappInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback2;
import cn.wildfirechat.remote.UserSettingScope;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ConversationFragment extends Fragment implements
        KeyboardAwareLinearLayout.OnKeyboardShownListener,
        KeyboardAwareLinearLayout.OnKeyboardHiddenListener,
        ConversationMessageAdapter.OnPortraitClickListener,
        ConversationMessageAdapter.OnPortraitLongClickListener,
        ConversationInputPanel.OnConversationInputPanelStateChangeListener,
        ConversationMessageAdapter.OnMessageCheckListener, ConversationMessageAdapter.OnMessageReceiptClickListener {

    private static final String TAG = "convFragment";

    public static final int REQUEST_PICK_MENTION_CONTACT = 100;
    public static final int REQUEST_CODE_GROUP_VIDEO_CHAT = 101;
    public static final int REQUEST_CODE_GROUP_AUDIO_CHAT = 102;

    private Conversation conversation;
    private boolean loadingNewMessage;
    private boolean shouldContinueLoadNewMessage = false;

    private static final int MESSAGE_LOAD_COUNT_PER_TIME = 20;
    private static final int MESSAGE_LOAD_AROUND = 10;

    @BindView(R2.id.rootLinearLayout)
    InputAwareLayout rootLinearLayout;
    @BindView(R2.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R2.id.msgRecyclerView)
    RecyclerView recyclerView;


    @BindView(R2.id.inputPanelFrameLayout)
    ConversationInputPanel inputPanel;

    @BindView(R2.id.multiMessageActionContainerLinearLayout)
    LinearLayout multiMessageActionContainerLinearLayout;

    @BindView(R2.id.unreadCountLinearLayout)
    LinearLayout unreadCountLinearLayout;
    @BindView(R2.id.unreadCountTextView)
    TextView unreadCountTextView;
    @BindView(R2.id.unreadMentionCountTextView)
    TextView unreadMentionCountTextView;
    @BindView(R2.id.groupNotice)
    TextView groupNotice;
    @BindView(R2.id.rl_groupNotice)
    RelativeLayout rl_groupNotice;
    @BindView(R2.id.rl_aplets)
    RelativeLayout rl_aplets;
    @BindView(R2.id.rl_aplets_open)
    RelativeLayout rl_aplets_open;
    @BindView(R2.id.iv_aplets)
    ImageView iv_aplets;
    @BindView(R2.id.rl_webView)
    RelativeLayout rl_webView;
    @BindView(R2.id.litappWebview)
    WebView mWebView;
    @BindView(R2.id.iv_close)
    ImageView iv_close;
    @BindView(R2.id.iv_fullscreen)
    ImageView iv_fullscreen;
    @BindView(R2.id.rl_topMessage)
    RelativeLayout rl_topMessage;
    @BindView(R2.id.tv_topMessage1)
    TextView tv_topMessage1;
    @BindView(R2.id.tv_topMessage2)
    TextView tv_topMessage2;
    @BindView(R2.id.iv_delete)
    ImageView iv_delete;
    @BindView(R2.id.ll_topMessage1)
    LinearLayout ll_topMessage1;
    @BindView(R2.id.ll_topMessage2)
    LinearLayout ll_topMessage2;


    private ConversationMessageAdapter adapter;
    private boolean moveToBottom = true;
    private ConversationViewModel conversationViewModel;
    private SettingViewModel settingViewModel;
    private MessageViewModel messageViewModel;
    private UserViewModel userViewModel;
    private GroupViewModel groupViewModel;
    private ChatRoomViewModel chatRoomViewModel;

    private Handler handler;
    private long initialFocusedMessageId;
    private long firstUnreadMessageId;
    // 用户channel主发起，针对某个用户的会话
    private String channelPrivateChatUser;
    private String conversationTitle = "";
    private LinearLayoutManager layoutManager;

    // for group
    private GroupInfo groupInfo;
    private GroupMember groupMember;
    private boolean showGroupMemberName = false;
    private Observer<List<GroupMember>> groupMembersUpdateLiveDataObserver;
    private Observer<List<GroupInfo>> groupInfosUpdateLiveDataObserver;
    private Observer<Object> settingUpdateLiveDataObserver;

    private String token = null;
    private ValueCallback<Uri[]> uploadMessageAboveL;
    /**
     * 视频全屏参数
     */
    protected static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    private View customView;
    private FrameLayout fullscreenContainer;
    private WebChromeClient.CustomViewCallback customViewCallback;

    boolean isLogin = false;
    boolean isSysExit = false;
    boolean isJumpOut = false;
    private final static int FILE_CHOOSER_RESULT_CODE = 10000;
    private String dappUrlFront = null;
    private LitappInfo litappInfo = new LitappInfo();
    private List<LitappInfo> litappInfos;
    private String shareId;

    private Observer<UiMessage> messageLiveDataObserver = new Observer<UiMessage>() {
        @Override
        public void onChanged(@Nullable UiMessage uiMessage) {
            if (!isMessageInCurrentConversation(uiMessage)) {
                return;
            }
            if (isNoSavedCallMessage(uiMessage))
                return;
            MessageContent content = uiMessage.message.content;
            if (isDisplayableMessage(uiMessage)) {
                // 消息定位时，如果收到新消息、或者发送消息，需要重新加载消息列表
                if (shouldContinueLoadNewMessage) {
                    shouldContinueLoadNewMessage = false;
                    reloadMessage();
                    return;
                }
                adapter.addNewMessage(uiMessage);
                if (moveToBottom || uiMessage.message.sender.equals(ChatManager.Instance().getUserId())) {
                    UIUtils.postTaskDelay(() -> {

                                int position = adapter.getItemCount() - 1;
                                if (position < 0) {
                                    return;
                                }
                                recyclerView.scrollToPosition(position);
                            },
                            100);
                }
            }
            if (content instanceof TypingMessageContent && uiMessage.message.direction == MessageDirection.Receive) {
                updateTypingStatusTitle((TypingMessageContent) content);
            } else {
                resetConversationTitle();
            }

            if (getLifecycle().getCurrentState() == Lifecycle.State.RESUMED && uiMessage.message.direction == MessageDirection.Receive) {
                conversationViewModel.clearUnreadStatus(conversation);
            }
        }
    };
    private Observer<UiMessage> messageUpdateLiveDatObserver = new Observer<UiMessage>() {
        @Override
        public void onChanged(@Nullable UiMessage uiMessage) {
            if (!isMessageInCurrentConversation(uiMessage)) {
                return;
            }
            if (isDisplayableMessage(uiMessage)) {
                adapter.updateMessage(uiMessage);
            }
        }
    };

    public void test(int messageUid) {
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                recyclerView.scrollToPosition(messageUid);
            }
        });

    }

    private Observer<UiMessage> messageRemovedLiveDataObserver = new Observer<UiMessage>() {
        @Override
        public void onChanged(@Nullable UiMessage uiMessage) {
            // 当通过server api删除消息时，只知道消息的uid
            if (uiMessage.message.conversation != null && !isMessageInCurrentConversation(uiMessage)) {
                return;
            }
            if (uiMessage.message.messageId == 0 || isDisplayableMessage(uiMessage)) {
                adapter.removeMessage(uiMessage);
            }
        }
    };

    private boolean isDisplayableMessage(UiMessage uiMessage) {
        MessageContent content = uiMessage.message.content;
        if (content.getPersistFlag() == PersistFlag.Persist
                || content.getPersistFlag() == PersistFlag.Persist_And_Count) {
            return true;
        }
        return false;
    }

    private Observer<Map<String, String>> mediaUploadedLiveDataObserver = new Observer<Map<String, String>>() {
        @Override
        public void onChanged(@Nullable Map<String, String> stringStringMap) {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("sticker", Context.MODE_PRIVATE);
            for (Map.Entry<String, String> entry : stringStringMap.entrySet()) {
                sharedPreferences.edit()
                        .putString(entry.getKey(), entry.getValue())
                        .apply();
            }

        }
    };

    private Observer<Conversation> clearConversationMessageObserver = new Observer<Conversation>() {
        @Override
        public void onChanged(Conversation conversation) {
            if (conversation.equals(ConversationFragment.this.conversation)) {
                adapter.setMessages(null);
                adapter.notifyDataSetChanged();
            }
        }
    };

    private Observer<List<UserInfo>> userInfoUpdateLiveDataObserver = new Observer<List<UserInfo>>() {
        @Override
        public void onChanged(@Nullable List<UserInfo> userInfos) {
            if (conversation == null) {
                return;
            }
            if (conversation.type == Conversation.ConversationType.Single) {
                conversationTitle = null;
                setTitle();
            }
            int start = layoutManager.findFirstVisibleItemPosition();
            int end = layoutManager.findLastVisibleItemPosition();
            adapter.notifyItemRangeChanged(start, end - start + 1, userInfos);
        }
    };

    private void initGroupObservers() {
        groupMembersUpdateLiveDataObserver = groupMembers -> {
            if (groupMembers == null || groupInfo == null) {
                return;
            }
            for (GroupMember member : groupMembers) {
                if (member.groupId.equals(groupInfo.target) && member.memberId.equals(userViewModel.getUserId())) {
                    groupMember = member;
                    updateGroupMuteStatus();
                    break;
                }
            }
        };

        groupInfosUpdateLiveDataObserver = groupInfos -> {
            if (groupInfo == null || groupInfos == null) {
                return;
            }
            for (GroupInfo info : groupInfos) {
                if (info.target.equals(groupInfo.target)) {
                    groupInfo = info;
                    updateGroupMuteStatus();
                    setTitle();
                    adapter.notifyDataSetChanged();
                    break;
                }
            }
        };


        groupViewModel.groupInfoUpdateLiveData().observeForever(groupInfosUpdateLiveDataObserver);
        groupViewModel.groupMembersUpdateLiveData().observeForever(groupMembersUpdateLiveDataObserver);
    }

    private void unInitGroupObservers() {
        if (groupViewModel == null) {
            return;
        }
        groupViewModel.groupInfoUpdateLiveData().removeObserver(groupInfosUpdateLiveDataObserver);
        groupViewModel.groupMembersUpdateLiveData().removeObserver(groupMembersUpdateLiveDataObserver);
    }

    private boolean isMessageInCurrentConversation(UiMessage message) {
        if (conversation == null || message == null || message.message == null) {
            return false;
        }
        return conversation.equals(message.message.conversation);
    }
    private boolean isNoSavedCallMessage(UiMessage message){
        if (conversation == null || message == null || message.message == null || message.message.content == null) {
            return true;
        }
        if(!(message.message.content instanceof CallMessageContent))
            return false;
        CallMessageContent call = (CallMessageContent) message.message.content;
        return call.duration == -1;
    }

    public ConversationInputPanel getConversationInputPanel() {
        return inputPanel;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.conversation_activity, container, false);
        ButterKnife.bind(this, view);

        SharedPreferences sp = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
        token = sp.getString("token", null);
        String hostip = sp.getString("hostip", null);
        ChatManager.init(getActivity().getApplication(), hostip);


        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setBuiltInZoomControls(false);
        mWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setAllowContentAccess(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setDatabaseEnabled(true);
        mWebView.getSettings().setGeolocationEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
        mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        mWebView.addJavascriptInterface(this, "osnsdk");
        mWebView.setWebChromeClient(new WebChromeClient() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                             FileChooserParams fileChooserParams) {
                //Log.d(TAG, "onShowFileChooser: "+fileChooserParams);
                uploadMessageAboveL = filePathCallback;
                Intent intent = fileChooserParams.createIntent();
                startActivityForResult(Intent.createChooser(intent, "Image Browser"), FILE_CHOOSER_RESULT_CODE);
                return true;
            }

            @Override
            public View getVideoLoadingProgressView() {
                FrameLayout frameLayout = new FrameLayout(getActivity());
                frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                return frameLayout;
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                showCustomView(view, callback);
            }

            @Override
            public void onHideCustomView() {
                hideCustomView();
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                return super.onJsConfirm(view, url, message, result);
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
                return super.onJsPrompt(view, url, message, message, result);
            }
        });

        initView();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (conversationViewModel != null && conversation != null) {
            conversationViewModel.clearUnreadStatus(conversation);
        }
        adapter.notifyDataSetChanged();

        if(conversation.type == Conversation.ConversationType.Single){
            rl_topMessage.setVisibility(View.GONE);
            rl_groupNotice.setVisibility(View.GONE);
        }

        if (groupInfo != null) {
            getKeyWordList();
            System.out.println("@@@ conversionFragment");
            List<String> list = new ArrayList<>();
            list = groupInfo.getTopList();
            if(list.size() == 0){
                rl_topMessage.setVisibility(View.GONE);
            }else{
                rl_topMessage.setVisibility(View.VISIBLE);
                ll_topMessage1.setVisibility(View.VISIBLE);
                String str_list1 = groupInfo.getTopMessage(list.get(0));
                String str_list2 = null;

                JSONObject attrJson1 = JSONObject.parseObject(str_list1);
                String userID1 = attrJson1.getString("fromUser");
                String topMessage1 = attrJson1.getString("data");
                UserInfo userInfo1 = ChatManager.Instance().getUserInfo(userID1, false);
                tv_topMessage1.setText(userInfo1.displayName + ": "+topMessage1);
                /*if(list.size() > 1){
                    ll_topMessage2.setVisibility(View.VISIBLE);
                    str_list2 = groupInfo.getTopMessage(list.get(1));
                    JSONObject attrJson2 = JSONObject.parseObject(str_list2);
                    String userID2 = attrJson2.getString("fromUser");
                    String topMessage2 = attrJson2.getString("data");
                    UserInfo userInfo2 = ChatManager.Instance().getUserInfo(userID2, false);
                    tv_topMessage2.setText(userInfo2.displayName + ": "+topMessage2);
                }else{
                    ll_topMessage2.setVisibility(View.GONE);
                }*/
            }
        }

    }

    public void setupConversation(Conversation conversation, String title, long focusMessageId, String target) {
        this.conversation = conversation;
        this.conversationTitle = title;
        this.initialFocusedMessageId = focusMessageId;
        this.channelPrivateChatUser = target;
        setupConversation(conversation);
        showAplets(conversation);
    }

    @SuppressLint("JavascriptInterface")
    private void initView() {

        handler = new Handler();
        rootLinearLayout.addOnKeyboardShownListener(this);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (adapter.getMessages() == null || adapter.getMessages().isEmpty()) {
                swipeRefreshLayout.setRefreshing(false);
                return;
            }
            loadMoreOldMessages();
        });

        // message list
        adapter = new ConversationMessageAdapter(this);
        adapter.setOnPortraitClickListener(this);
        adapter.setOnMessageReceiptClickListener(this);
        adapter.setOnPortraitLongClickListener(this);
        adapter.setOnMessageCheckListener(this);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // 向上滑动，不在底部，收到消息时，不滑动到底部, 发送消息时，可以强制置为true
                if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                    return;
                }
                if (!recyclerView.canScrollVertically(1)) {
                    moveToBottom = true;
                    if ((initialFocusedMessageId != -1 || firstUnreadMessageId != 0) && !loadingNewMessage && shouldContinueLoadNewMessage) {
                        int lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition();
                        if (lastVisibleItem > adapter.getItemCount() - 3) {
                            loadMoreNewMessages();
                        }
                    }
                } else {
                    moveToBottom = false;
                }
            }
        });

        inputPanel.init(this, rootLinearLayout);
        inputPanel.setOnConversationInputPanelStateChangeListener(this);

        settingViewModel = ViewModelProviders.of(this).get(SettingViewModel.class);
        conversationViewModel = WfcUIKit.getAppScopeViewModel(ConversationViewModel.class);
        conversationViewModel.clearConversationMessageLiveData().observeForever(clearConversationMessageObserver);
        messageViewModel = ViewModelProviders.of(this).get(MessageViewModel.class);

        messageViewModel.messageLiveData().observeForever(messageLiveDataObserver);
        messageViewModel.messageUpdateLiveData().observeForever(messageUpdateLiveDatObserver);
        messageViewModel.messageRemovedLiveData().observeForever(messageRemovedLiveDataObserver);
        messageViewModel.mediaUpdateLiveData().observeForever(mediaUploadedLiveDataObserver);

        messageViewModel.messageDeliverLiveData().observe(getActivity(), stringLongMap -> {
            if (conversation == null) {
                return;
            }
            Map<String, Long> deliveries = ChatManager.Instance().getMessageDelivery(conversation);
            adapter.setDeliveries(deliveries);
        });

        messageViewModel.messageReadLiveData().observe(getActivity(), readEntries -> {
            if (conversation == null) {
                return;
            }
            Map<String, Long> convReadEntities = ChatManager.Instance().getConversationRead(conversation);
            adapter.setReadEntries(convReadEntities);
        });

        userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        userViewModel.userInfoLiveData().observeForever(userInfoUpdateLiveDataObserver);

        settingUpdateLiveDataObserver = o -> {
            if (groupInfo == null) {
                return;
            }
            boolean show = "1".equals(userViewModel.getUserSetting(UserSettingScope.GroupHideNickname, groupInfo.target));
            if (showGroupMemberName != show) {
                showGroupMemberName = show;
                adapter.notifyDataSetChanged();
            }
            reloadMessage();
        };
        settingViewModel.settingUpdatedLiveData().observeForever(settingUpdateLiveDataObserver);

        rl_groupNotice.setOnClickListener(new View.OnClickListener() {
            Boolean flag = true;

            @Override
            public void onClick(View view) {
                /*if (flag) {
                    flag = false;
                    groupNotice.setEllipsize(null); // 展开
                    groupNotice.setSingleLine(false);//将TextView设置为不是单行显示的
                } else {
                    flag = true;
                    groupNotice.setEllipsize(TextUtils.TruncateAt.END); // 收缩
                    groupNotice.setSingleLine(true);//收缩的时候设置为是单行显示的
                }*/

                Intent intent = new Intent(getActivity(), ActivityGroupAnnounceMent.class);
                intent.putExtra("content",groupNotice.getText().toString().trim());
                startActivity(intent);

            }
        });

        rl_aplets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("@@@     channelPrivateChatUser=" + conversation.target);

                rl_aplets.setVisibility(View.GONE);
                rl_aplets_open.setVisibility(View.VISIBLE);
            }
        });
        rl_aplets_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rl_aplets_open.setVisibility(View.GONE);
                rl_aplets.setVisibility(View.VISIBLE);
            }
        });
        iv_aplets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rl_webView.setVisibility(View.VISIBLE);
                rl_aplets_open.setVisibility(View.GONE);

                if (!CheckRight(litappInfo.target)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    builder.setTitle(R.string.authorize);
                    builder.setMessage(litappInfo.displayName + getString(R.string.auth_tip));
                    builder.setPositiveButton(R.string.confirm, (dialog, which) -> {
                        AddRight(litappInfo.target);
                        startLitapp();
                    });
                    builder.setNegativeButton(R.string.cancel, (dialog, which) -> getActivity().finish());
                    builder.show();
                } else {
                    startLitapp();
                }
            }
        });
        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rl_webView.setVisibility(View.GONE);
                rl_aplets_open.setVisibility(View.GONE);
                rl_aplets.setVisibility(View.VISIBLE);
            }
        });
        iv_fullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rl_aplets_open.setVisibility(View.GONE);
                rl_aplets.setVisibility(View.VISIBLE);
                rl_webView.setVisibility(View.GONE);
                Intent intent = new Intent(getActivity(), LitappActivity.class);
                intent.putExtra("litappInfo", litappInfo);
                getActivity().startActivity(intent);
            }
        });
    }

    void startLitapp() {
        new Thread(() -> {
            while (!ChatManager.Instance().checkRemoteService()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            getActivity().runOnUiThread(() -> {
                if (checkLitapp()) {
                    mWebView.loadUrl(litappInfo.url+shareId);
                    //   mWebView.loadUrl("http://8.210.19.46/imageAdd/add.html");
                } else {
                    Toast.makeText(getActivity(), "小程序签名校验失败", Toast.LENGTH_LONG).show();
                    new Thread(() -> {
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        getActivity().finish();
                    }).start();
                }
            });
        }).start();
    }

    private boolean CheckRight(String osnID) {
        boolean hasRight = false;
        SharedPreferences sp = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
        String cache = sp.getString("litappAccess", null);
        if (cache != null && !cache.isEmpty()) {
            try {
                JSONArray json = JSON.parseArray(cache);
                for (Object o : json) {
                    if (osnID.equalsIgnoreCase((String) o)) {
                        hasRight = true;
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return hasRight;
    }

    /**
     * 视频播放全屏
     **/
    private void showCustomView(View view, WebChromeClient.CustomViewCallback callback) {
        // if a view already exists then immediately terminate the new one
        if (customView != null) {
            callback.onCustomViewHidden();
            return;
        }

        getActivity().getWindow().getDecorView();

        FrameLayout decor = (FrameLayout) getActivity().getWindow().getDecorView();
        fullscreenContainer = new FullscreenHolder(getActivity());
        fullscreenContainer.addView(view, COVER_SCREEN_PARAMS);
        decor.addView(fullscreenContainer, COVER_SCREEN_PARAMS);
        customView = view;
        setStatusBarVisibility(false);
        customViewCallback = callback;
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    /**
     * 隐藏视频全屏
     */
    private void hideCustomView() {
        if (customView == null) {
            return;
        }

        setStatusBarVisibility(true);
        FrameLayout decor = (FrameLayout) getActivity().getWindow().getDecorView();
        decor.removeView(fullscreenContainer);
        fullscreenContainer = null;
        customView = null;
        customViewCallback.onCustomViewHidden();
        mWebView.setVisibility(View.VISIBLE);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    /**
     * 全屏容器界面
     */
    static class FullscreenHolder extends FrameLayout {

        public FullscreenHolder(Context ctx) {
            super(ctx);
            setBackgroundColor(ctx.getResources().getColor(android.R.color.black));
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouchEvent(MotionEvent evt) {
            return true;
        }
    }

    JSONObject setReply(String error, JSONObject json) {
        if (json == null)
            json = new JSONObject();
        json.put("errCode", error == null ? "0:success" : error);
        Log.d(TAG, "reply: " + json.toString());
        return json;
    }

    JSONObject runJsCommand(JSONObject json, String callback) {
        AtomicReference<JSONObject> data = new AtomicReference<>();
        try {
            switch (json.getString("command")) {
                case "Login":
                    Log.d(TAG, "Login to url: " + json.getString("url"));
                    String url = json.getString("url");
                    if (url == null)
                        url = litappInfo.url;
                    ChatManager.Instance().ltbLogin(litappInfo, url, new GeneralCallback2() {
                        @Override
                        public void onSuccess(String sessionKey) {
                            getActivity().runOnUiThread(() -> {
                                isLogin = true;
                                JSONObject result = JSON.parseObject(sessionKey);
                                if (callback == null) {
                                    data.set(result);
                                    synchronized (this) {
                                        this.notify();
                                    }
                                } else {
                                    mWebView.loadUrl("javascript:" + callback + "(" + result.toString() + ")");
                                }
                            });
                        }

                        @Override
                        public void onFail(int errorCode) {
                            getActivity().runOnUiThread(() -> {
                                isLogin = false;
                                JSONObject result = setReply("-1:failure", null);
                                if (callback == null) {
                                    data.set(result);
                                    synchronized (this) {
                                        this.notify();
                                    }
                                } else {
                                    mWebView.loadUrl("javascript:" + callback + "(" + result.toString() + ")");
                                }
                            });
                        }
                    });
                    if (callback == null) {
                        synchronized (this) {
                            this.wait(10000);
                        }
                    }
                    break;
                case "GetUserInfo":
                    if (!isLogin) {
                        data.set(setReply("-1:need login", null));
                        if (callback == null)
                            return data.get();
                        mWebView.loadUrl("javascript:" + callback + "(" + data.get().toString() + ")");
                        break;
                    }
                    UserInfo userInfo = ChatManager.Instance().getUserInfo(null, false);
                    JSONObject infoResult = new JSONObject();
                    infoResult.put("userID", userInfo.uid);
                    infoResult.put("userName", userInfo.name);
                    infoResult.put("nickName", userInfo.displayName);
                    infoResult.put("portrait", userInfo.portrait);
                    //userInfo.getNft()
                    data.set(setReply(null, infoResult));
                    if (callback == null)
                        return data.get();
                    mWebView.loadUrl("javascript:" + callback + "(" + data.get().toString() + ")");
                    break;
                case "SignData":
                    if (!isLogin) {
                        data.set(setReply("-1:need login", null));
                        if (callback == null)
                            return data.get();
                        mWebView.loadUrl("javascript:" + callback + "(" + data.get().toString() + ")");
                        break;
                    }
                    String sign = ChatManager.Instance().signData(json.getString("data").getBytes());
                    JSONObject signResult = new JSONObject();
                    signResult.put("sign", sign);
                    signResult.put("data", json.getString("data"));
                    data.set(setReply(null, signResult));
                    if (callback == null)
                        return data.get();
                    mWebView.loadUrl("javascript:" + callback + "(" + data.get().toString() + ")");
                    break;
                case "AddFriend":
                    userInfo = ChatManager.Instance().getUserInfo(json.getString("userID"), false);
                    Intent intent = new Intent(mWebView.getContext(), UserInfoActivity.class);
                    intent.putExtra("userInfo", userInfo);
                    startActivity(intent);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data.get();
    }

    private void setStatusBarVisibility(boolean visible) {
        int flag = visible ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getActivity().getWindow().setFlags(flag, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void setupConversation(Conversation conversation) {

        if (conversation.type == Conversation.ConversationType.Group) {
            groupViewModel = ViewModelProviders.of(this).get(GroupViewModel.class);
            initGroupObservers();
            // 进入会话不需要刷新
            groupViewModel.getGroupMembers(conversation.target, false);
            groupInfo = groupViewModel.getGroupInfo(conversation.target, false);
            groupMember = groupViewModel.getGroupMember(conversation.target, userViewModel.getUserId());

            conversation.redPacketBomb = groupInfo.getBombEnable();

            /*if(groupInfo.target == null || groupInfo.target.equals("")){
                showGroupMemberName = "1".equals(userViewModel.getUserSetting(UserSettingScope.GroupHideNickname, ""));
            }else{
                showGroupMemberName = "1".equals(userViewModel.getUserSetting(UserSettingScope.GroupHideNickname, groupInfo.target));
            }*/
            showGroupMemberName = "1".equals(userViewModel.getUserSetting(UserSettingScope.GroupHideNickname, groupInfo.target));

            updateGroupMuteStatus();
        }

        userViewModel.getUserInfo(userViewModel.getUserId(), false);



        boolean enable2 = (boolean) SPUtils.get(getActivity(), "enable2", false);
        System.out.println("@@@ conversation enable2 : " +enable2);
        boolean enable1 = (boolean) SPUtils.get(getActivity(), "enable1", false);
        conversation.enable2 = enable2 || enable1;
        boolean enable3 = (boolean) SPUtils.get(getActivity(), "enable3", false);
        conversation.enable3 = enable3;


        String temp = ChatManager.Instance().getVoiceBaseUrl();
        if (temp.equals("")) {
            conversation.enableVoip = false;
        } else {
            conversation.enableVoip = true;
        }
        //conversation.enableVoip

        inputPanel.setupConversation(conversation);

        if (conversation.type != Conversation.ConversationType.ChatRoom) {
            loadMessage(initialFocusedMessageId);
        } else {
            joinChatRoom();
        }

        ConversationInfo conversationInfo = ChatManager.Instance().getConversation(conversation);
        int unreadCount = conversationInfo.unreadCount.unread + conversationInfo.unreadCount.unreadMention + conversationInfo.unreadCount.unreadMentionAll;
        if (unreadCount > 10 && unreadCount < 300) {
            firstUnreadMessageId = ChatManager.Instance().getFirstUnreadMessageId(conversation);
            showUnreadMessageCountLabel(unreadCount);
        }
        conversationViewModel.clearUnreadStatus(conversation);

        setTitle();
    }

    private void AddRight(String osnID) {
        SharedPreferences sp = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
        String cache = sp.getString("litappAccess", null);
        JSONArray json = null;
        if (cache != null && !cache.isEmpty()) {
            try {
                json = JSON.parseArray(cache);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (json == null)
            json = new JSONArray();
        json.add(osnID);
        sp.edit().putString("litappAccess", json.toString()).apply();
    }


    private void showAplets(Conversation conversation) {
        if (conversation.type == Conversation.ConversationType.Group) {
            System.out.println("@@@    是群聊");
            dappUrlFront = (String) SPUtils.get(getActivity(), conversation.target, "");
            System.out.println("@@@   dappUrlFront=" + dappUrlFront);
            String url_QueryAplets = (String) SPUtils.get(getActivity(), "QueryAplets", "");
            if (url_QueryAplets.length() < 5) {
                return;
            }
            //本地没有群绑定的信息,需从接口获取
            System.out.println("@@@   url_QueryAplets=  "+url_QueryAplets);
            OkHttpClient okHttpClient = new OkHttpClient();
            final Request request = new Request.Builder()
                    .url(url_QueryAplets + "?groupId=" + groupInfo.target + "&owner=" + groupInfo.owner)
                    .get()
                    .addHeader("X-Token", token)
                    .build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    System.out.println("@@@   获取绑定小程序失败： " + e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String result = response.body().string();
                    System.out.println("@@@   获取绑定小程序成功: " + result);
                    try {
                        Gson gson = new Gson();
                        ShowApletsInfo showApletsInfo = gson.fromJson(result, ShowApletsInfo.class);
                        if (showApletsInfo.getCode() == 200) {
                            if (showApletsInfo.getData() == null) {
                                return;
                            }
                            String dappUrlFront = showApletsInfo.getData().getDappUrlFront();
                            if (dappUrlFront == null) {
                                return;
                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String dappUrlFront = showApletsInfo.getData().getDappUrlFront();
                                    JSONObject jsonObject = JSONObject.parseObject(dappUrlFront);
                                    String imageUrl = jsonObject.getString("portrait");
                                    rl_aplets.setVisibility(View.VISIBLE);
                                    GlideApp.with(getActivity())
                                            .load(imageUrl)
                                            .centerCrop()
                                            .transform(new CircleCrop())
                                            .into(iv_aplets);
                                    //     litappInfo.displayName = jsonObject.getString("name");
                                    litappInfo.info = jsonObject.getString("info");
                                    litappInfo.target = jsonObject.getString("target");
                                    litappInfo.url = jsonObject.getString("url");
                                    litappInfo.name = jsonObject.getString("name");
                                    litappInfo.portrait = jsonObject.getString("portrait");
                                    shareId = jsonObject.getString("shareId");
                                    SPUtils.put(getActivity(), conversation.target, dappUrlFront);
                                }
                            });
                        } else {

                        }
                    }catch (Exception e){

                    }

                }
            });
            /*if (dappUrlFront.equals("") || dappUrlFront == null) {
                String url_QueryAplets = (String) SPUtils.get(getActivity(), "QueryAplets", "");
                if (url_QueryAplets.length() < 5) {
                    return;
                }
                //本地没有群绑定的信息,需从接口获取
                System.out.println("@@@   url_QueryAplets=  "+url_QueryAplets);
                OkHttpClient okHttpClient = new OkHttpClient();
                final Request request = new Request.Builder()
                        .url(url_QueryAplets + "?groupId=" + groupInfo.target + "&owner=" + groupInfo.owner)
                        .get()
                        .addHeader("X-Token", token)
                        .build();
                Call call = okHttpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        System.out.println("@@@   获取绑定小程序失败： " + e);
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        String result = response.body().string();
                        System.out.println("@@@   获取绑定小程序成功: " + result);
                        try {
                            Gson gson = new Gson();
                            ShowApletsInfo showApletsInfo = gson.fromJson(result, ShowApletsInfo.class);
                            if (showApletsInfo.getCode() == 200) {
                                if (showApletsInfo.getData() == null) {
                                    return;
                                }
                                String dappUrlFront = showApletsInfo.getData().getDappUrlFront();
                                if (dappUrlFront == null) {
                                    return;
                                }
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String dappUrlFront = showApletsInfo.getData().getDappUrlFront();
                                        JSONObject jsonObject = JSONObject.parseObject(dappUrlFront);
                                        String imageUrl = jsonObject.getString("portrait");
                                        rl_aplets.setVisibility(View.VISIBLE);
                                        GlideApp.with(getActivity())
                                                .load(imageUrl)
                                                .centerCrop()
                                                .transform(new CircleCrop())
                                                .into(iv_aplets);
                                        //     litappInfo.displayName = jsonObject.getString("name");
                                        litappInfo.info = jsonObject.getString("info");
                                        litappInfo.target = jsonObject.getString("target");
                                        litappInfo.url = jsonObject.getString("url");
                                        litappInfo.name = jsonObject.getString("name");
                                        litappInfo.portrait = jsonObject.getString("portrait");
                                        SPUtils.put(getActivity(), conversation.target, dappUrlFront);
                                    }
                                });
                            } else {

                            }
                        }catch (Exception e){

                        }

                    }
                });
            }
            else {
                System.out.println("@@@   从本地取得dappUrlFront=" + dappUrlFront);
                rl_aplets.setVisibility(View.VISIBLE);
                JSONObject jsonObject = JSONObject.parseObject(dappUrlFront);
                GlideApp.with(getActivity())
                        .load(jsonObject.getString("portrait"))
                        .centerCrop()
                        .transform(new CircleCrop())
                        .into(iv_aplets);
                //     litappInfo.displayName = jsonObject.getString("name");
                litappInfo.info = jsonObject.getString("info");
                litappInfo.target = jsonObject.getString("target");
                litappInfo.url = jsonObject.getString("url");
                litappInfo.name = jsonObject.getString("name");
                litappInfo.portrait = jsonObject.getString("portrait");
            }*/

        } else {
            System.out.println("@@@    不是群聊");
            rl_aplets.setVisibility(View.GONE);
        }
    }

    boolean checkLitapp() {
        if (litappInfo.info == null || litappInfo.info.isEmpty())
            return true;
        try {
            JSONObject json = JSON.parseObject(litappInfo.info);
            String sign = json.getString("sign");
            if (sign != null) {
                String calc = litappInfo.target + litappInfo.url;
                String hash = ChatManager.Instance().hashData(calc.getBytes());
                Log.d(TAG, "verify data: " + calc + ", sign: " + sign + ", hash: " + hash);
                return ChatManager.Instance().verifyData(litappInfo.target, hash.getBytes(), sign);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void loadMessage(long focusMessageId) {

        MutableLiveData<List<UiMessage>> messages;
        if (focusMessageId != -1) {
            shouldContinueLoadNewMessage = true;
            messages = conversationViewModel.loadAroundMessages(conversation, channelPrivateChatUser, focusMessageId, MESSAGE_LOAD_AROUND);
        } else {
            messages = conversationViewModel.getMessages(conversation, channelPrivateChatUser);
        }

        // load message
        swipeRefreshLayout.setRefreshing(true);
        adapter.setDeliveries(ChatManager.Instance().getMessageDelivery(conversation));
        adapter.setReadEntries(ChatManager.Instance().getConversationRead(conversation));
        messages.observe(this, uiMessages -> {
            swipeRefreshLayout.setRefreshing(false);
            adapter.setMessages(uiMessages);
            adapter.notifyDataSetChanged();

            if (adapter.getItemCount() > 1) {
                int initialMessagePosition;
                if (focusMessageId != -1) {
                    initialMessagePosition = adapter.getMessagePosition(focusMessageId);
                    if (initialMessagePosition != -1) {
                        recyclerView.scrollToPosition(initialMessagePosition);
                        adapter.highlightFocusMessage(initialMessagePosition);
                    }
                } else {
                    moveToBottom = true;
                    recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                }
            }
        });
    }

    private void updateGroupMuteStatus() {
        if (groupInfo == null || groupMember == null) {
            return;
        }
        if (groupInfo.mute == 1) {
            if (groupMember.type != GroupMember.GroupMemberType.Owner
                    && groupMember.type != GroupMember.GroupMemberType.Manager) {
                inputPanel.disableInput(getString(R.string.all_member_mute));
            }
        } else {
            inputPanel.enableInput();
        }
        if (groupInfo.notice == null || groupInfo.notice.isEmpty()) {
            rl_groupNotice.setVisibility(View.GONE);
        } else {
            rl_groupNotice.setVisibility(View.VISIBLE);
            groupNotice.setText(getString(R.string.group_announcement) + " : " + groupInfo.notice);
        }

        List<String> list = new ArrayList<>();
        list = groupInfo.getTopList();
        if(list.size() == 0){
            rl_topMessage.setVisibility(View.GONE);
        }else{
            rl_topMessage.setVisibility(View.VISIBLE);
            ll_topMessage1.setVisibility(View.VISIBLE);
            String str_list1 = groupInfo.getTopMessage(list.get(0));
            String str_list2 = null;

            JSONObject attrJson1 = JSONObject.parseObject(str_list1);
            String userID1 = attrJson1.getString("fromUser");
            String topMessage1 = attrJson1.getString("data");
            UserInfo userInfo1 = ChatManager.Instance().getUserInfo(userID1, false);
            tv_topMessage1.setText(userInfo1.displayName + ": "+topMessage1);
            /*if(list.size() > 1){
                ll_topMessage2.setVisibility(View.VISIBLE);
                str_list2 = groupInfo.getTopMessage(list.get(1));
                JSONObject attrJson2 = JSONObject.parseObject(str_list2);
                String userID2 = attrJson2.getString("fromUser");
                String topMessage2 = attrJson2.getString("data");
                UserInfo userInfo2 = ChatManager.Instance().getUserInfo(userID2, false);
                tv_topMessage2.setText(userInfo2.displayName + ": "+topMessage2);
            }*/


        }
    }

    @OnClick(R2.id.unreadCountTextView)
    void onUnreadCountTextViewClick() {
        hideUnreadMessageCountLabel();
        shouldContinueLoadNewMessage = true;
        loadMessage(firstUnreadMessageId);
    }

    @OnClick(R2.id.iv_delete)
    void ivDelete(){
        boolean isDelete = false;
        List<GroupMember> managers = ChatManager.Instance().getGroupManagers(groupInfo.target);
        if (groupMember.type == GroupMember.GroupMemberType.Manager
                || groupMember.type == GroupMember.GroupMemberType.Owner
                || groupMember.memberId.equalsIgnoreCase(groupInfo.owner)
        ) {
            isDelete = true;
        }
        for (GroupMember member : managers) {
            if(member.memberId.equals(ChatManager.Instance().getUserId())){
                isDelete = true;
            }
        }

        List<String> list = new ArrayList<>();
        list = groupInfo.getTopList();
        Intent intent = new Intent(getActivity(), TopMessageActivity.class);
        intent.putStringArrayListExtra("list", (ArrayList<String>) list);
        intent.putExtra("groupInfo",groupInfo);
        intent.putExtra("isDelete",isDelete);
        startActivity(intent);
    }

    private void showUnreadMessageCountLabel(int count) {
        //会话页面显示消息条数
        unreadCountLinearLayout.setVisibility(View.GONE);
        unreadCountTextView.setVisibility(View.GONE);
        unreadCountTextView.setText(count + getString(R.string.no_message));
    }

    private void hideUnreadMessageCountLabel() {
        unreadCountTextView.setVisibility(View.GONE);
    }

    private void showUnreadMentionCountLabel(int count) {
        unreadCountLinearLayout.setVisibility(View.VISIBLE);
        unreadMentionCountTextView.setVisibility(View.VISIBLE);
        unreadMentionCountTextView.setText(count + getString(R.string.no_a_message));
    }

    private void hideUnreadMentionCountLabel() {
        unreadMentionCountTextView.setVisibility(View.GONE);
    }

    private void joinChatRoom() {
        chatRoomViewModel = ViewModelProviders.of(this).get(ChatRoomViewModel.class);
        chatRoomViewModel.joinChatRoom(conversation.target)
                .observe(this, new Observer<OperateResult<Boolean>>() {
                    @Override
                    public void onChanged(@Nullable OperateResult<Boolean> booleanOperateResult) {
                        if (booleanOperateResult.isSuccess()) {
                            String welcome = getString(R.string.welcome_join_room);
                            TipNotificationContent content = new TipNotificationContent();
                            String userId = userViewModel.getUserId();
                            UserInfo userInfo = userViewModel.getUserInfo(userId, false);
                            if (userInfo != null) {
                                content.tip = String.format(welcome, userViewModel.getUserDisplayName(userInfo));
                            } else {
                                content.tip = String.format(welcome, "<" + userId + ">");
                            }
                            handler.postDelayed(() -> {
                                messageViewModel.sendMessage(conversation, content);
                            }, 1000);
                            setChatRoomConversationTitle();

                        } else {
                            Toast.makeText(getActivity(), getString(R.string.join_room_failure), Toast.LENGTH_SHORT).show();
                            getActivity().finish();
                        }
                    }
                });
    }

    private void quitChatRoom() {
        String welcome = getString(R.string.no_leave_room);
        TipNotificationContent content = new TipNotificationContent();
        String userId = userViewModel.getUserId();
        UserInfo userInfo = userViewModel.getUserInfo(userId, false);
        if (userInfo != null) {
            content.tip = String.format(welcome, userViewModel.getUserDisplayName(userInfo));
        } else {
            content.tip = String.format(welcome, "<" + userId + ">");
        }
        messageViewModel.sendMessage(conversation, content);
        chatRoomViewModel.quitChatRoom(conversation.target);
    }

    private void setChatRoomConversationTitle() {
        chatRoomViewModel.getChatRoomInfo(conversation.target, System.currentTimeMillis())
                .observe(this, chatRoomInfoOperateResult -> {
                    if (chatRoomInfoOperateResult.isSuccess()) {
                        ChatRoomInfo chatRoomInfo = chatRoomInfoOperateResult.getResult();
                        conversationTitle = chatRoomInfo.title;
                        setActivityTitle(conversationTitle);
                    }
                });
    }

    private void setTitle() {
        if (!TextUtils.isEmpty(conversationTitle)) {
            setActivityTitle(conversationTitle);
        }

        if (conversation.type == Conversation.ConversationType.Single) {
            UserInfo userInfo = ChatManagerHolder.gChatManager.getUserInfo(conversation.target, false);
            conversationTitle = userViewModel.getUserDisplayName(userInfo);
        } else if (conversation.type == Conversation.ConversationType.Group) {
            if (groupInfo != null) {
                conversationTitle = groupInfo.name + "(" + groupInfo.memberCount + "人)";
            }
        } else if (conversation.type == Conversation.ConversationType.Channel) {
            ChannelViewModel channelViewModel = ViewModelProviders.of(this).get(ChannelViewModel.class);
            ChannelInfo channelInfo = channelViewModel.getChannelInfo(conversation.target, false);
            if (channelInfo != null) {
                conversationTitle = channelInfo.name;
            }

            if (!TextUtils.isEmpty(channelPrivateChatUser)) {
                UserInfo channelPrivateChatUserInfo = userViewModel.getUserInfo(channelPrivateChatUser, false);
                if (channelPrivateChatUserInfo != null) {
                    conversationTitle += "@" + userViewModel.getUserDisplayName(channelPrivateChatUserInfo);
                } else {
                    conversationTitle += "@<" + channelPrivateChatUser + ">";
                }
            }
        }
        setActivityTitle(conversationTitle);
    }

    private void setActivityTitle(String title) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle(title);
        }
    }

    @OnTouch({R2.id.contentLayout, R2.id.msgRecyclerView})
    boolean onTouch(View view, MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_DOWN && inputPanel.extension.canHideOnScroll()) {
//            inputPanel.collapse();
//        }
        inputPanel.closeConversationInputPanel();
        return false;
    }

    @Override
    public void onPortraitClick(UserInfo userInfo) {
        if (groupInfo != null && groupInfo.privateChat == 1) {
            boolean allowPrivateChat = false;
            GroupMember groupMember = groupViewModel.getGroupMember(groupInfo.target, userViewModel.getUserId());
            if (groupMember != null && groupMember.type == GroupMember.GroupMemberType.Normal) {
                GroupMember targetGroupMember = groupViewModel.getGroupMember(groupInfo.target, userInfo.uid);
                if (targetGroupMember != null && (targetGroupMember.type == GroupMember.GroupMemberType.Owner || targetGroupMember.type == GroupMember.GroupMemberType.Manager)) {
                    allowPrivateChat = true;
                }
            } else if (groupMember != null && (groupMember.type == GroupMember.GroupMemberType.Owner || groupMember.type == GroupMember.GroupMemberType.Manager)) {
                allowPrivateChat = true;
            }

            if (!allowPrivateChat) {
                Toast.makeText(getActivity(), getString(R.string.disable_talk), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (userInfo.deleted == 0) {
            Intent intent = new Intent(getActivity(), UserInfoActivity.class);
            intent.putExtra("userInfo", userInfo);
            startActivity(intent);
        }
    }

    @Override
    public void onPortraitLongClick(UserInfo userInfo) {
        if (conversation.type == Conversation.ConversationType.Group) {
            SpannableString spannableString = mentionSpannable(userInfo);
            int position = inputPanel.editText.getSelectionEnd();
            inputPanel.editText.getEditableText().append(" ");
            inputPanel.editText.getEditableText().replace(position, position + 1, spannableString);
        } else {
            inputPanel.editText.getEditableText().append(userViewModel.getUserDisplayName(userInfo));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode >= ConversationExtension.REQUEST_CODE_MIN) {
            boolean result = inputPanel.extension.onActivityResult(requestCode, resultCode, data);
            if (result) {
                return;
            }
            Log.d(TAG, "extension can not handle " + requestCode);
        }
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PICK_MENTION_CONTACT) {
                boolean isMentionAll = data.getBooleanExtra("mentionAll", false);
                SpannableString spannableString;
                if (isMentionAll) {
                    spannableString = mentionAllSpannable();
                } else {
                    String userId = data.getStringExtra("userId");
                    UserInfo userInfo = userViewModel.getUserInfo(userId, false);
                    spannableString = mentionSpannable(userInfo);
                }
                int position = inputPanel.editText.getSelectionEnd();
                position = position > 0 ? position - 1 : 0;
                inputPanel.editText.getEditableText().replace(position, position + 1, spannableString);

            } else if (requestCode == REQUEST_CODE_GROUP_AUDIO_CHAT || requestCode == REQUEST_CODE_GROUP_VIDEO_CHAT) {
                onPickGroupMemberToVoipChat(data, requestCode == REQUEST_CODE_GROUP_AUDIO_CHAT);
            }
        }
    }

    private SpannableString mentionAllSpannable() {
        String text = getString(R.string.a_all);
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new MentionSpan(true), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    private SpannableString mentionSpannable(UserInfo userInfo) {
        String text = "@" + userInfo.displayName + " ";
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new MentionSpan(userInfo.uid), 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    @Override
    public void onPause() {
        super.onPause();
        inputPanel.onActivityPause();
        messageViewModel.stopPlayAudio();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (conversation == null) {
            return;
        }

        if (conversation.type == Conversation.ConversationType.ChatRoom) {
            quitChatRoom();
        }

        messageViewModel.messageLiveData().removeObserver(messageLiveDataObserver);
        messageViewModel.messageUpdateLiveData().removeObserver(messageUpdateLiveDatObserver);
        messageViewModel.messageRemovedLiveData().removeObserver(messageRemovedLiveDataObserver);
        messageViewModel.mediaUpdateLiveData().removeObserver(mediaUploadedLiveDataObserver);
        userViewModel.userInfoLiveData().removeObserver(userInfoUpdateLiveDataObserver);
        conversationViewModel.clearConversationMessageLiveData().removeObserver(clearConversationMessageObserver);
        settingViewModel.settingUpdatedLiveData().removeObserver(settingUpdateLiveDataObserver);

        unInitGroupObservers();
        inputPanel.onDestroy();
    }

    boolean onBackPressed() {
        boolean consumed = true;
        if (rootLinearLayout.getCurrentInput() != null) {
            rootLinearLayout.hideAttachedInput(true);
            inputPanel.closeConversationInputPanel();
        } else if (multiMessageActionContainerLinearLayout.getVisibility() == View.VISIBLE) {
            toggleConversationMode();
        } else {
            consumed = false;
        }
        return consumed;
    }

    @Override
    public void onKeyboardShown() {
        inputPanel.onKeyboardShown();
        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
    }

    @Override
    public void onKeyboardHidden() {
        inputPanel.onKeyboardHidden();
    }

    private void reloadMessage() {
        conversationViewModel.getMessages(conversation, channelPrivateChatUser).observe(this, uiMessages -> {
            adapter.setMessages(uiMessages);
            adapter.notifyDataSetChanged();
        });
    }

    private void loadMoreOldMessages() {
        long fromMessageId = Long.MAX_VALUE;
        long fromMessageUid = Long.MAX_VALUE;
        if (adapter.getMessages() != null && !adapter.getMessages().isEmpty()) {
            fromMessageId = adapter.getItem(0).message.messageId;
            fromMessageUid = adapter.getItem(0).message.messageUid;
        }

        conversationViewModel.loadOldMessages(conversation, channelPrivateChatUser, fromMessageId, fromMessageUid, MESSAGE_LOAD_COUNT_PER_TIME)
                .observe(this, uiMessages -> {
                    adapter.addMessagesAtHead(uiMessages);

                    swipeRefreshLayout.setRefreshing(false);
                });
    }

    private void loadMoreNewMessages() {
        loadingNewMessage = true;
        adapter.showLoadingNewMessageProgressBar();
        conversationViewModel.loadNewMessages(conversation, channelPrivateChatUser, adapter.getItem(adapter.getItemCount() - 2).message.messageId, MESSAGE_LOAD_COUNT_PER_TIME)
                .observe(this, messages -> {
                    loadingNewMessage = false;
                    adapter.dismissLoadingNewMessageProgressBar();

                    if (messages == null || messages.isEmpty()) {
                        shouldContinueLoadNewMessage = false;
                    }
                    if (messages != null && !messages.isEmpty()) {
                        adapter.addMessagesAtTail(messages);
                    }
                });
    }

    private void updateTypingStatusTitle(TypingMessageContent typingMessageContent) {
        String typingDesc = "";
        switch (typingMessageContent.getTypingType()) {
            case TypingMessageContent.TYPING_TEXT:
                typingDesc = getString(R.string.inputting);
                break;
            case TypingMessageContent.TYPING_VOICE:
                typingDesc = getString(R.string.recording);
                break;
            case TypingMessageContent.TYPING_CAMERA:
                typingDesc = getString(R.string.photographing);
                break;
            case TypingMessageContent.TYPING_FILE:
                typingDesc = getString(R.string.send_file);
                break;
            case TypingMessageContent.TYPING_LOCATION:
                typingDesc = getString(R.string.send_location);
                break;
            default:
                typingDesc = "unknown";
                break;
        }
        setActivityTitle(typingDesc);
        handler.postDelayed(resetConversationTitleRunnable, 5000);
    }

    private Runnable resetConversationTitleRunnable = this::resetConversationTitle;

    private void resetConversationTitle() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        if (!TextUtils.equals(conversationTitle, getActivity().getTitle())) {
            setActivityTitle(conversationTitle);
            handler.removeCallbacks(resetConversationTitleRunnable);
        }
    }

    @Override
    public void onInputPanelExpanded() {
        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
    }

    @Override
    public void onInputPanelCollapsed() {
        // do nothing
    }

    public void toggleMultiMessageMode(UiMessage message) {
        inputPanel.setVisibility(View.GONE);
        message.isChecked = true;
        adapter.setMode(ConversationMessageAdapter.MODE_CHECKABLE);
        adapter.notifyDataSetChanged();
        multiMessageActionContainerLinearLayout.setVisibility(View.VISIBLE);
        setupMultiMessageAction();
    }

    public void toggleConversationMode() {
        inputPanel.setVisibility(View.VISIBLE);
        multiMessageActionContainerLinearLayout.setVisibility(View.GONE);
        adapter.setMode(ConversationMessageAdapter.MODE_NORMAL);
        adapter.clearMessageCheckStatus();
        adapter.notifyDataSetChanged();
    }

    public void setInputText(String text) {
        inputPanel.setInputText(text);
    }

    private void setupMultiMessageAction() {
        multiMessageActionContainerLinearLayout.removeAllViews();
        List<MultiMessageAction> actions = MultiMessageActionManager.getInstance().getConversationActions(conversation);
        int width = getResources().getDisplayMetrics().widthPixels;

        for (MultiMessageAction action : actions) {
            action.onBind(this, conversation);
            ImageView imageView = new ImageView(getActivity());
            imageView.setImageResource(action.iconResId());


            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width / actions.size(), LinearLayout.LayoutParams.WRAP_CONTENT);
            multiMessageActionContainerLinearLayout.addView(imageView, layoutParams);
            ViewGroup.LayoutParams p = imageView.getLayoutParams();
            p.height = 70;
            imageView.requestLayout();

            imageView.setOnClickListener(v -> {
                List<UiMessage> checkedMessages = adapter.getCheckedMessages();
                if (action.confirm()) {
                    new MaterialDialog.Builder(getActivity()).content(action.confirmPrompt())
                            .negativeText(getString(R.string.cancel))
                            .positiveText(getString(R.string.confirm))
                            .onPositive((dialog, which) -> {
                                action.onClick(checkedMessages);
                                toggleConversationMode();
                            })
                            .build()
                            .show();

                } else {
                    action.onClick(checkedMessages);
                    toggleConversationMode();
                }
            });
        }
    }

    @Override
    public void onMessageCheck(UiMessage message, boolean checked) {
        List<UiMessage> checkedMessages = adapter.getCheckedMessages();
        setAllClickableChildViewState(multiMessageActionContainerLinearLayout, checkedMessages.size() > 0);
    }

    private void setAllClickableChildViewState(View view, boolean enable) {
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                setAllClickableChildViewState(((ViewGroup) view).getChildAt(i), enable);
            }
        }
        if (view.isClickable()) {
            view.setEnabled(enable);
        }
    }

    public void pickGroupMemberToVoipChat(boolean isAudioOnly) {
        Intent intent = new Intent(getActivity(), PickGroupMemberActivity.class);
        GroupInfo groupInfo = groupViewModel.getGroupInfo(conversation.target, false);
        intent.putExtra("groupInfo", groupInfo);
//        int maxCount = AVEngineKit.isSupportMultiCall() ? (isAudioOnly ? AVEngineKit.MAX_AUDIO_PARTICIPANT_COUNT - 1 : AVEngineKit.MAX_VIDEO_PARTICIPANT_COUNT - 1) : 1;
        intent.putExtra("maxCount", 9);
        startActivityForResult(intent, isAudioOnly ? REQUEST_CODE_GROUP_AUDIO_CHAT : REQUEST_CODE_GROUP_VIDEO_CHAT);
    }

    private void onPickGroupMemberToVoipChat(Intent intent, boolean isAudioOnly) {
        List<String> memberIds = intent.getStringArrayListExtra(PickGroupMemberActivity.EXTRA_RESULT);
        if (memberIds != null && memberIds.size() > 1) {
            WfcUIKit.multipleCall(getActivity(), conversation.target, memberIds, isAudioOnly);
        }
    }

    @Override
    public void onMessageReceiptCLick(Message message) {
        Map<String, Long> deliveries = adapter.getDeliveries();
        Map<String, Long> readEntries = adapter.getReadEntries();
        int deliveryCount = 0;
        if (deliveries != null) {
            for (Map.Entry<String, Long> delivery : deliveries.entrySet()) {
                if (delivery.getValue() >= message.serverTime) {
                    deliveryCount++;
                }
            }
        }
        int readCount = 0;
        if (readEntries != null) {
            for (Map.Entry<String, Long> readEntry : readEntries.entrySet()) {
                if (readEntry.getValue() >= message.serverTime) {
                    readCount++;
                }
            }
        }
        StringBuilder builder = new StringBuilder();
        builder.append(getString(R.string.count_receive))
                .append(deliveryCount)
                .append("\n")
                .append(getString(R.string.count_no_receive))
                .append(groupInfo.memberCount - 1 - deliveryCount)
                .append("\n")
                .append(getString(R.string.count_read))
                .append(readCount)
                .append("\n")
                .append(getString(R.string.count_unread))
                .append(groupInfo.memberCount - 1 - readCount)
        ;
        new MaterialDialog.Builder(getActivity())
                .title(getString(R.string.receipt))
                .content(builder.toString())
                .build()
                .show();
    }

    @android.webkit.JavascriptInterface
    public String run(String args) {
        String result = null;
        Log.d(TAG, "run args: " + args);
        try {
            JSONObject json = JSON.parseObject(args);
            if (json == null)
                return setReply("-1:json parse error", null).toString();
            String callback = json.getString("callback");
            json = runJsCommand(json, callback);
            if (json != null)
                result = json.toString();
        } catch (Exception e) {
            e.printStackTrace();
            result = setReply("-1:" + e.getLocalizedMessage(), null).toString();
        }
        return result;
    }

    private void getKeyWordList() {
        if (groupInfo == null) {
            return;
        }
        String url = (String) SPUtils.get(getActivity(), "PREFIX", "");
        if (url.equals("") || url == null) {
            return;
        }
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url + BuildConfig.KEYWORD_LIST + "?groupId=" + groupInfo.target)
                .get()
                .addHeader("X-Token", token)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String result = response.body().string();
                try {
                    System.out.println("@@@   result =" + result);
                    if (result == null || result.equals("")) {
                        return;
                    }
                    Gson gson = new Gson();
                    KeyWordListInfo keyWordListInfo = gson.fromJson(result, KeyWordListInfo.class);
                    if (keyWordListInfo.getCode() == 200) {
                        String keyWordList = "";
                        if (keyWordListInfo.getData().getKeywordList().size() == 0) {
                            keyWordList = "";
                        } else {
                            keyWordList = gson.toJson(keyWordListInfo.getData());
                        }
                        SPUtils.put(getActivity(), groupInfo.target + "keyWordList", keyWordList);
                    }
                } catch (Exception e) {

                }
            }
        });
    }
}
