package com.sidebeam.common.cache.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 캐시 환경 설정을 담당하는 클래스.
 *
 * 이 클래스는 애플리케이션에서 캐싱을 활성화하고, Caffeine 기반의 캐시 관리자(CacheManager)를 생성한다.
 * 캐시는 특정 데이터의 빠른 접근을 위해 설정되며, 애플리케이션 성능을 최적화하는 데 사용된다.
 * 이 클래스는 특히 북마크(bookmarks) 데이터와 카테고리 트리(category tree) 데이터를
 * 캐싱하여 반복적인 데이터 로드 작업을 줄이는 것을 목표로 한다.
 */
@Configuration
public class CacheConfig {

    /**
     * 북마크 데이터를 캐싱하기 위한 캐시 이름을 정의하는 상수.
     */
    public static final String BOOKMARKS_CACHE = "bookmarks";

    /**
     * 카테고리 트리 데이터를 캐싱하기 위한 캐시 이름 상수.
     */
    public static final String CATEGORY_TREE_CACHE = "categoryTree";

    /**
     * GitLab 원본 데이터를 캐싱하기 위한 캐시 이름 상수.
     */
    public static final String GITLAB_DATA_CACHE = "gitlabDataCache";

    /**
     * 캐시 관련 설정 값을 주입받기 위해 사용되는 객체.
     */
    private final CacheProperties cacheProperties;

    /**
     * CacheConfig 생성자.
     */
    public CacheConfig(CacheProperties cacheProperties) {
        this.cacheProperties = cacheProperties;
    }

    /**
     * 애플리케이션에서 캐싱을 활성화하고 관리하기 위한 Caffeine 기반 CacheManager를 생성한다.
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCacheNames(List.of(BOOKMARKS_CACHE, CATEGORY_TREE_CACHE, GITLAB_DATA_CACHE));
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    /**
     * Caffeine 캐시의 동작을 정의하는 빌더를 생성한다.
     * 캐시 만료 시간, 최대 크기, 통계 기록 등을 설정한다.
     * @return Caffeine 빌더 객체
     */
    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .expireAfterWrite(cacheProperties.getTtl(), TimeUnit.SECONDS)
                .maximumSize(500) // 캐시 최대 항목 수
                .recordStats();   // 캐시 사용 통계 기록 (Actuator 메트릭스 연동)
    }
}