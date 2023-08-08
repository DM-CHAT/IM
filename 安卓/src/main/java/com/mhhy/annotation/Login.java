package com.mhhy.annotation;

import java.lang.annotation.*;

/**
 * 用于标记接口登录、参数解密，标记的非加密接口必须登录
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Login {

    //该接口是否加密
    boolean encrypted() default false;

    //AES解密密钥，在encrypted为true时有效，若不指定，则从当前token中获取
    String key() default "";
}
