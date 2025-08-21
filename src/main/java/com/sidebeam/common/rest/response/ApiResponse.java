package com.sidebeam.common.rest.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sidebeam.common.core.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;

/**
 * API 공통 응답(안 A형): 성공/실패를 단일 스키마로 반환합니다.
 * 성공: { success:true, data, error:null, timestamp }
 * 실패: { success:false, data:null, error:{code,message,details}, timestamp }
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"success", "data", "error", "timestamp"})
@Schema(description = "API 공통 응답 형식(단일 스키마)")
public class ApiResponse<T> {

    @Schema(description = "요청 성공 여부", example = "true")
    private Boolean success;

    @Schema(description = "응답 데이터")
    private T data;

    @Schema(description = "오류 정보(성공 시 null)")
    private ErrorInfo error;

    @Schema(description = "UTC ISO-8601(Z) 타임스탬프", example = "2025-01-01T00:00:00Z")
    private Instant timestamp;

    /**
     * 오류 상세 정보 - ErrorResponse 대신 인라인 클래스 사용
     */
    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "오류 상세 정보")
    public static class ErrorInfo {
        @Schema(description = "오류 코드", example = "VALIDATION_ERROR")
        private String code;

        @Schema(description = "사람 친화적 메시지", example = "Invalid request")
        private String message;

        @Schema(description = "추가 상세", example = "{\"fieldErrors\":[{\"field\":\"name\",\"reason\":\"must not be blank\"}]}")
        private Map<String, Object> details;

        public static ErrorInfo of(ErrorCode errorCode, String message, Map<String, Object> details) {
            String msg = (message != null && !message.isBlank()) ? message : errorCode.getMessage();
            return new ErrorInfo(errorCode.name(), msg, details);
        }

        public static ErrorInfo of(ErrorCode errorCode, Map<String, Object> details) {
            return of(errorCode, null, details);
        }

        public static ErrorInfo of(ErrorCode errorCode) {
            return of(errorCode, null, null);
        }
    }

    // ---------- Factory methods ----------

    public static <T> ApiResponse<T> ok(T data) {
        return ok(data, Clock.systemUTC());
    }

    public static ApiResponse<Void> ok() {
        return ok(null, Clock.systemUTC());
    }

    public static <T> ApiResponse<T> ok(T data, Clock clock) {
        return new ApiResponse<>(true, data, null, Instant.now(clock));
    }

    public static ApiResponse<Void> error(ErrorInfo error) {
        return error(error, Clock.systemUTC());
    }

    public static ApiResponse<Void> error(ErrorCode code, String message, Map<String, Object> details) {
        return error(ErrorInfo.of(code, message, details));
    }

    public static ApiResponse<Void> error(ErrorInfo error, Clock clock) {
        return new ApiResponse<>(false, null, error, Instant.now(clock));
    }

    /**
     * 이미 ApiResponse로 래핑된 응답인지 확인합니다.
     * ResponseBodyAdvice에서 중복 래핑을 방지하기 위해 사용됩니다.
     */
    public static boolean isApiResponse(Object obj) {
        return obj instanceof ApiResponse;
    }
}
