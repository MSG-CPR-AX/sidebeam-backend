package com.sidebeam.common.response;

import com.sidebeam.common.controller.TestController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ApiResponse와 GlobalResponseAdvice의 통합 테스트
 */
@ActiveProfiles("test")
@WebMvcTest(TestController.class)
class ApiResponseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testApiResponseWrapping() throws Exception {
        // 문자열 응답 테스트 - 문자열은 래핑되지 않음
        mockMvc.perform(get("/test/string"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string("Hello, World!"));
    }

    @Test
    void testObjectResponseWrapping() throws Exception {
        // 객체 응답 테스트 - 객체는 ApiResponse로 래핑됨
        mockMvc.perform(get("/test/object"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Object"))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    void testApiResponseNotDoubleWrapped() throws Exception {
        // 이미 ApiResponse로 감싸진 응답 테스트 - 재래핑되지 않음
        mockMvc.perform(get("/test/api-response"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.data").value("Already wrapped response"));
    }
}
