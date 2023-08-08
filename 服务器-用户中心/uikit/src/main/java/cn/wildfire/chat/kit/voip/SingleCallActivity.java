package cn.wildfire.chat.kit.voip;

import static cn.wildfirechat.message.CallMessageContent.typeAudio;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import org.webrtc.SurfaceViewRenderer;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.conversation.message.model.UiMessage;
import cn.wildfire.chat.kit.third.utils.UIUtils;
import cn.wildfire.chat.kit.utils.SPUtils;
import cn.wildfire.chat.kit.viewmodel.MessageViewModel;
import cn.wildfirechat.message.CallMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.MessageContent;
import cn.wildfirechat.message.core.MessageContentType;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;

public class SingleCallActivity extends WfcBaseActivity {
    @BindView(R2.id.bigView)
    SurfaceViewRenderer bigView;
    @BindView(R2.id.litView)
    SurfaceViewRenderer litView;
    @BindView(R2.id.acceptImage)
    ImageView acceptImage;
    @BindView(R2.id.rejectImage)
    ImageView rejectImage;
    @BindView(R2.id.target)
    ImageView targetImage;
    @BindView(R2.id.chr_timer)
    Chronometer chr_timer;
    @BindView(R2.id.iv_audio_true)
    ImageView iv_audio_true;
    @BindView(R2.id.iv_audio_false)
    ImageView iv_audio_false;

    @BindView(R2.id.iv_yangshengqi)
    ImageView iv_yangshengqi;
    @BindView(R2.id.iv_tingtong)
    ImageView iv_tingtong;

    @BindView(R2.id.tv_name)
    TextView tv_name;

    @BindView(R2.id.ll_shiping)
    LinearLayout ll_shiping;
    @BindView(R2.id.iv_touxiang)
    ImageView iv_touxiang;
    @BindView(R2.id.tv_names)
    TextView tv_names;
    @BindView(R2.id.chr_timers)
    Chronometer chr_timers;
    @BindView(R2.id.iv_suoxiao)
    ImageView iv_suoxiao;

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
    private String voiceBaseUrl,voiceHostUrl;
    boolean direct = true;

    protected int contentLayout() {
        return R.layout.activity_single_call;
    }
    @Override
    protected void afterViews() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        messageViewModel = ViewModelProviders.of(this).get(MessageViewModel.class);
        messageViewModel.messageLiveData().observe(this, messageObserver);
        messageViewModel.setCallStatue(true);
   //     callViewModel = new CallViewModel(this, bigView, litView);


        callViewModel = new CallViewModel(this);
        callViewModel.initSurfaceView(bigView);
        callViewModel.initSurfaceView(litView);
        String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkPermission(permissions)) {
                requestPermissions(permissions, 100);
             //   Toast.makeText(SingleCallActivity.this,"请打开相机和录音权限",Toast.LENGTH_SHORT).show();
            //    finish();
            //    return;
            }
        }

        callViewModel.checkPermission(this);
        userInfo = ChatManager.Instance().getUserInfo(null, false);
        conversation = getIntent().getParcelableExtra("conversation");
        isAudioOnly = getIntent().getBooleanExtra("isAudioOnly", false);
        receiveCall = getIntent().getParcelableExtra("call");

        voiceHostUrl = null;
        voiceBaseUrl = null;

        if (receiveCall != null) {
            if (receiveCall.voiceBaseUrl != null){
                voiceHostUrl = receiveCall.voiceHostUrl;
                voiceBaseUrl = receiveCall.voiceBaseUrl;
            }
        }

        if (voiceHostUrl == null) {
            voiceHostUrl = ChatManager.Instance().getVoiceHostUrl();
            voiceBaseUrl = ChatManager.Instance().getVoiceBaseUrl();
        }


        /*else {
            if (receiveCall.voiceBaseUrl == null) {
                voiceHostUrl = ChatManager.Instance().getVoiceHostUrl();
                voiceBaseUrl = ChatManager.Instance().getVoiceBaseUrl();
            } else {

            }
        }*/


        /*if(receiveCall.voiceHostUrl.equals("")){
            voiceHostUrl = ChatManager.Instance().getVoiceBaseUrl();
            voiceBaseUrl = ChatManager.Instance().getVoiceHostUrl();
        } else {

        }*/

        status = getIntent().getStringExtra("status");
        if(status.equalsIgnoreCase("launch")){

            acceptImage.setVisibility(View.GONE);
            callViewModel.launchCall(bigView, isAudioOnly);
            if(!callViewModel.pushCall(userInfo.uid,voiceBaseUrl,voiceHostUrl))
                Toast.makeText(this, "push call failed", Toast.LENGTH_SHORT).show();
            sendCallMessage(CallMessageContent.actionInvite);
        }else{
            isLaunch = false;
            callViewModel.launchCall(bigView, isAudioOnly);
        }
        setTargetPortrait();
        startTimer();
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ringtone = RingtoneManager.getRingtone(this, uri);
        ringtone.play();
        duration = System.currentTimeMillis();

    }
