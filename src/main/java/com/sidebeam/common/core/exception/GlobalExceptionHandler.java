package com.sidebeam.common.core.exception;

import com.sidebeam.common.rest.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public ResponseEntity<ApiResponse<Void>> handleTechnicalException(
            TechnicalException ex, HttpServletRequest request) {

        log.error("Technical exception occurred: {}", ex.getMessage(), ex);

        Map<String, Object> details = new HashMap<>();
        details.put("path", request.getRequestURI());
        details.put("correlationId", MDC.get("correlationId"));
        details.put("hint", ex.getMessage());
        ApiResponse.ErrorInfo error = ApiResponse.ErrorInfo.of(ex.getErrorCode(), null, details);

        return ResponseEntity
            .status(ex.getErrorCode().getHttpStatus())
            .body(ApiResponse.error(error));
    }

    /**
     * BusinessException 처리
     * 비즈니스 도메인 로직 예외를 처리합니다.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainException(
            BusinessException ex, HttpServletRequest request) {

        log.warn("Domain exception occurred: {}", ex.getMessage(), ex);

        Map<String, Object> details = new HashMap<>();
        details.put("path", request.getRequestURI());
        details.put("correlationId", MDC.get("correlationId"));
        details.put("hint", ex.getMessage());
        ApiResponse.ErrorInfo error = ApiResponse.ErrorInfo.of(ex.getErrorCode(), null, details);

        return ResponseEntity
            .status(ex.getErrorCode().getHttpStatus())
            .body(ApiResponse.error(error));
    }

    /**
     * ValidationException 처리
     * 입력값 유효성 검사 예외를 처리합니다.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            ValidationException ex, HttpServletRequest request) {

        log.warn("Validation exception occurred: {}", ex.getMessage(), ex);

        Map<String, Object> details = new HashMap<>();
        details.put("path", request.getRequestURI());
        details.put("correlationId", MDC.get("correlationId"));
        details.put("hint", ex.getMessage());
        ApiResponse.ErrorInfo error = ApiResponse.ErrorInfo.of(ex.getErrorCode(), null, details);

        return ResponseEntity
            .status(ex.getErrorCode().getHttpStatus())
            .body(ApiResponse.error(error));
    }

    /**
     * SystemException 처리
     * 시스템 레벨에서 발생하는 예외를 처리합니다.
     */
    @ExceptionHandler(SystemException.class)
    public ResponseEntity<ApiResponse<Void>> handleSystemException(
            SystemException ex, HttpServletRequest request) {

        log.error("System exception occurred: {}", ex.getMessage(), ex);

        Map<String, Object> details = new HashMap<>();
        details.put("path", request.getRequestURI());
        details.put("correlationId", MDC.get("correlationId"));
        details.put("hint", ex.getMessage());
        ApiResponse.ErrorInfo error = ApiResponse.ErrorInfo.of(ex.getErrorCode(), null, details);

        return ResponseEntity
            .status(ex.getErrorCode().getHttpStatus())
            .body(ApiResponse.error(error));
    }

    /**
     * ApplicationException 처리
     * 비즈니스 로직에서 발생하는 예외를 처리합니다.
     */
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            ApplicationException ex, HttpServletRequest request) {

        log.warn("Business exception occurred: {}", ex.getMessage(), ex);

        Map<String, Object> details = new HashMap<>();
        details.put("path", request.getRequestURI());
        details.put("correlationId", MDC.get("correlationId"));
        details.put("hint", ex.getMessage());
        ApiResponse.ErrorInfo error = ApiResponse.ErrorInfo.of(ex.getErrorCode(), null, details);

        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus())
                .body(ApiResponse.error(error));
    }

    /**
     * Validation 예외 처리 (@Valid 어노테이션 사용 시)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        log.warn("Validation exception occurred: {}", ex.getMessage());

        List<Map<String, Object>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> {
                Map<String, Object> m = new HashMap<>();
                m.put("field", fe.getField());
                m.put("reason", fe.getDefaultMessage());
                return m;
            }).toList();

        Map<String, Object> details = new HashMap<>();
        details.put("fieldErrors", fieldErrors);
        details.put("path", request.getRequestURI());
        details.put("correlationId", MDC.get("correlationId"));

        ApiResponse.ErrorInfo error = ApiResponse.ErrorInfo.of(ErrorCode.VALIDATION_ERROR, null, details);

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(error));
    }

    /**
     * Bind 예외 처리
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(
            BindException ex, HttpServletRequest request) {

        log.warn("Bind exception occurred: {}", ex.getMessage());

        List<Map<String, Object>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> {
                Map<String, Object> m = new HashMap<>();
                m.put("field", fe.getField());
                m.put("reason", fe.getDefaultMessage());
                return m;
            }).toList();

        Map<String, Object> details = new HashMap<>();
        details.put("fieldErrors", fieldErrors);
        details.put("path", request.getRequestURI());
        details.put("correlationId", MDC.get("correlationId"));

        ApiResponse.ErrorInfo error = ApiResponse.ErrorInfo.of(ErrorCode.VALIDATION_ERROR, null, details);

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(error));
    }

    /**
     * Constraint Violation 예외 처리
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {

        log.warn("Constraint violation exception occurred: {}", ex.getMessage());

        List<String> violations = ex.getConstraintViolations().stream()
            .map(ConstraintViolation::getMessage)
            .toList();

        Map<String, Object> details = new HashMap<>();
        details.put("violations", violations);
        details.put("path", request.getRequestURI());
        details.put("correlationId", MDC.get("correlationId"));

        ApiResponse.ErrorInfo error = ApiResponse.ErrorInfo.of(ErrorCode.VALIDATION_ERROR, null, details);

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(error));
    }

    /**
     * 필수 파라미터 누락 예외 처리
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        log.warn("Missing parameter exception occurred: {}", ex.getMessage());

        Map<String, Object> details = new HashMap<>();
        details.put("path", request.getRequestURI());
        details.put("correlationId", MDC.get("correlationId"));
        details.put("hint", "Required parameter '" + ex.getParameterName() + "' is missing");
        ApiResponse.ErrorInfo error = ApiResponse.ErrorInfo.of(ErrorCode.MISSING_REQUIRED_PARAMETER, null, details);

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(error));
    }

    /**
     * 메서드 인자 타입 불일치 예외 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        log.warn("Method argument type mismatch exception occurred: {}", ex.getMessage());

        Map<String, Object> details = new HashMap<>();
        details.put("path", request.getRequestURI());
        details.put("correlationId", MDC.get("correlationId"));
        details.put("hint", "Invalid parameter type for '" + ex.getName() + "'");
        ApiResponse.ErrorInfo error = ApiResponse.ErrorInfo.of(ErrorCode.INVALID_REQUEST, null, details);

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(error));
    }

    /**
     * HTTP 메서드 지원하지 않음 예외 처리
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        log.warn("HTTP method not supported exception occurred: {}", ex.getMessage());

        Map<String, Object> details = new HashMap<>();
        details.put("path", request.getRequestURI());
        details.put("correlationId", MDC.get("correlationId"));
        details.put("hint", "HTTP method '" + ex.getMethod() + "' is not supported");
        ApiResponse.ErrorInfo error = ApiResponse.ErrorInfo.of(ErrorCode.INVALID_REQUEST, null, details);

        return ResponseEntity
            .status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(ApiResponse.error(error));
    }

    /**
     * HTTP 미디어 타입 지원하지 않음 예외 처리
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {

        log.warn("HTTP media type not supported exception occurred: {}", ex.getMessage());

        Map<String, Object> details = new HashMap<>();
        details.put("path", request.getRequestURI());
        details.put("correlationId", MDC.get("correlationId"));
        details.put("hint", "Media type '" + ex.getContentType() + "' is not supported");
        ApiResponse.ErrorInfo error = ApiResponse.ErrorInfo.of(ErrorCode.INVALID_REQUEST, null, details);

        return ResponseEntity
            .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .body(ApiResponse.error(error));
    }

    /**
     * HTTP 메시지 읽기 불가 예외 처리
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("HTTP message not readable exception occurred: {}", ex.getMessage());

        Map<String, Object> details = new HashMap<>();
        details.put("path", request.getRequestURI());
        details.put("correlationId", MDC.get("correlationId"));
        details.put("hint", "Invalid request body format");
        ApiResponse.ErrorInfo error = ApiResponse.ErrorInfo.of(ErrorCode.INVALID_REQUEST, null, details);

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(error));
    }

    /**
     * 핸들러를 찾을 수 없음 예외 처리
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpServletRequest request) {

        log.warn("No handler found exception occurred: {}", ex.getMessage());

        Map<String, Object> details = new HashMap<>();
        details.put("path", request.getRequestURI());
        details.put("correlationId", MDC.get("correlationId"));
        details.put("hint", "No handler found for " + ex.getHttpMethod() + " " + ex.getRequestURL());
        ApiResponse.ErrorInfo error = ApiResponse.ErrorInfo.of(ErrorCode.RESOURCE_NOT_FOUND, null, details);

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(error));
    }

    /**
     * IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {

        log.warn("Illegal argument exception occurred: {}", ex.getMessage());

        Map<String, Object> details = new HashMap<>();
        details.put("path", request.getRequestURI());
        details.put("correlationId", MDC.get("correlationId"));
        details.put("hint", ex.getMessage());
        ApiResponse.ErrorInfo error = ApiResponse.ErrorInfo.of(ErrorCode.INVALID_REQUEST, null, details);

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(error));
    }

    /**
     * 모든 예외의 최종 처리기
     * 위에서 처리되지 않은 모든 예외를 처리합니다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected exception occurred: {}", ex.getMessage(), ex);

        Map<String, Object> details = new HashMap<>();
        details.put("path", request.getRequestURI());
        details.put("correlationId", MDC.get("correlationId"));
        details.put("hint", "An unexpected error occurred");
        ApiResponse.ErrorInfo error = ApiResponse.ErrorInfo.of(ErrorCode.INTERNAL_SERVER_ERROR, null, details);

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(error));
    }
}
