package com.mhhy.controller;

import com.mhhy.common.BaseResult;
import com.mhhy.config.sms.SmsCache;
import com.mhhy.service.SmsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(value="短信接口",tags={"验证码"})
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("api/sms")
public class SmsController {


    @Autowired
    SmsService smsService;
    @Autowired
    SmsCache smsCache;

    /*@ApiOperation(value="发送验证码",notes = "发送验证码5分钟有效")
    @GetMapping("code")
    public void send(@RequestParam("tel") String tel,
                     @ApiParam(value = "图片验证码,超级验证码 3721",required=true)
                     @RequestParam("kaptcha")String kaptcha){
        smsService.sendCode(tel,kaptcha);
    }*/

    @ApiOperation(value="发送验证码",notes = "发送验证码5分钟有效")
    @GetMapping("code")
    @CrossOrigin(origins = "*")
    public BaseResult send(@RequestParam("tel") String tel){
        System.out.println("api/sms/code tel:" + tel);
        return smsService.sendCode(tel,"372100");
    }
}
