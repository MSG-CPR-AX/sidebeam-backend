package com.sidebeam.common.exception;

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

    /**
     * -- GETTER --
     *  오류 코드를 반환합니다.
     *
     * @return ErrorCode
     */
    private final ErrorCode errorCode;

    /**
     * ErrorCode를 사용하여 BusinessException을 생성합니다.
     * 
     * @param errorCode 오류 코드
     */
    public ApplicationException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * ErrorCode와 추가 메시지를 사용하여 BusinessException을 생성합니다.
     * 
     * @param errorCode 오류 코드
     * @param message 추가 오류 메시지
     */
    public ApplicationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * ErrorCode와 원인 예외를 사용하여 BusinessException을 생성합니다.
     * 
     * @param errorCode 오류 코드
     * @param cause 원인 예외
     */
    public ApplicationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    /**
     * ErrorCode, 추가 메시지, 원인 예외를 사용하여 BusinessException을 생성합니다.
     * 
     * @param errorCode 오류 코드
     * @param message 추가 오류 메시지
     * @param cause 원인 예외
     */
    public ApplicationException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 기존 WaffulException과의 호환성을 위한 생성자
     * 
     * @param message 오류 메시지
     */
    public ApplicationException(String message) {
        super(message);
        this.errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
    }

    /**
     * 기존 WaffulException과의 호환성을 위한 생성자
     * 
     * @param cause 원인 예외
     */
    public ApplicationException(Throwable cause) {
        super(cause);
        this.errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
    }

    /**
     * 기존 WaffulException과의 호환성을 위한 생성자
     * 
     * @param message 오류 메시지
     * @param cause 원인 예외
     */
    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
    }

}
