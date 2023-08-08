/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.app.main;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
//import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.king.zxing.Intents;
import com.xiaomi.mipush.sdk.MiPushClient;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.wildfire.chat.app.FileUtil;
import cn.wildfire.chat.app.HashUtil;
import cn.wildfire.chat.app.MyApp;
import cn.wildfire.chat.app.SPUtils;
import cn.wildfire.chat.app.Utils;
//import cn.wildfire.chat.app.push.MyFirebaseMessagingService;
import cn.wildfire.chat.app.login.model.AppVersionInfo;
import cn.wildfire.chat.app.login.model.NoticeInfo;
import cn.wildfire.chat.kit.ChatManagerHolder;
import cn.wildfire.chat.kit.IMConnectionStatusViewModel;
import cn.wildfire.chat.kit.IMServiceStatusViewModel;
import cn.wildfire.chat.kit.UiWrapper;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.WfcScheme;
import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.channel.ChannelInfoActivity;
import cn.wildfire.chat.kit.contact.ContactListFragment;
import cn.wildfire.chat.kit.contact.ContactListFragment2;
import cn.wildfire.chat.kit.contact.ContactViewModel;
import cn.wildfire.chat.kit.contact.newfriend.AddOsnIDActivity;
import cn.wildfire.chat.kit.contact.newfriend.FriendRequestListActivity;
import cn.wildfire.chat.kit.contact.newfriend.SearchUserActivity;
import cn.wildfire.chat.kit.conversation.CreateConversationActivity;
import cn.wildfire.chat.kit.conversationlist.ConversationListFragment;
import cn.wildfire.chat.kit.conversationlist.ConversationListViewModel;
import cn.wildfire.chat.kit.conversationlist.ConversationListViewModelFactory;
import cn.wildfire.chat.kit.group.GroupInfoActivity;
import cn.wildfire.chat.kit.group.GroupListActivity;
import cn.wildfire.chat.kit.kefu.KefuListFragment;
import cn.wildfire.chat.kit.litapp.LitappInfoActivity;
import cn.wildfire.chat.kit.litapp.LitappListActivity;
import cn.wildfire.chat.kit.net.OKHttpHelper;
import cn.wildfire.chat.kit.net.SimpleCallback;
import cn.wildfire.chat.kit.qrcode.ScanQRCodeActivity;
import cn.wildfire.chat.kit.redpacket.RedPacketActivity;
import cn.wildfire.chat.kit.search.SearchPortalActivity;
import cn.wildfire.chat.kit.user.ChangeMyNameActivity;
import cn.wildfire.chat.kit.user.UserInfoActivity;
import cn.wildfire.chat.kit.user.UserViewModel;
import cn.wildfire.chat.kit.viewmodel.MessageViewModel;
import cn.wildfire.chat.kit.voip.MultipleCallActivity;
import cn.wildfire.chat.kit.voip.SingleCallActivity;
import cn.wildfire.chat.kit.widget.ViewPagerFixed;
import cn.wildfirechat.chat.BuildConfig;
import cn.wildfirechat.chat.R;
import cn.wildfirechat.client.ConnectionStatus;
import cn.wildfirechat.client.SqliteUtils;
import cn.wildfirechat.message.CallMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.core.MessageContentType;
import cn.wildfirechat.message.core.MessageStatus;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import q.rorbin.badgeview.QBadgeView;

public class MainActivity extends WfcBaseActivity implements ViewPager.OnPageChangeListener {
    String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_PICK = 100;
    private List<Fragment> mFragmentList = new ArrayList<>(5);
   // private List<Integer> mFragmentIndex = new ArrayList<>(Arrays.asList(R.id.found,R.id.conversation_list, R.id.contact, R.id.me));
    private List<Integer> mFragmentIndex = new ArrayList<>(Arrays.asList(R.id.found,R.id.conversation_list, R.id.contact, R.id.me));
    private boolean pick = false;

    @BindView(R.id.bottomNavigationView)
    BottomNavigationView bottomNavigationView;
    @BindView(R.id.contentViewPager)
    ViewPagerFixed contentViewPager;
    @BindView(R.id.startingTextView)
    TextView startingTextView;
    @BindView(R.id.contentLinearLayout)
    LinearLayout contentLinearLayout;
    @BindView(R.id.ll_contact_title)
    LinearLayout ll_contact_title;

