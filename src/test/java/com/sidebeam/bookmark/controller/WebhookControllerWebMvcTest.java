package com.sidebeam.bookmark.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sidebeam.bookmark.service.BookmarkService;
import com.sidebeam.common.security.util.SignatureVerifyUtil;
import com.sidebeam.external.gitlab.config.property.WebhookProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = WebhookController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({WebhookProperties.class, com.sidebeam.common.core.exception.GlobalExceptionHandler.class})
@org.springframework.test.context.TestPropertySource(properties = "webhook.secret-token=secret")
class WebhookControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    BookmarkService bookmarkService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("Given invalid token when POST webhook then returns 401 unauthorized")
    void givenInvalidToken_whenPostWebhook_thenReturnsUnauthorized() throws Exception {
        // Given
        String body = objectMapper.writeValueAsString(Map.of("event_name", "push"));

        try (MockedStatic<SignatureVerifyUtil> mockedSignatureUtil = mockStatic(SignatureVerifyUtil.class)) {
            mockedSignatureUtil.when(() -> SignatureVerifyUtil.verifySignature(any(byte[].class), eq("wrong"), eq("secret")))
                    .thenReturn(false);

            // When / Then
            mockMvc.perform(post("/webhook/gitlab")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .header("X-Gitlab-Token", "wrong"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));

            verifyNoInteractions(bookmarkService);
        }
    }

    @Test
    @DisplayName("Given missing token when POST webhook then returns 401 unauthorized")
    void givenMissingToken_whenPostWebhook_thenReturnsUnauthorized() throws Exception {
        // Given
        String body = objectMapper.writeValueAsString(Map.of("event_name", "push"));

        try (MockedStatic<SignatureVerifyUtil> mockedSignatureUtil = mockStatic(SignatureVerifyUtil.class)) {
            mockedSignatureUtil.when(() -> SignatureVerifyUtil.verifySignature(any(byte[].class), isNull(), eq("secret")))
                    .thenReturn(false);

            // When / Then
            mockMvc.perform(post("/webhook/gitlab")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));

            verifyNoInteractions(bookmarkService);
        }
    }

    @Test
    @DisplayName("Given valid token when POST webhook then processes successfully")
    void givenValidToken_whenPostWebhook_thenProcessesSuccessfully() throws Exception {
        // Given
        doNothing().when(bookmarkService).refreshBookmarks();
        String body = objectMapper.writeValueAsString(Map.of("event_name", "push"));

        try (MockedStatic<SignatureVerifyUtil> mockedSignatureUtil = mockStatic(SignatureVerifyUtil.class)) {
            mockedSignatureUtil.when(() -> SignatureVerifyUtil.verifySignature(any(byte[].class), eq("secret"), eq("secret")))
                    .thenReturn(true);

            // When / Then
            mockMvc.perform(post("/webhook/gitlab")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .header("X-Gitlab-Token", "secret"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Webhook processed successfully"));

            verify(bookmarkService).refreshBookmarks();
        }
    }

    @Test
    @DisplayName("Given service exception when POST webhook then returns 500 internal server error")
    void givenServiceException_whenPostWebhook_thenReturnsInternalServerError() throws Exception {
        // Given
        doThrow(new RuntimeException("Service error")).when(bookmarkService).refreshBookmarks();
        String body = objectMapper.writeValueAsString(Map.of("event_name", "push"));

        try (MockedStatic<SignatureVerifyUtil> mockedSignatureUtil = mockStatic(SignatureVerifyUtil.class)) {
            mockedSignatureUtil.when(() -> SignatureVerifyUtil.verifySignature(any(byte[].class), eq("secret"), eq("secret")))
                    .thenReturn(true);

            // When / Then
            mockMvc.perform(post("/webhook/gitlab")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .header("X-Gitlab-Token", "secret"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error.code").value("INTERNAL_SERVER_ERROR"));

            verify(bookmarkService).refreshBookmarks();
        }
    }

    @Test
    @DisplayName("Given different event types when POST webhook then processes all successfully")
    void givenDifferentEventTypes_whenPostWebhook_thenProcessesAll() throws Exception {
        // Given
        doNothing().when(bookmarkService).refreshBookmarks();
        String[] events = {"push", "merge_request", "tag_push", "unknown"};

        try (MockedStatic<SignatureVerifyUtil> mockedSignatureUtil = mockStatic(SignatureVerifyUtil.class)) {
            mockedSignatureUtil.when(() -> SignatureVerifyUtil.verifySignature(any(byte[].class), eq("secret"), eq("secret")))
                    .thenReturn(true);

            for (String eventType : events) {
                // When / Then
                String body = objectMapper.writeValueAsString(Map.of("event_name", eventType));
                
                mockMvc.perform(post("/webhook/gitlab")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                                .header("X-Gitlab-Token", "secret"))
                        .andExpect(status().isOk())
                        .andExpect(content().string("Webhook processed successfully"));
            }

            verify(bookmarkService, times(events.length)).refreshBookmarks();
        }
    }
}
