package com.mhhy.config.redis;

import com.mhhy.util.Md5Util;
import org.springframework.cache.interceptor.KeyGenerator;

import java.lang.reflect.Method;

public class CacheKeyGenerator implements KeyGenerator {

    @Override
    public Object generate(Object target, Method method, Object... params) {
        String className = target.getClass().getName();
        String methodName = method.getName();
        StringBuilder builder = new StringBuilder();
        //builder.append(className).append("&");
        //builder.append(methodName).append("&");
        if (params.length > 0) {
            int i = 0;
            while (i < params.length) {
                builder.append(params[i++].toString()).append("&");
            }
        }
        return className + ":" + methodName + ":" + Md5Util.md5(builder.toString());
    }
}
