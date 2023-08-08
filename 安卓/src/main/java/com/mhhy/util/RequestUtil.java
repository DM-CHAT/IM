package com.mhhy.util;



import com.alibaba.fastjson.JSONObject;
import com.mhhy.SpringContextHolder;
import com.mhhy.model.entity.UserEntity;
import com.mhhy.service.impl.UserServiceImpl;
import com.mhhy.util.token.JwtUtil;
import com.mhhy.util.token.OdcProperties;
import com.ospn.common.OsnUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zcw
 * @version 1.0
 * @date 2020/1/14 00:19
 * @description 请求工具
 */
@Log4j2
public class RequestUtil {

    private final static UserServiceImpl userService = SpringContextHolder.getBean("userService", UserServiceImpl.class);

    private final static OdcProperties properties = SpringContextHolder.getBean("odcProperties");

    private static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    public static String getIp() {
        HttpServletRequest request = getRequest();
        String clientIp = request.getHeader("x-forwarded-for");
        if (clientIp == null || clientIp.length() == 0 || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.length() == 0 || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("WL-Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.length() == 0 || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getRemoteAddr();
        }
        return clientIp;
    }

    public static String getRequestURI() {
        HttpServletRequest request = getRequest();
        return request.getRequestURI();
    }

    public static String getHeader(String name) {
        HttpServletRequest request = getRequest();
        return request.getHeader(name);
    }

    public static String getParameter(String name) {
        HttpServletRequest request = getRequest();
        return request.getParameter(name);
    }

    public static String getDevice(){
        HttpServletRequest request = getRequest();
        return request.getHeader("device");
    }

    public static String getUserAgent() {
        HttpServletRequest request = getRequest();
        return request.getHeader("User-Agent");
    }

    public static String getToken() {
        String token = getHeader(properties.getConfig().getTokenHeader());
        if (token == null) {
            token = getParameter("token");
        }
        return token;
    }

    public static boolean isLogined() {
        String token = getToken();
        if (token == null || token.trim().length() == 0) {
            return false;
        }
        try {
            return getCurUserId() > 0;
        } catch (Exception e) {
            log.debug(e.getMessage());
        }
        return false;
    }

    //获取token中的用户id
    public static int getMainUserId() {
        int userId = JwtUtil.verifyToken(getToken());
        //int userId = 1;
        //int userId = Integer.parseInt(RequestUtil.getHeader("userId"));
        UserEntity userEntity = userService.getById(userId);
        //AssertUtil.notNull(userEntity, ResultCodeEnum.DATA_ORDER);
        //AssertUtil.isTrue(userEntity.getStatus() == 1, ResultCodeEnum.ACCOUNT_DISABLED);
        return userId;
    }

    @Deprecated
    //获取请求头的用户id
    public static int getCurUserId() {
        int userId = JwtUtil.verifyToken(getToken());
        //int userId = Integer.parseInt(RequestUtil.getHeader("userId"));
        UserEntity userEntity = userService.getById(userId);
        //AssertUtil.notNull(userEntity, ResultCodeEnum.WALLET_BAD_PARAM);
        //AssertUtil.isTrue(userEntity.getStatus() == 1, ResultCodeEnum.ACCOUNT_DISABLED);
        return userId;
    }

    public static UserEntity getCurUser() {
        return userService.getById(getCurUserId());
    }

    //获取当前主账号
    public static UserEntity getMainUser() {
        return userService.getById(getMainUserId());
    }

    public static JSONObject getParam(String user, String data) {

        try {
            String communicationKey = RedisUtil.get("Communicationkey:"+user);
            System.out.println("Communicationkey:"+ communicationKey);
            return JSONObject.parseObject(OsnUtils.aesDecrypt(data, communicationKey));
        } catch (Exception e) {

        }

        return null;
    }

}
