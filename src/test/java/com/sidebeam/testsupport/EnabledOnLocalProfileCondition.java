package com.sidebeam.testsupport;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Arrays;

/**
 * spring.profiles.active 시스템 프로퍼티 또는 SPRING_PROFILES_ACTIVE 환경변수에
 * 'local'이 포함되어 있는지 검사하여 테스트 활성화 여부를 결정합니다.
 * 조건을 만족하지 않으면 테스트가 비활성화되어 스프링 컨텍스트가 기동되지 않습니다.
 */
public class EnabledOnLocalProfileCondition implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        String sysProp = System.getProperty("spring.profiles.active");
        String envVar = System.getenv("SPRING_PROFILES_ACTIVE");
        String value = isBlank(sysProp) ? envVar : sysProp;

        if (!isBlank(value)) {
            boolean containsLocal = Arrays.stream(value.split(","))
                    .map(String::trim)
                    .anyMatch(p -> p.equalsIgnoreCase("local"));
            if (containsLocal) {
                return ConditionEvaluationResult.enabled("Active profiles contain 'local'");
            }
        }
        return ConditionEvaluationResult.disabled("Active profile is not 'local'");
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
