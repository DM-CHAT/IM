package com.mhhy.util.token;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import com.mhhy.SpringContextHolder;
import com.mhhy.enums.ResultCodeEnum;
import com.mhhy.exception.OdcException;
import com.mhhy.util.RedisUtil;
import com.mhhy.util.RequestUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * @author zcw
 * @version 1.0
 * @date 2019/11/23 11:06
 * @description jwt工具类
 */
@Slf4j
public class JwtUtil {

    private final static Algorithm algorithm = SpringContextHolder.getBean("algorithm", Algorithm.class);

    private final static OdcProperties properties = SpringContextHolder.getBean("odcProperties", OdcProperties.class);

    /**
     * 创建token
     *
     * @param userId;
     * @param timeout; 单位是秒
     */
    public static String createJwtToken(int userId, long timeout) {
        return JWT.create()
            .withClaim("member", userId)
            .withExpiresAt(new Date(System.currentTimeMillis() + timeout * 1000))
            .sign(algorithm);
    }


    /**
     * token正确且有效，则返回userId
     */
    public static int verifyToken(String token) {
        Map<String, String> extraMap = Collections.singletonMap("uri", RequestUtil.getRequestURI());
        if (token == null || token.trim().length() == 0) {
            throw new OdcException(ResultCodeEnum.UN_AUTHORIZATION);
        }
        try {
            String noBearerToken = token.replaceFirst("Bearer ", "");
            Integer userId = JWT.require(algorithm)
                .build()
                .verify(noBearerToken)
                .getClaim("member")
                .asInt();
            if (RedisUtil.get(getRedisKey(userId, noBearerToken)) != null) {
                return userId;
            }
        } catch (Exception e) {
            throw new OdcException(ResultCodeEnum.UN_AUTHORIZATION, extraMap);
        }
        throw new OdcException(ResultCodeEnum.UN_AUTHORIZATION, extraMap);
    }

    public static String getRedisKey(Integer userId, String token) {
        return String.format(properties.getConfig().getTokenRedisKeyFormat(), userId, token);
    }

    public static void putTokenToRedis(Integer userId, String token) {
        RedisUtil.setEx(getRedisKey(userId, token), "0", properties.getConfig().getTokenExpireSeconds());
    }

    public static void removeTokenByUserId(Integer userId) {
        Set<String> tokenSet = RedisUtil.keys(getRedisKey(userId, "*"));
        for (String key : tokenSet) {
            log.info("删除token, {}, {}", userId, key);
            RedisUtil.del(key);
        }
    }
}
