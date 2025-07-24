package com.sidebeam.common.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.io.InputStream;

/**
 * 전역 응답 처리를 위한 ResponseBodyAdvice 구현 클래스입니다.
 * 
 * 모든 REST API의 정상 응답을 일관된 ApiResponse 구조로 자동 래핑합니다.
 * Controller에서는 순수 데이터 객체만 반환해도 공통 포맷으로 자동 변환됩니다.
 * 
 * 제외 대상:
 * - 이미 ApiResponse로 래핑된 응답
 * - ResponseEntity 타입의 응답
 * - byte[] 타입의 응답 (파일 다운로드 등)
 * - InputStream 타입의 응답 (스트리밍 등)
 * - Resource 타입의 응답 (정적 리소스 등)
 * - String 타입이면서 JSON이 아닌 응답 (단순 텍스트 응답)
 * 
 * 이 클래스는 정상 응답만 처리하며, 예외 응답은 GlobalExceptionHandler에서 별도 처리됩니다.
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;

    /**
     * 응답 처리 여부를 결정합니다.
     * 
     * @param returnType 컨트롤러 메서드의 반환 타입
     * @param converterType 사용될 HttpMessageConverter 타입
     * @return 응답을 처리할지 여부 (true: 처리함, false: 처리하지 않음)
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 이미 ApiResponse로 래핑된 경우 제외
        if (returnType.getParameterType().equals(ApiResponse.class)) {
            return false;
        }

        // ResponseEntity 타입 제외 (이미 HTTP 응답 구조를 가지고 있음)
        if (ResponseEntity.class.isAssignableFrom(returnType.getParameterType())) {
            return false;
        }

        // String 타입 제외 (Spring의 StringHttpMessageConverter와 충돌 방지)
        if (String.class.equals(returnType.getParameterType())) {
            return false;
        }

        // 바이너리 데이터 타입들 제외
        return !(byte[].class.equals(returnType.getParameterType()) ||
                InputStream.class.isAssignableFrom(returnType.getParameterType()) ||
                Resource.class.isAssignableFrom(returnType.getParameterType()));
    }

    /**
     * 응답 본문을 ApiResponse로 래핑합니다.
     * 
     * @param body 원본 응답 본문
     * @param returnType 컨트롤러 메서드의 반환 타입
     * @param selectedContentType 선택된 Content-Type
     * @param selectedConverterType 선택된 HttpMessageConverter 타입
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @return 래핑된 응답 객체
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                ServerHttpRequest request, ServerHttpResponse response) {

        // 이미 ApiResponse로 래핑된 경우 그대로 반환
        if (ApiResponse.isApiResponse(body)) {
            return body;
        }

        // null 응답 처리
        if (body == null) {
            return ApiResponse.success();
        }

        // String 타입 응답 특별 처리
        if (body instanceof String stringBody) {
            // JSON 형태의 문자열인지 확인
            if (isJsonString(stringBody)) {
                // JSON 문자열은 래핑하지 않고 그대로 반환 (이미 구조화된 응답일 가능성)
                return body;
            }

            // 단순 문자열은 ApiResponse로 래핑
            // String을 직접 JSON으로 변환하지 않고 ApiResponse 객체로 반환
            return ApiResponse.success(stringBody, "요청이 성공적으로 처리되었습니다.");
        }

        // 일반 객체는 ApiResponse로 래핑
        return ApiResponse.success(body);
    }

    /**
     * 문자열이 JSON 형태인지 확인합니다.
     * 
     * @param str 확인할 문자열
     * @return JSON 형태이면 true, 아니면 false
     */
    private boolean isJsonString(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }

        String trimmed = str.trim();
        return (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
               (trimmed.startsWith("[") && trimmed.endsWith("]"));
    }
}
