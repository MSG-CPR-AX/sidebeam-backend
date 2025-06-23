package com.sidebeam.bookmark.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient 설정을 위한 구성 클래스입니다.
 * GitLab API 호출에 사용될 WebClient 인스턴스를 생성합니다.
 */
@Configuration
public class WebClientConfig {

    /**
     * 기본 WebClient 빈을 생성합니다.
     * 이 WebClient는 GitLab API 호출에 사용됩니다.
     *
     * @return 구성된 WebClient 빌더
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
    }
}