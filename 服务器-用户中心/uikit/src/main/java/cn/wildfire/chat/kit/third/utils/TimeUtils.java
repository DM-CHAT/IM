package cn.wildfire.chat.kit.third.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.WfcUIKit;

/**
 * @创建者 CSDN_LQR
 * @描述 时间工具（需要joda-time）
 */
public class TimeUtils {

    /**
     * 得到仿微信日期格式输出
     *
     * @param msgTimeMillis
     * @return
     */
    public static String getMsgFormatTime(long msgTimeMillis) {
        if (msgTimeMillis == 0) {
            return "";
        }

        long now = System.currentTimeMillis();
        DateTime nowTime = new DateTime(now);
        DateTime msgTime = new DateTime(msgTimeMillis);
        long dayMillis = 24 * 60 * 60 * 1000;

        if ((int) (now / dayMillis) == (int) (msgTimeMillis / dayMillis)) {
            //早上、下午、晚上 1:40
            return getTime(msgTime);
        } else if ((int) (msgTimeMillis / dayMillis) + 1 == (int) (now / dayMillis)) {
            //昨天
            return WfcUIKit.getString(R.string.Yesterday) + " " + getTime(msgTime);
        } else if (nowTime.getYearOfCentury() == msgTime.getYearOfCentury()) {
            if (nowTime.getWeekOfWeekyear() == msgTime.getWeekOfWeekyear()) {
                //星期
                switch (msgTime.getDayOfWeek()) {
                    case DateTimeConstants.SUNDAY:
                        return WfcUIKit.getString(R.string.Sunday)+ " " + getTime(msgTime);
                    case DateTimeConstants.MONDAY:
                        return WfcUIKit.getString(R.string.Monday)+ " " + getTime(msgTime);
                    case DateTimeConstants.TUESDAY:
                        return WfcUIKit.getString(R.string.Tuesday)+" " + getTime(msgTime);
                    case DateTimeConstants.WEDNESDAY:
                        return WfcUIKit.getString(R.string.Wednesday)+" " + getTime(msgTime);
                    case DateTimeConstants.THURSDAY:
                        return WfcUIKit.getString(R.string.Thursday)+" " + getTime(msgTime);
                    case DateTimeConstants.FRIDAY:
                        return WfcUIKit.getString(R.string.Friday)+" " + getTime(msgTime);
                    case DateTimeConstants.SATURDAY:
                        return WfcUIKit.getString(R.string.Saturday)+" " + getTime(msgTime);
                    default:
                        break;
                }
                return "";
            } else {
                //12月22日
                return msgTime.toString("MM-dd HH:mm");
            }
        } else {
            return msgTime.toString("yyyy-MM-dd HH:mm");
        }
    }

    @NonNull
    private static String getTime(DateTime msgTime) {
        return msgTime.toString("HH:mm");
    }
}
