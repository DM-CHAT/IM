/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Process;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.xiaomi.mipush.sdk.MiPushClient;

import java.util.List;

import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.conversation.message.viewholder.MessageViewHolderManager;
import cn.wildfire.chat.kit.third.location.viewholder.LocationMessageContentViewHolder;
import cn.wildfirechat.chat.BuildConfig;
import cn.wildfirechat.chat.R;


public class MyApp extends BaseApp {

    public static String BUGLY_ID = "08ab5f39b0";
    public static String HOST_IP = null;
    public static final String APP_ID = "2882303761520248330";
    public static final String APP_KEY = "5772024839330";
    public static final String TAG = "your packagename";

    @Override
    public void onCreate() {
        super.onCreate();

        AppService.validateConfig(this);


        if(isMainProcess()){
            MiPushClient.registerPush(this, APP_ID, APP_KEY);
        }


        /*String languageToLoad  = "zh";
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = getResources().getConfiguration();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        config.locale = Locale.SIMPLIFIED_CHINESE;
        getResources().updateConfiguration(config, metrics);*/

        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        HOST_IP = sp.getString("hostip", null);
        if(HOST_IP == null || HOST_IP.equals("")){
            HOST_IP = BuildConfig.HOST_IP;
            sp.edit().putString("hostip", HOST_IP).apply();
        }
//        if ("wildfirechat.cn".equals(Config.IM_SERVER_HOST)) {
//            CrashReport.initCrashReport(getApplicationContext(), BUGLY_ID, false);
//        }
        // 只在主进程初始化，否则会导致重复收到消息
        if (getCurProcessName(this).equals(BuildConfig.APPLICATION_ID)) {
            // 如果uikit是以aar的方式引入 ，那么需要在此对Config里面的属性进行配置，如：
            // Config.IM_SERVER_HOST = "im.example.com";
            WfcUIKit wfcUIKit = WfcUIKit.getWfcUIKit();
            wfcUIKit.init(this,HOST_IP);
            wfcUIKit.setAppServiceProvider(AppService.Instance());
            //PushService.init(this, BuildConfig.APPLICATION_ID);
            MessageViewHolderManager.getInstance().registerMessageViewHolder(LocationMessageContentViewHolder.class, R.layout.conversation_item_location_send, R.layout.conversation_item_location_send);
            setupWFCDirs();

            registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
        }
    }

    private boolean isMainProcess() {
        ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = getPackageName();
        int myPid = Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }

    private void setupWFCDirs() {
        /*File file = new File(Config.VIDEO_SAVE_DIR);
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(Config.AUDIO_SAVE_DIR);
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(Config.FILE_SAVE_DIR);
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(Config.PHOTO_SAVE_DIR);
        if (!file.exists()) {
            file.mkdirs();
        }*/
    }

    public static String getCurProcessName(Context context) {

        int pid = android.os.Process.myPid();

        ActivityManager activityManager = (ActivityManager) context
            .getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager
            .getRunningAppProcesses()) {

            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    private Application.ActivityLifecycleCallbacks activityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
        //打开的Activity数量统计
        private int activityStartCount = 0;

        @Override
        public void onActivityStarted(Activity activity) {
            activityStartCount++;
            //数值从0变到1说明是从后台切到前台
            if (activityStartCount == 1){
                //从后台切到前台
        //        ChatManager.Instance().setHost(HOST_IP);
            }
        }

        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {

        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {
            activityStartCount--;
            //数值从1到0说明是从前台切到后台
            if (activityStartCount == 0){
                //从前台切到后台
            }
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {

        }

    };
}
