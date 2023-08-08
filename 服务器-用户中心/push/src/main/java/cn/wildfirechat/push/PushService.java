package cn.wildfirechat.push;

import android.app.ActivityManager;
import android.content.Context;

import com.xiaomi.mipush.sdk.MiPushClient;

import java.util.List;

import cn.jpush.android.api.JPushInterface;

public class PushService {
    public static void init(Context context, String APP_KEY, String APP_ID){
   //     JPushInterface.setDebugMode(BuildConfig.DEBUG);
   //     JPushInterface.init(context);

        //初始化push推送服务
        MiPushClient.registerPush(context, APP_ID, APP_KEY);
        String regId = MiPushClient.getRegId(context);
    }
    public static void setAlias(Context context, String alias){
        JPushInterface.setAlias(context, 0, alias);
        //MiPushClient.setAlias(context, alias, null);
    }
}
