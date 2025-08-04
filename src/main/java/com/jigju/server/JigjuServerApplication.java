package com.jigju.server;

import com.jigju.server.external.config.NaverConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableConfigurationProperties(NaverConfig.class)
@EnableCaching
public class JigjuServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(JigjuServerApplication.class, args);
    }

}
