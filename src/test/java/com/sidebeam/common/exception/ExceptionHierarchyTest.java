package com.sidebeam.common.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sidebeam.common.core.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 새로운 예외 계층 구조의 통합 테스트
 */
@ActiveProfiles("test")
@WebMvcTest(controllers = ExceptionHierarchyTestController.class)
@Import(ExceptionHierarchyTest.TestConfig.class)
class ExceptionHierarchyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * TechnicalException 처리 테스트
     */
    @Test
    void testTechnicalExceptionHandling() throws Exception {
        mockMvc.perform(get("/test/technical-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error.code").value("PROPERTY_CONVERSION_ERROR"))
                .andExpect(jsonPath("$.error.message").value("프로퍼티 변환 중 오류가 발생했습니다."))
                .andExpect(jsonPath("$.error.details.path").value("/test/technical-exception"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * BusinessException 처리 테스트
     */
    @Test
    void testDomainExceptionHandling() throws Exception {
        mockMvc.perform(get("/test/domain-exception"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error.code").value("BOOKMARK_NOT_FOUND"))
                .andExpect(jsonPath("$.error.message").value("북마크를 찾을 수 없습니다."))
                .andExpect(jsonPath("$.error.details.path").value("/test/domain-exception"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * ValidationException 처리 테스트
     */
    @Test
    void testValidationExceptionHandling() throws Exception {
        mockMvc.perform(get("/test/validation-exception"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error.code").value("INVALID_PARAMETER_FORMAT"))
                .andExpect(jsonPath("$.error.message").value("파라미터 형식이 올바르지 않습니다."))
                .andExpect(jsonPath("$.error.details.path").value("/test/validation-exception"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * TechnicalException with custom message 테스트
     */
    @Test
    void testTechnicalExceptionWithCustomMessage() throws Exception {
        mockMvc.perform(get("/test/technical-exception-custom"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error.code").value("DATA_PARSING_ERROR"))
                .andExpect(jsonPath("$.error.details.path").value("/test/technical-exception-custom"))
                .andExpect(jsonPath("$.error.details.hint").value("Custom parsing error message"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * BusinessException with business rule violation 테스트
     */
    @Test
    void testDomainExceptionBusinessRule() throws Exception {
        mockMvc.perform(get("/test/domain-exception-business-rule"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error.code").value("BUSINESS_RULE_VIOLATION"))
                .andExpect(jsonPath("$.error.message").value("비즈니스 규칙 위반입니다."))
                .andExpect(jsonPath("$.error.details.path").value("/test/domain-exception-business-rule"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * ValidationException with parameter out of range 테스트
     */
    @Test
    void testValidationExceptionParameterRange() throws Exception {
        mockMvc.perform(get("/test/validation-exception-range"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error.code").value("PARAMETER_OUT_OF_RANGE"))
                .andExpect(jsonPath("$.error.message").value("파라미터 값이 허용 범위를 벗어났습니다."))
                .andExpect(jsonPath("$.error.details.path").value("/test/validation-exception-range"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * 기존 ApplicationException 호환성 테스트 (하위 호환성 확인)
     */
    @Test
    void testBusinessExceptionCompatibility() throws Exception {
        mockMvc.perform(get("/test/business-exception-compatibility"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error.code").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.error.message").value("내부 서버 오류가 발생했습니다."))
                .andExpect(jsonPath("$.error.details.path").value("/test/business-exception-compatibility"))
                .andExpect(jsonPath("$.timestamp").exists());
    }


    @TestConfiguration
    static class TestConfig {
        @Bean
        public GlobalExceptionHandler globalExceptionHandler() {
            return new GlobalExceptionHandler();
        }
    }
}
