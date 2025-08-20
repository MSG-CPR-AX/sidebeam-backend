package com.sidebeam.common.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * Populates MDC with request-scoped identifiers and propagates them via response headers.
 * - correlationId: preferred for cross-service tracing
 * - requestId: per-request id for this service
 *
 * Header names used:
 *  - X-Correlation-Id
 *  - X-Request-Id
 */
@Slf4j
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String HEADER_CORRELATION_ID = "X-Correlation-Id";
    public static final String HEADER_REQUEST_ID = "X-Request-Id";

    public static final String MDC_CORRELATION_ID = "correlationId";
    public static final String MDC_REQUEST_ID = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String correlationId = Optional.ofNullable(request.getHeader(HEADER_CORRELATION_ID))
                .filter(s -> !s.isBlank())
                .orElse(UUID.randomUUID().toString());
        String requestId = Optional.ofNullable(request.getHeader(HEADER_REQUEST_ID))
                .filter(s -> !s.isBlank())
                .orElse(UUID.randomUUID().toString());

        MDC.put(MDC_CORRELATION_ID, correlationId);
        MDC.put(MDC_REQUEST_ID, requestId);

        // Reflect back to response headers for client visibility
        response.setHeader(HEADER_CORRELATION_ID, correlationId);
        response.setHeader(HEADER_REQUEST_ID, requestId);

        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long latency = System.currentTimeMillis() - start;
            // Safe structured logging without sensitive data
            log.debug("Request completed: method={}, path={}, status={}, latencyMs={}, correlationId={}, requestId={}",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), latency, correlationId, requestId);
            MDC.remove(MDC_CORRELATION_ID);
            MDC.remove(MDC_REQUEST_ID);
        }
    }
}
