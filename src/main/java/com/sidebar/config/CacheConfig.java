package com.sidebar.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 캐시 환경 설정을 담당하는 클래스.
 *
 * 이 클래스는 애플리케이션에서 캐싱을 활성화하고, 캐시 관리자(CacheManager)를 생성하며,
 * 주기적인 캐시 제거 작업을 처리하기 위한 설정을 포함한다.
 *
 * 캐시는 특정 데이터의 빠른 접근을 위해 설정되며, 애플리케이션 성능을 최적화하는 데 사용된다.
 * 이 클래스는 특히 북마크(bookmarks) 데이터와 카테고리 트리(category tree) 데이터를
 * 캐싱하여 반복적인 데이터 로드 작업을 줄이는 것을 목표로 한다.
 */
@Configuration
@EnableCaching
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
     * 애플리케이션에서 캐싱을 활성화하고 관리하기 위한 CacheManager를 생성한다.
     */
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
                new ConcurrentMapCache(BOOKMARKS_CACHE),
                new ConcurrentMapCache(CATEGORY_TREE_CACHE)
        ));
        return cacheManager;
    }

    /**
     * 모든 캐시를 주기적으로 제거하는 메서드.
     */
    @Scheduled(fixedRateString = "${cache.ttl}000") // Convert seconds to milliseconds
    public void evictAllCaches() {
        if (cacheProperties.isEnabled()) {
            cacheManager().getCacheNames()
                    .forEach(cacheName -> cacheManager().getCache(cacheName).clear());
        }
    }
}