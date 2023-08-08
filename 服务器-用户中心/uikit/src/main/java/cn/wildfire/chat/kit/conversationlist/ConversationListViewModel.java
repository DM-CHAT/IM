/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversationlist;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import cn.wildfire.chat.kit.BuildConfig;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.net.OKHttpHelper;
import cn.wildfire.chat.kit.net.SimpleCallback;
import cn.wildfire.chat.kit.redpacket.RedPacketUtils;
import cn.wildfire.chat.kit.utils.SPUtils;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.OrgTag;
import cn.wildfirechat.model.UnreadCount;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback;
import cn.wildfirechat.remote.OnClearMessageListener;
import cn.wildfirechat.remote.OnConnectionStatusChangeListener;
import cn.wildfirechat.remote.OnConversationInfoUpdateListener;
import cn.wildfirechat.remote.OnDeleteMessageListener;
import cn.wildfirechat.remote.OnRecallMessageListener;
import cn.wildfirechat.remote.OnReceiveMessageListener;
import cn.wildfirechat.remote.OnRemoveConversationListener;
import cn.wildfirechat.remote.OnSendMessageListener;

/**
 * how
 * 1. observe conversationInfoLiveData in your activity or fragment, but if you still not called getConversationList,
 * just ignore the data.
 * 2. call getConversationList
 */
