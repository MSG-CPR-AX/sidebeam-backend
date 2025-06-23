package com.sidebeam.external.gitlab.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "webhook")
public class WebhookProperties {

    private String secretToken;
}