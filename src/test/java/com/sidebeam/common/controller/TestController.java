package com.sidebeam.common.controller;

import com.sidebeam.common.rest.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * ResponseBodyAdvice 동작을 테스트하기 위한 컨트롤러입니다.
 * 
 * 이 컨트롤러는 다양한 타입의 응답을 반환하여 GlobalResponseBodyAdvice가
 * 올바르게 동작하는지 확인하는 용도로 사용됩니다.
 */
@Slf4j
@Tag(name = "Test", description = "API for testing ResponseBodyAdvice")
@RequestMapping("/test")
@RestController
public class TestController {

    /**
     * 순수 문자열을 반환합니다.
     * GlobalResponseBodyAdvice에 의해 ApiResponse로 래핑되어야 합니다.
     */
    @GetMapping("/string")
    @Operation(summary = "Test string response", description = "Returns a plain string that should be wrapped in ApiResponse")
    public String getString() {
        log.info("REST request to get string");
        return "Hello, World!";
    }

    /**
     * 순수 객체를 반환합니다.
     * GlobalResponseBodyAdvice에 의해 ApiResponse로 래핑되어야 합니다.
     */
    @GetMapping("/object")
    @Operation(summary = "Test object response", description = "Returns a plain object that should be wrapped in ApiResponse")
    public Map<String, Object> getObject() {
        log.info("REST request to get object");
        return Map.of(
            "id", 1,
            "name", "Test Object",
            "active", true
        );
    }

    /**
     * 리스트를 반환합니다.
     * GlobalResponseBodyAdvice에 의해 ApiResponse로 래핑되어야 합니다.
     */
    @GetMapping("/list")
    @Operation(summary = "Test list response", description = "Returns a list that should be wrapped in ApiResponse")
    public List<String> getList() {
        log.info("REST request to get list");
        return List.of("item1", "item2", "item3");
    }

    /**
     * 이미 ApiResponse로 래핑된 응답을 반환합니다.
     * GlobalResponseBodyAdvice에 의해 재래핑되지 않아야 합니다.
     */
    @GetMapping("/api-response")
    @Operation(summary = "Test ApiResponse", description = "Returns an already wrapped ApiResponse that should not be re-wrapped")
    public ApiResponse<String> getApiResponse() {
        log.info("REST request to get ApiResponse");
        return ApiResponse.ok("Already wrapped response");
    }

    /**
     * null을 반환합니다.
     * GlobalResponseBodyAdvice에 의해 빈 성공 응답으로 래핑되어야 합니다.
     */
    @GetMapping("/null")
    @Operation(summary = "Test null response", description = "Returns null that should be wrapped as empty success response")
    public Object getNull() {
        log.info("REST request to get null");
        return null;
    }

    /**
     * JSON 형태의 문자열을 반환합니다.
     * GlobalResponseBodyAdvice에서 JSON으로 인식하여 래핑하지 않을 수 있습니다.
     */
    @GetMapping("/json-string")
    @Operation(summary = "Test JSON string response", description = "Returns a JSON-formatted string")
    public String getJsonString() {
        log.info("REST request to get JSON string");
        return "{\"message\": \"This is a JSON string\", \"status\": \"success\"}";
    }
}