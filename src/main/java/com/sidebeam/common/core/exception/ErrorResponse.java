package com.sidebeam.common.core.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.MDC;

import java.time.LocalDateTime;

/**
 * API 오류 응답을 위한 공통 응답 구조입니다.
 * 모든 예외 응답은 이 구조를 따라 일관된 형태로 반환됩니다.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    private String code;
    private String message;
    private int status;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private String path;
    private String details;
    private String correlationId;
    
    /**
     * 기본 생성자
     */
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
        this.correlationId = MDC.get("correlationId");
    }
    
    /**
     * ErrorCode를 사용하여 ErrorResponse를 생성합니다.
     * 
     * @param errorCode 오류 코드
     */
    public ErrorResponse(ErrorCode errorCode) {
        this();
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.status = errorCode.getStatus();
    }
    
    /**
     * ErrorCode와 경로를 사용하여 ErrorResponse를 생성합니다.
     * 
     * @param errorCode 오류 코드
     * @param path 요청 경로
     */
    public ErrorResponse(ErrorCode errorCode, String path) {
        this(errorCode);
        this.path = path;
    }
    
    /**
     * ErrorCode, 경로, 상세 정보를 사용하여 ErrorResponse를 생성합니다.
     * 
     * @param errorCode 오류 코드
     * @param path 요청 경로
     * @param details 상세 오류 정보
     */
    public ErrorResponse(ErrorCode errorCode, String path, String details) {
        this(errorCode, path);
        this.details = details;
    }
    
    /**
     * 모든 필드를 사용하여 ErrorResponse를 생성합니다.
     * 
     * @param code 오류 코드
     * @param message 오류 메시지
     * @param status HTTP 상태 코드
     * @param path 요청 경로
     */
    public ErrorResponse(String code, String message, int status, String path) {
        this();
        this.code = code;
        this.message = message;
        this.status = status;
        this.path = path;
    }
    
    /**
     * 정적 팩토리 메서드: ErrorCode로부터 ErrorResponse 생성
     * 
     * @param errorCode 오류 코드
     * @return ErrorResponse 인스턴스
     */
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode);
    }
    
    /**
     * 정적 팩토리 메서드: ErrorCode와 경로로부터 ErrorResponse 생성
     * 
     * @param errorCode 오류 코드
     * @param path 요청 경로
     * @return ErrorResponse 인스턴스
     */
    public static ErrorResponse of(ErrorCode errorCode, String path) {
        return new ErrorResponse(errorCode, path);
    }
    
    /**
     * 정적 팩토리 메서드: ErrorCode, 경로, 상세 정보로부터 ErrorResponse 생성
     * 
     * @param errorCode 오류 코드
     * @param path 요청 경로
     * @param details 상세 오류 정보
     * @return ErrorResponse 인스턴스
     */
    public static ErrorResponse of(ErrorCode errorCode, String path, String details) {
        return new ErrorResponse(errorCode, path, details);
    }
    
    // Getters and Setters
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public int getStatus() {
        return status;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
}