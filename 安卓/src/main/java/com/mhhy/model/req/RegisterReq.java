package com.mhhy.model.req;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

@Data
public class RegisterReq {

    private String username;

    private String password;

    private String nickName;

    private String smsCode;

    private String picCode;

    private String data;

    public RegisterReq() {

    }

    public RegisterReq(String param) {
        try {
            JSONObject json = JSONObject.parseObject(param);
            username = json.getString("username");
            password = json.getString("password");
            nickName = json.getString("nickName");
        } catch (Exception e) {

        }

    }
}
