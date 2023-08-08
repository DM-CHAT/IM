package com.mhhy.controller;

import com.alibaba.fastjson.JSONObject;
import com.mhhy.common.BaseResult;
import com.mhhy.model.entity.ServerInfoEntity;
import com.mhhy.model.entity.UserEntity;
import com.mhhy.model.req.LoginReq;
import com.mhhy.model.resp.LoginResp;
import com.mhhy.model.struct.ServiceInfo;
import com.mhhy.service.ServerInfoService;
import com.mhhy.service.UserService;
import io.swagger.annotations.ApiOperation;
import lombok.AccessLevel;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/server")
@Setter(value = AccessLevel.PRIVATE, onMethod_ = @Autowired)
public class ServerController {

    @Autowired
    public ServerInfoService serverInfoService;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    @CrossOrigin(origins = "*")
    //@ApiOperation(value = "登录", response = LoginResp.class)
    public BaseResult getServerList(){

        /*ServiceInfo info = new ServiceInfo();
        info.setId(1);
        info.setIconUrl("https://luckmoney8888.com/static/zolo.jpg");
        info.setServiceName("J-Talking");
        info.setUrl("https://luckmoney8888.com/#/login");
        info.setServiceRemark("杀戮空间阿拉山口的房价");
        info.setAppRemark("阿斯顿发送到发斯蒂芬");
        info.setAppIntroduction("大事发生打发斯蒂芬");
        info.setCreateTime("2022-09-28T14:34:39.000+08:00");
        List<ServiceInfo> list = new ArrayList<>();
        list.add(info);
        JSONObject data = new JSONObject();
        data.put("data", list);*/


        return BaseResult.success(200, serverInfoService.getServerList());
    }

}
