package cn.wildfire.chat.kit.voip;

import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

public class SdpAdapter implements SdpObserver {
    public interface Callback{
        void onCreateSuccess(SessionDescription sessionDescription);
    }
    Callback callback;
    SdpAdapter(Callback callback){
        this.callback = callback;
    }
    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        if(callback != null)
            callback.onCreateSuccess(sessionDescription);
    }

    @Override
    public void onSetSuccess() {

    }

    @Override
    public void onCreateFailure(String s) {

    }

    @Override
    public void onSetFailure(String s) {

    }
}
