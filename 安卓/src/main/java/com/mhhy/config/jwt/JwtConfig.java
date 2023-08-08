package com.mhhy.config.jwt;

import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.UnsupportedEncodingException;

@Configuration
public class JwtConfig {

    public static final String JWT_SECRET = "mhhy2022";

    @Bean
    public Algorithm algorithm() throws UnsupportedEncodingException {
        return Algorithm.HMAC256(JWT_SECRET);
    }
}
