package com.sidebeam.common.core.exception;

import lombok.Getter;

/**
 * 비즈니스 계층에서 발생하는 모든 예외의 최상위 추상 클래스입니다.
 * WaffulException을 대체하며, ErrorCode를 포함하여 일관된 오류 처리를 제공합니다.
 * 
 * 이 클래스는 다음과 같은 하위 예외 클래스들의 루트 역할을 합니다:
 * - TechnicalException: 내부 코드 문제 (NPE, 변환 실패, 인코딩/디코딩 실패 등)
 * - BusinessException: 실제 비즈니스 로직 오류
 * - ValidationException: 입력값 유효성 검사 오류
 */
@Getter
public class ApplicationException extends RuntimeException {

    private final ErrorCode errorCode;

    public ApplicationException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ApplicationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ApplicationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public ApplicationException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }


    public ApplicationException(String message) {
        super(message);
        this.errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
    }

    public ApplicationException(Throwable cause) {
        super(cause);
        this.errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
    }

    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
    }

}
