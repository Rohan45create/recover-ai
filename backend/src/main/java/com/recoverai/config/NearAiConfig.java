package com.recoverai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "nearai")
public class NearAiConfig {
    private String url;
    private String key;
    private String model;
}