//    @Override
//    protected void onPause() {
//        super.onPause();
//        if(isFinishing()){
//            callViewModel.clear();
//            messageViewModel.messageLiveData().removeObserver(messageObserver);
//            messageViewModel.setCallStatue(false);
//            ringtone.stop();
//            callFinish();
//        }
//    }
    @OnClick(R2.id.iv_audio_true)
    void muteImageTrue(){
        iv_audio_true.setVisibility(View.GONE);
        iv_audio_false.setVisibility(View.VISIBLE);
        callViewModel.setSpeaker(false);

    }
    @OnClick(R2.id.iv_audio_false)
    void setMuteImageFalse(){
        iv_audio_true.setVisibility(View.VISIBLE);
        iv_audio_false.setVisibility(View.GONE);
        callViewModel.setSpeaker(true);
    }
    @OnClick(R2.id.iv_yangshengqi)
    void setIv_yangshengqi(){
        iv_yangshengqi.setVisibility(View.GONE);
        iv_tingtong.setVisibility(View.VISIBLE);
        callViewModel.setAudio(false);
    }
    @OnClick(R2.id.iv_tingtong)
    void setIv_tingtong(){
        iv_tingtong.setVisibility(View.GONE);
        iv_yangshengqi.setVisibility(View.VISIBLE);
        callViewModel.setAudio(true);
    }
    @OnClick(R2.id.iv_suoxiao)
    void setIv_suoxiao(){
        moveTaskToBack(true);//最小化Activity
        Intent intent = new Intent(this, FloatVideoWindowService.class);//开启服务显示悬浮框

    }
    ServiceConnection mVideoServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 获取服务的操作对象
            FloatVideoWindowService.MyBinder binder = (FloatVideoWindowService.MyBinder) service;
            binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    protected void onRestart() {
        super.onRestart();
   //     unbindService(mVideoServiceConnection);//不显示悬浮框
    }

    @OnClick(R2.id.acceptImage)
    void acceptCall(){
        ringtone.stop();
        iv_audio_true.setVisibility(View.VISIBLE);
        iv_yangshengqi.setVisibility(View.VISIBLE);
        acceptImage.setVisibility(View.GONE);
   //     UserInfo userInfo = ChatManager.Instance().getUserInfo(conversation.target, false);
        UserInfo userInfo0 = ChatManager.Instance().getUserInfo(conversation.target, false);
        if(isAudioOnly){
            targetImage.setVisibility(View.VISIBLE);
            chr_timer.setVisibility(View.VISIBLE);
            chr_timer.setBase(SystemClock.elapsedRealtime());
            int hour = (int) ((SystemClock.elapsedRealtime() - chr_timer.getBase()) / 1000 / 60);
            chr_timer.setFormat("0"+String.valueOf(hour)+":%s");
            chr_timer.start();

            tv_name.setVisibility(View.VISIBLE);
            tv_name.setText(userInfo0.displayName);
            ll_shiping.setVisibility(View.GONE);
        }else{
            targetImage.setVisibility(View.GONE);
            chr_timer.setVisibility(View.GONE);
            ll_shiping.setVisibility(View.VISIBLE);
            chr_timers.setBase(SystemClock.elapsedRealtime());
            int hour = (int) ((SystemClock.elapsedRealtime() - chr_timers.getBase()) / 1000 / 60);
            chr_timers.setFormat("0"+String.valueOf(hour)+":%s");
            chr_timers.start();
            tv_names.setText(userInfo0.displayName);
            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.mipmap.avatar_def)
                    .transforms(new CenterCrop(), new RoundedCorners(UIUtils.dip2Px(this, 10)));
            Glide.with(this)
                    .load(userInfo0.portrait)
                    .apply(requestOptions)
                    .into(iv_touxiang);

        }




        status = "calling";
