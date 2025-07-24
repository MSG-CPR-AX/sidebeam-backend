package com.sidebeam.common.exception;

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
    
    /**
     * ErrorCode를 사용하여 DomainException을 생성합니다.
     * 
     * @param errorCode 오류 코드
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    /**
     * ErrorCode와 추가 메시지를 사용하여 DomainException을 생성합니다.
     * 
     * @param errorCode 오류 코드
     * @param message 추가 오류 메시지
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    /**
     * ErrorCode와 원인 예외를 사용하여 DomainException을 생성합니다.
     * 
     * @param errorCode 오류 코드
     * @param cause 원인 예외
     */
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
    
    /**
     * ErrorCode, 추가 메시지, 원인 예외를 사용하여 DomainException을 생성합니다.
     * 
     * @param errorCode 오류 코드
     * @param message 추가 오류 메시지
     * @param cause 원인 예외
     */
    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
    
    /**
     * 기존 코드와의 호환성을 위한 생성자
     * 
     * @param message 오류 메시지
     */
    public BusinessException(String message) {
        super(message);
    }
    
    /**
     * 기존 코드와의 호환성을 위한 생성자
     * 
     * @param cause 원인 예외
     */
    public BusinessException(Throwable cause) {
        super(cause);
    }
    
    /**
     * 기존 코드와의 호환성을 위한 생성자
     * 
     * @param message 오류 메시지
     * @param cause 원인 예외
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}