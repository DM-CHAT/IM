package cn.wildfire.chat.kit.voip;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.conversation.message.model.UiMessage;
import cn.wildfire.chat.kit.utils.SPUtils;
import cn.wildfire.chat.kit.viewmodel.MessageViewModel;
import cn.wildfirechat.message.CallMessageContent;
import cn.wildfirechat.message.MessageContent;
import cn.wildfirechat.message.core.MessageContentType;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;

public class MultipleCallActivity extends WfcBaseActivity {
    @BindView(R2.id.view0)
    SurfaceViewRenderer userView0;
    @BindView(R2.id.view1)
    SurfaceViewRenderer userView1;
    @BindView(R2.id.view2)
    SurfaceViewRenderer userView2;
    @BindView(R2.id.acceptImage)
    ImageView acceptImage;
    @BindView(R2.id.rejectImage)
    ImageView rejectImage;

    String TAG = "MultipleCallActivity";
    int callId = (int)System.currentTimeMillis();
    Ringtone ringtone = null;
    CallViewModel callViewModel = null;
    MessageViewModel messageViewModel = null;
    CallMessageContent receiveCall = null;
    Conversation conversation = null;
    boolean isAudioOnly = false;
    Timer timer = null;
    boolean uiShow = true;
    long duration = -1;
    UserInfo userInfo = null;
    boolean isLaunch = true;
    String status;

    Map<String,StreamInfo> streams = new HashMap<>();
    List<SurfaceViewRenderer> views = new ArrayList<>();
    List<String> users;
    private String voiceBaseUrl,voiceHostUrl;

    public static class StreamInfo{
        public String url;
        public SurfaceViewRenderer view;
    }

    @Override
    protected int contentLayout() {
        return R.layout.activity_multiple_call;
    }
    @Override
    protected void afterViews() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        messageViewModel = ViewModelProviders.of(this).get(MessageViewModel.class);
        messageViewModel.messageLiveData().observe(this, messageObserver);
        messageViewModel.setCallStatue(true);
        voiceBaseUrl = (String) SPUtils.get(this,"voiceBaseUrl",null);
        voiceHostUrl = (String) SPUtils.get(this,"voiceHostUrl",null);

