package com.sidebeam.common.security.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    private final ApiKey apiKey = new ApiKey();

    @Data
    public static class ApiKey {
        // 허용 목록(whitelist)에 포함되지 않은 엔드포인트에 대해 API 키 인증을 활성화합니다.
        private boolean enabled = false;
        // API 키를 읽어올 HTTP 헤더 이름
        private String headerName = "X-Api-Key";
        // 기대하는 API 키 값 (환경 변수/시크릿으로 제공되어야 함)
        private String value = "";
        // API 키 검사를 제외할 쉼표(,) 구분 Ant 패턴들 (선택 사항, 기본값에 추가)
        private String excludePatterns = "";
    }
}
