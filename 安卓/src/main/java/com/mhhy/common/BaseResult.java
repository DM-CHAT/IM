package com.mhhy.common;

import com.mhhy.util.Aes128Util;
import com.mhhy.util.PasswordUtil;
import com.mhhy.util.RedisUtil;
import com.mhhy.util.RequestUtil;
import com.ospn.common.OsnUtils;
import lombok.Data;

import static com.mhhy.service.impl.LoginServiceImpl.conversationFlag;

@Data
//@Builder
public class BaseResult<T> {

    private int code = 200;

    private String msg = "Success";

    private long timeMillis = System.currentTimeMillis();

    private Object data;

    public BaseResult() {
    }

    public BaseResult(Object data) {
        int mainUserId = RequestUtil.getMainUserId();
        String conversationId = RedisUtil.get(conversationFlag + mainUserId);
        byte[] sha256Str = PasswordUtil.getSHA256Str(conversationId);
        data = Aes128Util.AESEncode(data.toString(), sha256Str);
        this.data = data;
    }

    public BaseResult(String data, String key) {
        //byte[] sha256Str = PasswordUtil.decodeBase64(key);
        System.out.println("[BaseResult success] key:" + key);
        this.data = OsnUtils.aesEncrypt(data, key);
        //this.data = Aes128Util.AESEncode(data.toString(), sha256Str);
    }

    public BaseResult(Object data, String key, int type) {
        if (type == 1){
            byte[] sha256Str = PasswordUtil.getSHA256Str(key);
            this.data = Aes128Util.AESEncode(data.toString(), sha256Str);
        } else if (type == 0) {
            byte[] sha256Str = PasswordUtil.decodeBase64(key);
            this.data = Aes128Util.AESEncode(data.toString(), sha256Str);
        }
    }

    public BaseResult(int code, Object data) {
        this.code = code;
        this.data = data;
    }



    public BaseResult(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static BaseResult<?> success(String msg) {
        return new BaseResult<>(200, msg);
    }

    public static BaseResult<?> success(int code, Object msg) {
        return new BaseResult<>(200, msg);
    }





    public static BaseResult<?> success() {
        return new BaseResult<>(200, "success");
    }

    public static BaseResult<?> err(String msg) {
        return new BaseResult<>(500, msg);
    }

}
