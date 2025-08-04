package com.jigju.server.external.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "naver")
public class NaverConfig {
    private String apiUrl;
    private String clientId;
    private String clientSecret;
}