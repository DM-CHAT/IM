/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversation.ext.core;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.fragment.app.Fragment;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import cn.wildfire.chat.kit.BuildConfig;
import cn.wildfire.chat.kit.conversation.DappTransferExt;
import cn.wildfire.chat.kit.conversation.DappTransferExt1;
import cn.wildfire.chat.kit.conversation.ext.FileExt;
import cn.wildfire.chat.kit.conversation.ext.ImageExt;
import cn.wildfire.chat.kit.conversation.ext.LocationExt;
import cn.wildfire.chat.kit.conversation.ext.RedPacketBombExt;
import cn.wildfire.chat.kit.conversation.ext.RedPacketExt;
import cn.wildfire.chat.kit.conversation.ext.RedPacketGroupExt;
import cn.wildfire.chat.kit.conversation.ext.ShootExt;
import cn.wildfire.chat.kit.conversation.ext.UserCardExt;
import cn.wildfire.chat.kit.conversation.ext.VoipExt;
import cn.wildfire.chat.kit.utils.SPUtils;
import cn.wildfirechat.client.ClientService;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.remote.ChatManager;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class ConversationExtManager {
    private static ConversationExtManager instance;
    private List<ConversationExt> conversationExts;
    private static Context context;
    private ConversationExtManager(Context context) {
        conversationExts = new ArrayList<>();
        this.context = context;
        init();
    }

    public static synchronized ConversationExtManager getInstance() {
        if (instance == null) {
            instance = new ConversationExtManager(context);
        }
        return instance;
    }

    private void init() {
        registerExt(ImageExt.class);

        registerExt(VoipExt.class);

        registerExt(ShootExt.class);
        registerExt(FileExt.class);
    //    registerExt(LocationExt.class);
        //registerExt(ExampleAudioInputExt.class);
        registerExt(UserCardExt.class);


        /*if (isEnable()) {

        }*/
    //    registerExt(RedPacketExt.class);
        registerExt(RedPacketGroupExt.class);
        registerExt(RedPacketBombExt.class);
       /* if(isHideEnable3()) {
            registerExt(DappTransferExt.class);
        }*/
        registerExt(DappTransferExt.class);
        registerExt(DappTransferExt1.class);
    }
    public void registerExt(Class<? extends ConversationExt> clazz) {
        Constructor constructor;
        try {
            constructor = clazz.getConstructor();
            ConversationExt ext = (ConversationExt) constructor.newInstance();
            conversationExts.add(ext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unregisterExt(Class<? extends ConversationExt> clazz) {
        // TODO
    }

    public List<ConversationExt> getConversationExts(Conversation conversation) {
        List<ConversationExt> currentExts = new ArrayList<>();
        for (ConversationExt ext : this.conversationExts) {
            if (!ext.filter(conversation)) {
                currentExts.add(ext);
            }
        }
        return currentExts;
    }
    private boolean isEnable() {

        return ChatManager.Instance().getHideEnable();
        //return false;
    }

    private boolean isHideEnable3(){
        return ChatManager.Instance().getHideEnable3();
    }

}
