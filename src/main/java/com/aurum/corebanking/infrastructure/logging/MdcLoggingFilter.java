package com.aurum.corebanking.infrastructure.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(1)
public class MdcLoggingFilter implements Filter {

    private static final String TRACE_HEADER   = "X-Trace-Id";
    private static final String REQUEST_HEADER = "X-Request-Id";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest  request  = (HttpServletRequest)  req;
        HttpServletResponse response = (HttpServletResponse) res;

        try {
            String traceId = request.getHeader(TRACE_HEADER);
            if (traceId == null || traceId.isBlank()) {
                traceId = UUID.randomUUID().toString()
                              .replace("-", "").substring(0, 16);
            }
            String requestId = UUID.randomUUID().toString()
                                   .replace("-", "").substring(0, 12);

            MDC.put("traceId",   traceId);
            MDC.put("requestId", requestId);
            MDC.put("method",    request.getMethod());
            MDC.put("path",      request.getRequestURI());
            MDC.put("clientIp",  getClientIp(request));

            response.setHeader(TRACE_HEADER,   traceId);
            response.setHeader(REQUEST_HEADER, requestId);

            injectUserId();

            long start = System.currentTimeMillis();
            chain.doFilter(req, res);
            long duration = System.currentTimeMillis() - start;

            MDC.put("durationMs", String.valueOf(duration));
            MDC.put("statusCode", String.valueOf(response.getStatus()));
            log.info("Request completed");

        } finally {
            MDC.clear(); // always clear — prevents thread-pool leaks
        }
    }

    private void injectUserId() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
                MDC.put("userId", jwt.getSubject());
            }
        } catch (Exception ignored) { }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded != null
            ? forwarded.split(",")[0].trim()
            : request.getRemoteAddr();
    }
}