    /*@BindView(R.id.tv_friend)
    TextView tv_friend;
    @BindView(R.id.tv_group)
    TextView tv_group;
    @BindView(R.id.tv_kefu)
    TextView tv_kefu;
    @BindView(R.id.view_friend)
    View view_friend;
    @BindView(R.id.view_group)
    View view_group;
    @BindView(R.id.view_kefu)
    View view_kefu;*/
    @BindView(R.id.unreadFriendRequestCountTextView)
    ImageView unreadFriendRequestCountTextView;

    private QBadgeView unreadMessageUnreadBadgeView;
    private QBadgeView unreadFriendRequestBadgeView;
    private QBadgeView discoveryBadgeView;

    private QBadgeView testView;


    private QBadgeView unreadMessageFriends;
    private QBadgeView unreadMessageGroup;
    private QBadgeView unreadMessagekefu;


    private static final int REQUEST_CODE_SCAN_QR_CODE = 100;
    private static final int REQUEST_IGNORE_BATTERY_CODE = 101;
    private static final int REQUEST_PICK_CONTACT = 102;
    private static boolean isApk = true;

    private boolean isInitialized = false;
    private boolean isShowLogout = false;

    private FoundFragment foundFragment;
    private ConversationListFragment conversationListFragment;
    private ContactListFragment2 contactListFragment;

    private ContactViewModel contactViewModel;
    private ConversationListViewModel conversationListViewModel;

    private Dialog dialog;

    private int selectType = 0;
    private String LOGIN_URL;

    private Observer<Boolean> imStatusLiveDataObserver = status -> {
        if (status && !isInitialized) {
            init();
            isInitialized = true;
        }
    };

    @Override
    protected int contentLayout() {
        return R.layout.main_activity_3;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (contactViewModel != null) {
            contactViewModel.reloadFriendRequestStatus();
            conversationListViewModel.reloadConversationUnreadStatus();
        }
        updateMomentBadgeView();
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void afterViews() {
        Intent intent = getIntent();
        boolean restart = intent.getBooleanExtra("restart", false);
        String new_lang = intent.getStringExtra("new_lang");

        LOGIN_URL = (String) SPUtils.get(MainActivity.this,"LOGIN_URL","");

        if (restart) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setTitle(getString(R.string.restart_app))
                    .setMessage(getString(R.string.restart_app2))
                    .setPositiveButton(getString(cn.wildfire.chat.kit.R.string.confirm), (dialog1, which1) -> {
                        WfcBaseActivity.setBaseLanguage(new_lang);
                        ChatManager.Instance().setLanguage(WfcBaseActivity.toLanguage(new_lang));
                        final Intent intent1 = getPackageManager().getLaunchIntentForPackage(getPackageName());
                        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent1);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(0);
                    }).show();
        }

        String path = new File(getFilesDir(), "sticker").getAbsolutePath();
        FileUtil.delAllFile(path);

