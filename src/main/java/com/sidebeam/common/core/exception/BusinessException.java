package com.sidebeam.common.core.exception;

/**
 * 실제 비즈니스 도메인 로직에서 발생하는 예외를 처리하기 위한 클래스입니다.
 * BusinessException을 상속하며, 다음과 같은 비즈니스 로직 문제들을 처리합니다:
 * 
 * - 리소스를 찾을 수 없는 경우 (북마크, 사용자 등)
 * - 비즈니스 규칙 위반 (중복 데이터, 상태 불일치 등)
 * - 도메인 제약 조건 위반
 * - 인증/권한 관련 오류
 * - 비즈니스 프로세스 오류
 */
public class BusinessException extends ApplicationException {

    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(Throwable cause) {
        super(cause);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}