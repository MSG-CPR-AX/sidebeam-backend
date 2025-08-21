package com.sidebeam.common.rest.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Data
@Configuration
@ConfigurationProperties(prefix = "http.client")
public class RestClientProperties {

    // TCP connect timeout in milliseconds.
    private int connectTimeoutMillis = 3000;

    // Read/response timeout in milliseconds.
    private int responseTimeoutMillis = 5000;

    // Max connections in the Reactor Netty connection pool.
    private int poolMaxConnections = 100;

    // Max time to wait for a connection from the pool in milliseconds.
    private int pendingAcquireTimeoutMillis = 2000;

    // Max idle time for pooled connections.
    private Duration poolMaxIdleTime = Duration.ofSeconds(30);

    // Max lifetime for pooled connections.
    private Duration poolMaxLifeTime = Duration.ofMinutes(5);
}