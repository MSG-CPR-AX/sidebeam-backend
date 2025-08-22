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
 * 예외 처리 시스템의 통합 테스트
 */
@ActiveProfiles("test")
@WebMvcTest(controllers = TestExceptionController.class)
@Import(ExceptionHandlingTest.TestConfig.class)
class ExceptionHandlingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    /**
     * ApplicationException 처리 테스트
     */
    @Test
    void testBusinessExceptionHandling() throws Exception {
        mockMvc.perform(get("/test/business-exception"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error.code").value("BOOKMARK_NOT_FOUND"))
                .andExpect(jsonPath("$.error.message").value("북마크를 찾을 수 없습니다."))
                .andExpect(jsonPath("$.error.details.path").value("/test/business-exception"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * SystemException 처리 테스트
     */
    @Test
    void testSystemExceptionHandling() throws Exception {
        mockMvc.perform(get("/test/system-exception"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error.code").value("GITLAB_CONNECTION_ERROR"))
                .andExpect(jsonPath("$.error.message").value("GitLab 연결 중 오류가 발생했습니다."))
                .andExpect(jsonPath("$.error.details.path").value("/test/system-exception"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * IllegalArgumentException 처리 테스트
     */
    @Test
    void testIllegalArgumentExceptionHandling() throws Exception {
        mockMvc.perform(get("/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.error.message").value("잘못된 요청입니다."))
                .andExpect(jsonPath("$.error.details.path").value("/test/illegal-argument"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * 일반 Exception 처리 테스트
     */
    @Test
    void testGenericExceptionHandling() throws Exception {
        mockMvc.perform(get("/test/generic-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error.code").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.error.message").value("내부 서버 오류가 발생했습니다."))
                .andExpect(jsonPath("$.error.details.path").value("/test/generic-exception"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * 파라미터 누락 테스트
     */
    @Test
    void testMissingParameterHandling() throws Exception {
        mockMvc.perform(get("/test/missing-param"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error.code").value("MISSING_REQUIRED_PARAMETER"))
                .andExpect(jsonPath("$.error.message").value("필수 파라미터가 누락되었습니다."))
                .andExpect(jsonPath("$.error.details.path").value("/test/missing-param"))
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
