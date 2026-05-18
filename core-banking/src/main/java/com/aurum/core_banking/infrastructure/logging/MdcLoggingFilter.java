package com.aurum.core_banking.infrastructure.logging;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Injects correlation IDs into MDC so every log line carries:
 * traceId, requestId, userId, method, path, clientIp.
 *
 * Example JSON log output:
 * {
 *   "timestamp": "2024-11-15T14:23:01Z",
 *   "level": "INFO",
 *   "traceId": "abc1234567890abc",
 *   "userId": "usr_keycloak_sub",
 *   "requestId": "req12345678",
 *   "method": "POST",
 *   "path": "/api/v1/transfers",
 *   "durationMs": "42",
 *   "statusCode": "200",
 *   "message": "Request completed"
 * }
 */
@Slf4j
@Component
@Order(1)   // run first — before security filters log anything
public class MdcLoggingFilter implements Filter {

    private static final String TRACE_HEADER   = "X-Trace-Id";
    private static final String REQUEST_HEADER = "X-Request-Id";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest  request  = (HttpServletRequest)  req;
        HttpServletResponse response = (HttpServletResponse) res;

        try {
            // Use incoming trace ID from API gateway or generate a new one
            String traceId = request.getHeader(TRACE_HEADER);
            if (traceId == null || traceId.isBlank()) {
                traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            }
            String requestId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

            MDC.put("traceId",   traceId);
            MDC.put("requestId", requestId);
            MDC.put("method",    request.getMethod());
            MDC.put("path",      request.getRequestURI());
            MDC.put("clientIp",  getClientIp(request));

            // Propagate trace ID back to the caller
            response.setHeader(TRACE_HEADER,   traceId);
            response.setHeader(REQUEST_HEADER, requestId);

            // Inject authenticated user ID if available
            injectUserId();

            long start = System.currentTimeMillis();
            chain.doFilter(req, res);
            long duration = System.currentTimeMillis() - start;

            MDC.put("durationMs", String.valueOf(duration));
            MDC.put("statusCode", String.valueOf(response.getStatus()));
            log.info("Request completed");

        } finally {
            // CRITICAL: always clear MDC to prevent thread-pool context leaks
            MDC.clear();
        }
    }

    private void injectUserId() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
                MDC.put("userId", jwt.getSubject());
            }
        } catch (Exception ignored) {
            // Security context not yet populated — OK for public endpoints
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
