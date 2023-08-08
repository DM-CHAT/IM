/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.lqr.emoji.LQREmotionKit;

import org.webrtc.PeerConnectionFactory;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import cn.wildfire.chat.kit.common.AppScopeViewModel;
import cn.wildfire.chat.kit.net.OKHttpHelper;
import cn.wildfire.chat.kit.third.utils.UIUtils;
//import cn.wildfire.chat.kit.voip.AsyncPlayer;
//import cn.wildfire.chat.kit.voip.MultiCallActivity;
//import cn.wildfire.chat.kit.voip.SingleCallActivity;
//import cn.wildfire.chat.kit.voip.VoipCallService;
//import cn.wildfirechat.avenginekit.AVEngineKit;
//import cn.wildfirechat.avenginekit.VideoProfile;
import cn.wildfire.chat.kit.utils.SPUtils;
import cn.wildfire.chat.kit.voip.MultipleCallActivity;
import cn.wildfire.chat.kit.voip.SingleCallActivity;
import cn.wildfirechat.client.NotInitializedExecption;
import cn.wildfirechat.message.CallMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.core.MessageContentType;
import cn.wildfirechat.message.core.PersistFlag;
import cn.wildfirechat.message.notification.PCLoginRequestMessageContent;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.OnFriendUpdateListener;
import cn.wildfirechat.remote.OnRecallMessageListener;
import cn.wildfirechat.remote.OnReceiveMessageListener;


public class WfcUIKit implements OnReceiveMessageListener, OnRecallMessageListener, OnFriendUpdateListener {

    private boolean isBackground = true;
    private Application application;
    private static ViewModelProvider viewModelProvider;
    private ViewModelStore viewModelStore;
    private AppServiceProvider appServiceProvider;
    private static WfcUIKit wfcUIKit;
    private boolean isSupportMoment = false;
    private Activity gActivity;

    private WfcUIKit() {
    }

    public static WfcUIKit getWfcUIKit() {
        if (wfcUIKit == null) {
            wfcUIKit = new WfcUIKit();
        }
        return wfcUIKit;
    }

    public void init(Application application, String imServer) {
        this.application = application;
        UIUtils.application = application;
        initWFClient(application,imServer);
        //initMomentClient(application);
        //初始化表情控件
        LQREmotionKit.init(application, (context, path, imageView) -> Glide.with(context).load(path).apply(new RequestOptions().centerCrop().diskCacheStrategy(DiskCacheStrategy.RESOURCE).dontAnimate()).into(imageView));

        ProcessLifecycleOwner.get().getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            public void onForeground() {
                WfcNotificationManager.getInstance().clearAllNotification(application);
                isBackground = false;

                // 处理没有后台弹出界面权限
//                AVEngineKit.CallSession session = AVEngineKit.Instance().getCurrentSession();
//                if (session != null) {
//                    onReceiveCall(session);
//                }
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            public void onBackground() {
                isBackground = true;
            }
        });

