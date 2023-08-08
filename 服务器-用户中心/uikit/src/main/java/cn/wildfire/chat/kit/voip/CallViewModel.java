package cn.wildfire.chat.kit.voip;

import static java.lang.Math.max;
import static java.lang.Math.min;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactoryExtKt;
import org.webrtc.EglBase;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpTransceiver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.wildfire.chat.kit.BuildConfig;
import cn.wildfire.chat.kit.net.Callback;
import cn.wildfire.chat.kit.net.OKHttpHelper;
import cn.wildfire.chat.kit.utils.SPUtils;

public class CallViewModel {
    String TAG = "CallViewModel";
    Context context;
    EglBase.Context eglBaseContext;
    PeerConnectionFactory peerConnectionFactory;
    CameraVideoCapturer cameraVideoCapturer = null;
    PeerConnection pushConnection = null;
    MediaStream mediaStream;
    Map<String,CallStream> peers = new HashMap<>();
    boolean isAudioOnly = false;


    static class CallStream {
        MediaStream stream;
        PeerConnection peer;
        SurfaceViewRenderer view;
    }

    public CallViewModel(Context context1){
        //Logging.enableLogToDebugOutput(Logging.Severity.LS_VERBOSE);
        this.context = context1;
        eglBaseContext = EglBase.create().getEglBaseContext();
        VideoEncoderFactory encoderFactory = DefaultVideoEncoderFactoryExtKt
                .createCustomVideoEncoderFactory(eglBaseContext, true, true, info -> true);
        DefaultVideoDecoderFactory decoderFactory = new DefaultVideoDecoderFactory(eglBaseContext);
        peerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(new PeerConnectionFactory.Options())
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();
        mediaStream = peerConnectionFactory.createLocalMediaStream("ARDAMS");
    }
    public void initSurfaceView(SurfaceViewRenderer view){
        view.init(eglBaseContext, null);
        view.setEnableHardwareScaler(true);
    }
    public void checkPermission(Activity activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {
                    Manifest.permission.CAMERA,
                    Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO
            };
            boolean needRequest = false;
            for(String p : permissions){
                if(activity.checkSelfPermission(p) != PackageManager.PERMISSION_GRANTED){
                    needRequest = true;
                    break;
                }
            }
            if(needRequest){
                ActivityCompat.requestPermissions(activity,permissions,1);
            }
        }
    }
    public void launchCall(SurfaceViewRenderer view, boolean isAudioOnly){
        this.isAudioOnly = isAudioOnly;
        AudioSource createAudioSource = peerConnectionFactory.createAudioSource(createAudioConstraints());
        AudioTrack audioTrack = peerConnectionFactory.createAudioTrack("ARDAMSa0", createAudioSource);
        mediaStream.addTrack(audioTrack);
        if(!isAudioOnly){
            cameraVideoCapturer = createVideoCapture(context);
            VideoSource videoSource = peerConnectionFactory.createVideoSource(cameraVideoCapturer.isScreencast());
            SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("captureThread", eglBaseContext);
            cameraVideoCapturer.initialize(surfaceTextureHelper, context, videoSource.getCapturerObserver());
            cameraVideoCapturer.startCapture(720, 1280, 5);
            VideoTrack videoTrack = peerConnectionFactory.createVideoTrack("ARDAMSv0", videoSource);
            videoTrack.addSink(view);
            mediaStream.addTrack(videoTrack);
        }
    }
    public boolean pushCall(String target,String voiceBaseUrl,String voiceHostUrl){
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(new ArrayList<>());
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;
        pushConnection = peerConnectionFactory.createPeerConnection(rtcConfig, new PeerAdapter(null));
        if(pushConnection == null){
            Log.d(TAG, "createPeerConnection push call failed");
            return false;
        }
        if(!isAudioOnly)
            pushConnection.addTransceiver(mediaStream.videoTracks.get(0),
                    new RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.SEND_ONLY));
        pushConnection.addTransceiver(mediaStream.audioTracks.get(0),
                new RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.SEND_ONLY));
        pushConnection.createOffer(new SdpAdapter(sessionDescription -> {
            if (sessionDescription.type == SessionDescription.Type.OFFER) {
                String offerSdp = sessionDescription.description;
                pushConnection.setLocalDescription(new SdpAdapter(null), sessionDescription);
                createSession(offerSdp, getUrl(target,voiceBaseUrl), pushConnection, true,voiceHostUrl);
            }
        }),new MediaConstraints());
        return true;
    }
    public boolean pullCall(SurfaceViewRenderer view, String url,String voiceHostUrl){
        if(peers.containsKey(url)){
            Log.d(TAG, "already pull url: "+url);
            return true;
        }
        Log.d(TAG, "receiveCall: "+url);
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(new ArrayList<>());
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;
        PeerConnection peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, new PeerAdapter(mediaStream->{
            if(!mediaStream.videoTracks.isEmpty())
                mediaStream.videoTracks.get(0).addSink(view);
            addStream(url, mediaStream);
        }));
        if(peerConnection == null){
            Log.d(TAG, "createPeerConnection pull call failed");
            return false;
        }
        addPeer(url, peerConnection);
        addView(url, view);
        if(!isAudioOnly)
            peerConnection.addTransceiver(MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO,
                    new RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.RECV_ONLY));
        peerConnection.addTransceiver(MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO,
                new RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.RECV_ONLY));
        peerConnection.createOffer(new SdpAdapter(sessionDescription -> {
            if (sessionDescription.type == SessionDescription.Type.OFFER) {
                String offerSdp = sessionDescription.description;
                peerConnection.setLocalDescription(new SdpAdapter(null), sessionDescription);
                createSession(offerSdp, url, peerConnection, false,voiceHostUrl);
            }
        }), new MediaConstraints());
        setSpeaker(true);
        return true;
    }
    public void exitCall(String url){
        CallStream callStream = peers.remove(url);
        if(callStream == null)
            return;
        if(callStream.stream != null && !callStream.stream.videoTracks.isEmpty())
            callStream.stream.videoTracks.get(0).removeSink(callStream.view);
        if(callStream.peer != null)
            callStream.peer.close();
    }
    public void setSpeaker(boolean isOn){
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if(isOn) {
            audioManager.setSpeakerphoneOn(true);
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        }else{
            audioManager.setSpeakerphoneOn(false);
            audioManager.setMode(AudioManager.MODE_IN_CALL);
        }
    }
    public void setAudio(boolean enable){
        if(mediaStream.audioTracks.isEmpty())
            return;
        AudioTrack audioTrack = mediaStream.audioTracks.get(0);
        audioTrack.setEnabled(enable);
    }
    public void setVideo(boolean enable){
        if(mediaStream.videoTracks.isEmpty())
            return;
        VideoTrack videoTrack = mediaStream.videoTracks.get(0);
        videoTrack.setEnabled(enable);
    }
    public String getUrl(String target,String voiceBaseUrl){
        return voiceBaseUrl+target;
    }
    public void clear() {
        if(cameraVideoCapturer != null){
            try {
                cameraVideoCapturer.stopCapture();
            }catch (Exception ignored){
            }
        }
        if(pushConnection != null)
            pushConnection.close();
        if(peers != null) {
            String[] keys = peers.keySet().toArray(new String[0]);
            for (String url : keys)
                exitCall(url);
        }
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_NORMAL);
    }
    void createSession(String offerSdp, String url, PeerConnection peerConnection, boolean isPublish,String voiceHostUrl){

        JSONObject request = new JSONObject();
        request.put("sdp", offerSdp);
        request.put("streamurl", url);
        Log.d(TAG, "request: "+request);
        OKHttpHelper.postJson(voiceHostUrl + "/rtc/v1/" + (isPublish ? "publish/" : "play/"),
                request.toString(), new Callback<String>() {
                    @Override
                    public void onSuccess(String s) {
                        Log.d(TAG, "result: " + s);
                        JSONObject json = JSON.parseObject(s);
                        if (json.getIntValue("code") != 0)
                            return;
                        SessionDescription remoteSdp = new SessionDescription(
                                SessionDescription.Type.ANSWER,
                                convertAnswerSdp(offerSdp, json.getString("sdp")));
                        peerConnection.setRemoteDescription(
                                new SdpAdapter(null),
                                remoteSdp);
                    }

                    @Override
                    public void onFailure(int code, String message) {
                        Log.d(TAG, "code: "+code+", message: "+message);
                    }

                    @Override
                    public void onSuccess1(String t) {

                    }
                });
    }
    String convertAnswerSdp(String offerSdp, String answerSdp) {
        if(answerSdp == null || answerSdp.isEmpty())
            return answerSdp;
        int indexOfOfferVideo = offerSdp.indexOf("m=video");
        int indexOfOfferAudio = offerSdp.indexOf("m=audio");
        if (indexOfOfferVideo == -1 || indexOfOfferAudio == -1) {
            return answerSdp;
        }
        int indexOfAnswerVideo = answerSdp.indexOf("m=video");
        int indexOfAnswerAudio = answerSdp.indexOf("m=audio");
        if (indexOfAnswerVideo == -1 || indexOfAnswerAudio == -1) {
            return answerSdp;
        }
        boolean isFirstOfferVideo = indexOfOfferVideo < indexOfOfferAudio;
        boolean isFirstAnswerVideo = indexOfAnswerVideo < indexOfAnswerAudio;
        if(isFirstOfferVideo == isFirstAnswerVideo)
            return answerSdp;
        return answerSdp.substring(0, min(indexOfAnswerAudio, indexOfAnswerVideo)) +
                answerSdp.substring(max(indexOfAnswerAudio, indexOfAnswerVideo)) +
                answerSdp.substring(min(indexOfAnswerAudio, indexOfAnswerVideo), max(indexOfAnswerAudio, indexOfAnswerVideo));
    }
    MediaConstraints createAudioConstraints(){
        MediaConstraints audioConstraints = new MediaConstraints();
        //回声消除
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googEchoCancellation", "true"));
        //自动增益
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googAutoGainControl", "true"));
        //高音过滤
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googHighpassFilter", "true"));
        //噪音处理
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googNoiseSuppression", "true"));
        return audioConstraints;
    }
    CameraVideoCapturer createVideoCapture(Context context) {
        CameraEnumerator enumerator;
        if(Camera2Enumerator.isSupported(context)){
            enumerator = new Camera2Enumerator(context);
        } else {
            enumerator = new Camera1Enumerator();
        }
        for (String name : enumerator.getDeviceNames()) {
            if (enumerator.isFrontFacing(name)) {
                return enumerator.createCapturer(name, null);
            }
        }
        for (String name : enumerator.getDeviceNames()) {
            if (enumerator.isBackFacing(name)) {
                return enumerator.createCapturer(name, null);
            }
        }
        return null;
    }
    void addView(String url, SurfaceViewRenderer view){
        CallStream callStream = peers.computeIfAbsent(url, k -> new CallStream());
        callStream.view = view;
    }
    void addPeer(String url, PeerConnection peer){
        CallStream callStream = peers.computeIfAbsent(url, k -> new CallStream());
        callStream.peer = peer;
    }
    void addStream(String url, MediaStream stream){
        CallStream callStream = peers.computeIfAbsent(url, k -> new CallStream());
        callStream.stream = stream;
    }
}
