package com.sidebeam.bookmark.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 검증 관련 설정을 관리하는 Properties 클래스
 * application.yml에서 validation 관련 설정값들을 바인딩하여 관리합니다.
 */
@Data
@Component
@ConfigurationProperties(prefix = "validation")
public class ValidationProperties {

    /**
     * 스키마 검증 관련 설정
     */
    private Schema schema = new Schema();

    /**
     * 중복 검증 관련 설정
     */
    private Duplicate duplicate = new Duplicate();

    /**
     * 스키마 검증 설정 클래스
     */
    @Data
    public static class Schema {
        /**
         * JSON 스키마 파일의 경로
         * 기본값: bookmark-schema/bookmark.schema.json
         */
        private String bookmarkSchemaPath = "bookmark-schema/bookmark.schema.json";

        /**
         * 스키마 검증 활성화 여부
         * 기본값: true
         */
        private boolean enabled = true;

        /**
         * 스키마 검증 실패 시 예외 발생 여부
         * false로 설정하면 검증 실패 시에도 로그만 출력하고 계속 진행
         * 기본값: false (로그만 출력)
         */
        private boolean strict = false;
    }

    /**
     * 중복 검증 설정 클래스
     */
    @Data
    public static class Duplicate {
        /**
         * 중복 URL 검증 활성화 여부
         * 기본값: true
         */
        private boolean enabled = true;

        /**
         * 중복 검증 실패 시 예외 발생 여부
         * false로 설정하면 검증 실패 시에도 로그만 출력하고 계속 진행
         * 기본값: true (예외 발생)
         */
        private boolean strict = true;
    }
}