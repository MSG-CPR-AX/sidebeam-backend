package com.sidebeam.common.response;

import com.sidebeam.common.core.exception.ErrorCode;
import com.sidebeam.common.rest.response.ApiResponse;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ApiResponse 클래스의 기능을 테스트합니다. (안A형 스키마)
 */
class ApiResponseTest {

    @Test
    void testSuccessWithData() {
        // Given
        String testData = "test data";

        // When
        ApiResponse<String> response = ApiResponse.ok(testData);

        // Then
        assertTrue(response.getSuccess());
        assertNull(response.getError());
        assertNotNull(response.getTimestamp());
        assertEquals(testData, response.getData());
    }

    @Test
    void testSuccessWithoutData() {
        // When
        ApiResponse<Void> response = ApiResponse.ok();

        // Then
        assertTrue(response.getSuccess());
        assertNull(response.getError());
        assertNotNull(response.getTimestamp());
        assertNull(response.getData());
    }

    @Test
    void testError() {
        // Given
        Map<String, Object> details = Map.<String, Object>of("hint", "bad request");

        // When
        ApiResponse<Void> response = ApiResponse.error(ErrorCode.INVALID_REQUEST, null, details);

        // Then
        assertFalse(response.getSuccess());
        assertNotNull(response.getTimestamp());
        assertNull(response.getData());
        assertNotNull(response.getError());
        assertEquals("INVALID_REQUEST", response.getError().getCode());
        assertTrue(response.getError().getDetails().containsKey("hint"));
    }

    @Test
    void testIsApiResponse() {
        // Given
        ApiResponse<String> apiResponse = ApiResponse.ok("test");
        String normalString = "test";

        // When & Then
        assertTrue(ApiResponse.isApiResponse(apiResponse));
        assertFalse(ApiResponse.isApiResponse(normalString));
        assertFalse(ApiResponse.isApiResponse(null));
    }
}
