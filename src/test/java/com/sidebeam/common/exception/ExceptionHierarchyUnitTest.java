package com.sidebeam.common.exception;

import com.sidebeam.common.core.exception.*;
import com.sidebeam.common.rest.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 새로운 예외 계층 구조의 단위 테스트
 */
class ExceptionHierarchyUnitTest {

    private GlobalExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/test/endpoint");
    }

    /**
     * TechnicalException 생성 및 처리 테스트
     */
    @Test
    void testTechnicalExceptionCreation() {
        // Given
        TechnicalException exception = new TechnicalException(ErrorCode.PROPERTY_CONVERSION_ERROR);
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleTechnicalException(exception, request);
        
        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PROPERTY_CONVERSION_ERROR", response.getBody().getError().getCode());
        assertEquals("프로퍼티 변환 중 오류가 발생했습니다.", response.getBody().getError().getMessage());
    }

    /**
     * TechnicalException with custom message 테스트
     */
    @Test
    void testTechnicalExceptionWithCustomMessage() {
        // Given
        String customMessage = "Custom technical error message";
        TechnicalException exception = new TechnicalException(ErrorCode.DATA_PARSING_ERROR, customMessage);
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleTechnicalException(exception, request);
        
        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("DATA_PARSING_ERROR", response.getBody().getError().getCode());
        assertTrue(response.getBody().getError().getDetails().containsValue(customMessage));
    }

    /**
     * BusinessException 생성 및 처리 테스트
     */
    @Test
    void testDomainExceptionCreation() {
        // Given
        BusinessException exception = new BusinessException(ErrorCode.BOOKMARK_NOT_FOUND);
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleDomainException(exception, request);
        
        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("BOOKMARK_NOT_FOUND", response.getBody().getError().getCode());
        assertEquals("북마크를 찾을 수 없습니다.", response.getBody().getError().getMessage());
    }

    /**
     * BusinessException with business rule violation 테스트
     */
    @Test
    void testDomainExceptionBusinessRule() {
        // Given
        BusinessException exception = new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION);
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleDomainException(exception, request);
        
        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("BUSINESS_RULE_VIOLATION", response.getBody().getError().getCode());
        assertEquals("비즈니스 규칙 위반입니다.", response.getBody().getError().getMessage());
    }

    /**
     * ValidationException 생성 및 처리 테스트
     */
    @Test
    void testValidationExceptionCreation() {
        // Given
        ValidationException exception = new ValidationException(ErrorCode.INVALID_PARAMETER_FORMAT);
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleValidationException(exception, request);
        
        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_PARAMETER_FORMAT", response.getBody().getError().getCode());
        assertEquals("파라미터 형식이 올바르지 않습니다.", response.getBody().getError().getMessage());
    }

    /**
     * ValidationException with parameter out of range 테스트
     */
    @Test
    void testValidationExceptionParameterRange() {
        // Given
        ValidationException exception = new ValidationException(ErrorCode.PARAMETER_OUT_OF_RANGE);
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleValidationException(exception, request);
        
        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PARAMETER_OUT_OF_RANGE", response.getBody().getError().getCode());
        assertEquals("파라미터 값이 허용 범위를 벗어났습니다.", response.getBody().getError().getMessage());
    }

    /**
     * SystemException 처리 테스트 (기존 기능 유지 확인)
     */
    @Test
    void testSystemExceptionHandling() {
        // Given
        SystemException exception = new SystemException(ErrorCode.GITLAB_CONNECTION_ERROR);
        
        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleSystemException(exception, request);
        
        // Then
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("GITLAB_CONNECTION_ERROR", response.getBody().getError().getCode());
        assertEquals("GitLab 연결 중 오류가 발생했습니다.", response.getBody().getError().getMessage());
    }

    /**
     * 예외 계층 구조 확인 테스트
     */
    @Test
    void testExceptionHierarchy() {
        // Given
        TechnicalException technicalException = new TechnicalException(ErrorCode.PROPERTY_CONVERSION_ERROR);
        BusinessException businessException = new BusinessException(ErrorCode.BOOKMARK_NOT_FOUND);
        ValidationException validationException = new ValidationException(ErrorCode.INVALID_PARAMETER_FORMAT);
        
        // Then - 모든 예외가 BusinessException을 상속하는지 확인
        assertTrue(technicalException instanceof ApplicationException);
        assertTrue(businessException instanceof ApplicationException);
        assertTrue(validationException instanceof ApplicationException);
        
        // BusinessException은 RuntimeException을 상속
        assertTrue(technicalException instanceof RuntimeException);
        assertTrue(businessException instanceof RuntimeException);
        assertTrue(validationException instanceof RuntimeException);
    }

    /**
     * ErrorCode 접근 테스트
     */
    @Test
    void testErrorCodeAccess() {
        // Given
        TechnicalException technicalException = new TechnicalException(ErrorCode.PROPERTY_CONVERSION_ERROR);
        BusinessException businessException = new BusinessException(ErrorCode.BOOKMARK_NOT_FOUND);
        ValidationException validationException = new ValidationException(ErrorCode.INVALID_PARAMETER_FORMAT);
        
        // Then
        assertEquals(ErrorCode.PROPERTY_CONVERSION_ERROR, technicalException.getErrorCode());
        assertEquals(ErrorCode.BOOKMARK_NOT_FOUND, businessException.getErrorCode());
        assertEquals(ErrorCode.INVALID_PARAMETER_FORMAT, validationException.getErrorCode());
    }

    /**
     * 기존 호환성 생성자 테스트
     */
    @Test
    void testLegacyCompatibilityConstructors() {
        // Given
        String message = "Legacy error message";
        RuntimeException cause = new RuntimeException("Cause");
        
        // When
        TechnicalException technicalException = new TechnicalException(message);
        BusinessException businessException = new BusinessException(cause);
        ValidationException validationException = new ValidationException(message, cause);
        
        // Then
        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, technicalException.getErrorCode());
        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, businessException.getErrorCode());
        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, validationException.getErrorCode());
        
        assertEquals(message, technicalException.getMessage());
        assertEquals(cause, businessException.getCause());
        assertEquals(message, validationException.getMessage());
        assertEquals(cause, validationException.getCause());
    }
}