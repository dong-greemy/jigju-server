package com.jigju.server;

import com.jigju.server.external.config.NaverConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(NaverConfig.class)
public class JigjuServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(JigjuServerApplication.class, args);
    }

}
