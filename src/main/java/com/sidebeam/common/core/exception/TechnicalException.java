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

    public TechnicalException(ErrorCode errorCode) {
        super(errorCode);
    }

    public TechnicalException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public TechnicalException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public TechnicalException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public TechnicalException(String message) {
        super(message);
    }

    public TechnicalException(Throwable cause) {
        super(cause);
    }

    public TechnicalException(String message, Throwable cause) {
        super(message, cause);
    }
}