package com.sidebeam.bookmark.controller;

import com.sidebeam.external.gitlab.property.WebhookProperties;
import com.sidebeam.bookmark.service.BookmarkService;
import com.sidebeam.common.core.exception.BusinessException;
import com.sidebeam.common.core.exception.ErrorCode;
import com.sidebeam.common.core.exception.SystemException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * GitLab 웹훅을 수신하고 북마크 데이터를 갱신하는 컨트롤러입니다.
 * 웹훅 이벤트의 종류에 관계없이 북마크 데이터를 최신 상태로 유지하는 역할을 합니다.
 * 수신된 웹훅 페이로드를 기반으로 북마크 서비스를 호출하여 데이터 일관성을 확보합니다.
 */
@Slf4j
@RestController
@RequestMapping("/webhook")
@Tag(name = "Webhooks", description = "API for handling GitLab webhooks")
public class WebhookController {

    private final BookmarkService bookmarkService;
    private final WebhookProperties webhookProperties;

    public WebhookController(BookmarkService bookmarkService, WebhookProperties webhookProperties) {
        this.bookmarkService = bookmarkService;
        this.webhookProperties = webhookProperties;
    }

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
            @RequestHeader(value = "X-Gitlab-Token", required = false) String token) {

        // 토큰 검증
        if (webhookProperties.getSecretToken() != null && !webhookProperties.getSecretToken().isEmpty()
                && !webhookProperties.getSecretToken().equals(token)) {
                log.warn("Invalid webhook token received");
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid webhook token");
            }


        String event = payload.containsKey("event_name") ? payload.get("event_name").toString() : "unknown";
        log.info("Received GitLab webhook event: {}", event);

        try {
            bookmarkService.refreshBookmarks();
            return "Webhook processed successfully";
        } catch (Exception e) {
            log.error("Error processing webhook", e);
            throw new SystemException(ErrorCode.INTERNAL_SERVER_ERROR, "Error processing webhook: " + e.getMessage(), e);
        }
    }
}
