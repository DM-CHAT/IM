package com.mhhy.util;

import com.ospn.common.OsnUtils;

public class ResponseUtil {

    public static String createResponse(String user, String data) {

        try {
            String communicationKey = RedisUtil.get("Communicationkey:"+user);
            return OsnUtils.aesEncrypt(data, communicationKey);
        } catch (Exception e) {

        }

        return null;
    }

}
