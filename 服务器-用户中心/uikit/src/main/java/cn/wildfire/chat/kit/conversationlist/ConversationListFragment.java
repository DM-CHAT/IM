/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversationlist;

import static android.app.Activity.RESULT_OK;
import static org.webrtc.ContextUtils.getApplicationContext;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.king.zxing.Intents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import cn.bingoogolapple.transformerstip.TransformersTip;
import cn.bingoogolapple.transformerstip.gravity.TipGravity;
import cn.wildfire.chat.kit.BuildConfig;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.WfcScheme;
import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.channel.ChannelInfoActivity;
import cn.wildfire.chat.kit.contact.newfriend.AddFriendActivity;
import cn.wildfire.chat.kit.contact.newfriend.AddOsnIDActivity;
import cn.wildfire.chat.kit.contact.newfriend.FriendRequestListActivity;
import cn.wildfire.chat.kit.conversation.CreateConversationActivity;
import cn.wildfire.chat.kit.conversationlist.notification.ConnectionStatusNotification;
import cn.wildfire.chat.kit.conversationlist.notification.PCOnlineStatusNotification;
import cn.wildfire.chat.kit.conversationlist.notification.StatusNotificationViewModel;
import cn.wildfire.chat.kit.group.GroupInfoActivity;
import cn.wildfire.chat.kit.group.GroupViewModel;
import cn.wildfire.chat.kit.litapp.LitappInfoActivity;
import cn.wildfire.chat.kit.net.OKHttpHelper;
import cn.wildfire.chat.kit.net.SimpleCallback;
import cn.wildfire.chat.kit.qrcode.ScanQRCodeActivity;
import cn.wildfire.chat.kit.redpacket.RedPacketUtils;
import cn.wildfire.chat.kit.search.SearchPortalActivity;
import cn.wildfire.chat.kit.user.UserInfoActivity;
import cn.wildfire.chat.kit.user.UserViewModel;
import cn.wildfire.chat.kit.utils.NetWorkUtils;
import cn.wildfire.chat.kit.utils.SPUtils;
import cn.wildfire.chat.kit.viewmodel.SettingViewModel;
import cn.wildfire.chat.kit.widget.ProgressFragment;
import cn.wildfirechat.client.ConnectionStatus;
import cn.wildfirechat.client.SqliteUtils;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.OrgTag;
import cn.wildfirechat.model.PCOnlineInfo;
import cn.wildfirechat.model.UnreadCount;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback;
import cn.wildfirechat.remote.GeneralCallback2;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import q.rorbin.badgeview.QBadgeView;

public class ConversationListFragment extends ProgressFragment {
    private RecyclerView recyclerView;
    private ConversationListAdapter adapter;
    private List<Conversation.ConversationType> types = Arrays.asList(Conversation.ConversationType.Single,
            Conversation.ConversationType.Group,
            Conversation.ConversationType.Notify);

    private static final List<Integer> lines = Arrays.asList(0);

    private ConversationListViewModel conversationListViewModel;
    private SettingViewModel settingViewModel;
    private LinearLayoutManager layoutManager;
    private Toolbar toolbar;

    List<UserInfo> userInfos;
    private ImageView iv_default;
    private TransformersTip transformersTip;
    private ImageView iv_add;
    private ImageView iv_search;
    private TextView tv_friend;
    private TextView tv_group;
    private View view_friend, view_group;
    private int selectType = 0;
    private ImageView iv_friend, iv_group;
    private TabLayout tabLayout;
    private List<String> tabList = new ArrayList<>();
    private TextView tv_tabItem;
    private View itemView;
    private GroupInfo groupInfo;
    private String clearTimes;

    @Override
    protected int contentLayout() {
        return R.layout.conversationlist_frament2;
    }

    @Override
    protected void afterViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        iv_default = view.findViewById(R.id.iv_default);
        iv_add = view.findViewById(R.id.iv_add);
        iv_search = view.findViewById(R.id.iv_search);
        tv_friend = view.findViewById(R.id.tv_friend);
        tv_group = view.findViewById(R.id.tv_group);
        view_friend = view.findViewById(R.id.view_friend);
        view_group = view.findViewById(R.id.view_group);
        iv_friend = view.findViewById(R.id.iv_friend);
        iv_group = view.findViewById(R.id.iv_group);
        tabLayout = view.findViewById(R.id.tabLayout);