        //getOwner2();
        UiWrapper.setUiCallback(builder -> builder.setSmallIcon(R.mipmap.ic_launcher));
        bottomNavigationView.setItemIconTintList(null);
        //     requestPermission();
        IMServiceStatusViewModel imServiceStatusViewModel = ViewModelProviders.of(this).get(IMServiceStatusViewModel.class);
        imServiceStatusViewModel.imServiceStatusLiveData().observe(this, imStatusLiveDataObserver);
        IMConnectionStatusViewModel connectionStatusViewModel = ViewModelProviders.of(this).get(IMConnectionStatusViewModel.class);
        connectionStatusViewModel.connectionStatusLiveData().observe(this, status -> {
            /*if (status == ConnectionStatus.ConnectionStatusTokenIncorrect || status == ConnectionStatus.ConnectionStatusSecretKeyMismatch || status == ConnectionStatus.ConnectionStatusRejected || status == ConnectionStatus.ConnectionStatusLogout) {
                ChatManager.Instance().disconnect(true, true);
                reLogin();
            }*/
            if (status == ConnectionStatus.ConnectionStatusKickOffline) {
                if (!isShowLogout) {
                    isShowLogout = true;
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.warning))
                            .setMessage(getString(R.string.kickoff))
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    MainActivity.logout(MainActivity.this);
                                }
                            })
                            .create().show();
                }
            }
        });
        MessageViewModel messageViewModel = ViewModelProviders.of(this).get(MessageViewModel.class);
        messageViewModel.messageLiveData().observe(this, uiMessage -> {
            if (uiMessage.message.content.getMessageContentType() == MessageContentType.MESSAGE_CONTENT_TYPE_FEED
                    || uiMessage.message.content.getMessageContentType() == MessageContentType.MESSAGE_CONTENT_TYPE_FEED_COMMENT) {
                updateMomentBadgeView();
            }
        });

        if (isApk) {
            OKHttpHelper.get("http://" + MyApp.HOST_IP + ":8300/appVersion", null, new SimpleCallback<String>() {
                @Override
                public void onSuccess1(String t) {

                }
                @Override
                public void onUiSuccess(String s) {
                    try {
                        JSONObject json = JSON.parseObject(s);
                        JSONObject appVersion = json.getJSONObject("appVersion");
                        if (appVersion == null)
                            return;

                        String versionCode = appVersion.getString("versionCode");
                        if (versionCode != null)
                        {
                            int verCode = Integer.getInteger(versionCode);
                            int version = Utils.getVersionCode(MainActivity.this);
                            if (verCode <= version) {
                                return;
                            }
                        }

                        String url = appVersion.getString("url");
                        String versionNew = appVersion.getString("version");
                        String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                        if (!versionNew.equalsIgnoreCase(versionName)) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setPositiveButton(getString(R.string.upgrade), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
    //                                UpdateManager receiver = UpdateManager.inst();
    //                                IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
    //                                registerReceiver(receiver, intentFilter);
    //                                receiver.startUpdate(MainActivity.this, url);
                                    String url_APP_URL = (String) SPUtils.get(MainActivity.this, "APP_URL", "");
                                    Intent intent = new Intent();
                                    intent.setAction("android.intent.action.VIEW");
                                    //Uri uri = Uri.parse(url);
                                    Uri uri = Uri.parse(appVersion.getString("url"));
                                    intent.setData(uri);
                                    startActivity(intent);
                                }
                            });
                            builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                            View view = View.inflate(MainActivity.this, R.layout.update_notify, null);
                            TextView textView = view.findViewById(R.id.updateInfo);
                            textView.setText(appVersion.getString("context"));
                            builder.setView(view);
                            builder.setTitle(getString(R.string.find_new_version) + versionNew);
                            builder.show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onUiFailure(int code, String msg) {
                }
            });

        }
           setVendor();
        ChatManager.Instance().setIconId(R.mipmap.ic_launcher);
        getUpdateApk();
        getGongGao();
        if(LOGIN_URL.equals("") || LOGIN_URL == null){

        }else{
            if(LOGIN_URL.equals("https://luckmoney8888.com/#/login")){
                showDialogInputAccount();
            }
        }

    }

    private void showDialogInputAccount(){
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        String token = sp.getString("token", "");
        String userID = ChatManager.Instance().getUserId();
        String url = (String) SPUtils.get(MainActivity.this, "PREFIX", "");
        if(url.equals("") || url == null){
            return;
        }

        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url+"/im/bindWindow"+"?osnId="+userID)
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
            }
        });
    }


    private void getGongGao() {
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        String token = sp.getString("token", "");
        String url = (String) SPUtils.get(MainActivity.this, "PREFIX", "");
        if(url.equals("") || url == null){
            return;
        }
        String language = sp.getString("language", "0");
        String url1 = url + BuildConfig.getNotice+"?language="+language;
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url1)
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
                if (result == null || result.equals("")) {
                    return;
                }
                try {
                    JSONObject jsonObject = JSONObject.parseObject(result);
                    String data = jsonObject.getString("data");
                    if(TextUtils.isEmpty(data)){
                        return;
                    }
                    Gson gson = new Gson();
                    NoticeInfo noticeInfo = gson.fromJson(result, NoticeInfo.class);
                    if (noticeInfo.getCode() == 200) {

                        View views = View.inflate(MainActivity.this, R.layout.dialog_show_notice, null);
                        Button btn_confirm = views.findViewById(R.id.btn_confirm);
                        TextView tv_notice = views.findViewById(R.id.tv_notice);
                        tv_notice.setText(noticeInfo.getData().getRemark());
                        tv_notice.setMovementMethod(new ScrollingMovementMethod());

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setView(views);
                        builder.setCancelable(false);


                        Looper.prepare();
                        dialog = builder.create();
                        dialog.show();
                        btn_confirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                affirmNotice(token,noticeInfo.getData().getId(),url);
                                dialog.dismiss();
                            }
                        });

                        Looper.loop();
                    }
                }catch (Exception e){
                    return;
                }

            }
        });
    }

    private void affirmNotice(String token, int id,String url){

        if (url == null){
            return;
        }
        if (url.length() < 5) {
            return;
        }

        String url1 = url + BuildConfig.affirmNotice+"?id="+id;
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url1)
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

            }
        });
    }

    private void getUpdateApk() {
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        String token = sp.getString("token", "");
        String url =BuildConfig.AppVersion;
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .get()
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
                    if (result == null || result.equals("")) {
                        return;
                    }
                    Gson gson = new Gson();
                    AppVersionInfo appVersionInfo = gson.fromJson(result, AppVersionInfo.class);
                    if (appVersionInfo.getCode() == 200) {
                        int androidShow = appVersionInfo.getData().getAndroidShow();
                        if(androidShow == 0){
                            return;
                        }
                        int androidCode = appVersionInfo.getData().getAndroidCode();
                        int code = Utils.getVersionCode(MainActivity.this);
                        if(androidCode <= code){
                            return;
                        }
                        View views = View.inflate(MainActivity.this, R.layout.dialog_show_appupdate, null);
                        Button btn_updateapp = views.findViewById(R.id.btn_updateapp);
                        TextView tv_cancle = views.findViewById(R.id.tv_cancle);
                        TextView tv_tishi = views.findViewById(R.id.tv_tishi);
                        tv_tishi.setText(getString(R.string.software_update_prompt) + " V"+appVersionInfo.getData().getAndroidVersion());
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setView(views);
                        builder.setCancelable(false);

                        if (appVersionInfo.getData().getUpdateVersion() == 1) {
                            tv_cancle.setVisibility(View.GONE);
                        }

                        Looper.prepare();
                        dialog = builder.create();
                        dialog.show();
                        btn_updateapp.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                                String updateUrl = "https://luckmoney8888.com/download.html";
                                final Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                //       intent.setData(Uri.parse(appVersionInfo.getData().getApkUrl()));
                                intent.setData(Uri.parse(updateUrl));
                                // 注意此处的判断intent.resolveActivity()可以返回显示该Intent的Activity对应的组件名
                                // 官方解释 : Name of the component implementing an activity that can display the intent
                                if (intent.resolveActivity(getPackageManager()) != null) {
                                    final ComponentName componentName = intent.resolveActivity(getPackageManager());
                                    startActivity(Intent.createChooser(intent, "请选择浏览器"));
                                    finish();
                                } else {
                                    // GlobalMethod.showToast(context, "链接错误或无浏览器");
                                }
                            }
                        });
                        tv_cancle.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                        Looper.loop();
                    }
                }catch (Exception e){
                    return;
                }

            }
        });
    }

    private void init() {
        initView();

        conversationListViewModel = ViewModelProviders
                .of(this, new ConversationListViewModelFactory(Arrays.asList(Conversation.ConversationType.Single, Conversation.ConversationType.Group, Conversation.ConversationType.Channel, Conversation.ConversationType.Notify), Arrays.asList(0)))
                .get(ConversationListViewModel.class);
        conversationListViewModel.unreadCountLiveData().observe(this, unreadCount -> {

            //conversationListViewModel.setConversationListLiveData(selectType);
            if (unreadCount != null && unreadCount.unread > 0) {
                showUnreadMessageBadgeView(unreadCount.unread);
            } else {
                hideUnreadMessageBadgeView();
            }
            /*if (unreadCount != null && unreadCount.unreadFriends > 0) {
                showUnreadMessageFriendsBadgeView(unreadCount.unreadFriends, tv_friend);
            } else {
                hideUnreadMessageBadgeView(unreadMessageFriends);
            }*/

            /*if (unreadCount != null && unreadCount.unreadGroup > 0) {
                showUnreadMessageGroupBadgeView(unreadCount.unreadGroup, tv_group);
            } else {
                hideUnreadMessageBadgeView(unreadMessageGroup);
            }*/
//            if (unreadCount != null && unreadCount.unreadkefu > 0) {
//                showUnreadMessageKefusBadgeView(unreadCount.unreadkefu, tv_kefu);
//            } else {
//                hideUnreadMessageBadgeView(unreadMessagekefu);
//            }

            int friendMessage = ChatManager.Instance().getUnreadFriendRequestStatus();
            if( friendMessage != 0){
                unreadFriendRequestCountTextView.setVisibility(View.VISIBLE);
            }else{
                unreadFriendRequestCountTextView.setVisibility(View.GONE);
            }
        });


        contactViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);
        contactViewModel.friendRequestUpdatedLiveData().observe(this, count -> {
            if (count == null || count == 0) {
                hideUnreadFriendRequestBadgeView();
            } else {
                showUnreadFriendRequestBadgeView(count);
            }
        });

        if (checkDisplayName()) {
            //ignoreBatteryOption();
        }


    }

    private void showUnreadMessageFriendsBadgeView(int count, View view) {
        if (unreadMessageFriends == null) {
            unreadMessageFriends = new QBadgeView(MainActivity.this);
            unreadMessageFriends.bindTarget(view);
            unreadMessageFriends.setGravityOffset(0, 5, true);
        }
        unreadMessageFriends.setBadgeNumber(-1);
    }

    private void showUnreadMessageGroupBadgeView(int count, View view) {
        if (unreadMessageGroup == null) {
            unreadMessageGroup = new QBadgeView(MainActivity.this);
            unreadMessageGroup.bindTarget(view);
            unreadMessageGroup.setGravityOffset(0, 5, true);
        }
        unreadMessageGroup.setBadgeNumber(-1);
    }

    private void showUnreadMessageKefusBadgeView(int count, View view) {
        if (unreadMessagekefu == null) {
            unreadMessagekefu = new QBadgeView(MainActivity.this);
            unreadMessagekefu.bindTarget(view);
            unreadMessagekefu.setGravityOffset(0, 5, true);
        }
        unreadMessagekefu.setBadgeNumber(-1);
    }

    private void hideUnreadMessageBadgeView(QBadgeView view) {
        if (view != null) {
            view.hide(true);
            view = null;
        }
    }

    private void showNewFriendMessageBadgeView(int count){
        if(testView == null){
            BottomNavigationMenuView bottomNavigationMenuView = (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);
            View view = bottomNavigationMenuView.getChildAt(mFragmentIndex.indexOf(R.id.contact));
            testView = new QBadgeView(MainActivity.this);
            testView.bindTarget(view);
            testView.setGravityOffset(40, 0, true);
        }
        testView.setBadgeNumber(-1);
    }

    private void hindNewFriendMessageBadgeView(){
        if(testView != null){
            testView.hide(true);
            testView = null;
        }
    }

    private void showUnreadMessageBadgeView(int count) {
        if (unreadMessageUnreadBadgeView == null) {
            BottomNavigationMenuView bottomNavigationMenuView = ((BottomNavigationMenuView) bottomNavigationView.getChildAt(0));
            View view = bottomNavigationMenuView.getChildAt(mFragmentIndex.indexOf(R.id.conversation_list));
            unreadMessageUnreadBadgeView = new QBadgeView(MainActivity.this);
            unreadMessageUnreadBadgeView.bindTarget(view);
            unreadMessageUnreadBadgeView.setGravityOffset(25, 0, true);
        }
        unreadMessageUnreadBadgeView.setBadgeNumber(-1);
    }

    private void hideUnreadMessageBadgeView() {
        if (unreadMessageUnreadBadgeView != null) {
            unreadMessageUnreadBadgeView.hide(true);
            unreadMessageUnreadBadgeView = null;
        }
    }

    private void updateMomentBadgeView() {
        if (!WfcUIKit.getWfcUIKit().isSupportMoment()) {
            return;
        }
        List<Message> messages = ChatManager.Instance().getMessagesEx2(Collections.singletonList(Conversation.ConversationType.Single), Collections.singletonList(1), Arrays.asList(MessageStatus.Unread), 0, true, 100, null);
        int count = messages == null ? 0 : messages.size();
        if (count > 0) {
            if (discoveryBadgeView == null) {
                BottomNavigationMenuView bottomNavigationMenuView = ((BottomNavigationMenuView) bottomNavigationView.getChildAt(0));
                View view = bottomNavigationMenuView.getChildAt(mFragmentIndex.indexOf(R.id.discovery));
                discoveryBadgeView = new QBadgeView(MainActivity.this);
                discoveryBadgeView.bindTarget(view);
            }
            discoveryBadgeView.setBadgeNumber(count);
        } else {
            if (discoveryBadgeView != null) {
                discoveryBadgeView.hide(true);
                discoveryBadgeView = null;
            }
        }
    }

    private void showUnreadFriendRequestBadgeView(int count) {
        if (unreadFriendRequestBadgeView == null) {
            BottomNavigationMenuView bottomNavigationMenuView = ((BottomNavigationMenuView) bottomNavigationView.getChildAt(0));
            View view = bottomNavigationMenuView.getChildAt(mFragmentIndex.indexOf(R.id.contact));
            unreadFriendRequestBadgeView = new QBadgeView(MainActivity.this);
            unreadFriendRequestBadgeView.bindTarget(view);
            unreadFriendRequestBadgeView.setGravityOffset(40, 0, true);
        }
        unreadFriendRequestBadgeView.setBadgeNumber(count);
    }

    public void hideUnreadFriendRequestBadgeView() {
        if (unreadFriendRequestBadgeView != null) {
            unreadFriendRequestBadgeView.hide(true);
            unreadFriendRequestBadgeView = null;
        }
    }

    @Override
    protected int menu() {
        return R.menu.main;
    }

    @Override
    protected boolean showHomeMenuItem() {
        return false;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private RelativeLayout rl_search;

    private void initView() {
        setTitle(getString(R.string.zolo_name));
        yincangToolbar();
    //    ll_conversation_select.setVisibility(View.VISIBLE);
        rl_search = findViewById(R.id.rl_search);
        rl_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSearchPortal();
            }
        });

        startingTextView.setVisibility(View.GONE);
        contentLinearLayout.setVisibility(View.VISIBLE);

        //设置ViewPager的最大缓存页面
        contentViewPager.setOffscreenPageLimit(3);
        foundFragment = new FoundFragment();
        conversationListFragment = new ConversationListFragment();
        contactListFragment = new ContactListFragment2();
        //DiscoveryFragment discoveryFragment = new DiscoveryFragment();
        //LitappGridFragment litappGridFragment = new LitappGridFragment();
        //ShareHomeFragment shareHomeFragment = new ShareHomeFragment();
        MeFragment meFragment = new MeFragment();
        KefuListFragment kefuListFragment = new KefuListFragment();
        //mFragmentList.add(shareHomeFragment);
        mFragmentList.add(foundFragment);
        mFragmentList.add(conversationListFragment);
        mFragmentList.add(contactListFragment);
        mFragmentList.add(meFragment);
    //    mFragmentList.add(kefuListFragment);
        contentViewPager.setAdapter(new HomeFragmentPagerAdapter(getSupportFragmentManager(), mFragmentList));
        contentViewPager.setOnPageChangeListener(this);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.found:
                    //     showToolbar();
                    yincangToolbar();
                    rl_search.setVisibility(View.GONE);
                    ll_contact_title.setVisibility(View.GONE);
                    contentViewPager.setCurrentItem(mFragmentIndex.indexOf(R.id.discovery));
                    //         setTitle("");
                    if (!isDarkTheme()) {
                        setTitleBackgroundResource(R.color.white, false);
                    }

                    break;
                case R.id.conversation_list:
          //          showToolbar();
                    rl_search.setVisibility(View.GONE);
       //             ll_conversation_select.setVisibility(View.VISIBLE);
                    ll_contact_title.setVisibility(View.GONE);
                    contentViewPager.setCurrentItem(mFragmentIndex.indexOf(R.id.conversation_list));
           //         setTitle("");
                    if (!isDarkTheme()) {
                        setTitleBackgroundResource(R.color.white, false);
                    }
                    break;
                case R.id.contact:
     ///               yincangToolbar();
                //    showToolbar();
          //          rl_search.setVisibility(View.VISIBLE);
          //          ll_contact_title.setVisibility(View.VISIBLE);
                    contentViewPager.setCurrentItem(mFragmentIndex.indexOf(R.id.contact));
               //     setTitle(getString(R.string.page_contact));
         //           setTitle("");
                    if (!isDarkTheme()) {
                        setTitleBackgroundResource(R.color.white, false);
                    }
                    contactListFragment.refreshData();
                    break;
                case R.id.discovery:

                    ll_contact_title.setVisibility(View.GONE);
                    contentViewPager.setCurrentItem(mFragmentIndex.indexOf(R.id.discovery));
                    setTitle(getString(R.string.page_main));
                    if (!isDarkTheme()) {
                        setTitleBackgroundResource(R.color.white, false);
                    }
                    break;
                case R.id.me:
    //                yincangToolbar();
                    rl_search.setVisibility(View.GONE);
                    ll_contact_title.setVisibility(View.GONE);
                    contentViewPager.setCurrentItem(mFragmentIndex.indexOf(R.id.me));
                    /*setTitle(getString(R.string.page_me));
                    if (!isDarkTheme()) {
                        setTitleBackgroundResource(R.color.white, false);
                    }*/
                    break;
                default:
                    break;
            }
            return true;
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                showSearchPortal();
                break;
            case R.id.chat:
                createConversation();
                break;
            case R.id.add_contact:
                addContact();
                //searchUser();
                break;
            case R.id.scan_qrcode:
                String[] permissions = new String[]{Manifest.permission.CAMERA};
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!checkPermission(permissions)) {
                        requestPermissions(permissions, 100);
                        return true;
                    }
                }
                startActivityForResult(new Intent(this, ScanQRCodeActivity.class), REQUEST_CODE_SCAN_QR_CODE);
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSearchPortal() {
        Intent intent = new Intent(this, SearchPortalActivity.class);
        startActivity(intent);
    }

    private void createConversation() {
        Intent intent = new Intent(this, CreateConversationActivity.class);
        startActivity(intent);
    }

    private void searchUser() {
        Intent intent = new Intent(this, SearchUserActivity.class);
        startActivity(intent);
    }

    private void addContact() {
        Intent intent = new Intent(this, AddOsnIDActivity.class);
        startActivity(intent);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (mFragmentIndex.size() > position) {
            bottomNavigationView.setSelectedItemId(mFragmentIndex.get(position));
        }
        contactListFragment.showQuickIndexBar(position == 1);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state != ViewPager.SCROLL_STATE_IDLE) {
            //滚动过程中隐藏快速导航条
            contactListFragment.showQuickIndexBar(false);
        } else {
            contactListFragment.showQuickIndexBar(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_SCAN_QR_CODE){
            if (resultCode == RESULT_OK) {
                String result = data.getStringExtra(Intents.Scan.RESULT);
                onScanPcQrCode(result);
            }
        }else if(requestCode == REQUEST_IGNORE_BATTERY_CODE){
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, getString(R.string.background_info), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static String getObjectImageKey(String path) {
        String fileMd5 = HashUtil.getMD5String(new File(path));
        String dateString = getDateString();
        return String.format("image/%s/%s.jpg", dateString, fileMd5);
    }

    private static String getDateString() {
        return DateFormat.format("yyyyMM", new Date()).toString();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startActivityForResult(new Intent(this, ScanQRCodeActivity.class), REQUEST_CODE_SCAN_QR_CODE);
        }
    }

    private void onScanPcQrCode(String qrcode) {
        String prefix = qrcode.substring(0, qrcode.lastIndexOf('/') + 1);
        String value = qrcode.substring(qrcode.lastIndexOf("/") + 1);
        switch (prefix) {
            case WfcScheme.QR_CODE_PREFIX_PC_SESSION:
                pcLogin(value);
                break;
            case WfcScheme.QR_CODE_PREFIX_USER:
                showUser(value);
                break;
            case WfcScheme.QR_CODE_PREFIX_GROUP:
                joinGroup(value);
                break;
            case WfcScheme.QR_CODE_PREFIX_CHANNEL:
                subscribeChannel(value);
                break;
            case WfcScheme.QR_CODE_PREFIX_LITAPP:
                showLitapp(value);
                break;
            default:
                Toast.makeText(this, "qrcode: " + qrcode, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void pcLogin(String token) {
        Intent intent = new Intent(this, PCLoginActivity.class);
        intent.putExtra("token", token);
        startActivity(intent);
    }

    private void showUser(String uid) {

        UserViewModel userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        UserInfo userInfo = userViewModel.getUserInfo(uid, true);
        if (userInfo == null) {
            return;
        }
        Intent intent = new Intent(this, UserInfoActivity.class);
        intent.putExtra("userInfo", userInfo);
        startActivity(intent);
    }

    private void joinGroup(String groupId) {
        Intent intent = new Intent(this, GroupInfoActivity.class);
        intent.putExtra("groupId", groupId);
        startActivity(intent);
    }

    private void showLitapp(String litappId) {
        Intent intent = new Intent(this, LitappInfoActivity.class);
        intent.putExtra("litappId", litappId);
        startActivity(intent);
    }

    private void subscribeChannel(String channelId) {
        Intent intent = new Intent(this, ChannelInfoActivity.class);
        intent.putExtra("channelId", channelId);
        startActivity(intent);
    }

    private boolean checkDisplayName() {
        UserViewModel userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        UserInfo userInfo = userViewModel.getUserInfo(userViewModel.getUserId(), false);
        if (userInfo != null && TextUtils.equals(userInfo.displayName, userInfo.mobile)) {
            if (!sp.getBoolean("updatedDisplayName", false)) {
                sp.edit().putBoolean("updatedDisplayName", true).apply();
                updateDisplayName();
                return false;
            }
        }
        return true;
    }

    private void updateDisplayName() {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .content(getString(R.string.modify_user_nickname))
                .positiveText(R.string.modify)
                .negativeText(R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent intent = new Intent(MainActivity.this, ChangeMyNameActivity.class);
                        startActivity(intent);
                    }
                }).build();
        dialog.show();
    }
    public static void logout(Activity activity) {
        //不要清除session，这样再次登录时能够保留历史记录。如果需要清除掉本地历史记录和服务器信息这里使用true
        ChatManagerHolder.gChatManager.disconnect(true, false);
        SharedPreferences sp = activity.getSharedPreferences("config", Context.MODE_PRIVATE);
        sp.edit().clear().apply();

        sp = activity.getSharedPreferences("moment", Context.MODE_PRIVATE);
        sp.edit().clear().apply();

        SPUtils.clear(activity);



        OKHttpHelper.clearCookies();

        Intent intent = new Intent(activity, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

   /* @RequiresApi(api = Build.VERSION_CODES.O)
    public void setAlias() {
//        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
//        String alias = sp.getString("alias", null);
        //if(alias == null){
        String alias = ChatManager.Instance().getUserId();
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(alias.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithmException", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UnsupportedEncodingException", e);
        }
        //alias = Base64.encodeToString(hash, Base64.NO_WRAP|Base64.URL_SAFE);
        alias = Base64.getUrlEncoder().encodeToString(hash);
        PushService.setAlias(this, alias);
        //sp.edit().putString("alias", alias).apply();
        Log.d(TAG, "set alias: " + alias);
        //}
    }*/

    public void setVendor() {
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        String vendor = sp.getString("manufacturer", null);
        String language = sp.getString("language", "0");

        if (vendor == null) {
            String regID = MiPushClient.getRegId(MainActivity.this);
            JSONObject json = new JSONObject();
            json.put("osnID", ChatManager.Instance().getUserId());
            json.put("vendor", android.os.Build.MANUFACTURER);
            json.put("name",regID);  //设备号
            json.put("language",language);

            // 写死
            String url_APP_DEVICE = BuildConfig.APP_PUSH_INFO_URL;
            //String url_APP_DEVICE = (String) SPUtils.get(MainActivity.this,"APP_DEVICE","");
            OKHttpHelper.postJsonNoToken(url_APP_DEVICE, json.toString(), new SimpleCallback<String>() {
                @Override
                public void onSuccess1(String t) {

                }

                @Override
                public void onUiSuccess(String result) {
                    try {

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onUiFailure(int code, String msg) {

                }
            });
        }
        /*FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {


            }
        });*/

    }

    @OnClick(R.id.btn_new_friends)
    void showFriendRequest() {
        Intent intent = new Intent(this, FriendRequestListActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btn_groups)
    void showGroupList() {
        Intent intent = new Intent(this, GroupListActivity.class);
        if (pick) {
            intent.putExtra("pick", true);
            startActivityForResult(intent, REQUEST_CODE_PICK);
        } else {
            startActivity(intent);
        }
    }

    @OnClick(R.id.btn_litapp)
    void showLitappList() {
        Intent intent = new Intent(this, LitappListActivity.class);
        if (pick) {
            intent.putExtra("pick", true);
            startActivityForResult(intent, REQUEST_CODE_PICK);
        } else {
            startActivity(intent);
        }
    }
}
