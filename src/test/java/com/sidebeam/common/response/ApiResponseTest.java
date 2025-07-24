package com.sidebeam.common.response;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ApiResponse 클래스의 기능을 테스트합니다.
 */
class ApiResponseTest {

    @Test
    void testSuccessWithData() {
        // Given
        String testData = "test data";

        // When
        ApiResponse<String> response = ApiResponse.success(testData);

        // Then
        assertTrue(response.getSuccess());
        assertEquals("200", response.getCode());
        assertEquals("요청이 성공적으로 처리되었습니다.", response.getMessage());
        assertEquals(testData, response.getData());
    }

    @Test
    void testSuccessWithDataAndMessage() {
        // Given
        String testData = "test data";
        String customMessage = "커스텀 메시지";

        // When
        ApiResponse<String> response = ApiResponse.success(testData, customMessage);

        // Then
        assertTrue(response.getSuccess());
        assertEquals("200", response.getCode());
        assertEquals(customMessage, response.getMessage());
        assertEquals(testData, response.getData());
    }

    @Test
    void testSuccessWithoutData() {
        // When
        ApiResponse<Void> response = ApiResponse.success();

        // Then
        assertTrue(response.getSuccess());
        assertEquals("200", response.getCode());
        assertEquals("요청이 성공적으로 처리되었습니다.", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testFailure() {
        // Given
        String errorCode = "400";
        String errorMessage = "잘못된 요청입니다.";

        // When
        ApiResponse<Void> response = ApiResponse.failure(errorCode, errorMessage);

        // Then
        assertFalse(response.getSuccess());
        assertEquals(errorCode, response.getCode());
        assertEquals(errorMessage, response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testIsApiResponse() {
        // Given
        ApiResponse<String> apiResponse = ApiResponse.success("test");
        String normalString = "test";

        // When & Then
        assertTrue(ApiResponse.isApiResponse(apiResponse));
        assertFalse(ApiResponse.isApiResponse(normalString));
        assertFalse(ApiResponse.isApiResponse(null));
    }
}
