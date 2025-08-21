package com.sidebeam.external.gitlab.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 간단한 재시도 정책 구현입니다. 기본적으로 지정된 횟수만큼 즉시 재시도합니다.
 * 백오프나 조건부 재시도가 필요하면 다른 구현체를 제공할 수 있습니다.
 */
@Slf4j
@Component
public class SimpleRetryPolicy implements RetryPolicy {

    /**
     * 최대 재시도 횟수 (프로퍼티로 주입 가능)
     */
    private final int maxAttempts;

    public SimpleRetryPolicy(@Value("${gitlab.retry.max-attempts:3}") int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    @Override
    public <T> Mono<T> withRetry(Mono<T> source, String context) {
        return source.retryWhen(reactor.util.retry.Retry
                .fixedDelay(maxAttempts - 1, java.time.Duration.ofMillis(300))
                .doBeforeRetry(signal ->
                        log.warn("[Retry] {} attempt {} due to: {}",
                                context,
                                signal.totalRetriesInARow() + 1,
                                signal.failure().getMessage()))
        );
    }

    @Override
    public <T> Flux<T> withRetry(Flux<T> source, String context) {
        return source.retryWhen(reactor.util.retry.Retry
                .fixedDelay(maxAttempts - 1, java.time.Duration.ofMillis(300))
                .doBeforeRetry(signal ->
                        log.warn("[Retry] {} attempt {} due to: {}",
                                context,
                                signal.totalRetriesInARow() + 1,
                                signal.failure().getMessage()))
        );
    }
}
