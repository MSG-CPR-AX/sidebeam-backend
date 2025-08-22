package com.sidebeam.common.core.exception;

/**
 * 입력값 유효성 검사에서 발생하는 예외를 처리하기 위한 클래스입니다.
 * BusinessException을 상속하며, 다음과 같은 유효성 검사 문제들을 처리합니다:
 * 
 * - 잘못된 요청 형식
 * - 필수 파라미터 누락
 * - 입력값 형식 오류 (타입 불일치, 범위 초과 등)
 * - 스키마 유효성 검사 실패
 * - Bean Validation 오류 (@Valid, @NotNull 등)
 * - 요청 데이터 구조 오류
 */
public class ValidationException extends ApplicationException {

    public ValidationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ValidationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ValidationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public ValidationException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(Throwable cause) {
        super(cause);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}