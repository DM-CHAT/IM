package com.mhhy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.mhhy.mapper")
public class MhhyFramworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(MhhyFramworkApplication.class, args);
    }

}
