package com.sidebeam.bookmark.config;

import com.sidebeam.bookmark.config.property.ValidationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * 스키마 검증 관련 설정을 초기화하고 검증하는 Configuration 클래스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SchemaValidationConfig {

    private final ValidationProperties validationProperties;

    /**
     * 애플리케이션 시작 시 스키마 파일의 존재 여부를 확인합니다.
     * 설정된 스키마 경로가 유효하지 않은 경우 경고 로그를 출력합니다.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void validateSchemaConfiguration() {

        ValidationProperties.Schema schema = validationProperties.getSchema();

        if (!schema.isEnabled()) {
            log.info("Schema validation is disabled");
            return;
        }

        String schemaPath = schema.getBookmarkSchemaPath();
        ClassPathResource resource = new ClassPathResource(schemaPath);

        if (!resource.exists()) {
            log.error("Schema file not found at path: {}. Schema validation will fail!", schemaPath);
            if (schema.isStrict()) {
                throw new IllegalStateException("Schema file not found at path: " + schemaPath);
            }
        } else {
            log.info("Schema validation configuration validated successfully:");
            log.info("  - Schema path: {}", schemaPath);
            log.info("  - Validation enabled: {}", schema.isEnabled());
            log.info("  - Strict validation: {}", schema.isStrict());
        }
    }
}
