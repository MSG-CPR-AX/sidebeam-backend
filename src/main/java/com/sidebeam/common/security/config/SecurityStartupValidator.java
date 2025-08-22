package com.sidebeam.common.security.config;

import com.sidebeam.common.security.config.property.SecurityProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.StringUtils;

/**
 * 운영 환경에서 API Key 설정 여부를 검증하는 가드레일 클래스입니다.
 *
 * <p>prod 프로파일에서 애플리케이션 기동 시 API Key 인증이 활성화되어 있지 않거나
 * 값이 비어있는 경우 즉시 예외를 발생시켜 애플리케이션이 시작되지 않도록 합니다.
 * 이는 안전하지 않은 상태로 운영 환경에 노출되는 것을 방지하기 위한 보호 장치입니다.</p>
 */
@Configuration
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
public class SecurityStartupValidator {

    private final SecurityProperties securityProperties;

    @PostConstruct
    public void validateApiKeyConfiguration() {
        var apiKeyProps = securityProperties.getApiKey();
        if (!apiKeyProps.isEnabled()) {
            // prod 환경에서는 API 키가 반드시 활성화되어야 한다
            String msg = "[Security] prod 환경에서 security.api-key.enabled=false 로 설정되어 있습니다. API 키를 활성화하십시오.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        if (!StringUtils.hasText(apiKeyProps.getValue())) {
            String msg = "[Security] prod 환경에서 API 키 값이 설정되지 않았습니다. SECURITY_API_KEY_VALUE 환경 변수를 설정하세요.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
    }
}