        init();
        //getTabList();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (adapter != null && isVisibleToUser) {
            //切换回来刷新
            //         reloadConversations();
        }
    }

    private void showPopup() {
        View view = getActivity().findViewById(R.id.iv_add);
        transformersTip = new TransformersTip(view, R.layout.popupwindow) {
            @Override
            protected void initView(View contentView) {
                super.initView(contentView);

                LinearLayout ll_new_friend = contentView.findViewById(R.id.ll_new_friend);
                LinearLayout ll_groups = contentView.findViewById(R.id.ll_groups);
                LinearLayout ll_litapp = contentView.findViewById(R.id.ll_litapp);

                ll_new_friend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), CreateConversationActivity.class);
                        startActivity(intent);
                        transformersTip.dismissTip();
                    }
                });
                ll_groups.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), AddFriendActivity.class);
                        startActivity(intent);
                        transformersTip.dismissTip();
                    }
                });
                ll_litapp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        transformersTip.dismissTip();
                        String[] permissions = new String[]{Manifest.permission.CAMERA};
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (!checkPermission(permissions)) {
                                requestPermissions(permissions, 100);
                                return;
                            }
                        }
                        startActivityForResult(new Intent(getActivity(), ScanQRCodeActivity.class), 100);
                    }
                });
            }
        };

        transformersTip.setShadowColor(Color.parseColor("#ADADAD"));
        transformersTip.setArrowHeightDp(8);
        transformersTip.setTipGravity(TipGravity.TO_BOTTOM_CENTER);
        transformersTip.setTipOffsetXDp(-63);
        transformersTip.setBackgroundDimEnabled(true);
        transformersTip.setDismissOnTouchOutside(true);
        transformersTip.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        //    reloadConversations();
        getTabList();
    //    reloadMessage();
    }

    public void reloadMessage111() {
        List<OrgTag> tagList = ChatManager.Instance().listTag();
        if (tagList.size() != 0) {
            for (int j = 0; j < tagList.size(); j++) {
                int finalJ = j;
                ChatManager.Instance().getWorkHandler().post(() -> {

                    List<ConversationInfo> conversations = ChatManager.Instance().getConversationList(types, lines, finalJ + 1);
                    if (conversations != null) {

                        int converSize = conversations.size();
                        for (int i = 0; i < converSize; i++) {
                            if (!conversations.get(i).isSilent) {
                                if (conversations.get(i).unreadCount.unread > 0) {
                                    if(tabLayout.getTabAt(finalJ + 2) != null){
                                        View selected = tabLayout.getTabAt(finalJ + 2).getCustomView();
                                        ImageView iv_yuan = selected.findViewById(R.id.iv_yuandian);
                                        iv_yuan.setVisibility(View.VISIBLE);
                                    }



                                } else {
                                    if(tabLayout.getTabAt(finalJ + 2) != null){
                                        View selected = tabLayout.getTabAt(finalJ + 2).getCustomView();
                                        ImageView iv_yuan = selected.findViewById(R.id.iv_yuandian);
                                        iv_yuan.setVisibility(View.INVISIBLE);
                                    }
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    boolean tabListChanged(List<OrgTag> tagList) {
        if (tabList.size() == 0) {
            return true;
        }
        if (tagList.size() + 2 != tabList.size()) {
            return true;
        }
        if (tagList.size() == 0) {
            return false;
        }

        for (int i = 0; i < tagList.size(); i++) {
            if (!tagList.get(i).tagName.equals(tabList.get(i + 2))) {
                return true;
            }
        }

        return false;
    }

    private void getTabList() {

        List<OrgTag> tagList = ChatManager.Instance().listTag();

        if (!tabListChanged(tagList)) {
            return;
        }

        tabList.clear();
        tabList.add(getString(R.string.friend));
        tabList.add(getString(R.string.group));
        for (OrgTag tag : tagList) {
            tabList.add(tag.tagName);
        }


        int tabListLength = tabList.size();
        tabLayout.removeAllTabs();
        for (int i = 0; i < tabListLength; i++) {
            // tabLayout.addTab(tabLayout.newTab().setText(tabList.get(i)));
            TabLayout.Tab tab = tabLayout.newTab();

            itemView = LayoutInflater.from(getActivity()).inflate(R.layout.tablayout_item, null);
            tv_tabItem = itemView.findViewById(R.id.tv_tabItem);
            tv_tabItem.setText(tabList.get(i));
            tab.setCustomView(itemView);
            tabLayout.addTab(tab);
            /*if (selectType == i) {
                tab.select();
            }*/
        }

    }

    private void init() {

        tabLayout.setSelectedTabIndicatorHeight(0);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                //添加选中Tab的逻辑
                //System.out.println("@@@   tab=" + tab.getPosition());
                //if (selectType)
                selectType = tab.getPosition();
                setConversationListType(selectType);
                View selected = tab.getCustomView();
                TextView textView = selected.findViewById(R.id.tv_tabItem);
                textView.setTextColor(getResources().getColor(R.color.green5));


            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //添加未选中Tab的逻辑
                View selected = tab.getCustomView();
                TextView textView = selected.findViewById(R.id.tv_tabItem);
                textView.setTextColor(getResources().getColor(R.color.gray1));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                //再次选中tab的逻辑
            }
        });


        iv_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup();
            }
        });
        iv_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SearchPortalActivity.class);
                startActivity(intent);
            }
        });
        tv_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_friend.setTextColor(ContextCompat.getColor(getActivity(), cn.wildfire.chat.kit.R.color.black));
                tv_group.setTextColor(ContextCompat.getColor(getActivity(), cn.wildfire.chat.kit.R.color.gray1));

                view_friend.setVisibility(View.VISIBLE);
                view_group.setVisibility(View.INVISIBLE);

                selectType = 0;
                setConversationListType(selectType);
            }
        });
        tv_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_friend.setTextColor(ContextCompat.getColor(getActivity(), cn.wildfire.chat.kit.R.color.gray1));
                tv_group.setTextColor(ContextCompat.getColor(getActivity(), cn.wildfire.chat.kit.R.color.black));

                view_friend.setVisibility(View.INVISIBLE);
                view_group.setVisibility(View.VISIBLE);

                selectType = 1;
                setConversationListType(selectType);
            }
        });

        adapter = new ConversationListAdapter(this);
        conversationListViewModel = ViewModelProviders
                .of(getActivity(), new ConversationListViewModelFactory(types, lines))
                .get(ConversationListViewModel.class);

        /*ChatManager.Instance().getWorkHandler().post(() -> {
            List<ConversationInfo> conversationInfos = ChatManager.Instance().getConversationList(types, lines);
            showContent();

            if (conversationInfos.size() == 0) {
                recyclerView.setVisibility(View.GONE);
                iv_default.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                iv_default.setVisibility(View.GONE);
            }
            adapter.setConversationInfos(conversationInfos);

        });*/

        conversationListViewModel.conversationListLiveData().observe(this, conversationInfos -> {
            showContent();

            if (conversationInfos.size() == 0) {
                recyclerView.setVisibility(View.GONE);
                iv_default.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                iv_default.setVisibility(View.GONE);
            }
            adapter.setConversationInfos(conversationInfos);

            //定时删除消息
            GroupViewModel groupViewModel1 = ViewModelProviders.of(getActivity()).get(GroupViewModel.class);
            for (int i=0;i<conversationInfos.size();i++){
                if(conversationInfos.get(i).conversation.type.equals(Conversation.ConversationType.Group)){
                    groupInfo = groupViewModel1.getGroupInfo(conversationInfos.get(i).conversation.target, false);
                    if(groupInfo == null){
                        return;
                    }
                    clearTimes = groupInfo.getClearTimes();
                    if(!clearTimes.equals("0")){
                        ChatManager.Instance().deleteTimeMessage(clearTimes, groupInfo.target, new GeneralCallback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onFail(int errorCode) {

                            }
                        });
                        List<String> key = new ArrayList<>();
                     //   times[times.length] = "sdfs";
                        List<String> list = new ArrayList<>();
                        list = groupInfo.getTopList();
                        if(list.size() > 0){
                            for(String s : list){
                                s = s.substring(4,s.length());
                                long newTime = System.currentTimeMillis();
                                long oldTime = newTime - Long.valueOf(clearTimes);
                                long saveTime = Long.valueOf(s);
                                if(saveTime < oldTime){
                                    key.add("top_"+s);
                                }
                            }
                            ChatManager.Instance().deleteTopMessage(key, groupInfo.target, new GeneralCallback2() {
                                @Override
                                public void onSuccess(String result) {

                                }

                                @Override
                                public void onFail(int errorCode) {

                                }
                            });
                        }

                    }
                }
            }
        });



        conversationListViewModel.unreadCountLiveData().observe(this, unreadCount -> {
            //System.out.println("@@@    index=" + unreadCount.index);
            //System.out.println("@@@    unreadCount.unreadFriends=" + unreadCount.unreadFriends);
            //System.out.println("@@@    unreadCount.unreadGroup=" + unreadCount.unreadGroup);


            //System.out.println("@@@  unreadCountLiveData begin");
            unreadCount.printMap();


            Set<Integer> keys = unreadCount.unreadMap.keySet();
            for (Integer i : keys) {
                UnreadCount.UnreadObj obj = unreadCount.unreadMap.get(i);
                if (i == -1) {

                    if (obj.unread > 0) {
                        if (tabLayout.getTabAt(0) != null) {
                            View selected = tabLayout.getTabAt(0).getCustomView();
                            ImageView iv_yuan = selected.findViewById(R.id.iv_yuandian);
                            iv_yuan.setVisibility(View.VISIBLE);
                        }

                    } else {
                        if (tabLayout.getTabAt(0) != null) {
                            View selected = tabLayout.getTabAt(0).getCustomView();
                            ImageView iv_yuan = selected.findViewById(R.id.iv_yuandian);
                            iv_yuan.setVisibility(View.INVISIBLE);
                        }
                    }


                } else if (i == -2) {

                    if (obj.unread > 0) {
                        if (tabLayout.getTabAt(1) != null) {
                            View selected = tabLayout.getTabAt(1).getCustomView();
                            ImageView iv_yuan = selected.findViewById(R.id.iv_yuandian);
                            iv_yuan.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (tabLayout.getTabAt(1) != null) {
                            View selected = tabLayout.getTabAt(1).getCustomView();
                            ImageView iv_yuan = selected.findViewById(R.id.iv_yuandian);
                            iv_yuan.setVisibility(View.INVISIBLE);
                        }
                    }

                } else {

                    int index = conversationListViewModel.getShowTabIndex(i);
                    if (index == -1) {
                        System.out.println("@@@ index = -1, tagId = " + i);
                        return;
                    }
                    if (obj.unread > 0) {
                        if (tabLayout.getTabAt(index + 2) != null) {
                            View selected = tabLayout.getTabAt(index+2).getCustomView();
                            ImageView iv_yuan = selected.findViewById(R.id.iv_yuandian);
                            iv_yuan.setVisibility(View.VISIBLE);
                        }

                    } else {
                        if (tabLayout.getTabAt(index + 2) != null) {
                            View selected = tabLayout.getTabAt(index + 2).getCustomView();
                            ImageView iv_yuan = selected.findViewById(R.id.iv_yuandian);
                            iv_yuan.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            }




            /*int index = unreadCount.index;
            if (index == -1) {
                if (unreadCount != null && unreadCount.unreadFriends > 0) {
                    View selected = tabLayout.getTabAt(0).getCustomView();
                    ImageView iv_yuan = selected.findViewById(R.id.iv_yuandian);
                    iv_yuan.setVisibility(View.VISIBLE);
                } else {
                    if (tabLayout.getTabAt(0) != null) {
                        View selected = tabLayout.getTabAt(0).getCustomView();
                        ImageView iv_yuan = selected.findViewById(R.id.iv_yuandian);
                        iv_yuan.setVisibility(View.GONE);
                    }

                }
                if (unreadCount != null && unreadCount.unreadGroup > 0) {
                    if (tabLayout.getTabAt(0) != null) {
                        View selected = tabLayout.getTabAt(1).getCustomView();
                        ImageView iv_yuan = selected.findViewById(R.id.iv_yuandian);
                        iv_yuan.setVisibility(View.VISIBLE);
                    }

                } else {
                    View selected = tabLayout.getTabAt(1).getCustomView();
                    ImageView iv_yuan = selected.findViewById(R.id.iv_yuandian);
                    iv_yuan.setVisibility(View.GONE);
                }
            } else {
                if (unreadCount.unreadFriends > 0 || unreadCount.unreadGroup > 0) {
                    int index1 = index + 1;
                    System.out.println("@@@   index1=" + index1);
                    View selected = tabLayout.getTabAt(index1).getCustomView();
                    ImageView iv_yuan = selected.findViewById(R.id.iv_yuandian);
                    iv_yuan.setVisibility(View.VISIBLE);
                } else {
                    int index1 = index + 1;
                    System.out.println("@@@   index1=" + index1);
                    View selected = tabLayout.getTabAt(index1).getCustomView();
                    ImageView iv_yuan = selected.findViewById(R.id.iv_yuandian);
                    iv_yuan.setVisibility(View.GONE);
                }
            }*/

        });

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(adapter);
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);


        UserViewModel userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        userViewModel.userInfoLiveData().observe(this, new Observer<List<UserInfo>>() {
            @Override
            public void onChanged(List<UserInfo> userInfos) {
                int start = layoutManager.findFirstVisibleItemPosition();
                int end = layoutManager.findLastVisibleItemPosition();
                adapter.notifyItemRangeChanged(start, end - start + 1);
            }
        });
        GroupViewModel groupViewModel = ViewModelProviders.of(this).get(GroupViewModel.class);
        groupViewModel.groupInfoUpdateLiveData().observe(this, new Observer<List<GroupInfo>>() {
            @Override
            public void onChanged(List<GroupInfo> groupInfos) {
                int start = layoutManager.findFirstVisibleItemPosition();
                int end = layoutManager.findLastVisibleItemPosition();
                adapter.notifyItemRangeChanged(start, end - start + 1);
            }
        });

        StatusNotificationViewModel statusNotificationViewModel = WfcUIKit.getAppScopeViewModel(StatusNotificationViewModel.class);
        statusNotificationViewModel.statusNotificationLiveData().observe(this, new Observer<Object>() {
            @Override
            public void onChanged(Object o) {
                adapter.updateStatusNotification(statusNotificationViewModel.getNotificationItems());
            }
        });
        conversationListViewModel.connectionStatusLiveData().observe(this, status -> {
            ConnectionStatusNotification connectionStatusNotification = new ConnectionStatusNotification();
            switch (status) {
                case ConnectionStatus.ConnectionStatusConnecting:
                    connectionStatusNotification.setValue(getString(R.string.linking));
                    statusNotificationViewModel.showStatusNotification(connectionStatusNotification);
                    break;
                case ConnectionStatus.ConnectionStatusReceiveing:
                    connectionStatusNotification.setValue(getString(R.string.syncing));
                    statusNotificationViewModel.showStatusNotification(connectionStatusNotification);
                    break;
                case ConnectionStatus.ConnectionStatusConnected:
                    statusNotificationViewModel.hideStatusNotification(connectionStatusNotification);
                    break;
                case ConnectionStatus.ConnectionStatusUnconnected:
                    connectionStatusNotification.setValue(getString(R.string.link_fail));
                    statusNotificationViewModel.showStatusNotification(connectionStatusNotification);
                    break;
                default:
                    break;
            }
        });
        settingViewModel = new ViewModelProvider(this).get(SettingViewModel.class);
        settingViewModel.settingUpdatedLiveData().observe(this, o -> {
            if (ChatManager.Instance().getConnectionStatus() == ConnectionStatus.ConnectionStatusReceiveing) {
                return;
            }
            conversationListViewModel.reloadConversationList(true);
            conversationListViewModel.reloadConversationUnreadStatus();

            List<PCOnlineInfo> infos = ChatManager.Instance().getPCOnlineInfos();
            statusNotificationViewModel.clearStatusNotificationByType(PCOnlineStatusNotification.class);
            if (infos.size() > 0) {
                for (PCOnlineInfo info : infos) {
                    PCOnlineStatusNotification notification = new PCOnlineStatusNotification(info);
                    statusNotificationViewModel.showStatusNotification(notification);

                    SharedPreferences sp = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
                    sp.edit().putBoolean("wfc_uikit_had_pc_session", true).commit();
                }
            }
        });
        List<PCOnlineInfo> pcOnlineInfos = ChatManager.Instance().getPCOnlineInfos();
        if (pcOnlineInfos != null && !pcOnlineInfos.isEmpty()) {
            for (PCOnlineInfo info : pcOnlineInfos) {
                PCOnlineStatusNotification notification = new PCOnlineStatusNotification(info);
                statusNotificationViewModel.showStatusNotification(notification);

                SharedPreferences sp = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
                sp.edit().putBoolean("wfc_uikit_had_pc_session", true).commit();
            }
        }
    }

    private void reloadConversations() {
        /*if (ChatManager.Instance().getConnectionStatus() == ConnectionStatus.ConnectionStatusReceiveing) {
            return;
        }*/
        conversationListViewModel.reloadConversationList();
        conversationListViewModel.reloadConversationUnreadStatus();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void setConversationListType(int type) {

        //if(NetWorkUtils.isNetworkAvailable(getActivity()))
        {
            conversationListViewModel.setConversationListLiveData(type);
            reloadConversations();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                String result = data.getStringExtra(Intents.Scan.RESULT);
                onScanPcQrCode(result);
            }
        }
    }

    public void androidVerify(String str){
        //获取到ospn:// 截取到weblogin/{}内容
        //weblogin方法
        //将{}数据转换成json

        /*JSONObject json = new JSONObject();
        json.put("url", url);
        json.put("webId", webId);
        json.put("timestamp", timestamp);
        json.put("sign", sign);
        json.put("osnId", ChatManager.Instance().getUserId());*/
        //发送请求给小程序后端，请求地址是url，请求参数是json
        SharedPreferences sp = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
        String token = sp.getString("token", null);
        String url = null;
        OkHttpClient okHttpClient = new OkHttpClient();
        JSONObject requestData = new JSONObject();
        String json = "";
        try {
            String str1 = str.split("ospn://weblogin/")[1];
            //String str4 = str3[1];//数据
            JSONObject jsonObject = JSONObject.parseObject(str1);
            url = jsonObject.getString("url");
            String webId = jsonObject.getString("webId");
            String timestamp = System.currentTimeMillis() + "";
            //hash
            String hashStr = url+webId+timestamp;
            String hash = ChatManager.Instance().hashData(hashStr.getBytes());
            //生成App签名
            String sign = ChatManager.Instance().signData(hash.getBytes());


            requestData.put("url",url);
            requestData.put("webId", webId);
            requestData.put("timestamp", timestamp);
            requestData.put("sign", sign);
            requestData.put("osnId", ChatManager.Instance().getUserId());

            json = requestData.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , json);

        final Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("X-Token",token)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                System.out.println("@@@   扫码失败="+e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String result = response.body().string();
                System.out.println("@@@   扫码成功: "+result);
            }
        });
    }

    private void onScanPcQrCode(String qrcode) {
        System.out.println("@@@         qrcode="+qrcode);
        if(qrcode.startsWith("ospn://weblogin/")){
            androidVerify(qrcode);
            return;
        }
        String prefix = qrcode.substring(0, qrcode.lastIndexOf('/') + 1);
        String value = qrcode.substring(qrcode.lastIndexOf("/") + 1);
        System.out.println("@@@      prefix="+prefix);
        System.out.println("@@@      value="+value);
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
                Toast.makeText(getActivity(), "qrcode: " + qrcode, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void pcLogin(String token) {
       /* Intent intent = new Intent(this, PCLoginActivity.class);
        intent.putExtra("token", token);
        startActivity(intent);*/
    }

    private void subscribeChannel(String channelId) {
        Intent intent = new Intent(getActivity(), ChannelInfoActivity.class);
        intent.putExtra("channelId", channelId);
        startActivity(intent);
    }

    private void showUser(String uid) {

        UserViewModel userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        UserInfo userInfo = userViewModel.getUserInfo(uid, true);
        if (userInfo == null) {
            return;
        }
        Intent intent = new Intent(getActivity(), UserInfoActivity.class);
        intent.putExtra("userInfo", userInfo);
        startActivity(intent);
    }

    private void joinGroup(String groupId) {
        Intent intent = new Intent(getActivity(), GroupInfoActivity.class);
        intent.putExtra("groupId", groupId);
        intent.putExtra("refresh", true);
        startActivity(intent);
    }

    private void showLitapp(String litappId) {
        Intent intent = new Intent(getActivity(), LitappInfoActivity.class);
        intent.putExtra("litappId", litappId);
        startActivity(intent);
    }
}
