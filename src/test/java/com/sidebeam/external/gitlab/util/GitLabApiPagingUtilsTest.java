package com.sidebeam.external.gitlab.util;

import com.sidebeam.common.core.exception.TechnicalException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class GitLabApiPagingUtilsTest {

    private static final String X_NEXT_PAGE_HEADER = "X-Next-Page";

    @Test
    void paginateAll_normalPagination_shouldReturnAllItems() {
        // Given: 3페이지에 걸친 데이터
        Function<Integer, Mono<ResponseEntity<List<String>>>> pageFetcher = page -> {
            HttpHeaders headers = new HttpHeaders();
            List<String> body;
            
            switch (page) {
                case 1:
                    headers.add(X_NEXT_PAGE_HEADER, "2");
                    body = Arrays.asList("item1", "item2");
                    break;
                case 2:
                    headers.add(X_NEXT_PAGE_HEADER, "3");
                    body = Arrays.asList("item3", "item4");
                    break;
                case 3:
                    // 마지막 페이지 - X-Next-Page 헤더 없음
                    body = Arrays.asList("item5");
                    break;
                default:
                    body = Collections.emptyList();
            }
            
            return Mono.just(ResponseEntity.ok().headers(headers).body(body));
        };

        // When
        List<String> result = GitLabApiPagingUtils.paginateAll(pageFetcher)
                .collectList()
                .block();

        // Then
        assertNotNull(result);
        assertEquals(Arrays.asList("item1", "item2", "item3", "item4", "item5"), result);
    }

    @Test
    void paginateAll_singlePage_shouldReturnAllItems() {
        // Given: 단일 페이지 데이터
        Function<Integer, Mono<ResponseEntity<List<String>>>> pageFetcher = page -> {
            if (page == 1) {
                List<String> body = Arrays.asList("item1", "item2", "item3");
                return Mono.just(ResponseEntity.ok().body(body));
            }
            return Mono.just(ResponseEntity.ok().body(Collections.emptyList()));
        };

        // When
        List<String> result = GitLabApiPagingUtils.paginateAll(pageFetcher)
                .collectList()
                .block();

        // Then
        assertNotNull(result);
        assertEquals(Arrays.asList("item1", "item2", "item3"), result);
    }

    @Test
    void paginateAll_emptyResponse_shouldCompleteEmpty() {
        // Given: 빈 응답
        Function<Integer, Mono<ResponseEntity<List<String>>>> pageFetcher = page -> 
            Mono.just(ResponseEntity.ok().body(Collections.emptyList()));

        // When
        List<String> result = GitLabApiPagingUtils.paginateAll(pageFetcher)
                .collectList()
                .block();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void paginateAll_nullBodyWithNextPageHeader_shouldThrowTechnicalException() {
        // Given: body가 null이지만 X-Next-Page 헤더가 존재
        Function<Integer, Mono<ResponseEntity<List<String>>>> pageFetcher = page -> {
            HttpHeaders headers = new HttpHeaders();
            headers.add(X_NEXT_PAGE_HEADER, "2");
            return Mono.just(ResponseEntity.ok().headers(headers).body(null));
        };

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class, () -> {
            GitLabApiPagingUtils.paginateAll(pageFetcher)
                    .collectList()
                    .block();
        });
        
        assertTrue(exception.getMessage().contains("Response body is empty"));
    }

    @Test
    void paginateAll_emptyBodyWithNextPageHeader_shouldThrowTechnicalException() {
        // Given: body가 빈 리스트이지만 X-Next-Page 헤더가 존재
        Function<Integer, Mono<ResponseEntity<List<String>>>> pageFetcher = page -> {
            HttpHeaders headers = new HttpHeaders();
            headers.add(X_NEXT_PAGE_HEADER, "2");
            return Mono.just(ResponseEntity.ok().headers(headers).body(Collections.emptyList()));
        };

        // When & Then
        TechnicalException exception = assertThrows(TechnicalException.class, () -> {
            GitLabApiPagingUtils.paginateAll(pageFetcher)
                    .collectList()
                    .block();
        });
        
        assertTrue(exception.getMessage().contains("Response body is empty but X-Next-Page header exists: 2"));
    }

    @Test
    void paginateAll_nullBodyWithoutNextPageHeader_shouldCompleteEmpty() {
        // Given: body가 null이지만 X-Next-Page 헤더가 없음 (정상 종료 조건)
        Function<Integer, Mono<ResponseEntity<List<String>>>> pageFetcher = page -> 
            Mono.just(ResponseEntity.ok().body(null));

        // When
        List<String> result = GitLabApiPagingUtils.paginateAll(pageFetcher)
                .collectList()
                .block();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void paginateAll_emptyBodyWithoutNextPageHeader_shouldCompleteEmpty() {
        // Given: body가 빈 리스트이고 X-Next-Page 헤더가 없음 (정상 종료 조건)
        Function<Integer, Mono<ResponseEntity<List<String>>>> pageFetcher = page -> 
            Mono.just(ResponseEntity.ok().body(Collections.emptyList()));

        // When
        List<String> result = GitLabApiPagingUtils.paginateAll(pageFetcher)
                .collectList()
                .block();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void paginateAll_emptyNextPageHeader_shouldNotThrowException() {
        // Given: X-Next-Page 헤더가 빈 문자열인 경우
        Function<Integer, Mono<ResponseEntity<List<String>>>> pageFetcher = page -> {
            HttpHeaders headers = new HttpHeaders();
            headers.add(X_NEXT_PAGE_HEADER, "");
            return Mono.just(ResponseEntity.ok().headers(headers).body(Collections.emptyList()));
        };

        // When
        List<String> result = GitLabApiPagingUtils.paginateAll(pageFetcher)
                .collectList()
                .block();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}