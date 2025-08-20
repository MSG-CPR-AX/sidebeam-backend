package com.sidebeam.external.gitlab.config.property;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ToString(exclude = {"secretToken"})
@Configuration
@ConfigurationProperties(prefix = "webhook")
public class WebhookProperties {

    @JsonIgnore
    private String secretToken;
}