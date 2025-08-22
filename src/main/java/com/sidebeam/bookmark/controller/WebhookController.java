package com.sidebeam.bookmark.controller;

import com.sidebeam.bookmark.service.BookmarkService;
import com.sidebeam.common.core.exception.BusinessException;
import com.sidebeam.common.core.exception.ErrorCode;
import com.sidebeam.common.core.exception.SystemException;
import com.sidebeam.external.gitlab.config.property.WebhookProperties;
import com.sidebeam.common.security.util.SignatureVerifyUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

/**
 * GitLab 웹훅을 수신하고 북마크 데이터를 갱신하는 컨트롤러입니다.
 * 웹훅 이벤트의 종류에 관계없이 북마크 데이터를 최신 상태로 유지하는 역할을 합니다.
 * 수신된 웹훅 페이로드를 기반으로 북마크 서비스를 호출하여 데이터 일관성을 확보합니다.
 */
@Slf4j
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "API for handling GitLab webhooks")
public class WebhookController {

    private final BookmarkService bookmarkService;
    private final WebhookProperties webhookProperties;

    /**
     * GitLab 웹훅을 처리하고 북마크 데이터를 갱신합니다.
     * 수신된 GitLab 웹훅 이벤트의 종류에 관계없이 북마크 데이터를 갱신하는 역할을 합니다.
     * GlobalResponseBodyAdvice에 의해 자동으로 ApiResponse로 래핑됩니다.
     * 오류 발생 시 적절한 예외를 던져 GlobalExceptionHandler가 처리하도록 합니다.
     */
    @PostMapping("/gitlab")
    @Operation(summary = "Handle GitLab webhook", description = "Processes GitLab webhook events and refreshes bookmark data")
    public String handleGitLabWebhook(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "X-Gitlab-Token", required = false) String token,
            HttpServletRequest request) {

        // 서명 검증 (HMAC-SHA256) - 더 안전한 방식
        try {
            byte[] requestBody = request.getInputStream().readAllBytes();
            if (!SignatureVerifyUtil.verifySignature(requestBody, token, webhookProperties.getSecretToken())) {
                log.warn("Webhook signature verification failed from IP: {}", request.getRemoteAddr());
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid webhook signature");
            }
        } catch (IOException e) {
            log.error("Error reading request body for signature verification", e);
            throw new SystemException(ErrorCode.INTERNAL_SERVER_ERROR, "Error processing webhook request", e);
        }

        String event = payload.containsKey("event_name") ? payload.get("event_name").toString() : "unknown";
        log.info("Received GitLab webhook event: {} from IP: {}", event, request.getRemoteAddr());

        try {
            bookmarkService.refreshBookmarks();
            log.info("Webhook processed successfully for event: {}", event);
            return "Webhook processed successfully";
        } catch (Exception e) {
            log.error("Error processing webhook for event: {}", event, e);
            throw new SystemException(ErrorCode.INTERNAL_SERVER_ERROR, "Error processing webhook: " + e.getMessage(), e);
        }
    }
}
