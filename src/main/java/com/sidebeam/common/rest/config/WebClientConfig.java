package com.sidebeam.common.rest.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import javax.net.ssl.SSLException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClient 설정을 위한 구성 클래스입니다.
 * 외부 API 호출(WebClient)에 대한 연결 풀/타임아웃을 중앙집중적으로 적용합니다.
 */
@Configuration
public class WebClientConfig {

    @Bean
    public ReactorClientHttpConnector reactorClientHttpConnector(RestClientProperties props) throws SSLException {

        ConnectionProvider provider = ConnectionProvider.builder("http-pool")
                .maxConnections(props.getPoolMaxConnections())
                .pendingAcquireTimeout(Duration.ofMillis(props.getPendingAcquireTimeoutMillis()))
                .maxIdleTime(props.getPoolMaxIdleTime())
                .maxLifeTime(props.getPoolMaxLifeTime())
                .build();

        // SSL 검증을 비활성화하는 SslContext 생성
        SslContext sslContext = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();

        HttpClient httpClient = HttpClient.create(provider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, props.getConnectTimeoutMillis())
                .responseTimeout(Duration.ofMillis(props.getResponseTimeoutMillis()))
                .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(props.getResponseTimeoutMillis(), TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(props.getResponseTimeoutMillis(), TimeUnit.MILLISECONDS))
                );

        return new ReactorClientHttpConnector(httpClient);
    }

    /**
     * 기본 WebClient 빌더 빈을 생성합니다.
     * 타임아웃/커넥션 풀/기본 헤더를 적용합니다.
     */
    @Bean
    public WebClient.Builder webClientBuilder(ReactorClientHttpConnector connector) {
        return WebClient.builder()
                .clientConnector(connector)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
    }
}