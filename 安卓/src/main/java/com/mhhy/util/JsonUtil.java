package com.mhhy.util;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;

import static com.mhhy.service.impl.LoginServiceImpl.conversationFlag;

public class JsonUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 将 POJO 转为 JSON
     */
    public static <T> String toJson(T obj) {
        String json;
        try {
            json = OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return json;
    }

    /**
     * 将 JSON 转为 POJO
     */
    public static <T> T fromJson(String json, Class<T> type) {
        T pojo;
        try {
            pojo = OBJECT_MAPPER.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return pojo;
    }

    /**
     * 解密并返回具体类型
     * @param data
     * @param userId
     * @param obj
     * @param <T>
     * @return
     */
    public static <T> T getResult(String data, int userId, Class<T> obj) {
        T pojo = null;
        try {
            //获取会话密钥
            String conversationId = RedisUtil.get(conversationFlag + userId);
            if (conversationId == null) {
                System.out.println("conversationId is null.");
                return null;
            }
            //对数据进行解密
            System.out.println("conversationId : " + conversationId);
            System.out.println("data : " + data);

            byte[] bytes = PasswordUtil.getSHA256Str(conversationId);
            String result = Aes128Util.AESDecode(data, bytes);

            System.out.println("result : " + result);

            pojo = JsonUtil.fromJson(result, obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pojo;
    }

    public static String genResult(JSONObject json, int userId) {
        try {
            //获取会话密钥
            String conversationId = RedisUtil.get(conversationFlag + userId);
            //对数据进行解密
            byte[] bytes = PasswordUtil.getSHA256Str(conversationId);
            String result = Aes128Util.AESEncode(json.toString(), bytes);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
