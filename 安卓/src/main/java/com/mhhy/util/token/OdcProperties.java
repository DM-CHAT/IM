package com.mhhy.util.token;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "com.mhhy", ignoreUnknownFields = true)
public class OdcProperties {

    private Oss oss = new Oss();

    private Async async = new Async();

    private Config config = new Config();

    @Data
    public static class Oss {
        private String endpoint;
        private String accessKeyId;
        private String accessKeySecret;
        private String bucketName;
        private String prefixUrl;
    }

    @Data
    public static class Async {
        private int corePoolSize = 10;
        private int maxPoolSize = 40;
        private int queueCapacity = 20;
        private int keepAliveSeconds = 30;
    }

    @Data
    public static class Config {

        private String tokenHeader = "Authorization";

        //登录token，redis key格式
        private String tokenRedisKeyFormat = "mhhy:token:%d:%s";

        //token有效期,10年
        private long tokenExpireSeconds = 315360000L;

        //限制只能登录一个
        private boolean limitLoginOneOnly = true;

        private String userPasswordKey = "mhhy2022";
    }
}
