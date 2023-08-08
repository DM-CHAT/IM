package com.mhhy.controller;


import com.google.code.kaptcha.Producer;
import com.mhhy.common.BaseResult;
import com.mhhy.service.KaptchaService;
import com.mhhy.util.RedisUtil;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

@Api(value="授权",tags={"验证码"})
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("api/kaptcha")
public class KaptchaController {

    @Autowired
    Producer kaptchaProduer;

    @Autowired
    KaptchaService kaptchaService;

    /**
     * 生成图形验证码
     * @author:
     * @param:null
     * @return:Ajax_Result（统一返回工具）
     * @date: 2022/9/9 15:47
     */

    @ApiOperation(value="获取图片验证码",notes = "发送短信验证码需要")
    @GetMapping("/create")
    @CrossOrigin(origins = "*")
    public BaseResult getKaptcha(@RequestParam("tel") String tel){
        String imagecode = kaptchaProduer.createText();
        // 生成图片
        BufferedImage image = kaptchaProduer.createImage(imagecode);
        try(FastByteArrayOutputStream os = new FastByteArrayOutputStream()){
            ImageIO.write(image,"png",os);
            RedisUtil.setEx("kaptcha:" + tel, imagecode, 90);
            //kaptchaService.setKaptcha(tel,imagecode);
            return BaseResult.success(200, Base64.encode(os.toByteArray()));
        }catch(Exception e){
            throw  new RuntimeException("验证码渲染错误!");
        }
    }


}
