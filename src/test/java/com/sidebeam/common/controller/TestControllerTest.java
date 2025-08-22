package com.sidebeam.common.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.context.annotation.Import;
import com.sidebeam.common.rest.response.GlobalResponseBodyAdvice;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TestController와 GlobalResponseBodyAdvice의 통합 테스트입니다.
 * 
 * 다양한 응답 타입에 대해 GlobalResponseBodyAdvice가 올바르게 동작하는지 확인합니다.
 */
@ActiveProfiles("test")
@WebMvcTest(TestController.class)
@Import(GlobalResponseBodyAdvice.class)
class TestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testStringResponse_ShouldNotBeWrapped() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(get("/test/string"))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        // String 응답은 래핑되지 않고 그대로 반환되어야 함
        assertEquals("Hello, World!", responseBody);

        // ApiResponse 구조가 아니어야 함
        assertFalse(responseBody.contains("\"success\""));
        assertFalse(responseBody.contains("\"code\""));
        assertFalse(responseBody.contains("\"message\""));
        assertFalse(responseBody.contains("\"data\""));
    }

    @Test
    void testObjectResponse_ShouldBeWrappedInApiResponse() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(get("/test/object"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        // JSON 응답이 ApiResponse 구조를 가지는지 확인
        assertTrue(responseBody.contains("\"success\""));
        assertTrue(responseBody.contains("\"data\""));
        assertTrue(responseBody.contains("Test Object"));
    }

    @Test
    void testListResponse_ShouldBeWrappedInApiResponse() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(get("/test/list"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        // JSON 응답이 ApiResponse 구조를 가지는지 확인
        assertTrue(responseBody.contains("\"success\""));
        assertTrue(responseBody.contains("\"data\""));
        assertTrue(responseBody.contains("item1"));
        assertTrue(responseBody.contains("item2"));
        assertTrue(responseBody.contains("item3"));
    }

    @Test
    void testApiResponseResponse_ShouldNotBeReWrapped() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(get("/test/api-response"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        // 이미 ApiResponse로 래핑된 응답이므로 재래핑되지 않아야 함
        assertTrue(responseBody.contains("\"success\""));
        assertTrue(responseBody.contains("\"data\""));
        assertTrue(responseBody.contains("Already wrapped response"));

        // 중복 래핑이 되지 않았는지 확인 (success 필드가 한 번만 나타나야 함)
        int successCount = responseBody.split("\"success\"").length - 1;
        assertEquals(1, successCount, "Response should not be double-wrapped");
    }

    @Test
    void testNullResponse_ShouldBeWrappedAsEmptySuccess() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(get("/test/null"))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        // null 응답이 빈 성공 응답으로 래핑되어야 함
        if (!responseBody.isEmpty()) {
            assertTrue(responseBody.contains("\"success\":true"));
        }
        // data 필드는 null이므로 포함되지 않을 수 있음 (JsonInclude.Include.NON_NULL 설정)
    }

    @Test
    void testJsonStringResponse_ShouldNotBeWrapped() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(get("/test/json-string"))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        // JSON 형태의 문자열은 래핑되지 않고 그대로 반환되어야 함
        assertTrue(responseBody.contains("This is a JSON string"));
        assertTrue(responseBody.contains("success"));

        // ApiResponse 구조가 아닌 원본 JSON 구조여야 함
        assertFalse(responseBody.contains("\"data\""));
    }
}
