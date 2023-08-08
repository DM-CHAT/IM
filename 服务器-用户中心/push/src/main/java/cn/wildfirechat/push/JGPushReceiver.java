package cn.wildfirechat.push;

import android.content.Context;
import android.util.Log;

import cn.jpush.android.api.JPushMessage;
import cn.jpush.android.service.JPushMessageReceiver;

public class JGPushReceiver extends JPushMessageReceiver {
    String TAG = JGPushReceiver.class.getSimpleName();
    public void onAliasOperatorResult(Context var1, JPushMessage var2) {
        Log.d(TAG, "onAliasOperatorResult: "+var2.toString());
    }
}
