package cn.wildfire.chat.app;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    /**
     * 获取自己应用内部的版本号
     */
    public static int getVersionCode(Context context) {
        PackageManager manager = context.getPackageManager();
        int code = 0;
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            code = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return code;
    }
    /**
     * 获取自己应用内部的版本名
     */
    public static String getVersionName(Context context) {
        PackageManager manager = context.getPackageManager();
        String name = null;
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            name = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return name;
    }

    //获取设备唯一标识
    public synchronized static String getDeviceId(Context mContext) {
        TelephonyManager telephonyManager = (TelephonyManager)mContext.getSystemService(mContext.TELEPHONY_SERVICE);
        String deviceId= telephonyManager.getDeviceId();
        return deviceId;
    }


    public static String getDeviceSN(){
        String serialNumber = android.os.Build.SERIAL;
        return serialNumber;
    }

    public static String getDateToString(long milSecond, String pattern) {
        Date date = new Date(milSecond);
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    // 将文本复制到剪切板
    public static void copyToClipboard(Context context, String content) {
        // 从 API11 开始 android 推荐使用 android.content.ClipboardManager
        // 为了兼容低版本我们这里使用旧版的 android.text.ClipboardManager，虽然提示 deprecated，但不影响使用。
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        // 将文本内容放到系统剪贴板里。
        cm.setText(content);
    }
}
