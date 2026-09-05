package com.recoverai.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.Arrays;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ConfigValidator {

    private final Environment env;

    @Value("${api.demo-key:}")
    private String apiDemoKey;

    @Value("${razorpay.webhook-secret:}")
    private String razorpayWebhookSecret;

    @Value("${nearai.key:}")
    private String nearAiKey;

    @PostConstruct
    public void validateConfig() {
        boolean isDev = Arrays.asList(env.getActiveProfiles()).contains("dev");

        if (!isDev) {
            log.info("Running in non-dev profile. Validating critical security configurations...");
            
            if (!StringUtils.hasText(apiDemoKey)) {
                log.error("CRITICAL CONFIGURATION ERROR: API_DEMO_KEY is empty or missing.");
                throw new IllegalStateException("API_DEMO_KEY must be configured in non-dev profiles.");
            }
            
            if (!StringUtils.hasText(razorpayWebhookSecret)) {
                log.error("CRITICAL CONFIGURATION ERROR: RAZORPAY_WEBHOOK_SECRET is empty or missing.");
                throw new IllegalStateException("RAZORPAY_WEBHOOK_SECRET must be configured in non-dev profiles.");
            }
            
            if (!StringUtils.hasText(nearAiKey)) {
                log.error("CRITICAL CONFIGURATION ERROR: NEAR_AI_KEY is empty or missing.");
                throw new IllegalStateException("NEAR_AI_KEY must be configured in non-dev profiles.");
            }
            
            log.info("Critical security configurations validated successfully.");
        }
    }
}
