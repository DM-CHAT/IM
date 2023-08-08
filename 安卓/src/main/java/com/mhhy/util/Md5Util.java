
package com.mhhy.util;

import org.springframework.util.DigestUtils;

import java.security.MessageDigest;

/**
 * @author zcw
 * @version 1.0
 * @date 2020/1/6 17:03
 * @description md5工具类
 */
public class Md5Util {

    public static String md5(String str) {
        if (str == null) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(str.getBytes());
    }

    public static String MD5(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(data.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte item : array) {
                sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString().toUpperCase();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
