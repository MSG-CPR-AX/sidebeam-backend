package com.sidebeam.common.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 예외 처리 시스템의 통합 테스트
 */
@WebMvcTest(controllers = {ExceptionHandlingTest.TestController.class, GlobalExceptionHandler.class})
public class ExceptionHandlingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * ApplicationException 처리 테스트
     */
    @Test
    public void testBusinessExceptionHandling() throws Exception {
        mockMvc.perform(get("/test/business-exception"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("BOOKMARK_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("북마크를 찾을 수 없습니다."))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/test/business-exception"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * SystemException 처리 테스트
     */
    @Test
    public void testSystemExceptionHandling() throws Exception {
        mockMvc.perform(get("/test/system-exception"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("GITLAB_CONNECTION_ERROR"))
                .andExpect(jsonPath("$.message").value("GitLab 연결 중 오류가 발생했습니다."))
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.path").value("/test/system-exception"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * IllegalArgumentException 처리 테스트
     */
    @Test
    public void testIllegalArgumentExceptionHandling() throws Exception {
        mockMvc.perform(get("/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/test/illegal-argument"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * 일반 Exception 처리 테스트
     */
    @Test
    public void testGenericExceptionHandling() throws Exception {
        mockMvc.perform(get("/test/generic-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("내부 서버 오류가 발생했습니다."))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.path").value("/test/generic-exception"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * 파라미터 누락 테스트
     */
    @Test
    public void testMissingParameterHandling() throws Exception {
        mockMvc.perform(get("/test/missing-param"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("MISSING_REQUIRED_PARAMETER"))
                .andExpect(jsonPath("$.message").value("필수 파라미터가 누락되었습니다."))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/test/missing-param"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * 테스트용 컨트롤러
     */
    @RestController
    public static class TestController {

        @GetMapping("/test/business-exception")
        public String testBusinessException() {
            throw new BusinessException(ErrorCode.BOOKMARK_NOT_FOUND);
        }

        @GetMapping("/test/system-exception")
        public String testSystemException() {
            throw new SystemException(ErrorCode.GITLAB_CONNECTION_ERROR);
        }

        @GetMapping("/test/illegal-argument")
        public String testIllegalArgumentException() {
            throw new IllegalArgumentException("Invalid argument provided");
        }

        @GetMapping("/test/generic-exception")
        public String testGenericException() {
            throw new RuntimeException("Generic runtime exception");
        }

        @GetMapping("/test/missing-param")
        public String testMissingParameter(@RequestParam String requiredParam) {
            return "Success";
        }
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public GlobalExceptionHandler globalExceptionHandler() {
            return new GlobalExceptionHandler();
        }
    }
}
