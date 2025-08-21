package com.sidebeam.common.web.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sidebeam.common.security.config.property.SecurityProperties;
import com.sidebeam.common.core.exception.ErrorCode;
import com.sidebeam.common.rest.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final SecurityProperties securityProperties;
    private final ObjectMapper objectMapper;

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    private static final List<String> DEFAULT_EXCLUDES = List.of(
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs/**",
            "/actuator/health",
            "/actuator/info",
            "/webhook/**"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var props = securityProperties.getApiKey();
        if (!props.isEnabled() || isExcluded(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String headerName = props.getHeaderName();
        String expected = props.getValue();
        String provided = request.getHeader(headerName);

        if (!StringUtils.hasText(expected)) {
            log.warn("API key security enabled but no expected value configured. Denying request to {} {}", request.getMethod(), request.getRequestURI());
            writeUnauthorized(response, request.getRequestURI(), "API key not configured");
            return;
        }

        if (!StringUtils.hasText(provided) || !expected.equals(provided)) {
            log.warn("Unauthorized access attempt to {} {} from {} - missing/invalid API key", request.getMethod(), request.getRequestURI(), request.getRemoteAddr());
            writeUnauthorized(response, request.getRequestURI(), "Invalid API key");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isExcluded(HttpServletRequest request) {
        String uri = request.getRequestURI();
        List<String> patterns = new ArrayList<>(DEFAULT_EXCLUDES);
        String extra = securityProperties.getApiKey().getExcludePatterns();
        if (StringUtils.hasText(extra)) {
            patterns.addAll(Arrays.stream(extra.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList());
        }
        return patterns.stream().anyMatch(p -> MATCHER.match(p, uri));
    }

    private void writeUnauthorized(HttpServletResponse response, String path, String details) throws IOException {
        var detailMap = java.util.Map.<String, Object>of(
                "path", path,
                "hint", details
        );
        ApiResponse.ErrorInfo err = ApiResponse.ErrorInfo.of(ErrorCode.UNAUTHORIZED, null, detailMap);
        response.setStatus(ErrorCode.UNAUTHORIZED.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(err));
    }
}
