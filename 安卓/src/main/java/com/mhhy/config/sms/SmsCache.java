package com.mhhy.config.sms;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mhhy.exception.CheckException;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class SmsCache {
    private static Cache<String, String> smsCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    private static Cache<String, Integer> maxCount = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .build();

    public  void checkMaxCount(String tel){
        Integer max = maxCount.getIfPresent(tel);
        if (max != null && max > 10) {
            throw new CheckException("同一号码每天最多10次");
        }
    }
    public void add(String tel, String code) {
        Integer max = maxCount.getIfPresent(tel);
        maxCount.put(tel, max == null ? 1 : max + 1);
        smsCache.put(tel, code);
    }

    /*public String get(String tel) {
        return smsCache.get(tel, "");
    }*/


    public boolean haveCode(String tel) {
        return smsCache.getIfPresent(tel) != null;
    }
    public void clear(String tel ) {
        smsCache.invalidate(tel);
    }
}
