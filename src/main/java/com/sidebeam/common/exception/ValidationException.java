package com.sidebeam.common.exception;

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
    
    /**
     * ErrorCode를 사용하여 ValidationException을 생성합니다.
     * 
     * @param errorCode 오류 코드
     */
    public ValidationException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    /**
     * ErrorCode와 추가 메시지를 사용하여 ValidationException을 생성합니다.
     * 
     * @param errorCode 오류 코드
     * @param message 추가 오류 메시지
     */
    public ValidationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    /**
     * ErrorCode와 원인 예외를 사용하여 ValidationException을 생성합니다.
     * 
     * @param errorCode 오류 코드
     * @param cause 원인 예외
     */
    public ValidationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
    
    /**
     * ErrorCode, 추가 메시지, 원인 예외를 사용하여 ValidationException을 생성합니다.
     * 
     * @param errorCode 오류 코드
     * @param message 추가 오류 메시지
     * @param cause 원인 예외
     */
    public ValidationException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
    
    /**
     * 기존 코드와의 호환성을 위한 생성자
     * 
     * @param message 오류 메시지
     */
    public ValidationException(String message) {
        super(message);
    }
    
    /**
     * 기존 코드와의 호환성을 위한 생성자
     * 
     * @param cause 원인 예외
     */
    public ValidationException(Throwable cause) {
        super(cause);
    }
    
    /**
     * 기존 코드와의 호환성을 위한 생성자
     * 
     * @param message 오류 메시지
     * @param cause 원인 예외
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}