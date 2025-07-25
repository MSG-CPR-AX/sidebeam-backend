package com.sidebeam.common.core.exception;

/**
 * 시스템 레벨에서 발생하는 예외를 처리하기 위한 커스텀 예외 클래스입니다.
 * SysException을 대체하며, ErrorCode를 포함하여 일관된 오류 처리를 제공합니다.
 * 주로 외부 시스템 연동, 인프라 관련 오류 등에 사용됩니다.
 */
public class SystemException extends RuntimeException {
    
    private final ErrorCode errorCode;
    
    /**
     * ErrorCode를 사용하여 SystemException을 생성합니다.
     * 
     * @param errorCode 오류 코드
     */
    public SystemException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    
    /**
     * ErrorCode와 추가 메시지를 사용하여 SystemException을 생성합니다.
     * 
     * @param errorCode 오류 코드
     * @param message 추가 오류 메시지
     */
    public SystemException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * ErrorCode와 원인 예외를 사용하여 SystemException을 생성합니다.
     * 
     * @param errorCode 오류 코드
     * @param cause 원인 예외
     */
    public SystemException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
    
    /**
     * ErrorCode, 추가 메시지, 원인 예외를 사용하여 SystemException을 생성합니다.
     * 
     * @param errorCode 오류 코드
     * @param message 추가 오류 메시지
     * @param cause 원인 예외
     */
    public SystemException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    /**
     * 기존 SysException과의 호환성을 위한 생성자
     * 
     * @param message 오류 메시지
     */
    public SystemException(String message) {
        super(message);
        this.errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
    }
    
    /**
     * 기존 SysException과의 호환성을 위한 생성자
     * 
     * @param cause 원인 예외
     */
    public SystemException(Throwable cause) {
        super(cause);
        this.errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
    }
    
    /**
     * 기존 SysException과의 호환성을 위한 생성자
     * 
     * @param message 오류 메시지
     * @param cause 원인 예외
     */
    public SystemException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
    }
    
    /**
     * 오류 코드를 반환합니다.
     * 
     * @return ErrorCode
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}