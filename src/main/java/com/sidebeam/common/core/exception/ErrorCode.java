package com.sidebeam.common.core.exception;

import org.springframework.http.HttpStatus;

/**
 * 애플리케이션에서 발생할 수 있는 오류 코드와 메시지를 정의하는 enum입니다.
 * 각 오류 코드는 고유한 코드, 메시지, HTTP 상태 코드를 포함합니다.
 */
public enum ErrorCode {

    // 일반적인 시스템 오류
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "내부 서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REQUEST("INVALID_REQUEST", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "요청한 리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // 북마크 관련 오류
    BOOKMARK_NOT_FOUND("BOOKMARK_NOT_FOUND", "북마크를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    BOOKMARK_PARSING_ERROR("BOOKMARK_PARSING_ERROR", "북마크 데이터 파싱 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    BOOKMARK_VALIDATION_ERROR("BOOKMARK_VALIDATION_ERROR", "북마크 데이터 유효성 검사에 실패했습니다.", HttpStatus.BAD_REQUEST),

    // GitLab 관련 오류
    GITLAB_CONNECTION_ERROR("GITLAB_CONNECTION_ERROR", "GitLab 연결 중 오류가 발생했습니다.", HttpStatus.SERVICE_UNAVAILABLE),
    GITLAB_API_ERROR("GITLAB_API_ERROR", "GitLab API 호출 중 오류가 발생했습니다.", HttpStatus.BAD_GATEWAY),
    GITLAB_FILE_NOT_FOUND("GITLAB_FILE_NOT_FOUND", "GitLab에서 파일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // 스키마 검증 오류
    SCHEMA_VALIDATION_ERROR("SCHEMA_VALIDATION_ERROR", "스키마 유효성 검사에 실패했습니다.", HttpStatus.UNPROCESSABLE_ENTITY),

    // 캐시 관련 오류
    CACHE_ERROR("CACHE_ERROR", "캐시 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // 기술적 오류 (TechnicalException용)
    PROPERTY_CONVERSION_ERROR("PROPERTY_CONVERSION_ERROR", "프로퍼티 변환 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ENCODING_DECODING_ERROR("ENCODING_DECODING_ERROR", "인코딩/디코딩 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    DATA_PARSING_ERROR("DATA_PARSING_ERROR", "데이터 파싱 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    NULL_POINTER_ERROR("NULL_POINTER_ERROR", "예상치 못한 null 값이 발견되었습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    TYPE_CONVERSION_ERROR("TYPE_CONVERSION_ERROR", "타입 변환 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // 도메인 오류 (DomainException용)
    BUSINESS_RULE_VIOLATION("BUSINESS_RULE_VIOLATION", "비즈니스 규칙 위반입니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_RESOURCE("DUPLICATE_RESOURCE", "중복된 리소스입니다.", HttpStatus.CONFLICT),
    RESOURCE_STATE_INVALID("RESOURCE_STATE_INVALID", "리소스 상태가 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
    DOMAIN_CONSTRAINT_VIOLATION("DOMAIN_CONSTRAINT_VIOLATION", "도메인 제약 조건 위반입니다.", HttpStatus.BAD_REQUEST),

    // 입력값 유효성 검사 오류 (ValidationException용)
    VALIDATION_ERROR("VALIDATION_ERROR", "입력값 유효성 검사에 실패했습니다.", HttpStatus.BAD_REQUEST),
    MISSING_REQUIRED_PARAMETER("MISSING_REQUIRED_PARAMETER", "필수 파라미터가 누락되었습니다.", HttpStatus.BAD_REQUEST),
    INVALID_PARAMETER_FORMAT("INVALID_PARAMETER_FORMAT", "파라미터 형식이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    PARAMETER_OUT_OF_RANGE("PARAMETER_OUT_OF_RANGE", "파라미터 값이 허용 범위를 벗어났습니다.", HttpStatus.BAD_REQUEST),

    // 인증/권한 관련 오류 (향후 확장용)
    UNAUTHORIZED("UNAUTHORIZED", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("FORBIDDEN", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // 외부 서비스 오류
    EXTERNAL_SERVICE_ERROR("EXTERNAL_SERVICE_ERROR", "외부 서비스 호출 중 오류가 발생했습니다.", HttpStatus.BAD_GATEWAY),
    SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", "서비스를 사용할 수 없습니다.", HttpStatus.SERVICE_UNAVAILABLE);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public int getStatus() {
        return httpStatus.value();
    }
}
