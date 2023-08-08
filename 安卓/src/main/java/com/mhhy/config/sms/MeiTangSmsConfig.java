package com.mhhy.config.sms;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "sms.mei-tang")
public class MeiTangSmsConfig {
    private String account;
    private String password;
    private String url;
    private String content;
}
