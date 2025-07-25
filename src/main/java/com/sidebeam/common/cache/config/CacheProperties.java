package com.sidebeam.common.cache.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "cache")
public class CacheProperties {

    private boolean enabled = true;

    private long ttl = 3600;
}