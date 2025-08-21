package com.sidebeam.common.rest.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

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
     */
    @Override
    public boolean supports(MethodParameter returnType, @NotNull Class<? extends HttpMessageConverter<?>> converterType) {

        // 예외 처리기에서 반환되는 응답은 제외
        if (returnType.hasMethodAnnotation(ExceptionHandler.class)) {
            return false;
        }

        // ApiResponse 또는 그 하위 타입은 제외
        if (ApiResponse.class.isAssignableFrom(returnType.getParameterType())) {
            return false;
        }

        // ResponseEntity 타입 제외 (이미 HTTP 응답 구조를 가지고 있음)
        if (ResponseEntity.class.isAssignableFrom(returnType.getParameterType())) {
            return false;
        }

        // 스트리밍/이벤트 응답 제외
        Class<?> pt = returnType.getParameterType();
        if (byte[].class.equals(pt) || InputStream.class.isAssignableFrom(pt) || Resource.class.isAssignableFrom(pt) ||
                StreamingResponseBody.class.isAssignableFrom(pt) || ResponseBodyEmitter.class.isAssignableFrom(pt) ||
                SseEmitter.class.isAssignableFrom(pt)) {
            return false;
        }

        // String은 기본적으로 제외 (Object 반환 시 런타임 String은 beforeBodyWrite에서 추가 방어)
        return !String.class.equals(pt);
    }

    /**
     * 응답 본문을 ApiResponse로 래핑합니다.
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                ServerHttpRequest request, ServerHttpResponse response) {

        // 204 No Content 또는 HEAD 요청은 본문 생성 금지
        HttpStatus status = response instanceof ServletServerHttpResponse servletResp ?
                HttpStatus.resolve(servletResp.getServletResponse().getStatus()) : null;

        boolean noContent = (status == HttpStatus.NO_CONTENT);
        boolean headMethod = "HEAD".equalsIgnoreCase(request.getMethod().name());

        if (noContent || headMethod) {
            return body;
        }

        // 이미 ApiResponse로 래핑된 경우 그대로 반환
        if (ApiResponse.isApiResponse(body)) {
            return body;
        }

// 메시지 컨버터/미디어 타입에 따른 방어 로직
        boolean jsonLike = MediaType.APPLICATION_JSON.includes(selectedContentType)
                || MimeTypeUtils.APPLICATION_JSON_VALUE.equals(selectedContentType.toString());

        // String 컨버터가 선택된 경우: 래핑하지 않음 (타입 불일치 방지)
        if (StringHttpMessageConverter.class.isAssignableFrom(selectedConverterType)) {
            return body;
        }

        // null 응답: JSON으로 협상된 경우에만 성공 래핑
        if (body == null) {
            return jsonLike ? ApiResponse.ok() : null;
        }

        // 런타임이 String일 때: JSON 협상이 아니면 래핑 금지
        if (body instanceof String s) {
            if (!jsonLike) {
                return s;   // text/plain 등은 래핑하지 않음
            }

            // 실제 JSON 여부를 안전하게 판단하고 싶다면 파싱 시도
            if (isParsableJson(s)) {
                return s; // 이미 구조화된 JSON 문자열로 간주
            }

            return ApiResponse.ok(s);
        }

        // 일반 객체: JSON 협상일 때만 래핑
        if (jsonLike) {
            return ApiResponse.ok(body);
        }

        return body;

    }

    /**
     * 문자열이 JSON 형태인지 확인합니다.
     */
    private boolean isParsableJson(String s) {
        if (s == null || s.isBlank()) return false;
        try {
            objectMapper.readTree(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
