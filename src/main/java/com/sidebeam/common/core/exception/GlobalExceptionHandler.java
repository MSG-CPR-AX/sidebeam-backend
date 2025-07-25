package com.sidebeam.common.core.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 전역 예외 처리기입니다.
 * 애플리케이션에서 발생하는 모든 예외를 일관된 형태로 처리하여 클라이언트에게 반환합니다.
 * Swagger 및 Spring RestDocs와 연동 가능한 구조로 설계되었습니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * TechnicalException 처리
     * 내부 코드 문제로 인한 기술적 예외를 처리합니다.
     */
    @ExceptionHandler(TechnicalException.class)
    public ResponseEntity<ErrorResponse> handleTechnicalException(
            TechnicalException ex, HttpServletRequest request) {

        log.error("Technical exception occurred: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.of(
            ex.getErrorCode(), 
            request.getRequestURI(),
            ex.getMessage()
        );

        return ResponseEntity
            .status(ex.getErrorCode().getHttpStatus())
            .body(errorResponse);
    }

    /**
     * BusinessException 처리
     * 비즈니스 도메인 로직 예외를 처리합니다.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(
            BusinessException ex, HttpServletRequest request) {

        log.warn("Domain exception occurred: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.of(
            ex.getErrorCode(), 
            request.getRequestURI(),
            ex.getMessage()
        );

        return ResponseEntity
            .status(ex.getErrorCode().getHttpStatus())
            .body(errorResponse);
    }

    /**
     * ValidationException 처리
     * 입력값 유효성 검사 예외를 처리합니다.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, HttpServletRequest request) {

        log.warn("Validation exception occurred: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.of(
            ex.getErrorCode(), 
            request.getRequestURI(),
            ex.getMessage()
        );

        return ResponseEntity
            .status(ex.getErrorCode().getHttpStatus())
            .body(errorResponse);
    }

    /**
     * SystemException 처리
     * 시스템 레벨에서 발생하는 예외를 처리합니다.
     */
    @ExceptionHandler(SystemException.class)
    public ResponseEntity<ErrorResponse> handleSystemException(
            SystemException ex, HttpServletRequest request) {

        log.error("System exception occurred: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.of(
            ex.getErrorCode(), 
            request.getRequestURI(),
            ex.getMessage()
        );

        return ResponseEntity
            .status(ex.getErrorCode().getHttpStatus())
            .body(errorResponse);
    }

    /**
     * ApplicationException 처리
     * 비즈니스 로직에서 발생하는 예외를 처리합니다.
     */
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            ApplicationException ex, HttpServletRequest request) {

        log.warn("Business exception occurred: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.of(
                ex.getErrorCode(),
                request.getRequestURI(),
                ex.getMessage()
        );

        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus())
                .body(errorResponse);
    }

    /**
     * Validation 예외 처리 (@Valid 어노테이션 사용 시)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        log.warn("Validation exception occurred: {}", ex.getMessage());

        String details = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));

        ErrorResponse errorResponse = ErrorResponse.of(
            ErrorCode.VALIDATION_ERROR, 
            request.getRequestURI(),
            details
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }

    /**
     * Bind 예외 처리
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(
            BindException ex, HttpServletRequest request) {

        log.warn("Bind exception occurred: {}", ex.getMessage());

        String details = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));

        ErrorResponse errorResponse = ErrorResponse.of(
            ErrorCode.VALIDATION_ERROR, 
            request.getRequestURI(),
            details
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }

    /**
     * Constraint Violation 예외 처리
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {

        log.warn("Constraint violation exception occurred: {}", ex.getMessage());

        String details = ex.getConstraintViolations().stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining(", "));

        ErrorResponse errorResponse = ErrorResponse.of(
            ErrorCode.VALIDATION_ERROR, 
            request.getRequestURI(),
            details
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }

    /**
     * 필수 파라미터 누락 예외 처리
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        log.warn("Missing parameter exception occurred: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
            ErrorCode.MISSING_REQUIRED_PARAMETER, 
            request.getRequestURI(),
            "Required parameter '" + ex.getParameterName() + "' is missing"
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }

    /**
     * 메서드 인자 타입 불일치 예외 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        log.warn("Method argument type mismatch exception occurred: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
            ErrorCode.INVALID_REQUEST, 
            request.getRequestURI(),
            "Invalid parameter type for '" + ex.getName() + "'"
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }

    /**
     * HTTP 메서드 지원하지 않음 예외 처리
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        log.warn("HTTP method not supported exception occurred: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
            ErrorCode.INVALID_REQUEST, 
            request.getRequestURI(),
            "HTTP method '" + ex.getMethod() + "' is not supported"
        );

        return ResponseEntity
            .status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(errorResponse);
    }

    /**
     * HTTP 미디어 타입 지원하지 않음 예외 처리
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {

        log.warn("HTTP media type not supported exception occurred: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
            ErrorCode.INVALID_REQUEST, 
            request.getRequestURI(),
            "Media type '" + ex.getContentType() + "' is not supported"
        );

        return ResponseEntity
            .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .body(errorResponse);
    }

    /**
     * HTTP 메시지 읽기 불가 예외 처리
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("HTTP message not readable exception occurred: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
            ErrorCode.INVALID_REQUEST, 
            request.getRequestURI(),
            "Invalid request body format"
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }

    /**
     * 핸들러를 찾을 수 없음 예외 처리
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpServletRequest request) {

        log.warn("No handler found exception occurred: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
            ErrorCode.RESOURCE_NOT_FOUND, 
            request.getRequestURI(),
            "No handler found for " + ex.getHttpMethod() + " " + ex.getRequestURL()
        );

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(errorResponse);
    }

    /**
     * IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {

        log.warn("Illegal argument exception occurred: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
            ErrorCode.INVALID_REQUEST, 
            request.getRequestURI(),
            ex.getMessage()
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }

    /**
     * 모든 예외의 최종 처리기
     * 위에서 처리되지 않은 모든 예외를 처리합니다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected exception occurred: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.of(
            ErrorCode.INTERNAL_SERVER_ERROR, 
            request.getRequestURI(),
            "An unexpected error occurred"
        );

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorResponse);
    }
}
