package com.sidebeam.external.gitlab.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 외부 API 호출에 대한 재시도 정책을 정의하는 인터페이스입니다.
 *
 * <p>이 컴포넌트를 도입하면 WebClient 호출 코드에서 재시도 횟수나 백오프 전략을 일관되게
 * 적용할 수 있으며, 필요 시 다른 구현체로 쉽게 교체할 수 있습니다.</p>
 */
public interface RetryPolicy {
    /**
     * 재시도 로직을 Mono에 적용합니다.
     */
    <T> Mono<T> withRetry(Mono<T> source, String context);

    /**
     * 재시도 로직을 Flux에 적용합니다.
     */
    <T> Flux<T> withRetry(Flux<T> source, String context);
}
