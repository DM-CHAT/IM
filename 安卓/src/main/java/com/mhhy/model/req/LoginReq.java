package com.mhhy.model.req;

import lombok.Data;

@Data
public class LoginReq {

    private String username;

    private String password;

    private String nickName;

    private String smsCode;

    private String picCode;

    private String data;

    private String user;
}
