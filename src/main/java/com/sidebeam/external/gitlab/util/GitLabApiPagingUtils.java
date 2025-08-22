package com.sidebeam.external.gitlab.util;

import com.sidebeam.common.core.exception.TechnicalException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Slf4j
public class GitLabApiPagingUtils {

    private GitLabApiPagingUtils() {}

    private static final String X_NEXT_PAGE_HEADER = "X-Next-Page";

    /**
     * GitLab 스타일의 페이지 기반 API 순회를 통해 전체 데이터를 Flux<T>로 평탄화해서 반환합니다.
     */
    public static <T> Flux<T> paginateAll(Function<Integer, Mono<ResponseEntity<List<T>>>> pageFetcher) {

        // 무한 루프 방지를 위한 페이지 카운트
        AtomicInteger lastPage = new AtomicInteger(0);

        return Mono.just(1) // GitLab은 page=1부터 시작
                .expand(page -> pageFetcher.apply(page)
                        .<Integer>handle((response, sink) -> {
                            String nextPage = response.getHeaders().getFirst(X_NEXT_PAGE_HEADER);

                            if (StringUtils.isEmpty(nextPage)) {
                                sink.complete();
                                return;
                            }

                            int next = Integer.parseInt(nextPage);
                            if (next <= lastPage.get()) {
                                sink.error(new IllegalStateException("Infinite loop detected: page regress"));
                                return;
                            }

                            lastPage.set(next);
                            sink.next(next);
                        }).filter(Objects::nonNull))
                .flatMap(pageFetcher)
                .flatMap(response -> {
                    String nextPage = response.getHeaders().getFirst(X_NEXT_PAGE_HEADER);

                    // 예외 처리: body가 빈 리스트인데 X-Next-Page가 존재하는 경우
                    if (StringUtils.isEmpty(nextPage) && CollectionUtils.isEmpty(response.getBody())) return Mono.empty();

                    List<T> body = response.getBody();
                    // 예외 처리: body가 null이거나 빈 리스트인데 X-Next-Page가 존재하는 경우
                    if (StringUtils.isNotEmpty(nextPage) && CollectionUtils.isEmpty(body))
                        return Mono.error(new TechnicalException(
                            "Response body is empty but X-Next-Page header exists: " + nextPage));
                    
                    return Mono.justOrEmpty(body).flatMapMany(Flux::fromIterable);
                });
    }

    // 재사용 가능한 페이징 유틸리티
    public static <T> Flux<ResponseEntity<List<T>>> paginate(Function<Integer, Mono<ResponseEntity<List<T>>>> pageRequest) {
        return Mono.just(1)
                .expand(page -> pageRequest.apply(page)
                        .map(response -> {
                            String nextPage = response.getHeaders().getFirst(X_NEXT_PAGE_HEADER);
                            return nextPage != null && !nextPage.isEmpty() ?
                                    Integer.parseInt(nextPage) : null;
                        })
                        .filter(Objects::nonNull))
                .flatMap(pageRequest);
    }
}
