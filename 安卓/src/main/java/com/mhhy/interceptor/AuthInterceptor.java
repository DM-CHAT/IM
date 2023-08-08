package com.mhhy.interceptor;



import com.mhhy.annotation.Login;
import com.mhhy.util.RedisUtil;
import com.mhhy.util.RequestUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author zcw
 * @version 1.0
 * @date 2019/11/21 21:53
 * @description 授权认证
 */
@Log4j2
@Configuration
@Component(value = "authInterceptor")
public class AuthInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        Login login = ((HandlerMethod) handler).getMethodAnnotation(Login.class);
        if (login == null) {
            return true;
        }
        if (login.encrypted()) {
            return true;
        }
        //已登录的接口，限制同一个接口，每两秒最多只能调一次
        int userId = RequestUtil.getMainUserId();
        String path = request.getServletPath();
        String method = request.getMethod();
        if ("GET".equalsIgnoreCase(method)) {
            return true;
        }
        String cacheKey = String.format("vex:apiLimit:%d-%s-%s", userId, method, path);
        if (RedisUtil.setNx(cacheKey, "0")) {
            RedisUtil.expire(cacheKey, 2);
            return true;
        }
        throw new IllegalArgumentException("请勿重复点击");//请勿重复点击 Please do not click repeatedly
    }
}