        viewModelStore = new ViewModelStore();
        ViewModelProvider.Factory factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application);
        viewModelProvider = new ViewModelProvider(viewModelStore, factory);
        OKHttpHelper.init(application.getApplicationContext());

        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions.builder(application.getApplicationContext()).createInitializationOptions());

        Log.d("WfcUIKit", "init end");
    }

    public boolean isSupportMoment() {
        return isSupportMoment;
    }

    public Application getApplication() {
        return application;
    }

    private void initWFClient(Application application,String imServer) {
        ChatManager.init(application, imServer);
        try {
            ChatManagerHolder.gChatManager = ChatManager.Instance();
            ChatManagerHolder.gChatManager.startLog();
            ChatManagerHolder.gChatManager.addOnReceiveMessageListener(this);
            ChatManagerHolder.gChatManager.addRecallMessageListener(this);
            ChatManagerHolder.gChatManager.addFriendUpdateListener(this);

//            ringPlayer = new AsyncPlayer(null);
//            AVEngineKit.init(application, this);
//            ChatManagerHolder.gAVEngine = AVEngineKit.Instance();
//            for (String[] server : Config.ICE_SERVERS) {
//                ChatManagerHolder.gAVEngine.addIceServer(server[0], server[1], server[2]);
//            }

            SharedPreferences sp = application.getSharedPreferences("config", Context.MODE_PRIVATE);
            String id = sp.getString("id", null);
            String token = sp.getString("token", null);
//            if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(token)) {
//                //需要注意token跟clientId是强依赖的，一定要调用getClientId获取到clientId，然后用这个clientId获取token，这样connect才能成功，如果随便使用一个clientId获取到的token将无法链接成功。
//                //另外不能多次connect，如果需要切换用户请先disconnect，然后3秒钟之后再connect（如果是用户手动登录可以不用等，因为用户操作很难3秒完成，如果程序自动切换请等3秒）
//                ChatManagerHolder.gChatManager.connect(id, token);
//            }
        } catch (NotInitializedExecption notInitializedExecption) {
            notInitializedExecption.printStackTrace();
        }
    }

    private void initMomentClient(Application application) {
        String momentClientClassName = "cn.wildfirechat.moment.MomentClient";
        try {
            Class clazz = Class.forName(momentClientClassName);
            Constructor constructor = clazz.getConstructor();
            Object o = constructor.newInstance();
            Method method = clazz.getMethod("init", Context.class);
            method.invoke(o, application);
            isSupportMoment = true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 当{@link ViewModel} 需要跨{@link Activity} 共享数据时使用
     */
    public static <T extends ViewModel> T getAppScopeViewModel(@NonNull Class<T> modelClass) {
        if (!AppScopeViewModel.class.isAssignableFrom(modelClass)) {
            throw new IllegalArgumentException("the model class should be subclass of AppScopeViewModel");
        }
        return viewModelProvider.get(modelClass);
    }

//    @Override
//    public void onReceiveCall(AVEngineKit.CallSession session) {
//        List<String> participants = session.getParticipantIds();
//        if (participants == null || participants.isEmpty()) {
//            return;
//        }
//
//        boolean speakerOff = session.getConversation().type == Conversation.ConversationType.Single && session.isAudioOnly();
//        AudioManager audioManager = (AudioManager) application.getSystemService(Context.AUDIO_SERVICE);
//        audioManager.setMode(speakerOff ? AudioManager.MODE_IN_COMMUNICATION : AudioManager.MODE_NORMAL);
//        audioManager.setSpeakerphoneOn(!speakerOff);
//
//        Conversation conversation = session.getConversation();
//        if (conversation.type == Conversation.ConversationType.Single) {
//            Intent intent = new Intent(WfcIntent.ACTION_VOIP_SINGLE);
//            startActivity(application, intent);
//        } else {
//            Intent intent = new Intent(WfcIntent.ACTION_VOIP_MULTI);
//            startActivity(application, intent);
//        }
//        VoipCallService.start(application, false);
//    }
//
//    private AsyncPlayer ringPlayer;
//
//    @Override
//    public void shouldStartRing(boolean isIncoming) {
//        if (isIncoming) {
//            Uri uri = Uri.parse("android.resource://" + application.getPackageName() + "/" + R.raw.incoming_call_ring);
//            ringPlayer.play(application, uri, true, AudioManager.STREAM_RING);
//        } else {
//            Uri uri = Uri.parse("android.resource://" + application.getPackageName() + "/" + R.raw.outgoing_call_ring);
//            ringPlayer.play(application, uri, true, AudioManager.STREAM_RING);
//        }
//    }
//
//    @Override
//    public void shouldSopRing() {
//        Log.d("wfcUIKit", "showStopRing");
//        ringPlayer.stop();
//    }

    // pls refer to https://stackoverflow.com/questions/11124119/android-starting-new-activity-from-application-class
    public static void singleCall(Context context, String targetId, boolean isAudioOnly) {
        Conversation conversation = new Conversation(Conversation.ConversationType.Single, targetId);
//        AVEngineKit.Instance().startCall(conversation, Collections.singletonList(targetId), isAudioOnly, null);

//        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//        audioManager.setMode(isAudioOnly ? AudioManager.MODE_IN_COMMUNICATION : AudioManager.MODE_NORMAL);
//        audioManager.setSpeakerphoneOn(!isAudioOnly);

        Intent voip = new Intent(context, SingleCallActivity.class);
        voip.putExtra("isAudioOnly", isAudioOnly);
        voip.putExtra("conversation", conversation);
        voip.putExtra("status", "launch");
        startActivity(context, voip);
//
//        VoipCallService.start(context, false);
    }

    public static void multipleCall(Context context, String groupId, List<String> participants, boolean isAudioOnly) {
        Conversation conversation = new Conversation(Conversation.ConversationType.Group, groupId);

        Intent voip = new Intent(context, MultipleCallActivity.class);
        voip.putExtra("isAudioOnly", isAudioOnly);
        voip.putExtra("conversation", conversation);
        voip.putExtra("status", "launch");
        voip.putStringArrayListExtra("users", new ArrayList<>(participants));
        startActivity(context, voip);
    }

    public static void startActivity(Context context, Intent intent) {
        if (context instanceof Activity) {
            context.startActivity(intent);
            ((Activity) context).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
            String MainDapp = (String) SPUtils.get(context,"MainDapp","");
            Intent main = null;
            if(MainDapp.length() == 0){
                main  = new Intent(context.getPackageName() + ".main1");
            }else{
                main  = new Intent(context.getPackageName() + ".main");
            }
            //    Intent main = new Intent(context.getPackageName() + ".main");
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent pendingIntent;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                pendingIntent = PendingIntent.getActivities(context, 100, new Intent[]{main, intent}, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            } else {
                pendingIntent = PendingIntent.getActivities(context, 100, new Intent[]{main, intent}, PendingIntent.FLAG_ONE_SHOT);
            }

          //  PendingIntent pendingIntent = PendingIntent.getActivities(context, 100, new Intent[]{main, intent}, PendingIntent.FLAG_UPDATE_CURRENT);
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    void checkCallMessage(Message message){
        if(message.content.getMessageContentType() == MessageContentType.ContentType_Call){
            CallMessageContent callMessageContent = (CallMessageContent) message.content;
            if(callMessageContent.action == CallMessageContent.actionInvite){
                if(callMessageContent.mode == CallMessageContent.modeSingle) {
                    Intent voip = new Intent(application, SingleCallActivity.class);
                    voip.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    voip.putExtra("conversation", message.conversation);
                    voip.putExtra("isAudioOnly", callMessageContent.type == CallMessageContent.typeAudio);
                    voip.putExtra("status", "invite");
                    voip.putExtra("call", callMessageContent);
                    application.startActivity(voip);
                }else if(callMessageContent.mode == CallMessageContent.modeMultiple){
                    Intent voip = new Intent(application, MultipleCallActivity.class);
                    voip.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    voip.putExtra("conversation", message.conversation);
                    voip.putExtra("isAudioOnly", callMessageContent.type == CallMessageContent.typeAudio);
                    voip.putExtra("status", "invite");
                    voip.putExtra("call", callMessageContent);
                    voip.putStringArrayListExtra("users", (ArrayList<String>) callMessageContent.users);
                    application.startActivity(voip);
                }
            }
        }
    }

    @Override
    public void onReceiveMessage(List<Message> messages, boolean hasMore) {
        for(Message message : messages)
            checkCallMessage(message);
        long now = System.currentTimeMillis();
        long delta = ChatManager.Instance().getServerDeltaTime();
        if (messages != null) {
            for (Message msg : messages) {
                if (msg.content instanceof PCLoginRequestMessageContent && (now - (msg.serverTime - delta)) < 60 * 1000) {
                    PCLoginRequestMessageContent content = ((PCLoginRequestMessageContent) msg.content);
                    appServiceProvider.showPCLoginActivity(ChatManager.Instance().getUserId(), content.getSessionId(), content.getPlatform());
                    break;
                }
            }
        }

        if (isBackground && !ChatManager.Instance().isMuteNotificationWhenPcOnline()) {
            if (messages == null) {
                return;
            }

            List<Message> msgs = new ArrayList<>(messages);
            Iterator<Message> iterator = msgs.iterator();
            while (iterator.hasNext()) {
                Message message = iterator.next();
                if (message.content.getPersistFlag() == PersistFlag.No_Persist
                        || now - (message.serverTime - delta) > 10 * 1000) {
                    iterator.remove();
                }
            }
            WfcNotificationManager.getInstance().handleReceiveMessage(application, msgs);
        } else {
            // do nothing
        }


    }

    @Override
    public void onRecallMessage(Message message) {
        if (isBackground) {
            WfcNotificationManager.getInstance().handleRecallMessage(application, message);
        }
    }

    @Override
    public void onFriendListUpdate(List<String> updateFriendList) {
        // do nothing

    }

    @Override
    public void onFriendRequestUpdate(List<String> newRequests) {
        if (isBackground) {
            if (newRequests == null || newRequests.isEmpty()) {
                return;
            }
            WfcNotificationManager.getInstance().handleFriendRequest(application, newRequests);
        }
    }

    public AppServiceProvider getAppServiceProvider() {
        return this.appServiceProvider;
    }


    public void setAppServiceProvider(AppServiceProvider appServiceProvider) {
        this.appServiceProvider = appServiceProvider;
    }
    public void setActivity(Activity activity){
        gActivity = activity;
    }
    public static Activity getActivity(){
        return getWfcUIKit().gActivity;
    }
    public static String getString(int id){
        return getWfcUIKit().gActivity.getString(id);
    }
}
