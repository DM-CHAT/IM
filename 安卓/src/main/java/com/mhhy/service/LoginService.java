package com.mhhy.service;

import com.alibaba.fastjson.JSONObject;
import com.mhhy.common.BaseResult;
import com.mhhy.model.entity.UserEntity;
import com.mhhy.model.req.LoginReq;
import com.mhhy.model.req.RegisterReq;
import com.mhhy.model.resp.LoginResp;

public interface LoginService {

    BaseResult register(RegisterReq registerReq);
    BaseResult register2(RegisterReq registerReq);
    BaseResult register4(RegisterReq registerReq, String key);
    BaseResult register5(RegisterReq registerReq, String key, String UserPwd, String owner2);

    JSONObject register3(RegisterReq registerReq);
    UserEntity register6(String imId, String owner2);

    BaseResult login(LoginReq loginReq);

    BaseResult getChallenge(String userName);

    JSONObject getConfig();

    String createGroup(String groupName, String owner);
}
