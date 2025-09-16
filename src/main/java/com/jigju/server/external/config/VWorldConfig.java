package com.jigju.server.external.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "vworld")
public class VWorldConfig {
    private String apiUrl;
    private String key;
}
