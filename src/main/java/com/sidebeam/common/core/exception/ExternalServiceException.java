package com.sidebeam.common.core.exception;

/**
 * 외부 서비스 연동 중 발생하는 예외를 표현합니다.
 * 예: 타임아웃, 5xx 응답, 네트워크 오류, 계약 위반 등.
 * ErrorCode.EXTERNAL_SERVICE_ERROR 및 관련 코드를 활용합니다.
 */
public class ExternalServiceException extends ApplicationException {

    public ExternalServiceException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ExternalServiceException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ExternalServiceException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public ExternalServiceException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    // Backward-compat convenience constructors
    public ExternalServiceException(String message) {
        super(message);
    }

    public ExternalServiceException(Throwable cause) {
        super(cause);
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
