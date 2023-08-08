/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.app;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import java.util.Locale;

public class BaseApp extends MultiDexApplication {

    private static Context mContext;
    private static long mMainThreadId;
    private static Handler mHandler;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = getApplicationContext();
        mMainThreadId = android.os.Process.myTid();
        mHandler = new Handler();

        webviewSetPath(mContext);
    }

    public static Context getContext() {
        return mContext;
    }

    public static void setContext(Context mContext) {
        BaseApp.mContext = mContext;
    }

    public static long getMainThreadId() {
        return mMainThreadId;
    }

    public static Handler getMainHandler() {
        return mHandler;
    }

    public void webviewSetPath(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String processName = getProcessName(context);
            if (!context.getPackageName().equals(processName)) {
                WebView.setDataDirectorySuffix(processName);
            }
        }
    }

    public String getProcessName(Context context) {
        if (context == null) return null;
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            if (processInfo.pid == android.os.Process.myPid()) {
                return processInfo.processName;
            }
        }
        return null;
    }
}
