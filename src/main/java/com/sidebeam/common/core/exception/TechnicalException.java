package com.sidebeam.common.core.exception;

/**
 * 시스템 내부 코드 문제로 인해 발생하는 예외를 처리하기 위한 클래스입니다.
 * BusinessException을 상속하며, 다음과 같은 기술적 문제들을 처리합니다:
 * 
 * - NullPointerException 및 기타 런타임 예외
 * - 프로퍼티 변환 실패 (타입 변환, 파싱 오류 등)
 * - 인코딩/디코딩 실패
 * - 데이터 파싱 오류 (JSON, YAML, XML 등)
 * - 캐시 처리 오류
 * - 기타 내부 시스템 처리 오류
 */
public class TechnicalException extends ApplicationException {
    
    /**
     * ErrorCode를 사용하여 TechnicalException을 생성합니다.
     * 
     * @param errorCode 오류 코드
     */
    public TechnicalException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    /**
     * ErrorCode와 추가 메시지를 사용하여 TechnicalException을 생성합니다.
     * 
     * @param errorCode 오류 코드
     * @param message 추가 오류 메시지
     */
    public TechnicalException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    /**
     * ErrorCode와 원인 예외를 사용하여 TechnicalException을 생성합니다.
     * 
     * @param errorCode 오류 코드
     * @param cause 원인 예외
     */
    public TechnicalException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
    
    /**
     * ErrorCode, 추가 메시지, 원인 예외를 사용하여 TechnicalException을 생성합니다.
     * 
     * @param errorCode 오류 코드
     * @param message 추가 오류 메시지
     * @param cause 원인 예외
     */
    public TechnicalException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
    
    /**
     * 기존 코드와의 호환성을 위한 생성자
     * 
     * @param message 오류 메시지
     */
    public TechnicalException(String message) {
        super(message);
    }
    
    /**
     * 기존 코드와의 호환성을 위한 생성자
     * 
     * @param cause 원인 예외
     */
    public TechnicalException(Throwable cause) {
        super(cause);
    }
    
    /**
     * 기존 코드와의 호환성을 위한 생성자
     * 
     * @param message 오류 메시지
     * @param cause 원인 예외
     */
    public TechnicalException(String message, Throwable cause) {
        super(message, cause);
    }
}