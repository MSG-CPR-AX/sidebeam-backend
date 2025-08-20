package com.sidebeam.common.core.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    private final ApiKey apiKey = new ApiKey();

    @Data
    public static class ApiKey {
        /** Enable API key authentication for non-whitelisted endpoints */
        private boolean enabled = false;
        /** Header name to read API key from */
        private String headerName = "X-Api-Key";
        /** Expected API key value (should be provided via environment/secret) */
        private String value = "";
        /** Comma-separated ant patterns to exclude from API key check (optional, in addition to defaults) */
        private String excludePatterns = "";
    }
}