public class ConversationListViewModel extends ViewModel implements OnReceiveMessageListener,
        OnSendMessageListener,
        OnRecallMessageListener,
        OnDeleteMessageListener,
        OnConversationInfoUpdateListener,
        OnRemoveConversationListener,
        OnConnectionStatusChangeListener,
        OnClearMessageListener {

    private MutableLiveData<List<ConversationInfo>> conversationListLiveData;
    private MutableLiveData<UnreadCount> unreadCountLiveData;
    private MutableLiveData<Integer> connectionStatusLiveData = new MutableLiveData<>();

    private List<Conversation.ConversationType> types;
    private List<Integer> lines;
    List<UserInfo> costumerInfos;

    List<ConversationInfo> listUser = new ArrayList<>();
    List<ConversationInfo> listGroup = new ArrayList<>();
    //List<ConversationInfo> listKefu = new ArrayList<>();

    //Map<Integer, TagConversation> tagMap = new HashMap<>();
    public Map<Integer, OrgTag> tagMap = new HashMap<>();

    private int selectType = -1;//默认选择好友列表
    private int tagId = -1;
    private int firstType = 0;

    class TagConversation {
        public String tagName;
        public List<ConversationInfo> convList = new ArrayList<>();

        public void insert(ConversationInfo conv) {
            convList.add(conv);
        }

        public void clear() {
            convList.clear();
        }
    }

    public int getShowTabIndex(int tagId) {

        for (Integer i : tagMap.keySet()) {
            OrgTag tag = tagMap.get(i);
            if (tag.id == tagId) {
                return i;
            }
        }
        return -1;
    }

    public ConversationListViewModel(List<Conversation.ConversationType> types, List<Integer> lines) {
        super();
        this.types = types;
        this.lines = lines;
        ChatManager.Instance().addOnReceiveMessageListener(this);
        ChatManager.Instance().addSendMessageListener(this);
        ChatManager.Instance().addConversationInfoUpdateListener(this);
        ChatManager.Instance().addRecallMessageListener(this);
        ChatManager.Instance().addConnectionChangeListener(this);
        ChatManager.Instance().addDeleteMessageListener(this);
        ChatManager.Instance().addClearMessageListener(this);
        ChatManager.Instance().addRemoveConversationListener(this);
        String url_KEFU_LIST = "";
        //为了从好友列表里面筛选客户 专门请求数据
        OKHttpHelper.postJson(url_KEFU_LIST, "", new SimpleCallback<String>() {
            @Override
            public void onSuccess1(String t) {

            }

            @Override
            public void onUiSuccess(String result) {
                try {

                    JSONObject json = RedPacketUtils.getData(WfcUIKit.getActivity(), result);
                    if (json == null)
                        return;
                    List<String> kefuList = json.getJSONArray("kf").toJavaList(String.class);
                    costumerInfos = new ArrayList<>();
                    for (String kefu : kefuList) {
                        UserInfo userInfo = ChatManager.Instance().getUserInfo(kefu, false);
                        if (userInfo != null) {
                            costumerInfos.add(userInfo);
                        }
                    }
                    reloadConversationUnreadStatus();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onUiFailure(int code, String msg) {

            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        ChatManager.Instance().removeOnReceiveMessageListener(this);
        ChatManager.Instance().removeSendMessageListener(this);
        ChatManager.Instance().removeConversationInfoUpdateListener(this);
        ChatManager.Instance().removeConnectionChangeListener(this);
        ChatManager.Instance().removeRecallMessageListener(this);
        ChatManager.Instance().removeDeleteMessageListener(this);
        ChatManager.Instance().removeClearMessageListener(this);
        ChatManager.Instance().removeRemoveConversationListener(this);
    }

    private AtomicInteger loadingCount = new AtomicInteger(0);

    public void reloadConversationList() {
        reloadConversationList(false);
    }

    public void reloadConversationList(boolean force) {
        if (conversationListLiveData == null) {
            return;
        }
        if (!force) {
            int count = loadingCount.get();
            if (count > 0) {
                return;
            }
        }
        loadingCount.incrementAndGet();

        ChatManager.Instance().getWorkHandler().post(() -> {
            loadingCount.decrementAndGet();

            List<ConversationInfo> conversationInfos = ChatManager.Instance().getConversationList(types, lines, tagId);

            List<OrgTag> tagList = ChatManager.Instance().listTag();
            flashTagMap(tagList);

            selectConversation(conversationInfos);

            if (conversationInfos != null) {
                conversationListLiveData.postValue(conversationInfos);
            }

            /*List<ConversationInfo> list = getConversationSelect(conversationInfos);
            if (list != null) {
                conversationListLiveData.postValue(list);   //通知消息没法显示
            }*/

            //   conversationListLiveData.postValue(conversationInfos);

        });
    }

    private List<ConversationInfo> getConversationSelect(List<ConversationInfo> convList) {

        // for test
        //if (convList != null)
        //return convList;

      //  System.out.println("@@@ select type : " + selectType);

        if (selectType == 1) {
            return listGroup;
        } else if (selectType == 0) {
            return listUser;
        } else if (selectType == -1) {
            return listUser;
        } else {
            return convList;
        }

        //return null;
    }

    public List<ConversationInfo> getConversationList(List<Conversation.ConversationType> conversationTypes,
                                                      List<Integer> lines,
                                                      int tagId) {
        return ChatManager.Instance().getConversationList(conversationTypes, lines, tagId);
    }

    public MutableLiveData<List<ConversationInfo>> conversationListLiveData() {
        if (conversationListLiveData == null) {
            conversationListLiveData = new MutableLiveData<>();
        }
        /*ChatManager.Instance().getWorkHandler().post(() -> {
            List<ConversationInfo> conversationInfos = ChatManager.Instance().getConversationList(types, lines);
            conversationListLiveData.postValue(conversationInfos);
        });*/
        //通知消息不显示，注释这个
        ChatManager.Instance().getWorkHandler().post(() -> {

            List<ConversationInfo> conversationInfos = ChatManager.Instance().getConversationList(types, lines, tagId);

            List<OrgTag> tagList = ChatManager.Instance().listTag();
            flashTagMap(tagList);

            selectConversation(conversationInfos);
            conversationListLiveData.postValue(conversationInfos);
        });

        return conversationListLiveData;
    }

    public MutableLiveData<UnreadCount> unreadCountLiveData() {
        if (unreadCountLiveData == null) {
            unreadCountLiveData = new MutableLiveData<>();
        }

        reloadConversationUnreadStatus();
        return unreadCountLiveData;
    }


    public MutableLiveData<Integer> connectionStatusLiveData() {
        return connectionStatusLiveData;
    }

    public void reloadConversationUnreadStatus() {
        ChatManager.Instance().getWorkHandler().post(() -> {

            List<ConversationInfo> conversations = ChatManager.Instance().getConversationList(types, lines, -3);


            if (conversations != null) {
                //UnreadCount unreadCount = new UnreadCount(tagMap);
                UnreadCount unreadCount = new UnreadCount();
                unreadCount.index = tagId;
                //int converSize = conversations.size();

                for (ConversationInfo conv : conversations) {
                    if (!conv.isSilent) {
                        if (conv.tagId == -1) {
                            if (conv.conversation.type == Conversation.ConversationType.Single
                                    || conv.conversation.type == Conversation.ConversationType.Notify) {
                                // tagName == 好友
                                UnreadCount.UnreadObj unreadObj = unreadCount.getUnreadObj(-1);
                                unreadObj.unread += conv.unreadCount.unread;
                                unreadObj.unreadMention += conv.unreadCount.unreadMention;
                            }
                        } else if (conv.tagId == -2){
                            if (conv.conversation.type == Conversation.ConversationType.Group) {
                                // tagName == 群组
                                UnreadCount.UnreadObj unreadObj = unreadCount.getUnreadObj(-2);
                                unreadObj.unread += conv.unreadCount.unread;
                                unreadObj.unreadMention += conv.unreadCount.unreadMention;
                            }
                        } else {
                            // 自定义标签
                            UnreadCount.UnreadObj unreadObj = unreadCount.getUnreadObj(conv.tagId);
                            unreadObj.unread += conv.unreadCount.unread;
                            unreadObj.unreadMention += conv.unreadCount.unreadMention;
                        }
                    }
                    unreadCount.unread += conv.unreadCount.unread;
                    unreadCount.unreadMention += conv.unreadCount.unreadMention;
                    unreadCount.unreadMentionAll += conv.unreadCount.unreadMentionAll;
                }



                /*for (int i = 0; i < converSize; i++) {
                    if (!conversations.get(i).isSilent) {
                        unreadCount.unread += conversations.get(i).unreadCount.unread;

                        if (conversations.get(i).conversation.type == Conversation.ConversationType.Single) {
                            unreadCount.unreadFriends += conversations.get(i).unreadCount.unread;
                        } else if (conversations.get(i).conversation.type == Conversation.ConversationType.Group) {
                            unreadCount.unreadGroup += conversations.get(i).unreadCount.unread;
                        }
                    }
                    unreadCount.unreadMention += conversations.get(i).unreadCount.unreadMention;
                    unreadCount.unreadMentionAll += conversations.get(i).unreadCount.unreadMentionAll;
                }*/



                unreadCount.printMap();

                postUnreadCount(unreadCount);
            }
        });
    }

    private void postUnreadCount(UnreadCount unreadCount) {
        if (unreadCountLiveData == null) {
            return;
        }
        unreadCountLiveData.postValue(unreadCount);
    }

    @Override
    public void onReceiveMessage(List<Message> messages, boolean hasMore) {
        reloadConversationList(true);
        reloadConversationUnreadStatus();
    }

    @Override
    public void onRecallMessage(Message message) {
        reloadConversationList(true);
        reloadConversationUnreadStatus();
    }


    @Override
    public void onSendSuccess(Message message) {
        reloadConversationList();
    }

    @Override
    public void onSendFail(Message message, int errorCode) {
        reloadConversationList();
    }

    @Override
    public void onSendPrepare(Message message, long savedTime) {
        Conversation conversation = message.conversation;
        if (types.contains(conversation.type) && lines.contains(conversation.line)) {
            if (message.messageId > 0) {
                reloadConversationList();
            }
        }
    }

    public void removeConversation(ConversationInfo conversationInfo, boolean clearMsg) {
        ChatManager.Instance().clearUnreadStatus(conversationInfo.conversation);
        ChatManager.Instance().removeConversation(conversationInfo.conversation, clearMsg);
    }

    public void clearMessages(Conversation conversation) {
        ChatManager.Instance().clearMessages(conversation);
    }

    // TODO move the following to another class
    public void unSubscribeChannel(ConversationInfo conversationInfo) {
        ChatManager.Instance().listenChannel(conversationInfo.conversation.target, false, new GeneralCallback() {
            @Override
            public void onSuccess() {
                removeConversation(conversationInfo, false);
            }

            @Override
            public void onFail(int errorCode) {
                // do nothing
            }
        });
    }

    public void setConversationTop(ConversationInfo conversationInfo, boolean top) {
        ChatManager.Instance().setConversationTop(conversationInfo.conversation, top);
    }

    @Override
    public void onDeleteMessage(Message message) {
        reloadConversationList();
        reloadConversationUnreadStatus();
    }

    @Override
    public void onConversationDraftUpdate(ConversationInfo conversationInfo, String draft) {
        reloadConversationList();
    }

    @Override
    public void onConversationTopUpdate(ConversationInfo conversationInfo, boolean top) {
        reloadConversationList();
    }

    @Override
    public void onConversationSilentUpdate(ConversationInfo conversationInfo, boolean silent) {
        reloadConversationList();
    }

    @Override
    public void onConversationUnreadStatusClear(ConversationInfo conversationInfo) {
        reloadConversationList();
        reloadConversationUnreadStatus();
    }

    @Override
    public void onConnectionStatusChange(int status) {
        connectionStatusLiveData.postValue(status);
    }

    @Override
    public void onClearMessage(Conversation conversation) {
        reloadConversationList();
        reloadConversationUnreadStatus();
    }

    @Override
    public void onConversationRemove(Conversation conversation) {
        reloadConversationList();
        reloadConversationUnreadStatus();
    }

    public void setConversationListLiveData(int type) {
        this.selectType = type;

        if (selectType == 0) {
            tagId = -1;
        } else if (selectType == 1) {
            tagId = -2;
        } else {
            try {
                tagId = tagMap.get(selectType - 2).id;
            } catch (Exception e) {

            }
        }
        //System.out.println("@@@     selectType111="+selectType);

        // for test
        //tagId = 5;

    }

    private void flashTagMap(List<OrgTag> tagList) {
        tagMap = new HashMap<>();
        int i = 0;
        for (OrgTag orgTag : tagList) {
            tagMap.put(i, orgTag);
            i++;
        }
    }

    private void selectConversation(List<ConversationInfo> conversationInfos) {

        return;

        /*if (selectType > 1) {
            return;
        }

        int converSize = conversationInfos.size();
        if (converSize != 0) {
            listUser = new ArrayList<>();
            listGroup = new ArrayList<>();

            for (int i = 0; i < converSize; i++) {
                if (conversationInfos.get(i).conversation.type == Conversation.ConversationType.Single) {
                    listUser.add(conversationInfos.get(i));
//                    }
                } else if (conversationInfos.get(i).conversation.type == Conversation.ConversationType.Group) {
                    listGroup.add(conversationInfos.get(i));
                } else if (conversationInfos.get(i).conversation.type == Conversation.ConversationType.Notify) {
                    listUser.add(conversationInfos.get(i));
                }

            }
        }*/
    }

}
