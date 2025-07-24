package com.sidebeam.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * API 응답을 위한 공통 응답 래퍼 클래스입니다.
 * 
 * GitHub의 Spring 기반 오픈소스 프로젝트들에서 널리 사용되는 구조를 기반으로 설계되었습니다.
 * 모든 REST API 응답을 일관된 형태로 반환하여 클라이언트에서 예측 가능한 응답 구조를 제공합니다.
 * 
 * 응답 구조:
 * - success: 요청 성공 여부 (true/false)
 * - code: 응답 코드 (HTTP 상태 코드 또는 비즈니스 코드)
 * - message: 응답 메시지 (성공/실패 메시지)
 * - data: 실제 응답 데이터 (제네릭 타입으로 다양한 데이터 타입 지원)
 * 
 * @param <T> 응답 데이터의 타입
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "API 공통 응답 형식")
public class ApiResponse<T> {

    @Schema(description = "요청 성공 여부", example = "true")
    private Boolean success;

    @Schema(description = "응답 코드", example = "200")
    private String code;

    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    private String message;

    @Schema(description = "응답 데이터")
    private T data;

    /**
     * 성공 응답을 생성합니다.
     * 
     * @param data 응답 데이터
     * @param <T> 데이터 타입
     * @return 성공 응답 객체
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "200", "요청이 성공적으로 처리되었습니다.", data);
    }

    /**
     * 성공 응답을 생성합니다. (커스텀 메시지)
     * 
     * @param data 응답 데이터
     * @param message 커스텀 메시지
     * @param <T> 데이터 타입
     * @return 성공 응답 객체
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, "200", message, data);
    }

    /**
     * 데이터 없는 성공 응답을 생성합니다.
     * 
     * @return 성공 응답 객체
     */
    public static ApiResponse<Void> success() {
        return new ApiResponse<>(true, "200", "요청이 성공적으로 처리되었습니다.", null);
    }

    /**
     * 데이터 없는 성공 응답을 생성합니다. (커스텀 메시지)
     * 
     * @param message 커스텀 메시지
     * @return 성공 응답 객체
     */
    public static ApiResponse<Void> successWithMessage(String message) {
        return new ApiResponse<>(true, "200", message, null);
    }

    /**
     * 실패 응답을 생성합니다.
     * 
     * @param code 에러 코드
     * @param message 에러 메시지
     * @return 실패 응답 객체
     */
    public static ApiResponse<Void> failure(String code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }

    /**
     * 실패 응답을 생성합니다. (데이터 포함)
     * 
     * @param code 에러 코드
     * @param message 에러 메시지
     * @param data 에러 관련 데이터
     * @param <T> 데이터 타입
     * @return 실패 응답 객체
     */
    public static <T> ApiResponse<T> failure(String code, String message, T data) {
        return new ApiResponse<>(false, code, message, data);
    }

    /**
     * 이미 ApiResponse로 래핑된 응답인지 확인합니다.
     * ResponseBodyAdvice에서 중복 래핑을 방지하기 위해 사용됩니다.
     * 
     * @param obj 확인할 객체
     * @return ApiResponse 타입이면 true, 아니면 false
     */
    public static boolean isApiResponse(Object obj) {
        return obj instanceof ApiResponse;
    }
}
