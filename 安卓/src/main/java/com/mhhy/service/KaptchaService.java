package com.mhhy.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mhhy.config.sms.Check;
import com.mhhy.util.RedisUtil;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class KaptchaService {
    private static Cache<String, String> kaptchaCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    public void setKaptcha(String tel,String code){
        kaptchaCache.put(tel,code);
    }
    public void check(String tel,String code){
        Check.requireNonNull(code,"验证码错误!");
        String c = kaptchaCache.getIfPresent(tel);
        Check.requireNonNull(c,"验证码错误!");
        Check.requireTrue(c.equalsIgnoreCase(code),"验证码错误!");
    }
    public boolean check2(String tel,String code){
        try {
            String c = kaptchaCache.getIfPresent(tel);
            return code.equalsIgnoreCase(c);

        } catch (Exception e) {

        }
        return false;
    }
    public String getPicCode(String tel) {
        //RedisUtil.setEx("kaptcha:" + tel, imagecode, 90);
        return RedisUtil.get("kaptcha:" + tel);
    }
    public void clear(String tel){
        kaptchaCache.invalidate(tel);
    }
}