//        System.out.println("acceptCall url: "+receiveCall.url);
        if(!callViewModel.pushCall(userInfo.uid,voiceBaseUrl,voiceHostUrl))
            Toast.makeText(this, "push call failed", Toast.LENGTH_SHORT).show();
        if(!callViewModel.pullCall(litView, receiveCall.url,voiceHostUrl))
            Toast.makeText(this, "pull call failed", Toast.LENGTH_SHORT).show();
        sendCallMessage(CallMessageContent.actionAnswer);
    }
    @OnClick(R2.id.rejectImage)
    void rejectCall(){
        if(status.equalsIgnoreCase("invite")){
            status = "refuse";
            sendCallMessage(CallMessageContent.actionReject);
        }else if(status.equalsIgnoreCase("launch")){
            status = "cancel";
            sendCallMessage(CallMessageContent.actionCancel);
        }else{
            status = "finish";
            sendCallMessage(CallMessageContent.actionFinish);
        }
        finishCall();
    }
    void finishCall(){
        chr_timer.stop();
        callViewModel.clear();
        messageViewModel.messageLiveData().removeObserver(messageObserver);
        messageViewModel.setCallStatue(false);
        ringtone.stop();
        callFinish();
        bigView.release();
        litView.release();
        finish();
    }
    final Observer<UiMessage> messageObserver = uiMessage -> {
        MessageContent content = uiMessage.message.content;
        if(content.getMessageContentType() == MessageContentType.ContentType_Call &&
            uiMessage.message.direction == MessageDirection.Receive){
            CallMessageContent callMessageContent = (CallMessageContent) uiMessage.message.content;
            /*if(callMessageContent.action == CallMessageContent.actionReject ||
                callMessageContent.action == CallMessageContent.actionFinish){
                finishCall();
            }else */
            if(callMessageContent.action == CallMessageContent.actionAnswer){
                targetImage.setVisibility(View.GONE);
                iv_audio_true.setVisibility(View.VISIBLE);
                iv_yangshengqi.setVisibility(View.VISIBLE);
                UserInfo userInfo = ChatManager.Instance().getUserInfo(conversation.target, false);
                if(isAudioOnly){
                    targetImage.setVisibility(View.VISIBLE);
                    chr_timer.setVisibility(View.VISIBLE);
                    chr_timer.setBase(SystemClock.elapsedRealtime());
                    int hour = (int) ((SystemClock.elapsedRealtime() - chr_timer.getBase()) / 1000 / 60);
                    chr_timer.setFormat("0"+String.valueOf(hour)+":%s");
                    chr_timer.start();

                    tv_name.setText(userInfo.displayName);
                    tv_name.setVisibility(View.VISIBLE);
                    ll_shiping.setVisibility(View.GONE);
                }else {
                    ll_shiping.setVisibility(View.VISIBLE);
                    chr_timers.setBase(SystemClock.elapsedRealtime());
                    int hour = (int) ((SystemClock.elapsedRealtime() - chr_timers.getBase()) / 1000 / 60);
                    chr_timers.setFormat("0"+String.valueOf(hour)+":%s");
                    chr_timers.start();
                    tv_names.setText(userInfo.displayName);
                    RequestOptions requestOptions = new RequestOptions()
                            .placeholder(R.mipmap.avatar_def)
                            .transforms(new CenterCrop(), new RoundedCorners(UIUtils.dip2Px(this, 10)));
                    Glide.with(this)
                            .load(userInfo.portrait)
                            .apply(requestOptions)
                            .into(iv_touxiang);

                }
                if(callViewModel.pullCall(litView, callMessageContent.url,voiceHostUrl)){
                    ringtone.stop();
                    status = "calling";
                }
            }else{
                if(callMessageContent.action == CallMessageContent.actionReject){
                    status = "reject";
                }else if(callMessageContent.action == CallMessageContent.actionFinish){
                    status = "finish";
                }else if(callMessageContent.action == CallMessageContent.actionCancel){
                    status = "reject";
                }
                finishCall();
            }
        }
    };
    void setTargetPortrait(){
        UserInfo userInfo = ChatManager.Instance().getUserInfo(conversation.target, false);
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.mipmap.avatar_def)
                .transforms(new CenterCrop(), new RoundedCorners(UIUtils.dip2Px(this, 10)));
        Glide.with(this)
                .load(userInfo.portrait)
                .apply(requestOptions)
                .into(targetImage);
    }
    CallMessageContent getMessage(int action){
        CallMessageContent content = new CallMessageContent();
      //  content.type = isAudioOnly ? CallMessageContent.typeAudio : CallMessageContent.typeVideo;
        content.type = isAudioOnly ? typeAudio : CallMessageContent.typeVideo;
        content.mode = CallMessageContent.modeSingle;
        content.action = action;
        content.url = callViewModel.getUrl(userInfo.uid,voiceBaseUrl);
        content.user = userInfo.uid;
        return content;
    }
    void sendCallMessage(int action){
        messageViewModel.sendCallMessage(conversation.target, getMessage(action));
    }

    void callFinish(){
        /*CallMessageContent call = receiveCall;
        if(call == null)
            call = getMessage(CallMessageContent.actionInvite);
        if(status.equalsIgnoreCase("calling"))
            call.duration = (int) (System.currentTimeMillis()-duration);
        messageViewModel.saveCallMessage(conversation, call, isLaunch);*/
        CallMessageContent call = getMessage(CallMessageContent.actionFinish);
        call.duration = (int) (System.currentTimeMillis() - duration);
        call.status = status;
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

    //调用此方法传入true缩小当前activity
    @Override
    public boolean moveTaskToBack(boolean nonRoot) {
        return super.moveTaskToBack(nonRoot);
    }
}