        callViewModel = new CallViewModel(this);
        callViewModel.initSurfaceView(userView0);
        callViewModel.initSurfaceView(userView1);
        callViewModel.initSurfaceView(userView2);
        callViewModel.checkPermission(this);
        views.add(userView0);
        views.add(userView1);
        views.add(userView2);
        userInfo = ChatManager.Instance().getUserInfo(null, false);
        conversation = getIntent().getParcelableExtra("conversation");
        isAudioOnly = getIntent().getBooleanExtra("isAudioOnly", false);
        receiveCall = getIntent().getParcelableExtra("call");
        users = getIntent().getStringArrayListExtra("users");
        status = getIntent().getStringExtra("status");
        if(status.equalsIgnoreCase("launch")){
            acceptImage.setVisibility(View.GONE);
            callViewModel.launchCall(getStreamView(userInfo.uid, null), isAudioOnly);
            if(!callViewModel.pushCall(userInfo.uid,voiceHostUrl,voiceBaseUrl))
                Log.d(TAG, "push call failed");
            userInitCall();
        }else{
            isLaunch = false;
            callViewModel.launchCall(getStreamView(userInfo.uid, null), isAudioOnly);
        }
        layoutViews();
        startTimer();
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ringtone = RingtoneManager.getRingtone(this, uri);
        ringtone.play();
        duration = System.currentTimeMillis();
    }
    @OnClick(R2.id.acceptImage)
    void acceptCall(){
        ringtone.stop();
        acceptImage.setVisibility(View.GONE);
        status = "calling";
        if(!callViewModel.pushCall(userInfo.uid,voiceHostUrl,voiceBaseUrl))
            Log.d(TAG,  "push call failed");
        if(!callViewModel.pullCall(getStreamView(receiveCall.user, receiveCall.url), receiveCall.url,voiceBaseUrl))
            Log.d(TAG, "pull accept failed: "+receiveCall.url);
        sendCallMessage(receiveCall.user, CallMessageContent.actionAnswer);
    }
    @OnClick(R2.id.rejectImage)
    void rejectCall(){
        if(status.equalsIgnoreCase("invite")){
            sendCallMessage(receiveCall.user, CallMessageContent.actionReject);
        }else{
            for(String user : streams.keySet())
                sendCallMessage(user, CallMessageContent.actionFinish);
        }
        finishCall();
    }
    void layoutViews(){
        switch(views.size()){
            case 3:
                break;
            case 4:
                break;
            case 5:
                break;
        }
    }
    void finishCall(){
        callViewModel.clear();
        messageViewModel.messageLiveData().removeObserver(messageObserver);
        messageViewModel.setCallStatue(false);
        ringtone.stop();
        callFinish();
        finish();
    }
    void userInitCall(){
        CallMessageContent call = getMessage(CallMessageContent.actionInvite);
        call.users = users;
        for(String user : users)
            messageViewModel.sendCallMessage(user, call);
    }
    void userExitCall(CallMessageContent call){
        StreamInfo info = streams.remove(call.user);
        if(info == null)
            return;
        callViewModel.exitCall(info.url);
        layoutViews();
        if(streams.size() < 2)
            finishCall();
    }
    void userJoinCall(CallMessageContent call){
        SurfaceViewRenderer view = getStreamView(call.user, call.url);
        if(!callViewModel.pullCall(view, call.url,voiceBaseUrl)) {
            Log.d(TAG, "pull join failed: " + call.url);
            return;
        }
        forwardCallMessage(call, CallMessageContent.actionStream);
        deliveryCallMessage(call.user);
    }
    void userStreamCall(CallMessageContent call){
        if(call.action == CallMessageContent.actionStream) {
            SurfaceViewRenderer view = getStreamView(call.user, call.url);
            if (!callViewModel.pullCall(view, call.url,voiceBaseUrl))
                Log.d(TAG, "pull stream failed: " + call.url);
        }else{
            for(int i = 0; i < call.users.size(); ++i){
                if(call.users.get(i).equalsIgnoreCase(userInfo.uid))
                    continue;
                SurfaceViewRenderer view = getStreamView(call.users.get(i), call.urls.get(i));
                if (!callViewModel.pullCall(view, call.urls.get(i),voiceBaseUrl))
                    Log.d(TAG, "pull stream failed: " + call.urls.get(i));
            }
        }
    }
    String getStreamUrl(String user){
        StreamInfo info = streams.get(user);
        if(info == null)
            return null;
        return info.url;
    }
    SurfaceViewRenderer getStreamView(String user, String url){
        if(url == null)
            url = callViewModel.getUrl(userInfo.uid,voiceHostUrl);
        String finalUrl = url;
        return streams.computeIfAbsent(user, k -> {
            StreamInfo info = new StreamInfo();
            info.url = finalUrl;
            info.view = views.remove(0);
            return info;
        }).view;
    }
    CallMessageContent getMessage(int action){
        CallMessageContent content = new CallMessageContent();
        content.id = receiveCall == null ? callId : receiveCall.id;
        content.type = isAudioOnly ? CallMessageContent.typeAudio : CallMessageContent.typeVideo;
        content.mode = CallMessageContent.modeMultiple;
        content.action = action;
        content.url = callViewModel.getUrl(userInfo.uid,voiceHostUrl);
        content.user = userInfo.uid;
        return content;
    }
    CallMessageContent getMessages(int action){
        CallMessageContent content = new CallMessageContent();
        content.id = receiveCall == null ? callId : receiveCall.id;
        content.type = isAudioOnly ? CallMessageContent.typeAudio : CallMessageContent.typeVideo;
        content.mode = CallMessageContent.modeMultiple;
        content.action = action;
        content.urls = new ArrayList<>();
        content.users = new ArrayList<>();
        return content;
    }
    void sendCallMessage(String target, int action){
        messageViewModel.sendCallMessage(target, getMessage(action));
    }
    void forwardCallMessage(CallMessageContent call, int action){
        call.action = action;
        for(String user : streams.keySet()){
            if(!user.equalsIgnoreCase(call.user))
                messageViewModel.sendCallMessage(user, call);
        }
    }
    void deliveryCallMessage(String target){
        CallMessageContent call = getMessages(CallMessageContent.actionStreams);
        for(String user : streams.keySet()){
            call.users.add(user);
            call.urls.add(getStreamUrl(user));
        }
        messageViewModel.sendCallMessage(target, call);
    }
    void callFinish(){
        CallMessageContent call = receiveCall;
        if(call == null)
            call = getMessage(CallMessageContent.actionInvite);
        if(status.equalsIgnoreCase("calling"))
            call.duration = (int) (System.currentTimeMillis()-duration);
        messageViewModel.saveCallMessage(conversation, call, isLaunch);
    }
    void startTimer(){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(status.equalsIgnoreCase("calling")){
                    if(uiShow){
                        uiShow = false;
                    }
                }
            }
        }, 3000);
    }
    final Observer<UiMessage> messageObserver = uiMessage -> {
        MessageContent content = uiMessage.message.content;
        if(content.getMessageContentType() == MessageContentType.ContentType_Call &&
                uiMessage.message.direction == MessageDirection.Receive){
            CallMessageContent callMessageContent = (CallMessageContent) uiMessage.message.content;
            if(callMessageContent.action == CallMessageContent.actionReject ||
                    callMessageContent.action == CallMessageContent.actionFinish){
                userExitCall(callMessageContent);
            }else if(callMessageContent.action == CallMessageContent.actionAnswer){
                ringtone.stop();
                status = "calling";
                userJoinCall(callMessageContent);
            }else if(callMessageContent.action == CallMessageContent.actionStream ||
                    callMessageContent.action == CallMessageContent.actionStreams){
                userStreamCall(callMessageContent);
            }
        }
    };
}