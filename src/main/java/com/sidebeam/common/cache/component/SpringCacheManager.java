package com.sidebeam.common.cache.component;

import com.sidebeam.common.cache.config.CacheProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * GitLab 데이터를 캐시하기 위한 컴포넌트입니다.
 */
@Slf4j
@Component
public class SpringCacheManager {

    private static final String GITLAB_DATA_CACHE = "gitlabDataCache";
    private static final String GITLAB_DATA_KEY = "aggregatedData";

    private final CacheManager cacheManager;
    private final CacheProperties cacheProperties;

    public SpringCacheManager(CacheManager cacheManager, CacheProperties cacheProperties) {
        this.cacheManager = cacheManager;
        this.cacheProperties = cacheProperties;
    }

    /**
     * 데이터를 캐시에 저장합니다.
     *
     * @param data 캐시할 데이터
     * @return 캐시된 데이터
     */
    public <T> Mono<T> cacheData(T data) {
        return Mono.fromCallable(() -> {
            if (!cacheProperties.isEnabled()) {
                log.debug("Caching is disabled, skipping cache operation");
                return data;
            }

            Cache cache = cacheManager.getCache(GITLAB_DATA_CACHE);
            if (cache != null) {
                cache.put(GITLAB_DATA_KEY, data);
                log.debug("Data cached successfully");
            } else {
                log.warn("Cache '{}' not found", GITLAB_DATA_CACHE);
            }
            return data;
        });
    }

    /**
     * 캐시에서 데이터를 가져옵니다.
     *
     * @param type 데이터 타입
     * @return 캐시된 데이터 또는 빈 Mono
     */
    public <T> Mono<T> getCachedData(Class<T> type) {
        return Mono.fromCallable(() -> {
            if (!cacheProperties.isEnabled()) {
                log.debug("Caching is disabled, skipping cache lookup");
                return null;
            }

            Cache cache = cacheManager.getCache(GITLAB_DATA_CACHE);
            if (cache != null) {
                Cache.ValueWrapper wrapper = cache.get(GITLAB_DATA_KEY);
                if (wrapper != null) {
                    Object value = wrapper.get();
                    if (type.isInstance(value)) {
                        log.debug("Cache hit for key: {}", GITLAB_DATA_KEY);
                        return type.cast(value);
                    }
                }
                log.debug("Cache miss for key: {}", GITLAB_DATA_KEY);
            } else {
                log.warn("Cache '{}' not found", GITLAB_DATA_CACHE);
            }
            return null;
        }).filter(data -> data != null);
    }

    /**
     * 캐시를 비웁니다.
     */
    public Mono<Void> clearCache() {
        return Mono.fromRunnable(() -> {
            Cache cache = cacheManager.getCache(GITLAB_DATA_CACHE);
            if (cache != null) {
                cache.clear();
                log.debug("Cache cleared");
            } else {
                log.warn("Cache '{}' not found", GITLAB_DATA_CACHE);
            }
        });
    }
}